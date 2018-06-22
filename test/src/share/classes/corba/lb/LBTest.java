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

package corba.lb ;

import java.util.Properties ;

import corba.framework.CORBATest ;
import corba.framework.Options ;
import corba.framework.Controller ;


import com.sun.corba.ee.spi.misc.ORBConstants ;
import com.sun.corba.ee.impl.plugin.hwlb.VirtualAddressAgentImpl ;
import com.sun.corba.ee.impl.plugin.hwlb.NoConnectionCacheImpl ;

public class LBTest
    extends
        CORBATest
{
    private static final String LB_HOST = "localhost" ;
    private static final int LB_PORT = 57340 ;
    private static final int S1_PORT = 57351 ;
    private static final int S2_PORT = 57352 ;

    private static final int SHARED_SERVER_ID = 2727 ;

    protected void doTest()
        throws Exception
    {
        String thisPackage = this.getClass().getPackage().getName() ;
        String pluginPackage = "com.sun.corba.ee.impl.plugin.hwlb" ;

        // Set up shared client and server properties.  This causes the client
        // ORBs to be initialized without connection caching, and the server
        // ORBs to use ORT to set the server port to Sx_PORT, while creating
        // IORs that contains the LB_PORT.
        Properties serverProps = Options.getServerProperties() ; 
        serverProps.setProperty( ORBConstants.ORB_SERVER_ID_PROPERTY, 
            Integer.toString(SHARED_SERVER_ID)) ;
        serverProps.setProperty( ORBConstants.USER_CONFIGURATOR_PREFIX 
            + pluginPackage + "." + "VirtualAddressAgentImpl",
            "dummy" ) ;
        serverProps.setProperty( VirtualAddressAgentImpl.VAA_HOST_PROPERTY, 
            LB_HOST ) ;
        serverProps.setProperty( VirtualAddressAgentImpl.VAA_PORT_PROPERTY, 
            Integer.toString(LB_PORT) ) ;

        Properties clientProps = Options.getClientProperties() ; 
        clientProps.setProperty( ORBConstants.USER_CONFIGURATOR_PREFIX 
                + pluginPackage + "." + "NoConnectionCacheImpl",
                "dummy" ) ;
        
        Controller orbd = createORBD();
        orbd.start();

        Controller lb;
        Controller server1;
        Controller server2;
        Controller client;

        String lbArgs = "-listen " + LB_PORT + " -pool " + S1_PORT + " " + S2_PORT ;
        Options.addServerArgs( lbArgs ) ;

        lb = createServer(thisPackage+"."+"LB", "LB" ) ;

        serverProps.setProperty( ORBConstants.PERSISTENT_SERVER_PORT_PROPERTY, 
            Integer.toString(S1_PORT)) ;
        server1 = createServer(thisPackage+"."+"Server", "Server1.1");

        serverProps.setProperty( ORBConstants.PERSISTENT_SERVER_PORT_PROPERTY, 
            Integer.toString(S2_PORT)) ;
        server2 = createServer(thisPackage+"."+"Server", "Server2");
        
        client = createClient(thisPackage+"."+"Client", "Client");

        lb.start() ;
        Thread.sleep( 1000 ) ;

        server1.start();
        server2.start();

        Thread.sleep( 1000 ) ;

        client.start();

        // Wait for client to get started before stopping server1.
        Thread.sleep( 4000 ) ;
        server1.stop();

        Thread.sleep( 1000 ) ;
        serverProps.setProperty( ORBConstants.PERSISTENT_SERVER_PORT_PROPERTY, 
            Integer.toString(S1_PORT)) ;
        server1 = createServer(thisPackage+"."+"Server", "Server1.2");
        server1.start() ;

        client.waitFor(1000 * 60 * 2);

        client.stop();
        lb.stop() ;
        server1.stop();
        server2.stop();
        orbd.stop();
    }
}
