/*
 * Copyright (c) 1994, 2004, 2018, Oracle and/or its affiliates. All rights reserved.
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

package org.glassfish.rmic.tools.javac;

import org.glassfish.rmic.tools.java.*;
// JCOV
// end JCOV

import java.util.*;
import java.text.MessageFormat;

/**
 * Main program of the Java compiler
 *
 * WARNING: The contents of this source file are not part of any
 * supported API.  Code that depends on them does so at its own risk:
 * they are subject to change or removal without notice.
 *
 * @deprecated As of J2SE 1.3, the preferred way to compile Java
 * language sources is by using the new compiler,
 * com.org.glassfish.rmic.tools.javac.Main.
 */
@Deprecated
public
class Main implements Constants {

    private static ResourceBundle messageRB;

    /**
     * Initialize ResourceBundle
     */
    private static void initResource() {
        try {
            messageRB =
                ResourceBundle.getBundle("org.glassfish.rmic.tools.javac.resources.javac");
        } catch (MissingResourceException e) {
            throw new Error("Fatal: Resource for javac is missing");
        }
    }

    public static String getText(String key, String fixed) {
        return getText(key, fixed, null);
    }

    static String getText(String key, String fixed1, String fixed2) {
        return getText(key, fixed1, fixed2, null);
    }

    static String getText(String key, String fixed1,
                          String fixed2, String fixed3) {
        if (messageRB == null) {
            initResource();
        }
        try {
            String message = messageRB.getString(key);
            return MessageFormat.format(message, fixed1, fixed2, fixed3);
        } catch (MissingResourceException e) {
            if (fixed1 == null)  fixed1 = "null";
            if (fixed2 == null)  fixed2 = "null";
            if (fixed3 == null)  fixed3 = "null";
            String message = "JAVAC MESSAGE FILE IS BROKEN: key={0}, arguments={1}, {2}, {3}";
            return MessageFormat.format(message, key, fixed1, fixed2, fixed3);
        }
    }

}
