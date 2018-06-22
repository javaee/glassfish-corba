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

import org.omg.CORBA.*;
import org.omg.PortableServer.*;
import com.sun.corba.ee.impl.interceptors.*;
import org.omg.PortableInterceptor.*;
import org.omg.Messaging.*;

import java.util.*;

/**
 * Strategy to test get_server_policy.
 */
public class GetServerPolicyStrategy
    extends InterceptorStrategy
{

    // The request count. We should be calling:
    //   0 - sayHello
    //   1 - saySystemException
    //   2 - saySystemException.
    private int count = 0;

    private static final int INVALID_POLICY_TYPE = 101;

    public void receive_request_service_contexts (
        SampleServerRequestInterceptor interceptor, ServerRequestInfo ri)
        throws org.omg.PortableInterceptor.ForwardRequest
    {
        try {
            super.receive_request_service_contexts( interceptor, ri );
            count++;
            checkGetServerPolicy( "rrsc", ri );
        }
        catch( Exception ex ) {
            failException( "rrsc", ex );
        }
    }

    public void receive_request (
        SampleServerRequestInterceptor interceptor, ServerRequestInfo ri)
    {
        try {
            super.receive_request( interceptor, ri );
            checkGetServerPolicy( "rr", ri );
        }
        catch( Exception ex ) {
            failException( "receive_request", ex );
        }
    }

    public void send_reply (
        SampleServerRequestInterceptor interceptor, ServerRequestInfo ri)
    {
        try {
            super.send_reply( interceptor, ri );
            checkGetServerPolicy( "sr", ri );
        }
        catch( Exception ex ) {
            failException( "send_reply", ex );
        }
    }

    public void send_exception (
        SampleServerRequestInterceptor interceptor, ServerRequestInfo ri)
        throws org.omg.PortableInterceptor.ForwardRequest
    {
        try {
            super.send_exception( interceptor, ri );
            checkGetServerPolicy( "se", ri );
        }
        catch( Exception ex ) {
            failException( "send_exception", ex );
        }
    }

    public void send_other (
        SampleServerRequestInterceptor interceptor, ServerRequestInfo ri)
        throws org.omg.PortableInterceptor.ForwardRequest
    {
        try {
            super.send_other( interceptor, ri );
            checkGetServerPolicy( "so", ri );
        }
        catch( Exception ex ) {
            failException( "send_other", ex );
        }
    }

    private void checkGetServerPolicy( String method, ServerRequestInfo ri ) 
        throws Exception
    {
        // Try an invalid policy:
        try {
            Policy policy = ri.get_server_policy( INVALID_POLICY_TYPE );
            if( policy != null ) {
                fail( method + "(): get_server_policy( INVALID ) " +
                    "is not null!" );
            }
            else {
                log( method + "(): get_server_policy( INVALID ) " +
                    "is null (ok)" );
            }
        }
        catch( INV_POLICY e ) {
            fail( method + "(): get_server_policy( INVALID ) " +
                "throws INV_POLICY (error - should return null)" );
        }
        catch( Exception e ) {
            e.printStackTrace();
            fail( method + "(): get_server_policy( INVALID ) " +
                "throws incorrect exception: " + e );
        }

        // Try a standard policy:
        try {
            Policy policy = ri.get_server_policy( 
                ID_UNIQUENESS_POLICY_ID.value );
            if( policy instanceof IdUniquenessPolicy ) {
                log( method + "(): get_server_policy( STANDARD ) " +
                    "returns correct policy." );
            }
            else {
                fail( method + "(): get_server_policy( STANDARD ) " +
                    "returns incorrect policy: " + 
                    policy.getClass().getName() );
            }
        }
        catch( INV_POLICY e ) {
            fail( method + "(): get_server_policy( STANDARD ) " +
                "throws INV_POLICY" );
        }
        catch( Exception e ) {
            e.printStackTrace();
            fail( method + "(): get_server_policy( STANDARD ) " +
                "throws incorrect exception: " + e );
        }

        // Try a custom policy:
        try {
            Policy policy = ri.get_server_policy( 100 );
            if( policy instanceof PolicyHundred ) {
                log( method + "(): get_server_policy( CUSTOM ) " +
                    "returns correct policy." );
            }
            else {
                fail( method + "(): get_server_policy( CUSTOM ) " +
                    "returns incorrect policy: " + 
                    policy.getClass().getName() );
            }
        }
        catch( INV_POLICY e ) {
            fail( method + "(): get_server_policy( CUSTOM ) " +
                "throws INV_POLICY" );
        }
        catch( Exception e ) {
            e.printStackTrace();
            fail( method + "(): get_server_policy( CUSTOM ) " +
                "throws incorrect exception: " + e );
        }
    }

}
