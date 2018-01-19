package org.glassfish.rmic.asm;
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

import org.glassfish.rmic.Names;
import org.glassfish.rmic.tools.java.ClassDeclaration;
import org.glassfish.rmic.tools.java.ClassDefinition;
import org.glassfish.rmic.tools.java.ClassDefinitionFactory;
import org.glassfish.rmic.tools.java.Environment;
import org.glassfish.rmic.tools.java.Identifier;
import org.glassfish.rmic.tools.java.MemberDefinition;
import org.glassfish.rmic.tools.java.Type;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.FieldVisitor;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;

import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;

public class AsmClassFactory implements ClassDefinitionFactory {
    // This field exists to allow unit testing of the case when ASM is not in the classpath.
    @SuppressWarnings("unused")
    private static final Boolean simulateMissingASM = false;

    private Map<Identifier, Identifier> outerClasses = new HashMap<>();

    public AsmClassFactory() {
        if (simulateMissingASM) throw new NoClassDefFoundError();
    }

    Identifier getOuterClassName(Identifier className) {
        if (isResolvedInnerClassName(className))
            className = Names.mangleClass(className);
        return outerClasses.get(className);
    }

    // This is needed to compensate for the hack described in Main.getClassIdentifier()
    private boolean isResolvedInnerClassName(Identifier className) {
        return className.toString().contains(". ");
    }

    @Override
    public ClassDefinition loadDefinition(InputStream is, Environment env) throws IOException {
        ClassDefinitionVisitor visitor = new ClassDefinitionVisitor(env);
        ClassReader classReader = new ClassReader(is);
        classReader.accept(visitor, ClassReader.SKIP_CODE | ClassReader.SKIP_DEBUG);
        return visitor.getDefinition();
    }

    class ClassDefinitionVisitor extends ClassVisitor {
        private Environment env;
        private AsmClass asmClass;

        ClassDefinitionVisitor(Environment env) {
            super(Opcodes.ASM6);
            this.env = env;
        }

        ClassDefinition getDefinition() {
            return asmClass;
        }

        @Override
        public void visit(int version, int access, String name, String signature, String superName, String[] interfaces) {
            asmClass = new AsmClass(AsmClassFactory.this, toSourceFileName(name), access, toClassDeclaration(name),
                                    toClassDeclaration(superName), toClassDeclarations(interfaces));
        }

        private String toSourceFileName(String name) {
            String className = toClassName(name);
            if (className.contains("$"))
                className = className.substring(0, className.indexOf("$"));
            return className + ".java";
        }

        private String toClassName(String name) {
            return name.substring(name.lastIndexOf('/') + 1);
        }

        private ClassDeclaration[] toClassDeclarations(String... names) {
            ClassDeclaration[] result = new ClassDeclaration[names.length];
            for (int i = 0; i < names.length; i++)
                result[i] = new ClassDeclaration(getIdentifier(names[i]));
            return result;
        }

        private ClassDeclaration toClassDeclaration(String name) {
            return name == null ? null : new ClassDeclaration(getIdentifier(name));
        }

        private Identifier getIdentifier(String name) {
            return Identifier.lookup(name.replace('/', '.'));
        }

        @Override
        public void visitInnerClass(String name, String outerName, String innerName, int access) {
            if (outerName != null)
                outerClasses.put(getIdentifier(name), getIdentifier(outerName));
        }

        @Override
        public FieldVisitor visitField(int access, String name, String desc, String signature, Object value) {
            MemberDefinition definition = new MemberDefinition(0, asmClass, access, TypeFactory.createType(desc), getIdentifier(name), null, null);
            asmClass.addMember(env, definition);
            return null;
        }

        @Override
        public MethodVisitor visitMethod(int access, String name, String desc, String signature, String[] exceptions) {
            MemberDefinition definition = new AsmMemberDefinition(0, asmClass, access, createType(desc), getIdentifier(name), exceptions);
            asmClass.addMember(env, definition);
            return null;
        }

        private Type createType(String desc) {
            return TypeFactory.createMethodType(desc);
        }
    }
}
