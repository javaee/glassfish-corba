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

package hopper.h4620926;

import org.omg.CORBA.*;
import org.omg.PortableServer.*;
import org.omg.CosNaming.*;
import test.*;
import java.util.Properties ;

class HelloServant extends HelloPOA {
    public String sayHello() {
        return "Hello";
    }
}

public class Server {

    public static int delay = 100;

    public static void main(String[] args) {

        try {

            // try {
                // delay = Integer.parseInt(args[0]);
            // } catch (Exception e) { }
            
            Properties props = new Properties() ;
            props.setProperty( "com.sun.corba.ee.ORBDebug", "poa" ) ;
            ORB orb = ORB.init(args, props);

            POA rootPOA = POAHelper.narrow(
                orb.resolve_initial_references("RootPOA"));
            Policy[] policy = new Policy[2];
            policy[0] = rootPOA.create_request_processing_policy(
                RequestProcessingPolicyValue.USE_SERVANT_MANAGER);
            policy[1] = rootPOA.create_id_assignment_policy(
                IdAssignmentPolicyValue.USER_ID);

            POA childPOA = rootPOA.create_POA("Child", null, policy);
            childPOA.set_servant_manager(new MyServantActivator());
            System.out.println("Set servant manager");
        
            String str = "ABCRef";
            org.omg.CORBA.Object obj = childPOA.create_reference_with_id(
                str.getBytes(), "IDL:test/Hello:1.0");
            childPOA.the_POAManager().activate();

            Hello ref = HelloHelper.narrow(obj);
            NamingContext namingContext = NamingContextHelper.narrow(
            orb.resolve_initial_references("NameService"));
            NameComponent[] name = { new NameComponent("Hello", "") };

            namingContext.rebind(name, ref);
            System.out.println("Servant registered");

            System.out.println("Server is ready.");

            orb.run();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}

class MyServantActivator extends LocalObject implements ServantActivator {

    public Servant incarnate(byte[] oid, POA adapter) {

        System.out.println("Incarnating Object - " + new String(oid) +
                           " in POA - " + adapter.the_name());
        try {
            System.out.println("Sleeping for " + Server.delay + "msecs");
            Thread.sleep(Server.delay);
        } catch (Exception e) { }
        return new HelloServant();
    }

    public void etherealize(byte[] oid, POA adapter, Servant servant,
                            boolean cleanUpInProgress, 
                            boolean remaingActivations) {
        System.out.println("Etherealizing Object ");
    }
}
