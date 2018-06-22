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

import org.omg.PortableInterceptor.ForwardRequest;
import org.omg.PortableInterceptor.ServerRequestInfo;
import org.omg.PortableInterceptor.ServerRequestInterceptor;

/**
 * Sample ServerRequestInterceptor for use in testing.  This interceptor
 * is dynamically configurable via an InterceptorStrategy.  It assumes
 * three interceptors have been registered, and most operations will be
 * performed on interceptor number 2 and only if the target() is not
 * helloRefForward.
 */
public class SampleServerRequestInterceptor 
    extends org.omg.CORBA.LocalObject 
    implements ServerRequestInterceptor
{
    // Valid message for exceptions
    public static final String VALID_MESSAGE = "Valid test result.";

    // The dyanmic strategy that will be used for this round.
    public static InterceptorStrategy strategy = null;
    
    // The name of this interceptor
    private String name;

    // Ensure this call was intercepted.  This is set to true when at
    // least one interceptor was called, and is reset before the server
    // requests an invocation from the client.  This helps detect a faulty
    // test run.
    public static boolean intercepted = false;

    // True if enabled, false if all interception points must 
    // return immediately.
    public static boolean enabled = false;

    // Selective enabling and disabling of interception points.  These
    // are only applicable if enabled is true:
    public static boolean receiveRequestServiceContextsEnabled = true;
    public static boolean receiveRequestEnabled = true;
    public static boolean sendReplyEnabled = true;
    public static boolean sendExceptionEnabled = true;
    public static boolean sendOtherEnabled = true;

    // Special flags to override the strategy behavior of this interceptor.

    // Throw ForwardRequest during receive_exception so that receive_other
    // is called.
    public static boolean exceptionRedirectToOther = false;

    // Allow interceptors to be invoked for forwarded object as well.
    public static boolean invokeOnForwardedObject = false;

    // Normally, _is_a invocations are not recorded.  This is because _is_a
    // is invoked often by RMI lookup code.  If this flag is set,
    // _is_a invocations are recorded at most once.  This is useful for
    // when we actually want to record interception on the _is_a "special op".
    static boolean dontIgnoreIsA = false;
    

    // Records the method invocation order
    public static String methodOrder = "";

    private static int invokeCount = 0;

    public SampleServerRequestInterceptor( String name ) {
        this.name = name;
    }

    public String name() {
        return name;
    }

    public void destroy() {
    }

    public void receive_request_service_contexts( ServerRequestInfo ri )
        throws ForwardRequest 
    {
        // Only execute if the interceptor is enabled, this interception
        // point is enabled, we are the second interceptor, and we are 
        // executing on hello, not helloForward.
        if( ri.operation().equals( "syncWithServer" ) ) return;
        if( !dontIgnoreIsA && ri.operation().equals( "_is_a" ) ) {
            if( name.equals( "1" ) ) {
                System.out.println( 
                    "    - Interceptor: Ingoring _is_a call..." );
            }
            return;
        }
        if( !enabled ) return;
        if( !receiveRequestServiceContextsEnabled ) return;
        if( !name.equals( "2" ) ) return;

        intercepted = true;
        strategy.receive_request_service_contexts( this, ri );
    }

    public void receive_request (ServerRequestInfo ri) 
        throws ForwardRequest
    {
        // Only execute if the interceptor is enabled, this interception
        // point is enabled, we are the second interceptor, and we are 
        // executing on hello, not helloForward.
        if( ri.operation().equals( "syncWithServer" ) ) return;
        if( !dontIgnoreIsA && ri.operation().equals( "_is_a" ) ) return;
        if( !enabled ) return;
        if( !receiveRequestEnabled ) return;
        if( !name.equals( "2" ) ) return;

        strategy.receive_request( this, ri );
    }

    public void send_reply (ServerRequestInfo ri) {
        // Only execute if the interceptor is enabled, this interception
        // point is enabled, we are the second interceptor, and we are 
        // executing on hello, not helloForward.
        if( ri.operation().equals( "syncWithServer" ) ) return;
        if( !dontIgnoreIsA && ri.operation().equals( "_is_a" ) ) return;
        if( !enabled ) return;
        if( !sendReplyEnabled ) return;
        if( !name.equals( "2" ) ) return;

        strategy.send_reply( this, ri );
    }

    public void send_exception (ServerRequestInfo ri) 
        throws ForwardRequest
    {
        // Only execute if the interceptor is enabled, this interception
        // point is enabled, we are the second interceptor, and we are 
        // executing on hello, not helloForward.
        if( ri.operation().equals( "syncWithServer" ) ) return;
        if( !dontIgnoreIsA && ri.operation().equals( "_is_a" ) ) return;
        if( !enabled ) return;
        if( !sendExceptionEnabled ) return;
        if( exceptionRedirectToOther && name.equals( "3" ) ) {
            exceptionRedirectToOther = false;
            throw new ForwardRequest( TestInitializer.helloRefForward );
        }
        if( !name.equals( "2" ) ) return;

        strategy.send_exception( this, ri );
    }

    public void send_other (ServerRequestInfo ri) 
        throws ForwardRequest 
    {
        // Only execute if the interceptor is enabled, this interception
        // point is enabled, we are the second interceptor, and we are 
        // executing on hello, not helloForward.
        if( ri.operation().equals( "syncWithServer" ) ) return;
        if( !dontIgnoreIsA && ri.operation().equals( "_is_a" ) ) return;
        if( !enabled ) return;
        if( !sendOtherEnabled ) return;
        if( !name.equals( "2" ) ) return;

        strategy.send_other( this, ri );

        // Make sure we do not invoke any more strategy methods.
        // This will effectively ignore invoking interceptors on forwards.
        if( !invokeOnForwardedObject ) {
            enabled = false;
        }
    }

}
