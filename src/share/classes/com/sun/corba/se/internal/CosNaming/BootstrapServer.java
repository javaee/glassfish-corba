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

package com.sun.corba.se.internal.CosNaming;

import java.util.Enumeration;
import java.util.Properties;

import java.io.File;
import java.io.FileInputStream;

import com.sun.corba.se.spi.orb.ORB ;

import com.sun.corba.se.spi.resolver.Resolver ;
import com.sun.corba.se.spi.resolver.LocalResolver ;
import com.sun.corba.se.spi.resolver.ResolverDefault ;

import com.sun.corba.se.impl.misc.CorbaResourceUtil;
import com.sun.corba.se.spi.misc.ORBConstants;

/**
 * Class BootstrapServer is the main entry point for the bootstrap server
 * implementation.  The BootstrapServer makes all object references
 * defined in a configurable file available using the old
 * naming bootstrap protocol.
 */
public class BootstrapServer
{
    private ORB orb;

     /**
     * Main startup routine for the bootstrap server.
     * It first determines the port on which to listen, checks that the
     * specified file is available, and then creates the resolver 
     * that will be used to service the requests in the 
     * BootstrapServerRequestDispatcher.
     * @param args the command-line arguments to the main program.
     */
    public static final void main(String[] args)
    {
	String propertiesFilename = null;
	int initialPort = ORBConstants.DEFAULT_INITIAL_PORT;

	// Process arguments
	for (int i=0;i<args.length;i++) {
	    // Look for the filename
	    if (args[i].equals("-InitialServicesFile") && i < args.length -1) {
		propertiesFilename = args[i+1];
	    }

	    // Was the initial port specified? If so, override
	    // This property normally is applied for the client side
	    // configuration of resolvers.  Here we are using it to
	    // define the server port that the with which the resolvers
	    // communicate.
	    if (args[i].equals("-ORBInitialPort") && i < args.length-1) {
		initialPort = java.lang.Integer.parseInt(args[i+1]);
	    }
	}

	if (propertiesFilename == null) {
	    System.out.println( CorbaResourceUtil.getText("bootstrap.usage", 
		"BootstrapServer"));
	    return;
	}

	// Create a file
	File file = new File(propertiesFilename);

	// Verify that if it exists, it is readable
	if (file.exists() == true && file.canRead() == false) {
	    System.err.println(CorbaResourceUtil.getText(
		"bootstrap.filenotreadable", file.getAbsolutePath()));
	    return;
	}

	// Success: start up
	System.out.println(CorbaResourceUtil.getText(
	    "bootstrap.success", Integer.toString(initialPort), 
	    file.getAbsolutePath()));

	Properties props = new Properties() ;

	// Use the SERVER_PORT to create an Acceptor using the
	// old legacy code in ORBConfiguratorImpl.  When (if?)
	// the legacy support is removed, this code will need
	// to create an Acceptor directly.
	props.put( ORBConstants.SERVER_PORT_PROPERTY,  
	    Integer.toString( initialPort ) ) ;

	ORB orb = (ORB) org.omg.CORBA.ORB.init(args,props);

	LocalResolver lres = orb.getLocalResolver() ;
	Resolver fres = ResolverDefault.makeFileResolver( orb, file ) ;
	Resolver cres = ResolverDefault.makeCompositeResolver( fres, lres ) ;
	LocalResolver sres = ResolverDefault.makeSplitLocalResolver( cres, lres ) ;

	orb.setLocalResolver( sres ) ;

	try {
	    // This causes the acceptors to start listening.
	    orb.resolve_initial_references(ORBConstants.ROOT_POA_NAME);
	} catch (org.omg.CORBA.ORBPackage.InvalidName e) {
	    RuntimeException rte = new RuntimeException("This should not happen");
	    rte.initCause(e);
	    throw rte;
	}

	orb.run() ;
    }
}
