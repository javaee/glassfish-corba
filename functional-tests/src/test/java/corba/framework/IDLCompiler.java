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
package corba.framework;

import java.util.Vector;
import java.io.File;

/**
 * Wrapper around calling a real IDL compiler.
 */
public class IDLCompiler extends Compiler
{
    /**
     * Command line option for specifying the output directory
     */
    protected String OUTPUT_DIR_OPTION = "-td";

    /**
     * Default IDL compiler class name
     */
    protected static final String DEFAULT_IDL_COMPILER_CLASS 
        = "com.sun.tools.corba.se.idl.toJavaPortable.Compile";
    
    /**
     * Returns the class name of the compiler.
     */
    public String compilerClassName()
    {
        return DEFAULT_IDL_COMPILER_CLASS;
    }
    
    /**
     * Compile the given IDL files.  Files are given as absolute paths.
     * The text output messages from the compile are placed in files
     * named idlcompile.out.txt and idlcompile.err.txt in the
     * given report directory.
     *
     *@param    files           Absolute paths to IDL files for compilation
     *                          (can be null)
     *@param    arguments       Command line arguments to the compiler
     *@param    stubDirectory   Where the resulting .java files should go
     *@param    reportDirectory Where the output/error stream dumps should go
     *
     *@exception    Exception   Any error generated during compile or setup,
     *                          such as abnormal termination
     */
    public void compile(String files [],
                        Vector arguments,
                        String stubDirectory,
                        String reportDirectory) throws Exception
    {
        if (files == null || files.length == 0)
            return;

        // Probably the right way to do this modification (which is
        // specific to our compiler) would've been to subclass IDLCompiler
        // and use the subclass until our compiler is better.

        // If there's only one file, use "idlcompiler" as the base of the
        // name for the stdout/stderr streams files.
        if (files.length == 1) {
            compileHelper(files[0], 
                          "idlcompiler", 
                          arguments, 
                          stubDirectory,
                          reportDirectory);
        } else {
            // Currently, our IDL compiler can only handle one file at
            // a time.  This means we must make multiple executions to
            // get everything compiled!
            for (int i = 0; i < files.length; i++) {
                String fn = null;
                try {
                    // Try to obtain the filename (without .idl) so
                    // that the base of the output file names will
                    // be idlcompiler_{filename}
                    File file = new File(files[i]);
                    String fileName = file.getName();
                    int dotIndex = fileName.indexOf(".idl");
                    if (dotIndex > 0) 
                        fileName = fileName.substring(0, dotIndex);
                    fn = fileName;
                } catch (Throwable t) {
                    // If something goes wrong, just make it
                    // idlcompiler_{file number in the sequence}
                    fn = "" + i;
                }

                // Do the compilation for this file
                compileHelper(files[i],
                              "idlcompiler_" + fn,
                              arguments,
                              stubDirectory,
                              reportDirectory);
            }
        }
    }


    /**
     * Helper that compiles one file externally.  When our IDL compiler
     * supports multiple files, this can be moved back into compile
     * above.
     */
    private void compileHelper(String file,
                               String outputFileName,
                               Vector arguments,
                               String stubDirectory,
                               String reportDirectory) throws Exception
    {

        Vector args = new Vector(1 + arguments.size() + 2);
        args.add(OUTPUT_DIR_OPTION);
        args.add(stubDirectory);

        if (arguments != null)
            args.addAll(arguments);

        args.add(file);

        compileExternally(compilerClassName(),
                          CORBAUtil.toArray(args),
                          stubDirectory,
                          reportDirectory,
                          outputFileName);
    }
}
