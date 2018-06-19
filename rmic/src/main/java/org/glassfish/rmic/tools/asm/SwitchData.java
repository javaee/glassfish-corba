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
import java.util.Hashtable;
import java.util.Enumeration;
import java.util.Arrays;

/**
 * WARNING: The contents of this source file are not part of any
 * supported API.  Code that depends on them does so at its own risk:
 * they are subject to change or removal without notice.
 */
public final
class SwitchData {
    int minValue, maxValue;
    Label defaultLabel = new Label();
    Hashtable<Integer, Label> tab = new Hashtable<>();
// JCOV
    Hashtable<Integer, Long> whereCaseTab = null;
// end JCOV

    /**
     * Get a label
     */
    public Label get(int n) {
        return tab.get(n);
    }

    /**
     * Get a label
     */
    public Label get(Integer n) {
        return tab.get(n);
    }

    /**
     * Add a label
     */
    public void add(int n, Label lbl) {
        if (tab.size() == 0) {
            minValue = n;
            maxValue = n;
        } else {
            if (n < minValue) {
                minValue = n;
            }
            if (n > maxValue) {
                maxValue = n;
            }
        }
        tab.put(Integer.valueOf(n), lbl);
    }

    /**
     * Get the default label
     */
    public Label getDefaultLabel() {
        return defaultLabel;
    }

    /**
     * Return the keys of this enumaration sorted in ascending order
     */
    public synchronized Enumeration<Integer> sortedKeys() {
        return new SwitchDataEnumeration(tab);
    }

// JCOV
    public void initTableCase() {
        whereCaseTab = new Hashtable<Integer, Long>();
    }
    public void addTableCase(int index, long where) {
        if (whereCaseTab != null)
            whereCaseTab.put(Integer.valueOf(index), Long.valueOf(where));
    }
    // this puts String key into Hashtable<Integer, Long>
    @SuppressWarnings("unchecked")
    public void addTableDefault(long where) {
        if (whereCaseTab != null)
            ((Hashtable)whereCaseTab).put("default", Long.valueOf(where));
    }
    public long whereCase(Object key) {
        Long i = whereCaseTab.get(key);
        return (i == null) ? 0L : i.longValue();
    }
    public boolean getDefault() {
         return (whereCase("default") != 0L);
    }
// end JCOV
}

class SwitchDataEnumeration implements Enumeration<Integer> {
    private Integer table[];
    private int current_index = 0;

    /**
     * Create a new enumeration from the hashtable.  Each key in the
     * hash table will be an Integer, with the value being a label.  The
     * enumeration returns the keys in sorted order.
     */
    SwitchDataEnumeration(Hashtable<Integer, Label> tab) {
        table = new Integer[tab.size()];
        int i = 0;
        for (Enumeration<Integer> e = tab.keys() ; e.hasMoreElements() ; ) {
            table[i++] = e.nextElement();
        }
        Arrays.sort(table);
        current_index = 0;
    }

    /**
     * Are there more keys to return?
     */
    public boolean hasMoreElements() {
        return current_index < table.length;
    }

    /**
     * Return the next key.
     */
    public Integer nextElement() {
        return table[current_index++];
    }
}
