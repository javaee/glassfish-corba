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
import org.omg.CORBA.ORBPackage.*;
import org.omg.CosNaming.*;
import org.omg.PortableServer.*;
import org.omg.PortableServer.POAPackage.*;
import org.omg.PortableServer.ServantLocatorPackage.*;
import org.omg.PortableInterceptor.*;
import com.sun.corba.ee.impl.interceptors.*;
import corba.framework.*;
import com.sun.corba.ee.spi.misc.ORBConstants;

import java.util.*;
import java.io.*;

import ServerRequestInterceptor.*;
import java.rmi.*;
import javax.rmi.*;
import javax.naming.*;

public class RMIRemoteServer 
    extends RMIServer
{
    public static void main(String args[]) {
        try {
            (new RMIRemoteServer()).run( System.getProperties(),
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

        out.println( "=====================================" );
        out.println( "Instantiating ORB for RMI Remote test" );
        out.println( "=====================================" );

        out.println( "+ Creating ORB..." );
        createORB( args, new Properties() );

        super.run( environment, args, out, err, extra );
    }

    void handshake() {
        out.println( "Server is ready." );
        out.flush();
    }

    void waitForClients() {
        // wait for invocations from clients
        java.lang.Object sync = new java.lang.Object();
        synchronized( sync ) {
            try {
                sync.wait();
            }
            catch( InterruptedException e ) {
            }
        }
    }
}

