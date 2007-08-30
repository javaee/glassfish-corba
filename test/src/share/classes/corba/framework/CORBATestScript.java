/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2007 Sun Microsystems, Inc. All rights reserved.
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
package corba.framework ;

import java.io.File ;
import java.io.IOException ;

import java.net.Socket ;
import java.net.ServerSocket ;

import java.util.Hashtable ;
import java.util.Properties ;

import com.sun.javatest.Script ;
import com.sun.javatest.TestDescription ;
import com.sun.javatest.TestEnvironment ;
import com.sun.javatest.Status ;

import sun.tools.java.ClassPath ;
import sun.tools.java.ClassFile ;

import test.ServantContext ;

import test.Test ;
import test.ClassUtils ;

public class CORBATestScript extends Script {

    private File testPackageDir = null ;
    private File outputDir = null ;
    
    public Status run( String[] args,
	TestDescription td,
	TestEnvironment env )
    {
	String testClass = (String)td.getParameter( "executeClass" ) ;
	int num ;
	try {
	    num = Integer.parseInt( "numArgs" ) ;
	} catch (NumberFormatException exc) {
	    return Status.error( "Bad test description: error in getting number of args: " 
		+ exc ) ;
	}

	String[] testArgs = new String[num] ;
	for (int ctr=0; ctr<num; ctr++ )
	    testArgs[ctr] = td.getParameter( "testArg." + ctr ) ;
	
	// execute the test
	Status status = corbaMain( testClass, testArgs ) ;
	return status ;
    }

    private int[] getFreePorts(int count) throws IOException {
        int[] result = new int[count];
        ServerSocket[] sockets = new ServerSocket[count];
        for (int i = 0; i < count; i++) {
            sockets[i] = new ServerSocket(0);
            result[i] = sockets[i].getLocalPort();
        }
        for (int i = 0; i < count; i++) {
            sockets[i].close();
        }
        return result;
    }

    /**
     * Takes an array of strings and produces a Hashtable wherein the keys
     * are strings prefixed with a hyphen and the values are the strings which
     * immediately follow such hyphen prefixed strings.  However, if strings
     * '-one' and '-two' are found in sequence, the key '-one' has an empty
     * string for a value.
     * <p>
     * Example :
     * <p>
     * If an array of {'-one','-two','Value2','-three','Value3'} is the input,
     * the Hashtable { key = '-one' value = ''; key = '-two' value = 'Value2';
     * key = '-three' value = 'Value3'} is the output.
     *
     * @param args[] An array of strings.
     * @return The hashtable created from the array
     * @exception ConsolePairArrayException Bad key position or format
     **/
    public Hashtable createFromConsolePairs(String  args[])
    {
	int i = 0;
	Hashtable table = new Hashtable();
	String key = null, value = null;

	while(i < args.length){
	    key = args[i].toLowerCase();

	    if (key.charAt(0) != '-')
		throw new Error(key + " is not a key");

	    i++;
	    if (i == args.length)
		value = new String();
	    else {
		value = args[i];

		if (value.charAt(0) == '-') {
		    value = new String();
		} else
		    i++;
	    }

	    table.put(key, value);
	}

	return table;
    }

    private static void setDefaultOutputDir (Hashtable flags) {
        String outputDir = (String) flags.get(Test.OUTPUT_DIRECTORY);
        if (outputDir == null) {
    	
    	    // No output dir, so set to gen if present...
 
            String className = "com.sun.corba.se.impl.orb.ORBImpl";
            ClassPath classPath = new ClassPath(System.getProperty("java.class.path"));
            ClassFile cls = classPath.getFile(className.replace('.',File.separatorChar) + ".class");
            try {
                classPath.close();
            } catch (IOException e) {}

            if (cls != null) {

                File genDir = null;
                
                // Ok, we have a ClassFile for com.sun.corba.se.internal.core.ORB.class. Is it in 
                // a zip file?
                
                if (cls.isZipped()) {
                    // Yes. _REVISIT_
                    
                    System.out.println( "test.Test " + 
			"found com.sun.corba.se.internal.core.ORB.class in: " + cls.getPath());
                    System.out.println( "The output directory did not default to 'gen'.");
                } else {
                    // No, so walk back to root...
                    
                    String path = cls.getPath();
                    if (File.separatorChar == '\\') {
                        path = path.replace('/',File.separatorChar);
                    } else {
                        path = path.replace('\\',File.separatorChar);
                    }
                    File dir = new File(path);

                    for (int i = 0; i < 7; i++) {
                        dir = new File(dir.getParent());

                    }

                    genDir = new File(dir,"gen");
                }

                // If we have a directory, update the Hashtable...
                
		if (genDir != null && genDir.exists() && genDir.isDirectory()) {
		    flags.put(Test.OUTPUT_DIRECTORY,genDir.getPath());
		}
	    }
        }
    }

    private boolean expandMagicDirectoryFor(String key, Hashtable flags) {

        boolean result = false;
        String value = (String) flags.get(key);

        // Do we need to expand our magic directory?

        if (value != null && (value.startsWith("%") || value.startsWith("@"))) {

	    // Yes, do we already have our test directory?

	    if (testPackageDir == null) {

                // Nope, so look it up...

		testPackageDir = ClassUtils.packageDirectory("test.Test",true);

		if (testPackageDir == null || !testPackageDir.isDirectory()) {
		    System.out.println("Cannot find directory for 'test'.");
		    throw new Error( "expandMagicDirectoryFor(" + key + ") failed." ) ;
		}

		// Make an output dir also...

		outputDir = new File(testPackageDir.getParent());
	    }

	    // Make a new value...

	    String magicValue = null;

	    if (key.equals(Test.OUTPUT_DIRECTORY)) {
		magicValue = outputDir.getPath();
	    } else {

		if (value.length() > 1) {
		    File theFile = new File(testPackageDir,value.substring(1));
		    magicValue = theFile.getPath();
		} else {
		    magicValue = testPackageDir.getPath();
		}
	    }

	    // Change the value in the hashtable...

	    flags.put(key,magicValue);
	    result = true;
	}

	return result;
    }

    public Status corbaMain(String testClass, String[] testArgs)
    {
	Status status = null ;

        try {
            if (System.getSecurityManager() == null) {
                // _REVISIT_ This disables all security checks. What is really needed?
                System.setSecurityManager(new javax.rmi.download.SecurityManager());
            }

            // Initialize the port based properties with free ports...

            Properties sysProps = System.getProperties();
            int[] ports = getFreePorts(2);
            
            if (sysProps.get("http.server.port") == null) {
                // System.out.println("Setting http.server.port = "+ports[0]);
                sysProps.put("http.server.port",Integer.toString(ports[0]));
            }
            
            if (sysProps.get("name.server.port") == null) {
                // System.out.println("Setting name.server.port = "+ports[1]);
                sysProps.put("name.server.port",Integer.toString(ports[1]));
            }
            
            if (sysProps.get("java.rmi.server.codebase") == null) {
                String codebase = "http://localhost:"+Integer.toString(ports[0])+"/";
                // System.out.println("Setting java.rmi.server.codebase = "+codebase);
                sysProps.put("java.rmi.server.codebase",codebase);
            }

            // Initialize the ORB class property...
            if (sysProps.get(Test.ORB_CLASS_KEY) == null) {
                sysProps.put(Test.ORB_CLASS_KEY, Test.ORB_CLASS);
            }

	    Hashtable flags = createFromConsolePairs(testArgs);

	    // Expand our 'magic' directories, if need be...
	    expandMagicDirectoryFor(Test.TEST_FILE_NAME_FLAG,flags);
	    expandMagicDirectoryFor(Test.OUTPUT_DIRECTORY,flags);
	    setDefaultOutputDir(flags);

	    status = runTestClass(testClass,flags,testArgs);
        } catch (Exception e) {
            System.out.println("Caught "+e);
	    e.printStackTrace() ;
	    status = Status.error( "Caught " + e ) ;
        } finally {
            // Make sure we clean up...
            ServantContext.destroyAll();
        }

	return status ;
    }

    private int getNumIterations( Hashtable flags ) 
    {
        int iterations = 1;
        boolean noIterate = flags.containsKey(Test.NO_ITERATE_FLAG);
        if (!noIterate && flags.containsKey(Test.NUMBER_OF_ITERATIONS_FLAG)) {
            iterations = Integer.parseInt(
		(String)flags.get(Test.NUMBER_OF_ITERATIONS_FLAG));
        }  

	return iterations ;
    }

    private long doAnIteration( int iterNum, Test testObj, boolean verbose ) 
    {
	testObj.beginAnIteration();

	long start = System.currentTimeMillis();
	testObj.run();
	long duration = System.currentTimeMillis() - start;

	Error err = testObj.finishAnIteration(iterNum+1, duration);
	testObj.getResults()[iterNum] = new Hashtable();
	testObj.getResults()[iterNum].put(Test.DURATION, new Long(duration));
	if (err != null) {
	    testObj.getResults()[iterNum].put(Test.STATUS, err);
	}
	
	if (verbose) 
	    System.out.print("[" + duration + "ms] ");

	if (err == null) {
	    System.out.println(testObj.getPassed());
	} else {
	    System.out.println(testObj.getFailed(err));
	}

	System.out.flush();
	return duration ;
    }

    public Status runTestClass(String testClassName, Hashtable flags, 
	String args[]) throws ClassNotFoundException, 
	InstantiationException, IllegalAccessException
    {
	Status status = null ;
	Test testObj = (Test)Class.forName(testClassName).newInstance();
        // testObj.sArgs = args;
        testObj.setArgs(flags);

        long sum = 0;
	int numErrors = 0;
	Error lastError = null ;
	int iterations = getNumIterations( flags ) ;
	testObj.createResultsTable(iterations);

	boolean verbose = flags.containsKey(Test.VERBOSE_FLAG);
	boolean demure = flags.containsKey(Test.DEMURE_FLAG);
	String info = "";
	if (flags.containsKey(Test.FVD_FLAG))
	    info = " (Using FVD) ";

	if (verbose) {
	    System.out.print("    " + testClassName + info + ": ");
	} else if (demure) {
	    System.out.print("    " + testObj.getName() + " ... ");
	}

	testObj.setup();

	for (int i = 0; i < iterations; i++) {
	    if (verbose) {
		System.out.print("Run " + (i+1) + " : ");
		System.out.flush();
	    }

	    sum += doAnIteration( i, testObj, verbose ) ;
	    if (testObj.getResults()[i].get(Test.STATUS) != null)
		numErrors++ ;
	}

	if (verbose && iterations > 1)
	    System.out.println("   AVERAGE TIME (MS) = " + (sum/iterations));

	if (numErrors > 0) {
	    if (iterations > 1)
		status = Status.failed( "" + numErrors + " of " + iterations +
		    " iterations of the test failed.  Last error was " + lastError ) ;
	    else 
		status = Status.failed( "Test failed with error " + lastError ) ;
	} else {
	    if (iterations > 1)
		status = Status.passed( "" + iterations + " passed" ) ;
	    else 
		status = Status.passed( "Test passed" ) ;
	}

	testObj.finish();

	return status ;
    }
}
