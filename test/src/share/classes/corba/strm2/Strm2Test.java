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

package corba.strm2;

import test.Test;
import corba.framework.*;
import java.util.*;
import java.io.*;
import com.sun.corba.ee.spi.orb.ORB;

public class Strm2Test extends CORBATest 
{
    public static String[] rmicClasses = { "corba.strm2.TesterImpl"};
    
    protected void compileSpecialSubdirectory(String dirName) throws Exception {
        System.out.println("      Compiling classes under " + dirName + "...");

        File outputDir = new File(Options.getOutputDirectory()
                                  + File.separator
                                  + dirName);

        if (!outputDir.mkdir())
            throw new Exception("Error making directory: "
                                + outputDir.getAbsolutePath());

        File testDir = new File(Options.getTestDirectory()
                                + File.separator
                                + dirName);

        if (!testDir.exists())
            throw new Exception("Can't find directory: "
                                + testDir.getAbsolutePath());

        // First look in the directory for all the
        // .java files and get their absolute paths
        File[] filesInDir = testDir.listFiles();
        ArrayList filesToCompile = new ArrayList(filesInDir.length);

        for (int i = 0; i < filesInDir.length; i++) {
            if (filesInDir[i].isFile() &&
                filesInDir[i].toString().endsWith(".java"))
                filesToCompile.add(filesInDir[i]);
        }

        String[] filePathsToCompile = new String[filesToCompile.size()];

        for (int i = 0; i < filePathsToCompile.length; i++) {
            File file = (File)filesToCompile.get(i);

            filePathsToCompile[i] = file.getAbsolutePath();
        }

        // Now compile them to the output directory
        javac.compile(filePathsToCompile,
                      null,
                      outputDir.getAbsolutePath(),
                      Options.getReportDirectory());
    }

    protected void doTest() throws Throwable {
        if (test.Test.useJavaSerialization()) {
            return;
        }

        Options.setRMICClasses(rmicClasses);
        Options.addRMICArgs("-nolocalstubs -iiop -keep -g");
        
        compileRMICFiles();
        compileJavaFiles();

        Controller orbd = createORBD();
        orbd.start();
        
        // This could be done in the overall makefile
        // if someone could figure it out!
        for (int i = 0; i < Versions.testableVersions.length; i++) {
            compileSpecialSubdirectory(Versions.testableVersions[i]);
        }

        Controller servers[] = new Controller[Versions.testableVersions.length];
        Controller clients[] = new Controller[Versions.testableVersions.length];

        // Add these for debugging:
        // Properties clientProps = Options.getExtraClientProperties();
        // clientProps.setProperty("com.sun.corba.ee.ORBDebug", "transport,subcontract,giop");

        // Properties serverProps = Options.getExtraServerProperties();
        // serverProps.setProperty("com.sun.corba.ee.ORBDebug", "transport,subcontract,giop");

        String oldClasspath = Options.getClasspath();
        for (int i = 0; i < Versions.testableVersions.length; i++) {
            String newClasspath = oldClasspath
                + File.pathSeparator
                + Options.getOutputDirectory()
                + Versions.testableVersions[i];

            Options.setClasspath(newClasspath);

            servers[i] = createServer("corba.strm2.Server",
                                      "server_" +
                                      Versions.testableVersions[i]);

            clients[i] = createClient("corba.strm2.Client",
                                      "client_" +
                                      Versions.testableVersions[i]);
            
            servers[i].start();
        }
        Options.setClasspath(oldClasspath);

        // Run through the clients

        for (int i = 0; i < clients.length; i++) {
            String version = Versions.testableVersions[i] ;
            System.out.println("      Running client version " + version ) ;

            clients[i].start();

            clients[i].waitFor(360000);

            clients[i].stop();
        }

        // Stop all the servers
        
        for (int i = 0; i < servers.length; i++)
            servers[i].stop();

        // Finally, stop ORBD
        orbd.stop();

        // The framework will check and report any error
        // codes from the client processes
    }
}
