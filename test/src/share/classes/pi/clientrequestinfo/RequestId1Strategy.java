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

/**
 * Strategy to test request_id.1
 */
public class RequestId1Strategy
    extends InterceptorStrategy
{

    // The id received in send_request:
    private int requestId;

    public void send_request (
	SampleClientRequestInterceptor interceptor, ClientRequestInfo ri)
        throws ForwardRequest
    {
	super.send_request( interceptor, ri );
        
        try {
            this.requestId = ri.request_id();
            log( "send_request(): request_id = " + requestId );
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
	    testId( "receive_reply", ri.request_id() );
        }
        catch( Exception e ) {
            failException( "receive_reply", e );
        }
    }


    public void receive_exception (
	SampleClientRequestInterceptor interceptor, ClientRequestInfo ri) 
	throws ForwardRequest
    {
	super.receive_exception( interceptor, ri );
        try {
	    testId( "receive_exception", ri.request_id() );
        }
        catch( Exception e ) {
            failException( "receive_exception", e );
        }
    }

    public void receive_other (
	SampleClientRequestInterceptor interceptor, ClientRequestInfo ri) 
        throws ForwardRequest
    {
	super.receive_other( interceptor, ri );
        
        try {
	    testId( "receive_other", ri.request_id() );
        }
        catch( Exception e ) {
            failException( "receive_other", e );
        }
    }

    /**
     * Tests the given id after send_request
     */
    private void testId( String method, int id ) {
        log( method + "(): request_id = " + id );
	if( id != this.requestId ) {
	    fail( "Request ID in " + method + " did not match request " +
		  "id in send_request." );
	}
    }

}
