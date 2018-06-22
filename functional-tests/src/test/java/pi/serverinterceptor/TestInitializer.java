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

import org.omg.PortableInterceptor.*;
import org.omg.PortableInterceptor.ORBInitInfoPackage.*;

import java.util.*;
import java.io.*;
import org.omg.CORBA.*;

/**
 * Registers the necessary Server Interceptors to test 
 * ServerRequestInterceptor.
 */
public class TestInitializer 
    extends org.omg.CORBA.LocalObject
    implements ORBInitializer
{

    // The PrintStream to pass to the ServerRequestInterceptor for output
    // This is set from Server.java, statically.
    static PrintStream out;

    /** The ORB to pass to the ServerRequestInterceptor */
    static ORB orb;

    // The location of the original hello object.
    static org.omg.CORBA.Object helloRef;

    // Where to forward the caller on a ForwardRequest
    static org.omg.CORBA.Object helloRefForward;

    /**
     * Creates a TestInitializer
     */
    public TestInitializer() {
    } 

    /**
     * Called before all references are registered
     */
    public void pre_init (org.omg.PortableInterceptor.ORBInitInfo info) {
    }

    /**
     * Called after all references are registered
     */
    public void post_init (org.omg.PortableInterceptor.ORBInitInfo info) {
        ServerRequestInterceptor interceptor1;
        ServerRequestInterceptor interceptor2;
        ServerRequestInterceptor interceptor3;

        interceptor1 = new SampleServerRequestInterceptor( "1" );
        interceptor2 = new SampleServerRequestInterceptor( "2" );
        interceptor3 = new SampleServerRequestInterceptor( "3" );

        try {
            out.println( "    - post_init: adding 3 server interceptors..." );
            info.add_server_request_interceptor( interceptor1 );
            info.add_server_request_interceptor( interceptor2 );
            info.add_server_request_interceptor( interceptor3 );
        }
        catch( DuplicateName e ) {
            out.println( "    - post_init: received DuplicateName!" );
        }
    }

}
