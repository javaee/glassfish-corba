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
package naming.rinameservice;

import org.omg.CosNaming.*;
import org.omg.CORBA.*;
import corba.framework.*;
import java.util.*;
import java.io.*;
import com.sun.corba.ee.spi.misc.ORBConstants ;

/**
 * NameServiceClient just tests that StandAlone Name Service that we
 * ship with RI. The test makes sure that 
 * 1. Root NamingContext can be resolved using the -ORBInitRef property
 * 2. Basic operations of Bind and Resolve works fine 
 */
public class NameServiceClient implements InternalProcess {
    public static void main( String args[] ) {
        try {
            (new NameServiceClient()).run( System.getProperties(),
                                args, System.out, System.err, null );
        } catch( Exception e ) {
            e.printStackTrace( System.err );
            System.exit( 1 );
        }
    }

    public void run(Properties environment,
                    String args[],
                    PrintStream out,
                    PrintStream err,
                    Hashtable extra) throws Exception
    {
        try {
            System.out.println( "Start of NameService test" ) ;

            String orbArguments[] = new String[2];
            orbArguments[0] = "-ORBInitRef";
            orbArguments[1] = "NameService=corbaloc:iiop:1.2@localhost:" +
                TestConstants.RI_NAMESERVICE_PORT + "/NameService";
            Properties props = new Properties() ;
            props.setProperty( ORBConstants.DEBUG_PROPERTY, "subcontract,giop,transport" ) ;
            ORB orb = ORB.init( orbArguments, props );
            System.out.println( "Created ORB" ) ;

            org.omg.CORBA.Object object = orb.resolve_initial_references(
                "NameService" );
            System.out.println( "Resolved NameService" ) ;
            
            org.omg.CosNaming.NamingContextExt namingContext =
                 org.omg.CosNaming.NamingContextExtHelper.narrow( object );
            System.out.println( "Narrowed NameService" ) ;

            NameComponent[] name = new NameComponent[1];
            name[0] = new NameComponent();
            name[0].id = "Test";
            name[0].kind = "";
            namingContext.bind( name, object );
            System.out.println( "Bound object in NameService" ) ;

            object = namingContext.resolve_str( "Test" );
            if( object == null ) {
                System.err.println( "NamingContext resolve failed..." );
                System.exit( 1 );
            }
            System.out.println( "Successfully resolved Standalone Name Server" +
                " Using INS" );
        } catch ( Exception e ) {
            System.out.println( "Caught exception " + e ) ;
            e.printStackTrace();
            System.exit( 1 );
        }
    }
}


