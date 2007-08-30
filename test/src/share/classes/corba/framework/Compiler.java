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
import java.util.Properties ;

/**
 * Abstraction of a compiler, used to implement IDLJ, RMIC, and Javac
 * wrappers.
 */
public abstract class Compiler
{
    /**
     * Compile the given files according to the other parameters.
     *
     *@param files           Array of files to compile (assumes full paths)
     *@param arguments       Arguments to the compiler
     *@param outputDirectory Directory in which to place generated files
     *@param reportDirectory Directory in which to place dump files of
     *                       the compiler's stdout and stderr
     *
     *@exception Exception   Error occured
     *                       (probably bad exit value)
     */
    public abstract void compile(String files[],
                                 Vector arguments,
                                 String outputDirectory,
                                 String reportDirectory) throws Exception;

    /**
     * Perform the compile in a separate process.  It's easier to do it
     * that way since the compiler's output streams can be dumped to files.
     * This waits for completion or a maximum timeout (defined in Options)
     *
     *@param className  Name of the class of the compiler
     *@param progArgs   Arguments to the compiler (including file names)
     *@param outputDirectory  Directory in which to place generated files
     *@param reportDirectory  Directory in which to place IO dumps
     *@param compilerName  Identifying name of the compiler for the IO
     *                     files (to create "javac.err.txt", etc)
     *@exception Exception  Exception  Error occured (probably bad exit value)
     */
    protected void compileExternally(String className,
                                     String[] progArgs,
                                     String outputDirectory,
                                     String reportDirectory,
                                     String compilerName) throws Exception
    {
	// Make certain the directories exist
	// Note: this must be done here as well as in the test harness
	// in case a test (like corba.codebase) changes the output directory
	// in the test itself!
        CORBAUtil.mkdir(outputDirectory);
        CORBAUtil.mkdir(reportDirectory);

        FileOutputDecorator exec 
            = new FileOutputDecorator(new ExternalExec(false));
   
	Properties props = new Properties() ;
	int emmaPort = EmmaControl.setCoverageProperties( props ) ;
        exec.initialize(className,
                        compilerName,
                        props,
                        null,
                        progArgs,
                        reportDirectory + compilerName + ".out.txt",
                        reportDirectory + compilerName + ".err.txt",
                        null,
			emmaPort ) ;
    
        exec.start();
        int result = 1;

	try {

	    result = exec.waitFor(Options.getMaximumTimeout());

	} catch (Exception e) {
	    exec.stop();
	    throw e;
	}

        if (result != Controller.SUCCESS) 
            throw new Exception(compilerName 
                                + " compile failed with result: " 
                                + result);

        exec.stop();
    }
}
