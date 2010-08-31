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

/*
 * Licensed Materials - Property of IBM
 * RMI-IIOP v1.0
 * Copyright IBM Corp. 1998 1999  All Rights Reserved
 *
 * US Government Users Restricted Rights - Use, duplication or
 * disclosure restricted by GSA ADP Schedule Contract with IBM Corp.
 */

package javax.rmi.test1;

import org.omg.CORBA.ORB;

import java.util.Properties;
import java.io.IOException;
import java.io.FileInputStream;
import java.io.DataInputStream;
import java.io.File;
import javax.rmi.PortableRemoteObject;
import javax.naming.InitialContext;
import javax.naming.Context;
import test.Util;
import test.Test;
import java.util.Hashtable;

import com.sun.corba.se.spi.orbutil.test.JUnitReportHelper ;

public class TheTest extends test.Test {
    // This test runs the NameServer on port 1050.
    private static  String[] myArgs = new String[]{"-ORBInitialPort" , "1050" };

    public  void run() {
        JUnitReportHelper helper = new JUnitReportHelper( 
            this.getClass().getName() ) ;

        String testName     = new TheTest().getClass().getName();
        Process nameServer  = null;
        Process server      = null;
        boolean testPassed  = true;

    	try {
    	    // First Compile the classes to generate the Stub and Tie 
    	    // files that are needed.  
            if (!getArgs().containsKey("-normic")) {
                compileClasses();
            }
    	    
    	    // Now we need to start the NameServer and
    	    // our test server. The test server will register
    	    // with the NameServer.
    	    
            nameServer  = Util.startNameServer("1050",true);
    	    server      = Util.startServer("javax.rmi.test1.TheServer");
            
    	    // Lets setup some properties that we are using
    	    // for this test and then create the ORB Object...

    	    Properties props = System.getProperties();
            
            props.put(  "java.naming.factory.initial",
                        "com.sun.jndi.cosnaming.CNCtxFactory");
    	    
    	    props.put(  "org.omg.CORBA.ORBClass", 
    	                "com.sun.corba.se.impl.orb.ORBImpl");
    	    
    	    props.put(  "org.omg.CORBA.ORBSingletonClass", 
    	                "com.sun.corba.se.impl.orb.ORBSingleton");
    	    
    	    ORB orb = ORB.init(myArgs, props);
	        
            // We are going to use JNDI/CosNaming so lets go ahead and
    	    // create our root naming context.  NOTE:  We setup CosNaming
    	    // as our naming plug-in for JNDI by setting properties above.
            Hashtable env = new Hashtable();
	    env.put(  "java.naming.corba.orb", orb);
    	    
    	    Context ic = new InitialContext(env);
    	    
    	    // Let the test begin...
            helper.start( "test1" ) ;
            // Resolve the Object Reference using JNDI/CosNaming
            java.lang.Object objref  = ic.lookup("TheTestServer");

            // This test is designed to verify PortableRemoteObject.narrow
          
    	    try {
		RemoteInterface1 narrowTo = null;
		if ( (narrowTo = (RemoteInterface1)
		      PortableRemoteObject.narrow(objref,RemoteInterface1.class)) != null ) {
            	    if (!narrowTo.EchoRemoteInterface1().equals("EchoRemoteInterface1")) {
            	        throw new Exception("javax.rmi.test1.TheTest: EchoRemoteInterface1() narrow failed");
            	    }
    	        }
    	    } catch (Throwable ex) {
                System.out.println(testName + " FAILED.");
		ex.printStackTrace();
		testPassed = false;
            }


    	    try {
		RemoteInterface2 narrowTo = null;
		if ( (narrowTo = (RemoteInterface2)
		      PortableRemoteObject.narrow(objref,RemoteInterface2.class)) != null ) {
            	    if (!narrowTo.EchoRemoteInterface2().equals("EchoRemoteInterface2")) {
            	        throw new Exception("javax.rmi.test1.TheTest: EchoRemoteInterface2() narrow failed");
            	    }
    	        }
    	    } catch (Throwable ex) {
                System.out.println(testName + " FAILED.");
		ex.printStackTrace();
		testPassed = false;
            }

    	    try {
		RemoteInterface3 narrowTo = null;
		if ( (narrowTo = (RemoteInterface3)
		      PortableRemoteObject.narrow(objref,RemoteInterface3.class)) != null ) {
            	    if (!narrowTo.EchoRemoteInterface3().equals("EchoRemoteInterface3")) {
            	        throw new Exception("javax.rmi.test1.TheTest: EchoRemoteInterface3() narrow failed");
            	    }
    	        }
    	    } catch (Throwable ex) {
                System.out.println(testName + " FAILED.");
		ex.printStackTrace();
		testPassed = false;
            }

    	    try {
		SingleRemoteInterface narrowTo = null;
		if ( (narrowTo = (SingleRemoteInterface)
		      PortableRemoteObject.narrow(objref,SingleRemoteInterface.class)) != null ) {
            	    if (!narrowTo.EchoSingleRemoteInterface().equals("EchoSingleRemoteInterface")) {
            	        throw new Exception("javax.rmi.test1.TheTest: SingleRemoteInterface() narrow failed");
            	    }
    	        }
    	    } catch (Throwable ex) {
                System.out.println(testName + " FAILED.");
		ex.printStackTrace();
		testPassed = false;
            }

    	} catch (Exception ex) {
            System.out.println(testName + " FAILED.");
    	    ex.printStackTrace();
    	    testPassed = false;
    	} finally {
            if (server != null) {
                server.destroy();
            }
  
            if (nameServer != null) {
                nameServer.destroy();
            }
        }

        if ( testPassed == true ) {
            helper.pass() ;
            status = null;
        } else {
            helper.fail( "test failed" ) ;
            status = new Error("PortableRemoteObject.narrow Test Failed");
        }

        helper.done() ;
    }

    // Compiling ComboInterface cause the compiler to compile
    // all the other classes that need to be compiled.
    
    private  void compileClasses () throws Exception
    {
        String arg = "-iiop";
        String[] additionalArgs = null;
        String[] classes = {"javax.rmi.test1.ComboInterfaceImpl"};
        
        // Create the additional args array...
               
        String outputDirectory = null;
        int length = 3;
        Hashtable flags = getArgs();
        if (flags.containsKey(test.Test.OUTPUT_DIRECTORY)) {
            outputDirectory = (String)flags.get(test.Test.OUTPUT_DIRECTORY);
            length += 2;
        }
        additionalArgs = new String[length];
        int offset = 0;
        
        if (outputDirectory != null) {
            additionalArgs[offset++] = "-d";
            additionalArgs[offset++] = outputDirectory;
        }
        additionalArgs[offset++] = "-Xreverseids";
        additionalArgs[offset++] = "-alwaysgenerate";
        additionalArgs[offset++] = "-keepgenerated";
        
        // Run rmic...
        
        Util.rmic(arg,additionalArgs,classes,false);
    }
}
