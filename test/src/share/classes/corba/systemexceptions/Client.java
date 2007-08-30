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
// Created       : 2003 Apr 09 (Wed) 16:54:21 by Harold Carr.
// Last Modified : 2004 Jan 31 (Sat) 10:06:37 by Harold Carr.
//

package corba.systemexceptions;

import javax.naming.InitialContext;
import javax.rmi.CORBA.Util;

import javax.activity.ActivityRequiredException;
import javax.activity.ActivityCompletedException;
import javax.activity.InvalidActivityException;

import corba.framework.Controller;
import corba.hcks.C;
import corba.hcks.U;

import com.sun.corba.se.impl.orbutil.ORBUtility;

import org.omg.CORBA.*;
import org.omg.PortableInterceptor.*;

public class Client extends org.omg.CORBA.LocalObject 
    implements ORBInitializer, ClientRequestInterceptor {

    public static final String baseMsg = Client.class.getName();
    public static final String main = baseMsg + ".main";

    public static ORB orb;
    public static InitialContext initialContext;
    
    private static String excs[] = { 
	"org.omg.CORBA.ACTIVITY_COMPLETED", "org.omg.CORBA.ACTIVITY_REQUIRED",
	"org.omg.CORBA.BAD_QOS", "org.omg.CORBA.CODESET_INCOMPATIBLE",
	"org.omg.CORBA.INVALID_ACTIVITY", "org.omg.CORBA.REBIND",
	"org.omg.CORBA.TIMEOUT", "org.omg.CORBA.TRANSACTION_MODE",
	"org.omg.CORBA.TRANSACTION_UNAVAILABLE", "org.omg.CORBA.UNKNOWN" };
	
    static int counter; // counter

    public static void main(String[] av) {

        try {
	    U.sop(main + " starting");

	    if (! ColocatedClientServer.isColocated) {
		U.sop(main + " : creating ORB.");
		orb = ORB.init(av, null);
		U.sop(main + " : creating InitialContext.");
		initialContext = C.createInitialContext(orb);
	    }

	    // RMI invocations

	    rmiiI rmiiIPOA = (rmiiI) U.lookupAndNarrow(Server.rmiiIPOA,
						 rmiiI.class, initialContext);
	    U.sop("\nRMI invocations:\n");
	    int i = 0;
	    for (counter = 0, i = 0; i < 10; i++, counter++) {
		try {
		    rmiiIPOA.invoke(i);
		} catch (java.rmi.RemoteException re) {
		    SystemException se = (SystemException) re.getCause();
		    if (se instanceof ACTIVITY_REQUIRED) {
			if (!(re instanceof ActivityRequiredException)) {
			    throw new RuntimeException("Test Failed");
			}
			U.sop("javax.activity.ActivityRequiredException");
		    } else if (se instanceof ACTIVITY_COMPLETED) {
			if (!(re instanceof ActivityCompletedException)) {
			    throw new RuntimeException("Test Failed");
			}
			U.sop("javax.activity.ActivityCompletedException");
		    } else if (se instanceof INVALID_ACTIVITY) {
			if (!(re instanceof InvalidActivityException)) {
			    throw new RuntimeException("Test Failed");
			}
			U.sop("javax.activity.InvalidActivityException");
		    }
		    String name = se.getClass().getName();
		    U.sop("name: " + name + ", minorCode: " + se.minor +
			  ", completed: " + 
			  ((se.completed.value() == 
			   CompletionStatus._COMPLETED_YES) ?
			   "true" : "false") + "\n");
		    if (!(name.equals(excs[i]))) {
			throw new RuntimeException("Test Failed");
		    }
		}
	    }

	    // IDL invocations

	    idlI idlIPOA = idlIHelper.narrow(U.resolve(Server.idlIPOA, orb));
	    U.sop("IDL invocations:\n");
	    for (counter = 0, i = 0; i < 10; i++, counter++) {
		try {
		    idlIPOA.invoke(i);
		} catch (org.omg.CORBA.SystemException se) {
		    String name = se.getClass().getName();
		    U.sop("name: " + name + ", minorCode: " + se.minor +
			  ", completed: " + 
			  ((se.completed.value() == 
			   CompletionStatus._COMPLETED_YES) ?
			   "true" : "false") + "\n");
		    if (!(name.equals(excs[i]))) {
			throw new RuntimeException("Test Failed");
		    }
		}
	    }

	    orb.shutdown(true);

        } catch (Exception e) {
            U.sopUnexpectedException(main + " : ", e);
	    System.exit(1);
        }
	U.sop(main + " ending successfully");
	System.exit(Controller.SUCCESS);
    }

    ////////////////////////////////////////////////////
    //    
    // ORBInitializer interface implementation.
    //

    public void pre_init(ORBInitInfo info) 
    {
    }

    public void post_init(ORBInitInfo info) 
    {
        // register the interceptors.
        try {
            info.add_client_request_interceptor(this);
        } catch (org.omg.PortableInterceptor.ORBInitInfoPackage.DuplicateName e) {
            throw new org.omg.CORBA.INTERNAL();
        }
        U.sop("ORBInitializer.post_init completed");
    }

    ////////////////////////////////////////////////////
    //
    // implementation of the Interceptor interface.
    //

    public String name() 
    {
        return "ClientInterceptor";
    }

    public void destroy() 
    {
    }

    ////////////////////////////////////////////////////
    //    
    // implementation of the ClientInterceptor interface.
    //

    public void send_request(ClientRequestInfo ri) throws ForwardRequest 
    {
    }

    public void send_poll(ClientRequestInfo ri) 
    {
    }

    public void receive_reply(ClientRequestInfo ri) 
    {    
    }

    public void receive_exception(ClientRequestInfo ri) throws ForwardRequest 
    {
	String repID = ri.received_exception_id();
	String className = ORBUtility.classNameOf(repID);
	U.sop("receive_exception.repID: " + repID);
	U.sop("receive_exception.className: " + className);
	if ( !(className.equals(excs[counter])) ) {
	    throw new RuntimeException("Test Failed");
	}
    }

    public void receive_other(ClientRequestInfo ri) throws ForwardRequest 
    {
    }
}

// End of file.

