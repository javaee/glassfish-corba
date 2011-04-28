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

package com.sun.corba.se.impl.oa.rfm;

import org.omg.CORBA.LocalObject ;

import org.omg.PortableServer.POA ;

import org.omg.PortableInterceptor.IORInterceptor_3_0 ;
import org.omg.PortableInterceptor.IORInfo ;
import org.omg.PortableInterceptor.ORBInitializer ;
import org.omg.PortableInterceptor.ORBInitInfo ;
import org.omg.PortableInterceptor.ObjectReferenceTemplate ;

import com.sun.corba.se.spi.orb.ORBConfigurator ;
import com.sun.corba.se.spi.orb.DataCollector ;
import com.sun.corba.se.spi.orb.ORB ;

import com.sun.corba.se.spi.oa.ObjectAdapter ;

import com.sun.corba.se.spi.legacy.interceptor.IORInfoExt ;

import com.sun.corba.se.spi.misc.ORBConstants ;

import com.sun.corba.se.spi.logging.POASystemException ;

/** Used to initialize the ReferenceManager in the ORB.
 * The ReferenceManager is an optional component built
 * on top of the ORB that is used to manage a group
 * of POAs that require reconfigurability.  This class
 * sets up the ORB as follows:
 * <ol>
 * <li>Create an instance of ReferenceFactoryManagerImpl and register it with 
 * register_local_reference.
 * <li>Create and register an IORInterceptor that prevent outside POAs from 
 * interfering with the ReferenceManager.
 * </ol>
 */
public class ReferenceManagerConfigurator implements ORBConfigurator {
    private static final POASystemException wrapper =
        POASystemException.self ;

    private static class RMIORInterceptor
	extends LocalObject 
	implements IORInterceptor_3_0 
    {
	private ReferenceFactoryManagerImpl rm ;

	public RMIORInterceptor( ReferenceFactoryManagerImpl rm ) {
	    this.rm = rm ;
	}

	public String name() {
	    return "##" + this.getClass().getName() + "##" ;
	}

	public void destroy() {
	    // NO-OP
	}

	public void establish_components( IORInfo info ) {
	    // NO-OP
	}
	
	public void adapter_manager_state_changed( int id, short state ) {
	    // NO-OP
	}

	public void adapter_state_changed( ObjectReferenceTemplate[] templates, short state ) {
	    // NO-OP
	}

	// We must do the checking here, because exceptions are not 
	// ignored.  All exceptions thrown in establish_components
	// are ignored.  The whole purpose of this interceptor is
	// to throw an exception if an error is detected.
	public void components_established( IORInfo info ) {
	    IORInfoExt ext = IORInfoExt.class.cast( info ) ;
	    ObjectAdapter oa = ext.getObjectAdapter() ;
	    if (!(oa instanceof POA)) {
                return;
            } // if not POA, then there is no chance of a conflict.
	    POA poa = POA.class.cast( oa ) ;
	    rm.validatePOACreation( poa ) ;
	}
    }

    private static class RMORBInitializer
	extends LocalObject 
	implements ORBInitializer 
    {
	private IORInterceptor_3_0 interceptor ;

	public RMORBInitializer( IORInterceptor_3_0 interceptor ) {
	    this.interceptor = interceptor ;
	}

	public void pre_init( ORBInitInfo info ) {
	    // NO-OP
	}

	public void post_init( ORBInitInfo info ) {
	    try {
		info.add_ior_interceptor( interceptor ) ;
	    } catch (Exception exc) {
		throw wrapper.rfmPostInitException( exc ) ;
	    }
	}
    }

    public void configure( DataCollector collector, ORB orb ) 
    {
	try {
	    ReferenceFactoryManagerImpl rm = new ReferenceFactoryManagerImpl( orb ) ;
	    orb.register_initial_reference( ORBConstants.REFERENCE_FACTORY_MANAGER, rm ) ;
	    IORInterceptor_3_0 interceptor = new RMIORInterceptor( rm ) ;
	    ORBInitializer initializer = new RMORBInitializer( interceptor ) ;	
	    orb.getORBData().addORBInitializer( initializer ) ;
	} catch (Exception exc) {
	    throw wrapper.rfmConfigureException( exc ) ;
	}
    }
}
