/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright (c) 1995-2018 Oracle and/or its affiliates. All rights reserved.
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

package org.glassfish.rmic.tools.asm;

import org.glassfish.rmic.tools.java.*;
import java.io.IOException;
import java.io.DataOutputStream;

/**
 * This class is used to assemble the local variable table.
 *
 * WARNING: The contents of this source file are not part of any
 * supported API.  Code that depends on them does so at its own risk:
 * they are subject to change or removal without notice.
 *
 * @author Arthur van Hoff
 */
final
class LocalVariableTable {
    LocalVariable locals[] = new LocalVariable[8];
    int len;

    /**
     * Define a new local variable. Merge entries where possible.
     */
    void define(MemberDefinition field, int slot, int from, int to) {
        if (from >= to) {
            return;
        }
        for (int i = 0 ; i < len ; i++) {
            if ((locals[i].field == field) && (locals[i].slot == slot) &&
                (from <= locals[i].to) && (to >= locals[i].from)) {
                locals[i].from = Math.min(locals[i].from, from);
                locals[i].to = Math.max(locals[i].to, to);
                return;
            }
        }
        if (len == locals.length) {
            LocalVariable newlocals[] = new LocalVariable[len * 2];
            System.arraycopy(locals, 0, newlocals, 0, len);
            locals = newlocals;
        }
        locals[len++] = new LocalVariable(field, slot, from, to);
    }

    /**
     * Trim overlapping local ranges.  Java forbids shadowing of
     * locals in nested scopes, but non-nested scopes may still declare
     * locals with the same name.  Because local variable ranges are
     * computed using flow analysis as part of assembly, it isn't
     * possible to simply make sure variable ranges end where the
     * enclosing lexical scope ends.  This method makes sure that
     * variables with the same name don't overlap, giving priority to
     * fields with higher slot numbers that should have appeared later
     * in the source.
     */
    private void trim_ranges() {
        for (int i=0; i<len; i++) {
            for (int j=i+1; j<len; j++) {
                if ((locals[i].field.getName()==locals[j].field.getName())
                        && (locals[i].from <= locals[j].to)
                        && (locals[i].to >= locals[j].from)) {
                    // At this point we know that both ranges are
                    // the same name and there is also overlap or they abut
                    if (locals[i].slot < locals[j].slot) {
                        if (locals[i].from < locals[j].from) {
                          locals[i].to = Math.min(locals[i].to, locals[j].from);
                        } else {
                          // We've detected two local variables with the
                          // same name, and the one with the greater slot
                          // number starts before the other.  This order
                          // reversal may happen with locals with the same
                          // name declared in both a try body and an
                          // associated catch clause.  This is rare, and
                          // we give up.
                        }
                    } else if (locals[i].slot > locals[j].slot) {
                        if (locals[i].from > locals[j].from) {
                          locals[j].to = Math.min(locals[j].to, locals[i].from);
                        } else {
                          // Same situation as above; just give up.
                        }
                    } else {
                        // This case can happen if there are two variables
                        // with the same name and slot numbers, and ranges
                        // that abut.  AFAIK the only way this can occur
                        // is with multiple static initializers.  Punt.
                    }
                }
            }
        }
    }

    /**
     * Write out the data.
     */
    void write(Environment env, DataOutputStream out, ConstantPool tab) throws IOException {
        trim_ranges();
        out.writeShort(len);
        for (int i = 0 ; i < len ; i++) {
            //System.out.println("pc=" + locals[i].from + ", len=" + (locals[i].to - locals[i].from) + ", nm=" + locals[i].field.getName() + ", slot=" + locals[i].slot);
            out.writeShort(locals[i].from);
            out.writeShort(locals[i].to - locals[i].from);
            out.writeShort(tab.index(locals[i].field.getName().toString()));
            out.writeShort(tab.index(locals[i].field.getType().getTypeSignature()));
            out.writeShort(locals[i].slot);
        }
    }
}
