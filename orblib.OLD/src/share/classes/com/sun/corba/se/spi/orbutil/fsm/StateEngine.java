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

package com.sun.corba.se.spi.orbutil.fsm ;

import java.util.Collections ;
import java.util.Map ;
import java.util.EnumMap ;
import java.util.HashSet ;
import java.util.Set ;
import java.util.Iterator ;

/**
 * Encodes the state transition function for a finite state machine.
 *
 * @author Ken Cavanaugh
 */
public class StateEngine {
    /** Create an empty uninitialized state engine.
     */
    public static StateEngine create() {
	return new StateEngine() ;
    }

    // An action that does nothing at all.
    private static Action emptyAction = new Action.Base( "Empty" ) {
	public void doIt( FSM fsm, Input in ) {
	    // NO-OP
	}
    } ;

    private boolean initializing ;
    private Action defaultAction ;
    private Map<State.Kind,Set<State>> stateKinds ;
    private Map<State.Kind,Set<State>> unmodifiableStateKinds ;

    private StateEngine() {
	initializing = true ;
	defaultAction = new Action.Base("Invalid Transition") {
	    public void doIt( FSM fsm, Input in ) {
		throw new IllegalStateException(
		    "Invalid transition attempted from " + 
			fsm.getState() + " under " + in ) ;
	    }
	} ;

	stateKinds = new EnumMap<State.Kind,Set<State>>( State.Kind.class ) ;
	final Map<State.Kind,Set<State>> map = 
	    new EnumMap<State.Kind,Set<State>>( State.Kind.class ) ;
	for (State.Kind kind : State.Kind.class.getEnumConstants()) {
	    Set<State> states = new HashSet<State>() ;
	    stateKinds.put( kind, states ) ;
	    map.put( kind, Collections.unmodifiableSet( states )) ;
	}

	unmodifiableStateKinds = Collections.unmodifiableMap( stateKinds ) ;
    }

    private void dprint( String msg ) {
	System.out.println( "StateEngine: " + msg ) ;
    }

    private Set<State> getKindSet( State.Kind kind ) {
	Set<State> result = stateKinds.get( kind ) ;
	return result ;
    }

    private void updateStateMap( State oldState, State newState ) {
	if (oldState.getKind() == State.Kind.FINAL)
	    throw new IllegalStateException( 
		"Cannot create a transition that leaves a final state" ) ;

	if (oldState.getKind() == State.Kind.REFERENCE)
	    throw new IllegalStateException( 
		"Cannot create a transition that leaves a reference state" ) ;

	Set<State> oldKindSet = getKindSet( oldState.getKind() ) ;
	oldKindSet.add( oldState ) ;

	Set<State> newKindSet = getKindSet( newState.getKind() ) ;
	newKindSet.add( newState ) ;
    }

    /** Return the set of final states for this state engine.
     * This may be an empty set.
     */
    public Set<State> getStates( State.Kind kind ) {
	return unmodifiableStateKinds.get( kind ) ;
    }

    /** Add a new transition (old,in,guard,act,new) to the state engine.
    * Multiple calls to add with the same old and in are permitted,
    * in which case only a transition in which the guard evaluates to
    * true will be taken.  If no such transition is enabled, a default
    * will be taken.  If more than one transition is enabled, one will
    * be chosen arbitrarily.
    * This method can only be called before done().  An attempt to
    * call it after done() results in an IllegalStateException.
    */
    public StateEngine add( State oldState, Input input, Guard guard, 
	Action action, State newState ) { 
	mustBeInitializing() ;
	updateStateMap( oldState, newState ) ;

	Transition ga = new Transition( guard, action, newState ) ;
	oldState.addTransition( input, ga ) ;

	return this ;
    }

    /** Repeatedly calls add( State, Input, Guard, Action, State ) for
     * each element of input.
     */
    public StateEngine add( State oldState, Set<Input> input, Guard guard, 
	Action action, State newState ) { 

	for (Input in : input)
	    add( oldState, in, guard, action, newState ) ;

	return this ;
    }

    /** Add a transition with a guard that always evaluates to true.
     */
    public StateEngine add( State oldState, Input input, Action action,
	State newState ) {
	mustBeInitializing() ;
	updateStateMap( oldState, newState ) ;

	Transition ta = new Transition( action, newState ) ;
	oldState.addTransition( input, ta ) ;

	return this ;
    }

    /** Repeatedly call add( State, Input, Action, State ) for each 
     * element of input.
     */
    public StateEngine add( State oldState, Set<Input> input, Action action,
	State newState ) {

	for (Input in : input)
	    add( oldState, in, action, newState ) ;

	return this ;
    }

    /** Set the default transition and action for a state.
    * This transition will be used if no more specific transition was
    * defined for the actual input.  Repeated calls to this method
    * simply change the default.
    * This method can only be called before done().  An attempt to
    * call it after done() results in an IllegalStateException.
    */
    public StateEngine setDefault( State oldState, Action action, 
	State newState ) {

	mustBeInitializing() ;

	oldState.setDefaultAction( action ) ;
	oldState.setDefaultNextState( newState ) ;

	return this ;
    }

    /** Equivalent to setDefault( oldState, act, newState ) where act is an
     * action that does nothing.
     */
    public StateEngine setDefault( State oldState, State newState ) {
	return setDefault( oldState, emptyAction, newState ) ;
    }

    /** Euaivalent to setDefault( oldState, oldState ) 
     */
    public StateEngine setDefault( State oldState ) {
	return setDefault( oldState, oldState ) ;
    }

    /** Called after all transitions have been added to the state engine.
    * This provides an opportunity for the implementation to optimize
    * its representation before the state engine is used.  This method 
    * may only be called once.  An attempt to call it more than once
    * results in an IllegalStateException.
    * <P> 
    * Note that a valid StateEngine must satisfy the following conditions:
    * <OL>
    * <LI>It must contain exactly one state with Kind INITIAL.
    * <LI>No final state may have an outgoing transition.
    * </OL>
    * If either of these conditions are violated, done() throws an
    * IllegalStateException.
    */
   public void done() {
	mustBeInitializing() ;

	// optimize FSM here if desired.  For example,
	// we could choose different strategies for implementing
	// the state transition function based on the distribution 
	// of values for states and input labels.

	initializing = false ;
    }

    /** Set the default action used in this state engine.  This is the
    * action that is called whenever there is no applicable transition.
    * Normally this would simply flag an error.  This method can only
    * be called before done().  An attempt to
    * call it after done() results in an IllegalStateException.
    */
    public void setDefaultAction( Action act ) {
	mustBeInitializing() ;
	defaultAction = act ;
    }

    /** Actually perform a state transition on the FSM on 
     * the runner.peek() FSM under Input in.
     */
    public void doIt( Runner runner, Input in, boolean debug ) {
	// This method is present only for debugging.
	// innerDoIt does the actual transition.

	FSM fsm = runner.peek() ;
	if (debug)
	    dprint( "doIt enter: currentState = " + 
		fsm.getState() + " in = " + in ) ;

	try {
	    innerDoIt( runner, in, debug ) ;
	} finally {
	    if (debug)
		dprint( "doIt exit" ) ;
	}
    }

    private State getDefaultNextState( State currentState ) {
	// Use the currentState defaults if 
	// set, otherwise use the state engine default.
	State nextState = currentState.getDefaultNextState() ;
	if (nextState == null)
	    // The state engine default never changes the state
	    nextState = currentState ;

	return nextState ;
    }

    private Action getDefaultAction( State currentState ) {
	Action action = currentState.getDefaultAction() ;
	if (action == null)
	    action = defaultAction ;

	return action ;
    }

    private void innerDoIt( Runner runner, Input in, boolean debug ) {
	if (debug) {
	    dprint( "Calling innerDoIt with input " + in ) ;
	}

	FSM fsm = runner.peek() ;

	// Locals needed for performing the state transition, once we determine
	// the required transition.
	State currentState = null ;
	State nextState = null ;
	Action action = null ;

	// Do until no guard has deferred. 
	boolean deferral = false ;
	do {
	    deferral = false ; // clear this after each deferral!
	    currentState = fsm.getState() ;
	    nextState = getDefaultNextState( currentState ) ;
	    action = getDefaultAction( currentState ) ;

	    if (debug) {
		dprint( "currentState      = " + currentState ) ;
		dprint( "in                = " + in ) ;
		dprint( "default nextState = " + nextState    ) ;
		dprint( "default action    = " + action ) ;
	    }

	    Set<Transition> gas = currentState.getTransitions(in) ;
	    if (gas != null) {
		Iterator<Transition> iter = gas.iterator() ;

		// Search for a guard that is not DISABLED.  
		// All DISABLED means use defaults.
		while (iter.hasNext()) {
		    Transition ga = iter.next() ;
		    Guard.Result gr = ga.getGuard().evaluate( fsm, in ) ;
		    if (debug)
			dprint( "doIt: evaluated " + ga + " with result " 
			    + gr ) ;

		    if (gr == Guard.Result.ENABLED) {
			// ga has the next state and action.
			nextState = ga.getNextState() ;
			action = ga.getAction() ;
			if (debug) {
			    dprint( "nextState = " + nextState ) ;
			    dprint( "action    = " + action ) ;
			}
			break ;
		    } else if (gr == Guard.Result.DEFERRED) {
			deferral = true ;
			break ;
		    }
		}
	    }
	} while (deferral) ;

	performStateTransition( runner, in, nextState, action, debug ) ;

	fsm = runner.peek() ;
	final State state = fsm.getState() ;
	if (state.getKind() == State.Kind.FINAL) {
	    runner.pop() ;
	    final FSM nextFSM = runner.peek() ;
	    if (nextFSM == null) 
		return ;

	    final State st1 = nextFSM.getState() ;
	    final State newState = st1.returnAction( nextFSM, fsm ) ;
	    final StateEngine se = nextFSM.getStateEngine() ;
	    se.performStateTransition( runner, null, newState, null, false ) ;
	}
    }

    // We can't really allow this, because it would invalidate the
    // pre/post semantics.
    // This is needed in Runner.
    void performStateTransition( Runner runner, Input in, 
	State nextState, Action action, boolean debug ) {

	FSM fsm = runner.peek() ;
	State currentState = fsm.getState() ;

	// Perform the state transition.  Pre and post actions are only
	// performed if the state changes (see UML hidden transitions).

	boolean different = !currentState.equals( nextState ) ;

	if (different) {
	    if (debug)
		dprint( "doIt: executing postAction for state " 
		    + currentState ) ;
	    try {
		currentState.postAction( fsm ) ;
	    } catch (Throwable thr) {
		if (debug)
		    dprint( "doIt: postAction threw " + thr ) ;
	    }
	}

	try {
	    // Note that action may be null in a transition, which simply
	    // means that no action is needed.  Note that action.doIt may
	    // throw an exception, in which case the exception is
	    // propagated after making sure that the transition is properly
	    // completed.
	    if (action != null)
		action.doIt( fsm, in ) ;
	} finally {
	    if (different) {
		if (debug)
		    dprint( "doIt: executing preAction for state " 
			+ nextState ) ;

		FSM newFSM = null ;
		try {
		    newFSM = nextState.preAction( fsm ) ;
		} catch (Throwable thr) {
		    if (debug)
			dprint( "doIt: preAction threw " + thr ) ;
		}

		if (newFSM == null) {
		    fsm.setState( nextState ) ;
		} else {
		    runner.push( newFSM ) ;
		}
	    }

	    if (debug)
		dprint( "doIt: state is now " + nextState ) ;
	}
    }

    private void mustBeInitializing() {
	if (!initializing)
	    throw new IllegalStateException( 
		"Invalid method call after initialization completed" ) ;
    }

    private void mustNotBeInitializing() {
	if (initializing)
	    throw new IllegalStateException( 
		"Invalid method call before initialization completed" ) ;
    }
}

// end of StateEngineImpl.java
