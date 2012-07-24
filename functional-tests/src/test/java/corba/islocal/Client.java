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
// Created       : 2003 Apr 09 (Wed) 16:54:21 by Harold Carr.
// Last Modified : 2003 May 19 (Mon) 16:06:58 by Harold Carr.
//

package corba.islocal;

import javax.naming.InitialContext;
import org.omg.CORBA.ORB;

import corba.framework.Controller;
import corba.hcks.C;
import corba.hcks.U;

import com.sun.corba.ee.spi.presentation.rmi.StubAdapter ;

public class Client 
{
    public static final String baseMsg = Client.class.getName();
    public static final String main = baseMsg + ".main";
    
    public static ORB orb;
    public static InitialContext initialContext;

    public static idlI idlIConnect;
    public static idlI idlIPOA;
    public static rmiiI rmiiIConnect;
    public static rmiiI rmiiIPOA;

    public static String idlIConnectArg  = Server.idlIConnect;
    public static String idlIPOAArg      = Server.idlIPOA;
    public static String rmiiIConnectArg = Server.rmiiIConnect;
    public static String rmiiIPOAArg     = Server.rmiiIPOA;

    public static int errors = 0;
    public static Thread clientThread;

    public static void main(String[] av)
    {
        try {
            U.sop(main + " starting");

            if (! ColocatedClientServer.isColocated) {
                U.sop(main + " : creating ORB.");
                orb = ORB.init(av, null);
                U.sop(main + " : creating InitialContext.");
                initialContext = C.createInitialContext(orb);
            }

            idlIConnect = idlIHelper.narrow(U.resolve(Server.idlIConnect,orb));
            idlIPOA     = idlIHelper.narrow(U.resolve(Server.idlIPOA,    orb));

            rmiiIConnect = (rmiiI)
                U.lookupAndNarrow(Server.rmiiIConnect,
                                  rmiiI.class, initialContext);

            /*
            rmiiIPOA = (rmiiI)
                U.lookupAndNarrow(C.rmiiSL, rmiiI.class, initialContext);
            */


            U.sop("-----------isLocal-------------");

            boolean is_local_result = StubAdapter.isLocal( rmiiIConnect ) ;
            U.sop("is_local: " + is_local_result);
            if (is_local_result != ColocatedClientServer.isColocated) {
                    errors++;
                    U.sop("!!! is_local value incorrect !!!");
            }

            /* REVISIT - you cannot call StubAdapter.isLocal outside of stub.
               It HAS state.
            boolean isLocalResult = 
                StubAdapter.isLocal((javax.rmi.CORBA.Stub)rmiiIConnect);
            U.sop("StubAdapter.isLocal: " + isLocalResult);
            if (isLocalResult != ColocatedClientServer.isColocated) {
                    errors++;
                    U.sop("!!! StubAdapter.isLocal value incorrect !!!");
            }
            */

            U.sop("-----------calls-------------");

            if (ColocatedClientServer.isColocated) {
                clientThread = Thread.currentThread();
            }

            U.sop("CLIENT: " + idlIConnect.o(idlIConnectArg));
            U.sop("CLIENT: " + idlIPOA.o(idlIPOAArg));
            U.sop("CLIENT: " + rmiiIConnect.m(rmiiIConnectArg));
            /*
            U.sop("CLIENT: " + rmiiIPOA.m(rmiiIPOAArg));
            */

            orb.shutdown(true);

            if (errors != 0) {
                U.sop("!!! Errors found !!!");
                System.exit(1);
            }

        } catch (Exception e) {
            U.sopUnexpectedException(main + " : ", e);
            System.exit(1);
        }
        U.sop(main + " ending successfully");
        System.exit(Controller.SUCCESS);
    }
}

// End of file.

