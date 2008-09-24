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
 *
 * @author Ken Cavanaugh
 */
public class Runner {
    private boolean debug ;
    private FSM current ;

    /** Create a new Runner with fsm on top of the stack.
     */
    public Runner( FSM fsm ) {
	this( fsm, false ) ;
    }

    public Runner( FSM fsm, boolean debug ) {
	current = fsm ;
	this.debug = debug ;
    }

    /** Return the top fsm on the stack.
     */
    public FSM peek() {
	return current ;
    }

    /** Push a new fsm onto the stack.
     */
    public void push( FSM fsm ) {
	fsm.setParent( current ) ;
	current = fsm ;
    }

    public FSM pop() {
	FSM result = current ;
	current = current.getParent() ;
	return result ;
    }

    /** Return true if the stack is empty, which means that the runner is
     * finished.
     */
    public boolean done() {
	return current == null ;
    }

    /** Perform the transition for the given input in the current state.  
     * This proceeds as follows:
    * <p>Let S be the current state of the FSM.  
    * If there are guarded actions for S with input in, evaluate their guards 
    * successively until all have been evaluted, or one returns a 
    * non-DISABLED Result. 
    * <ol>
    * <li>If a DEFERED result is returned, retry the input
    * <li>If a ENABLED result is returned, the action for the guarded action 
    * is the current action
    * <li>Otherwise there is no enabled action.  If S has a default action 
    * and next state, use them; otherwise use the state engine default action 
    * (the next state is always the current state).
    * </ol>
    * After the action is available, the transition proceeds as follows:
    * <ol>
    * <li>If the next state is not the current state, execute the current state 
    * postAction method.
    * <li>Execute the action.
    * <li>If the next state is not the current state, execute the next state 
    * preAction method.
    * <li>Set the current state to the next state.
    * </ol>
    */
    public void doIt( Input in ) {
	current.getStateEngine().doIt( this, in, debug ) ;
    }
}
