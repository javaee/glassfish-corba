/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright (c) 1997-2018 Oracle and/or its affiliates. All rights reserved.
 * Copyright (c) 1998-1999 IBM Corp. All rights reserved.
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

package javax.rmi.download;

import com.sun.corba.ee.spi.JndiConstants;
import org.omg.CORBA.ORB;

import javax.naming.Context;
import javax.naming.InitialContext;
import javax.rmi.PortableRemoteObject;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.Hashtable;
import java.util.Properties;

public class TheClient {

    private static String[] myArgs = new String[]{"-ORBInitialPort" , "1050" };

    private static void passed(){
        System.out.println(test.Util.HANDSHAKE);
        System.out.flush();
    }

    private static void failed(Throwable t){
        StringWriter strWriter = new StringWriter();
        PrintWriter printWriter = new PrintWriter(strWriter);
        t.printStackTrace(printWriter);
        System.out.println("Download test FAILED:\n"+strWriter.toString()+"\n <<< END STACK TRACE >>>");
        System.out.flush();
        System.exit(1);
    }

    public static void main(String[] args) {
        try {
            System.setSecurityManager(new javax.rmi.download.SecurityManager());
            // Lets setup some properties that we are using
            // for this test and then create the ORB Object...
                        
            Properties props = System.getProperties();
            
            props.put(  "java.naming.factory.initial",
                        JndiConstants.COSNAMING_CONTEXT_FACTORY);
            
            props.put(  "org.omg.CORBA.ORBClass", 
                        "com.sun.corba.ee.impl.orb.ORBImpl");
            
            props.put(  "org.omg.CORBA.ORBSingletonClass", 
                        "com.sun.corba.ee.impl.orb.ORBSingleton");
            
            ORB orb = ORB.init(myArgs, props);
                
            // We are going to use JNDI/CosNaming so lets go ahead and
            // create our root naming context.  NOTE:  We setup CosNaming
            // as our naming plug-in for JNDI by setting properties above.
            Hashtable env = new Hashtable();
            env.put(  "java.naming.corba.orb", orb);
            Context ic = new InitialContext(env);
            
            // Let the test begin...
            // Resolve the Object Reference using JNDI/CosNaming
            java.lang.Object objref  = ic.lookup("TheDownloadTestServer");
                        
            // This test is designed to verify PortableRemoteObject.narrow
                        
            try{
                Servant narrowTo = null;
                if ( (narrowTo = (Servant)
                      PortableRemoteObject.narrow(objref,Servant.class)) != null ) {
                    Servant serv1 = narrowTo;
                    String mssg = narrowTo.getValue().sayHello();
                    if (!mssg.equals("Hello, world!")) {
                        System.err.println(mssg);
                        throw new Exception("javax.rmi.download.TheTest: SingleRemoteInterface() narrow failed");
                    }
                                        
                    IIOPTestSerializable ones = new IIOPTestSerializable();
                    ones.setRef(serv1);
                    IIOPTestSerializable twos = (IIOPTestSerializable)serv1.testWriteReadObject(ones);
                    Servant serv2 = twos.getRef();
                    String mssg2 = serv2.EchoSingleRemoteInterface();
                    if (!mssg2.equals("EchoSingleRemoteInterface")) {
                        System.err.println(mssg);
                        throw new Exception("javax.rmi.download.TheTest: Reverse pass failed");
                    }   
                                        
                    passed();
                                        
                                        
                }
            } catch (Throwable ex) {
                failed(ex);
                ex.printStackTrace();
            }        
        } catch (Exception ex) {
            failed(ex);
            ex.printStackTrace(System.out);
            System.out.println();
            System.out.flush();
        }
    }
}

