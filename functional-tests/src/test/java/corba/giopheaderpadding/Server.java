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
// Created       : 2003 Apr 09 (Wed) 16:28:12 by Harold Carr.
// Last Modified : 2004 Jan 31 (Sat) 11:12:48 by Harold Carr.
//

package corba.giopheaderpadding;

import javax.naming.InitialContext;

import org.omg.CORBA.Policy;
import org.omg.PortableServer.POA;
import org.omg.PortableServer.ServantRetentionPolicyValue;

import corba.framework.Controller;
import corba.framework.Options;
import corba.hcks.C;
import corba.hcks.U;

import com.sun.corba.ee.spi.transport.TransportManager;
import com.sun.corba.ee.spi.orb.ORB;
import com.sun.corba.ee.impl.protocol.MessageMediatorImpl;

import java.lang.reflect.*;
import org.omg.PortableInterceptor.*;

public class Server extends org.omg.CORBA.LocalObject
    implements ORBInitializer, ServerRequestInterceptor {

    public static final String baseMsg = Server.class.getName();
    public static final String main = baseMsg + ".main";
    public static final String thisPackage = 
        Server.class.getPackage().getName();

    public static final String rmiiIServantPOA_Tie = 
        thisPackage + "._rmiiIServantPOA_Tie";

    public static final String SLPOA = "SLPOA";

    public static ORB orb;
    public static InitialContext initialContext;
    public static TransportManager transportManager;
    public static POA rootPOA;
    public static POA slPOA;

    public static void main(String[] av) {
        try {
            U.sop(main + " starting");

            if (! ColocatedClientServer.isColocated) {
                U.sop(main + " : creating ORB.");
                orb = (ORB) ORB.init(av, null);
                U.sop(main + " : creating InitialContext.");
                initialContext = C.createInitialContext(orb);
            }

            rootPOA = U.getRootPOA(orb);
            rootPOA.the_POAManager().activate();

            Policy[] policies = U.createUseServantManagerPolicies(
                                     rootPOA, 
                                     ServantRetentionPolicyValue.NON_RETAIN);

            slPOA = U.createPOAWithServantManager(
                                     rootPOA, SLPOA, policies, 
                                     new ServantLocator());

            U.createRMIPOABind(C.rmiiSL, rmiiIServantPOA_Tie, slPOA, orb, 
                               initialContext);

            U.sop(main + " ready");
            U.sop(Options.defServerHandshake);
            System.out.flush();

            synchronized (ColocatedClientServer.signal) {
                ColocatedClientServer.signal.notifyAll();
            }
            
            orb.run();

        } catch (Exception e) {
            U.sopUnexpectedException(main, e);
            System.exit(1);
        }
        U.sop(main + " ending successfully");
        System.exit(Controller.SUCCESS);
    }
        
    // ORBInitializer interface implementation.

    public void pre_init(ORBInitInfo info) {}

    public void post_init(ORBInitInfo info) {
        // register the interceptors.
        try {
            info.add_server_request_interceptor(this);
        } catch (org.omg.PortableInterceptor.ORBInitInfoPackage.DuplicateName e) {
            throw new org.omg.CORBA.INTERNAL();
        }
        U.sop("ORBInitializer.post_init completed");
    }

    // implementation of the Interceptor interface.

    public String name() { return "ServerInterceptor"; }

    public void destroy() {}

    // implementation of the ServerInterceptor interface.

    public void receive_request_service_contexts(ServerRequestInfo ri)
        throws ForwardRequest {
        
        String opName = ri.operation();
        U.sop("receive_request_service_contexts.opName: " + opName);

        if ( ! (opName.equals("fooA") || opName.equals("fooB")) ) {
            return;
        }

        Class riClass = ri.getClass();
        MessageMediatorImpl cri;
        try {
            Field riMember = riClass.getDeclaredField("request");
            riMember.setAccessible(true);
            cri = (MessageMediatorImpl) riMember.get(ri);
        } catch (Throwable e) { 
            e.printStackTrace(System.out);
            throw new RuntimeException("impl class instrospection failed", e);
        }

        // fooA.buffer: [header + padding + body (1 byte)]
        // fooA.buffer: [header + body (1 byte)]

        // get header size
        int size = cri.getRequestHeader().getSize();
        U.sop("request message size: " + size);

        if (! ColocatedClientServer.isColocated) {
            if (opName.equals("fooA")) {
                if (size != 153) {
                    throw new RuntimeException("header padding error");
                }
            } else { // opName == fooB
                if (size != 146) {
                    throw new RuntimeException("header padding error");
                }
            }
        } else {
            if (opName.equals("fooA")) {
                if (size != 129) {
                    throw new RuntimeException("header padding error");
                }
            } else { // opName == fooB
                if (size != 126) {
                    throw new RuntimeException("header padding error");
                }
            }
        }
    }

    public void receive_request(ServerRequestInfo ri) throws ForwardRequest {
        U.sop("receive_request called : " + ri.operation());
    }

    public void send_reply(ServerRequestInfo ri) {
        U.sop("send_reply called : " + ri.operation());
    }

    public void send_exception(ServerRequestInfo ri) throws ForwardRequest {
        U.sop("send_exception called : " + ri.operation());
    }

    public void send_other(ServerRequestInfo ri) throws ForwardRequest {
        U.sop("send_other called : " + ri.operation());
    }
}

// End of file.

