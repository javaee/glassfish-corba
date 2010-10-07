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
package corba.multiorb;

import java.util.Properties;
import org.omg.CORBA.Object;
import org.omg.CORBA.*;
import org.omg.CosNaming.*;
import org.omg.CosNaming.NamingContextHelper;
import org.omg.PortableServer.*;
import org.omg.PortableServer.POAPackage.AdapterAlreadyExists;
import org.omg.PortableServer.POAManagerPackage.AdapterInactive;
import org.omg.PortableServer.IdAssignmentPolicyValue;
import org.omg.PortableServer.ThreadPolicyValue;
import org.omg.PortableServer.LifespanPolicyValue;
import org.omg.PortableServer.IdUniquenessPolicyValue;
import org.omg.PortableServer.ServantRetentionPolicyValue;
import org.omg.PortableServer.RequestProcessingPolicyValue;
import org.omg.PortableServer.ImplicitActivationPolicyValue;
import org.omg.CORBA.Policy;
import org.omg.PortableServer.Servant;
import examples.*;

import com.sun.corba.se.spi.orbutil.ORBConstants ;

class policy2_servantA extends policy_2POA
{
	
	private int countValue;
	
	public policy2_servantA() 
	{
		countValue = 0;
	}

	/**
	 * Implementation of the servant object.
	 * The funtion intakes no parameter
	 * and returns an int value incremented by one.
	 */
	
	public int increment()
	{
		return ++countValue;
	}
}

class policy2_servantB extends policy_2POA
{
	
	private int countValue;
	
	public policy2_servantB() 
	{
		countValue = 1000;
	}

	/**
	 * Implementation of the servant object.
	 * The funtion intakes no parameter
	 * and returns an int value incremented by one.
	 */
	
	public int increment()
	{
		return ++countValue;
	}
}

public class policy2Server
{
	
	private static final String msgPassed = "policy_2: **PASSED**";
	
	private static final String msgFailed = "policy_2: **FAILED**";
	
	public static void main( String args[] )
	{
		try
		{
			Properties prop = new Properties();
			prop.setProperty("org.omg.CORBA.ORBClass", System.getProperty("org.omg.CORBA.ORBClass"));
//			System.out.println( "POLICIES : ORB_CTRL_MODEL,PERSISTENT,UNIQUE_ID,SYSTEM_ID,"
//			    + "RETAIN,USE_ACTIVE_OBJECT_MAP_ONLY,NO_IMPLICIT_ACTIVATION" );
			prop.setProperty( ORBConstants.OLD_ORB_ID_PROPERTY, "sunorb1");
			prop.setProperty( ORBConstants.ORB_SERVER_ID_PROPERTY, "257");
			prop.setProperty( ORBConstants.PERSISTENT_SERVER_PORT_PROPERTY, "10032");
//		        System.out.println(ORBConstants.OLD_ORB_ID_PROPERTY 
//		            + prop.getProperty(ORBConstants.OLD_ORB_ID_PROPERTY));
			ORB orb1 = ORB.init( args, prop );
			
			prop = new Properties();
			prop.setProperty("org.omg.CORBA.ORBClass", System.getProperty("org.omg.CORBA.ORBClass"));
			prop.setProperty( ORBConstants.OLD_ORB_ID_PROPERTY, "sunorb2");
			prop.setProperty( ORBConstants.ORB_SERVER_ID_PROPERTY, "257");
			prop.setProperty( ORBConstants.PERSISTENT_SERVER_PORT_PROPERTY, "20032");
//		        System.out.println(ORBConstants.OLD_ORB_ID_PROPERTY 
//		            + prop.getProperty(ORBConstants.OLD_ORB_ID_PROPERTY));
			ORB orb2 = ORB.init( args, prop );

			//create the rootPOA and activate it as first element of the array
	                // creating and initializing POAs/Objects in First ORB

			policy2_servantA acs1 = new policy2_servantA();
			policy2_servantB acs2 = new policy2_servantB();
		        createAndPublishObjects(orb1, acs1, "Object1");
		        createAndPublishObjects(orb2, acs2, "Object2");
	                System.out.println("Server is ready.");
			java.lang.Object sync = new java.lang.Object();
			synchronized( sync )
			{
				sync.wait();
			}
		}
		catch( Exception exp )
		{
			exp.printStackTrace();
			System.out.println( msgFailed + "\n" );
		}

	}

	public static void createAndPublishObjects(org.omg.CORBA.ORB orb, Servant servantObj, String Name) throws Exception
	{

			POA rootPoa = (POA)orb.resolve_initial_references( "RootPOA" );
			rootPoa.the_POAManager().activate();
			
			// Create a POA 
			POA childpoa = null;
			
			// create policy for the new POA.
			Policy[] policy = new Policy[7];
			policy[0] = rootPoa.create_id_assignment_policy( IdAssignmentPolicyValue.SYSTEM_ID );
			policy[1] = rootPoa.create_thread_policy( ThreadPolicyValue.ORB_CTRL_MODEL );
			policy[2] = rootPoa.create_lifespan_policy( LifespanPolicyValue.PERSISTENT );
			policy[3] = rootPoa.create_id_uniqueness_policy( IdUniquenessPolicyValue.UNIQUE_ID );
			policy[4] = rootPoa.create_servant_retention_policy( ServantRetentionPolicyValue.RETAIN );
			policy[5] = rootPoa.create_request_processing_policy( RequestProcessingPolicyValue.USE_ACTIVE_OBJECT_MAP_ONLY );
			policy[6] = rootPoa.create_implicit_activation_policy( ImplicitActivationPolicyValue.NO_IMPLICIT_ACTIVATION );
			
			// get the root naming context
			org.omg.CORBA.Object obj = orb.resolve_initial_references( "NameService" );
			NamingContext rootContext = NamingContextHelper.narrow( obj );
			
			// create the child poa and activate it
			childpoa = rootPoa.create_POA( "policy_2", null, policy );
			childpoa.the_POAManager().activate();
			childpoa.activate_object( (Servant)servantObj );
			
			// Binding to NamingService
			System.out.println( "Binding to NamingService" );
			NameComponent nc = new NameComponent( Name, "" );
			NameComponent path[] = 
			{
				nc
			};
			org.omg.CORBA.Object obj1 = childpoa.servant_to_reference( (Servant)servantObj );
			rootContext.rebind( path, obj1 );



	}

	public static void shutdown()
	{
	}

	public static void install()
	{
	}

	public static void uninstall()
	{
	}
}
