package com.sun.corba.ee.impl.transport;

import com.sun.corba.ee.impl.encoding.CDRInputObject;
import com.sun.corba.ee.impl.protocol.RequestIdImpl;
import com.sun.corba.ee.impl.protocol.giopmsgheaders.Message;
import com.sun.corba.ee.spi.protocol.MessageMediator;
import com.sun.corba.ee.spi.threadpool.Work;
import org.junit.Ignore;
import org.junit.Test;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.List;
import java.util.Queue;

import static org.junit.Assert.*;

public class ConnectionImplTest extends TransportTestBase {

    private static final byte[] BYTE_DATA = {0,1,2,3,4,5,6,7,8,9,10};

    @Test
    public void whenRequest1_0_receivedFromSocketInvokeObject() throws IOException {
        final List<Short> params = new ArrayList<Short>();
        defineRequestDispatcher( new RequestDispatcher() {
            public void readParameters(CDRInputObject input) {
                params.add(input.read_short());
            }
        });
        readFromSocketWithoutChannelAndDispatch(new byte[]{'G', 'I', 'O', 'P', 1, 0, Message.FLAG_NO_FRAG_BIG_ENDIAN,
                Message.GIOPRequest, /* size */ 0, 0, 0, 38, /* no service contexts */ 0, 0, 0, 0,
                /* request ID */ 0, 0, 0, 2, /* response expected */ 1, /* padding */ 0,0,0,
                /* object key */ 0, 0, 0, 4, 0, 0, 0, 6, /* operation */ 0, 0, 0, 5, 'd', 'o', 'I', 't', 0,
                0, 0, 0, /* principal */ 0, 0, 0, 0, /* short param */ 1, 1});
        getConnection().doWork();
        assertEquals(1, getMediators().size());
        MessageMediator mediator = getMediators().remove(0);
        assertEquals("doIt", mediator.getOperationName());
        assertEquals(2, mediator.getRequestId());
        assertFalse(mediator.isOneWay());
        assertEquals(257, (short) params.get(0));
    }

    @Test
    public void whenRequest1_0_receivedFromNioInvokeObject() throws IOException {
        final List<Short> params = new ArrayList<Short>();
        defineRequestDispatcher( new RequestDispatcher() {
            public void readParameters(CDRInputObject input) {
                params.add(input.read_short());
            }
        });
        readFromNio(new byte[]{'G', 'I', 'O', 'P', 1, 0, Message.FLAG_NO_FRAG_BIG_ENDIAN,
                Message.GIOPRequest, /* size */ 0, 0, 0, 38, /* no service contexts */ 0, 0, 0, 0,
                /* request ID */ 0, 0, 0, 2, /* response expected */ 1, /* padding */ 0, 0, 0,
                /* object key */ 0, 0, 0, 4, 0, 0, 0, 6, /* operation */ 0, 0, 0, 5, 'd', 'o', 'I', 't', 0,
                0, 0, 0, /* principal */ 0, 0, 0, 0, /* short param */ 1, 1});
        getConnection().doWork();
        processQueuedWork();

        assertEquals(1, getMediators().size());
        MessageMediator mediator = getMediators().remove(0);
        assertEquals("doIt", mediator.getOperationName());
        assertEquals(2, mediator.getRequestId());
        assertFalse(mediator.isOneWay());
        assertEquals(257, (short) params.get(0));
    }

    @Test @Ignore("The fragment needs to be processed asynchronously")
    public void whenRequest1_1_receivedFromNioWithFragmentsInvokeObject() throws IOException {
        final List<Short> params = new ArrayList<Short>();
        defineRequestDispatcher( new RequestDispatcher() {
            public void readParameters(CDRInputObject input) {
                params.add(input.read_short());
            }
        });
        readFromNio(new byte[]{'G', 'I', 'O', 'P', 1, 1, Message.MORE_FRAGMENTS_BIT,
                Message.GIOPRequest, /* size */ 0, 0, 0, 12, /* no service contexts */ 0, 0, 0, 0,
                /* request ID */ 0, 0, 0, 2, /* response expected */ 1, /* padding */ 0, 0, 0,

                'G', 'I', 'O', 'P', 1, 1, Message.FLAG_NO_FRAG_BIG_ENDIAN, Message.GIOPFragment,
                /* size */ 0, 0, 0, 30, /* request ID */ 0, 0, 0, 2, /* object key */ 0, 0, 0, 4, 0, 0, 0, 6,
                /* operation */ 0, 0, 0, 5, 'd', 'o', 'I', 't', 0,
                0, 0, 0, /* principal */ 0, 0, 0, 0, /* short param */ 1, 1});
        getConnection().doWork();
        processQueuedWork();

        assertEquals(1, getMediators().size());
        MessageMediator mediator = getMediators().remove(0);
        assertEquals("doIt", mediator.getOperationName());
        assertEquals(2, mediator.getRequestId());
        assertFalse(mediator.isOneWay());
        assertEquals(257, (short) params.get(0));
    }

    @Test
    public void whenRequest1_1_receivedFromNioInvokeObject() throws IOException {
        final List<Short> params = new ArrayList<Short>();
        defineRequestDispatcher( new RequestDispatcher() {
            public void readParameters(CDRInputObject input) {
                params.add(input.read_short());
            }
        });
        readFromNio(new byte[]{'G', 'I', 'O', 'P', 1, 1, Message.FLAG_NO_FRAG_BIG_ENDIAN,
                Message.GIOPRequest, /* size */ 0, 0, 0, 38, /* no service contexts */ 0, 0, 0, 0,
                /* request ID */ 0, 0, 0, 2, /* response expected */ 1, /* padding */ 0,0,0,
                /* object key */ 0, 0, 0, 4, 0, 0, 0, 6, /* operation */ 0, 0, 0, 5, 'd', 'o', 'I', 't', 0,
                0, 0, 0, /* principal */ 0, 0, 0, 0, /* short param */ 1, 1});
        getConnection().doWork();
        processQueuedWork();

        assertEquals(1, getMediators().size());
        MessageMediator mediator = getMediators().remove(0);
        assertEquals("doIt", mediator.getOperationName());
        assertEquals(2, mediator.getRequestId());
        assertFalse(mediator.isOneWay());
        assertEquals(257, (short) params.get(0));
    }

    @Test
    public void whenRequest1_2_receivedFromNioInvokeObject() throws IOException {
        final List<Short> params = new ArrayList<Short>();
        defineRequestDispatcher( new RequestDispatcher() {
            public void readParameters(CDRInputObject input) {
                params.add(input.read_short());
            }
        });
        readFromNio(new byte[]{'G', 'I', 'O', 'P', 1, 2, Message.FLAG_NO_FRAG_BIG_ENDIAN,
                Message.GIOPRequest, /* size */ 0, 0, 0, 38,
                /* request ID */ 0, 0, 0, 2, /* response expected */ 1, /* request reserved */ 0,0,0,
                /* use key */ 0, 0, /* padding */ 0, 0, /* object key */ 0, 0, 0, 4, 0, 0, 0, 6,
                /* operation */ 0, 0, 0, 5, 'd', 'o', 'I', 't', 0,
                /* padding */ 0, 0, 0, /* no service contexts */ 0, 0, 0, 0, /* short param */ 1, 1});
        getConnection().doWork();
        processQueuedWork();

        assertEquals(1, getMediators().size());
        MessageMediator mediator = getMediators().remove(0);
        assertEquals("doIt", mediator.getOperationName());
        assertEquals(2, mediator.getRequestId());
        assertFalse(mediator.isOneWay());
        assertEquals(257, (short) params.get(0));
    }

    @Test(expected = RuntimeException.class)
    public void whenNioConfigureBlockingFails_throwException() throws IOException {
        SocketChannelFake socketChannel = getSocketChannel();
        socketChannel.setFailToConfigureBlocking();
        useNio();
    }

    @Test
    public void whenNioFullBufferWritable_allDataIsWritten() throws IOException {
        useNio();
        getConnection().write(ByteBuffer.wrap(BYTE_DATA));

        assertArrayEquals(BYTE_DATA, getSocketChannel().getDataWritten());
    }


    @Test
    public void whenNioChannelMomentarilyBusy_allDataIsWritten() throws IOException {
        useNio();
        getSocketChannel().setNumBytesToWrite(0);
        getConnection().write(ByteBuffer.wrap(BYTE_DATA));

        assertArrayEquals(BYTE_DATA, getSocketChannel().getDataWritten());
    }

    @Test
    public void whenNioWholeMessageReceived_queueSingleEntry() throws IOException {
        useNio();
        getSocketChannel().enqueData(new byte[]{'G', 'I', 'O', 'P', 1, 0, Message.FLAG_NO_FRAG_BIG_ENDIAN, Message.GIOPRequest, 0, 0, 0, 6, 1, 2, 3, 4, 5, 6});
        getConnection().doWork();
        assertEquals(1, getWorkQueue().size());
        assertTrue(getWorkQueue().remove() instanceof MessageMediator);
    }

    @Test
    public void whenNioMessageReceivedInTwoReads_queueSingleEntryAfterSecond() throws IOException {
        useNio();
        getSocketChannel().enqueData(new byte[]{'G', 'I', 'O', 'P', 1, 0, Message.FLAG_NO_FRAG_BIG_ENDIAN, Message.GIOPRequest, 0, 0, 0, 6, 1, 2, 3, 4, 5, 6});
        getSocketChannel().setNumBytesToRead(8);
        getConnection().doWork();
        assertEquals(1, getWorkQueue().size());
        assertTrue(getWorkQueue().remove() instanceof MessageMediator);
    }

    @Test
    public void whenNioFragmentsIncluded_queueFirstMessageAndAddFragmentsToFragmentList() throws IOException {
        useNio();
        byte[] messages = {'G', 'I', 'O', 'P', 1, 2, Message.MORE_FRAGMENTS_BIT, Message.GIOPRequest, 0, 0, 0, 6, 0, 0, 0, 3, 5, 6,
                          'G', 'I', 'O', 'P', 1, 2, Message.MORE_FRAGMENTS_BIT, Message.GIOPFragment, 0, 0, 0, 6, 0, 0, 0, 3, 5, 6,
                          'G', 'I', 'O', 'P', 1, 2, Message.FLAG_NO_FRAG_BIG_ENDIAN, Message.GIOPFragment, 0, 0, 0, 4, 0, 0, 0, 3};
        getSocketChannel().enqueData(messages);
        getConnection().doWork();
        assertEquals(1, getWorkQueue().size());
        Work workItem = getWorkQueue().remove();
        assertTrue(workItem instanceof MessageMediator);
        Queue<MessageMediator> fragmentList = getConnection().getFragmentList(new RequestIdImpl(3));
        assertEquals(2, fragmentList.size());
    }

    @Test
    public void whenMessageWithFragmentsReceivedFromSocket_dispatchEachPart() throws IOException {
        byte[] messages = {'G', 'I', 'O', 'P', 1, 2, Message.MORE_FRAGMENTS_BIT, Message.GIOPRequest, 0, 0, 0, 6, 0, 0, 0, 3, 5, 6,
                          'G', 'I', 'O', 'P', 1, 2, Message.MORE_FRAGMENTS_BIT, Message.GIOPFragment, 0, 0, 0, 6, 0, 0, 0, 3, 5, 6,
                          'G', 'I', 'O', 'P', 1, 2, Message.FLAG_NO_FRAG_BIG_ENDIAN, Message.GIOPFragment, 0, 0, 0, 4, 0, 0, 0, 3};
        readFromSocketWithoutChannel(messages);
        getConnection().doWork();
        assertEquals(1, getMediators().size());
        getConnection().doWork();
        getConnection().doWork();
        assertEquals(3, getMediators().size());
    }


}
