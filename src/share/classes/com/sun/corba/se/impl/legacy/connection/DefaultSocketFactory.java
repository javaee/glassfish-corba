/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright (c) 1997-2010 Oracle and/or its affiliates. All rights reserved.
 * 
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common Development
 * and Distribution License("CDDL") (collectively, the "License").  You
 * may not use this file except in compliance with the License.  You can
 * obtain a copy of the License at
 * https://glassfish.dev.java.net/public/CDDL+GPL_1_1.html
 * or packager/legal/LICENSE.txt.  See the License for the specific
 * language governing permissions and limitations under the License.
 * 
 * When distributing the software, include this License Header Notice in each
 * file and include the License file at glassfish/bootstrap/legal/LICENSE.txt.
 * 
 * GPL Classpath Exception:
 * Oracle designates this particular file as subject to the "Classpath"
 * exception as provided by Oracle in the GPL Version 2 section of the License
 * file that accompanied this code.
 * 
 * Modifications:
 * If applicable, add the following below the License Header, with the fields
 * enclosed by brackets [] replaced by your own identifying information:
 * "Portions Copyright [year] [name of copyright owner]"
 * 
 * Contributor(s):
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
package com.sun.corba.se.impl.legacy.connection;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;

import org.omg.CORBA.ORB;

import com.sun.corba.se.spi.ior.IOR;
import com.sun.corba.se.spi.ior.iiop.IIOPProfileTemplate ;
import com.sun.corba.se.spi.ior.iiop.IIOPAddress ;
import com.sun.corba.se.spi.legacy.connection.GetEndPointInfoAgainException;
import com.sun.corba.se.spi.legacy.connection.ORBSocketFactory;
import com.sun.corba.se.spi.transport.SocketInfo;

import com.sun.corba.se.impl.logging.ORBUtilSystemException;
import com.sun.corba.se.spi.orbutil.ORBConstants;
import com.sun.corba.se.impl.orbutil.ORBUtility;

public class DefaultSocketFactory 
    implements 
	ORBSocketFactory
{
    private com.sun.corba.se.spi.orb.ORB orb;
    private static ORBUtilSystemException wrapper = 
	com.sun.corba.se.spi.orb.ORB.getStaticLogWrapperTable()
	    .get_RPC_TRANSPORT_ORBUtil() ;

    public DefaultSocketFactory()
    {
    }

    public void setORB(com.sun.corba.se.spi.orb.ORB orb)
    {
	this.orb = orb;
    }

    public ServerSocket createServerSocket(String type, int port)
	throws
	    IOException
    {
	if (! type.equals(ORBSocketFactory.IIOP_CLEAR_TEXT)) {
	    throw wrapper.defaultCreateServerSocketGivenNonIiopClearText( type ) ;
	}

	ServerSocket serverSocket;

	if (orb.getORBData().acceptorSocketType().equals(ORBConstants.SOCKETCHANNEL)) {
	    ServerSocketChannel serverSocketChannel =
		ServerSocketChannel.open();
	    serverSocket = serverSocketChannel.socket();
	} else {
	    serverSocket = new ServerSocket();
	}
	serverSocket.bind(new InetSocketAddress(port));
	return serverSocket;
    }

    public SocketInfo getEndPointInfo(ORB orb,
					IOR ior,
					SocketInfo socketInfo)
    {
        IIOPProfileTemplate temp = 
	    (IIOPProfileTemplate)ior.getProfile().getTaggedProfileTemplate() ;
	IIOPAddress primary = temp.getPrimaryAddress() ;

	return new EndPointInfoImpl(ORBSocketFactory.IIOP_CLEAR_TEXT,
				    primary.getPort(),
				    primary.getHost().toLowerCase());
    }

    public Socket createSocket(SocketInfo socketInfo)
	throws
	    IOException,
	    GetEndPointInfoAgainException
    {
	Socket socket;

	if (orb.getORBData().acceptorSocketType().equals(ORBConstants.SOCKETCHANNEL)) {
	    InetSocketAddress address = 
		new InetSocketAddress(socketInfo.getHost(), 
				      socketInfo.getPort());
	    SocketChannel socketChannel = ORBUtility.openSocketChannel(address);
	    socket = socketChannel.socket();
	} else {
	    socket = new Socket(socketInfo.getHost(), 
				socketInfo.getPort());
	}

	// REVISIT - this is done in SocketOrChannelConnectionImpl
	try {
	    socket.setTcpNoDelay(true);
	} catch (Exception e) {
            // XXX log this
	}
	return socket;
    }
}

// End of file.

