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

package com.sun.corba.se.impl.folb;

import java.rmi.RemoteException ;
import java.rmi.Remote ;
import java.io.PrintStream ;

import java.rmi.Remote ;
import java.rmi.RemoteException ;

import java.util.Arrays ;
import java.util.List ;
import java.util.ArrayList ;
import java.util.Map ;
import java.util.HashMap ;
import java.util.Properties ;

import javax.rmi.PortableRemoteObject ;

import javax.rmi.CORBA.Tie ;

import org.omg.CORBA.Policy ;
import org.omg.CORBA.BAD_OPERATION ;
import org.omg.CORBA.LocalObject ;

import org.omg.CosNaming.NamingContext ;
import org.omg.CosNaming.NamingContextHelper ;
import org.omg.CosNaming.NamingContextExt ;
import org.omg.CosNaming.NamingContextExtHelper ;
import org.omg.CosNaming.NameComponent ;
import org.omg.CosNaming.NamingContextPackage.CannotProceed ;
import org.omg.CosNaming.NamingContextPackage.InvalidName ;
import org.omg.CosNaming.NamingContextPackage.AlreadyBound ;
import org.omg.CosNaming.NamingContextPackage.NotFound ;

import org.omg.PortableServer.POA ;
import org.omg.PortableServer.Servant ;
import org.omg.PortableServer.ServantLocator ;

//import com.sun.corba.se.spi.orb.ORB ;

import org.omg.CORBA.ORB;

import com.sun.corba.se.impl.orbutil.ORBConstants ;
import com.sun.corba.se.spi.folb.GroupInfoService;

import org.omg.PortableServer.ForwardRequest ;
import org.omg.PortableServer.POA ;
import org.omg.PortableServer.Servant ;
import org.omg.PortableServer.ServantLocator ;

import org.omg.PortableServer.ServantLocatorPackage.CookieHolder ;
import com.sun.corba.se.spi.folb.ClusterInstanceInfo;

import org.omg.PortableServer.POA;
import org.omg.PortableServer.Servant;
import org.omg.PortableServer.LifespanPolicyValue;
import org.omg.CORBA.Policy;
import org.omg.PortableServer.ImplicitActivationPolicyValue;
import javax.rmi.CORBA.Tie;

import org.omg.PortableServer.RequestProcessingPolicyValue ;
import org.omg.PortableServer.ServantRetentionPolicyValue ;
import com.sun.corba.se.impl.orbutil.ORBUtility;


/**
 * This class consists of :
 * - Remote interface
 * - Impl of the Remote interface
 * - ServantLocator
 *
 * This is needed for getting the information about all the cluster endpoints
 * when an appclient/standalone client enables FOLB.
 * Without this feature, the client always ends up talking to only the endpoints specified 
 * as part of the endpoints property. But in reality, the the endpoints property only contains  
 * around 2 endpoints for bootstrapping purposes.
 *
 * In this design, we register the following remote object with CosNaming 
 * and then look it up in CosNaming to get hold of the cluster instances
 * This is done only once (during the first call to new InitialContext()).
 *
 * @author Sheetal Vartak
 */
public class InitialGroupInfoService {

    public interface InitialGIS extends Remote {

	public List<ClusterInstanceInfo> getClusterInstanceInfo() throws RemoteException ;
        
    }

    public class InitialGISImpl extends PortableRemoteObject implements InitialGIS {
       
        private ORB orb;

      	public InitialGISImpl(ORB orb) throws RemoteException {	  
	    super() ;	   
	    this.orb = orb; 	
	}
	
	public List<ClusterInstanceInfo> getClusterInstanceInfo() throws RemoteException {
	  try {
	      GroupInfoService gis = (GroupInfoService) PortableRemoteObject.narrow(
							orb.resolve_initial_references(
							ORBConstants.FOLB_SERVER_GROUP_INFO_SERVICE),
							GroupInfoService.class);
	      return gis.getClusterInstanceInfo(null);
	  } catch (org.omg.CORBA.ORBPackage.InvalidName inv) {
	      dprint("Exception in looking up GroupInfoService ==> ", inv);
	      return null;
	  }
	    
	}

    }

    public class InitialGISServantLocator extends LocalObject
	implements ServantLocator {
	private Servant servant ;
	private InitialGISImpl impl = null; 

	public InitialGISServantLocator(ORB orb) {
	    try {
		impl = new InitialGISImpl(orb) ;
	    } catch (Exception exc) {
	      System.out.println( "Exception in creating servant: " + exc ) ;
	    }

	    Tie tie = com.sun.corba.se.spi.orb.ORB.class.cast( orb )
		.getPresentationManager().getTie() ;
	    tie.setTarget( impl ) ;
	    servant = Servant.class.cast( tie ) ;
	}
        public String getType() {
	    return servant._all_interfaces(null, null)[0];
	}

	public synchronized Servant preinvoke( byte[] oid, POA adapter,
	    String operation, CookieHolder the_cookie 
	) throws ForwardRequest {
	    return servant ;
	}

	public void postinvoke( byte[] oid, POA adapter,
	    String operation, Object the_cookie, Servant the_servant ) {
	}

    }

    public InitialGroupInfoService(ORB orb) {             
        bindName(orb);
    }


    public void bindName (ORB orb) {
      try {
	POA rootPOA = (POA)orb.resolve_initial_references(ORBConstants.ROOT_POA_NAME ) ;

	Policy[] arr = new Policy[] { 					
					 rootPOA.create_servant_retention_policy( 
						ServantRetentionPolicyValue.NON_RETAIN ),
					 rootPOA.create_request_processing_policy(
					 	RequestProcessingPolicyValue.USE_SERVANT_MANAGER ),
					 rootPOA.create_lifespan_policy( 
						LifespanPolicyValue.TRANSIENT ) 
					 } ;

	POA poa = rootPOA.create_POA( ORBConstants.INITIAL_GROUP_INFO_SERVICE, null, arr ) ;

	InitialGISServantLocator servantLocator = new InitialGISServantLocator(orb);
	poa.set_servant_manager(servantLocator) ; 
	poa.the_POAManager().activate();

	byte[] id = new byte[]{ 1, 2, 3 } ;
	org.omg.CORBA.Object provider = 
	  poa.create_reference_with_id(id, servantLocator.getType());
            
	// put object in NameService
	org.omg.CORBA.Object objRef =
	  orb.resolve_initial_references("NameService");
	NamingContext ncRef = NamingContextHelper.narrow(objRef);
	NameComponent nc = 
	  new NameComponent(ORBConstants.INITIAL_GROUP_INFO_SERVICE, "");
	NameComponent path[] = {nc};
	ncRef.rebind(path, provider);	
      } catch (Exception e) {
	dprint("Exception in InitialGroupInfoService.bindName()==> ", e);
      }
    }


    static void dprint(String msg)
    {
	ORBUtility.dprint("InitialGroupInfoService", msg);
    }

    static void dprint(String msg, Throwable t)
    {
	dprint(msg);
	dprint(t.toString());
    }

  
}
