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

//
// Created       : 2000 Oct 13 (Fri) 09:55:19 by Harold Carr.
// Last Modified : 2002 Mar 24 (Sun) 09:23:14 by Harold Carr.
//

package corba.connectintercept_1_4;

import org.omg.PortableInterceptor.ORBInitializer;
import org.omg.PortableInterceptor.ORBInitInfo;
import org.omg.PortableInterceptor.ORBInitInfoPackage.DuplicateName;

public class ServerORBInitializer
    extends
        org.omg.CORBA.LocalObject
    implements
        ORBInitializer
{
    public static final String baseMsg = ServerORBInitializer.class.getName();

    public void pre_init(ORBInitInfo orbInitInfo) { }

    public void post_init(ORBInitInfo orbInitInfo)
    {
        try {
            // These are intentionally random to test ordering.

            orbInitInfo.add_client_request_interceptor(
                new CRI());

            orbInitInfo.add_server_request_interceptor(
                new SRIOrdered("Three", 3));

            orbInitInfo.add_ior_interceptor(
                new ServerIORInterceptor());

            orbInitInfo.add_server_request_interceptor(
                new SRIOrdered("One", 1));

            orbInitInfo.add_server_request_interceptor(
                new SRI());

            orbInitInfo.add_server_request_interceptor(
                new SRIOrdered("Two", 2));

            System.out.println(baseMsg + ".post_init: add_* completed.");
        } catch (DuplicateName ex) {
            System.out.println(baseMsg + ".post_init: " + ex);
            System.exit(-1);
        }
    }
}

// End of file.
