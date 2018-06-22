/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright (c) 1997-2018 Oracle and/or its affiliates. All rights reserved.
 *
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common Development
 * and Distribution License("CDDL") (collectively, the "License").  You
 * may not use this file except in compliance with the License.  You can
 * obtain a copy of the License at
 * https://oss.oracle.com/licenses/CDDL+GPL-1.1
 * or LICENSE.txt.  See the License for the specific
 * language governing permissions and limitations under the License.
 *
 * When distributing the software, include this License Header Notice in each
 * file and include the License file at LICENSE.txt.
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

package corba.folb_8_1;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.SocketException;
import java.net.ServerSocket;
import java.nio.channels.SocketChannel;
import java.nio.channels.ServerSocketChannel;

import com.sun.corba.ee.spi.transport.Acceptor;

import com.sun.corba.ee.spi.orb.ORB;
import com.sun.corba.ee.spi.transport.ORBSocketFactory;

import com.sun.corba.ee.spi.misc.ORBConstants;
import com.sun.corba.ee.impl.misc.ORBUtility;

public class SocketFactoryImpl
    implements ORBSocketFactory
{
    private ORB orb;

    public void setORB(ORB orb)
    {
        this.orb = orb;
    }

    public ServerSocket createServerSocket(String type, 
                                           InetSocketAddress inetSocketAddress)
        throws IOException
    {
        ServerSocket serverSocket = null;
        try {
            if (! Common.timing) {
                System.out.println(".createServerSocket->: " + type + " " 
                                   + inetSocketAddress);
            }

            ServerSocketChannel serverSocketChannel = null;

            if (orb.getORBData().acceptorSocketType().equals(ORBConstants.SOCKETCHANNEL)) {
                serverSocketChannel = ServerSocketChannel.open();
                serverSocket = serverSocketChannel.socket();
            } else {
                serverSocket = new ServerSocket();
            }
            serverSocket.bind(inetSocketAddress);
            return serverSocket;
        } finally {
            if (! Common.timing) {
                System.out.println(".createServerSocket<-: " + type + " " 
                                   + inetSocketAddress + " " + serverSocket);
            }
        }
    }

    public Socket createSocket(String type, 
                               InetSocketAddress inetSocketAddress)
        throws IOException
    {
        Socket socket = null;

        try {
            if (! Common.timing) {
                System.out.println(".createSocket->: " + type + " " 
                                   + inetSocketAddress);
            }

            SocketChannel socketChannel = null;

            if (orb.getORBData().connectionSocketType().equals(ORBConstants.SOCKETCHANNEL)) {
                socketChannel = ORBUtility.openSocketChannel(inetSocketAddress);
                socket = socketChannel.socket();
            } else {
                socket = new Socket(inetSocketAddress.getHostName(),
                                    inetSocketAddress.getPort());
            }

            // Disable Nagle's algorithm (i.e., always send immediately).
            socket.setTcpNoDelay(true);

            return socket;

        } finally {
            if (! Common.timing) {
                System.out.println(".createSocket<-: " + type + " " 
                                   + inetSocketAddress + " " + socket);
            }
        }
    }

    public void setAcceptedSocketOptions(Acceptor acceptor,
                                         ServerSocket serverSocket,
                                         Socket socket)
        throws SocketException
    {
        // Disable Nagle's algorithm (i.e., always send immediately).
        socket.setTcpNoDelay(true);
    }
}

// End of file.
