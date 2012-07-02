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


import org.omg.CORBA_2_3.portable.OutputStream;

import com.sun.corba.ee.spi.orb.ORB;

import com.sun.corba.ee.impl.misc.ORBUtility;

import java.util.HashMap;
import java.util.Map;
import java.nio.ByteBuffer;

public final class TypeCodeOutputStream extends EncapsOutputStream 
{
    private OutputStream enclosure = null;
    private Map<String,Integer> typeMap = null;
    private boolean isEncapsulation = false;

    public TypeCodeOutputStream(ORB orb) {
        super(orb);
    }

    @Override
    public org.omg.CORBA.portable.InputStream create_input_stream() {
        return new TypeCodeInputStream(orb(), getByteBuffer(), getIndex(), false, getGIOPVersion());
    }

    public void setEnclosingOutputStream(OutputStream enclosure) {
        this.enclosure = enclosure;
    }

    public TypeCodeOutputStream getTopLevelStream() {
        if (enclosure == null)
            return this;
        if (enclosure instanceof TypeCodeOutputStream)
            return ((TypeCodeOutputStream)enclosure).getTopLevelStream();
        return this;
    }

    public int getTopLevelPosition() {
        if (enclosure != null && enclosure instanceof TypeCodeOutputStream) {
            int pos = ((TypeCodeOutputStream)enclosure).getTopLevelPosition()
                + getPosition();
            // Add four bytes for the encaps length, not another 4 for the 
            // byte order which is included in getPosition().
            if (isEncapsulation) {
                pos += 4;
            }

            return pos;
        }
        return getPosition();
    }

    public void addIDAtPosition(String id, int position) {
        if (typeMap == null)
            typeMap = new HashMap<String,Integer>(16);
        typeMap.put(id, position);
    }

    public int getPositionForID(String id) {
        if (typeMap == null)
            throw wrapper.refTypeIndirType() ;
        return
            typeMap.get(id) ;
    }

    public void writeRawBuffer(org.omg.CORBA.portable.OutputStream s, int firstLong) {
        // Writes this streams buffer to the given OutputStream
        // without byte order flag and length as is the case for encapsulations.

        // Make sure to align s to 4 byte boundaries.
        // Unfortunately we can't do just this:
        // s.alignAndReserve(4, 4);
        // So we have to take the first four bytes given in firstLong
        // and write them
        // with a call to write_long which will trigger the alignment.
        // Then write the rest of the byte array.

        s.write_long(firstLong);
        byte[] buf = ORBUtility.getByteBufferArray(getByteBuffer());
        s.write_octet_array(buf, 4, getIndex() - 4);
    }

    public TypeCodeOutputStream createEncapsulation(org.omg.CORBA.ORB _orb) {
        TypeCodeOutputStream encap = new TypeCodeOutputStream((ORB)_orb);
        encap.setEnclosingOutputStream(this);
        encap.makeEncapsulation();
        return encap;
    }

    protected void makeEncapsulation() {
        // first entry in an encapsulation is the endianess
        putEndian();
        isEncapsulation = true;
    }

    public static TypeCodeOutputStream wrapOutputStream(OutputStream os) {
        TypeCodeOutputStream tos = new TypeCodeOutputStream((ORB)os.orb());
        tos.setEnclosingOutputStream(os);
        return tos;
    }

    public int getPosition() {
        return getIndex();
    }

    @Override
    public int getRealIndex(int index) {
        int topPos = getTopLevelPosition();
        return topPos;
    }

    public byte[] getTypeCodeBuffer() {
        // Returns the buffer trimmed of the trailing zeros and without the
        // known _kind value at the beginning.
        ByteBuffer theBuffer = getByteBuffer();
        //System.out.println("outBuffer length = " + (getIndex() - 4));
        byte[] tcBuffer = new byte[getIndex() - 4];
        // Micro-benchmarks show that DirectByteBuffer.get(int) is faster
        // than DirectByteBuffer.get(byte[], offset, length).
        // REVISIT - May want to check if buffer is direct or non-direct
        //           and use array copy if ByteBuffer is non-direct.
        for (int i = 0; i < tcBuffer.length; i++)
            tcBuffer[i] = theBuffer.get(i+4);
        return tcBuffer;
    }

    public void printTypeMap() {
        System.out.println("typeMap = {");
        for (String id : typeMap.keySet()) {
            System.out.println("  key = " + id + ", value = "
                + typeMap.get(id));
        }
        System.out.println("}");
    }
}
