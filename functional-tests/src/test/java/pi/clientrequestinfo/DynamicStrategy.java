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

import org.omg.CORBA.*;
import com.sun.corba.ee.impl.interceptors.*;
import org.omg.PortableInterceptor.*;
import org.omg.Messaging.*;
import org.omg.Dynamic.*;

import ClientRequestInfo.*;

/**
 * Strategy to test DII dynamic operations.  This was originally its own
 * test, but has been moved to this test to simulate a more realistic
 * environment.
 * <p>
 * This strategy will only work with DII calls.  The following methods are
 * tested:
 * <ul>
 *   <li>arguments</li>
 *   <li>exceptions</li>
 *   <li>contexts</li>
 *   <li>operation_context</li>
 *   <li>result</li>
 * </ul>
 */
public class DynamicStrategy
    extends InterceptorStrategy
{
    // The request count. We should be calling:
    //   0 - sayArguments
    //   1 - sayUserException
    //   2 - saySystemException.
    private int count = 0;

    public void send_request (
        SampleClientRequestInterceptor interceptor, ClientRequestInfo ri)
        throws ForwardRequest
    {
        try {
            super.send_request( interceptor, ri );

            if( count == 0 ) {
                // sayArguments was just called.
                testArguments( "send_request", "sayArguments", ri );
                testContexts( "send_request", "sayArguments", ri );

                // Ensure result() is inaccessible.
                try {
                    ri.result();
                    fail( "send_request: Should not have been " +
                        "able to access result()" );
                }
                catch( BAD_INV_ORDER e ) {
                    log( "send_request: Could not access " +
                        "result() (ok)" );
                }
            }
            else if( count == 1 ) {
                // sayUserException was just called.
                testExceptions( "send_request", "sayUserException", ri );
            }

            count++;
        }
        catch( Exception ex ) {
            failException( "send_request", ex );
        }
    }

    public void send_poll (
        SampleClientRequestInterceptor interceptor, ClientRequestInfo ri)
    {
        super.send_poll( interceptor, ri );
        // never executed in our orb.
    }

    public void receive_reply (
        SampleClientRequestInterceptor interceptor, ClientRequestInfo ri)
    {
        try {
            super.receive_reply( interceptor, ri );

            if( count == 1 ) {
                // sayArguments was just called.
                testArguments( "receive_reply", "sayArguments", ri );
                testContexts( "receive_reply", "sayArguments", ri );
                testResult( "receive_reply", "sayArguments", ri );
            }
            else if( count == 2 ) {
                // sayUserException was just called.
                testExceptions( "receive_reply", "sayUserException", ri );
            }
        }
        catch( Exception ex ) {
            failException( "receive_reply", ex );
        }
    }


    public void receive_exception (
        SampleClientRequestInterceptor interceptor, ClientRequestInfo ri) 
        throws ForwardRequest
    {
        try {
            super.receive_exception( interceptor, ri );

            if( count == 1 ) {
                // Ensure arguments() is inaccessible.
                try {
                    ri.arguments();
                    fail( "receive_exception: Should not have been " +
                        "able to access arguments()" );
                }
                catch( BAD_INV_ORDER e ) {
                    log( "receive_exception: Could not access " +
                        "arguments() (ok)" );
                }

                testContexts( "receive_exception", "sayArguments", ri );

                // Ensure result() is inaccessible.
                try {
                    ri.result();
                    fail( "send_request: Should not have been " +
                        "able to access result()" );
                }
                catch( BAD_INV_ORDER e ) {
                    log( "send_request: Could not access " +
                        "result() (ok)" );
                }
            }
            else if( count == 2 ) {
                // sayUserException was just called.
                testExceptions( "receive_exception", "sayUserException", ri );
            }
        }
        catch( Exception ex ) {
            failException( "receive_exception", ex );
        }
    }

    public void receive_other (
        SampleClientRequestInterceptor interceptor, ClientRequestInfo ri) 
        throws ForwardRequest
    {
        try {
            super.receive_other( interceptor, ri );

            if( count == 1 ) {
                // Ensure arguments() is inaccessible.
                try {
                    ri.arguments();
                    fail( "receive_exception: Should not have been " +
                        "able to access arguments()" );
                }
                catch( BAD_INV_ORDER e ) {
                    log( "receive_exception: Could not access " +
                        "arguments() (ok)" );
                }

                testContexts( "receive_other", "sayArguments", ri );

                // Ensure result() is inaccessible.
                try {
                    ri.result();
                    fail( "send_request: Should not have been " +
                        "able to access result()" );
                }
                catch( BAD_INV_ORDER e ) {
                    log( "send_request: Could not access " +
                        "result() (ok)" );
                }
            }
            else if( count == 2 ) {
                // sayUserException was just called.
                testExceptions( "receive_other", "sayUserException", ri );
            }
        }
        catch( Exception ex ) {
            failException( "receive_other", ex );
        }
    }

    // Test that arguments() returns valid values.
    private void testArguments( String interceptionPoint, String methodName,
        ClientRequestInfo ri ) 
        throws Exception
    {
        String header = interceptionPoint + "(): ";
        if( methodName.equals( "sayArguments" ) ) {
            log( header + "Analyzing arguments for " + methodName );
            Parameter[] params = ri.arguments();
            if( params.length != 3 ) {
                fail( header + "sayArguments should have 3 parameters." );
            }
            else {
                Any arg = params[0].argument;
                if( arg == null ) {
                    fail( header + "first argument is null" );
                }
                else {
                    String par = arg.extract_string();
                    if( !par.equals( "one" ) ) {
                        fail( header + "first argument was not \"one\"" );
                    }
                    else {
                        log( header + "first argument is valid." );
                    }
                }

                arg = params[1].argument;
                if( arg == null ) {
                    fail( header + "second argument is null" );
                }
                else {
                    int par = arg.extract_long();
                    if( par != 2 ) {
                        fail( header + "second argument was not \"2\"" );
                    }
                    else {
                        log( header + "second argument is valid." );
                    }
                }

                arg = params[2].argument;
                if( arg == null ) {
                    fail( header + "third argument is null" );
                }
                else {
                    boolean par = arg.extract_boolean();
                    if( !par ) {
                        fail( header + "third argument was not \"true\"" );
                    }
                    else {
                        log( header + "third argument is valid." );
                    }
                }
            }
        }
    }

    // Test that exceptions() returns valid values.
    private void testExceptions( String interceptionPoint, String methodName,
        ClientRequestInfo ri ) 
        throws Exception
    {
        String header = interceptionPoint + "(): ";
        if( methodName.equals( "sayUserException" ) ) {
            log( header + "Analyzing exceptions for " + methodName );
            TypeCode[] excList = ri.exceptions();

            if( excList.length != 1 ) {
                fail( header + methodName + " should throw one exception." );
            }
            else {
                if( excList[0].kind().value() != TCKind._tk_except ) {
                    fail( header + "the TypeCode is not _tk_except." );
                }
                else {
                    String id = excList[0].id();
                    String correctId = ExampleExceptionHelper.id();
                    if( !correctId.equals( id ) ) {
                        fail( header + "the exception id is " + id + 
                            " instead of " + correctId );
                    }
                    else {
                        log( header + "exception id is " + id + " (ok)" );
                    }
                }
            }
        }
    }

    // Tests that contexts() returns valid values.
    private void testContexts( String interceptionPoint, String methodName,
        ClientRequestInfo ri ) 
        throws Exception
    {
        String header = interceptionPoint + "(): ";
        if( methodName.equals( "sayArguments" ) ) {
            log( header + "Analyzing contexts for " + methodName );

            String[] contexts = ri.contexts();
            if( contexts.length != 2 ) {
                fail( header + "sayArguments should have 2 contexts." );
            }
            else {
                if( !contexts[0].equals( "context1" ) ) {
                    fail( header + "expected context1.  got " + contexts[0] );
                }
                else if( !contexts[1].equals( "context2" ) ) {
                    fail( header + "expected context2.  got " + contexts[1] );
                }
                else {
                    log( header + "both contexts are valid." );
                }
            }
        }
        
    }

    // Tests that result() returns a valid value.
    private void testResult( String interceptionPoint, String methodName,
        ClientRequestInfo ri ) 
        throws Exception
    {
        String header = interceptionPoint + "(): ";
        if( methodName.equals( "sayArguments" ) ) {
            Any result = ri.result();

            // Ensure result is "return value"
            if( result == null ) {
                fail( header + "result was null!" );
            }
            else {
                String stringResult = result.extract_string();
                if( !stringResult.equals( "return value" ) ) {
                    fail( header + "result was " + stringResult + " (error)" );
                }
                else {
                    log( header + "result was correct." );
                }
            }
        }
        else if( methodName.equals( "sayUserException" ) ) {
            Any result = ri.result();

            // Ensure result is void:
            if( result.type().kind().value() != TCKind._tk_void ) {
                fail( header + "result was not void (error)" );
            }
            else {
                log( header + "result was void (ok)" );
            }
        }
    }

}
