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

import java.util.Vector;
import java.util.StringTokenizer;

/**
 * Wrapper around calling javac.
 */
public class JavaCompiler extends Compiler
{
    private boolean newVM() 
    {
        String version = System.getProperty( "java.version" ) ;
        StringTokenizer st = new StringTokenizer( version, "." ) ;

        // Assume that version is major.minor.patch format.
        // We can ignore the patch, which need not be a string
        // (e.g. 1.3.1_01 is a valid version).
        int major = Integer.parseInt( st.nextToken() ) ;
        int minor = Integer.parseInt( st.nextToken() ) ;

        // If we ever have a 2.x.y version, it would be new.
        // For now, 1.4 and greater are new VMs.
        return (major > 1) || (minor > 3) ;
    }

    /**
     * Returns the class name of the compiler.
     */
    public String compilerClassName()
    {
        // Break this up to avoid rename conflicts on sun tools java.
        return (newVM() ? "com.sun." : "sun.") 
            + "tools.javac.Main" ;
    }

    /**
     * Compile the given .java files.  Files are given as absolute paths.
     * The text output messages from the compile are placed in files
     * named javac.out.txt and javac.err.txt in the
     * given report directory.
     *
     *@param    files           Absolute paths to .java files for compilation
     *                          (can be null)
     *@param    arguments       Command line arguments to the compiler
     *@param    outputDirectory Where the resulting .class should go
     *@param    reportDirectory Where the output/error stream dumps should go
     *
     *@exception    Exception   Any error generated during compile or setup,
     *                          such as abnormal termination
     */
    public void compile(String files[],
                        Vector arguments,
                        String outputDirectory,
                        String reportDirectory) throws Exception
    {
        if (files == null || files.length == 0)
            return;

        Vector args = new Vector(5 + (arguments == null ? 0 : arguments.size()) + files.length);
        args.add("-g");
        args.add("-d");
        args.add(outputDirectory);
        // args.add("-bootclasspath");
        // args.add(Options.getClasspath());
        args.add( "-Xbootclasspath/p:" + 
            System.getProperty( "corba.test.orb.classpath" ) ) ;

        if (arguments != null)
            args.addAll(arguments);

        for(int i = 0; i < files.length; i++)
            args.add(files[i]);

        compileExternally(compilerClassName(),
                          CORBAUtil.toArray(args),
                          outputDirectory,
                          reportDirectory,
                          "javac");
    }
}
