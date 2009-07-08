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

package pi.clientinterceptor;

import org.omg.CORBA.*;

import org.omg.PortableInterceptor.ForwardRequest;
import org.omg.PortableInterceptor.ClientRequestInfo;
import org.omg.PortableInterceptor.ClientRequestInterceptor;
import org.omg.PortableInterceptor.RequestInfo;

//import ORBInitTest.*;

/**
 * Sample ClientRequestInterceptor for use in testing
 */
public class SampleClientRequestInterceptor 
    extends org.omg.CORBA.LocalObject 
    implements ClientRequestInterceptor
{
    // This string is modified from within this class and from Client.java.
    // It keeps track of which method was invoked when by appending 
    // two-letter codes in succession.  The following codes are used
    // <name> represents the name of the interceptor invoked.  This is
    // used to check order of interceptor invocation.
    //
    //  sr<name> = send_request
    //  sp<name> = send_poll
    //  rr<name> = receive_reply
    //  re<name> = receive_exception
    //  ro<name> = receive_other
    // For example, sr1sr2rr2rr1 would indicate a normal invocation.
    public static String invocationOrder = "";
    
    // This attribute is set by Client.java to indicate how this interceptor
    // should behave.  There are a predetermined set of behavior values:
    //   MODE_NORMAL - All interceptors exit without throwing an Exception
    //   MODE_SYSTEM_EXCEPTION - Interceptors 1 and 3 return normally, 
    //     while interceptor 2 throws a SYSTEM_EXCEPTION during send_request.
    //   MODE_FORWARD_REQUEST - Interceptors 1 and 3 return normally,
    //     while interceptor 2 throws a ForwardRequest during send_request.
    //   MODE_RECEIVE_REPLY_EXCEPTION - Interceptors 3 and 1 return normally,
    //     while interceptor 2 throws a SystemException during receive_reply.
    //   MODE_RECEIVE_EXCEPTION_FORWARD - Interceptors 3 and 1 return
    //     normally, while interceptor 2 throws a ForwardRequest during
    //     receive_exception.
    //   MODE_RECEIVE_OTHER_EXCEPTION - Interceptors 3 and 1 return
    //     normally, while interceptor 2 throws a SystemException during
    //     receive_other.
    public static int testMode;
    
    public static final int MODE_NORMAL = 0;
    public static final int MODE_SYSTEM_EXCEPTION = 1;
    public static final int MODE_FORWARD_REQUEST = 2;
    public static final int MODE_RECEIVE_REPLY_EXCEPTION = 3;
    public static final int MODE_RECEIVE_EXCEPTION_FORWARD = 4;
    public static final int MODE_RECEIVE_OTHER_EXCEPTION = 5;
    
    private String name;

    // Counter to make sure each start is matched by an end
    public static int callCounter = 0;

    public static boolean enabled = false;

    public static boolean printPointEntryFlag = false;

    private void printPointEntry( String message, RequestInfo ri )
    {
	if (printPointEntryFlag) {
	    System.out.println(message +
			       " " + ri.request_id() +
			       " " + ri.operation() +
			       " " + callCounter);
	}
    }

    public SampleClientRequestInterceptor( String name ) {
	this.name = name;
    }

    public String name() {
	return name;
    }

    public void destroy() {
    }

    public void send_request (ClientRequestInfo ri) 
        throws ForwardRequest 
    {
	// Count all calls (and print entry), not just test ones,
	// to make sure all ORB internal calls are balanced.
	callCounter++;	// Starting point - add
	printPointEntry("send_request", ri);

	if( !enabled ) return;

        // Log that we did a send_request on this interceptor so we can
        // verify invocation order was correct in test.
        invocationOrder += "sr" + name;
        
        // Only exhibit behavior if we are the initial object, not the
        // forwarded object.
        if( name.equals( "2" ) && 
            !TestInitializer.helloRefForward._is_equivalent( 
            ri.effective_target() ) )
        {
            if( testMode == MODE_SYSTEM_EXCEPTION ) {
                // If we are the second interceptor, it is our turn to
                // throw a SystemException here.

		// Since this starting point is throwing an exception
		// an ending point will not be called.  Therefore,
		// explicitly decrement the call counter.
		callCounter--;
                throw new UNKNOWN( "Valid Test Result" );
            }
            else if( testMode == MODE_FORWARD_REQUEST ) {
                // If we are the second interceptor, it is our turn to
                // throw a ForwardRequest here.

		// Since this starting point is throwing an exception
		// an ending point will not be called.  Therefore,
		// explicitly decrement the call counter.
		callCounter--;
                throw new ForwardRequest( TestInitializer.helloRefForward );
            }
        }
    }

    public void send_poll (ClientRequestInfo ri) 
    {
	callCounter++;	// Starting point - add
	printPointEntry("send_poll", ri);

	if( !enabled ) return;

        // Log that we did a send_poll on this interceptor so we can
        // verify invocation order was correct in test.
        invocationOrder += "sp" + name;
    }

    public void receive_reply (ClientRequestInfo ri) 
    {
	// Ending points have the print/call statements reverse intentionally.
	printPointEntry("receive_reply", ri);
	callCounter--; 	// Ending point - subtracm

	if( !enabled ) return;

        // Log that we did a receive_reply on this interceptor so we can
        // verify invocation order was correct in test.
        invocationOrder += "rr" + name;

        // Only exhibit behavior if we are the initial object, not the
        // forwarded object.
        if( name.equals( "2" ) && 
            !TestInitializer.helloRefForward._is_equivalent( 
            ri.effective_target() ) )
        {
            if( testMode == MODE_RECEIVE_REPLY_EXCEPTION ) {
                // If we are the second interceptor, it is our turn to
                // throw a SystemException here.
                throw new UNKNOWN( "Valid Test Result" );
            }
        }
    }

    public void receive_exception (ClientRequestInfo ri) 
        throws ForwardRequest
    {
	printPointEntry("receive_exception", ri);
	callCounter--; 	// Ending point - subtract

	if( !enabled ) return;

        // Log that we did a receive_exception on this interceptor so we can
        // verify invocation order was correct in test.
        invocationOrder += "re" + name;
        
        // Only exhibit behavior if we are the initial object, not the
        // forwarded object.
        if( name.equals( "2" ) && 
            !TestInitializer.helloRefForward._is_equivalent( 
            ri.effective_target() ) )
        {
            if( testMode == MODE_RECEIVE_EXCEPTION_FORWARD ) {
                // If we are the second interceptor, it is our turn to
                // throw a ForwardRequest here.
                
	        throw new ForwardRequest( TestInitializer.helloRefForward );
            }
        }
    }

    public void receive_other (ClientRequestInfo ri) 
        throws ForwardRequest 
    {
	printPointEntry("receive_other", ri);
	callCounter--; 	// Ending point - subtract

	if( !enabled ) return;

        // Log that we did a receive_other on this interceptor so we can
        // verify invocation order was correct in test.
        invocationOrder += "ro" + name;
        
        // Only exhibit behavior if we are the initial object, not the
        // forwarded object.
        if( name.equals( "2" ) && 
            !TestInitializer.helloRefForward._is_equivalent( 
            ri.effective_target() ) )
        {
            if( testMode == MODE_RECEIVE_OTHER_EXCEPTION ) {
                // If we are the second interceptor, it is our turn to
                // throw a SystemException here.
                throw new UNKNOWN( "Valid Test Result" );
            }
        }
    }
}
