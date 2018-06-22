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

package corba.poaandequals;

import WombatStuff.WombatHelper;
import corba.framework.Controller;
import corba.framework.InternalProcess;
import java.io.File;
import java.io.FileOutputStream;
import java.io.PrintStream;
import java.util.Hashtable;
import java.util.Properties;
import org.glassfish.pfl.test.JUnitReportHelper;
import org.omg.CORBA.ORB;
import org.omg.CORBA.ORBPackage.InvalidName;
import org.omg.PortableServer.POA;

public class WombatServer implements InternalProcess 
{
    PrintStream out;
    PrintStream err;
    ORB orb;

    static String root = "RootPOA";

    public void writeObjref(org.omg.CORBA.Object ref, 
                            String file,
                            String outputDir) throws java.io.IOException {
        String fil = outputDir
            + File.separator
            + file;

        java.io.DataOutputStream outstr = new 
            java.io.DataOutputStream(new FileOutputStream(fil));
        outstr.writeBytes(orb.object_to_string(ref));
    }

    public void run(Properties environment,
                    String args[],
                    PrintStream out,
                    PrintStream err,
                    Hashtable extra) throws Exception
    {
        this.out = out;
        this.err = err;
        JUnitReportHelper helper = new JUnitReportHelper( WombatServer.class.getName() ) ;

        try {
            Controller client = (Controller)extra.get("client");
            orb = (ORB)extra.get("orb");

            out.println("Running server");

            POA poa = null;
            try {
                poa = (POA) orb.resolve_initial_references(root);
            } catch (InvalidName name) {
                err.println(root + " is an invalid name");
                throw name;
            }

            out.println("Activating servant...");

            WombatImpl w = new WombatImpl("BooBoo");
            byte[] id = null;
            try {
                helper.start( "ActivationTest" ) ;
                id = poa.activate_object(w);
                writeObjref(poa.create_reference_with_id(id, 
                    WombatHelper.id()), "WombatObjRef",
                    environment.getProperty("output.dir"));
                poa.the_POAManager().activate();
                helper.pass() ;
            } catch (Exception ex) {
                err.println(root+" threw "+ex+" after activate_object");
                helper.fail( ex ) ;
                throw ex;
            }

            out.println("Activated object, starting client");
        
            client.start();
            client.waitFor();

            out.println("Client finished, deactivating object");

            try {
                helper.start( "DeactivationTest" ) ;
                poa.deactivate_object(id);
                helper.pass() ;
            } catch (Exception ex) {
                err.println(root+" threw "+ex+" in deactivate_object");
                helper.fail( ex ) ;
                throw ex;
            }

            out.println("Destroying poa");
            
            poa.destroy(true, false);

            out.println("Finished");
        } finally {
            helper.done() ;
        }
    }
}
