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
class ConvertExpression extends UnaryExpression {
    /**
     * Constructor
     */
    public ConvertExpression(long where, Type type, Expression right) {
        super(CONVERT, where, type, right);
    }

    /**
     * Check the value
     */
    public Vset checkValue(Environment env, Context ctx, Vset vset, Hashtable<Object, Object> exp) {
        return right.checkValue(env, ctx, vset, exp);
    }

    /**
     * Simplify
     */
    Expression simplify() {
        switch (right.op) {
          case BYTEVAL:
          case CHARVAL:
          case SHORTVAL:
          case INTVAL: {
            int value = ((IntegerExpression)right).value;
            switch (type.getTypeCode()) {
              case TC_BYTE:     return new ByteExpression(right.where, (byte)value);
              case TC_CHAR:     return new CharExpression(right.where, (char)value);
              case TC_SHORT:    return new ShortExpression(right.where, (short)value);
              case TC_INT:      return new IntExpression(right.where, value);
              case TC_LONG:     return new LongExpression(right.where, (long)value);
              case TC_FLOAT:    return new FloatExpression(right.where, (float)value);
              case TC_DOUBLE:   return new DoubleExpression(right.where, (double)value);
            }
            break;
          }
          case LONGVAL: {
            long value = ((LongExpression)right).value;
            switch (type.getTypeCode()) {
              case TC_BYTE:     return new ByteExpression(right.where, (byte)value);
              case TC_CHAR:     return new CharExpression(right.where, (char)value);
              case TC_SHORT:    return new ShortExpression(right.where, (short)value);
              case TC_INT:      return new IntExpression(right.where, (int)value);
              case TC_FLOAT:    return new FloatExpression(right.where, (float)value);
              case TC_DOUBLE:   return new DoubleExpression(right.where, (double)value);
            }
            break;
          }
          case FLOATVAL: {
            float value = ((FloatExpression)right).value;
            switch (type.getTypeCode()) {
              case TC_BYTE:     return new ByteExpression(right.where, (byte)value);
              case TC_CHAR:     return new CharExpression(right.where, (char)value);
              case TC_SHORT:    return new ShortExpression(right.where, (short)value);
              case TC_INT:      return new IntExpression(right.where, (int)value);
              case TC_LONG:     return new LongExpression(right.where, (long)value);
              case TC_DOUBLE:   return new DoubleExpression(right.where, (double)value);
            }
            break;
          }
          case DOUBLEVAL: {
            double value = ((DoubleExpression)right).value;
            switch (type.getTypeCode()) {
              case TC_BYTE:     return new ByteExpression(right.where, (byte)value);
              case TC_CHAR:     return new CharExpression(right.where, (char)value);
              case TC_SHORT:    return new ShortExpression(right.where, (short)value);
              case TC_INT:      return new IntExpression(right.where, (int)value);
              case TC_LONG:     return new LongExpression(right.where, (long)value);
              case TC_FLOAT:    return new FloatExpression(right.where, (float)value);
            }
            break;
          }
        }
        return this;
    }

    /**
     * Check if the expression is equal to a value
     */
    public boolean equals(int i) {
        return right.equals(i);
    }
    public boolean equals(boolean b) {
        return right.equals(b);
    }

    /**
     * Inline
     */
    public Expression inline(Environment env, Context ctx) {
        // super.inline throws away the op.
        // This is sometimes incorrect, since casts can have side effects.
        if (right.type.inMask(TM_REFERENCE) && type.inMask(TM_REFERENCE)) {
            try {
                if (!env.implicitCast(right.type, type))
                    return inlineValue(env, ctx);
            } catch (ClassNotFound e) {
                throw new CompilerError(e);
            }
        }
        return super.inline(env, ctx);
    }

    /**
     * Code
     */
    public void codeValue(Environment env, Context ctx, Assembler asm) {
        right.codeValue(env, ctx, asm);
        codeConversion(env, ctx, asm, right.type, type);
    }

    /**
     * Print
     */
    public void print(PrintStream out) {
        out.print("(" + opNames[op] + " " + type.toString() + " ");
        right.print(out);
        out.print(")");
    }
}
