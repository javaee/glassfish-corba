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
// Created       : 2001 Jan 24 (Wed) 14:38:57 by Harold Carr.
// Last Modified : 2001 Feb 05 (Mon) 14:12:34 by Harold Carr.
//

package corba.hcks;

import org.omg.CORBA.Any;
import org.omg.CORBA.BAD_PARAM;
import org.omg.CORBA.ORB;

import org.omg.CosNaming.*;
import org.omg.CosNaming.NamingContextPackage.*;

import org.omg.IOP.ServiceContext;

import org.omg.PortableInterceptor.ClientRequestInterceptor;
import org.omg.PortableInterceptor.ClientRequestInfo;
import org.omg.PortableInterceptor.Current;
import org.omg.PortableInterceptor.InvalidSlot;
import org.omg.PortableInterceptor.RequestInfo;
import org.omg.PortableInterceptor.ServerRequestInterceptor;
import org.omg.PortableInterceptor.ServerRequestInfo;

/**
 * This interceptor helps test Server-Side PICurrent operation.
 */

public class SsPicInterceptor
    extends
        org.omg.CORBA.LocalObject
    implements
        ClientRequestInterceptor,
        ServerRequestInterceptor
{
    public static final String baseMsg = MyInterceptor.class.getName();

    public ORB anyFactory = ORB.init();

    public Current piCurrent;
    public NamingContext nameService;
    public String name;

    // The contents do not matter.
    public static final byte[] serviceContextData = { 1 };

    //
    // From client to server and vice-versa.  
    // Services the client call itself.
    //

    // The ids which the client side uses to communicate to the
    // to the server side on a call to operation sPic1.
    public static final int sPic1AServiceContextId = 0x1A;
    public static final int sPic1BServiceContextId = 0x1B;

    // Value of above for this instance.
    public int sPic1ServiceContextId;

    // The slots which RRSC uses communicate the above info downstream.
    public static int sPic1ASlotId; // Used in other files.
    public static int sPic1BSlotId; // Used in other files.
    public int sPic1SlotId;  // Used here in this instance.


    //
    // From server interceptor to itself.
    //

    // The ids which the server side uses to communicate back to itself
    // to avoid recursion.
    public static final int sPic2AServiceContextId = 0x2A;
    public static final int sPic2BServiceContextId = 0x2B;

    // Value of above for this instance.
    public int sPic2ServiceContextId;

    // The slot which RRSC uses to communicate with client interceptor
    // on a out call call to avoid recursion.
    public int sPic2SlotId;


    //
    // Constructor.
    //

    public SsPicInterceptor(int sPic1ServiceContextId,
                            int sPic2ServiceContextId,
                            int sPic1SlotId,
                            int sPic2SlotId,
                            Current piCurrent,
                            NamingContext nameService,
                            String name)
    {
        this.sPic1ServiceContextId = sPic1ServiceContextId;
        this.sPic2ServiceContextId = sPic2ServiceContextId;
        this.sPic1SlotId = sPic1SlotId;
        this.sPic2SlotId = sPic2SlotId;
        this.piCurrent = piCurrent;
        this.nameService = nameService;
        this.name = name;
    }

    //
    // Interceptor operations
    //

    public String name()
    {
        return name;
    }

    public void destroy() 
    {
    }

    //
    // ClientRequestInterceptor operations
    //

    public void send_request(ClientRequestInfo ri)
    {
        checkPicMemory(ri);

        // If the client gives us info then send it to the server.
        // Existence counts, not contents.
        Any any = null;
        any = U.getSlot(ri, sPic1SlotId);
        if (! U.isTkNull(any)) {
            ServiceContext serviceContext = 
                new ServiceContext(sPic1ServiceContextId, serviceContextData);
            ri.add_request_service_context(serviceContext, false);
        }

        // If the server interceptor sets the recursion slot then
        // put in the service context so the server doesn't make
        // the call again.

        any = null;
        any = U.getSlot(ri, sPic2SlotId);
        if (U.isTkBoolean(any)) {
            ServiceContext serviceContext = 
                new ServiceContext(sPic2ServiceContextId, serviceContextData);
            ri.add_request_service_context(serviceContext, false);
        }

    }

    public void send_poll(ClientRequestInfo ri)
    {
    }

    public void receive_reply(ClientRequestInfo ri)
    {
        checkPicMemory(ri);

        if (isSPic1(ri)) {
            // This should not result in an exception.
            // If it does the client test will report it.
            ri.get_reply_service_context(sPic1ServiceContextId);
        
            // The server's sets to these same slot ids should not effect
            // the client interceptors ri slots nor
            // the client interceptors pic slots.
            Any anyRi  = null;
            Any anyPic = null;
            anyRi  = U.getSlot(ri, sPic1SlotId);
            anyPic = U.getSlot(piCurrent, sPic1SlotId);
            if (U.isTkLong(anyRi)) {
                int value = anyRi.extract_long();
                if (value != 0) {
                    throw new RuntimeException("RI CLOBBERED");
                }
            }
            if (! U.isTkNull(anyPic)) {
                throw new RuntimeException("PIC CLOBBERED");
            }
        }
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
        checkPicMemory(ri);

        try {
            // If the service context is sent from the client
            // then send its info downstream.
            // Existence counts, not contents.
            ri.get_request_service_context(sPic1ServiceContextId);
            Any any = anyFactory.create_any();
            any.insert_long(1);
            U.setSlot(ri, sPic1SlotId, any);
        } catch (BAD_PARAM e) {
            // Do nothing when the context is absent.
            ;
        }

        // We only want to test this server side recursion when testing
        // sPic1 - it works with anything but this just speeds up the test
        // by narrowing its applicability.
        if (ri.operation().equals(C.sPic1)) {
            // Always set the recursion slot info.
            Any any = anyFactory.create_any();
            any.insert_boolean(true);
            U.setSlot(piCurrent, sPic2SlotId, any);

            // Now make the out call if you have not already done so.
            try {
                // Check the recursion indicator.
                ri.get_request_service_context(sPic2ServiceContextId);
            } catch (BAD_PARAM e) {
                // Recusion indicator not set so make the call.
                try {
                    idlSLI ridlSLI1 = 
                        idlSLIHelper.narrow(
                         nameService.resolve(U.makeNameComponent(C.idlSLI1)));
                    ridlSLI1.sPic2();
                } catch (CannotProceed ex) {
                    U.sopUnexpectedException(baseMsg, ex);
                } catch (InvalidName ex) {
                    U.sopUnexpectedException(baseMsg, ex);
                } catch (NotFound ex) {
                    U.sopUnexpectedException(baseMsg, ex);
                }
            }
        }
    }

    public void receive_request(ServerRequestInfo ri)
    {
        checkPicMemory(ri);

        boolean ensure = isSPic1(ri);
        C.testAndIncrementPICSlot(ensure, "receive_request",
                                  sPic1SlotId, 2, piCurrent);
    }

    public void send_reply(ServerRequestInfo ri)
    {
        checkPicMemory(ri);

        boolean ensure = isSPic1(ri);
        if (C.testAndIncrementPICSlot(ensure, "send_reply",
                                      sPic1SlotId, 5, piCurrent))
        {
            // Complete the end-to-end test by sending a reply
            // service context if all went well.  If this is absent
            // for this method then the test will complain.
            ServiceContext serviceContext = 
                new ServiceContext(sPic1ServiceContextId, serviceContextData);
            ri.add_reply_service_context(serviceContext, false);
        }
    }

    public void send_exception(ServerRequestInfo ri)
    {
    }

    public void send_other(ServerRequestInfo ri)
    {
    }

    //
    // Utilities.
    //

    // To see memory locations while stepping.
    public void checkPicMemory(RequestInfo ri)
    {
        try {
            ri.get_slot(sPic1SlotId);
            piCurrent.get_slot(sPic1SlotId);
        } catch (Throwable t) {}
    }

    public static boolean isSPic1(RequestInfo ri)
    {
        return ri.operation().equals(C.sPic1);
    }

}

// End of file.

