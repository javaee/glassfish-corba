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

import java.util.Set;
import java.util.HashSet;

import org.omg.CORBA.CompletionStatus ;

import org.omg.PortableServer.POAManager;
import org.omg.PortableServer.POAManagerPackage.State;
import org.omg.PortableServer.POA;

import org.omg.PortableInterceptor.DISCARDING ;
import org.omg.PortableInterceptor.ACTIVE ;
import org.omg.PortableInterceptor.HOLDING ;
import org.omg.PortableInterceptor.INACTIVE ;
import org.omg.PortableInterceptor.NON_EXISTENT ;

import com.sun.corba.se.spi.protocol.PIHandler ;

import com.sun.corba.se.spi.orbutil.generic.MultiSet ;

import com.sun.corba.se.impl.logging.POASystemException ;

import com.sun.corba.se.impl.orbutil.ORBUtility ;

import org.glassfish.gmbal.ManagedObject ;
import org.glassfish.gmbal.ManagedAttribute ;
import org.glassfish.gmbal.ManagedOperation ;
import org.glassfish.gmbal.Description ;
import org.glassfish.gmbal.ParameterNames ;

/** POAManagerImpl is the implementation of the POAManager interface.
 *  Its public methods are activate(), hold_requests(), discard_requests()
 *  and deactivate().
 */

@ManagedObject
@Description( "A POAManager which controls invocations of its POAs")
public class POAManagerImpl extends org.omg.CORBA.LocalObject implements 
    POAManager
{
    private static final long serialVersionUID = -751471445699682659L;

    private final POAFactory factory ;	// factory which contains global state 
					// for all POAManagers 
    private PIHandler pihandler ;	// for adapterManagerStateChanged
    private State state;		// current state of this POAManager
    private Set<POAImpl> poas =
        new HashSet<POAImpl>(4) ;       // all poas controlled by this POAManager
    private int nInvocations=0;		// Number of invocations in progress
    private int nWaiters=0;		// Number of threads waiting for 
					// invocations to complete
    private int myId = 0 ;		// This POAManager's ID
    private boolean debug ;
    private boolean explicitStateChange ; // initially false, set true as soon as 
					// one of activate, hold_request, 
					// discard_request, or deactivate is called.

    private static final boolean AM_DEBUG = false ;

    @ManagedAttribute
    @Description( "The set of POAs managed by this POAManager" )
    Set<POAImpl> getManagedPOAs() {
        return new HashSet<POAImpl>( poas ) ;
    }

    @ManagedAttribute
    @Description( "Number of active invocations executing in this POAManager" )
    public synchronized int numberOfInvocations() {
        return nInvocations ;
    }

    @ManagedAttribute
    @Description( "Number of threads waiting for invocations to complete in this POAManager" )
    public synchronized int numberOfWaiters() {
        return nWaiters ;
    }

    @ManagedAttribute
    @Description( "The current state of this POAManager" ) 
    public synchronized String displayState() {
        return stateToString( state ) ;
    }

    /** activeManagers is the set of POAManagerImpls for which a thread has called
     * enter without exit 1 or more times.  Once a thread has entered a POAManager,
     * it must be able to re-enter the POAManager, even if the manager is HOLDING,
     * because state transitions can be deferred until all threads have completed execution
     * and called exit().  Without this change, one thread can be blocked on the
     * state change method, and another thread that has entered the POAManager once
     * can be blocked from re-entry on a nested co-located call.  This leads to a 
     * permanent deadlock between the two threads.  See Bug 6586417.
     *
     * To avoid this, we create a set of active managers, and record which managers
     * a particular thread is using.  A thread may re-enter any manager in HOLDING state 
     * once it has entered it for the first time.  Note that POAManagerImpl uses the
     * default equals and hashCode methods inherited from Object.  This is fine,
     * because two distinct POAManagerImpl instances always represent distinct
     * POAManagerImpls.
     *
     * This is only a partial solution to the problem, but it should be sufficient for
     * the app server, because all EJBs in the app server share the same POAManager.
     * The problem in general is that state changes between multiple POAManager and
     * invocation threads that make co-located calls to different POAManagers can still
     * deadlock.  This problem requires a different solution, because the hold_requests 
     * method may have already returned when the active thread needs to enter the 
     * holding POAManager, so we can't just let the thread in.  I think in this case
     * we need to reject the request because it may cause a deadlock.  So, checkState
     * needs to throw a TRANSIENT exception if it finds that the thread is already active
     * in one or more POAManagers, AND it tries to enter a new POAManager.  Such exceptions
     * should be re-tried by the client, and will succeed after 
     * the holding POAManagers have been resumed.
     *
     * Another possible route to fix the app server bug (more globally) is to have the RFM 
     * suspend method use discard instead of hold.  This may be better in some ways, 
     * but early tests with that approach led to some problems (which I can't recall right now).  
     * I suspect e the issues may have been related to problems with the client-side retry logic,
     * but those problems have now been fixed.  In any case, we need to fix the POAManager
     * issues.
     */
    private static ThreadLocal<MultiSet<POAManagerImpl>> activeManagers =
	new ThreadLocal<MultiSet<POAManagerImpl>>() {
	    public MultiSet<POAManagerImpl> initialValue() {
		return new MultiSet<POAManagerImpl>() ;
	    }
	} ;


    private String stateToString( State state )
    {
	switch (state.value()) {
	    case State._HOLDING : return "HOLDING" ;
	    case State._ACTIVE : return "ACTIVE" ;
	    case State._DISCARDING : return "DISCARDING" ;
	    case State._INACTIVE : return "INACTIVE" ;
	}

	return "State[UNKNOWN]" ;
    }

    @Override
    public int hashCode() 
    {
	return myId ;
    }

    @Override
    public boolean equals( Object obj ) 
    {
	if (obj == this) {
	    return true ;
	}

	if (!(obj instanceof POAManagerImpl)) {
	    return false ;
	}

	POAManagerImpl other = (POAManagerImpl)obj ;

	return other.myId == myId ;
    }

    @Override
    public synchronized String toString()
    {
	return "POAManagerImpl[" + myId + 
	    "," + stateToString(state) +
	    ",nInvocations=" + nInvocations + 
	    ",nWaiters=" + nWaiters + "]" ;
    }

    @ManagedAttribute
    @Description( "The POAFactory that manages this POAManager" )
    POAFactory getFactory()
    {
	return factory ;
    }

    PIHandler getPIHandler()
    {
	return pihandler ;
    }

    private void countedWait()
    {
	try {
	    if (debug) {
		ORBUtility.dprint( this, "Calling countedWait on POAManager " +
		    this + " nWaiters=" + nWaiters ) ;
	    }

	    nWaiters++ ;
	    wait(); 
	} catch ( java.lang.InterruptedException ex ) {
	    // NOP
	} finally {
	    nWaiters-- ;

	    if (debug) {
		ORBUtility.dprint( this, "Exiting countedWait on POAManager " +
		    this + " nWaiters=" + nWaiters ) ;
	    }
	}
    }

    private void notifyWaiters() 
    {
	if (debug) {
	    ORBUtility.dprint( this, "Calling notifyWaiters on POAManager " +
		this + " nWaiters=" + nWaiters ) ;
	}

	if (nWaiters >0)
	    notifyAll() ;
    }

    @ManagedAttribute
    @Description( "The ID of this POAManager" )
    public int getManagerId() 
    {
	return myId ;
    }

    POAManagerImpl( POAFactory factory, PIHandler pihandler )
    {
	this.factory = factory ;
        factory.addPoaManager(this);
	this.pihandler = pihandler ;
	myId = factory.newPOAManagerId() ;
	state = State.HOLDING;
	debug = factory.getORB().poaDebugFlag ;
	explicitStateChange = false ;
	
	if (debug) {
	    ORBUtility.dprint( this, "Creating POAManagerImpl " + this ) ;
	}
    }

    synchronized void addPOA(POA poa)
    {
	// XXX This is probably not the correct error
	if (state.value() == State._INACTIVE) {
	    POASystemException wrapper = factory.getWrapper();
	    throw wrapper.addPoaInactive( CompletionStatus.COMPLETED_NO ) ;
	}
	    
        poas.add( (POAImpl)poa );
    }

    synchronized void removePOA(POA poa)
    {
        poas.remove( (POAImpl)poa );
        if ( poas.isEmpty() ) {
            factory.removePoaManager(this);
	}
    }

    @ManagedAttribute
    @Description( "The ObjectReferenceTemplate state of this POAManager" )
    public short getORTState() 
    {
	switch (state.value()) {
	    case State._HOLDING    : return HOLDING.value ;
	    case State._ACTIVE     : return ACTIVE.value ;
	    case State._INACTIVE   : return INACTIVE.value ;
	    case State._DISCARDING : return DISCARDING.value ;
	    default		   : return NON_EXISTENT.value ;
	}
    }

/****************************************************************************
 * The following four public methods are used to change the POAManager's state.
 *
 * A note on the design of synchronization code:
 * There are 4 places where a thread would need to wait for a condition:
 *      - in hold_requests, discard_requests, deactivate, enter 
 * There are 5 places where a thread notifies a condition:
 *      - in activate, hold_requests, discard_requests, deactivate, exit 
 *
 * Since each notify needs to awaken waiters in several of the 4 places,
 * and since wait() in Java has the nice property of releasing the lock
 * on its monitor before sleeping, it seemed simplest to have just one
 * monitor object: "this". Thus all notifies will awaken all waiters.
 * On waking up, each waiter verifies that the condition it was waiting 
 * for is satisfied, otherwise it goes back into a wait().
 * 
 ****************************************************************************/

    /**
     * <code>activate</code>
     * <b>Spec: pages 3-14 thru 3-18</b>
     */
    @ManagedOperation
    @Description( "Make this POAManager active, so it can handle new requests" ) 
    public synchronized void activate()
        throws org.omg.PortableServer.POAManagerPackage.AdapterInactive
    {
	explicitStateChange = true ;

	if (debug) {
	    ORBUtility.dprint( this, 
		"Calling activate on POAManager " + this ) ;
	}

	try {
	    if ( state.value() == State._INACTIVE )
		throw new org.omg.PortableServer.POAManagerPackage.AdapterInactive();

	    // set the state to ACTIVE
	    state = State.ACTIVE;
	    
	    pihandler.adapterManagerStateChanged( myId, getORTState() ) ;

	    // Notify any invocations that were waiting because the previous
	    // state was HOLDING, as well as notify any threads that were waiting
	    // inside hold_requests() or discard_requests(). 
	    notifyWaiters();
	} finally {
	    if (debug) {
		ORBUtility.dprint( this, 
		    "Exiting activate on POAManager " + this ) ;
	    }
	}
    }

    /**
     * <code>hold_requests</code>
     * <b>Spec: pages 3-14 thru 3-18</b>
     */
    @ManagedOperation
    @Description( "Hold all requests to this POAManager" ) 
    public synchronized void hold_requests(boolean wait_for_completion)
        throws org.omg.PortableServer.POAManagerPackage.AdapterInactive
    {
	explicitStateChange = true ;

	if (debug) {
	    ORBUtility.dprint( this, 
		"Calling hold_requests on POAManager " + this ) ;
	}

	try {
	    if ( state.value() == State._INACTIVE )
		throw new org.omg.PortableServer.POAManagerPackage.AdapterInactive();
	    // set the state to HOLDING
	    state  = State.HOLDING;

	    pihandler.adapterManagerStateChanged( myId, getORTState() ) ;

	    // Notify any threads that were waiting in the wait() inside
	    // discard_requests. This will cause discard_requests to return
	    // (which is in conformance with the spec).
	    notifyWaiters(); 

	    if ( wait_for_completion ) {
		while ( state.value() == State._HOLDING && nInvocations > 0 ) {
		    countedWait() ;
		}
	    }
	} finally {
	    if (debug) {
		ORBUtility.dprint( this, 
		    "Exiting hold_requests on POAManager " + this ) ;
	    }
	}
    }

    /**
     * <code>discard_requests</code>
     * <b>Spec: pages 3-14 thru 3-18</b>
     */
    @ManagedOperation
    @ParameterNames( { "waitForCompletion" } )
    @Description( "Make this POAManager discard all incoming requests" ) 
    public synchronized void discard_requests(boolean wait_for_completion)
        throws org.omg.PortableServer.POAManagerPackage.AdapterInactive
    {
	explicitStateChange = true ;

	if (debug) {
	    ORBUtility.dprint( this, 
		"Calling hold_requests on POAManager " + this ) ;
	}
	 
	try {
	    if ( state.value() == State._INACTIVE )
		throw new org.omg.PortableServer.POAManagerPackage.AdapterInactive();

	    // set the state to DISCARDING
	    state = State.DISCARDING;

	    pihandler.adapterManagerStateChanged( myId, getORTState() ) ;

	    // Notify any invocations that were waiting because the previous
	    // state was HOLDING. Those invocations will henceforth be rejected with
	    // a TRANSIENT exception. Also notify any threads that were waiting
	    // inside hold_requests().
	    notifyWaiters(); 

	    if ( wait_for_completion ) {
		while ( state.value() == State._DISCARDING && nInvocations > 0 ) {
		    countedWait() ;
		}
	    }
	} finally {
	    if (debug) {
		ORBUtility.dprint( this, 
		    "Exiting hold_requests on POAManager " + this ) ;
	    }
	}
    }

    /**
     * <code>deactivate</code>
     * <b>Spec: pages 3-14 thru 3-18</b>
     * Note: INACTIVE is a permanent state.
     */

    public void deactivate(boolean etherealize_objects, boolean wait_for_completion)
        throws org.omg.PortableServer.POAManagerPackage.AdapterInactive
    {
	explicitStateChange = true ;

	try {
	    synchronized( this ) {
		if (debug) {
		    ORBUtility.dprint( this, 
			"Calling deactivate on POAManager " + this ) ;
		}

		if ( state.value() == State._INACTIVE )
		    throw new org.omg.PortableServer.POAManagerPackage.AdapterInactive();

		state = State.INACTIVE;

		pihandler.adapterManagerStateChanged( myId, getORTState() ) ;

		// Notify any invocations that were waiting because the previous
		// state was HOLDING. Those invocations will then be rejected with
		// an OBJ_ADAPTER exception. Also notify any threads that were waiting
		// inside hold_requests() or discard_requests().
		notifyWaiters();
	    }

	    POAManagerDeactivator deactivator = new POAManagerDeactivator( this,
		etherealize_objects, debug ) ;

	    if (wait_for_completion)
		deactivator.run() ;
	    else {
		Thread thr = new Thread(deactivator) ;
		thr.start() ;
	    }
	} finally { 
	    synchronized(this) {
		if (debug) {
		    ORBUtility.dprint( this, 
			"Exiting deactivate on POAManager " + this ) ;
		}
	    }
	}
    }

    private class POAManagerDeactivator implements Runnable
    {
	private boolean etherealize_objects ;
	private final POAManagerImpl pmi ;
	private boolean debug ;

	POAManagerDeactivator( POAManagerImpl pmi, boolean etherealize_objects,
	    boolean debug )
	{
	    this.etherealize_objects = etherealize_objects ;
	    this.pmi = pmi ;
	    this.debug = debug ;
	}

	public void run() 
	{
	    try {
		synchronized (pmi) {
		    if (debug) {
			ORBUtility.dprint( this,
			    "Calling run with etherealize_objects=" +
			    etherealize_objects + " pmi=" + pmi ) ;
		    }

		    while ( pmi.nInvocations > 0 ) { 
			countedWait() ;
		    }
		}

		if (etherealize_objects) {
                    Set<POAImpl> copyOfPOAs ;

		    // Make sure that poas cannot change while we copy it!
		    synchronized (pmi) {
			if (debug) {
			    ORBUtility.dprint( this,
				"run: Preparing to etherealize with pmi=" + 
				pmi ) ;
			}

                        copyOfPOAs = new HashSet<POAImpl>( pmi.poas ) ;
		    } 

                    for (POAImpl poa : copyOfPOAs) {
			// Each RETAIN+USE_SERVANT_MGR poa
			// must call etherealize for all its objects
                        poa.etherealizeAll();
                    }

                    // XXX What if a new POA is created here before 
                    // etherealization completes?  We would fail to call 
                    // etherealize!
		    synchronized (pmi) {
			if (debug) {
			    ORBUtility.dprint( this,
				"run: removing POAManager and clearing poas " +
				"with pmi=" + pmi ) ;
			}

			factory.removePoaManager(pmi);
			poas.clear();
		    }
		}
	    } finally {
		if (debug) {
		    synchronized (pmi) {
			ORBUtility.dprint( this, "Exiting run" ) ;
		    }
		}
	    }
	}
    }

    /**
     * Added according to the spec CORBA V2.3; this returns the
     * state of the POAManager
     */

    public org.omg.PortableServer.POAManagerPackage.State get_state () {
	return state;
    }

/****************************************************************************
 * The following methods are used on the invocation path.
 ****************************************************************************/

    private void checkState()
    {
	MultiSet<POAManagerImpl> am = activeManagers.get() ;
	if (AM_DEBUG) {
	    ORBUtility.dprint( this, "6586417: in checkState: am = " + am ) ;
	}

	while ( state.value() != State._ACTIVE ) {
	    switch ( state.value() ) {
		case State._HOLDING:
		    // Never block a thread that is already active in this POAManager.
		    if (am.contains( this )) {
			if (AM_DEBUG) {
			    ORBUtility.dprint( this, 
				"6586417: thread is already active in POAManager "
				    + this ) ;
			}

			return ;
		    } else {
			if (am.size() == 0) {
			    while ( state.value() == State._HOLDING ) {
				countedWait() ;
			    }
			} else {
			    if (AM_DEBUG) {
				ORBUtility.dprint( this, 
				    "6586417: thread is active in POAManagers other than " 
				    + this + ": throwing TRANSIENT exception " ) ;
			    }

			    // This thread is already active in one or more other POAManagers.
			    // This could cause a deadlock, so throw a TRANSIENT exception 
			    // to prevent it.
			    throw factory.getWrapper().poaManagerMightDeadlock() ;
			}
		    }
		    break;

		case State._DISCARDING:
		    throw factory.getWrapper().poaDiscarding() ;

		case State._INACTIVE:
		    throw factory.getWrapper().poaInactive() ;
	    }
	}
    }

    synchronized void enter()
    {
	try {
	    if (debug) {
		ORBUtility.dprint( this,
		    "Calling enter for POAManagerImpl " + this ) ;
	    } 

	    checkState();
	    nInvocations++;

	    activeManagers.get().add( this ) ;
	    if (AM_DEBUG) {
		ORBUtility.dprint( this, "6586417: thread is adding " + this 
		    + " to activeManagers" ) ;
	    }
	} finally {
	    if (debug) {
		ORBUtility.dprint( this,
		    "Exiting enter for POAManagerImpl " + this ) ;
	    } 
	}
    }

    synchronized void exit()
    {
	try {
	    if (debug) {
		ORBUtility.dprint( this,
		    "Calling exit for POAManagerImpl " + this ) ;
	    } 

	    activeManagers.get().remove( this ) ;
	    if (AM_DEBUG) {
		ORBUtility.dprint( this, "6586417: Thread is removing " 
		    + this + " from activeManagers" ) ;
	    }

	    nInvocations--; 

	    if ( nInvocations == 0 ) {
		// This notifies any threads that were in the 
		// wait_for_completion loop in hold/discard/deactivate().
		notifyWaiters();
	    }
	} finally {
	    if (debug) {
		ORBUtility.dprint( this,
		    "Exiting exit for POAManagerImpl " + this ) ;
	    } 
	}
    }

    /** Activate the POAManager if no explicit state change has ever been
     * previously invoked.
     */
    public synchronized void implicitActivation() 
    {
	if (!explicitStateChange)
	    try {
		activate() ;
	    } catch (org.omg.PortableServer.POAManagerPackage.AdapterInactive ai) {
		// ignore the exception.
	    }
    }
}
