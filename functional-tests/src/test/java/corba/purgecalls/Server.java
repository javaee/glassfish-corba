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
// Created       : 2002 Jan 17 (Thu) 14:09:43 by Harold Carr.
// Last Modified : 2002 Jan 17 (Thu) 15:42:33 by Harold Carr.
//

package corba.purgecalls;

import corba.framework.Controller;
import corba.hcks.C;
import corba.hcks.U;
import org.omg.CORBA.ORB;
import org.omg.PortableServer.POA;

public class Server 
{
    public static final String baseMsg = Server.class.getName();
    public static final String main = baseMsg + ".main";

    public static final String ServerSide = "ServerSide";

    public static ORB        orb;
    public static POA        rRootPOA;
    public static ServerSide rServerSide;

    public static void main(String[] av)
    {
        try {

            U.sop(main + " starting");

            orb = C.createORB(av, 1024);
            rRootPOA = U.getRootPOA(orb);
            rRootPOA.the_POAManager().activate();

            U.createWithServantAndBind(ServerSide,
                                       new ServerSideServant(),
                                       rRootPOA, orb);

            U.sop(main + " ready");
            U.sop("Server is ready."); // CORBATest handshake.

            System.out.flush();

            orb.run();

        } catch (Exception e) {
            U.sopUnexpectedException(main, e);
            System.exit(1);
        }
        System.exit(Controller.SUCCESS);
    }
}

// End of file.

