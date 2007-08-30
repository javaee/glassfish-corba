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

import com.sun.corba.se.spi.orbutil.fsm.Input ;
import com.sun.corba.se.spi.orbutil.fsm.Action ;
import com.sun.corba.se.spi.orbutil.fsm.Guard ;
import com.sun.corba.se.spi.orbutil.fsm.StateEngine ;
import com.sun.corba.se.spi.orbutil.fsm.StateImpl ;
import com.sun.corba.se.spi.orbutil.fsm.StateEngineFactory ;
import com.sun.corba.se.spi.orbutil.fsm.FSM ;

class TestInput {
    TestInput( Input value, String msg )
    {
	this.value = value ;
	this.msg = msg ;
    }

    public String toString()
    {
	return "Input " + value + " : " + msg ;
    }

    public Input getInput()
    {
	return value ;
    }

    Input value ;
    String msg ;
}

class TestAction1 implements Action
{
    public void doIt( FSM fsm, Input in )
    {
	System.out.println( "TestAction1:" ) ;
	System.out.println( "\tlabel    = " + label ) ;
	System.out.println( "\toldState = " + oldState ) ;
	System.out.println( "\tnewState = " + newState ) ;
	if (label != in)
	    throw new Error( "Unexcepted Input " + in ) ;
	if (oldState != fsm.getState())
	    throw new Error( "Unexpected old State " + fsm.getState() ) ;
    }

    public TestAction1( State oldState, Input label, State newState )
    {
	this.oldState = oldState ;
	this.newState = newState ;
	this.label = label ;
    }

    private State oldState ;
    private Input label ;
    private State newState ;
}

class TestAction2 implements Action
{
    private State oldState ;
    private State newState ;

    public void doIt( FSM fsm, Input in )
    {
	System.out.println( "TestAction2:" ) ;
	System.out.println( "\toldState = " + oldState ) ;
	System.out.println( "\tnewState = " + newState ) ;
	System.out.println( "\tinput    = " + in ) ;
	if (oldState != fsm.getState())
	    throw new Error( "Unexpected old State " + fsm.getState() ) ;
    }

    public TestAction2( State oldState, State newState )
    {
	this.oldState = oldState ;
	this.newState = newState ;
    }
}

class TestAction3 implements Action {
    private State oldState ;
    private Input label ;

    public void doIt( FSM fsm, Input in )
    {
	System.out.println( "TestAction1:" ) ;
	System.out.println( "\tlabel    = " + label ) ;
	System.out.println( "\toldState = " + oldState ) ;
	if (label != in)
	    throw new Error( "Unexcepted Input " + in ) ;
    }

    public TestAction3( State oldState, Input label )
    {
	this.oldState = oldState ;
	this.label = label ;
    }
}

class NegateGuard implements Guard {
    Guard guard ;

    public NegateGuard( Guard guard ) 
    {
	this.guard = guard ;
    }

    public Guard.Result evaluate( FSM fsm, Input in )
    {
	return guard.evaluate( fsm, in ).complement() ;
    }
}

class MyFSM extends FSMImpl {
    public MyFSM( StateEngine se )
    {
	super( se, FSMTest.STATE1 ) ;
    }

    public int counter = 0 ;
}

public class FSMTest {
    public static final State 	STATE1 = new StateImpl( "1" ) ;
    public static final State	STATE2 = new StateImpl( "2" ) ;
    public static final State 	STATE3 = new StateImpl( "3" ) ;
    public static final State	STATE4 = new StateImpl( "4" ) ;

    public static final Input 	INPUT1 = new InputImpl( "1" ) ;
    public static final Input	INPUT2 = new InputImpl( "2" ) ;
    public static final Input 	INPUT3 = new InputImpl( "3" ) ;
    public static final Input	INPUT4 = new InputImpl( "4" ) ;

    private Guard counterGuard = new Guard() {
	public Guard.Result evaluate( FSM fsm, Input in )
	{
	    MyFSM mfsm = (MyFSM) fsm ;
	    return Guard.Result.convert( mfsm.counter < 3 ) ;
	}
    } ;

    private static void add1( StateEngine se, State oldState, Input in, State newState )
    {
	se.add( oldState, in, new TestAction1( oldState, in, newState ), newState ) ;
    }

    private static void add2( StateEngine se, State oldState, State newState )
    {
	se.setDefault( oldState, new TestAction2( oldState, newState ), newState ) ;
    }

    public static void main( String[] args )
    {
	TestAction3 ta3 = new TestAction3( STATE3, INPUT1 ) ;

	StateEngine se = StateEngineFactory.create() ;
	add1( se, STATE1, INPUT1, STATE1 ) ;
	add2( se, STATE1,         STATE2 ) ;

	add1( se, STATE2, INPUT1, STATE2 ) ;
	add1( se, STATE2, INPUT2, STATE2 ) ;
	add1( se, STATE2, INPUT3, STATE1 ) ;
	add1( se, STATE2, INPUT4, STATE3 ) ;

	se.add(   STATE3, INPUT1, ta3,	STATE3 ) ;
	se.add(   STATE3, INPUT1, ta3,	STATE4 ) ;
	add1( se, STATE3, INPUT2, STATE1 ) ;
	add1( se, STATE3, INPUT3, STATE2 ) ;
	add1( se, STATE3, INPUT4, STATE2 ) ;

	MyFSM fsm = new MyFSM( se ) ;
	TestInput in11 = new TestInput( INPUT1, "1.1" ) ;
	TestInput in12 = new TestInput( INPUT1, "1.2" ) ;
	TestInput in21 = new TestInput( INPUT2, "2.1" ) ;
	TestInput in31 = new TestInput( INPUT3, "3.1" ) ;
	TestInput in32 = new TestInput( INPUT3, "3.2" ) ;
	TestInput in33 = new TestInput( INPUT3, "3.3" ) ;
	TestInput in41 = new TestInput( INPUT4, "4.1" ) ;

	fsm.doIt( in11.getInput() ) ;
	fsm.doIt( in12.getInput() ) ;
	fsm.doIt( in41.getInput() ) ;
	fsm.doIt( in11.getInput() ) ;
	fsm.doIt( in21.getInput() ) ;
	fsm.doIt( in31.getInput() ) ;
	fsm.doIt( in33.getInput() ) ;
	fsm.doIt( in41.getInput() ) ;
	fsm.doIt( in41.getInput() ) ;
	fsm.doIt( in41.getInput() ) ;
	fsm.doIt( in21.getInput() ) ;
	fsm.doIt( in32.getInput() ) ;
	fsm.doIt( in41.getInput() ) ;
	fsm.doIt( in11.getInput() ) ;
	fsm.doIt( in12.getInput() ) ;
	fsm.doIt( in11.getInput() ) ;
	fsm.doIt( in11.getInput() ) ;
	fsm.doIt( in11.getInput() ) ;
	fsm.doIt( in11.getInput() ) ;
	fsm.doIt( in11.getInput() ) ;
    }
}
