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
import java.io.PrintStream;
import java.util.Hashtable;

/**
 * WARNING: The contents of this source file are not part of any
 * supported API.  Code that depends on them does so at its own risk:
 * they are subject to change or removal without notice.
 */
public
class UnaryExpression extends Expression {
    Expression right;

    /**
     * Constructor
     */
    UnaryExpression(int op, long where, Type type, Expression right) {
        super(op, where, type);
        this.right = right;
    }

    /**
     * Order the expression based on precedence
     */
    public Expression order() {
        if (precedence() > right.precedence()) {
            UnaryExpression e = (UnaryExpression)right;
            right = e.right;
            e.right = order();
            return e;
        }
        return this;
    }

    /**
     * Select the type of the expression
     */
    void selectType(Environment env, Context ctx, int tm) {
        throw new CompilerError("selectType: " + opNames[op]);
    }

    /**
     * Check a unary expression
     */
    public Vset checkValue(Environment env, Context ctx, Vset vset, Hashtable<Object, Object> exp) {
        vset = right.checkValue(env, ctx, vset, exp);

        int tm = right.type.getTypeMask();
        selectType(env, ctx, tm);
        if (((tm & TM_ERROR) == 0) && type.isType(TC_ERROR)) {
            env.error(where, "invalid.arg", opNames[op]);
        }
        return vset;
    }

    /**
     * Check if constant
     */
    public boolean isConstant() {
        switch (op) {
        case POS:
        case NEG:
        case BITNOT:
        case NOT:
        case EXPR:
        case CONVERT: // generated inside of CastExpression
            return right.isConstant();
        }
        return false;
    }

    /**
     * Evaluate
     */
    Expression eval(int a) {
        return this;
    }
    Expression eval(long a) {
        return this;
    }
    Expression eval(float a) {
        return this;
    }
    Expression eval(double a) {
        return this;
    }
    Expression eval(boolean a) {
        return this;
    }
    Expression eval(String a) {
        return this;
    }
    Expression eval() {
        switch (right.op) {
          case BYTEVAL:
          case CHARVAL:
          case SHORTVAL:
          case INTVAL:
            return eval(((IntegerExpression)right).value);
          case LONGVAL:
            return eval(((LongExpression)right).value);
          case FLOATVAL:
            return eval(((FloatExpression)right).value);
          case DOUBLEVAL:
            return eval(((DoubleExpression)right).value);
          case BOOLEANVAL:
            return eval(((BooleanExpression)right).value);
          case STRINGVAL:
            return eval(((StringExpression)right).value);
        }
        return this;
    }

    /**
     * Inline
     */
    public Expression inline(Environment env, Context ctx) {
        return right.inline(env, ctx);
    }
    public Expression inlineValue(Environment env, Context ctx) {
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
            // I am not positive that this catch is ever reached.
            //
            // env.error(where, "arithmetic.exception");
            return this;
        }
    }

    /**
     * Create a copy of the expression for method inlining
     */
    public Expression copyInline(Context ctx) {
        UnaryExpression e = (UnaryExpression)clone();
        if (right != null) {
            e.right = right.copyInline(ctx);
        }
        return e;
    }

    /**
     * The cost of inlining this expression
     */
    public int costInline(int thresh, Environment env, Context ctx) {
        return 1 + right.costInline(thresh, env, ctx);
    }

    /**
     * Print
     */
    public void print(PrintStream out) {
        out.print("(" + opNames[op] + " ");
        right.print(out);
        out.print(")");
    }
}
