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

package com.sun.corba.se.impl.oa.poa;

import java.util.Map ;
import java.util.HashMap ;
import java.util.BitSet ;
import java.util.Iterator ;

import com.sun.corba.se.spi.orbutil.ORBConstants ;
import com.sun.corba.se.spi.extension.ServantCachingPolicy ;
import com.sun.corba.se.spi.extension.ZeroPortPolicy ;
import com.sun.corba.se.spi.extension.CopyObjectPolicy ;

import org.omg.CORBA.*;
import org.omg.PortableServer.*;
import org.omg.PortableServer.POAPackage.*;

public final class Policies {
/* Order of *POLICY_ID :
   THREAD_
   LIFESPAN_
   ID_UNIQUENESS_
   ID_ASSIGNMENT_
   IMPLICIT_ACTIVATION_
   SERvANT_RETENTION_
   REQUEST_PROCESSING_
   The code in this class depends on this order!
*/
    private static final int MIN_POA_POLICY_ID = THREAD_POLICY_ID.value ;
    private static final int MAX_POA_POLICY_ID = REQUEST_PROCESSING_POLICY_ID.value ;
    private static final int POLICY_TABLE_SIZE = MAX_POA_POLICY_ID -
        MIN_POA_POLICY_ID + 1 ;

    int defaultObjectCopierFactoryId ;

    private Map<Integer,Policy> policyMap = new HashMap<Integer,Policy>() ;	

    public static final Policies defaultPolicies 
	= new Policies() ;

    public static final Policies rootPOAPolicies
        = new Policies(
	    ThreadPolicyValue._ORB_CTRL_MODEL,
            LifespanPolicyValue._TRANSIENT,
            IdUniquenessPolicyValue._UNIQUE_ID,
            IdAssignmentPolicyValue._SYSTEM_ID,
            ImplicitActivationPolicyValue._IMPLICIT_ACTIVATION,
            ServantRetentionPolicyValue._RETAIN,
            RequestProcessingPolicyValue._USE_ACTIVE_OBJECT_MAP_ONLY ) ;
			  
    private int[] poaPolicyValues ;

    private int getPolicyValue( int id )
    {
        return poaPolicyValues[ id - MIN_POA_POLICY_ID ] ;
    }

    private void setPolicyValue( int id, int value ) 
    {
        poaPolicyValues[ id - MIN_POA_POLICY_ID ] = value ;
    }
    
    private Policies(
        int threadModel, 
	int lifespan, 
	int idUniqueness, 
	int idAssignment,
	int implicitActivation, 
        int retention, 
	int requestProcessing )
    {
 	poaPolicyValues = new int[] {
	    threadModel,
	    lifespan,
	    idUniqueness,
	    idAssignment,
	    implicitActivation,
	    retention,
	    requestProcessing };
    }

    private Policies() {
        this( ThreadPolicyValue._ORB_CTRL_MODEL,
	    LifespanPolicyValue._TRANSIENT,
	    IdUniquenessPolicyValue._UNIQUE_ID,
	    IdAssignmentPolicyValue._SYSTEM_ID,
	    ImplicitActivationPolicyValue._NO_IMPLICIT_ACTIVATION,
	    ServantRetentionPolicyValue._RETAIN,
	    RequestProcessingPolicyValue._USE_ACTIVE_OBJECT_MAP_ONLY ) ;
    }

    public String toString() {
	StringBuffer buffer = new StringBuffer();
	buffer.append( "Policies[" ) ;
	boolean first = true ;
	for ( Policy p : policyMap.values() ) {
	    if (first)
		first = false ;
	    else
		buffer.append( "," ) ;

	    buffer.append( p.toString() ) ;
	} 
	buffer.append( "]" ) ;
	return buffer.toString() ;
    }

    /* Returns the integer value of the POA policy, if this is a 
     * POA policy, otherwise returns -1.
     */
    private int getPOAPolicyValue( Policy policy) 
    {
        if (policy instanceof ThreadPolicy) {
	    return ((ThreadPolicy) policy).value().value();
        } else if (policy instanceof LifespanPolicy) {
	    return ((LifespanPolicy) policy).value().value();
        } else if (policy instanceof IdUniquenessPolicy) {
	    return ((IdUniquenessPolicy) policy).value().value();
        } else if (policy instanceof IdAssignmentPolicy) {
	    return ((IdAssignmentPolicy) policy).value().value();
        } else if (policy instanceof ServantRetentionPolicy) {
	    return ((ServantRetentionPolicy) policy).value().value();
        } else if (policy instanceof RequestProcessingPolicy) {
	    return  ((RequestProcessingPolicy) policy).value().value();
        } else if (policy instanceof ImplicitActivationPolicy) {
	    return ((ImplicitActivationPolicy) policy).value().value();
        }  else
	    return -1 ;
    }

    /** If any errors were found, throw INVALID_POLICY with the smallest
     * index of any offending policy.
     */
    private void checkForPolicyError( BitSet errorSet ) throws InvalidPolicy
    {
        for (short ctr=0; ctr<errorSet.length(); ctr++ )
	    if (errorSet.get(ctr)) 
		throw new InvalidPolicy(ctr);
    }

    /** Add the first index in policies at which the policy is of type
    * policyId to errorSet, if the polictId is in policies (it may not be).  
    */
    private void addToErrorSet( Policy[] policies, int policyId, 
	BitSet errorSet )
    {
        for (int ctr=0; ctr<policies.length; ctr++ )
	    if (policies[ctr].policy_type() == policyId) {
		errorSet.set( ctr ) ;
		return ;
	    }
    }

    /** Main constructor used from POA::create_POA.  This need only be visible 
    * within the POA package.
    */
    Policies(Policy[] policies, int id ) throws InvalidPolicy 
    {
	// Make sure the defaults are set according to the POA spec
	this();			

	defaultObjectCopierFactoryId = id ;

	if ( policies == null )
	    return;

	// Set to record all indices in policies for which errors
	// were observed.
        BitSet errorSet = new BitSet( policies.length ) ;

	for(short i = 0; i < policies.length; i++) {
	    Policy policy = policies[i];
	    int POAPolicyValue = getPOAPolicyValue( policy ) ;

	    // Save the policy in policyMap to support 
	    // POA.get_effective_policy, if it was not already saved
	    // in policyMap.
	    int key = policy.policy_type() ;
	    Policy prev = policyMap.get( key ) ;
	    if (prev == null) 
	        policyMap.put( key, policy ) ;

	    if (POAPolicyValue >= 0) {
	        setPolicyValue( key, POAPolicyValue  ) ;

		// if the value of this POA policy was previously set to a 
		// different value than the current value given in 
		// POAPolicyValue, record an error.
		if ((prev != null) && 
		    (getPOAPolicyValue( prev ) != POAPolicyValue))
		    errorSet.set( i ) ;
	    }
	}

	// Check for bad policy combinations

	// NON_RETAIN requires USE_DEFAULT_SERVANT or USE_SERVANT_MANAGER
	if (!retainServants() && useActiveMapOnly() ) {
	    addToErrorSet( policies, SERVANT_RETENTION_POLICY_ID.value, 
		errorSet ) ;
	    addToErrorSet( policies, REQUEST_PROCESSING_POLICY_ID.value, 
		errorSet ) ;
	}

	// IMPLICIT_ACTIVATION requires SYSTEM_ID and RETAIN
	if (isImplicitlyActivated()) {
	    if (!retainServants()) {
		addToErrorSet( policies, IMPLICIT_ACTIVATION_POLICY_ID.value, 
		    errorSet ) ;
		addToErrorSet( policies, SERVANT_RETENTION_POLICY_ID.value, 
		    errorSet ) ;
	    }

	    if (!isSystemAssignedIds()) {
		addToErrorSet( policies, IMPLICIT_ACTIVATION_POLICY_ID.value, 
		    errorSet ) ;
		addToErrorSet( policies, ID_ASSIGNMENT_POLICY_ID.value, 
		    errorSet ) ;
	    } 
	}

	checkForPolicyError( errorSet ) ;
    }
	
    public Policy get_effective_policy( int type )
    {
	return policyMap.get(type) ;
    }

    /* Thread Policies */
    public final boolean isOrbControlledThreads() {
	return getPolicyValue( THREAD_POLICY_ID.value ) == 
	    ThreadPolicyValue._ORB_CTRL_MODEL;
    }
    public final boolean isSingleThreaded() {
	return getPolicyValue( THREAD_POLICY_ID.value ) == 
	    ThreadPolicyValue._SINGLE_THREAD_MODEL;
    }

    /* Lifespan */
    public final boolean isTransient() {
	return getPolicyValue( LIFESPAN_POLICY_ID.value ) == 
	    LifespanPolicyValue._TRANSIENT;
    }
    public final boolean isPersistent() {
	return getPolicyValue( LIFESPAN_POLICY_ID.value ) == 
	    LifespanPolicyValue._PERSISTENT;
    }

    /* ID Uniqueness */
    public final boolean isUniqueIds() {
	return getPolicyValue( ID_UNIQUENESS_POLICY_ID.value ) == 
	    IdUniquenessPolicyValue._UNIQUE_ID;
    }
    public final boolean isMultipleIds() {
	return getPolicyValue( ID_UNIQUENESS_POLICY_ID.value ) == 
	    IdUniquenessPolicyValue._MULTIPLE_ID;
    }

    /* ID Assignment */
    public final boolean isUserAssignedIds() {
	return getPolicyValue( ID_ASSIGNMENT_POLICY_ID.value ) == 
	    IdAssignmentPolicyValue._USER_ID;
    }
    public final boolean isSystemAssignedIds() {
	return getPolicyValue( ID_ASSIGNMENT_POLICY_ID.value ) == 
	    IdAssignmentPolicyValue._SYSTEM_ID;
    }

    /* Servant Rentention */
    public final boolean retainServants() {
	return getPolicyValue( SERVANT_RETENTION_POLICY_ID.value ) == 
	    ServantRetentionPolicyValue._RETAIN;
    }

    /* Request Processing */
    public final boolean useActiveMapOnly() {
	return getPolicyValue( REQUEST_PROCESSING_POLICY_ID.value ) == 
	    RequestProcessingPolicyValue._USE_ACTIVE_OBJECT_MAP_ONLY;
    }
    public final boolean useDefaultServant() {
	return getPolicyValue( REQUEST_PROCESSING_POLICY_ID.value ) == 
	    RequestProcessingPolicyValue._USE_DEFAULT_SERVANT;
    }
    public final boolean useServantManager() {
	return getPolicyValue( REQUEST_PROCESSING_POLICY_ID.value ) == 
	    RequestProcessingPolicyValue._USE_SERVANT_MANAGER;
    }

    /* Implicit Activation */
    public final boolean isImplicitlyActivated() {
	return getPolicyValue( IMPLICIT_ACTIVATION_POLICY_ID.value ) == 
	ImplicitActivationPolicyValue._IMPLICIT_ACTIVATION;
    }

    /* proprietary servant caching policy */
    public final int servantCachingLevel()
    {
	ServantCachingPolicy policy = 
	    ServantCachingPolicy.class.cast( policyMap.get( 
		ORBConstants.SERVANT_CACHING_POLICY ) ) ;

	if (policy == null)
	    return ServantCachingPolicy.NO_SERVANT_CACHING ;
	else
	    return policy.getType() ;
    }

    public final boolean forceZeroPort()
    {
	ZeroPortPolicy policy = 
	    ZeroPortPolicy.class.cast( policyMap.get( 
		ORBConstants.ZERO_PORT_POLICY ) ) ;

	if (policy == null)
	    return false ;
	else
	    return policy.forceZeroPort() ;
    }

    public final int getCopierId() 
    {
	CopyObjectPolicy policy = 
	    CopyObjectPolicy.class.cast( policyMap.get( 
		ORBConstants.COPY_OBJECT_POLICY ) ) ;

	if (policy != null)
	    return policy.getValue() ;
	else
	    return defaultObjectCopierFactoryId ;
    }
}
