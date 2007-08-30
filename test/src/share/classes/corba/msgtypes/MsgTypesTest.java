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
package corba.msgtypes;

import test.Test;
import corba.framework.*;
import java.util.*;
import com.sun.corba.se.impl.orbutil.ORBConstants;

public class MsgTypesTest extends CORBATest {
    static final int GROW = 0;
    static final int COLLECT = 1;
    static final int STREAM = 2;
    static String[] GIOP_version = { "1.0", "1.1", "1.2" };
    static String[] GIOP_strategy = { "GROW", "CLCT", "STRM" };

    int errors = 0; // keeps the error count

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

    private void printEndTest(String result)
    {
        System.out.println(result);
    }

    private void printFinishedTest(String result) {
        StringBuffer output = new StringBuffer(80);
        output.append("      ");
        output.append(result);

        System.out.println(output.toString());
    }

    protected void doTest() throws Throwable
    {
        // Pleasing aesthetics
        System.out.println();

        runLocateMsgType();
        runEarlyReply();

        runSimpleCancelRequest();
        runAbortiveCancelRequest1();
        runAbortiveCancelRequest2();

        runMessageError();
        runCloseConnection();
        runGIOPInterop();
        runTargetAddressDisp();

	// This has been commented out for a long time, and
	// it currently fails with a buffer underflow error.
        // runFragmentedReply();

        runHeaderPaddingTest();
        
        System.out.print("      Test result : " );

        if (errors > 0)
            throw new Exception("Errors detected");

    }

    public void runLocateMsgType() throws Throwable {

        Options.getClientArgs().clear();
        Options.addClientArg("LocateMsg");
        int fragmentSize = 32;

        for (int client_strategy = GROW, i = 0; i < GIOP_version.length; i++) {

            Properties clientProps = Options.getExtraClientProperties();

            clientProps.put(ORBConstants.GIOP_FRAGMENT_SIZE, "" + fragmentSize);
            clientProps.put(ORBConstants.GIOP_VERSION, GIOP_version[i]);
            clientProps.put(ORBConstants.GIOP_11_BUFFMGR, "" + client_strategy);
            clientProps.put(ORBConstants.GIOP_12_BUFFMGR, "" + client_strategy);

            for (int server_strategy = GROW, j = 0; j < GIOP_version.length; j++) {

                printBeginTest(i, client_strategy, j, server_strategy);

                Properties serverProps = Options.getExtraServerProperties();
                serverProps.put(ORBConstants.GIOP_VERSION, GIOP_version[i]);
                serverProps.put(ORBConstants.GIOP_11_BUFFMGR, "" + server_strategy);
                serverProps.put(ORBConstants.GIOP_12_BUFFMGR, "" + server_strategy);

                Controller server = createServer("corba.msgtypes.Server");
                Controller client = createClient("corba.msgtypes.Client");

                server.start();
                client.start();

                client.waitFor(60000);

                if (client.exitValue() != Controller.SUCCESS) {
                    errors++;
                    printEndTest("LocateMsgTest FAILED, Client exit value = " +
                                 client.exitValue());
                } else {
                    if (server.finished()) {
                        errors++;
                        printEndTest("LocateMsgTest FAILED, Server crashed");
                    } else {
                        printEndTest("LocateMsgTest PASSED");
                    }
                }
                client.stop();
                server.stop();

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
    }

    public void runEarlyReply() throws Throwable {
        Options.getClientArgs().clear();
        Options.addClientArg("EarlyReply");

        int fragmentSize = 1024;
        Properties clientProps = Options.getExtraClientProperties();
        clientProps.put(ORBConstants.GIOP_FRAGMENT_SIZE, "" + fragmentSize);

        Controller server = createServer("corba.msgtypes.Server");
        Controller client = createClient("corba.msgtypes.Client");

        server.start();
        client.start();

        client.waitFor(60000);

        if (client.exitValue() != Controller.SUCCESS) {
            errors++;
            printFinishedTest("EarlyReplyTest FAILED, Client exit value = " +
                         client.exitValue());
        } else {
            if (server.finished()) {
                errors++;
                printFinishedTest("EarlyReplyTest FAILED, Server crashed");
            } else {
                printFinishedTest("EarlyReplyTest PASSED");
            }
        }

        client.stop();
        server.stop();
    }

    public void runSimpleCancelRequest() throws Throwable {
        Options.getClientArgs().clear();
        Options.addClientArg("SimpleCancelRequest");

        int fragmentSize = 32;
        Properties clientProps = Options.getExtraClientProperties();
        clientProps.put(ORBConstants.GIOP_FRAGMENT_SIZE, "" + fragmentSize);

        Controller server = createServer("corba.msgtypes.Server");
        Controller client = createClient("corba.msgtypes.Client");

        server.start();
        client.start();

        client.waitFor(60000);

        if (client.exitValue() != Controller.SUCCESS) {
            errors++;
            printFinishedTest("SimpleCancelRqstTest FAILED, Client exit value = " +
                         client.exitValue());
        } else {
            if (server.finished()) {
                errors++;
                printFinishedTest("SimpleCancelRqstTest FAILED, Server crashed");
            } else {
                printFinishedTest("SimpleCancelRqstTest PASSED");
            }
        }

        client.stop();
        server.stop();
    }

    public void runAbortiveCancelRequest1() throws Throwable {
        Options.getClientArgs().clear();
        Options.addClientArg("AbortiveCancelRequest1");

        int fragmentSize = 1024;
        Properties clientProps = Options.getExtraClientProperties();
        clientProps.put(ORBConstants.GIOP_FRAGMENT_SIZE, "" + fragmentSize);

        Controller server = createServer("corba.msgtypes.Server");
        Controller client = createClient("corba.msgtypes.Client");

        server.start();
        client.start();

        client.waitFor(60000);

        if (client.exitValue() != Controller.SUCCESS) {
            errors++;
            printFinishedTest("AbortiveCancelRqTest1 FAILED, Client exit value = " +
                         client.exitValue());
        } else {
            if (server.finished()) {
                errors++;
                printFinishedTest("AbortiveCancelRqTest1 FAILED, Server crashed");
            } else {
                printFinishedTest("AbortiveCancelRqTest1 PASSED");
            }
        }

        client.stop();
        server.stop();
    }

    public void runAbortiveCancelRequest2() throws Throwable {
        Options.getClientArgs().clear();
        Options.addClientArg("AbortiveCancelRequest2");

        int fragmentSize = 1024;
        Properties clientProps = Options.getExtraClientProperties();
        clientProps.put(ORBConstants.GIOP_FRAGMENT_SIZE, "" + fragmentSize);

        Properties serverProps = Options.getExtraServerProperties();
        serverProps.put("org.omg.CORBA.ORBClass",
                        "com.sun.corba.se.impl.orb.ORBImpl");
        serverProps.put("org.omg.PortableInterceptor.ORBInitializerClass." +
                  "corba.msgtypes.Server", "true");

        Controller server = createServer("corba.msgtypes.Server");
        Controller client = createClient("corba.msgtypes.Client");

        server.start();
        client.start();

        client.waitFor(60000);

        if (client.exitValue() != Controller.SUCCESS) {
            errors++;
            printFinishedTest("AbortiveCancelRqTest2 FAILED, Client exit value = " +
                         client.exitValue());
        } else {
            if (server.finished()) {
                errors++;
                printFinishedTest("AbortiveCancelRqTest2 FAILED, Server crashed");
            } else {
                printFinishedTest("AbortiveCancelRqTest2 PASSED");
            }
        }

        client.stop();
        server.stop();
    }

    public void runTargetAddressDisp() throws Throwable {
        Options.getClientArgs().clear();
        Options.addClientArg("TargetAddrDisposition");

        int fragmentSize = 1024;
        Properties clientProps = Options.getExtraClientProperties();
        clientProps.put(ORBConstants.GIOP_FRAGMENT_SIZE, "" + fragmentSize);
        clientProps.put(ORBConstants.GIOP_TARGET_ADDRESSING,
                  "" + ORBConstants.ADDR_DISP_IOR);
        clientProps.put("org.omg.CORBA.ORBClass",
                        "com.sun.corba.se.impl.orb.ORBImpl");
        clientProps.put("org.omg.PortableInterceptor.ORBInitializerClass." +
                  "corba.msgtypes.Client", "true");
                  
        Properties serverProps = Options.getExtraServerProperties();
        serverProps.put("org.omg.CORBA.ORBClass",
                        "com.sun.corba.se.impl.orb.ORBImpl");
        serverProps.put("org.omg.PortableInterceptor.ORBInitializerClass." +
                  "corba.msgtypes.Server", "true");
        serverProps.put(ORBConstants.GIOP_TARGET_ADDRESSING,
                  "" + ORBConstants.ADDR_DISP_OBJKEY);              
        Controller server = createServer("corba.msgtypes.Server");
        Controller client = createClient("corba.msgtypes.Client");

        server.start();
        client.start();

        client.waitFor(60000);

        if (client.exitValue() != Controller.SUCCESS) {
            errors++;
            printFinishedTest("TargetAddrDisposition FAILED, Client exit value = " +
                         client.exitValue());
        } else {
            if (server.finished()) {
                errors++;
                printFinishedTest("TargetAddrDisposition FAILED, Server crashed");
            } else {
                printFinishedTest("TargetAddrDisposition PASSED");
            }
        }

        client.stop();
        server.stop();
    }

    public void runCloseConnection() throws Throwable {
        Options.getClientArgs().clear();
        Options.addClientArg("CloseConnection");

        int fragmentSize = 32;
        Properties clientProps = Options.getExtraClientProperties();
        clientProps.put(ORBConstants.GIOP_FRAGMENT_SIZE, "" + fragmentSize);

        Controller server = createServer("corba.msgtypes.Server");
        Controller client = createClient("corba.msgtypes.Client");

        server.start();
        client.start();

        client.waitFor(60000);

        if (client.exitValue() != Controller.SUCCESS) {
            errors++;
            printFinishedTest("CloseConnectionTest FAILED, Client exit value = " +
                         client.exitValue());
        } else {
            if (server.finished()) {
                errors++;
                printFinishedTest("CloseConnectionTest FAILED, Server crashed");
            } else {
                printFinishedTest("CloseConnectionTest PASSED");
            }
        }

        client.stop();
        server.stop();
    }

    public void runMessageError() throws Throwable {
        Options.getClientArgs().clear();
        Options.addClientArg("MessageError");

        int fragmentSize = 32;
        Properties clientProps = Options.getExtraClientProperties();
        clientProps.put(ORBConstants.GIOP_FRAGMENT_SIZE, "" + fragmentSize);

        Controller server = createServer("corba.msgtypes.Server");
        Controller client = createClient("corba.msgtypes.Client");

        server.start();
        client.start();

        client.waitFor(60000);

        if (client.exitValue() != Controller.SUCCESS) {
            errors++;
            printFinishedTest("MessageErrorTest FAILED, Client exit value = " +
                         client.exitValue());
        } else {
            if (server.finished()) {
                errors++;
                printFinishedTest("MessageErrorTest FAILED, Server crashed");
            } else {
                printFinishedTest("MessageErrorTest PASSED");
            }
        }

        client.stop();
        server.stop();
    }

    public void runGIOPInterop() throws Throwable {
        Options.getClientArgs().clear();
        Options.addClientArg("GIOPInterop");

        int fragmentSize = 32;
        Properties clientProps = Options.getExtraClientProperties();
        clientProps.put(ORBConstants.GIOP_FRAGMENT_SIZE, "" + fragmentSize);

        Controller server = createServer("corba.msgtypes.Server");
        Controller client = createClient("corba.msgtypes.Client");

        server.start();
        client.start();

        client.waitFor(60000);

        if (client.exitValue() != Controller.SUCCESS) {
            errors++;
            printFinishedTest("GIOPInteropTest FAILED, Client exit value = " +
                         client.exitValue());
        } else {
            if (server.finished()) {
                errors++;
                printFinishedTest("GIOPInteropTest FAILED, Server crashed");
            } else {
                printFinishedTest("GIOPInteropTest PASSED");
            }
        }

        client.stop();
        server.stop();
    }
    
    public void runFragmentedReply() throws Throwable {
        Options.getClientArgs().clear();
        Options.addClientArg("FragmentedReply");

        int fragmentSize = 32;
        Properties serverProps = Options.getExtraServerProperties();
        serverProps.put(ORBConstants.GIOP_FRAGMENT_SIZE, "" + fragmentSize);

        Controller server = createServer("corba.msgtypes.Server");
        Controller client = createClient("corba.msgtypes.Client");

        server.start();
        client.start();

        client.waitFor(60000);

        if (client.exitValue() != Controller.SUCCESS) {
            errors++;
            printFinishedTest("FragmentedReplyTest FAILED, Client exit value = "
                         + client.exitValue());
        } else {
            if (server.finished()) {
                errors++;
                printFinishedTest("FragmentedReplyTest FAILED, Server crashed");
            } else {
                printFinishedTest("FragmentedReplyTest PASSED");
            }
        }

        client.stop();
        server.stop();
    }
    
    public void runHeaderPaddingTest() throws Throwable {
        Options.getClientArgs().clear();
        Options.addClientArg("HeaderPaddingTest");

        Properties clientProps = Options.getExtraClientProperties();
        clientProps.put(ORBConstants.GIOP_VERSION, GIOP_version[2]);
        clientProps.put(ORBConstants.GIOP_12_BUFFMGR, "" + GROW);

        Controller server = createServer("corba.msgtypes.Server");
        Controller client = createClient("corba.msgtypes.Client");

        server.start();
        client.start();

        client.waitFor(60000);

        if (client.exitValue() != Controller.SUCCESS) {
            errors++;
            printFinishedTest("HeaderPaddingTest FAILED, Client exit value = "
                         + client.exitValue());
        } else {
            if (server.finished()) {
                errors++;
                printFinishedTest("HeaderPaddingTest FAILED, Server crashed");
            } else {
                printFinishedTest("HeaderPaddingTest PASSED");
            }
        }

        client.stop();
        server.stop();
    }  
    
}

