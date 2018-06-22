/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright (c) 1997-2018 Oracle and/or its affiliates. All rights reserved.
 * Copyright (c) 1998-1999 IBM Corp. All rights reserved.
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

package rmic;

import java.util.Enumeration;
import java.util.Vector;
import java.util.Hashtable;
import java.io.File;
import java.io.IOException;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;
import java.util.zip.ZipException;
import java.util.Comparator;
import java.util.Arrays;

/**
 * This class provides static methods to enumerate all classes in
 * a specified class path.
 */
public class ClassEnumerator {

    /**
     * Get classes using the specified path string.
     */
    public static Vector getClasses (String pathstr, boolean sort) {
        Vector list = new Vector(4096);
        Hashtable roots = new Hashtable(20);
        Object nullValue = new Object();
        ClassPathEntry[] path = null;

        try {
            path = parsePath(pathstr);
            for (int i = path.length; --i >= 0; ) {
                if (path[i].zip != null) {
                    Enumeration e = path[i].zip.entries();
                    while (e.hasMoreElements()) {
                        ZipEntry entry = (ZipEntry)e.nextElement();
                        String name = entry.getName();
                        int index = name.indexOf(".class");
                        if (index >=0) {
                            name = name.replace('/','.').substring(0,index);
                            list.addElement(name);
                        }
                    }
                } else {

                    // Just gather the unique root directories...

                    File rootDir = path[i].dir;
                    if (rootDir.getPath().equals(".")) {
                        rootDir = new File(System.getProperty("user.dir"));
                    }

                    // _REVISIT_ Forcing lower case here could cause us to skip a
                    //           real root (if actually different case); however,
                    //           if we don't do this, the "user.dir" property can
                    //           screw us up by being in different case!

                    String pathName = rootDir.getPath().toLowerCase();

                    if (!roots.containsKey(pathName)) {
                        roots.put(pathName,rootDir);
                    }
                }
            }

            // Process the root directories...

            for (Enumeration e = roots.keys(); e.hasMoreElements() ;) {
                File rootDir = (File) roots.get(e.nextElement());
                int rootLen = rootDir.getPath().length() + 1;
                addClasses(rootLen,rootDir,list,roots);
            }

            // Release resources...

        } finally {

            if (path != null) {
                for (int i = path.length; --i >= 0; ) {
                    if (path[i].zip != null) {
                        try {
                            path[i].zip.close();
                        } catch (IOException e) {}
                    }
                }
            }
        }

        // Sort it if we're supposed to...

        if (sort) {
            int size = list.size();
            String[] temp = new String[size];
            list.copyInto(temp);
            Arrays.sort(temp,new StringComparator());
            list = new Vector(size);
            for (int i = 0; i < size; i++) {
                list.addElement(temp[i]);
            }
        }

        return list;
    }

    /**
     * Get classes using the class path returned by <code>getFullClassPath()</code>.
     */
    public static Vector getClasses (boolean sort) {
        String cp = getFullClassPath();
        return getClasses(cp,sort);
    }

    /**
     * Return a class path constructed by concatenating the System
     * Property values for:
     * <pre>
     *    java.sys.class.path
     *    java.class.path
     *    env.class.path
     * </pre>
     * in that order.
     */
    public static String getFullClassPath () {
        String syscp = System.getProperty("java.sys.class.path");
        String appcp = System.getProperty("java.class.path");
        String envcp = System.getProperty("env.class.path");
        String cp = null;

        if (syscp != null) {
            cp = syscp;
        }

        if (appcp != null) {
            if (cp == null) {
                cp = appcp;
            } else {
                cp = cp + File.pathSeparator + appcp;
            }
        }

        if (envcp != null) {
            if (cp == null) {
                cp = envcp;
            } else {
                cp = cp + File.pathSeparator + envcp;
            }
        }

        if (cp == null) {
            cp = ".";
        }

        return cp;
    }

    private static ClassPathEntry[] parsePath (String pathstr) {

        char dirSeparator = File.pathSeparatorChar;
        int i, j, n;
        ClassPathEntry[] path;

        if (pathstr.length() == 0) {
            path = new ClassPathEntry[0];
        }

        // Count the number of path separators
        i = n = 0;
        while ((i = pathstr.indexOf(dirSeparator, i)) != -1) {
            n++; i++;
        }
        // Build the class path
        path = new ClassPathEntry[n+1];
        int len = pathstr.length();
        for (i = n = 0; i < len; i = j + 1) {
            if ((j = pathstr.indexOf(dirSeparator, i)) == -1) {
                j = len;
            }
            if (i == j) {
                path[n] = new ClassPathEntry();
                path[n++].dir = new File(".");
            } else {
                File file = new File(pathstr.substring(i, j));
                if (file.exists()) {
                    if (file.isFile()) {
                        try {
                            ZipFile zip = new ZipFile(file);
                            path[n] = new ClassPathEntry();
                            path[n++].zip = zip;
                        } catch (ZipException e) {
                        } catch (IOException e) {
                                // Ignore exceptions, at least for now...
                        }
                    } else {
                        path[n] = new ClassPathEntry();
                        path[n++].dir = file;
                    }
                }
            }
        }
        // Trim class path to exact size
        ClassPathEntry[] result = new ClassPathEntry[n];
        System.arraycopy((Object)path, 0, (Object)result, 0, n);
        return result;
    }

    private static void addClasses(int rootLen, File dir, Vector list, Hashtable roots) {
        String[] files = dir.list();
        for (int i = 0; i < files.length; i++) {
            File file = new File(dir,files[i]);
            if (file.isDirectory()) {

                // Does our list of roots contain this directory? Must
                // use a case-insensitive compare...

                String path = file.getPath().toLowerCase();
                if (roots.get(path) == null) {

                    // No, so add it...

                    addClasses(rootLen,file,list,roots);
                }
            } else {
                String name = file.getPath();
                int index = name.lastIndexOf(".class");
                if (index >= 0) {
                    name = name.replace(File.separatorChar,'.').substring(rootLen,index);
                    list.addElement(name);
                }
            }
        }
    }
}

class ClassPathEntry {
    File dir;
    ZipFile zip;
}

class StringComparator implements Comparator {

    public int compare(Object o1, Object o2) {
        String s1 = (String) o1;
        String s2 = (String) o2;
        return s1.compareTo(s2);
    }
}
