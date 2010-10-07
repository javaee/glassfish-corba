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
package com.sun.corba.se.spi.oa ;

import org.omg.PortableInterceptor.ObjectReferenceTemplate ;
import org.omg.PortableInterceptor.ObjectReferenceFactory ;

import org.omg.CORBA.Policy ;

import com.sun.corba.se.spi.orbutil.copyobject.ObjectCopierFactory ;
import com.sun.corba.se.spi.ior.IORFactories ;
import com.sun.corba.se.spi.ior.IORTemplate ;
import com.sun.corba.se.spi.ior.ObjectAdapterId;
import com.sun.corba.se.spi.ior.ObjectKeyTemplate ;
import com.sun.corba.se.spi.orb.ORB ;
import com.sun.corba.se.spi.protocol.PIHandler ;

import com.sun.corba.se.impl.logging.POASystemException ;
import com.sun.corba.se.impl.logging.OMGSystemException ;
import com.sun.corba.se.impl.oa.poa.Policies;
import org.glassfish.gmbal.Description;
import org.glassfish.gmbal.ManagedAttribute;
import org.omg.PortableInterceptor.ACTIVE;
import org.omg.PortableInterceptor.ACTIVE;
import org.omg.PortableInterceptor.DISCARDING;
import org.omg.PortableInterceptor.HOLDING;
import org.omg.PortableInterceptor.INACTIVE;
import org.omg.PortableInterceptor.NON_EXISTENT;

abstract public class ObjectAdapterBase extends org.omg.CORBA.LocalObject 
    implements ObjectAdapter
{
    private ORB orb;

    // Exception wrappers
    private final POASystemException _iorWrapper ;
    private final POASystemException _invocationWrapper ;
    private final POASystemException _lifecycleWrapper ;
    private final OMGSystemException _omgInvocationWrapper ;
    private final OMGSystemException _omgLifecycleWrapper ;

    // Data related to the construction of object references and
    // supporting the Object Reference Template.
    private IORTemplate iortemp;
    private byte[] adapterId ;
    private ObjectReferenceTemplate adapterTemplate ;
    private ObjectReferenceFactory currentFactory ;  
   
    public ObjectAdapterBase( ORB orb ) 
    {
	this.orb = orb ;
	_iorWrapper = orb.getLogWrapperTable().get_OA_IOR_POA() ;
	_lifecycleWrapper = orb.getLogWrapperTable().get_OA_LIFECYCLE_POA() ;
	_omgLifecycleWrapper = orb.getLogWrapperTable().get_OA_LIFECYCLE_OMG() ;
	_invocationWrapper = orb.getLogWrapperTable().get_OA_INVOCATION_POA() ;
	_omgInvocationWrapper = orb.getLogWrapperTable().get_OA_INVOCATION_OMG() ;
    }

    public final POASystemException iorWrapper()
    {
	return _iorWrapper ;
    }

    public final POASystemException lifecycleWrapper()
    {
	return _lifecycleWrapper ;
    }

    public final OMGSystemException omgLifecycleWrapper()
    {
	return _omgLifecycleWrapper ;
    }

    public final POASystemException invocationWrapper()
    {
	return _invocationWrapper ;
    }

    public final OMGSystemException omgInvocationWrapper()
    {
	return _omgInvocationWrapper ;
    }

    /*
     * This creates the complete template.
     * When it is done, reference creation can proceed.
     */
    final public void initializeTemplate( ObjectKeyTemplate oktemp,
	boolean notifyORB, Policies policies, String codebase,
	String objectAdapterManagerId, ObjectAdapterId objectAdapterId)
    {
	adapterId = oktemp.getAdapterId() ;

	iortemp = IORFactories.makeIORTemplate(oktemp) ;

	// This calls acceptors which create profiles and may
	// add tagged components to those profiles.
	orb.getCorbaTransportManager().addToIORTemplate(
            iortemp, policies,
	    codebase, objectAdapterManagerId, objectAdapterId);

	adapterTemplate = IORFactories.makeObjectReferenceTemplate( orb, 
	    iortemp ) ;
	currentFactory = adapterTemplate ;

	if (notifyORB) {
	    PIHandler pih = orb.getPIHandler() ;
	    if (pih != null)
		// This runs the IORInterceptors.
		pih.objectAdapterCreated( this ) ;
	}

	iortemp.makeImmutable() ;
    }

    final public org.omg.CORBA.Object makeObject( String repId, byte[] oid )
    {
	if (repId == null) {
	    throw iorWrapper().nullRepositoryId();
	}
	return currentFactory.make_object( repId, oid ) ;
    }

    final public byte[] getAdapterId() 
    {
	return adapterId ;
    }

    final public ORB getORB() 
    {
	return orb ;
    }

    abstract public Policy getEffectivePolicy( int type ) ;

    final public IORTemplate getIORTemplate() 
    {
	return iortemp ;
    }

    abstract public int getManagerId() ;

    abstract public short getState() ; 

    @ManagedAttribute( id="State" )
    @Description( "The current Adapter state")
    private String getDisplayState( ) {
        final short state = getState() ;
        switch (state) {
            case HOLDING.value : return "HOLDING" ;
            case ACTIVE.value : return "ACTIVE" ;
            case DISCARDING.value : return "DISCARDING" ;
            case INACTIVE.value : return "INACTIVE" ;
            case NON_EXISTENT.value : return "NON_EXISTENT" ;
            default : return "<INVALID>" ;
        }
    }

    final public ObjectReferenceTemplate getAdapterTemplate()
    {
	return adapterTemplate ;
    }

    final public ObjectReferenceFactory getCurrentFactory()
    {
	return currentFactory ;
    }

    final public void setCurrentFactory( ObjectReferenceFactory factory )
    {
	currentFactory = factory ;
    }

    abstract public org.omg.CORBA.Object getLocalServant( byte[] objectId ) ;

    abstract public void getInvocationServant( OAInvocationInfo info ) ;

    abstract public void returnServant() ;

    abstract public void enter() throws OADestroyed ;

    abstract public void exit() ;

    abstract protected ObjectCopierFactory getObjectCopierFactory() ;

    // Note that all current subclasses share the same implementation of this method,
    // but overriding it would make sense for OAs that use a different InvocationInfo.
    public OAInvocationInfo makeInvocationInfo( byte[] objectId )
    {
	OAInvocationInfo info = new OAInvocationInfo( this, objectId ) ;
	info.setCopierFactory( getObjectCopierFactory() ) ;
	return info ;
    }

    abstract public String[] getInterfaces( Object servant, byte[] objectId ) ;
} 
