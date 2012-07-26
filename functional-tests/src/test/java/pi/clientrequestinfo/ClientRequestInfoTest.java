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
package pi.clientrequestinfo;

import corba.framework.*;
import java.util.*;

/**
 * Tests ClientRequestInfo as per Portable Interceptors spec
 * orbos/99-12-02, section 5.4.  See pi/assertions.html for Assertions
 * covered in this test.
 */
public class ClientRequestInfoTest
    extends CORBATest 
{
    // Set to true if at least one test fails.
    private boolean failed = false;

    Controller orbd;

    public static String[] rmicClasses = {
        "pi.clientrequestinfo.helloRMIIIOP"
    };

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

        beginTest( "[POA DII]\t\t\t" );
        testPOADIILocal();
        endTest( "\t\t" );
        testPOADIIRemote();
        endTest( "\n" );

        beginTest( "[RMI]\t\t\t" );
        testRMILocal();
        endTest( "\t\t" );
        testRMIRemote();
        endTest( "\n" );

        beginTest( "[ClientDelegate DII]\t" );
        testClientDelegateDIILocal();
        endTest( "\t\t" );
        testClientDelegateDIIRemote();
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
        Controller client;

        try {
            // Start only a client - the client will create the server.
            client = createClient( 
                "pi.clientrequestinfo.POALocalClient",
                "poalocal" );
            client.start();
            client.waitFor();
            printEndTest( client, null );
            client.stop();
        }
        finally {
        }
    }

    private void testPOARemote()
        throws Throwable
    {
        Controller client, server;

        try {
            server = createServer( "pi.clientrequestinfo.POAServer",
                                              "poa-server" );
            server.start();
            client = createClient( "pi.clientrequestinfo.POAClient",
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

    private void testPOADIILocal() 
        throws Throwable
    {
        Controller client;

        try {
            // Start only a client - the client will create the server.
            client = createClient( "pi.clientrequestinfo.DIIPOALocalClient",
                                              "diipoalocal" );
            client.start();
            client.waitFor();
            printEndTest( client, null );
            client.stop();
        }
        finally {
        }
    }

    private void testPOADIIRemote()
        throws Throwable
    {
        Controller client, server;

        try {
            server = createServer( "pi.clientrequestinfo.POAServer",
                                              "dii-poa-server" );
            server.start();
            client = createClient( "pi.clientrequestinfo.DIIPOAClient",
                                              "dii-poa-client" );
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
        Controller client;

        try {
            // Start only a client - the client will create the server.
            client = createClient( "pi.clientrequestinfo.RMILocalClient",
                                   "rmilocal" );
            client.start();
            client.waitFor();
            printEndTest( client, null );
            client.stop();
        }
        finally {
        }
    }

    private void testRMIRemote()
        throws Throwable
    {
        Controller client, server;

        try {
            server = createServer( "pi.clientrequestinfo.RMIServer",
                                   "rmi-server" );
            server.start();
            client = createClient( "pi.clientrequestinfo.RMIClient",
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

    private void testClientDelegateDIILocal()
        throws Throwable
    {
        Controller client;

        try {
            // Start only a client - the client will create the server.
            client = createClient( "pi.clientrequestinfo.DIIRMILocalClient",
                                   "diirmilocal" );
            client.start();
            client.waitFor();
            printEndTest( client, null );
            client.stop();
        }
        finally {
        }
    }

    private void testClientDelegateDIIRemote()
        throws Throwable
    {
        Controller client, server;

        try {
            server = createServer( "pi.clientrequestinfo.OldRMIServer",
                                   "dii-rmi-server" );
            server.start();
            client = createClient( "pi.clientrequestinfo.DIIPOAClient",
                                   "dii-rmi-client" );
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


    private void printEndTest( Controller client, Controller server ) 
        throws Throwable
    {
        if( (server != null) && server.finished() ) {
            System.out.print( "FAILED, Server crashed" );
            failed = true;
        }
        else if( client.exitValue() != Controller.SUCCESS ) {
            System.out.print( "FAILED, Client exit value = " + 
                client.exitValue() );
            failed = true;
        }
        else {
            System.out.print( "PASSED" );
        }
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
