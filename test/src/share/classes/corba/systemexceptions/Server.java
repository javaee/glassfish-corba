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
//
// Created       : 2003 Apr 09 (Wed) 16:28:12 by Harold Carr.
// Last Modified : 2004 Jan 31 (Sat) 11:12:48 by Harold Carr.
//

package corba.systemexceptions;

import javax.naming.InitialContext;

import org.omg.PortableServer.POA;
import org.omg.PortableServer.Servant;

import corba.framework.Controller;
import corba.framework.Options;
import corba.hcks.C;
import corba.hcks.U;

import com.sun.corba.se.spi.orb.ORB;

import java.rmi.Remote; 
import java.rmi.RemoteException; 
import javax.rmi.PortableRemoteObject;

import org.omg.CORBA.*;

interface rmiiI extends Remote {
    void invoke(int excType) throws RemoteException;
}

class rmiiIServantPOA extends PortableRemoteObject implements rmiiI {

    rmiiIServantPOA() throws RemoteException {
	// DO NOT CALL SUPER - that would connect the object.
    }

    public void invoke(int excType) {
	Server.invoke(excType);
    }
}

class idlIServantPOA extends idlIPOA {

    public void invoke(int excType) {
	Server.invoke(excType);
    }
}

public class Server extends org.omg.CORBA.LocalObject {

    public static final String baseMsg = Server.class.getName();
    public static final String main = baseMsg + ".main";
    public static final String thisPackage = 
	Server.class.getPackage().getName();

    public static final String rmiiIServantPOA_Tie = 
	thisPackage + "._rmiiIServantPOA_Tie";

    public static final String rmiiIPOA = "rmiiIPOA";
    public static final String idlIPOA = "idlIPOA";

    public static ORB orb;
    public static InitialContext initialContext;

    static void invoke(int excType) {

	switch (excType) {

	case 0: 
	    U.sop("ACTIVITY_COMPLETED");
	    throw new ACTIVITY_COMPLETED("ACTIVITY_COMPLETED", 
					 100, CompletionStatus.COMPLETED_YES);
	case 1: 
	    U.sop("ACTIVITY_REQUIRED");
	    throw new ACTIVITY_REQUIRED("ACTIVITY_REQUIRED", 
					101, CompletionStatus.COMPLETED_YES);
	case 2: 
	    U.sop("BAD_QOS");
	    throw new BAD_QOS("BAD_QOS", 102, CompletionStatus.COMPLETED_YES);
	case 3: 
	    U.sop("CODESET_INCOMPATIBLE");
	    throw new CODESET_INCOMPATIBLE("CODESET_INCOMPATIBLE",
					   103,
					   CompletionStatus.COMPLETED_YES);
	case 4:
	    U.sop("INVALID_ACTIVITY");
	    throw new INVALID_ACTIVITY("INVALID_ACTIVITY", 
				       104, CompletionStatus.COMPLETED_YES);
	case 5:
 	    U.sop("REBIND");
	    throw new REBIND("REBIND", 105, CompletionStatus.COMPLETED_YES);
	case 6:
	    U.sop("TIMEOUT");
	    throw new TIMEOUT("TIMEOUT", 106, CompletionStatus.COMPLETED_YES);
	case 7:
	    U.sop("TRANSACTION_MODE");
	    throw new TRANSACTION_MODE("TRANSACTION_MODE", 
				       107, CompletionStatus.COMPLETED_YES);
	case 8:
	    U.sop("TRANSACTION_UNAVAILABLE");
	    throw new TRANSACTION_UNAVAILABLE("TRANSACTION_UNAVAILABLE",
					      108,
					      CompletionStatus.COMPLETED_YES);
	default:
	    U.sop("UNKNOWN");
	    throw new UNKNOWN("UNKNOWN", 109, CompletionStatus.COMPLETED_YES);
	}
    }

    public static void main(String[] av) {

        try {
	    U.sop(main + " starting");

	    if (! ColocatedClientServer.isColocated) {
		U.sop(main + " : creating ORB.");
		orb = (ORB) ORB.init(av, null);
		U.sop(main + " : creating InitialContext.");
		initialContext = C.createInitialContext(orb);
	    }

	    POA rootPOA = U.getRootPOA(orb);
            rootPOA.the_POAManager().activate();

            // RMI-IIOP references.
            U.sop("Creating/binding RMI-IIOP references.");
            Servant servant = (Servant)
                javax.rmi.CORBA.Util.getTie(new rmiiIServantPOA());
            U.createWithServantAndBind(rmiiIPOA, servant, rootPOA, orb);

            // IDL references.
            U.sop("Creating/binding IDL references.");
            U.createWithServantAndBind(idlIPOA,
                                       new idlIServantPOA(), rootPOA, orb);

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
}

// End of file.


