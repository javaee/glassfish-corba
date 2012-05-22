package com.sun.corba.ee.impl.encoding;

import com.sun.corba.ee.impl.protocol.RequestCanceledException;
import com.sun.corba.ee.impl.protocol.giopmsgheaders.FragmentMessage;
import com.sun.corba.ee.impl.protocol.giopmsgheaders.Message;
import com.sun.corba.ee.spi.ior.iiop.GIOPVersion;
import com.sun.corba.ee.spi.orb.ORB;
import com.sun.corba.ee.spi.orb.ORBData;
import com.sun.corba.ee.spi.orb.ORBVersion;
import com.sun.corba.ee.spi.orb.ORBVersionFactory;
import com.sun.corba.ee.spi.transport.Connection;
import org.glassfish.simplestub.SimpleStub;
import org.glassfish.simplestub.Stub;
import org.junit.Before;
import org.junit.Test;
import org.omg.CORBA.COMM_FAILURE;
import org.omg.CORBA.MARSHAL;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.Arrays;

import static com.sun.corba.ee.impl.encoding.CDRInputTest.Endian.*;
import static com.sun.corba.ee.impl.encoding.CDRInputTest.Fragments.*;
import static com.sun.corba.ee.impl.encoding.CodeSetComponentInfo.*;
import static com.sun.corba.ee.spi.ior.iiop.GIOPVersion.*;
import static org.junit.Assert.*;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

public class CDRInputTest {

    final static byte REQUEST = 0;
    private static final int ISO_8859_1 = OSFCodeSetRegistry.ISO_8859_1.getNumber();
    private static final int UTF_8 = OSFCodeSetRegistry.UTF_8.getNumber();
    private static final int UTF_16 = OSFCodeSetRegistry.UTF_16.getNumber();

    private static final int FE = 0xfe;
    private static final int FF = 0xff;

    private ORBDataFake orbData = Stub.create(ORBDataFake.class);
    private ORBFake orb = Stub.create(ORBFake.class);
    private ConnectionFake connection = Stub.create(ConnectionFake.class);
    private MessageFake message = Stub.create(MessageFake.class);
    private MessageFake fragment = Stub.create(MessageFake.class);
    private CDRInputObject inputObject;

    enum Endian {big_endian, little_endian}

    ;

    enum Fragments {no_more_fragments, more_fragments}

    ;

    static byte flags(Endian endian, Fragments fragments) {
        byte result = 0;
        if (endian == little_endian) result |= 0x01;
        if (fragments == more_fragments) result |= 0x02;
        return result;
    }

    static byte pad() {
        return (byte) (FF & (int) (Math.random() * 256));
    }

    @Before
    public void setUp() throws Exception {
        orb.setORBData(orbData);
    }

    @Test
    public void whenCDRInputObjectCreated_canReadBoolean() throws IOException {
        setMessageBody(0, 1);
        assertFalse(getInputObject().read_boolean());
        assertTrue(getInputObject().read_boolean());
    }

    @Test
    public void whenCDRInputObjectCreated_canReadLatin1Char() throws IOException {
        connection.setCharEncoding(ISO_8859_1);
        setMessageBody('x');
        assertEquals('x', getInputObject().read_char());
    }

    @Test
    public void whenCDRInputObjectCreated_canReadUTF8Char() throws IOException {
        connection.setCharEncoding(UTF_8);
        setMessageBody('{');
        assertEquals('{', getInputObject().read_char());
    }

    @Test(expected = MARSHAL.class)
    public void whenCDRInputObjectCreated_cannotReadUTF16CharIn_1_0() throws IOException {
        connection.setWCharEncoding(UTF_16);
        setMessageBody(0x04, FE, FF, 0x34, 0x56);
        assertEquals('\u3456', getInputObject().read_wchar());
    }

    @Test
    public void whenCDRInputObjectCreated_canReadUTF16CharIn_1_1() throws IOException {
        connection.setWCharEncoding(UTF_16);
        message.giopVersion = V1_1;
        setMessageBody(0x34, 0x56);
        assertEquals('\u3456', getInputObject().read_wchar());
    }

    private CDRInputObject getInputObject() {
        if (inputObject == null)
            inputObject = new CDRInputObject(orb, connection, getByteBuffer(), message);
        return inputObject;
    }

    @Test
    public void whenCDRInputObjectCreated_canReadUTF16CharIn_1_2() throws IOException {
        connection.setWCharEncoding(UTF_16);
        useV1_2();
        setMessageBody(0x04, FE, FF, 0x34, 0x56);
        assertEquals('\u3456', getInputObject().read_wchar());
    }

    private void useV1_1() {
        message.giopVersion = V1_1;
    }

    private void useV1_2() {
        message.giopVersion = V1_2;
    }

    @Test
    public void whenCDRInputObjectCreated_canReadOctet() throws IOException {
        setMessageBody(25);
        CDRInputObject inputObject = getInputObject();
        assertEquals(25, inputObject.read_octet());
    }

    @Test
    public void can_read_integers() {
        int[] data = { /* octet */ 0x04, pad(), /* short*/ FF, (byte) 0xf2,
                /* short */ 0x00, 0x03,
                pad(), pad(),
                /* long1 */ 0, 1, 2, (byte) 0x83, /* long2 */ FF, FF, (byte) 0xfd, 0x71,
                pad(), pad(), pad(), pad(),
                /* long long */ 0, 0, 1, 0, 0, (byte) 0x80, 1, 7};
        setMessageBody(data);

        assertEquals("Octet value", 4, getInputObject().read_octet());
        assertEquals("Signed short value", -14, getInputObject().read_short());
        assertEquals("Standard unsigned short value", 3, getInputObject().read_ushort());
        assertEquals("Unsigned long value", 66179, getInputObject().read_ulong());
        assertEquals("Long value", -655, getInputObject().read_long());
        assertEquals("Long long value", 1099520016647L, getInputObject().read_longlong());
    }

    @Test
    public void can_read_little_endian_integers() {
        int[] data = { /* octet */ 0x04, pad(), /* short*/ 0xf2, FF,
                /* short */ 0x03, 0x00,
                pad(), pad(),
                /* long1 */ 0x83, 2, 1, 0, /* long2 */  0x71, 0xfd, FF, FF,
                pad(), pad(), pad(), pad(),
                /* long long */ 7, 1, (byte) 0x80, 0, 0, 1, 0, 0 };
        setMessageBody(data);
        message.endian = little_endian;

        assertEquals("Octet value", 4, getInputObject().read_octet());
        assertEquals("Signed short value", -14, getInputObject().read_short());
        assertEquals("Standard unsigned short value", 3, getInputObject().read_ushort());
        assertEquals("Unsigned long value", 66179, getInputObject().read_ulong());
        assertEquals("Long value", -655, getInputObject().read_long());
        assertEquals("Long long value", 1099520016647L, getInputObject().read_longlong());
    }


    @Test
    public void can_read_floats() {
        int[] data = {0x3f, (byte) 0x80, 0, 0,
                      0x3f, (byte) 0xd5, 0x55, 0x55, 0x55, 0x55, 0x55, 0x55};
        setMessageBody(data);

        assertEquals("Float", 1, getInputObject().read_float(), 0.001);
        assertEquals("Double", 0.33333, getInputObject().read_double(), 0.001);
    }

    @Test
    public void whenUsingV1_0_canReadCharString() {
        int[] data = {0, 0, 0, 11, 't', 'h', 'i', 's', ' ', 'w', 'o', 'r', 'k', 's', 0};
        setMessageBody(data);

        assertEquals("String value", "this works", getInputObject().read_string());
    }

    @Test(expected = MARSHAL.class)
    public void whenUsingV1_0_cannotReadWCharString() {
        int[] data = {0, 0, 0, 22, FE, FF,
                0, 'T', 0, 'h', 0, 'i', 0, 's', 0, ',', 0, ' ', 0, 't', 0, 'o', 0, 'o', 0, '!'};
        setMessageBody(data);
        assertEquals("Wide string value", "This, too!", getInputObject().read_wstring());
    }

    @Test
    public void whenUsingV1_1_canReadCharAndWCharStrings() {
        useV1_1();
        int[] data = {0, 0, 0, 11, 't', 'h', 'i', 's', ' ', 'w', 'o', 'r', 'k', 's', 0,
                pad(),
                0, 0, 0, 11,
                0, 'T', 0, 'h', 0, 'i', 0, 's', 0, ',', 0, ' ', 0, 't', 0, 'o', 0, 'o', 0, '!',
                0, 0,
                pad(), pad(), 0, 0, 0, 0};
        setMessageBody(data);

        assertEquals("String value", "this works", getInputObject().read_string());
        assertEquals("Wide string value", "This, too!", getInputObject().read_wstring());
        assertEquals("position before reading empty string", 54, getInputObject().getPosition());
        assertEquals("Empty string value", "", getInputObject().read_wstring());
    }

    @Test
    public void whenUsingV1_2_canReadCharAndWCharStrings() {
        useV1_2();
        int[] data = {0, 0, 0, 11, 't', 'h', 'i', 's', ' ', 'w', 'o', 'r', 'k', 's', 0,
                pad(),
                0, 0, 0, 22, FE, FF,
                0, 'T', 0, 'h', 0, 'i', 0, 's', 0, ',', 0, ' ', 0, 't', 0, 'o', 0, 'o', 0, '!',
                pad(), pad(), 0, 0, 0, 0};
        setMessageBody(data);

        assertEquals("String value", "this works", getInputObject().read_string());
        assertEquals("Wide string value", "This, too!", getInputObject().read_wstring());
        assertEquals("Empty string value", "", getInputObject().read_wstring());
    }

    @Test
    public void can_read_boolean_array() throws Exception {
        final int[] data = {1, FF, 0, 7, 0};
        final boolean[] expected = {true, true, false, true, false};
        setMessageBody(data);
        readAndVerifyBooleanArray(expected);
    }

    private void readAndVerifyBooleanArray(boolean[] expected) {
        boolean[] actual = new boolean[expected.length];

        getInputObject().read_boolean_array(actual, 0, expected.length);

        if (!Arrays.equals(expected, actual)) {
            fail("Expected " + Arrays.toString(expected) + " but found " + Arrays.toString(actual));
        }
    }


    @Test
    public void can_read_octet_array() throws Exception {
        final int[] data = {0, 1, 2, 3, -1, -1};
        final byte[] expected = {0, 1, 2, 3, -1, -1};
        setMessageBody(data);
        readAndVerifyOctetArray(expected);
    }

    private void readAndVerifyOctetArray(byte[] expected) {
        byte[] actual = new byte[expected.length];

        getInputObject().read_octet_array(actual, 0, expected.length);

        assertArrayEquals("Octet array", expected, actual);
    }


    @Test
    public void can_read_short_array() throws Exception {
        final int[] data = {0, 1, 2, 3, -1, -1};
        final short[] expected = {1, 515, -1};
        setMessageBody(data);
        readAndVerifyShortArray(expected);
    }

    private void readAndVerifyShortArray(short[] expected) {
        short[] actual = new short[expected.length];

        getInputObject().read_short_array(actual, 0, expected.length);

        assertArrayEquals(expected, actual);
    }


    @Test
    public void can_read_ushort_array() throws Exception {
        final int[] data = {0, 1, 2, 3};
        final short[] expected = {1, 515};
        setMessageBody(data);
        readAndVerifyUshortArray(expected);
    }

    private void readAndVerifyUshortArray(short[] expected) {
        short[] actual = new short[expected.length];

        getInputObject().read_ushort_array(actual, 0, expected.length);

        assertArrayEquals(expected, actual);
    }


    @Test
    public void can_read_long_array() throws Exception {
        final int[] data = {0, 1, 2, 3, -1, -1, -3, 30};
        final int[] expected = {66051, -738};
        setMessageBody(data);
        readAndVerifyLongArray(expected);
    }

    private void readAndVerifyLongArray(int[] expected) {
        int[] actual = new int[expected.length];

        getInputObject().read_long_array(actual, 0, expected.length);

        assertArrayEquals(expected, actual);
    }


    @Test
    public void can_read_ulong_array() throws Exception {
        final int[] data = {0, 1, 2, 3, -1, -1, -3, 30};
        final int[] expected = {66051, -738};
        setMessageBody(data);
        readAndVerifyULongArray(expected);
    }

    private void readAndVerifyULongArray(int[] expected) {
        int[] actual = new int[expected.length];

        getInputObject().read_ulong_array(actual, 0, expected.length);

        assertArrayEquals(expected, actual);
    }


    @Test
    public void can_read_longlong_array() throws Exception {
        final int[] data = {pad(), pad(), pad(), pad(), 0, 0, 1, 0, 0, 0, 1, 7, -1, -1, -1, -1, -1, -1, -3, -20};
        final long[] expected = {1099511628039L, -532};
        setMessageBody(data);

        readAndVerifyLongLongArray(expected);
    }


    private void readAndVerifyLongLongArray(long[] expected) {
        long[] actual = new long[expected.length];

        getInputObject().read_longlong_array(actual, 0, expected.length);

        assertArrayEquals(expected, actual);
    }


    @Test
    public void can_read_ulonglong_array() throws Exception {
        final int[] data = {pad(), pad(), pad(), pad(), 0, 0, 1, 0, 0, 0, 1, 7, FF, FF, FF, FF, FF, FF, -3, -20};
        final long[] expected = {1099511628039L, -532};
        setMessageBody(data);
        readAndVerifyULongLongArray(expected);
    }

    private void readAndVerifyULongLongArray(long[] expected) {
        long[] actual = new long[expected.length];

        getInputObject().read_ulonglong_array(actual, 0, expected.length);

        if (!Arrays.equals(expected, actual)) {
            fail("Expected " + Arrays.toString(expected) + " but found " + Arrays.toString(actual));
        }
    }


    @Test
    public void can_read_char_array() throws Exception {
        final int[] data = {'b', 'u', 'c', 'k', 'l', 'e', 'u', 'p'};
        final char[] expected = {'b', 'u', 'c', 'k', 'l', 'e', 'u', 'p'};
        setMessageBody(data);
        readAndVerifyCharArray(expected);
    }

    private void readAndVerifyCharArray(char[] expected) {
        char[] actual = new char[expected.length];

        getInputObject().read_char_array(actual, 0, expected.length);

        assertArrayEquals(expected, actual);
    }


    @Test
    public void can_read_wchar_array() throws Exception {
        useV1_2();
        final int[] data = {4, FE, FF, 0, 'b', 4, FE, FF, 0, 'u', 4, FF, FE, 't', 0};
        final char[] expected = {'b', 'u', 't'};
        setMessageBody(data);
        readAndVerifyWCharArray(expected);
    }

    private void readAndVerifyWCharArray(char[] expected) {
        char[] actual = new char[expected.length];

        getInputObject().read_wchar_array(actual, 0, expected.length);

        assertArrayEquals(expected, actual);
    }

    @Test
    public void whenUsingV1_2_headerPaddingForces8ByteAlignmentOnce() {
        useV1_2();
        int[] data = {pad(), pad(), pad(), pad(),
                      0, 0, 1, 0, FF, FF, FF, FF};
        setMessageBody(data);

        getInputObject().setHeaderPadding(true);
        assertEquals(256, getInputObject().read_long());
        assertEquals(-1, getInputObject().read_long());
    }

    @Test
    public void whenMarkIsSetInV1_0_restoreAllowsReread() {
        setMessageBody(0, 0, 1, 23, 'x');
        getInputObject().mark(0);
        assertEquals(0, getInputObject().read_short());
        getInputObject().reset();
        assertEquals(279, getInputObject().read_long());
    }

    @Test
    public void whenMarkIsSetInV1_2_restoreAllowsReread() {
        useV1_2();
        setMessageBody(0, 0, 1, 23, 'x');
        getInputObject().mark(0);
        assertEquals(0, getInputObject().read_short());
        getInputObject().reset();
        assertEquals(279, getInputObject().read_long());
    }

    @Test
    public void whenUsingV1_2_continueReadingOnToFragment() {
        useV1_2();
        setMessageBody(0, 0, 1, 23);
        addFragment(0, 7);
        getInputObject().mark(0);
        getInputObject().read_long();
        assertEquals(7, getInputObject().read_short());
    }

    @Test
    public void whenUsingV1_2_skipPaddingBeforeReadingNextFragment() {
        useV1_2();
        setMessageBody(0, 23, pad(), pad());
        addFragment(0, 0, 0, 7);
        getInputObject().mark(0);
        getInputObject().read_short();
        assertEquals(7, getInputObject().read_long());
    }

    @Test
    public void whenUsingV1_1_skipOptionalPaddingBeforeReadingNextFragment() {
        useV1_1();
        setMessageBody(0, 23, pad(), pad());
        addFragment(0, 0, 0, 7);
        getInputObject().mark(0);
        getInputObject().read_short();
        assertEquals(7, getInputObject().read_long());
    }

    @Test
    public void whenUsingV1_1_alignToStartOfNextFragment() {
        useV1_1();
        setMessageBody(0, 23);
        addFragment(0, 0, 0, 7);
        getInputObject().mark(0);
        getInputObject().read_short();
        assertEquals(7, getInputObject().read_long());
    }

    private void addFragment(int... values) {
        fragment.body = new byte[values.length];
        for (int i = 0; i < values.length; i++)
            fragment.body[i] = (byte) (FF & values[i]);
        getInputObject().addFragment(fragment, ByteBuffer.wrap(fragment.getMessageData()));
    }

    @Test(expected = RequestCanceledException.class)
    public void whenUsingV1_2_throwExceptionIfCanceled() {
        useV1_2();
        setMessageBody(0, 23, pad(), pad());
        getInputObject().mark(0);
        getInputObject().read_short();
        getInputObject().cancelProcessing(0);
        getInputObject().read_long();
    }

    @Test(expected = MARSHAL.class)
    public void whenUsingV1_2_throwExceptionOnReadPastEnd() {
        useV1_2();
        setMessageBody(0, 23, pad(), pad());
        getInputObject().mark(0);
        getInputObject().read_short();
        getInputObject().read_long();
    }

    @Test(expected = COMM_FAILURE.class)
    public void whenUsingV1_2_throwExceptionOnTimeout() {
        useV1_2();
        message.fragments = more_fragments;
        setMessageBody(0, 23, pad(), pad());
        getInputObject().mark(0);
        getInputObject().read_short();
        getInputObject().read_long();
    }

    @Test(expected = RequestCanceledException.class)
    public void whenUsingV1_2_throwExceptionWhenCanceledDuringWait() {
        useV1_2();
        setMessageBody(0, 23, pad(), pad());
        message.fragments = more_fragments;

        whileWaitingForFragmentsDo(new AsynchronousAction() {
            public void exec() {
                addFragment(0, 0, 0, 7);
                getInputObject().cancelProcessing(0);
            }
        });
        getInputObject().mark(0);
        getInputObject().read_short();
        getInputObject().read_long();
    }

    private void whileWaitingForFragmentsDo(AsynchronousAction asynchronousAction) {
        orbData.asynchronousAction = asynchronousAction;
    }


    private ByteBuffer getByteBuffer() {
        return ByteBuffer.wrap(message.getMessageData());
    }

    private void setMessageBody(int... values) {
        message.body = new byte[values.length];
        for (int i = 0; i < values.length; i++)
            message.body[i] = (byte) (FF & values[i]);
    }

    //-------------------------------------- fake implementation of an ORBData -----------------------------------------

    interface AsynchronousAction {
        void exec();
    }

    @SimpleStub(strict=true)
    static abstract class ORBDataFake implements ORBData {
        private AsynchronousAction asynchronousAction;

        @Override
        public int fragmentReadTimeout() {
            if (asynchronousAction != null) asynchronousAction.exec();
            return 1;
        }
    }

    //---------------------------------------- fake implementation of the ORB ------------------------------------------

    @SimpleStub(strict = true)
    static abstract class ORBFake extends ORB {
        private ORBDataFake orbData;

        public void setORBData(ORBDataFake orbData) {
            this.orbData = orbData;
        }

        @Override
        public ORBVersion getORBVersion() {
            return ORBVersionFactory.getFOREIGN();  // test interoperability
        }

        @Override
        public ORBData getORBData() {
            return orbData;
        }
    }

    //------------------------------------- fake implementation of a Connection ----------------------------------------

    @SimpleStub(strict = true)
    static abstract class ConnectionFake implements Connection {
        int char_encoding = ISO_8859_1;
        int wchar_encoding = UTF_16;
        private CodeSetContext codeSets;

        void setCharEncoding(int char_encoding) {
            this.char_encoding = char_encoding;
            codeSets = new CodeSetContext(char_encoding, wchar_encoding);
        }

        void setWCharEncoding(int wchar_encoding) {
            this.wchar_encoding = wchar_encoding;
            codeSets = new CodeSetContext(char_encoding, wchar_encoding);
        }

        @Override
        public CodeSetContext getCodeSetContext() {
            if (codeSets == null)
                codeSets = new CodeSetContext(char_encoding, wchar_encoding);
            return codeSets;
        }

    }

    //--------------------------------------- fake implementation of a Message -----------------------------------------

    @SimpleStub(strict = true)
    static abstract class MessageFake implements FragmentMessage {
        Endian endian = big_endian;
        Fragments fragments = no_more_fragments;
        private GIOPVersion giopVersion = V1_0;
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
