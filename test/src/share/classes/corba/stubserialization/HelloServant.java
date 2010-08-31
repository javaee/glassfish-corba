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

package corba.stubserialization;

import java.rmi.RemoteException;

import javax.rmi.PortableRemoteObject; 
import java.io.FileInputStream ;
import java.io.ObjectInputStream ;

import com.sun.corba.se.spi.presentation.rmi.StubAdapter ;
import com.sun.corba.se.spi.orb.ORB ;

public class HelloServant extends PortableRemoteObject implements Hello
{
    ORB orb ;

    public HelloServant( ORB orb ) throws RemoteException 
    {
        super();
	this.orb = orb ;
    }
        
    public String sayHello( ) throws RemoteException
    {
        return Constants.HELLO;
    }

    public String sayHelloToStub( String fileName ) throws RemoteException 
    {
       FileInputStream fis = null ;
       ObjectInputStream ois = null ;

       try {
           System.out.println(
               "Deserializing the Stub from a FileStream: Start");
           fis = new FileInputStream( Client.getFile( fileName ) ) ;
	   ois = new ObjectInputStream(fis);
	   Object obj = ois.readObject(); 
	   StubAdapter.connect( obj, orb ) ;
           System.out.println(
               "Deserializing the Stub from a FileStream: Complete");
           Echo echo = (Echo) obj;
           System.out.println( 
               "Invoking after Serialization and Deserialization" );
	   String msg = echo.echo( Constants.HELLO ) ;
           System.out.println( 
               "Invoking after Serialization and Deserialization Complete" );
	   return msg ; 
	} catch (Exception exc) {
	    throw new RemoteException( "Error in sayHelloToStub", exc ) ;
	} finally {
	    try {
		if (ois != null)
		    ois.close() ;
		if (fis != null)
		    fis.close() ;
	    } catch (Exception exc) {
		// Nothing to do if close throws an IOException.
	    }
	}

    }

    /*
    public TestAppReturnValue getTARV() throws RemoteException 
    {
	return new TestAppReturnValue() ;
    }
    */
}
