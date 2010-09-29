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

package com.sun.corba.se.impl.protocol.giopmsgheaders;

import java.nio.ByteBuffer;
import com.sun.corba.se.spi.ior.iiop.GIOPVersion;

import com.sun.corba.se.spi.logging.ORBUtilSystemException ;

/*
 * This implements the GIOP 1.0 Message header.
 *
 * @author Ram Jeyaraman 05/14/2000
 * @version 1.0
 */

public class Message_1_0
        extends com.sun.corba.se.impl.protocol.giopmsgheaders.MessageBase {

    private static ORBUtilSystemException wrapper =
        ORBUtilSystemException.self ;

    // Instance variables
    int magic = 0;
    GIOPVersion GIOP_version = null;
    boolean byte_order = false;
    byte message_type = (byte) 0;
    int message_size = (int) 0;

    // Constructor

    Message_1_0() {
    }
    
    Message_1_0(int _magic, boolean _byte_order, byte _message_type,
            int _message_size) {
        magic = _magic;
        GIOP_version = GIOPVersion.V1_0;
        byte_order = _byte_order;
        message_type = _message_type;
        message_size = _message_size;
    }

    // Accessor methods

    public GIOPVersion getGIOPVersion() {
        return this.GIOP_version;
    }

    public int getType() {
    	return this.message_type;
    }

    public int getSize() {
	    return this.message_size;
    }

    public boolean isLittleEndian() {
    	return this.byte_order;
    }

    public boolean moreFragmentsToFollow() {
    	return false;
    }

    // Mutator methods

    public void	setSize(ByteBuffer byteBuffer, int size) {
	    this.message_size = size;

        //
    	// Patch the size field in the header.
    	//
	    int patch = size - GIOPMessageHeaderLength;
        if (!isLittleEndian()) {
            byteBuffer.put(8,  (byte)((patch >>> 24) & 0xFF));
            byteBuffer.put(9,  (byte)((patch >>> 16) & 0xFF));
            byteBuffer.put(10, (byte)((patch >>> 8)  & 0xFF));
            byteBuffer.put(11, (byte)((patch)  & 0xFF));
        } else {
            byteBuffer.put(8,  (byte)((patch)  & 0xFF));
            byteBuffer.put(9,  (byte)((patch >>> 8)  & 0xFF));
            byteBuffer.put(10, (byte)((patch >>> 16) & 0xFF));
            byteBuffer.put(11, (byte)((patch >>> 24) & 0xFF));
        }
    }

    public FragmentMessage createFragmentMessage() {
	throw wrapper.fragmentationDisallowed() ;
    }
        
    // IO methods

    // This should do nothing even if it is called. The Message Header already
    // is read off java.io.InputStream (not a CDRInputStream) by IIOPConnection
    // in order to choose the correct CDR Version, msg_type, and msg_size.
    // So, we would never need to read the Message Header off a CDRInputStream.
    public void read(org.omg.CORBA.portable.InputStream istream) {
        /*
        this.magic = istream.read_long();
        this.GIOP_version = (new GIOPVersion()).read(istream);
        this.byte_order = istream.read_boolean();
        this.message_type = istream.read_octet();
        this.message_size = istream.read_ulong();
        */
    }

    public void write(org.omg.CORBA.portable.OutputStream ostream) {
        ostream.write_long(this.magic);
        nullCheck(this.GIOP_version);
        this.GIOP_version.write(ostream);
        ostream.write_boolean(this.byte_order);
        ostream.write_octet(this.message_type);
        ostream.write_ulong(this.message_size);
    }

} // class Message_1_0
