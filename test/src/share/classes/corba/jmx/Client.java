/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2003-2007 Sun Microsystems, Inc. All rights reserved.
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

package corba.jmx ;

import java.lang.annotation.Target ;
import java.lang.annotation.ElementType ;
import java.lang.annotation.Retention ;
import java.lang.annotation.RetentionPolicy ;

import java.lang.reflect.Method ;

import java.util.Iterator ;
import java.util.Properties ;
import java.util.Map ;
import java.util.HashMap ;
import java.util.List ;
import java.util.ArrayList ;
import java.util.Arrays ;
import java.util.List ;
import java.util.Set ;
import java.util.HashSet ;

import java.io.PrintWriter ;

import org.testng.TestNG ;
import org.testng.Assert ;
import org.testng.annotations.Test ;
import org.testng.annotations.Configuration ;
import org.testng.annotations.ExpectedExceptions ;

import com.sun.corba.se.spi.orbutil.generic.UnaryFunction ;
import com.sun.corba.se.spi.orbutil.generic.UnaryBooleanFunction ;
import com.sun.corba.se.spi.orbutil.generic.Algorithms ;
import com.sun.corba.se.spi.orbutil.generic.Pair ;

import com.sun.corba.se.impl.orbutil.newtimer.VersionedHashSet ;
import com.sun.corba.se.impl.orbutil.newtimer.TimingPoints ;

import com.sun.corba.se.impl.orbutil.jmx.AnnotationUtil ;

import static java.util.Arrays.asList ;

public class Client {
    //==============================================================================================
    // Tests for Algorithms class
    //==============================================================================================
    private <A> void compareSequence( List<A> result, List<A> sdata ) {
	Iterator<A> resIter = result.iterator() ;
	Iterator<A> sdataIter = sdata.iterator() ;

	while (resIter.hasNext() && sdataIter.hasNext()) 
	    Assert.assertEquals( resIter.next(), sdataIter.next() ) ;

	Assert.assertEquals( resIter.hasNext(), sdataIter.hasNext() ) ;
    }
    
    // Test Algorithms
    @Test()
    public void testMap1() {
	final List<Integer> data = Arrays.asList( 12, 23, 4, 9, 17, 213 ) ;
	final List<Integer> sdata = new ArrayList<Integer>() ;

	for (Integer i : data) {
	    sdata.add( i*i ) ;
	}

	final UnaryFunction<Integer,Integer> square = new UnaryFunction<Integer,Integer>() {
	    public Integer evaluate( Integer arg ) {
		return arg*arg ;
	    }
	} ;

	List<Integer> result = Algorithms.map( data, square ) ;
    
	compareSequence( result, sdata ) ;
    }

    @Test()
    public void testMap2() {
	final List<Integer> data = Arrays.asList( 12, 23, 4, 9, 17, 213 ) ;
	final List<Integer> sdata = new ArrayList<Integer>() ;

	for (Integer i : data) {
	    sdata.add( i*i ) ;
	}

	final UnaryFunction<Integer,Integer> square = new UnaryFunction<Integer,Integer>() {
	    public Integer evaluate( Integer arg ) {
		return arg*arg ;
	    }
	} ;

	List<Integer> result = new ArrayList<Integer>() ;
	Algorithms.map( data, result, square ) ;

	compareSequence( result, sdata ) ;
    }

    @Test()
    public void testFilter1() {
	final List<Integer> data = Arrays.asList( 12, 23, 4, 9, 17, 213, 16, 1, 25 ) ;
	final List<Integer> sdata = new ArrayList<Integer>() ;

	for (Integer i : data) {
	    if ((i & 2) == 0)
		sdata.add( i ) ;
	}

	final UnaryBooleanFunction<Integer> ifEven = new UnaryBooleanFunction<Integer>() {
	    public boolean evaluate( Integer arg ) {
		return (arg & 2) == 0 ;
	    }
	} ;

	List<Integer> result = Algorithms.filter( data, ifEven ) ;
	compareSequence( result, sdata ) ;
    }

    @Test()
    public void testFilter2() {
	final List<Integer> data = Arrays.asList( 12, 23, 4, 9, 17, 213, 16, 1, 25 ) ;
	final List<Integer> sdata = new ArrayList<Integer>() ;

	for (Integer i : data) {
	    if ((i & 2) == 0)
		sdata.add( i ) ;
	}

	final UnaryBooleanFunction<Integer> ifEven = new UnaryBooleanFunction<Integer>() {
	    public boolean evaluate( Integer arg ) {
		return (arg & 2) == 0 ;
	    }
	} ;

	List<Integer> result = new ArrayList<Integer>() ;
	Algorithms.filter( data, result, ifEven ) ;
	compareSequence( result, sdata ) ;
    }

    @Test()
    public void testFind() {
	final List<Integer> data = Arrays.asList( 12, 23, 4, 9, 17, 42, 213, 16, 1, 25 ) ;

	final UnaryBooleanFunction<Integer> is42 = new UnaryBooleanFunction<Integer>() {
	    public boolean evaluate( Integer arg ) {
		return arg == 42 ;
	    }
	} ;

	Integer result = Algorithms.find( data, is42 ) ;
	Assert.assertTrue( result != null ) ;
	Assert.assertTrue( result == 42 ) ;
    }

    //==============================================================================================
    // Tests for AnnotationUtil class
    //==============================================================================================
   
    public interface A {} 
    public interface B extends A {}
    public interface C extends A {}
    public interface D extends B, C {}
    public interface E {}
    public interface F extends A, E {}
    public interface G extends F, D {}
    public class H implements F {} 
    public class I extends H implements G {}

    private static final Class[] cdata =  {
	Object.class, 
	A.class, B.class, C.class, D.class, E.class,
	F.class, G.class, H.class, I.class } ;

    // Invariants to test on makeInheritanceChain:
    // For any class C:
    // Let R = makeInheritanceChain(C). Then:
    //
    // C precedes C.super, all C.implements in R
    // C is the first element of R.

    @Test() 
    public void testGetInheritanceChain() {
	List<Class<?>> res = AnnotationUtil.getInheritanceChain( I.class ) ;
	System.out.println( "Inheritance chain for class " + I.class.getName() 
	    + " is " + res ) ;

	Map<Class,Integer> positions = new HashMap<Class,Integer>() ;
	int position = 0 ;
	for (Class cls : res) {
	    positions.put( cls, position++ ) ;
	}

	Integer firstIndex = positions.get( I.class ) ;
	Assert.assertNotNull( firstIndex,
	    "Index for top-level class " + I.class.getName() 
	    + " is null" ) ;
	Assert.assertTrue( firstIndex == 0,
	    "Index of top-level class " + I.class.getName() 
	    + " is " + firstIndex + " but should be 0" ) ;

	for (Class cls : cdata) {
	    Integer cindex = positions.get( cls ) ;
	    Assert.assertNotNull( cindex,
		"Index for class " + cls.getName() + " is null" ) ;

	    Class sclass = cls.getSuperclass() ;
	    if (sclass != null) {
		Integer superIndex = positions.get( sclass ) ;
		Assert.assertNotNull( superIndex, 
		    "Index for " + sclass.getName() + " is null" ) ;
		Assert.assertTrue( cindex < superIndex, "Class index = " + cindex 
		    + ", superclass index = " + superIndex ) ;
	    }

	    for (Class icls : cls.getInterfaces()) {
		Integer iindex = positions.get( icls ) ;
		Assert.assertNotNull( iindex, 
		    "Index of interface " + icls.getName() + " should not be null" ) ;
		Assert.assertTrue( cindex < iindex,
		    "Index of class " + cls.getName() + " is " + cindex 
		    + ": should be less than index of interface " 
		    + icls.getName() + " which is " + iindex ) ;
	    }
	}
    }

    @Target(ElementType.METHOD) 
    @Retention(RetentionPolicy.RUNTIME)
    private @interface Test1{} 

    @Target(ElementType.METHOD) 
    @Retention(RetentionPolicy.RUNTIME)
    private @interface Test2{}

    @Target(ElementType.TYPE)
    @Retention(RetentionPolicy.RUNTIME)
    private @interface Test3{}

    @Test3
    private interface AA {
	@Test1
	int getFooA() ;

	@Test1
	void setFooA( int arg ) ;


	@Test2
	int barA() ;

	@Test2
	void barA( int arg ) ;
    }

    private interface BB extends AA {
	@Test1
	boolean isSomething() ;
    }

    @Test3
    private interface CC extends AA {
	@Test2
	int getFooC() ;
    }

    private interface DD extends AA, BB {
	@Test2
	void setFooD( int arg ) ;
    }

    private static Method getMethod( Class<?> cls, String name, Class... args) {
	try {
	    return cls.getDeclaredMethod( name, args ) ;
	} catch (Exception exc) {
	    Assert.fail( "getMethod() caught exception " + exc ) ;
	    return null ;
	}
    }

    @Test()
    public void testFindMethod() {
	final UnaryBooleanFunction<Method> predicate = 
	    new UnaryBooleanFunction<Method>() {
		public boolean evaluate( Method method ) {
		    return (method.getName() == "barA") &&
			(method.getReturnType() == int.class) ;
		}
	    } ;

	final Method expectedResult = getMethod( AA.class, "barA" ) ;

	final Method result = AnnotationUtil.findMethod( DD.class,
	    predicate ) ;

	Assert.assertEquals( expectedResult, result ) ;
    }

    @Test
    public void testGetAnnotatedMethods() {
	List<Method> methods = AnnotationUtil.getAnnotatedMethods( DD.class, Test2.class ) ;
	Set<Method> methodSet = new HashSet<Method>( methods ) ;

	Method[] expectedMethods = { 
	    getMethod( DD.class, "setFooD", int.class ),
	    getMethod( CC.class, "getFooC" ),
	    getMethod( AA.class, "barA" ),
	    getMethod( AA.class, "barA", int.class ) } ;

	List<Method> expectedMethodList = Arrays.asList( expectedMethods ) ;
	Set<Method> expectedMethodSet = new HashSet<Method>( expectedMethodList ) ;

	Assert.assertEquals( expectedMethodSet, methodSet ) ;
    }

    @Test
    public void testGetClassAnnotations() {
	Set<Pair<Class<?>,Test3>> expectedClassAnnotations = 
	    new HashSet<Pair<Class<?>,Test3>>() ;
	expectedClassAnnotations.add( new Pair<Class<?>,Test3>( CC.class, 
	    CC.class.getAnnotation( Test3.class ))) ;
	expectedClassAnnotations.add( new Pair<Class<?>,Test3>( AA.class, 
	    AA.class.getAnnotation( Test3.class ))) ;

	List<Pair<Class<?>,Test3>> classAnnotationList = AnnotationUtil.getClassAnnotations( 
	    DD.class, Test3.class ) ;
	Set<Pair<Class<?>,Test3>> classAnnotations = new HashSet<Pair<Class<?>,Test3>>( 
	    classAnnotationList ) ;

	Assert.assertEquals( expectedClassAnnotations, classAnnotations ) ;
    }

    private static Method getter_fooA = getMethod( AA.class, "getFooA" ) ;
    private static Method setter_fooA = getMethod( AA.class, "setFooA", int.class ) ;
    private static Method getter_barA = getMethod( AA.class, "barA" ) ;
    private static Method setter_barA = getMethod( AA.class, "barA", int.class ) ; 
    private static Method getter_something = getMethod( BB.class, "isSomething" ) ;
    private static Method getter_fooC = getMethod( CC.class, "getFooC" ) ;
    private static Method setter_fooD = getMethod( DD.class, "setFooD", int.class ) ;

    @Test
    public void testIsSetterIsGetter() {
	Method m = null ;

	m = AnnotationUtil.getGetterMethod( DD.class, "fooA" ) ;
	Assert.assertEquals( getter_fooA, m ) ;

	m = AnnotationUtil.getSetterMethod( DD.class, "fooA" ) ;
	Assert.assertEquals( setter_fooA, m ) ;

	m = AnnotationUtil.getGetterMethod( DD.class, "barA" ) ;
	Assert.assertEquals( getter_barA, m ) ;

	m = AnnotationUtil.getSetterMethod( DD.class, "barA" ) ;
	Assert.assertEquals( setter_barA, m ) ;

	m = AnnotationUtil.getGetterMethod( DD.class, "something" ) ;
	Assert.assertEquals( getter_something, m ) ;

	m = AnnotationUtil.getGetterMethod( DD.class, "fooC" ) ;
	Assert.assertEquals( getter_fooC, m ) ;

	m = AnnotationUtil.getSetterMethod( DD.class, "fooD" ) ;
	Assert.assertEquals( setter_fooD, m ) ;
    }

    // @ExpectedExceptions( { IllegalArgumentException.class } )

    public static void main( String[] args ) {
	TestNG tng = new TestNG() ;
	tng.setOutputDirectory( "gen/corba/jmx/test-output" ) ;

	Class[] tngClasses = new Class[] { Client.class } ;

	tng.setTestClasses( tngClasses ) ;

	tng.run() ;

	System.exit( tng.hasFailure() ? 1 : 0 ) ;
    }
}
