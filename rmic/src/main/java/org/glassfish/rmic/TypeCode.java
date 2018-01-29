package org.glassfish.rmic;
/*
 * Copyright (c) 2018, Oracle and/or its affiliates. All rights reserved.
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS FILE HEADER.
 *
 * This code is free software; you can redistribute it and/or modify it
 * under the terms of the GNU General Public License version 2 only, as
 * published by the Free Software Foundation.  Oracle designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Oracle in the LICENSE file that accompanied this code.
 *
 * This code is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
 * FITNESS FOR A PARTICULAR PURPOSE.  See the GNU General Public License
 * version 2 for more details (a copy is included in the LICENSE file that
 * accompanied this code).
 *
 * You should have received a copy of the GNU General Public License version
 * 2 along with this work; if not, write to the Free Software Foundation,
 * Inc., 51 Franklin St, Fifth Floor, Boston, MA 02110-1301 USA.
 *
 * Please contact Oracle, 500 Oracle Parkway, Redwood Shores, CA 94065 USA
 * or visit www.oracle.com if you need additional information or have any
 * questions.
 */

import static org.glassfish.rmic.tools.java.Constants.*;

public enum TypeCode {
    BOOLEAN(TC_BOOLEAN) {
        @Override
        public String format(String s) {
            return Boolean.toString(s.equals("1"));
        }
    },
    BYTE(TC_BYTE),
    CHAR(TC_CHAR) {
        @Override
        public String toValueString(Object value) {
            return value == null ? null : "L'" + asCharacter(value) + "'";
        }

        private String asCharacter(Object value) {
            return String.valueOf((char) ((Number) value).intValue());
        }
    },
    SHORT(TC_SHORT),
    INT(TC_INT),
    LONG(TC_LONG) {
        @Override
        public String format(String s) {
            return s + "L";
        }
    },
    FLOAT(TC_FLOAT) {
        @Override
        public String format(String s) {
            return s + "F";
        }
    },
    DOUBLE(TC_DOUBLE) {
        @Override
        public String format(String s) {
            return s + "D";
        }
    },
    NULL(TC_NULL),
    ARRAY(TC_ARRAY),
    CLASS(TC_CLASS),
    VOID(TC_VOID),
    METHOD(TC_METHOD),
    ERROR(TC_ERROR);

    private int tcCode;

    public int tcCode() {
        return tcCode;
    }

    TypeCode(int tcCode) {
        this.tcCode = tcCode;
    }

    String format(String s) {
        return s;
    }

    public String toValueString(Object value) {
        return value == null ? null : format(value.toString());
    }
}
