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

package corba.framework ;

import java.lang.reflect.Method ;
import java.lang.reflect.Constructor ;
import java.lang.reflect.Modifier ;
import java.lang.reflect.InvocationTargetException ;

import java.util.List ;
import java.util.Vector ;
import java.util.Enumeration ;
import java.util.ArrayList ;
import java.util.Iterator ;

import java.io.PrintStream ;
import java.io.PrintWriter ;
import java.io.StringWriter ;

import junit.framework.TestCase ;
import junit.framework.Test;
import junit.framework.TestSuite ;
import junit.framework.TestResult ;

public class TestCaseTools 
{
    public static void reportTiming( int repCount, PrintStream out, 
	List<TimedTest> timedTests )
    {
	for (TimedTest test : timedTests) {
	    // Convert to microseconds
	    long duration = test.getDuration()/1000 ;
	    double perTest = duration/repCount ;

	    out.println( test ) ;
	    out.println( "\texecuted " + 
		repCount + " times in " + duration + 
		    " microseconds (per test time is " +
		    perTest + " microseconds)" ) ;
	}
    }

    private static String getSuffix( char ch, String str )
    {
	// Return the substring after the last ch, or null
	// if ch does not occur in str.
	int index = str.lastIndexOf( ch ) ;
	if (index == -1)
	    return null ;
	else
	    return str.substring( index + 1 ) ;
    }

    private static String makeTestSuiteName( Class cls )
    {
	String str = cls.getName() ;
	String result = getSuffix( '$', str ) ;
	if (result == null)
	    result = getSuffix( '.', str ) ;
	if (result == null)
	    result = str ;
	return result ;
    }

    /** This is a special version of TestSuite that fixes, once and for
     * all, the irritating design of JUnit to invoke the constructor
     * multiple times on a test class.  This operates as follows:
     * <code>
     * construct an instance of the test class in inst
     * try {
     *	    for each test method
     *		method.invoke( inst ) ;
     * } catch ... {
     * } finally {
     *	    invoke post method (if any)
     * }
     */
    public static class SingleInstanceTestSuite extends TestSuite {
	private Test test = null ;
	private Method setUpMethod = null ;
	private Method tearDownMethod = null ;
	private Method postMethod = null ;

	/** A simple variant of JUnit's TestCase that invokes the
	 * test method on the supplied test.  This allows creating
	 * multiple TestCases that share the same instance.
	 */
	private class SingleInstanceTestCase extends TestCase {
	    private Method runMethod ;

	    public SingleInstanceTestCase( Method runMethod ) {
		super( runMethod.getName() ) ;
		this.runMethod = runMethod ;

		assertNotNull(getName());

		if (!Modifier.isPublic(runMethod.getModifiers())) {
		    fail("Method \""+getName()+"\" should be public");
		}
	    }

	    protected void setUp() {
		if (setUpMethod != null) 
		    try {
			setUpMethod.invoke( test ) ;
		    } catch (Exception exc) {
			fail( "setUp method threw exception " + exc ) ;
			exc.printStackTrace() ;
		    }
	    }

	    protected void tearDown() {
		if (tearDownMethod != null)
		    try {
			tearDownMethod.invoke( test ) ;
		    } catch (Exception exc) {
			fail( "tearDown method threw exception " + exc ) ;
			exc.printStackTrace() ;
		    }
	    }

	    protected void runTest() throws Throwable {
		try {
		    runMethod.invoke(test);
		} catch (InvocationTargetException e) {
		    System.out.println( "Error in invoking the method " +
			runMethod ) ;
		    e.printStackTrace() ;
		    // e.fillInStackTrace();
		    throw e.getTargetException();
		} catch (IllegalAccessException e) {
		    // e.fillInStackTrace();
		    throw e;
		}
	    }
	}

	public SingleInstanceTestSuite(final Class theClass) {
	    super( theClass.getName() ) ;

	    if (!Modifier.isPublic(theClass.getModifiers())) {
		addTest( warning( "Class " + theClass.getName()
		    + " is not public"));
		return;
	    }

	    // Search for a usable constructor and use it to create an instance
	    // of the test.  The constructor may either take a single
	    // string, or no arguments, and must be public.
	    try {
		try {
		    Constructor constructor = theClass.getConstructor( String.class ) ;
		    test = Test.class.cast( constructor.newInstance( "" ) ) ;
		} catch (NoSuchMethodException e) {
		    try {
			Constructor constructor = theClass.getConstructor( ) ;
			test = Test.class.cast( constructor.newInstance() ) ;
		    } catch (NoSuchMethodException ex) {
			addTest( warning( "Class " + theClass.getName()
			    + " has no public constructor " 
			    + "TestCase(String name) or TestCase()"));
			e.printStackTrace() ;
			return;
		    }
		}
	    } catch (InstantiationException e) {
		addTest( warning( "Cannot instantiate test case: "
		    + getName() + " (" 
		    + exceptionToString(e)+")" ) ) ;
		e.printStackTrace() ;
		return ;
	    } catch (InvocationTargetException e) {
		addTest( warning( "Exception in constructor: "
		    + getName() 
		    + " (" + exceptionToString(e.getTargetException()) + ")" ) );
		e.printStackTrace() ;
		return ;
	    } catch (IllegalAccessException e) {
		addTest( warning( "Cannot access test case: "
		    + getName() 
		    + " (" + exceptionToString(e) + ")" ) );
		e.printStackTrace() ;
		return ;
	    }

	    // Search for special methods
	    setUpMethod = getMethod( theClass, "setUp" ) ;
	    tearDownMethod = getMethod( theClass, "tearDown" ) ;
	    postMethod = getMethod( theClass, "post" ) ;

	    // We have an instance of theClass, so construct SingleInstanceTestCases
	    // for each public test method in the instance.  
	    Class superClass= theClass;
	    Vector names= new Vector();
	    while (Test.class.isAssignableFrom(superClass)) {
		Method[] methods= superClass.getDeclaredMethods();
		for (int i= 0; i < methods.length; i++) {
		    addTestMethod(methods[i], names, test);
		}
		superClass= superClass.getSuperclass();
	    }
	}

	private Method getMethod( Class theClass, String name ) {
	    try {
		return theClass.getMethod( name ) ;
	    } catch (Exception exc) {
		return null ;
	    }
	}

	private void addTestMethod(Method m, Vector names, Test test) {
	    String name= m.getName();
	    if (names.contains(name))
		return;

	    if (!isPublicTestMethod(m)) {
		if (isTestMethod(m))
		    addTest(warning("Test method isn't public: "+m.getName()));
		return;
	    }
	    names.addElement(name);
	    addTest( new SingleInstanceTestCase( m ) );
	}

	private boolean isPublicTestMethod(Method m) {
	    return isTestMethod(m) && Modifier.isPublic(m.getModifiers());
	}
	 
	private boolean isTestMethod(Method m) {
	    String name= m.getName();
	    Class[] parameters= m.getParameterTypes();
	    Class returnType= m.getReturnType();
	    return (parameters.length == 0) && name.startsWith("test") 
		&& returnType.equals(Void.TYPE);
	}
	 
	private String exceptionToString(Throwable t) {
	    StringWriter stringWriter= new StringWriter();
	    PrintWriter writer= new PrintWriter(stringWriter);
	    t.printStackTrace(writer);
	    return stringWriter.toString();
	}
	
	public void run(TestResult result) {
	    try {
		for (Enumeration<Test> e= tests(); e.hasMoreElements(); ) {
		    if (result.shouldStop() )
			break;
		    Test test= e.nextElement();
		    runTest(test, result);
		}
	    } finally {
		if (postMethod != null) {
		    try {
			postMethod.invoke( test ) ;
		    } catch (Exception exc) {
			runTest( warning( "postMethod threw exception " + exc ), result ) ;
		    }
		}
	    }
	}

	public void runTest(Test test, TestResult result) {
	    test.run(result);
	}

	private Test warning(final String message) {
	    return new TestCase("warning") {
		protected void runTest() {
		    fail(message);
		}
	    };
	}
    }

    public enum TestSuiteType { STANDARD, SINGLE } ;

    /** Method to create a collection of nested TestSuites out of
     * nested classes.  Can either create standard JUnit TestSuites,
     * or SingleInstanceTestSuites.
     */
    public static TestSuite makeTestSuite( Class cls ) {
	return makeTestSuite( cls, TestSuiteType.STANDARD ) ;
    }

    public static TestSuite makeTestSuite( Class cls, TestSuiteType type )
    {
	TestSuite result ;
	if (type == TestSuiteType.STANDARD)
	    result = new TestSuite( cls ) ;
	else
	    result = new SingleInstanceTestSuite( cls ) ;

	result.setName( makeTestSuiteName( cls ) ) ;

	// search for public xxxTestSuite classes and turn these into
	// test suites using makeTestSuite.
	Class current = cls ;
	List names= new ArrayList();
	while (Test.class.isAssignableFrom(current)) {
	    Class[] classes = current.getClasses() ;
	    for (int i= 0; i < classes.length; i++) {
		Class next = classes[i] ;
		if (next.getName().endsWith( "TestSuite" ))
		    result.addTest( makeTestSuite( next, type ) ) ;
	    }
	    current= current.getSuperclass();
	}

	return result ;
    }
}
