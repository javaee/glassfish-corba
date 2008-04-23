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
package corba.multiorb;

import org.omg.CosNaming.*;
import org.omg.CosNaming.NamingContextPackage.*;
import org.omg.CORBA.*;
import java.util.*;
import examples.*;
import com.sun.corba.se.spi.orbutil.test.JUnitReportHelper ;

public class policy2Client {
    private static final String msgPassed = "policy_2: **PASSED**";
    
    private static final String msgFailed = "policy_2: **FAILED**";
    
    public static void main( String args[] ) {
        JUnitReportHelper helper = new JUnitReportHelper( policy2Client.class.getName() ) ;

        try {
            helper.start( "TwoORBTest" ) ;
            System.out.println( "POLICIES : ORB_CTRL_MODEL,PERSISTENT,UNIQUE_ID,SYSTEM_ID,RETAIN,USE_ACTIVE_OBJECT_MAP_ONLY,NO_IMPLICIT_ACTIVATION" );
            System.out.println( "Starting client" );
            System.out.println( "ORB Initializing" );
            Properties props = new Properties();
            props.put( "org.omg.corba.ORBClass", System.getProperty("org.omg.CORBA.ORBClass"));
            props.setProperty( "com.sun.corba.se.ORBid", "sunorb1");
            System.out.println("com.sun.corba.se.ORBid " + props.getProperty("com.sun.corba.se.ORBid"));
            ORB orb1 = ORB.init( args, props );

            props = new Properties();
            props.put( "org.omg.corba.ORBClass", System.getProperty("org.omg.CORBA.ORBClass"));
            props.setProperty( "com.sun.corba.se.ORBid", "sunorb2");
            System.out.println("com.sun.corba.se.ORBid " + props.getProperty("com.sun.corba.se.ORBid"));
            ORB orb2 = ORB.init( args, props );

            lookupAndInvoke(orb1, "Object1");
            lookupAndInvoke(orb2, "Object2");
            helper.pass() ;
        } catch( Exception exp ) {
            exp.printStackTrace();
            System.out.println( msgFailed + "\n" );
            helper.fail( exp ) ;
        } finally {
            helper.done() ;
        }
    }

    public static void lookupAndInvoke(org.omg.CORBA.ORB orb, String ObjName) throws Exception {
        try {
            System.out.println( "Looking for naming Service" );
            org.omg.CORBA.Object objRef = orb.resolve_initial_references( "NameService" );
            NamingContext ncRef = NamingContextHelper.narrow( objRef );
            System.out.println( "Getting Object Reference" );
            NameComponent nc = new NameComponent( ObjName, "" );
            NameComponent path[] = { nc };
            policy_2 Ref = policy_2Helper.narrow( ncRef.resolve( path ) );
            int l = Ref.increment();
            System.out.println( "Incremented value:" + l );
            System.out.println( msgPassed + "\n" );
        } catch( Exception exp ) {
            throw exp;
        }
    }
}
