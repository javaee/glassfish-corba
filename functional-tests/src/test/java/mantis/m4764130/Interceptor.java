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
// Created       : 2002 Oct 16 (Wed) 08:32:24 by Harold Carr.
// Last Modified : 2003 Mar 17 (Mon) 20:51:22 by Harold Carr.
//

package mantis.m4764130;

import java.util.Properties;
import org.omg.CORBA.BAD_PARAM;
import org.omg.CORBA.ORB;
import org.omg.CosNaming.*;
import org.omg.IOP.ServiceContext;
import org.omg.PortableInterceptor.ClientRequestInterceptor;
import org.omg.PortableInterceptor.ClientRequestInfo;
import org.omg.PortableInterceptor.ForwardRequest;
import org.omg.PortableInterceptor.ORBInitInfo;
import org.omg.PortableInterceptor.ORBInitializer;
import org.omg.PortableInterceptor.ServerRequestInterceptor;
import org.omg.PortableInterceptor.ServerRequestInfo;
import com.sun.corba.ee.spi.servicecontext.SendingContextServiceContext;
import com.sun.corba.ee.spi.misc.ORBConstants;

public class Interceptor
    extends
        org.omg.CORBA.LocalObject
    implements
        ClientRequestInterceptor,
        ServerRequestInterceptor,
        ORBInitializer
{
    public int numberOfClientHelloInvocations = 0;
    //
    // Interceptor operations
    //

    public String name() 
    {
        return this.getClass().getName();
    }

    public void destroy() 
    {
    }

    //
    // ClientRequestInterceptor operations
    //

    public void send_request(ClientRequestInfo ri)
        throws
            ForwardRequest
    {
        System.out.println(ri.operation());
        if (ri.operation().equals("hello")) {
            numberOfClientHelloInvocations++;
            if (numberOfClientHelloInvocations == 1) {
                throw new ForwardRequest(ri.target());
            }
        }
    }

    public void send_poll(ClientRequestInfo ri)
    {
    }

    public void receive_reply(ClientRequestInfo ri)
    {
    }

    public void receive_exception(ClientRequestInfo ri)
    {
    }

    public void receive_other(ClientRequestInfo ri)
    {
    }

    //
    // ServerRequestInterceptor operations
    //

    public void receive_request_service_contexts(ServerRequestInfo ri)
    {
        System.out.println(ri.operation());
        try {
            ServiceContext serviceContext =
                ri.get_request_service_context(SendingContextServiceContext.SERVICE_CONTEXT_ID);
        } catch (BAD_PARAM e) {
            // Not present.
            System.out.println("SendingContextServiceContext not present");
            System.exit(1);
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

    //
    // Initializer operations.
    //

    public void pre_init(ORBInitInfo info)
    {
        System.out.println(this.getClass().getName() + " .pre_init");
        try {
            // NOTE: The client only needs the client side points.
            // The server only needs the server side points.
            // It just saves me time just to write one interceptor/initializer.
            info.add_client_request_interceptor(new Interceptor());
            info.add_server_request_interceptor(new Interceptor());
        } catch (Throwable t) {
            System.out.println("Cannot register interceptor: " + t);
        }
    }
    
    public void post_init(ORBInitInfo info) {}
}

// End of file.
