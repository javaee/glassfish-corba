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
class AndExpression extends BinaryLogicalExpression {
    /**
     * constructor
     */
    public AndExpression(long where, Expression left, Expression right) {
        super(AND, where, left, right);
    }

    /*
     * Check an "and" expression.
     *
     * cvars is modified so that
     *    cvar.vsTrue indicates variables with a known value if
     *        both the left and right hand side are true
     *    cvars.vsFalse indicates variables with a known value
     *        either the left or right hand side is false
     */
    public void checkCondition(Environment env, Context ctx, Vset vset,
                               Hashtable<Object, Object> exp, ConditionVars cvars) {
        // Find out when the left side is true/false
        left.checkCondition(env, ctx, vset, exp, cvars);
        left = convert(env, ctx, Type.tBoolean, left);
        Vset vsTrue = cvars.vsTrue.copy();
        Vset vsFalse = cvars.vsFalse.copy();

        // Only look at the right side if the left side is true
        right.checkCondition(env, ctx, vsTrue, exp, cvars);
        right = convert(env, ctx, Type.tBoolean, right);

        // cvars.vsTrue already reports when both returned true
        // cvars.vsFalse must be set to either the left or right side
        //    returning false
        cvars.vsFalse = cvars.vsFalse.join(vsFalse);
    }

    /**
     * Evaluate
     */
    Expression eval(boolean a, boolean b) {
        return new BooleanExpression(where, a && b);
    }

    /**
     * Simplify
     */
    Expression simplify() {
        if (left.equals(true)) {
            return right;
        }
        if (right.equals(false)) {
            // Preserve effects of left argument.
            return new CommaExpression(where, left, right).simplify();
        }
        if (right.equals(true)) {
            return left;
        }
        if (left.equals(false)) {
            return left;
        }
        return this;
    }

    /**
     * Code
     */
    void codeBranch(Environment env, Context ctx, Assembler asm, Label lbl, boolean whenTrue) {
        if (whenTrue) {
            Label lbl2 = new Label();
            left.codeBranch(env, ctx, asm, lbl2, false);
            right.codeBranch(env, ctx, asm, lbl, true);
            asm.add(lbl2);
        } else {
            left.codeBranch(env, ctx, asm, lbl, false);
            right.codeBranch(env, ctx, asm, lbl, false);
        }
    }
}
