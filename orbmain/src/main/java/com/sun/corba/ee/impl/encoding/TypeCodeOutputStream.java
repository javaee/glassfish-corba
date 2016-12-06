/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright (c) 1997-2012 Oracle and/or its affiliates. All rights reserved.
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


import com.sun.corba.ee.spi.ior.iiop.GIOPVersion;
import org.omg.CORBA_2_3.portable.OutputStream;

import com.sun.corba.ee.spi.orb.ORB;

import java.nio.ByteOrder;
import java.util.HashMap;
import java.util.Map;
import java.nio.ByteBuffer;

public final class TypeCodeOutputStream extends EncapsOutputStream {

    private static final InputObjectFactory TYPE_CODE_INPUT_OBJECT_FACTORY = new TypeCodeInputStreamFactory();

    private OutputStream enclosure = null;
    private Map<String,Integer> typeMap = null;
    private boolean isEncapsulation = false;

    public TypeCodeOutputStream(ORB orb) {
        super(orb);
    }

    @Override
    public org.omg.CORBA.portable.InputStream create_input_stream() {
        return createInputObject(null, TYPE_CODE_INPUT_OBJECT_FACTORY);
    }

    private static class TypeCodeInputStreamFactory implements InputObjectFactory {
        @Override
        public CDRInputObject createInputObject(CDROutputObject outputObject, ORB orb, ByteBuffer byteBuffer, int size, GIOPVersion giopVersion) {
            return EncapsInputStreamFactory.newTypeCodeInputStream(outputObject.orb(), byteBuffer, size, ByteOrder.BIG_ENDIAN, giopVersion);
        }
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

    public TypeCodeOutputStream createEncapsulation(org.omg.CORBA.ORB _orb) {
        TypeCodeOutputStream encap = OutputStreamFactory.newTypeCodeOutputStream((ORB)_orb);
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
        TypeCodeOutputStream tos = OutputStreamFactory.newTypeCodeOutputStream((ORB)os.orb());
        tos.setEnclosingOutputStream(os);
        return tos;
    }

    public int getPosition() {
        return getIndex();
    }

    @Override
    public int getRealIndex(int index) {
        return getTopLevelPosition();
    }

    public byte[] getTypeCodeBuffer() {
        return toByteArray(4);
    }

}
