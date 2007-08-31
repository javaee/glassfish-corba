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
 * An FSM is used to represent an instance of a finite state machine
 * which has a transition function represented by an instance of
 * StateEngine.  An instance of an FSM may be created either by calling
 * StateEngine.makeFSM( startState ) on a state engine, or by extending FSMImpl and
 * using a constructor.  Using FSMImpl as a base class is convenient if
 * additional state is associated with the FSM beyond that encoded 
 * by the current state.  This is especially convenient if an action
 * needs some additional information.  For example, counters are best
 * handled by special actions rather than encoding a bounded counter
 * in a state machine.  It is also possible to create a class that
 * implements the FSM interface by delegating to an FSM instance
 * created by StateEngine.makeFSM.
 *
 * @author Ken Cavanaugh
 */
public interface FSM
{
    /** Get the current state of this FSM.
    */
    public State getState() ;

    /** Perform the action and transition to the next state based
    * on the current state of the FSM and the input.
    */
    public void doIt( Input in ) ;
}

// end of FSM.java

