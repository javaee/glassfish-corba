package com.sun.corba.ee.impl.transport;

import com.sun.corba.ee.spi.orb.ORB;
import com.sun.corba.ee.spi.orb.ORBData;
import com.sun.corba.ee.spi.transport.Connection;
import com.sun.corba.ee.spi.transport.ConnectionCache;
import com.sun.corba.ee.spi.transport.TcpTimeouts;
import org.glassfish.simplestub.SimpleStub;
import org.glassfish.simplestub.Stub;
import org.junit.Before;
import org.junit.Test;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.SocketChannel;
import java.nio.channels.spi.AbstractSelectableChannel;
import java.nio.channels.spi.AbstractSelectionKey;
import java.nio.channels.spi.AbstractSelector;
import java.nio.channels.spi.SelectorProvider;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;

import static org.junit.Assert.assertArrayEquals;

public class ConnectionImplTest {

    private static final byte[] BYTE_DATA = {0,1,2,3,4,5,6,7,8,9,10};

    private OrbFake orb = Stub.create(OrbFake.class);
    private ORBDataFake orbData = Stub.create(ORBDataFake.class);
    private SelectorProviderFake selectorProvider = Stub.create(SelectorProviderFake.class);
    private SocketChannelFake socketChannel = Stub.create(SocketChannelFake.class, selectorProvider);
    private TcpTimeoutsFake tcpTimeouts = Stub.create(TcpTimeoutsFake.class);
    private ConnectionCacheFake connectionCache = Stub.create(ConnectionCacheFake.class);
    private WaiterFake waiter = Stub.create(WaiterFake.class);
    private ConnectionImpl connection;

    @Before
    public void setUp() throws IOException {
        orb.data = orbData;
        orbData.transportTcpTimeouts = tcpTimeouts;
        tcpTimeouts.waiter = waiter;
        connection = new ConnectionImpl(orb, true, true);
        connection.socketChannel = socketChannel;
        socketChannel.configureBlocking(false);
        connection.setConnectionCache(connectionCache);
    }

    @Test
    public void whenFullBufferWritable_allDataIsWritten() throws IOException {
        connection.write(ByteBuffer.wrap(BYTE_DATA));

        assertArrayEquals(BYTE_DATA, socketChannel.dataWritten);
    }


    @Test
    public void whenChannelBusy_allDataIsWritten() throws IOException {
        socketChannel.setNumBytesToWrite(0);
        connection.write(ByteBuffer.wrap(BYTE_DATA));

        assertArrayEquals(BYTE_DATA, socketChannel.dataWritten);
    }

    @SimpleStub(strict = true)
    static abstract class ORBDataFake implements ORBData {
        private TcpTimeouts transportTcpTimeouts;

        @Override
        public TcpTimeouts getTransportTcpTimeouts() {
            return transportTcpTimeouts;
        }
    }


    @SimpleStub(strict = true)
    static abstract class OrbFake extends ORB {

        private ORBDataFake data;

        @Override
        public ORBData getORBData() {
            return data;
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
        byte[] dataWritten = new byte[0];

        private int getNumBytesToWrite() {
            return numBytesToWrite.isEmpty() ? Integer.MAX_VALUE : numBytesToWrite.remove(0);
        }

        private void setNumBytesToWrite(int... numBytesToWrite) {
            for (int i : numBytesToWrite)
                this.numBytesToWrite.add(i);
        }

        private ArrayList<Integer> numBytesToWrite = new ArrayList<Integer>();

        protected SocketChannelFake(SelectorProvider provider) {
            super(provider);
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
}
