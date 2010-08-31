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
//
// Created       : 2003 Apr 17 (Thu) 17:05:00 by Harold Carr.
// Last Modified : 2005 Jun 01 (Wed) 11:05:35 by Harold Carr.
//

package corba.folb_8_1;

import java.util.Properties;
import org.omg.CORBA.ORB;

public class ColocatedCS
{
    public static final String baseMsg = ColocatedCS.class.getName();
    public static final String main = baseMsg + ".main";

    public static ORB orb;
    public static boolean isColocated = false;
    public static boolean clientTwoRefs = false;
    public static java.lang.Object signal = new java.lang.Object();

    public static void main (String[] av)
    {
	isColocated = true; // Used by Client and Server.

	try {
	    // Share an ORB between a client and server.
	    // So ClientDelegate.isLocal currently succeeds.

	    Properties props = new Properties();
	    props.setProperty("com.sun.corba.se.ORBAllowLocalOptimization",
			      "true");
	    Client.setProperties(props);
	    Server.setProperties(props, Common.socketPorts);
	    System.out.println(main + " : creating ORB.");
	    orb = ORB.init(av, props);
	    Server.orb = orb;
	    if (clientTwoRefs) {
		ClientTwoRefs.orb = orb;
	    } else {
		Client.orb = orb;
	    }
	    
	    ServerThread ServerThread = new ServerThread(av);
	    ServerThread.start();
	    synchronized (signal) {
		try {
		    signal.wait();
		} catch (InterruptedException e) {
		    ;
		}
	    }
	    if (clientTwoRefs) {
		ClientTwoRefs.main(av);
	    } else {
		Client.main(av);
	    }
	    if (Client.foundErrors) {
		System.out.println("FAIL");
		System.exit(1);
	    }
	} catch (Throwable t) {
	    System.out.println(main);
	    t.printStackTrace(System.out);
	    System.exit(1);
	}
	System.out.println(main + " done");
    }
}

class ServerThread extends Thread
{
    String[] args;
    ServerThread (String[] args)
    {
	this.args = args;
    }
    public void run ()
    {
	Server.main(args);
    }
}

// End of file.
