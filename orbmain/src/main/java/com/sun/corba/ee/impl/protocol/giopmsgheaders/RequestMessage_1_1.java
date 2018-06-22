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

import com.sun.corba.ee.spi.servicecontext.ServiceContexts;
import com.sun.corba.ee.spi.servicecontext.ServiceContextDefaults;
import com.sun.corba.ee.spi.ior.iiop.GIOPVersion;
import com.sun.corba.ee.spi.orb.ORB;
import com.sun.corba.ee.spi.orb.ObjectKeyCacheEntry;

import com.sun.corba.ee.spi.logging.ORBUtilSystemException;

/**
 * This implements the GIOP 1.1 Request header.
 *
 * @author Ram Jeyaraman 05/14/2000
 * @version 1.0
 */

public final class RequestMessage_1_1 extends Message_1_1
        implements RequestMessage {

    private static final ORBUtilSystemException wrapper =
            ORBUtilSystemException.self;

    // Instance variables

    private ORB orb = null;
    private ServiceContexts service_contexts = null;
    private int request_id = 0;
    private boolean response_expected = false;
    private byte[] reserved = null; // Added in GIOP 1.1
    private byte[] object_key = null;
    private String operation = null;
    @SuppressWarnings({"deprecation"})
    private org.omg.CORBA.Principal requesting_principal = null;
    private ObjectKeyCacheEntry entry = null;

    // Constructors

    RequestMessage_1_1(ORB orb) {
        this.orb = orb;
        this.service_contexts = ServiceContextDefaults.makeServiceContexts(orb);
    }

    @SuppressWarnings({"deprecation"})
    RequestMessage_1_1(ORB orb, ServiceContexts _service_contexts,
                       int _request_id, boolean _response_expected, byte[] _reserved,
                       byte[] _object_key, String _operation,
                       org.omg.CORBA.Principal _requesting_principal) {
        super(Message.GIOPBigMagic, GIOPVersion.V1_1, FLAG_NO_FRAG_BIG_ENDIAN,
                Message.GIOPRequest, 0);
        this.orb = orb;
        service_contexts = _service_contexts;
        request_id = _request_id;
        response_expected = _response_expected;
        reserved = _reserved;
        object_key = _object_key;
        operation = _operation;
        requesting_principal = _requesting_principal;
    }

    // Accessor methods (RequestMessage interface)

    public ServiceContexts getServiceContexts() {
        return this.service_contexts;
    }

    public void setServiceContexts(ServiceContexts sc) {
        this.service_contexts = sc;
    }

    public int getRequestId() {
        return this.request_id;
    }

    public boolean isResponseExpected() {
        return this.response_expected;
    }

    public byte[] getReserved() {
        return this.reserved;
    }

    public ObjectKeyCacheEntry getObjectKeyCacheEntry() {
        if (this.entry == null) {
            // this will raise a MARSHAL exception upon errors.
            this.entry = orb.extractObjectKeyCacheEntry(object_key);
        }

        return this.entry;
    }

    public String getOperation() {
        return this.operation;
    }

    @SuppressWarnings({"deprecation"})
    public org.omg.CORBA.Principal getPrincipal() {
        return this.requesting_principal;
    }

    // IO methods

    public void read(org.omg.CORBA.portable.InputStream istream) {
        super.read(istream);
        this.service_contexts
                = ServiceContextDefaults.makeServiceContexts(
                (org.omg.CORBA_2_3.portable.InputStream) istream);
        this.request_id = istream.read_ulong();
        this.response_expected = istream.read_boolean();
        this.reserved = new byte[3];
        for (int _o0 = 0; _o0 < (3); ++_o0) {
            this.reserved[_o0] = istream.read_octet();
        }
        int _len1 = istream.read_long();
        this.object_key = new byte[_len1];
        istream.read_octet_array(this.object_key, 0, _len1);
        this.operation = istream.read_string();
        this.requesting_principal = istream.read_Principal();
    }

    public void write(org.omg.CORBA.portable.OutputStream ostream) {
        super.write(ostream);
        service_contexts.write(
                (org.omg.CORBA_2_3.portable.OutputStream) ostream,
                GIOPVersion.V1_1);
        ostream.write_ulong(this.request_id);
        ostream.write_boolean(this.response_expected);
        nullCheck(this.reserved);
        if (this.reserved.length != (3)) {
            throw wrapper.badReservedLength();
        }
        for (int _i0 = 0; _i0 < (3); ++_i0) {
            ostream.write_octet(this.reserved[_i0]);
        }
        nullCheck(this.object_key);
        ostream.write_long(this.object_key.length);
        ostream.write_octet_array(this.object_key, 0, this.object_key.length);
        ostream.write_string(this.operation);
        if (this.requesting_principal != null) {
            ostream.write_Principal(this.requesting_principal);
        } else {
            ostream.write_long(0);
        }
    }

    public void callback(MessageHandler handler)
            throws java.io.IOException {
        handler.handleInput(this);
    }

    @Override
    public boolean supportsFragments() {
        return true;
    }
} // class RequestMessage_1_1
