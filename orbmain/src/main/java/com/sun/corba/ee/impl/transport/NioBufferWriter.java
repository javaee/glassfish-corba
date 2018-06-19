/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright (c) 2018 Oracle and/or its affiliates. All rights reserved.
 *
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common Development
 * and Distribution License("CDDL") (collectively, the "License").  You
 * may not use this file except in compliance with the License.  You can
 * obtain a copy of the License at
 * https://oss.oracle.com/licenses/CDDL+GPL-1.1
 * or LICENSE.txt.  See the License for the specific
 * language governing permissions and limitations under the License.
 *
 * When distributing the software, include this License Header Notice in each
 * file and include the License file at LICENSE.txt.
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

package com.sun.corba.ee.impl.transport;

import com.sun.corba.ee.spi.transport.TcpTimeouts;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.SocketChannel;

public class NioBufferWriter {
    protected TemporarySelector tmpWriteSelector;
    protected final java.lang.Object tmpWriteSelectorLock = new java.lang.Object();

    private SocketChannel socketChannel;
    private TcpTimeouts tcpTimeouts;

    public NioBufferWriter(SocketChannel socketChannel, TcpTimeouts tcpTimeouts) {
        this.socketChannel = socketChannel;
        this.tcpTimeouts = tcpTimeouts;
    }

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
                sk = tmpSelector.registerChannel(socketChannel,
                                                SelectionKey.OP_WRITE);
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
                ioe.printStackTrace();
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

    TemporarySelector getTemporaryWriteSelector(SocketChannel socketChannel1) throws IOException {
        synchronized (tmpWriteSelectorLock) {
            if (tmpWriteSelector == null) {
                tmpWriteSelector = new TemporarySelector(socketChannel1);
            }
        }
        return tmpWriteSelector;
    }
}
