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

package corba.stubserialization  ;

import java.rmi.Remote ;
import java.rmi.RemoteException ;
import java.rmi.UnexpectedException ;

import java.io.Serializable ;
import java.io.Externalizable ;

import javax.rmi.CORBA.Tie ;

import org.omg.CORBA.BAD_PARAM ;
import org.omg.CORBA.BAD_OPERATION ;
import org.omg.CORBA.SystemException ;
import org.omg.CORBA.portable.ApplicationException ;
import org.omg.CORBA.portable.ResponseHandler ;
import org.omg.CORBA.portable.UnknownException ;
import org.omg.CORBA.portable.Delegate ;
import org.omg.CORBA_2_3.portable.InputStream ;
import org.omg.CORBA_2_3.portable.OutputStream ;

import org.omg.CosNaming.*;
import org.omg.CORBA.ORB;

import java.util.Map ;
import java.util.Set ;
import java.util.HashSet ;
import java.util.Properties ;

import java.lang.reflect.Proxy;
import java.lang.reflect.Method;
import java.lang.reflect.InvocationTargetException ;

import javax.naming.NamingException;
import javax.naming.InitialContext;
import javax.naming.Context;

import javax.rmi.PortableRemoteObject;
import javax.rmi.CORBA.Util;

import corba.framework.*;
import java.util.*;
import java.io.*;

import com.sun.corba.ee.spi.presentation.rmi.StubAdapter ;

import org.testng.Assert ;
import org.testng.annotations.Test ;

/**
 * This tests that Stub serialization and deserialization to and from the
 * file stream works right.
 *
 * This test first tests the normal rmi-iiop invocation path from client
 * server. A typical HelloWorld kind of test.
 * 
 * After testing the normal invocation, it serializes the HelloStub into a
 * FileOutputStream and then deserializes it back and makes the same invocation.
 * This is a typical case in our AppServer's Stateless Session Beans
 * passivate and activate calls.
 */
public class Client
{
    private PrintStream out ;
    private PrintStream err ;
    private NamingContextExt nctx = null;
    private Hello hello = null;
    private ORB orb;

    private static String[] args;

    public static void main( String[] args ) 
    {
        Client.args = args ;
        TestngRunner runner = new TestngRunner() ;
        runner.registerClass( Client.class ) ;
        runner.run() ;
    }

    public Client() throws Exception {
        this.out = System.out;
        this.err = System.err;

        orb = ORB.init( args, null );

        org.omg.CORBA.Object objRef =
            orb.resolve_initial_references("NameService");

        NamingContext ncRef = NamingContextHelper.narrow(objRef);
        NameComponent nc = new NameComponent(Constants.HELLO_SERVICE, "");
        NameComponent[] path = {nc};
                                                                            
        hello = (Hello)PortableRemoteObject.narrow(ncRef.resolve(path),
                                                   Hello.class);
    }

    @Test
    private void testRemoteSerializedInvocation()
    {
        try {
            System.out.println( "Testing Remote Serialized Invocation: Start" );
            EchoServant echoServant = new EchoServant() ;
            Tie tie = Util.getTie( echoServant ) ;
            tie.orb( orb ) ;
            Remote stub = PortableRemoteObject.toStub( echoServant ) ;
            String fname = Constants.SERIALIZED_STUB_FILE_NAME + "2" ;
            serializeStub( fname, stub ) ;
            String result = hello.sayHelloToStub( fname ) ;
            if (!result.equals( Constants.HELLO ))
                throw new Exception( "Bad return value" ) ;
            System.out.println( "Testing Remote Serialized Invocation: Complete" );
        } catch (Throwable exc) {
           System.err.println( "Exception in Remote Serialized Invocation Test :" + exc );
           exc.printStackTrace( );
           System.exit( 1 );
        }
    }

    @Test
    private void testNormalInvocation( ) {
       try {
           System.out.println( "Testing Normal Invocation Start : " );
           String messageFromServer = hello.sayHello( );
           if( !messageFromServer.equals( Constants.HELLO ) ) {
               System.err.println( "Got an Incorrect message from Server" );
               System.exit( 1 );
           }
           System.out.println( "Message from Server = " + messageFromServer );
           System.out.println( "Testing Normal Invocation Complete : " );
       } catch( Exception e ) {
           System.err.println( "Exception in Normal Invocation Test :" + e );
           e.printStackTrace( );
           System.exit( 1 );
       }
    }

    public static File getFile( String fname ) {
        File dir = new File( "gen" ) ;
        dir.mkdir() ; // ensure that it exists
        File file = new File( dir, fname ) ;
        return file ;
    }

    @Test
    private void serializeStub( String fname, Remote stub ) 
    {
        FileOutputStream fos = null ;
        ObjectOutputStream oos = null ;

        try {
            System.out.println(
                "Serialing the Stub to FileOutputStream " + fname + 
                    ": Start");
            fos = new FileOutputStream( getFile(fname) ) ;
            oos = new ObjectOutputStream(fos);
            oos.writeObject(stub); 
            System.out.println(
                "Serializing the Stub to a FileOutputStream " + fname +
                ": Complete");
        } catch( Exception e ) {
            System.err.println( "Exception in Stub Serialization : " + e );
            e.printStackTrace( );
            System.exit( 1 );
        } finally {
            try {
                if (oos != null)
                    oos.close() ;
                if (fos != null)
                    fos.close() ;
            } catch (Exception exc) {
                // ignore IOException on close.
            }
        }
    }

    @Test
    private void deserializeStubandInvoke( String fname ) 
    {
        FileInputStream fis = null ;
        ObjectInputStream ois = null ;

        try {
           System.out.println(
               "DeSerializing the Stub from a FileStream Start");
           fis = new FileInputStream( getFile(fname) ) ;
           ois = new ObjectInputStream(fis);
           Object obj = ois.readObject(); 
           StubAdapter.connect( obj, orb ) ;
           System.out.println(
               "DeSerializing the Stub from a FileStream Complete");
           Hello helloAfterDeserialization = (Hello) obj;
           System.out.println( 
               "Invoking after Serialization and Deserialization" );
           String messageFromServer = helloAfterDeserialization.sayHello();
           if( !messageFromServer.equals( Constants.HELLO ) ) {
               System.err.println( "Got an Incorrect message from Server" );
               System.exit( 1 );
           }
           System.out.println( "Message from Server = " + messageFromServer );
           System.out.println( 
               "Invoking after Serialization and Deserialization Complete" );
       } catch( Exception e ) {
           System.err.println( 
               "Exception in Stub DeSerialization and Invoke : " + e );
           e.printStackTrace( );
           System.exit( 1 );
       } finally {
            try {
                if (ois != null)
                   ois.close() ;
                if (fis != null)
                    fis.close() ;
            } catch (Exception exc) {
                // ignore exception on close
            }
       }
    }

    /*
    @Test
    public void testAppReturnValue() 
    {
        try {
            TestAppReturnValue v1 = new TestAppReturnValue() ;
            TestAppReturnValue v2 = hello.getTARV() ;
            if (!v1.toString().equals( v2.toString() ))
                throw new RuntimeException( "v1 and v2 are not equal" ) ;
        } catch (Exception exc) {
           System.err.println( 
               "Exception in TestAppReturnValue : " + exc );
           exc.printStackTrace( );
           System.exit( 1 );
        }
    }
    */
}
