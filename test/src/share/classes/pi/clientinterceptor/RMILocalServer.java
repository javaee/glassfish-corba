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

package pi.clientinterceptor;

import org.omg.CORBA.*;
import org.omg.CosNaming.*;
import org.omg.CORBA.ORBPackage.*;
import org.omg.PortableServer.*;
import org.omg.PortableServer.POAPackage.*;
import org.omg.PortableInterceptor.*;
import corba.framework.*;
import com.sun.corba.se.impl.orbutil.ORBConstants;
import com.sun.corba.se.impl.interceptors.*;

import java.util.*;
import java.io.*;
import javax.naming.*;

/**
 * Server for RMI/IIOP version of test
 */
public class RMILocalServer 
    implements Observer
{
    // Set from run()
    private PrintStream out;
    
    private com.sun.corba.se.spi.orb.ORB orb;

    InitialContext initialNamingContext;

    public void run( com.sun.corba.se.spi.orb.ORB orb, java.lang.Object syncObject,
		     Properties environment, String args[], 
		     PrintStream out, PrintStream err, Hashtable extra) 
        throws Exception
    {
	this.out = out;

        out.println( "+ Creating Initial naming context..." );
        // Inform the JNDI provider of the ORB to use and create intial
        // naming context:
        Hashtable env = new Hashtable();
        env.put( "java.naming.corba.orb", orb );
        initialNamingContext = new InitialContext( env );

	rebindObjects();

        // no handshake required here:
        //out.println("Server is ready.");
        //out.flush();

	// Notify client to wake up:
	synchronized( syncObject ) {
	    syncObject.notifyAll();
	}

	// wait for invocations from clients
	java.lang.Object sync = new java.lang.Object();
	synchronized (sync) {
	    sync.wait();
	}

    }

    private void rebindObjects() 
	throws Exception
    {
        out.println( "+ Creating and binding hello objects..." );
	createAndBind( "Hello1" );
	createAndBind( "Hello1Forward" );
    }
    
    /**
     * Creates and binds a hello object using RMI
     */
    public void createAndBind (String name)
	throws Exception
    {
	helloRMIIIOP obj = new helloRMIIIOP( out );
	initialNamingContext.rebind( name, obj );

	// Add this server as an observer so that when resetServant is called
	// we can rebind.
	helloDelegate delegate = obj.getDelegate();
	delegate.addObserver( this );
    }

    public void update( Observable o, java.lang.Object arg ) {
	try {
	    rebindObjects();
	}
	catch( Exception e ) {
	    System.err.println( "rebindObjects() failed! " + e );
	    throw new RuntimeException( "rebindObjects failed!" );
	}
    }

}
