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
class ReturnStatement extends Statement {
    Expression expr;

    /**
     * Constructor
     */
    public ReturnStatement(long where, Expression expr) {
        super(RETURN, where);
        this.expr = expr;
    }

    /**
     * Check statement
     */
    Vset check(Environment env, Context ctx, Vset vset, Hashtable<Object, Object> exp) {
        checkLabel(env, ctx);
        vset = reach(env, vset);
        if (expr != null) {
            vset = expr.checkValue(env, ctx, vset, exp);
        }

        // Make sure the return isn't inside a static initializer
        if (ctx.field.isInitializer()) {
            env.error(where, "return.inside.static.initializer");
            return DEAD_END;
        }
        // Check return type
        if (ctx.field.getType().getReturnType().isType(TC_VOID)) {
            if (expr != null) {
                if (ctx.field.isConstructor()) {
                    env.error(where, "return.with.value.constr", ctx.field);
                } else {
                    env.error(where, "return.with.value", ctx.field);
                }
                expr = null;
            }
        } else {
            if (expr == null) {
                env.error(where, "return.without.value", ctx.field);
            } else {
                expr = convert(env, ctx, ctx.field.getType().getReturnType(), expr);
            }
        }
        CheckContext mctx = ctx.getReturnContext();
        if (mctx != null) {
            mctx.vsBreak = mctx.vsBreak.join(vset);
        }
        CheckContext exitctx = ctx.getTryExitContext();
        if (exitctx != null) {
            exitctx.vsTryExit = exitctx.vsTryExit.join(vset);
        }
        if (expr != null) {
            // see if we are returning a value out of a try or synchronized
            // statement.  If so, find the outermost one. . . .
            Node outerFinallyNode = null;
            for (Context c = ctx; c != null; c = c.prev) {
                if (c.node == null) {
                    continue;
                }
                if (c.node.op == METHOD) {
                    // Don't search outside current method. Fixes 4084230.
                    break;
                }
                if (c.node.op == SYNCHRONIZED) {
                    outerFinallyNode = c.node;
                    break;
                } else if (c.node.op == FINALLY
                           && ((CheckContext)c).vsContinue != null) {
                    outerFinallyNode = c.node;
                }
            }
            if (outerFinallyNode != null) {
                if (outerFinallyNode.op == FINALLY) {
                    ((FinallyStatement)outerFinallyNode).needReturnSlot = true;
                } else {
                    ((SynchronizedStatement)outerFinallyNode).needReturnSlot = true;
                }
            }
        }
        return DEAD_END;
    }


    /**
     * Inline
     */
    public Statement inline(Environment env, Context ctx) {
        if (expr != null) {
            expr = expr.inlineValue(env, ctx);
        }
        return this;
    }

    /**
     * The cost of inlining this statement
     */
    public int costInline(int thresh, Environment env, Context ctx) {
        return 1 + ((expr != null) ? expr.costInline(thresh, env, ctx) : 0);
    }

    /**
     * Create a copy of the statement for method inlining
     */
    public Statement copyInline(Context ctx, boolean valNeeded) {
        Expression e = (expr != null) ? expr.copyInline(ctx) : null;
        if ((!valNeeded) && (e != null)) {
            Statement body[] = {
                new ExpressionStatement(where, e),
                new InlineReturnStatement(where, null)
            };
            return new CompoundStatement(where, body);
        }
        return new InlineReturnStatement(where, e);
    }

    /**
     * Code
     */
    public void code(Environment env, Context ctx, Assembler asm) {
        if (expr == null) {
            codeFinally(env, ctx, asm, null, null);
            asm.add(where, opc_return);
        } else {
            expr.codeValue(env, ctx, asm);
            codeFinally(env, ctx, asm, null, expr.type);
            asm.add(where, opc_ireturn + expr.type.getTypeCodeOffset());
        }
    }

    /**
     * Print
     */
    public void print(PrintStream out, int indent) {
        super.print(out, indent);
        out.print("return");
        if (expr != null) {
            out.print(" ");
            expr.print(out);
        }
        out.print(";");
    }
}
