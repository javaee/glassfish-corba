/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright (c) 1997-2011 Oracle and/or its affiliates. All rights reserved.
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
// Last Modified : 2004 Jan 31 (Sat) 10:06:37 by Harold Carr.
//

package corba.giopheaderpadding;

import javax.naming.InitialContext;
import org.omg.CORBA.ORB;

import corba.framework.Controller;
import corba.hcks.C;
import corba.hcks.U;

import com.sun.corba.ee.impl.protocol.MessageMediatorImpl;

import java.lang.reflect.*;
import org.omg.PortableInterceptor.*;

public class Client extends org.omg.CORBA.LocalObject
    implements ORBInitializer, ClientRequestInterceptor {

    public static final String baseMsg = Client.class.getName();
    public static final String main = baseMsg + ".main";
    
    public static ORB orb;
    public static InitialContext initialContext;

    public static rmiiI rmiiIPOA;

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

            rmiiIPOA = (rmiiI)
                U.lookupAndNarrow(C.rmiiSL, rmiiI.class, initialContext);

            U.sop("CLIENT.fooA: " + rmiiIPOA.fooA((byte)5));
            rmiiIPOA.fooB();
            U.sop("CLIENT.fooB completed");

            orb.shutdown(true);

        } catch (Exception e) {
            U.sopUnexpectedException(main + " : ", e);
            System.exit(1);
        }
        U.sop(main + " ending successfully");
        System.exit(Controller.SUCCESS);
    }

    ////////////////////////////////////////////////////
    //    
    // ORBInitializer interface implementation.
    //

    public void pre_init(ORBInitInfo info) 
    {
    }

    public void post_init(ORBInitInfo info) 
    {
        // register the interceptors.
        try {
            info.add_client_request_interceptor(this);
        } catch (org.omg.PortableInterceptor.ORBInitInfoPackage.DuplicateName e) {
            throw new org.omg.CORBA.INTERNAL();
        }
        U.sop("ORBInitializer.post_init completed");
    }

    ////////////////////////////////////////////////////
    //
    // implementation of the Interceptor interface.
    //

    public String name() 
    {
        return "ClientInterceptor";
    }

    public void destroy() 
    {
    }

    ////////////////////////////////////////////////////
    //    
    // implementation of the ClientInterceptor interface.
    //

    public void send_request(ClientRequestInfo ri) throws ForwardRequest 
    {
        U.sop("send_request called : " + ri.operation());        
    }

    public void send_poll(ClientRequestInfo ri) 
    {
        U.sop("send_poll called : " + ri.operation());
    }

    public void receive_reply(ClientRequestInfo ri) 
    {    
        String opName = ri.operation();
        U.sop("receive_reply.opName: " + opName);

        if ( ! (opName.equals("fooA") || opName.equals("fooB")) ) {
            return;
        }

        Class riClass = ri.getClass();
        MessageMediatorImpl cri;
        try {
            Field riMember = riClass.getDeclaredField("messageMediator");
            riMember.setAccessible(true);
            cri = (MessageMediatorImpl) riMember.get(ri);
        } catch (Throwable e) { 
            e.printStackTrace(System.out); 
            throw new RuntimeException("impl class instrospection failed", e);
        }

        // fooA.buffer: [header + padding + body (1 byte)]
        // fooA.buffer: [header + body (1 byte)]

        // get header size
        int size = cri.getReplyHeader().getSize();
        U.sop("reply message size: " + size);

        if (opName.equals("fooA")) {
            if (size != 41) {
                throw new RuntimeException("header padding error");
            }
        } else { // opName == fooB
            if (size != 34) {
                throw new RuntimeException("header padding error");
            }
        }
    }

    public void receive_exception(ClientRequestInfo ri) throws ForwardRequest 
    {
        U.sop("receive_exception called : " + ri.operation());
    }

    public void receive_other(ClientRequestInfo ri) throws ForwardRequest 
    {
        U.sop("receive_other called : " + ri.operation());
    }
}

// End of file.

