/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2007 Sun Microsystems, Inc. All rights reserved.
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

package corba.simpledynamic;

import java.rmi.Remote ;
import java.rmi.RemoteException ;

import javax.rmi.PortableRemoteObject ;
import javax.rmi.CORBA.Util ;
import javax.rmi.CORBA.Tie ;

import javax.naming.InitialContext ;

import org.testng.Assert ;

import org.omg.CORBA.ORB ;

import static corba.framework.PRO.* ;

public class FrameworkClient extends Framework {
    private static final String SERVER_NAME = "fromServer" ;
    private static final String CLIENT_NAME = "fromClient" ;
    private static final String TEST_REF_NAME = "testref" ;

    private Echo makeServant( String name ) {
	try {
	    return new EchoImpl( name ) ;
	} catch (RemoteException rex) {
	    Assert.fail( "Unexpected remote exception " + rex ) ;
	    return null ; // never reached
	}
    }

    public void doServer( ORB orb, InitialContext ic ) {
	try {
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

    public void doClient( ORB orb, InitialContext ic ) {
	try {
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
	} catch (Exception exc) {
	    System.out.println( "Caught exception " + exc ) ;
	    exc.printStackTrace() ;
	    System.exit( 1 ) ;
	}
    }

    public static void main( String[] args ) {
	Framework.main( args ) ;
    }
}

