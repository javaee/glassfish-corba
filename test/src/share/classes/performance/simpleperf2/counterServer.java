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

package performance.simpleperf2;

import javax.rmi.PortableRemoteObject ;
import java.io.*;
import java.io.DataOutputStream ;
import java.util.*;
import java.rmi.RemoteException ;
import org.omg.CORBA.*;
import org.omg.CosNaming.*;
import org.omg.PortableServer.*;
import org.omg.PortableServer.ServantLocatorPackage.*;
import com.sun.corba.ee.spi.misc.ORBConstants ;
import corba.framework.ThreadProcess ;

public class counterServer extends ThreadProcess {

    public void run()
    {
        try{
            // create and initialize the ORB
            Properties p = new Properties();
            p.put("org.omg.CORBA.ORBClass",  
                "com.sun.corba.ee.impl.orb.ORBImpl" ) ;
            p.put( ORBConstants.PERSISTENT_SERVER_PORT_PROPERTY, "9999");
            p.put( ORBConstants.ORB_SERVER_ID_PROPERTY, "9999");
            String[] args = null ;
            ORB orb = ORB.init(args, p);

            POA rootPOA = (POA)orb.resolve_initial_references("RootPOA");
            rootPOA.the_POAManager().activate();

            POA poa = createPOA(orb, rootPOA);
            createCounter1(orb, poa);

            // wait for invocations from clients
            System.out.println("Server is ready.");
            orb.run();

        } catch (Exception e) {
            System.err.println("ERROR: " + e);
            e.printStackTrace(System.out);
            System.exit(1);
        }
    }

    private POA createPOA(ORB orb, POA rootPOA)
        throws Exception
    {
        // create a persistent POA
        Policy[] tpolicy = new Policy[3];
        tpolicy[0] = rootPOA.create_lifespan_policy(LifespanPolicyValue.TRANSIENT);
        tpolicy[1] = rootPOA.create_request_processing_policy(RequestProcessingPolicyValue.USE_SERVANT_MANAGER);
        tpolicy[2] = rootPOA.create_servant_retention_policy(ServantRetentionPolicyValue.NON_RETAIN) ;
        POA tpoa = rootPOA.create_POA("PersistentPOA", null, tpolicy);
 
        counterImpl impl = new counterImpl();
        Servant servant = (Servant)(javax.rmi.CORBA.Util.getTie( impl ) ) ;
        CSLocator csl = new CSLocator(servant);
        tpoa.set_servant_manager(csl);
        tpoa.the_POAManager().activate();
        return tpoa;
    }

    private void createCounter1(ORB orb, POA tpoa)
        throws Exception
    {
        // create an objref using POA
        byte[] id = "abcdef".getBytes();
        String intf = "" ; // new _counterImpl_Tie()._all_interfaces(tpoa,id)[0];

        org.omg.CORBA.Object obj = tpoa.create_reference_with_id(id, intf);

        counterIF counterRef 
            = (counterIF)PortableRemoteObject.narrow(obj, counterIF.class );

        // put objref in NameService
        org.omg.CORBA.Object objRef =
            orb.resolve_initial_references("NameService");
        NamingContext ncRef = NamingContextHelper.narrow(objRef);
        NameComponent nc = new NameComponent("Counter1", "");
        NameComponent path[] = {nc};

        ncRef.rebind(path, obj);
    }
}

class CSLocator extends org.omg.CORBA.LocalObject implements ServantLocator
{
    Servant servant;

    CSLocator(Servant servant)
    {
        this.servant = servant;
    }

    public Servant preinvoke(byte[] oid, POA adapter, String operation, 
                             CookieHolder the_cookie)
        throws org.omg.PortableServer.ForwardRequest
    {
        return servant ;
    }

    public void postinvoke(byte[] oid, POA adapter, String operation, 
                           java.lang.Object cookie, Servant servant)
    {
        return;
    }
}
