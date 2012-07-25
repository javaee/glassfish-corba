package com.sun.corba.ee.impl.transport;

import com.sun.corba.ee.spi.transport.TcpTimeouts;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.SocketChannel;

public class NioBufferWriter {

    private SocketChannel socketChannel;
    private TcpTimeouts tcpTimeouts;

    public NioBufferWriter(SocketChannel socketChannel, TcpTimeouts tcpTimeouts) {
        this.socketChannel = socketChannel;
        this.tcpTimeouts = tcpTimeouts;
    }

    // A temporary selector for writing to non-blocking SocketChannels
    // when entire message is not written in one write().
    private TemporarySelector tmpWriteSelector;
    // A lock used for lazily initializing tmpWriteSelector
    private final java.lang.Object tmpWriteSelectorLock = new java.lang.Object();

    void write(ByteBuffer byteBuffer) throws IOException {
        int nbytes = socketChannel.write(byteBuffer);
        if (byteBuffer.hasRemaining()) {
            // Can only occur on non-blocking connections.
            // Using long for backoff_factor to avoid floating point
            // calculations.
            TcpTimeouts.Waiter waiter = tcpTimeouts.waiter() ;
            SelectionKey sk = null;
            TemporarySelector tmpSelector = null;
            try {
                tmpSelector = getTemporaryWriteSelector(socketChannel);
                tmpSelector.registerChannel(socketChannel, SelectionKey.OP_WRITE);
                while (byteBuffer.hasRemaining() && !waiter.isExpired()) {
                    int nsel = tmpSelector.select(waiter.getTimeForSleep());
                    if (nsel > 0) {
                        tmpSelector.removeSelectedKey(sk);
                        do {
                            // keep writing while bytes can be written
                            nbytes = socketChannel.write(byteBuffer);
                        } while (nbytes > 0 && byteBuffer.hasRemaining());
                    }
                    // selector timed out or no bytes have been written
                    if (nsel == 0 || nbytes == 0) {
                        waiter.advance() ;
                    }
                }
            } catch (IOException ioe) {
                throw ConnectionImpl.wrapper.exceptionWhenWritingWithTemporarySelector(ioe,
                        byteBuffer.position(), byteBuffer.limit(),
                        waiter.timeWaiting(), tcpTimeouts.get_max_time_to_wait());
            } finally {
                if (tmpSelector != null) {
                    tmpSelector.cancelAndFlushSelector(sk);
                }
            }
            // if message not fully written, throw exception
            if (byteBuffer.hasRemaining() && waiter.isExpired()) {
                // failed to write entire message
                throw ConnectionImpl.wrapper.transportWriteTimeoutExceeded(
                        tcpTimeouts.get_max_time_to_wait(), waiter.timeWaiting());
            }
        }
    }

    TemporarySelector getTemporaryWriteSelector(SocketChannel socketChannel) throws IOException {
        synchronized (tmpWriteSelectorLock) {
            if (tmpWriteSelector == null) {
                tmpWriteSelector = new TemporarySelector(socketChannel);
            }
        }
        return tmpWriteSelector;
    }

    void closeTemporaryWriteSelector() throws IOException {
        synchronized (tmpWriteSelectorLock) {
            if (tmpWriteSelector != null) {
                try {
                    tmpWriteSelector.close();
                } catch (IOException ex) {
                    throw ex;
                }
            }
        }
    }
}
