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

package pi.clientrequestinfo;

import org.omg.CORBA.*;
import org.omg.CosNaming.*;
import com.sun.corba.se.impl.corba.AnyImpl;
import com.sun.corba.se.impl.orbutil.ORBConstants;
import com.sun.corba.se.impl.interceptors.*;
import org.omg.PortableInterceptor.*;
import corba.framework.*;

import java.rmi.*;

import java.util.*;
import java.io.*;
import javax.naming.*;
import javax.rmi.*;

public class RMIClient 
    extends ClientCommon
    implements InternalProcess 
{
    // Reference to hello object
    private helloIF helloRef;
    
    // Reference to hello object to be forwarded to:
    private helloIF helloRefForward;

    // Initial naming context
    InitialContext initialNamingContext;

    // Names for JNDI lookup:
    public static final String NAME1 = "hello2";
    public static final String NAME2 = "hello2Forward";

    public static void main(String args[]) {
	try {
	    (new RMIClient()).run( System.getProperties(),
		                args, System.out, System.err, null );
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
	TestInitializer.out = out;
	this.out = out;
	this.err = err;

	out.println( "===================================" );
	out.println( "Instantiating ORB for RMI/IIOP test" );
	out.println( "===================================" );

	out.println( "+ Creating ORB..." );
	createORB( args );

	// Inform the JNDI provider of the ORB to use and create intial
	// naming context:
        out.println( "+ Creating initial naming context..." );
	Hashtable env = new Hashtable();
	env.put( "java.naming.corba.orb", orb );
	initialNamingContext = new InitialContext( env );

        try {
            // Test ClientInterceptor
            testClientRequestInfo();
        } finally {
            finish() ;
        }
    }

    /**
     * Clear invocation flags of helloRef and helloRefForward
     */
    protected void clearInvoked() 
	throws Exception
    {
	helloRef.clearInvoked();
	helloRefForward.clearInvoked();
    }

    /**
     * Invoke the method with the given name on the object
     */
    protected void invokeMethod( String methodName )
        throws Exception
    {
	try {
	    // Make an invocation:
	    if( methodName.equals( "sayHello" ) ) {
		helloRef.sayHello();
	    }
	    else if( methodName.equals( "saySystemException" ) ) {
		helloRef.saySystemException();
	    }
	    else if( methodName.equals( "sayUserException" ) ) {
		helloRef.sayUserException();
	    }
	    else if( methodName.equals( "sayOneway" ) ) {
		helloRef.sayOneway();
	    }
	    else if( methodName.equals( "sayArguments" ) ) {
		helloRef.sayArguments( "one", 2, true );
	    }
	}
	catch( RemoteException e ) {
	    throw (Exception)e.detail;
	}
    }

    /**
     * Return true if the method was invoked
     */
    protected boolean wasInvoked() 
	throws Exception 
    {
        return helloRef.wasInvoked();
    }

    /**
     * Return true if the method was forwarded
     */
    protected boolean didForward() 
	throws Exception
    {
        return helloRefForward.wasInvoked();
    }

    /**
     * Perform ClientRequestInfo tests
     */
    protected void testClientRequestInfo() 
	throws Exception 
    {
        super.testClientRequestInfo();
    }

    /**
     * One-way test not applicable for RMI case.  Override it.
     */
    protected void testOneWay() throws Exception {
	out.println( "+ OneWay test not applicable for RMI.  Skipping..." );
    }

    /**
     * Re-resolves all references to eliminate any cached ForwardRequests
     * from the last invocation
     */
    protected void resolveReferences() 
	throws Exception 
    {
        out.println( "    + resolving references..." );
        out.println( "      - disabling interceptors..." );
        SampleClientRequestInterceptor.enabled = false;
        // Resolve the hello object.
	out.println( "      - " + NAME1 );
        helloRef = resolve( NAME1 );
        // The initializer will store the location the interceptors should
        // use during a normal request:
        TestInitializer.helloRef = (org.omg.CORBA.Object)helloRef;
	out.println( "      - " + NAME2 );
        helloRefForward = resolve( NAME2 );
        // The initializer will store the location the interceptors should
        // use during a forward request:
        TestInitializer.helloRefForward = 
	    (org.omg.CORBA.Object)helloRefForward;
        out.println( "      - enabling interceptors..." );
        SampleClientRequestInterceptor.enabled = true;
    }

    /**
     * Implementation borrwed from corba.socket.HelloClient.java test
     */
    private helloIF resolve(String name)
	throws Exception
    {
	java.lang.Object obj = initialNamingContext.lookup( name );
	helloIF helloRef = (helloIF)PortableRemoteObject.narrow( 
	    obj, helloIF.class );
        
        return helloRef;
    }
    
}



