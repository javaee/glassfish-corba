package org.glassfish.rmic.asm;

import org.glassfish.rmic.classes.nestedClasses.TwoLevelNested;
import org.glassfish.rmic.tools.java.Identifier;
import org.glassfish.rmic.tools.java.Type;
import org.junit.Test;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.arrayContaining;
import static org.hamcrest.Matchers.emptyArray;
import static org.hamcrest.Matchers.equalTo;

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
public class TypeFactoryTest {
    private TypeFactory factory = new TypeFactory();

    @Test
    public void constructNoArgVoidMethodType() throws Exception {
        Type methodType = TypeFactory.createMethodType("()V");

        assertThat(methodType.getReturnType(), equalTo(Type.tVoid));
        assertThat(methodType.getArgumentTypes(), emptyArray());
    }

    @Test
    public void constructByteArrayToIntType() throws Exception {
        Type methodType = TypeFactory.createMethodType("([B)I");

        assertThat(methodType.getReturnType(), equalTo(Type.tInt));
        assertThat(methodType.getArgumentTypes(), arrayContaining(Type.tArray(Type.tByte)));
    }

    @Test
    public void constructAllNumericArgsToBooleanMethod() throws Exception {
        Type methodType = TypeFactory.createMethodType("(SIJFD)Z");

        assertThat(methodType.getReturnType(), equalTo(Type.tBoolean));
        assertThat(methodType.getArgumentTypes(), arrayContaining(Type.tShort, Type.tInt, Type.tLong, Type.tFloat, Type.tDouble));
    }

    @Test
    public void constructAllObjectArguments() throws Exception {
        Type methodType = TypeFactory.createMethodType("(Ljava/lang/String;Lorg/glassfish/rmic/classes/nestedClasses/TwoLevelNested$Level1;)V");

        assertThat(methodType.getReturnType(), equalTo(Type.tVoid));
        assertThat(methodType.getArgumentTypes(), arrayContaining(Type.tString, Type.tClass(Identifier.lookup(TwoLevelNested.Level1.class.getName()))));
    }

    @Test
    public void constructObjectArrayArgument() throws Exception {
        Type methodType = TypeFactory.createMethodType("([Ljava/lang/Object;)V");

        assertThat(methodType.getReturnType(), equalTo(Type.tVoid));
        assertThat(methodType.getArgumentTypes(), arrayContaining(Type.tArray(Type.tObject)));
    }

    @Test
    public void constructMultiDimensionalArrayType() throws Exception {
        assertThat(TypeFactory.createType("[[I"), equalTo(Type.tArray(Type.tArray(Type.tInt))));
    }
}
