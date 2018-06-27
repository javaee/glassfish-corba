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
import org.glassfish.rmic.tools.asm.Assembler;
import java.util.Hashtable;

/**
 * WARNING: The contents of this source file are not part of any
 * supported API.  Code that depends on them does so at its own risk:
 * they are subject to change or removal without notice.
 */
public
class DeclarationStatement extends Statement {
    int mod;
    Expression type;
    Statement args[];

    /**
     * Constructor
     */
    public DeclarationStatement(long where, int mod, Expression type, Statement args[]) {
        super(DECLARATION, where);
        this.mod = mod;
        this.type = type;
        this.args = args;
    }

    /**
     * Check statement
     * Report an error unless the call is checkBlockStatement.
     */
    Vset check(Environment env, Context ctx, Vset vset, Hashtable<Object, Object> exp) {
        env.error(where, "invalid.decl");
        return checkBlockStatement(env, ctx, vset, exp);
    }
    Vset checkBlockStatement(Environment env, Context ctx, Vset vset, Hashtable<Object, Object> exp) {
        if (labels != null) {
            env.error(where, "declaration.with.label", labels[0]);
        }
        vset = reach(env, vset);
        Type t = type.toType(env, ctx);

        for (int i = 0 ; i < args.length ; i++) {
            vset = args[i].checkDeclaration(env, ctx, vset, mod, t, exp);
        }

        return vset;
    }

    /**
     * Inline
     */
    public Statement inline(Environment env, Context ctx) {
        int n = 0;
        for (int i = 0 ; i < args.length ; i++) {
            if ((args[i] = args[i].inline(env, ctx)) != null) {
                n++;
            }
        }
        return (n == 0) ? null : this;
    }

    /**
     * Create a copy of the statement for method inlining
     */
    public Statement copyInline(Context ctx, boolean valNeeded) {
        DeclarationStatement s = (DeclarationStatement)clone();
        if (type != null) {
            s.type = type.copyInline(ctx);
        }
        s.args = new Statement[args.length];
        for (int i = 0; i < args.length; i++){
            if (args[i] != null){
                s.args[i] = args[i].copyInline(ctx, valNeeded);
            }
        }
        return s;
    }

    /**
     * The cost of inlining this statement
     */
    public int costInline(int thresh, Environment env, Context ctx) {
        int cost = 1;
        for (int i = 0; i < args.length; i++){
            if (args[i] != null){
                cost += args[i].costInline(thresh, env, ctx);
            }
        }
        return cost;
    }


    /**
     * Code
     */
    public void code(Environment env, Context ctx, Assembler asm) {
        for (int i = 0 ; i < args.length ; i++) {
            if (args[i] != null) {
                args[i].code(env, ctx, asm);
            }
        }
    }

    /**
     * Print
     */
    public void print(PrintStream out, int indent) {
        out.print("declare ");
        super.print(out, indent);
        type.print(out);
        out.print(" ");
        for (int i = 0 ; i < args.length ; i++) {
            if (i > 0) {
                out.print(", ");
            }
            if (args[i] != null)  {
                args[i].print(out);
            } else {
                out.print("<empty>");
            }
        }
        out.print(";");
    }
}
