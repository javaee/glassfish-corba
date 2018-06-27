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

package sun.rmi.rmic.iiop;

import java.io.File;
import java.io.IOException;
import java.io.OutputStreamWriter;
import sun.tools.java.CompilerError;
import sun.tools.java.ClassDefinition;
import sun.rmi.rmic.IndentingWriter;
import sun.rmi.rmic.Main;

/**
 * An IDL generator for rmic.
 *
 * @version     1.0, 3/9/98
 * @author      Bryan Atsatt
 */
public class PrintGenerator implements sun.rmi.rmic.Generator,
                                       sun.rmi.rmic.iiop.Constants {

    private static final int JAVA = 0;
    private static final int IDL = 1;
    private static final int BOTH = 2;
        
    private int whatToPrint; // Initialized in parseArgs.
    private boolean global = false;
    private boolean qualified = false;
    private boolean trace = false;
    private boolean valueMethods = false;
        
    private IndentingWriter out;

    /**
     * Default constructor for Main to use.
     */
    public PrintGenerator() {
        OutputStreamWriter writer = new OutputStreamWriter(System.out);
        out = new IndentingWriter (writer);
    }

    /**
     * Examine and consume command line arguments.
     * @param argv The command line arguments. Ignore null
     * @param error Report any errors using the main.error() methods.
     * @return true if no errors, false otherwise.
     */
    public boolean parseArgs(String argv[], Main main) {
        for (int i = 0; i < argv.length; i++) {
            if (argv[i] != null) {
                String arg = argv[i].toLowerCase();
                if (arg.equals("-xprint")) {
                    whatToPrint = JAVA;
                    argv[i] = null;
                    if (i+1 < argv.length) {
                        if (argv[i+1].equalsIgnoreCase("idl")) {
                            argv[++i] = null;
                            whatToPrint = IDL;
                        } else if (argv[i+1].equalsIgnoreCase("both")) {
                            argv[++i] = null;
                            whatToPrint = BOTH;
                        }
                    }                       
                } else if (arg.equals("-xglobal")) {
                    global = true;
                    argv[i] = null;
                } else if (arg.equals("-xqualified")) {
                    qualified = true;
                    argv[i] = null;
                } else if (arg.equals("-xtrace")) {
                    trace = true;
                    argv[i] = null;
                } else if (arg.equals("-xvaluemethods")) {
                    valueMethods = true;
                    argv[i] = null;
                }
            }
        }
        return true;
    }

    /**
     * Generate output. Any source files created which need compilation should
     * be added to the compiler environment using the addGeneratedFile(File)
     * method.
     *
     * @param env       The compiler environment
     * @param cdef      The definition for the implementation class or interface from
     *              which to generate output
     * @param destDir   The directory for the root of the package hierarchy
     *                          for generated files. May be null.
     */
    public void generate(sun.rmi.rmic.BatchEnvironment env, ClassDefinition cdef, File destDir) {
                
        BatchEnvironment ourEnv = (BatchEnvironment) env;
        ContextStack stack = new ContextStack(ourEnv);
        stack.setTrace(trace);

        if (valueMethods) {
            ourEnv.setParseNonConforming(true);
        }
                
        // Get our top level type...
                
        CompoundType topType = CompoundType.forCompound(cdef,stack);
        
        if (topType != null) {
                        
            try {
        
                                // Collect up all the compound types...
                                
                Type[] theTypes = topType.collectMatching(TM_COMPOUND);
                                
                for (int i = 0; i < theTypes.length; i++) {
        
                    out.pln("\n-----------------------------------------------------------\n");
                
                    Type theType = theTypes[i];

                    switch (whatToPrint) {
                    case JAVA:  theType.println(out,qualified,false,false);
                        break;
                                                                         
                    case IDL:   theType.println(out,qualified,true,global);
                        break;
                                                                        
                    case BOTH:  theType.println(out,qualified,false,false);
                        theType.println(out,qualified,true,global);
                        break;
                                                
                    default:    throw new CompilerError("Unknown type!");
                    }
                }
                                
                out.flush();
                                
            } catch (IOException e) {
                throw new CompilerError("PrintGenerator caught " + e);
            }
        }
    }
}
