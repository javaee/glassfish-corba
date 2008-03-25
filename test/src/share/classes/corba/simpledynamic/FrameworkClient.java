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

import java.io.IOException ;
import java.io.ObjectInputStream ;
import java.io.ObjectOutputStream ;
import java.io.Serializable ;

import java.rmi.MarshalException ;

import java.util.Vector ;
import java.util.Properties ;

import java.rmi.Remote ;
import java.rmi.RemoteException ;

import javax.rmi.PortableRemoteObject ;
import javax.rmi.CORBA.Util ;
import javax.rmi.CORBA.Tie ;

import javax.naming.InitialContext ;

import org.testng.Assert ;
import org.testng.annotations.Test ;
import org.testng.annotations.BeforeGroups ;
import org.testng.annotations.AfterGroups ;
   
import org.omg.CORBA.ORB ;

import corba.nortel.NortelSocketFactory ;

import com.sun.corba.se.impl.orbutil.ORBConstants ;

import static corba.framework.PRO.* ;

public class FrameworkClient extends Framework {
    private static final boolean RUN_FRAGMENT_TEST = false ;

    private static final String SERVER_NAME = "fromServer" ;
    private static final String CLIENT_NAME = "fromClient" ;
    private static final String TEST_REF_NAME = "testref" ;

    private static final String TESTREF_GROUP = "testref_group" ;

    private Echo makeServant( String name ) {
	try {
	    return new EchoImpl( name ) ;
	} catch (RemoteException rex) {
	    Assert.fail( "Unexpected remote exception " + rex ) ;
	    return null ; // never reached
	}
    }

    private void msg( String msg ) {
	System.out.println( "+++FrameworkClient: " + msg ) ;
    }

    @BeforeGroups( { TESTREF_GROUP } ) 
    public void initTestRef() {
	bindServant( makeServant( SERVER_NAME ), Echo.class, TEST_REF_NAME ) ;
    }

    @Test( groups = { TESTREF_GROUP } ) 
    public void firstTest() {
	try {
	    InterceptorTester.theTester.clear() ;
	    Echo servant = makeServant( CLIENT_NAME ) ;
	    connectServant( servant, getClientORB() ) ;

	    System.out.println( "Creating first echoref" ) ;
	    Echo ref = toStub( servant, Echo.class ) ;

	    System.out.println( "Hello?" ) ;
	    System.out.println( "Looking up second echoref" ) ;
	    Echo sref = findStub( Echo.class, TEST_REF_NAME ) ;
	    Assert.assertEquals( sref.name(), SERVER_NAME ) ;

	    if (RUN_FRAGMENT_TEST) {
		System.out.println( "Running test for bug 6578707" ) ;
		testFragmentation( sref ) ;
	    }

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
	}
    }

    @Override
    protected Properties extraClientProperties() {
	Properties result = new Properties() ;
	
	// register nortel socket factory
	result.setProperty( ORBConstants.SOCKET_FACTORY_CLASS_PROPERTY, 
	    NortelSocketFactory.class.getName() ) ;
	
	// register ORBInitializer
	result.setProperty( ORBConstants.PI_ORB_INITIALIZER_CLASS_PREFIX + 
	    InterceptorTester.class.getName(), "true" ) ;

	// result.setProperty( ORBConstants.DEBUG_PROPERTY, 
	    // "transport" ) ;
	
	result.setProperty( ORBConstants.TRANSPORT_TCP_CONNECT_TIMEOUTS_PROPERTY,
	    "100:2000:100" ) ;

	return result ;
    }

    private static class Fragment implements java.io.Serializable {
	String str;

	Fragment(int  size) {
	    str="";
	    for(int i=0;i<size;i++) {
		str+="B";
	    }
	}
    }

    private static class Wrapper implements java.io.Serializable{
	Fragment f = null;
	Vector vec = null;

	public Wrapper(int len, Vector vec){
	    this.vec = vec;
	    f = new Fragment(len);
	}

	private void readObject(java.io.ObjectInputStream is
	    ) throws java.io.IOException,  ClassNotFoundException{

	    is.defaultReadObject();
	}

	private void writeObject(java.io.ObjectOutputStream is
	    ) throws java.io.IOException{

	    is.defaultWriteObject();
	}
    }

    public void testFragmentation( Echo sref ) {
	Throwable t = new Throwable();
	Vector v = new Vector();
	v.add(t);
	for (int i = 0; i < 1024; i++){
	    try {
		System.out.println("Hello call " + i);
		Wrapper w = new Wrapper(i, v);
		sref.sayHello(w);
	    } catch (Exception exc) {
		System.out.println( "Caught exception " + exc ) ;
		exc.printStackTrace() ;
	    }
	}
    }

    private int[] makeIntArray( int size ) {
	int[] result = new int[size] ;
	for (int ctr=0; ctr<size; ctr++)
	    result[ctr] = ctr ;
	return result ;
    }

    private void testWriteFailure( int[] arg ) {
	try {
	    msg( "testWriteFailure with " + arg.length + " ints" ) ;
	    InterceptorTester.theTester.clear() ;
	    Echo sref = findStub( Echo.class, TEST_REF_NAME ) ;
	    sref.echo( arg ) ;

	    NortelSocketFactory.disconnectSocket() ;
	    NortelSocketFactory.simulateConnectionDown() ;
	    InterceptorTester.theTester.setExceptionExpected() ;

	    msg( "******* Start Test with disconnected connection *******" ) ; 
	    // ((com.sun.corba.se.spi.orb.ORB)(getClientORB())).transportDebugFlag = true;
	    sref.echo( arg ) ;
	    // ((com.sun.corba.se.spi.orb.ORB)(getClientORB())).transportDebugFlag = false;
	    msg( "******* End test with disconnected connection *******" ) ; 
	} catch (MarshalException exc) {
	    msg( "Caught expected MarshalException" ) ;
	} catch (Exception exc) {
	    exc.printStackTrace() ;
	    Assert.fail( "Unexpected exception " + exc ) ;
	} finally {
	    NortelSocketFactory.simulateConnectionUp() ;
	    Assert.assertEquals( InterceptorTester.theTester.getErrors(), 0 ) ;
	}
    }

    @Test( groups = { TESTREF_GROUP } ) 
    public void testWriteFailureFragment() {
	testWriteFailure( makeIntArray( 50000 ) ) ;
    }

    @Test( groups = { TESTREF_GROUP } ) 
    public void testWriteFailureNoFragment() {
	testWriteFailure( makeIntArray( 50 ) ) ;
    }

    private static class RCTest implements Serializable {
	byte[] front ;
	Throwable thr ;

	void setPrefixSize( int size ) {
	    front = new byte[size] ;
	    for (int ctr=0; ctr<size; ctr++ ) {
		front[ctr] = (byte)(ctr & 255) ;
	    }
	}

	RCTest( Throwable thr ) {
	    setPrefixSize( 0 ) ;
	    this.thr = thr ;
	}

	private void readObject( ObjectInputStream is ) throws IOException, ClassNotFoundException {
	    is.defaultReadObject() ;
	}

	private void writeObject( ObjectOutputStream os ) throws IOException {
	    os.defaultWriteObject() ;
	}
    }

    @Test()
    public void testRecursiveTypeCode() {
	int ctr=0 ;
	try {
	    msg( "Start recursive TypeCode test" ) ;
	    Throwable thr = new Throwable( "Top level" ) ;
	    Throwable cause = new Throwable( "The cause" ) ;
	    thr.initCause( cause ) ;
	    RCTest rct = new RCTest( thr ) ;
	    Echo sref = findStub( Echo.class, TEST_REF_NAME ) ;

	    // ((com.sun.corba.se.spi.orb.ORB)(getClientORB())).giopDebugFlag = true;
	    for (ctr=0; ctr<4096; ctr+=256) {
		rct.setPrefixSize( ctr ) ;
		sref.echo( rct ) ;
	    }
	    // ((com.sun.corba.se.spi.orb.ORB)(getClientORB())).giopDebugFlag = false;

	} catch (Exception exc) {
	    exc.printStackTrace() ;
	    Assert.fail( "Unexpected exception in testRecursiveTypeCode for ctr = " + ctr + " :" + exc ) ;
	}
    }

    @Test()
    public void testCorbalocRir() {
	msg( "corbaloc:rir URL test" ) ;
	String name = "UseThisName" ;
	String url = "corbaloc:rir:/" + name ;
	ORB orb = getClientORB() ;
	try {
	    Echo serv = makeServant( "purple" ) ;
	    connectServant( serv, getClientORB() ) ;
	    Echo stub = toStub( serv, Echo.class ) ;
	    ((com.sun.corba.se.org.omg.CORBA.ORB)getClientORB())
		.register_initial_reference( name, (org.omg.CORBA.Object)stub ) ;

	    Echo echo = narrow( orb.string_to_object( url ), Echo.class ) ;
	    Assert.assertFalse( echo == null ) ;
	} catch (Exception exc) {
	    exc.printStackTrace() ;
	    Assert.fail( "Unexpected exception in testCorbalocRir: " + exc ) ;
	}
    }

    public static void main( String[] args ) {
	Class[] classes = { FrameworkClient.class } ;
	Framework.run( "gen/corba/simpledynamic/test-output", classes ) ;
    }
}
