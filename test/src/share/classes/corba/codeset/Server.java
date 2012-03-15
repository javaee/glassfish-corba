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
package corba.codeset;

import CodeSetTester.VerifierPOA;
import org.omg.CORBA.ORB;
import org.omg.CosNaming.NameComponent;
import org.omg.CosNaming.NamingContext;
import org.omg.CosNaming.NamingContextHelper;
import org.omg.PortableServer.POA;

public class Server extends VerifierPOA
{
    public char verifyChar (char ch) {
        System.out.println("verifyChar " + (int)ch);
        return ch;
    }

    public char verifyWChar (char wch) {
        System.out.println("verifyWChar " + (int)wch);
        return wch;
    }

    public String verifyString (String str) {
        System.out.println("verifyString " + str);
        return str;
    }

    public String verifyWString (String wstr) {
        System.out.println("verifyWString " + wstr.length());
        return wstr;
    }

    public void verifyCharSeq (CodeSetTester.VerifierPackage.TestCharSeqHolder chSeq) {
        System.out.println("verifyCharSeq " + chSeq.value.length);
    }

    public void verifyWCharSeq (CodeSetTester.VerifierPackage.TestWCharSeqHolder wchSeq) {
        System.out.println("verifyWCharSeq " + wchSeq.value.length);
    }

    public CodeSetTester.CustomMarshaledValue verifyTransmission (CodeSetTester.CustomMarshaledValue cv) {
        return cv;
    }

    public static void main(String args[])
    {
        try {
      
            ORB orb = ORB.init(args, System.getProperties());
            System.out.println(orb);
      
            // Get rootPOA
            POA rootPOA = (POA)orb.resolve_initial_references("RootPOA");
            rootPOA.the_POAManager().activate();
      
            // create servant and register it with the ORB
            Server verifierRef = new Server();
      
            byte[] id = rootPOA.activate_object(verifierRef);
      
            // get the root naming context
            org.omg.CORBA.Object objRef = 
                orb.resolve_initial_references("NameService");
            NamingContext ncRef = NamingContextHelper.narrow(objRef);
      
            // bind the Object Reference in Naming
            NameComponent nc = new NameComponent("Verifier", "");
            NameComponent path[] = {nc};
      
            org.omg.CORBA.Object ref = rootPOA.id_to_reference(id);
            
            ncRef.rebind(path, ref);
            
            // Emit the handshake the test framework expects
            // (can be changed in Options by the running test)
            System.out.println ("Server is ready.");

            // Wait for clients
            orb.run();
            
        } catch (Exception e) {
            System.err.println("ERROR: " + e);
            e.printStackTrace(System.out);

            // Make sure to exit with a value greater than 0 on
            // error.
            System.exit(1);
        }
    }
}
