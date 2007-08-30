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

package corba.poapolicies;

import org.omg.CORBA.*;
import org.omg.PortableServer.*;

import java.io.*;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.util.*;

public class Utility {
    private ORB orb;
    
    public Utility(String[] orbArgs) {
	orb = createORB(orbArgs);
    }
    
    private ORB createORB(String[] args) {
	Properties props = System.getProperties();
	props.put("org.omg.CORBA.ORBClass",
		  System.getProperty("org.omg.CORBA.ORBClass"));
	ORB o = ORB.init(args, props);
	return o;
    }

    public ORB getORB() {
	return this.orb;
    }

    public void writeObjref(org.omg.CORBA.Object ref, String file) {
	String fil = System.getProperty("output.dir")
            + System.getProperty("file.separator")
            + file;
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
	String fil = System.getProperty("output.dir")
            + System.getProperty("file.separator")
            + file;
	try {
	    DataInputStream in = 
		new DataInputStream(new FileInputStream(fil));
	    String ior = in.readLine();
	    System.out.println("IOR: "+ior);
	    return orb.string_to_object(ior);
	} catch (java.io.IOException e) {
	    System.err.println("Unable to open file "+fil);
	    System.exit(1);
	}
	return null;
    }

    public void writeFactory(Util.Factory ref) {
	writeObjref(ref, "Factory");
    }

    public Util.Factory readFactory() {
	return Util.FactoryHelper.narrow(readObjref("Factory"));
    }
}

