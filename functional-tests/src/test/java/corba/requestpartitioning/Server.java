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

package corba.requestpartitioning;

import java.util.Properties;
import com.sun.corba.ee.spi.orb.ORB;
import com.sun.corba.ee.spi.misc.ORBConstants;
import com.sun.corba.ee.spi.extension.RequestPartitioningPolicy;
import com.sun.corba.ee.spi.threadpool.ThreadPoolManager;

import corba.framework.Options;
import corba.hcks.U;

import org.omg.CORBA.Policy;
import org.omg.CosNaming.NamingContextExt;
import org.omg.CosNaming.NamingContextExtHelper;
import org.omg.CosNaming.NameComponent;
import org.omg.PortableServer.POA;
import org.omg.PortableServer.POAHelper;

//
// Created      : 2004 June 2, 2004 by Charlie Hunt
// Last Modified: 2004 June 2, 2004 by Charlie Hunt
//

public class Server
{
    private static ORB orb = null;

    public static void main(String[] args)
    {
        Properties props = System.getProperties();
        try
        {
            orb = (ORB)org.omg.CORBA.ORB.init(args, props);

            // set custom thread pool manager
            ThreadPoolManager threadPoolManager =
                          TestThreadPoolManager.getThreadPoolManager();
            orb.setThreadPoolManager(threadPoolManager);

            // Get a reference to rootpoa
            POA rootPOA = POAHelper.narrow(
                   orb.resolve_initial_references(ORBConstants.ROOT_POA_NAME)); 

            // Create servant and register it with the ORB
            TesterImpl testerImpl = new TesterImpl();

            U.sop("Creating a request partitioning policy with -1...");
            try {
                Policy policy[] = new Policy[1];
                policy[0] = new RequestPartitioningPolicy(-1);
                throw new Exception("new RequestPartitionPolicy(-1) was not rejected when it should have been!");
            }
            catch (Exception ex) {
                U.sop("Received expected exception...");
            }
            U.sop("Creating a request partitioning policy with 64...");
            try {
                Policy policy[] = new Policy[1];
                policy[0] = new RequestPartitioningPolicy(64);
                throw new Exception("new RequestPartitionPolicy(64) was not rejected when it should have been!");
            }
            catch (Exception ex) {
                U.sop("Received expected exception...");
            }

            org.omg.CORBA.Object objRef =
                             orb.resolve_initial_references("NameService");
            NamingContextExt ncRef = NamingContextExtHelper.narrow(objRef);
            NameComponent[] path = null;

            POA[] poa = new POA[TestThreadPoolManager.NUMBER_OF_THREAD_POOLS_TO_CREATE];
            Policy policy[] = new Policy[1];

            for (int i = 0; i < poa.length; i++) {
                policy[0] = new RequestPartitioningPolicy(i);
                String poaName = "POA-Tester" + i;
                poa[i] = rootPOA.create_POA(poaName, null, policy);
                poa[i].activate_object(testerImpl);

                org.omg.CORBA.Object ref = 
                       poa[i].servant_to_reference(testerImpl);
                Tester testerRef = TesterHelper.narrow(ref);

                String name = "Tester" + i;
                path = ncRef.to_name(name);
                ncRef.rebind(path, testerRef);

                poa[i].the_POAManager().activate();
            }
    
            // create one POA for default thread pool
            String specialPoaName = "POA-Default-Tester";
            POA specialPoa = rootPOA.create_POA(specialPoaName, null, null);
            specialPoa.activate_object(testerImpl);
            org.omg.CORBA.Object sref = 
                       specialPoa.servant_to_reference(testerImpl);
            Tester specialTesterRef = TesterHelper.narrow(sref);
            String sname = "DefaultTester";
            path = ncRef.to_name(sname);
            ncRef.rebind(path, specialTesterRef);
            specialPoa.the_POAManager().activate();

            U.sop(Options.defServerHandshake);

            orb.run();

        } catch (Throwable t) {
            U.sop("Unexpected throwable...");
            t.printStackTrace();
            System.exit(1);
        }
        U.sop("Ending successfully...");
    }
}
