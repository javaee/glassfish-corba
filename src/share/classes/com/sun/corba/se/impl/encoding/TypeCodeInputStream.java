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

import org.omg.CORBA.TypeCode ;
import org.omg.CORBA.StructMember ;
import org.omg.CORBA.UnionMember ;
import org.omg.CORBA.ValueMember ;
import org.omg.CORBA.TCKind ;
import org.omg.CORBA.Any ;
import org.omg.CORBA.BAD_TYPECODE ;
import org.omg.CORBA.BAD_PARAM ;
import org.omg.CORBA.BAD_OPERATION ;
import org.omg.CORBA.INTERNAL ;
import org.omg.CORBA.MARSHAL ;

import org.omg.CORBA.TypeCodePackage.BadKind ;

import org.omg.CORBA_2_3.portable.InputStream;
import org.omg.CORBA_2_3.portable.OutputStream;

import com.sun.corba.se.spi.ior.iiop.GIOPVersion;
import com.sun.corba.se.impl.corba.TypeCodeImpl;
import com.sun.corba.se.spi.orb.ORB;
import com.sun.corba.se.impl.encoding.CodeSetConversion;
import com.sun.corba.se.impl.encoding.CDRInputStream;
import com.sun.corba.se.impl.encoding.CDROutputStream;
import com.sun.corba.se.impl.encoding.MarshalInputStream;

public class TypeCodeInputStream extends EncapsInputStream implements TypeCodeReader
{
    private Map<Integer,TypeCodeImpl> typeMap = null;
    private InputStream enclosure = null;
    private boolean isEncapsulation = false;

    public TypeCodeInputStream(org.omg.CORBA.ORB orb, byte[] data, int size) {
        super(orb, data, size);
    }

    public TypeCodeInputStream(org.omg.CORBA.ORB orb, 
                               byte[] data, 
                               int size,
                               boolean littleEndian,
                               GIOPVersion version) {
        super(orb, data, size, littleEndian, version);
    }

    public TypeCodeInputStream(org.omg.CORBA.ORB orb,
                               ByteBuffer byteBuffer,
                               int size,
                               boolean littleEndian,
                               GIOPVersion version) {
        super(orb, byteBuffer, size, littleEndian, version);
    }

    public void addTypeCodeAtPosition(TypeCodeImpl tc, int position) {
        if (typeMap == null) {
            //if (TypeCodeImpl.debug) System.out.println("Creating typeMap");
            typeMap = new HashMap<Integer,TypeCodeImpl>(16);
        }
        //if (TypeCodeImpl.debug) System.out.println(this + " adding tc " + tc + " at position " + position);
        typeMap.put(position, tc);
    }

    public TypeCodeImpl getTypeCodeAtPosition(int position) {
        if (typeMap == null)
	    return null;
        //if (TypeCodeImpl.debug) {
            //System.out.println("Getting tc " + typeMap.get(position) +
                               //" at position " + position);
        //}
        return typeMap.get(position);
    }

    public void setEnclosingInputStream(InputStream enclosure) {
        this.enclosure = enclosure;
    }

    public TypeCodeReader getTopLevelStream() {
        if (enclosure == null)
            return this;
        if (enclosure instanceof TypeCodeReader)
            return ((TypeCodeReader)enclosure).getTopLevelStream();
        return this;
    }

    public int getTopLevelPosition() {
        if (enclosure != null && enclosure instanceof TypeCodeReader) {
            // The enclosed stream has to consider if the enclosing stream
            // had to read the enclosed stream completely when creating it.
            // This is why the size of the enclosed stream needs to be substracted.
            int topPos = ((TypeCodeReader)enclosure).getTopLevelPosition();
            // Substract getBufferLength from the parents pos because it read this stream
            // from its own when creating it
            int pos = topPos - getBufferLength() + getPosition();
            //if (TypeCodeImpl.debug) {
                //System.out.println("TypeCodeInputStream.getTopLevelPosition using getTopLevelPosition " + topPos +
                    //(isEncapsulation ? " - encaps length 4" : "") +
                    //" - getBufferLength() " + getBufferLength() +
                    //" + getPosition() " + getPosition() + " = " + pos);
            //}
            return pos;
        }
        //if (TypeCodeImpl.debug) {
            //System.out.println("TypeCodeInputStream.getTopLevelPosition returning getPosition() = " +
                               //getPosition() + " because enclosure is " + enclosure);
        //}
        return getPosition();
    }

    public static TypeCodeInputStream readEncapsulation(InputStream is, org.omg.CORBA.ORB _orb) {
	// _REVISIT_ Would be nice if we didn't have to copy the buffer!
	TypeCodeInputStream encap;

        int encapLength = is.read_long();

        // read off part of the buffer corresponding to the encapsulation
	byte[] encapBuffer = new byte[encapLength];
	is.read_octet_array(encapBuffer, 0, encapBuffer.length);

	// create an encapsulation using the marshal buffer
        if (is instanceof CDRInputStream) {
            encap = new TypeCodeInputStream((ORB)_orb, encapBuffer, encapBuffer.length,
                                            ((CDRInputStream)is).isLittleEndian(),
                                            ((CDRInputStream)is).getGIOPVersion());
        } else {
            encap = new TypeCodeInputStream((ORB)_orb, encapBuffer, encapBuffer.length);
        }
	encap.setEnclosingInputStream(is);
        encap.makeEncapsulation();
	return encap;
    }

    protected void makeEncapsulation() {
        // first entry in an encapsulation is the endianess
        consumeEndian();
        isEncapsulation = true;
    }

    public void printTypeMap() {
        System.out.println("typeMap = {");
	for (Integer pos : typeMap.keySet() ) {
	    System.out.println( "  key = " + pos + ", value = " +
		typeMap.get(pos).description() ) ;
	}
	System.out.println("}") ;
    }
}
