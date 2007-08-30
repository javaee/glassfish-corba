/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2002-2007 Sun Microsystems, Inc. All rights reserved.
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

package com.sun.corba.se.impl.naming.pcosnaming;

import java.io.File;
import java.util.Properties;

import com.sun.corba.se.impl.orbutil.ORBConstants;
import com.sun.corba.se.impl.orbutil.CorbaResourceUtil;
import com.sun.corba.se.spi.orb.ORB;
import com.sun.corba.se.spi.activation.InitialNameService;
import com.sun.corba.se.spi.activation.InitialNameServiceHelper;
import org.omg.CosNaming.NamingContext;
/**
 * Class NameServer is a standalone application which
 * implements a persistent and a transient name service.
 * It uses the PersistentNameService and TransientNameService
 * classes for the name service implementation.
 *
 * @version     1.1, 99/10/07
 * @author      Hemanth Puttaswamy
 * @since       JDK1.2
 */

public class NameServer 
{
    private ORB orb;

    private File dbDir; // name server database directory

    private final static String dbName = "names.db";

    public static void main(String args[]) 
    {
	NameServer ns = new NameServer(args);
	ns.run();
    }

    protected NameServer(String args[]) 
    {
     	// create the ORB Object
     	java.util.Properties props = System.getProperties();
	props.put( ORBConstants.ORB_SERVER_ID_PROPERTY, "1000" ) ;
     	props.put("org.omg.CORBA.ORBClass", 
		  "com.sun.corba.se.impl.orb.ORBImpl");
     	orb = (ORB) org.omg.CORBA.ORB.init(args,props);

	// set up the database directory
	String dbDirName = props.getProperty( ORBConstants.DB_DIR_PROPERTY ) +
	    props.getProperty("file.separator") + dbName + 
	    props.getProperty("file.separator");

	dbDir = new File(dbDirName);
	if (!dbDir.exists()) dbDir.mkdir();
    }

    protected void run() 
    {
	try {

	    // create the persistent name service
	    NameService ns = new NameService(orb, dbDir);

	    // add root naming context to initial naming
	    NamingContext rootContext = ns.initialNamingContext();
	    InitialNameService ins = InitialNameServiceHelper.narrow(
				     orb.resolve_initial_references(
				     ORBConstants.INITIAL_NAME_SERVICE_NAME ));
	    ins.bind( "NameService", rootContext, true);
	    System.out.println(CorbaResourceUtil.getText("pnameserv.success"));

	    // wait for invocations
	    orb.run();

	} catch (Exception ex) {

	    ex.printStackTrace(System.err);
	}
    }

}
