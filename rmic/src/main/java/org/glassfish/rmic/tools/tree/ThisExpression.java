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
import java.io.PrintStream;
import java.util.Hashtable;

/**
 * WARNING: The contents of this source file are not part of any
 * supported API.  Code that depends on them does so at its own risk:
 * they are subject to change or removal without notice.
 */
public
class ThisExpression extends Expression {
    LocalMember field;
    Expression implementation;
    Expression outerArg;

    /**
     * Constructor
     */
    public ThisExpression(long where) {
        super(THIS, where, Type.tObject);
    }
    protected ThisExpression(int op, long where) {
        super(op, where, Type.tObject);
    }
    public ThisExpression(long where, LocalMember field) {
        super(THIS, where, Type.tObject);
        this.field = field;
        field.readcount++;
    }
    public ThisExpression(long where, Context ctx) {
        super(THIS, where, Type.tObject);
        field = ctx.getLocalField(idThis);
        field.readcount++;
    }

    /**
     * Constructor for "x.this()"
     */
    public ThisExpression(long where, Expression outerArg) {
        this(where);
        this.outerArg = outerArg;
    }

    public Expression getImplementation() {
        if (implementation != null)
            return implementation;
        return this;
    }

    /**
     * From the 'this' in an expression of the form outer.this(...),
     * or the 'super' in an expression of the form outer.super(...),
     * return the "outer" expression, or null if there is none.
     */
    public Expression getOuterArg() {
        return outerArg;
    }

    /**
     * Check expression
     */
    public Vset checkValue(Environment env, Context ctx, Vset vset, Hashtable<Object, Object> exp) {
        if (ctx.field.isStatic()) {
            env.error(where, "undef.var", opNames[op]);
            type = Type.tError;
            return vset;
        }
        if (field == null) {
            field = ctx.getLocalField(idThis);
            field.readcount++;
        }
        if (field.scopeNumber < ctx.frameNumber) {
            // get a "this$C" copy via the current object
            implementation = ctx.makeReference(env, field);
        }
        if (!vset.testVar(field.number)) {
            env.error(where, "access.inst.before.super", opNames[op]);
        }
        if (field == null) {
            type = ctx.field.getClassDeclaration().getType();
        } else {
            type = field.getType();
        }
        return vset;
    }

    public boolean isNonNull() {
        return true;
    }

    // A 'ThisExpression' node can never appear on the LHS of an assignment in a correct
    // program, but handle this case anyhow to provide a safe error recovery.

    public FieldUpdater getAssigner(Environment env, Context ctx) {
        return null;
    }

    public FieldUpdater getUpdater(Environment env, Context ctx) {
        return null;
    }

    /**
     * Inline
     */
    public Expression inlineValue(Environment env, Context ctx) {
        if (implementation != null)
            return implementation.inlineValue(env, ctx);
        if (field != null && field.isInlineable(env, false)) {
            Expression e = (Expression)field.getValue(env);
            //System.out.println("INLINE = "+ e + ", THIS");
            if (e != null) {
                e = e.copyInline(ctx);
                e.type = type;  // in case op==SUPER
                return e;
            }
        }
        return this;
    }

    /**
     * Create a copy of the expression for method inlining
     */
    public Expression copyInline(Context ctx) {
        if (implementation != null)
            return implementation.copyInline(ctx);
        ThisExpression e = (ThisExpression)clone();
        if (field == null) {
            // The expression is copied into the context of a method
            e.field = ctx.getLocalField(idThis);
            e.field.readcount++;
        } else {
            e.field = field.getCurrentInlineCopy(ctx);
        }
        if (outerArg != null) {
            e.outerArg = outerArg.copyInline(ctx);
        }
        return e;
    }

    /**
     * Code
     */
    public void codeValue(Environment env, Context ctx, Assembler asm) {
        asm.add(where, opc_aload, field.number);
    }

    /**
     * Print
     */
    public void print(PrintStream out) {
        if (outerArg != null) {
            out.print("(outer=");
            outerArg.print(out);
            out.print(" ");
        }
        String pfx = (field == null) ? ""
            : field.getClassDefinition().getName().getFlatName().getName()+".";
        pfx += opNames[op];
        out.print(pfx + "#" + ((field != null) ? field.hashCode() : 0));
        if (outerArg != null)
            out.print(")");
    }
}
