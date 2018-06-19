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
import org.glassfish.rmic.tools.asm.Label;
import java.io.PrintStream;
import java.util.Hashtable;

/**
 * WARNING: The contents of this source file are not part of any
 * supported API.  Code that depends on them does so at its own risk:
 * they are subject to change or removal without notice.
 */
public
class ConditionalExpression extends BinaryExpression {
    Expression cond;

    /**
     * Constructor
     */
    public ConditionalExpression(long where, Expression cond, Expression left, Expression right) {
        super(COND, where, Type.tError, left, right);
        this.cond = cond;
    }

    /**
     * Order the expression based on precedence
     */
    public Expression order() {
        if (precedence() > cond.precedence()) {
            UnaryExpression e = (UnaryExpression)cond;
            cond = e.right;
            e.right = order();
            return e;
        }
        return this;
    }

    /**
     * Check the expression
     */
    public Vset checkValue(Environment env, Context ctx, Vset vset, Hashtable<Object, Object> exp) {
        ConditionVars cvars = cond.checkCondition(env, ctx, vset, exp);
        vset = left.checkValue(env, ctx, cvars.vsTrue, exp).join(
               right.checkValue(env, ctx, cvars.vsFalse, exp) );
        cond = convert(env, ctx, Type.tBoolean, cond);

        int tm = left.type.getTypeMask() | right.type.getTypeMask();
        if ((tm & TM_ERROR) != 0) {
            type = Type.tError;
            return vset;
        }
        if (left.type.equals(right.type)) {
            type = left.type;
        } else if ((tm & TM_DOUBLE) != 0) {
            type = Type.tDouble;
        } else if ((tm & TM_FLOAT) != 0) {
            type = Type.tFloat;
        } else if ((tm & TM_LONG) != 0) {
            type = Type.tLong;
        } else if ((tm & TM_REFERENCE) != 0) {
            try {
                // This is wrong.  We should be using their most common
                // ancestor, instead.
                type = env.implicitCast(right.type, left.type)
                    ? left.type : right.type;
            } catch (ClassNotFound e) {
                type = Type.tError;
            }
        } else if (((tm & TM_CHAR) != 0) && left.fitsType(env, ctx, Type.tChar) && right.fitsType(env, ctx, Type.tChar)) {
            type = Type.tChar;
        } else if (((tm & TM_SHORT) != 0) && left.fitsType(env, ctx, Type.tShort) && right.fitsType(env, ctx, Type.tShort)) {
            type = Type.tShort;
        } else if (((tm & TM_BYTE) != 0) && left.fitsType(env, ctx, Type.tByte) && right.fitsType(env, ctx, Type.tByte)) {
            type = Type.tByte;
        } else {
            type = Type.tInt;
        }

        left = convert(env, ctx, type, left);
        right = convert(env, ctx, type, right);
        return vset;
    }

    public Vset check(Environment env, Context ctx, Vset vset, Hashtable<Object, Object> exp) {
        vset = cond.checkValue(env, ctx, vset, exp);
        cond = convert(env, ctx, Type.tBoolean, cond);
        return left.check(env, ctx, vset.copy(), exp).join(right.check(env, ctx, vset, exp));
    }

    /**
     * Check if constant
     */
    public boolean isConstant() {
        return cond.isConstant() && left.isConstant() && right.isConstant();
    }

    /**
     * Simplify
     */
    Expression simplify() {
        if (cond.equals(true)) {
            return left;
        }
        if (cond.equals(false)) {
            return right;
        }
        return this;
    }

    /**
     * Inline
     */
    public Expression inline(Environment env, Context ctx) {
        left = left.inline(env, ctx);
        right = right.inline(env, ctx);
        if ((left == null) && (right == null)) {
            return cond.inline(env, ctx);
        }
        if (left == null) {
            left = right;
            right = null;
            cond = new NotExpression(where, cond);
        }
        cond = cond.inlineValue(env, ctx);
        return simplify();
    }

    public Expression inlineValue(Environment env, Context ctx) {
        cond = cond.inlineValue(env, ctx);
        left = left.inlineValue(env, ctx);
        right = right.inlineValue(env, ctx);
        return simplify();
    }

    /**
     * The cost of inlining this expression
     */
    public int costInline(int thresh, Environment env, Context ctx) {
        // We need to check if right is null in case costInline()
        // is called after this expression has been inlined.
        // This call can happen, for example, in MemberDefinition#cleanup().
        // (Fix for 4069861).
        return 1 +
            cond.costInline(thresh, env, ctx) +
            left.costInline(thresh, env, ctx) +
            ((right == null) ? 0 : right.costInline(thresh, env, ctx));
    }

    /**
     * Create a copy of the expression for method inlining
     */
    public Expression copyInline(Context ctx) {
        ConditionalExpression e = (ConditionalExpression)clone();
        e.cond = cond.copyInline(ctx);
        e.left = left.copyInline(ctx);

        // If copyInline() is called after inlining is complete,
        // right could be null.
        e.right = (right == null) ? null : right.copyInline(ctx);

        return e;
    }

    /**
     * Code
     */
    public void codeValue(Environment env, Context ctx, Assembler asm) {
        Label l1 = new Label();
        Label l2 = new Label();

        cond.codeBranch(env, ctx, asm, l1, false);
        left.codeValue(env, ctx, asm);
        asm.add(where, opc_goto, l2);
        asm.add(l1);
        right.codeValue(env, ctx, asm);
        asm.add(l2);
    }
    public void code(Environment env, Context ctx, Assembler asm) {
        Label l1 = new Label();
        cond.codeBranch(env, ctx, asm, l1, false);
        left.code(env, ctx, asm);
        if (right != null) {
            Label l2 = new Label();
            asm.add(where, opc_goto, l2);
            asm.add(l1);
            right.code(env, ctx, asm);
            asm.add(l2);
        } else {
            asm.add(l1);
        }
    }

    /**
     * Print
     */
    public void print(PrintStream out) {
        out.print("(" + opNames[op] + " ");
        cond.print(out);
        out.print(" ");
        left.print(out);
        out.print(" ");
        if (right != null) {
            right.print(out);
        } else {
            out.print("<null>");
        }
        out.print(")");
    }
}
