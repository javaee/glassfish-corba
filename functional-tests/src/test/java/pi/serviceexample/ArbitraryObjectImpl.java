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

package pi.serviceexample;

import org.omg.CORBA.ORB;
import org.omg.CORBA.ORBPackage.InvalidName;
import org.omg.CosNaming.*;
import org.omg.CosNaming.NamingContextPackage.*;
import org.omg.PortableServer.POA;

import java.util.Properties;

class ArbitraryObjectImpl
    extends ArbitraryObjectPOA
{
    public static ORB orb;

    private AService aService;

    //
    // The IDL operations.
    //

    public String arbitraryOperation1(String a1)
    {
        verifyService();
        return "I got this from the client: " + a1;
    }

    public void arbitraryOperation2 (int a1)
    {
        verifyService();
    }

    public void arbitraryOperation3(String a1)
        throws ArbitraryObjectException
    {
        verifyService();
        if (a1.equals("throw exception")) {
            throw new ArbitraryObjectException("because you told me to");
        }
    }

    private void verifyService()
    {
        getAService().verify();
    }

    private AService getAService()
    {
        // Only look up the service once, then cache it.

        if (aService == null) {
            try {
                aService =      
                    AServiceHelper.narrow(
                        orb.resolve_initial_references("AService"));
            } catch (InvalidName e) {
                System.out.println("Exception handling not shown.");
            }
        }
        return aService;
    }

    //
    // The server.
    //

    public static void main(String[] av)
    {
        try {
            if (orb == null) {
                Properties props = new Properties();
                props.put("org.omg.PortableInterceptor.ORBInitializerClass."
                          + "pi.serviceexample.AServiceORBInitializer",
                          "");
                props.put("org.omg.PortableInterceptor.ORBInitializerClass."
                          + "pi.serviceexample.LoggingServiceServerORBInitializer",
                          "");
                orb = ORB.init(av, props);
            }
            
            POA rootPOA =  (POA) orb.resolve_initial_references("RootPOA");
            // Create a POA so the IOR interceptor executes.
            POA childPOA = rootPOA.create_POA("childPOA", null, null);
            childPOA.the_POAManager().activate();
            
            byte[] objectId =
                childPOA.activate_object(new ArbitraryObjectImpl());
            org.omg.CORBA.Object ref = childPOA.id_to_reference(objectId);

            NamingContext nameService = 
                NamingContextHelper.narrow(
                    orb.resolve_initial_references("NameService"));
            NameComponent path[] =
                { new NameComponent("ArbitraryObject", "") };
            nameService.rebind(path, ref);

            System.out.println("ArbitaryObject ready.");

            orb.run();

        } catch (Exception e) {
            e.printStackTrace();
            System.exit(-1);
        }
        System.exit(0);
    }
}

// End of file.

