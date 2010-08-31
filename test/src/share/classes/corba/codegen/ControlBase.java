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

package corba.codegen ;

import java.util.List ;
import java.util.ArrayList ;
import java.util.Iterator ;

import sun.corba.Bridge ; // for throwing undeclared checked exceptions 

import com.sun.corba.se.spi.orbutil.generic.Pair ;
import com.sun.corba.se.spi.orbutil.generic.NullaryFunction ;

/**
 * This class provides some base flow control detection primitives
 * to test generated code.
 *
 * The basic problem here is: How do we test generated code?
 * In particular, how do we test that correct flow of control
 * takes?
 *
 * Consider testing an if statement:
 *
 * if (trace(1)) {
 *	trace(2)
 * } else {
 *	trace(3)
 * }
 * trace(4)
 *
 * Here we want to know the following:
 * 1. If trace(1) returns true, trace(2) is executed, followed by trace(4)
 * 2. If trace(2) returns false, trace(3) is executed, followed by trace(4)
 *
 * How do we test this?
 * We test the following implications:
 * trace(1) -> trace(2) trace(4)
 * ~trace(1) -> trace(3) trace(4)
 * and we basically create a simple state machine. 
 *
 * A more complex example:
 *
 * Assume exception A B C with C extends RuntimeException, A extends C, 
 * and B extends C
 * <pre>
    try {
	trace(1)
	try {
	    trace(2)
	} catch(A) {
	    trace(3)
	} catch(C) {
	    trace(4)
	} finally {
	    trace(5)
	}
	trace(6)
    } catch (C) {
	trace(7)
    } finally {
	trace(8)
    }
    trace(9)
    </pre>

 * Then any call to trace could be made to throw an exception, and we need to 
 * observer the correct sequence in response.  
 * No throws:				1 2 5 6 8 9
 * trace(1) throws A:			1 7 8 9
 * trace(2) throws A:			1 2 3 5 6 8 9
 * trace(2) throws A, trace(3) throws B:  1 2 3 5 7 8 9
 * trace(2) throws B:			1 2 4 5 8 9
 * trace(5) throws A:			1 2 5 7 8 9
 * trace(6) throws A:			1 2 5 6 7 8 9
 * trace(8) throws A:			1 2 5 6 8
 *
 * The normal operation of trace is just to record the fact that it was called and
 * return true (which may be ignored).
 * However, we can also set this up so that trace() throws a particular unchecked
 * exception when it is given a particular input.
 *
 * So, how do we capture these possible variations?  A simple FSM seems like a
 * fairly reasonable solution.  This FSM is so simple that we do not need the
 * ORB FSM library: we simply need sequences of inputs, and each input in the
 * sequence corresponds to a state.
 * So, we set up the input alphabet to be non-negative integers (really some
 * small initial sequence starting with 0).  Each call to trace(int) corresponds
 * to an input to the FSM.  The result of the trace call is the action in the
 * FSM.  This action can be:
 * <ol>
 * <li> Throw a particular exception as part of the test.  For this purpose,
 * we will define TestException extends RuntimeException, and only allow
 * subclasses of TestException for such an action.
 * <li> Return true
 * <li> Return false
 * <li> Record an error and throw an IllegalStateException, 
 * which will terminate the test
 * </ol>
 *
 * The idea here is that the FSM will terminate in an accepting state
 * if the test case executes correctly, otherwise the test case will
 * terminate in an error state.  In all cases the framework will
 * record the sequence of state transitions that were observed for
 * diagnostic purposes.
 *
 * A typical use of this class is to derive a class from ControlBase, and
 * then set up the test cases as follows:
 * <ol>
 * <li> ControlBase cb = new (generated test class)()
 * <li> Method testMethod = cb.getDeclaredMethod( "name" ) 
 * <li> cb.defineTest( 1, 2, 4, moa( 5, false ), moa( 8, AnException.class ) ) ;
 * <li> testMethod.invoke( cb ) ;
 * <li> If this throws IllegalStateException, use cb.events() as diagnostic
 * information, otherwise the test passes.
 * </ol>
 *
 * Obviously, the test cases written using this facility could easily be
 * incorrect.  The best way to validate the test case is to first generate
 * source code and compile the generated source code, which should validate
 * the test itself.  Then we can generate bytecode directly and test
 * the code generation.
 */
public class ControlBase {
    private static final Bridge bridge = Bridge.get() ;

    private static class ReturnBooleanAction implements NullaryFunction<Object> {
	public static final ReturnBooleanAction trueAction = 
	    new ReturnBooleanAction( true ) ;
	public static final ReturnBooleanAction falseAction = 
	    new ReturnBooleanAction( false ) ;

	private final boolean result ;

	private ReturnBooleanAction( boolean result ) {
	    this.result = result ;
	}

	private ReturnBooleanAction() {
	    this( false ) ;
	}

	public Boolean evaluate() {
	    return result ;
	}
    }

    private static class ReturnIntegerAction implements NullaryFunction<Object> {
	private final int result ;

	public ReturnIntegerAction( int result ) {
	    this.result = result ;
	}

	public Integer evaluate() {
	    return result ;
	}
    }
   
    private static class ThrowAction implements NullaryFunction<Object> {
	private final Class<? extends Throwable> cls ;

	public ThrowAction( Class<? extends Throwable> cls ) {
	    this.cls = cls ;
	}

	public Boolean evaluate() {
	    Throwable thr ;
	    try {
		thr = cls.newInstance() ;
	    } catch (Exception ex) {
		throw new RuntimeException( "Could not instantiate exception " 
		    + cls.getName() ) ;
	    }
	    bridge.throwException( thr ) ;
	    return false ; // not reachable
	 }
    }

    private static class ErrorAction implements NullaryFunction<Object> {
	public Boolean evaluate() {
	    throw new IllegalStateException( "Error: execution after end of test" ) ;
	}
    }

    private static final int DEFAULT_INPUT = 0 ;
    private static final Pair<Integer,NullaryFunction<Object>> ERROR_ACTION = 
	new Pair( DEFAULT_INPUT, new ErrorAction()) ; 

    private List<Integer> events = new ArrayList<Integer>() ;
    private List<Pair<Integer,NullaryFunction<Object>>> actionSequence = 
	new ArrayList<Pair<Integer,NullaryFunction<Object>>>() ;

    private Iterator<Pair<Integer,NullaryFunction<Object>>> actionIterator = null ;
    private Pair<Integer,NullaryFunction<Object>> nextAction = null ;

    /** Return a list of the arguments to trace calls.
     */
    public List<Integer> events() {
	return new ArrayList( events ) ;
    }

    /** Clear the recorded events and the action map to prepare for
     * another test case.
     */
    public void clear() {
	events.clear() ;
	actionSequence.clear() ;
    }

    /** Utility to Make an Object Array.  Very convenient for
     * defining nested lists.
     */
    public static Object[] moa( Object... arg ) {
	return arg ;
    }

    /** Define a test.  The data is defined as Object[],
     * where each object is either:
     * Integer, or
     * Object[] of length 2, where the first element
     * is an Integer, and the second element is an action.
     * An action is specified as either Boolean (for returning
     * the value of the boolean, or a Class<? extends RuntimeException>,
     * which gives the class of the exception to throw.
     * The Integer case implies a default action of Boolean.TRUE.
     * <p>
     * For example, the test
     *
     * defineTest( 1, 2, moa( 4, AException.class ), moa( 6, false ), 7, 8 )
     *
     * represents a test that expectes trace to be called with the
     * arguments 1,2,4,5,7,8 in sequence, and performs the following
     * actions:
     * 1: return true
     * 2: return true
     * 4: throw new AException() 
     * 6: return false
     * 7: return true 
     * 8: return true
     *
     * Any other sequence of inputs results in trace throwing an
     * IllegalStateException.
     *
     * Note that there is one problem with this interface: 
     * A single argument of type Object[] is interpreted as
     * data instead of data[0], which causes problems here.
     * To avoid this, simple terminate the list with a null,
     * which indicates end of list and avoids the ambiguity.
     * All nulls are simply ignored.
     */
    public void defineTest( Object... data ) {
	clear() ;

	for (Object obj : data ) {
	    if (obj == null) {
		// Just ignore this
	    } else if (obj instanceof Integer) {
		actionSequence.add( new Pair( obj, ReturnBooleanAction.trueAction ) ) ;
	    } else if (obj instanceof Object[]) {
		Object[] arr = Object[].class.cast( obj ) ;
		if (arr.length != 2)
		    throw new IllegalArgumentException( 
			"Object[] argument must have length 2 in defineTest" ) ;

		if (!(arr[0] instanceof Integer))
		    throw new IllegalArgumentException(
			"first element must be an Integer" ) ;

		int input = Integer.class.cast( arr[0] ) ;

		if (arr[1] instanceof Boolean) {
		    boolean flag = Boolean.class.cast( arr[1] ) ;
		    actionSequence.add( new Pair( input, 
			flag ? ReturnBooleanAction.trueAction : 
			       ReturnBooleanAction.falseAction ) ) ;
		} else if (arr[1] instanceof Integer) {
		    int value = Integer.class.cast( arr[1] ) ;
		    actionSequence.add( new Pair( input,
			new ReturnIntegerAction( value )) ) ;
		} else if (arr[1] instanceof Class) {
		    Class<? extends Throwable> cls = Class.class.cast( arr[1] ) ;
		    if (!Throwable.class.isAssignableFrom( cls ))
			throw new IllegalArgumentException( "Class " + cls
			    + " must be a subclass of Throwable" ) ;
		    actionSequence.add( new Pair( input,
			new ThrowAction( cls ) ) ) ;
		} else {
		    throw new IllegalArgumentException( 
			"second element must be a boolean or Class<? extends TestException>" ) ;
		}
	    } else {
		throw new IllegalArgumentException( 
		    "data must contain only ints and Object[] of length 2" ) ;
	    }
	}

	actionIterator = actionSequence.iterator() ;
	setAction() ;
    }

    private void setAction() {
	if (actionIterator.hasNext())
	    nextAction = actionIterator.next() ;
	else
	    nextAction = ERROR_ACTION ;
    }

    /** Method to be invoked from generated code to test for
     * correct execution.  It may return true, false, or throw
     * a TestException (or a subclass of TestException).
     * arg must be positive, as 0 is reserved for internal use.
     */
    protected boolean trace( int arg ) {
	if (arg <= 0)
	    throw new IllegalArgumentException( "trace called with arg " + arg ) ;

	events.add( arg ) ;

	if (nextAction.first() == 0) {
	    nextAction.second().evaluate() ;
	    return false ; // not reachable, as the only action with 0 always throws an exception
	} else if (nextAction.first() == arg) {
	    try {
		Object result = nextAction.second().evaluate() ;
		if (!(result instanceof Boolean))
		    throw new IllegalStateException( "Action must evaluate to Boolean" ) ;
		return Boolean.class.cast( result ) ;
	    } finally {
		setAction() ;
	    }
	} else {
	    throw new IllegalStateException( "Bad argument to trace: expected " 
		+ nextAction.first() + " but got " + arg ) ;
	}
    }

    /** Method to be invoked from generated code to test for
     * correct execution.  It may return an int or throw
     * an Exception.
     * arg must be positive, as 0 is reserved for internal use.
     */
    protected int traceInt( int arg ) {
	if (arg <= 0)
	    throw new IllegalArgumentException( "trace called with arg " + arg ) ;

	events.add( arg ) ;

	if (nextAction.first() == 0) {
	    nextAction.second().evaluate() ;
	    return 0 ; // not reachable, as the only action with 0 always throws an exception
	} else if (nextAction.first() == arg) {
	    try {
		Object result = nextAction.second().evaluate() ;
		if (!(result instanceof Integer))
		    throw new IllegalStateException( "Action must evaluate to Integer" ) ;
		return Integer.class.cast( result ) ;
	    } finally {
		setAction() ;
	    }
	} else {
	    throw new IllegalStateException( "Bad argument to trace: expected " 
		+ nextAction.first() + " but got " + arg ) ;
	}
    }

    // Methods called from generated code to validate values.  They throw
    // IllegalArgumentException, which should never by caught by any generated code.
    
    protected void expect( Object obj, Class cls ) {
	if (!(cls.isInstance( obj )))
	    throw new IllegalArgumentException( "Object " + obj 
		+ " is not of expected class " + cls.getName() ) ;
    }

    protected void expect( int value, int expectedValue ) {
	if (value != expectedValue)
	    throw new IllegalArgumentException( "Value " + value 
		+ " does not equal expected value" + expectedValue ) ;
    }
}
