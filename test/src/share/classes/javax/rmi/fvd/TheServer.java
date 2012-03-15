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
/* @(#)TheServer.java   1.4 99/06/07 */
/*
 * Licensed Materials - Property of IBM
 * RMI-IIOP v1.0
 * Copyright IBM Corp. 1998 1999  All Rights Reserved
 *
 * US Government Users Restricted Rights - Use, duplication or
 * disclosure restricted by GSA ADP Schedule Contract with IBM Corp.
 */

package javax.rmi.fvd;

import org.omg.CORBA.ORB;

import java.util.Properties;
import java.io.FileOutputStream;
import java.io.DataOutputStream;
import java.io.File;
import javax.rmi.CORBA.Tie;
import javax.rmi.PortableRemoteObject;
import javax.naming.InitialContext;
import javax.naming.Context;
import java.util.Hashtable;

public class TheServer {

    // This test runs the NameServer on port 1050.

    private static String[] myArgs = new String[]{"-ORBInitialPort" , "1050" };

    public static void main(String[] args) {
        try {

            // The RMIClassLoader requires a security manager to be set
            System.setSecurityManager(new javax.rmi.download.SecurityManager());
            //System.setSecurityManager(new java.rmi.RMISecurityManager());

            // Lets setup some properties that we are using
            // for this test and then create the ORB Object...
            
            Properties props = System.getProperties();
        
            props.put(  "java.naming.factory.initial",
                        "com.sun.jndi.cosnaming.CNCtxFactory");
            
            props.put(  "org.omg.CORBA.ORBClass", 
                        "com.sun.corba.ee.impl.orb.ORBImpl");

            props.put("org.omg.CORBA.ORBSingletonClass", 
                        "com.sun.corba.ee.impl.orb.ORBSingleton");
            
            ORB orb = ORB.init(myArgs, props);
            
            // create an RMI Servant.  The Servant will actually
            // handle the users request.
            
            ServantImpl servant = new ServantImpl();
            
            // Let use PortableRemoteObject to export our servant.
            // This same method works for JRMP and IIOP.
            
            PortableRemoteObject.exportObject(servant);
            
            // Once the Object is exported we are going to link it to
            // our ORB.  To do this we need to get the Tie associated
            // with our Servant.  PortableRemoteObject.export(...) 
            // create a Tie for us.  All we have to do is to retrieve the
            // Tie from javax.rmi.CORBA.Util.getTie(...);
            
            Tie servantsTie = javax.rmi.CORBA.Util.getTie(servant);
            
            // Now lets set the orb in the Tie object.  The Sun/IBM
            // ORB will perform a orb.connect.  So at this point the
            // Tie is connected to the ORB and ready for work.
            servantsTie.orb(orb);

        
            // We are using JNDI/CosNaming to export our object so we
            // need to get the root naming context.  We use the properties
            // set above to initialize JNDI.
            
            Hashtable env = new Hashtable();
            env.put(  "java.naming.corba.orb", orb);
            
            Context ic = new InitialContext(env);

            // Now lets Export our object by publishing the object
            // with JNDI
            ic.rebind("TheFVDTestServer", servant);

            // Self-Test
            // resolve the Object Reference using JNDI
            Servant iServant = (Servant)
                PortableRemoteObject.narrow(ic.lookup("TheFVDTestServer"),
                                            Servant.class);

            System.out.println(test.Util.HANDSHAKE);
            System.out.flush();
        
            // wait for object invocation
            Object sync = new Object();
            synchronized (sync) { sync.wait(); }

        } catch (Exception ex) {

            ex.printStackTrace(System.out);
            System.out.println();
            System.out.flush();
        }
    }
}
