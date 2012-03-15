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
package corba.evolve;

import test.Test;
import corba.framework.*;
import java.util.*;
import java.io.*;
import com.sun.corba.ee.spi.misc.ORBConstants;

// Tests basic class evolution
public class EvolveTest extends CORBATest
{
    public static String[] rmicClasses = { "corba.evolve.UserNameVerifierImpl"};

    public static String[] compileClasses = { "UserName", "UserNameRO", "UserNameROD", "FeatureInfoImpl",
        "mymath" + File.separator + "BigDecimal",
        "mymath" + File.separator + "BigInteger",
        "mymath" + File.separator + "BitSieve",
        "mymath" + File.separator + "MathContext",
        "mymath" + File.separator + "MutableBigInteger",
        "mymath" + File.separator + "RoundingMode",
        "mymath" + File.separator + "SignedMutableBigInteger" };

    public static final String EVOLVED_DIR = "evolved";
    public static final String ORIG_DIR = "original";

    private String testDir ;

    private void compileFiles( String dirName ) throws Exception {
        File dir = new File(Options.getOutputDirectory() 
            + File.separator + dirName );

        dir.mkdirs() ;
        if (!dir.isDirectory())
            throw new Exception( "Error making directory" + dir ) ;

        testDir = Options.getTestDirectory() + File.separator ;

        String result[] = new String[compileClasses.length] ;
        for (int ctr=0; ctr<compileClasses.length; ctr++) {
            result[ctr] = testDir + dirName + File.separator 
                + compileClasses[ctr] + ".java" ;
        }

        javac.compile( result, Options.getJavacArgs(),
            dir.getAbsolutePath(), Options.getReportDirectory() ) ;
    }

    // Compile the original and evolved classes into separate
    // directories under the output directory.
    private void compileSpecialClasses() throws Exception {
        compileFiles( ORIG_DIR ) ;
        compileFiles( EVOLVED_DIR ) ;
    }

    private int failures = 0 ;
    private String origClasspath ;
    private String evolClasspath ;

    private String setClasspath( String side, boolean isOriginal ) {
        Test.dprint( "Starting " + side + " with the " 
            + (isOriginal ? "original" : "evolved") + " classpath" ) ;
        Options.setClasspath( isOriginal ? origClasspath : evolClasspath ) ;
        return (isOriginal ? "orig_" : "evol_") + side ;
    }

    private void runTest( boolean clientIsOriginal ) throws Exception {
        String serverName = setClasspath( "server", !clientIsOriginal ) ;
        Controller server = createServer( "corba.evolve.Server", 
            serverName ) ;
        server.start();

        String clientName = setClasspath( "client", clientIsOriginal ) ;
        Controller client = createClient( "corba.evolve.Client", 
            clientName ) ;
        client.start();
            
        if (client.waitFor(10*Options.getMaximumTimeout()) != Controller.SUCCESS) {
            System.out.println("Bad client exit value (" + client.exitValue() 
                + ") with evolved class");
            failures++;
        }

        server.stop();
        client.stop();
    }

    protected void doTest() throws Throwable {
        Options.setRMICClasses(rmicClasses);
        Options.addRMICArgs("-poa -nolocalstubs -iiop -keep -g");

        compileRMICFiles();
        compileJavaFiles();

        Controller orbd = createORBD();
        orbd.start();

        // Generate the original and evolved classes in their own
        // directories under the output directory.
        Test.dprint("Compiling original and generated files");
        compileSpecialClasses();

        String testClasspath = Options.getClasspath();

        origClasspath = Options.getOutputDirectory() + ORIG_DIR 
            + File.pathSeparator + testClasspath; 
        evolClasspath = Options.getOutputDirectory() + EVOLVED_DIR 
            + File.pathSeparator + testClasspath;

        // Test 1:  Server with the original class vs evolved client
        runTest( true ) ;

        try { Thread.sleep(1000); } catch (InterruptedException e) {}

        // Test 2:  Server with the evolved class vs original client
        runTest( false ) ;

        orbd.stop();

        if (failures > 0)
            throw new Error("Failures detected: " + failures);
    }
}
