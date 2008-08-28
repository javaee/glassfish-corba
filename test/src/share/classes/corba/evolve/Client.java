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
package corba.evolve;

import javax.rmi.PortableRemoteObject;
import org.omg.CosNaming.*;
import org.omg.CORBA.*;
import java.util.*;
import java.rmi.RemoteException;
import java.io.*;

public class Client
{
    public static void main(String args[])
    {
        try {

            // First make sure we can find a UserName class.
            Class userNameClass = Class.forName("UserName");

            UserNameInt localName = (UserNameInt)userNameClass.newInstance();

            // If we get here, then we did.

            ORB orb = ORB.init(args, System.getProperties());

            org.omg.CORBA.Object objRef = 
                orb.resolve_initial_references("NameService");
            NamingContext ncRef = NamingContextHelper.narrow(objRef);
 
            NameComponent nc = new NameComponent("UserNameVerifier", "");
            NameComponent path[] = {nc};

            org.omg.CORBA.Object obj = ncRef.resolve(path);

	    UserNameVerifier verifier = 
                (UserNameVerifier) PortableRemoteObject.narrow(obj, 
                                                               UserNameVerifier.class);
            
            System.out.println("Trying to send a UserName...");
            verifier.verifyName(localName);

            System.out.println("PASSED");

            System.out.println("Requesting a name...");
            UserNameInt testName = verifier.requestName();
            if (testName == null || !testName.validate())
                throw new Exception("Name returned from server was null or invalid");

            System.out.println("PASSED");

            System.out.println( "Requesting a FeatureInfo" ) ;
            FeatureInfo finfo = verifier.getFeatureInfo() ;
            System.out.println("PASSED");

            System.out.println("Validating the FeatureInfo" ) ;
            boolean result = verifier.validateFeatureInfo( finfo ) ;
            if (!result)
                throw new Exception( "Failure validating FeatureInfo "  + finfo ) ;
        } catch (Throwable t) {
            t.printStackTrace(System.out);
            System.exit (1);
        }
    }
}
