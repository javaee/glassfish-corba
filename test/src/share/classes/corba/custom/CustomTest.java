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
package corba.custom;

import test.Test;
import corba.framework.*;
import java.util.*;
import com.sun.corba.se.impl.orbutil.ORBConstants;

// Loops through all possible fragment sizes from 32 through user defined
// max.  Currently [32, 512]
//
public class CustomTest extends CORBATest
{
    public static String[] rmicClasses = { "corba.custom.VerifierImpl"};

    protected void doTest() throws Throwable
    {
        Options.setRMICClasses(rmicClasses);
        Options.addRMICArgs("-poa -nolocalstubs -iiop -keep -g");
        boolean failed = false ;

        compileRMICFiles();
        compileJavaFiles();

        Controller orbd = createORBD();
        orbd.start();

        System.out.println();

        for (int fragmentSize = 32; fragmentSize <= 512; fragmentSize+=16) {

            System.out.print("  Fragment size " + fragmentSize + ": ");

            // Specify the fragment size property
            Properties clientProps = Options.getClientProperties();
            Properties serverProps = Options.getServerProperties();

            clientProps.setProperty(ORBConstants.GIOP_FRAGMENT_SIZE,
                                    "" + fragmentSize);
            serverProps.setProperty(ORBConstants.GIOP_FRAGMENT_SIZE,
                                    "" + fragmentSize);

            // Give each client and server a different name so all
            // output files are separate
            Controller server = createServer("corba.custom.Server",
                                             "server" + fragmentSize);
            Controller client = createClient("corba.custom.Client",
                                             "client" + fragmentSize);

            // Go ahead and restart both server and client each time to
            // make sure we test all fragment sizes for replies, too.
            server.start();
            client.start() ; 

            try {
                if (client.waitFor(60000) == Controller.SUCCESS) {
                    System.out.println("PASSED");
                } else {
                    String msg = "FAILED (" + client.exitValue() + ")" ;
                    System.out.println( msg ) ;
                    failed = true;
                }
            } catch (Exception e) {
                // Timed out waiting for the client
                System.out.println("HUNG");
                failed = true ;
            } finally {
                client.stop();
                server.stop();
            }
        }

        orbd.stop();

        System.out.println();

        if (failed)
            throw new Error("Failures detected" );
    }
}
