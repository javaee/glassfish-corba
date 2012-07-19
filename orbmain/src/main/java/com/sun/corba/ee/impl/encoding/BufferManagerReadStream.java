/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright (c) 1997-2010 Oracle and/or its affiliates. All rights reserved.
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
package com.sun.corba.ee.impl.encoding;

import java.nio.ByteBuffer;

import com.sun.corba.ee.spi.transport.ByteBufferPool;
import com.sun.corba.ee.spi.orb.ORB;
import com.sun.corba.ee.spi.logging.ORBUtilSystemException;
import com.sun.corba.ee.impl.protocol.RequestCanceledException;
import com.sun.corba.ee.impl.protocol.giopmsgheaders.FragmentMessage;
import com.sun.corba.ee.impl.protocol.giopmsgheaders.Message;
import com.sun.corba.ee.spi.trace.Transport;

import java.util.LinkedList;

import org.glassfish.pfl.tf.spi.annotation.InfoMethod;

@Transport
public class BufferManagerReadStream
        implements BufferManagerRead, MarkAndResetHandler {
    private static final ORBUtilSystemException wrapper =
            ORBUtilSystemException.self;

    private volatile boolean receivedCancel = false;
    private int cancelReqId = 0;

    // We should convert endOfStream to a final static dummy end node
    private boolean endOfStream = true;
    private final BufferQueue fragmentQueue = new BufferQueue();
    // REVISIT - This should go in BufferManagerRead. But, since
    //           BufferManagerRead is an interface. BufferManagerRead
    //           might ought to be an abstract class instead of an
    //           interface.
    private final ORB orb;

    BufferManagerReadStream(ORB orb) {
        this.orb = orb;
    }

    public void cancelProcessing(int requestId) {
        synchronized (fragmentQueue) {
            receivedCancel = true;
            cancelReqId = requestId;
            fragmentQueue.notify();
        }
    }

    @InfoMethod
    private void bufferMessage(String msg, int bbAddr, String tail) {}

    @Transport
    public void processFragment(ByteBuffer byteBuffer, FragmentMessage msg) {
        ByteBufferWithInfo bbwi = new ByteBufferWithInfo( byteBuffer, msg.getHeaderLength());

        synchronized (fragmentQueue) {
            if (orb.transportDebugFlag) {
                logBufferMessage("processFragment() - queuing ByteByffer id (", byteBuffer, ") to fragment queue.");
            }
            fragmentQueue.enqueue(bbwi);
            endOfStream = !msg.moreFragmentsToFollow();
            fragmentQueue.notify();
        }
    }

    @InfoMethod
    private void underflowMessage(String msg, int rid) {
    }

    @Transport
    public ByteBufferWithInfo underflow(ByteBufferWithInfo bbwi) {

        ByteBufferWithInfo result;

        synchronized (fragmentQueue) {

            if (receivedCancel) {
                underflowMessage("underflow() - Cancel request id:", cancelReqId);
                throw new RequestCanceledException(cancelReqId);
            }

            while (fragmentQueue.size() == 0) {

                if (endOfStream) {
                    throw wrapper.endOfStream();
                }

                boolean interrupted = false;
                try {
                    fragmentQueue.wait(orb.getORBData().fragmentReadTimeout());
                } catch (InterruptedException e) {
                    interrupted = true;
                }

                if (!interrupted && fragmentQueue.size() == 0) {
                    throw wrapper.bufferReadManagerTimeout();
                }

                if (receivedCancel) {
                    underflowMessage("underflow() - Cancel request id after wait:", cancelReqId);
                    throw new RequestCanceledException(cancelReqId);
                }
            }

            result = fragmentQueue.dequeue();

            // VERY IMPORTANT
            // Release bbwi.byteBuffer to the ByteBufferPool only if
            // this BufferManagerStream is not marked for potential restore.
            if (!markEngaged && bbwi != null && bbwi.hasByteBuffer()) {
                ByteBufferPool byteBufferPool = getByteBufferPool();

                byteBufferPool.releaseByteBuffer(bbwi.getByteBuffer());
                bbwi.releaseByteBuffer();
            }
        }
        return result;
    }

    @Override
    public boolean isFragmentOnUnderflow() {
        return true;
    }

    public void init(Message msg) {
        if (msg != null) {
            endOfStream = !msg.moreFragmentsToFollow();
        }
    }

    // Release any queued ByteBufferWithInfo's byteBuffers to the
    // ByteBufferPoool
    @Transport
    public void close(ByteBufferWithInfo bbwi) {
        int inputBbAddress = 0;

        if (bbwi != null) {
            inputBbAddress = System.identityHashCode(bbwi.getByteBuffer());
        }
        ByteBufferPool byteBufferPool = getByteBufferPool();

        // release ByteBuffers on fragmentQueue
        synchronized (fragmentQueue) {
            // IMPORTANT: The fragment queue may have one ByteBuffer
            //            on it that's also on the CDRInputStream if
            //            this method is called when the stream is 'marked'.
            //            Thus, we'll compare the ByteBuffer passed
            //            in (from a CDRInputStream) with all ByteBuffers
            //            on the stack. If one is found to equal, it will
            //            not be released to the ByteBufferPool.

            ByteBufferWithInfo abbwi;
            while (fragmentQueue.size() != 0) {
                abbwi = fragmentQueue.dequeue();
                if (abbwi != null && abbwi.hasByteBuffer()) {
                    byteBufferPool.releaseByteBuffer(abbwi.getByteBuffer());
                }
            }
        }
        fragmentQueue.clear();

        // release ByteBuffers on fragmentStack
        if (fragmentStack != null && fragmentStack.size() != 0) {
            // IMPORTANT: The fragment stack may have one ByteBuffer
            //            on it that's also on the CDRInputStream if
            //            this method is called when the stream is 'marked'.
            //            Thus, we'll compare the ByteBuffer passed
            //            in (from a CDRInputStream) with all ByteBuffers
            //            on the stack. If one is found to equal, it will
            //            not be released to the ByteBufferPool.

            for (ByteBufferWithInfo abbwi : fragmentStack) {
                if (abbwi != null && abbwi.hasByteBuffer()) {
                    if (inputBbAddress != System.identityHashCode(abbwi.getByteBuffer())) {
                        byteBufferPool.releaseByteBuffer(abbwi.getByteBuffer());
                    }
                }
            }

            fragmentStack = null;
        }
    }

    private void logBufferMessage(String prefix, ByteBuffer byteBuffer, String suffix) {
        bufferMessage(prefix, System.identityHashCode(byteBuffer), suffix);
    }

    protected ByteBufferPool getByteBufferPool() {
        return orb.getByteBufferPool();
    }

    // Mark and reset handler ----------------------------------------

    private boolean markEngaged = false;

    // List of fragment ByteBufferWithInfos received since
    // the mark was engaged.
    private LinkedList<ByteBufferWithInfo> fragmentStack = null;
    private RestorableInputStream inputStream = null;

    // Original state of the stream
    private Object streamMemento = null;

    public void mark(RestorableInputStream inputStream) {
        this.inputStream = inputStream;
        markEngaged = true;

        // Get the magic Object that the stream will use to
        // reconstruct it's state when reset is called
        streamMemento = inputStream.createStreamMemento();

        if (fragmentStack != null) {
            fragmentStack.clear();
        }
    }

    // Collects fragments received since the mark was engaged.
    public void fragmentationOccured(ByteBufferWithInfo newFragment) {
        if (!markEngaged) {
            return;
        }

        if (fragmentStack == null) {
            fragmentStack =
                    new LinkedList<ByteBufferWithInfo>();
        }

        fragmentStack.addFirst(new ByteBufferWithInfo(newFragment));
    }

    public void reset() {
        if (!markEngaged) {
            // REVISIT - call to reset without call to mark
            return;
        }

        markEngaged = false;

        // If we actually did peek across fragments, we need
        // to push those fragments onto the front of the
        // buffer queue.
        if (fragmentStack != null && fragmentStack.size() != 0) {

            synchronized (fragmentQueue) {
                for (ByteBufferWithInfo bbwi : fragmentStack) {
                    fragmentQueue.push(bbwi);
                }
            }

            fragmentStack.clear();
        }

        // Give the stream the magic Object to restore
        // it's state.
        inputStream.restoreInternalState(streamMemento);
    }

    public MarkAndResetHandler getMarkAndResetHandler() {
        return this;
    }
}
