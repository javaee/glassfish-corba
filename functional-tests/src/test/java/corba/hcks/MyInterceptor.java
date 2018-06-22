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
// Created       : 2000 Nov 03 (Fri) 12:50:56 by Harold Carr.
// Last Modified : 2003 Feb 11 (Tue) 14:11:42 by Harold Carr.
//

package corba.hcks;

import org.omg.CORBA.IMP_LIMIT;
import org.omg.CORBA.PERSIST_STORE;
import org.omg.CORBA.SystemException;
import org.omg.IOP.ServiceContext;
import org.omg.PortableInterceptor.ClientRequestInterceptor;
import org.omg.PortableInterceptor.ClientRequestInfo;
import org.omg.PortableInterceptor.RequestInfo;
import org.omg.PortableInterceptor.ServerRequestInterceptor;
import org.omg.PortableInterceptor.ServerRequestInfo;

import com.sun.corba.ee.spi.legacy.interceptor.RequestInfoExt;
import com.sun.corba.ee.spi.presentation.rmi.StubAdapter;

import java.util.Hashtable;

public class MyInterceptor
    extends
        org.omg.CORBA.LocalObject
    implements
        ClientRequestInterceptor,
        ServerRequestInterceptor
{
    public static final String baseMsg = MyInterceptor.class.getName();

    public static final boolean doShowConnection = false;

    public static final int clientSendServiceContextID = 8000;

    public static final int serverReceiveServiceContextID = 9998;
    public static final int serverSendServiceContextID = 9999;

    // Meaningless but big enought to cause fragmentation.a
    public static byte[] serviceContextData = 
        { 0, 1, 2, 3, 4, 5, 6, 7, 8, 9,
          10, 11, 12, 13, 14, 15, 16, 17, 18, 19};

    // Set this to true when fixing bug 4392779.
    // Once that is fixed just leave it true.
    // mroth: Fixed - setting to true.
    public static boolean testingAdapterAndObjectIdWhenExceptionBeforeRR =
        true;

    public static boolean testBug4383192 = true;

    public Hashtable effectiveTargetHashtable = new Hashtable();
    public int effectiveTargetSlotId;

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

        if (ri.operation().equals(C.testEffectiveTarget1)) {
            org.omg.CORBA.Object effectiveTarget1 = ri.effective_target();
            int hash = effectiveTarget1._hash(Integer.MAX_VALUE);
            effectiveTargetHashtable.put(new Integer(hash), effectiveTarget1);

            // Not necessary to set recursion slot because of above if.
            boolean isA = effectiveTarget1._is_a(idlIHelper.id());
            if (isA) {
                idlI ridlI = idlIHelper.narrow(effectiveTarget1);
                ridlI.testEffectiveTarget2();
            } else {
                throw new RuntimeException(C.testEffectiveTarget1);
            }
        } else if (ri.operation().equals(C.testEffectiveTarget2)) {
            org.omg.CORBA.Object effectiveTarget2 = ri.effective_target();
            int hash = effectiveTarget2._hash(Integer.MAX_VALUE);
            org.omg.CORBA.Object effectiveTarget1 = (org.omg.CORBA.Object)
                effectiveTargetHashtable.get(new Integer(hash));
            if (! effectiveTarget2._is_equivalent(effectiveTarget1)) {
                throw new RuntimeException(C.testEffectiveTarget1);
            }
        }

        // This is sent just to cause fragmentation.
        ri.add_request_service_context(
            new ServiceContext(clientSendServiceContextID,
                               serviceContextData),
            true);
    }

    public void send_poll(ClientRequestInfo ri)
    {
        sopCR(baseMsg, "send_poll", ri);
    }

    public void receive_reply(ClientRequestInfo ri)
    {
        sopCR(baseMsg, "receive_reply", ri);
        checkServiceContexts(ri);
    }

    public void receive_exception(ClientRequestInfo ri)
    {
        sopCR(baseMsg, "receive_exception", ri);
        checkServiceContexts(ri);
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
        String point = "receive_request_service_contexts";
        // Check that stuff you add is available, even if exception raised.
        // This is done here rather than receive_request since it is not
        // called in this test when doing DSI throwSystemException.
        ri.add_reply_service_context(
            new ServiceContext(serverReceiveServiceContextID,
                               serviceContextData),
            true);
        try {
            U.sop(baseMsg + "." + point + " " + ri.operation());
        } catch (SystemException e) {
            U.sopUnexpectedException(baseMsg + "." + point, e);
        }

        if (ri.operation().equals(C.throwThreadDeathInReceiveRequestServiceContexts)) {
            throw new ThreadDeath();
        }
    }

    public void receive_request(ServerRequestInfo ri)
    {
        sopSR(baseMsg, "receive_request", ri);

        if (ri.operation().equals(C.throwThreadDeathInReceiveRequest)) {

            throw new ThreadDeath();

        }

    }

    public void send_reply(ServerRequestInfo ri)
    {
        sopSR(baseMsg, "send_reply", ri);
        // Check that stuff you add is avaible, even if exception raised.
        ri.add_reply_service_context(
            new ServiceContext(serverSendServiceContextID,
                               serviceContextData),
            true);

        if (ri.operation().equals(C.raiseSystemExceptionInSendReply)) {

            throw new IMP_LIMIT();

        } else if (ri.operation().equals(C.throwThreadDeathInSendReply)) {

            throw new ThreadDeath();

        }
    }

    public void send_exception(ServerRequestInfo ri)
    {
        sopSR(baseMsg, "send_exception", ri);
        ri.add_reply_service_context(
            new ServiceContext(serverSendServiceContextID,
                               serviceContextData),
            true);
        String operation = ri.operation();
        if (operation.equals(C.raiseUserInServantThenSystemInPOThenSE) ||
            operation.equals(C.raiseSystemInServantThenPOThenSE))
        {

            throw new PERSIST_STORE();

        } else if (operation.equals(C.throwThreadDeathInServantThenSysInPostThenSysInSendException))
        {
            throw new PERSIST_STORE();
/* KMC: Can't find any test affected by this code in HCKS, which causes problems in testMisc            
        } else if (operation.equals(C.sendTwoObjects)) {
            SystemException sex =
                ORBUtility.extractSystemException(ri.sending_exception());
            U.sop(sex);
            if ((! (sex instanceof TRANSIENT)) ||
                (sex.minor != ORBUtilSystemException.REQUEST_CANCELED) ||
                (sex.completed != CompletionStatus.COMPLETED_NO))
            {
                U.sopUnexpectedException(
                    "Not handling REQUEST_CANCELED correctly",
                    sex);
                // Since this never makes it back to the client
                // the only easy way to make the test indicate a failure
                // is to kill the server.
                System.exit(-1);
                // REVISIT: Not complete.  What if ending point never called?
                // What if wrong ending point called?
            }
*/
        }

    }

    public void send_other(ServerRequestInfo ri)
    {
        sopSR(baseMsg, "send_other", ri);
        ri.add_reply_service_context(
            new ServiceContext(serverSendServiceContextID,
                               serviceContextData),
            true);
    }

    //
    // Utilities.
    //

    public static void sopCR(String clazz, String point, ClientRequestInfo ri)
    {
        try {
            U.sop(clazz + "." + point + " " + ri.operation());
            showConnection(ri);
        } catch (SystemException e) {
            U.sopUnexpectedException(baseMsg + "." + point, e);
        }
    }

    public static void sopSR(String clazz, String point, ServerRequestInfo ri)
    {
        try {
            ri.adapter_id();
            U.sop(clazz + "." + point + " " + new String(ri.object_id()) +
                  " " + ri.operation());
            showConnection(ri);
        } catch (SystemException e) {
            U.sopUnexpectedException(baseMsg + "." + point, e);
            if (testingAdapterAndObjectIdWhenExceptionBeforeRR) {
                throw e;
            }
        }
    }

    public static void showConnection(RequestInfo ri)
    {
        if (doShowConnection && (ri instanceof RequestInfoExt)) {
            RequestInfoExt rie = (RequestInfoExt) ri;
            if (rie.connection() != null) {
                U.sop("    request on connection: " + rie.connection());
            }
        }
    }

    public static void checkServiceContexts(ClientRequestInfo ri)
    {
        if (testBug4383192) {
            org.omg.CORBA.Object target = ri.effective_target();
            // Only do this on type idlI.
            // Note: Do not use target._is_a to avoid interceptor recursion.
            if ( StubAdapter.getTypeIds(target)[0].equals( 
                idlIHelper.id() )) {
                ri.get_reply_service_context(serverReceiveServiceContextID);
                ri.get_reply_service_context(serverSendServiceContextID);
            }
        }
    }
}

// End of file.

