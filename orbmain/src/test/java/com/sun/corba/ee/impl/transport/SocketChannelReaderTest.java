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

import org.junit.Before;
import org.junit.Test;

import java.io.EOFException;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.Arrays;

import static org.junit.Assert.assertNull;
import static org.junit.Assert.fail;

public class SocketChannelReaderTest extends TransportTestBase {

    private static final byte[] DATA_TO_BE_READ = new byte[]{0, 1, 2, 3, 4, 5, 6};
    private SocketChannelReader reader;

    @Before
    public void setUpReaderTest() {
        reader = new SocketChannelReader(getOrb());
    }

    @Test
    public void whenCurrentBufferNull_allocateBufferAndRead() throws IOException {
        enqueData(DATA_TO_BE_READ);
        ByteBuffer buffer = reader.read(getSocketChannel(), null, 0);
        assertBufferContents(buffer, DATA_TO_BE_READ);
    }

    private void enqueData(byte[] dataToBeRead) {
        getSocketChannel().enqueData(dataToBeRead);
    }

    @Test
    public void whenCurrentBufferHasPartialData_readToAppendData() throws IOException {
        ByteBuffer oldBuffer = ByteBuffer.allocate(100);
        populateBuffer(oldBuffer, DATA_TO_BE_READ, 0, 3);
        enqueData(DATA_TO_BE_READ, 3, DATA_TO_BE_READ.length - 3);
        ByteBuffer buffer = reader.read(getSocketChannel(), oldBuffer, 0);
        assertBufferContents(buffer, DATA_TO_BE_READ);
    }

    private void populateBuffer(ByteBuffer buffer, byte[] bytes, int offset, int length) {
        buffer.put(bytes, offset, length);
        buffer.flip();
    }

    private void enqueData(byte[] dataToBeRead, int offset, int length) {
        enqueData(getSubarray(dataToBeRead, offset, length));
    }

    private byte[] getSubarray(byte[] dataToBeRead, int offset, int length) {
        byte[] data = new byte[Math.min(length, dataToBeRead.length-offset)];
        System.arraycopy(dataToBeRead, offset, data, 0, data.length);
        return data;
    }

    private void assertBufferContents(ByteBuffer buffer, byte... bytes) {
        buffer.flip();
        assertPopulatedBufferContents(buffer, bytes);
    }

    private void assertPopulatedBufferContents(ByteBuffer buffer, byte[] bytes) {
        byte[] actual = new byte[buffer.limit()];
        buffer.get(actual);
        assertEqualData(bytes, actual);
    }

    private void assertEqualData( byte[] expected, byte[] actual) {
        if (!Arrays.equals(expected, actual))
            fail( "expected " + Arrays.toString(expected) + " but was " + Arrays.toString(actual));
    }

    @Test
    public void whenCurrentBufferIsFull_readToAppendData() throws IOException {
        ByteBuffer oldBuffer = ByteBuffer.allocate(3);
        populateBuffer(oldBuffer, DATA_TO_BE_READ, 0, 3);
        enqueData(DATA_TO_BE_READ, 3, DATA_TO_BE_READ.length - 3);
        ByteBuffer buffer = reader.read(getSocketChannel(), oldBuffer, 0);
        assertBufferContents(buffer, DATA_TO_BE_READ);
    }

    @Test
    public void whenCurrentBufferTooSmallForIncomingData_reallocateAndAppend() throws IOException {
        ByteBuffer oldBuffer = ByteBuffer.allocate(5);
        populateBuffer(oldBuffer, DATA_TO_BE_READ, 0, 3);
        enqueData(DATA_TO_BE_READ, 3, DATA_TO_BE_READ.length - 3);
        ByteBuffer buffer = reader.read(getSocketChannel(), oldBuffer, DATA_TO_BE_READ.length);
        assertBufferContents(buffer, DATA_TO_BE_READ);
    }

    @Test
    public void whenMoreDataAvailableThanNeeded_ignoreIt() throws IOException {
        ByteBuffer oldBuffer = ByteBuffer.allocate(10);
        oldBuffer.flip();
        enqueData(DATA_TO_BE_READ);
        getSocketChannel().setNumBytesToRead(3, 3);
        ByteBuffer buffer = reader.read(getSocketChannel(), oldBuffer, 2);
        assertBufferContents(buffer, getSubarray(DATA_TO_BE_READ, 0, 3));
    }

    @Test(expected = EOFException.class)
    public void whenEOFDetectedThrowException() throws IOException {
        getSocketChannel().setEndOfInput();
        ByteBuffer oldBuffer = ByteBuffer.allocate(5);
        reader.read(getSocketChannel(), oldBuffer, 0);
    }

    @Test
    public void whenNoDataRemains_returnNull() throws IOException {
        ByteBuffer oldBuffer = ByteBuffer.allocate(10);
        populateBuffer(oldBuffer, DATA_TO_BE_READ, 0, DATA_TO_BE_READ.length);
        ByteBuffer buffer = reader.read(getSocketChannel(), oldBuffer, 10);
        assertNull(buffer);
        assertPopulatedBufferContents(oldBuffer, DATA_TO_BE_READ);
    }

    @Test
    public void whenAtCapacityAndNoDataRemains_returnNullAndPreserveOldBuffer() throws IOException {
        ByteBuffer oldBuffer = ByteBuffer.wrap(DATA_TO_BE_READ);
        ByteBuffer buffer = reader.read(getSocketChannel(), oldBuffer, 10);
        assertNull(buffer);
        assertPopulatedBufferContents(oldBuffer, DATA_TO_BE_READ);
    }
}
