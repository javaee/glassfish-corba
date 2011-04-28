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
package com.sun.corba.se.impl.encoding;

import com.sun.corba.se.spi.ior.iiop.GIOPVersion;
import com.sun.corba.se.spi.misc.ORBConstants;
import com.sun.corba.se.spi.trace.CdrRead;

@CdrRead
public class CDRInputStream_1_2 extends CDRInputStream_1_1
{
    // Indicates whether the header is padded. In GIOP 1.2 and above,
    // the body must be aligned on an 8-octet boundary, and so the header is
    // padded appropriately. However, if there is no body to a request or reply
    // message, there is no header padding, in the unfragmented case.
    protected boolean headerPadding;
    
    // used to remember headerPadding flag when mark() and restore() are used.
    protected boolean restoreHeaderPadding;

    // Called by RequestMessage_1_2 or ReplyMessage_1_2 classes only.
    @Override
    void setHeaderPadding(boolean headerPadding) {
        this.headerPadding = headerPadding;
    }

    // the mark and reset methods have been overridden to remember the
    // headerPadding flag.
    
    @Override
    public void mark(int readlimit) {
        super.mark(readlimit);
        restoreHeaderPadding = headerPadding;
    }

    @Override
    public void reset() {
        super.reset();
        headerPadding = restoreHeaderPadding;
        restoreHeaderPadding = false;
    }

    // Template method
    // This method has been overriden to ensure that the duplicated stream
    // inherits the headerPadding flag, in case of GIOP 1.2 and above, streams.
    @Override
    public CDRInputStreamBase dup() {
        CDRInputStreamBase result = super.dup();
        ((CDRInputStream_1_2)result).headerPadding = this.headerPadding;
        return result;
    }
    
    @CdrRead
    @Override
    protected void alignAndCheck(int align, int n) {
        // headerPadding bit is set by read method of the RequestMessage_1_2
        // or ReplyMessage_1_2 classes. When set, the very first body read
        // operation (from the stub code) would trigger an alignAndCheck
        // method call, that would in turn skip the header padding that was
        // inserted during the earlier write operation by the sender. The
        // padding ensures that the body is aligned on an 8-octet boundary,
        // for GIOP versions 1.2 and beyond.
        if (headerPadding == true) {
            headerPadding = false;
            alignOnBoundary(ORBConstants.GIOP_12_MSG_BODY_ALIGNMENT);
        }

        checkBlockLength(align, n);

        // WARNING: Must compute real alignment after calling
        // checkBlockLength since it may move the position

        // In GIOP 1.2, a fragment may end with some alignment
        // padding (which leads to all fragments ending perfectly
        // on evenly divisible 8 byte boundaries).  A new fragment
        // never requires alignment with the header since it ends
        // on an 8 byte boundary.
        // NOTE: Change underlying ByteBuffer's position only if
        //       alignIncr is less than or equal to underlying
        //       ByteBuffer's limit.
        int savedPosition = bbwi.position();
        int alignIncr = computeAlignment(savedPosition,align);
        int bytesNeeded = alignIncr + n;
        if (savedPosition + alignIncr <= bbwi.getLength()) {
            bbwi.position(savedPosition + alignIncr);
        }

        if (savedPosition + bytesNeeded > bbwi.getLength()) {
            grow(1, n);
        }
    }

    @Override
    public GIOPVersion getGIOPVersion() {
        return GIOPVersion.V1_2;
    }
        
    @Override
    public char read_wchar() {
        // In GIOP 1.2, a wchar is encoded as an unsigned octet length
        // followed by the octets of the converted wchar.
        int numBytes = read_octet();

        char[] result = getConvertedChars(numBytes, getWCharConverter());

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
        // In GIOP 1.2, wstrings are not terminated by a null.  The
        // length is the number of octets in the converted format.
        // A zero length string is represented with the 4 byte length
        // value of 0.

        int len = read_long();

        //
        // IMPORTANT: Do not replace 'new String("")' with "", it may result
        // in a Serialization bug (See serialization.zerolengthstring) and
        // bug id: 4728756 for details
        if (len == 0)
            return new String("");

        checkForNegativeLength(len);

        return new String(getConvertedChars(len, getWCharConverter()),
                          0,
                          getWCharConverter().getNumChars());
    }
}
