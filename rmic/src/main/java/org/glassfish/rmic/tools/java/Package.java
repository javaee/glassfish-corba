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

package org.glassfish.rmic.tools.java;

import java.io.File;

/**
 * This class is used to represent the classes in a package.
 *
 * WARNING: The contents of this source file are not part of any
 * supported API.  Code that depends on them does so at its own risk:
 * they are subject to change or removal without notice.
 */
public
class Package {
    /**
     * The path which we use to locate source files.
     */
    private final ClassPath sourcePath = new ClassPath("");

    /**
     * The path which we use to locate class (binary) files.
     */
    private ClassPath binaryPath;

    /**
     * The path name of the package.
     */
    private String pkg;

    /**
     * Create a package given a source path, binary path, and package
     * name.
     */
    public Package(ClassPath binaryPath, Identifier pkg) {
        if (pkg.isInner())
            pkg = Identifier.lookup(pkg.getQualifier(), pkg.getFlatName());
        this.binaryPath = binaryPath;
        this.pkg = pkg.toString().replace('.', File.separatorChar);
    }

    /**
     * Check if a class is defined in this package.
     * (If it is an inner class name, it is assumed to exist
     * only if its binary file exists.  This is somewhat pessimistic.)
     */
    public boolean classExists(Identifier className) {
        return getBinaryFile(className) != null ||
                !className.isInner() &&
               getSourceFile(className) != null;
    }

    /**
     * Check if the package exists
     */
    public boolean exists() {
        // Look for the directory on our binary path.
        ClassFile dir = binaryPath.getDirectory(pkg);
        if (dir != null && dir.isDirectory()) {
            return true;
        }

        /* Accommodate ZIP files without CEN entries for directories
         * (packages): look on class path for at least one binary
         * file or one source file with the right package prefix
         */
        String prefix = pkg + File.separator;

        return binaryPath.getFiles(prefix, ".class").hasMoreElements();
    }

    private String makeName(String fileName) {
        return pkg.equals("") ? fileName : pkg + File.separator + fileName;
    }

    /**
     * Get the .class file of a class
     */
    public ClassFile getBinaryFile(Identifier className) {
        className = Type.mangleInnerType(className);
        String fileName = className.toString() + ".class";
        return binaryPath.getFile(makeName(fileName));
    }

    /**
     * Get the .java file of a class
     */
    public ClassFile getSourceFile(Identifier className) {
        // The source file of an inner class is that of its outer class.
        className = className.getTopName();
        String fileName = className.toString() + ".java";
        return sourcePath.getFile(makeName(fileName));
    }

    public ClassFile getSourceFile(String fileName) {
        if (fileName.endsWith(".java")) {
            return sourcePath.getFile(makeName(fileName));
        }
        return null;
    }

    public String toString() {
        if (pkg.equals("")) {
            return "unnamed package";
        }
        return "package " + pkg;
    }
}
