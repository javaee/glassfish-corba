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
// Created       : 2003 Dec 11 (Thu) 11:04:04 by Harold Carr.
// Last Modified : 2003 Dec 17 (Wed) 21:29:35 by Harold Carr.
//

package corba.legacybootstrapserver;

import java.util.Properties;
import org.omg.CORBA.ORB;
import corba.framework.Controller;
import corba.framework.Options;
import com.sun.corba.se.impl.orbutil.ORBConstants;

public class Server 
{
    public static final String baseMsg = Server.class.getName();
    public static final String main = baseMsg + ".main";

    public static void main(String[] av)
    {
        try {
	    System.out.println(main + " starting");
	    System.out.println(main + " " + getBootstrapFilePathAndName());

	    // Initialize the file.
	    Properties props = new Properties();
	    ORB orb = ORB.init((String[])null, (Properties) null);
	    org.omg.CORBA.Object o = new IServantConnect();
	    orb.connect(o);
	    props.put(Client.initialEntryName, o.toString());
	    Client.writeProperties(props, getBootstrapFilePathAndName());

	    // Set up args.
	    String[] args = { "-InitialServicesFile", 
			      getBootstrapFilePathAndName(),
			      "-ORBInitialPort",
			      Client.getORBInitialPort() };

	    ServerThread serverThread = new ServerThread(args);
	    serverThread.start();

	    // Wait 5 seconds before sending handshake.
	    Thread.sleep(5000);

	    System.out.println(Options.defServerHandshake);

	    Object wait = new Object();
	    synchronized (wait) {
		wait.wait();
	    }
        } catch (Exception e) {
	    System.out.println(main + ": unexpected exception: " + e);
	    e.printStackTrace(System.out);
	    System.exit(1);
        }
	System.exit(Controller.SUCCESS);
    }

    public static String getBootstrapFilePathAndName()
    {
	return
	    //Options.getOutputDirectory()
	    System.getProperty("output.dir")
	    + System.getProperty("file.separator")
	    + Client.bootstrapFilename;
    }
}

class ServerThread extends Thread
{
    String[] av;
    ServerThread (String[] av)
    {
	this.av = av;
    }

    public void run ()
    {
	try {
	    // Start server.
	    com.sun.corba.se.internal.CosNaming.BootstrapServer.main(av);
	} catch (Throwable t) {
	    System.out.println("BootstrapServer.main Throwable:");
	    t.printStackTrace(System.out);
	    System.exit(1);
	}
    }
}

class IServantConnect
    extends
	_IImplBase
{
    public void dummy(){}
}

// End of file.

