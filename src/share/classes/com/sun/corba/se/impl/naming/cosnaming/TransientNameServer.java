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

package com.sun.corba.se.impl.naming.cosnaming;

import java.util.Properties;

import org.omg.CORBA.ORB;

import com.sun.corba.se.spi.orbutil.ORBConstants;
import com.sun.corba.se.impl.orbutil.CorbaResourceUtil;
import com.sun.corba.se.spi.logging.NamingSystemException;
import com.sun.corba.se.spi.trace.Naming;

/**
 * Class TransientNameServer is a standalone application which
 * implements a transient name service. It uses the TransientNameService
 * class for the name service implementation, and the BootstrapServer
 * for implementing bootstrapping, i.e., to get the initial NamingContext.
 * <p>
 * The BootstrapServer uses a Properties object specify the initial service
 * object references supported; such as Properties object is created containing
 * only a "NameService" entry together with the stringified object reference
 * for the initial NamingContext. The BootstrapServer's listening port
 * is set by first checking the supplied arguments to the name server
 * (-ORBInitialPort), and if not set, defaults to the standard port number.
 * The BootstrapServer is created supplying the Properties object, using no
 * external File object for storage, and the derived initial port number.
 * @see TransientNameService
 * @see BootstrapServer
 */
public class TransientNameServer
{
    static private boolean debug = false ;
    private static final NamingSystemException wrapper =
        NamingSystemException.self ;

    @Naming
    private static org.omg.CORBA.Object initializeRootNamingContext( ORB orb ) {
        org.omg.CORBA.Object rootContext = null;
        try {
	    com.sun.corba.se.spi.orb.ORB coreORB =
		(com.sun.corba.se.spi.orb.ORB)orb ; 
		
            TransientNameService tns = new TransientNameService(coreORB );
            return tns.initialNamingContext();
        } catch (org.omg.CORBA.SystemException e) {
	    throw wrapper.transNsCannotCreateInitialNcSys( e ) ;
        } catch (Exception e) {
	    throw wrapper.transNsCannotCreateInitialNc( e ) ;
        }
    }

    /**
     * Main startup routine. It instantiates a TransientNameService
     * object and a BootstrapServer object, and then allows invocations to
     * happen.
     * @param args an array of strings representing the startup arguments.
     */
    @Naming
    public static void main(String args[]) {
        boolean invalidHostOption = false;
        boolean orbInitialPort0 = false;

	// Determine the initial bootstrap port to use
	int initialPort = 0;
	try {
	    // Create an ORB object
	    Properties props = System.getProperties() ;

	    props.put( ORBConstants.ORB_SERVER_ID_PROPERTY,
                ORBConstants.NAME_SERVICE_SERVER_ID ) ;
            props.put( "org.omg.CORBA.ORBClass", 
                "com.sun.corba.se.impl.orb.ORBImpl" );

	    try {
		String ips = System.getProperty( ORBConstants.INITIAL_PORT_PROPERTY ) ;
		if (ips != null && ips.length() > 0 ) {
		    initialPort = java.lang.Integer.parseInt(ips);
                    if( initialPort == 0 ) {
                        orbInitialPort0 = true;
			throw wrapper.transientNameServerBadPort() ;
                    }
                }

		String hostName = 
                    System.getProperty( ORBConstants.INITIAL_HOST_PROPERTY ) ;

                if( hostName != null ) {
                    invalidHostOption = true;
		    throw wrapper.transientNameServerBadHost() ;
                }
	    } catch (java.lang.NumberFormatException e) {
		// do nothing
	    }

	    // Let arguments override
	    for (int i=0;i<args.length;i++) {
		if (args[i].equals("-ORBInitialPort") &&
		    i < args.length-1) {
		    initialPort = java.lang.Integer.parseInt(args[i+1]);

                    if( initialPort == 0 ) {
                        orbInitialPort0 = true;
			throw wrapper.transientNameServerBadPort() ;
                    }
		}

                if (args[i].equals("-ORBInitialHost" ) ) { 
                    invalidHostOption = true;
		    throw wrapper.transientNameServerBadHost() ;
                }
	    }

            // If initialPort is not set, then we need to set the Default 
            // Initial Port Property for the ORB
            if( initialPort == 0 ) {
                initialPort = ORBConstants.DEFAULT_INITIAL_PORT;
                props.put( ORBConstants.INITIAL_PORT_PROPERTY,
                    java.lang.Integer.toString(initialPort) );
            }

            // Set -ORBInitialPort = Persistent Server Port so that ORBImpl
            // will start Boot Strap.
            props.put( ORBConstants.PERSISTENT_SERVER_PORT_PROPERTY, 
               java.lang.Integer.toString(initialPort) );

	    org.omg.CORBA.ORB corb = ORB.init( args, props ) ;
  
            org.omg.CORBA.Object ns = initializeRootNamingContext( corb ) ;
	    ((com.sun.corba.se.org.omg.CORBA.ORB)corb).register_initial_reference( 
		"NamingService", ns ) ;

	    String stringifiedIOR = null;
 
            if( ns != null ) {
	        stringifiedIOR = corb.object_to_string(ns) ;
            } else {
	         NamingUtils.errprint(CorbaResourceUtil.getText(
                     "tnameserv.exception", initialPort));
                 NamingUtils.errprint(CorbaResourceUtil.getText(
                     "tnameserv.usage"));
                System.exit( 1 );
            }

	    // This is used for handshaking by the IBM test framework!
	    // Do not modify, unless another synchronization protocol is 
	    // used to replace this hack!

	    System.out.println(CorbaResourceUtil.getText(
                "tnameserv.hs1", stringifiedIOR));
            System.out.println(CorbaResourceUtil.getText(
                "tnameserv.hs2", initialPort));
            System.out.println(CorbaResourceUtil.getText("tnameserv.hs3"));

	    // Serve objects.
	    java.lang.Object sync = new java.lang.Object();
	    synchronized (sync) {sync.wait();}
	} catch (Exception e) {
	    if( invalidHostOption ) {
                // Let the User Know that -ORBInitialHost is not valid for
                // tnameserver
                NamingUtils.errprint( CorbaResourceUtil.getText(
                    "tnameserv.invalidhostoption" ) );
            } else if( orbInitialPort0 ) {
                // Let the User Know that -ORBInitialPort 0 is not valid for
                // tnameserver
                NamingUtils.errprint( CorbaResourceUtil.getText(
                    "tnameserv.orbinitialport0" ));
            } else {
	        NamingUtils.errprint(CorbaResourceUtil.getText(
                    "tnameserv.exception", initialPort));
                NamingUtils.errprint(CorbaResourceUtil.getText(
                    "tnameserv.usage"));
            }
	}
    }

    /**
     * Private constructor since no object of this type should be instantiated.
     */ 
    private TransientNameServer() {}
}
