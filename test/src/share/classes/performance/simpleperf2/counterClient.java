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
package performance.simpleperf2;

import java.io.PrintStream;
import java.util.Hashtable;
import java.util.Properties;

import javax.rmi.PortableRemoteObject ;
import java.rmi.RemoteException ;

import org.omg.CORBA.ORB;
import org.omg.PortableServer.POA;
import org.omg.PortableServer.Servant ;
import org.omg.PortableServer.ServantLocator;
import org.omg.PortableServer.ServantLocatorPackage.CookieHolder ;

import corba.framework.InternalProcess;

public class counterClient implements InternalProcess
{
    private counterIF createLocalObject( ORB orb ) 
	throws java.rmi.RemoteException
    {
	counterImpl cimpl = new counterImpl() ;
	 
	return cimpl ;
    }

    private counterIF createRemoteObject( ORB orb ) 
	throws java.rmi.RemoteException
    {
	counterImpl obj = new counterImpl() ;

        counterIF counterRef 
            = (counterIF)PortableRemoteObject.narrow(obj, counterIF.class );

	return counterRef ;
    }

    private counterIF createRemoteObjectMarshal( ORB orb )
	throws java.rmi.RemoteException, java.rmi.NoSuchObjectException
    {
	counterImpl obj = new counterImpl() ;

        counterIF counterRef 
            = (counterIF)PortableRemoteObject.narrow(obj, counterIF.class );

	java.rmi.Remote stub = PortableRemoteObject.toStub( counterRef ) ;
	
	String str = orb.object_to_string( (org.omg.CORBA.Object)stub ) ;
	org.omg.CORBA.Object obj2 = orb.string_to_object( str ) ;

	return (counterIF)(PortableRemoteObject.narrow( obj2,
	    counterIF.class )) ;
    }

    private static final int COUNT = 10000 ;

    private void performTest(PrintStream out, counterIF counterRef, 
	String testType ) throws RemoteException
    {
	long time = System.currentTimeMillis() ;
	long value = 0 ;

	for (int i = 0; i < COUNT; i++) {
	    value += counterRef.increment(1);
	}
        
	double elapsed = System.currentTimeMillis() - time ;

	out.println( "Test " + testType + ": Elapsed time per invocation = " + 
	    elapsed/COUNT + " milliseconds" ) ;
    }

    public void run(Properties environment,
                    String args[],
                    PrintStream out,
                    PrintStream err,
                    Hashtable extra) throws Exception
    {
        environment.list(out);

        try {
            // create and initialize the ORB
            ORB orb = ORB.init(args, environment);

            counterIF counterRef1 = createLocalObject( orb ) ;
	    performTest(out, counterRef1, "local object" );

	    counterIF counterRef2 = createRemoteObject( orb ) ;
	    performTest(out, counterRef2, "local RMI-IIOP" );
/* There are problems here that need further investigation
	    counterIF counterRef3 = createRemoteObjectMarshal( orb ) ;
	    performTest(out, counterRef3, "local RMI-IIOP (marshalled)" );
*/
        } catch (Exception e) {
            e.printStackTrace(err);
            throw e;
        }
    }

    public static void main(String args[])
    {
        try {
            (new counterClient()).run(System.getProperties(),
                                      args,
                                      System.out,
                                      System.err,
                                      null);

        } catch (Exception e) {
            System.err.println("ERROR : " + e) ;
            e.printStackTrace(System.err);
            System.exit(1);
        }
    }
}

class CounterServantLocator extends org.omg.CORBA.LocalObject implements ServantLocator
{
    Servant servant;

    CounterServantLocator(Servant servant)
    {
        this.servant = servant;
    }

    public Servant preinvoke(byte[] oid, POA adapter, String operation, 
                             CookieHolder the_cookie)
        throws org.omg.PortableServer.ForwardRequest
    {
	return servant ;
    }

    public void postinvoke(byte[] oid, POA adapter, String operation, 
                           java.lang.Object cookie, Servant servant)
    {
        return;
    }
}
