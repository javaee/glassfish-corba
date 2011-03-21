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
//
// Created       : 2003 Apr 09 (Wed) 16:28:12 by Harold Carr.
// Last Modified : 2003 May 19 (Mon) 13:33:14 by Harold Carr.
//

package corba.islocal;

import javax.naming.InitialContext;

import org.omg.CORBA.Policy;
import org.omg.PortableServer.POA;
import org.omg.PortableServer.ServantRetentionPolicyValue;

import corba.framework.Controller;
import corba.framework.Options;
import corba.hcks.C;
import corba.hcks.U;

import com.sun.corba.se.spi.transport.TransportManager;
import com.sun.corba.se.spi.orb.ORB;
import com.sun.corba.se.impl.legacy.connection.LegacyServerSocketManagerImpl;

public class Server 
{
    public static final String baseMsg = Server.class.getName();
    public static final String main = baseMsg + ".main";
    public static final String thisPackage = 
	Server.class.getPackage().getName();

    public static final String rmiiIServantPOA_Tie = 
	thisPackage + "._rmiiIServantPOA_Tie";

    public static final String idlIConnect  = "idlIConnect";
    public static final String idlIPOA      = "idlIPOA";
    public static final String rmiiIConnect = "rmiiIConnect";
    public static final String rmiiIConnectDifferentLoader =
	"rmiiIConnectDifferentLoader";
    public static final String rmiiIPOA     = "rmiiIPOA";
    public static final String SLPOA        = "SLPOA";

    public static ORB orb;
    public static InitialContext initialContext;
    public static TransportManager transportManager;
    public static POA rootPOA;
    public static POA slPOA;

    public static CustomClassLoader loader;

    public static void main(String[] av)
    {
        try {
	    U.sop(main + " starting");

	    if (! ColocatedClientServer.isColocated) {
		U.sop(main + " : creating ORB.");
		orb = (ORB) ORB.init(av, null);
		U.sop(main + " : creating InitialContext.");
		initialContext = C.createInitialContext(orb);
	    }

	    rootPOA = U.getRootPOA(orb);
            rootPOA.the_POAManager().activate();

	    Policy[] policies = U.createUseServantManagerPolicies(
                rootPOA,
		ServantRetentionPolicyValue.NON_RETAIN);
	    slPOA = U.createPOAWithServantManager(rootPOA, SLPOA, policies,
						  new MyServantLocator(orb));

	    //
	    // IDL references.
	    //

	    U.createWithConnectAndBind(idlIConnect, 
				       new idlIServantConnect(), orb);
	    U.createWithServantAndBind(idlIPOA,
				       new idlIServantPOA(), rootPOA, orb);

	    //
	    // RMI-IIOP references.
	    //

	    Object rmiiIServantConnectInstance;
	    ClassLoader classLoader;

	    System.out.println("getSystemClassLoader: "
			       + ClassLoader.getSystemClassLoader());

	    // Create one in standard class loader.

	    rmiiIServantConnectInstance = new rmiiIServantConnect();
	    classLoader = 
		rmiiIServantConnectInstance.getClass().getClassLoader();
	    System.out.println("rmiiIServantConnectInstance: " +
			       rmiiIServantConnectInstance);
	    System.out.println("rmiiIServantConnectInstance classLoader: " +
			       classLoader);
	    initialContext.rebind(rmiiIConnect, rmiiIServantConnectInstance);

	    // Create one is a different class loader.

	    U.createRMIPOABind(C.rmiiSL, rmiiIServantPOA_Tie,
			       slPOA, orb, initialContext);

	    // Create a POA-based RMI-IIOP Servant

	    /* REVISIT
	    U.createWithServantAndBind(rmiiIPOA,
				       new rmiiIServantPOA(), rootPOA, 
				       (org.omg.CORBA.ORB) orb);
	    */

	    U.sop(main + " ready");
	    U.sop(Options.defServerHandshake);
	    System.out.flush();

	    synchronized (ColocatedClientServer.signal) {
		ColocatedClientServer.signal.notifyAll();
	    }
	    
	    orb.run();

        } catch (Exception e) {
	    U.sopUnexpectedException(main, e);
	    System.exit(1);
        }
	U.sop(main + " ending successfully");
	System.exit(Controller.SUCCESS);
    }

    public static String filter(String a, String msg)
    {
	return a + " (echo from " + msg + ")";
    }

    public static void 	checkThread(String msg)
    {
	if (ColocatedClientServer.isColocated) {
	    if (Client.clientThread == Thread.currentThread()) {
		U.sop("NOTE: " 
		      + msg
		      + ": colocated call correctly running in server on client thread");
	    } else {
		Client.errors++;
		U.sop("!!! " + msg + ": incorrect thread !!!");
	    }
	}
    }
}

// End of file.

