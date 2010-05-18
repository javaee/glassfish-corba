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
// Created       : 2005 Jun 10 (Fri) 10:04:09 by Harold Carr.
// Last Modified : 2005 Sep 30 (Fri) 15:38:24 by Harold Carr.
//

package corba.folb;

import java.util.Properties;

import corba.framework.CORBATest;
import corba.framework.Controller;
import corba.framework.Options;

import com.sun.corba.se.spi.orbutil.ORBConstants;

/**
 * @author Harold Carr
 */
public class FolbTest
    extends
	CORBATest
{
    protected void doTest()
	throws Exception
    {
	String thisPackage = FolbTest.class.getPackage().getName();

	Controller orbd = createORBD();
	Controller server;
	Controller client;

	orbd.start();

	//
	// Main test
	//

	server = createServer(thisPackage+"."+"Server", "Server");
	client = createClient(thisPackage+"."+"Client", "Client");

	server.start();
	client.start();

	client.waitFor(180000);

	client.stop();
	server.stop();

	//
	// ClientMulti test
	//

	server = createServer(thisPackage+"."+"Server", "ServerMulti");
	client = createClient(thisPackage+"."+"ClientMulti", "ClientMulti");
	server.start();
	client.start();
	
	client.waitFor(300000); // 5 minutes

	client.stop();
	server.stop();
        
	//
	// ClientCircular test
	//

	server = createServer(thisPackage+"."+"Server", "ServerCircular");
	client = createClient(thisPackage+"."+"ClientCircular", "ClientCircular");
	server.start();
	client.start();
	
	client.waitFor(180000);

	client.stop();
	server.stop();

	//
	// ClientWaitTimeout test
	//

	server = createServer(thisPackage+"."+"Server", "ServerWaitTimeout");
	client = createClient(thisPackage+"."+"ClientWaitTimeout", "ClientWaitTimeout");
	server.start();
	client.start();
	
	client.waitFor(120000);

	client.stop();
	server.stop();

	//
	// Cleanup
	//

	orbd.stop();
    }
}

// End of file.
