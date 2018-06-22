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

package corba.simpledynamic;

import java.util.Properties ;
import java.util.Hashtable ;

import java.rmi.RemoteException ;

import javax.rmi.CORBA.Util ;
import javax.rmi.CORBA.Tie ;

import javax.naming.InitialContext ;

import com.sun.corba.ee.spi.JndiConstants;
import org.omg.CORBA.ORB ;

import org.testng.TestNG ;
import org.testng.Assert ;
import org.testng.annotations.Test ;
import org.testng.annotations.Configuration ;
import org.testng.annotations.ExpectedExceptions ;

import com.sun.corba.ee.spi.misc.ORBConstants ;

import com.sun.corba.ee.impl.naming.cosnaming.TransientNameService ;

import static corba.framework.PRO.* ;

public class Client {
    private ORB clientORB ;
    private ORB serverORB ;

    private static final String TEST_REF_NAME = "testref" ;
    private static final String SERVER_NAME = "fromServer" ;
    private static final String CLIENT_NAME = "fromClient" ;
    private static final String PORT_NUM = "3992" ;

    private String BASE = "com.sun.corba.ee." ;

    private void setSystemProperties() {
        System.setProperty( "javax.rmi.CORBA.UtilClass",
            BASE + "impl.javax.rmi.CORBA.Util" ) ;
        System.setProperty( "javax.rmi.CORBA.StubClass",
            BASE + "impl.javax.rmi.CORBA.StubDelegateImpl" ) ;
        System.setProperty( "javax.rmi.CORBA.PortableRemoteObjectClass",
            BASE + "impl.javax.rmi.PortableRemoteObject" ) ;

        // We will only use dynamic RMI-IIOP for this test.
        System.out.println( "Setting property " + ORBConstants.USE_DYNAMIC_STUB_PROPERTY 
            + " to true" ) ;
        System.setProperty( ORBConstants.USE_DYNAMIC_STUB_PROPERTY, "true" ) ;

        // Use the J2SE ic provider
        System.setProperty( "java.naming.factory.initial", 
            JndiConstants.COSNAMING_CONTEXT_FACTORY ) ;
    }

    // We need to set up the client and server ORBs, and start a transient
    // name server that runs on the server ORB, with the client ORB referring
    // to the server ORB's name service.
    private ORB makeORB( boolean isServer) {
        Properties props = new Properties() ;
        props.setProperty( "org.omg.CORBA.ORBClass", BASE + "impl.orb.ORBImpl" ) ;
        props.setProperty( ORBConstants.INITIAL_HOST_PROPERTY, "localhost" ) ;
        props.setProperty( ORBConstants.INITIAL_PORT_PROPERTY, PORT_NUM ) ;
        props.setProperty( ORBConstants.ALLOW_LOCAL_OPTIMIZATION, "true" ) ;

        if (isServer) {
            props.setProperty( ORBConstants.ORB_ID_PROPERTY, "serverORB" ) ;
            props.setProperty( ORBConstants.PERSISTENT_SERVER_PORT_PROPERTY, PORT_NUM ) ;
            props.setProperty( ORBConstants.SERVER_HOST_PROPERTY, "localhost" ) ;
            props.setProperty( ORBConstants.ORB_SERVER_ID_PROPERTY, "300" ) ;
        } else {
            props.setProperty( ORBConstants.ORB_ID_PROPERTY, "clientORB" ) ;
        }

        ORB orb = ORB.init( new String[0], props ) ;

        if (isServer) {
            new TransientNameService( 
                com.sun.corba.ee.spi.orb.ORB.class.cast(orb) ) ;
        }

        return orb ;
    }

    private Echo makeServant( String name ) {
        try {
            return new EchoImpl( name ) ;
        } catch (RemoteException rex) {
            Assert.fail( "Unexpected remote exception " + rex ) ;
            return null ; // never reached
        }
    }

    private void doServer( ORB orb ) {
        try {
            Hashtable env = new Hashtable() ;
            env.put( "java.naming.corba.orb", orb ) ;
            InitialContext ic = new InitialContext( env ) ;

            Echo servant = makeServant( SERVER_NAME ) ;
            Tie tie = Util.getTie( servant ) ;
            tie.orb( orb ) ;

            Echo ref = toStub( servant, Echo.class ) ;
            ic.bind( TEST_REF_NAME, ref ) ;
        } catch (Exception exc) {
            System.out.println( "Caught exception " + exc ) ;
            exc.printStackTrace() ;
            System.exit( 1 ) ;
        }
    }

    private void doClient( ORB orb ) {
        try {
            Hashtable env = new Hashtable() ;
            env.put( "java.naming.corba.orb", orb ) ;
            InitialContext ic = new InitialContext( env ) ;

            Echo servant = makeServant( CLIENT_NAME ) ;
            Tie tie = Util.getTie( servant ) ;
            tie.orb( orb ) ;

            System.out.println( "Creating first echoref" ) ;
            Echo ref = toStub( servant, Echo.class ) ;

            System.out.println( "Looking up second echoref" ) ;
            Echo sref = narrow( ic.lookup( TEST_REF_NAME ), Echo.class ) ;
            Assert.assertEquals( sref.name(), SERVER_NAME ) ;

            System.out.println( "Echoing first echoref" ) ;
            Echo rref = sref.say( ref ) ;
            Assert.assertEquals( rref.name(), CLIENT_NAME ) ;

            System.out.println( "Echoing second echoref" ) ;
            Echo r2ref = rref.say( sref ) ;
            Assert.assertEquals( r2ref.name(), SERVER_NAME ) ;

            System.out.println( "Echoing third echoref" ) ;
            Echo ref2 = ref.say( ref ) ;
            Assert.assertEquals( ref2.name(), ref.name() ) ;

            System.out.println( "Trying exception context" ) ;
            ref.testExceptionContext() ;
        } catch (Exception exc) {
            System.out.println( "Caught exception " + exc ) ;
            exc.printStackTrace() ;
            System.exit( 1 ) ;
        }
    }

    @Configuration( beforeTest = true ) 
    public void setUp() {
        setSystemProperties() ;
        serverORB = makeORB( true ) ;
        clientORB = makeORB( false ) ;

        try {
            serverORB.resolve_initial_references( "NameService" ) ;

            // Make sure that the FVD codebase IOR is not shared between
            // multiple ORBs in the value handler, because that causes
            // errors in the JDK ORB.
            // com.sun.corba.ee.spi.orb.ORB orb = (com.sun.corba.ee.spi.orb.ORB)serverORB ;
            // orb.getFVDCodeBaseIOR() ;

            clientORB.resolve_initial_references( "NameService" ) ;
        } catch (Exception exc) {
            throw new RuntimeException( exc ) ;
        }
    }

    @Test()
    public void run() {
        doServer( serverORB ) ;
        doClient( clientORB ) ;
    }

    @Configuration( afterTest = true )
    public void tearDown() {
        // The Client ORB does not correctly clean up its
        // exported targets: it tries to go to the SE
        // RMI-IIOP implementation, which is not even
        // instantiated here.  So clean up manually.
        //
        // Fixing this requires changes in the ORB:
        // basically it should be the TOA's job to keep
        // track of connected objrefs and clean up the
        // information in RMI-IIOP.  This would affect
        // both the se and ee ORBs, and require a patch
        // to JSE 5.
        clientORB.shutdown( true ) ;
        // com.sun.corba.ee.impl.javax.rmi.CORBA.Util.getInstance().
        //    unregisterTargetsForORB( clientORB ) ;
        clientORB.destroy() ;

        // The Server ORB does clean up correctly.
        serverORB.destroy() ;
    }

    public static void main( String[] args ) {
        TestNG tng = new TestNG() ;
        tng.setOutputDirectory( "gen/corba/simpledynamic/test-output" ) ;

        Class[] tngClasses = new Class[] {
            Client.class 
        } ;

        tng.setTestClasses( tngClasses ) ;

        tng.run() ;

        // Make sure we report success/failure to the wrapper.
        System.exit( tng.hasFailure() ? 1 : 0 ) ;
    }
}
