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

package com.sun.corba.ee.impl.encoding;

import com.sun.corba.ee.impl.protocol.giopmsgheaders.Message;
import org.glassfish.corba.testutils.HexBuffer;

import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.util.Stack;

public class ValueTestBase extends EncodingTestBase {
    protected static final int USE_CODEBASE = 0x01;
    protected static final int ONE_REPID_ID = 0x02;
    protected static final int USE_CHUNKING = 0x08;
    private static final int BASE_VALUE_TAG = 0x7fffff00;

    private DataByteOutputStream out = new DataByteOutputStream();
    private final Stack<DataByteOutputStream> chunkStack = new Stack<DataByteOutputStream>();

    protected void writeValueTag(int flags) throws IOException {
        writeInt(BASE_VALUE_TAG | flags);
    }

    protected byte[] getGeneratedBody() {
        return out.toByteArray();
    }

    protected void writeByte(int aByte) throws IOException {
        out.write(aByte);
    }

    protected int getCurrentLocation() {
        return out.pos();
    }

    protected void writeCodebase(String location) throws IOException {
        writeString(location);
    }

    protected void writeNull() throws IOException {
        writeInt(0);
    }

    protected void dumpBuffer() {
        HexBuffer.dumpBuffer(getGeneratedBody());
    }

    protected void writeWchar_1_1(char aChar) throws IOException {
        out.write((aChar >> 8));
        out.write(aChar);
    }

    protected void writeWchar_1_2(char aChar) throws IOException {
        out.write(4);
        writeBigEndianMarker();
        out.write((aChar >> 8));
        out.write(aChar);
    }

    protected void writeEndTag(int chunkLevel) throws IOException {
        writeInt(chunkLevel);
    }

    /** When starting a new chunk, align and reserve space for the chunk length. **/
    protected void startChunk() throws IOException {
        align(4);
        chunkStack.push(out);
        out = new DataByteOutputStream(out.pos() + 4);
    }

    protected void endChunk() throws IOException {
        byte[] chunkData = out.toByteArray();
        out = chunkStack.pop();
        writeInt(chunkData.length);
        out.write(chunkData);
    }

    protected void writeStringValue_1_2(String value) throws IOException {
        writeInt(2 + 2*value.length());
        writeBigEndianMarker();
        for (char aChar : value.toCharArray()) {
            out.write(0);
            out.write(aChar);
        }
    }

    private void writeBigEndianMarker() throws IOException {
        out.write(FE);
        out.write(FF);
    }

    protected void writeRepId(String id) throws IOException {
        writeString(id);
    }

    protected void writeString(String string) throws IOException {
        writeInt(string.length() + 1);
        for (char aChar : string.toCharArray())
            out.write(aChar);
        out.write(0);
    }

    protected void writeInt(int value) throws IOException {
        align(4);
        out.writeInt(value);
    }

    protected void writeLong(long value) throws IOException {
        align(8);
        out.writeLong(value);
    }

    private void align(int size) throws IOException {
        while ((out.pos() % size) != 0)
            out.write(0);
    }

    protected void writeIndirectionTo(int location) throws IOException {
        writeInt(-1);
        writeInt(location - out.pos());
    }

    static class DataByteOutputStream extends DataOutputStream {
        private int streamStart;

        DataByteOutputStream() {
            this(Message.GIOPMessageHeaderLength);
        }

        DataByteOutputStream(int streamStart) {
            super(new ByteArrayOutputStream());
            this.streamStart = streamStart;
        }

        private byte[] toByteArray() {
            return ((ByteArrayOutputStream) out).toByteArray();
        }

        private int pos() {
            return streamStart + size();
        }
    }
}
