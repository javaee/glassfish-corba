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
import org.glassfish.rmic.tools.asm.Assembler;
import java.util.Hashtable;

/**
 * WARNING: The contents of this source file are not part of any
 * supported API.  Code that depends on them does so at its own risk:
 * they are subject to change or removal without notice.
 */
public
class IncDecExpression extends UnaryExpression {

    private FieldUpdater updater = null;

    /**
     * Constructor
     */
    public IncDecExpression(int op, long where, Expression right) {
        super(op, where, right.type, right);
    }

    /**
     * Check an increment or decrement expression
     */
    public Vset checkValue(Environment env, Context ctx, Vset vset, Hashtable<Object, Object> exp) {
        vset = right.checkAssignOp(env, ctx, vset, exp, this);
        if (right.type.inMask(TM_NUMBER)) {
            type = right.type;
        } else {
            if (!right.type.isType(TC_ERROR)) {
                env.error(where, "invalid.arg.type", right.type, opNames[op]);
            }
            type = Type.tError;
        }
        updater = right.getUpdater(env, ctx);  // Must be called after 'checkAssignOp'.
        return vset;
    }

    /**
     * Check void expression
     */
    public Vset check(Environment env, Context ctx, Vset vset, Hashtable<Object, Object> exp) {
        return checkValue(env, ctx, vset, exp);
    }

    /**
     * Inline
     */
    public Expression inline(Environment env, Context ctx) {
        return inlineValue(env, ctx);
    }
    public Expression inlineValue(Environment env, Context ctx) {
        // Why not inlineLHS?  But that does not work.
        right = right.inlineValue(env, ctx);
        if (updater != null) {
            updater = updater.inline(env, ctx);
        }
        return this;
    }

    public int costInline(int thresh, Environment env, Context ctx) {
        if (updater == null) {
            if ((right.op == IDENT) && type.isType(TC_INT) &&
                (((IdentifierExpression)right).field.isLocal())) {
                // Increment variable in place.  Count 3 bytes for 'iinc'.
                return 3;
            }
            // Cost to load lhs reference, fetch local, increment, and store.
            // Load/store cost will be higher if variable is a field.  Note that
            // costs are highly approximate. See 'AssignOpExpression.costInline'
            // Does not account for cost of conversions,or duplications in
            // value-needed context..
            return right.costInline(thresh, env, ctx) + 4;
        } else {
            // Cost of two access method calls (get/set) + cost of increment.
            return updater.costInline(thresh, env, ctx, true) + 1;
        }
    }


    /**
     * Code
     */

    private void codeIncDecOp(Assembler asm, boolean inc) {
        switch (type.getTypeCode()) {
          case TC_BYTE:
            asm.add(where, opc_ldc, 1);
            asm.add(where, inc ? opc_iadd : opc_isub);
            asm.add(where, opc_i2b);
            break;
          case TC_SHORT:
            asm.add(where, opc_ldc, 1);
            asm.add(where, inc ? opc_iadd : opc_isub);
            asm.add(where, opc_i2s);
            break;
          case TC_CHAR:
            asm.add(where, opc_ldc, 1);
            asm.add(where, inc ? opc_iadd : opc_isub);
            asm.add(where, opc_i2c);
            break;
          case TC_INT:
            asm.add(where, opc_ldc, 1);
            asm.add(where, inc ? opc_iadd : opc_isub);
            break;
          case TC_LONG:
            asm.add(where, opc_ldc2_w, 1L);
            asm.add(where, inc ? opc_ladd : opc_lsub);
            break;
          case TC_FLOAT:
            asm.add(where, opc_ldc, new Float(1));
            asm.add(where, inc ? opc_fadd : opc_fsub);
            break;
          case TC_DOUBLE:
            asm.add(where, opc_ldc2_w, new Double(1));
            asm.add(where, inc ? opc_dadd : opc_dsub);
            break;
          default:
            throw new CompilerError("invalid type");
        }
    }

    void codeIncDec(Environment env, Context ctx, Assembler asm, boolean inc, boolean prefix, boolean valNeeded) {

        // The 'iinc' instruction cannot be used if an access method call is required.
        if ((right.op == IDENT) && type.isType(TC_INT) &&
            (((IdentifierExpression)right).field.isLocal()) && updater == null) {
            if (valNeeded && !prefix) {
                right.codeLoad(env, ctx, asm);
            }
            int v = ((LocalMember)((IdentifierExpression)right).field).number;
            int[] operands = { v, inc ? 1 : -1 };
            asm.add(where, opc_iinc, operands);
            if (valNeeded && prefix) {
                right.codeLoad(env, ctx, asm);
            }
            return;

        }

        if (updater == null) {
            // Field is directly accessible.
            int depth = right.codeLValue(env, ctx, asm);
            codeDup(env, ctx, asm, depth, 0);
            right.codeLoad(env, ctx, asm);
            if (valNeeded && !prefix) {
                codeDup(env, ctx, asm, type.stackSize(), depth);
            }
            codeIncDecOp(asm, inc);
            if (valNeeded && prefix) {
                codeDup(env, ctx, asm, type.stackSize(), depth);
            }
            right.codeStore(env, ctx, asm);
        } else {
            // Must use access methods.
            updater.startUpdate(env, ctx, asm, (valNeeded && !prefix));
            codeIncDecOp(asm, inc);
            updater.finishUpdate(env, ctx, asm, (valNeeded && prefix));
        }
    }

}
