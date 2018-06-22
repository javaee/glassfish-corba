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

package corba.preinvokepostinvoke;

import org.omg.CORBA.Policy;
import org.omg.CORBA.ORB;
import org.omg.PortableServer.POA;
import org.omg.PortableServer.RequestProcessingPolicyValue;
import org.omg.PortableServer.Servant;
import org.omg.PortableServer.ServantManager;
import org.omg.PortableServer.ServantRetentionPolicyValue;
import org.omg.PortableServer.ServantLocator;
import javax.rmi.PortableRemoteObject ;
import com.sun.corba.ee.spi.extension.ServantCachingPolicy;

public class Server {

     private static ORB orb;
     private static  org.omg.CosNaming.NamingContextExt nctx; 
     private static  POA poaWithServantCachingPolicy; 


     public static void main( String[] args ) {
        System.out.println( " Starting Server.... " );
        System.out.flush( );
     
        try {
            orb = ORB.init( args, null );

            org.omg.CORBA.Object obj = 
                orb.resolve_initial_references( "NameService");
            nctx = org.omg.CosNaming.NamingContextExtHelper.narrow( obj );

            POA rPOA = (POA)orb.resolve_initial_references( "RootPOA" );
            rPOA.the_POAManager().activate( );

            Policy[] policies = new Policy[3];
            policies[0] = rPOA.create_servant_retention_policy(
                ServantRetentionPolicyValue.NON_RETAIN);
            policies[1] = rPOA.create_request_processing_policy(
                          RequestProcessingPolicyValue.USE_SERVANT_MANAGER);
            policies[2] = ServantCachingPolicy.getFullPolicy( );

            MyServantLocator sl = new MyServantLocator( orb );

            poaWithServantCachingPolicy = rPOA.create_POA( "poa", null, 
                policies );
            poaWithServantCachingPolicy.set_servant_manager( sl );
            poaWithServantCachingPolicy.the_POAManager().activate();


            _Interface_Stub s = new _Interface_Stub( );
            bindInstance( (s._ids())[0], "Instance1" );
            System.out.println( "Created and Bound instance1" );
            System.out.flush( );

            bindInstance( (s._ids())[0], "Instance2" );
            System.out.println( "Created and Bound instance2" );
            System.out.flush( );

            TestAssert.startTest( );
            resolveReferenceAndInvoke( orb  );
            TestAssert.isTheCallBalanced( 2 );

            // Emit the handshake the test framework expects
            // (can be changed in Options by the running test)
            System.out.println ("Server is ready.");
            System.out.flush( );
        } catch( Exception e ) {
            e.printStackTrace( );
        }
    }

    private static void bindInstance( String repId, String bindingName )
    {
        try {
            org.omg.CORBA.Object obj = 
                poaWithServantCachingPolicy.create_reference_with_id(
                    bindingName.getBytes( ), repId );
            org.omg.CosNaming.NameComponent[] nc = nctx.to_name( bindingName );
            nctx.rebind( nc, obj );
        } catch( Exception e ) {
            e.printStackTrace( );
        }
    }

    private static void resolveReferenceAndInvoke(ORB orb) {
        try {
             org.omg.CORBA.Object obj;

             obj = nctx.resolve_str( "Instance1" );
             Interface i1 = 
                 (Interface) PortableRemoteObject.narrow(obj,Interface.class );
             i1.o1( "Invoking from Client..." );
        }catch( Exception e ) {
            e.printStackTrace( );
            System.exit( 1 );
        }
    }
}

        
        


       
    



