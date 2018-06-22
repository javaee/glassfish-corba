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

package pi.orbinit;

import corba.framework.InternalProcess;
import java.io.PrintStream;
import java.util.Hashtable;
import java.util.Properties;
import org.glassfish.pfl.test.JUnitReportHelper;
import org.omg.CORBA.ORB;
import org.omg.IOP.CodecFactory;

public abstract class ClientCommon
    implements InternalProcess 
{
    JUnitReportHelper helper = new JUnitReportHelper( this.getClass().getName() ) ;

    // Set from run()
    private ORB orb;
    
    // Set from run()
    private PrintStream out;
    
    // Set from run()
    private PrintStream err;
    
    private CodecFactory codecFactory;

    public void run( Properties environment, String args[], PrintStream out,
                     PrintStream err, Hashtable extra) 
        throws Exception
    {
        out.println( "Client" );
        out.println( "======" );

        this.out = out;
        this.err = err;
        ClientTestInitializer.out = this.out;

        this.orb = createORB( args );
        ClientTestInitializer.orb = this.orb;

        try {
            // Test ORBInitializer
            testORBInitializer();

            // Test ORBInitInfo
            testORBInitInfo();

            // Test destroy
            testDestroy();
        } finally {
            helper.done() ;
        }
    }

    /**
     * Perform ORBInitializer-related tests
     */
    private void testORBInitializer() {
        helper.start( "testORBInitializer" ) ;

        try {
            out.println();
            out.println( "Testing ORBInitializer" );
            out.println( "======================" );

            // Ensure the test initializer was initialized appropriately.
            out.println( "Verifying testInitializer: " );
            if( !ClientTestInitializer.initializedAppropriately() ) {
                throw new RuntimeException( 
                    "ClientTestInitializer not initialized appropriately." );
            }
            out.println( "  - initialized appropriately. (ok)" );

            if( !ClientTestInitializer.post_post_init() ) {
                throw new RuntimeException( 
                    "ORBInitInfo allowed access after post_init." );
            }
            helper.pass() ;
        } catch (RuntimeException exc) {
            helper.fail( exc ) ;
            throw exc ;
        }
    }

    /**
     * Perform ORBInitInfo-related tests
     */
    private void testORBInitInfo() {
        helper.start( "testORBInitInfo" ) ;

        try {
            // Any tests on ORBInitInfo are actually done inside the 
            // ORBInitializer.  At this point, we just analyze the results of
            // tests that have already run.

            out.println();
            out.println( "Testing ORBInitInfo" );
            out.println( "===================" );

            // Analyze resolve_initial_references results
            out.println( ClientTestInitializer.resolveInitialReferencesResults );
            if( !ClientTestInitializer.passResolveInitialReferences ) {
                throw new RuntimeException( 
                    "resolve_initial_references not functioning properly." );
            }
            else if( !ClientTestInitializer.passResolveInitialReferencesInvalid ) {
                throw new RuntimeException( 
                    "resolve_initial_references not raising InvalidName." );
            }

            // Analyze add_*_interceptor
            out.println( "Testing pre_init add interceptor..." );
            out.println( ClientTestInitializer.preAddInterceptorResult );
            if( !ClientTestInitializer.preAddInterceptorPass ) {
                throw new RuntimeException(
                    "pre_init add interceptor test failed." );
            }

            out.println( "Testing post_init add interceptor..." );
            out.println( ClientTestInitializer.postAddInterceptorResult );
            if( !ClientTestInitializer.postAddInterceptorPass ) {
                throw new RuntimeException(
                    "post_init add interceptor test failed." );
            }

            // Analyze get/set_slot test results
            out.println( "Testing get/set slot from within ORBInitializer..." );
            out.println( ClientTestInitializer.getSetSlotResult );
            if( !ClientTestInitializer.getSetSlotPass ) {
                throw new RuntimeException( "get/set slot test failed." );
            }
 
            helper.pass() ;
        } catch (RuntimeException exc) {
            helper.fail( exc ) ;
            throw exc ;
        }
    }

    /**
     * Test that destroy is called on all interceptors.
     */
    private void testDestroy() 
        throws Exception
    {
        helper.start( "testDestroy" ) ;

        try {
            out.println();
            out.println( "Testing destroy functionality" );
            out.println( "=============================" );

            out.println( "Checking destroy counts before calling destroy..." );
            int clientCount = SampleClientRequestInterceptor.destroyCount;
            int serverCount = SampleServerRequestInterceptor.destroyCount;
            int iorCount = SampleIORInterceptor.destroyCount;
            checkDestroyCount( "Client", 0, clientCount );
            checkDestroyCount( "Server", 0, serverCount );
            checkDestroyCount( "IOR", 0, iorCount );

            out.println( "Calling ORB.destroy..." );
            orb.destroy();

            out.println( 
                "Checking that interceptors' destroy methods were called." );
            clientCount = SampleClientRequestInterceptor.destroyCount;
            serverCount = SampleServerRequestInterceptor.destroyCount;
            iorCount = SampleIORInterceptor.destroyCount;

            checkDestroyCount( "Client", 6, clientCount );
            checkDestroyCount( "Server", 2, serverCount );
            checkDestroyCount( "IOR", 2, iorCount );
            helper.pass() ;
        } catch (Exception exc) {
            helper.fail( exc ) ;
            throw exc ;
        }
    }

    /**
     * Checks that a single interceptor passed the destroy test
     */
    private void checkDestroyCount( String name, int expected, int actual ) 
        throws Exception
    {
        out.println( "* " + name + " interceptor: Expected " + expected + 
            " destroys.  Received " + actual + "." );
        if( expected != actual ) {
            throw new RuntimeException( 
                "Incorrect number of destroys called." );
        }
    }

    abstract protected ORB createORB( String[] args );
}
