/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright (c) 2018 Oracle and/or its affiliates. All rights reserved.
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

package com.sun.corba.ee.impl.folb;

import com.sun.corba.ee.spi.ior.IOR;
import com.sun.corba.ee.spi.protocol.ClientDelegate;
import com.sun.corba.ee.spi.transport.ContactInfoList;
import org.omg.CORBA.portable.ObjectImpl;

import static com.meterware.simplestub.Stub.createStrictStub;

public class StubObject extends ObjectImpl {
    static org.omg.CORBA.Object createObjectWithIOR(IOR ior) {
        StubObject result = new StubObject();
        result._set_delegate(createDelegateWithIOR(ior));
        return result;
    }

    private static TestClientDelegate createDelegateWithIOR(IOR ior) {
        TestClientDelegate delegate = createStrictStub(TestClientDelegate.class);
        delegate.setContactInfoList(createInfoListWithIOR(ior));
        return delegate;
    }

    private static TestContactInfoList createInfoListWithIOR(IOR ior) {
        TestContactInfoList infoList = createStrictStub(TestContactInfoList.class);
        infoList.setTargetIOR(ior);
        return infoList;
    }

    public String[] _ids() {
        return new String[0];
    }

    abstract static class TestClientDelegate extends ClientDelegate {
        private ContactInfoList contactInfoList;

        public void setContactInfoList(ContactInfoList contactInfoList) {
            this.contactInfoList = contactInfoList;
        }

        public ContactInfoList getContactInfoList() {
            return contactInfoList;
        }
    }

    abstract static class TestContactInfoList implements ContactInfoList {
        private IOR ior;

        public void setTargetIOR(IOR ior) {
            this.ior = ior;
        }

        public IOR getTargetIOR() {
            return ior;
        }
    }
}
