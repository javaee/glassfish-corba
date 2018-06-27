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
// Created       : 2002 Jul 19 (Fri) 14:48:59 by Harold Carr.
// Last Modified : 2005 Jul 19 (Tue) 12:24:38 by Harold Carr.
//

package corba.folb_8_1;


import java.util.Properties ;

import org.omg.CORBA.ORB;
import org.omg.PortableServer.POA;

import com.sun.corba.ee.spi.misc.ORBConstants;

import corba.hcks.U;

/**
 * @author Harold Carr
 */
public class Server
{
    public static final String baseMsg = Common.class.getName();

    public static ORB orb;

    // So it can be accessed later.
    public static org.omg.CORBA.Object ref;

    public static void setProperties(Properties props, int[] socketPorts)
    {
        //
        // Debugging flags.  Generally commented out.
        //
        /*
        props.setProperty(ORBConstants.DEBUG_PROPERTY,
                          "giop,transport,subcontract");
        */

        //
        // Tell the ORB to listen on user-defined port types.
        //

        String listenPorts = formatListenPorts();
        props.setProperty(ORBConstants.LISTEN_SOCKET_PROPERTY, listenPorts);
        System.out.println(listenPorts);

        //
        // Register the socket factory that knows how to create
        // Sockets of type X Y and Z.
        //

        props.setProperty(ORBConstants.SOCKET_FACTORY_CLASS_PROPERTY,
                          SocketFactoryImpl.class.getName());

        //
        // Register and IORInterceptor that will put port 
        // type/address info into IORs.
        // E.G.: X/<hostanme>:*, Y/<hostname>:4444, Z/<hostname>:5555
        //

        props.setProperty("org.omg.PortableInterceptor.ORBInitializerClass." + IORInterceptorImpl.class.getName(),
                          "dummy");
    }

    public static String formatListenPorts()
    {
        String result = "";
        for (int i = 0; i < Common.socketTypes.length; i++) {
            result += Common.socketTypes[i] 
                + ":" 
                + Integer.toString(Common.socketPorts[i]);
            if (i + 1 < Common.socketTypes.length) {
                result += ",";
            }
        }
        return result;
    }
  
    public static void main(String av[])
    {
        try {
            if (! ColocatedCS.isColocated) {
                Properties props = System.getProperties();
                setProperties(props, Common.socketPorts);
                orb = ORB.init(av, props);
            }

            POA poa = Common.createPOA("child", false, orb);
            ref = Common.createAndBind(Common.serverName1, orb, poa);
            Common.createAndBind(Common.serverName2, orb, poa);
      
            System.out.println ("Server is ready.");

            synchronized (ColocatedCS.signal) {
                ColocatedCS.signal.notifyAll();
            }
            
            orb.run();
            
        } catch (Throwable t) {
            System.out.println(baseMsg + t);
            t.printStackTrace(System.out);
            System.exit(1);
        }
    }

}

// This class is to ensure that we do NOT store a contact info to
// a different object and try to send an invocation from a client to
// an incorrect Tie.
class I2Servant extends I2POA
{
    private com.sun.corba.ee.spi.orb.ORB orb;

    public I2Servant(ORB orb)
    {
        this.orb = (com.sun.corba.ee.spi.orb.ORB) orb;
    }

    public int m(String x)
    {
        int result = new Integer(x).intValue();
        System.out.println("I2Servant.m result: " + result);
        System.out.flush();
        return result;
    }
  
    public org.omg.CORBA.Object n(String x)
    {
        return Server.ref;
    }

    public int foo(int x)
    {
        return x;
    }
}

class IServant extends IPOA
{
    private com.sun.corba.ee.spi.orb.ORB orb;

    public IServant(ORB orb)
    {
        this.orb = (com.sun.corba.ee.spi.orb.ORB) orb;
    }

    public String m(String x)
    {
        return "IServant echoes: " + x;
    }

    public int n(String x)
    {
        return 101;
    }

    public int throwRuntimeException(int x)
    {
        return 1/x;
    }

    public boolean unregister(String socketType)
    {
        return U.unregisterAcceptorAndCloseConnections(socketType, orb);
    }

    public boolean register(String socketType)
    {
        return U.registerAcceptor(socketType, 
                          ((Integer) Common.socketTypeToPort.get(socketType))
                              .intValue(),
                          orb);
    }
}

// End of file.
