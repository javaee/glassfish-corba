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

package pi.serverrequestinfo;

import com.sun.corba.ee.impl.misc.*;
import com.sun.corba.ee.impl.interceptors.*;
import org.omg.PortableInterceptor.*;
import org.omg.IOP.*;
import ServerRequestInfo.*;
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

    public void receive_request_service_contexts (
        SampleServerRequestInterceptor interceptor, ServerRequestInfo ri)
        throws ForwardRequest
    {
        super.receive_request_service_contexts( interceptor, ri );
         
        try { 
            count++;

            testException( "rrsc", ri );
        }
        catch( Exception ex ) {
            failException( "rrsc", ex );
        }
    }

    public void receive_request (
        SampleServerRequestInterceptor interceptor, ServerRequestInfo ri)
    {
        super.receive_request( interceptor, ri );

        try {
            testException( "receive_request", ri );
        }
        catch( Exception ex ) {
            failException( "receive_request", ex );
        }
    }

    public void send_reply (
        SampleServerRequestInterceptor interceptor, ServerRequestInfo ri)
    {
        super.send_reply( interceptor, ri );

        try {
            testException( "send_reply", ri );
        }
        catch( Exception ex ) {
            failException( "send_reply", ex );
        }
    }


    public void send_exception (
        SampleServerRequestInterceptor interceptor, ServerRequestInfo ri)
        throws ForwardRequest
    {
        super.send_exception( interceptor, ri );

        try {
            testException( "send_exception", ri );
        }
        catch( Exception ex ) {
            failException( "send_exception", ex );
        }
    }

    public void send_other (
        SampleServerRequestInterceptor interceptor, ServerRequestInfo ri)
        throws ForwardRequest
    {
        super.send_other( interceptor, ri );

        try {
            testException( "send_other", ri );
        }
        catch( Exception ex ) {
            failException( "send_other", ex );
        }
    }

    private void testException( String methodName, 
                                ServerRequestInfo ri ) 
    {
        String header = methodName + "(): ";
        if( methodName.equals( "send_exception" ) ) {
            if( count == 2 ) {
                // Called for System Exception:
                // Test send_exception:
                Any sendingException = ri.sending_exception();
                SystemException sysex = ORBUtility.extractSystemException( 
                    sendingException );
                if( !(sysex instanceof IMP_LIMIT) ) {
                    fail( header + "sending_exception() did not return " +
                          "correct SystemException" );
                }
                else {
                    log( header + "sending_exception() returned " +
                         "correct SystemException." );
                }
            }
            else if( count == 3 ) {
                // Called for User Exception:
                // Test send_exception:
                // _REVISIT_ Currently, we do not have access to the 
                // user exception in the Java Language mappings.  When this
                // is fixed, uncomment this test.
                /*
                Any sendingException = ri.sending_exception();

                try {
                    log( header + "Got any with type = " + 
                        sendingException.type().name() );
                }
                catch( org.omg.CORBA.TypeCodePackage.BadKind e ) {
                    log( "" + e );
                }

                SystemException sex = 
                    ORBUtility.extractSystemException( sendingException );
                log( "SystemException: " + sex );
                log( "SystemException: " + sex.getMessage() );

                ExampleException exception = ExampleExceptionHelper.extract( 
                    sendingException );
                if( !exception.reason.equals( "valid" ) ) {
                    fail( header + 
                          "sending_exception() did not return valid " +
                          "ExampleException.  Reason = " + exception.reason );
                }
                else {
                    log( header + "sending_exception() is valid." );
                }
                */
            }
            else {
                fail( header + "sending_exception should not be " +
                      "called when count = " + count );
            }
        }
        else {
            // We should not be able to access received_exception!
            try {
                ri.sending_exception();
                fail( header + 
                      "sending_exception() did not raise BAD_INV_ORDER!" );
            }
            catch( BAD_INV_ORDER e ) {
                log( header + "sending_exception() raised BAD_INV_ORDER (ok)");
            }
        }
    }

}
