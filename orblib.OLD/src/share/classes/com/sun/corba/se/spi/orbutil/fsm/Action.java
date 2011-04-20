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

package com.sun.corba.se.spi.orbutil.fsm;

/** An Action may be associated with a transition between to 
 * states.  The transition action doIt method is called
 * before the transition takes place.
 *
 * @author Ken Cavanaugh
 */
public interface Action {
    /** Called by the state engine to perform an action
    * before a state transition takes place.  The FSM is 
    * passed so that the Action may examine the state of
    * the FSM.   Note that an
    * action should complete in a timely manner.  If the state machine
    * is used for concurrency control with multiple threads, the
    * action must not allow multiple threads to run simultaneously
    * in the state machine, as the state could be corrupted.
    * Any exception thrown by the Action for the transition
    * will be propagated to doIt.  
    * @param fsm is the state machine causing this action.
    * @param in is the input that caused the transition.
    */
    public void doIt( FSM fsm, Input in ) ;

    public abstract class Base extends NameBase implements Action {
	public static Action compose( final Action arg1, final Action arg2 ) {
	    return new Base( 
		"compose(" + arg1.toString() + "," + arg2.toString() + ")" ) {

		public void doIt( final FSM fsm, final Input in ) {
		    arg1.doIt( fsm, in ) ;
		    arg2.doIt( fsm, in ) ;
		}
	    } ;
	}

	public Base( String name ) { super( name ) ; } 
    }
}

// end of Action.java
