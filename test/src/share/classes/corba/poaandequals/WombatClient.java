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
 * @(#)WombatImpl.java	1.9 99/10/29
 *
 * Copyright 1997-1999 Sun Microsystems, Inc. All Rights Reserved.
 * 
 * This software is the proprietary information of Sun Microsystems, Inc.  
 * Use is subject to license terms.
 * 
 */
package corba.poaandequals;

import org.omg.CORBA.*;
import org.omg.CORBA.ORBPackage.*;
import org.omg.PortableServer.*;
import org.omg.PortableServer.POAPackage.*;
import WombatStuff.*;
import corba.framework.*;
import java.io.*;
import java.util.*;

public class WombatClient extends ThreadProcess
{
    ORB orb;

    public org.omg.CORBA.Object readObjref(String file) throws IOException {

	String fil = environment.getProperty("output.dir")
            + File.separator
            + file;

        String ior = null;

	try {
	    java.io.DataInputStream in = 
		new java.io.DataInputStream(new FileInputStream(fil));
	    ior = in.readLine();
	    out.println("IOR: "+ ior);
            in.close();

	} catch (java.io.IOException e) {
	    err.println("Unable to open file "+ fil);
            throw e;
	}

        return orb.string_to_object(ior);
    }

    public void run() {

        try {

            orb = (ORB)extra.get("orb");

            Wombat w = WombatHelper.narrow(readObjref("WombatObjRef"));

            out.println("Invoking...");
            
            out.println("Result: " + w.squawk() + " squawked");

            setExitValue(Controller.SUCCESS);

        } catch (Exception ex) {
            setExitValue(1);
            ex.printStackTrace(err);
        } finally {
            setFinished();
        }

        out.println("Client finished");
    }
}
