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

import org.glassfish.rmic.tools.java.*;
import org.glassfish.rmic.tools.tree.StringExpression;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.Vector;
import java.io.IOException;
import java.io.DataOutputStream;

/**
 * A table of constants
 *
 * WARNING: The contents of this source file are not part of any
 * supported API.  Code that depends on them does so at its own risk:
 * they are subject to change or removal without notice.
 */
public final
class ConstantPool implements RuntimeConstants {
    Hashtable<Object, ConstantPoolData> hash = new Hashtable<>(101);

    /**
     * Find an entry, may return 0
     */
    public int index(Object obj) {
        return hash.get(obj).index;
    }

    /**
     * Add an entry
     */
    public void put(Object obj) {
        ConstantPoolData data = hash.get(obj);
        if (data == null) {
            if (obj instanceof String) {
                data = new StringConstantData(this, (String)obj);
            } else if (obj instanceof StringExpression) {
                data = new StringExpressionConstantData(this, (StringExpression)obj);
            } else if (obj instanceof ClassDeclaration) {
                data = new ClassConstantData(this, (ClassDeclaration)obj);
            } else if (obj instanceof Type) {
                data = new ClassConstantData(this, (Type)obj);
            } else if (obj instanceof MemberDefinition) {
                data = new FieldConstantData(this, (MemberDefinition)obj);
            } else if (obj instanceof NameAndTypeData) {
                data = new NameAndTypeConstantData(this, (NameAndTypeData)obj);
            } else if (obj instanceof Number) {
                data = new NumberConstantData(this, (Number)obj);
            }
            hash.put(obj, data);
        }
    }

    /**
     * Write to output
     */
    public void write(Environment env, DataOutputStream out) throws IOException {
        ConstantPoolData list[] = new ConstantPoolData[hash.size()];
        String keys[] = new String[list.length];
        int index = 1, count = 0;

        // Make a list of all the constant pool items
        for (int n = 0 ; n < 5 ; n++) {
            int first = count;
            for (Enumeration<ConstantPoolData> e = hash.elements() ; e.hasMoreElements() ;) {
                ConstantPoolData data = e.nextElement();
                if (data.order() == n) {
                    keys[count] = sortKey(data);
                    list[count++] = data;
                }
            }
            xsort(list, keys, first, count-1);
        }

        // Assign an index to each constant pool item
        for (int n = 0 ; n < list.length ; n++) {
            ConstantPoolData data = list[n];
            data.index = index;
            index += data.width();
        }

        // Write length
        out.writeShort(index);

        // Write each constant pool item
        for (int n = 0 ; n < count ; n++) {
            list[n].write(env, out, this);
        }
    }

    private
    static String sortKey(ConstantPoolData f) {
        if (f instanceof NumberConstantData) {
            Number num = ((NumberConstantData)f).num;
            String str = num.toString();
            int key = 3;
            if (num instanceof Integer)  key = 0;
            else if (num instanceof Float)  key = 1;
            else if (num instanceof Long)  key = 2;
            return "\0" + (char)(str.length() + key<<8) + str;
        }
        if (f instanceof StringExpressionConstantData)
            return (String)((StringExpressionConstantData)f).str.getValue();
        if (f instanceof FieldConstantData) {
            MemberDefinition fd = ((FieldConstantData)f).field;
            return fd.getName()+" "+fd.getType().getTypeSignature()
                +" "+fd.getClassDeclaration().getName();
        }
        if (f instanceof NameAndTypeConstantData)
            return  ((NameAndTypeConstantData)f).name+
                " "+((NameAndTypeConstantData)f).type;
        if (f instanceof ClassConstantData)
            return ((ClassConstantData)f).name;
        return ((StringConstantData)f).str;
    }

    /**
     * Quick sort an array of pool entries and a corresponding array of Strings
     * that are the sort keys for the field.
     */
    private
    static void xsort(ConstantPoolData ff[], String ss[], int left, int right) {
        if (left >= right)
            return;
        String pivot = ss[left];
        int l = left;
        int r = right;
        while (l < r) {
            while (l <= right && ss[l].compareTo(pivot) <= 0)
                l++;
            while (r >= left && ss[r].compareTo(pivot) > 0)
                r--;
            if (l < r) {
                // swap items at l and at r
                ConstantPoolData def = ff[l];
                String name = ss[l];
                ff[l] = ff[r]; ff[r] = def;
                ss[l] = ss[r]; ss[r] = name;
            }
        }
        int middle = r;
        // swap left and middle
        ConstantPoolData def = ff[left];
        String name = ss[left];
        ff[left] = ff[middle]; ff[middle] = def;
        ss[left] = ss[middle]; ss[middle] = name;
        xsort(ff, ss, left, middle-1);
        xsort(ff, ss, middle + 1, right);
    }

}
