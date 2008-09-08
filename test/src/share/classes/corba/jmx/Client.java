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
import java.util.Hashtable ;
import java.util.List ;
import java.util.ArrayList ;
import java.util.Arrays ;
import java.util.List ;
import java.util.Set ;
import java.util.HashSet ;
import java.util.Date ;

import java.io.PrintWriter ;

import java.math.BigInteger ;
import java.math.BigDecimal ;

import javax.management.MalformedObjectNameException ;
import javax.management.ObjectName ;
import javax.management.MBeanServer ;
import javax.management.openmbean.SimpleType ;
import javax.management.openmbean.OpenType ;
import javax.management.openmbean.CompositeData ;
import javax.management.openmbean.CompositeType ;

import org.testng.TestNG ;
import org.testng.Assert ;
import org.testng.annotations.Test ;
import org.testng.annotations.Configuration ;
import org.testng.annotations.ExpectedExceptions ;

import com.sun.corba.se.spi.orbutil.generic.UnaryFunction ;
import com.sun.corba.se.spi.orbutil.generic.UnaryBooleanFunction ;
import com.sun.corba.se.spi.orbutil.generic.Algorithms ;
import com.sun.corba.se.spi.orbutil.generic.Pair ;

import com.sun.corba.se.spi.orbutil.jmx.ManagedData ;
import com.sun.corba.se.spi.orbutil.jmx.ManagedObject ;
import com.sun.corba.se.spi.orbutil.jmx.ManagedAttribute ;
import com.sun.corba.se.spi.orbutil.jmx.ManagedOperation ;
import com.sun.corba.se.spi.orbutil.jmx.ManagedObjectManager ;
import com.sun.corba.se.spi.orbutil.jmx.ManagedObjectManagerFactory ;

import com.sun.corba.se.impl.orbutil.newtimer.VersionedHashSet ;
import com.sun.corba.se.impl.orbutil.newtimer.TimingPoints ;

import com.sun.corba.se.impl.orbutil.jmx.TypeConverter ;
import com.sun.corba.se.impl.orbutil.jmx.AttributeDescriptor ;
import com.sun.corba.se.impl.orbutil.jmx.ClassAnalyzer ;
import com.sun.corba.se.impl.orbutil.jmx.AnnotationUtil ;
import com.sun.corba.se.impl.orbutil.jmx.ManagedObjectManagerInternal ;

import static java.util.Arrays.asList ;

import corba.framework.TestngRunner ;

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
        ClassAnalyzer ca = new ClassAnalyzer( I.class ) ;
        List<Class<?>> res = ca.findClasses( ca.alwaysTrue() ) ;
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

    private interface DD extends CC, BB {
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
        final ClassAnalyzer ca = new ClassAnalyzer( DD.class ) ;
	final ClassAnalyzer.Predicate predicate = 
	    new ClassAnalyzer.Predicate() {
		public boolean evaluate( Object obj ) {
                    Method method = (Method)obj ;

		    return (method.getName() == "barA") &&
			(method.getReturnType() == int.class) ;
		}
	    } ;

	final Method expectedResult = getMethod( AA.class, "barA" ) ;

	final List<Method> result = ca.findMethods( predicate ) ;
        Assert.assertEquals( result.size(), 1 ) ;
        Method resultMethod = result.get(0) ;
	Assert.assertEquals( expectedResult, resultMethod ) ;
    }

    @Test
    public void testGetAnnotatedMethods() {
        ClassAnalyzer ca = new ClassAnalyzer( DD.class ) ;
        List<Method> methods = ca.findMethods( ca.forAnnotation( Test2.class ) ) ;
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
        List<Class<?>> expectedResult = new ArrayList<Class<?>>() ;
        expectedResult.add( CC.class ) ;
        expectedResult.add( AA.class ) ;

        ClassAnalyzer ca = new ClassAnalyzer( DD.class ) ;
        List<Class<?>> classes = ca.findClasses( ca.forAnnotation( Test3.class ) ) ;

	Assert.assertEquals( classes, expectedResult ) ;
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
        ClassAnalyzer ca = new ClassAnalyzer( DD.class ) ;
	ManagedObjectManagerInternal mom = 
            (ManagedObjectManagerInternal)ManagedObjectManagerFactory.create( "ORBTest" ) ;

        AttributeDescriptor ad = null ;

        ad = AttributeDescriptor.findAttribute( mom, ca, "fooA", "null description", 
            AttributeDescriptor.AttributeType.GETTER ) ;
	Assert.assertEquals( getter_fooA, ad.method() ) ;
        
        ad = AttributeDescriptor.findAttribute( mom, ca, "fooA", "null description", 
            AttributeDescriptor.AttributeType.SETTER ) ;
	Assert.assertEquals( setter_fooA, ad.method() ) ;

        ad = AttributeDescriptor.findAttribute( mom, ca, "barA", "null description", 
            AttributeDescriptor.AttributeType.GETTER ) ;
	Assert.assertEquals( getter_barA, ad.method() ) ;

        ad = AttributeDescriptor.findAttribute( mom, ca, "barA", "null description", 
            AttributeDescriptor.AttributeType.SETTER ) ;
	Assert.assertEquals( setter_barA, ad.method() ) ;

        ad = AttributeDescriptor.findAttribute( mom, ca, "something", "null description", 
            AttributeDescriptor.AttributeType.GETTER ) ;
	Assert.assertEquals( getter_something, ad.method() ) ;

        ad = AttributeDescriptor.findAttribute( mom, ca, "fooC", "null description", 
            AttributeDescriptor.AttributeType.GETTER ) ;
	Assert.assertEquals( getter_fooC, ad.method() ) ;

        ad = AttributeDescriptor.findAttribute( mom, ca, "fooD", "null description", 
            AttributeDescriptor.AttributeType.SETTER ) ;
	Assert.assertEquals( setter_fooD, ad.method() ) ;
    }

    // @ExpectedExceptions( { IllegalArgumentException.class } )

    //==============================================================================================
    // Tests for TypeConverter class
    //==============================================================================================
    
    // For each kind of type, test:
    // getDataType
    // getManagedType
    // toManagedEntity
    // fromManagedEntity
    // isIdentity
    //
    // to/from round trip should result in equal objects (java -> managed -> java -> managed)
    //
    // Types to test:
    //
    // primitives (DONE)
    // Date, ObjectName, String, BigDecimal, BigInteger (DONE)
    // enum
    // ManagedObject
    // ManagedData, simple case (DONE)
    // ManagedData, @IncludeSubclass
    // ManagedData, @InheritTable
    // C<X> for a subtype of Collection
    // M<K,V> for a subtype of map (map, sorted map)
    // X[]
    // Main cases of interest for collections, maps, and arrays are ManagedObject, ManagedData, and something 
    //	    simple like String
    // Defer testing of TypeVariable and WildCardType
    // Also test an arbitrary class not of the above (uses toString, may or may not have a <init>( String )
    // constructor

    private static ObjectName makeObjectName( String str ) {
	try {
	    return new ObjectName( str ) ;
	} catch (MalformedObjectNameException exc) {
	    throw new RuntimeException( exc ) ;
	}
    }

    private static final Object[][] primitiveTCTestData = new Object[][] {
	{ BigDecimal.class, SimpleType.BIGDECIMAL, new BigDecimal( "1.234556677888" ) },
	{ BigInteger.class, SimpleType.BIGINTEGER, new BigInteger( "1234566789012334566576790" ) },
	{ boolean.class, SimpleType.BOOLEAN, Boolean.TRUE },
	{ Boolean.class, SimpleType.BOOLEAN, Boolean.TRUE },
	{ byte.class, SimpleType.BYTE, 5 },
	{ Byte.class, SimpleType.BYTE, -23 },
	{ char.class, SimpleType.CHARACTER, 'a' },
	{ Character.class, SimpleType.CHARACTER, 'A' },
	{ Date.class, SimpleType.DATE, new Date() },
	{ double.class, SimpleType.DOUBLE, Double.valueOf( 1.2345D ) },
	{ Double.class, SimpleType.DOUBLE, Double.valueOf( 1.2345D ) },
	{ float.class, SimpleType.FLOAT, Float.valueOf( 1.2345F ) },
	{ Float.class, SimpleType.FLOAT, Float.valueOf( 1.2345F ) },
	{ short.class, SimpleType.SHORT, Short.valueOf( (short)24 ) },
	{ Short.class, SimpleType.SHORT, Short.valueOf( (short)26 ) },
	{ int.class, SimpleType.INTEGER, Integer.valueOf( 2345323 ) },
	{ Integer.class, SimpleType.INTEGER, Integer.valueOf( 2345323 ) },
	{ long.class, SimpleType.LONG, Long.valueOf( 743743743743743743L ) },
	{ Long.class, SimpleType.LONG, Long.valueOf( 743743743743743743L ) },
	{ ObjectName.class, SimpleType.OBJECTNAME, makeObjectName( "foo: bar1=red, bar2=blue" ) },
	{ String.class, SimpleType.STRING, "foo" } 
    } ;

    @Test
    public void testPrimitiveTypeConverter() {
	ManagedObjectManagerInternal mom = 
            (ManagedObjectManagerInternal)ManagedObjectManagerFactory.create( "ORBTest" ) ;

	for (Object[] data : primitiveTCTestData) {
	    Class cls = (Class)data[0] ;
	    SimpleType st = (SimpleType)data[1] ;
	    Object value = data[2] ;

	    TypeConverter tc = mom.getTypeConverter( cls ) ;

	    Assert.assertTrue( tc.getDataType() == cls ) ;
	    Assert.assertTrue( tc.getManagedType() == st ) ;
	    Assert.assertTrue( tc.isIdentity() ) ;

	    Object managed = tc.toManagedEntity( value ) ;
	    Object value2 = tc.fromManagedEntity( managed ) ;
	    Assert.assertEquals( value, value2 ) ;

	    Object managed2 = tc.toManagedEntity( value2 ) ;
	    Assert.assertEquals( managed, managed2 ) ;
	}
    }

    public static final String MDE_DESCRIPTION = "Description of ManagedDataExample" ;
    public static final String MDE_ATTR_DESC_NAME = "Description of ManagedDataExample name attribute" ;
    public static final String MDE_ATTR_DESC_DATE = "Description of ManagedDataExample date attribute" ;
    public static final String MDE_ATTR_ID_NAME = "name" ;
    public static final String MDE_ATTR_ID_DATE = "currentDate" ;

    @ManagedData( description=MDE_DESCRIPTION )
    public class ManagedDataExample {
	private String name ;
	private Date date ;

	public ManagedDataExample( String name ) {
	    this.name = name ;
	    date = new Date() ;
	}

	@ManagedAttribute( description=MDE_ATTR_DESC_NAME, id=MDE_ATTR_ID_NAME )
	public String name() {
	    return name ;
	}

	@ManagedAttribute( description=MDE_ATTR_DESC_DATE, id=MDE_ATTR_ID_DATE )
	public Date date() {
	    return date ;
	}

	public boolean equals( Object obj ) {
	    if (this.equals( obj ))
		return true ;

	    if (!(obj instanceof ManagedDataExample))
		return false ;

	    ManagedDataExample mde = (ManagedDataExample)obj ;
	    return mde.name.equals( name ) && mde.date.equals( date ) ;
	}

	public int hashCode() {
	    return name.hashCode() ^ date.hashCode() ;
	}
    }

    public static final String MOE_DESCRIPTION = "Description of ManagedObject" ;
    public static final String MOE_ATTR_DESC_NAME = "Description of ManagedAttribute name" ;
    public static final String MOE_ATTR_DESC_NUM = "Description of ManagedAttribute num" ;
    public static final String MOE_ATTR_DESC_MDE = "Description of ManagedAttribute mde" ;
    public static final String MOE_OP_DESC_INCREMENT = "Description of ManagedOperation increment" ;
    public static final String MOE_OP_DESC_DECREMENT = "Description of ManagedOperation decrement" ;

    @ManagedObject( description=MOE_DESCRIPTION ) 
    public class ManagedObjectExample {
	private int num ;
	private String name ;
	private ManagedDataExample mde ;

	public ManagedObjectExample( int num, String name ) {
	    this.num = num ;
	    this.name = name ;
	    this.mde = new ManagedDataExample( name ) ;
	}

	@ManagedAttribute( description=MOE_ATTR_DESC_NAME )
	public String getName() {
	    return name ;
	}

	@ManagedAttribute( description=MOE_ATTR_DESC_NUM ) 
	public int getNum() {
	    return num ;
	}

	@ManagedAttribute( description=MOE_ATTR_DESC_MDE )
	public ManagedDataExample getMde() {
	    return mde ;
	}

	@ManagedOperation( description=MOE_OP_DESC_INCREMENT ) 
	public int increment( int value ) {
	    return num+=value ;
	}

	@ManagedOperation( description=MOE_OP_DESC_DECREMENT ) 
	public int decrement( int value ) {
	    return num-=value ;
	}

	public void nop() {
	    // This is not a managed operation.
	}
    }

    @Test
    public void testManagedObjectExample() {
	final String domain = "ORBTest" ;
	final int num = 12 ;
	final String name = "Liskov" ;
	final ManagedObjectExample moe = new ManagedObjectExample( num, name ) ;
	final String propName = "ObjectNumber" ;
	final int onum = 1 ;

	final ManagedObjectManager mom = ManagedObjectManagerFactory.create( domain ) ;

	try {
	    mom.register( moe, propName + "=" + onum ) ;

	    ObjectName moeName = mom.getObjectName( moe ) ;
	    Assert.assertEquals( domain, moeName.getDomain() ) ;
	    
	    Hashtable expectedProperties = new Hashtable() ;
	    expectedProperties.put( propName, "" + onum ) ;
	    expectedProperties.put( "type", ManagedObjectExample.class.getName() ) ;
	    
	    Assert.assertEquals( expectedProperties, moeName.getKeyPropertyList() ) ;

	    MBeanServer mbs = mom.getMBeanServer() ;

	    // Validate attributes
	    Assert.assertEquals( mbs.getAttribute( moeName, "num" ), Integer.valueOf( num ) ) ;
	    Assert.assertEquals( mbs.getAttribute( moeName, "name" ), name ) ;
	    Object obj = mbs.getAttribute( moeName, "mde" ) ;
	    Assert.assertTrue( obj instanceof CompositeData ) ;
	    CompositeData cdata = (CompositeData)obj ;
	    Assert.assertEquals( moe.getMde().name(), cdata.get( MDE_ATTR_ID_NAME ) ) ;
	    Assert.assertEquals( moe.getMde().date(), cdata.get( MDE_ATTR_ID_DATE ) ) ;

	    // Validate operations
	} catch (Exception exc) {
	    exc.printStackTrace() ;
	    Assert.fail( "Caught exception on register " + exc ) ;
	} finally {
	    try {
		mom.unregister( moe ) ;
	    } catch (Exception exc) {
		exc.printStackTrace() ;
		Assert.fail( "Caught exception on unregister " + exc ) ;
	    }
	}

	// make sure object is really gone
    }

    @Test
    public void testManagedDataTypeConverter() {
	ManagedObjectManagerInternal mom = 
            (ManagedObjectManagerInternal)ManagedObjectManagerFactory.create( "ORBTest" ) ;
    
	TypeConverter tc = mom.getTypeConverter( ManagedDataExample.class ) ;
	Assert.assertTrue( tc.getDataType() == ManagedDataExample.class ) ;

	OpenType otype = tc.getManagedType() ;
	Assert.assertTrue( otype instanceof CompositeType ) ;
	CompositeType ctype = (CompositeType)otype ;
	Assert.assertEquals( MDE_DESCRIPTION, ctype.getDescription() ) ;
	Assert.assertEquals( MDE_ATTR_DESC_NAME, ctype.getDescription( MDE_ATTR_ID_NAME ) ) ;
	Assert.assertEquals( MDE_ATTR_DESC_DATE, ctype.getDescription( MDE_ATTR_ID_DATE ) ) ;
	Assert.assertEquals( SimpleType.STRING, ctype.getType( MDE_ATTR_ID_NAME ) ) ;
	Assert.assertEquals( SimpleType.DATE, ctype.getType( MDE_ATTR_ID_DATE ) ) ;

	Set keys = new HashSet() ;
	keys.add( MDE_ATTR_ID_NAME ) ;
	keys.add( MDE_ATTR_ID_DATE ) ;
	Assert.assertEquals( keys, ctype.keySet() ) ;

	Assert.assertFalse( tc.isIdentity() ) ;

	ManagedDataExample value = new ManagedDataExample( "test" ) ;

	Object managed = tc.toManagedEntity( value ) ;

	Assert.assertTrue( managed instanceof CompositeData ) ;
	CompositeData cdata = (CompositeData)managed ;
	Assert.assertEquals( cdata.getCompositeType(), ctype ) ;
	Assert.assertEquals( value.name(), (String)cdata.get( MDE_ATTR_ID_NAME ) ) ;
	Assert.assertEquals( value.date(), (Date)cdata.get( MDE_ATTR_ID_DATE ) ) ;
    }

    public static void main( String[] args ) {
        TestngRunner runner = new TestngRunner() ;
        runner.registerClass( Client.class ) ;
        runner.run() ;
        runner.systemExit() ;
    }
}
