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

//
// Created       : 2003 Apr 15 (Tue) 16:16:46 by Harold Carr.
// Last Modified : 2004 May 12 (Wed) 11:59:38 by Harold Carr.
//

package corba.orbconfigappserv;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

import org.omg.CORBA.ORB;

import com.sun.corba.ee.spi.ior.IOR;
import com.sun.corba.ee.spi.legacy.connection.GetEndPointInfoAgainException;
import com.sun.corba.ee.spi.legacy.connection.ORBSocketFactory;
import com.sun.corba.ee.spi.transport.SocketInfo;

import com.sun.corba.ee.impl.legacy.connection.DefaultSocketFactory;
import com.sun.corba.ee.impl.legacy.connection.EndPointInfoImpl;

public class SocketFactoryImpl
    implements 
        ORBSocketFactory
{
    ORBSocketFactory socketFactory;

    public SocketFactoryImpl()
    {
        System.out.println("SocketFactoryImpl()");
        socketFactory = new DefaultSocketFactory();
    }

    public ServerSocket createServerSocket(String type, int port)
        throws
            IOException
    {
        System.out.println("createServerSocket: " + type + " " + port);
        return socketFactory.createServerSocket(type, port);
    }

    public SocketInfo getEndPointInfo(ORB orb,
                                        IOR ior,
                                        SocketInfo socketInfo)
    {
        System.out.println("getEndPointInfo");
        return socketFactory.getEndPointInfo(orb, ior, socketInfo);
    }

    public Socket createSocket(SocketInfo socketInfo)
        throws
            IOException,
            GetEndPointInfoAgainException
    {
        System.out.println("createSocket: " + socketInfo);
        return socketFactory.createSocket(socketInfo);
    }
}

// End of file.
