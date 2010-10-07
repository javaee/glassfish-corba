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
import java.rmi.*;
import javax.rmi.*;
import javax.naming.*;

public abstract class DSIRMIServer 
    extends ServerCommon 
    implements helloDelegate.ClientCallback
{
    InitialContext initialNamingContext;

    public void run( Properties environment, String args[], PrintStream out,
	             PrintStream err, Hashtable extra) 
        throws Exception
    {
        try {
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
            testServerRequestInfo();
        } finally {
            finish() ;

            // Notify client it's time to exit.
            exitClient();

            waitForClients();
        }
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
	    orb, out, symbol, this );
	orb.connect( obj );
	initialNamingContext.rebind( name, obj );

	java.lang.Object o = initialNamingContext.lookup( name );
	return (org.omg.CORBA.Object)PortableRemoteObject.narrow( o,
	    org.omg.CORBA.Object.class );
    }

    /**
     * One-way test not applicable for RMI case.  Override it.
     */
    protected void testOneWay() throws Exception {
        out.println( "+ OneWay test not applicable for RMI.  Skipping..." );
    }

    /**
     * Passes in the appropriate valid and invalid repository ids for RMI
     */
    protected void testAttributesValid() 
	throws Exception
    {
	testAttributesValid( 
	    "IDL:ServerRequestInfo/hello:1.0",
	    "IDL:ServerRequestInfo/goodbye:1.0" );
    }


    // ClientCallback interface

    public String sayHello() {
	String result = "";

	out.println( 
	    "    + ClientCallback: resolving and invoking sayHello()..." );
	try {
	    hello helloRef = resolve( "Hello1" );
	    result = helloRef.sayHello();
	}
	catch( Exception e ) {
	    e.printStackTrace();
	    throw new RuntimeException( "ClientCallback: Exception thrown." );
	}

	return result;
    }

    public void saySystemException() {
	out.println( 
	    "    + ClientCallback: resolving and invoking " + 
	    "saySystemException()..." );
	try {
	    hello helloRef = resolve( "Hello1" );
	    helloRef.saySystemException();
	}
	catch( SystemException e ) {
	    // expected.
	    throw e;
	}
	catch( Exception e ) {
	    e.printStackTrace();
	    throw new RuntimeException( "ClientCallback: Exception thrown." );
	}
    }

    /**
     * Resolves name using RMI
     */
    hello resolve(String name)
        throws Exception
    {
        java.lang.Object obj = initialNamingContext.lookup( name );
        hello helloRef = (hello)helloHelper.narrow( (org.omg.CORBA.Object)obj);

        return helloRef;
    }

}

