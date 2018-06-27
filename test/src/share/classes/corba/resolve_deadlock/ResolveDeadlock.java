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

package corba.resolve_deadlock;

import java.util.Properties ;

import com.sun.corba.ee.spi.misc.ORBConstants ;

import com.sun.corba.ee.impl.naming.cosnaming.TransientNameService;

import org.omg.CORBA.ORB ;



public class ResolveDeadlock {   

    private static final String PORT_NUM = "3074" ;
    private static ORB serverORB ;

    private static void initializeORBs( String[] args ) {
        // The following must be set as system properties 
        System.setProperty( "javax.rmi.CORBA.PortableRemoteObjectClass",
            "com.sun.corba.ee.impl.javax.rmi.PortableRemoteObject" ) ;
        System.setProperty( "javax.rmi.CORBA.StubClass",
            "com.sun.corba.ee.impl.javax.rmi.CORBA.StubDelegateImpl" ) ;
        System.setProperty( "javax.rmi.CORBA.UtilClass",
            "com.sun.corba.ee.impl.javax.rmi.CORBA.Util" ) ;

        // initializer server ORB.

        Properties serverProps = new Properties() ;
        serverProps.setProperty( "org.omg.CORBA.ORBSingletonClass",
            "com.sun.corba.ee.impl.orb.ORBSingleton" ) ;
        serverProps.setProperty( "org.omg.CORBA.ORBClass",
            "com.sun.corba.ee.impl.orb.ORBImpl" ) ;
        serverProps.setProperty( ORBConstants.INITIAL_HOST_PROPERTY,
            "localhost" ) ;
        serverProps.setProperty( ORBConstants.INITIAL_PORT_PROPERTY,
            PORT_NUM ) ;
        serverProps.setProperty( ORBConstants.ALLOW_LOCAL_OPTIMIZATION,
            "true" ) ;  
        serverProps.setProperty( ORBConstants.PERSISTENT_SERVER_PORT_PROPERTY,
            PORT_NUM ) ;
        serverProps.setProperty( ORBConstants.SERVER_HOST_PROPERTY,
            "localhost" ) ;
        serverProps.setProperty( ORBConstants.ORB_ID_PROPERTY,
            "serverORB" ) ;
        serverProps.setProperty( ORBConstants.ORB_SERVER_ID_PROPERTY,
            "300" ) ;

        // Ignore the args! Don't want to pick up setting of ORBInitialPort from args!
        String[] noArgs = null ;
        serverORB = ORB.init( noArgs, serverProps ) ;
        new TransientNameService( 
            com.sun.corba.ee.spi.orb.ORB.class.cast(serverORB) ) ;

        // Activate the transport
        try {
            serverORB.resolve_initial_references( "RootPOA" ) ;
        } catch (Exception exc) {
            throw new RuntimeException( exc ) ;
        }
    }

    public static void main( String[] args ) {
        initializeORBs( args ) ;
        try {
            //lookup a non-existing name "Foo"
            org.omg.CORBA.Object objRef = serverORB.resolve_initial_references( "Foo" );
            System.out.println( "Unexpectedly found the name Foo! ");
            System.exit(1);         
        } catch (Exception exc) {           
            System.out.println( "Expected exception in getting initial references: " + exc);
            exc.printStackTrace() ;
            System.exit(0) ;
        }
    }
}
