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

package com.sun.corba.se.impl.protocol.giopmsgheaders;

import java.io.IOException;
import java.nio.ByteBuffer;

import com.sun.corba.se.spi.ior.iiop.GIOPVersion;
import com.sun.corba.se.spi.protocol.CorbaRequestId;

/**
 * This is the base interface for different message type interfaces.
 *
 * @author Ram Jeyaraman 05/14/2000
 * @version 1.0
 */

public interface Message {

    // Generic constants

    static final int defaultBufferSize = 1024;
    static final int GIOPBigEndian = 0;
    static final int GIOPLittleEndian = 1;
    static final int GIOPBigMagic =    0x47494F50;
    static final int GIOPLittleMagic = 0x504F4947;
    static final int GIOPMessageHeaderLength = 12;

    // Other useful constants

    static final byte LITTLE_ENDIAN_BIT = 0x01;
    static final byte MORE_FRAGMENTS_BIT = 0x02;
    static final byte FLAG_NO_FRAG_BIG_ENDIAN = 0x00;
    static final byte TRAILING_TWO_BIT_BYTE_MASK = 0x3;
    static final byte THREAD_POOL_TO_USE_MASK = 0x3F;

    // Message types

    static final byte GIOPRequest = 0;
    static final byte GIOPReply = 1;
    static final byte GIOPCancelRequest = 2;
    static final byte GIOPLocateRequest = 3;
    static final byte GIOPLocateReply = 4;
    static final byte GIOPCloseConnection = 5;
    static final byte GIOPMessageError = 6;
    static final byte GIOPFragment = 7; // 1.1 & 1.2:

    // Accessor methods

    GIOPVersion getGIOPVersion();
    byte getEncodingVersion();
    boolean isLittleEndian();
    boolean moreFragmentsToFollow();
    int getType();
    int getSize();
    ByteBuffer getByteBuffer();
    int getThreadPoolToUse();

    // Mutator methods

    void read(org.omg.CORBA.portable.InputStream istream);
    void write(org.omg.CORBA.portable.OutputStream ostream);

    void setSize(ByteBuffer byteBuffer, int size);

    FragmentMessage createFragmentMessage();

    void callback(MessageHandler handler) throws IOException;

    void setByteBuffer(ByteBuffer byteBuffer);
    void setEncodingVersion(byte version);
    
    /**
     * Return a Message's CorbaRequestId. Messages which do not support
     * a request id in the 4 bytes following the 12 byte GIOP message 
     * header shall return an undefined CorbaRequestId.
     */
    CorbaRequestId getCorbaRequestId();
}
