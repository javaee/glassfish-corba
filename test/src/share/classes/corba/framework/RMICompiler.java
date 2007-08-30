/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2000-2007 Sun Microsystems, Inc. All rights reserved.
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
package corba.framework;

import java.util.Vector;

/**
 * Wrapper around calling a real RMIC.
 */
public class RMICompiler extends Compiler
{
    /**
     * Command line option for specifying the output directory
     */
    protected String OUTPUT_DIR_OPTION = "-d";

    /**
     * Default RMIC class name
     */
    protected static final String DEFAULT_RMIC_CLASS 
        = "sun.rmi.rmic.Main";
    
    /**
     * Returns the class name of the compiler.
     */
    public String compilerClassName()
    {
        return DEFAULT_RMIC_CLASS;
    }
    
    /**
     * Compile the given class files.  Files are given as absolute paths.
     * The text output messages from the compile are placed in files
     * named rmic.out.txt and rmic.err.txt in the
     * given report directory.
     *
     *@param    files           Absolute paths to class files for compilation
     *                          (can be null)
     *@param    arguments       Command line arguments to the compiler
     *@param    outputDirectory Where the resulting files should go
     *@param    reportDirectory Where the output/error stream dumps should go
     *
     *@exception    Exception   Any error generated during compile or setup,
     *                          such as abnormal termination
     */
    public void compile(String files [],
                        Vector arguments,
                        String outputDirectory,
                        String reportDirectory) throws Exception
    {
        if (files == null || files.length == 0)
            return;

        Vector args = new Vector(files.length + arguments.size() + 4);
        args.add(OUTPUT_DIR_OPTION);
        args.add(outputDirectory);
        args.add("-classpath");
        args.add(Options.getClasspath());

        if (arguments != null)
            args.addAll(arguments);

        for(int i = 0; i < files.length; i++)
            args.add(files[i]);

        compileExternally(compilerClassName(),
                          CORBAUtil.toArray(args),
                          outputDirectory,
                          reportDirectory,
                          "rmic");
    }
}
