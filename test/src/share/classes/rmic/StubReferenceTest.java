/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1998-2007 Sun Microsystems, Inc. All rights reserved.
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

package rmic;

import java.io.ByteArrayOutputStream;
import java.util.StringTokenizer;

/*
 * @test
 */
public class StubReferenceTest extends StubTest {

    public static final String[] ADDITIONAL_ARGS = {"-alwaysGenerate","-keep"};

    public static final String CLASS_LIST_FILE = ".classlist";
    public static final String FILE_EXT =  ".java";
    public static final String FILE_REF_EXT =  ".javaref";

    private String[] shouldCompileClasses = null;
    private Target[] targets = null;

    /**
     * Return an array of fully qualified class names for which generation
     * should occur. Return empty array if none.
     */
    protected String[] getGenerationClasses () throws Throwable {
        initClasses();
        return shouldCompileClasses;
    }

    /**
     * Perform the test.
     */
    protected void doTest () throws Throwable {

        // Those classes that should compile have been compiled. Check to
        // ensure that they match there reference files...
        
        for (int i = 0; i < targets.length; i++) {
            if (targets[i].shouldCompile) {    
                String[] output = targets[i].output;
                
                for (int j = 0; j < output.length; j++) {
                    compareResources(output[j],FILE_EXT,FILE_REF_EXT);
                }
            }
        }
        
        // Now ensure that those classes which should NOT compile, do in
        // fact fail as expected...
        
        for (int i = 0; i < targets.length; i++) {
            if (targets[i].shouldCompile == false) {
                Target target = targets[i];
                boolean failed = false;
                ByteArrayOutputStream out = new ByteArrayOutputStream();
                try {
                    generate(target.inputClass,out);               
                } catch (Exception e) {
                    failed = true;
                }
                
                if (failed) {
                    
                    // Make sure that the error output contains all the errorStrings in the
                    // output array...
                    
                    String[] errors = target.output;
                    String errorText = out.toString();
                    
                    for (int j = 1; j < errors.length; j++) {
                        if (errorText.indexOf(errors[j]) < 0) {
                            throw new Error(target.inputClass + " error message did not contain '" +
					    errors[j] + "'. Got " + errorText);
                        }
                    }
                } else {
                    throw new Error(target.inputClass + " should FAIL to compile but did not.");
                }
            }
        }
    }

    /**
     * Append additional (i.e. after -idl and before classes) rmic arguments
     * to 'currentArgs'. This implementation will set the output directory if
     * the OUTPUT_DIRECTORY flag was passed on the command line.
     */
    protected String[] getAdditionalRMICArgs (String[] currentArgs) {
        return super.getAdditionalRMICArgs(ADDITIONAL_ARGS);
    }

    private synchronized void initClasses () throws Throwable {
        
        if (shouldCompileClasses == null) {
            String[] array = getResourceAsArray(getClass().getName(),CLASS_LIST_FILE,"#");
            int totalCount = array.length;
            targets = new Target[totalCount];
            int shouldCompileCount = 0;
            
            // Parse input into our Target array, keeping count of those that should
            // compile...
            
            for (int i = 0; i < totalCount; i++) {
                targets[i] = new Target(array[i]);
                
                if (targets[i].shouldCompile) {
                    shouldCompileCount++;
                }
            }
            
            int shouldNotCompileCount = totalCount - shouldCompileCount;
            
            // Allocate our array...
            
            shouldCompileClasses = new String[shouldCompileCount];

            // Fill them up...
            
            int shouldOffset = 0;
            for (int i = 0; i < totalCount; i++) {
                if (targets[i].shouldCompile) {
                    shouldCompileClasses[shouldOffset++] = targets[i].inputClass;
                }
            }
        }
    }
}

class Target {
    public String inputClass = null;
    public String[] output = null;
    public boolean shouldCompile = true;
    
    public Target (String entry) {
    
        // Parse the <inputclass>=<outputclass>[,<outputclass>] format...
   
	StringTokenizer s = new StringTokenizer(entry,"=,");
        int count = s.countTokens() - 1;
        output = new String[count];
        inputClass = s.nextToken().trim();
        int offset = 0;
        while (s.hasMoreTokens()) {
            output[offset++] = s.nextToken().trim();
        }
        
        // Set the shouldCompile flag if needed...
        
        if (output[0].equalsIgnoreCase("ERROR")) {
            shouldCompile = false;
        }
    }
}
