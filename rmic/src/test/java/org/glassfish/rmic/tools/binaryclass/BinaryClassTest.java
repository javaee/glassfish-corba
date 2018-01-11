package org.glassfish.rmic.tools.binaryclass;
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

import com.sun.deploy.util.StringUtils;
import org.glassfish.rmic.BatchEnvironment;
import org.glassfish.rmic.TestUtils;
import org.glassfish.rmic.classes.covariantReturn.AnimalFinder;
import org.glassfish.rmic.classes.hcks.RmiII;
import org.glassfish.rmic.classes.hcks.RmiIIServantPOA;
import org.glassfish.rmic.classes.inneraccess.Rainbow;
import org.glassfish.rmic.classes.nestedClasses.TwoLevelNested;
import org.glassfish.rmic.tools.java.ClassDefinition;
import org.glassfish.rmic.tools.java.ClassDefinitionFactory;
import org.glassfish.rmic.tools.java.ClassPath;
import org.glassfish.rmic.tools.java.Environment;
import org.glassfish.rmic.tools.java.MemberDefinition;
import org.glassfish.rmic.tools.java.Type;
import org.hamcrest.Description;
import org.hamcrest.Matcher;
import org.hamcrest.TypeSafeDiagnosingMatcher;
import org.junit.Test;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.AccessibleObject;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;

import static org.glassfish.rmic.tools.binaryclass.BinaryClassTest.MemberDefinitionMatcher.isDefinitionFor;
import static org.glassfish.rmic.tools.java.Constants.*;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;

public class BinaryClassTest {

    private ByteArrayOutputStream messagesOut = new ByteArrayOutputStream();
    private Environment environment = new BatchEnvironment(messagesOut, createTestClassPath(), null);
    private ClassDefinitionFactory factory = new BinaryClassFactory();

    private ClassPath createTestClassPath() {
        return BatchEnvironment.createClassPath(TestUtils.getClassPathString(), null);
    }

    @Test
    public void classDefinitionForCompiledClass_hasSourceName() throws Exception {
        ClassDefinition classDefinition = definitionFor(RmiII.class);

        assertThat(classDefinition.getSource(), equalTo("RmiII.java"));
    }

    private ClassDefinition definitionFor(Class<?> aClass) throws IOException {
        InputStream classFileInputStream = getClass().getClassLoader().getResourceAsStream(toPath(aClass));
        return factory.loadDefinition(classFileInputStream, environment);
    }

    private String toPath(Class<?> aClass) {
        return aClass.getName().replace('.', File.separatorChar) + ".class";
    }

    @Test
    public void classDefinitionForCompiledClass_hasNoError() throws Exception {
        ClassDefinition classDefinition = definitionFor(RmiII.class);

        assertThat(classDefinition.getError(), is(false));
    }

    @Test
    public void classDefinitionForCompiledClass_hasZeroWhereValue() throws Exception {
        ClassDefinition classDefinition = definitionFor(RmiII.class);

        assertThat(classDefinition.getWhere(), equalTo(0L));
    }

    @Test
    public void classDefinitionForRmiII_hasExpectedModifiers() throws Exception {
        ClassDefinition classDefinition = definitionFor(RmiII.class);

        assertThat(classDefinition.getModifiers(), equalTo(M_ABSTRACT | M_INTERFACE | M_PUBLIC));
    }

    @Test
    public void classDefinitionForRmiIIServantPOA_hasExpectedModifiers() throws Exception {
        ClassDefinition classDefinition = definitionFor(RmiIIServantPOA.class);

        assertThat(classDefinition.getModifiers(), equalTo(ACC_SUPER | M_PUBLIC));
    }

    @Test
    public void classDefinitionForRmiIIServantPOA_hasExpectedMemberDefinitions() throws Exception {
        ClassDefinition classDefinition = definitionFor(RmiIIServantPOA.class);

        List<MemberDefinition> memberDefinitions = new ArrayList<>();
        for (MemberDefinition member = classDefinition.getFirstMember(); member != null; member = member.getNextMember())
            if (!isStaticInitializer(member)) memberDefinitions.add(member);

        assertThat(memberDefinitions, containsInAnyOrder(allMembers(RmiIIServantPOA.class)));
    }

    private boolean isStaticInitializer(MemberDefinition member) {
        return member.getName().toString().equals("<clinit>");
    }

    @Test
    public void classDefinitionForAnimalFinder_hasExpectedMemberDefinitions() throws Exception {
        ClassDefinition classDefinition = definitionFor(AnimalFinder.class);

        List<MemberDefinition> memberDefinitions = new ArrayList<>();
        for (MemberDefinition member = classDefinition.getFirstMember(); member != null; member = member.getNextMember())
            if (!isStaticInitializer(member)) memberDefinitions.add(member);

        assertThat(memberDefinitions, containsInAnyOrder(allMembers(AnimalFinder.class)));
    }

    @SuppressWarnings("unchecked")
    private <T> Matcher<MemberDefinition>[] allMembers(Class<?> aClass) {
        List<Matcher<MemberDefinition>> matchers = new ArrayList<>();
        for (Method method : aClass.getDeclaredMethods())
            matchers.add(isDefinitionFor(method));
        for (Field field : aClass.getDeclaredFields())
            matchers.add(isDefinitionFor(field));
        for (Constructor constructor : aClass.getDeclaredConstructors())
            matchers.add(isDefinitionFor(constructor));
        return matchers.toArray(new Matcher[matchers.size()]);
    }


    @Test
    public void afterLoadNested_topClassOfInnerClassMatchesOuterClass() throws Exception {
        ClassDefinition inner = definitionFor(Rainbow.getInterfaceCheckerClass());
        inner.loadNested(environment);

        assertThat(inner.getTopClass().getName(), equalTo(definitionFor(Rainbow.class).getName()));
    }

    @Test
    public void afterLoadNested_topClassOfInner2LevelNestedClassMatchesOuterClass() throws Exception {
        ClassDefinition inner = definitionFor(TwoLevelNested.Level1.Level2.class);
        inner.loadNested(environment);

        assertThat(inner.getTopClass().getName(), equalTo(definitionFor(TwoLevelNested.class).getName()));
    }

    static class MemberDefinitionMatcher extends TypeSafeDiagnosingMatcher<MemberDefinition> {
        private AccessibleObject member;

        private MemberDefinitionMatcher(AccessibleObject member) {
            this.member = member;
        }

        static Matcher<MemberDefinition> isDefinitionFor(AccessibleObject method) {
            return new MemberDefinitionMatcher(method);
        }

        @Override
        protected boolean matchesSafely(MemberDefinition memberDefinition, Description description) {
            if (!matches(memberDefinition)) {
                description.appendValue(memberDefinition.getName());
                return false;
            }
            return true;
        }

        private boolean matches(MemberDefinition memberDefinition) {
            if (member instanceof Constructor)
                return parameterTypesMatch(((Constructor) member).getParameterTypes(), memberDefinition.getType().getArgumentTypes());
            else if (member instanceof Method)
                return isSameMethod(memberDefinition, (Method) this.member);
            else
                return getName(member).equals(memberDefinition.getName().toString());
        }

        private boolean isSameMethod(MemberDefinition memberDefinition, Method method) {
            return method.getName().equals(memberDefinition.getName().toString()) &&
                    parameterTypeMatch(method.getReturnType(), memberDefinition.getType().getReturnType()) &&
                    parameterTypesMatch(method.getParameterTypes(), memberDefinition.getType().getArgumentTypes());
        }

        private boolean parameterTypesMatch(Class<?>[] parameterTypes, Type[] memberDefinition) {
            if (parameterTypes.length != memberDefinition.length) return false;

            for (int i = 0; i < parameterTypes.length; i++)
                if (!parameterTypeMatch(parameterTypes[i], memberDefinition[i])) return false;
            return true;
        }

        private boolean parameterTypeMatch(Class<?> parameterType, Type type) {
            return parameterType.getTypeName().equals(type.toString());
        }

        private String getName(AccessibleObject member) {
            if (member instanceof Method)
                return getMethodName((Method) member);
            else if (member instanceof Field)
                return ((Field) member).getName();
            else if (member instanceof Constructor)
                return getConstructorName((Constructor) member);
            else
                return "??";
        }

        private String getMethodName(Method member) {
            return toDisplayType(member.getReturnType()) + " " + member.getName() + toParameterString(member.getParameterTypes());
        }

        private String getConstructorName(Constructor member) {
            return toDisplayType(member.getDeclaringClass()) + toParameterString(member.getParameterTypes());
        }

        private String toParameterString(Class[] parameterTypes) {
            return "(" + StringUtils.join(toStringList(parameterTypes), ", ") + ")";
        }

        private List<String> toStringList(Class<?>[] parameterTypes) {
            List<String> list = new ArrayList<>();
            for (Class<?> parameterType : parameterTypes)
                list.add(toDisplayType(parameterType));
            return list;
        }

        private String toDisplayType(Class<?> parameterType) {
            return parameterType.getTypeName();
        }

        @Override
        public void describeTo(Description description) {
            description.appendValue(getName(member));
        }
    }
}