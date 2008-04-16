/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2004-2007 Sun Microsystems, Inc. All rights reserved.
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

// Test the request partitioning feature.
//
// This test creates a Server and a Client.
// The Server is configured to recieve requests for a given thread pool.
//
// Created       : 2004 May 23 by Charlie Hunt.
// Last Modified : 2004 May 23 by Charlie Hunt.
//

package corba.requestpartitioning;

import com.sun.corba.se.impl.orbutil.ORBConstants;
import corba.framework.Controller;
import corba.framework.CORBATest;
import corba.framework.Options;
import java.util.Properties;

public class RequestPartitioningTest
    extends
	CORBATest
{
    public static final String thisPackage =
	RequestPartitioningTest.class.getPackage().getName();

    private final static int CLIENT_TIMEOUT = 90000;

    protected void doTest()
	throws
	    Throwable
    {
        // Run test with DirectByteBuffers
        Controller orbd = createORBD();
        orbd.start();

        Properties serverProps = Options.getServerProperties();
        serverProps.setProperty(ORBConstants.ALWAYS_ENTER_BLOCKING_READ_PROPERTY, "true");
//        serverProps.setProperty(ORBConstants.DEBUG_PROPERTY,"transport,giop");
        Controller server = createServer(thisPackage + ".Server","Server1");
        server.start();

        Properties clientProps = Options.getClientProperties();
        clientProps.setProperty(ORBConstants.ALWAYS_ENTER_BLOCKING_READ_PROPERTY, "true");
//        clientProps.setProperty(ORBConstants.DEBUG_PROPERTY,"transport,giop");
        Controller client = createClient(thisPackage + ".Client", "Client1");
        client.start();

        client.waitFor(CLIENT_TIMEOUT);

        client.stop();
        server.stop();

        serverProps.setProperty(ORBConstants.DISABLE_DIRECT_BYTE_BUFFER_USE_PROPERTY, "true");
        serverProps.setProperty(ORBConstants.ALWAYS_ENTER_BLOCKING_READ_PROPERTY, "false");
        server = createServer(thisPackage + ".Server","Server2");
        server.start();

        clientProps.setProperty(ORBConstants.DISABLE_DIRECT_BYTE_BUFFER_USE_PROPERTY, "true");
        clientProps.setProperty(ORBConstants.ALWAYS_ENTER_BLOCKING_READ_PROPERTY, "false");
        client = createClient(thisPackage + ".Client", "Client2");
        client.start();

        client.waitFor(CLIENT_TIMEOUT);

        client.stop();
        server.stop();

        orbd.stop();
    }
}

// End of file.
