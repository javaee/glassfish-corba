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

package com.sun.corba.se.impl.oa.toa ;

import org.omg.CORBA.Policy ;
import org.omg.PortableInterceptor.ObjectReferenceFactory ;
import org.omg.PortableInterceptor.ACTIVE;

import com.sun.corba.se.spi.protocol.ClientDelegate ;

import com.sun.corba.se.spi.copyobject.CopierManager ;
import com.sun.corba.se.spi.orbutil.copyobject.ObjectCopierFactory ;
import com.sun.corba.se.spi.ior.ObjectKeyTemplate ;
import com.sun.corba.se.spi.oa.OAInvocationInfo ;
import com.sun.corba.se.spi.oa.OADestroyed ;
import com.sun.corba.se.spi.oa.ObjectAdapterBase ;
import com.sun.corba.se.spi.orb.ORB ;
import com.sun.corba.se.spi.presentation.rmi.StubAdapter ;
import com.sun.corba.se.spi.protocol.LocalClientRequestDispatcher ;
import com.sun.corba.se.spi.transport.CorbaContactInfoList ;

import com.sun.corba.se.impl.ior.JIDLObjectKeyTemplate ;
import com.sun.corba.se.impl.oa.NullServantImpl;
import com.sun.corba.se.impl.oa.poa.Policies;
import com.sun.corba.se.spi.orbutil.ORBConstants ;
import com.sun.corba.se.impl.protocol.JIDLLocalCRDImpl ;
import java.util.concurrent.atomic.AtomicLong;
import org.glassfish.gmbal.Description;
import org.glassfish.gmbal.ManagedAttribute;
import org.glassfish.gmbal.ManagedObject;
import org.glassfish.gmbal.NameValue;

/** The Transient Object Adapter (TOA) represents the OA for purely transient
* objects.  It is used for standard RMI-IIOP as well as backwards compatible
* server support (i.e. the ORB.connect() method)
* Its characteristics include:
* <UL>
* <LI>There is only one OA instance of the TOA.  Its OAId is { "TOA" }</LI>
* <LI>There is not adapter manager.  The TOA manager ID is fixed.<LI>
* <LI>State is the same as ORB state (TBD)</LI>
* </UL>
* Other requirements:
* <UL>
* <LI>All object adapters must invoke ORB.adapterCreated when they are created.
* </LI>
* <LI>All adapter managers must invoke ORB.adapterManagerStateChanged when
* their state changes, mapping the internal state to an ORT state.</LI>
* <LI>AdapterStateChanged must be invoked (from somewhere) whenever
* an adapter state changes that is not due to an adapter manager state change.</LI>
* </UL>
*/
@ManagedObject
@Description( "The Transient Object Adapter")
public class TOAImpl extends ObjectAdapterBase implements TOA 
{
    private static AtomicLong currentId = new AtomicLong( 0 );

    private TransientObjectManager servants ;
    private long id ;
    private String codebase ;

    @NameValue
    private long getId() {
        return id ;
    }

    @ManagedAttribute
    @Description( "The codebase used to create this TOA")
    private String getCodebase() {
        return codebase ;
    }

    @ManagedAttribute
    @Description( "The TransientObjectManager")
    private TransientObjectManager getTransientObjectManager() {
        return servants ;
    }

    public TOAImpl( ORB orb, TransientObjectManager tom, String codebase ) 
    {
	super( orb ) ;
	servants = tom ;
        this.codebase = codebase ;
        id = currentId.getAndIncrement() ;

	// Make the object key template
	int serverid = (getORB()).getTransientServerId();
	int scid = ORBConstants.TOA_SCID ;

	ObjectKeyTemplate oktemp = new JIDLObjectKeyTemplate( orb, scid, serverid ) ;

	// REVISIT - POA specific
	Policies policies = Policies.defaultPolicies;

	// REVISIT - absorb codebase into a policy
	initializeTemplate( oktemp, true,
			    policies, 
			    codebase,
			    null, // manager id
			    oktemp.getObjectAdapterId()
			    ) ;
    }

    // Methods required for dispatching requests

    public ObjectCopierFactory getObjectCopierFactory()
    {
	CopierManager cm = getORB().getCopierManager() ;
	return cm.getDefaultObjectCopierFactory() ;
    }

    public org.omg.CORBA.Object getLocalServant( byte[] objectId ) 
    {
	return (org.omg.CORBA.Object)(servants.lookupServant( objectId ) ) ;
    }

    /** Get the servant for the request given by the parameters. 
    * This will update thread Current, so that subsequent calls to
    * returnServant and removeCurrent from the same thread are for the
    * same request.
    * @param request is the request containing the rest of the request
    */
    public void getInvocationServant( OAInvocationInfo info ) 
    {
	java.lang.Object servant = servants.lookupServant( info.id() ) ;
	if (servant == null) {
            servant =
                new NullServantImpl(wrapper.nullServant());
        }
	info.setServant( servant ) ;
    }

    public void returnServant()
    {
	// NO-OP
    }

    /** Return the most derived interface for the given servant and objectId.
    */
    public String[] getInterfaces( Object servant, byte[] objectId ) 
    {
	return StubAdapter.getTypeIds( servant ) ;
    }

    // This will need changing once we support ORB and thread level policies,
    // but for now, there is no way to associate policies with the TOA, so
    // getEffectivePolicy must always return null.
    public Policy getEffectivePolicy( int type ) 
    {
	return null ;
    }

    public int getManagerId() 
    {
	return -1 ;
    }

    public short getState() 
    {
	return ACTIVE.value ;
    }

    public void enter() throws OADestroyed
    {
    }

    public void exit() 
    {
    }
 
    // Methods unique to the TOA

    public void connect( org.omg.CORBA.Object objref) 
    {
        // Store the objref and get a userkey allocated by the transient
        // object manager.
        byte[] key = servants.storeServant(objref, null);

        // Find out the repository ID for this objref.
	String id = StubAdapter.getTypeIds( objref )[0] ;

	// Create the new objref
	ObjectReferenceFactory orf = getCurrentFactory() ;
	org.omg.CORBA.Object obj = orf.make_object( id, key ) ;

	// Copy the delegate from the new objref to the argument

	org.omg.CORBA.portable.Delegate delegate = StubAdapter.getDelegate( 
	    obj ) ;
	CorbaContactInfoList ccil = ((ClientDelegate) delegate).getContactInfoList() ;
	LocalClientRequestDispatcher lcs = 
	    ccil.getLocalClientRequestDispatcher() ;

	if (lcs instanceof JIDLLocalCRDImpl) {
	    JIDLLocalCRDImpl jlcs = (JIDLLocalCRDImpl)lcs ;
	    jlcs.setServant( objref ) ;
	} else {	
	    throw new RuntimeException( 
		"TOAImpl.connect can not be called on " + lcs ) ;
	}

	StubAdapter.setDelegate( objref, delegate ) ;
    }

    public void disconnect( org.omg.CORBA.Object objref ) 
    {
        // Get the delegate, then ior, then transientKey, then delete servant
        org.omg.CORBA.portable.Delegate del = StubAdapter.getDelegate( 
	    objref ) ; 
	CorbaContactInfoList ccil = ((ClientDelegate) del).getContactInfoList() ;
	LocalClientRequestDispatcher lcs = 
	    ccil.getLocalClientRequestDispatcher() ;

	if (lcs instanceof JIDLLocalCRDImpl) {
	    JIDLLocalCRDImpl jlcs = (JIDLLocalCRDImpl)lcs ;
	    byte[] oid = jlcs.getObjectId() ;
	    servants.deleteServant(oid);
	    jlcs.unexport() ;
	} else {	
	    throw new RuntimeException( 
		"TOAImpl.disconnect can not be called on " + lcs ) ;
	}
    }
} 
