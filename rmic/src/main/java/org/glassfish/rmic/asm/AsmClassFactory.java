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

package org.glassfish.rmic.asm;

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
import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.Map;

/**
 * A factory for a class definition produced via ASM.
 */
public class AsmClassFactory implements ClassDefinitionFactory {
    // This field exists to allow unit testing of the case when ASM is not in the classpath.
    @SuppressWarnings("unused")
    private static final Boolean simulateMissingASM = false;

    private Map<Identifier, Identifier> outerClasses = new HashMap<>();

    public AsmClassFactory() {
        if (simulateMissingASM) throw new NoClassDefFoundError();
    }

    /**
     * Returns the latest API supported by the active version of ASM.
     * @return an integer value
     */
    static int getLatestVersion() {
        try {
            int latest = 0;
            for (Field field : Opcodes.class.getDeclaredFields()) {
                if (field.getName().startsWith("ASM") && field.getType().equals(int.class)) {
                    latest = Math.max(latest, field.getInt(Opcodes.class));
                }
            }
            return latest;
        } catch (IllegalAccessException e) {
            return Opcodes.ASM6;
        }
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
            super(getLatestVersion());
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
            MemberDefinition definition = new AsmMemberDefinition(0, asmClass, access, TypeFactory.createType(desc), getIdentifier(name), value);
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
