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
import org.glassfish.rmic.tools.java.ClassNotFound;
import org.glassfish.rmic.tools.java.Environment;
import org.glassfish.rmic.tools.java.Identifier;

/**
 * This represents a class for RMIC to process. It is built from a class file using ASM.
 */
class AsmClass extends ClassDefinition {

    private final AsmClassFactory factory;

    AsmClass(AsmClassFactory factory, String name, int modifiers, ClassDeclaration declaration, ClassDeclaration superClassDeclaration, ClassDeclaration[] interfaceDeclarations) {
        super(name, 0, declaration, modifiers, null, null);
        this.factory = factory;
        superClass = superClassDeclaration;
        interfaces = interfaceDeclarations;
    }

    @Override
    public void loadNested(Environment env) {
        try {
            Identifier outerClass = factory.getOuterClassName(getName());
            if (outerClass != null)
                this.outerClass = env.getClassDefinition(outerClass);
        } catch (ClassNotFound ignore) {
        }
    }

}
