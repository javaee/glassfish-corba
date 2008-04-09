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
package corba.ortremote ;

import corba.ortremote.ORTEcho ;
import corba.ortremote.TestSession ;
import com.sun.corba.se.spi.oa.ObjectAdapter ;
import java.rmi.Remote ;
import java.rmi.RemoteException ;
import org.omg.PortableInterceptor.ObjectReferenceTemplate ;
import org.omg.PortableInterceptor.ObjectReferenceFactory ;
import org.omg.PortableServer.* ;
import org.omg.PortableServer.POAPackage.* ;
import org.omg.PortableServer.POAManagerPackage.* ;
import org.omg.PortableServer.ServantLocatorPackage.CookieHolder ;
import java.util.Properties ;
import org.omg.CORBA.ORB ;
import com.sun.corba.se.spi.orbutil.closure.Closure ;
import org.omg.CORBA.Policy ;
import javax.rmi.PortableRemoteObject ;
import javax.rmi.CORBA.Util ;
import org.omg.CORBA.LocalObject ;
import org.omg.CORBA.ORBPackage.* ;

public class Test
{
    public static ORTEcho makeServant( POA poa ) throws RemoteException 
    {
	return new ORTEchoImpl( poa ) ;
    }
   
    static class CounterServantLocator extends LocalObject implements ServantLocator
    {
	public Servant preinvoke( byte[] oid, POA poa, String operation,
	    CookieHolder cookie ) throws ForwardRequest
	{
	    ORTEcho impl = null ;

	    try {
		impl = makeServant( poa ) ;
	    } catch (RemoteException rexc) {
		RuntimeException exc = new RuntimeException( 
		    "Error in creating servant" ) ;
		exc.initCause( rexc ) ;
		throw exc ;
	    }

	    Servant servant = (Servant)Util.getTie( impl ) ;
	    return servant ;
	}

	public void postinvoke( byte[] oid, POA poa, String operation,
	    java.lang.Object cookie, Servant servant ) 
	{
	    // NOP
	}
    }

    public static ServantLocator makeServantLocator()
    {
	return new CounterServantLocator() ;
    }

    public static ORB makeORB() 
    {
	Properties props = null ;
	String[] args = null ;

	return ORB.init( args, props ) ;
    }

    public static POA makePOA( ORB orb ) throws AdapterAlreadyExists,
	AdapterInactive, WrongPolicy, InvalidName, InvalidPolicy
    {
	POA rootPOA = (POA)orb.resolve_initial_references( "RootPOA" ) ;
        Policy[] tpolicy = new Policy[] {
	    rootPOA.create_lifespan_policy(
		LifespanPolicyValue.TRANSIENT),
	    rootPOA.create_request_processing_policy(
		RequestProcessingPolicyValue.USE_SERVANT_MANAGER),
	    rootPOA.create_servant_retention_policy(
		ServantRetentionPolicyValue.NON_RETAIN) } ;

        POA tpoa = rootPOA.create_POA("NonRetainPOA", null, tpolicy);
        tpoa.the_POAManager().activate();

        ServantLocator csl = makeServantLocator();
        tpoa.set_servant_manager(csl);
	return tpoa;
    }

    public static void main( String[] args )
    {
	TestSession session = new TestSession( System.out, Test.class ) ;

	ORB clientORB = makeORB() ;
	ORB serverORB = makeORB() ;
	POA poa = null ;

	try {
	    poa = makePOA( serverORB ) ;
	} catch (Throwable thr ) {
	    session.testAbort( "Error in makePOA", thr ) ;
	}

	byte[] id = "FOO".getBytes() ;

	org.omg.CORBA.Object serverObjref = poa.create_reference_with_id( id,
	    "IDL:omg.org/Object:1.0" ) ;
	
	String serverObjrefStr = serverORB.object_to_string( serverObjref ) ;

	org.omg.CORBA.Object clientObjref = clientORB.string_to_object( serverObjrefStr ) ;

	final ORTEcho testRef = (ORTEcho)PortableRemoteObject.narrow( clientObjref, ORTEcho.class ) ;
	
	ObjectAdapter oa = (ObjectAdapter)poa ;
	ObjectReferenceFactory orf = oa.getCurrentFactory() ;
	ObjectReferenceTemplate ort = oa.getAdapterTemplate() ;

	session.start( "ORT marshalling test over RMI-IIOP" ) ;

	session.testForPass( "ObjectReferenceFactory",
	    new Closure() {
		public Object evaluate() {
		    try {
			return testRef.getORF() ;
		    } catch (Throwable thr) {
			RuntimeException err = new RuntimeException(
			    "Unexpected exception in getORF()" ) ;
			err.initCause( thr ) ;
			throw err ;
		    }
		}
	    },
	    orf ) ;
	
	session.testForPass( "ObjectReferenceTemplate",
	    new Closure() {
		public Object evaluate() {
		    try {
			return testRef.getORT() ;
		    } catch (Throwable thr) {
			RuntimeException err = new RuntimeException(
			    "Unexpected exception in getORT()" ) ;
			err.initCause( thr ) ;
			throw err ;
		    }
		}
	    },
	    ort ) ;

	clientORB.destroy() ;
	serverORB.destroy() ;
	session.end() ;
    }
}
