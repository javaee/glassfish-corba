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

import java.util.Set ;
import java.util.HashSet ;

import com.sun.corba.se.spi.orbutil.fsm.Input ;
import com.sun.corba.se.spi.orbutil.fsm.StateEngine ;
import com.sun.corba.se.spi.orbutil.fsm.FSM ;

import com.sun.corba.se.impl.orbutil.fsm.StateEngineImpl ;

/**
 * This is the main class that represents an instance of a state machine
 * using a state engine.  It may be used as a base class, in which case
 * the guards and actions have access to the derived class.
 *
 * @author Ken Cavanaugh
 */
public class FSMImpl implements FSM
{
    private boolean debug ;
    private State state ;
    private StateEngineImpl stateEngine ;

    /** Create an instance of an FSM using the StateEngine
    * in a particular start state.
    */
    public FSMImpl( StateEngine se, State startState )
    {
	this( se, startState, false ) ;
    }

    public FSMImpl( StateEngine se, State startState, boolean debug )
    {
	state = startState ;
	stateEngine = (StateEngineImpl)se ;
	this.debug = debug ;
    }

    /** Return the current state.
    */
    public State getState()
    {
	return state ;
    }

    /** Perform the transition for the given input in the current state.  This proceeds as follows:
    * <p>Let S be the current state of the FSM.  
    * If there are guarded actions for S with input in, evaluate their guards successively until
    * all have been evaluted, or one returns a non-DISABLED Result. 
    * <ol>
    * <li>If a DEFERED result is returned, retry the input
    * <li>If a ENABLED result is returned, the action for the guarded action 
    * is the current action
    * <li>Otherwise there is no enabled action.  If S has a default action and next state, use them; otherwise
    * use the state engine default action (the next state is always the current state).
    * </ol>
    * After the action is available, the transition proceeds as follows:
    * <ol>
    * <li>If the next state is not the current state, execute the current state postAction method.
    * <li>Execute the action.
    * <li>If the next state is not the current state, execute the next state preAction method.
    * <li>Set the current state to the next state.
    * </ol>
    */
    public void doIt( Input in )
    {
	stateEngine.doIt( this, in, debug ) ;
    }

    // Methods for use only by StateEngineImpl

    public void internalSetState( State nextState ) 
    {
	if (debug) {
	    System.out.println( "Calling internalSetState with nextState = " +
		nextState ) ;
	}

	state = nextState ;

	if (debug) {
	    System.out.println( "Exiting internalSetState with state = " +
		state ) ;
	}
    }
}

// end of FSMImpl.java

