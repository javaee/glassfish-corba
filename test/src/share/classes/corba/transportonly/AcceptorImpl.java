/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2003-2007 Sun Microsystems, Inc. All rights reserved.
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


package corba.transportonly;

import com.sun.corba.se.impl.oa.poa.Policies;
import com.sun.corba.se.spi.ior.IORTemplate;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.nio.channels.SelectableChannel;
import java.nio.channels.SelectionKey;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;

import com.sun.corba.se.impl.encoding.CDRInputObject;
import com.sun.corba.se.impl.encoding.CDROutputObject;
import com.sun.corba.se.spi.protocol.CorbaMessageMediator;
import com.sun.corba.se.spi.transport.CorbaAcceptor;
import com.sun.corba.se.spi.transport.CorbaConnection;
import com.sun.corba.se.spi.transport.EventHandler;
import com.sun.corba.se.spi.transport.CorbaInboundConnectionCache;
import com.sun.corba.se.spi.transport.Selector;

import com.sun.corba.se.spi.orb.ORB;
import com.sun.corba.se.spi.orbutil.threadpool.Work;

import com.sun.corba.se.impl.transport.EventHandlerBase;

public class AcceptorImpl
    extends
	EventHandlerBase
    implements
	CorbaAcceptor,
	Work
{
    private ServerSocketChannel serverSocketChannel;
    private ServerSocket serverSocket;
    private boolean useSelectThreadForConnections;
    private boolean useWorkerThreadForConnections;

    public AcceptorImpl(ORB orb, 
			int port,
			boolean useSelectThreadToWait,
			boolean isBlocking, 
			boolean useWorkerThreadForEvent,
			boolean useSelectThreadForConnections)
    {
	try {
	    this.orb = orb;
	    setWork(this);
	    serverSocketChannel = ServerSocketChannel.open();
	    serverSocketChannel.configureBlocking(isBlocking);
	    serverSocket = serverSocketChannel.socket();
	    serverSocket.bind(new InetSocketAddress(port));
	    setUseSelectThreadToWait(useSelectThreadToWait);
	    setUseWorkerThreadForEvent(useWorkerThreadForEvent);
	    setUseSelectThreadForConnections(useSelectThreadForConnections);
	} catch (IOException e) {
	    RuntimeException rte = new RuntimeException();
	    rte.initCause(e);
	    throw rte;
	}
    }

    ////////////////////////////////////////////////////
    //
    // Acceptor methods
    //

    public boolean initialize()
    {
	return false;
    }

    public boolean initialized()
    {
	return false;
    }

    public String getConnectionCacheType()
    {
	throw new RuntimeException();
    }

    public void setConnectionCache(CorbaInboundConnectionCache connectionCache)
    {
	throw new RuntimeException();
    }

    public CorbaInboundConnectionCache getConnectionCache()
    {
	throw new RuntimeException();
    }

    public boolean shouldRegisterAcceptEvent()
    {
	throw new RuntimeException("NO");
    }

    public void accept()
    {
	SocketChannel socketChannel = null;
	try {
	    socketChannel = serverSocketChannel.accept();
	    System.out.println("accepted.");
	    ConnectionImpl connection =
		new ConnectionImpl(orb, socketChannel,
				   shouldUseSelectThreadForConnections(),
				   !shouldUseSelectThreadForConnections(),
				   false);
	    Selector selector = orb.getCorbaTransportManager().getSelector(0);
	    selector.registerForEvent(connection);
	    System.out.println("connection registered.");
	} catch (IOException e) {
	    throw new RuntimeException(e.toString());
	}
    }

    public void setUseSelectThreadForConnections(boolean x)
    {
	useSelectThreadForConnections = x;
    }

    public boolean shouldUseSelectThreadForConnections()
    {
	return useSelectThreadForConnections;
    }

    public void setUseWorkerThreadForConnections(boolean x)
    {
	useWorkerThreadForConnections = x;
    }

    public boolean shouldUseWorkerThreadForConnections()
    {
	return useWorkerThreadForConnections;
    }

    public void close ()
    {
	try {
	    Selector selector = orb.getTransportManager().getSelector(0);
	    selector.unregisterForEvent(this);
	    serverSocketChannel.close();
	    serverSocket.close();
	} catch (IOException e) {
	    throw new RuntimeException(e.toString());
	}
    }

    public EventHandler getEventHandler()
    {
	return this;
    }

    ////////////////////////////////////////////////////
    //
    // EventHandler methods
    //

    public SelectableChannel getChannel()
    {
	return serverSocketChannel;
    }

    public int getInterestOps()
    {
	return SelectionKey.OP_ACCEPT;
    }

    public CorbaAcceptor getAcceptor()
    {
	return this;
    }

    public CorbaConnection getConnection()
    {
	return null;
    }

    ////////////////////////////////////////////////////
    //
    // Work methods.
    //

    public String getName()
    {
	return this.toString();
    }

    public void doWork()
    {
	System.out.println("+++++ Acceptor: BEGIN doWork +++++");
	if (! selectionKey.isAcceptable()) {
	    throw new RuntimeException("Excepting isAcceptable.");
	}
	accept();
	System.out.println("+++++ Acceptor: END doWork +++++");
	selectionKey.interestOps(selectionKey.interestOps() |
				 getInterestOps());
    }

    protected long enqueueTime;

    public void setEnqueueTime(long timeInMillis)
    {
	enqueueTime = timeInMillis;
    }

    public long getEnqueueTime()
    {
	return enqueueTime;
    }

    //////////////////////////////////////////////////
    //
    //
    //

    public CorbaMessageMediator createMessageMediator(ORB xbroker,
						 CorbaConnection xconnection)
    {
	throw new RuntimeException("NO.");
    }

    public CDRInputObject createInputObject(ORB broker,
					 CorbaMessageMediator messageMediator)
    {
	throw new RuntimeException("NO");
    }

    public CDROutputObject createOutputObject(ORB broker,
					   CorbaMessageMediator messageMediator)
    {
	throw new RuntimeException("NO");
    }

    public String getObjectAdapterId() {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public String getObjectAdapterManagerId() {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public void addToIORTemplate(IORTemplate iorTemplate, Policies policies, String codebase) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public String getMonitoringName() {
        throw new UnsupportedOperationException("Not supported yet.");
    }
}

// End of file.
