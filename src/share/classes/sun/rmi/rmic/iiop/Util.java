/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright (c) 1997-2018 Oracle and/or its affiliates. All rights reserved.
 * Copyright (c) 1998 IBM Corp. All rights reserved.
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
 * file and include the License file at packager/legal/LICENSE.txt.
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

package sun.rmi.rmic.iiop;

import java.io.File;
import sun.tools.java.Identifier;

import com.sun.corba.ee.impl.util.PackagePrefixChecker;

/**
 * Util provides static utility methods used by other rmic classes.
 * @author Bryan Atsatt
 */

public final class Util implements sun.rmi.rmic.Constants {


    public static String packagePrefix(){ return PackagePrefixChecker.packagePrefix();}


    /**
     * Return the directory that should be used for output for a given
     * class.
     * @param theClass The fully qualified name of the class.
     * @param rootDir The directory to use as the root of the
     * package heirarchy.  May be null, in which case the current
     * working directory is used as the root.
     */
    private static File getOutputDirectoryFor(Identifier theClass,
                                             File rootDir,
                                             BatchEnvironment env,
                                             boolean idl ) {
        File outputDir = null;
        String className = theClass.getFlatName().toString().replace('.', SIGC_INNERCLASS);             
        String qualifiedClassName = className;
        String packagePath = null;
        String packageName = theClass.getQualifier().toString();
        //Shift package names for stubs generated for interfaces.
        /*if(type.isInterface())*/ 
        packageName = 
                correctPackageName(packageName, idl, env.getStandardPackage());
        //Done.
        if (packageName.length() > 0) {
            qualifiedClassName = packageName + "." + className;
            packagePath = packageName.replace('.', File.separatorChar);
        }

        // Do we have a root directory?
        
        if (rootDir != null) {
                    
            // Yes, do we have a package name?
                
            if (packagePath != null) {
                    
                // Yes, so use it as the root. Open the directory...
                            
                outputDir = new File(rootDir, packagePath);
                            
                // Make sure the directory exists...
                            
                ensureDirectory(outputDir,env);
                    
            } else {
                    
                // Default package, so use root as output dir...
                    
                outputDir = rootDir;
            }               
        } else {
                    
            // No root directory. Get the current working directory...
                    
            String workingDirPath = System.getProperty("user.dir");
            File workingDir = new File(workingDirPath);
                    
            // Do we have a package name?
                    
            if (packagePath == null) {
                        
                // No, so use working directory...
               
                outputDir = workingDir;
                        
            } else {
                        
                // Yes, so use working directory as the root...
                            
                outputDir = new File(workingDir, packagePath);
                                    
                // Make sure the directory exists...
                                    
                ensureDirectory(outputDir,env);
            }
        }

        // Finally, return the directory...
            
        return outputDir;
    }

    public static File getOutputDirectoryForIDL(Identifier theClass,
                                             File rootDir,
                                             BatchEnvironment env) {
        return getOutputDirectoryFor(theClass, rootDir, env, true);
    }

    public static File getOutputDirectoryForStub(Identifier theClass,
                                             File rootDir,
                                             BatchEnvironment env) {
        return getOutputDirectoryFor(theClass, rootDir, env, false);
    }

    private static void ensureDirectory (File dir, BatchEnvironment env) {
        if (!dir.exists()) {
            dir.mkdirs();
            if (!dir.exists()) {
                env.error(0,"rmic.cannot.create.dir",dir.getAbsolutePath());
                throw new InternalError();
            }
        }
    }

    public static String correctPackageName(
            String p, boolean idl, boolean standardPackage){
        if (idl){
            return p;
        } else {
            if (standardPackage) {
                return p;
            } else {
                return PackagePrefixChecker.correctPackageName(p);
            }
        }
    }

    public static boolean isOffendingPackage(String p){
        return PackagePrefixChecker.isOffendingPackage(p);
    }

    public static boolean hasOffendingPrefix(String p){
        return PackagePrefixChecker.hasOffendingPrefix(p);
    }

}



