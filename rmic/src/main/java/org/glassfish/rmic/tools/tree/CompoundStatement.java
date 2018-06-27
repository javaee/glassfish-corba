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
class CompoundStatement extends Statement {
    Statement args[];

    /**
     * Constructor
     */
    public CompoundStatement(long where, Statement args[]) {
        super(STAT, where);
        this.args = args;
        // To avoid the need for subsequent null checks:
        for (int i = 0 ; i < args.length ; i++) {
            if (args[i] == null) {
                args[i] = new CompoundStatement(where, new Statement[0]);
            }
        }
    }

    /**
     * Insert a new statement at the front.
     * This is used to introduce an implicit super-class constructor call.
     */
    public void insertStatement(Statement s) {
        Statement newargs[] = new Statement[1+args.length];
        newargs[0] = s;
        for (int i = 0 ; i < args.length ; i++) {
            newargs[i+1] = args[i];
        }
        this.args = newargs;
    }

    /**
     * Check statement
     */
    Vset check(Environment env, Context ctx, Vset vset, Hashtable<Object, Object> exp) {
        checkLabel(env, ctx);
        if (args.length > 0) {
            vset = reach(env, vset);
            CheckContext newctx = new CheckContext(ctx, this);
            // In this environment, 'resolveName' will look for local classes.
            Environment newenv = Context.newEnvironment(env, newctx);
            for (int i = 0 ; i < args.length ; i++) {
                vset = args[i].checkBlockStatement(newenv, newctx, vset, exp);
            }
            vset = vset.join(newctx.vsBreak);
        }
        return ctx.removeAdditionalVars(vset);
    }

    /**
     * Inline
     */
    public Statement inline(Environment env, Context ctx) {
        ctx = new Context(ctx, this);
        boolean expand = false;
        int count = 0;
        for (int i = 0 ; i < args.length ; i++) {
            Statement s = args[i];
            if (s != null) {
                if ((s = s.inline(env, ctx)) != null) {
                    if ((s.op == STAT) && (s.labels == null)) {
                        count += ((CompoundStatement)s).args.length;
                    } else {
                        count++;
                    }
                    expand = true;
                }
                args[i] = s;
            }
        }
        switch (count) {
          case 0:
            return null;

          case 1:
            for (int i = args.length ; i-- > 0 ;) {
                if (args[i] != null) {
                    return eliminate(env, args[i]);
                }
            }
            break;
        }
        if (expand || (count != args.length)) {
            Statement newArgs[] = new Statement[count];
            for (int i = args.length ; i-- > 0 ;) {
                Statement s = args[i];
                if (s != null) {
                    if ((s.op == STAT) && (s.labels == null)) {
                        Statement a[] = ((CompoundStatement)s).args;
                        for (int j = a.length ; j-- > 0 ; ) {
                            newArgs[--count] = a[j];
                        }
                    } else {
                        newArgs[--count] = s;
                    }
                }
            }
            args = newArgs;
        }
        return this;
    }

    /**
     * Create a copy of the statement for method inlining
     */
    public Statement copyInline(Context ctx, boolean valNeeded) {
        CompoundStatement s = (CompoundStatement)clone();
        s.args = new Statement[args.length];
        for (int i = 0 ; i < args.length ; i++) {
            s.args[i] = args[i].copyInline(ctx, valNeeded);
        }
        return s;
    }

    /**
     * The cost of inlining this statement
     */
    public int costInline(int thresh, Environment env, Context ctx) {
        int cost = 0;
        for (int i = 0 ; (i < args.length) && (cost < thresh) ; i++) {
            cost += args[i].costInline(thresh, env, ctx);
        }
        return cost;
    }

    /**
     * Code
     */
    public void code(Environment env, Context ctx, Assembler asm) {
        CodeContext newctx = new CodeContext(ctx, this);
        for (int i = 0 ; i < args.length ; i++) {
            args[i].code(env, newctx, asm);
        }
        asm.add(newctx.breakLabel);
    }

    /**
     * Check if the first thing is a constructor invocation
     */
    public Expression firstConstructor() {
        return (args.length > 0) ? args[0].firstConstructor() : null;
    }

    /**
     * Print
     */
    public void print(PrintStream out, int indent) {
        super.print(out, indent);
        out.print("{\n");
        for (int i = 0 ; i < args.length ; i++) {
            printIndent(out, indent+1);
            if (args[i] != null) {
                args[i].print(out, indent + 1);
            } else {
                out.print("<empty>");
            }
            out.print("\n");
        }
        printIndent(out, indent);
        out.print("}");
    }
}
