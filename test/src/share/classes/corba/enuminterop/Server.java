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

package corba.enuminterop;

import javax.naming.InitialContext;
import javax.naming.Context;
import java.util.Properties;

import javax.rmi.PortableRemoteObject ;
import javax.rmi.CORBA.Tie ;

import org.omg.CORBA.ORB;
import org.omg.CosNaming.*;

import org.omg.PortableServer.*;

import com.sun.corba.se.spi.presentation.rmi.PresentationManager ;
                                                                                
import com.sun.corba.se.impl.orbutil.ORBConstants ;


/**
 * This is a Server that uses Dynamic RMI IIOP Tie model. A Simple Server 
 * with 1 Servant that is associated with the RootPOA.
 */
public class Server {
    public final static String REF_NAME = "EchoService" ;
    
    private static final String PKG_PREFIX = "com.sun.corba." + "se." ;

    public static void main(String[] args ) {
        try {
            System.out.println( "Arguments:" ) ;
            for (String str : args) 
                System.out.println( "\t" + str ) ;

            // Use the JDK ORB for this test
            System.setProperty( "org.omg.CORBA.ORBClass", 
                PKG_PREFIX + "impl.orb.ORBImpl" ) ;
            System.setProperty( "org.omg.CORBA.ORBSingletonClass", 
                PKG_PREFIX + "impl.orb.ORBSingleton" ) ;

            System.setProperty( "javax.rmi.CORBA.PortableRemoteObjectClass",
                PKG_PREFIX + "impl.javax.rmi.PortableRemoteObject" ) ; 
            System.setProperty( "javax.rmi.CORBA.StubClass",
                PKG_PREFIX + "impl.javax.rmi.CORBA.StubDelegateImpl" ) ; 
            System.setProperty( "javax.rmi.CORBA.UtilClass",
                PKG_PREFIX + "impl.javax.rmi.CORBA.Util" ) ; 

            System.setProperty( "com.sun.CORBA.ORBDebug", "subcontract" ) ;

            System.out.println( ORB.init() ) ;

            ORB orb = ORB.init( (String[])null, null );

            org.omg.CORBA.Object objRef =
                orb.resolve_initial_references("NameService");
 
            NamingContext ncRef = NamingContextHelper.narrow(objRef);
            NameComponent nc = new NameComponent( REF_NAME, "");
            NameComponent path[] = {nc};

            POA rootPOA = (POA)orb.resolve_initial_references( "RootPOA" );
            rootPOA.the_POAManager().activate();
            
            byte[] id = REF_NAME.getBytes();
            rootPOA.activate_object_with_id(id, 
                (Servant)makeEchoServant(orb));
            org.omg.CORBA.Object obj = rootPOA.id_to_reference( id );
                                                                                
            ncRef.rebind(path, obj);

            // wait for invocations from clients
            System.out.println("Server is ready.");
            System.out.flush();

            java.lang.Object sync = new java.lang.Object();
            synchronized (sync) { sync.wait(); }
        } catch( Exception e ) {
            System.err.println( e );
            e.printStackTrace( );
        }
    }

    static Tie makeEchoServant( ORB orb ) {
        try {
            EchoServant servant = new EchoServant();

            Tie tie = javax.rmi.CORBA.Util.getTie( servant ) ;
            // Tie tie = orb.getPresentationManager().getTie();
	    tie.orb( orb ) ;
            tie.setTarget( (java.rmi.Remote)servant );
            return tie;
        } catch( Exception e ) {
            e.printStackTrace( );
            System.exit( -1 );
        }
        return null;
    }

}

