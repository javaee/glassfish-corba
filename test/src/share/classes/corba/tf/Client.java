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

package corba.tf  ;

import com.sun.corba.se.spi.orbutil.tf.MethodMonitor;
import com.sun.corba.se.spi.orbutil.tf.MethodMonitorFactory;
import com.sun.corba.se.spi.orbutil.tf.MethodMonitorFactoryDefaults;
import com.sun.corba.se.spi.orbutil.tf.MethodMonitorRegistry;
import com.sun.corba.se.spi.orbutil.tf.annotation.InfoMethod;

import corba.framework.TestngRunner;

import java.io.PrintStream;

import org.testng.Assert ;
import org.testng.annotations.Test ;

/**
 * This tests that enums can be correctly deserialized when sent from the JDK ORB (no EnumDesc support)
 * to GlassFish, which supports EnumDesc.  We may also add a config flag to allow testing between two
 * GlassFish ORB instances.
 *
 * Basic test: have server run on JDK ORB (or GF with noEnumDesc configuration), and
 * then see if the client can correctly receive an echoed enum from the server.
 */
public class Client
{
    private PrintStream out ;
    private PrintStream err ;

    public static void main( String[] args )
    {
        TestngRunner runner = new TestngRunner() ;
        runner.registerClass( Client.class ) ;
        runner.run() ;
    }

    public Client() throws Exception {
	this.out = System.out;
	this.err = System.err;
    }

    TestClass getTestClass( boolean isTraced ) {
        // if (isTraced) {
            // return new TestClassImpl_tf() ;
        // } else {
            return new TestClassImpl() ;
        // }
    }

    private void doSimpleTest( boolean isTraced ) {
        TestClass tc = getTestClass( isTraced ) ;
        Assert.assertEquals( tc.add( 10, 10 ), 20 ) ;
        Assert.assertEquals( tc.mult( 10, 10 ), 100 ) ;
    }

    @Test
    public void testSimple() {
        doSimpleTest( false ) ;
    }

    @Test
    public void testSimpleTraced() {
        doSimpleTest( true ) ;
    }

    @Test
    public void testWithTracing() {
        MethodMonitorRegistry.register(A.class, 
            MethodMonitorFactoryDefaults.dprint() );
        MethodMonitorRegistry.register(B.class, 
            MethodMonitorFactoryDefaults.dprint() );
        MethodMonitorRegistry.register(C.class, 
            MethodMonitorFactoryDefaults.dprint() );

        TestClass tc = getTestClass( true ) ;
        System.out.println( "result = " + tc.mult( 10, 10 ) ) ;
    }

    MethodMonitorFactory tracingMonitorFactory = new MethodMonitorFactory() {
        public MethodMonitor create(Class<?> cls) {
            return new MethodMonitorTracingImpl( cls ) ;
        }
    } ;

    @A @B
    public static class TestCombination {
        @A
        void single1( int arg1 ) {
            arg1++ ;
        }

        /*
        private static final MethodMonitor mm = new MethodMonitorTracingImpl(
            TestCombination.class ) ;

        void singl1_instr( int arg1 ) {
            final MethodMonitor __mm = mm  ;
            if (__mm != null) {
                __mm.enter( 1, arg1 )  ;
            }

            try {
            } finally {
                if (__mm != null) {
                    __mm.exit( 1 ) ;
                } 
            }
        }
        */

        @A
        int single2( int arg1 ) { return arg1 ; }

        @InfoMethod
        private void someInfo( int arg1 ) { }

        @A
        int single3( int arg1 ) { someInfo( arg1 ) ; return arg1 ; }

        @A
        int single4( int arg1 ) {
            throw new RuntimeException() ;
        }

        @A
        int call2( int arg1 ) { return call3( arg1 ) ; }

        @A
        int call3( int arg1 ) {
            if (arg1 == 0) {
                throw new RuntimeException() ;
            }

            return arg1 ;
        }
         
        @InfoMethod
        private void inSync() {
        }

        @A
        int call4( int arg1 ) {
            int result ;

            synchronized (this) {
                inSync() ;
                result = 2*arg1 ;
            }

            return result ;
        }

        @A
        void methodA() { methodB() ; }

        @B
        void methodB() { methodC() ; }

        @A
        void methodC() { }
    }

    private static final int SINGLE1 ;
    private static final int SINGLE2 ;
    private static final int SINGLE3 ;
    private static final int SOMEINFO ;
    private static final int SINGLE4 ;
    private static final int CALL2 ;
    private static final int CALL3 ;
    private static final int CALL4 ;
    private static final int METHODA ;
    private static final int METHODB ;
    private static final int METHODC ;
    private static final int INSYNC ;

    private static final MethodMonitor expected =
        new MethodMonitorTracingImpl( TestCombination.class ) ;

    private static final TestCombination tc ;

    static {
        Class<?> cls = TestCombination.class ;
        tc = new TestCombination() ;
        SINGLE1 = MethodMonitorRegistry.getMethodIdentifier( cls, "single1" ) ;
        SINGLE2 = MethodMonitorRegistry.getMethodIdentifier( cls, "single2" ) ;
        SINGLE3 = MethodMonitorRegistry.getMethodIdentifier( cls, "single3" ) ;
        SOMEINFO = MethodMonitorRegistry.getMethodIdentifier( cls, "someInfo" ) ;
        SINGLE4 = MethodMonitorRegistry.getMethodIdentifier( cls, "single4" ) ;
        CALL2 = MethodMonitorRegistry.getMethodIdentifier( cls, "call2" ) ;
        CALL3 = MethodMonitorRegistry.getMethodIdentifier( cls, "call3" ) ;
        CALL4 = MethodMonitorRegistry.getMethodIdentifier( cls, "call4" ) ;
        METHODA = MethodMonitorRegistry.getMethodIdentifier( cls, "methodA" ) ;
        METHODB = MethodMonitorRegistry.getMethodIdentifier( cls, "methodB" ) ;
        METHODC = MethodMonitorRegistry.getMethodIdentifier( cls, "methodC" ) ;
        INSYNC = MethodMonitorRegistry.getMethodIdentifier( cls, "inSync" ) ;
    }

    @Test
    public void singleMethodNoReturn() {
        final int arg = 42 ;

        expected.clear() ;
        expected.enter( SINGLE1, arg ) ;
        expected.exit( SINGLE1 ) ;

        MethodMonitorRegistry.register( A.class, tracingMonitorFactory ) ;

        tc.single1( arg ) ;

        MethodMonitor actual = MethodMonitorRegistry.getMethodMonitorForClass(
            TestCombination.class, A.class ) ;

        Assert.assertEquals( actual, expected );
    }

    @Test
    public void singleMethodReturn() {
        final int arg = 42 ;

        expected.clear() ;
        expected.enter( SINGLE2, arg ) ;
        expected.exit( SINGLE2, arg ) ;

        MethodMonitorRegistry.register( A.class, tracingMonitorFactory ) ;

        tc.single2( arg ) ;

        MethodMonitor actual = MethodMonitorRegistry.getMethodMonitorForClass(
            TestCombination.class, A.class ) ;

        Assert.assertEquals( actual, expected );
    }

    @Test
    public void singleMethodInfoCall() {
        final int arg = 42 ;

        expected.clear() ;
        expected.enter( SINGLE3, arg ) ;
        Object[] args = { arg } ;
        expected.info( args, SINGLE3, SOMEINFO ) ;
        expected.exit( SINGLE3, arg ) ;

        MethodMonitorRegistry.register( A.class, tracingMonitorFactory ) ;

        tc.single3( arg ) ;

        MethodMonitor actual = MethodMonitorRegistry.getMethodMonitorForClass(
            TestCombination.class, A.class ) ;

        Assert.assertEquals( actual, expected );
    }

    @Test
    public void singleMethodThrowsException() {
        final int arg = 42 ;

        expected.clear() ;
        expected.enter( SINGLE4, arg ) ;
        expected.exception( SINGLE4, new RuntimeException() ) ;
        expected.exit( SINGLE4, 0 ) ;

        MethodMonitorRegistry.register( A.class, tracingMonitorFactory ) ;

        try {
            tc.single4( arg ) ;
            Assert.fail( "Unexpected normal completion") ;
        } catch (RuntimeException exc) {
            MethodMonitor actual =
                MethodMonitorRegistry.getMethodMonitorForClass(
                    TestCombination.class, A.class ) ;

            Assert.assertEquals( actual, expected );
        } catch (Exception exc) {
            Assert.fail( "Unexpected exception " + exc ) ;
        }
    }

    @Test
    public void twoCalls() {
        final int arg = 42 ;

        expected.clear() ;
        expected.enter( CALL2, arg ) ;
        expected.enter( CALL3, arg ) ;
        expected.exit( CALL3, arg ) ;
        expected.exit( CALL2, arg ) ;

        MethodMonitorRegistry.register( A.class, tracingMonitorFactory ) ;

        tc.call2( arg ) ;

        MethodMonitor actual = MethodMonitorRegistry.getMethodMonitorForClass(
            TestCombination.class, A.class ) ;

        Assert.assertEquals( actual, expected );
    }

    @Test
    public void twoCallsException() {
        final int arg = 0 ;

        expected.clear() ;
        expected.enter( CALL2, arg ) ;
        expected.enter( CALL3, arg ) ;
        expected.exception( CALL3, new RuntimeException() ) ;
        expected.exit( CALL3, arg ) ;
        expected.exit( CALL2, arg ) ;

        MethodMonitorRegistry.register( A.class, tracingMonitorFactory ) ;

        try {
            tc.call2( arg ) ;
            Assert.fail( "Unexpected normal completion") ;
        } catch (RuntimeException exc) {
            MethodMonitor actual =
                MethodMonitorRegistry.getMethodMonitorForClass(
                    TestCombination.class, A.class ) ;

            Assert.assertEquals( actual, expected );
        } catch (Exception exc) {
            Assert.fail( "Unexpected exception " + exc ) ;
        }
    }

    @Test
    public void testSync() {
        int arg = 23 ;
        expected.clear() ;
        expected.enter( CALL4, arg ) ;
        expected.info( new Object[0], CALL4, INSYNC);
        // expected.info( null, CALL4, INSYNC);
        expected.exit( CALL4, 2*arg ) ;

        MethodMonitorRegistry.register( A.class, tracingMonitorFactory ) ;

        tc.call4( arg ) ;

        MethodMonitor actual = MethodMonitorRegistry.getMethodMonitorForClass(
            TestCombination.class, A.class ) ;

        Assert.assertEquals( actual, expected );
    }

    @Test
    public void twoAnnotations() {
        expected.clear() ;
        expected.enter( METHODA ) ;
        expected.enter( METHODC ) ;
        expected.exit( METHODC ) ;
        expected.exit( METHODA ) ;

        MethodMonitorRegistry.register( A.class, tracingMonitorFactory ) ;

        tc.methodA() ;

        MethodMonitor actual = MethodMonitorRegistry.getMethodMonitorForClass(
            TestCombination.class, A.class ) ;

        Assert.assertEquals( actual, expected );

    }

    // Tests:
    // Two MM annotations, MM1 enabled, MM2 disabled
    // 7. Method (MM1) A calls (MM2) B calls (MM1) C

}
