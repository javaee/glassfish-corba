/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright (c) 2017-2018 Oracle and/or its affiliates. All rights reserved.
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

package com.sun.corba.ee.impl.protocol;

import com.sun.corba.ee.impl.encoding.BufferManagerFactory;
import com.sun.corba.ee.impl.encoding.CDRInputObject;
import com.sun.corba.ee.impl.encoding.CDROutputObject;
import com.sun.corba.ee.impl.protocol.giopmsgheaders.Message;
import com.sun.corba.ee.spi.ior.iiop.GIOPVersion;
import com.sun.corba.ee.spi.misc.ORBConstants;
import com.sun.corba.ee.spi.orb.ORB;
import com.sun.corba.ee.spi.orb.ORBData;
import com.sun.corba.ee.spi.protocol.ClientInvocationInfo;
import com.sun.corba.ee.spi.protocol.MessageMediator;
import com.sun.corba.ee.spi.protocol.PIHandler;
import com.sun.corba.ee.spi.transport.Connection;
import com.sun.corba.ee.spi.transport.ContactInfo;
import com.sun.corba.ee.spi.transport.ContactInfoListIterator;
import org.junit.Test;
import org.omg.CORBA.portable.RemarshalException;

import java.util.Iterator;

import static com.meterware.simplestub.Stub.createStrictStub;

public class ClientRequestDispatcherImplTest {

    static final int BUFFER_SIZE = 128;
    private final ORBFake orb = createStrictStub(ORBFake.class);
    private final MessageMediatorFake mediator = createStrictStub(MessageMediatorFake.class, orb);
    private final MessageFake message = createStrictStub(MessageFake.class);
    private final byte streamFormatVersion = 1;

    private ClientRequestDispatcherImpl impl = new ClientRequestDispatcherImpl();
    private CDROutputObject outputObject = new CDROutputObject(orb, mediator, message, streamFormatVersion);

    @Test(expected = RemarshalException.class)
    public void whenPIHandleReturnsRemarshalException_throwIt() throws Exception {
        orb.setPiEndingPointException(new RemarshalException());

        impl.marshalingComplete(null, outputObject);
    }

    abstract static class ORBFake extends ORB {
        private ClientInvocationInfoFake invocationInfo = createStrictStub(ClientInvocationInfoFake.class);
        private ORBDataFake orbData = createStrictStub(ORBDataFake.class);
        private Exception piEndingPointException;

        void setPiEndingPointException(Exception piEndingPointException) {
            this.piEndingPointException = piEndingPointException;
        }

        @Override
        public ClientInvocationInfo getInvocationInfo() {
            return invocationInfo;
        }

        @Override
        public ORBData getORBData() {
            return orbData;
        }

        @Override
        public PIHandler getPIHandler() {
            return createStrictStub(PIHandlerFake.class, this);
        }
    }

    abstract static class ClientInvocationInfoFake implements ClientInvocationInfo {
        @Override
        public Iterator getContactInfoListIterator() {
            return createStrictStub(ContactInfoListIteratorFake.class);
        }
    }

    abstract static class ContactInfoListIteratorFake implements ContactInfoListIterator {
        @Override
        public boolean reportException(ContactInfo contactInfo, RuntimeException exception) {
            return false;
        }
    }

    abstract static class ORBDataFake implements ORBData {
        @Override
        public int getGIOPBufferSize() {
            return BUFFER_SIZE;
        }

        @Override
        public int getGIOPBuffMgrStrategy(GIOPVersion gv) {
            return BufferManagerFactory.GROW;
        }
    }

    abstract static class PIHandlerFake implements PIHandler {
        private ORBFake orb;

        public PIHandlerFake(ORBFake orb) {
            this.orb = orb;
        }

        @Override
        public Exception invokeClientPIEndingPoint(int replyStatus, Exception exception) {
            return orb.piEndingPointException == null ? exception : orb.piEndingPointException;
        }
    }

    abstract static class MessageMediatorFake implements MessageMediator {
        private ORB orb;
        private RuntimeException exception = new RuntimeException("test");

        public MessageMediatorFake(ORB orb) {
            this.orb = orb;
        }

        @Override
        public void finishSendingRequest() {

        }

        @Override
        public ORB getBroker() {
            return orb;
        }

        @Override
        public Connection getConnection() {
            return null;
        }

        @Override
        public ContactInfo getContactInfo() {
            return null;
        }

        @Override
        public GIOPVersion getGIOPVersion() {
            return GIOPVersion.DEFAULT_VERSION;
        }

        @Override
        public String getOperationName() {
            return "test";
        }

        @Override
        public int getRequestId() {
            return 0;
        }

        @Override
        public boolean isOneWay() {
            return false;
        }

        @Override
        public CDRInputObject waitForResponse() {
            if (exception != null) throw exception;
            return null;
        }
    }

    abstract static class MessageFake implements Message {
        @Override
        public byte getEncodingVersion() {
            return ORBConstants.CDR_ENC_VERSION;
        }
    }
}
