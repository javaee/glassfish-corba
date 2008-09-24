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
package corba.evolve;

import test.Test;
import corba.framework.*;
import java.util.*;
import java.io.*;
import com.sun.corba.se.spi.orbutil.ORBConstants;

// Tests basic class evolution
public class EvolveTest extends CORBATest
{
    public static String[] rmicClasses = { "corba.evolve.UserNameVerifierImpl"};

    public static final String EVOLVED_DIR = "evolved";
    public static final String ORIG_DIR = "original";

    // Compile the original and evolved classes into separate
    // directories under the output directory.
    private void compileSpecialClasses() throws Exception
    {
        File origDir = new File(Options.getOutputDirectory()
                                + File.separator
                                + ORIG_DIR);

        File evolvedDir = new File(Options.getOutputDirectory()
                                   + File.separator
                                   + EVOLVED_DIR);

        if (!origDir.mkdir() || !evolvedDir.mkdir())
            throw new Exception("Error making test/make/gen/original or evolved dirs");

        String testDir = Options.getTestDirectory() + File.separator;
        String files[] = new String[] { testDir 
                                        + ORIG_DIR 
                                        + File.separator 
                                        + "UserName.java" };

        // Compile the original UserName class into
        // test/make/gen/corba/evolve/original
        javac.compile(files,
                      Options.getJavacArgs(),
                      origDir.getAbsolutePath(),
                      Options.getReportDirectory());
                      
        files[0] = testDir + EVOLVED_DIR + File.separator + "UserName.java";

        // Now compile the evolved version into
        // test/make/gen/corba/evolve/evolved
        javac.compile(files,
                      Options.getJavacArgs(),
                      evolvedDir.getAbsolutePath(),
                      Options.getReportDirectory());
    }

    protected void doTest() throws Throwable
    {
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

        String origClasspath = (Options.getOutputDirectory() 
                                + ORIG_DIR 
                                + File.pathSeparator 
                                + testClasspath);

        String evolClasspath = (Options.getOutputDirectory()
                                + EVOLVED_DIR 
                                + File.pathSeparator 
                                + testClasspath);

        int failures = 0;

        /***********************************************************
         * Test 1:  Server with the original class vs evolved client
         **********************************************************/

        Test.dprint("Starting server with the original class");
        Options.setClasspath(origClasspath);
        Controller server = createServer("corba.evolve.Server",
                                         "orig_server");
        server.start();

        Test.dprint("Starting client with the evolved class");
        Options.setClasspath(evolClasspath);
        Controller client = createClient("corba.evolve.Client",
                                         "evol_client");
        client.start();
            
        if (client.waitFor(Options.getMaximumTimeout()) != Controller.SUCCESS) {
            System.out.println("Bad client exit value ("
                               + client.exitValue()
                               + ") with evolved class");
            failures++;
        }

        server.stop();
        client.stop();

        try { Thread.sleep(1000); } catch (InterruptedException e) {}

        /***********************************************************
         * Test 2:  Server with the evolved class vs original client
         **********************************************************/

        Test.dprint("Starting server with the evolved class");
        Options.setClasspath(evolClasspath);
        server = createServer("corba.evolve.Server",
                              "evol_server");
        server.start();

        Test.dprint("Starting client with the original class");
        Options.setClasspath(origClasspath);
        client = createClient("corba.evolve.Client",
                              "orig_client");
        client.start();
        if (client.waitFor(Options.getMaximumTimeout()) != Controller.SUCCESS) {
            System.out.println("Bad client exit value ("
                               + client.exitValue()
                               + ") with original class");
            failures++;
        }

        server.stop();
        client.stop();

        orbd.stop();

        if (failures > 0)
            throw new Error("Failures detected: " + failures);
    }
}
