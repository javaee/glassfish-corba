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
import java.util.Hashtable;

/**
 * Parenthesized expressions.
 *
 * WARNING: The contents of this source file are not part of any
 * supported API.  Code that depends on them does so at its own risk:
 * they are subject to change or removal without notice.
 */

public
class ExprExpression extends UnaryExpression {
    /**
     * Constructor
     */
    public ExprExpression(long where, Expression right) {
        super(EXPR, where, right.type, right);
    }

    /**
     * Check a condition.  We must pass it on to our unparenthesised form.
     */
    public void checkCondition(Environment env, Context ctx, Vset vset,
                               Hashtable<Object, Object> exp, ConditionVars cvars) {
        right.checkCondition(env, ctx, vset, exp, cvars);
        type = right.type;
    }

    /**
     * Check the expression if it appears as an lvalue.
     * We just pass it on to our unparenthesized subexpression.
     * (Part of fix for 4090372)
     */
    public Vset checkAssignOp(Environment env, Context ctx,
                              Vset vset, Hashtable<Object, Object> exp, Expression outside) {
        vset = right.checkAssignOp(env, ctx, vset, exp, outside);
        type = right.type;
        return vset;
    }

    /**
     * Delegate to our subexpression.
     * (Part of fix for 4090372)
     */
    public FieldUpdater getUpdater(Environment env, Context ctx) {
        return right.getUpdater(env, ctx);
    }

    // Allow (x) = 9;
    //
    // I will hold off on this until I'm sure about it.  Nobody's
    // going to clammer for this one.
    //
    // public Vset checkLHS(Environment env, Context ctx,
    //     Vset vset, Hashtable<Object, Object> exp) {
    //     vset = right.check(env, ctx, vset, exp);
    //     type = right.type;
    //     return vset;
    // }

    public boolean isNull() {
        return right.isNull();
    }

    public boolean isNonNull() {
        return right.isNonNull();
    }

    // Probably not necessary
    public Object getValue() {
        return right.getValue();
    }

    /**
     * Delegate to our subexpression.
     * See the comment in AddExpression#inlineValueSB() for
     * information about this method.
     */
    protected StringBuffer inlineValueSB(Environment env,
                                         Context ctx,
                                         StringBuffer buffer) {
        return right.inlineValueSB(env, ctx, buffer);
    }

    /**
     * Select the type of the expression
     */
    void selectType(Environment env, Context ctx, int tm) {
        type = right.type;
    }

    /**
     * Simplify
     */
    Expression simplify() {
        return right;
    }
}
