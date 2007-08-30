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
/*
 * @(#)HelloClient.java	1.5 99/10/29
 *
 * Copyright 1997-1999 Sun Microsystems, Inc. All Rights Reserved.
 * 
 * This software is the proprietary information of Sun Microsystems, Inc.  
 * Use is subject to license terms.
 * 
 */
package corba.poamanager;

import org.omg.CORBA.*;
import org.omg.PortableServer.POAManagerPackage.AdapterInactive;

import Util.*;
import HelloStuff.*;

import com.sun.corba.se.impl.logging.ORBUtilSystemException ;
import com.sun.corba.se.impl.logging.POASystemException ;

// Will this test exit with value 1 when errors in WorkerThreads?  REVISIT

public class HelloClient {
    private static final int N_LOOPS = 10;
    private static ORBUtilSystemException orbutilWrapper ;
    private static POASystemException poaWrapper ;
    
    public static void main(String[] args) 
    {
	try {
	    Utility u = new Utility(args);

	    orbutilWrapper = ((com.sun.corba.se.spi.orb.ORB)u.getORB())
		.getLogWrapperTable().get_RPC_PROTOCOL_ORBUtil() ;
	    poaWrapper = ((com.sun.corba.se.spi.orb.ORB)u.getORB())
		.getLogWrapperTable().get_OA_POA() ;

	    GenericFactory f = u.readFactory();

	    System.out.println("----------------------------------------");
	    System.out.println("Creating objects");
	    System.out.println("----------------------------------------");
	    
	    Hello h1 = createHello(CreationMethods.EXPLICIT_ACTIVATION_WITH_POA_ASSIGNED_OIDS, f);

	    Hello h2 = createHello(CreationMethods.EXPLICIT_ACTIVATION_WITH_USER_ASSIGNED_OIDS, f);

	    Hello h3 = createHello(CreationMethods.CREATE_REFERENCE_BEFORE_ACTIVATION_WITH_POA_ASSIGNED_OIDS, f);

	    Hello h4 = createHello(CreationMethods.CREATE_REFERENCE_BEFORE_ACTIVATION_WITH_USER_ASSIGNED_OIDS, f);

	    System.out.println("----------------------------------------");
            System.out.println("Invoking");
	    System.out.println("----------------------------------------");

	    invoke(h1);
	    invoke(h2);
	    invoke(h3);
	    invoke(h4);

	    System.out.println("----------------------------------------");
            System.out.println("Creating threads");
	    System.out.println("----------------------------------------");

	    // Create lots of threads, then call holdRequests. This
	    // tests the wait_for_completion code on the server.
	    WorkerThread[] threads = new WorkerThread[N_LOOPS*4];
	    for ( int i=0; i<N_LOOPS; i++ ) {
	        threads[i*4] = invokeOnThread(h1);
	        threads[i*4+1] = invokeOnThread(h2);
	        threads[i*4+2] = invokeOnThread(h3);
	        threads[i*4+3] = invokeOnThread(h4);
	    }

	    Thread.sleep(500); // sleep to allow some invocations to happen

	    System.out.println("----------------------------------------");
	    System.out.println("holding requests");
	    System.out.println("----------------------------------------");

	    f.holdRequests();

	    System.out.println("----------------------------------------");
            System.out.println("finished holding");
	    System.out.println("----------------------------------------");

	    Thread.sleep(1000); // sleep for 1 seconds to quiesce invocations

	    System.out.println("----------------------------------------");
	    System.out.println("re-activating");
	    System.out.println("----------------------------------------");

	    f.activate();

	    System.out.println("----------------------------------------");
            System.out.println("reactivated, waiting for threads to join");
	    System.out.println("----------------------------------------");

	    // wait for all the threads to finish
            int errors = 0;
	    for ( int i=0; i<N_LOOPS*4; i++ ) {
	        threads[i].join();
                if (threads[i].errorOccured())
                    errors++;
            }

            if (errors > 0) {
		String msg = "WorkerThread(s) had " + errors + " error(s)";
		System.out.println("----------------------------------------");
		System.out.println(msg);
		System.out.println("----------------------------------------");
                throw new Exception(msg);
	    }

	    System.out.println("----------------------------------------");
	    System.out.println("discarding requests");
	    System.out.println("----------------------------------------");

	    f.discardRequests();

            // Each of these should throw an exception, but that's what they're
            // supposed to do

	    try {
	        invoke(h1);
                throw new Exception("Didn't throw COMM_FAILURE on invoke(h1)");
	    } catch ( COMM_FAILURE ex ) {
		checkTransient("h1", ex);
            }
	    try {
	        invoke(h2);
                throw new Exception("Didn't throw COMM_FAILURE on invoke(h2)");
	    } catch ( COMM_FAILURE ex ) {
		checkTransient("h2", ex);
            }
	    try {
	        invoke(h3);
                throw new Exception("Didn't throw COMM_FAILURE on invoke(h3)");
	    } catch ( COMM_FAILURE ex ) {
		checkTransient("h3", ex);
            }
	    try {
	        invoke(h4);
                throw new Exception("Didn't throw COMM_FAILURE on invoke(h4)");
	    } catch ( COMM_FAILURE ex ) {
		checkTransient("h4", ex);
            }

	    System.out.println("----------------------------------------");
	    System.out.println("deactivating");
	    System.out.println("----------------------------------------");

	    f.deactivate();

	    try {
	        invoke(h1);
                throw new Exception("Didn't throw OBJ_ADAPTER on invoke(h1)");
	    } catch ( OBJ_ADAPTER ex ) {
		System.out.println("----------------------------------------");
                System.out.println("Correct behavior - OBJ_ADAPTER/h1");
		System.out.println("----------------------------------------");
            }
	    try {
	        invoke(h2);
                throw new Exception("Didn't throw OBJ_ADAPTER on invoke(h2)");
	    } catch ( OBJ_ADAPTER ex ) {
		System.out.println("----------------------------------------");
                System.out.println("Correct behavior - OBJ_ADAPTER/h2");
		System.out.println("----------------------------------------");
            }
	    try {
	        invoke(h3);
                throw new Exception("Didn't throw OBJ_ADAPTER on invoke(h3)");
	    } catch ( OBJ_ADAPTER ex ) {
		System.out.println("----------------------------------------");
                System.out.println("Correct behavior - OBJ_ADAPTER/h3");
		System.out.println("----------------------------------------");
            }
	    try {
	        invoke(h4);
                throw new Exception("Didn't throw OBJ_ADAPTER on invoke(h4)");
	    } catch ( OBJ_ADAPTER ex ) {
		System.out.println("----------------------------------------");
                System.out.println("Correct behavior - OBJ_ADAPTER/h4");
		System.out.println("----------------------------------------");
            }

            try {
                f.activate();
                throw new Exception("Didn't throw AdapterInactive");
            } catch (AdapterInactive ex) {
		System.out.println("----------------------------------------");
                System.out.println("Correct behavior - AdapterInactive");
		System.out.println("----------------------------------------");
            }

	} catch (Exception e) {
	    System.out.println("----------------------------------------");
	    System.out.println("Client FAILED");
	    System.out.println("----------------------------------------");
	    e.printStackTrace(System.out);
            System.exit(1);
	}

	System.out.println("----------------------------------------");
        System.out.println("Client SUCCEEDED");
	System.out.println("----------------------------------------");
    }

    public static Hello createHello(CreationMethods c, GenericFactory f) 
    {
	return HelloHelper.narrow(f.create(HelloHelper.id(),
					   "corba.poamanager.HelloImpl",
					   c));
    }

    static final void invoke(Hello h) 
    {
	System.out.println(h.hi());
    }

    static final WorkerThread invokeOnThread(Hello h) 
    {
	WorkerThread th = new WorkerThread(h);
	th.start();
	return th;
    }

    public static void checkTransient(String msg, COMM_FAILURE e)
    {
	SystemException expected = 
	    orbutilWrapper.communicationsRetryTimeout(
		new Integer(-1));
	SystemException expectedCause = poaWrapper.poaDiscarding();
	if (e.getClass().isInstance(expected)
	    && ((SystemException)e).minor == expected.minor
	    && ((SystemException)e).completed == expected.completed
	    && e.getCause() != null
	    && e.getCause().getClass().isInstance(expectedCause)
	    && ((SystemException)e.getCause()).minor == expectedCause.minor
	    && ((SystemException)e.getCause()).completed == expectedCause.completed)
	{
	    System.out.println("----------------------------------------");
	    System.out.println(msg + " TRANSIENT timeout SUCCESS");
	    System.out.println("----------------------------------------");
	} else {
	    String message = msg + " TRANSIENT timeout FAILED";
	    System.out.println("----------------------------------------");
	    System.out.println(message);
	    System.out.println("----------------------------------------");
	    throw new RuntimeException(message);
	}
    }
}


class WorkerThread extends Thread 
{
    Hello h;
    private boolean errorOccured;

    WorkerThread(Hello h)
    {
	this.h = h;
        errorOccured = false;
    }

    public void run()
    {
        try {
            System.out.println(h.hi());
        } catch (Exception e) {
            errorOccured = true;
            e.printStackTrace();
        }
    }

    public boolean errorOccured()
    {
        return errorOccured;
    }
}


