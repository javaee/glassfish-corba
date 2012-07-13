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

import com.sun.corba.ee.spi.trace.Transport;
import org.glassfish.grizzly.Buffer;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;


@Transport
class ByteBufferWithInfo // implements org.glassfish.grizzly.Buffer
{
    private ByteBuffer byteBuffer;
    private ByteOrder order;

    ByteBufferWithInfo(ByteBuffer byteBuffer, int index) {
        this.setByteBuffer(byteBuffer);
        position(index);
    }

    // Shallow copy constructor
    ByteBufferWithInfo(ByteBufferWithInfo bbwi) {
        // IMPORTANT: Cannot simply assign the reference of
        //            bbwi.byteBuffer to this.byteBuffer since
        //            bbwi's can be restored via restore-able
        //            stream in CDRInputObject_1_0.java. To
        //            restore a bbwi, we must also keep the
        //            bbwi's position and limit. If we use
        //            ByteBuffer.duplicate() we'll get independent
        //            positions and limits, but the same ByteBuffer,
        //            (which is what we want).
        this.setByteBuffer(bbwi.getByteBuffer().duplicate());
        this.limit(bbwi.limit());
        this.position(bbwi.position());
    }

    ByteBufferWithInfo(ByteBuffer byteBuffer) {
        this(byteBuffer, 0);
    }

    public ByteBuffer toByteBuffer() {
        return getByteBuffer();
    }

    public ByteBufferWithInfo get(byte[] byteArray) {
        getByteBuffer().get(byteArray);
        return this;
    }

    public ByteBufferWithInfo get(byte[] buffer, int offset, int length) {
        getByteBuffer().get(buffer, offset, length);
        return this;
    }

    public ByteBufferWithInfo put(byte x) {
        getByteBuffer().put(x);
        return this;
    }

    public ByteBufferWithInfo put(byte[] buffer, int offset, int length) {
        getByteBuffer().put(buffer, offset, length);
        return this;
    }

    public ByteBuffer getByteBuffer() {
        return byteBuffer;
    }

    public void setByteBuffer(ByteBuffer byteBuffer) {
        this.byteBuffer = byteBuffer;
    }

    public ByteOrder order() {
        return order;
    }

    public ByteBufferWithInfo order(ByteOrder byteOrder) {
        order = byteOrder;
        return this;
    }

    public byte get() {
        return getByteBuffer().get();
    }

    public boolean hasRemaining() {
        return remaining() > 0;
    }

    public int remaining() {
        return limit() - position();
    }

    public short getShort() {
        int b1, b2;

        if (isLittleEndian()) {
            b2 = (getByteBuffer().get()) & 0x000000FF;
            b1 = (getByteBuffer().get() << 8) & 0x0000FF00;
        } else {
            b1 = (getByteBuffer().get() << 8) & 0x0000FF00;
            b2 = (getByteBuffer().get()) & 0x000000FF;
        }

        return (short) (b1 | b2);
    }

    public long getLong() {
        long i1, i2;

        if (isLittleEndian()) {
            i2 = getInt() & 0xFFFFFFFFL;
            i1 = (long) getInt() << 32;
        } else {
            i1 = (long) getInt() << 32;
            i2 = getInt() & 0xFFFFFFFFL;
        }

        return i1 | i2;
    }

    private boolean isLittleEndian() {
        return order == ByteOrder.LITTLE_ENDIAN;
    }

    public int getInt() {

        int b1, b2, b3, b4;

        if (isLittleEndian()) {
            b4 = getByteBuffer().get() & 0xFF;
            b3 = getByteBuffer().get() & 0xFF;
            b2 = getByteBuffer().get() & 0xFF;
            b1 = getByteBuffer().get() & 0xFF;
        } else {
            b1 = getByteBuffer().get() & 0xFF;
            b2 = getByteBuffer().get() & 0xFF;
            b3 = getByteBuffer().get() & 0xFF;
            b4 = getByteBuffer().get() & 0xFF;
        }

        return (b1 << 24) | (b2 << 16) | (b3 << 8) | b4;
    }


    public ByteBufferWithInfo putLong(long x) {
        getByteBuffer().put((byte) ((x >>> 56) & 0xFF));
        getByteBuffer().put((byte) ((x >>> 48) & 0xFF));
        getByteBuffer().put((byte) ((x >>> 40) & 0xFF));
        getByteBuffer().put((byte) ((x >>> 32) & 0xFF));
        getByteBuffer().put((byte) ((x >>> 24) & 0xFF));
        getByteBuffer().put((byte) ((x >>> 16) & 0xFF));
        getByteBuffer().put((byte) ((x >>> 8) & 0xFF));
        getByteBuffer().put((byte) (x & 0xFF));
        return this;
    }

    public ByteBufferWithInfo putInt(int x) {
        getByteBuffer().put((byte) ((x >>> 24) & 0xFF));
        getByteBuffer().put((byte) ((x >>> 16) & 0xFF));
        getByteBuffer().put((byte) ((x >>> 8) & 0xFF));
        getByteBuffer().put((byte) (x & 0xFF));
        return this;
    }

    public ByteBufferWithInfo putShort(short x) {
        getByteBuffer().put((byte) ((x >>> 8) & 0xFF));
        getByteBuffer().put((byte) (x & 0xFF));
        return this;
    }

    public int capacity() {
        return getByteBuffer().capacity();
    }

    public int limit() {
        return getByteBuffer().limit();
    }

    public int position() {
        return getByteBuffer().position();
    }

    public ByteBufferWithInfo position(int newPosition) {
        getByteBuffer().position(newPosition);
        return this;
    }

    public ByteBufferWithInfo flip() {
        getByteBuffer().flip();
        return this;
    }

    public ByteBufferWithInfo limit(int theLength) {
        getByteBuffer().limit(theLength);
        return this;
    }

    public ByteBufferWithInfo slice() {
        ByteBufferWithInfo bufferWithInfo = new ByteBufferWithInfo(getByteBuffer().slice());
        bufferWithInfo.order(order());
        return bufferWithInfo;
    }

    public String toString() {
        StringBuilder str = new StringBuilder("ByteBufferWithInfo:");

        str.append(" length = ").append(limit());
        str.append(" position = ").append(position());
        str.append(" byteBuffer = ").append(getByteBuffer() == null ? "null" : "not null");

        return str.toString();
    }

}
