/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2005-2007 Sun Microsystems, Inc. All rights reserved.
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

package com.sun.corba.se.impl.oa.rfm;

import java.util.List ;
import java.util.ArrayList ;
import java.util.Map ;
import java.util.HashMap ;
import java.util.Arrays ;

import java.util.concurrent.locks.ReentrantLock ;
import java.util.concurrent.locks.Condition ;

import org.omg.CORBA.Policy ;
import org.omg.CORBA.LocalObject ;

import org.omg.PortableServer.POA ;
import org.omg.PortableServer.POAManager ;
import org.omg.PortableServer.AdapterActivator ;
import org.omg.PortableServer.ServantLocator ;
import org.omg.PortableServer.RequestProcessingPolicyValue ;
import org.omg.PortableServer.LifespanPolicyValue ;
import org.omg.PortableServer.ServantRetentionPolicyValue ;

import org.omg.PortableServer.POAManagerPackage.AdapterInactive ;

import com.sun.corba.se.spi.orb.ORB ;

import com.sun.corba.se.spi.orbutil.generic.Pair ;

import com.sun.corba.se.spi.oa.ObjectAdapter ;
import com.sun.corba.se.spi.oa.rfm.ReferenceFactory ;
import com.sun.corba.se.spi.oa.rfm.ReferenceFactoryManager ;

import com.sun.corba.se.impl.logging.POASystemException ;
import com.sun.corba.se.spi.orbutil.ORBConstants ;
import com.sun.corba.se.spi.trace.Poa;
import org.glassfish.gmbal.Description;
import org.glassfish.gmbal.ManagedObject;

@Poa
@ManagedObject
@Description( "The ReferenceFactoryManager, used to handle dynamic cluster membership updates")
public class ReferenceFactoryManagerImpl 
    extends org.omg.CORBA.LocalObject
    implements ReferenceFactoryManager
{
    private static final long serialVersionUID = -6689846523143143228L;

    private enum RFMState { READY, SUSPENDED } ;

    private static final String PARENT_POA_NAME = "#RFMBase#" ;

    // Initialized in the constructor
    private RFMState state ;
    private final ReentrantLock lock ;
    private final Condition suspendCondition ;
    private final ORB orb ;
    private final POASystemException wrapper ;
    // poatable contains the mapping from the ReferenceFactory name to
    // the ServantLocator and list of policies.  Note that the policy
    // list is stored in the form passed to the create() call: that is,
    // it does not contain the standard policies.
    private final Map<String,Pair<ServantLocator,List<Policy>>> poatable ;
    private final Map<String,ReferenceFactory> factories ;

    private volatile boolean isActive ;

    // Initialized on activation because the root POA is required.
    private POA rootPOA ;
    private List<Policy> standardPolicies ;
    private POA parentPOA ;
    private String[] parentPOAAdapterName ;
    private POAManager manager ; 
    private AdapterActivator activator ; 
	
    private class AdapterActivatorImpl 
	extends LocalObject 
	implements AdapterActivator 
    {
        private static final long serialVersionUID = 7922226881290146012L;

        @Poa
	public boolean unknown_adapter( POA parent, String name ) {
            Pair<ServantLocator,List<Policy>> data = null ;
            synchronized (poatable) {
                // REVISIT: make sure that data can't change concurrently!
                // Should be OK because Pair is immutable.
                data = poatable.get( name ) ;
            }

            if (data == null) {
                return false ;
            } else {
                try {
                    List<Policy> policies = new ArrayList<Policy>() ;
                    // XXX What should we do if data.second() contains
                    // policies with the same ID as standard policies?
                    if (data.second() != null)
                        policies.addAll( data.second() ) ;
                    policies.addAll( standardPolicies ) ;
                    Policy[] arr = policies.toArray( new Policy[policies.size()] ) ;

                    POA child = parentPOA.create_POA( name, manager, arr ) ;
                    child.set_servant_manager( data.first() ) ;
                    return true ;
                } catch (Exception exc) {
                    wrapper.rfmAdapterActivatorFailed( exc ) ;
                    return false ;
                }
            }
	}
    } ;

    // Policy used to indicate that a POA may particpate in the reference manager.
    // If this policy is not present, and a create_POA call is made under base POA,
    // an IORInterceptor will be used reject the POA creation.
    private static class ReferenceManagerPolicy 
	extends LocalObject 
	implements Policy 
    {
	private static Policy thisPolicy = new ReferenceManagerPolicy() ;
        private static final long serialVersionUID = -4780983694679451387L;

	public static Policy getPolicy() {
	    return thisPolicy ;
	}

	private ReferenceManagerPolicy() {
	}

	public int policy_type() {
	    return ORBConstants.REFERENCE_MANAGER_POLICY ;
	}

	public Policy copy() {
	    return this ;
	}

	public void destroy() {
	}
    }

    public ReferenceFactoryManagerImpl( ORB orb ) 
    {
	lock = new ReentrantLock() ;
	suspendCondition = lock.newCondition() ;
	state = RFMState.READY ;
	this.orb = orb ;
	wrapper = orb.getLogWrapperTable().get_OA_LIFECYCLE_POA() ;
    	poatable = new HashMap<String,Pair<ServantLocator,List<Policy>>>() ;
	factories = new HashMap<String,ReferenceFactory>() ;
	isActive = false ;
    }

    public org.omg.PortableServer.POAManagerPackage.State getState()
    {
	return manager.get_state();
    }

    @Poa
    public void activate() 
    {
	lock.lock() ;
	try {
	    if (isActive)
		throw wrapper.rfmNotActive() ;

	    rootPOA = (POA)orb.resolve_initial_references( 
		ORBConstants.ROOT_POA_NAME ) ;

	    standardPolicies = Arrays.asList( 
		ReferenceManagerPolicy.getPolicy(), 
		rootPOA.create_servant_retention_policy( 
		    ServantRetentionPolicyValue.NON_RETAIN ),
		rootPOA.create_request_processing_policy(
		    RequestProcessingPolicyValue.USE_SERVANT_MANAGER ),
		rootPOA.create_lifespan_policy( 
		    LifespanPolicyValue.PERSISTENT ) 
	    ) ;

	    Policy[] policies = { ReferenceManagerPolicy.getPolicy() } ;
	    parentPOA = rootPOA.create_POA( PARENT_POA_NAME,
		null, policies ) ;
	    parentPOAAdapterName = ObjectAdapter.class.cast( parentPOA )
		.getIORTemplate().getObjectKeyTemplate().getObjectAdapterId()
		.getAdapterName() ;

	    manager = parentPOA.the_POAManager() ;
	    activator = new AdapterActivatorImpl() ;
	    parentPOA.the_activator( activator ) ;
	    manager.activate() ;

	    // Don't activate if there is a failure
	    isActive = true ;
	} catch (Exception exc) {
	    throw wrapper.rfmActivateFailed( exc ) ;
	} finally {
	    lock.unlock() ;
	}
    }

    // XXX rfmMightDeadlock exceptions are a problem, because it is possible
    // to attempt to deploy an EJB while the cluster shape is changing.
    // We really need to enqueue (at least) create calls while suspended.
    // It may also be better to get rid of separate suspend/resume calls, instead
    // passing an object to a method that does suspend/resume (as in 
    // doPrivileged).  See GF issue 4560.
    @Poa
    public ReferenceFactory create( final String name, 
				    final String repositoryId,
				    final List<Policy> policies,
				    final ServantLocator locator ) 
    {
	lock.lock() ;
	try {
	    if (state == RFMState.SUSPENDED)
		throw wrapper.rfmMightDeadlock() ;

	    if (!isActive) 
		throw wrapper.rfmNotActive() ;

	    List<Policy> newPolicies = null ;
	    if (policies != null)
		newPolicies = new ArrayList<Policy>( policies ) ;

	    // Store an entry for the appropriate POA in the POA table,
	    // which is used by the AdapterActivator on the root.
	    synchronized (poatable) {
		poatable.put( name, new Pair( locator, newPolicies ) ) ;
	    }

	    ReferenceFactory factory = new ReferenceFactoryImpl( this, name, repositoryId ) ;
	    factories.put( name, factory ) ;
	    return factory ;
	} finally {
	    lock.unlock() ;
	}
    }

    @Poa
    public ReferenceFactory find( String[] adapterName ) 
    {
	lock.lock() ;
	try {
	    if (state == RFMState.SUSPENDED)
		throw wrapper.rfmMightDeadlock() ;

	    if (!isActive) {
		return null;
	    }
	    
	    int expectedLength = parentPOAAdapterName.length + 1 ;

	    if (expectedLength != adapterName.length)
		return null ;

	    for (int ctr=0; ctr<expectedLength-1; ctr++)
		if (!adapterName[ctr].equals( parentPOAAdapterName[ctr] ))
		    return null ;

	    return factories.get( adapterName[expectedLength-1] ) ;
	} finally {
	    lock.unlock() ;
	}
    }

    public ReferenceFactory find( String name ) {
	lock.lock() ;
	try {
	    if (state == RFMState.SUSPENDED)
		throw wrapper.rfmMightDeadlock() ;

	    if (!isActive)
		return null ;

	    return factories.get( name ) ;
	} finally {
	    lock.unlock() ;
	}
    }

    // We need to prevent new requests from being
    // processed while we reconfigure the POAs
    // in the RFM.  This could be done either by
    // hold_requests or discard_requests.  Hold will
    // cause incoming requests to be suspended, which
    // could rapidly consume all of the threads in the
    // threadpool.  Instead, we will discard the 
    // requests, which causes a TRANSIENT system
    // exception to be sent to the client.  All
    // TRANSIENT system exceptions will be retried
    // on the client side to the same endpoint.
    // manager.discard_requests( true ) ; 

    @Poa
    public void suspend() 
    {
        lock.lock() ;

        // wait until all requests in the manager have completed.
        try {
            if (!isActive) {
                throw wrapper.rfmNotActive() ;
            }

            while (state == RFMState.SUSPENDED)
                try {
                    suspendCondition.await() ;
                } catch (InterruptedException exc) {
                    throw wrapper.rfmSuspendConditionWaitInterrupted() ;
                }

            // At this point, the state must be READY, and any other
            // suspending thread has released the lock.  So now
            // we set the state back to SUSPENDED, drop the lock,
            // and continue.

            state = RFMState.SUSPENDED ;
        } finally {
            lock.unlock() ;
        }

        // do NOT hold the RFM lock here, because then we would hold
        // first the RFM, and then the POAManager lock.  Another thread
        // could reverse the order, leading to a deadlock.  See bug
        // 6586417.
        try {
            manager.hold_requests( true ) ;
        } catch (AdapterInactive ai) {
            // This should never happen
            throw wrapper.rfmManagerInactive( ai ) ;
        }
    }

    @Poa
    public void resume() 
    {
        lock.lock() ;

        try {
            if (!isActive)
                throw wrapper.rfmNotActive() ;

            state = RFMState.READY ;
            suspendCondition.signalAll() ;
        } finally {
            lock.unlock() ;
        }

        // Allow new requests to start.  This will lazily
        // re-create POAs as needed through the parentPOA's
        // AdapterActivator.
        try {
            manager.activate() ;
        } catch (AdapterInactive ai) {
            // This should never happen
            throw wrapper.rfmManagerInactive( ai ) ;
        }
    }

    @Poa
    public void restartFactories(
        Map<String,Pair<ServantLocator,List<Policy>>> updates )
    {
	lock.lock() ;
	try {
	    if (!isActive)
		throw wrapper.rfmNotActive() ;

	    if (state != RFMState.SUSPENDED)
		throw wrapper.rfmMethodRequiresSuspendedState( "restartFactories" ) ;
	} finally {
	    lock.unlock() ;
	}

        if (updates == null)
            throw wrapper.rfmNullArgRestart() ;

        synchronized (poatable) {
            // Update the poatable with the updates information.
            poatable.putAll( updates ) ;
        }

        try {
            // Now destroy all POAs that are used to
            // implement ReferenceFactory instances.
            for (POA poa : parentPOA.the_children()) {
                poa.destroy( false, true ) ;
            }
        } catch (Exception exc) {
            throw wrapper.rfmRestartFailed( exc ) ;
        }
    }

    public void restartFactories() {
	restartFactories( new HashMap<String,Pair<ServantLocator,List<Policy>>>() ) ;
    }

    /** Restart all ReferenceFactories.  This is done safely, so that
     * any request against object references created from these factories
     * complete correctly.  Restart does not return until all restart
     * activity completes.
     * @param policyUpdates is a map giving the updated policies for
     * some or all of the ReferenceFactory instances in this ReferenceFactoryManager.
     * This parameter must not be null.
     */
    @Poa
    public void restart( Map<String,Pair<ServantLocator,List<Policy>>> updates ) 
    {
        suspend() ;
        try {
            restartFactories( updates ) ;
        } finally {
            resume() ;
        }
    }

    /** Restart all ReferenceFactories.  This is done safely, so that
     * any request against object references created from these factories
     * complete correctly.  Restart does not return until all restart
     * activity completes.  Equivalent to calling restart( new Map() ).
     */
    public void restart() {
	restart( new HashMap<String,Pair<ServantLocator,List<Policy>>>() ) ;
    }

    // Methods used to implement the ReferenceFactory interface.
    // ReferenceFactoryImpl just delegates to these methods.
    @Poa
    org.omg.CORBA.Object createReference( String name, byte[] key,
	String repositoryId ) 
    {
        try {
            POA child = parentPOA.find_POA( name, true ) ;
            return child.create_reference_with_id( key, repositoryId ) ;
        } catch (Exception exc) {
            throw wrapper.rfmCreateReferenceFailed( exc ) ;
        }
    }

    // Called from ReferenceFactoryImpl.
    @Poa
    void destroy( String name ) {
        try {
            POA child = parentPOA.find_POA( name, true ) ;
            synchronized (poatable) {
                poatable.remove( name ) ;
            }

            lock.lock() ;
            try {
                factories.remove( name ) ;
            } finally {
                lock.unlock() ;
            }

            // Wait for all requests to complete before completing
            // destruction of the POA.
            child.destroy( false, true ) ;
        } catch (Exception exc) {
            throw wrapper.rfmDestroyFailed( exc ) ;
        }
    }

    // Called from ReferenceManagerConfigurator.
    void validatePOACreation( POA poa ) {
	// Some POAs are created before the ReferenceFactoryManager is created.
	// In particular, the root POA and parentPOA are created before isActive
	// is set to true.  Don't check in these case.
	if (!isActive)
	    return ;

	// Only check the case where poa does not have the reference manager
	// policy.  We assume that we handle the policy correctly inside
	// the RFM itself.
	Policy policy = ObjectAdapter.class.cast(poa).getEffectivePolicy( 
	    ORBConstants.REFERENCE_MANAGER_POLICY ) ;
	if (policy != null)
	    return ;

	// At this point, we know that poa comes from outside the
	// active RFM.  If poa's parent POA has the policy, we have an
	// error.
	POA parent = poa.the_parent() ;
	Policy parentPolicy = 
	    ObjectAdapter.class.cast(parent).getEffectivePolicy(
	    ORBConstants.REFERENCE_MANAGER_POLICY ) ;
	if (parentPolicy != null)
	    throw wrapper.rfmIllegalParentPoaUsage() ;

	// If poa's POAManager is the manager for the RFM, we have
	// an error.
	if (poa.the_POAManager() == manager) 
	    throw wrapper.rfmIllegalPoaManagerUsage() ;
    }

    // locking not required
    @Poa
    public boolean isRfmName( String[] adapterName ) 
    {
        if (!isActive) {
            return false ;
        }

        int expectedLength = parentPOAAdapterName.length + 1 ;

        if (expectedLength != adapterName.length)
            return false ;

        for (int ctr=0; ctr<expectedLength-1; ctr++)
            if (!adapterName[ctr].equals( parentPOAAdapterName[ctr] ))
                return false ;

        return true ;
    }
}

// End of file.
