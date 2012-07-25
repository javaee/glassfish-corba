package com.sun.corba.ee.impl.encoding;
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

import com.sun.corba.ee.impl.orb.ORBImpl;
import com.sun.corba.ee.impl.protocol.giopmsgheaders.FragmentMessage;
import com.sun.corba.ee.impl.protocol.giopmsgheaders.Message;
import com.sun.corba.ee.impl.transport.MessageTraceManagerImpl;
import com.sun.corba.ee.spi.ior.iiop.GIOPVersion;
import com.sun.corba.ee.spi.misc.ORBConstants;
import com.sun.corba.ee.spi.orb.ORB;
import com.sun.corba.ee.spi.orb.ORBData;
import com.sun.corba.ee.spi.orb.ORBVersion;
import com.sun.corba.ee.spi.orb.ORBVersionFactory;
import com.sun.corba.ee.spi.protocol.MessageMediator;
import com.sun.corba.ee.spi.transport.ByteBufferPool;
import com.sun.corba.ee.spi.transport.Connection;
import com.sun.corba.ee.spi.transport.MessageTraceManager;
import com.sun.corba.ee.spi.transport.TransportManager;
import com.sun.org.apache.xerces.internal.impl.dv.util.HexBin;
import com.sun.org.omg.SendingContext.CodeBase;
import org.glassfish.simplestub.SimpleStub;
import org.glassfish.simplestub.Stub;
import org.junit.Before;
import org.omg.CORBA.portable.OutputStream;
import org.omg.CORBA.portable.ValueFactory;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.ArrayList;
import java.util.List;

import static com.sun.corba.ee.impl.encoding.EncodingTestBase.Endian.*;
import static com.sun.corba.ee.impl.encoding.EncodingTestBase.Fragments.*;
import static com.sun.corba.ee.spi.ior.iiop.GIOPVersion.V1_0;
import static com.sun.corba.ee.spi.ior.iiop.GIOPVersion.V1_1;
import static com.sun.corba.ee.spi.ior.iiop.GIOPVersion.V1_2;
import static org.junit.Assert.*;

public class EncodingTestBase {
    protected static final byte REQUEST = 0;
    protected static final int ISO_8859_1 = OSFCodeSetRegistry.ISO_8859_1.getNumber();
    protected static final int UTF_8 = OSFCodeSetRegistry.UTF_8.getNumber();
    protected static final int UTF_16 = OSFCodeSetRegistry.UTF_16.getNumber();
    protected static final byte FE = -2;
    protected static final byte FF = -1;
    protected static final int PAD = 0;  // use for output tests only, to make comparison possible

    private ORBDataFake orbData = Stub.create(ORBDataFake.class);
    private ORBFake orb = Stub.create(ORBFake.class);
    private ConnectionFake connection = Stub.create(ConnectionFake.class);
    private MessageFake message = Stub.create(MessageFake.class);
    private MessageFake fragment = Stub.create(MessageFake.class);
    private ByteBufferPoolFake pool = Stub.create(ByteBufferPoolFake.class);
    private MessageMediatorFake mediator = Stub.create(MessageMediatorFake.class);
    private TransportManagerFake transportManager = Stub.create(TransportManagerFake.class);

    private CDRInputObject inputObject;
    private CDROutputObject outputObject;
    private byte formatVersion = ORBConstants.STREAM_FORMAT_VERSION_1;

    private List<byte[]> fragments = new ArrayList<byte[]>();

    static byte flags(Endian endian, Fragments fragments) {
        byte result = 0;
        if (endian == little_endian) result |= 0x01;
        if (fragments == more_fragments) result |= 0x02;
        return result;
    }

    /** Returns a random value to ensure that the test never reads it. **/
    static byte pad() {
        return (byte) ((int) (Math.random() * 256));
    }

    @Before
    public void setUp() throws Exception {
        orb.setORBData(orbData);
        orb.setByteBufferPool(pool);
        orb.transportManager = transportManager;
        mediator.setConnection(connection);
        connection.fragments = fragments;
    }

    protected final ORB getOrb() {
        return orb;
    }

    protected final void useRepId() {
        orbData.useRepId = true;
    }

    protected final void useEnumDesc() {
        orbData.useEnumDesc = true;
    }

    protected final void setBufferSize(int size) {
        orbData.giopBufferSize = size;
    }

    protected final void setFragmentSize(int size) {
        orbData.giopFragmentSize = size;
    }

    protected final void useStreamFormatVersion1() {
        formatVersion = ORBConstants.STREAM_FORMAT_VERSION_1;
    }

    protected final void useStreamFormatVersion2() {
        formatVersion = ORBConstants.STREAM_FORMAT_VERSION_2;
    }

    protected final void setCharEncoding(int encoding) {
        connection.setCharEncoding(encoding);
    }

    protected final void setWCharEncoding(int encoding) {
        connection.setWCharEncoding(encoding);
    }

    protected final EncapsInputStream createEncapsulatedInputStream(int... contents) {
        byte[] bytes = new byte[contents.length];
        for (int i = 0; i < contents.length; i++)
            bytes[i] = (byte) contents[i];
        return new EncapsInputStream(orb, bytes, bytes.length, getByteOrder(), message.giopVersion);
    }

    private ByteOrder getByteOrder() {
        return isLittleEndian() ? ByteOrder.LITTLE_ENDIAN : ByteOrder.BIG_ENDIAN;
    }

    protected final CDRInputObject getInputObject() {
        if (inputObject == null)
            inputObject = createInputObject();
        return inputObject;
    }

    private CDRInputObject createInputObject() {
        CDRInputObject inputObject = new CDRInputObject(orb, connection, getByteBuffer(), message);
        inputObject.performORBVersionSpecificInit();
        return inputObject;
    }

    protected final void useV1_0() {
        message.giopVersion = V1_0;
    }

    protected final void useV1_1() {
        message.giopVersion = V1_1;
    }

    protected final void useV1_2() {
        message.giopVersion = V1_2;
    }

    protected final void useLittleEndian() {
        message.endian = little_endian;
    }

    private boolean isLittleEndian() {
        return message.endian == little_endian;
    }

    protected final void setOrbVersion(ORBVersion version) {
        orb.setORBVersion(version);
    }

    protected final void addFragment(int... values) {
        fragment.body = new byte[values.length];
        for (int i = 0; i < values.length; i++)
            fragment.body[i] = (byte) (values[i]);
        getInputObject().addFragment(fragment, ByteBuffer.wrap(fragment.getMessageData()));
    }

    /**
     * Sets the message 'more fragments coming' flag.
     */
    protected final void expectMoreFragments() {
        message.fragments = more_fragments;
    }

    protected final void whileWaitingForFragmentsDo(AsynchronousAction asynchronousAction) {
        orbData.asynchronousAction = asynchronousAction;
    }

    private ByteBuffer getByteBuffer() {
        return ByteBuffer.wrap(message.getMessageData());
    }

    protected final void setMessageBody(int... values) {
        message.body = new byte[values.length];
        for (int i = 0; i < values.length; i++)
            message.body[i] = (byte) values[i];
    }

    protected final void setMessageBody(byte[] values) {
        message.body = values.clone();
    }

    protected final int getNumBuffersReleased() {
        return pool.getNumBuffersReleased();
    }

    // Note: the tests assume that the buffer contents start after the header. For the output logic, the object is created
    // positioned after the header, so the comparison must skip that.

    protected final CDROutputObject getOutputObject() {
        if (outputObject == null)
            outputObject = createOutputObject();
        return outputObject;
    }

    private CDROutputObject createOutputObject() {
        CDROutputObject outputObject = new CDROutputObject(orb, mediator, message.giopVersion, connection, message, formatVersion);
        outputObject.setIndex(Message.GIOPMessageHeaderLength);
        return outputObject;
    }

    protected final void expectByteArray(byte... expected) {
        getOutputObject().finishSendingMessage();
        assertEquals(1, fragments.size());
        expectFragment(0, expected);
    }

    private void expectFragment(int index, byte[] expected) {
        byte[] actual = subBuffer(fragments.get(index), Message.GIOPMessageHeaderLength);
        try {
            assertArrayEquals(expected, actual);
        } catch (AssertionError e) {
            System.out.println("expected: " + HexBin.encode(expected));
            System.out.println("  actual: " + HexBin.encode(actual));
            throw e;
        }
    }

    protected final void expectByteArrays(byte[]... expected) {
        getOutputObject().getBufferManager().sendMessage();
        assertEquals(expected.length, fragments.size());
        for (int i = 0; i < expected.length; i++)
            expectFragment(i, expected[i]);
    }

    private byte[] subBuffer(byte[] input, int start) {
        byte[] result = new byte[input.length-start];
        System.arraycopy(input, start, result, 0, result.length);
        return result;
    }

    protected final void expectByteArray(int... expected) {
        byte[] bytes = new byte[expected.length];
        for (int i = 0; i < expected.length; i++)
            bytes[i] = (byte) (expected[i]);
        expectByteArray(bytes);
    }

    enum Endian {big_endian, little_endian}

    enum Fragments {no_more_fragments, more_fragments}

    interface AsynchronousAction {
        void exec();
    }


    //--------------------------------- fake implementation of a TransportManager --------------------------------------

    @SimpleStub(strict=true)
    static abstract class TransportManagerFake implements TransportManager {
        @Override
        public MessageTraceManager getMessageTraceManager() {
            return new MessageTraceManagerImpl();
        }
    }

    //-------------------------------------- fake implementation of an ORBData -----------------------------------------

    @SimpleStub(strict=true)
    static abstract class ORBDataFake implements ORBData {
        private AsynchronousAction asynchronousAction;
        private int giopBufferSize = 250;
        private boolean useRepId;
        private boolean useEnumDesc;
        private int giopFragmentSize = 250;

        @Override
        public int fragmentReadTimeout() {
            if (asynchronousAction != null) asynchronousAction.exec();
            return 1;
        }

        @Override
        public int getGIOPBuffMgrStrategy(GIOPVersion gv) {
            return gv.equals(GIOPVersion.V1_0) ? BufferManagerFactory.GROW : BufferManagerFactory.STREAM;
        }

        @Override
        public int getGIOPBufferSize() {
            return giopBufferSize;
        }

        @Override
        public boolean useByteOrderMarkers() {
            return true;
        }

        @Override
        public boolean useRepId() {
            return useRepId;
        }

        @Override
        public boolean useEnumDesc() {
            return useEnumDesc;
        }

        @Override
        public int getGIOPFragmentSize() {
            return giopFragmentSize;
        }
    }

    //----------------------------------- fake implementation of a ByteBufferPool --------------------------------------

    @SimpleStub(strict=true)
    static abstract class ByteBufferPoolFake implements ByteBufferPool {
        private List<ByteBuffer> buffers = new ArrayList<ByteBuffer>();

        protected int getNumBuffersReleased() {
            return buffers.size();
        }

        @Override
        public void releaseByteBuffer(ByteBuffer buffer) {
            buffers.add(buffer);
        }

        @Override
        public ByteBuffer getByteBuffer(int theSize) {
            return ByteBuffer.allocate(theSize);
        }
    }

    //---------------------------------------- fake implementation of the ORB ------------------------------------------

    @SimpleStub(strict = true)
    static abstract class ORBFake extends ORBImpl {
        private ORBDataFake orbData;
        private ORBVersion version = ORBVersionFactory.getFOREIGN();
        private ByteBufferPool pool;
        private TransportManager transportManager = null;

        protected ORBFake() {
            initializePrimitiveTypeCodeConstants();
        }

        void setORBData(ORBDataFake orbData) {
            this.orbData = orbData;
        }

        public void setORBVersion(ORBVersion version) {
            this.version = version;
        }

        @Override
        public ORBVersion getORBVersion() {
            return version;
        }

        @Override
        public ORBData getORBData() {
            return orbData;
        }

        @Override
        public ByteBufferPool getByteBufferPool() {
            return pool;
        }

        @Override
        public ValueFactory lookup_value_factory(String repID) {
            return null;
        }

        public void setByteBufferPool(ByteBufferPool pool) {
            this.pool = pool;
        }

        @Override
        public TransportManager getTransportManager() {
            return transportManager;
        }
    }

    //------------------------------------- fake implementation of a Connection ----------------------------------------

    @SimpleStub(strict = true)
    static abstract class ConnectionFake implements Connection {
        int char_encoding = ISO_8859_1;
        int wchar_encoding = UTF_16;
        boolean locked;
        private CodeSetComponentInfo.CodeSetContext codeSets;
        List<byte[]> fragments;

        void setCharEncoding(int char_encoding) {
            this.char_encoding = char_encoding;
            codeSets = new CodeSetComponentInfo.CodeSetContext(char_encoding, wchar_encoding);
        }

        void setWCharEncoding(int wchar_encoding) {
            this.wchar_encoding = wchar_encoding;
            codeSets = new CodeSetComponentInfo.CodeSetContext(char_encoding, wchar_encoding);
        }

        @Override
        public CodeSetComponentInfo.CodeSetContext getCodeSetContext() {
            if (codeSets == null)
                codeSets = new CodeSetComponentInfo.CodeSetContext(char_encoding, wchar_encoding);
            return codeSets;
        }

        @Override
        public CodeBase getCodeBase() {
            return null;
        }

        @Override
        public boolean hasSocketChannel() {
            return true;
        }

        @Override
        public void writeLock() {
            locked = true;
        }

        @Override
        public void writeUnlock() {
            locked = false;
        }

        @Override
        public void sendWithoutLock(CDROutputObject outputObject) {
            try {
                if (!locked) fail("sendWithoutLock called while connection is not locked");
                outputObject.writeTo(this);
            } catch (IOException e) {
                fail("Connection reported: " + e);
            }
        }

        @Override
        public void write(ByteBuffer byteBuffer) throws IOException {
            byte[] buf = new byte[byteBuffer.remaining()];
            byteBuffer.get(buf);
            fragments.add(buf);
        }
    }

    //---------------------------------- fake implementation of a Message Mediator -------------------------------------

    @SimpleStub(strict = true)
    static abstract class MessageMediatorFake implements MessageMediator {

        private Connection connection;
        private CDRInputObject inputObject;
        private CDROutputObject outputObject;

        public void setConnection(Connection connection) {
            this.connection = connection;
        }

        @Override
        public Connection getConnection() {
            return connection;
        }

        @Override
        public void setInputObject(CDRInputObject inputObject) {
            this.inputObject = inputObject;
        }

        @Override
        public CDRInputObject getInputObject() {
            return inputObject;
        }

        @Override
        public void setOutputObject(CDROutputObject outputObject) {
            this.outputObject = outputObject;
        }

        @Override
        public CDROutputObject getOutputObject() {
            return outputObject;
        }
    }

    //--------------------------------------- fake implementation of a Message -----------------------------------------

    @SimpleStub(strict = true)
    static abstract class MessageFake implements FragmentMessage {
        Endian endian = big_endian;
        Fragments fragments = no_more_fragments;
        private GIOPVersion giopVersion = V1_2;
        private byte messageType = REQUEST;
        byte[] body;
        byte[] data;
        int headerIndex = 0;
        int sizeInHeader = -1;
        private boolean startedNewMessage;

        byte[] getMessageData() {
            if (data != null) return data;

            if (body == null) throw new RuntimeException("No message body defined");
            data = new byte[body.length + getHeaderLength()];
            System.arraycopy(body, 0, data, getHeaderLength(), body.length);
            copyToHeader((byte) 'G', (byte) 'I', (byte) 'O', (byte) 'P');
            copyToHeader(giopVersion.getMajor(), giopVersion.getMinor());
            copyToHeader(flags(endian, fragments), messageType);
            copyToHeader(body.length);
            return data;
        }

        private void copyToHeader(int value) {
            data[headerIndex++] = (byte) (0xFF & value >> 24);
            data[headerIndex++] = (byte) (0xFF & value >> 16);
            data[headerIndex++] = (byte) (0xFF & value >> 8);
            data[headerIndex++] = (byte) (0xFF & value);
        }

        private void copyToHeader(byte... bytes) {
            for (byte aByte : bytes) {
                data[headerIndex++] = aByte;
            }
        }

        public int getHeaderLength() {
            return Message.GIOPMessageHeaderLength;
        }

        @Override
        public int getSize() {
            return sizeInHeader >=0 ? sizeInHeader : getMessageData().length;
        }

        @Override
        public boolean isLittleEndian() {
            return endian == little_endian;
        }

        @Override
        public GIOPVersion getGIOPVersion() {
            return giopVersion;
        }

        @Override
        public byte getEncodingVersion() {
            return 0; // not actually used
        }

        @Override
        public boolean moreFragmentsToFollow() {
            return fragments == more_fragments;
        }

        @Override
        public void setSize(ByteBuffer byteBuffer, int size) {
            sizeInHeader = size;
        }

        @Override
        public FragmentMessage createFragmentMessage() {
            return Stub.create(MessageFake.class);
        }

        public int getSizeInHeader() {
            return sizeInHeader;
        }

        @Override
        public void write(OutputStream ostream) {
            startedNewMessage = true;
            for (int i = 0; i < GIOPMessageHeaderLength; i++)
                ostream.write_octet((byte) 0);
        }
    }
}
