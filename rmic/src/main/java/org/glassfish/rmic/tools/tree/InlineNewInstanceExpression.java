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
import org.glassfish.rmic.tools.asm.Label;
import org.glassfish.rmic.tools.asm.Assembler;
import java.io.PrintStream;
import java.util.Vector;

/**
 * WARNING: The contents of this source file are not part of any
 * supported API.  Code that depends on them does so at its own risk:
 * they are subject to change or removal without notice.
 */
public
class InlineNewInstanceExpression extends Expression {
    MemberDefinition field;
    Statement body;

    /**
     * Constructor
     */
    InlineNewInstanceExpression(long where, Type type, MemberDefinition field, Statement body) {
        super(INLINENEWINSTANCE, where, type);
        this.field = field;
        this.body = body;
    }
    /**
     * Inline
     */
    public Expression inline(Environment env, Context ctx) {
        return inlineValue(env, ctx);
    }
    public Expression inlineValue(Environment env, Context ctx) {
        if (body != null) {
            LocalMember v = (LocalMember)field.getArguments().elementAt(0);
            Context newctx = new Context(ctx, this);
            newctx.declare(env, v);
            body = body.inline(env, newctx);
        }
        if ((body != null) && (body.op == INLINERETURN)) {
            body = null;
        }
        return this;
    }

    /**
     * Create a copy of the expression for method inlining
     */
    public Expression copyInline(Context ctx) {
        InlineNewInstanceExpression e = (InlineNewInstanceExpression)clone();
        e.body = body.copyInline(ctx, true);
        return e;
    }

    /**
     * Code
     */
    public void code(Environment env, Context ctx, Assembler asm) {
        codeCommon(env, ctx, asm, false);
    }
    public void codeValue(Environment env, Context ctx, Assembler asm) {
        codeCommon(env, ctx, asm, true);
    }
    private void codeCommon(Environment env, Context ctx, Assembler asm,
                            boolean forValue) {
        asm.add(where, opc_new, field.getClassDeclaration());
        if (body != null) {
            LocalMember v = (LocalMember)field.getArguments().elementAt(0);
            CodeContext newctx = new CodeContext(ctx, this);
            newctx.declare(env, v);
            asm.add(where, opc_astore, v.number);
            body.code(env, newctx, asm);
            asm.add(newctx.breakLabel);
            if (forValue) {
                asm.add(where, opc_aload, v.number);
            }
        }
    }

    /**
     * Print
     */
    public void print(PrintStream out) {
        LocalMember v = (LocalMember)field.getArguments().elementAt(0);
        out.println("(" + opNames[op] + "#" + v.hashCode() + "=" + field.hashCode());
        if (body != null) {
            body.print(out, 1);
        } else {
            out.print("<empty>");
        }
        out.print(")");
    }
}
