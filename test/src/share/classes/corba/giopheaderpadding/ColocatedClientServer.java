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
// Created       : 2003 Apr 17 (Thu) 17:05:00 by Harold Carr.
// Last Modified : 2004 Jan 31 (Sat) 10:57:47 by Harold Carr.
//

package corba.giopheaderpadding;

import java.util.Properties;
import javax.naming.InitialContext;
import org.omg.CORBA.ORB;
import corba.hcks.C;
import corba.hcks.U;

public class ColocatedClientServer 
{
    public static final String baseMsg = ColocatedClientServer.class.getName();
    public static final String main = baseMsg + ".main";

    // REVISIT: FRAMEWORK DEVELOPMENT
    // REMOVE THIS LATER.
    // Necessary so calls not going through locals do not hang
    // until I implement the reader thread/work split.
    public static int fragmentSize = -1;
    //public static int fragmentSize = C.DEFAULT_FRAGMENT_SIZE;

    public static ORB orb;
    public static InitialContext initialContext;
    public static boolean isColocated = false;
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
	    orb = ORB.init(av, props);
	    U.sop(main + " : creating ORB.");
	    Server.orb = (com.sun.corba.se.spi.orb.ORB) orb;
	    Client.orb = orb;
	    
	    // Share a naming context between client and server
	    // so Util.isLocal is true.

	    // Use the same ORB which has interceptor properties set.
	    U.sop(main + " : creating InitialContext.");
	    initialContext = C.createInitialContext(orb);
	    Server.initialContext = initialContext;
	    Client.initialContext = initialContext;
	    
	    ServerThread ServerThread = new ServerThread(av);
	    ServerThread.start();
	    synchronized (signal) {
		try {
		    signal.wait();
		} catch (InterruptedException e) {
		    ;
		}
	    }
	    Client.main(av);
	} catch (Exception e) {
	    U.sopUnexpectedException(main, e);
	    System.exit(1);
	}
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
