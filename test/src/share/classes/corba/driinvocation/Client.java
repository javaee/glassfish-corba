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
package corba.driinvocation;

import java.util.*;
import java.io.*;
import java.io.DataInputStream ;
import org.omg.CORBA.*;
import org.omg.CosNaming.*;
import javax.rmi.PortableRemoteObject ;
import corba.framework.*;

public class counterClient implements InternalProcess
{
    // Temporary hack to get this test to work and keep the output
    // directory clean
    private static final String outputDirOffset 
        = "/corba/rmipoacounter/".replace('/', File.separatorChar);

    /**
     * These counters are used to check that the values remain
     * correct even when the server is restarted.
     */
    private static long counterValue = 1;

    private void performTest(PrintStream out,
                             PrintStream err,
                             counterIF counterRef1,
                             counterIF counterRef2) throws Exception
    {
        // call the counter server objects and print results
        long value = counterRef1.increment(1);
        out.println("Counter1 value = " + value);
        if (++counterValue != value)
            throw new Exception("Invalid counter1: "
                                + value + " but should be " + counterValue);

        for (int i = 0; i < 2; i++) {
            value = counterRef2.increment(1);
            out.println("Counter2 value = "+value);
            if (++counterValue != value)
                throw new Exception("Invalid counter2: "
                                    + value + " but should be " + counterValue);
        }
        
    }

    public void run(Properties environment,
                    String args[],
                    PrintStream out,
                    PrintStream err,
                    Hashtable extra) throws Exception
    {
        environment.list(out);

        try {
            // create and initialize the ORB
            ORB orb = ORB.init(args, environment);

            // get counter objrefs from NameService
            org.omg.CORBA.Object objRef =
                orb.resolve_initial_references("NameService");
            NamingContext ncRef = NamingContextHelper.narrow(objRef);
            NameComponent nc = new NameComponent("Counter1", "");
            NameComponent[] path = {nc};

            counterIF counterRef1 = 
                (counterIF)PortableRemoteObject.narrow(ncRef.resolve(path),
                                                       counterIF.class);

            // Read IOR from file and destringify it
            InputStream inf = 
                new FileInputStream(environment.getProperty("output.dir")
                                    + outputDirOffset
                                    + "counterior2");
            DataInputStream in = new DataInputStream(inf);
            String ior = in.readLine() ;
            org.omg.CORBA.Object obj = orb.string_to_object(ior) ;
            counterIF counterRef2 
                = (counterIF)PortableRemoteObject.narrow(obj, counterIF.class);

            Controller server = (Controller)extra.get("server");

            for (int i = 0; i < 3; i++) {
                out.println("Testing, pass #" + i);
                performTest(out, err, counterRef1, counterRef2);
                out.println("Restarting server...");
                server.stop();
                server.start();
            }

        } catch (Exception e) {
            e.printStackTrace(err);
            throw e;
        }
    }

    public static void main(String args[])
    {
        try {

            (new counterClient()).run(System.getProperties(),
                                      args,
                                      System.out,
                                      System.err,
                                      null);

        } catch (Exception e) {
            System.err.println("ERROR : " + e) ;
            e.printStackTrace(System.err);
            System.exit(1);
        }
    }
}
