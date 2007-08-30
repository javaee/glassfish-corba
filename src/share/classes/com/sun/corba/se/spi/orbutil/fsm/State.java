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
 * This interface must be implemented by any class that is used as 
 * a state in a FSM.  The FSM only needs the identity of this 
 * object, so all that is really needs is the default equals implementation.
 * The toString() method should also be overridden to give a concise
 * description or name of the state.  The StateImpl class handles this.
 * <P>
 * Pre- and post- actions are taken only on completed transitions between
 * different states.  Assume that the FSM is in state A, and the FSM will
 * transition to state B under input I with action X.  If A != B and X completes
 * successfully, then after X completes execution, A.postAction is executed,
 * followed by B.preAction.
 *
 * @author Ken Cavanaugh
 */
public interface State
{
    /** Method that defines action that occurs whenever this state is entered.
    * Any exceptions thrown by this method are ignored.
    */
    void preAction( FSM fsm ) ;

    /** Method that defines action that occurs whenever this state is exited.
    * Any exceptions thrown by this method are ignored.
    */
    void postAction( FSM fsm ) ;
}

// end of State.java
