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
// Created       : 2001 May 23 (Wed) 19:46:30 by Harold Carr.
// Last Modified : 2001 Sep 24 (Mon) 21:42:34 by Harold Carr.
//

package pi.serviceexample;

import org.omg.CORBA.Any;
import org.omg.CORBA.ORB;
import org.omg.CORBA.TCKind;
import org.omg.IOP.ServiceContext;
import org.omg.PortableInterceptor.ClientRequestInterceptor;
import org.omg.PortableInterceptor.ClientRequestInfo;
import org.omg.PortableInterceptor.Current;
import org.omg.PortableInterceptor.InvalidSlot;

public class LoggingServiceClientInterceptor
    extends org.omg.CORBA.LocalObject
    implements ClientRequestInterceptor
{
    private LoggingService loggingService;
    private Current piCurrent;
    private int outCallIndicatorSlotId;

    public LoggingServiceClientInterceptor(LoggingService loggingService,
					   Current piCurrent,
					   int outCallIndicatorSlotId)
    {
	this.loggingService = loggingService;
	this.piCurrent = piCurrent;
	this.outCallIndicatorSlotId = outCallIndicatorSlotId;
    }

    //
    // Interceptor operations
    //

    public String name() 
    {
	return "LoggingServiceClientInterceptor";
    }

    public void destroy() 
    {
    }

    //
    // ClientRequestInterceptor operations
    //

    public void send_request(ClientRequestInfo ri)
    {
	log(ri, "send_request");
    }

    public void send_poll(ClientRequestInfo ri)
    {
	log(ri, "send_poll");
    }

    public void receive_reply(ClientRequestInfo ri)
    {
	log(ri, "receive_reply");
    }

    public void receive_exception(ClientRequestInfo ri)
    {
	log(ri, "receive_exception");
    }

    public void receive_other(ClientRequestInfo ri)
    {
	log(ri, "receive_other");
    }

    //
    // Utilities.
    //

    public void log(ClientRequestInfo ri, String point)
    {
	// IMPORTANT: Always set the TSC out call indicator in case
	// other interceptors make outcalls for this request.
	// Otherwise the outcall will not be set for the other interceptor's
	// outcall resulting in infinite recursion.

	Any indicator = ORB.init().create_any();
	indicator.insert_boolean(true);
	try {
	    piCurrent.set_slot(outCallIndicatorSlotId, indicator);
	} catch (InvalidSlot e) { }

	try {
	    indicator = ri.get_slot(outCallIndicatorSlotId);

	    // If the RSC out call slot is not set then log this invocation.
	    // If it is set that indicates the interceptor is servicing the
	    // invocation of loggingService itself.  In that case do
	    // nothing (to avoid infinite recursion).

	    if (indicator.type().kind().equals(TCKind.tk_null)) {
		loggingService.log(ri.operation() + " " + point);
	    }
	} catch (InvalidSlot e) {
	    System.out.println("Exception handling not shown.");	    
	}
    }
}

// End of file.

