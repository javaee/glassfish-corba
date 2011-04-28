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
//
// Created       : 2005 Oct 05 (Wed) 14:28:37 by Harold Carr.
// Last Modified : 2005 Oct 19 (Wed) 14:01:31 by Harold Carr.
//

package com.sun.corba.se.impl.plugin.hwlb ;

import org.omg.PortableInterceptor.ClientRequestInfo;
import org.omg.PortableInterceptor.ClientRequestInterceptor;
import org.omg.PortableInterceptor.ForwardRequest;
import org.omg.PortableInterceptor.ORBInitializer;
import org.omg.PortableInterceptor.ORBInitInfo;
import org.omg.PortableInterceptor.ORBInitInfoPackage.DuplicateName;
import org.omg.CORBA.SystemException;
import com.sun.corba.se.spi.logging.ORBUtilSystemException;
import com.sun.corba.se.impl.misc.ORBUtility;

public class RetryClientRequestInterceptor
    extends org.omg.CORBA.LocalObject
    implements ORBInitializer, ClientRequestInterceptor
{
    private static final String baseMsg	= 
	RetryClientRequestInterceptor.class.getName();

    private static final String TRANSIENT_REPOSITORY_ID =
	"IDL:omg.org/CORBA/TRANSIENT:1.0";

    private static final String OBJECT_NOT_EXIST_REPOSITORY_ID =
	"IDL:omg.org/CORBA/OBJECT_NOT_EXIST:1.0";

    // The logic causes the initial value to be doubled each time
    // it is used, including the first time.  So the first sleep
    // will be 2 * initialBackoff.
    private static final long INITIAL_BACKOFF_DEFAULT = 500; // 1/2 second
    private static long initialBackoff = INITIAL_BACKOFF_DEFAULT;

    private static final long TRANSIENT_RETRY_TIMEOUT_DEFAULT =
	1000 * 60 * 5; // 5 minutes
    private static long transientRetryTimeout =
	TRANSIENT_RETRY_TIMEOUT_DEFAULT;

    private static boolean debug = true;

    private static class BackoffAndStartTime {
	public long startTime;
	public long backoff;
	BackoffAndStartTime() {
	    backoff = initialBackoff;
	}
    }

    // NOTE: Cannot use slots since they are reset on retry.
    private ThreadLocal backoffAndStartTime =
        new ThreadLocal() {
            protected Object initialValue() {
                return new BackoffAndStartTime();
            }
        };

    private long getStartTime() {
	return ((BackoffAndStartTime)backoffAndStartTime.get()).startTime;
    }

    private void setStartTime(long x) {
	((BackoffAndStartTime)backoffAndStartTime.get()).startTime = x;
    }

    private long getBackoff() {
	return ((BackoffAndStartTime)backoffAndStartTime.get()).backoff;
    }

    private void setBackoff(long x) {
	((BackoffAndStartTime)backoffAndStartTime.get()).backoff = x;
    }

    private void doubleBackoff() {
	setBackoff(getBackoff() * 2);
    }

    ////////////////////////////////////////////////////
    //
    // Application specific
    //

    public static void setInitialBackoff(long x) {
	initialBackoff = x;
    }

    public static long getInitialBackoff() {
	return initialBackoff;
    }

    public static void setTransientRetryTimeout(long x) {
	transientRetryTimeout = x;
    }

    public static long getTransientRetryTimeout() {
	return transientRetryTimeout;
    }

    public static void setDebug(boolean x) {
	debug = x;
    }

    ////////////////////////////////////////////////////
    //
    // Interceptor operations
    //

    public String name() 
    {
	return baseMsg; 
    }

    public void destroy() 
    {
    }

    ////////////////////////////////////////////////////
    //
    // ClientRequestInterceptor
    //

    public void send_request(ClientRequestInfo ri)
    {
    }

    public void send_poll(ClientRequestInfo ri)
    {
    }

    public void receive_reply(ClientRequestInfo ri)
    {
	setBackoff(initialBackoff);
    }

    public void receive_exception(ClientRequestInfo ri)
	throws ForwardRequest
    {
	if (! (isTransientException(ri) || isBadServerIdException(ri))) {
	    setBackoff(initialBackoff);
	    return;
	}

	String msg = 
	    ".receive_exception:" 
	    + " " + ri.received_exception_id()
	    + " " + ri.operation()
	    + ": ";

	if (getBackoff() == initialBackoff) {

	    if (debug) {
		System.out.println(msg + "initializing timer");
	    }

	    setStartTime(System.currentTimeMillis());
	    
	} else if (System.currentTimeMillis() - getStartTime() 
		   >= transientRetryTimeout) {

	    if (debug) {
		System.out.println(msg
				   + "exceeded transientRetryTimeout: "
				   + transientRetryTimeout
				   + " - not retrying");
	    }

	    return;
	}

	doubleBackoff();

	if (debug) {
	    System.out.println(msg + "sleep: " + getBackoff());
	}
	try {
	    Thread.sleep(getBackoff());
	} catch (InterruptedException e) {
	    // Ignore
	}
	if (debug) {
	    System.out.println(msg + "done sleeping");
	}
	if (isTransientException(ri)) {
	    throw new ForwardRequest(ri.effective_target());
	} else if (isBadServerIdException(ri)) {
	    throw new ForwardRequest(ri.target());
	} else {
	    if (debug) {
		System.out.println(msg + "unexpected: " 
				   + ri.received_exception_id());
	    }
	}
    }

    public void receive_other(ClientRequestInfo ri)
    {
	setBackoff(initialBackoff);
    }

    ////////////////////////////////////////////////////
    //
    // ORBInitializer
    //

    public void pre_init(ORBInitInfo info) 
    {
    }

    public void post_init(ORBInitInfo info) 
    {
	try {
	    if (debug) {
		System.out.println(".post_init: registering: " + this);
	    }
	    info.add_client_request_interceptor(this);
	} catch (DuplicateName e) {
	    // REVISIT - LOG AND EXIT
	    if (debug) {
		System.out.println(".post_init: exception: " + e);
	    }
	}
    }

    //////////////////////////////////////////////////
    //
    // Implementation
    //

    private boolean isTransientException(ClientRequestInfo ri)
    {
	return ri.received_exception_id().equals(TRANSIENT_REPOSITORY_ID);
    }

    private boolean isBadServerIdException(ClientRequestInfo ri)
    {
	if (! ri.received_exception_id().equals(OBJECT_NOT_EXIST_REPOSITORY_ID)) {
	    return false;
	}

	SystemException se = 
	    ORBUtility.extractSystemException(ri.received_exception());

	return 
	    se instanceof org.omg.CORBA.OBJECT_NOT_EXIST
	    && se.minor == ORBUtilSystemException.BAD_SERVER_ID;
    }
}

// End of file.

