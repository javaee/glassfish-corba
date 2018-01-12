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

import org.glassfish.rmic.tools.java.Identifier;
import org.glassfish.rmic.tools.java.Type;

/**
 * A Factory to create MethodType objects from ASM method descriptors.
 */
class TypeFactory {
    static Type createType(String descriptor) {
        return toRmicType(org.objectweb.asm.Type.getType(descriptor));
    }

    static Type createMethodType(String descriptor) {
        org.objectweb.asm.Type returnType = org.objectweb.asm.Type.getReturnType(descriptor);
        return Type.tMethod(toRmicType(returnType), toTypeArray(org.objectweb.asm.Type.getArgumentTypes(descriptor)));
    }

    private static Type[] toTypeArray(org.objectweb.asm.Type[] argumentTypes) {
        Type[] result = new Type[argumentTypes.length];
        for (int i = 0; i < result.length; i++)
            result[i] = toRmicType(argumentTypes[i]);
        return result;
    }

    private static Type toRmicType(org.objectweb.asm.Type asmType) {
        switch (asmType.getSort()) {
            case org.objectweb.asm.Type.VOID:
                return Type.tVoid;
            case org.objectweb.asm.Type.BOOLEAN:
                return Type.tBoolean;
            case org.objectweb.asm.Type.BYTE:
                return Type.tByte;
            case org.objectweb.asm.Type.SHORT:
                return Type.tShort;
            case org.objectweb.asm.Type.INT:
                return Type.tInt;
            case org.objectweb.asm.Type.LONG:
                return Type.tLong;
            case org.objectweb.asm.Type.FLOAT:
                return Type.tFloat;
            case org.objectweb.asm.Type.DOUBLE:
                return Type.tDouble;
            case org.objectweb.asm.Type.ARRAY:
                return toArrayType(asmType);
            default:
                return Type.tClass(Identifier.lookup(asmType.getClassName()));
        }
    }

    private static Type toArrayType(org.objectweb.asm.Type asmType) {
        Type type = toRmicType(asmType.getElementType());
        for (int i = 0; i < asmType.getDimensions(); i++)
            type = Type.tArray(type);
        return type;
    }
}
