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
import org.glassfish.rmic.tools.asm.TryData;
import org.glassfish.rmic.tools.asm.CatchData;
import java.io.PrintStream;
import java.util.Hashtable;

/**
 * WARNING: The contents of this source file are not part of any
 * supported API.  Code that depends on them does so at its own risk:
 * they are subject to change or removal without notice.
 */
public
class SynchronizedStatement extends Statement {
    Expression expr;
    Statement body;
    boolean needReturnSlot;   // set by inner return statement

    /**
     * Constructor
     */
    public SynchronizedStatement(long where, Expression expr, Statement body) {
        super(SYNCHRONIZED, where);
        this.expr = expr;
        this.body = body;
    }

    /**
     * Check statement
     */
    Vset check(Environment env, Context ctx, Vset vset, Hashtable<Object, Object> exp) {
        checkLabel(env, ctx);
        CheckContext newctx = new CheckContext(ctx, this);
        vset = reach(env, vset);
        vset = expr.checkValue(env, newctx, vset, exp);
        if (expr.type.equals(Type.tNull)) {
            env.error(expr.where, "synchronized.null");
        }
        expr = convert(env, newctx, Type.tClass(idJavaLangObject), expr);
        vset = body.check(env, newctx, vset, exp);
        return ctx.removeAdditionalVars(vset.join(newctx.vsBreak));
    }

    /**
     * Inline
     */
    public Statement inline(Environment env, Context ctx) {
        if (body != null) {
            body = body.inline(env, ctx);
        }
        expr = expr.inlineValue(env, ctx);
        return this;
    }

    /**
     * Create a copy of the statement for method inlining
     */
    public Statement copyInline(Context ctx, boolean valNeeded) {
        SynchronizedStatement s = (SynchronizedStatement)clone();
        s.expr = expr.copyInline(ctx);
        if (body != null) {
            s.body = body.copyInline(ctx, valNeeded);
        }
        return s;
    }

    /**
     * Compute cost of inlining this statement
     */
    public int costInline(int thresh, Environment env, Context ctx){
        int cost = 1;
        if (expr != null) {
            cost += expr.costInline(thresh, env,ctx);
            if (cost >= thresh) return cost;
        }
        if (body != null) {
            cost += body.costInline(thresh, env,ctx);
        }
        return cost;
    }

    /**
     * Code
     */
    public void code(Environment env, Context ctx, Assembler asm) {
        ClassDefinition clazz = ctx.field.getClassDefinition();
        expr.codeValue(env, ctx, asm);
        ctx = new Context(ctx);

        if (needReturnSlot) {
            Type returnType = ctx.field.getType().getReturnType();
            LocalMember localfield = new LocalMember(0, clazz, 0, returnType,
                                                   idFinallyReturnValue);
            ctx.declare(env, localfield);
            Environment.debugOutput("Assigning return slot to " + localfield.number);
        }

        LocalMember f1 = new LocalMember(where, clazz, 0, Type.tObject, null);
        LocalMember f2 = new LocalMember(where, clazz, 0, Type.tInt, null);
        Integer num1 = ctx.declare(env, f1);
        Integer num2 = ctx.declare(env, f2);

        Label endLabel = new Label();

        TryData td = new TryData();
        td.add(null);

        // lock the object
        asm.add(where, opc_astore, num1);
        asm.add(where, opc_aload, num1);
        asm.add(where, opc_monitorenter);

        // Main body
        CodeContext bodyctx = new CodeContext(ctx, this);
        asm.add(where, opc_try, td);
        if (body != null) {
            body.code(env, bodyctx, asm);
        } else {
            asm.add(where, opc_nop);
        }
        asm.add(bodyctx.breakLabel);
        asm.add(td.getEndLabel());

        // Cleanup afer body
        asm.add(where, opc_aload, num1);
        asm.add(where, opc_monitorexit);
        asm.add(where, opc_goto, endLabel);

        // Catch code
        CatchData cd = td.getCatch(0);
        asm.add(cd.getLabel());
        asm.add(where, opc_aload, num1);
        asm.add(where, opc_monitorexit);
        asm.add(where, opc_athrow);

        // Final body
        asm.add(bodyctx.contLabel);
        asm.add(where, opc_astore, num2);
        asm.add(where, opc_aload, num1);
        asm.add(where, opc_monitorexit);
        asm.add(where, opc_ret, num2);

        asm.add(endLabel);
    }

    /**
     * Print
     */
    public void print(PrintStream out, int indent) {
        super.print(out, indent);
        out.print("synchronized ");
        expr.print(out);
        out.print(" ");
        if (body != null) {
            body.print(out, indent);
        } else {
            out.print("{}");
        }
    }
}
