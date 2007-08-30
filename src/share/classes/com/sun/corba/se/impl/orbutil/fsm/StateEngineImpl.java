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

package com.sun.corba.se.impl.orbutil.fsm ;

import java.util.HashMap ;
import java.util.HashSet ;
import java.util.Set ;
import java.util.Iterator ;

import com.sun.corba.se.spi.orbutil.fsm.Input ;
import com.sun.corba.se.spi.orbutil.fsm.Guard ;
import com.sun.corba.se.spi.orbutil.fsm.Action ;
import com.sun.corba.se.spi.orbutil.fsm.ActionBase ;
import com.sun.corba.se.spi.orbutil.fsm.State ;
import com.sun.corba.se.spi.orbutil.fsm.StateEngine ;
import com.sun.corba.se.spi.orbutil.fsm.StateImpl ;
import com.sun.corba.se.spi.orbutil.fsm.FSM ;
import com.sun.corba.se.spi.orbutil.fsm.FSMImpl ;

import com.sun.corba.se.impl.orbutil.fsm.GuardedAction ;

/**
 * Encodes the state transition function for a finite state machine.
 *
 * @author Ken Cavanaugh
 */
public class StateEngineImpl implements StateEngine
{
    // An action that does nothing at all.
    private static Action emptyAction = new ActionBase( "Empty" ) 
    {
	public void doIt( FSM fsm, Input in )
	{
	}
    } ;

    private boolean initializing ;
    private Action defaultAction ;

    private void dprint( String msg ) {
	System.out.println( "StateEngineImpl: " + msg ) ;
    }

    public StateEngineImpl()
    {
	initializing = true ;
	defaultAction = new ActionBase("Invalid Transition")
	    {
		public void doIt( FSM fsm, Input in )
		{
		    throw new IllegalStateException(
			"Invalid transition attempted from " + 
			    fsm.getState() + " under " + in ) ;
		}
	    } ;
    }

    public StateEngine add( State oldState, Input input, Guard guard, Action action,
	State newState ) throws IllegalArgumentException,
	IllegalStateException
    {
	mustBeInitializing() ;

	StateImpl oldStateImpl = (StateImpl)oldState ;
	GuardedAction ga = new GuardedAction( guard, action, newState ) ;
	oldStateImpl.addGuardedAction( input, ga ) ;

	return this ;
    }

    public StateEngine add( State oldState, Input input, Action action,
	State newState ) throws IllegalArgumentException,
	IllegalStateException
    {
	mustBeInitializing() ;

	StateImpl oldStateImpl = (StateImpl)oldState ;
	GuardedAction ta = new GuardedAction( action, newState ) ;
	oldStateImpl.addGuardedAction( input, ta ) ;

	return this ;
    }

    public StateEngine setDefault( State oldState, Action action, State newState )
	throws IllegalArgumentException, IllegalStateException
    {
	mustBeInitializing() ;

	StateImpl oldStateImpl = (StateImpl)oldState ;
	oldStateImpl.setDefaultAction( action ) ;
	oldStateImpl.setDefaultNextState( newState ) ;

	return this ;
    }

    public StateEngine setDefault( State oldState, State newState )
	throws IllegalArgumentException, IllegalStateException
    {
	return setDefault( oldState, emptyAction, newState ) ;
    }

    public StateEngine setDefault( State oldState )
	throws IllegalArgumentException, IllegalStateException
    {
	return setDefault( oldState, oldState ) ;
    }

    public void done() throws IllegalStateException
    {
	mustBeInitializing() ;

	// optimize FSM here if desired.  For example,
	// we could choose different strategies for implementing
	// the state transition function based on the distribution 
	// of values for states and input labels.

	initializing = false ;
    }

    public void setDefaultAction( Action act ) throws IllegalStateException
    {
	mustBeInitializing() ;
	defaultAction = act ;
    }

    public void doIt( FSM fsm, Input in, boolean debug )
    {
	// This method is present only for debugging.
	// innerDoIt does the actual transition.

	if (debug)
	    dprint( "doIt enter: currentState = " + 
		fsm.getState() + " in = " + in ) ;

	try {
	    innerDoIt( fsm, in, debug ) ;
	} finally {
	    if (debug)
		dprint( "doIt exit" ) ;
	}
    }

    private StateImpl getDefaultNextState( StateImpl currentState )
    {
	// Use the currentState defaults if 
	// set, otherwise use the state engine default.
	StateImpl nextState = (StateImpl)currentState.getDefaultNextState() ;
	if (nextState == null)
	    // The state engine default never changes the state
	    nextState = currentState ;

	return nextState ;
    }

    private Action getDefaultAction( StateImpl currentState ) 
    {
	Action action = currentState.getDefaultAction() ;
	if (action == null)
	    action = defaultAction ;

	return action ;
    }

    private void innerDoIt( FSM fsm, Input in, boolean debug )
    {
	if (debug) {
	    dprint( "Calling innerDoIt with input " + in ) ;
	}

	// Locals needed for performing the state transition, once we determine
	// the required transition.
	StateImpl currentState = null ;
	StateImpl nextState = null ;
	Action action = null ;

	// Do until no guard has deferred. 
	boolean deferral = false ;
	do {
	    deferral = false ; // clear this after each deferral!
	    currentState = (StateImpl)fsm.getState() ;
	    nextState = getDefaultNextState( currentState ) ;
	    action = getDefaultAction( currentState ) ;

	    if (debug) {
		dprint( "currentState      = " + currentState ) ;
		dprint( "in                = " + in ) ;
		dprint( "default nextState = " + nextState    ) ;
		dprint( "default action    = " + action ) ;
	    }

	    Set gas = currentState.getGuardedActions(in) ;
	    if (gas != null) {
		Iterator iter = gas.iterator() ;

		// Search for a guard that is not DISABLED.  
		// All DISABLED means use defaults.
		while (iter.hasNext()) {
		    GuardedAction ga = (GuardedAction)iter.next() ;
		    Guard.Result gr = ga.getGuard().evaluate( fsm, in ) ;
		    if (debug)
			dprint( "doIt: evaluated " + ga + " with result " + gr ) ;

		    if (gr == Guard.Result.ENABLED) {
			// ga has the next state and action.
			nextState = (StateImpl)ga.getNextState() ;
			action = ga.getAction() ;
			if (debug) {
			    dprint( "nextState = " + nextState ) ;
			    dprint( "action    = " + action ) ;
			}
			break ;
		    } else if (gr == Guard.Result.DEFERED) {
			deferral = true ;
			break ;
		    }
		}
	    }
	} while (deferral) ;

	performStateTransition( fsm, in, nextState, action, debug ) ;
    }

    private void performStateTransition( FSM fsm, Input in, 
	StateImpl nextState, Action action, boolean debug )
    {
	StateImpl currentState = (StateImpl)fsm.getState() ;

	// Perform the state transition.  Pre and post actions are only
	// performed if the state changes (see UML hidden transitions).

	boolean different = !currentState.equals( nextState ) ;

	if (different) {
	    if (debug)
		dprint( "doIt: executing postAction for state " + currentState ) ;
	    try {
		currentState.postAction( fsm ) ;
	    } catch (Throwable thr) {
		if (debug)
		    dprint( "doIt: postAction threw " + thr ) ;

		if (thr instanceof ThreadDeath)
		    throw (ThreadDeath)thr ;
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
		    dprint( "doIt: executing preAction for state " + nextState ) ;

		try {
		    nextState.preAction( fsm ) ;
		} catch (Throwable thr) {
		    if (debug)
			dprint( "doIt: preAction threw " + thr ) ;

		    if (thr instanceof ThreadDeath)
			throw (ThreadDeath)thr ;
		}

		((FSMImpl)fsm).internalSetState( nextState ) ;
	    }

	    if (debug)
		dprint( "doIt: state is now " + nextState ) ;
	}
    }

    public FSM makeFSM( State startState ) throws IllegalStateException
    {
	mustNotBeInitializing() ;

	return new FSMImpl( this, startState ) ;
    }

    private void mustBeInitializing() throws IllegalStateException
    {
	if (!initializing)
	    throw new IllegalStateException( 
		"Invalid method call after initialization completed" ) ;
    }

    private void mustNotBeInitializing() throws IllegalStateException
    {
	if (initializing)
	    throw new IllegalStateException( 
		"Invalid method call before initialization completed" ) ;
    }
}

// end of StateEngineImpl.java

