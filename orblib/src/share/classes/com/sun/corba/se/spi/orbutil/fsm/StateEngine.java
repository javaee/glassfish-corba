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

package com.sun.corba.se.spi.orbutil.fsm;

/**
 * A StateEngine defines the state transition function for a 
 * finite state machine (FSM). A FSM always has a current state.
 * In response to an Input, the FSM performs an Action and 
 * makes a transition to a new state.  Note that any object can
 * be used as an input if it supports the Input interface.
 * For example, a protocol message may be an input.  The FSM
 * uses only the result of calling getLabel on the Input to 
 * drive the transition.
 * <p>
 * The function can be non-deterministic
 * in that the same input may cause transitions to different new
 * states from the current state.  In this case, the action that
 * is executed for the transition must set the correct new state.
 *
 * @author Ken Cavanaugh
 */
public interface StateEngine
{
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
	    Action action, State newState ) throws IllegalStateException ;

	/** Add a transition with a guard that always evaluates to true.
	*/
	public StateEngine add( State oldState, Input input, 
	    Action action, State newState ) throws IllegalStateException ;

	/** Set the default transition and action for a state.
	* This transition will be used if no more specific transition was
	* defined for the actual input.  Repeated calls to this method
	* simply change the default.
	* This method can only be called before done().  An attempt to
	* call it after done() results in an IllegalStateException.
	*/
	public StateEngine setDefault( State oldState, Action action, State newState )
		throws IllegalStateException ;

	/** Equivalent to setDefault( oldState, act, newState ) where act is an
	 * action that does nothing.
	 */
	public StateEngine setDefault( State oldState, State newState )
		throws IllegalStateException ;

	/** Euaivalent to setDefault( oldState, oldState ) 
	 */
	public StateEngine setDefault( State oldState )
		throws IllegalStateException ;

	/** Set the default action used in this state engine.  This is the
	* action that is called whenever there is no applicable transition.
	* Normally this would simply flag an error.  This method can only
	* be called before done().  An attempt to
	* call it after done() results in an IllegalStateException.
	*/
	public void setDefaultAction( Action act ) throws IllegalStateException ;

	/** Called after all transitions have been added to the state engine.
	* This provides an opportunity for the implementation to optimize
	* its representation before the state engine is used.  This method 
	* may only be called once.  An attempt to call it more than once
	* results in an IllegalStateException.
	*/
	public void done() throws IllegalStateException ;

	/** Create an instance of a FSM that uses this state engine.
	* The initial state of the FSM will be the stateState specified
	* here.  This method can only be called after done().  An attempt
	* to call it before done results in an IllegalStateException.
	*/
	public FSM makeFSM( State startState ) throws IllegalStateException ;
}

// end of StateEngine.java


