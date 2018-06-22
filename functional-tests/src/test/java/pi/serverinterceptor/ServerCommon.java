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

package pi.serverinterceptor;

import com.sun.corba.ee.spi.misc.ORBConstants;
import corba.framework.InternalProcess;
import java.io.PrintStream;
import java.util.Properties;
import org.glassfish.pfl.test.JUnitReportHelper;
import org.omg.CORBA.ORB;

/**
 * Common methods for Server implementations in this test to use
 */
public abstract class ServerCommon
    implements InternalProcess 
{
    // Set from run()
    com.sun.corba.ee.spi.orb.ORB orb;
    
    // Set from run()
    PrintStream out;
    
    // Set from run()
    PrintStream err;

    // True if one of the methods on the Servant was invoked, false if not.
    // This is reset and set, and tested for each invocation tested.
    static boolean servantInvoked = false;
    
    // True if the client is currently waiting for syncWithServer to return
    // or false otherwise.
    static boolean syncing = false;
    
    // The name of the next method to invoke:
    static String nextMethodToInvoke;
    
    // An object for syncWithServer to wait on before returning to the client.
    static final Integer syncObject = new Integer( 0 );
    
    // Constant string to indicate to the client that we are done.
    static final String EXIT_METHOD = "exit";

    // Set to true by syncWithServer if the last invocation resulted in
    // an exception on the client side.
    static boolean exceptionRaised;

    JUnitReportHelper helper = new JUnitReportHelper( this.getClass().getName() ) ;

    protected void finish() {
        helper.done() ;
    }

    /**
     * Creates a com.sun.corba.ee.spi.orb.ORB and notifies the TestInitializer of its presence
     */
    void createORB( String[] args, Properties props ) {
        // create and initialize the ORB with initializer
        String testInitializer = "pi.serverinterceptor.TestInitializer";
        props.put( "org.omg.CORBA.ORBClass",
                   System.getProperty("org.omg.CORBA.ORBClass"));
        props.put( ORBConstants.PI_ORB_INITIALIZER_CLASS_PREFIX +
                   testInitializer, "" );
        // props.put( ORBConstants.DEBUG_PROPERTY, "subcontract" ) ;
        this.orb = (com.sun.corba.ee.spi.orb.ORB)ORB.init(args, props);
        TestInitializer.orb = this.orb;
    }

    /**
     * Perform ServerRequestInterceptor tests for server side
     */
    void testServerInterceptor() throws Exception {
        out.println();
        out.println( "Running common ServerRequestInterceptor tests" );
        out.println( "=============================================" );
        
        // Wait for client to synchronize with server for the first time.
        // We do this because we want testInvocation to wait for the client
        // at the end of an invocation so we know the method finished
        // executing.  However, we do not want to incur the 0.5 second 
        // overhead of waiting for the client both at the begininng and the 
        // end of the testInvocation method.  
        waitForClient();
        
        // No exceptions thrown.  Should call receive_request_service_contexts,
        // receive_request, process sayHello, and then send_reply on all 
        // 3 interceptors in the correct order
        out.println( "+ Testing standard invocation..." );
        testInvocation( "testStandardInvocation",
            SampleServerRequestInterceptor.MODE_NORMAL,
            "rs1rs2rs3rr1rr2rr3sr3sr2sr1", "sayHello", "[Hello1]", false );

        // No exceptions thrown.  Should call receive_request_service_contexts,
        // receive_request, process sayOneWay, and then send_reply on all 
        // 3 interceptors in the correct order
        out.println( "+ Testing oneway invocation..." );
        testInvocation( "testOnewayInvocation",
            SampleServerRequestInterceptor.MODE_NORMAL,
            "rs1rs2rs3rr1rr2rr3sr3sr2sr1", "sayOneway", "[Hello1]", false );
        
        // SYSTEM_EXCEPTION thrown by method.  Should call 
        // receive_request_service_contexts, receive_request,
        // process saySystemException, and then send_exception on all 3
        // interceptors in the correct order.
        out.println( "+ Testing invocation resulting in SYSTEM_EXCEPTION..." );
        testInvocation( "testInvocationResultSystemException",
            SampleServerRequestInterceptor.MODE_NORMAL,
            "rs1rs2rs3rr1rr2rr3se3se2se1", "saySystemException", 
            "[Hello1]", true );

        // USER_EXCEPTION thrown by method.  Should call 
        // receive_request_service_contexts, receive_request,
        // process sayUserException, and then send_exception on all 3
        // interceptors in the correct order.
        // We pass in false for exception expected because it is not
        // the exception we are checking for (which is a SystemException).
        // The check is instead done in the client code during invocation.
        out.println( "+ Testing invocation resulting in USER_EXCEPTION..." );
        testInvocation( "testInvocationResultUserException",
            SampleServerRequestInterceptor.MODE_NORMAL,
            "rs1rs2rs3rr1rr2rr3se3se2se1", "sayUserException", 
            "[Hello1]", false );

        // SYSTEM_EXCEPTION thrown in rrsc for second interceptor.
        // Should result in rrsc being called for 1 and 2, but not
        // 3, no intermediate points invoked, and send_exception
        // called for 1 only.  The method sayHello should never be 
        // invoked.
        out.println( "+ Testing invocation where interceptor #2 " +
            "throws exception in rrsc." );
        testInvocation( "testInvocationInterceptorExceptionRRSC",
            SampleServerRequestInterceptor.MODE_RRSC_SYSTEM_EXCEPTION,
            "rs1rs2se1", "sayHello", "", true );

        // ForwardRequest raised in rrsc for second interceptor.
        // Should result in rrsc being called for 1 and 2, but not
        // 3, no intermediate points invoked, and send_exception
        // called for 1 only.  The method sayHello should be called
        // on the second object.
        out.println( "+ Testing invocation where interceptor #2 " +
            "raises ForwardRequest in rrsc." );
        testInvocation( "testInvocationInterceptorForwardRequestRRSC",
            SampleServerRequestInterceptor.MODE_RRSC_FORWARD_REQUEST,
            "rs1rs2so1rs1rs2rs3rr1rr2rr3sr3sr2sr1", "sayHello", 
            "[Hello1Forward]", false );

        // SYSTEM_EXCEPTION thrown in rr for second interceptor.
        // Should result in rrsc being called for all interceptors,
        // intermediate points rr1 and rr2 but not rr3, and send_exception
        // called for all points.  The method sayHello should never be 
        // invoked.
        out.println( "+ Testing invocation where interceptor #2 " +
            "throws exception in rr." );
        testInvocation( "testInvocationInteceptorExceptionRR",
            SampleServerRequestInterceptor.MODE_RR_SYSTEM_EXCEPTION,
            "rs1rs2rs3rr1rr2se3se2se1", "sayHello", "", true );

        // ForwardRequest raised in rr for second interceptor.
        // Should result in rrsc being called for all interceptors,
        // intermediate points rr1 and rr2 but not rr3, and 
        // send_other for all interceptors.  The method sayHello should 
        // be called on the second object.
        out.println( "+ Testing invocation where interceptor #2 " +
            "raises ForwardRequest in rr." );
        testInvocation( "testInvocationInterceptorForwardRequestRR",
            SampleServerRequestInterceptor.MODE_RR_FORWARD_REQUEST,
            "rs1rs2rs3rr1rr2so3so2so1rs1rs2rs3rr1rr2rr3sr3sr2sr1",
            "sayHello", "[Hello1Forward]", false );

        // SYSTEM_EXCEPTION thrown in sr for second interceptor.
        // Should result in rrsc being called for all interceptors,
        // intermediate points for all, and send_reply for 1 and 2,
        // then send_exception for 3. The method sayHello should have been
        // invoked.
        out.println( "+ Testing invocation where interceptor #2 " +
            "throws exception in sr." );
        testInvocation( "testInvocationInterceptorExceptionSR",
            SampleServerRequestInterceptor.MODE_SR_SYSTEM_EXCEPTION,
            "rs1rs2rs3rr1rr2rr3sr3sr2se1", "sayHello", "[Hello1]", true );

        // SYSTEM_EXCEPTION thrown in se for second interceptor.
        // Should result in rrsc being called for all interceptors,
        // intermediate points for all, and send_exception for 1 and 2,
        // then send_exception for 3. The method sayException should have been
        // invoked.
        out.println( "+ Testing invocation where interceptor #2 " +
            "throws exception in se." );
        testInvocation( "testInvocationInterceptorExceptionSE",
            SampleServerRequestInterceptor.MODE_SE_SYSTEM_EXCEPTION,
            "rs1rs2rs3rr1rr2rr3se3se2se1", "saySystemException", 
            "[Hello1]", true );

        // ForwardRequest thrown in se for second interceptor.
        // Should result in rrsc being called for all interceptors,
        // intermediate points for all, and send_reply for 1 and 2,
        // then send_other for 3. The method sayException should have been
        // invoked.  Then, sayHello is invoked again, on the forwarded
        // object.
        out.println( "+ Testing invocation where interceptor #2 " +
            "throws forward request in se." );
        testInvocation( "testInvocationInterceptorForwardRequestSE",
            SampleServerRequestInterceptor.MODE_SE_FORWARD_REQUEST,
            "rs1rs2rs3rr1rr2rr3se3se2so1rs1rs2rs3rr1rr2rr3se3se2se1", 
            "saySystemException", "[Hello1][Hello1Forward]", true );

        // _REVISIT_ The callCounter should be zero here.  However,
        // when we check the count we have not executed the
        // server interceptor reply points for the final message yet.
        // Since there are 3 interceptors registered the call count
        // is 3.  Any number other than three indicates an unbalanced
        // PICurrent stack.

        // Test call counter (all starting points should be matched by an
        // ending point):
        out.print(
            "- Checking call counter: " +
            SampleServerRequestInterceptor.callCounter + " " );
        if( SampleServerRequestInterceptor.callCounter == 3 ) {
            out.println( "(ok)" );
        }
        else {
            out.println( "(error - should be 3)" );
            throw new RuntimeException( "Call counter was not 3" );
        }
    }

    /** 
     * Tests a standard invocation by instructing the client to
     * resolve a reference to helloServer and make an invocation,
     * recording the interceptor invocation ordering.  Assumes we are
     * already synchronized with the client.
     *
     * @param mode - See SampleServerRequestIntreceptor.testMode for more 
     *     details of mode parameter.  
     * @param correctOrder - See SampleServerRequestInterceptor.
     *     invocationOrder for more details on correctOrder.  
     * @param methodName is either "sayHello" or "sayException"
     * @param correctMethodOrder - See SampleServerRequestInterceptor.
     *     methodOrder for more details on correctMethodOrder.
     * @param exceptionExpected - True if an exception should make its way
     *     to the client or false if not.
     */
    void testInvocation( String name,
                         int mode, 
                         String correctOrder,
                         String methodName,
                         String correctMethodOrder,
                         boolean exceptionExpected )
        throws Exception 
    {
        helper.start( name ) ;

        try {
            // We are already synchronized with the client.
            
            // Prepare for the call:
            nextMethodToInvoke = methodName;
            servantInvoked = false;
            SampleServerRequestInterceptor.invocationOrder = "";
            SampleServerRequestInterceptor.methodOrder = "";
            SampleServerRequestInterceptor.setTestMode( mode );
            
            // Return from syncWithServer() and let the client make the call:
            synchronized( syncObject ) {
                syncObject.notify();
            }

            // If this is a oneway call, wait an extra second to make sure
            // everything went through okay.
            if( methodName.equals( "sayOneway" ) ) {
                out.println( "    - Delaying for oneway..." );
                try {
                    Thread.sleep( 1000 );
                }
                catch( InterruptedException e ) {
                }
                out.println( "    - This should be long enough." );
            }
            
            // Wait for client to synchronize with server again.  Now we know
            // for sure that the method invocation is complete.  Even if the
            // call was a oneway, we can be fairly certain the call has completed
            // since we delay for 0.5 seconds before exiting waitForClient()
            // which should be enough time in most cases.
            waitForClient();
            
            // Examine invocation order to ensure everything was called in the
            // right order.
            //
            // NOTE: This is merely a convenient way to check ordering.
            // Since the specification does not require that the order in which
            // interceptors are registered match the order in which they are
            // invoked, the actual invocationOrder may be different but still
            // valid.  This test may need modification if we change the way
            // we determine the initial invocation order of interceptors.
            String order = SampleServerRequestInterceptor.invocationOrder;
            checkOrder( correctOrder, order );

            // Examine method invocation order to ensure all appropriate methods
            // were called, and in the right order.
            String methodOrder = SampleServerRequestInterceptor.methodOrder;
            checkMethodOrder( correctMethodOrder, methodOrder );
            
            // Determine if an exception was raised:
            out.println( "    - Client-side exception expected: " + 
                exceptionExpected );
            out.println( "    - Client-side exception raised: " + 
                exceptionRaised );
            if( exceptionExpected != exceptionRaised ) {
                throw new RuntimeException( "Method should " + 
                    (!exceptionExpected ? "not" : "") + 
                    " have raised an exception!" );
            }

            helper.pass() ;
        } catch (Exception exc) {
            helper.fail( exc ) ;
        }
    }
    
    /**
     * Waits for the client to sync with the server
     */
    private void waitForClient() {
        out.println( "    - Waiting for client..." );
        while( !syncing ) {
            try {
                Thread.sleep( 100 );
            }
            catch( InterruptedException e ) {
            }
        }
        
        // Leave enough time for the method and interceptors to finish 
        // invoking so we can clear the invocation history:
        try {
            Thread.sleep( 500 );
        }
        catch( InterruptedException e ) {
        }
        out.println( "    - Synchronized with client." );
    }
    
    /**
     * Notifies the client it is time to exit
     */
    void exitClient() {
        out.println( "+ Notifying client it's time to exit..." );
        nextMethodToInvoke = EXIT_METHOD;
        synchronized( syncObject ) {
            syncObject.notify();
        }
    }
    
    /**
     * Checks the invocation order against the correct invocation order,
     * displays some debug output and throws an Exception if they do not
     * match.
     */
    private void checkOrder( String correctOrder, String order ) 
        throws Exception 
    {
        // Because we synchronize with the client before we check the
        // invocation order, all invocations for syncWithServer are
        // present at the end of the actual invocation order.  Additionally,
        // the end of the previous syncWithServer should appear.  We will
        // check for the presence of these as well.
        //
        // Client                      Server
        //   |                           |
        //  [ ]     syncWithServer()     | 
        //  [ ]------------------------>[ ]     rs1 rs2 rs3 rr1 rr2 rr3
        //  [ ]                         [ ]
        //  [ ]     "sayHello"          [ ]   // order cleared here
        //  [ ]<------------------------[ ]     sr3 sr2 sr1
        //  [ ]                          |
        //  [ ]     sayHello()           |    // <important>
        //  [ ]------------------------>[ ]     rs1 rs2 rs3 rr1 rr2 rr3
        //  [ ]                         [ ]
        //  [ ]     "hello there"       [ ]
        //  [ ]<------------------------[ ]     sr3 sr2 sr1
        //  [ ]                          |    // </important>
        //  [ ]     syncWithServer()     |
        //  [ ]------------------------>[ ]     rs1 rs2 rs3 rr1 rr2 rr3
        //  [ ]                         [ ] 
        //  [ ]                         [ ]   // check order here
        //  [ ]                         [ ]   // next test begins soon after.
        //  ...                         ...
        //
        // Notice that at "check order here" our string is as follows:
        // sr3sr2sr1rs1rs2rs3rr1rr2rr3sr3sr2sr1rs1rs2rs3rr1rr2rr3
        // ~~~~~~~~~^^^^^^^^^^^^^^^^^^^^^^^^^^^~~~~~~~~~~~~~~~~~~
        // [  junk ][     important info      ][     junk       ]

        String prependOrder = "sr3sr2sr1";
        String appendedOrder = "rs1rs2rs3rr1rr2rr3";
        correctOrder = prependOrder + correctOrder + appendedOrder;
        
        out.println( "    - Expected invocation order: " + 
                     correctOrder.substring( prependOrder.length(), 
                                             correctOrder.length() - 
                                             appendedOrder.length() ) );
        
        out.println( "    - Actual invocation order: " + 
                     order.substring( prependOrder.length(), 
                                      order.length() -
                                      appendedOrder.length() ) );
        
        if( !order.equals( correctOrder ) ) {
            out.println( "    - MISMATCH.  Exiting." );
            throw new Exception( "Invocation order mismatch." );
        }
    }

    /**
     * Checks the method invocation order against the correct method 
     * invocation order, displays some debug output and throws an Exception 
     * if they do not match.
     */
    private void checkMethodOrder( String correctMethodOrder, 
                                   String methodOrder ) 
        throws Exception 
    {
        out.println( "    - Expected method invocation order: " + 
                     correctMethodOrder );
        
        out.println( "    - Actual method invocation order: " + 
                     methodOrder );
        
        if( !methodOrder.equals( correctMethodOrder ) ) {
            out.println( "    - MISMATCH.  Exiting." );
            throw new Exception( "Method Invocation order mismatch." );
        }
    }

}
