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

package pi.serverinterceptor;

import org.omg.CORBA.*;
import org.omg.CORBA.ORBPackage.*;
import org.omg.CosNaming.*;
import org.omg.PortableServer.*;
import org.omg.PortableServer.POAPackage.*;
import org.omg.PortableServer.ServantLocatorPackage.*;
import org.omg.PortableInterceptor.*;
import com.sun.corba.se.impl.interceptors.*;
import corba.framework.*;
import com.sun.corba.se.impl.orbutil.ORBConstants;

import java.util.*;
import java.io.*;

import ServerRequestInterceptor.*;
import java.rmi.*;
import javax.rmi.*;
import javax.naming.*;

public abstract class DSIRMIServer 
    extends ServerCommon 
{
    InitialContext initialNamingContext;

    private static final String hello2Id = "qwerty";
    private String hello2IOR;

    private TestServantLocator servantLocator;
    
    public void run( Properties environment, String args[], PrintStream out,
	             PrintStream err, Hashtable extra) 
        throws Exception
    {
	out.println( "+ Creating Initial naming context..." );
	// Inform the JNDI provider of the ORB to use and create
	// initial naming context:
	Hashtable env = new Hashtable();
	env.put( "java.naming.corba.orb", orb );
	initialNamingContext = new InitialContext( env );

        // Set up hello object:
        out.println( "+ Creating and binding Hello1 object..." );
        TestInitializer.helloRef = createAndBind( "Hello1", 
						  "[Hello1]" );

        out.println( "+ Creating and binding Hello1Forward object..." );
        TestInitializer.helloRefForward = createAndBind( "Hello1Forward",
							 "[Hello1Forward]" ); 

	handshake();

	// Test ServerInterceptor
	testServerInterceptor();

	// Notify client it's time to exit.
	exitClient();

	waitForClients();
    }

    abstract void handshake();

    abstract void waitForClients();

    /**
     * Creates and binds a hello object using RMI
     */
    public org.omg.CORBA.Object createAndBind ( String name, 
                                                String symbol )
	throws Exception
    {
	// create and register it with RMI
	helloDSIDeprecatedServant obj = new helloDSIDeprecatedServant( 
	    orb, out, symbol );
	orb.connect( obj );
	initialNamingContext.rebind( name, obj );

	java.lang.Object o = initialNamingContext.lookup( name );
	return (org.omg.CORBA.Object)PortableRemoteObject.narrow( o, 
            org.omg.CORBA.Object.class );
    }

    /** 
     * Overridden from ServerCommon.  Oneway calls are not supported in RMI.
     */
    void testInvocation( int mode, 
                         String correctOrder,
                         String methodName,
                         String correctMethodOrder,
                         boolean exceptionExpected )
        throws Exception 
    {
        // Rebind each time so that location forward information is
        // wiped out.  See CDRInputStream1_0 readObject.  This is necessary 
	// because the local case will always return the exact same object
	// on the client side otherwise.

        // Set up hello object:
        out.println( "+ Creating and binding Hello1 object..." );
        TestInitializer.helloRef = createAndBind( "Hello1", 
						  "[Hello1]" );

        out.println( "+ Creating and binding Hello1Forward object..." );
        TestInitializer.helloRefForward = createAndBind( "Hello1Forward",
							 "[Hello1Forward]" ); 


	if( !methodName.equals( "sayOneway" ) ) {
	    super.testInvocation( mode, correctOrder, methodName,
				  correctMethodOrder, exceptionExpected );
	}
    }
}

