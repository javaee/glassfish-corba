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
//
// Created       : 2002 Jul 19 (Fri) 14:50:37 by Harold Carr.
// Last Modified : 2003 Jun 03 (Tue) 18:11:37 by Harold Carr.
//

package corba.iorintsockfact;

import java.util.Properties;

import org.omg.CORBA.ORB;

/**
 * @author Harold Carr
 */
public class Client
{
    public static final String baseMsg = Client.class.getName();

    public static boolean foundAlternateIIOPAddressComponent = false;
    
    public static void main(String args[])
    {
        try {
            Properties props = new Properties();

            props.put(Common.SOCKET_FACTORY_CLASS_PROPERTY,
		      Common.CUSTOM_FACTORY_CLASS);

	    ORB orb = ORB.init(args, props);

	    I iRef =
		IHelper.narrow(
	            Common.getNameService(orb)
		    .resolve(Common.makeNameComponent(Common.serverName1)));

	    System.out.println(iRef.m("Hello"));

	    if (! foundAlternateIIOPAddressComponent) {
		System.out.println("DID NOT FIND AlternateIIOPAddressComponent");
		System.exit(1);
	    }

	    orb.shutdown(false);
	    orb.destroy();

	    System.out.println();
	    System.out.println(baseMsg + ".main: Test complete.");

        } catch (Exception e) {
            System.out.println(baseMsg + e) ;
            e.printStackTrace(System.out);
            System.exit (1);
        }
    }
}

// End of file.
