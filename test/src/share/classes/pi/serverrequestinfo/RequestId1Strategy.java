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

package pi.serverrequestinfo;

import com.sun.corba.se.impl.interceptors.*;
import org.omg.PortableInterceptor.*;

/**
 * Strategy to test request_id.1
 */
public class RequestId1Strategy
    extends InterceptorStrategy
{

    // The id received in receive_request_service_contexts:
    private int requestId;

    public void receive_request_service_contexts (
	SampleServerRequestInterceptor interceptor, ServerRequestInfo ri)
        throws ForwardRequest
    {
	super.receive_request_service_contexts( interceptor, ri );
        
        try {
            this.requestId = ri.request_id();
            log( "receive_request_service_contexts(): request_id = " + 
		requestId );
        }
        catch( Exception ex ) {
            failException( "receive_request_service_contexts", ex );
        }
    }

    public void receive_request (
	SampleServerRequestInterceptor interceptor, ServerRequestInfo ri)
    {
	super.receive_request( interceptor, ri );
        try {
	    testId( "receive_request", ri.request_id() );
        }
        catch( Exception e ) {
            failException( "send_reply", e );
        }
    }

    public void send_reply (
	SampleServerRequestInterceptor interceptor, ServerRequestInfo ri)
    {
	super.send_reply( interceptor, ri );
        try {
	    testId( "send_reply", ri.request_id() );
        }
        catch( Exception e ) {
            failException( "send_reply", e );
        }
    }


    public void send_exception (
	SampleServerRequestInterceptor interceptor, ServerRequestInfo ri) 
	throws ForwardRequest
    {
	super.send_exception( interceptor, ri );
        try {
	    testId( "send_exception", ri.request_id() );
        }
        catch( Exception e ) {
            failException( "send_exception", e );
        }
    }

    public void send_other (
	SampleServerRequestInterceptor interceptor, ServerRequestInfo ri) 
        throws ForwardRequest
    {
	super.send_other( interceptor, ri );
        
        try {
	    testId( "send_other", ri.request_id() );
        }
        catch( Exception e ) {
            failException( "send_other", e );
        }
    }

    /**
     * Tests the given id after send_request
     */
    private void testId( String method, int id ) {
        log( method + "(): request_id = " + id );
	if( id != this.requestId ) {
	    fail( "Request ID in " + method + " did not match request " +
		  "id in " + method );
	}
    }

}
