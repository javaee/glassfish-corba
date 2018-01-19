package org.glassfish.rmic.tools.javac;
/*
 * Copyright (c) 2018, Oracle and/or its affiliates. All rights reserved.
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS FILE HEADER.
 *
 * This code is free software; you can redistribute it and/or modify it
 * under the terms of the GNU General Public License version 2 only, as
 * published by the Free Software Foundation.  Oracle designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Oracle in the LICENSE file that accompanied this code.
 *
 * This code is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
 * FITNESS FOR A PARTICULAR PURPOSE.  See the GNU General Public License
 * version 2 for more details (a copy is included in the LICENSE file that
 * accompanied this code).
 *
 * You should have received a copy of the GNU General Public License version
 * 2 along with this work; if not, write to the Free Software Foundation,
 * Inc., 51 Franklin St, Fifth Floor, Boston, MA 02110-1301 USA.
 *
 * Please contact Oracle, 500 Oracle Parkway, Redwood Shores, CA 94065 USA
 * or visit www.oracle.com if you need additional information or have any
 * questions.
 */
import com.meterware.simplestub.Memento;
import com.meterware.simplestub.StaticStubSupport;
import com.meterware.simplestub.SystemPropertySupport;
import org.glassfish.rmic.BatchEnvironmentError;
import org.glassfish.rmic.asm.AsmClassFactory;
import org.glassfish.rmic.tools.binaryclass.BinaryClassFactory;
import org.glassfish.rmic.tools.java.ClassDefinitionFactory;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.instanceOf;

@SuppressWarnings("deprecation")
public class BatchEnvironmentTest {
    private static final String USE_LEGACY_PARSING_PROPERTY = "org.glassfish.rmic.UseLegacyClassParsing";
    private static final String JAVA_VERSION_PROPERTY = "java.version";
    private List<Memento> mementos = new ArrayList<>();

    @Before
    public void setUp() throws Exception {
        mementos.add(SystemPropertySupport.preserve(USE_LEGACY_PARSING_PROPERTY));
        mementos.add(SystemPropertySupport.preserve(JAVA_VERSION_PROPERTY));
        System.clearProperty(USE_LEGACY_PARSING_PROPERTY);
    }

    @After
    public void tearDown() throws Exception {
        for (Memento memento : mementos) memento.revert();
    }

    @Test
    public void whenPropertyNotSet_chooseAsmParser() throws Exception {
        ClassDefinitionFactory factory = BatchEnvironment.createClassDefinitionFactory();

        assertThat(factory, instanceOf(AsmClassFactory.class));
    }

    @Test
    public void whenAsmClassesMissingOnJdk8_chooseBinaryParser() throws Exception {
        simulateAsmClassesMissing();
        simulateJdkVersion("1.8");

        ClassDefinitionFactory factory = BatchEnvironment.createClassDefinitionFactory();

        assertThat(factory, instanceOf(BinaryClassFactory.class));
    }

    private void simulateAsmClassesMissing() throws NoSuchFieldException {
        mementos.add(StaticStubSupport.install(AsmClassFactory.class, "simulateMissingASM", true));
    }

    private void simulateJdkVersion(String jdkVersion) {
        System.setProperty(JAVA_VERSION_PROPERTY, jdkVersion);
    }

    @Test(expected = BatchEnvironmentError.class)
    public void whenAsmClassesMissingOnJdk10_reportError() throws Exception {
        simulateAsmClassesMissing();
        simulateJdkVersion("10");

        BatchEnvironment.createClassDefinitionFactory();
    }

    @Test
    public void whenLegacyParserRequestedOnJdk8_chooseBinaryParser() throws Exception {
        preferLegacyParser();
        simulateJdkVersion("1.8");

        ClassDefinitionFactory factory = BatchEnvironment.createClassDefinitionFactory();

        assertThat(factory, instanceOf(BinaryClassFactory.class));
    }

    private void preferLegacyParser() {
        System.setProperty(USE_LEGACY_PARSING_PROPERTY, "true");
    }

    @Test
    public void whenLegacyParserRequestedOnJdk9_chooseBinaryParser() throws Exception {
        preferLegacyParser();
        simulateJdkVersion("9");

        ClassDefinitionFactory factory = BatchEnvironment.createClassDefinitionFactory();

        assertThat(factory, instanceOf(BinaryClassFactory.class));
    }

    @Test
    public void whenLegacyParserRequestedOnJdk10_chooseAsmParser() throws Exception {
        preferLegacyParser();
        simulateJdkVersion("10");

        ClassDefinitionFactory factory = BatchEnvironment.createClassDefinitionFactory();

        assertThat(factory, instanceOf(AsmClassFactory.class));
    }

    @Test
    public void whenLegacyParserRequestedOnJdk10EarlyAccess_chooseAsmParser() throws Exception {
        preferLegacyParser();
        simulateJdkVersion("10-ea");

        ClassDefinitionFactory factory = BatchEnvironment.createClassDefinitionFactory();

        assertThat(factory, instanceOf(AsmClassFactory.class));
    }

    @Test
    public void whenLegacyParserRequestedOnJdk11_chooseAsmParser() throws Exception {
        preferLegacyParser();
        simulateJdkVersion("11");

        ClassDefinitionFactory factory = BatchEnvironment.createClassDefinitionFactory();

        assertThat(factory, instanceOf(AsmClassFactory.class));
    }

}