/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright (c) 1997-2018 Oracle and/or its affiliates. All rights reserved.
 *
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common Development
 * and Distribution License("CDDL") (collectively, the "License").  You
 * may not use this file except in compliance with the License.  You can
 * obtain a copy of the License at
 * https://oss.oracle.com/licenses/CDDL+GPL-1.1
 * or LICENSE.txt.  See the License for the specific
 * language governing permissions and limitations under the License.
 *
 * When distributing the software, include this License Header Notice in each
 * file and include the License file at LICENSE.txt.
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

package com.sun.corba.ee.impl.protocol.giopmsgheaders;

import org.omg.CORBA.CompletionStatus;
import org.omg.CORBA.SystemException;
import org.omg.CORBA_2_3.portable.InputStream;

import com.sun.corba.ee.spi.ior.IOR;
import com.sun.corba.ee.spi.ior.IORFactories;

import com.sun.corba.ee.spi.orb.ORB;

import com.sun.corba.ee.spi.ior.iiop.GIOPVersion;
import com.sun.corba.ee.impl.encoding.CDRInputObject;

import com.sun.corba.ee.spi.logging.ORBUtilSystemException ;

/**
 * This implements the GIOP 1.1 LocateReply header.
 *
 * @author Ram Jeyaraman 05/14/2000
 * @version 1.0
 */

public final class LocateReplyMessage_1_1 extends Message_1_1
        implements LocateReplyMessage {

    private static final ORBUtilSystemException wrapper =
        ORBUtilSystemException.self ;

    // Instance variables

    private ORB orb = null;
    private int request_id = 0;
    private int reply_status = 0;
    private IOR ior = null;

    // Constructors

    LocateReplyMessage_1_1(ORB orb) {
        this.orb = orb;
    }

    LocateReplyMessage_1_1(ORB orb, int _request_id,
            int _reply_status, IOR _ior) {
        super(Message.GIOPBigMagic, GIOPVersion.V1_1, FLAG_NO_FRAG_BIG_ENDIAN,
            Message.GIOPLocateReply, 0);
        this.orb = orb;
        request_id = _request_id;
        reply_status = _reply_status;
        ior = _ior;
    }

    // Accessor methods

    public int getRequestId() {
        return this.request_id;
    }

    public int getReplyStatus() {
        return this.reply_status;
    }

    public short getAddrDisposition() {
        return KeyAddr.value;
    }
        
    public SystemException getSystemException(String message) {
        return null; // 1.0 LocateReply body does not contain SystemException
    }

    public IOR getIOR() {
        return this.ior;
    }

    // IO methods

    @Override
    public void read(org.omg.CORBA.portable.InputStream istream) {
        super.read(istream);
        this.request_id = istream.read_ulong();
        this.reply_status = istream.read_long();
        isValidReplyStatus(this.reply_status); // raises exception on error

        // The code below reads the reply body if status is OBJECT_FORWARD
        if (this.reply_status == OBJECT_FORWARD) {
            CDRInputObject cdr = (CDRInputObject) istream;
            this.ior = IORFactories.makeIOR( orb, (InputStream)cdr ) ;
        }
    }

    // Note, this writes only the header information. SystemException or
    // IOR may be written afterwards into the reply mesg body.
    @Override
    public void write(org.omg.CORBA.portable.OutputStream ostream) {
        super.write(ostream);
        ostream.write_ulong(this.request_id);
        ostream.write_long(this.reply_status);
    }

    // Static methods

    public static void isValidReplyStatus(int replyStatus) {
        switch (replyStatus) {
        case UNKNOWN_OBJECT :
        case OBJECT_HERE :
        case OBJECT_FORWARD :
            break;
        default :
            throw wrapper.illegalReplyStatus();
        }
    }

    public void callback(MessageHandler handler)
        throws java.io.IOException
    {
        handler.handleInput(this);
    }
} // class LocateReplyMessage_1_1
