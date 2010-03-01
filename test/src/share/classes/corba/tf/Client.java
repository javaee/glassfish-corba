/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2003-2010 Sun Microsystems, Inc. All rights reserved.
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

package corba.tf  ;

import com.sun.corba.se.spi.orbutil.tf.MethodMonitorFactoryDefaults;
import com.sun.corba.se.spi.orbutil.tf.MethodMonitorRegistry;
import corba.framework.TestngRunner;
import java.io.PrintStream;
import java.lang.annotation.Annotation;

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

    @A @B @C
    public static class TestCombination {
         
    }
}
