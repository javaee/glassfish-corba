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

package pi.clientrequestinfo;

import com.sun.corba.ee.impl.misc.*;
import com.sun.corba.ee.impl.interceptors.*;
import org.omg.PortableInterceptor.*;
import org.omg.IOP.*;
import ClientRequestInfo.*;
import org.omg.CORBA.*;

/**
 * Strategy to test received_exception() and received_exception_id().
 * Expected invocation order:
 *     count = 1, send_request, receive_reply
 *     count = 2, send_request, receive_exception (SystemException)
 *     count = 3, send_request, receive_exception (UserException)
 *     count = 4, send_request, receive_other
 * All points are checked in order to assure received_exception() 
 * can only be called in the receive_exception interception point.
 */
public class ExceptionStrategy
    extends InterceptorStrategy
{
    
    private int count = 0;


    // True if this test is being run in DII mode.  In DII mode, all 
    // UserException tests are skipped.
    //
    // _REVISIT_ Remove this special mode once UserExceptions work properly
    // with DII.
    boolean diiMode;

    public ExceptionStrategy() {
        this( false );
    }

    public ExceptionStrategy( boolean diiMode ) {
        this.diiMode = diiMode;
    }

    public void send_request (
        SampleClientRequestInterceptor interceptor, ClientRequestInfo ri)
        throws ForwardRequest
    {
        super.send_request( interceptor, ri );
        
        try { 
            count++;

            testException( "send_request", ri );
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
        super.receive_reply( interceptor, ri );

        try {
            testException( "receive_reply", ri );
        }
        catch( Exception ex ) {
            failException( "receive_reply", ex );
        }
    }


    public void receive_exception (
        SampleClientRequestInterceptor interceptor, ClientRequestInfo ri) 
        throws ForwardRequest
    {
        super.receive_exception( interceptor, ri );

        try {
            testException( "receive_exception", ri );
        }
        catch( Exception ex ) {
            failException( "receive_exception", ex );
        }
    }

    public void receive_other (
        SampleClientRequestInterceptor interceptor, ClientRequestInfo ri) 
        throws ForwardRequest
    {
        super.receive_other( interceptor, ri );

        try {
            testException( "receive_other", ri );
        }
        catch( Exception ex ) {
            failException( "receive_other", ex );
        }
    }

    private void testException( String methodName, 
                                ClientRequestInfo ri ) 
    {
        String header = methodName + "(): ";
        if( methodName.equals( "receive_exception" ) ) {
            if( count == 2 ) {
                // Called for System Exception:
                // Test received_exception:
                Any receivedException = ri.received_exception();
                SystemException sysex = ORBUtility.extractSystemException( 
                    receivedException );
                if( !(sysex instanceof UNKNOWN) ) {
                    fail( header + "received_exception() did not return " +
                          "correct SystemException" );
                }
                else {
                    log( header + "received_exception() returned " +
                         "correct SystemException." );
                }
                
                // Test received_exception_id:
                String exceptionId = ri.received_exception_id();

                log( header + "exceptionId for SystemException is: " + 
                    exceptionId );
                
                if( exceptionId.indexOf( "UNKNOWN" ) == -1 ) {
                    fail( header + "exceptionId incorrect!" );
                }
            }
            else if( count == 3 ) {
                // Skip this test in DII mode:
                if( diiMode ) {
                    log( header + "skipping UserException test for DII" );    
                }
                else {
                    // Called for User Exception:
                    // Test received_exception:
                    Any receivedException = ri.received_exception();

                    ExampleException exception = 
                        ExampleExceptionHelper.extract( receivedException );
                    if( !exception.reason.equals( "valid" ) ) {
                        fail( header + 
                              "received_exception() did not return valid " +
                              "ExampleException" );
                    }
                    else {
                        log( header + "received_exception() is valid." );
                    }

                    // Test received_exception_id:
                    String exceptionId = ri.received_exception_id();

                    log( header + "exceptionId for UserException is: " + 
                        exceptionId );
                    
                    if( exceptionId.indexOf( "ExampleException" ) == -1 ) {
                        fail( header + "exceptionId incorrect!" );
                    }
                }
            }
            else {
                fail( header + "receive_exception should not be " +
                      "called when count = " + count );
            }
        }
        else {
            // We should not be able to access received_exception!
            try {
                ri.received_exception();
                fail( header + 
                      "received_exception() did not raise BAD_INV_ORDER!" );
            }
            catch( BAD_INV_ORDER e ) {
                log( header + "received_exception() raised BAD_INV_ORDER (ok)");
            }
            
            // We should not be able to access received_exception_id!
            try {
                ri.received_exception_id();
                fail( header + 
                      "received_exception_id() did not raise BAD_INV_ORDER!" );
            }
            catch( BAD_INV_ORDER e ) {
                log( header + 
                     "received_exception_id() raised BAD_INV_ORDER (ok)");
            }
        }
    }

}
