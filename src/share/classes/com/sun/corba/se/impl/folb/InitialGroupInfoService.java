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

package com.sun.corba.se.impl.folb;


import java.rmi.Remote ;
import java.rmi.RemoteException ;

import java.util.List ;

import javax.rmi.PortableRemoteObject ;

import org.omg.CORBA.LocalObject ;

import org.omg.CosNaming.NamingContext ;
import org.omg.CosNaming.NamingContextHelper ;
import org.omg.CosNaming.NameComponent ;

//import com.sun.corba.se.spi.orb.ORB ;

import org.omg.CORBA.ORB;

import com.sun.corba.se.spi.orbutil.ORBConstants ;
import com.sun.corba.se.spi.folb.GroupInfoService;

import org.omg.PortableServer.ForwardRequest ;
import org.omg.PortableServer.ServantLocator ;

import org.omg.PortableServer.ServantLocatorPackage.CookieHolder ;
import com.sun.corba.se.spi.folb.ClusterInstanceInfo;
import com.sun.corba.se.spi.logging.ORBUtilSystemException;

import org.omg.PortableServer.POA;
import org.omg.PortableServer.Servant;
import org.omg.PortableServer.LifespanPolicyValue;
import org.omg.CORBA.Policy;
import javax.rmi.CORBA.Tie;

import org.omg.PortableServer.RequestProcessingPolicyValue ;
import org.omg.PortableServer.ServantRetentionPolicyValue ;
import com.sun.corba.se.spi.orbutil.tf.annotation.InfoMethod;
import com.sun.corba.se.spi.trace.Folb;


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
@Folb
public class InitialGroupInfoService {
    private static final ORBUtilSystemException wrapper =
        ORBUtilSystemException.self ;

    public interface InitialGIS extends Remote {
	public List<ClusterInstanceInfo> getClusterInstanceInfo()
            throws RemoteException ;
    }

    @Folb
    public static class InitialGISImpl extends PortableRemoteObject
        implements InitialGIS {
       
        private ORB orb;

      	public InitialGISImpl(ORB orb) throws RemoteException {	  
	    super() ;	   
	    this.orb = orb; 	
	}
	
        @InfoMethod
        private void exceptionReport( Exception exc ) { }

        @Folb
	public List<ClusterInstanceInfo> getClusterInstanceInfo()
            throws RemoteException {

            try {
	        GroupInfoService gis =
                    (GroupInfoService)PortableRemoteObject.narrow(
                    orb.resolve_initial_references(
                        ORBConstants.FOLB_SERVER_GROUP_INFO_SERVICE),
                        GroupInfoService.class);
                return gis.getClusterInstanceInfo(null);
            } catch (org.omg.CORBA.ORBPackage.InvalidName inv) {
                exceptionReport( inv ) ;
	        return null;
            }
	}
    }

    public static class InitialGISServantLocator extends LocalObject
	implements ServantLocator {
	private Servant servant ;
	private InitialGISImpl impl = null; 

	public InitialGISServantLocator(ORB orb) {
	    try {
		impl = new InitialGISImpl(orb) ;
	    } catch (Exception exc) {
                wrapper.couldNotInitializeInitialGIS( exc ) ;
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
	POA rootPOA = (POA)orb.resolve_initial_references(
            ORBConstants.ROOT_POA_NAME ) ;

	Policy[] arr = new Policy[] { 					
            rootPOA.create_servant_retention_policy(
                ServantRetentionPolicyValue.NON_RETAIN ),
            rootPOA.create_request_processing_policy(
                RequestProcessingPolicyValue.USE_SERVANT_MANAGER ),
            rootPOA.create_lifespan_policy(
                LifespanPolicyValue.TRANSIENT ) } ;

	POA poa = rootPOA.create_POA( ORBConstants.INITIAL_GROUP_INFO_SERVICE,
            null, arr ) ;

	InitialGISServantLocator servantLocator =
            new InitialGISServantLocator(orb);
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
          throw wrapper.bindNameException( e ) ;
      }
    }
}
