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

package pi.clientrequestinfo;

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
 *       send_request
 *       receive_reply
 *     count = 2
 *       send_request
 *       receive_exception
 *     count = 3
 *       send_request
 *       receive_other
 *     count = 4, effective_target is now helloRefForward
 *       send_request
 *       receive_exception
 */
public class ForwardReferenceStrategy
    extends InterceptorStrategy
{

    private int count = 0;

    public ForwardReferenceStrategy() {
    }

    public void send_request (
	SampleClientRequestInterceptor interceptor, ClientRequestInfo ri)
        throws ForwardRequest
    {
	super.send_request( interceptor, ri );

        try {
            count++;
            log( "send_request(): count is " + count );

            // Try calling forward_reference().  Should fail.
            try {
                ri.forward_reference();
                fail( "send_request(): forward_reference() did not " +
                      "raise BAD_INV_ORDER" );
            }
            catch( BAD_INV_ORDER e ) {
                log( "send_request(): forward_reference() " +
                     "raised BAD_INV_ORDER (ok)" );
            }

	    // Try target()
	    org.omg.CORBA.Object obj = ri.target();
	    org.omg.CORBA.Object correctObject;
	    correctObject = TestInitializer.helloRef;

	    if( correctObject._is_equivalent( obj ) ) {
		log( "send_request(): target() is valid." );
	    }
	    else {
		fail( "send_request(): target() is invalid." );
	    }

            // Try effective_target()
            obj = ri.effective_target();
            if( count < 4 ) {
                // This is before we are invoking on helloRefForward.
                correctObject = TestInitializer.helloRef;
            }
            else {
                // This is after we are invoking on helloRefForward.
                correctObject = TestInitializer.helloRefForward;
            }
            if( correctObject._is_equivalent( obj ) ) {
                log( "send_request(): effective_target() is valid." );
            }
            else {
                fail( "send_request(): effective_target() is invalid." );
            }
        }
        catch( Exception ex ) {
            failException( "send_request", ex );
        }
    }

    public void receive_reply(
	SampleClientRequestInterceptor interceptor, ClientRequestInfo ri)
    {
	super.receive_reply( interceptor, ri );

        try {
            // Try calling forward_reference().  Should fail.
            try {
                ri.forward_reference();
                fail( "receive_reply(): forward_reference() did not " +
                      "raise BAD_INV_ORDER" );
            }
            catch( BAD_INV_ORDER e ) {
                log( "receive_reply(): forward_reference() " +
                     "raised BAD_INV_ORDER (ok)" );
            }

            // Try target()
            org.omg.CORBA.Object obj = ri.target();
            if( TestInitializer.helloRef._is_equivalent( obj ) ) {
                log( "send_request(): target() is valid." );
            }
            else {
                fail( "send_request(): target() is invalid." );
            }

            // Try effective_target()
            obj = ri.effective_target();
            if( TestInitializer.helloRef._is_equivalent( obj ) ) {
                log( "send_request(): effective_target() is valid." );
            }
            else {
                fail( "send_request(): effective_target() is invalid." );
            }
        }
        catch( Exception ex ) {
            failException( "send_request", ex );
        }
    }

    public void receive_exception (
	SampleClientRequestInterceptor interceptor, ClientRequestInfo ri)
	throws ForwardRequest
    {
        super.receive_exception( interceptor, ri );

        try {
            // Try calling forward_reference().  Should fail.
            try {
                ri.forward_reference();
                fail( "receive_exception(): forward_reference() did not " +
                      "raise BAD_INV_ORDER" );
            }
            catch( BAD_INV_ORDER e ) {
                log( "receive_exception(): forward_reference() " +
                     "raised BAD_INV_ORDER (ok)" );
            }

            // Try target()
            org.omg.CORBA.Object obj = ri.target();
            org.omg.CORBA.Object correctObject;
	    correctObject = TestInitializer.helloRef;

            if( correctObject._is_equivalent( obj ) ) {
                log( "receive_exception(): target() is valid." );
            }
            else {
                fail( "receive_exception(): target() is invalid." );
            }

            // Try effective_target()
            obj = ri.effective_target();
            if( count < 4 ) {
                // This is before we are invoking on helloRefForward.
                correctObject = TestInitializer.helloRef;
            }
            else {
                // This is after we are invoking on helloRefForward.
                correctObject = TestInitializer.helloRefForward;
            }
            if( correctObject._is_equivalent( obj ) ) {
                log( "receive_exception(): effective_target() is valid." );
            }
            else {
                fail( "receive_exception(): effective_target() is invalid." );
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
	super.receive_other( interceptor, ri );

        try {
            try {
                // Try calling forward_reference().  Should not fail.
                org.omg.CORBA.Object obj = ri.forward_reference();
                if( TestInitializer.helloRefForward._is_equivalent( obj ) ) {
                    log( "receive_other(): forward_reference() is valid." );
                }
                else {
                    fail( "receive_other(): forward_reference() is " +
                          "invalid." );
                }
            }
            catch( BAD_INV_ORDER e ) {
                fail( "receive_other(): forward_reference() raised " +
                      "BAD_INV_ORDER");
            }

            // Try target()
            org.omg.CORBA.Object obj = ri.target();
            if( TestInitializer.helloRef._is_equivalent( obj ) ) {
                log( "receive_other(): target() is valid." );
            }
            else {
                fail( "receive_other(): target() is invalid." );
            }

            // Try effective_target()
            obj = ri.effective_target();
            if( TestInitializer.helloRef._is_equivalent( obj ) ) {
                log( "receive_other(): effective_target() is valid." );
            }
            else {
                fail( "receive_other(): effective_target() is invalid." );
            }
        }
        catch( Exception ex ) {
            failException( "receive_other", ex );
        }
    }

}
