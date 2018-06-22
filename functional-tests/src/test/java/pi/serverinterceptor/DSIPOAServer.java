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

import org.omg.CORBA.*;
import org.omg.CORBA.ORBPackage.*;
import org.omg.CosNaming.*;
import org.omg.PortableServer.*;
import org.omg.PortableServer.POAPackage.*;
import org.omg.PortableInterceptor.*;
import com.sun.corba.ee.impl.interceptors.*;
import corba.framework.*;
import com.sun.corba.ee.spi.misc.ORBConstants;

import java.util.*;
import java.io.*;

import ServerRequestInterceptor.*;

/**
 * Common base class for DSI POA Server test files.
 */
public abstract class DSIPOAServer 
    extends ServerCommon 
{
    static final String ROOT_POA = "RootPOA";
    
    POA rootPOA;

    // To be invoked by subclass after orb is created.
    public void run( Properties environment, String args[], PrintStream out,
                     PrintStream err, Hashtable extra) 
        throws Exception
    {
        try {
            // Get the root POA:
            rootPOA = null;
            out.println( "+ Obtaining handle to root POA and activating..." );
            try {
                rootPOA = (POA)orb.resolve_initial_references( ROOT_POA );
            }
            catch( InvalidName e ) {
                err.println( ROOT_POA + " is an invalid name." );
                throw e;
            }
            rootPOA.the_POAManager().activate();
            
            // Set up hello object:
            out.println( "+ Creating and binding Hello1 object..." );
            TestInitializer.helloRef = createAndBind( rootPOA, "Hello1", 
                                                      "[Hello1]" );

            out.println( "+ Creating and binding Hello1Forward object..." );
            TestInitializer.helloRefForward = createAndBind( rootPOA, 
                                                             "Hello1Forward",
                                                             "[Hello1Forward]" ); 

            handshake();
            
            // Test ServerInterceptor
            testServerInterceptor();

            // Test POA special operations
            testSpecialOps();
        } finally {
            finish() ;

            // Notify client it's time to exit.
            exitClient();

            // wait for invocations from clients
            waitForClients();
        }
    }

    // Output handshake or wake up main.
    abstract void handshake();

    // Wait for invocations from clients.
    abstract void waitForClients();

    /**
     * Tests the special operations _is_a, _get_interface_def, and 
     * _non_existent.
     */
    void testSpecialOps()
        throws Exception
    {
        out.println();
        out.println( "Running Special Operations Tests" );
        out.println( "================================" );

        out.println( "+ Testing _is_a..." );
        SampleServerRequestInterceptor.dontIgnoreIsA = true;
        testInvocation( "testInvocationIsA",
            SampleServerRequestInterceptor.MODE_NORMAL,
            "rs1rs2rs3rr1rr2rr3sr3sr2sr1",
            "_is_a", "", false );

        // We do not implement this interface in our ORB. 
        // Thus, the send_exception.  We pass in false for exception
        // expected because this is not the exception we normally look for.
        out.println( "+ Testing _get_interface_def..." );
        testInvocation( "testInvocationGetInterfaceDef",
            SampleServerRequestInterceptor.MODE_NORMAL,
            "rs1rs2rs3rr1rr2rr3se3se2se1",
            "_get_interface_def", "", false );

        out.println( "+ Testing _non_existent..." );
        testInvocation( "testInvocationNonExistent",
            SampleServerRequestInterceptor.MODE_NORMAL,
            "rs1rs2rs3rr1rr2rr3sr3sr2sr1",
            "_non_existent", "", false );
    }

    /**
     * Implementation borrowed from corba.socket.HelloServer test
     */
    public org.omg.CORBA.Object createAndBind ( POA poa, String name, 
                                                String symbol )
        throws Exception
    {
        // create servant and register it with the ORB
        helloDSIServant helloRef = new helloDSIServant( orb, out, symbol );

        byte[] id = poa.activate_object(helloRef);
        org.omg.CORBA.Object ref = poa.id_to_reference(id);
      
        // get the root naming context
        org.omg.CORBA.Object objRef = 
            orb.resolve_initial_references("NameService");
        NamingContext ncRef = NamingContextHelper.narrow(objRef);
      
        // bind the Object Reference in Naming
        NameComponent nc = new NameComponent(name, "");
        NameComponent path[] = {nc};
            
        ncRef.rebind(path, ref);

        return ref;
    }

}
