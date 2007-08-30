/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1999-2007 Sun Microsystems, Inc. All rights reserved.
 *
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common Development
 * and Distribution License("CDDL") (collectively, the "License").  You
 * may not use this file except in compliance with the License. You can obtain
 * a copy of the License at https://glassfish.dev.java.net/public/CDDL+GPL.html
 * or glassfish/bootstrap/legal/LICENSE.txt.  See the License for the specific
 * language governing permissions and limitations under the License.
 *
 * When distributing the software, include this License Header Notice in each
 * file and include the License file at glassfish/bootstrap/legal/LICENSE.txt.
 * Sun designates this particular file as subject to the "Classpath" exception
 * as provided by Sun in the GPL Version 2 section of the License file that
 * accompanied this code.  If applicable, add the following below the License
 * Header, with the fields enclosed by brackets [] replaced by your own
 * identifying information: "Portions Copyrighted [year]
 * [name of copyright owner]"
 *
 * Contributor(s):
 *
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
/* @(#)ClassUtils.java	1.3 99/06/07 */
/*
 * Licensed Materials - Property of IBM
 * RMI-IIOP v1.0
 * Copyright IBM Corp. 1998 1999  All Rights Reserved
 *
 * US Government Users Restricted Rights - Use, duplication or
 * disclosure restricted by GSA ADP Schedule Contract with IBM Corp.
 */

package test;

import java.io.File;
import sun.tools.java.ClassPath;
import sun.tools.java.ClassFile;
import java.io.IOException;
import java.util.StringTokenizer;

/**
 * ClassUtils provides miscellaneous static utility methods related to
 * classes and their packages.
 * @author Bryan Atsatt
 */

public class ClassUtils {

    /**
     * Return the directory which contains a given class (either .java or .class).
     * Uses the current system classpath.
     * @param className Fully qualified class name.
     * @param requireFile True if .class or .java file must be found. False if
     * ok to return a directory which does not contain file.
     * @return the directory or null if none found (or zipped).
     */
    public static File packageDirectory (String className, boolean requireFile) {
        ClassPath path = new ClassPath(System.getProperty("java.class.path"));
        File result = packageDirectory(className,path,requireFile);
        try {
            path.close();
        } catch (IOException e) {}
            
        return result;
    }
	
    /**
     * Return the directory which contains a given class (either .java or .class).
     * @param className Fully qualified class name.
     * @param path the class path.
     * @param requireFile True if .class or .java file must be found. False if
     * ok to return a directory which does not contain file.
     * @return the directory or null if none found (or zipped).
     */
    public static File packageDirectory (String className, ClassPath path, boolean requireFile) {
        
        // Try binary first, then source, then directory...
            
        File result = packageDirectory(className,path,".class");
        if (result == null) {
            result = packageDirectory(className,path,".java");
            if (result == null && !requireFile) {
                int i = className.lastIndexOf('.');
                if (i >= 0) {
                    String packageName = className.substring(0,i);
                    ClassFile cls = path.getDirectory(packageName.replace('.',File.separatorChar));
                    if (cls != null && ! cls.isZipped()) {
			result = new File(cls.getPath());
                    }
                }
            }
        }
        return result;
    }
	
    private static boolean directoryInPath(String dirPath, String path) {
        if (!dirPath.endsWith(File.separator)) {
            dirPath = dirPath + File.separator;
        }
        StringTokenizer st = new StringTokenizer(path,"\t\n\r"+File.pathSeparator);
        while (st.hasMoreTokens()) {
            String entry = st.nextToken();
            if (!entry.endsWith(".zip") &&
                !entry.endsWith(".jar")) {
                     
                if (entry.equals(".")) {
                    return true;
                } else {
                    if (!entry.endsWith(File.separator)) {
                        entry = entry + File.separator;  
                    }
                    if (entry.equalsIgnoreCase(dirPath)) {
                        return true;   
                    }
                }
            }
        }
      
	return false;
    }
	
    private static File packageDirectory (String className, ClassPath path, String fileExt) {
        
        ClassFile cls = path.getFile(className.replace('.',File.separatorChar) + fileExt);

        if (cls != null && ! cls.isZipped()) {
    	    File file = new File(cls.getPath());
    	    File dir = new File(file.getParent());
    	    return dir;
        }

	return null;
    }
}

