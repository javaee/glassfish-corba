/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2002-2007 Sun Microsystems, Inc. All rights reserved.
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

/*
 * Licensed Materials - Property of IBM
 * RMI-IIOP v1.0
 * Copyright IBM Corp. 1998 1999  All Rights Reserved
 *
 * US Government Users Restricted Rights - Use, duplication or
 * disclosure restricted by GSA ADP Schedule Contract with IBM Corp.
 */

package sun.rmi.rmic.iiop;

import java.util.Hashtable;
import java.io.File;
import java.io.FileInputStream;

/**
 * DirectoryLoader is a simple ClassLoader which loads from a specified
 * file system directory.
 * @author Bryan Atsatt
 * @version 1.0
 */

public class DirectoryLoader extends ClassLoader {
    
    private Hashtable cache;
    private File root;

    /**
     * Constructor.
     */
    public DirectoryLoader (File rootDir) {
	cache = new Hashtable();
	if (rootDir == null || !rootDir.isDirectory()) {
	    throw new IllegalArgumentException();
	}
	root = rootDir;
    }
	
    private DirectoryLoader () {}

    /**
     * Convenience version of loadClass which sets 'resolve' == true.
     */
    public Class loadClass(String className) throws ClassNotFoundException {
        return loadClass(className, true);
    }

    /**
     * This is the required version of loadClass which is called
     * both from loadClass above and from the internal function
     * FindClassFromClass.
     */
    public synchronized Class loadClass(String className, boolean resolve)
	throws ClassNotFoundException {
        Class result;
        byte  classData[];

        // Do we already have it in the cache?

        result = (Class) cache.get(className);

        if (result == null) {
            
	    // Nope, can we get if from the system class loader?

	    try {
	            
		result = super.findSystemClass(className);
	            
	    } catch (ClassNotFoundException e) {
	            
    	        // No, so try loading it...

    	        classData = getClassFileData(className);

    	        if (classData == null) {
    	            throw new ClassNotFoundException();
    	        }

    	        // Parse the class file data...

    	        result = defineClass(classData, 0, classData.length);

    	        if (result == null) {
    	            throw new ClassFormatError();
    	        }

		// Resolve it...

    	        if (resolve) resolveClass(result);

		// Add to cache...

    	        cache.put(className, result);
	    }
	}

        return result;
    }

    /**
     * Reurn a byte array containing the contents of the class file.  Returns null
     * if an exception occurs.
     */
    private byte[] getClassFileData (String className) {
        
        byte result[] = null;
	FileInputStream stream = null;

        // Get the file...

        File classFile = new File(root,className.replace('.',File.separatorChar) + ".class");

        // Now get the bits...

        try {
            stream = new FileInputStream(classFile);
            result = new byte[stream.available()];
            stream.read(result);
        } catch(ThreadDeath death) {
	    throw death;
	} catch (Throwable e) {
        }

	finally {
	    if (stream != null) {
		try {
		    stream.close();
		} catch(ThreadDeath death) {
		    throw death;
		} catch (Throwable e) {
		}
	    }
	}

        return result;
    }
}
