/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright (c) 2000-2018 Oracle and/or its affiliates. All rights reserved.
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

package org.glassfish.rmic.iiop;

import org.glassfish.rmic.tools.java.ClassFile;
import org.glassfish.rmic.tools.java.ClassPath;

import java.io.DataInputStream;
import java.io.File;
import java.io.IOException;

/**
 * A ClassLoader that will ultimately use a given org.glassfish.rmic.tools.java.ClassPath to
 * find the desired file.  This works for any JAR files specified in the given
 * ClassPath as well -- reusing all of that wonderful org.glassfish.rmic.tools.java code.
 *
 *@author Everett Anderson
 */
public class ClassPathLoader extends ClassLoader
{
    private ClassPath classPath;

    public ClassPathLoader(ClassPath classPath) {
        this.classPath = classPath;
    }

    // Called by the super class
    protected Class findClass(String name) throws ClassNotFoundException
    {
        byte[] b = loadClassData(name);
        return defineClass(name, b, 0, b.length);
    }

    /**
     * Load the class with the given fully qualified name from the ClassPath.
     */
    private byte[] loadClassData(String className)
        throws ClassNotFoundException
    {
        // Build the file name and subdirectory from the
        // class name
        String filename = className.replace('.', File.separatorChar)
                          + ".class";

        // Have ClassPath find the file for us, and wrap it in a
        // ClassFile.  Note:  This is where it looks inside jar files that
        // are specified in the path.
        ClassFile classFile = classPath.getFile(filename);

        if (classFile != null) {

            // Provide the most specific reason for failure in addition
            // to ClassNotFound
            Exception reportedError = null;
            byte data[] = null;

            try {
                // ClassFile is beautiful because it shields us from
                // knowing if it's a separate file or an entry in a
                // jar file.
                DataInputStream input
                    = new DataInputStream(classFile.getInputStream());

                // Can't rely on input available() since it will be
                // something unusual if it's a jar file!  May need
                // to worry about a possible problem if someone
                // makes a jar file entry with a size greater than
                // max int.
                data = new byte[(int)classFile.length()];

                try {
                    input.readFully(data);
                } catch (IOException ex) {
                    // Something actually went wrong reading the file.  This
                    // is a real error so save it to report it.
                    data = null;
                    reportedError = ex;
                } finally {
                    // Just don't care if there's an exception on close!
                    // I hate that close can throw an IOException!
                    try { input.close(); } catch (IOException ex) {}
                }
            } catch (IOException ex) {
                // Couldn't get the input stream for the file.  This is
                // probably also a real error.
                reportedError = ex;
            }

            if (data == null)
                throw new ClassNotFoundException(className, reportedError);

            return data;
        }

        // Couldn't find the file in the class path.
        throw new ClassNotFoundException(className);
    }
}
