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
class WhileStatement extends Statement {
    Expression cond;
    Statement body;

    /**
     * Constructor
     */
    public WhileStatement(long where, Expression cond, Statement body) {
        super(WHILE, where);
        this.cond = cond;
        this.body = body;
    }

    /**
     * Check a while statement
     */
    Vset check(Environment env, Context ctx, Vset vset, Hashtable<Object, Object> exp) {
        checkLabel(env, ctx);
        CheckContext newctx = new CheckContext(ctx, this);
        // remember what was unassigned on entry
        Vset vsEntry = vset.copy();
        // check the condition.  Determine which variables have values if
        // it returns true or false.
        ConditionVars cvars =
              cond.checkCondition(env, newctx, reach(env, vset), exp);
        cond = convert(env, newctx, Type.tBoolean, cond);
        // check the body, given that the condition returned true.
        vset = body.check(env, newctx, cvars.vsTrue, exp);
        vset = vset.join(newctx.vsContinue);
        // make sure the back-branch fits the entry of the loop
        ctx.checkBackBranch(env, this, vsEntry, vset);
        // Exit the while loop by testing false or getting a break statement
        vset = newctx.vsBreak.join(cvars.vsFalse);
        return ctx.removeAdditionalVars(vset);
    }

    /**
     * Inline
     */
    public Statement inline(Environment env, Context ctx) {
        ctx = new Context(ctx, this);
        cond = cond.inlineValue(env, ctx);
        if (body != null) {
            body = body.inline(env, ctx);
        }
        return this;
    }

    /**
     * The cost of inlining this statement
     */
    public int costInline(int thresh, Environment env, Context ctx) {
        return 1 + cond.costInline(thresh, env, ctx)
                 + ((body != null) ? body.costInline(thresh, env, ctx) : 0);
    }

    /**
     * Create a copy of the statement for method inlining
     */
    public Statement copyInline(Context ctx, boolean valNeeded) {
        WhileStatement s = (WhileStatement)clone();
        s.cond = cond.copyInline(ctx);
        if (body != null) {
            s.body = body.copyInline(ctx, valNeeded);
        }
        return s;
    }

    /**
     * Code
     */
    public void code(Environment env, Context ctx, Assembler asm) {
        CodeContext newctx = new CodeContext(ctx, this);

        asm.add(where, opc_goto, newctx.contLabel);

        Label l1 = new Label();
        asm.add(l1);

        if (body != null) {
            body.code(env, newctx, asm);
        }

        asm.add(newctx.contLabel);
        cond.codeBranch(env, newctx, asm, l1, true);
        asm.add(newctx.breakLabel);
    }

    /**
     * Print
     */
    public void print(PrintStream out, int indent) {
        super.print(out, indent);
        out.print("while ");
        cond.print(out);
        if (body != null) {
            out.print(" ");
            body.print(out, indent);
        } else {
            out.print(";");
        }
    }
}
