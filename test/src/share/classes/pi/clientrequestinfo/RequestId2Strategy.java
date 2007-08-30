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
 * Strategy to test request_id.2
 * <p>
 * A recursive call will be made.  We will ensure the inner-most call has a 
 * different requestId than the outer-most call.
 */
public class RequestId2Strategy
    extends InterceptorStrategy
{

    // The request id for the outer-most call:
    private int outerId = -1;

    // The request id for the inner-most call:
    private int innerId = -1;

    // The request id count:
    private int count = 0;

    public void send_request (
	SampleClientRequestInterceptor interceptor, ClientRequestInfo ri)
        throws ForwardRequest
    {
	super.send_request( interceptor, ri );

        try {
            if( count == 0 ) {
                outerId = ri.request_id();
                log( "send_request(): outer-most id is " + outerId );
                count++;
            }
            else if( count == 1 ) {
                innerId = ri.request_id();
                log( "send_request(): inner-most id is " + innerId );
                count++;

                if( innerId == outerId ) {
                    fail( "outer and inner requests ids are the same." );
                }
            }
        }
        catch( Exception e ) {
            failException( "send_request", e );
        }
    }

    public void receive_reply(
	SampleClientRequestInterceptor interceptor, ClientRequestInfo ri)
    {
	super.receive_reply( interceptor, ri );

        try {
            // check to make sure inner ids match.
            count--;

            if( count == 1 ) {
                int id = ri.request_id();
                log( "receive_reply(): inner-most id is " + id );
                if( id != innerId ) {
                    fail( "inner id is not the same in receive_reply() as " +
                          "it was in send_request()" );
                }
            }
            else if( count == 0 ) {
                int id = ri.request_id();
                log( "receive_reply(): outer-most id is " + id );
                if( id != outerId ) {
                    fail( "outer id is not the same in receive_reply() as " +
                          "it was in send_request()" );
                }
            }
        }
        catch( Exception e ) {
            failException( "receive_reply", e );
        }
    }

}
