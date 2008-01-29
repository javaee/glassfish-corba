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

package com.sun.corba.se.spi.orbutil.fsm ;

import java.util.Collections ;
import java.util.Map ;
import java.util.HashMap ;
import java.util.Set ;
import java.util.HashSet ;

/** Base class for all states in a StateEngine.  This must be used
* as the base class for all states in transitions added to a StateEngine.
*/
public class State extends NameBase {
    /** Kind of state.  A StateEngine must have at least one INITIAL state.
     * An FSM may only be created in an INITIAL state.
     * It may have 0 or more FINAL states. A FINAL state may only be the
     * target of a state transition.  If a state engine is used as a 
     * submachine, it must have at least one final state.
     * <P>
     * A REFERENCE state is handled specially.  It is used to call into
     * another state engine much as a normal subroutine call.  A 
     * REFERENCE state (like a FINAL state) may not have any transitions
     * that leave it.  The returnAction method on a REFERENCE state is 
     * responsible for setting the state directly.
     */
    public enum Kind { INITIAL, NORMAL, REFERENCE, FINAL }

    private Kind kind ;
    private Action defaultAction ;
    private State defaultNextState ;

    private Map<Input,Set<Transition>> inputMap ;
    // XXX this is still not correct for an unmodifiable map:
    private Map<Input,Set<Transition>> inputMapImage ;

    public State( String name )  {
	this( name, Kind.NORMAL ) ;
    }

    public State( String name, Kind kind ) 
    { 
	super( name ) ; 
	this.kind = kind ;
	defaultAction = null ;
	inputMap = new HashMap<Input,Set<Transition>>() ;
	inputMapImage = Collections.unmodifiableMap( inputMap ) ;
    } 

    /** Return the Kind of this state.
     */
    public Kind getKind() {
	return kind ;
    }

    /** Method that defines action that occurs whenever this state is entered
     * from a different state.  preAction is not called on a self-transition.
     * If preAction returns a non-null result, the result becomes the current FSM.
     * <P>
     * Any exceptions except ThreadDeath thrown by this method are ignored.
     * This method can be overridden in a state implementation if needed.
     */
    public FSM preAction( FSM fsm ) {
	return null ;
    }

    /** If this state has Kind REFERENCE, and its preAction pushes a 
     * nested FSM onto the stack, the returnAction method is called after the
     * nested FSM reaches a final state.  The nested FSM is passed into
     * nestedFSM, and fsm is the new top of stack, which is the FSM
     * that was active when the preAction was called.  The result is
     * the new state that will be assumed after this REFERENCE's 
     * state postAction method is called.
     * <p>
     * If the returnAction method sets the state to a new state,
     * the postAction method is called as usuTransition.
     * <P>
     * Any exceptions except ThreadDeath thrown by this method are ignored.
     * This method can be overridden in a state implementation if needed.
     */
    public State returnAction( FSM fsm, FSM nestedFSM ) {
	return null ;
    }

    /** Method that defines action that occurs whenever this state is exited,
     * that is, when the state is changed from this state to a new state.
     * <P>
     * Any exceptions except ThreadDeath thrown by this method are ignored.
     * This method can be overridden in a state implementation if needed.
     */
    public void postAction( FSM fsm ) {
    }

    /** Return the default next state for this state.  This is the next
     * state if the input is not found in the action map.
     */
    public State getDefaultNextState() {
	return defaultNextState ;
    }

    /** Get the default transition action that is used if the default next
     * state is used.
     */
    public Action getDefaultAction() {
	return defaultAction ;
    }

    public Map<Input,Set<Transition>> getInputMap() {
	return inputMapImage ;
    }

    // These methods are only called from the StateEngine.
   
    void setDefaultNextState( State defaultNextState ) {
	this.defaultNextState = defaultNextState ;
    }

    void setDefaultAction( Action defaultAction ) {
	this.defaultAction = defaultAction ;
    }

    void addTransition( Input in, Transition ga ) {
	Set<Transition> gas = inputMap.get( in ) ;
	if (gas == null) {
	    gas = new HashSet<Transition>() ;
	    inputMap.put( in, gas ) ;
	}

	gas.add( ga ) ;
    }

    Set<Transition> getTransitions( Input in ) {
	return inputMap.get( in ) ;
    }
}
