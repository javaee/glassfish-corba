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

package com.sun.corba.ee.impl.ior.iiop;

import com.sun.corba.ee.impl.orb.ORBVersionImpl;
import com.sun.corba.ee.spi.ior.ObjectId;
import com.sun.corba.ee.spi.ior.ObjectKeyTemplate;
import com.sun.corba.ee.spi.ior.iiop.IIOPProfileTemplate;
import com.sun.corba.ee.spi.orb.ORB;
import com.sun.corba.ee.spi.orb.ORBVersion;
import org.junit.Test;

import static com.meterware.simplestub.Stub.createStrictStub;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;

public class IIOPProfileImplTest {
    private ORB orb = createStrictStub(ORB.class);
    private ObjectId oid = createStrictStub(ObjectId.class);
    private IIOPProfileTemplate profileTemplate = createStrictStub(IIOPProfileTemplate.class);
    private ObjectKeyTemplateStub objectKeyTemplate = createStrictStub(ObjectKeyTemplateStub.class);

    private IIOPProfileImpl iiopProfile = new IIOPProfileImpl(orb, objectKeyTemplate, oid, profileTemplate);

    @Test
    public void whenForeignProfileOrbVersion_isLocalReturnsFalse() throws Exception {
        setOrbVersion(ORBVersionImpl.FOREIGN);

        assertThat(iiopProfile.isLocal(), is(false));
    }

    private void setOrbVersion(ORBVersion orbVersion) {
        objectKeyTemplate.setOrbVersion(orbVersion);
    }

    abstract static class ObjectKeyTemplateStub implements ObjectKeyTemplate {

        private ORBVersion orbVersion = ORBVersionImpl.NEW;

        public void setOrbVersion(ORBVersion orbVersion) {
            this.orbVersion = orbVersion;
        }

        @Override
        public ORBVersion getORBVersion() {
            return orbVersion;
        }
    }

}
