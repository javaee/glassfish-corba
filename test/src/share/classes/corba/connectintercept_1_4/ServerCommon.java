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
// Created       : by Everett Anderson.
// Last Modified : 2004 Apr 14 (Wed) 19:26:04 by Harold Carr.
//

package corba.connectintercept_1_4;

import java.util.Properties ;

import org.omg.CORBA.ORB;
import org.omg.CORBA.Policy;
import org.omg.PortableServer.LifespanPolicyValue;
import org.omg.PortableServer.POA;

import com.sun.corba.se.impl.orbutil.ORBConstants;

class ExIServant extends ExIPOA
{
    public ORB orb;

    public ExIServant(ORB orb) 
    {
	this.orb = orb;
    }

    public String sayHello()
    {
        return "Hello world!";
    }
}

public class ServerCommon
{
    public static final String baseMsg = ServerCommon.class.getName();

    public static ORB orb;
    public static POA rootPoa;
    public static POA childPoa;

    // The same server code is used conditionally for both
    // persistent and transient servers.
    public static boolean isTransient;


    public static void main(String av[])
    {
	if (av[0].equals(Common.Transient)) {
	    isTransient = true;
	} else if (av[0].equals(Common.Persistent)) {
	    isTransient = false;
	} else {
	    System.out.println(baseMsg + ".main: unknown: " + av[0]);
	    System.exit(-1);
	}
	    
        try {

	    Properties props = System.getProperties();

	    props.setProperty(Common.ORBClassKey, MyPIORB.class.getName());

	    props.setProperty(ORBConstants.PI_ORB_INITIALIZER_CLASS_PREFIX +
			      ServerORBInitializer.class.getName(),
			      "dummy");

	    props.setProperty(ORBConstants.LEGACY_SOCKET_FACTORY_CLASS_PROPERTY,
			      Common.CUSTOM_FACTORY_CLASS);

	    String value;
	    if (isTransient) {
		// It makes sense to assign specific ports for
		// transient servers.
		value =
		    Common.MyType1 + ":" + Common.MyType1TransientPort + "," +
		    Common.MyType2 + ":" + Common.MyType2TransientPort + "," +
		    Common.MyType3 + ":" + Common.MyType3TransientPort;
	    } else {
		// It makes sense to assign emphemeral ports
		// to persistent servers since the ORBD will most
		// likely be assigned the fixed ports.
		value =
		    Common.MyType1 + ":" + Common.MyType1PersistentPort + "," +
		    Common.MyType2 + ":" + Common.MyType2PersistentPort + "," +
		    Common.MyType3 + ":" + Common.MyType3PersistentPort;
	    }
	    props.setProperty(ORBConstants.LISTEN_SOCKET_PROPERTY, value);

	    // REVISIT: not sure why I have to explicitly set these here
	    // but not in other tests.
	    props.setProperty(ORBConstants.INITIAL_PORT_PROPERTY, "1049");

            orb = ORB.init(av, props);

	    createAndBind(Common.serverName1);
	    createAndBind(Common.serverName2);
      
            System.out.println ("Server is ready.");

            orb.run();
            
        } catch (Exception e) {
            System.out.println(baseMsg + ".main: ERROR: " + e);
            e.printStackTrace(System.out);
            System.exit(1);
        }
    }

    public static void createAndBind (String name)
	throws
	    Exception
    {
        if (rootPoa == null) {
	    // Get rootPOA
            rootPoa =
	      (POA)orb.resolve_initial_references(ORBConstants.ROOT_POA_NAME);
	    rootPoa.the_POAManager().activate();

	    // Create POAs.

            Policy[] policies = new Policy[1];

	    // Create child POA
            policies[0] =
		isTransient ?
		rootPoa.create_lifespan_policy(LifespanPolicyValue.TRANSIENT):
		rootPoa.create_lifespan_policy(LifespanPolicyValue.PERSISTENT);
            childPoa =rootPoa.create_POA("childPoa", null, policies);
            childPoa.the_POAManager().activate();
	}

	// REVISIT - bind a root and transient.

	// create servant and register it with the ORB
	ExIServant exIServant = new ExIServant(orb);
	byte[] id = childPoa.activate_object(exIServant);
	org.omg.CORBA.Object ref = childPoa.id_to_reference(id);

	Common.getNameService(orb).rebind(Common.makeNameComponent(name), ref);
    }
}

// End of file.
