/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2002-2007 Sun Microsystems, Inc. All rights reserved.
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

package com.sun.corba.se.impl.oa.poa ;


import org.omg.PortableServer.Servant ;
import org.omg.PortableServer.ForwardRequest ;
import org.omg.PortableServer.POAPackage.WrongPolicy ;

import com.sun.corba.se.spi.extension.ServantCachingPolicy ;
import com.sun.corba.se.spi.orb.ORB ;

import com.sun.corba.se.spi.orbutil.ORBConstants ;
import com.sun.corba.se.impl.orbutil.ORBUtility ;

/** Implementation of POARequesHandler that provides policy specific
 * operations on the POA.
 */
public abstract class POAPolicyMediatorBase implements POAPolicyMediator {
    protected POAImpl poa ;
    protected ORB orb ;

    private int sysIdCounter ;
    private Policies policies ;
    private DelegateImpl delegateImpl ;

    private int serverid ;
    private int scid ;

    protected boolean isImplicit ;
    protected boolean isUnique ;
    protected boolean isSystemId ;

    public final Policies getPolicies()
    {
	return policies ;
    }

    public final int getScid() 
    {
	return scid ;
    }

    public final int getServerId() 
    {
	return serverid ;
    }

    POAPolicyMediatorBase( Policies policies, POAImpl poa ) 
    {
	if (policies.isSingleThreaded())
	    throw poa.invocationWrapper().singleThreadNotSupported() ;

	POAManagerImpl poam = (POAManagerImpl)(poa.the_POAManager()) ;
	POAFactory poaf = poam.getFactory() ;
	delegateImpl = (DelegateImpl)(poaf.getDelegateImpl()) ;
	this.policies = policies ;
	this.poa = poa ;
	orb = (ORB)poa.getORB() ;

	switch (policies.servantCachingLevel()) {
	    case ServantCachingPolicy.NO_SERVANT_CACHING :
		scid = ORBConstants.TRANSIENT_SCID ;
		break ;
	    case ServantCachingPolicy.FULL_SEMANTICS :
		scid = ORBConstants.SC_TRANSIENT_SCID ;
		break ;
	    case ServantCachingPolicy.INFO_ONLY_SEMANTICS :
		scid = ORBConstants.IISC_TRANSIENT_SCID ;
		break ;
	    case ServantCachingPolicy.MINIMAL_SEMANTICS :
		scid = ORBConstants.MINSC_TRANSIENT_SCID ;
		break ;
	}

	if ( policies.isTransient() ) {
	    serverid = orb.getTransientServerId();
	} else {
	    serverid = orb.getORBData().getPersistentServerId();
	    scid = ORBConstants.makePersistent( scid ) ;
	}

	isImplicit = policies.isImplicitlyActivated() ;
	isUnique = policies.isUniqueIds() ;
	isSystemId = policies.isSystemAssignedIds() ;

	sysIdCounter = 0 ; 
    }
    
    public final java.lang.Object getInvocationServant( byte[] id, 
        String operation ) throws ForwardRequest
    {
	java.lang.Object result = internalGetServant( id, operation ) ;

	return result ;
    }

    // Create a delegate and stick it in the servant.
    // This delegate is needed during dispatch for the ObjectImpl._orb()
    // method to work.
    protected final void setDelegate(Servant servant, byte[] id) 
    {
        //This new servant delegate no longer needs the id for 
	// its initialization.
        servant._set_delegate(delegateImpl);
    }

    public synchronized byte[] newSystemId() throws WrongPolicy
    {
	if (!isSystemId)
	    throw new WrongPolicy() ;

	byte[] array = new byte[8];
	ORBUtility.intToBytes(++sysIdCounter, array, 0);
	ORBUtility.intToBytes( poa.getPOAId(), array, 4);
	return array;
    }

    protected abstract  java.lang.Object internalGetServant( byte[] id, 
	String operation ) throws ForwardRequest ;
}
