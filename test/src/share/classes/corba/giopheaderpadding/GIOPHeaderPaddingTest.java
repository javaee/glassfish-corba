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
//
// Created       : 2003 Apr 09 (Wed) 16:31:43 by Harold Carr.
// Last Modified : 2004 Jan 31 (Sat) 09:54:44 by Harold Carr.
//

package corba.giopheaderpadding;

import corba.framework.Controller;
import corba.framework.CORBATest;

import java.util.Properties;

import com.sun.corba.se.impl.orbutil.ORBConstants;
import com.sun.corba.se.spi.orb.ORB;

import corba.framework.*;

public class GIOPHeaderPaddingTest extends CORBATest {

    public static final String thisPackage =
	GIOPHeaderPaddingTest.class.getPackage().getName();

    protected void doTest() throws Throwable {
        if (test.Test.useJavaSerialization()) {
            return;
        }

        Controller orbd = createORBD();
        orbd.start();

        Properties clientProps = Options.getClientProperties();
        clientProps.put("org.omg.PortableInterceptor.ORBInitializerClass." +
                        "corba.giopheaderpadding.Client", "true");
        clientProps.put("org.omg.PortableInterceptor.ORBInitializerClass." +
                        "corba.giopheaderpadding.Server", "true");
        clientProps.put(ORBConstants.GIOP_VERSION, "1.2");
        clientProps.put(ORBConstants.GIOP_12_BUFFMGR, "0"); // GROW

        Properties serverProps = Options.getServerProperties();
        serverProps.put("org.omg.PortableInterceptor.ORBInitializerClass." +
                        "corba.giopheaderpadding.Server", "true");
        serverProps.put(ORBConstants.GIOP_VERSION, "1.2");
        serverProps.put(ORBConstants.GIOP_12_BUFFMGR, "0"); // GROW

        doTestType("Server", "Server",
                   "Client", "Client");

        Controller colocatedClientServer = 
            createClient(thisPackage + ".ColocatedClientServer",
                         "colocatedClientServer");
        colocatedClientServer.start();
        colocatedClientServer.waitFor();
        colocatedClientServer.stop();

        orbd.stop();
    }

    protected void doTestType(String serverMainClass, String serverTestName,
			      String clientMainClass, String clientTestName)
	throws Throwable {

	Controller server = createServer(thisPackage + "." + serverMainClass,
					 serverTestName);
	server.start();

	Controller client = createClient(thisPackage + "." + clientMainClass,
					 clientTestName);
	client.start();
	client.waitFor();
	client.stop();

	server.stop();
    }
}

// End of file.

