/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright (c) 1997-2018 Oracle and/or its affiliates. All rights reserved.
 *
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common Development
 * and Distribution License("CDDL") (collectively, the "License").  You
 * may not use this file except in compliance with the License.  You can
 * obtain a copy of the License at
 * https://oss.oracle.com/licenses/CDDL+GPL-1.1
 * or LICENSE.txt.  See the License for the specific
 * language governing permissions and limitations under the License.
 *
 * When distributing the software, include this License Header Notice in each
 * file and include the License file at LICENSE.txt.
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

package pi.clientinterceptor;

import org.omg.CORBA.*;
import org.omg.CosNaming.*;
import com.sun.corba.ee.impl.corba.AnyImpl;
import com.sun.corba.ee.spi.misc.ORBConstants;
import com.sun.corba.ee.impl.interceptors.*;
import org.omg.PortableInterceptor.*; 
import corba.framework.*;

import java.util.*;
import java.io.*;

import ClientRequestInterceptor.*;

/**
 * Tests DII RMI Local invocation
 */
public class DIIRMILocalClient
    extends ClientCommon
    implements InternalProcess 
{
    // Reference to hello object
    private helloDIIClientStub helloRef;
    
    // Reference to hello object to be forwarded to:
    private helloDIIClientStub helloRefForward;

    // Object to synchronize on to wait for server to start:
    private java.lang.Object syncObject;

    public static void main(String args[]) {
        final String[] arguments = args;

        try {
            System.out.println( "====================================" );
            System.out.println( "Creating ORB for DII RMI Local test" );
            System.out.println( "====================================" );

            final DIIRMILocalClient client = new DIIRMILocalClient();

            TestInitializer.out = System.out;
            client.out = System.out;
            client.err = System.err;

            // For this test, start both the client and the server
            // using the same ORB.
            System.out.println( "+ Creating ORB for client and server..." );
            client.createORB( args );

            System.out.println( "+ Starting Server..." );
            client.syncObject = new java.lang.Object();
            new Thread() {
                public void run() {
                    try {
                        (new OldRMILocalServer()).run(
                                                 client.orb, client.syncObject,
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
            synchronized( client.syncObject ) {
                try {
                    client.syncObject.wait();
                }
                catch( InterruptedException e ) {
                    // ignore.
                }
            }

            // Start client:
            System.out.println( "+ Starting client..." );
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
        try {
            // Test ClientInterceptor
            testClientInterceptor();
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
        // Make an invocation:
        if( methodName.equals( "sayHello" ) ) {
            helloRef.sayHello();
        }
        else if( methodName.equals( "sayException" ) ) {
            helloRef.saySystemException();
        }
        else if( methodName.equals( "sayOneway" ) ) {
            helloRef.sayOneway();
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
     * Perform ClientRequestInterceptor tests
     */
    protected void testClientInterceptor() 
        throws Exception 
    {
        super.testClientInterceptor();
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
        out.println( "      - Hello1" );
        helloRef = resolve( orb, "Hello1" );
        out.println( "      - Hello1Forward" );
        helloRefForward = resolve( orb, "Hello1Forward" );
        // The initializer will store the location the interceptors should
        // use during a forward request:
        TestInitializer.helloRefForward = helloRefForward.getObject();
        out.println( "      - enabling interceptors..." );
        SampleClientRequestInterceptor.enabled = true;
    }

    /**
     * Implementation borrwed from corba.socket.HelloClient.java test
     */
    private helloDIIClientStub resolve(ORB orb, String name)
        throws Exception
    {
        // Get the root naming context
        org.omg.CORBA.Object objRef = 
            orb.resolve_initial_references("NameService");
        NamingContext ncRef = NamingContextHelper.narrow(objRef);
        
        // resolve the Object Reference in Naming
        NameComponent nc = new NameComponent(name, "");
        NameComponent path[] = {nc};
        org.omg.CORBA.Object helloRef = ncRef.resolve(path);
        
        return new helloDIIClientStub( orb, helloRef );
    }

    /**
     * Overridden from ClientCommon.  Resets the servant after each
     * invocation.
     */
    protected void testInvocation( String name, 
                                   int mode,
                                   String correctOrder,
                                   String methodName,
                                   boolean shouldInvokeTarget,
                                   boolean exceptionExpected,
                                   boolean forwardExpected )
        throws Exception
    {
        super.testInvocation( name, mode, correctOrder, methodName,
                              shouldInvokeTarget,
                              exceptionExpected,
                              forwardExpected );

        // Reset the servant:
        helloRef.resetServant();
    }
}
