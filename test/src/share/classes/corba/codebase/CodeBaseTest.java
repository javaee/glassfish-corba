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
package corba.codebase;

import test.Test;
import corba.framework.*;
import java.util.*;
import java.io.*;
import com.sun.corba.ee.spi.orb.ORB;

public class CodeBaseTest extends CORBATest
{
    public static final String VALUE_DIR = "values";
    public static final String STUBTIE_DIR = "stubtie";
    public static final String[] VALUES 
        = new String[] { "TestValue.java" };

        protected void doTest() throws Throwable {
        
        if (test.Test.useJavaSerialization()) {
            return;
        }

        // Generate stubs and ties in the STUBTIE_DIR off of
        // the main output directory.
        String stubTieDir = (new File(Options.getOutputDirectory() 
                                      + STUBTIE_DIR
                                      + File.separator)).getAbsolutePath();
        String valueDir = (new File(Options.getOutputDirectory() 
                                    + VALUE_DIR
                                    + File.separator)).getAbsolutePath();

        String oldOutputDir = Options.getOutputDirectory();

        Options.setRMICClasses(new String[] { "corba.codebase.Server" });
        Options.addRMICArgs("-nolocalstubs -iiop -keep -g");
        Options.setOutputDirectory(stubTieDir);
        compileRMICFiles();

        // Also generate a Serializable in a different directory
        // to test value code downloading

        Options.setJavaFiles(VALUES);
        Options.setOutputDirectory(valueDir);
        compileJavaFiles();

        Options.setOutputDirectory(oldOutputDir);

        String oldClasspath = Options.getClasspath();
        String cpWithAllClasses = 
            stubTieDir
            + File.pathSeparator
            + valueDir
            + File.pathSeparator
            + Options.getClasspath();

        Controller orbd = createORBD();
        orbd.start();

        int webServerPort = Options.getUnusedPort().getValue();

        Controller webServer = createWebServer(oldOutputDir,
                                               webServerPort);
        webServer.start();
        Options.setClasspath(oldClasspath);

        // Add the special RMI property for code downloading.
        // NOTE: Unless it ends in a slash, the RMI code assumes
        // it is a jar file!
        Properties serverProps = Options.getServerProperties();
        Properties clientProps = Options.getClientProperties();

        String baseURL = "http://localhost:"
            + webServerPort
            + "/";

        String fullCodeBase 
            = baseURL + STUBTIE_DIR + "/ "
            + baseURL + VALUE_DIR + "/";

        // First test code downloading where the client downloads the
        // stub and value classes
        serverProps.put("java.rmi.server.codebase", fullCodeBase);
        testDownloading(cpWithAllClasses,
                        oldClasspath,
                        false);

        // Now test code downloading where the server downloads the
        // value classes

        // Note:  Giving server only the codebase so it can download
        // the Tie.  It will get the info for how to download the
        // valuetype from the client.
        serverProps.put("java.rmi.server.codebase",
                        baseURL + STUBTIE_DIR + "/");
        clientProps.put("java.rmi.server.codebase", fullCodeBase);
        testDownloading(cpWithAllClasses,
                        oldClasspath,
                        true);

        orbd.stop();
        webServer.stop();
    }

    void testDownloading(String fullClasspath,
                         String shortClasspath,
                         boolean serverDownloading) throws Exception
    {
        Controller server, client;

        Properties clientProps = Options.getClientProperties();
        if (serverDownloading) {
            clientProps.put(Tester.SERVER_DOWNLOADING_FLAG, "true");
            Options.setClasspath(shortClasspath);
            server = createServer("corba.codebase.Server", "server_dl");
            Options.setClasspath(fullClasspath);
            client = createClient("corba.codebase.Client", "client_reg");
        } else {
            Options.setClasspath(fullClasspath);
            server = createServer("corba.codebase.Server", "server_reg");
            Options.setClasspath(shortClasspath);
            client = createClient("corba.codebase.Client", "client_dl");
            Options.setClasspath(fullClasspath);
        }


        Test.dprint("Testing code downloading by the " 
                    + (serverDownloading ? "server" : "client"));

        server.start();
        client.start( );

        // Note that the test framework will handle reporting if the overall
        // test failed since it will check the exit codes of the client and
        // server controllers during cleanup
        if (client.waitFor(120000) == Controller.SUCCESS)
            Test.dprint("PASSED");
        else
            Test.dprint("FAILED");

        client.stop();
        server.stop();
    }

    public Controller createWebServer(String webRootDirectory,
                                      int webServerPort)
        throws Exception
    {
        Test.dprint("Creating WebServer object...");

        Controller executionStrategy;
        if (debugProcessNames.contains("WebServer"))
            executionStrategy = new DebugExec();
        else
            executionStrategy = new ExternalExec();

        Properties props = Options.getServerProperties() ;
        int emmaPort = EmmaControl.setCoverageProperties( props ) ;

        String args[] = new String[] { 
                             "-port",
                             "" + webServerPort,
                             "-docroot",
                             webRootDirectory
                             };

        FileOutputDecorator exec =
            new FileOutputDecorator(executionStrategy);

        Hashtable extra = new Hashtable(1);

        // Make sure that starting the web server controller waits until the web server is ready
        extra.put(ExternalExec.HANDSHAKE_KEY, "Ready.");

        exec.initialize("corba.codebase.WebServer",
                        "WebServer",
                        props,
                        null,
                        args,
                        Options.getReportDirectory() + "webserver.out.txt",
                        Options.getReportDirectory() + "webserver.err.txt",
                        extra,
                        emmaPort ) ;

        controllers.add(exec);

        return exec;
    }
}

