/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2000-2007 Sun Microsystems, Inc. All rights reserved.
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

package pi.serverrequestinfo;

import com.sun.corba.se.impl.interceptors.*;
import org.omg.PortableInterceptor.*;
import org.omg.CORBA.*;

/**
 * Strategy to test forward_reference
 * <p>
 * A call will be made, and the interceptor will forward the request to
 * another object to handle it.  forward_reference will be checked when
 * reply status is LOCATION_FORWARD and when it is not LOCATION_FORWARD.
 * <p>
 * Should be called as follows:
 *     count = 1
 *       receive_request_service_contexts
 *       receive_request
 *       send_reply
 *     count = 2
 *       receive_request_service_contexts
 *       receive_request
 *       send_exception
 *     count = 3
 *       receive_request_service_contexts
 *       receive_request
 *       send_other
 *     count = 4, effective_target is now helloRefForward
 *       receive_request_service_contexts
 *       receive_request
 *       send_reply
 */
public class ForwardReferenceStrategy
    extends InterceptorStrategy
{

    private int count = 0;

    public ForwardReferenceStrategy() {
    }

    public void receive_request_service_contexts (
        SampleServerRequestInterceptor interceptor, ServerRequestInfo ri)
        throws ForwardRequest
    {
        super.receive_request_service_contexts( interceptor, ri );

        try {
            count++;
            log( "rrsc(): count is " + count );

            // Try calling forward_reference().  Should fail.
            try {
                ri.forward_reference();
                fail( "rrsc(): forward_reference() did not " +
                      "raise BAD_INV_ORDER" );
            }
            catch( BAD_INV_ORDER e ) {
                log( "rrsc(): forward_reference() " +
                     "raised BAD_INV_ORDER (ok)" );
            }
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
            // Try calling forward_reference().  Should fail.
            try {
                ri.forward_reference();
                fail( "send_reply(): forward_reference() did not " +
                      "raise BAD_INV_ORDER" );
            }                                               
            catch( BAD_INV_ORDER e ) {
                log( "send_reply(): forward_reference() " +
                     "raised BAD_INV_ORDER (ok)" );
            }
        }
        catch( Exception ex ) {
            failException( "send_reply", ex );
        }
    }

    public void send_reply (
        SampleServerRequestInterceptor interceptor, ServerRequestInfo ri)
    {
        super.send_reply( interceptor, ri );

        try {
            // Try calling forward_reference().  Should fail.
            try {
                ri.forward_reference();
                fail( "send_reply(): forward_reference() did not " +
                      "raise BAD_INV_ORDER" );
            }
            catch( BAD_INV_ORDER e ) {
                log( "send_reply(): forward_reference() " +
                     "raised BAD_INV_ORDER (ok)" );
            }
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
            // Try calling forward_reference().  Should fail.
            try {
                ri.forward_reference();
                fail( "send_exception(): forward_reference() did not " +
                      "raise BAD_INV_ORDER" );
            }
            catch( BAD_INV_ORDER e ) {
                log( "send_exception(): forward_reference() " +
                     "raised BAD_INV_ORDER (ok)" );
            }
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
            try {
                // Try calling forward_reference().  Should not fail.
                org.omg.CORBA.Object obj = ri.forward_reference();
                if( TestInitializer.helloRefForward._is_equivalent( obj ) ) {
                    log( "send_other(): forward_reference() is valid." );
                }
                else {
                    fail( "send_other(): forward_reference() is " +
                          "invalid." );
                }
            }
            catch( BAD_INV_ORDER e ) {
                fail( "send_other(): forward_reference() raised " +
                      "BAD_INV_ORDER");
            }
        }
        catch( Exception ex ) {
            failException( "send_other", ex );
        }
    }

}
