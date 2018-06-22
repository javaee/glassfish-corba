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

// Test the ability to turn away a rogue client.
//
// This test creates a Server and a well behaved Client that simply pass
// a String array between. It also creates another Rogue Client that attempts
// a variety of rogue attack by connecting to the ORBInitialPort and sending
// partial GIOP headers, GIOP messages and making a large number of connections.

package corba.rogueclient;

import java.util.Properties;
import com.sun.corba.ee.spi.misc.ORBConstants;
import corba.framework.Controller;
import corba.framework.CORBATest;
import corba.framework.Options;

public class RogueClientTest extends CORBATest {

    public static final String thisPackage =
        RogueClientTest.class.getPackage().getName();

    private final static int CLIENT_TIMEOUT = 250000;

    protected void doTest() throws Throwable
    {
        Controller orbd = createORBD();
        orbd.start();
//        Properties serverProps = Options.getServerProperties();
//        serverProps.setProperty(ORBConstants.DEBUG_PROPERTY,"transport,giop");
        Controller server = createServer(thisPackage + ".Server","Server");
        server.start();

//        Properties rogueClientProps = Options.getClientProperties();
//        rogueClientProps.setProperty(ORBConstants.DEBUG_PROPERTY,"transport,giop");
        Controller rogueClient = createClient(thisPackage + ".RogueClient","RogueClient");

        // put some tougher than defaults settings on well behaved client
        // so command line property for read timeouts gets executed
        Properties clientProps = Options.getClientProperties();
        clientProps.setProperty(ORBConstants.TRANSPORT_TCP_TIMEOUTS_PROPERTY,
                                "150:2500:25");
        Controller client = createClient(thisPackage + ".Client","Client");


        client.start();

        rogueClient.start();

        client.waitFor(CLIENT_TIMEOUT);
        rogueClient.waitFor(CLIENT_TIMEOUT);

        client.stop();
        rogueClient.stop();
        server.stop();
        orbd.stop();
    }
}

// End of file.

