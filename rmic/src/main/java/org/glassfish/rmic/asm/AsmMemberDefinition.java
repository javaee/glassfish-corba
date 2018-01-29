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
import org.glassfish.rmic.tools.java.ClassDeclaration;
import org.glassfish.rmic.tools.java.ClassDefinition;
import org.glassfish.rmic.tools.java.Environment;
import org.glassfish.rmic.tools.java.Identifier;
import org.glassfish.rmic.tools.java.MemberDefinition;
import org.glassfish.rmic.tools.java.Type;

import java.util.Vector;

public class AsmMemberDefinition extends MemberDefinition {
    private final ClassDeclaration[] exceptions;

    /**
     * Constructor for a method definition
     * @param where the location of the definition relative to the class
     * @param clazz the containing class
     * @param modifiers the access modifiers
     * @param type the constructed type
     * @param name the name of the method
     * @param exceptions the checked exceptions throwable by the method
     */
    AsmMemberDefinition(long where, ClassDefinition clazz, int modifiers, Type type, Identifier name, String[] exceptions) {
        super(where, clazz, modifiers, type, name, null, null);

        this.exceptions = toClassDeclarations(exceptions);
    }

    /**
     * Constructor for a field definition
     * @param where the location of the definition relative to the class
     * @param clazz the containing class
     * @param modifiers the access modifiers
     * @param type the constructed type
     * @param name the name of the method
     * @param value the default value for the field
     */
    AsmMemberDefinition(long where, ClassDefinition clazz, int modifiers, Type type, Identifier name, Object value) {
        super(where, clazz, modifiers, type, name, null, null);
        exceptions = null;
    }

    private ClassDeclaration[] toClassDeclarations(String[] classNames) {
        if (classNames == null) return new ClassDeclaration[0];

        ClassDeclaration[] result = new ClassDeclaration[classNames.length];
        for (int i = 0; i < classNames.length; i++)
            result[i] = new ClassDeclaration(Identifier.lookup(classNames[i].replace('/','.')));
        return result;

    }

    @Override
    public ClassDeclaration[] getExceptions(Environment env) {
        return exceptions;
    }

    @Override
    public Vector<MemberDefinition> getArguments() {
        return null;
    }
}
