/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2004-2007 Sun Microsystems, Inc. All rights reserved.
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

package corba.requestpartitioning;

import com.sun.corba.se.spi.orb.ORB;
import com.sun.corba.se.spi.orbutil.ORBConstants;

import corba.hcks.U;
import java.rmi.RemoteException;
import java.util.Properties;

import org.omg.CosNaming.NamingContextExt;
import org.omg.CosNaming.NamingContextExtHelper;

public class Client
{
    protected final static int stringSize = 10000;
    protected final static String stringOf36 =
       	   "abcdefghijklmnopqrstuvwxyz0123456789";
    protected String reallyReallyBigString = null;
    protected Tester itsTester = null;
    protected ORB itsOrb = null;

    public Client(String[] args) throws Exception {

	Properties props = System.getProperties();

	itsOrb = (ORB)org.omg.CORBA.ORB.init(args, props);

	initializeReallyBigString();
    }

    protected void initializeReallyBigString() {
	StringBuffer sb = new StringBuffer(stringSize);
	int index = 0;
        final int lengthOfStr = stringOf36.length();
	for (int i = 0; i < stringSize; i++) {
	    index = i % lengthOfStr;
	    sb.append(stringOf36.charAt(index));
	}
        reallyReallyBigString = sb.toString();
    }

    protected void printError(int myPoolId, int remotePoolId)
	    throws Exception {
	StringBuffer error =  new StringBuffer(80);
	error.append("FAILED: client requested thread pool id (");
	error.append(myPoolId);
	error.append(") not executed on expected server thread pool id (");
	error.append(remotePoolId).append(")");
	U.sop(error.toString());
	throw new Exception(error.toString());
    }

    protected void runTest() throws RemoteException, Exception {

	U.sop("Getting name service...");
	org.omg.CORBA.Object objRef =
	    itsOrb.resolve_initial_references("NameService");
	NamingContextExt ncRef = NamingContextExtHelper.narrow(objRef);
	U.sop("Got name service.");

	int expectedPoolId;
	int returnedPoolId;
	for (int i = 0; i < TestThreadPoolManager.NUMBER_OF_THREAD_POOLS_TO_CREATE; i++)
	{
	    String name = "Tester" + i;
            U.sop("Finding, looking up & narrowing " + name + " ...");
	    itsTester = TesterHelper.narrow(ncRef.resolve_str(name));
            U.sop("Got " + name + " ...");

            U.sop("Testing thread pool id (" + i + ") usage...");
	    expectedPoolId = i;
	    returnedPoolId =
	        itsTester.getThreadPoolIdForThisRequest(reallyReallyBigString);
	    if (expectedPoolId != returnedPoolId) {
	        printError(expectedPoolId, returnedPoolId);
	    }
            U.sop("Thead pool (" + i + ") test PASSED.");
	}

	String defaultname = "DefaultTester";
        U.sop("Finding, looking up & narrowing " + defaultname + " ...");
	itsTester = TesterHelper.narrow(ncRef.resolve_str(defaultname));
        U.sop("Got " + defaultname + " ...");

        U.sop("Testing DEFAULT thread pool usage...");
	expectedPoolId = 0;
	returnedPoolId =
	    itsTester.getThreadPoolIdForThisRequest(reallyReallyBigString);
	if (expectedPoolId != returnedPoolId) {
	    printError(expectedPoolId, returnedPoolId);
	}
        U.sop("Default thead pool test PASSED.");

        U.sop("All thread pool tests PASSED.");
    }

    public static void main(String args[]) {
	try {

            U.sop("Beginning test...");

	    Client client = new Client(args);
	    client.runTest();

            U.sop("Test finished successfully...");

        } catch (Throwable t) {
    	    U.sop("Unexpected throwable...");
            t.printStackTrace();
            System.exit(1);
        }
    }
}

