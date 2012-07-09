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

public class CDROutputStream_1_1 extends CDROutputStream_1_0
{
    // This is used to keep indirections working across fragments.  When added
    // to the current bbwi.position(), the result is the current position
    // in the byte stream without any fragment headers.
    // 
    // It is equal to the following:
    //
    // n = number of buffers (0 is original buffer, 1 is first fragment, etc)
    // 
    // n == 0, fragmentOffset = 0
    //
    // n > 0, fragmentOffset
    //          = sum i=[1,n] { bbwi_i-1_.size - buffer i header length }
    //
    protected int fragmentOffset = 0;

    @Override
    protected void alignAndReserve(int align, int n) {

        // Notice that in 1.1, we won't end a fragment with
        // alignment padding.  We also won't guarantee that
        // our fragments end on evenly divisible 8 byte
        // boundaries.  There may be alignment
        // necessary with the header of the next fragment
        // since the header isn't aligned on an 8 byte
        // boundary, so we have to calculate it twice.

        int alignment = computeAlignment(align);

        if (bbwi.position() + n + alignment > bbwi.getLength()) {
            grow(align, n);

            // Must recompute the alignment after a grow.
            // In the case of fragmentation, the alignment
            // calculation may no longer be correct.

            // People shouldn't be able to set their fragment
            // sizes so small that the fragment header plus
            // this alignment fills the entire buffer.
            alignment = computeAlignment(align);
        }

        bbwi.position(bbwi.position() + alignment);
    }

    @Override
    protected void grow(int align, int n) {
        // Save the current size for possible post-fragmentation calculation
        int oldSize = bbwi.position();

        bufferManagerWrite.overflow(bbwi, n);

        // At this point, if we fragmented, we should have a ByteBufferWithInfo
        // with the fragment header already marshalled.  The size and length fields
        // should be updated accordingly, and the fragmented flag should be set.
        if (bufferManagerWrite.isFragmentOnOverflow()) {

            // Update fragmentOffset so indirections work properly.
            // At this point, oldSize is the entire length of the
            // previous buffer.  bbwi.position() is the length of the
            // fragment header of this buffer.
            fragmentOffset += (oldSize - bbwi.position());
        }
    }

    @Override
    public int get_offset() {
        return bbwi.position() + fragmentOffset;
    }

    @Override
    public GIOPVersion getGIOPVersion() {
        return GIOPVersion.V1_1;
    }

    @Override
    public void write_wchar(char x)
    {
        // In GIOP 1.1, interoperability with wchar is limited
        // to 2 byte fixed width encodings.  CORBA formal 99-10-07 15.3.1.6.
        // Note that the following code prohibits UTF-16 with a byte
        // order marker (which would result in 4 bytes).
        CodeSetConversion.CTBConverter converter = getWCharConverter();

        converter.convert(x);

        if (converter.getNumBytes() != 2)
            throw wrapper.badGiop11Ctb();

        alignAndReserve(converter.getAlignment(),
                        converter.getNumBytes());

        parent.write_octet_array(converter.getBytes(),
                                 0,
                                 converter.getNumBytes());
    }

    @Override
    public void write_wstring(String value)
    {
        if (value == null) {
            throw wrapper.nullParam();
        }

        // The length is the number of code points (which are 2 bytes each)
        // including the 2 byte null.  See CORBA formal 99-10-07 15.3.2.7.

        int len = value.length() + 1;

        write_long(len);

        CodeSetConversion.CTBConverter converter = getWCharConverter();

        converter.convert(value);

        internalWriteOctetArray(converter.getBytes(), 0, converter.getNumBytes());

        // Write the 2 byte null ending
        write_short((short)0);
    }
}

