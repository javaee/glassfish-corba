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
// Created       : 2002 Jan 17 (Thu) 14:19:20 by Harold Carr.
// Last Modified : 2003 Mar 12 (Wed) 09:55:39 by Harold Carr.
//

package corba.purgecalls;

import com.sun.corba.ee.spi.legacy.connection.Connection;
import corba.framework.Controller;
import corba.hcks.U;
import java.net.Socket;
import java.util.Properties;
import org.omg.CORBA.COMM_FAILURE;
import org.omg.CORBA.ORB;

public class Client 
{
    public static final String baseMsg = Client.class.getName();
    public static final String main = baseMsg + ".main";

    public static ORB orb;
    public static ServerSide rServerSide;

    // The client interceptor sets this.
    public static Connection requestConnection;

    public static Throwable noExceptionExpected;

    public static void main(String av[])
    {
        try {
            Properties props = new Properties();
            props.put(U.ORBInitializerClass + "." + "corba.purgecalls.ClientORBInitializer", "ignored");
            orb = ORB.init(av, props);

            
            rServerSide =  
                ServerSideHelper.narrow(U.resolve(Server.ServerSide, orb));

            runTests();

            // Wait for other thread to do its thing.
            Thread.sleep(2000);

            U.sop("Test complete.");

        } catch (java.io.IOException e) {

            U.sop(main + " Expected: " + e);

        } catch (Throwable t) {
            U.sopUnexpectedException(main + " : ", t);
            System.exit(1);
        }

        if (noExceptionExpected == null) {
            U.normalExit(main);
            // Do not explicitly exit to test that no non-daemon threads
            // are hanging.
            //System.exit(Controller.SUCCESS);
        } else {
            U.sopUnexpectedException(main + " : ", noExceptionExpected);
            System.exit(1);
        }
    }

    public static void runTests()
        throws
            Exception
    {
        CallThread CallThread = new CallThread();
        CallThread.start();
    
        Thread.sleep(5000);

        Socket socket = requestConnection.getSocket();
        socket.shutdownInput();
        socket.shutdownOutput();
        socket.getInputStream().close();
        socket.getOutputStream().close();
        socket.close();
    }
}

class CallThread extends Thread
{
    CallThread ()
    {
    }
    public void run ()
    {
        try {
            Client.rServerSide.neverReturns();
        } catch (COMM_FAILURE e) {
            U.sop("Expected: " + e);
        } catch (Throwable t) {
            Client.noExceptionExpected = t;
            t.printStackTrace();
        }
    }
}

// End of file.
