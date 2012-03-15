/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright (c) 1997-2010 Oracle and/or its affiliates. All rights reserved.
 * 
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common Development
 * and Distribution License("CDDL") (collectively, the "License").  You
 * may not use this file except in compliance with the License.  You can
 * obtain a copy of the License at
 * https://glassfish.dev.java.net/public/CDDL+GPL_1_1.html
 * or packager/legal/LICENSE.txt.  See the License for the specific
 * language governing permissions and limitations under the License.
 * 
 * When distributing the software, include this License Header Notice in each
 * file and include the License file at glassfish/bootstrap/legal/LICENSE.txt.
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
//
// Created       : 2003 May 18 (Sun) 15:17:31 by Harold Carr.
// Last Modified : 2003 May 19 (Mon) 13:33:33 by Harold Carr.
//

package corba.islocal;

import java.util.Hashtable;
import java.util.StringTokenizer;
import java.util.Vector;

import corba.framework.Loader;

public class CustomClassLoader
    extends Loader
{
    private int indent = 0;
    private Hashtable classes = new Hashtable();
    private boolean debug = false;

    public CustomClassLoader()
    {
        addPaths("java.class.path");
    }

    private void addPaths(String propertyName)
    {
        StringTokenizer tokens =
            new StringTokenizer(System.getProperty(propertyName),
                                System.getProperty("path.separator"));
        while (tokens.hasMoreTokens()) {
            addPath(tokens.nextToken());
        }
    }

    protected synchronized Class loadClass(String name, boolean resolve)
        throws ClassNotFoundException
    {
        boolean errorFound = false;
        Class c = null;
        try {

            printIndent(indent++, ">> " + name);

            c = (Class) classes.get(name);
            if (c != null) {
                return c;
            }

            try {
                c = findClass(name);
            } catch (ClassNotFoundException e) {
                c = super.findSystemClass(name);
                return c;
            }
            if (resolve) {
                resolveClass(c);
            }
        } catch (Throwable t) {
            errorFound = true;
            printIndent(--indent, "<E " + name + " " + t.toString());
            if (t instanceof ClassNotFoundException) {
                throw (ClassNotFoundException) t;
            }
        } finally {
            if (! errorFound) {
                printIndent(--indent, "<< " + name);
            }
        }
        classes.put(name, c);
        return c;
    }

    private void printIndent(int amount, String msg)
    {
        if (debug) {
            for (int i = 0; i < amount; i++) {
                System.out.print(" ");
            }
            System.out.println(msg);
        }
    }
}

// End of file.
