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

/**
 * This implements the GIOP 1.1 Fragment header.
 *
 * @author Ram Jeyaraman 05/14/2000
 * @version 1.0
 */

public final class FragmentMessage_1_1 extends Message_1_1
        implements FragmentMessage {

    // Constructors

    FragmentMessage_1_1() {
    }

    FragmentMessage_1_1(Message_1_1 msg11) {
        this.magic = msg11.magic;
        this.GIOP_version = msg11.GIOP_version;
        this.flags = msg11.flags;
        this.message_type = GIOPFragment;
        this.message_size = 0;
    }

    // Accessor methods

    public int getRequestId() {
        return -1; // 1.1 has no fragment header and so no request_id
    }

    public int getHeaderLength() {
        return GIOPMessageHeaderLength;
    }

    // IO methods

    /* This will never be called, since we do not currently read the
     * request_id from an CDRInputStream. Instead we use the
     * readGIOP_1_1_requestId to read the requestId from a byte buffer.
     */
    public void read(org.omg.CORBA.portable.InputStream istream) {
        super.read(istream);
    }

    /* 1.1 has no request_id; so nothing to write */
    public void write(org.omg.CORBA.portable.OutputStream ostream) {
        super.write(ostream);
    }

    public void callback(MessageHandler handler)
            throws java.io.IOException {
        handler.handleInput(this);
    }

    @Override
    public boolean supportsFragments() {
        return true;
    }
} // class FragmentMessage_1_1
