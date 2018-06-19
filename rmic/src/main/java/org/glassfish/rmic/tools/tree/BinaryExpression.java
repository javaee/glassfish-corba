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
import org.glassfish.rmic.tools.asm.Label;
import org.glassfish.rmic.tools.asm.Assembler;
import java.io.PrintStream;
import java.util.Hashtable;

/**
 * WARNING: The contents of this source file are not part of any
 * supported API.  Code that depends on them does so at its own risk:
 * they are subject to change or removal without notice.
 */
public
class BinaryExpression extends UnaryExpression {
    Expression left;

    /**
     * Constructor
     */
    BinaryExpression(int op, long where, Type type, Expression left, Expression right) {
        super(op, where, type, right);
        this.left = left;
    }

    /**
     * Order the expression based on precedence
     */
    public Expression order() {
        if (precedence() > left.precedence()) {
            UnaryExpression e = (UnaryExpression)left;
            left = e.right;
            e.right = order();
            return e;
        }
        return this;
    }

    /**
     * Check a binary expression
     */
    public Vset checkValue(Environment env, Context ctx, Vset vset, Hashtable<Object, Object> exp) {
        vset = left.checkValue(env, ctx, vset, exp);
        vset = right.checkValue(env, ctx, vset, exp);

        int tm = left.type.getTypeMask() | right.type.getTypeMask();
        if ((tm & TM_ERROR) != 0) {
            return vset;
        }
        selectType(env, ctx, tm);

        if (type.isType(TC_ERROR)) {
            env.error(where, "invalid.args", opNames[op]);
        }
        return vset;
    }

    /**
     * Check if constant
     */
    public boolean isConstant() {
        switch (op) {
        case MUL:
        case DIV:
        case REM:
        case ADD:
        case SUB:
        case LSHIFT:
        case RSHIFT:
        case URSHIFT:
        case LT:
        case LE:
        case GT:
        case GE:
        case EQ:
        case NE:
        case BITAND:
        case BITXOR:
        case BITOR:
        case AND:
        case OR:
            return left.isConstant() && right.isConstant();
        }
        return false;
    }
    /**
     * Evaluate
     */
    Expression eval(int a, int b) {
        return this;
    }
    Expression eval(long a, long b) {
        return this;
    }
    Expression eval(float a, float b) {
        return this;
    }
    Expression eval(double a, double b) {
        return this;
    }
    Expression eval(boolean a, boolean b) {
        return this;
    }
    Expression eval(String a, String b) {
        return this;
    }
    Expression eval() {
        // See also the eval() code in BinaryShiftExpression.java.
        if (left.op == right.op) {
            switch (left.op) {
              case BYTEVAL:
              case CHARVAL:
              case SHORTVAL:
              case INTVAL:
                return eval(((IntegerExpression)left).value, ((IntegerExpression)right).value);
              case LONGVAL:
                return eval(((LongExpression)left).value, ((LongExpression)right).value);
              case FLOATVAL:
                return eval(((FloatExpression)left).value, ((FloatExpression)right).value);
              case DOUBLEVAL:
                return eval(((DoubleExpression)left).value, ((DoubleExpression)right).value);
              case BOOLEANVAL:
                return eval(((BooleanExpression)left).value, ((BooleanExpression)right).value);
              case STRINGVAL:
                return eval(((StringExpression)left).value, ((StringExpression)right).value);
            }
        }
        return this;
    }

    /**
     * Inline
     */
    public Expression inline(Environment env, Context ctx) {
        left = left.inline(env, ctx);
        right = right.inline(env, ctx);
        return (left == null) ? right : new CommaExpression(where, left, right);
    }
    public Expression inlineValue(Environment env, Context ctx) {
        left = left.inlineValue(env, ctx);
        right = right.inlineValue(env, ctx);
        try {
            return eval().simplify();
        } catch (ArithmeticException e) {
            // Got rid of this error message.  It isn't illegal to
            // have a program which does a constant division by
            // zero.  We return `this' to make the compiler to
            // generate code here.
            // (bugs 4019304, 4089107).
            //
            // env.error(where, "arithmetic.exception");
            return this;
        }
    }

    /**
     * Create a copy of the expression for method inlining
     */
    public Expression copyInline(Context ctx) {
        BinaryExpression e = (BinaryExpression)clone();
        if (left != null) {
            e.left = left.copyInline(ctx);
        }
        if (right != null) {
            e.right = right.copyInline(ctx);
        }
        return e;
    }

    /**
     * The cost of inlining this expression
     */
    public int costInline(int thresh, Environment env, Context ctx) {
        return 1 + ((left != null) ? left.costInline(thresh, env, ctx) : 0) +
                   ((right != null) ? right.costInline(thresh, env, ctx) : 0);
    }

    /**
     * Code
     */
    void codeOperation(Environment env, Context ctx, Assembler asm) {
        throw new CompilerError("codeOperation: " + opNames[op]);
    }
    public void codeValue(Environment env, Context ctx, Assembler asm) {
        if (type.isType(TC_BOOLEAN)) {
            Label l1 = new Label();
            Label l2 = new Label();

            codeBranch(env, ctx, asm, l1, true);
            asm.add(true, where, opc_ldc, 0);
            asm.add(true, where, opc_goto, l2);
            asm.add(l1);
            asm.add(true, where, opc_ldc, 1);
            asm.add(l2);
        } else {
            left.codeValue(env, ctx, asm);
            right.codeValue(env, ctx, asm);
            codeOperation(env, ctx, asm);
        }
    }

    /**
     * Print
     */
    public void print(PrintStream out) {
        out.print("(" + opNames[op] + " ");
        if (left != null) {
            left.print(out);
        } else {
            out.print("<null>");
        }
        out.print(" ");
        if (right != null) {
            right.print(out);
        } else {
            out.print("<null>");
        }
        out.print(")");
    }
}
