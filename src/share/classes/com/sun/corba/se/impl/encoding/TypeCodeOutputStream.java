/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2002-2007 Sun Microsystems, Inc. All rights reserved.
 *
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common Development
 * and Distribution License("CDDL") (collectively, the "License").  You
 * may not use this file except in compliance with the License. You can obtain
 * a copy of the License at https://glassfish.dev.java.net/public/CDDL+GPL.html
 * or glassfish/bootstrap/legal/LICENSE.txt.  See the License for the specific
 * language governing permissions and limitations under the License.
 *
 * When distributing the software, include this License Header Notice in each
 * file and include the License file at glassfish/bootstrap/legal/LICENSE.txt.
 * Sun designates this particular file as subject to the "Classpath" exception
 * as provided by Sun in the GPL Version 2 section of the License file that
 * accompanied this code.  If applicable, add the following below the License
 * Header, with the fields enclosed by brackets [] replaced by your own
 * identifying information: "Portions Copyrighted [year]
 * [name of copyright owner]"
 *
 * Contributor(s):
 *
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

package com.sun.corba.se.impl.encoding;

import org.omg.CORBA.TypeCode ;
import org.omg.CORBA.StructMember ;
import org.omg.CORBA.UnionMember ;
import org.omg.CORBA.ValueMember ;
import org.omg.CORBA.TCKind ;
import org.omg.CORBA.Any ;
import org.omg.CORBA.CompletionStatus ;

import org.omg.CORBA.TypeCodePackage.BadKind ;

import org.omg.CORBA_2_3.portable.InputStream;
import org.omg.CORBA_2_3.portable.OutputStream;

import com.sun.corba.se.spi.orb.ORB;
import com.sun.corba.se.impl.encoding.OSFCodeSetRegistry;
import com.sun.corba.se.impl.encoding.MarshalInputStream;
import com.sun.corba.se.spi.ior.iiop.GIOPVersion;
import com.sun.corba.se.impl.encoding.CodeSetConversion;

import com.sun.corba.se.impl.encoding.CDRInputStream;
import com.sun.corba.se.impl.encoding.CDROutputStream;
import com.sun.corba.se.impl.orbutil.ORBUtility;

import java.util.HashMap;
import java.util.Map;
import java.util.Iterator;
import java.util.List;
import java.util.Collections;
import java.util.ArrayList;
import java.io.IOException;
import java.io.PrintStream;
import java.io.ByteArrayOutputStream;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.nio.ByteBuffer;

public final class TypeCodeOutputStream extends EncapsOutputStream 
{
    private OutputStream enclosure = null;
    private Map<String,Integer> typeMap = null;
    private boolean isEncapsulation = false;

    public TypeCodeOutputStream(ORB orb) {
        super(orb, false);
    }

    public TypeCodeOutputStream(ORB orb, boolean littleEndian) {
        super(orb, littleEndian);
    }

    public org.omg.CORBA.portable.InputStream create_input_stream()
    {
        TypeCodeInputStream tcis 
            = new TypeCodeInputStream((ORB)orb(), getByteBuffer(), getIndex(), 
		isLittleEndian(), getGIOPVersion());
        return tcis;
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
            int pos = ((TypeCodeOutputStream)enclosure).getTopLevelPosition() + getPosition();
            // Add four bytes for the encaps length, not another 4 for the byte order
            // which is included in getPosition().
            if (isEncapsulation) pos += 4;
            //if (TypeCodeImpl.debug) {
                //System.out.println("TypeCodeOutputStream.getTopLevelPosition using getTopLevelPosition " +
                    //((TypeCodeOutputStream)enclosure).getTopLevelPosition() +
                    //" + getPosition() " + getPosition() +
                    //(isEncapsulation ? " + encaps length 4" : "") +
                    //" = " + pos);
            //}
            return pos;
        }
        //if (TypeCodeImpl.debug) {
            //System.out.println("TypeCodeOutputStream.getTopLevelPosition returning getPosition() = " +
                               //getPosition() + ", enclosure is " + enclosure);
        //}
        return getPosition();
    }

    public void addIDAtPosition(String id, int position) {
        if (typeMap == null)
            typeMap = new HashMap<String,Integer>(16);
        //if (TypeCodeImpl.debug) System.out.println(this + " adding id " + id + " at position " + position);
        typeMap.put(id, position);
    }

    public int getPositionForID(String id) {
        if (typeMap == null)
	    throw wrapper.refTypeIndirType( CompletionStatus.COMPLETED_NO ) ;
        //if (TypeCodeImpl.debug) System.out.println("Getting position " + typeMap.get(id) +
            //" for id " + id);
        return typeMap.get(id) ;
    }

    public void writeRawBuffer(org.omg.CORBA.portable.OutputStream s, int firstLong) {
        // Writes this streams buffer to the given OutputStream
        // without byte order flag and length as is the case for encapsulations.

        // Make sure to align s to 4 byte boundaries.
        // Unfortunately we can't do just this:
        // s.alignAndReserve(4, 4);
        // So we have to take the first four bytes given in firstLong and write them
        // with a call to write_long which will trigger the alignment.
        // Then write the rest of the byte array.

        //if (TypeCodeImpl.debug) {
            //System.out.println(this + ".writeRawBuffer(" + s + ", " + firstLong + ")");
            //if (s instanceof CDROutputStream) {
                //System.out.println("Parent position before writing kind = " + ((CDROutputStream)s).getIndex());
            //}
        //}
        s.write_long(firstLong);
        //if (TypeCodeImpl.debug) {
            //if (s instanceof CDROutputStream) {
                //System.out.println("Parent position after writing kind = " + ((CDROutputStream)s).getIndex());
            //}
        //}
	byte[] buf = ORBUtility.getByteBufferArray(getByteBuffer());
	s.write_octet_array(buf, 4, getIndex() - 4);
        //if (TypeCodeImpl.debug) {
            //if (s instanceof CDROutputStream) {
                //System.out.println("Parent position after writing all " + getIndex() + " bytes = " + ((CDROutputStream)s).getIndex());
            //}
        //}
    }

    public TypeCodeOutputStream createEncapsulation(org.omg.CORBA.ORB _orb) {
	TypeCodeOutputStream encap = new TypeCodeOutputStream((ORB)_orb, isLittleEndian());
	encap.setEnclosingOutputStream(this);
        encap.makeEncapsulation();
        //if (TypeCodeImpl.debug) System.out.println("Created TypeCodeOutputStream " + encap + " with parent " + this);
	return encap;
    }

    protected void makeEncapsulation() {
        // first entry in an encapsulation is the endianess
        putEndian();
        isEncapsulation = true;
    }

    public static TypeCodeOutputStream wrapOutputStream(OutputStream os) {
        boolean littleEndian = ((os instanceof CDROutputStream) ? ((CDROutputStream)os).isLittleEndian() : false);
        TypeCodeOutputStream tos = new TypeCodeOutputStream((ORB)os.orb(), littleEndian);
        tos.setEnclosingOutputStream(os);
        //if (TypeCodeImpl.debug) System.out.println("Created TypeCodeOutputStream " + tos + " with parent " + os);
        return tos;
    }

    public int getPosition() {
        return getIndex();
    }

    public int getRealIndex(int index) {
        int topPos = getTopLevelPosition();
        //if (TypeCodeImpl.debug) System.out.println("TypeCodeOutputStream.getRealIndex using getTopLevelPosition " +
            //topPos + " instead of getPosition " + getPosition());
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
            System.out.println("  key = " + id + ", value = " + typeMap.get(id));
	}
        System.out.println("}");
    }
}
