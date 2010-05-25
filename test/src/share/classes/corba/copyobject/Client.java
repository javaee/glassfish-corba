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

package corba.copyobject  ;

import java.lang.reflect.Proxy;
import java.lang.reflect.Method;

import java.util.*;
import java.io.PrintStream ;
import java.io.Serializable ;
import java.math.BigInteger;
import java.math.BigDecimal;
import java.sql.Time;
import java.sql.Timestamp;

import java.rmi.Remote;
import java.rmi.RemoteException;

import javax.rmi.CORBA.Util;
import javax.rmi.PortableRemoteObject;
import javax.rmi.CORBA.Tie;

import org.omg.CORBA.CompletionStatus;
import org.omg.CORBA.BAD_PARAM;
import org.omg.CORBA.BAD_INV_ORDER;
import org.omg.CORBA.SystemException;
import org.omg.CORBA.TypeCode;

import junit.framework.AssertionFailedError ;
import junit.framework.TestCase ;
import junit.framework.Test ;
import junit.framework.TestResult ;
import junit.framework.TestSuite ;
import junit.framework.TestListener ;
import junit.textui.TestRunner ;

import corba.framework.TimedTest ;
import corba.framework.TestCaseTools ;
// import corba.framework.TimerUtils ;

import com.sun.corba.se.spi.orbutil.misc.ObjectUtility ;
import com.sun.corba.se.spi.orbutil.copyobject.LibraryClassLoader ;

import com.sun.corba.se.spi.orbutil.generic.Holder ;

import com.sun.corba.se.spi.orb.ORB ;

import com.sun.corba.se.spi.orbutil.copyobject.ReflectiveCopyException ;
import com.sun.corba.se.spi.orbutil.copyobject.ObjectCopier ;
import com.sun.corba.se.spi.orbutil.copyobject.ObjectCopierFactory ;

import com.sun.corba.se.spi.presentation.rmi.StubAdapter ;

import com.sun.corba.se.spi.orbutil.newtimer.Statistics ;
import com.sun.corba.se.spi.orbutil.newtimer.StatsEventHandler ;
import com.sun.corba.se.spi.orbutil.newtimer.Timer ;
import com.sun.corba.se.spi.orbutil.newtimer.TimerManager ;
import com.sun.corba.se.spi.orbutil.newtimer.TimerEvent ;
import com.sun.corba.se.spi.orbutil.newtimer.TimerEventController ;
import com.sun.corba.se.spi.orbutil.newtimer.TimerEventHandler ;
import com.sun.corba.se.spi.orbutil.newtimer.TimerFactory ;
import com.sun.corba.se.spi.orbutil.newtimer.TimerFactoryBuilder ;
import com.sun.corba.se.spi.orbutil.newtimer.TimerGroup ;

import com.sun.corba.se.impl.orbutil.newtimer.generated.TimingPoints ;
import com.sun.corba.se.spi.orbutil.ORBConstants ;

public abstract class Client extends TestCase
{
    private static final boolean SIMULATED_TIMING = false ;
    private static final boolean DEBUG = false ;
    private static final int REP_COUNT = 200 ;
    private static ORB orb ;
    private List timedTests ;

    static {
	try {
	    System.out.println( "START static init" ) ;
	    Properties props = System.getProperties() ;
	    props.setProperty( ORBConstants.TIMING_POINTS_ENABLED, "true" ) ;
	    System.out.println( props.getProperty( "org.omg.CORBA.ORBClass" ) ) ;
	    String[] args = null ;
	    orb = (ORB)ORB.init( args, props ) ;
	} catch (Throwable t) {
	    t.printStackTrace() ;
	} finally {
	    System.out.println( "END static init" ) ;
	}
    }
 
    protected boolean usesCDR() {
	return false ;
    }

    protected void runTest() throws Throwable
    {
	if (isTestExcluded())
	    // Excluded tests always pass.  Be careful with this.
	    assertTrue( true ) ;
	else
	    try {
		super.runTest() ;
		if (shouldThrowReflectiveCopyException())
		    fail( "Test did not throw expected ReflectiveCopyException" ) ;
	    } catch (RuntimeException rce) {
		Throwable cause = rce.getCause() ;
		if (!shouldThrowReflectiveCopyException()) {
		    System.out.println( "Printing stack trace:" ) ;
		    rce.printStackTrace() ;
		    fail( "Test should not have thrown Exception " + cause ) ;
		}
		if (!(rce.getCause() instanceof ReflectiveCopyException)) {
		    System.out.println( "Printing stack trace:" ) ;
		    rce.printStackTrace() ;
		    fail( "Cause of RuntimeException was " + cause 
			+ " instead of expected ReflectiveCopyException" ) ;
		}
	    } ;
    }
    
    public  Object copyObject( Object obj )
    {
	return copyObject( obj, false ) ;
    }

    public  Object copyObject( Object obj, boolean debug )
    {
	try {
	     ObjectCopierFactory factory = getCopierFactory( orb ) ;

	     // Create a new ObjectCopier and use it to copy obj.
	     return factory.make().copy( obj, debug ) ;
	} catch (ReflectiveCopyException rce) {
	    throw new RuntimeException( rce ) ;
	}
    }

    protected boolean findInArray( String name, String[] args ) 
    {
	for( String str : args ) 
	    if (name.equals( str ))
		return true ;

	return false ;
    }

    public abstract boolean isTestExcluded() ;

    // Override this in subclass to indicate that some
    // tests should get a ReflectiveCopyExcetion from copy.
    protected boolean shouldThrowReflectiveCopyException() 
    {
	return false ;
    }

    // Must be overridden in subclass to supply which ObjectCopierFactory is being
    // tested.
    public abstract ObjectCopierFactory getCopierFactory( ORB orb ) ;

    // Factory method for creating a test with the given name.
    // The subclass must override this, typically just be calling new
    // on its constructor.  If name is null, the no-args constructor
    // is typically called.
    public abstract Client makeTest( String name ) ;

    public  Object[] copyObjects( Object[] obj )
    {
        return (Object[])copyObject(obj);
    }

    public static void doMain( String[] args, Client root ) 
    {
	TestResult result = junit.textui.TestRunner.run(root.makeSuite()) ;

	TestCaseTools.reportTiming( REP_COUNT, System.out, root.timedTests ) ;

	if (result.errorCount() + result.failureCount() > 0) {
	    System.out.println( "Error: failures or errrors in JUnit test" ) ;
	    System.exit( 1 ) ;
	} else
	    System.exit( 0 ) ;
    }

    public Client()
    {
	super() ;
	timedTests = new ArrayList<TimedTest>() ;
    }

    public Client( String name )
    {
	super( name ) ;
	timedTests = null ;
    }

    private TestSuite makeTestSuite( String name,
	Object[] tests )
    {
	TestSuite ts = new TestSuite( name ) ;

	for (int ctr=0; ctr<tests.length; ctr++) {
	    Object obj = tests[ctr] ;
	    // System.out.println( obj.getClass() ) ;
	    if (obj instanceof String) {
		ts.addTest( makeTest( (String)tests[ctr] ) ) ;
	    } else if (obj instanceof TimedTest) {
		TimedTest tt = TimedTest.class.cast(obj) ;
		timedTests.add( tt ) ;
		ts.addTest( tt ) ;
	    } else if (obj instanceof Test) {
		ts.addTest( (Test)obj ) ;
	    } else {
		throw new RuntimeException(
		    "Error in test suite construction" ) ;
	    }
	}

	return ts ;
    }

    public Test makeSuite()
    {
	TestSuite ts = null ;

	try {
	    conditionTimingTests() ;

	    System.out.println( 
		"================================================================\n" +
		"Testing copyObject with the " + this.getClass().getName() + 
		" implementation\n" +
		"================================================================\n" ) ;

	    ts = makeTestSuite( "main", new Test[] { makeCopyObject(),
		makeCopyObjects()
	    } ) ;
	} catch (Throwable t) {
	    t.printStackTrace() ;
	} 

	return ts ;
    }

    public Test makeCopyObject()
    {
	return makeTestSuite( "testCopyObject", new Object[] {
	    makeImmutables(), makePrimitiveArray(), makeImmutableArray(),
	    makeJavaUtil(), makeJavaMath(), makeJavaSQL(),
            makeReadResolve(), makeComplexClass(), makeNonFinalComplexClass(), makeTypeCode(),
            makeExceptions(), makeExternalizable(), makeRemote(),
            makeInnerClass(), makeTransientNonSerializableField(),
            makeNonSerializableSuperClass(), 
	    makeIllegalTransients(),
	    "testClassLoader", "testDynamicProxy", "testEnum", "testSimulatedTimingGraph",
	    "testSimulatedTimingTree", "testSimulatedTimingTree1",
	    "testSimulatedTimingTree2", "testSimulatedTimingTree3",
	    "testSimulatedTimingTree4", "testSimulatedTimingTree5",
	    "testSimulatedIsDirty", "testSimulatedTimingIsDirty"
        } ) ;
    }

    public Test makeIllegalTransients() 
    {
	return makeTestSuite( "testIllegalTransients", new Object[] {
	    "testTransientThread",
	    "testTransientThreadGroup",
	    // "testTransientProcess",
	    "testTransientProcessBuilder",
	    // "testTransientSecurityManager"
	} ) ;
    }

    public Test makeImmutables()
    {
        //count - 10
	return makeTestSuite( "testImmutables", new Object[] {
	    "testImmutableString", "testImmutableBoolean", "testImmutableByte",
	    "testImmutableChar", "testImmutableShort", "testImmutableInteger",
	    "testImmutableLong", "testImmutableFloat", "testImmutableDouble",
            "testImmutableClass"
	} ) ;
    }

    public Test makePrimitiveArray() {
        //count - 9
        return makeTestSuite( "testPrimitiveArray",  new Object[] {
            "testPrimitiveBooleanArray", "testPrimitiveByteArray",
            "testPrimitiveShortArray", "testPrimitiveIntegerArray",
            "testPrimitiveLongArray", "testPrimitiveFloatArray",
            "testPrimitiveDoubleArray", "testPrimitiveLatinCharArray",
            "testPrimitiveUnicodeCharArray", "testNullObject",
	    "testObject", "testZeroLengthArray"
        } );
    }

    public Test makeImmutableArray() {
        //count - 11
        return makeTestSuite( "testImmutableArray",  new Object[] {
            "testImmutableBooleanArray", "testImmutableBooleanArrayAlias",
            "testImmutableByteArray", "testImmutableByteArrayAlias",
            "testImmutableShortArray","testImmutableShortArrayAlias",
            "testImmutableIntegerArray", "testImmutableIntegerArrayAlias",
            "testImmutableLongArray", "testImmutableLongArrayAlias",
            "testImmutableFloatArray", "testImmutableFloatArrayAlias",
            "testImmutableDoubleArray", "testImmutableDoubleArrayAlias",
            "testImmutableLatinCharArray","testImmutableUnicodeCharArray",
            "testImmutableCharArrayAlias",
            "testImmutableStringArray", "testImmutableUnicodeStringArray",
            "testImmutableStringArrayAlias", "testImmutable2dStringArray",
            "testImmutable2dStringArrayAlias",
            "testImmutable2dStringArrayComplex", "testImmutableClassArray",
            "testImmutableClassArrayAlias", "testNullObjects",
	    "testObjects" 
        } );

    }

    public Test makeJavaUtil() {
        //count - 19
        return makeTestSuite( "testJavaUtil", new Object[] {
            "testHashtable", "testHashtableComplex", "testHashMap",
            "testHashMapComplex", "testTreeSet", "testHashSet", "testVector",
            "testDate", "testCalendar", "testBitSet", "testStack",
            "testArrayList", "testLinkedList", "testLinkedHashMap",
            "testLinkedHashSet", "testProperties", "testIdentityHashMap",
            "testTreeMap", "testCustomMap"
        } );
    }

    public Test makeJavaSQL() {
        //count - 3
        return makeTestSuite( "testJavaSQL", new Object[] {
            "testSQLDate", "testSQLTime", "testSQLTimestamp"
        } );
    }

    public Test makeJavaMath() {
        //count - 3
        return makeTestSuite( "testJavaMath", new Object[] {
            "testBigInteger", "testBigDecimal", "testJavaMathArrayAlias"
        } );
    }

    public Test makeExceptions() {
        //count - 4
        return makeTestSuite( "testExceptions", new Object[] {
            "testRemoteException", "testRuntimeException",
            "testSystemException", "testUserException"
        } );
    }

    public Test makeReadResolve() {
        //count - 1
        return makeTestSuite( "testReadResolve", new Object[] {
            "testReadResolve"
        } );
    }

    public Test makeTypeCode() {
	return makeTestSuite( "testTypeCode", new Object[] {
            "testSimpleTypeCode" , "testUnionTypeCode" ,
            "testValuetypeTypeCode", "testRecursiveTypeCode"
        } );
    }

    public Test makeRemote() {
	return makeTestSuite( "testRemoteObject", new Object[] {
            "testRemoteStub", "testCORBAObject"
        } );
    }

    public Test makeExternalizable() {
	return makeTestSuite( "testExternalizable", new Object[] {
            "testExternalizable", "testExternalizableNonStaticContext"
        } );
    }

    private void checkDeepEquals( Object obj1, Object obj2 )
    {
	if (!ObjectUtility.equals( obj1, obj2 )) {
	    fail( "Deep Equals check unexpectedly failed on objects\n" +
		ObjectUtility.defaultObjectToString( obj1 ) +
		"\nand\n " +
		ObjectUtility.defaultObjectToString( obj2 ) + "\n" ) ;
	}
    }

    private void checkIdentity( Object obj1, Object obj2 )
    {
	if (obj1 != obj2) {
	    fail( "Reference equality check unexpectedly failed on objects\n" +
                  ObjectUtility.defaultObjectToString( obj1 ) +
                  "\nand\n " +
                  ObjectUtility.defaultObjectToString( obj2 ) + "\n" ) ;
	}
    }

    private void checkNotIdentity( Object obj1, Object obj2 )
    {
	if (obj1 == obj2) {
	    fail( "Objects are identical and should not be: obj =\n" +
		ObjectUtility.defaultObjectToString( obj1 ) + "\n" ) ;
	}
    }

    private void doImmutableTest( Object data )
    {
	if (DEBUG)
	    System.out.println( "doImmutableTest called with data type " + 
		data.getClass().getName() ) ;

	Object result = copyObject( data ) ;

        checkDeepEquals( data, result ) ;
	// do not check identity in this case, as it is an optimization
	// rather than part of the copyObject contract
    }

    private void doStandardTest( Object data )
    {
	if (DEBUG)
	    System.out.println( "doStandardTest called with data type " + 
		data.getClass().getName() ) ;

	Object result = copyObject( data ) ;

        checkDeepEquals( data, result ) ;
	checkNotIdentity( data, result ) ;
    }

    private void doCopyObjectsTest( Object[] data )
    {
	if (DEBUG)
	    System.out.println( "doCopyObjectsTest called with data type " + 
		data.getClass().getName() ) ;

	Object[] result = copyObjects( data ) ;

        checkDeepEquals( data, result ) ;
	checkNotIdentity( data, result ) ;
    }

    private boolean hasDelegateSet( Object obj )
    {
	if (!StubAdapter.isStub( obj )) 
	    return false ;

	try {
	    StubAdapter.getDelegate(obj) ;
	    return true ;
	} catch (Exception exc) {
	    return false ;
	}
    }

    private void doRemoteTest(Object data)
        throws RemoteException 
    {
	if (DEBUG)
	    System.out.println( "doRemoteTest called with data type " + 
		data.getClass().getName() ) ;

        try {
	    assertFalse( hasDelegateSet( data ) ) ;
            Object result = copyObject( data );
	    assertTrue( hasDelegateSet( data ) ) ;
            checkDeepEquals(data, result);
            // wrong test: checkNotIdentity(data, result);
        } finally {
            try {
                Util.unexportObject((Remote) data);
            } catch (Exception e) { }
        }
    }

    public void testImmutableClass()
    {
	Class arg = Client.class ;
	Object result = copyObject( arg ) ;
	String argName = arg.getName() ;
	String resultName = ((Class)result).getName() ;
	if (!argName.equals( resultName ))
	    fail( "Class test failed: argName = " + argName +
		" resultName = " + resultName ) ;
    }

    public void testImmutableString()
    {
	doImmutableTest( "This is a test" ) ;
    }

    public void testImmutableBoolean()
    {
	doImmutableTest( Boolean.TRUE ) ;
    }

    public void testImmutableByte()
    {
	doImmutableTest( Byte.valueOf( (byte)26 ) ) ;
    }

    public void testImmutableChar()
    {
        doImmutableTest(Character.valueOf('a'));
	//doImmutableTest(Character.valueOf('\u00FF'));
    }

    public void testImmutableShort()
    {
	doImmutableTest(Short.valueOf((short)1234));
    }

    public void testImmutableInteger()
    {
	doImmutableTest(Integer.valueOf(1234));
    }

    public void testImmutableLong()
    {
	doImmutableTest(Long.valueOf(903283420L));
    }

    public void testImmutableFloat()
    {
	doImmutableTest(Float.valueOf(93.0320F));
    }

    public void testImmutableDouble()
    {
	doImmutableTest(Double.valueOf(093209.329320));
    }

    public void testPrimitiveBooleanArray() {
        boolean toggle = false;
        boolean[] boolArray = new boolean[100];
        for (int i = 0; i < boolArray.length; i++) {
            toggle = !toggle;
            boolArray[i] = toggle;
        }

        doStandardTest(boolArray);
    }

    public void testPrimitiveByteArray()
    {
        byte[] byteArray = new byte[Byte.MAX_VALUE - Byte.MIN_VALUE];
        for (int i = 0; i < byteArray.length; i++) {
            byteArray[i] = (byte) (Byte.MIN_VALUE + i);
        }

        doStandardTest(byteArray);
    }

    public void testPrimitiveShortArray() {

        short[] shortArray = new short[100];
        for (short i = 0; i < shortArray.length; i++) {
            shortArray[i] = i;
        }

        doStandardTest(shortArray);
    }

    public void testPrimitiveIntegerArray() {
        int[] intArray = new int[100];
        for (int i = 0; i < intArray.length; i++) {
            intArray[i] = i;
        }
        doStandardTest(intArray);
    }

    public void testPrimitiveLongArray() {
        long[] longArray = new long[100];
        for (int i = 0; i < longArray.length; i++) {
            longArray[i] = (long) i;
        }
        doStandardTest(longArray);
    }

    public void testPrimitiveFloatArray() {

        float[] floatArray = new float[100];
        for (int i = 0; i < floatArray.length; i++) {
            floatArray[i] = (float) 3.2 + (float) i;
        }
        doStandardTest(floatArray);
    }

    public  void testPrimitiveDoubleArray() {

        double[] doubleArray = new double[100];
        for (int i = 0; i < doubleArray.length; i++) {
            doubleArray[i] = (double) 10.35 + (double) i;
        }

        doStandardTest(doubleArray);
    }

    public  void testPrimitiveLatinCharArray() {

        char[] latinCharArray = new char[128];
        for (int i = 0; i < latinCharArray.length; i++) {
            latinCharArray[i] = (char) i;
        }
        doStandardTest(latinCharArray);
    }

    public  void testPrimitiveUnicodeCharArray() {

        char[] unicodeCharArray = new char[100];
        for (int i = 0; i < unicodeCharArray.length; i++) {
            unicodeCharArray[i] = (char) ((char) '\u6D77' + (char) i);
        }
        doStandardTest(unicodeCharArray);
    }

    public  void testZeroLengthArray() {
        Object[] array = new Object[0];
        doStandardTest(array);
    }

    public  void testNullObject() {
        Object result = copyObject(null);
        checkIdentity(null, result);
    }

    public void testObject() {
	Object obj = new Object() ;
	doImmutableTest( obj ) ;
    }

    public void testObjects() {
	Object[] arr = new Object[3] ;
	arr[0] = new Object() ;
	arr[1] = arr[0] ;
	arr[2] = new Object() ;
	doStandardTest( arr ) ;
    }

    public  void testNullObjects() {
        Object[][] array = new Object[3][];
        array[0] =
            new Object[] { "test", new Object[] { null } };

	array[1] = new Object[] { null ,
	    new Object[] { null, "hello" }, "test" };

	array[2] = null;

	doStandardTest(array);
    }

    //----------

    public  void testImmutableBooleanArray() {
        Boolean[] boolArray = new Boolean[10];
        for (int i = 0; i < boolArray.length; i++) {
            if ((i % 2) == 0)
                boolArray[i] = Boolean.TRUE;
            else
                boolArray[i] = Boolean.FALSE;
        }
        doStandardTest(boolArray);
    }

    public  void testImmutableByteArray()
    {
        Byte[] byteArray = new Byte[10];
        for (int i = 0; i < byteArray.length; i++) {
            byteArray[i] =(byte)i;
        }

        doStandardTest(byteArray);
    }

    public  void testImmutableShortArray() {

        Short[] shortArray = new Short[10];
        for (short i = 0; i < shortArray.length; i++) {
            shortArray[i] = i;
        }

        doStandardTest(shortArray);
    }

    public  void testImmutableIntegerArray() {
        Integer[] intArray = new Integer[10];
        for (int i = 0; i < intArray.length; i++) {
            intArray[i] = i;
        }
        doStandardTest(intArray);
    }

    public  void testImmutableLongArray() {
        Long[] longArray = new Long[10];
        for (int i = 0; i < longArray.length; i++) {
            longArray[i] = (long) i;
        }
        doStandardTest(longArray);
    }

    public  void testImmutableFloatArray() {

        Float[] floatArray = new Float[10];
        for (int i = 0; i < floatArray.length; i++) {
            floatArray[i] = (float) 3.2 + (float) i;
        }
        doStandardTest(floatArray);
    }

    public  void testImmutableDoubleArray() {

        Double[] doubleArray = new Double[10];
        for (int i = 0; i < doubleArray.length; i++) {
            doubleArray[i] = 10.35 + (double) i;
        }
        doStandardTest(doubleArray);
    }

    public  void testImmutableLatinCharArray() {

        Character[] latinCharArray = new Character[128];
        for (int i = 0; i < latinCharArray.length; i++) {
            latinCharArray[i] = (char) i;
        }
        doStandardTest(latinCharArray);
    }

    public  void testImmutableUnicodeCharArray() {

        Character[] unicodeCharArray = new Character[100];
        for (int i = 0; i < unicodeCharArray.length; i++) {
            unicodeCharArray[i] = (char) ((char) '\u6D77' + (char) i);
        }
        doStandardTest(unicodeCharArray);
    }

    public  void testImmutableStringArray() {

        String[] stringArray = new String[25];
        for (int i = 0; i < stringArray.length; i++) {
            stringArray[i] = new String("String " + i);
        }
        doStandardTest(stringArray);
    }

    public  void testImmutableUnicodeStringArray() {

        String[] unicodeStrArray = new String[25];
        for (int i = 0; i < unicodeStrArray.length; i++) {
            unicodeStrArray[i] = new String("Unicode " + '\u6D77' + " #" + i);
        }
        doStandardTest(unicodeStrArray);
    }

    public  void testImmutableBooleanArrayAlias() {

        Boolean[] boolArray =
        { Boolean.TRUE, Boolean.FALSE, Boolean.valueOf("true"), Boolean.FALSE };

        doStandardTest(boolArray);
    }

    public  void testImmutableByteArrayAlias()
    {
        Byte b1 = Byte.valueOf(Byte.MAX_VALUE);
        Byte b2 = Byte.valueOf((byte) 2);
        Byte b11 = Byte.valueOf((byte) 1);

        Byte[] byteArray =
        { b1, b11, b1, Byte.valueOf((byte) 2), b2 };

        doStandardTest(byteArray);
    }

    public  void testImmutableShortArrayAlias() {

        Short s1 = Short.MAX_VALUE;
        Short[] shortArray = { s1, s1, s1 };

        doStandardTest(shortArray);
    }

    public  void testImmutableIntegerArrayAlias() {
        Integer one = Integer.valueOf(1);
        Integer two = Integer.valueOf(2);

        Integer[] intArray =  new Integer[]
        { one, two, Integer.valueOf(1), one, Integer.valueOf(2) } ;
        doStandardTest(intArray);
    }

    public  void testImmutableLongArrayAlias() {
        Long l1 = Long.valueOf(1);
        Long l2 = Long.MIN_VALUE;

        Long[] longArray = new Long[]
        {  l2, l2, l1 };

        doStandardTest(longArray);
    }

    public  void testImmutableFloatArrayAlias() {
        Float f1 = Float.MAX_VALUE;
        Float f2 = Float.MIN_VALUE;

        Float[] floatArray = { f1 , f1, Float.valueOf(8938.9329f), f2, f2 };
        doStandardTest(floatArray);
    }

    public  void testImmutableDoubleArrayAlias() {

        Double d1 = Double.valueOf(823.932);
        Double d2 = Double.valueOf(823.932);

        Double[] doubleArray = { d1, d1, d2, Double.valueOf(823.932) };
        doStandardTest(doubleArray);
    }

    public  void testImmutableCharArrayAlias() {

        Character c1 = Character.valueOf((char) 65);
        Character c2 = Character.valueOf('\u6d77');
        Character c3 = c1;

        Character[] latinCharArray = { c1, c2, c3 };

        doStandardTest(latinCharArray);
    }

    public  void testImmutableStringArrayAlias() {

        String s1 = "";
        String s2 = new String("");
        String s3 = "test";
        String s4 = new String("test");

        String[] stringArray = new String[] { s1, s2, s3, s4, s1, s2, s3, s4};

        doStandardTest(stringArray);
    }

    public  void testImmutable2dStringArray() {

        String[][] stringArray = new String[4][3];
        for (int i = 0; i < 4; i++){
            for (int j = 0; j < 3; j++) {
                stringArray[i][j] = String.valueOf(i * j);
            }
        }
        doStandardTest(stringArray);
    }

    public  void testImmutable2dStringArrayAlias() {
        String[] array =
        { new String("one"), new String("two"), "", new String("") };

        String[][] stringArray = new String[3][4];
        for (int i = 0; i < 3; i++){
            for (int j = 0; j < 4; j++) {
                stringArray[i][j] = array[j];
            }
        }
        doStandardTest(stringArray);
    }

    public  void testImmutable2dStringArrayComplex() {
        String s1 = new String("one");
        String s2 = new String("two");
        String s3 = "";
        String s4 = new String("");

        String[] array1 = new String[] { s1, s2, s3 };

        String[][] stringArray = new String[5][];
        stringArray[0] = array1;
        stringArray[1] = new String[] { s1, "", new String("") };
        stringArray[2] = new String[] { "test1", s4 };
        stringArray[3] = new String[] { s1, s2, s3 };
        stringArray[4] = array1;

        doStandardTest(stringArray);
    }

    public  void testImmutableClassArray() {

        Class[] classArray = new Class[]
        { new String("str").getClass(), new Integer(1).getClass() };

        doStandardTest(classArray);
    }

    public  void testImmutableClassArrayAlias() {

        Class c1 = new String("test").getClass();
        Class c2 = new Integer(1).getClass();

        Class[] classArray = { c1, c2, c1, c2 };

        doStandardTest(classArray);
    }

    public  void testHashtable() {

        Hashtable data = new Hashtable();
        Integer one = Integer.valueOf(1);
        Integer two = Integer.valueOf(2);
        Integer[] array = { one, two } ;
        data.put(one, array);
        data.put(two, "this is a test");

        doStandardTest(data);
    }

    public  void testHashtableComplex() {
        Hashtable data = new Hashtable();

        data.put("test", "this is a test");
        data.put("self", data);
        doStandardTest(data);
    }

    public  void testHashMap() {

        HashMap data = new HashMap(20, 0.75f);
        data.put("int", Integer.valueOf(1));
        data.put("this is a test", "this is a test");

        doStandardTest(data);
    }

    public  void testHashMapComplex() {

        String test = "this is a test";
        HashMap data = new HashMap(20, 0.75f);
        data.put(test, test);
        data.put("self", data);

        HashMap map1 = new HashMap(2, 0.5f);
        map1.put("test", test);

        HashMap map2 = new HashMap();
        map2.put("key", Character.valueOf('a'));

        Hashtable table = new Hashtable();
        table.put("key", test);

        map2.put("map1", map1);
        map2.put("table", table);

        data.put("map2", map2);
        data.put(data.getClass().getName(), data.getClass());

        doStandardTest(data);
    }

    public  void testTreeMap() {
        TreeMap data = new TreeMap();
        data.put("1", new String("hello"));
        StringBuffer sb = new StringBuffer();
        sb.append("hello \n").append("world");
        data.put("sb", sb);

        doStandardTest(data);
    }

    public  void testCustomMap() {
        CustomMap data = new CustomMap();
        data.put("int", Integer.MIN_VALUE);
        data.put("float", Float.MIN_VALUE);
        doStandardTest(data);
    }

    public  void testTreeSet() {
        TreeSet data = new TreeSet();

        data.add(Suit.HEARTS);
        data.add(Suit.DIAMONDS);
        doStandardTest(data);
    }

    public  void testHashSet() {
        HashSet data = new HashSet();
        data.add(Suit.CLUBS);
        data.add(Operation.PLUS);

        doStandardTest(data);
    }

    public  void testArrayList() {

        Object[] obj = { "test",  Integer.valueOf(1), Boolean.valueOf(true) };

        //create an immutable list
        List data = Collections.nCopies(3, obj);
        doImmutableTest(data);
    }

    public  void testVector() {

        //String[][] = new String
        String[] strArray = { "this" , "is", "a" , "test" };

        Object[] objectArray =
        { Byte.valueOf((byte) 1), Short.valueOf((short) 1), strArray };

        Vector vector = new Vector();
        vector.add(strArray);
        vector.add(objectArray);

        doStandardTest(vector);
    }

    public  void testCalendar() {
        Calendar calendar =
            Calendar.getInstance(TimeZone.getTimeZone("PST"), Locale.US);

        doStandardTest(calendar);
    }

    public  void testDate() {
        Date data = new Date(System.currentTimeMillis());
        doStandardTest(data);
    }

    /*
    public  void testLocale() {
	// fails on Java SE 6: 
        doStandardTest(Locale.JAPAN);
    }
    */

    public  void testBitSet() {
        BitSet data = new BitSet(32);
        data.set(0);
        data.set(24);

        doStandardTest(data);
    }

    public  void testStack() {
        Stack data = new Stack();

        data.push(new Date(System.currentTimeMillis()));
        String[] strArray = { "one", "two", "three" };
        Object[][] rows = new Object[1][3];
        rows[0] = strArray;
        data.push(rows);
        data.push(strArray);

        doStandardTest(data);
    }

    public  void testProperties() {
        doStandardTest(System.getProperties());
    }

    public  void testIdentityHashMap() {
        IdentityHashMap data = new IdentityHashMap();
        data.put("+", Operation.PLUS);
        data.put("*", ExtendedOperation.TIMES);
        data.put("spades", Suit.SPADES);

        data.put(null, "nullKey");
        data.put("nullValue", null);
        doStandardTest(data);
    }

    public  void testLinkedList() {
        LinkedList data = new LinkedList();
        LinkedList list1 = new LinkedList();

        data.addLast(list1);
        LinkedList list2 = new LinkedList();
        list1.addLast(list2);

        list2.addLast(data);

        doStandardTest(data);
    }

    public  void testLinkedHashMap() {
        LinkedHashMap data = new LinkedHashMap(10, 0.25f, true);
        data.put("int", Integer.MIN_VALUE);
        data.put("float", Float.MIN_VALUE);
        doStandardTest(data);
    }

    public  void testLinkedHashSet() {
        Collection c = new Vector();
        Hashtable hashtable = new Hashtable();
        hashtable.put("one", Integer.MAX_VALUE);
        c.add(hashtable);
        c.add(ExtendedOperation.LIST);
        c.add(new Date());

        LinkedHashSet data = new LinkedHashSet(c);
        doStandardTest(data);
    }

    public  void testSQLDate() {
        java.sql.Date date = new java.sql.Date(System.currentTimeMillis());
        doStandardTest(date);
    }

    public  void testSQLTime() {
        Time time = new Time(System.currentTimeMillis());
        doStandardTest(time);
    }

    public  void testSQLTimestamp() {
        Timestamp timestamp = new Timestamp(System.currentTimeMillis());
        doStandardTest(timestamp);
    }

    public  void testBigInteger() {
        BigInteger bigInt = new BigInteger("4399098320230329029230");

        doImmutableTest(bigInt);
    }

    public  void testBigDecimal() {

        BigInteger bigInt = new BigInteger("-7492932907329832");
        BigDecimal bigDecimal = new BigDecimal(bigInt);

        doImmutableTest(bigInt);
    }

    public  void testJavaMathArrayAlias() {
        BigInteger bigInt1 = new BigInteger("4399230329029230");
        BigInteger bigInt2 = new BigInteger("909832023029230");
        BigDecimal bigDecimal = new BigDecimal(bigInt1);

        Object[] array = { bigInt1, bigInt2, bigDecimal, bigInt1, bigDecimal };

        doStandardTest(array);
    }

    public  void testReadResolve() {
        doImmutableTest(Operation.PLUS);
    }


    public  void testRemoteException() {
        SystemException sx = new BAD_PARAM("Invalid argument");
        RemoteException rt = javax.rmi.CORBA.Util.mapSystemException(sx);

        doStandardTest(rt);
    }

    public  void testRuntimeException() {
        // BAD_PARAM param = new BAD_PARAM("Invalid argument");
	RuntimeException cause = new RuntimeException() ;
        RuntimeException rt = new RuntimeException();
        rt.initCause(cause);

        doStandardTest(rt);
    }

    public  void testSystemException() {
        BAD_INV_ORDER bio = new BAD_INV_ORDER("Delegate not set!!", 4,
                                          CompletionStatus.COMPLETED_NO);

        doStandardTest(bio);
    }

    public  void testUserException() {
        UserException ue = null;
        try {
            throwUserException();
        } catch (UserException ex) {
            ue = ex;
        }
        doStandardTest(ue);
    }

    private  void throwUserException() throws UserException {
        Object obj = null;
        try {
            obj.equals(obj);
        } catch (NullPointerException npe) {
            UserException ue = new UserException("User exception");
            ue.initCause(npe);
            throw ue;
        }
    }

    private static class UserException extends Exception {
        UserException(String msg) {
            super(msg);
        }
    }

    // Slightly modified to simulate generated copy() method.
    //
    private static class ComplexClass implements java.io.Serializable {
	public boolean publicBoolean = false ;
	protected char protectedChar = 'D' ;
	private byte privateByte = (byte)3 ;
	short shrt = (short)-2345 ;
	final public int finalPublicInt = 273415 ;
	final protected long finalProtectedLong = 38958284 ;
	final private float finalPrivateFloat = (float)3.1415926535 ;
	final double finalDouble = 2.718281828 ;
	String str1 ;
	final String str2 ;
	final private Object finalPrivateObject1 = new Object() ;
	final private Object finalPrivateObject2 = finalPrivateObject1 ;
	public Object[] references ;

	
	// Normally I would define hashCode, but we will not need it here.
	// Do not use an instance of complexClass as a map key.

	public String toString()
	{
	    return "ComplexClass[" + str2 + "]" ;
	}

	public ComplexClass( String name )
	{
	    str1 = name ;
	    str2 = str1 ;
	}

	public static ComplexClass makeComplexClass(String str )
	{
	    return new ComplexClass( str ) ;
	}

	public static ComplexClass makeComplexClassAliasedArray(String str )
	{
	    int num = 5 ;
	    ComplexClass[] classes = new ComplexClass[ num ] ;

	    for (int ctr = 0; ctr<num; ctr++ ) {
		classes[ctr] = makeComplexClass( str + ":member " + ctr ) ;
		if (ctr==0) { // 0th classes references all others
		    classes[ctr].references = new ComplexClass[num] ;
		} else { // others reference only 0th, but allocate
			 // different sizes reference arrays
		    classes[ctr].references = new ComplexClass[ctr] ;
		    classes[ctr].references[0] = classes[0] ;
		}

		// Make 0th class reference the others
		classes[0].references[ctr] = classes[ctr] ;
	    }

	    return classes[0] ;
	}

	public static ComplexClass makeComplexClassGraph()
	{
	    int num = 5 ;
	    ComplexClass[] classes = new ComplexClass[ num ] ;

	    for (int ctr = 0; ctr<num; ctr++ ) {
		classes[ctr] = makeComplexClassAliasedArray(
		    "group " + ctr ) ;
		if (ctr==0) { // 0th classes references all others
		    classes[ctr].references = new ComplexClass[num] ;
		} else { // others reference only 0th, but allocate
			 // different sizes reference arrays
		    classes[ctr].references = new ComplexClass[ctr] ;
		    classes[ctr].references[0] = classes[0] ;
		}

		// Make 0th class reference the others
		classes[0].references[ctr] = classes[ctr] ;
	    }

	    return classes[0] ;
	}
    }

    public  Test makeComplexClass()
    {
	return makeTestSuite( "testComplexClass", new Object[] {
	    "testComplexClassArray", "testComplexClassAliasedArray",
	    "testComplexClassGraph" } ) ;
    }

    public  void testComplexClassArray()
    {
	Object data = ComplexClass.makeComplexClass( "FOO" ) ;
	doStandardTest( data ) ;
    }

    public  void testComplexClassAliasedArray()
    {
	Object data = ComplexClass.makeComplexClassAliasedArray( "BAR" ) ;
	doStandardTest( data ) ;
    }

    public  void testComplexClassGraph()
    {
	Object data = ComplexClass.makeComplexClassGraph( ) ;
	doStandardTest( data ) ;
    }

    private static final int WARMUP_COUNT = 50 ;
    private static final int TEST_COUNT = 75 ;

    private static long theCopyEpoch = 1;

    private void testSimulatedTiming( NonFinalComplexClass obj, int count ) {
	int result = 0 ;
	for (int ctr=0; ctr<count; ctr++) {
	    NonFinalComplexClass res = (NonFinalComplexClass)obj.copy( 
		theCopyEpoch++ ) ;
	    result += res.i1 ;
	}
    }

    private void testSimulatedTiming( String msg, NonFinalComplexClass data ) {

	NonFinalComplexClass result = 
	    (NonFinalComplexClass)data.copy( theCopyEpoch++ ) ;
	result.clear() ;
	data.clear() ; // need to clear the simulated data to avoid 
			 // confusing deep equals check.

        checkDeepEquals( data, result ) ;
	checkNotIdentity( data, result ) ;

	// Now, do the timing tests 
	testSimulatedTiming( data, WARMUP_COUNT ) ;

	long startTime = System.nanoTime() ;
	testSimulatedTiming( data, TEST_COUNT ) ;
	long stopTime = System.nanoTime() ; ;
    
        if (SIMULATED_TIMING) {
            System.out.println( 
                "\nTime per iteration for simulated generated copy method on " 
                + msg + " " 
                + (((float)(stopTime - startTime))/TEST_COUNT)/1000 + " microseconds" ) ;
        }
    }

    public void testSimulatedTimingGraph() {
	NonFinalComplexClass data = NonFinalComplexClass.makeNonFinalComplexClassGraph( ) ;
	testSimulatedTiming( "graph", data ) ;
    }

    private static final int[] TREE_VALUES = { 4, 3, 2, 3 } ;

    public void testSimulatedTimingTree() {
	NonFinalComplexClass data = 
	    NonFinalComplexClass.makeNonFinalComplexClass( TREE_VALUES ) ;
	testSimulatedTiming( "tree(4,3,2,3)", data ) ;
    }

    public void testSimulatedTimingTree1() {
	NonFinalComplexClass data = 
	    NonFinalComplexClass.makeNonFinalComplexClass( 6 ) ;
	testSimulatedTiming( "tree(6)", data ) ;
    }

    public void testSimulatedTimingTree2() {
	NonFinalComplexClass data = 
	    NonFinalComplexClass.makeNonFinalComplexClass( 6, 6 ) ;
	testSimulatedTiming( "tree(6,6)", data ) ;
    }

    public void testSimulatedTimingTree3() {
	NonFinalComplexClass data = 
	    NonFinalComplexClass.makeNonFinalComplexClass( 6, 6, 6 ) ;
	testSimulatedTiming( "tree(6,6,6)", data ) ;
    }

    public void testSimulatedTimingTree4() {
	NonFinalComplexClass data = 
	    NonFinalComplexClass.makeNonFinalComplexClass( 6, 6, 6, 6 ) ;
	testSimulatedTiming( "tree(6,6,6,6)", data ) ;
    }

    public void testSimulatedTimingTree5() {
	NonFinalComplexClass data = 
	    NonFinalComplexClass.makeNonFinalComplexClass( 6, 6, 6, 6, 6 ) ;
	testSimulatedTiming( "tree(6,6,6,6,6)", data ) ;
    }

    private NonFinalComplexClass navigate( NonFinalComplexClass data, int... args ) {
	NonFinalComplexClass current = data ;
	for (int x : args) {
	    Object obj = current.references[x] ;
	    current = (NonFinalComplexClass)obj ;
	}
	return current ;
    }

    public void testSimulatedIsDirty() {
	NonFinalComplexClass data = 
	    NonFinalComplexClass.makeNonFinalComplexClass( 4, 3, 2, 3 ) ;
	long epoch = 1 ;
	assertTrue( data.isDirty( epoch++ ) ) ;
	assertFalse( data.isDirty( epoch++ ) ) ;
	navigate( data, 2, 2, 1 ).op1() ;
	assertTrue( data.isDirty( epoch++ ) ) ;
	assertFalse( data.isDirty( epoch++ ) ) ;
    }

    private void testSimulatedIsDirty( NonFinalComplexClass obj, int count ) {
	boolean result = false ;
	long epoch = 1 ;
	for (int ctr=0; ctr<count; ctr++) {
	    navigate( obj, 2, 2, 1 ).op1() ;
	    result = result || obj.isDirty( epoch++ ) ;
	}
    }

    private void testSimulatedIsDirty( String msg, NonFinalComplexClass data ) {
	// Now, do the timing tests 
	testSimulatedIsDirty( data, WARMUP_COUNT ) ;

	long startTime = System.nanoTime() ;
	testSimulatedIsDirty( data, TEST_COUNT ) ;
	long stopTime = System.nanoTime() ; ;
    
        if (SIMULATED_TIMING) {
            System.out.println( 
                "\nTimer per iteration for simulated generated isDirty method on " 
                + msg + " " 
                + (((float)(stopTime - startTime))/TEST_COUNT)/1000 + " microseconds" ) ;
        }
    }

    public void testSimulatedTimingIsDirty() {
	NonFinalComplexClass data = 
	    NonFinalComplexClass.makeNonFinalComplexClass( 4, 3, 2, 3 ) ;
	testSimulatedIsDirty( "tree(4,3,2,3)", data ) ;
    }


    interface Copyable {
	void clear() ;
	Copyable copy( long copyEpoch ) ;
    }

    private static class NonFinalComplexClass implements java.io.Serializable, Copyable {
	public boolean b1 = false ;
	protected char c1 = 'D' ;
	private byte b2 = (byte)3 ;
	short shrt = (short)-2345 ;
	public int i1 = 273415 ;
	protected long l1 = 38958284 ;
	private float f1 = (float)3.1415926535 ;
	double d1 = 2.718281828 ;
	String str1 ;
	String str2 ;
	private NonFinalComplexClass obj1 = this ;
	private NonFinalComplexClass obj2 = obj1 ;
	public NonFinalComplexClass[] references ;

	// Simulate generated code for copy and dirty check
	private long epoch = 0 ;
	private NonFinalComplexClass copy = null ;
	private boolean isDirty = true ;

	// Simulate a mutator method that changes the state
	public void op1() {
	    isDirty = true ;
	    l1++ ;
	}

	private boolean checkObject( long epoch, Object obj ) {
	    if (obj1 instanceof NonFinalComplexClass) {
		NonFinalComplexClass cc = (NonFinalComplexClass)obj ;
		return cc.isDirty( epoch ) ;
	    } else {
		return false ;
	    }
	}

	public boolean isDirty( long epochArg ) {
	    boolean result = isDirty ;
	    isDirty = false ;

	    if (epochArg == this.epoch) {
		return result ;
	    } else {
		this.epoch = epochArg ;
		result = checkObject( epochArg, obj1 ) || result ;
		result = checkObject( epochArg, obj2 ) || result ;
		if (references != null)
		    for (int ctr=0; ctr<references.length; ctr++)
			result = checkObject( epochArg, references[ctr] ) || result ;
	    }

	    return result ;
	}

	// Don't use this: extra scan
	public void clear() {
	    if (copy != null) {
		copy = null ;
		epoch = 0 ;

		if (this.obj1 != null) 
		    ((Copyable)this.obj1).clear() ;

		if (this.obj2 != null) 
		    ((Copyable)this.obj2).clear() ;

		if (this.references != null) {
		    for (int ctr=0; ctr<this.references.length; ctr++) {
			NonFinalComplexClass next = 
			    (NonFinalComplexClass)this.references[ctr] ;

			if (next != null)
			    next.clear() ;
		    }
		}
	    }
	}

	private Object copyObject( long epoch, Object arg ) {
	    if (arg == null)
		return null ;

	    if (arg instanceof Copyable) {
		return ((Copyable)arg).copy( epoch ) ;
	    } else {
		throw new IllegalArgumentException() ;
	    }
	}

	public Copyable copy( long copyEpoch ) {
	    if ((copy == null) || (copyEpoch != epoch)) {
		copy = new NonFinalComplexClass() ;
		epoch = copyEpoch ;

		copy.b1 = this.b1 ;
		copy.c1 = this.c1 ;
		copy.b2 = this.b2 ;
		copy.shrt = this.shrt ;
		copy.i1 = this.i1 ;
		copy.l1 = this.l1 ;
		copy.f1 = this.f1 ;
		copy.d1 = this.d1 ;
		copy.str1 = this.str1 ;
		copy.str2 = this.str2 ;
		copy.obj1 = (NonFinalComplexClass)copyObject( epoch, this.obj1 ) ;
		copy.obj2 = (NonFinalComplexClass)copyObject( epoch, this.obj2 ) ;

		if (this.references == null) {
		    copy.references = null ;
		} else {
		    copy.references = new NonFinalComplexClass[this.references.length] ;
		    for (int ctr=0; ctr<this.references.length; ctr++) {
			copy.references[ctr] = (NonFinalComplexClass)copyObject( epoch, 
			    this.references[ctr] ) ;
		    }
		}
	    }

	    return copy ;
	}
	// end of simulated generated code
	
	// Normally I would define hashCode, but we will not need it here.
	// Do not use an instance of complexClass as a map key.

	public String toString()
	{
	    return "NonFinalComplexClass[" + str2 + "]" ;
	}

	public NonFinalComplexClass( ) {
	}

	public NonFinalComplexClass( String name )
	{
	    str1 = name ;
	    str2 = str1 ;
	}

	public static NonFinalComplexClass generate( Holder<Integer> index, 
	    NonFinalComplexClass[] leaves, List<Integer> args ) {

	    NonFinalComplexClass result ;

	    if (args.size() == 0) {
		int x = index.content() ;
		result = new NonFinalComplexClass( "leaf" ) ;
		leaves[x] = result ;
		index.content( x+1 ) ; 
	    } else {
		int first = args.get(0) ;
		List<Integer> tail = new LinkedList<Integer>( args ) ;
		tail.remove(0) ;

		result = new NonFinalComplexClass( 
		    "Node(" + args + ")" ) ;
		result.references = new NonFinalComplexClass[first] ;
		for (int ctr=0; ctr<first; ctr++) {
		    NonFinalComplexClass cc = generate( index, leaves, tail ) ;
		    result.references[ctr] = cc ;
		}
	    }

	    return result ;
	}

	public static NonFinalComplexClass makeNonFinalComplexClass( int... args ) {
	    int size = 1 ;
	    List<Integer> sizes = new LinkedList<Integer>() ;
	    for (int x : args) {
		sizes.add( x ) ;
		size *= x ;
	    }

	    Holder<Integer> ccsIndex = new Holder<Integer>( 0 ) ;
	    NonFinalComplexClass[] ccs = new NonFinalComplexClass[size] ;

	    NonFinalComplexClass res = generate( ccsIndex, ccs, sizes ) ;

	    for (int ctr=0; ctr<size; ctr++) {
		ccs[ctr].obj1 = ccs[size-ctr-1] ;
	    }

	    return res ;
	}

	public static NonFinalComplexClass makeNonFinalComplexClass(String str )
	{
	    return new NonFinalComplexClass( str ) ;
	}

	public static NonFinalComplexClass makeNonFinalComplexClassAliasedArray(String str )
	{
	    int num = 5 ;
	    NonFinalComplexClass[] classes = new NonFinalComplexClass[ num ] ;

	    for (int ctr = 0; ctr<num; ctr++ ) {
		classes[ctr] = makeNonFinalComplexClass( str + ":member " + ctr ) ;
		if (ctr==0) { // 0th classes references all others
		    classes[ctr].references = new NonFinalComplexClass[num] ;
		} else { // others reference only 0th, but allocate
			 // different sizes reference arrays
		    classes[ctr].references = new NonFinalComplexClass[ctr] ;
		    classes[ctr].references[0] = classes[0] ;
		}

		// Make 0th class reference the others
		classes[0].references[ctr] = classes[ctr] ;
	    }

	    return classes[0] ;
	}

	public static NonFinalComplexClass makeNonFinalComplexClassGraph()
	{
	    int num = 5 ;
	    NonFinalComplexClass[] classes = new NonFinalComplexClass[ num ] ;

	    for (int ctr = 0; ctr<num; ctr++ ) {
		classes[ctr] = makeNonFinalComplexClassAliasedArray(
		    "group " + ctr ) ;
		if (ctr==0) { // 0th classes references all others
		    classes[ctr].references = new NonFinalComplexClass[num] ;
		} else { // others reference only 0th, but allocate
			 // different sizes reference arrays
		    classes[ctr].references = new NonFinalComplexClass[ctr] ;
		    classes[ctr].references[0] = classes[0] ;
		}

		// Make 0th class reference the others
		classes[0].references[ctr] = classes[ctr] ;
	    }

	    return classes[0] ;
	}
    }

    public  Test makeNonFinalComplexClass()
    {
	TestSuite ts = makeTestSuite( "testNonFinalComplexClass", new Object[] {
	    // First test these for correctnesss
	    makeTest( "testObject" ),
	    makeTest( "testNonFinalComplexClassArray" ),
	    makeTest( "testNonFinalComplexClassAliasedArray" ),
	    makeTest( "testNonFinalComplexClassGraph" ),
	    makeTest( "testNonFinalComplexClassTree" ),

	    // Then test for timing.
	    new TimedTest( makeTest( "testTimedObject" ), 1 ), 
	    new TimedTest( makeTest( "testTimedNonFinalComplexClassArray" ), 1 ), 
	    new TimedTest( makeTest( "testTimedNonFinalComplexClassAliasedArray" ), 1 ),
	    new TimedTest( makeTest( "testTimedNonFinalComplexClassGraph" ), 1 ),  
	    new TimedTest( makeTest( "testTimedNonFinalComplexClassTree" ), 1 ) } 
	) ;

	if (usesCDR()) {
	    TimedTest test = new TimedTest( 
		makeTest( "testTimedNonFinalComplexClassTreeCDRTiming" ), 1 ) ;
	    ts.addTest( test ) ;
	    timedTests.add( test ) ;
	}

	return ts ;
    }

    public  void testNonFinalComplexClassArray()
    {
	Object data = NonFinalComplexClass.makeNonFinalComplexClass( "FOO" ) ;
	doStandardTest( data ) ;
    }

    public  void testNonFinalComplexClassAliasedArray()
    {
	Object data = NonFinalComplexClass.makeNonFinalComplexClassAliasedArray( "BAR" ) ;
	doStandardTest( data ) ;
    }

    public  void testNonFinalComplexClassGraph()
    {
	Object data = NonFinalComplexClass.makeNonFinalComplexClassGraph( ) ;
	doStandardTest( data ) ;
    }

    public  void testNonFinalComplexClassTree()
    {
	Object data = NonFinalComplexClass.makeNonFinalComplexClass( TREE_VALUES) ;
	doStandardTest( data ) ;
    }

    // Run this first to make sure that HotSpot has a chance to optimize
    // the code.
    public void conditionTimingTests() {
	Object data = NonFinalComplexClass.makeNonFinalComplexClass( "FOO" ) ;
	for (int ctr=0; ctr<2000; ctr++)
	    copyObject( data ) ;
    }

    public void testTimedObject() {
	Object obj = new Object() ;
	for (int ctr=0; ctr<REP_COUNT; ctr++)
	    copyObject( obj ) ;
    }

    public  void testTimedNonFinalComplexClassArray()
    {
	Object data = NonFinalComplexClass.makeNonFinalComplexClass( "FOO" ) ;
	for (int ctr=0; ctr<REP_COUNT; ctr++)
	    copyObject( data ) ;
    }

    public  void testTimedNonFinalComplexClassAliasedArray()
    {
	Object data = NonFinalComplexClass.makeNonFinalComplexClassAliasedArray( "BAR" ) ;
	for (int ctr=0; ctr<REP_COUNT; ctr++)
	    copyObject( data ) ;
    }

    public  void testTimedNonFinalComplexClassGraph()
    {
	Object data = NonFinalComplexClass.makeNonFinalComplexClassGraph( ) ;
	for (int ctr=0; ctr<REP_COUNT; ctr++)
	    copyObject( data ) ;
    }

    public  void testTimedNonFinalComplexClassTree()
    {
	Object data = NonFinalComplexClass.makeNonFinalComplexClass( TREE_VALUES ) ;

	for (int ctr=0; ctr<REP_COUNT; ctr++)
	    copyObject( data ) ;
    }

    public  void testTimedNonFinalComplexClassTreeCDRTiming()
    {
	Object data = NonFinalComplexClass.makeNonFinalComplexClass( TREE_VALUES ) ;

	// Set up timing for CDR.
	TimerManager<TimingPoints> tm = orb.getTimerManager() ;
	TimingPoints tp = tm.points() ;
	TimerFactory tf = tm.factory() ;
	TimerEventController controller = tm.controller() ;
	Timer top = tf.makeTimer( "top", "Total time spent for making "
	    + REP_COUNT + " copies of Tree(4 3 2 3)" ) ;

	// Only create the handler after ALL timers are created.
	StatsEventHandler handler = tf.makeStatsEventHandler( 
	    "ComplexClassTreeTestStats" ) ;
	controller.register( handler ) ;

	top.enable() ;
	tp.CDR().enable() ;
	tp.Dynamic().enable() ;
	handler.clear() ;

	controller.enter( top ) ;

	// Run the actual timed test
	for (int ctr=0; ctr<REP_COUNT; ctr++)
	    copyObject( data ) ;

	controller.exit( top ) ;

	top.disable() ;
	tp.CDR().disable() ;
	tp.Dynamic().disable() ;
	
	// Dump out timing results.
	Map<Timer,Statistics> result = handler.stats() ;	
	// TimerUtils.writeHtmlTable( result, "CDRCopyTree4-3-2-3.html",
	    // "Timing Data for making " + REP_COUNT
	    // + " copies of Tree(4, 3, 2, 3) using ORBStream copier" ) ;
    }

    // Use reflection to get the typecodes so we don't need to generate the
    // classes at compile time.

    private TypeCode getTypeCode( String className )
    {
	try {
	    Class cls = Class.forName( "corba.copyobject." + className, true, 
		Thread.currentThread().getContextClassLoader() ) ;
	    Method method = cls.getDeclaredMethod( "type", new Class[0] ) ;
	    return (TypeCode)method.invoke( null, (Object[])null ) ;
	} catch (Exception exc) {
	    RuntimeException rexc = 
		new RuntimeException( "Could not get TypeCode for class " + className ) ;
	    rexc.initCause( exc ) ;
	    throw rexc ;
	}
    }

    public  void testSimpleTypeCode() {
        TypeCode typeCode = getTypeCode( "ColorHelper" ) ;
        doStandardTest(typeCode);
    }

    public  void testUnionTypeCode() {
        TypeCode typeCode = getTypeCode( "ColorCountHelper" ) ;
        doStandardTest(typeCode);
    }

    public  void testRecursiveTypeCode() {
        TypeCode typeCode = getTypeCode( "TwoLevelRecursiveHelper" ) ;
        doStandardTest(typeCode);
    }

    public  void testValuetypeTypeCode() 
    {
        TypeCode typeCode = getTypeCode( "ValueTestHelper" ) ;
        doStandardTest(typeCode);
    }

    public  void testExternalizable()
    {
        Object data = new TestExternalizableImpl("test", 3903);
        doStandardTest(data);
    }

    public  void testExternalizableNonStaticContext()
    {
        Object foo = new FooImpl() ;
        doStandardTest( foo ) ;
        /*
        try {
            FooImpl foo = new FooImpl();
            Object result = copyObject(foo.bar);
            fail("Externalizable with non-static context nested " +
                        "did not raise exception");
        } catch (Exception e) {
            assertTrue(true);
        }
        */
    }

    public  void testRemoteStub() throws Exception {
        RemoteTestImpl impl = new RemoteTestImpl();
	Remote stub = PortableRemoteObject.toStub( impl ) ;
        doRemoteTest( stub );
    }

    public  void testCORBAObject() throws Exception {
        RemoteTestImpl impl = null;
        try {
            impl = new RemoteTestImpl();
            Remote stub = PortableRemoteObject.toStub(impl);

            Object result = copyObject(stub);
            checkDeepEquals(stub, result);
            checkIdentity(stub, result);

        } finally {
            try {
                Util.unexportObject((Remote) impl);
            } catch (Exception e) { }
        }
    }

    public  Test makeInnerClass() {
        return makeTestSuite( "testInnerClass", new Object[] {
            "testInnerClass", "testExtendedInnerClass", "testNestedClass",
            "testLocalInner", "testAnonymousLocalInner"
        } );
    }

    public  void testInnerClass() {
        Outer outer = new Outer();
        doStandardTest(outer.inner);
    }

    public  void testExtendedInnerClass() {
        ExtendedOuter outer = new ExtendedOuter();
        doStandardTest(outer.inner);
    }

    public  void testNestedClass() {
        BankAccount account = new BankAccount(10001, 100);
        doStandardTest(account.perm);
    }

    public  void testLocalInner() {
        HashMap map = new HashMap();
        map.put("one", new Integer(1));
        Object[] objs = new Object[] { "this", map };
        LocalInner local = new LocalInner(objs);
        doStandardTest(local.iterator);
    }

    public  void testAnonymousLocalInner() {
        Object[] objs = { "test", new Integer(1), new BigInteger("9030923") };
        AnonymousInner inner = new AnonymousInner(objs);
        doStandardTest(inner.iterator);
    }

    public  Test makeTransientNonSerializableField() {
        return makeTestSuite("testTransientNonSerializableField", new Object[] {
            "testTransientNonSerializableField1",
            "testTransientNonSerializableField2",
            "testTransientNonSerializableField3",
        } );
    }

    public  void testTransientNonSerializableField1() {
        TestImpl1 impl = new TestImpl1(new NonSerializable1("test"));
        doStandardTest(impl);
    }

    public  void testTransientNonSerializableField2() {
        TestImpl2 impl = new TestImpl2(new NonSerializable2(true), "test");
        doStandardTest(impl);
    }

    public  void testTransientNonSerializableField3() {
        TestImpl3 impl = new TestImpl3(new NonSerializable3(true));
        doStandardTest(impl);
    }

    public  void testClassLoader() throws Exception {

        TestClassLoader cl = new TestClassLoader(
	    Thread.currentThread().getContextClassLoader());
        Bar bar = (Bar) cl.loadClass("foo.BarImpl").newInstance();
        doStandardTest(bar);
    }

    public  Test makeNonSerializableSuperClass()
    {
	return makeTestSuite( "testNonSerializableSuperClass", new String[] {
            "testNonSerializableSuperClass", "testNonSerializableObjectClass"
        } );
    }

    public  void testNonSerializableSuperClass() {
        SubClass1 cls = new SubClass1("test");
        doStandardTest(cls);
    }

    public  void testNonSerializableObjectClass() {
        SubClass2 cls = new SubClass2("test");
        doStandardTest(cls);
    }

    enum Color { RED, BLUE, GREEN } ;

    public void testEnum() {
	doImmutableTest( Color.RED ) ;
    }

    private static class TransientHolder<T> implements Serializable {
	private transient T contents ;

	public TransientHolder( T content ) {
	    contents = content ;
	}

	public T contents() {
	    return contents ;
	}
    }

    private static class TestThread extends Thread {
	private volatile boolean running = true ;

	public void run() {
	    while (running) {
		try { 
		    sleep( 100 ) ;
		} catch (Exception exx) {}
	    }
	}

	public void quit() {
	    running = false ;
	}
    }

    // All of these tests should either throw a 
    // ReflectiveCopyException OR result in an empty
    // TransientHolder.
    public void testTransientThread() throws Throwable {
	TransientHolder<Thread> value = 
	    new TransientHolder<Thread>( new TestThread() ) ;
	value.contents().start() ;
	Thread.sleep( 100 ) ;

	TransientHolder result = TransientHolder.class.cast(
	    copyObject( value, false )) ;
	assert( result.contents() == null ) ;
    }

    public void testTransientThreadGroup() throws Throwable {
	TransientHolder<ThreadGroup> value = 
	    new TransientHolder<ThreadGroup>( new ThreadGroup( "test" )) ;

	TransientHolder result = TransientHolder.class.cast(
	    copyObject( value, false )) ;
	assert( result.contents() == null ) ;
    }

    /*
    public void testTransientProcess() throws Throwable {
	TransientHolder<Process> value = 
	    new TransientHolder<Process>( new Process()) ;

	TransientHolder result = TransientHolder.class.cast(
	    copyObject( value, false )) ;
	assert( result.contents() == null ) ;
    }
    */

    public void testTransientProcessBuilder() throws Throwable {
	TransientHolder<ProcessBuilder> value = 
	    new TransientHolder<ProcessBuilder>( new ProcessBuilder("pwd")) ;

	TransientHolder result = TransientHolder.class.cast(
	    copyObject( value, false )) ;
	assert( result.contents() == null ) ;
    }

    public  void testDynamicProxy() throws Throwable {
        HashMap hashMap = new HashMap();

        Class hashMapClass = hashMap.getClass();

        MapImpl impl = new MapImpl(hashMap);
        Map map =
            (Map) Proxy.newProxyInstance(hashMapClass.getClassLoader(),
                                         hashMapClass.getInterfaces(),
                                         impl);
        map.put("one", Integer.valueOf(1));
        map.put("test", new String("hello"));

        doStandardTest(map);
    }

    public  Test makeCopyObjects()
    {
	return makeTestSuite( "testCopyObjects", new String[] {
            "testCopyObjects", "testCopyObjectsAliased"
        } );
    }

    public void testCopyObjectsNull()
    {
	try {
	    doCopyObjectsTest( null ) ;
	    fail( "copyObjects did not throw NullPointerException for a null argument" ) ;
	} catch (NullPointerException npe) {
	    // success
	}
    }


    public  void testCopyObjects()
    {
        Integer[] array = new Integer[10];
        for (int i = 0; i < array.length; i++) {
            array[i] = i * 10;
        }
        doCopyObjectsTest(array);
    }

    public  void testCopyObjectsAliased()
    {
        BigInteger bigInt = new BigInteger("8932823923");
        Date date = new Date(System.currentTimeMillis());
        String str = "";

        Object[] array = new Object[4];
        array[0] = new Object[] { bigInt, date, str };
        array[1] = new Object[] { new Object[] { array[0] } };
        array[2] = str;
        array[3] = date;

        doCopyObjectsTest(array);
    }
}
