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
import com.sun.corba.ee.spi.ior.TaggedComponent;
import com.sun.corba.ee.spi.ior.TaggedProfileTemplate;
import com.sun.corba.ee.spi.ior.iiop.IIOPProfile;
import com.sun.corba.ee.spi.ior.iiop.IIOPProfileTemplate;
import org.omg.CORBA.ORB;
import org.omg.CORBA_2_3.portable.OutputStream;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import static com.meterware.simplestub.Stub.createStrictStub;

abstract class TestIOR implements IOR {

    static TestIOR createIORWithTaggedComponents(int id, org.omg.IOP.TaggedComponent... components) {
        TestIOR ior = createStrictStub(TestIOR.class);
        ior.setProfile(createIIOPProfileWithTaggedComponents(id, components));
        return ior;
    }

    private IIOPProfile profile;

    public IIOPProfile getProfile() {
        return profile;
    }

    public void setProfile(IIOPProfile profile) {
        this.profile = profile;
    }

    static TestIIOPProfile createIIOPProfileWithTaggedComponents(int id, org.omg.IOP.TaggedComponent... components) {
        TestIIOPProfile profile = createStrictStub(TestIIOPProfile.class);
        profile.setProfileTemplate(createIIOPProfileTemplateWithTaggedComponents(id, components));
        return profile;
    }

    abstract static class TestIIOPProfile implements IIOPProfile {

        private TaggedProfileTemplate profileTemplate;

        public TaggedProfileTemplate getTaggedProfileTemplate() {
            return profileTemplate;
        }

        public void setProfileTemplate(TaggedProfileTemplate profileTemplate) {
            this.profileTemplate = profileTemplate;
        }

    }

    static TestIIOPProfileTemplate createIIOPProfileTemplateWithTaggedComponents(int id, org.omg.IOP.TaggedComponent... components) {
        TestIIOPProfileTemplate template = createStrictStub(TestIIOPProfileTemplate.class);
        for (org.omg.IOP.TaggedComponent component : components)
            template.addTaggedComponent(id, component);
        return template;
    }

    abstract static class TestIIOPProfileTemplate implements IIOPProfileTemplate {
        private Map<Integer, List<TaggedComponent>> taggedComponents = new HashMap<Integer, List<TaggedComponent>>();

        public void addTaggedComponent(int id, org.omg.IOP.TaggedComponent component) {
            getTaggedComponentList(id).add(new TestTaggedComponent(id,component));
        }

        public Iterator<TaggedComponent> iteratorById(int id) {
            return getTaggedComponentList(id).iterator();
        }

        private List<TaggedComponent> getTaggedComponentList(int id) {
            List<TaggedComponent> componentList = taggedComponents.get(id);
            if (componentList == null) {
                componentList = new ArrayList<TaggedComponent>();
                taggedComponents.put(id, componentList);
            }
            return componentList;
        }
    }

    static class TestTaggedComponent implements TaggedComponent {

        private int id;
        private org.omg.IOP.TaggedComponent iopComponent;

        TestTaggedComponent(int id, org.omg.IOP.TaggedComponent iopComponent) {
            this.id = id;
            this.iopComponent = iopComponent;
        }

        public org.omg.IOP.TaggedComponent getIOPComponent(ORB orb) {
            return iopComponent;
        }

        public int getId() {
            return id;
        }

        public void write(OutputStream outputStream) {
        }
    }
}


