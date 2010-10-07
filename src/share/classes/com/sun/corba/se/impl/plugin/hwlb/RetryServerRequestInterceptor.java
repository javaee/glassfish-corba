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
// Created       : 2005 Jul 01 (Fri) 13:36:46 by Harold Carr.
//

package com.sun.corba.se.impl.plugin.hwlb ;

import java.util.Properties;

import org.omg.CORBA.ORB;
import org.omg.CORBA.TRANSIENT;
import org.omg.PortableInterceptor.ORBInitializer;
import org.omg.PortableInterceptor.ORBInitInfo;
import org.omg.PortableInterceptor.ORBInitInfoPackage.DuplicateName;
import org.omg.PortableInterceptor.ServerRequestInterceptor;
import org.omg.PortableInterceptor.ServerRequestInfo;

/**
 * @author Harold Carr
 */
public class RetryServerRequestInterceptor
    extends  org.omg.CORBA.LocalObject
    implements ORBInitializer, ServerRequestInterceptor
{
    private static final String baseMsg = 
	RetryServerRequestInterceptor.class.getName();

    private static boolean rejectingRequests = false;

    private static boolean debug = true;

    ////////////////////////////////////////////////////
    //
    // Application specific
    //

    public static boolean getRejectingRequests() 
    {
	return rejectingRequests;
    }

    public static void setRejectingRequests(boolean x)
    {
	rejectingRequests = x;
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
    // ServerRequestInterceptor
    //

    public void receive_request_service_contexts(ServerRequestInfo ri)
    {
	if (rejectingRequests) {
	    if (debug) {
		System.out.println(baseMsg 
				   + ".receive_request_service_contexts:" 
				   + " rejecting request: "
				   + ri.operation());
	    }
	    throw new TRANSIENT();
	}
	if (debug) {
	    System.out.println(baseMsg
			       + ".receive_request_service_contexts:"
			       + " accepting request: "
			       + ri.operation());
	}
    }

    public void receive_request(ServerRequestInfo ri)
    {
    }

    public void send_reply(ServerRequestInfo ri)
    {
    }

    public void send_exception(ServerRequestInfo ri)
    {
    }

    public void send_other(ServerRequestInfo ri)
    {
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
	    info.add_server_request_interceptor(this);
	} catch (DuplicateName e) {
	    // REVISIT - LOG AND EXIT
	    if (debug) {
		System.out.println(".post_init: exception: " + e);
	    }
	}
    }
}

// End of file.
