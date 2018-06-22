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

package pi.serverinterceptor;

import com.sun.corba.ee.spi.misc.ORBConstants;
import corba.framework.*;
import java.util.*;

/**
 * Tests ServerInterceptor as per Portable Interceptors spec
 * orbos/99-12-02, section 5.3.  See pi/assertions.html for Assertions
 * covered in this test.
 * <p>
 * This test essentially is designed to determine which interception points
 * are invoked on various types of method invocations and varying interceptor
 * behaviors, and in what order those interception points are invoked.
 */
public class ServerInterceptorTest
    extends CORBATest 
{
    // Set to true if at least one test failes
    private boolean failed = false;

    Controller orbd;

    protected void doTest() 
        throws Throwable 
    {
        startORBD();
        System.out.println();
        System.out.println( "      \t\t\t\tLocal\t\tRemote" );

        beginTest( "[POA]\t\t\t" );
        testPOALocal();
        endTest( "\t\t" );
        testPOARemote();
        endTest( "\n" );

        beginTest( "[POA DSI]\t\t\t" );
        testPOADSILocal();
        endTest( "\t\t" );
        testPOADSIRemote();
        endTest( "\n" );

        beginTest( "[RMI]\t\t\t" );
        testRMILocal();
        endTest( "\t\t" );
        testRMIRemote();
        endTest( "\n" );

        beginTest( "[ServerRequestDispatcher DSI]\t" );
        testServerRequestDispatcherDSILocal();
        endTest( "\t\t" );
        testServerRequestDispatcherDSIRemote();
        endTest( "\n" );
        stopORBD();

        System.out.println();
        System.out.print( "      Final Result: " );
        if( failed ) {
            throw new RuntimeException( "Errors detected" );
        }
    }

    private void testPOALocal()
        throws Throwable
    {
        Controller server;

        try {

            Properties clientProps = Options.getClientProperties();

            clientProps.setProperty(ORBConstants.PERSISTENT_SERVER_PORT_PROPERTY,
                                    Options.getUnusedPort().toString());

            // Start only a server - the server will create the client in this 
            // test.  Create it as a client so no handshake is tested for.
            server = createClient( "pi.serverinterceptor.POALocalServer",
                                   "poalocal" );

            server.start();

            clientProps.remove(ORBConstants.PERSISTENT_SERVER_PORT_PROPERTY);

            server.waitFor();
            printEndTest( server, null );
            server.stop();
        }
        finally {
        }
    }

    private void testPOARemote()
        throws Throwable
    {
        Controller client, server;

        try {

            Properties serverProps = Options.getServerProperties();

            serverProps.setProperty(ORBConstants.PERSISTENT_SERVER_PORT_PROPERTY,
                                    Options.getUnusedPort().toString());

            server = createServer( "pi.serverinterceptor.POARemoteServer", 
                                   "poa-server" );
            server.start();

            serverProps.remove(ORBConstants.PERSISTENT_SERVER_PORT_PROPERTY);

            client = createClient( "pi.serverinterceptor.POARemoteClient",
                                   "poa-client" );
            client.start();
            client.waitFor();
            printEndTest( client, server );
            client.stop();
            server.stop();
        }
        finally {
        }
    }

    private void testPOADSILocal()
        throws Throwable
    {
        Controller server;

        try {

            // Start only a server - the server will create the client in this 
            // test.  Create it as a client so no handshake is tested for.
            server = createClient( "pi.serverinterceptor.DSIPOALocalServer",
                                   "dsipoalocal" );
            server.start();
            server.waitFor();
            printEndTest( server, null );
            server.stop();
        }
        finally {
        }
    }

    private void testPOADSIRemote()
        throws Throwable 
    {
        Controller client, server;

        try {
            server = createServer( "pi.serverinterceptor.DSIPOARemoteServer", 
                                   "dsi-poa-server" );
            server.start();
            client = createClient( "pi.serverinterceptor.POARemoteClient",
                                   "dsi-poa-client" );
            client.start();
            client.waitFor();
            printEndTest( client, server );
            client.stop();
            server.stop();
        }
        finally {
        }
    }

    private void testRMILocal()
        throws Throwable
    {
        Controller server;

        try {
            // Start only a server - the server will create the client in this 
            // test.  Create it as a client so no handshake is tested for.
            server = createClient( "pi.serverinterceptor.RMILocalServer",
                                   "rmilocal" );
            server.start();
            server.waitFor();
            printEndTest( server, null );
            server.stop();
        }
        finally {
        }
    }

    private void testRMIRemote() 
        throws Throwable
    {
        Controller client, server;

        try {
            server = createServer( "pi.serverinterceptor.RMIRemoteServer",
                                   "rmi-server" );
            server.start();
            client = createClient( "pi.serverinterceptor.RMIRemoteClient",
                                   "rmi-client" );

            client.start();
            client.waitFor();
            printEndTest( client, server );
            client.stop();
            server.stop();
        }
        finally {
        }
    }

    private void testServerRequestDispatcherDSILocal()
        throws Throwable
    {
        Controller server;

        try {
            // Start only a server - the server will create the client in this 
            // test.  Create it as a client so no handshake is tested for.
            server = createClient( "pi.serverinterceptor.DSIRMILocalServer",
                                   "dsirmilocal" );
            server.start();
            server.waitFor();
            printEndTest( server, null );
            server.stop();
        }
        finally {
        }
    }

    private void testServerRequestDispatcherDSIRemote()
        throws Throwable
    {
        Controller client, server;

        try {
            server = createServer( "pi.serverinterceptor.DSIRMIRemoteServer",
                                   "dsi-rmi-server" );
            server.start();
            client = createClient( "pi.serverinterceptor.DSIRMIRemoteClient",
                                   "dsi-rmi-client" );

            client.start();
            client.waitFor();
            printEndTest( client, server );
            client.stop();
            server.stop();
        }
        finally {
        }
    }

    private void beginTest( String name )
        throws Exception
    {
        System.out.print( "      " + name );
    }

    private void endTest( String terminator )
        throws Exception
    {
        System.out.print( terminator );
    }

    private void printBeginTest( String name ) {
        System.out.print( "      " + name );
    }

    private void printEndTest( Controller client, Controller server ) {
        if( (server != null) && server.finished() ) {
            System.out.print( "FAILED, Server crashed" );
            failed = true;
        }
        else if( (client != null) && 
                 (client.exitValue() != Controller.SUCCESS) ) 
        {
            System.out.print( "FAILED, Client exit value = " +
                client.exitValue() );
            failed = true;
        }
        else {
            System.out.print( "PASSED" );
        }
    }

    private void startORBD()
        throws Exception
    {
        orbd = createORBD();
        orbd.start();
    }

    private void stopORBD()
        throws Exception
    {
        orbd.stop();
        pause();
    }

    // Pause a little to allow all processes to fully terminate.
    private void pause() {
        try {
            Thread.sleep( 2000 );
        }
        catch( InterruptedException e ) {
            // ignore.
        }
    }

}

