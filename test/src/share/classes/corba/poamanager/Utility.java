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
/*
 * @(#)Utility.java	1.5 99/10/29
 *
 * Copyright 1997-1999 Sun Microsystems, Inc. All Rights Reserved.
 * 
 * This software is the proprietary information of Sun Microsystems, Inc.  
 * Use is subject to license terms.
 * 
 */
package corba.poamanager;

import org.omg.CORBA.*;
import org.omg.PortableServer.*;

import java.io.*;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.util.*;

import com.sun.corba.se.spi.orbutil.ORBConstants;

public class Utility {
    private ORB orb;
    
    public Utility(String args[]) {
	orb = createORB(args);
    }
    
    private static ORB createORB(String args[]) {
	Properties props = new Properties();
        props.setProperty("org.omg.CORBA.ORBClass",
                  System.getProperty("org.omg.CORBA.ORBClass"));
	props.setProperty(ORBConstants.TRANSPORT_TCP_CONNECT_TIMEOUTS_PROPERTY, "250:2000:100");
	// props.setProperty("com.sun.corba.se.ORBDebug", "transport,subcontract,poa");
	ORB o = ORB.init(args, props);
	return o;
    }

    public ORB getORB() {
	return this.orb;
    }

    public void writeObjref(org.omg.CORBA.Object ref, String file) {
	String fil = System.getProperty("output.dir")+System.getProperty("file.separator")+file;
	try {
	    DataOutputStream out = new 
		DataOutputStream(new FileOutputStream(fil));
	    out.writeBytes(orb.object_to_string(ref));
	} catch (java.io.IOException e) {
	    System.err.println("Unable to open file "+fil);
	    System.exit(1);
	}
    }

    public org.omg.CORBA.Object readObjref(String file) {
	String fil = System.getProperty("output.dir")+System.getProperty("file.separator")+file;
	try {
            BufferedReader in = new BufferedReader(new FileReader(fil));
	    String ior = in.readLine();
	    System.out.println("IOR: "+ior);
	    return orb.string_to_object(ior);
	} catch (java.io.IOException e) {
	    System.err.println("Unable to open file "+fil);
	    System.exit(1);
	}
	return null;
    }

    public void writeFactory(Util.GenericFactory ref) {
	writeObjref(ref, "Factory");
    }

    public Util.GenericFactory readFactory() {
	return Util.GenericFactoryHelper.narrow(readObjref("Factory"));
    }
}

