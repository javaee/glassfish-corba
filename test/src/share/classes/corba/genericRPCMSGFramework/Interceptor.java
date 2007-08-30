/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2007 Sun Microsystems, Inc. All rights reserved.
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
//
// Created       : 2002 Apr 17 (Wed) 14:30:36 by Harold Carr.
// Last Modified : 2002 Apr 25 (Thu) 19:01:33 by Harold Carr.
//

package corba.genericRPCMSGFramework;

import org.omg.PortableInterceptor.ClientRequestInterceptor;
import org.omg.PortableInterceptor.ClientRequestInfo;
import org.omg.PortableInterceptor.RequestInfo;
import org.omg.PortableInterceptor.ServerRequestInterceptor;
import org.omg.PortableInterceptor.ServerRequestInfo;

import corba.hcks.U;

public class Interceptor
    extends
        org.omg.CORBA.LocalObject
    implements
	ClientRequestInterceptor,
	ServerRequestInterceptor
{
    public static final String baseMsg = Interceptor.class.getName();

    public boolean printEnabled = false;

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

    //
    // ClientRequestInterceptor operations
    //

    public void send_request(ClientRequestInfo ri)
    {
	sopCR(baseMsg, "send_request", ri);
    }

    public void send_poll(ClientRequestInfo ri)
    {
	sopCR(baseMsg, "send_poll", ri);
    }

    public void receive_reply(ClientRequestInfo ri)
    {
	sopCR(baseMsg, "receive_reply", ri);
    }

    public void receive_exception(ClientRequestInfo ri)
    {
	sopCR(baseMsg, "receive_exception", ri);
    }

    public void receive_other(ClientRequestInfo ri)
    {
	sopCR(baseMsg, "receive_other", ri);
    }

    //
    // ServerRequestInterceptor operations
    //

    public void receive_request_service_contexts(ServerRequestInfo ri)
    {
	sopSR(baseMsg, "receive_request_service_contexts", ri);
    }

    public void receive_request(ServerRequestInfo ri)
    {
	sopSR(baseMsg, "receive_request", ri);
    }

    public void send_reply(ServerRequestInfo ri)
    {
	sopSR(baseMsg, "send_reply", ri);
    }

    public void send_exception(ServerRequestInfo ri)
    {
	sopSR(baseMsg, "send_exception", ri);
    }

    public void send_other(ServerRequestInfo ri)
    {
	sopSR(baseMsg, "send_other", ri);
    }

    //
    // Utilities.
    //

    public void sopCR(String clazz, String point, ClientRequestInfo ri)
    {
	if (!printEnabled) return;
	U.sop("");
	U.sop("--------------------------------------------------");
	U.sop(clazz + "." + point + " " + ri.operation());
	sopR(ri);
	tab("target", ri.target().toString());
	tab("effective_target", ri.effective_target().toString());
	tab("effective_profile", ri.effective_profile().toString());
	try {
	    tab("receive_exception", ri.received_exception().toString());
	} catch (Exception e) {
	    tab("received_exception", e);
	}
	try {
	    tab("received_exception_id", ri.received_exception_id().toString());
	} catch (Exception e) {
	    tab("received_exception_id", e);
	}
	// REVISIT: get_effective_component
	// REVISIT: get_effective_components
	// REVISIT: get_request_policy
	// REVISIT: add_request_service_context
	U.sop("--------------------------------------------------");
    }

    public void sopSR(String clazz, String point, ServerRequestInfo ri)
    {
	if (!printEnabled) return;
	U.sop("");
	U.sop("--------------------------------------------------");
	U.sop(clazz + "." + point + " " + ri.operation());
	sopR(ri);
	try {
	    tab("sending_exception", ri.sending_exception().toString());
	} catch (Exception e) {
	    tab("sending_exception", e);
	}
	if (Constants.jdkIsHopperOrGreater()) {
	    // These are only available when the classes generated from
	    // interceptors.idl are present in org.omg.*.
	    try {
		tab("server_id", new Integer(ri.server_id()).toString());
	    } catch (Exception e) {
		tab("server_id", e);
	    }
	    try {
		tab("orb_id", ri.orb_id().toString());
	    } catch (Exception e) {
		tab("orb_id", e);
	    }
	    try {
		tab("adapter_name", ri.adapter_name().toString());
	    } catch (Exception e) {
		tab("adapter_name", e);
	    }
	}
	try {
	    tab("object_id", ri.object_id().toString());
	} catch (Exception e) {
	    tab("object_id", e);
	}
	try {
	    tab("adapter_id", ri.adapter_id().toString());
	} catch (Exception e) {
	    tab("adapter_id", e);
	}
	try {
	    tab("target_most_derived_interface", ri.target_most_derived_interface().toString());
	} catch (Exception e) {
	    tab("target_most_derived_interface", e);
	}
	// REVISIT: get_server_policy
	// REVISIT: set_slot
	// REVISIT: target_is_a
	// REVISIT: add_reply_service_context
	U.sop("--------------------------------------------------");
    }

    public void sopR(RequestInfo ri)
    {
	tab("request_id", new Long(ri.request_id()).toString());
	tab("operation", ri.operation());
	try {
	    tab("arguments", ri.arguments().toString());
	} catch (Exception e) {
	    tab("arguments", e);
	}
	try {
	    tab("exceptions", ri.exceptions().toString());
	} catch (Exception e) {
	    tab("exceptions", e);
	}
	try {
	    tab("contexts", ri.contexts().toString());
	} catch (Exception e) {
	    tab("contexts", e);
	}
	try {
	    tab("operation_context", ri.operation_context().toString());
	} catch (Exception e) {
	    tab("operation_context", e);
	}
	try {
	    tab("result", ri.result().toString());
	} catch (Exception e) {
	    tab("result", e);
	}
	tab("response_expected", new Boolean(ri.response_expected()).toString());
	tab("sync_scope", new Short(ri.sync_scope()).toString());
	try {
	    tab("reply_status", new Short(ri.reply_status()).toString());
	} catch (Exception e) {
	    tab("reply_status", e);
	}
	try {
	    tab("forward_reference", ri.forward_reference().toString());
	} catch (Exception e) {
	    tab("forward_reference", e);
	}
	// REVISIT: get_slot
	// REVISIT: get_request_service_context
	// REVISIT: get_reply_service_context
    }

    public void tab(String message, Exception e)
    {
	System.out.print("\t");
	System.out.print(message);
	System.out.print(": ");
	U.sop(e);
    }
    public void tab(String message, String value)
    {
	System.out.print("\t");
	System.out.print(message);
	System.out.print(": ");
	U.sop(value);
    }
}

// End of file.

