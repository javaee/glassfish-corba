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


import java.nio.ByteBuffer;

import com.sun.corba.se.spi.ior.iiop.GIOPVersion;
import com.sun.corba.se.spi.protocol.CorbaRequestId;

import com.sun.corba.se.impl.orbutil.ORBConstants;
import com.sun.corba.se.impl.protocol.CorbaRequestIdImpl;

public class Message_1_2 extends Message_1_1
{
    protected int request_id = (int) 0;

    Message_1_2() {}
    
    Message_1_2(int _magic, GIOPVersion _GIOP_version, byte _flags,
            byte _message_type, int _message_size) {

        super(_magic,
              _GIOP_version,
              _flags,
              _message_type,
              _message_size);
    }    

    /**
     * The byteBuffer is presumed to have contents of the message already
     * read in.  It must have 12 bytes of space at the beginning for the GIOP header,
     * but the header doesn't have to be copied in.
     */
    public void unmarshalRequestID(ByteBuffer byteBuffer) {
        int b1, b2, b3, b4;

        if (!isLittleEndian()) {
            b1 = (byteBuffer.get(GIOPMessageHeaderLength+0) << 24) & 0xFF000000;
            b2 = (byteBuffer.get(GIOPMessageHeaderLength+1) << 16) & 0x00FF0000;
            b3 = (byteBuffer.get(GIOPMessageHeaderLength+2) << 8)  & 0x0000FF00;
            b4 = (byteBuffer.get(GIOPMessageHeaderLength+3) << 0)  & 0x000000FF;
        } else {
            b1 = (byteBuffer.get(GIOPMessageHeaderLength+3) << 24) & 0xFF000000;
            b2 = (byteBuffer.get(GIOPMessageHeaderLength+2) << 16) & 0x00FF0000;
            b3 = (byteBuffer.get(GIOPMessageHeaderLength+1) << 8)  & 0x0000FF00;
            b4 = (byteBuffer.get(GIOPMessageHeaderLength+0) << 0)  & 0x000000FF;
        }

        this.request_id = (b1 | b2 | b3 | b4);
    }

    public void write(org.omg.CORBA.portable.OutputStream ostream) {
	if (getEncodingVersion() == ORBConstants.CDR_ENC_VERSION) {
	    super.write(ostream);
	    return;
	}
	GIOPVersion gv = GIOP_version; // save
	GIOP_version = GIOPVersion.getInstance(GIOPVersion.V13_XX.getMajor(),
					       getEncodingVersion());
	super.write(ostream);
	GIOP_version = gv; // restore
    }

    public CorbaRequestId getCorbaRequestId() {
        return new CorbaRequestIdImpl(this.request_id);
    }
}

