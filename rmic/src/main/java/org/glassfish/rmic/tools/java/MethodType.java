/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright (c) 1994-2018 Oracle and/or its affiliates. All rights reserved.
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

package org.glassfish.rmic.tools.java;

import org.glassfish.rmic.TypeCode;

/**
 * This class represents an Java method type.
 * It overrides the relevant methods in class Type.
 *
 * WARNING: The contents of this source file are not part of any
 * supported API.  Code that depends on them does so at its own risk:
 * they are subject to change or removal without notice.
 *
 * @author      Arthur van Hoff
 */
public final
class MethodType extends Type {
    /**
     * The return type.
     */
    Type returnType;

    /**
     * The argument types.
     */
    Type argTypes[];

    /**
     * Construct a method type. Use Type.tMethod to create
     * a new method type.
     * @see Type.tMethod
     */
    MethodType(String typeSig, Type returnType, Type argTypes[]) {
        super(TypeCode.METHOD, typeSig);
        this.returnType = returnType;
        this.argTypes = argTypes;
    }

    public Type getReturnType() {
        return returnType;
    }

    public Type getArgumentTypes()[] {
        return argTypes;
    }

    public boolean equalArguments(Type t) {
        if (t.getTypeCode() != TC_METHOD) {
            return false;
        }
        MethodType m = (MethodType)t;
        if (argTypes.length != m.argTypes.length) {
            return false;
        }
        for (int i = argTypes.length - 1 ; i >= 0 ; i--) {
            if (argTypes[i] != m.argTypes[i]) {
                return false;
            }
        }
        return true;
    }

    public int stackSize() {
        int n = 0;
        for (int i = 0 ; i < argTypes.length ; i++) {
            n += argTypes[i].stackSize();
        }
        return n;
    }

    public String typeString(String id, boolean abbrev, boolean ret) {
        StringBuilder sb = new StringBuilder();
        sb.append(id);
        sb.append('(');
        for (int i = 0 ; i < argTypes.length ; i++) {
            if (i > 0) {
                sb.append(", ");
            }
            sb.append(argTypes[i].typeString("", abbrev, ret));
        }
        sb.append(')');

        return ret ? getReturnType().typeString(sb.toString(), abbrev, ret) : sb.toString();
    }
}
