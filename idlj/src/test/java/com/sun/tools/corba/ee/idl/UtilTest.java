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

package com.sun.tools.corba.ee.idl;

import com.meterware.simplestub.Memento;
import com.meterware.simplestub.StaticStubSupport;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.util.HashSet;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.equalTo;

public class UtilTest {
    private HashSet<Memento> mementos = new HashSet<>();

    @Before
    public void setUp() throws Exception {
        mementos.add(StaticStubSupport.install(Util.class, "messages", null));
        mementos.add(StaticStubSupport.preserve(Util.class, "msgResources"));
    }

    @After
    public void tearDown() throws Exception {
        for (Memento memento : mementos) memento.revert();
    }

    @Test
    public void readVersionFromDefaultPropertyFile() throws Exception {
        assertThat(Util.getVersion(), containsString("4.02"));
    }

    @Test
    public void readVersionFromUnnamedPropertyFile() throws Exception {
        assertThat(Util.getVersion(""), containsString("4.02"));
    }

    @Test
    public void retrieveMessage() throws Exception {
        assertThat(Util.getMessage("EvaluationException.not"), equalTo("bitwise not"));
    }

    @Test
    public void substituteSingleParamMessage() throws Exception {
        assertThat(Util.getMessage("InvalidArgument.1", "zork"), equalTo("Invalid argument:  zork."));
    }

    @Test
    public void substituteMultiParamMessage() throws Exception {
        assertThat(Util.getMessage("GenFileStream.1", new String[] {"zork", "foo"}), equalTo("zork could not be generated:  foo"));
    }

    @Test
    public void retrieveMessagesFromMultipleFiles() throws Exception {
        Util.registerMessageResource("com/sun/tools/corba/ee/idl/idl");
        Util.registerMessageResource("com/sun/tools/corba/ee/idl/toJavaPortable/toJavaPortable");

        assertThat(Util.getMessage("EvaluationException.not"), equalTo("bitwise not"));
        assertThat(Util.getMessage("NameModifier.TooManyPercent"), equalTo("Pattern contains more than one percent characters"));
    }

    @Test
    public void afterRetrievingMessagesFromOneFile_canRegisterAndRetrieveFromAnother() throws Exception {
        Util.registerMessageResource("com/sun/tools/corba/ee/idl/idl");
        Util.getMessage("EvaluationException.not");
        Util.registerMessageResource("com/sun/tools/corba/ee/idl/toJavaPortable/toJavaPortable");

        assertThat(Util.getMessage("NameModifier.TooManyPercent"), equalTo("Pattern contains more than one percent characters"));
    }
}
