package com.sun.corba.ee.impl.protocol;
/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright (c) 2012 Oracle and/or its affiliates. All rights reserved.
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
import com.sun.corba.ee.impl.protocol.giopmsgheaders.FragmentMessage_1_2;
import com.sun.corba.ee.impl.protocol.giopmsgheaders.LocateRequestMessage_1_0;
import com.sun.corba.ee.impl.protocol.giopmsgheaders.LocateRequestMessage_1_2;
import com.sun.corba.ee.impl.protocol.giopmsgheaders.Message;
import com.sun.corba.ee.impl.protocol.giopmsgheaders.ReplyMessage_1_1;
import com.sun.corba.ee.impl.protocol.giopmsgheaders.ReplyMessage_1_2;
import com.sun.corba.ee.impl.protocol.giopmsgheaders.RequestMessage_1_0;
import com.sun.corba.ee.impl.protocol.giopmsgheaders.RequestMessage_1_2;
import com.sun.corba.ee.impl.transport.MessageTraceManagerImpl;
import com.sun.corba.ee.spi.ior.iiop.GIOPVersion;
import com.sun.corba.ee.spi.orb.ORB;
import com.sun.corba.ee.spi.orb.ORBData;
import com.sun.corba.ee.spi.protocol.MessageMediator;
import com.sun.corba.ee.spi.protocol.MessageParser;
import com.sun.corba.ee.spi.transport.Connection;
import com.sun.corba.ee.spi.transport.MessageTraceManager;
import com.sun.corba.ee.spi.transport.TransportManager;
import org.glassfish.simplestub.SimpleStub;
import org.glassfish.simplestub.Stub;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;

import java.nio.ByteBuffer;

import static org.junit.Assert.*;

public class MessageParserTest {

    private static final int BUFFER_SIZE = 1357;  // pick a bizarre number that should not occur randomly

    private ORBDataFake orbData = Stub.create(ORBDataFake.class);
    private ORBFake orb = Stub.create(ORBFake.class);
    private ConnectionFake connection = Stub.create(ConnectionFake.class);
    private TransportManagerFake transportManager = Stub.create(TransportManagerFake.class);

    private MessageParser parser;

    @Before
    public void setUp() throws Exception {
        orb.orbData = orbData;
        orb.transportManager = transportManager;
        parser = new MessageParserImpl(orb, connection);
    }

    @Test
    public void oldwhenBufferDoesNotContainEntireHeader_requestMore() {
        byte[] header = {'G', 'I', 'O', 'P', 1, 0 };
        ByteBuffer buffer = ByteBuffer.wrap(header);

        Message message = parser.parseBytes(buffer, connection);

        assertFalse(parser.hasMoreBytesToParse());
        assertTrue(parser.isExpectingMoreData());
        assertEquals(BUFFER_SIZE, parser.getSizeNeeded());
        assertEquals(0, parser.getNextMessageStartPosition());
        assertNull(message);
        assertSame(buffer, parser.getRemainderBuffer());
    }

    @Test
    public void whenBufferDoesNotContainEntireHeader_requestMoreAndDoNotCreateMediator() {
        byte[] header = {'G', 'I', 'O', 'P', 1, 0 };
        ByteBuffer buffer = ByteBuffer.wrap(header);

        parser.offerBuffer(buffer);

        assertSame(buffer, parser.getRemainderBuffer());
        assertNull(parser.getMessageMediator());
    }

    @Test
    public void oldwhenBufferContainsHeaderOnly_requestMore() {
        byte[] header = {'G', 'I', 'O', 'P', 1, 1, Message.FLAG_NO_FRAG_BIG_ENDIAN, Message.GIOPCancelRequest, 0, 0, 0, 6, 1, 2 };
        ByteBuffer buffer = ByteBuffer.wrap(header);

        Message message = parser.parseBytes(buffer, connection);

        assertFalse(parser.hasMoreBytesToParse());
        assertTrue(parser.isExpectingMoreData());
        assertEquals(18, parser.getSizeNeeded());
        assertEquals(0, parser.getNextMessageStartPosition());
        assertNull(message);
        assertSame(buffer, parser.getRemainderBuffer());
    }

    @Test
    public void whenBufferContainsHeaderOnly_requestMoreAndDoNotCreateMediator() {
        byte[] header = {'G', 'I', 'O', 'P', 1, 1, Message.FLAG_NO_FRAG_BIG_ENDIAN, Message.GIOPCancelRequest, 0, 0, 0, 6, 1, 2 };
        ByteBuffer buffer = ByteBuffer.wrap(header);

        parser.offerBuffer(buffer);

        assertSame(buffer, parser.getRemainderBuffer());
        assertNull(parser.getMessageMediator());
    }

    @Test
    public void old_whenBufferContainsWholeMessage_consumeEntireBuffer() {
        byte[] header = {'G', 'I', 'O', 'P', 1, 2, Message.FLAG_NO_FRAG_BIG_ENDIAN, Message.GIOPReply, 0, 0, 0, 6, 1, 2, 3, 4, 5, 6 };
        ByteBuffer buffer = ByteBuffer.wrap(header);

        Message message = parser.parseBytes(buffer, connection);

        assertFalse(parser.hasMoreBytesToParse());
        assertFalse(parser.isExpectingMoreData());
        assertEquals(true, message instanceof ReplyMessage_1_2);
        assertEquals(header.length, parser.getMsgByteBuffer().limit());
        assertEquals(0, parser.getRemainderBuffer().remaining());
    }

    @Test
    public void whenBufferContainsWholeMessage_consumeEntireBuffer() {
        byte[] header = {'G', 'I', 'O', 'P', 1, 2, Message.FLAG_NO_FRAG_BIG_ENDIAN, Message.GIOPReply, 0, 0, 0, 6, 1, 2, 3, 4, 5, 6 };
        ByteBuffer buffer = ByteBuffer.wrap(header);

        parser.offerBuffer(buffer);
        assertNull(parser.getRemainderBuffer());
        MessageMediator mediator = parser.getMessageMediator();
        assertNotNull(mediator);
        assertTrue(mediator.getDispatchHeader() instanceof ReplyMessage_1_2);
    }

    @Test
    public void oldwhenBufferContainsRestOfMessage_consumeEntireBuffer() {
        byte[] partMessage = {'G', 'I', 'O', 'P', 1, 0, Message.FLAG_NO_FRAG_BIG_ENDIAN, Message.GIOPRequest, 0, 0, 0, 6, 1 };
        byte[] wholeMessage = {'G', 'I', 'O', 'P', 1, 0, Message.FLAG_NO_FRAG_BIG_ENDIAN, Message.GIOPRequest, 0, 0, 0, 6, 1, 2, 3, 4, 5, 6 };
        ByteBuffer buffer = ByteBuffer.wrap(partMessage);
        parser.parseBytes(buffer, connection);

        buffer = ByteBuffer.wrap(wholeMessage);
        Message message = parser.parseBytes(buffer, connection);

        assertFalse(parser.hasMoreBytesToParse());
        assertFalse(parser.isExpectingMoreData());
        assertEquals(true, message instanceof RequestMessage_1_0);
        assertEquals(wholeMessage.length, parser.getMsgByteBuffer().limit());
        assertEquals(0, parser.getRemainderBuffer().remaining());
    }

    @Test
    public void whenBufferContainsRestOfMessage_consumeEntireBuffer() {
        byte[] partMessage = {'G', 'I', 'O', 'P', 1, 0, Message.FLAG_NO_FRAG_BIG_ENDIAN, Message.GIOPRequest, 0, 0, 0, 6, 1 };
        byte[] wholeMessage = {'G', 'I', 'O', 'P', 1, 0, Message.FLAG_NO_FRAG_BIG_ENDIAN, Message.GIOPRequest, 0, 0, 0, 6, 1, 2, 3, 4, 5, 6 };
        parser.offerBuffer(ByteBuffer.wrap(partMessage));

        parser.offerBuffer(ByteBuffer.wrap(wholeMessage));
        assertNull(parser.getRemainderBuffer());
        MessageMediator mediator = parser.getMessageMediator();
        assertNotNull(mediator);
        assertTrue(mediator.getDispatchHeader() instanceof RequestMessage_1_0);
    }

    @Test
    public void oldwhenBufferContainsWholeMessagePlusMore_consumeMessageAndLeaveMore() {
        byte[] header = {'G', 'I', 'O', 'P', 1, 1, Message.FLAG_NO_FRAG_BIG_ENDIAN, Message.GIOPReply, 0, 0, 0, 6, 1, 2, 3, 4, 5, 6, 'G' };
        ByteBuffer buffer = ByteBuffer.wrap(header);

        Message message = parser.parseBytes(buffer, connection);

        assertTrue(parser.hasMoreBytesToParse());
        assertFalse(parser.isExpectingMoreData());
        assertTrue(message instanceof ReplyMessage_1_1);
        assertEquals(18, parser.getMsgByteBuffer().limit());
        assertEquals(1, parser.getRemainderBuffer().remaining());
//        assertEquals(1, parser.getRemainderBuffer().limit());
//        assertEquals(0, parser.getNextMessageStartPosition());
    }

    @Test
    public void whenBufferContainsWholeMessageAndMore_consumeMessageBytesAndLeaveRemainder() {
        byte[] header = {'G', 'I', 'O', 'P', 1, 1, Message.FLAG_NO_FRAG_BIG_ENDIAN, Message.GIOPReply, 0, 0, 0, 6,
                          1, 2, 3, 4, 5, 6,
                         'R', 'M', 'I' };
        ByteBuffer buffer = ByteBuffer.wrap(header);

        parser.offerBuffer(buffer);
        assertNotNull(parser.getRemainderBuffer());
        assertEquals(3, parser.getRemainderBuffer().remaining());
        assertEquals('R', parser.getRemainderBuffer().get(0));
        MessageMediator mediator = parser.getMessageMediator();
        assertNotNull(mediator);
        assertTrue(mediator.getDispatchHeader() instanceof ReplyMessage_1_1);
    }

    @Test
    public void oldwhenBufferContainsWholeMessageNeedingFragments_consumeEntireBufferAndExpectMore() {
        byte[] header = {'G', 'I', 'O', 'P', 1, 1, Message.MORE_FRAGMENTS_BIT, Message.GIOPReply, 0, 0, 0, 6, 1, 2, 3, 4, 5, 6 };
        ByteBuffer buffer = ByteBuffer.wrap(header);

        Message message = parser.parseBytes(buffer, connection);

        assertFalse(parser.hasMoreBytesToParse());
        assertTrue(parser.isExpectingMoreData());
        assertTrue(message instanceof ReplyMessage_1_1);
        assertEquals(header.length, parser.getMsgByteBuffer().limit());
    }

    @Test
    public void oldwhenBufferContainsFinalFragment_consumeBuffer() {
        byte[] header = {'G', 'I', 'O', 'P', 1, 2, Message.MORE_FRAGMENTS_BIT, Message.GIOPRequest, 0, 0, 0, 6, 1, 2, 3, 4, 5, 6,
                         'G', 'I', 'O', 'P', 1, 2, Message.FLAG_NO_FRAG_BIG_ENDIAN, Message.GIOPFragment, 0, 0, 0, 4, 1, 2, 3, 4, 5, 6};
        ByteBuffer buffer = ByteBuffer.wrap(header);

        Message message1 = parser.parseBytes(buffer, connection);
        Message message2 = parser.parseBytes(buffer, connection);

        assertTrue(parser.hasMoreBytesToParse());
        assertFalse(parser.isExpectingMoreData());
        assertEquals(34, parser.getNextMessageStartPosition());
        assertTrue(message1 instanceof RequestMessage_1_2);
        assertTrue(message2 instanceof FragmentMessage_1_2);
    }

    @Test
    public void oldwhenStartPositionNonZero_startReadingFromPosition() {
        byte[] header = {0, 0, 'G', 'I', 'O', 'P', 1, 2, Message.FLAG_NO_FRAG_BIG_ENDIAN, Message.GIOPLocateRequest, 0, 0, 0, 6, 1, 2, 3, 4, 5, 6, 'G' };
        ByteBuffer buffer = ByteBuffer.wrap(header);
        buffer.position(2);
        parser.setNextMessageStartPosition(2);

        Message message = parser.parseBytes(buffer, connection);

        assertTrue(parser.hasMoreBytesToParse());
        assertEquals(20, parser.getNextMessageStartPosition());
        assertFalse(parser.isExpectingMoreData());
        assertTrue(message instanceof LocateRequestMessage_1_2);
        assertEquals(18, parser.getMsgByteBuffer().limit());
    }

    @Test
    public void whenToStringInvoked_stateIsReported() {
        assertTrue( parser.toString().contains("expectingMoreData=false"));
    }

    @SimpleStub(strict=true)
    static abstract class ORBDataFake implements ORBData {
        private GIOPVersion giopVersion = GIOPVersion.V1_2;

        @Override
        public int getReadByteBufferSize() {
            return BUFFER_SIZE;
        }

        @Override
        public GIOPVersion getGIOPVersion() {
            return giopVersion;
        }
    }

    @SimpleStub(strict=true)
    static abstract class ORBFake extends ORB {
        private ORBData orbData;
        private TransportManager transportManager;

        @Override
        public ORBData getORBData() {
            return orbData;
        }

        @Override
        public TransportManager getTransportManager() {
            return transportManager;
        }
    }

    @SimpleStub(strict=true)
    static abstract class ConnectionFake implements Connection {
    }

    @SimpleStub(strict=true)
    static abstract class TransportManagerFake implements TransportManager {
        private MessageTraceManager mtm = new MessageTraceManagerImpl();
        @Override
        public MessageTraceManager getMessageTraceManager() {
            return mtm;
        }
    }
}
