/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2000-2007 Sun Microsystems, Inc. All rights reserved.
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

package pi.serverrequestinfo;

import org.omg.CORBA.*;
import org.omg.CORBA.ORBPackage.*;
import org.omg.CosNaming.*;
import org.omg.PortableServer.*;
import org.omg.PortableServer.POAPackage.*;
import org.omg.PortableServer.ServantLocatorPackage.*;
import org.omg.PortableInterceptor.*;
import com.sun.corba.se.impl.interceptors.*;
import corba.framework.*;
import com.sun.corba.se.spi.orbutil.ORBConstants;

import java.util.*;
import java.io.*;

import ServerRequestInfo.*;

/**
 * Tests POA Local invocations (with a co-located orb)
 */
public class POALocalServer 
    extends POAServer
{
    // Object to synchronize on to wait for server to start:
    private java.lang.Object syncObject;
    
    public static void main(String args[]) {
	final String[] arguments = args;
	try {
	    final POALocalServer server = new POALocalServer();

	    TestInitializer.out = System.out;
	    server.out = System.out;
	    server.err = System.err;

	    server.out.println( "===============================" );
	    server.out.println( "Creating ORB for POA Local test" );
	    server.out.println( "===============================" );

	    // For this test, start both the client and the server using
	    // the same ORB.
	    System.out.println( "+ Creating ORB for client and server..." );
	    Properties props = new Properties();
	    server.createORB( args, props );

	    System.out.println( "+ Starting Server..." );
	    server.syncObject = new java.lang.Object();
	    new Thread() {
		public void run() {
		    try {
			server.run(
			    System.getProperties(),
			    arguments, System.out,
			    System.err, null );
		    }
		    catch( Exception e ) {
			System.err.println( "SERVER CRASHED:" );
			e.printStackTrace( System.err );
			System.exit( 1 );
		    }
		}
	    }.start();

	    // Wait for server to start...
	    synchronized( server.syncObject ) {
		try {
		    server.syncObject.wait();
		}
		catch( InterruptedException e ) {
		    // ignore.
		}
	    }

	    // Start client:
	    System.out.println( "+ Starting Client..." );
	    POALocalClient client = new POALocalClient( server.orb );
	    client.run( System.getProperties(),
			args, System.out, System.err, null );
            System.exit( 0 );
	}
	catch( Exception e ) {
	    e.printStackTrace( System.err );
	    System.exit( 1 );
	}
    }

    public void run( Properties environment, String args[], PrintStream out,
	             PrintStream err, Hashtable extra) 
        throws Exception
    {
	super.run( environment, args, out, err, extra );
    }

    void handshake() {
	// notify main that client can launch now:
	synchronized( syncObject ) {
	    syncObject.notify();
	}
    }

    void waitForClients() {
	// NOP for this test.
    }

}
