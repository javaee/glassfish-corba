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
package corba.codebase;

import java.rmi.*;
import javax.rmi.PortableRemoteObject;
import javax.naming.*;

import java.io.*;

public class Server extends PortableRemoteObject implements Tester
{
    public Server() throws java.rmi.RemoteException {
    }

    public void printMessage(String message)
    {
        System.out.println(message);
    }

    public Object requestValue() 
        throws ClassNotFoundException, InstantiationException, IllegalAccessException
    {
        Class valueClass = Class.forName("TestValue");
        return valueClass.newInstance();
    }

    public String processValue(Object value)
    {
        Class valueClass = value.getClass();

        System.out.println("Received instance of: " + valueClass.getName());

        return valueClass.getName();
    }

    // This is just helpful for debugging to see whether or not the
    // server has access to these files.
    public static void tryLoadingClasses()
    {
        System.out.println("java.rmi.server.codebase = "
                           + System.getProperty("java.rmi.server.codebase"));

        try {
            System.out.println("Trying to load the stub class");
            Class stub = Class.forName("corba.codebase._Tester_Stub");
            System.out.println("Server has access to the stub");
        } catch (ClassNotFoundException cnfe) {
            System.out.println("Server doesn't have access to the stub");
        }

        try {
            System.out.println("Trying to load the tie class");
            Class tie = Class.forName("corba.codebase._Server_Tie");
            System.out.println("Server has access to the tie");
        } catch (ClassNotFoundException cnfe) {
            System.out.println("Server doesn't have access to the tie");
        }

        try {
            System.out.println("Trying to load the TestValue class");
            Class testValue = Class.forName("TestValue");
            System.out.println("Server has access to the TestValue class");
        } catch (ClassNotFoundException cnfe) {
            System.out.println("Server doesn't have access to TestValue");
        }
    }

    private static InitialContext rootContext ;

    public static void main(String[] args) {
	try {
            System.setSecurityManager(new NoSecurityManager());

            Server.tryLoadingClasses();

            rootContext = new InitialContext();
            Server p = new Server();

            rootContext.rebind("Tester", p);
            System.out.println("Server is ready.");
	} catch (Throwable t) {
            t.printStackTrace();
            System.exit(1);
	}
    }
}

