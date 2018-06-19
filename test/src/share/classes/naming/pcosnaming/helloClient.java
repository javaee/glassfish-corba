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
 * https://glassfish.dev.java.net/public/CDDL+GPL_1_1.html
 * or packager/legal/LICENSE.txt.  See the License for the specific
 * language governing permissions and limitations under the License.
 * 
 * When distributing the software, include this License Header Notice in each
 * file and include the License file at packager/legal/LICENSE.txt.
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

package naming.pcosnaming;

import HelloApp.*;
import org.omg.CosNaming.*;
import org.omg.CORBA.*;
import java.util.Properties ;

public class helloClient
{
    public static void main(String args[])
    {
        try {
            // create and initialize the ORB
            ORB orb = ORB.init(args, System.getProperties());

            // get the root naming context
            org.omg.CORBA.Object objRef =
                orb.resolve_initial_references("NameService");
            NamingContext ncRef = NamingContextHelper.narrow(objRef);

            // resolve the Object Reference (Simple Case -One Level )
            NameComponent nc1 = new NameComponent("HelloObj1", "");
            NameComponent path1[] = {nc1};
            hello helloRef = helloHelper.narrow(ncRef.resolve(path1));
            helloRef.sayHello();

            // resolve the Object Reference (Little Complex Case -Two Level )
            NameComponent nc2 = new NameComponent("HelloContext1", "");

            NameComponent temppath[] = {nc2};
            NamingContext temp = (NamingContext) ncRef.resolve( temppath );
            System.out.println( "NC Resolve worked" );
            System.out.flush( );

            NameComponent nc3 = new NameComponent("HelloObj2", "");

            NameComponent temppath1[] = {nc3};
            helloRef = helloHelper.narrow(temp.resolve(temppath1));
            System.out.println( "First Resolve Worked" );
            System.out.flush( );
            helloRef.sayHello( );
/*

            NameComponent path2[] = {nc2, nc3};
            helloRef = helloHelper.narrow(ncRef.resolve(path2));
            System.out.println( "helloRef is resolved" );
            System.out.flush( );
            helloRef.sayHello();

            // resolve the Object Reference (Little Complex Case -Three Level )
            NameComponent nc4 = new NameComponent("HelloContext2", "");
            NameComponent nc5 = new NameComponent("HelloObj3", "");
            NameComponent path3[] = { nc2, nc4, nc5};
            helloRef = helloHelper.narrow(ncRef.resolve(path3));
            helloRef.sayHello();

*/
            //orb.shutdown(true);

        } catch (Exception e) {
            System.out.println("ERROR : " + e) ;
            e.printStackTrace(System.out);
            System.exit(1);
        }
    }
}
