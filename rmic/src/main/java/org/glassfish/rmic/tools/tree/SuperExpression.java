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
class SuperExpression extends ThisExpression {

    /**
     * Constructor
     */
    public SuperExpression(long where) {
        super(SUPER, where);
    }

    /**
     * Constructor for "outer.super()"
     */
    public SuperExpression(long where, Expression outerArg) {
        super(where, outerArg);
        op = SUPER;
    }

    public SuperExpression(long where, Context ctx) {
        super(where, ctx);
        op = SUPER;
    }

    /**
     * Check expression
     */
    public Vset checkValue(Environment env, Context ctx, Vset vset, Hashtable<Object, Object> exp) {
        vset = checkCommon(env, ctx, vset, exp);
        if (type != Type.tError) {
            // "super" is not allowed in this context:
            env.error(where, "undef.var.super", idSuper);
        }
        return vset;
    }

    /**
     * Check if the present name is part of a scoping prefix.
     */
    public Vset checkAmbigName(Environment env, Context ctx,
                               Vset vset, Hashtable<Object, Object> exp,
                               UnaryExpression loc) {
        return checkCommon(env, ctx, vset, exp);
    }

    /** Common code for checkValue and checkAmbigName */
    private Vset checkCommon(Environment env, Context ctx, Vset vset, Hashtable<Object, Object> exp) {
        ClassDeclaration superClass = ctx.field.getClassDefinition().getSuperClass();
        if (superClass == null) {
            env.error(where, "undef.var", idSuper);
            type = Type.tError;
            return vset;
        }
        vset = super.checkValue(env, ctx, vset, exp);
        type = superClass.getType();
        return vset;
    }

}
