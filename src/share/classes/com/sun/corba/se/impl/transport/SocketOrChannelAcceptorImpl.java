/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1996-2007 Sun Microsystems, Inc. All rights reserved.
 *
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common Development
 * and Distribution License("CDDL") (collectively, the "License").  You
 * may not use this file except in compliance with the License. You can obtain
 * a copy of the License at https://glassfish.dev.java.net/public/CDDL+GPL.html
 * or glassfish/bootstrap/legal/LICENSE.txt.  See the License for the specific
 * language governing permissions and limitations under the License.
 *
 * When distributing the software, include this License Header Notice in each
 * file and include the License file at glassfish/bootstrap/legal/LICENSE.txt.
 * Sun designates this particular file as subject to the "Classpath" exception
 * as provided by Sun in the GPL Version 2 section of the License file that
 * accompanied this code.  If applicable, add the following below the License
 * Header, with the fields enclosed by brackets [] replaced by your own
 * identifying information: "Portions Copyrighted [year]
 * [name of copyright owner]"
 *
 * Contributor(s):
 *
 * If you wish your version of this file to be governed by only the CDDL or
 * only the GPL Version 2, indicate your decision by adding "[Contributor]
 * elects to include this software in this distribution under the [CDDL or GPL
 * Version 2] license."  If you don't indicate a single choice of license, a
 * recipient has the option to distribute your version of this file under
 * either the CDDL, the GPL Version 2 or to extend the choice of license to
 * its licensees as provided above.  However, if you add GPL Version 2 code
 * and therefore, elected the GPL Version 2 license, then the option applies
 * only if the new code is made subject to such option by the copyright
 * holder.
 */

package com.sun.corba.se.impl.transport;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.channels.SelectableChannel;
import java.nio.channels.SelectionKey;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.security.AccessController;
import java.security.PrivilegedAction;


import com.sun.corba.se.spi.transport.EventHandler;
import com.sun.corba.se.spi.transport.Selector;

import com.sun.corba.se.spi.extension.RequestPartitioningPolicy;
import com.sun.corba.se.spi.ior.IORTemplate;
import com.sun.corba.se.spi.ior.TaggedProfileTemplate;
import com.sun.corba.se.spi.ior.iiop.IIOPAddress ;
import com.sun.corba.se.spi.ior.iiop.IIOPFactories;
import com.sun.corba.se.spi.ior.iiop.IIOPProfileTemplate ;
import com.sun.corba.se.spi.ior.iiop.GIOPVersion ;
import com.sun.corba.se.spi.ior.iiop.AlternateIIOPAddressComponent;
import com.sun.corba.se.spi.orb.ORB;
import com.sun.corba.se.spi.transport.CorbaConnection;

import com.sun.corba.se.impl.orbutil.ORBUtility;

import com.sun.corba.se.spi.transport.CorbaContactInfo;
import com.sun.corba.se.spi.transport.CorbaInboundConnectionCache;
/**
 * @author Harold Carr
 */
public class SocketOrChannelAcceptorImpl
    extends
        SocketOrChannelAcceptorBase
{
    protected ServerSocketChannel serverSocketChannel;
    protected ServerSocket serverSocket;
    
    private Class<?> lastExceptionClassSeen = null ;

    public SocketOrChannelAcceptorImpl(ORB orb)
    {
        super( orb ) ;
    }

    public SocketOrChannelAcceptorImpl(ORB orb, int port)
    {
        super( orb, port ) ;
    }

    // BEGIN Legacy support.
    public SocketOrChannelAcceptorImpl(ORB orb, int port, 
				       String name, String type)
    {
        super( orb, port, name, type ) ;
    }
    // END Legacy support.

    public synchronized boolean initialize()
    {
	if (initialized) {
	    return false;
	}
	if (orb.transportDebugFlag) {
	    dprint(".initialize: " + this);
	}
	InetSocketAddress inetSocketAddress = null;
	String host = "all interfaces";
	try {
	    if (orb.getORBData().getListenOnAllInterfaces()) {
		inetSocketAddress = new InetSocketAddress(port);
	    } else {
		host = orb.getORBData().getORBServerHost();
		inetSocketAddress = new InetSocketAddress(host, port);
	    }
	    serverSocket = orb.getORBData().getSocketFactory()
		.createServerSocket(type, inetSocketAddress);
	    internalInitialize();
	    if (orb.getORBData().showInfoMessages()) {
		wrapper.infoCreateListenerSucceeded(host, Integer.toString(port));
	    }
	} catch (Throwable t) {
	    throw wrapper.createListenerFailed(t, host, Integer.toString(port));
	}
	initialized = true;
	return true;
    }

    protected void internalInitialize()
	throws Exception
    {
	// Determine the listening port (for the IOR).
	// This is important when using emphemeral ports (i.e.,
	// when the port value to the constructor is 0).

	port = serverSocket.getLocalPort();

	// Register with transport (also sets up monitoring).

	orb.getCorbaTransportManager().getInboundConnectionCache(this);

	// Finish configuation.

	serverSocketChannel = serverSocket.getChannel();

	if (serverSocketChannel != null) {
	    setUseSelectThreadToWait(
	        orb.getORBData().acceptorSocketUseSelectThreadToWait());
	    serverSocketChannel.configureBlocking(
	        ! orb.getORBData().acceptorSocketUseSelectThreadToWait());
	} else {
	    // Configure to use listener and reader threads.
	    setUseSelectThreadToWait(false);
	}
	setUseWorkerThreadForEvent(
            orb.getORBData().acceptorSocketUseWorkerThreadForEvent());

    }

    public void accept()
    {
	SocketChannel socketChannel = null;
	Socket socket = null;

	try {
	    if (serverSocketChannel == null) {
		socket = serverSocket.accept();
	    } else {
		socketChannel = serverSocketChannel.accept();
		socket = socketChannel.socket();
	    }

	    orb.getORBData().getSocketFactory()
		.setAcceptedSocketOptions(this, serverSocket, socket);

	    // Clear the last exception after a successful accept, in case
	    // we get sporadic bursts of related failures.
	    lastExceptionClassSeen = null ;
	} catch (IOException e) {
	    // Log the exception at WARNING level, unless the same exception
	    // occurs repeatedly.  In that case, only log the first exception
	    // as a warning.  Log all exceptions with the same class after the
	    // first of that class at FINE level.  We want to avoid flooding the
	    // log when the same error occurs repeatedly (e.g. we are using an
	    // SSLSocketChannel and there is a certificate problem that causes
	    // ALL accepts to fail).
	    if (e.getClass() == lastExceptionClassSeen) {
		wrapper.ioexceptionInAcceptFine(e);
	    } else {
		lastExceptionClassSeen = e.getClass() ;
		wrapper.ioexceptionInAccept(e);
	    }

	    orb.getTransportManager().getSelector(0).unregisterForEvent(this);
	    // REVISIT - need to close - recreate - then register new one.
	    orb.getTransportManager().getSelector(0).registerForEvent(this);
	    // NOTE: if register cycling we do not want to shut down ORB
	    // since local beans will still work.  Instead one will see
	    // a growing log file to alert admin of problem.
	}

	if (orb.transportDebugFlag) {
	    dprint(".accept: " + 
		   (serverSocketChannel == null 
		    ? serverSocket.toString()
		    : serverSocketChannel.toString()));
	}

	CorbaConnection connection = 
	    new SocketOrChannelConnectionImpl(orb, this, socket);
	if (orb.transportDebugFlag) {
	    dprint(".accept: new: " + connection);
	}

	// NOTE: The connection MUST be put in the cache BEFORE being
	// registered with the selector.  Otherwise if the bytes
	// are read on the connection it will attempt a time stamp
	// but the cache will be null, resulting in NPE.
	getConnectionCache().put(this, connection);

	if (connection.shouldRegisterServerReadEvent()) {
	    Selector selector = orb.getTransportManager().getSelector(0);
	    selector.registerForEvent(connection.getEventHandler());
	}

	getConnectionCache().reclaim();
    }

    public void close ()
    {
	try {
	    if (orb.transportDebugFlag) {
		dprint(".close->:");
	    }
	    Selector selector = orb.getTransportManager().getSelector(0);
	    selector.unregisterForEvent(this);
	    if (serverSocketChannel != null) {
		serverSocketChannel.close();
	    }
	    if (serverSocket != null) {
		serverSocket.close();
	    }
	} catch (IOException e) {
	    if (orb.transportDebugFlag) {
		dprint(".close:", e);
	    }
	} finally {
	    if (orb.transportDebugFlag) {
		dprint(".close<-:");
	    }
	}
    }

    // EventHandler methods
    //

    public SelectableChannel getChannel()
    {
	return serverSocketChannel;
    }

    ////////////////////////////////////////////////////
    //
    // Work methods.
    //

    /* CONFLICT: with legacy below.
    public String getName()
    {
	return this.toString();
    }
    */

    public void doWork()
    {
	try {
	    if (orb.transportDebugFlag) {
		dprint(".doWork->: " + this);
	    }
	    if (selectionKey.isAcceptable()) {
                AccessController.doPrivileged(
		    new PrivilegedAction<Object>() {
			public java.lang.Object run() {
			    accept();
			    return null;
			}
		    }
		);
	    } else {
		if (orb.transportDebugFlag) {
		    dprint(".doWork: ! selectionKey.isAcceptable: " + this);
		}
	    }
	} catch (SecurityException se) {
	    if (orb.transportDebugFlag) {
		dprint(".doWork: ignoring SecurityException: "
		       + se 
		       + " " + this);
	    }
	    String permissionStr = ORBUtility.getClassSecurityInfo(getClass());
            wrapper.securityExceptionInAccept(se, permissionStr);
	} catch (Exception ex) {
	    if (orb.transportDebugFlag) {
		dprint(".doWork: ignoring Exception: "
		       + ex 
		       + " " + this);
	    }
            wrapper.exceptionInAccept(ex);
	} catch (Throwable t) {
	    if (orb.transportDebugFlag) {
		dprint(".doWork: ignoring Throwable: "
		       + t
		       + " " + this);
	    }
	} finally {

            // IMPORTANT: To avoid bug (4953599), we force the
	    // Thread that does the NIO select to also do the
	    // enable/disable of Ops using SelectionKey.interestOps().
	    // Otherwise, the SelectionKey.interestOps() may block
	    // indefinitely.
	    // NOTE: If "acceptorSocketUseWorkerThreadForEvent" is
	    // set to to false in ParserTable.java, then this method,
	    // doWork(), will get executed by the same thread 
	    // (SelectorThread) that does the NIO select. 
	    // If "acceptorSocketUseWorkerThreadForEvent" is set
	    // to true, a WorkerThread will execute this method,
	    // doWork(). Hence, the registering of the enabling of
	    // the SelectionKey's interestOps is done here instead
	    // of calling SelectionKey.interestOps(<interest op>).

            Selector selector = orb.getTransportManager().getSelector(0);
            selector.registerInterestOps(this);

	    if (orb.transportDebugFlag) {
		dprint(".doWork<-:" + this);
	    }
	}
    }

    ////////////////////////////////////////////////////
    //
    // SocketOrChannelAcceptor
    //

    public ServerSocket getServerSocket()
    {
	return serverSocket;
    }
    // END Legacy support
}

// End of file.
