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

import org.glassfish.rmic.tools.java.ClassDeclaration;
import org.glassfish.rmic.tools.java.ClassDefinition;
import org.glassfish.rmic.tools.java.ClassNotFound;
import org.glassfish.rmic.tools.java.Environment;
import org.glassfish.rmic.tools.java.Identifier;
import org.glassfish.rmic.tools.java.MemberDefinition;
import org.glassfish.rmic.tools.java.Type;

import java.util.Vector;

public class AsmMemberDefinition extends MemberDefinition {
    private final ClassDeclaration[] exceptions;
    private final String memberValueString;

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

        this.memberValueString = null;
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

        memberValueString = type.toStringValue(value);
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
    public String getMemberValueString(Environment env) throws ClassNotFound {
        return memberValueString;
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
