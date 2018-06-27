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

package javax.rmi.fvd;

import com.sun.corba.ee.spi.JndiConstants;
import org.omg.CORBA.ORB;

import java.util.Properties;
import java.io.*;
import javax.rmi.PortableRemoteObject;
import javax.naming.InitialContext;
import javax.naming.Context;
import java.util.Hashtable;

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
        System.out.println("FVD test FAILED:\n"+strWriter.toString()+"\n <<< END STACK TRACE >>>");
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
            java.lang.Object objref  = ic.lookup("TheFVDTestServer");
                        
            // This test is designed to verify PortableRemoteObject.narrow
                        
            try{
                Servant narrowTo = null;
                if ( (narrowTo = (Servant)
                      PortableRemoteObject.narrow(objref,Servant.class)) != null ) {

                    // Verify connection
                    String str = "hello";
                    String res = narrowTo.ping(str);
                    if (!res.equals(new String("ServantImpl:"+str)))
                        throw new Error("Connection bad!");

                    // Send a mismatched class
                    // i.e. a matching class hierarchy with differing fields
                    ParentClass mismatch = 
                        (ParentClass)Class.forName("javax.rmi.download.values.ClientA").newInstance();
                    if (mismatch == null)
                        throw new Error("Could not create javax.rmi.download.values.ClientA");

                    if (narrowTo.send(mismatch) != mismatch.getOriginalTotal())
                        throw new Error("Mismatched class not sent correctly!");

                    // Send a differing hierarchy
                    // - Sender (TheClient) has shallow hierarchy C->A whereas
                    //   receiver (TheServer) has deeper hierarchy C->B->A.
                    ParentClass shallowHierarchy = 
                        (ParentClass)Class.forName("javax.rmi.download.values.ClassC").newInstance();
                                        
                    if (shallowHierarchy == null)
                        throw new Error("Could not create javax.rmi.download.values.ClassA");

                    if (narrowTo.send(shallowHierarchy) != shallowHierarchy.getOriginalTotal())
                        throw new Error("shallowHierarchy class not sent correctly!");
                                        
                    // Send a differing hierarchy
                    // - Sender (TheClient) has deeper hierarchy E->D->A whereas
                    //   receiver (TheServer) has shallow hierarchy E->A.
                    ParentClass deeperHierarchy = 
                        (ParentClass)Class.forName("javax.rmi.download.values.ClassE").newInstance();
                                        
                    if (deeperHierarchy == null)
                        throw new Error("Could not create javax.rmi.download.values.ClassE");

                    if (narrowTo.send(deeperHierarchy) != 19) // ! 26
                        throw new Error("deeperHierarchy class not sent correctly!");

                    // Send a value with a member who's type (class) does
                    // not exist on the receiver's side (i.e. not codebase
                    // to download it from either).
                    ParentClass missingClassContainer = 
                        (ParentClass)Class.forName("javax.rmi.download.values.MissingContainer").newInstance();

                    if (missingClassContainer == null)
                        throw new Error("Could not create javax.rmi.download.values.MissingContainer");

                    if (narrowTo.send(missingClassContainer) != 5)
                        throw new Error("missingClassContainer class not sent correctly");

                    passed();
                                        
                                        
                }
                else throw new Error("Failed to find narrowTo");


            } catch (Throwable ex) {
                failed(ex);

            }        
        } catch (Exception ex) {
            failed(ex);

        }
    }
}

