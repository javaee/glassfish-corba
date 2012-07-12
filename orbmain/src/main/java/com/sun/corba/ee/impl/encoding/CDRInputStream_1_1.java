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

public class CDRInputStream_1_1 extends CDRInputStream_1_0
{
    // See notes in CDROutputStream
    protected int fragmentOffset = 0;

    @Override
    public GIOPVersion getGIOPVersion() {
        return GIOPVersion.V1_1;
    }

    // Template method
    @Override
    public CDRInputStreamBase dup() {
        CDRInputStreamBase result = super.dup();

        ((CDRInputStream_1_1)result).fragmentOffset = this.fragmentOffset;

        return result;
    }

    @Override
    protected int get_offset() {
        return bbwi.position() + fragmentOffset;
    }

    @Override
    protected void alignAndCheck(int align, int n) {


        checkBlockLength(align, n);

        // WARNING: Must compute real alignment after calling
        // checkBlockLength since it may move the position
        int alignment = computeAlignment(bbwi.position(), align);

        if (bbwi.position() + n + alignment  > bbwi.limit()) {

            // Some other ORBs may have found a way to send 1.1
            // fragments which put alignment bytes at the end
            // of a fragment
            if (bbwi.position() + alignment == bbwi.limit())
            {
                bbwi.position(bbwi.position() + alignment);
            }

            grow(align, n);

            // We must recalculate the alignment after a possible
            // fragmentation since the new bbwi.position() (after the header)
            // may require a different alignment.

            alignment = computeAlignment(bbwi.position(), align);
        }

        bbwi.position(bbwi.position() + alignment);
    }

    //
    // This can be overridden....
    //
    @Override
    protected void grow(int align, int n) {

        // Save the size of the current buffer for
        // possible fragmentOffset calculation
        int oldSize = bbwi.position();

        bbwi = bufferManagerRead.underflow(bbwi);

        if (bufferManagerRead.isFragmentOnUnderflow()) {
            
            // By this point we should be guaranteed to have
            // a new fragment whose header has already been
            // unmarshalled.  bbwi.position() should point to the
            // end of the header.
            fragmentOffset += (oldSize - bbwi.position());

            markAndResetHandler.fragmentationOccured(bbwi);
        }
    }

    // Mark/reset ---------------------------------------

    private class FragmentableStreamMemento extends StreamMemento
    {
        private int fragmentOffset_;

        public FragmentableStreamMemento()
        {
            super();

            fragmentOffset_ = fragmentOffset;
        }
    }

    @Override
    public java.lang.Object createStreamMemento() {
        return new FragmentableStreamMemento();
    }

    @Override
    public void restoreInternalState(java.lang.Object streamMemento) 
    {
        super.restoreInternalState(streamMemento);

        fragmentOffset 
            = ((FragmentableStreamMemento)streamMemento).fragmentOffset_;
    }

    // --------------------------------------------------

    @Override
    public char read_wchar() {
        // In GIOP 1.1, interoperability with wchar is limited
        // to 2 byte fixed width encodings.  CORBA formal 99-10-07 15.3.1.6.
        // WARNING:  For UTF-16, this means that there can be no
        // byte order marker, so it must default to big endian!
        alignAndCheck(2, 2);

        // Because of the alignAndCheck, we should be guaranteed
        // 2 bytes of real data.
        char[] result = getConvertedChars(2, getWCharConverter());

        // Did the provided bytes convert to more than one
        // character?  This may come up as more unicode values are
        // assigned, and a single 16 bit Java char isn't enough.
        // Better to use strings for i18n purposes.
        if (getWCharConverter().getNumChars() > 1)
            throw wrapper.btcResultMoreThanOneChar() ;

        return result[0];
    }

    @Override
    public String read_wstring() {
        // In GIOP 1.1, interoperability with wchar is limited
        // to 2 byte fixed width encodings.  CORBA formal 99-10-07 15.3.1.6.
        int len = read_long();

        // Workaround for ORBs which send string lengths of
        // zero to mean empty string.
        //
        // IMPORTANT: Do not replace 'new String("")' with "", it may result
        // in a Serialization bug (See serialization.zerolengthstring) and
        // bug id: 4728756 for details
        if (len == 0)
            return new String("");

        checkForNegativeLength(len);

        // Don't include the two byte null for the
        // following computations.  Remember that since we're limited
        // to a 2 byte fixed width code set, the "length" was the
        // number of such 2 byte code points plus a 2 byte null.
        len = len - 1;

        char[] result = getConvertedChars(len * 2, getWCharConverter());

        // Skip over the 2 byte null
        read_short();

        return new String(result, 0, getWCharConverter().getNumChars());
    }

}
