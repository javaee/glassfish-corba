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

package org.glassfish.rmic.tools.tree;

import org.glassfish.rmic.tools.java.*;
import org.glassfish.rmic.tools.asm.*;
import java.io.PrintStream;
import java.util.Hashtable;

/**
 * WARNING: The contents of this source file are not part of any
 * supported API.  Code that depends on them does so at its own risk:
 * they are subject to change or removal without notice.
 */
public
class ArrayExpression extends NaryExpression {
    /**
     * Constructor
     */
    public ArrayExpression(long where, Expression args[]) {
        super(ARRAY, where, Type.tError, null, args);
    }

    /**
     * Check expression type
     */
    public Vset checkValue(Environment env, Context ctx, Vset vset, Hashtable<Object, Object> exp) {
        env.error(where, "invalid.array.expr");
        return vset;
    }
    public Vset checkInitializer(Environment env, Context ctx, Vset vset, Type t, Hashtable<Object, Object> exp) {
        if (!t.isType(TC_ARRAY)) {
            if (!t.isType(TC_ERROR)) {
                env.error(where, "invalid.array.init", t);
            }
            return vset;
        }
        type = t;
        t = t.getElementType();
        for (int i = 0 ; i < args.length ; i++) {
            vset = args[i].checkInitializer(env, ctx, vset, t, exp);
            args[i] = convert(env, ctx, t, args[i]);
        }
        return vset;
    }

    /**
     * Inline
     */
    public Expression inline(Environment env, Context ctx) {
        Expression e = null;
        for (int i = 0 ; i < args.length ; i++) {
            args[i] = args[i].inline(env, ctx);
            if (args[i] != null) {
                e = (e == null) ? args[i] : new CommaExpression(where, e, args[i]);
            }
        }
        return e;
    }
    public Expression inlineValue(Environment env, Context ctx) {
        for (int i = 0 ; i < args.length ; i++) {
            args[i] = args[i].inlineValue(env, ctx);
        }
        return this;
    }

    /**
     * Code
     */
    public void codeValue(Environment env, Context ctx, Assembler asm) {
        int t = 0;
        asm.add(where, opc_ldc, args.length);
        switch (type.getElementType().getTypeCode()) {
          case TC_BOOLEAN:      asm.add(where, opc_newarray, T_BOOLEAN);   break;
          case TC_BYTE:         asm.add(where, opc_newarray, T_BYTE);      break;
          case TC_SHORT:        asm.add(where, opc_newarray, T_SHORT);     break;
          case TC_CHAR:         asm.add(where, opc_newarray, T_CHAR);      break;
          case TC_INT:          asm.add(where, opc_newarray, T_INT);       break;
          case TC_LONG:         asm.add(where, opc_newarray, T_LONG);      break;
          case TC_FLOAT:        asm.add(where, opc_newarray, T_FLOAT);     break;
          case TC_DOUBLE:       asm.add(where, opc_newarray, T_DOUBLE);    break;

          case TC_ARRAY:
            asm.add(where, opc_anewarray, type.getElementType());
            break;

          case TC_CLASS:
            asm.add(where, opc_anewarray, env.getClassDeclaration(type.getElementType()));
            break;

          default:
            throw new CompilerError("codeValue");
        }

        for (int i = 0 ; i < args.length ; i++) {

            // If the array element is the default initial value,
            // then don't bother generating code for this element.
            if (args[i].equalsDefault()) continue;

            asm.add(where, opc_dup);
            asm.add(where, opc_ldc, i);
            args[i].codeValue(env, ctx, asm);
            switch (type.getElementType().getTypeCode()) {
              case TC_BOOLEAN:
              case TC_BYTE:
                asm.add(where, opc_bastore);
                break;
              case TC_CHAR:
                asm.add(where, opc_castore);
                break;
              case TC_SHORT:
                asm.add(where, opc_sastore);
                break;
              default:
                asm.add(where, opc_iastore + type.getElementType().getTypeCodeOffset());
            }
        }
    }
}
