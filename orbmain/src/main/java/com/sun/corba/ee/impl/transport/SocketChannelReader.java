package com.sun.corba.ee.impl.transport;

import com.sun.corba.ee.spi.orb.ORB;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;

public class SocketChannelReader {
    private ORB orb;

    public SocketChannelReader(ORB orb) {
        this.orb = orb;
    }

    /**
     * Reads all currently available data from the socket channel, appending it to any data left
     * from a previous read.
     * @param channel the channel from which to read
     * @param previouslyReadData the old data to read; note: all data up to the limit is considered valid.
     * @return a buffer containing all old data, with all newly available data appended to it.
     * @throws IOException if an error occurs while reading from the channel.
     */
    public ByteBuffer read(SocketChannel channel, ByteBuffer previouslyReadData) throws IOException {
        ByteBuffer byteBuffer = prepareToAppendTo(previouslyReadData);

        int numBytesRead = channel.read(byteBuffer);
        while (numBytesRead > 0) {
            if (haveFilledBuffer(byteBuffer))
                byteBuffer = expandBuffer(byteBuffer);
            numBytesRead = channel.read(byteBuffer);
        }

        return byteBuffer;
    }

    private ByteBuffer expandBuffer(ByteBuffer byteBuffer) {
        byteBuffer.flip();
        byteBuffer = reallocateBuffer(byteBuffer);
        return byteBuffer;
    }

    private boolean haveFilledBuffer(ByteBuffer byteBuffer) {
        return byteBuffer.position() == byteBuffer.capacity();
    }

    private ByteBuffer prepareToAppendTo(ByteBuffer byteBuffer) {
        if (byteBuffer == null) {
            byteBuffer = allocateBuffer();
        } else if (byteBuffer.limit() == byteBuffer.capacity()) {
            byteBuffer = reallocateBuffer(byteBuffer);
        } else {
            byteBuffer.position(byteBuffer.limit()).limit(byteBuffer.capacity());
        }
        return byteBuffer;
    }

    private ByteBuffer reallocateBuffer(ByteBuffer byteBuffer) {
        return orb.getByteBufferPool().reAllocate(byteBuffer, 2*byteBuffer.capacity());
    }

    private ByteBuffer allocateBuffer() {
        return orb.getByteBufferPool().getByteBuffer(orb.getORBData().getReadByteBufferSize());
    }

}
