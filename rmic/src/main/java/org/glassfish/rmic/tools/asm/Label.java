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

package org.glassfish.rmic.tools.asm;

import org.glassfish.rmic.tools.java.MemberDefinition;
import java.io.OutputStream;

/**
 * A label instruction. This is a 0 size instruction.
 * It is the only valid target of a branch instruction.
 *
 * WARNING: The contents of this source file are not part of any
 * supported API.  Code that depends on them does so at its own risk:
 * they are subject to change or removal without notice.
 */
public final
class Label extends Instruction {
    static int labelCount = 0;
    int ID;
    int depth;
    MemberDefinition locals[];

    /**
     * Constructor
     */
    public Label() {
        super(0, opc_label, null);
        this.ID = ++labelCount;
    }

    /**
     * Get the final destination, eliminate jumps gotos, and jumps to
     * labels that are immediately folowed by another label. The depth
     * field is used to leave bread crumbs to avoid infinite loops.
     */
    Label getDestination() {
        Label lbl = this;
        if ((next != null) && (next != this) && (depth == 0)) {
            depth = 1;

            switch (next.opc) {
              case opc_label:
                lbl = ((Label)next).getDestination();
                break;

              case opc_goto:
                lbl = ((Label)next.value).getDestination();
                break;

              case opc_ldc:
              case opc_ldc_w:
                if (next.value instanceof Integer) {
                    Instruction inst = next.next;
                    if (inst.opc == opc_label) {
                        inst = ((Label)inst).getDestination().next;
                    }

                    if (inst.opc == opc_ifeq) {
                        if (((Integer)next.value).intValue() == 0) {
                            lbl = (Label)inst.value;
                        } else {
                            lbl = new Label();
                            lbl.next = inst.next;
                            inst.next = lbl;
                        }
                        lbl = lbl.getDestination();
                        break;
                    }
                    if (inst.opc == opc_ifne) {
                        if (((Integer)next.value).intValue() == 0) {
                            lbl = new Label();
                            lbl.next = inst.next;
                            inst.next = lbl;
                        } else {
                            lbl = (Label)inst.value;
                        }
                        lbl = lbl.getDestination();
                        break;
                    }
                }
                break;
            }
            depth = 0;
        }
        return lbl;
    }

    public String toString() {
        String s = "$" + ID + ":";
        if (value != null)
            s = s + " stack=" + value;
        return s;
    }
}
