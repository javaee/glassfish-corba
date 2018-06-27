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

package com.sun.corba.ee.spi.oa ;

import org.omg.PortableInterceptor.ObjectReferenceTemplate ;
import org.omg.PortableInterceptor.ObjectReferenceFactory ;

import org.omg.CORBA.Policy ;

import com.sun.corba.ee.spi.ior.IORFactories ;
import com.sun.corba.ee.spi.ior.IORTemplate ;
import com.sun.corba.ee.spi.ior.ObjectAdapterId;
import com.sun.corba.ee.spi.ior.ObjectKeyTemplate ;
import com.sun.corba.ee.spi.orb.ORB ;
import com.sun.corba.ee.spi.protocol.PIHandler ;

import com.sun.corba.ee.spi.logging.POASystemException ;
import com.sun.corba.ee.impl.oa.poa.Policies;
import org.glassfish.gmbal.Description;
import org.glassfish.gmbal.ManagedAttribute;
import org.glassfish.pfl.dynamic.copyobject.spi.ObjectCopierFactory;
import org.omg.PortableInterceptor.ACTIVE;
import org.omg.PortableInterceptor.DISCARDING;
import org.omg.PortableInterceptor.HOLDING;
import org.omg.PortableInterceptor.INACTIVE;
import org.omg.PortableInterceptor.NON_EXISTENT;

abstract public class ObjectAdapterBase extends org.omg.CORBA.LocalObject 
    implements ObjectAdapter
{
    protected static final POASystemException wrapper =
        POASystemException.self ;

    private ORB orb;

    // Data related to the construction of object references and
    // supporting the Object Reference Template.
    private IORTemplate iortemp;
    private byte[] adapterId ;
    private ObjectReferenceTemplate adapterTemplate ;
    private ObjectReferenceFactory currentFactory ;
    private boolean isNameService = false ;
   
    public ObjectAdapterBase( ORB orb ) {
        this.orb = orb ;
    }

    public final POASystemException wrapper() {
        return wrapper ;
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
            if (pih != null) {
                pih.objectAdapterCreated(this);
            }
        }

        iortemp.makeImmutable() ;
    }

    final public org.omg.CORBA.Object makeObject( String repId, byte[] oid )
    {
        if (repId == null) {
            throw wrapper.nullRepositoryId();
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

    public boolean isNameService() {
        return isNameService ;
    }

    public void setNameService( boolean flag ) {
        isNameService = flag ;
    }
} 
