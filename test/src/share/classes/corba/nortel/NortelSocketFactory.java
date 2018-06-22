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

package corba.nortel ;

import com.sun.corba.ee.impl.transport.DefaultSocketFactoryImpl;

import java.io.IOException;

import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.Socket;

import java.lang.String;

public class NortelSocketFactory extends DefaultSocketFactoryImpl {
    private static Socket savedSocket = null ;
    private static boolean transportDown = false ;
    public static boolean useNio = true ;
    public static boolean verbose = false ;

    private static void msg( String str ) {
        if (verbose) {
            System.out.println( "+++NortelSocketFactory: " + str ) ;
        }
    }

    public ServerSocket createServerSocket(String type, InetSocketAddress in) throws IOException {
        if (transportDown) {
            msg( "Simulating transport failure..." ) ;
            throw new IOException( "Transport simulated down" ) ;
        }

        msg("In method createServerSocket, type:" + type + ", InetSocketAddress:" + in );
        ServerSocket serverSocket = new ServerSocket();
        serverSocket.bind(in);

        return serverSocket;
    }

    public Socket createSocket(String type, InetSocketAddress in) throws IOException {
        msg("In method createSocket, type:" + type + ", InetSocketAddress:" + in );
        if (transportDown) {
            msg( "Simulating transport failure..." ) ;
            throw new IOException( "Transport simulated down" ) ;
        }

        Socket socket = null;
        if (useNio) {
            socket = super.createSocket(type, in); 
        } else {
            socket = new Socket(in.getHostName(), in.getPort());
            socket.setTcpNoDelay(true);
        }
        
        savedSocket = socket;
        return socket;
    }

    public static void disconnectSocket(){
        msg( "Disconnecting socket" ) ;
        try  {
            savedSocket.close();
        } catch (Exception e) {

            msg("Exception " + e);
        }
    }

    // Simulate the failure of the destination: ensure that all connection attempts fail
    public static void simulateConnectionDown() {
        transportDown = true ;
    }

    public static void simulateConnectionUp() {
        transportDown = false ;
    }
}

