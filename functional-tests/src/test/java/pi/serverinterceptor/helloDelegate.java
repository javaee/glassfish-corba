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

import java.util.*;
import java.io.*;
import org.omg.PortableInterceptor.*;

import ServerRequestInterceptor.*;

/**
 * Servant implementation.  
 */
class helloDelegate implements helloIF {
    private PrintStream out = null;

    // The symbol to append to SampleServerRequestInterceptor.methodOrder
    // every time a relevant method is called on this object.
    String symbol;

    public helloDelegate( PrintStream out, String symbol ) {
        super();
        this.out = out;
        this.symbol = symbol;
    }

    public String sayHello() {
        ServerCommon.servantInvoked = true;
        out.println( "    - helloDelegate: sayHello() invoked" );
        SampleServerRequestInterceptor.methodOrder += symbol;
        return "Hello, world!";
    }

    public void sayOneway() {
        ServerCommon.servantInvoked = true;
        out.println( "    - helloDelegate: sayOneway() invoked" );
        SampleServerRequestInterceptor.methodOrder += symbol;
    }
    
    public void saySystemException() {
        ServerCommon.servantInvoked = true;
        out.println( "    - helloDelegate: saySystemException() invoked" );
        SampleServerRequestInterceptor.methodOrder += symbol;
        throw new IMP_LIMIT( SampleServerRequestInterceptor.VALID_MESSAGE );
    }

    public void sayUserException() 
        throws ForwardRequest
    {
        ServerCommon.servantInvoked = true;
        out.println( "    - helloDelegate: sayUserException() invoked" );
        SampleServerRequestInterceptor.methodOrder += symbol;
        throw new ForwardRequest( TestInitializer.helloRef );
    }
    
    // Client code calls this to synchronize with server.  This call
    // blocks until the server is ready for the next invocation.  
    // It then returns a String containing the name of the method to
    // invoke on (either "sayHello" or "saySystemException").
    // If the string "exit" is returned, the Client's
    // work is done and it may exit.
    //
    // @param exceptionRaised true if the last invocation resulted in
    //     an exception on the client side.
    public String syncWithServer( boolean exceptionRaised ) {
        out.println( "    - helloDelegate: syncWithServer() invoked" );
        // Notify the test case that the client is waiting for 
        // syncWithServer to return:
        ServerCommon.syncing = true;
        ServerCommon.exceptionRaised = exceptionRaised;
        
        // Wait for the next test case to start:
        synchronized( ServerCommon.syncObject ) {
            try {
                ServerCommon.syncObject.wait();
            }
            catch( InterruptedException e ) {
                // ignore, assume we are good to go.
            }
        }
        
        ServerCommon.syncing = false;
        
        return ServerCommon.nextMethodToInvoke;
    }

}

