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

package pi.serverinterceptor;

import org.omg.CORBA.*;
import org.omg.CosNaming.*;
import com.sun.corba.se.impl.corba.AnyImpl;
import com.sun.corba.se.spi.orbutil.ORBConstants;
import com.sun.corba.se.impl.interceptors.*;
import org.omg.PortableInterceptor.*;
import org.omg.IOP.*;
import org.omg.IOP.CodecPackage.*;
import org.omg.IOP.CodecFactoryPackage.*;
import corba.framework.*;

import java.util.*;
import java.io.*;
import java.rmi.*;
import javax.naming.*;
import javax.rmi.*;

import ServerRequestInterceptor.*;

/**
 * Common base class for RMI client test code
 */
public abstract class RMIClient 
    extends ClientCommon
{
    // The hello object to make invocations on.
    helloIF helloRef;

    // Reference to hello object to be forwarded to.
    helloIF helloRefForward;

    // RMI initial naming context
    InitialContext initialNamingContext;

    // to be invoked from subclasses after the ORB is created.
    public void run( Properties environment, String args[], PrintStream out,
	             PrintStream err, Hashtable extra) 
        throws Exception
    {
	this.out = out;
	this.err = err;

	out.println( "+ Creating initial naming context..." );
	Hashtable env = new Hashtable();
	env.put( "java.naming.corba.orb", orb );
	initialNamingContext = new InitialContext( env );

        // Obey the server's commands:
        obeyServer();
    }

    void resolveReferences() throws Exception {
        out.println( "    - Resolving Hello1..." );
        // Look up reference to hello object on server:
        helloRef = resolve( "Hello1" );
        out.println( "    - Resolved." );

        out.println( "    - Resolving Hello1Forward..." );
	helloRefForward = resolve( "Hello1Forward" );
        out.println( "    - Resolved." );
    }

    String syncWithServer() throws Exception {
        return helloRef.syncWithServer( exceptionRaised );
    }

    /**
     * Invoke the method with the given name on the object
     */
    protected void invokeMethod( String methodName ) 
	throws Exception 
    {
	try {
	    if( methodName.equals( "sayHello" ) ) {
		helloRef.sayHello();
	    }
	    else if( methodName.equals( "sayOneway" ) ) {
		helloRef.sayOneway();
	    }
	    else if( methodName.equals( "saySystemException" ) ) { 
		helloRef.saySystemException();
	    }
	    else if( methodName.equals( "sayUserException" ) ) { 
		try {
		    helloRef.sayUserException();
		    out.println( "    - Did not catch ForwardRequest user " +
			"exception (error)" );
		    throw new RuntimeException( 
			"Did not catch ForwardRequest user exception " +
			"on sayUserException" );
		}
		catch( ForwardRequest e ) {
		    out.println( "    - Caught ForwardRequest user " +
			"exception (ok)" );
		}
	    }
	}
	catch( RemoteException e ) {
	    throw (Exception)e.detail;
	}
    }
    
    /**
     * Resolves name using RMI
     */
    helloIF resolve(String name)
	throws Exception
    {
	java.lang.Object obj = initialNamingContext.lookup( name );
	helloIF helloRef = (helloIF)PortableRemoteObject.narrow(
	    obj, helloIF.class );

        return helloRef;
    }
    

}


