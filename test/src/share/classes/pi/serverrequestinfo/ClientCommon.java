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

package pi.serverrequestinfo;

import org.omg.CORBA.*;
import org.omg.CosNaming.*;
import com.sun.corba.se.impl.corba.AnyImpl;
import com.sun.corba.se.impl.orbutil.ORBConstants;
import com.sun.corba.se.impl.interceptors.*;
import org.omg.PortableInterceptor.*;
import org.omg.IOP.*;
import org.omg.IOP.CodecPackage.*;
import org.omg.IOP.CodecFactoryPackage.*;
import corba.framework.*;

import java.util.*;
import java.io.*;

import ServerRequestInfo.*;

public abstract class ClientCommon 
    implements InternalProcess 
{

    // Set in run()
    com.sun.corba.se.spi.orb.ORB orb;
    
    // Set in run()
    PrintStream out;
    
    // Set in run()
    PrintStream err;

    // Set to true if the last invocation resulted in an exception.
    boolean exceptionRaised;
    
    /**
     * Creates a com.sun.corba.se.spi.orb.ORB and notifies the TestInitializer of its presence
     */
    void createORB( String[] args ) {
        // create the ORB without an initializer
        Properties props = new Properties() ;
        props.put( "org.omg.CORBA.ORBClass",
                   System.getProperty("org.omg.CORBA.ORBClass"));
        this.orb = (com.sun.corba.se.spi.orb.ORB)ORB.init(args, props);
    }

    /**
     * Re-resolves all references to eliminate any cached ForwardRequests
     * from the last invocation.
     */
    abstract void resolveReferences() throws Exception;

    /**
     * Call syncWithServer on the server object
     */
    abstract String syncWithServer() throws Exception;

    /**
     * Invoke the method with the given name on the object
     */
    abstract protected void invokeMethod( String methodName ) throws Exception;

    /**
     * Wait for server to give us the name of a method to execute, and then
     * execute that method.  Repeat the process until the server tells us
     * to execute a method called "exit."
     */
    void obeyServer() throws Exception {
        out.println( "+ Obeying commands from server." );

        String methodName;
        do {
	    // Re-resolve all references to eliminate any cached 
	    // LOCATION_FORWARDs
	    resolveReferences();

            // Synchronize with the server and get the name of the 
            // method to invoke.:
            out.println( "    - Syncing with server..." + 
                new Date().toString() );
            methodName = syncWithServer();
            out.println( "    - Synced with server at " + 
                new Date().toString() );
            
            // Execute the appropriate method on the hello object:
            out.println( "    - Executing method " + methodName + "..." );
	    exceptionRaised = false;
	    if( !methodName.equals( ServerCommon.EXIT_METHOD ) ) {
		try {
		    invokeMethod( methodName );
		}
		catch( IMP_LIMIT e ) {
		    exceptionRaised = true;
		    out.println( "      + Received IMP_LIMIT exception" );
		}
	    }

        } while( !methodName.equals( ServerCommon.EXIT_METHOD ) );
        
        out.println( "    - Exit detected.  No longer obeying server." );
    }

}
