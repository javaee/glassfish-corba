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

import ServerRequestInfo.*;

public abstract class POAClient 
    extends ClientCommon 
{
    // The hello object to make invocations on.
    hello helloRef;

    // Reference to hello object to be forwarded to.
    hello helloRefForward;

    // Reference to hello child 1
    hello helloChild1;

    // Reference to hello child 2
    hello helloChild2;

    // To be invoked after the orb is created by subclasses.
    public void run( Properties environment, String args[], PrintStream out,
	             PrintStream err, Hashtable extra) 
        throws Exception
    {
	this.out = out;
	this.err = err;

        // Obey the server's commands:
        obeyServer();
    }

    void resolveReferences() throws Exception {
        out.println( "    - Resolving Hello1..." );
        // Look up reference to hello object on server:
        helloRef = resolve( orb, "Hello1" );
        out.println( "    - Resolved." );

        out.println( "    - Resolving Hello1Forward..." );
	helloRefForward = resolve( orb, "Hello1Forward" );
        out.println( "    - Resolved." );

        out.println( "    - Resolving HelloChild1..." );
	helloChild1 = resolve( orb, "HelloChild1" );
        out.println( "    - Resolved." );

        out.println( "    - Resolving HelloChild2..." );
	helloChild2 = resolve( orb, "HelloChild2" );
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
	if( methodName.equals( "sayHello" ) ) {
	    helloRef.sayHello();
	}
	else if( methodName.equals( "child1.sayHello" ) ) {
	    helloChild1.sayHello();
	}
	else if( methodName.equals( "child2.sayHello" ) ) {
	    helloChild2.sayHello();
	}
	else if( methodName.equals( "sayOneway" ) ) {
	    helloRef.sayOneway();
	}
	else if( methodName.equals( "saySystemException" ) ) { 
	    helloRef.saySystemException();
	}
	else if( methodName.equals( "child1.saySystemException" ) ) { 
	    helloChild1.saySystemException();
	}
	else if( methodName.equals( "child2.saySystemException" ) ) { 
	    helloChild2.saySystemException();
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
	    catch( ExampleException e ) {
		out.println( "    - Caught ExampleException user " +
		    "exception (ok)" );
	    }
	}
	else if( methodName.equals( "sayInvokeAgain.sayHello" ) ) {
	    helloRef.sayInvokeAgain( INVOKE_SAY_HELLO.value );
	}
	else if( methodName.equals( "sayInvokeAgain.saySystemException" ) ) {
	    helloRef.sayInvokeAgain( INVOKE_SAY_SYSTEM_EXCEPTION.value );
	}
	else {
	    throw new RuntimeException( "Unknown method: '" + 
		methodName + "'" );
	}
    }
    
    /**
     * Implementation borrwed from corba.socket.HelloClient.java test
     */
    static hello resolve(ORB orb, String name)
	throws Exception
    {
        // Get the root naming context
        org.omg.CORBA.Object objRef = 
            orb.resolve_initial_references("NameService");
        NamingContext ncRef = NamingContextHelper.narrow(objRef);
        
        // resolve the Object Reference in Naming
        NameComponent nc = new NameComponent(name, "");
        NameComponent path[] = {nc};
        hello helloRef = helloHelper.narrow(ncRef.resolve(path));
        
        return helloRef;
    }
    

}

