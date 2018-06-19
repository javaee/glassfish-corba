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
import org.glassfish.rmic.tools.asm.SwitchData;
import java.io.PrintStream;
import java.util.Hashtable;

/**
 * WARNING: The contents of this source file are not part of any
 * supported API.  Code that depends on them does so at its own risk:
 * they are subject to change or removal without notice.
 */
public
class SwitchStatement extends Statement {
    Expression expr;
    Statement args[];

    /**
     * Constructor
     */
    public SwitchStatement(long where, Expression expr, Statement args[]) {
        super(SWITCH, where);
        this.expr = expr;
        this.args = args;
    }

    /**
     * Check statement
     */
    Vset check(Environment env, Context ctx, Vset vset, Hashtable<Object, Object> exp) {
        checkLabel(env, ctx);
        CheckContext newctx = new CheckContext(ctx, this);
        vset = expr.checkValue(env, newctx, reach(env, vset), exp);
        Type switchType = expr.type;

        expr = convert(env, newctx, Type.tInt, expr);

        Hashtable<Expression, Statement> tab = new Hashtable<>();
        boolean hasDefault = false;
        // Note that vs is reset to vset.copy() on every case label.
        // If the first substatement is not a case label, it is unreached.
        Vset vs = DEAD_END;

        for (int i = 0 ; i < args.length ; i++) {
            Statement s = args[i];

            if (s.op == CASE) {

                vs = s.check(env, newctx, vs.join(vset.copy()), exp);

                Expression lbl = ((CaseStatement)s).expr;
                if (lbl != null) {
                    if (lbl instanceof IntegerExpression) {
                        Integer Ivalue =
                            (Integer)(((IntegerExpression)lbl).getValue());
                        int ivalue = Ivalue.intValue();
                        if (tab.get(lbl) != null) {
                            env.error(s.where, "duplicate.label", Ivalue);
                        } else {
                            tab.put(lbl, s);
                            boolean overflow;
                            switch (switchType.getTypeCode()) {
                                case TC_BYTE:
                                    overflow = (ivalue != (byte)ivalue); break;
                                case TC_SHORT:
                                    overflow = (ivalue != (short)ivalue); break;
                                case TC_CHAR:
                                    overflow = (ivalue != (char)ivalue); break;
                                default:
                                    overflow = false;
                            }
                            if (overflow) {
                                env.error(s.where, "switch.overflow",
                                          Ivalue, switchType);
                            }
                        }
                    } else {
                        // Suppose a class got an error early on during
                        // checking.  It will set all of its members to
                        // have the status "ERROR".  Now suppose that a
                        // case label refers to one of this class's
                        // fields.  When we check the case label, the
                        // compiler will try to inline the FieldExpression.
                        // Since the expression has ERROR status, it doesn't
                        // inline.  This means that instead of the case
                        // label being an IntegerExpression, it will still
                        // be a FieldExpression, and we will end up in this
                        // else block.  So, before we just assume that
                        // the expression isn't constant, do a check to
                        // see if it was constant but unable to inline.
                        // This eliminates some spurious error messages.
                        // (Bug id 4067498).
                        if (!lbl.isConstant() ||
                            lbl.getType() != Type.tInt) {
                            env.error(s.where, "const.expr.required");
                        }
                    }
                } else {
                    if (hasDefault) {
                        env.error(s.where, "duplicate.default");
                    }
                    hasDefault = true;
                }
            } else {
                vs = s.checkBlockStatement(env, newctx, vs, exp);
            }
        }
        if (!vs.isDeadEnd()) {
            newctx.vsBreak = newctx.vsBreak.join(vs);
        }
        if (hasDefault)
            vset = newctx.vsBreak;
        return ctx.removeAdditionalVars(vset);
    }

    /**
     * Inline
     */
    public Statement inline(Environment env, Context ctx) {
        ctx = new Context(ctx, this);
        expr = expr.inlineValue(env, ctx);
        for (int i = 0 ; i < args.length ; i++) {
            if (args[i] != null) {
                args[i] = args[i].inline(env, ctx);
            }
        }
        return this;
    }

    /**
     * Create a copy of the statement for method inlining
     */
    public Statement copyInline(Context ctx, boolean valNeeded) {
        SwitchStatement s = (SwitchStatement)clone();
        s.expr = expr.copyInline(ctx);
        s.args = new Statement[args.length];
        for (int i = 0 ; i < args.length ; i++) {
            if (args[i] != null) {
                s.args[i] = args[i].copyInline(ctx, valNeeded);
            }
        }
        return s;
    }

    /**
     * The cost of inlining this statement
     */
    public int costInline(int thresh, Environment env, Context ctx) {
        int cost = expr.costInline(thresh, env, ctx);
        for (int i = 0 ; (i < args.length) && (cost < thresh) ; i++) {
            if (args[i] != null) {
                cost += args[i].costInline(thresh, env, ctx);
            }
        }
        return cost;
    }

    /**
     * Code
     */
    public void code(Environment env, Context ctx, Assembler asm) {
        CodeContext newctx = new CodeContext(ctx, this);

        expr.codeValue(env, newctx, asm);

        SwitchData sw = new SwitchData();
        boolean hasDefault = false;

        for (int i = 0 ; i < args.length ; i++) {
            Statement s = args[i];
            if ((s != null) && (s.op == CASE)) {
                Expression e = ((CaseStatement)s).expr;
                if (e != null) {
                    sw.add(((IntegerExpression)e).value, new Label());
                }
// JCOV
                else {
                    hasDefault = true;
                }
// end JCOV
            }
        }

// JCOV
        if (env.coverage())
            sw.initTableCase();
// end JCOV
        asm.add(where, opc_tableswitch, sw);

        for (int i = 0 ; i < args.length ; i++) {
            Statement s = args[i];
            if (s != null) {
                if (s.op == CASE) {
                    Expression e = ((CaseStatement)s).expr;
                    if (e != null) {
                        asm.add(sw.get(((IntegerExpression)e).value));
// JCOV
                        sw.addTableCase(((IntegerExpression)e).value, s.where);
// end JCOV
                    } else {
                        asm.add(sw.getDefaultLabel());
// JCOV
                        sw.addTableDefault(s.where);
// end JCOV
/* JCOV                 hasDefault = true;   end JCOV */
                    }
                } else {
                    s.code(env, newctx, asm);
                }
            }
        }

        if (!hasDefault) {
            asm.add(sw.getDefaultLabel());
        }
        asm.add(newctx.breakLabel);
    }

    /**
     * Print
     */
    public void print(PrintStream out, int indent) {
        super.print(out, indent);
        out.print("switch (");
        expr.print(out);
        out.print(") {\n");
        for (int i = 0 ; i < args.length ; i++) {
            if (args[i] != null) {
                printIndent(out, indent + 1);
                args[i].print(out, indent + 1);
                out.print("\n");
            }
        }
        printIndent(out, indent);
        out.print("}");
    }
}
