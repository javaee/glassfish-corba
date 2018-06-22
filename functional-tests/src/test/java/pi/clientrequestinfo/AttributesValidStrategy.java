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

/**
 * Strategy to test operations()
 */
public class AttributesValidStrategy
    extends InterceptorStrategy
{

    // The request count. We should be calling:
    //   0 - sayHello
    //   1 - saySystemException
    //   2 - saySystemException.
    private int count = 0;

    // The most recent operation name received.
    private String operationName;

    public void send_request (
        SampleClientRequestInterceptor interceptor, ClientRequestInfo ri)
        throws ForwardRequest
    {
        try {
            super.send_request( interceptor, ri );

            String validName = (count == 0) ?  
                "sayHello" : "saySystemException";
            this.operationName = ri.operation();
            log( "send_request(): Expected operation name = " + validName );
            log( "send_request(): Actual operation name = " + 
                this.operationName );

            if( !this.operationName.equals( validName ) ) {
                fail( "Operation name not equal to expected name." );
            }

            checkSyncScope( "send_request", ri );

            // Check that within send_request, reply_status 
            // throws BAD_INV_ORDER:
            try {
                short replyStatus = ri.reply_status();
                fail( "send_request(): Should not be able to execute " +
                      "reply_status() here" );
            }
            catch( BAD_INV_ORDER e ) {
                log( "send_request(): Tried reply_status() and received " +
                     "BAD_INV_ORDER (ok)" );
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
            checkOperation( "receive_reply", ri.operation() );
            checkSyncScope( "receive_reply", ri );

            // Check that within receive_reply, reply_status is SUCCESSFUL.
            boolean[] validValues = { true, false, false, false, false };
            checkReplyStatus( "receive_reply", ri, validValues );
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
            checkOperation( "receive_exception", ri.operation() );
            checkSyncScope( "receive_exception", ri );

            // Check that within receive_exception, reply_status is 
            // SYSTEM_EXCEPTION or USER_EXCEPTION:
            boolean[] validValues = { false, true, true, false, false };
            checkReplyStatus( "receive_exception", ri, validValues );
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
            checkOperation( "receive_other", ri.operation() );
            checkSyncScope( "receive_other", ri );

            // Check that within receive_other, reply_status is 
            // SUCCESSFUL, LOCATION_FORWARD, or TRANSPORT_RETRY.
            boolean[] validValues = { true, false, false, true, true };
            checkReplyStatus( "receive_other", ri, validValues );
        }
        catch( Exception ex ) {
            failException( "receive_other", ex );
        }
    }

    private void checkOperation( String method, String opName ) {
        log( method + "(): Actual operation name = " + opName );
        if( !opName.equals( this.operationName ) ) {
            fail( "Operation name in " + method + " not equal to " + 
                  "operation name in send_request()" );
        }
    }

    private void checkSyncScope( String method, ClientRequestInfo ri ) {
        short syncScope = ri.sync_scope();
        log( method + "(): sync_scope() returns " + syncScope );
        if( syncScope != SYNC_WITH_TRANSPORT.value ) {
            fail( "sync_scope() is not SYNC_WITH_TRANSPORT" );
        }
    }

    private void checkReplyStatus( String method, ClientRequestInfo ri,
        boolean[] validValues )
    {
        int i;

        // Describe to user which values are valid:
        String validDesc = "{ ";
        for( i = 0; i < validValues.length; i++ ) {
            validDesc += "" + validValues[i] + " ";
        }
        validDesc += "}";
        log( method + "(): Valid values: " + validDesc );

        short replyStatus = ri.reply_status();
        log( method + "(): Actual value: " + replyStatus );

        if( !validValues[replyStatus] ) {
            fail( method + "(): Not a valid reply status." );
        }
    }

}
