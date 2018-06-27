/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright (c) 1997-2018 Oracle and/or its affiliates. All rights reserved.
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

package corba.framework;

import java.io.*;
import java.util.*;

/**
 * Allows extra paths to be specified when searching for classes.  This
 * gets around the security restriction of changing the classpath within
 * the program.  Also, even if there isn't a security restriction, I
 * think the default ClassLoader only does a getProperty once for the
 * value, so changing it is useless.
 * <P>
 * This is currently only used by Internal and ThreadExec, which execute the
 * class in the current process.  For the external processes, the
 * classpath is augmented on the command line to include the output
 * directory.
 * <P>
 * This follows the delegation model for class loading:  If the class has
 * already been loaded, it returns it.  Next, the system loader is tried.
 * Finally, if the system loader fails, the extra paths are searched.
 */
public class Loader extends ClassLoader
{

    /**
     * Vector of extra paths to search.
     */
    private Vector extraPaths = new Vector(10);

    /**
     *
     * Default constructor.
     *
     */
    public Loader()
    {
    }

    /**
     * Constructor allowing a Vector of search paths to be specified.
     *
     *@param paths  Extra paths to search when loading classes
     *
     */
    public Loader(Vector paths)
    {
        extraPaths = paths;
    }

    /**
     * Add another path to search when loading classes.
     *
     *@param path New path to search
     */
    public void addPath(String path)
    {
        extraPaths.add(path);
    }

    /**
     * Try to load the specified class using the extra paths.  This is 
     * called by the parent loader once it has tried all other means
     * (such as checking for it being loaded, or using the system loader).
     *
     *@param name name of the class to load
     *@exception ClassNotFoundException couldn't find the class
     *@return loaded Class instance
     */
    protected Class findClass(String name) throws ClassNotFoundException 
    {
        byte[] b = loadClassData(name);
        return defineClass(name, b, 0, b.length);
    }

    /**
     * Find the class by searching the extra paths, and read it into
     * a byte array.
     *
     *@param className  Fully qualified class name
     *@return byte array containing the contents of the class file
     *@exception ClassNotFoundException error loading the class
     */
    private byte[] loadClassData(String className) 
        throws ClassNotFoundException
    {
        byte data[] = null;

        // Build the file name and subdirectory from the
        // class name
        String filename = className.replace('.', File.separatorChar) 
                          + ".class";

        Enumeration paths = extraPaths.elements();

        // Search the extra paths
        while (paths.hasMoreElements() && data == null) {

            File file = new File((String)paths.nextElement()
                                 + File.separator 
                                 + filename);

            if (!file.exists())
                continue;

            try {
                
                // Found the file, so open it for reading
                FileInputStream in = new FileInputStream(file);
                
                // Protect against data loss (shouldn't happen)
                if (file.length() > Integer.MAX_VALUE)
                    throw new IOException (className
                                         + " exceeds max length");

                data = new byte[(int)file.length()];
                
                // Read in the file contents
                if (in.read(data) != data.length)
                    throw new IOException ("Lost data when loading "
                                         + className);
                
                in.close();
                
            } catch (Exception ex) {
                throw new ClassNotFoundException(className, ex);
            }
        }

        if (data == null)
            throw new ClassNotFoundException(className);

        return data;
    }
}
        
        
