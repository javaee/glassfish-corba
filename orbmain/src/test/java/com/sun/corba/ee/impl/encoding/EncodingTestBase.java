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

import com.sun.corba.ee.impl.protocol.giopmsgheaders.FragmentMessage;
import com.sun.corba.ee.impl.protocol.giopmsgheaders.Message;
import com.sun.corba.ee.spi.ior.iiop.GIOPVersion;
import com.sun.corba.ee.spi.misc.ORBConstants;
import com.sun.corba.ee.spi.orb.ORB;
import com.sun.corba.ee.spi.orb.ORBData;
import com.sun.corba.ee.spi.orb.ORBVersion;
import com.sun.corba.ee.spi.orb.ORBVersionFactory;
import com.sun.corba.ee.spi.protocol.MessageMediator;
import com.sun.corba.ee.spi.transport.ByteBufferPool;
import com.sun.corba.ee.spi.transport.Connection;
import com.sun.org.apache.xerces.internal.impl.dv.util.HexBin;
import com.sun.org.omg.SendingContext.CodeBase;
import org.glassfish.simplestub.SimpleStub;
import org.glassfish.simplestub.Stub;
import org.junit.Before;
import org.omg.CORBA.portable.ValueFactory;

import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static com.sun.corba.ee.impl.encoding.EncodingTestBase.Endian.*;
import static com.sun.corba.ee.impl.encoding.EncodingTestBase.Fragments.*;
import static com.sun.corba.ee.spi.ior.iiop.GIOPVersion.V1_0;
import static com.sun.corba.ee.spi.ior.iiop.GIOPVersion.V1_1;
import static com.sun.corba.ee.spi.ior.iiop.GIOPVersion.V1_2;
import static org.junit.Assert.assertArrayEquals;

public class EncodingTestBase {
    protected static final byte REQUEST = 0;
    protected static final int ISO_8859_1 = OSFCodeSetRegistry.ISO_8859_1.getNumber();
    protected static final int UTF_8 = OSFCodeSetRegistry.UTF_8.getNumber();
    protected static final int UTF_16 = OSFCodeSetRegistry.UTF_16.getNumber();
    protected static final int FE = 0xfe;
    protected static final int FF = 0xff;
    protected static final int PAD = 0;  // use for output tests only, to make comparison possible

    private ORBDataFake orbData = Stub.create(ORBDataFake.class);
    private ORBFake orb = Stub.create(ORBFake.class);
    private ConnectionFake connection = Stub.create(ConnectionFake.class);
    private MessageFake message = Stub.create(MessageFake.class);
    private MessageFake fragment = Stub.create(MessageFake.class);
    private ByteBufferPoolFake pool = Stub.create(ByteBufferPoolFake.class);
    private MessageMediatorFake mediator = Stub.create(MessageMediatorFake.class);

    private CDRInputObject inputObject;
    private CDROutputObject outputObject;
    private byte formatVersion = ORBConstants.STREAM_FORMAT_VERSION_1;

    static byte flags(Endian endian, Fragments fragments) {
        byte result = 0;
        if (endian == little_endian) result |= 0x01;
        if (fragments == more_fragments) result |= 0x02;
        return result;
    }

    /** Returns a random value to ensure that the test never reads it. **/
    static byte pad() {
        return (byte) (FF & (int) (Math.random() * 256));
    }

    @Before
    public void setUp() throws Exception {
        orb.setORBData(orbData);
        orb.setByteBufferPool(pool);
        mediator.setConnection(connection);
    }

    protected final void useRepId() {
        orbData.useRepId = true;
    }

    protected final void useEnumDesc() {
        orbData.useEnumDesc = true;
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
            bytes[i] = (byte) (FF & contents[i]);
        return new EncapsInputStream(orb, bytes, bytes.length, isLittleEndian(), message.giopVersion);
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
            fragment.body[i] = (byte) (FF & values[i]);
        getInputObject().addFragment(fragment, ByteBuffer.wrap(fragment.getMessageData()));
    }

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
            message.body[i] = (byte) (FF & values[i]);
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
        try {
            assertArrayEquals(expected, subBuffer(getOutputObject().toByteArray(), Message.GIOPMessageHeaderLength));
        } catch (AssertionError e) {
            System.out.println("expected: " + HexBin.encode(expected));
            System.out.println("  actual: " + HexBin.encode(subBuffer(getOutputObject().toByteArray(), Message.GIOPMessageHeaderLength)));
        }
    }

    private byte[] subBuffer(byte[] input, int start) {
        byte[] result = new byte[input.length-start];
        System.arraycopy(input, start, result, 0, result.length);
        return result;
    }

    protected final void expectByteArray(int... expected) {
        byte[] bytes = new byte[expected.length];
        for (int i = 0; i < expected.length; i++)
            bytes[i] = (byte) (FF & expected[i]);
        expectByteArray(bytes);
    }

    enum Endian {big_endian, little_endian}

    enum Fragments {no_more_fragments, more_fragments}

    interface AsynchronousAction {
        void exec();
    }

    //-------------------------------------- fake implementation of an ORBData -----------------------------------------

    @SimpleStub(strict=true)
    static abstract class ORBDataFake implements ORBData {
        private AsynchronousAction asynchronousAction;
        private int giopBufferSize = 250;
        private boolean useRepId;
        private boolean useEnumDesc;

        @Override
        public int fragmentReadTimeout() {
            if (asynchronousAction != null) asynchronousAction.exec();
            return 1;
        }

        @Override
        public int getGIOPBuffMgrStrategy(GIOPVersion gv) {
            return 0;
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
    }

    //---------------------------------------- fake implementation of the ORB ------------------------------------------

    @SimpleStub(strict = true)
    static abstract class ORBFake extends ORB {
        private ORBDataFake orbData;
        private ORBVersion version = ORBVersionFactory.getFOREIGN();
        private ByteBufferPool pool;

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
    }

    //------------------------------------- fake implementation of a Connection ----------------------------------------

    @SimpleStub(strict = true)
    static abstract class ConnectionFake implements Connection {
        int char_encoding = ISO_8859_1;
        int wchar_encoding = UTF_16;
        private CodeSetComponentInfo.CodeSetContext codeSets;

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
        public boolean shouldUseDirectByteBuffers() {
            return false;
        }
    }

    //---------------------------------- fake implementation of a Message Mediator -------------------------------------

    @SimpleStub(strict = true)
    static abstract class MessageMediatorFake implements MessageMediator {

        private Connection connection;

        public void setConnection(Connection connection) {
            this.connection = connection;
        }

        @Override
        public Connection getConnection() {
            return connection;
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
            return getMessageData().length;
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
    }
}
