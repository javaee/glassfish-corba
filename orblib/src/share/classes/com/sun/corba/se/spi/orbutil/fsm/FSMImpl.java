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

/**
 * This is the main class that represents an instance of a state machine
 * using a state engine.  It may be used as a base class, in which case
 * the guards and actions have access to the derived class.
 * Note that this is optional; an FSM implementation may directly
 * implement the FSM interface if desired.
 *
 * @author Ken Cavanaugh
 */
public class FSMImpl implements FSM
{
    private boolean debug ;
    private FSM parent ;
    private State state ;
    private StateEngine stateEngine ;

    /** Create an instance of an FSM using the StateEngine
    * in a particular start state.
    */
    public FSMImpl( StateEngine se, State initialState )
    {
	this( se, initialState, false ) ;
    }

    public FSMImpl( StateEngine se, State initialState, boolean debug )
    {
	parent = null ;
	state = initialState ;
	stateEngine = se ;
	if (!(se.getStates( State.Kind.INITIAL ).contains( initialState )))
	    throw new IllegalStateException(
		"Error: State " + initialState + " is not an initial state" ) ;
	this.debug = debug ;
    }

    public FSM getParent() {
	return parent ;
    }

    public void setParent( FSM fsm ) {
	parent = fsm ;
    }
    
    public StateEngine getStateEngine() {
	return stateEngine ;
    }

    /** Return the current state.
    */
    public State getState() {
	return state ;
    }

    public void setState( State nextState ) 
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

