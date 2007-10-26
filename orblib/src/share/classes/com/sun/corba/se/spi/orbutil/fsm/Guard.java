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
 *
 * @author Ken Cavanaugh
 */
public interface Guard
{
    public static final class Complement extends GuardBase {
	private Guard guard ;

	public Complement( GuardBase guard ) 
	{
	    super( "not(" + guard.getName() + ")" ) ;
	    this.guard = guard ;
	}

	public Result evaluate( FSM fsm, Input in )
	{
	    return guard.evaluate( fsm, in ).complement() ;
	}
    }

    public static final class Result {
	private String name ;

	private Result( String name ) 
	{
	    this.name = name ;
	}

	public static Result convert( boolean res ) 
	{
	    return res ? ENABLED : DISABLED ;
	}

	public Result complement() 
	{
	    if (this == ENABLED)
		return DISABLED ;
	    else if (this == DISABLED)
		return ENABLED ;
	    else 
		return DEFERED ;
	}
	
	public String toString()
	{
	    return "Guard.Result[" + name + "]" ;
	}

	public static final Result ENABLED = new Result( "ENABLED" ) ;
	public static final Result DISABLED = new Result( "DISABLED" ) ;
	public static final Result DEFERED = new Result( "DEFERED" ) ;
    }

    /** Called by the state engine to determine whether a
    * transition is enabled, defered, or disabled.
    * The result is interpreted as follows:
    * <ul>
    * <li>ENABLED if the transition is ready to proceed
    * <li>DISABLED if the transition is not ready to proceed
    * <li>DEFERED if the action associated with the transition
    * is to be deferred.  This means that the input will not be 
    * acted upon, but rather it will be saved for later execution.
    * Typically this is implemented using a CondVar wait, and the
    * blocked thread represents the defered input.  The defered
    * input is retried when the thread runs again.
    * </ul>
    *
    * @param fsm is the state machine causing this action.
    * @param in is the input that caused the transition.
    */
    public Result evaluate( FSM fsm, Input in ) ;
}

// end of Action.java


