/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2007 Sun Microsystems, Inc. All rights reserved.
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
//
// Created       : 2001 Dec 03 (Mon) 13:18:06 by Harold Carr.
// Last Modified : 2003 Nov 23 (Sun) 18:58:17 by Harold Carr.
//

package corba.genericRPCMSGFramework;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;

import com.sun.corba.se.pept.broker.Broker;
import com.sun.corba.se.pept.protocol.MessageMediator;
import com.sun.corba.se.pept.encoding.InputObject;
import com.sun.corba.se.pept.encoding.OutputObject;
import com.sun.corba.se.pept.transport.Acceptor;
import com.sun.corba.se.pept.transport.Connection;
import com.sun.corba.se.pept.transport.ContactInfo;
import com.sun.corba.se.pept.transport.EventHandler;

import com.sun.corba.se.spi.orb.ORB;

import com.sun.corba.se.impl.orbutil.ORBUtility;
import com.sun.corba.se.impl.transport.SocketOrChannelAcceptorImpl;

public class SOAPAcceptor
    extends
	SocketOrChannelAcceptorImpl
{
    private SOAPContactInfo soapContactInfo;

    // REVISIT - just give port, rather than SOAPContactInfo
    public SOAPAcceptor(ORB orb,  SOAPContactInfo soapContactInfo)
    {
	super(orb);
	this.soapContactInfo = soapContactInfo;
    }

    ////////////////////////////////////////////////////
    //
    // pept Acceptor
    //

    public boolean initialize()
    {
	if (initialized) {
	    return false;
	}
	if (orb.transportDebugFlag) {
	    ORBUtility.dprint(this, "initilize");
	}
	try {
	    serverSocketChannel = ServerSocketChannel.open();
	    serverSocketChannel.configureBlocking(true);
	    serverSocket = serverSocketChannel.socket();
	    serverSocket.bind(new InetSocketAddress(soapContactInfo.getPort()));
	    setUseSelectThreadToWait(false);
	    setUseWorkerThreadForEvent(false);
	} catch (IOException e) {
	    RuntimeException rte = new RuntimeException();
	    rte.initCause(e);
	    throw rte;
	}
	return initialized = true;
    }

    public boolean shouldRegisterAcceptEvent()
    {
	return true;
    }

    public void accept()
    {
	SocketChannel socketChannel = null;
	try {
	    socketChannel = serverSocketChannel.accept();
	} catch (IOException e) {
	    RuntimeException rte = new RuntimeException();
	    rte.initCause(e);
	    throw rte;
	}
	Socket socket = socketChannel.socket();
	try {
	    socket.setTcpNoDelay(true);
	} catch (Exception e) {
	    ;
	}

	// REVISIT - need to update connection
	SOAPConnection connection = new SOAPConnection(orb, socket);
	if (orb.transportDebugFlag) {
	    ORBUtility.dprint( this,
		"SOAPAcceptor.handleAcceptEvent: " + connection);
	}
	
	if (connection.shouldRegisterServerReadEvent()) {
	    orb.getTransportManager().getSelector(0)
		.registerForEvent(connection);
	}

	/* REVISIT : need to be able to asking about caching.
	   Either ask contactInfo (not present in server right now),
	   or ask connection.
	orb.getConnectionCache().stampTime(connection);
	// REVISIT - keys are contactInfoes - maybe acceptors should be contactInfoes?
	//connectionCache.put(connection);
	orb.getConnectionCache().maybeCloseSomeConnections();
	*/
    }
    
    public MessageMediator createMessageMediator(Broker orb,
						 Connection connection)
    {
	throw new RuntimeException("SOAPAcceptor.createMessageMediator");
    }

    public InputObject createInputObject(Broker orb,
					 MessageMediator messageMediator)
    {
	throw new RuntimeException("SOAPAcceptor.createInputObject");
    }

    public OutputObject createOutputObject(Broker orb,
					   MessageMediator messageMediator)
    {
	throw new RuntimeException("SOAPAcceptor.createOutputObject");
    }

    protected String toStringName()
    {
	return "SOAPAcceptor";
    }
}

// End of file.
