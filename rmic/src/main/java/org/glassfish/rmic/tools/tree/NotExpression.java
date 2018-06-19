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
import java.util.Hashtable;

/**
 * WARNING: The contents of this source file are not part of any
 * supported API.  Code that depends on them does so at its own risk:
 * they are subject to change or removal without notice.
 */
public
class NotExpression extends UnaryExpression {
    /**
     * Constructor
     */
    public NotExpression(long where, Expression right) {
        super(NOT, where, Type.tBoolean, right);
    }

    /**
     * Select the type of the expression
     */
    void selectType(Environment env, Context ctx, int tm) {
        right = convert(env, ctx, Type.tBoolean, right);
    }

    /*
     * Check a "not" expression.
     *
     * cvars is modified so that
     *    cvar.vsTrue indicates variables with a known value if
     *         the expression is true.
     *    cvars.vsFalse indicates variables with a known value if
     *         the expression is false
     *
     * For "not" expressions, we look at the inside expression, and then
     * swap true and false.
     */

    public void checkCondition(Environment env, Context ctx, Vset vset,
                               Hashtable<Object, Object> exp, ConditionVars cvars) {
        right.checkCondition(env, ctx, vset, exp, cvars);
        right = convert(env, ctx, Type.tBoolean, right);
        // swap true and false
        Vset temp = cvars.vsFalse;
        cvars.vsFalse = cvars.vsTrue;
        cvars.vsTrue = temp;
    }

    /**
     * Evaluate
     */
    Expression eval(boolean a) {
        return new BooleanExpression(where, !a);
    }

    /**
     * Simplify
     */
    Expression simplify() {
        // Check if the expression can be optimized
        switch (right.op) {
          case NOT:
            return ((NotExpression)right).right;

          case EQ:
          case NE:
          case LT:
          case LE:
          case GT:
          case GE:
            break;

          default:
            return this;
        }

        // Can't negate real comparisons
        BinaryExpression bin = (BinaryExpression)right;
        if (bin.left.type.inMask(TM_REAL)) {
            return this;
        }

        // Negate comparison
        switch (right.op) {
          case EQ:
            return new NotEqualExpression(where, bin.left, bin.right);
          case NE:
            return new EqualExpression(where, bin.left, bin.right);
          case LT:
            return new GreaterOrEqualExpression(where, bin.left, bin.right);
          case LE:
            return new GreaterExpression(where, bin.left, bin.right);
          case GT:
            return new LessOrEqualExpression(where, bin.left, bin.right);
          case GE:
            return new LessExpression(where, bin.left, bin.right);
        }
        return this;
    }

    /**
     * Code
     */
    void codeBranch(Environment env, Context ctx, Assembler asm, Label lbl, boolean whenTrue) {
        right.codeBranch(env, ctx, asm, lbl, !whenTrue);
    }

    /**
     * Instead of relying on the default code generation which uses
     * conditional branching, generate a simpler stream using XOR.
     */
    public void codeValue(Environment env, Context ctx, Assembler asm) {
        right.codeValue(env, ctx, asm);
        asm.add(where, opc_ldc, 1);
        asm.add(where, opc_ixor);
    }

}
