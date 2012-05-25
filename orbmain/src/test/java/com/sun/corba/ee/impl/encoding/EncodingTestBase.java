package com.sun.corba.ee.impl.encoding;

import com.sun.corba.ee.impl.protocol.giopmsgheaders.FragmentMessage;
import com.sun.corba.ee.impl.protocol.giopmsgheaders.Message;
import com.sun.corba.ee.spi.ior.iiop.GIOPVersion;
import com.sun.corba.ee.spi.orb.ORB;
import com.sun.corba.ee.spi.orb.ORBData;
import com.sun.corba.ee.spi.orb.ORBVersion;
import com.sun.corba.ee.spi.orb.ORBVersionFactory;
import com.sun.corba.ee.spi.transport.ByteBufferPool;
import com.sun.corba.ee.spi.transport.Connection;
import org.glassfish.simplestub.SimpleStub;
import org.glassfish.simplestub.Stub;
import org.junit.Before;

import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.List;

import static com.sun.corba.ee.impl.encoding.EncodingTestBase.Endian.*;
import static com.sun.corba.ee.impl.encoding.EncodingTestBase.Fragments.*;
import static com.sun.corba.ee.spi.ior.iiop.GIOPVersion.V1_0;
import static com.sun.corba.ee.spi.ior.iiop.GIOPVersion.V1_1;
import static com.sun.corba.ee.spi.ior.iiop.GIOPVersion.V1_2;

public class EncodingTestBase {
    protected static final byte REQUEST = 0;
    protected static final int ISO_8859_1 = OSFCodeSetRegistry.ISO_8859_1.getNumber();
    protected static final int UTF_8 = OSFCodeSetRegistry.UTF_8.getNumber();
    protected static final int UTF_16 = OSFCodeSetRegistry.UTF_16.getNumber();
    protected static final int FE = 0xfe;
    protected static final int FF = 0xff;

    private ORBDataFake orbData = Stub.create(ORBDataFake.class);
    private ORBFake orb = Stub.create(ORBFake.class);
    private ConnectionFake connection = Stub.create(ConnectionFake.class);
    private MessageFake message = Stub.create(MessageFake.class);
    private MessageFake fragment = Stub.create(MessageFake.class);
    private ByteBufferPoolFake pool = Stub.create(ByteBufferPoolFake.class);

    private CDRInputObject inputObject;

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
    }

    protected final void setCharEncoding(int encoding) {
        connection.setCharEncoding(encoding);
    }

    protected final void setWCharEncoding(int encoding) {
        connection.setWCharEncoding(encoding);
    }

    protected final CDRInputObject getInputObject() {
        if (inputObject == null)
            inputObject = new CDRInputObject(orb, connection, getByteBuffer(), message);
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

    protected final int getNumBuffersReturned() {
        return pool.getNumBuffersReturned();
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

        @Override
        public int fragmentReadTimeout() {
            if (asynchronousAction != null) asynchronousAction.exec();
            return 1;
        }
    }

    //----------------------------------- fake implementation of a ByteBufferPool --------------------------------------

    @SimpleStub(strict=true)
    static abstract class ByteBufferPoolFake implements ByteBufferPool {
        private List<ByteBuffer> buffers = new ArrayList<ByteBuffer>();

        protected int getNumBuffersReturned() {
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
