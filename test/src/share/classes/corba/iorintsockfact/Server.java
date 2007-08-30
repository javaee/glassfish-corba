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
// Created       : 2002 Jul 19 (Fri) 14:48:59 by Harold Carr.
// Last Modified : 2002 Jul 22 (Mon) 12:05:48 by Harold Carr.
//

package corba.iorintsockfact;

import java.util.Properties ;

import org.omg.CORBA.ORB;
import org.omg.CORBA.Policy;
import org.omg.PortableServer.LifespanPolicyValue;
import org.omg.PortableServer.POA;

class IServant extends IPOA
{
    public IServant()
    {
    }

    public String m(String x)
    {
        return "Server echoes: " + x;
    }
}

/**
 * @author Harold Carr
 */
public class Server
{
    public static final String baseMsg = Common.class.getName();

    public static ORB orb;
    public static POA rootPoa;
    public static POA childPoa;

    public static void main(String av[])
    {
        try {

	    Properties props = System.getProperties();

	    props.setProperty("org.omg.PortableInterceptor.ORBInitializerClass." + ServerORBInitializer.class.getName(),
			      "dummy");

            props.put(Common.SOCKET_FACTORY_CLASS_PROPERTY,
		      Common.CUSTOM_FACTORY_CLASS);

            orb = ORB.init(av, props);

	    createAndBind(Common.serverName1);
      
            System.out.println ("Server is ready.");

            orb.run();
            
        } catch (Exception e) {
            System.out.println(baseMsg + e);
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

            rootPoa = (POA)
		orb.resolve_initial_references("RootPOA");
	    rootPoa.the_POAManager().activate();

	    // Create child POAs.

            Policy[] policies = new Policy[1];

	    // Create child POA
            policies[0] =
		rootPoa.create_lifespan_policy(LifespanPolicyValue.TRANSIENT);
            childPoa = rootPoa.create_POA("childPoa", null, policies);
            childPoa.the_POAManager().activate();
	}

	// create servant and register it with the ORB

	IServant iServant = new IServant();
	byte[] id = childPoa.activate_object(iServant);
	org.omg.CORBA.Object ref = childPoa.id_to_reference(id);

	Common.getNameService(orb).rebind(Common.makeNameComponent(name), ref);
    }
}

// End of file.
