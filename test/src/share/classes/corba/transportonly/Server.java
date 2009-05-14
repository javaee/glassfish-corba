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
package corba.transportonly;

/*
 * Created       : 2003 Jan 16 (Thu) 10:08:56 by Harold Carr.
 * Last Modified : 2003 Apr 24 (Thu) 18:06:47 by Harold Carr.
 */


import com.sun.corba.se.pept.transport.EventHandler;

import com.sun.corba.se.spi.orb.ORB;


public class Server
{
    public static void main (String[] av)
    {
	try {
	    ORB orb = (ORB)
		org.omg.CORBA.ORB.init((String[])null, null);

	    AcceptorImpl acceptor;

	    acceptor = new AcceptorImpl(orb, 2222, true, 
					false, false, true);
	    orb.getTransportManager().getSelector(0)
		.registerForEvent((EventHandler) acceptor);
	    System.out.println("acceptor 2222 registered");

	    acceptor = new AcceptorImpl(orb, 3333, true, 
					false, true, true);
	    orb.getTransportManager().getSelector(0)
		.registerForEvent((EventHandler) acceptor);
	    System.out.println("acceptor 3333 registered");

	    acceptor = new AcceptorImpl(orb, 4444, false,
					true, false, false);
	    orb.getTransportManager().getSelector(0)
		.registerForEvent((EventHandler) acceptor);
	    System.out.println("acceptor 4444 registered.");

	    Object wait = new Object();
	    synchronized (wait) {
		wait.wait();
	    }
	} catch (Throwable t) {
	    System.out.println("Error: " + t);
	    t.printStackTrace();
	}
    }
}

// End of file.
