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
// Created       : 2005 Oct 05 (Wed) 13:54:09 by Harold Carr.
// Last Modified : 2005 Oct 06 (Thu) 11:32:08 by Harold Carr.
//

package corba.lbq;

import java.util.Properties;

import org.omg.CORBA.Policy ;

import org.omg.PortableServer.POA;
import org.omg.PortableServer.Servant;
import org.omg.PortableServer.LifespanPolicyValue;
import org.omg.PortableServer.IdUniquenessPolicyValue;
import org.omg.PortableServer.IdAssignmentPolicyValue;
import org.omg.PortableServer.ImplicitActivationPolicyValue;
import org.omg.PortableServer.ServantRetentionPolicyValue;
import org.omg.PortableServer.RequestProcessingPolicyValue;

import com.sun.corba.se.spi.orb.ORB ;

/**
 * @author Harold Carr
 */
public class Server
{
    private static final String baseMsg = Server.class.getName();
    private static final String RootPOA = "RootPOA";
    private static final long SERVER_RUN_LENGTH = 1000 * 60; // 1 minute

    public static void main(String[] av)
    {
	try {
	    Properties props = new Properties();
	    // props.setProperty("com.sun.CORBA.ORBDebug","transport");

	    ORB orb = (ORB)org.omg.CORBA.ORB.init(av, props);

	    POA rootPOA = (POA) orb.resolve_initial_references("RootPOA");
	    Policy[] policies = new Policy[] {
		rootPOA.create_lifespan_policy( 
		    LifespanPolicyValue.PERSISTENT ),
		rootPOA.create_id_uniqueness_policy( 
		    IdUniquenessPolicyValue.UNIQUE_ID ),
		rootPOA.create_id_assignment_policy( 
		    IdAssignmentPolicyValue.USER_ID ),
		rootPOA.create_implicit_activation_policy( 
		    ImplicitActivationPolicyValue.NO_IMPLICIT_ACTIVATION ),
		rootPOA.create_servant_retention_policy(
		    ServantRetentionPolicyValue.RETAIN ),
		rootPOA.create_request_processing_policy(
		    RequestProcessingPolicyValue.USE_ACTIVE_OBJECT_MAP_ONLY )
	    } ;

	    POA testPOA = rootPOA.create_POA( "testPOA", rootPOA.the_POAManager(), 
		policies ) ;

	    rootPOA.the_POAManager().activate();

	    Servant servant = (Servant)
		javax.rmi.CORBA.Util.getTie(new TestServant());

	    createWithServantAndBind(Common.ReferenceName, servant, 
				     testPOA, orb);

	    System.out.println("--------------------------------------------");
	    System.out.println("Server is ready.");
	    System.out.println("--------------------------------------------");

	    Thread.sleep(SERVER_RUN_LENGTH);

	    System.out.println("--------------------------------------------");
	    System.out.println("Server exiting correctly...");
	    System.out.println("--------------------------------------------");
	    System.exit(0);

	} catch (Exception e) {
	    e.printStackTrace(System.out);
	    System.out.println("--------------------------------------------");
	    System.out.println("!!!! Server exiting INCORRECTLY...");
	    System.out.println("--------------------------------------------");
	    System.exit(1);
	}
    }

    public static org.omg.CORBA.Object 
	createWithServantAndBind (String  name,
				  Servant servant,
				  POA     poa,
				  ORB     orb)
	throws
	    Exception
    {
	byte[] id = name.getBytes();
	poa.activate_object_with_id(id, servant);
	org.omg.CORBA.Object ref = poa.id_to_reference(id);
	Common.rebind(name, ref, orb);
	return ref;
    }

} 

// End of file.
