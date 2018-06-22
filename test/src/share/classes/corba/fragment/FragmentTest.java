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

package corba.fragment;

import test.Test;
import corba.framework.*;
import java.util.*;
import com.sun.corba.ee.spi.misc.ORBConstants;

public class FragmentTest extends CORBATest {
    static final int GROW = 0;
    static final int COLLECT = 1;
    static final int STREAM = 2;
    static String[] GIOP_version = { "1.0", "1.1", "1.2" };
    static String[] GIOP_strategy = { "GROW", "CLCT", "STRM" };

    private void printBeginTest(int clientVersion,
                                int clientStrategy,
                                int serverVersion,
                                int serverStrategy)
    {
        StringBuffer output = new StringBuffer(80);

        // Pleasing aesthetics
        output.append("      ");

        output.append(GIOP_version[clientVersion]);
        output.append(" ");
        output.append(GIOP_strategy[clientStrategy]);
        output.append(" client <> ");
        output.append(GIOP_version[serverVersion]);
        output.append(" ");
        output.append(GIOP_strategy[serverStrategy]);
        output.append(" server: ");

        System.out.print(output.toString());
    }

    private String testName(int clientVersion, int clientStrategy, int 
        serverVersion, int serverStrategy) {

        StringBuffer output = new StringBuffer(80);

        output.append(GIOP_version[clientVersion]);
        output.append("_");
        output.append(GIOP_strategy[clientStrategy]);
        output.append("_client_to_");
        output.append(GIOP_version[serverVersion]);
        output.append("_");
        output.append(GIOP_strategy[serverStrategy]);
        output.append("_server");

        return output.toString() ;
    }

    private void printEndTest(String result)
    {
        System.out.println(result);
    }

    protected void doTest() throws Throwable  
    {
        int errors = 0;
        int fragmentSize = 1024;

        // Pleasing aesthetics
        System.out.println();

        for (int client_strategy = GROW, i = 0; i < GIOP_version.length; i++) {

            Properties clientProps = Options.getClientProperties();

            clientProps.put(ORBConstants.GIOP_FRAGMENT_SIZE, "" + fragmentSize);
            clientProps.put("array.length", "" + (fragmentSize * 2));
            clientProps.put(ORBConstants.GIOP_VERSION, GIOP_version[i]);
            clientProps.put(ORBConstants.GIOP_11_BUFFMGR, "" + client_strategy);
            clientProps.put(ORBConstants.GIOP_12_BUFFMGR, "" + client_strategy);

            for (int server_strategy = GROW, j = 0; j < GIOP_version.length; j++) {

                errors = runTest(errors, client_strategy, i, server_strategy, j);

                if (GIOP_version[j].equals("1.1") && server_strategy == GROW) {
                    server_strategy = STREAM; j--;
                } else if (GIOP_version[j].equals("1.2") && server_strategy == STREAM) {
                    server_strategy = GROW; j--;
                }
            }

            if (GIOP_version[i].equals("1.1") && client_strategy == GROW) {
                client_strategy = COLLECT; i--;
            } else if (GIOP_version[i].equals("1.2") && client_strategy == COLLECT) {
                client_strategy = STREAM; i--;
            } else if (GIOP_version[i].equals("1.2") && client_strategy == STREAM) {
                client_strategy = GROW; i--;
            }
        }

        System.out.print("      Test result : " );
        
        if (errors > 0)
            throw new Exception("Errors detected");
    }

    private int runTest(int errors, int client_strategy, int client_version, int server_strategy, int server_version) throws Exception {
        // skip non-longer support COLLECT strategy tests
        if (client_strategy == COLLECT || server_strategy == COLLECT) return errors;

        printBeginTest(client_version, client_strategy, server_version, server_strategy);
        String name = testName(client_version, client_strategy, server_version, server_strategy);

        Properties serverProps = Options.getServerProperties();
        serverProps.put(ORBConstants.GIOP_VERSION, GIOP_version[client_version]);
        serverProps.put(ORBConstants.GIOP_11_BUFFMGR, "" + server_strategy);
        serverProps.put(ORBConstants.GIOP_12_BUFFMGR, "" + server_strategy);

        Controller server = createServer("corba.fragment.Server", name );
        Controller client = createClient("corba.fragment.Client", name );

        server.start();
        client.start();

        client.waitFor(60000);

        if (client.exitValue() != Controller.SUCCESS) {
            errors++;
            printEndTest("FAILED, Client exit value = " + client.exitValue());
        } else
        if (server.finished()) {
            errors++;
            printEndTest("FAILED, Server crashed");
        } else {
            printEndTest("PASSED");
        }

        client.stop();
        server.stop();
        return errors;
    }
}

