package com.sun.corba.ee.impl.transport;

import com.sun.corba.ee.impl.protocol.RequestIdImpl;
import com.sun.corba.ee.impl.protocol.giopmsgheaders.Message;
import com.sun.corba.ee.spi.ior.iiop.GIOPVersion;
import com.sun.corba.ee.spi.orb.ORB;
import com.sun.corba.ee.spi.orb.ORBData;
import com.sun.corba.ee.spi.protocol.MessageMediator;
import com.sun.corba.ee.spi.threadpool.NoSuchThreadPoolException;
import com.sun.corba.ee.spi.threadpool.NoSuchWorkQueueException;
import com.sun.corba.ee.spi.threadpool.ThreadPool;
import com.sun.corba.ee.spi.threadpool.ThreadPoolManager;
import com.sun.corba.ee.spi.threadpool.Work;
import com.sun.corba.ee.spi.threadpool.WorkQueue;
import com.sun.corba.ee.spi.transport.Connection;
import com.sun.corba.ee.spi.transport.ConnectionCache;
import com.sun.corba.ee.spi.transport.EventHandler;
import com.sun.corba.ee.spi.transport.MessageTraceManager;
import com.sun.corba.ee.spi.transport.Selector;
import com.sun.corba.ee.spi.transport.TcpTimeouts;
import com.sun.corba.ee.spi.transport.TransportManager;
import org.glassfish.simplestub.SimpleStub;
import org.glassfish.simplestub.Stub;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.SocketChannel;
import java.nio.channels.spi.AbstractSelectableChannel;
import java.nio.channels.spi.AbstractSelectionKey;
import java.nio.channels.spi.AbstractSelector;
import java.nio.channels.spi.SelectorProvider;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Queue;
import java.util.Set;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class ConnectionImplTest {

    private static final byte[] BYTE_DATA = {0,1,2,3,4,5,6,7,8,9,10};

    private OrbFake orb = Stub.create(OrbFake.class);
    private ORBDataFake orbData = Stub.create(ORBDataFake.class);
    private SelectorProviderFake selectorProvider = Stub.create(SelectorProviderFake.class);
    private SocketChannelFake socketChannel = null;
    private TcpTimeoutsFake tcpTimeouts = Stub.create(TcpTimeoutsFake.class);
    private ConnectionCacheFake connectionCache = Stub.create(ConnectionCacheFake.class);
    private WaiterFake waiter = Stub.create(WaiterFake.class);
    private WorkQueueFake workQueue = Stub.create(WorkQueueFake.class);
    private TransportManagerFake transportManager = Stub.create(TransportManagerFake.class);
    private TransportSelectorFake selector = Stub.create(TransportSelectorFake.class);
    private ThreadPoolManagerFake threadPoolManager = Stub.create(ThreadPoolManagerFake.class);
    private ThreadPoolFake threadPool = Stub.create(ThreadPoolFake.class);

    private ConnectionImpl connection;
    private SocketFake socket = new SocketFake();

    private class SocketFake extends Socket {
        private InputStream inputStream;
        private OutputStream outputStream;
        public SocketChannel getChannel() {
            return socketChannel;
        }
        public InputStream getInputStream() throws IOException {
            return inputStream;
        }
        public OutputStream getOutputStream() throws IOException {
            return outputStream;
        }
    }

    @Before
    public void setUp() throws IOException {
        orb.data = orbData;
        orbData.transportTcpTimeouts = tcpTimeouts;
        orb.transportManager = transportManager;
        orb.threadPoolManager = threadPoolManager;
        threadPoolManager.threadPool = threadPool;
        threadPool.workQueue = workQueue;
        transportManager.selector = selector;
        tcpTimeouts.waiter = waiter;
    }

    @Test
    public void whenNioFullBufferWritable_allDataIsWritten() throws IOException {
        useNio();
        connection.write(ByteBuffer.wrap(BYTE_DATA));

        assertArrayEquals(BYTE_DATA, socketChannel.dataWritten);
    }

    private void useNio() throws IOException {
        socketChannel = Stub.create(SocketChannelFake.class, selectorProvider);
        connection = new ConnectionImpl(orb, null, socket, true, true);
        connection.setConnectionCache(connectionCache);
        socketChannel.configureBlocking(false);
        socketChannel.socket = socket;
    }


    @Test
    public void whenNioChannelMomentarilyBusy_allDataIsWritten() throws IOException {
        useNio();
        socketChannel.setNumBytesToWrite(0);
        connection.write(ByteBuffer.wrap(BYTE_DATA));

        assertArrayEquals(BYTE_DATA, socketChannel.dataWritten);
    }

    @Test
    public void whenNioWholeMessageReceived_queueSingleEntry() throws IOException {
        useNio();
        socketChannel.enqueData(new byte[]{'G', 'I', 'O', 'P', 1, 0, Message.FLAG_NO_FRAG_BIG_ENDIAN, Message.GIOPRequest, 0, 0, 0, 6, 1, 2, 3, 4, 5, 6});
        connection.doWork();
        assertEquals(1, workQueue.items.size());
        assertTrue(workQueue.items.remove() instanceof MessageMediator);
    }

    @Test
    public void whenNioMessageReceivedInTwoReads_queueSingleEntryAfterSecond() throws IOException {
        useNio();
        socketChannel.enqueData(new byte[]{'G', 'I', 'O', 'P', 1, 0, Message.FLAG_NO_FRAG_BIG_ENDIAN, Message.GIOPRequest, 0, 0, 0, 6, 1, 2, 3, 4, 5, 6});
        socketChannel.setNumBytesToRead(8);
        connection.doWork();
        assertEquals(1, workQueue.items.size());
        assertTrue(workQueue.items.remove() instanceof MessageMediator);
    }

    @Test
    public void whenNioFragmentsIncluded_queueFirstMessageAndAddFragmentsToFragmentList() throws IOException {
        useNio();
        byte[] messages = {'G', 'I', 'O', 'P', 1, 2, Message.MORE_FRAGMENTS_BIT, Message.GIOPRequest, 0, 0, 0, 6, 0, 0, 0, 3, 5, 6,
                          'G', 'I', 'O', 'P', 1, 2, Message.MORE_FRAGMENTS_BIT, Message.GIOPFragment, 0, 0, 0, 6, 0, 0, 0, 3, 5, 6,
                          'G', 'I', 'O', 'P', 1, 2, Message.FLAG_NO_FRAG_BIG_ENDIAN, Message.GIOPFragment, 0, 0, 0, 4, 0, 0, 0, 3};
        socketChannel.enqueData(messages);
        connection.doWork();
        assertEquals(1, workQueue.items.size());
        Work workItem = workQueue.items.remove();
        assertTrue(workItem instanceof MessageMediator);
        Queue<MessageMediator> fragmentList = connection.getFragmentList(new RequestIdImpl(3));
        assertEquals(2, fragmentList.size());
    }

    @Test @Ignore
    public void whenWholeMessageReceivedFromSocket_queueSingleEntry() throws IOException {
        readFromBlockingSocket(new byte[]{'G', 'I', 'O', 'P', 1, 0, Message.FLAG_NO_FRAG_BIG_ENDIAN, Message.GIOPRequest, 0, 0, 0, 6, 1, 2, 3, 4, 5, 6});
        connection.doWork();
        assertEquals(1, workQueue.items.size());
        assertTrue(workQueue.items.remove() instanceof MessageMediator);
    }

    private void readFromBlockingSocket(byte[] bytes) {
        socket.inputStream = new ByteArrayInputStream( bytes );
        connection = new ConnectionImpl(orb, null, socket, false, true);
        connection.setConnectionCache(connectionCache);
    }

    @SimpleStub(strict = true)
    static abstract class ORBDataFake implements ORBData {
        private TcpTimeouts transportTcpTimeouts;

        @Override
        public TcpTimeouts getTransportTcpTimeouts() {
            return transportTcpTimeouts;
        }

        @Override
        public boolean disableDirectByteBufferUse() {
            return true;
        }

        @Override
        public int getReadByteBufferSize() {
            return 100;
        }

        @Override
        public GIOPVersion getGIOPVersion() {
            return GIOPVersion.V1_2;
        }

        @Override
        public boolean nonBlockingReadCheckMessageParser() {
            return true;
        }

        @Override
        public boolean alwaysEnterBlockingRead() {
            return false;
        }
    }


    @SimpleStub(strict = true)
    static abstract class OrbFake extends ORB {

        private ORBDataFake data;
        public TransportManagerFake transportManager;
        private ThreadPoolManager threadPoolManager;

        @Override
        public ORBData getORBData() {
            return data;
        }

        @Override
        public TransportManager getTransportManager() {
            return transportManager;
        }

        @Override
        public ThreadPoolManager getThreadPoolManager() {
            return threadPoolManager;
        }
    }

    @SimpleStub(strict = true)
    static abstract class SelectorFake extends AbstractSelector {
        private Set<SelectionKey> selectedKeys = new HashSet<SelectionKey>();

        public SelectorFake(SelectorProvider provider) {
            super(provider);
        }

        @Override
        public int selectNow() throws IOException {
            return 0;
        }

        @Override
        protected SelectionKey register(AbstractSelectableChannel ch, int ops, Object att) {
            SelectionKeyFake selectionKey = Stub.create(SelectionKeyFake.class, this);
            selectedKeys.add(selectionKey);
            return selectionKey;
        }

        @Override
        public int select(long timeout) throws IOException {
            return 1;
        }

        @Override
        public Set<SelectionKey> selectedKeys() {
            return selectedKeys;
        }

        @Override
        protected void implCloseSelector() throws IOException {
        }
    }

    @SimpleStub(strict = true)
    static abstract class SelectorProviderFake extends SelectorProvider {
        @Override
        public AbstractSelector openSelector() throws IOException {
            return Stub.create(SelectorFake.class, this);
        }
    }


    @SimpleStub(strict = true)
    static abstract class SocketChannelFake extends SocketChannel {
        private byte[] dataWritten = new byte[0];
        private byte[] readableData;
        private int readPos;
        private ArrayList<Integer> numBytesToWrite = new ArrayList<Integer>();
        private ArrayList<Integer> numBytesToRead = new ArrayList<Integer>();
        private Socket socket;

        private int getNumBytesToWrite() {
            return numBytesToWrite.isEmpty() ? Integer.MAX_VALUE : numBytesToWrite.remove(0);
        }

        private int getNumBytesToRead() {
            return numBytesToRead.isEmpty() ? Integer.MAX_VALUE : numBytesToRead.remove(0);
        }

        private void setNumBytesToWrite(int... numBytesToWrite) {
            for (int i : numBytesToWrite)
                this.numBytesToWrite.add(i);
        }

        public void setNumBytesToRead(int... numBytesToRead) {
            for (int i : numBytesToRead)
                this.numBytesToRead.add(i);
        }

        private void enqueData(byte... dataToBeRead) {
            readableData = new byte[dataToBeRead.length];
            System.arraycopy(dataToBeRead, 0, readableData, 0, dataToBeRead.length);
        }

        protected SocketChannelFake(SelectorProvider provider) {
            super(provider);
        }

        @Override
        public Socket socket() {
            return socket;
        }

        @Override
        public int write(ByteBuffer src) throws IOException {
            int numBytesAvailable = src.limit() - src.position();
            int numToWrite = Math.min(numBytesAvailable, getNumBytesToWrite());
            byte[] bytesToWrite = new byte[numToWrite];
            src.get(bytesToWrite);
            byte[] written = new byte[dataWritten.length + numToWrite];
            System.arraycopy(dataWritten, 0, written, 0, dataWritten.length);
            System.arraycopy(bytesToWrite, 0, written, dataWritten.length, written.length);
            dataWritten = written;
            return numToWrite;
        }

        @Override
        protected void implConfigureBlocking(boolean block) throws IOException {
        }

        @Override
        public int read(ByteBuffer dst) throws IOException {
            int numBytesToRead = Math.min(getNumBytesToRead(), Math.min(dataSize(), bufferCapacity(dst)));
            dst.put(readableData, readPos, numBytesToRead);
            readPos += numBytesToRead;
            return numBytesToRead;
        }

        private int bufferCapacity(ByteBuffer dst) {
            return dst.limit() - dst.position();
        }

        private int dataSize() {
            return this.readableData.length - readPos;
        }
    }

    @SimpleStub(strict = true)
    static abstract class TcpTimeoutsFake implements TcpTimeouts {
        private Waiter waiter;

        @Override
        public Waiter waiter() {
            return waiter;
        }
    }


    @SimpleStub(strict = true)
    static abstract class ConnectionCacheFake implements ConnectionCache {
        @Override
        public void stampTime(Connection connection) {
        }
    }


    @SimpleStub(strict = true)
    static abstract class WaiterFake implements TcpTimeouts.Waiter {
        @Override
        public boolean isExpired() {
            return false;
        }

        @Override
        public int getTimeForSleep() {
            return 1;
        }
    }

    @SimpleStub(strict=true)
    static abstract class SelectionKeyFake extends AbstractSelectionKey {
        private SelectorFake selector;

        protected SelectionKeyFake(SelectorFake selector) {
            this.selector = selector;
        }

        public SelectorFake selector() {
            return selector;
        }
    }

    @SimpleStub(strict = true)
    static abstract class WorkQueueFake implements WorkQueue {
        private Queue<Work> items = new ArrayDeque<Work>();

        @Override
        public void addWork(Work aWorkItem) {
            items.offer(aWorkItem);
        }
    }

    @SimpleStub(strict=true)
    static abstract class TransportManagerFake implements TransportManager {
        private MessageTraceManager mtm = new MessageTraceManagerImpl();
        public TransportSelectorFake selector;

        @Override
        public MessageTraceManager getMessageTraceManager() {
            return mtm;
        }

        @Override
        public Selector getSelector(int i) {
            return selector;
        }
    }

    @SimpleStub(strict=true)
    static abstract class TransportSelectorFake implements Selector {
        @Override
        public void unregisterForEvent(EventHandler eventHandler) {
        }

        @Override
        public void registerInterestOps(EventHandler eventHandler) {
        }
    }

    @SimpleStub(strict=true)
    static abstract class ThreadPoolManagerFake implements ThreadPoolManager {
        private ThreadPool threadPool;

        @Override
        public ThreadPool getThreadPool(int numericIdForThreadpool) throws NoSuchThreadPoolException {
            return threadPool;
        }
    }

    @SimpleStub(strict=true)
    static abstract class ThreadPoolFake implements ThreadPool {
        private WorkQueue workQueue;

        @Override
        public WorkQueue getWorkQueue(int queueId) throws NoSuchWorkQueueException {
            return workQueue;
        }
    }
}
