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
 * @(#)helloServer.java	1.2 99/10/29
 *
 * Copyright 1999 Sun Microsystems, Inc. All Rights Reserved.
 * 
 * This software is the proprietary information of Sun Microsystems, Inc.  
 * Use is subject to license terms.
 * 
 */
package naming.pcosnaming;

import HelloApp._helloImplBase ;
import corba.framework.Controller;
import corba.framework.InternalProcess;
import java.io.PrintStream;

import java.util.Properties;
import java.util.Hashtable;
import org.omg.CORBA.ORB;
import org.omg.CosNaming.NameComponent;
import org.omg.CosNaming.NamingContext;
import org.omg.CosNaming.NamingContextHelper;

class helloServant extends _helloImplBase
{
    public void sayHello()
    {
        helloServer.output.println("Servant: In helloServant.sayHello()");
    }

    public void shutdown() {
        throw new UnsupportedOperationException("Not supported yet.");
    }
}

public class helloServer implements InternalProcess 
{
    public NamingContext ncRef;
    public helloServant helloRef;
    public static PrintStream output;
    public static PrintStream errors;

    public void run(Properties environment,
                    String args[],
                    PrintStream out,
                    PrintStream err,
                    Hashtable extra) throws Exception
    {
        Controller orbd = (Controller)extra.get("orbd");
        Controller client = (Controller)extra.get("client");

        helloServer.output = out;
        helloServer.errors = err;

        ORB orb = ORB.init(args, environment);
        
        // create servant and register it with the ORB
        helloRef = new helloServant();
        orb.connect(helloRef);
        
        // get the root naming context
        org.omg.CORBA.Object objRef =
            orb.resolve_initial_references("NameService");
        ncRef = NamingContextHelper.narrow(objRef);
        
        // bind the Object Reference in Naming
        NameComponent nc1 = new NameComponent("HelloObj1", "");
        NameComponent path1[] = {nc1};
        ncRef.rebind(path1, helloRef);
        
        output.println("Killing and restarting ORBD...");

        orbd.stop();
        orbd.start();
        output.println("ORBD restarted");

        // Give a little more time
        Thread.sleep(1000);
        
        NamingContext ncRef1 = ncRef.new_context(); 
        output.println( "Persistent Reference was valid");
        
        NameComponent nc2 = new NameComponent("HelloContext1", "");
        NameComponent path2[] = {nc2};
        ncRef.rebind_context( path2, ncRef1 );
        
        output.println("Killing and restarting ORBD...");
        orbd.stop();
        orbd.start();
        output.println("ORBD restarted");
       
        Thread.sleep(1000);

        NamingContext ncRef2 = ncRef.new_context( ); 
        NameComponent nc3 = new NameComponent("HelloContext2", "");
        NameComponent path3[] = {nc3};
        ncRef1.rebind_context( path3, ncRef2 );
        output.println(" Persistent Reference of NCREF1 was valid....... " );
        
        NameComponent nc4 = new NameComponent( "HelloObj2", "");
        NameComponent path4[] = {nc4};
        ncRef1.rebind( path4, helloRef );
        
        output.println("Killing and restarting ORBD...");
        orbd.stop();
        orbd.start();
        output.println("ORBD restarted");

        Thread.sleep(1000);

        NameComponent nc5 = new NameComponent( "HelloObj3","");
        NameComponent path5[] = {nc5};
        ncRef2.rebind( path5, helloRef ); 
        
        output.println( " Persistent Reference of NCREF2 was valid....... " );
        
        output.println("Starting client...");
        
        // Not very intuitive, but start the client in a separate process.
        client.start();
        client.waitFor();
        
        output.println("Client finished, exiting...");

        output.flush();

        // orb.shutdown(true);
    }
}
