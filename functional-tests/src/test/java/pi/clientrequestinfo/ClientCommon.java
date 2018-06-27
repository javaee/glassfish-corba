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

package pi.clientrequestinfo;

import com.sun.corba.ee.spi.misc.ORBConstants;
import java.io.PrintStream;
import java.util.Properties;
import org.glassfish.pfl.test.JUnitReportHelper;
import org.omg.CORBA.ORB;

/**
 * Common methods for Client implementations in this test to use
 */
abstract public class ClientCommon
{
    // Set from run()
    protected com.sun.corba.ee.spi.orb.ORB orb;
    
    // Set from run()
    public PrintStream out;
    
    // Set from run()
    public PrintStream err;

    // Strategy for current run
    protected InterceptorStrategy interceptorStrategy;

    // Strategy for current run
    protected InvokeStrategy invokeStrategy;

    // The current Client being executed
    public static ClientCommon client;

    JUnitReportHelper helper = new JUnitReportHelper( this.getClass().getName() ) ;
    
    protected void finish() {
        helper.done() ;
    }

    /**
     * Creates a com.sun.corba.ee.spi.orb.ORB and notifies the TestInitializer of its presence
     */
    protected void createORB( String[] args ) {
        // create and initialize the ORB with initializer
        String testInitializer = "pi.clientrequestinfo.TestInitializer";
        Properties props = new Properties() ;
        props.put( "org.omg.CORBA.ORBClass", 
                   System.getProperty("org.omg.CORBA.ORBClass"));
        props.put( ORBConstants.PI_ORB_INITIALIZER_CLASS_PREFIX + 
                   testInitializer, "" );
        this.orb = (com.sun.corba.ee.spi.orb.ORB)ORB.init(args, props);
        TestInitializer.orb = this.orb;
    }

    /**
     * Perform common ClientRequestInfo tests
     */
    protected void testClientRequestInfo() throws Exception {
        out.println();
        out.println( "Running common ClientRequestInfo tests" );
        out.println( "======================================" );

        client = this;

        testRequestId();
        testAttributesValid();
        testOneWay();
        testServiceContext();

        testForwardReference();

        // _REVISIT_ Waiting on new IOR code to be checked in.
        //testEffectiveProfile();

        testException();
    }
    
    /**
     * Clear invocation flags of helloRef and helloRefForward
     */
    abstract protected void clearInvoked() throws Exception;

    /**
     * Invoke the method with the given name on the object
     */
    abstract protected void invokeMethod( String name ) throws Exception;

    /**
     * Return true if the method was invoked
     */
    abstract protected boolean wasInvoked() throws Exception;

    /**
     * Return true if the method was forwarded 
     */
    abstract protected boolean didForward() throws Exception;

    /**
     * Re-resolves all references to eliminate any cached ForwardRequests
     * from the last invocation.
     */
    abstract protected void resolveReferences() throws Exception;

    /**
     * Prepars for a test invocation by setting the interceptor strategy
     * and the invocation and forward objects.
     */
    protected void setParameters( InterceptorStrategy interceptorStrategy,
                                  InvokeStrategy invokeStrategy )
    {
        out.println( "  - Using interceptor strategy " + 
            interceptorStrategy.getClass().getName() );
        out.println( "  - Using invocation strategy " + 
            invokeStrategy.getClass().getName() );
        this.interceptorStrategy = interceptorStrategy;
        this.invokeStrategy = invokeStrategy;
    }

    /**
     * Executes the test case set up with the parameters in setParameters
     */
    protected void runTestCase( String testName ) 
        throws Exception 
    {
        helper.start( testName ) ;

        try {
            out.println( "  - Resolving references." );
            resolveReferences();
            out.println( "  - Executing test " + testName + "." );
            SampleClientRequestInterceptor.strategy = interceptorStrategy;
            invokeStrategy.invoke();
            if( interceptorStrategy.failed ) {
                throw new RuntimeException( interceptorStrategy.failReason );
            }
            helper.pass() ;
        } catch (Exception exc) {
            helper.fail( exc ) ;
            throw exc ;
        }
    }

    /*
     *********************************************************************
     * Test assertions
     *********************************************************************/
    
    /**
     * Tests request_id().  
     */
    protected void testRequestId() 
        throws Exception 
    {
        out.println( "+ Testing request_id()..." );

        // Test request_id is same for request as for reply:
        InterceptorStrategy interceptorStrategy = new RequestId1Strategy();
        InvokeStrategy invokeStrategy = new InvokeVisitAll();
        setParameters( interceptorStrategy, invokeStrategy );
        runTestCase( "request_id.1" );

        // Test request_id is unique for each currently active
        // request/reply sequence:
        interceptorStrategy = new RequestId2Strategy();
        invokeStrategy = new InvokeRecursive();
        setParameters( interceptorStrategy, invokeStrategy );
        runTestCase( "request_id.2" );
    }

    /**
     * Tests various attributes are valid.  Attributes tested:
     *    operation(), sync_scope(), reply_status()
     */
    protected void testAttributesValid() 
        throws Exception 
    {
        out.println( "+ Testing for valid attributes..." );

        InterceptorStrategy interceptorStrategy = 
            new AttributesValidStrategy();
        InvokeStrategy invokeStrategy = new InvokeVisitAll();
        setParameters( interceptorStrategy, invokeStrategy );
        runTestCase( "attributes_valid" );
    }

    /**
     * Tests response_expected() by invoking a oneWay method
     */
    protected void testOneWay() 
        throws Exception 
    {
        out.println( "+ Testing response_expected() with one way..." );

        InterceptorStrategy interceptorStrategy = new OneWayStrategy();
        InvokeStrategy invokeStrategy = new InvokeOneWay();
        setParameters( interceptorStrategy, invokeStrategy );
        runTestCase( "response_expected" );
    }

    /**
     * Tests forward_reference() 
     */
    protected void testForwardReference() 
        throws Exception 
    {
        out.println( "+ Testing forward_reference()..." );

        InterceptorStrategy interceptorStrategy = 
            new ForwardReferenceStrategy();
        InvokeStrategy invokeStrategy = new InvokeVisitAllForward();
        setParameters( interceptorStrategy, invokeStrategy );
        runTestCase( "forward_reference" );
    }

    /**
     * Tests get_request_service_context(), get_reply_service_context().
     * and add_request_service_context().
     */
    protected void testServiceContext() 
        throws Exception
    {
        out.println( "+ Testing {get|add}_*_service_context()..." );

        InterceptorStrategy interceptorStrategy = 
            new ServiceContextStrategy();
        InvokeStrategy invokeStrategy = new InvokeVisitAll();
        setParameters( interceptorStrategy, invokeStrategy );
        runTestCase( "{get|add}_*_service_context" );
    }

    /**
     * Tests effective_profile()
     */
    protected void testEffectiveProfile()
        throws Exception
    {
        out.println( "+ Testing effective_profile()..." );

        InterceptorStrategy interceptorStrategy = 
            new EffectiveProfileStrategy();
        InvokeStrategy invokeStrategy = new InvokeVisitAll();
        setParameters( interceptorStrategy, invokeStrategy );
        runTestCase( "effective_profile" );
    }

    /**
     * Tests received_exception() and received_exception_id()
     */
    protected void testException()
        throws Exception
    {
        out.println( "+ Testing received_exception[_id]()..." );

        InterceptorStrategy interceptorStrategy = 
            new ExceptionStrategy();
        InvokeStrategy invokeStrategy = new InvokeExceptions();
        setParameters( interceptorStrategy, invokeStrategy );
        runTestCase( "received_exception[_id]" );
    }

}

