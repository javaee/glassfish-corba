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

package corba.example;

import com.sun.corba.ee.spi.misc.ORBConstants;
import java.util.Properties ;
import org.omg.CORBA.ORB;
import org.omg.CosNaming.NameComponent;
import org.omg.CosNaming.NamingContext;
import org.omg.CosNaming.NamingContextHelper;
import HelloApp.* ;

public class Client implements Runnable
{
    static final int NTHREADS = 100;
    static final int NITNS = 10;
    static hello helloRef;

    static final java.lang.Object lock = new java.lang.Object ();
    static boolean errorOccured = false;

    public void signalError ()
    {
        synchronized (Client.lock) {
            errorOccured = true;
        }
    }

    public static void main(String args[])
    {
        try{
            Properties props = new Properties(System.getProperties());
// Examples of how to set ORB debug properties and default fragment size
//          props.put(ORBConstants.DEBUG_PROPERTY, "transport,giop");
//            props.put(ORBConstants.GIOP_FRAGMENT_SIZE, "32");
            // create and initialize the ORB
            ORB orb = ORB.init(args, props);

            // get the root naming context
            org.omg.CORBA.Object objRef = 
                orb.resolve_initial_references("NameService");
            NamingContext ncRef = NamingContextHelper.narrow(objRef);
 
            // resolve the Object Reference in Naming
            NameComponent nc = new NameComponent("Hello", "");
            NameComponent path[] = {nc};
            helloRef = helloHelper.narrow(ncRef.resolve(path));

            System.out.println ("Starting client threads...");

            Thread[] threads = new Thread[NTHREADS];
            for ( int i=0; i<NTHREADS; i++ ) {
                threads[i] = new Thread(new Client());
            }
            System.out.println("Starting all threads");
            for ( int i=0; i<NTHREADS; i++ ) {
                threads[i].start();
            }

            // Wait for all threads to finish
            for (int i = 0; i < NTHREADS; i++)
                threads[i].join ();

            // Perform a simple test on stub equality. ie., test two stubs
            // which point to the same object for equality.
            
            nc = new NameComponent("Hello", "");
            path = new NameComponent[] {nc};
            hello helloRef2 = helloHelper.narrow(ncRef.resolve(path));
            
            boolean result = helloRef.equals(helloRef2);
            System.out.println("equals: " + result);
            if (result == false) {                
                errorOccured = true;
            }
            
            // test finished
                        
            System.out.println ("All threads returned, client finished");

            if (errorOccured)
                System.exit (1);

        } catch (Exception e) {
            System.out.println("ERROR : " + e) ;
            e.printStackTrace(System.out);
            System.exit (1);
        }
    }

    public void run()
    {
        try {
            for ( int i=0; i<NITNS; i++ ) {
                // call the hello server object and print results
                String hello = helloRef.sayHello();
                System.out.println(hello);
                if (!hello.equals ("Hello world!")) {
                    System.out.println ("Bad result of \"" + hello + "\" in " 
                                        + Thread.currentThread ());
                    signalError ();
                }
            }
        } catch (Exception e) {
            System.out.println("ERROR in thread: " + e) ;
            e.printStackTrace(System.out);
            signalError ();
        }
        System.out.println("Thread "+Thread.currentThread()+" done.");
    }
}
