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
// Created       : 2005 Oct 05 (Wed) 13:54:09 by Harold Carr.
// Last Modified : 2005 Oct 06 (Thu) 11:32:08 by Harold Carr.
//

package corba.retryplugin;

import java.util.Properties;

import org.omg.CORBA.ORB;
import org.omg.PortableServer.POA;
import org.omg.PortableServer.Servant;

import com.sun.corba.ee.impl.plugin.hwlb.RetryServerRequestInterceptor ;

import com.sun.corba.ee.spi.misc.ORBConstants ;

/**
 * @author Harold Carr
 */
public class Server
{
    static {
        // This is needed to guarantee that this test will ALWAYS use dynamic
        // RMI-IIOP.  Currently the default is dynamic when renamed to "ee",
        // but static in the default "se" packaging, and this test will
        // fail without dynamic RMI-IIOP.
        System.setProperty( ORBConstants.USE_DYNAMIC_STUB_PROPERTY, "true" ) ;
    }

    private static final String baseMsg = Server.class.getName();
    private static final String RootPOA = "RootPOA";
    private static final long SERVER_RUN_LENGTH = 1000 * 60; // 1 minute

    public static void main(String[] av)
    {
        try {

            Properties props = new Properties();

            props.setProperty(
                "org.omg.PortableInterceptor.ORBInitializerClass."
                + RetryServerRequestInterceptor.class.getName(),
                "dummy");

            ORB orb = ORB.init(av, props);

            POA rootPOA = (POA) orb.resolve_initial_references("RootPOA");
            rootPOA.the_POAManager().activate();

            Servant servant = (Servant)
                javax.rmi.CORBA.Util.getTie(new TestServant());

            createWithServantAndBind(Common.ReferenceName, servant, 
                                     rootPOA, orb);

            System.out.println("--------------------------------------------");
            System.out.println("Server is ready.");
            System.out.println("--------------------------------------------");

            long startTime = System.currentTimeMillis();
            while (System.currentTimeMillis() - startTime < SERVER_RUN_LENGTH){
                RetryServerRequestInterceptor.setRejectingRequests(true);
                Thread.sleep(1000 * 10);
                RetryServerRequestInterceptor.setRejectingRequests(false);
                Thread.sleep(1000 * 10);
            }

            RetryServerRequestInterceptor.setRejectingRequests(true);
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
