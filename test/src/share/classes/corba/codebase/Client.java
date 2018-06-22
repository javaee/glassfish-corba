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

package corba.codebase;

import java.rmi.*;
import javax.rmi.PortableRemoteObject;
import javax.naming.*;

import java.io.*;

public class Client 
{
    // Tests stub/tie downloading
    public static void testDownloading(Tester tester) throws Exception
    {
        tester.printMessage("Simple message test with downloading");

        System.out.println("PASSED");
    }

    public static void testServerValueDownloading(Tester tester) 
        throws Exception
    {
        System.out.println("Testing server value downloading");

        Class testValueClass = Class.forName("TestValue");
        
        String result = tester.processValue(testValueClass.newInstance());

        if (!testValueClass.getName().equals(result))
            throw new Exception("Server didn't receive the right value class.  Got: "
                                + result);

        System.out.println("PASSED");
    }

    public static void testClientValueDownloading(Tester tester)
        throws Exception
    {
        System.out.println("Testing client value downloading");

        Object res = tester.requestValue();

        if (!res.getClass().getName().equals("TestValue"))
            throw new Exception("Client didn't receive a TestValue, got: "
                                + res.getClass().getName());

        System.out.println("PASSED");
    }

    // This is just helpful for debugging to see whether or not the
    // client has access to these files.
    public static void tryLoadingClasses()
    {
        System.out.println("java.rmi.server.codebase = "
                           + System.getProperty("java.rmi.server.codebase"));

        try {
            System.out.println("Trying to load the stub class");
            Class stub = Class.forName("corba.codebase._Tester_Stub");
            System.out.println("Client has access to the stub");
        } catch (ClassNotFoundException cnfe) {
            System.out.println("Client doesn't have access to the stub");
        }

        try {
            System.out.println("Trying to load the tie class");
            Class tie = Class.forName("corba.codebase._Server_Tie");
            System.out.println("Client has access to the tie");
        } catch (ClassNotFoundException cnfe) {
            System.out.println("Client doesn't have access to the tie");
        }

        try {
            System.out.println("Trying to load the TestValue class");
            Class testValue = Class.forName("TestValue");
            System.out.println("Client has access to the TestValue class");
        } catch (ClassNotFoundException cnfe) {
            System.out.println("Client doesn't have access to TestValue");
        }
    }

    private static InitialContext rootContext ;

    public static void main(String args[])
    {
        try {
            System.setSecurityManager(new NoSecurityManager());

            Client.tryLoadingClasses();

            rootContext = new InitialContext();
            Tester tester 
                = (Tester)PortableRemoteObject.narrow(rootContext.lookup("Tester"), Tester.class);
            
            System.out.println("Testing downloading.  Server downloading? "
                               + System.getProperty(Tester.SERVER_DOWNLOADING_FLAG));

            Client.testDownloading(tester);

            if (System.getProperty(Tester.SERVER_DOWNLOADING_FLAG) != null) {
                // The server is downloading code.  Try to send a TestValue
                // instance.
                Client.testServerValueDownloading(tester);
            } else {
                // The client is downloading code.  Try to receive a TestValue
                // instance.
                Client.testClientValueDownloading(tester);
            }

        } catch (Throwable t) {
            t.printStackTrace();
            System.exit(1);
        }
    }
}
