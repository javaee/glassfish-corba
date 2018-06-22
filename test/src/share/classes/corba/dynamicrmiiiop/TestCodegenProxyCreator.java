/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright (c) 1997-2018 Oracle and/or its affiliates. All rights reserved.
 *
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common Development
 * and Distribution License("CDDL") (collectively, the "License").  You
 * may not use this file except in compliance with the License.  You can
 * obtain a copy of the License at
 * https://oss.oracle.com/licenses/CDDL+GPL-1.1
 * or LICENSE.txt.  See the License for the specific
 * language governing permissions and limitations under the License.
 *
 * When distributing the software, include this License Header Notice in each
 * file and include the License file at LICENSE.txt.
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

package corba.dynamicrmiiiop ;

import junit.framework.TestCase ;
import junit.framework.Test ;
import junit.framework.TestSuite ;

import java.lang.reflect.Method ;
import java.lang.reflect.InvocationTargetException ;

import java.security.ProtectionDomain ;

import com.sun.corba.ee.impl.presentation.rmi.codegen.CodegenProxyCreator ;

/** Test for the codegen-based ProxyCreator.  
 * Steps to test:
 * <OL>
 * <LI>Create a class with a method Object invoke( int methodNumber, Object[] args ).
 * The invoke method simply forwards the invocation to the corresponding method.
 * <LI>Create a suitable remote interface for testing.
 * <LI>Implement the remote interface with an implementation that provides for testing
 * correct behavior.
 * <LI>Be able to run the tests on 2 implementations of the interface.
 * <LI>Create the second implementation of the interface by constructing a proxy
 * that delegates to the first implementation.
 * <LI>Test that the proxy extends the correct base class
 * <LI>Test that the proxy implements the expected interface
 * </OL>
 * We need to test the following kinds of calls:
 * <OL>
 * <LI>Method that echos single arg of primitive type to result (8 tests)
 * <LI>Method that takes one int, returns void, asserts expected value received
 * <LI>Method that takes one int, return void, throws checked exception for certain value
 * <LI>Method that returns one int, no args, certain result expected
 * <LI>Method that takes no args, returns no results, causes checked side effect
 * <LI>Method that takes no args, return no results, always throws checked exception
 * <LI>Method that takes two ints, returns int, computes the sum
 * <LI>Method that takes two Strings, returns string, computes concat
 * <LI>Method that takes int, String, returns string, computes concat
 * <LI>Method that takes String, int, returns string, computes concat
 * <LI>Method that takes String, int, String, returns string, computes concat
 * <LI>Method that takes int, String, int, return string, computes concat
 * <OL>
 */
public class TestCodegenProxyCreator extends TestCase {
    private static final boolean DEBUG = false ;

    public static class TestException extends Exception {
    }

    public static interface TestInterface {
        boolean echo( boolean arg ) ;
        char echo( char arg ) ;
        byte echo( byte arg ) ;
        short echo( short arg ) ;
        int echo( int arg ) ;
        long echo( long arg ) ;
        float echo( float arg ) ;
        double echo( double arg ) ;
        Object echo( Object arg ) ;
        Integer echo( Integer arg ) ;
        void throwIf42( int arg ) throws TestException ; 
        int return42() ;
        void alwaysThrow() throws TestException ;       
        void sideEffect() ;
        boolean sideEffectCalled() ;
        int sum( int arg1, int arg2 ) ;
        String concat( String arg1, String arg2 ) ;
        String concat( int arg1, String arg2 ) ;
        String concat( String arg1, int arg2 ) ;
        String concat( String arg1, int arg2, String arg3 ) ;
        String concat( int arg1, String arg2, int arg3 ) ;
    }

    public static class TestInterfaceImpl implements TestInterface {
        private boolean sideEffectCalledFlag = false ;

        public boolean sideEffectCalled() 
        {
            return sideEffectCalledFlag ;
        }

        public boolean echo( boolean arg ) 
        {
            return arg ;
        }

        public char echo( char arg ) 
        {
            return arg ;
        }

        public byte echo( byte arg ) 
        {
            return arg ;
        }

        public short echo( short arg ) 
        {
            return arg ;
        }

        public int echo( int arg ) 
        {
            return arg ;
        }

        public long echo( long arg ) 
        {
            return arg ;
        }

        public float echo( float arg ) 
        {
            return arg ;
        }

        public double echo( double arg ) 
        {
            return arg ;
        }

        public Object echo( Object arg )
        {
            return arg ;
        }

        public Integer echo( Integer arg ) 
        {
            return arg ;
        }

        public void throwIf42( int arg ) throws TestException
        {
            if (arg == 42)
                throw new TestException() ;
        }

        public int return42() 
        {
            return 42 ;
        }

        public void alwaysThrow() throws TestException 
        {
            throw new TestException() ;
        }

        public void sideEffect() 
        {
            sideEffectCalledFlag = true ;
        }

        public int sum( int arg1, int arg2 ) 
        {
            return arg1 + arg2 ;
        }

        public String concat( String arg1, String arg2 ) 
        {
            return arg1 + arg2 ;
        }

        public String concat( int arg1, String arg2 ) 
        {
            return arg1 + arg2 ;
        }

        public String concat( String arg1, int arg2 ) 
        {
            return arg1 + arg2 ;
        }

        public String concat( String arg1, int arg2, String arg3 ) 
        {
            return arg1 + arg2 + arg3 ;
        }

        public String concat( int arg1, String arg2, int arg3 ) 
        {
            return arg1 + arg2 + arg3 ;
        }
    }

    public static abstract class TestInterfaceImplTester extends TestCase {

        public abstract TestInterface getTestInterface() ;
    
        public TestInterfaceImplTester()
        {
            super() ;
        }

        public TestInterfaceImplTester( String name )
        {
            super( name ) ;
        }

        public void testBooleanEcho()
        {
            assertTrue( getTestInterface().echo( true ) ) ;
        }

        public void testCharEcho()
        {
            assertEquals( getTestInterface().echo( 'A' ), 'A' ) ;
        }

        public void testByteEcho()
        {
            assertEquals( getTestInterface().echo( (byte)23 ), (byte)23 ) ;
        }

        public void testShortEcho()
        {
            assertEquals( getTestInterface().echo( (short)23 ), (short)23 ) ;
        }

        public void testIntEcho() 
        {
            assertEquals( getTestInterface().echo( 3214 ), 3214 ) ;
        }

        public void testLongEcho()
        {
            assertEquals( getTestInterface().echo( 12345678901234L ), 
                12345678901234L ) ;
        }

        public void testFloatEcho()
        {
            assertEquals( getTestInterface().echo( 1.23f ), 1.23f, .00001f ) ;
        }

        public void testDoubleEcho()
        {
            assertEquals( getTestInterface().echo( 1.23 ), 1.23, .00001 ) ;
        }

        public void testObjectEcho()
        {
            Object obj = new Object() ;
            assertEquals( getTestInterface().echo( obj ), obj ) ;
        }

        public void testIntegerEcho() 
        {
            Integer num = new Integer( 43 ) ;
            assertEquals( getTestInterface().echo( num ), num ) ;
        }

        public void testThrowIf42Throw()
        {
            try {
                getTestInterface().throwIf42( 42 ) ;
                fail( "Call completed without exception" ) ;
            } catch (TestException exc) {
                // This is the expected result
            } catch (Throwable thr) {
                fail( "Unexpected exception " + thr ) ;
            }
        }

        public void testThrowIf42NoThrow()
        {
            try {
                getTestInterface().throwIf42( 24 ) ;
            } catch (Throwable thr) {
                fail( "Unexpected exception " + thr ) ;
            }
        }

        public void testReturn42()
        {
            assertEquals( getTestInterface().return42(), 42 ) ;
        }

        public void testAlwaysThrow()
        {
            try {
                getTestInterface().alwaysThrow() ;
                fail( "Call completed without exception" ) ;
            } catch (TestException exc) {
                // This is the expected result
            } catch (Throwable thr) {
                fail( "Unexpected exception " + thr ) ;
            }
        }

        public void testSideEffect()
        {
            getTestInterface().sideEffect() ;
            assertTrue( getTestInterface().sideEffectCalled() ) ;
        }

        public void testSum()
        {
            assertEquals( getTestInterface().sum( 237, 479 ), 237+479 ) ;
        }

        public void testConcatSS()
        {
            assertEquals( getTestInterface().concat( "Another ", "Test" ),
                "Another Test" ) ;
        }

        public void testConcatIS()
        {
            assertEquals( getTestInterface().concat( 1, "Test" ), "1Test" ) ;
        }

        public void testConcatSI()
        {
            assertEquals( getTestInterface().concat( "Test", 1 ), "Test1" ) ;
        }

        public void testConcatSIS()
        {
            assertEquals( getTestInterface().concat( "Test", 1, " Another" ), 
                "Test1 Another" ) ;
        }

        public void testConcatISI()
        {
            assertEquals( getTestInterface().concat( 1, "Test", 2 ), 
                "1Test2" ) ;
        }
    }

    public static class SimpleTestSuite extends TestInterfaceImplTester {
        private TestInterface object ;

        public SimpleTestSuite( String name )
        {
            super( name ) ;
            object = new TestInterfaceImpl() ;
        }
        public SimpleTestSuite()
        {
            super() ;
            object = new TestInterfaceImpl() ;
        }

        public TestInterface getTestInterface() 
        {
            return object ;
        }
    }

    public static class ProxyTestSuite extends TestInterfaceImplTester {
        private static Class proxyClass = null ;
        private static Method[] methods = null ;
        private TestInterface object ;

        public static class TestBase {
            Method[] methods = null ;
            TestInterface ti = null ;

            public Object selfAsBaseClass() {
                return this ;
            }

            public void initialize( Method[] methods, TestInterface ti ) 
            {
                this.methods = methods ;
                this.ti = ti ;
            }

            public Object invoke( int methodNumber, 
                Object[] args ) throws Throwable
            {
                try {
                    return methods[methodNumber].invoke( ti, args ) ;
                } catch (InvocationTargetException ite) {
                    throw ite.getCause() ;
                }
            }
        }

        public ProxyTestSuite( String name ) 
        {
            super( name ) ;
            init() ;
        }

        public ProxyTestSuite()
        {
            super() ;
            init() ;
        }

        private void init()
        {
            TestInterface ti = new TestInterfaceImpl() ;
            
            // Make sure we only try to create the proxy class once!
            if (proxyClass == null) {
                // Set up proxy for ti: this is really the main part of the test
                Class baseClass = TestBase.class ;
                Class interfaceClass = TestInterface.class ;
                Class[] interfaces = new Class[] { interfaceClass } ;
                methods = interfaceClass.getDeclaredMethods() ;

                ProtectionDomain pd = this.getClass().getProtectionDomain() ;
                ClassLoader loader = this.getClass().getClassLoader() ;

                CodegenProxyCreator pc = 
                    new CodegenProxyCreator( 
                        "corba.dynamicrmiiiop.TestInterfaceProxy", 
                        baseClass, interfaces, methods ) ;

                proxyClass = pc.create( pd, loader, DEBUG, System.out ) ;
            }

            try {
                TestBase base = (TestBase)proxyClass.newInstance() ;
                base.initialize( methods, ti ) ;
                object = (TestInterface)base ;
            } catch (Exception exc) {
                exc.printStackTrace() ;
                fail( "Could not create proxy" ) ;
            }
        }

        public TestInterface getTestInterface() 
        {
            return object ;
        }
    }

    public void testDummy()
    {
        // Dummy test to avoid Junit complaints.
    }

    public static Test suite() 
    {
        return new TestSuite( TestCodegenProxyCreator.class ) ;
    }
}
