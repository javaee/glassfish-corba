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
package corba.cmvt;

import test.Test;
import corba.framework.*;
import java.util.*;
import com.sun.corba.se.impl.orbutil.ORBConstants;

public class CMVTTest extends CORBATest {
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

    private void printEndTest(String result)
    {
        System.out.println(result);
    }

    private void setClient(int version, int strategy){
        Properties clientProps = Options.getExtraClientProperties();

        int fragmentSize = 1024;
        clientProps.put(ORBConstants.GIOP_FRAGMENT_SIZE, "" + fragmentSize);

        clientProps.put(ORBConstants.GIOP_VERSION, GIOP_version[version]);
        clientProps.put(ORBConstants.GIOP_11_BUFFMGR, "" + GIOP_strategy[strategy]);
        clientProps.put(ORBConstants.GIOP_12_BUFFMGR, "" + GIOP_strategy[strategy]);
    }

    private void setServer(int version, int strategy){
        Properties serverProps = Options.getExtraServerProperties();

        serverProps.put(ORBConstants.GIOP_VERSION, GIOP_version[version]);
        serverProps.put(ORBConstants.GIOP_11_BUFFMGR, "" + GIOP_strategy[strategy]);
        serverProps.put(ORBConstants.GIOP_12_BUFFMGR, "" + GIOP_strategy[strategy]);
    }

    private void runTest() throws Throwable{
        Controller server = createServer("corba.cmvt.Server");
        Controller client = createClient("corba.cmvt.Client");

        server.start();
        client.start();

        client.waitFor(60000);

        if (client.exitValue() != Controller.SUCCESS) {
            printEndTest("FAILED, Client exit value = " + client.exitValue());
        } else if (server.finished()) {
            printEndTest("FAILED, Server crashed");
        } else {
            printEndTest("PASSED");
        }

        client.stop();
        server.stop();
    }

    protected void doTest() throws Throwable  
    {
        int errors = 0;

        // Pleasing aesthetics
        System.out.println();

        //1.0 + grow
        setClient(0,0);
        setServer(0,0);
        printBeginTest(0,0,0,0);
        runTest();

        //1.2 + grow
        setClient(2,0);
        setServer(2,0);
        printBeginTest(2,0,2,0);
        runTest();

        //1.2 + stream
        setClient(2,2);
        setServer(2,2);
        printBeginTest(2,2,2,2);
        runTest();


        System.out.print("      Test result : " );
        
        if (errors > 0)
            throw new Exception("Errors detected");

    }
}

