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
// Created       : 2000 Oct 31 (Tue) 09:58:47 by Harold Carr.
// Last Modified : 2002 Mar 22 (Fri) 09:33:55 by Harold Carr.
//

package corba.connectintercept_1_4;

import org.omg.CORBA.INTERNAL;
import org.omg.PortableInterceptor.ServerRequestInfo;

public class SRIOrdered
    extends
        org.omg.CORBA.LocalObject
    implements
	org.omg.PortableInterceptor.ServerRequestInterceptor,
        Comparable
{
    public static final String baseMsg = SRIOrdered.class.getName();
    public String name;
    public int order;
    public SRIOrdered(String name, int order)
    {
	this.name = name;
	this.order = order;
    }
    public int compareTo(Object o)
    {
	int otherOrder = ((SRIOrdered)o).order;
	if (order < otherOrder) {
	    return -1;
	} else if (order == otherOrder) {
	    return 0;
	}
	return 1;
    }
    public String name() { return name; }

    public void destroy() 
    {
	try {
	    Common.up(order);
	} catch (INTERNAL e) {
	    // INTERNAL will get swallowed by ORB.
	    // Convert it to something else so server will exit incorrectly
	    // so error can be detected.
	    throw new RuntimeException(baseMsg + ": Wrong order in destroy.");
	}
    }

    public void receive_request_service_contexts(ServerRequestInfo sri)
    {
	Common.up(order);
    }

    public void receive_request(ServerRequestInfo sri)
    {
	// Note: Do NOT put Common.up here because all 3 ordered
	// interceptors run in RRSC so when we get here current will
	// be 3 but the first ordered interceptor will have value 1
	// and fail.
	// Bottom line: only count up in one point.
    }

    public void send_reply(ServerRequestInfo sri)
    {
	Common.down(order);
    }

    public void send_exception(ServerRequestInfo sri)
    {
	Common.down(order);
    }

    public void send_other(ServerRequestInfo sri)
    {
	Common.down(order);
    }
}

// End of file.







