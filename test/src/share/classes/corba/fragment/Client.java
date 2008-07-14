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
package corba.fragment;

import javax.rmi.PortableRemoteObject;
import org.omg.CosNaming.*;
import org.omg.CORBA.*;
import java.util.* ;
import java.rmi.RemoteException;
import java.io.*;

import com.sun.corba.se.spi.orbutil.ORBConstants;
import com.sun.corba.se.spi.ior.iiop.GIOPVersion;

public class Client
{
    // size must be divisible by four
    public static void testByteArray(FragmentTester tester, int size)
        throws RemoteException, BadArrayException
    {
        System.out.println("Sending array of length " + size);

        byte array[] = new byte[size];

        int i = 0;

        do {

            for (byte x = 0; x < 4; x++) {
                System.out.print("" + x + " ");
                array[i++] = x;
            }
            // System.out.println();

        } while (i < size);

        byte result[] = tester.verifyTransmission(array);

        if (result == null)
            throw new BadArrayException("result was null!");

        if (array.length != result.length)
            throw new BadArrayException("result length incorrect: " + result.length);

        for (i = 0; i < array.length; i++)
            if (array[i] != result[i])
                throw new BadArrayException("result mismatch at index: " + i);

        System.out.println("testByteArray completed normally");
    }

    public static org.omg.CORBA.Object readObjref(String file, org.omg.CORBA.ORB orb) {
	String fil = System.getProperty("output.dir")+System.getProperty("file.separator")+file;
	try {
	    java.io.DataInputStream in = 
		new java.io.DataInputStream(new FileInputStream(fil));
	    String ior = in.readLine();
	    System.out.println("IOR: "+ior);
	    return orb.string_to_object(ior);
	} catch (java.io.IOException e) {
	    System.err.println("Unable to open file "+fil);
	    System.exit(1);
	}
	return null;
    }

    public static void main(String args[])
    {
        try{

            ORB orb = ORB.init(args, System.getProperties());

            com.sun.corba.se.spi.orb.ORB ourORB
                = (com.sun.corba.se.spi.orb.ORB)orb;

            System.out.println("==== Client GIOP version "
                               + ourORB.getORBData().getGIOPVersion()
                               + " with strategy "
                               + ourORB.getORBData().getGIOPBuffMgrStrategy(
				    ourORB.getORBData().getGIOPVersion())
                               + "====");

            /*
            org.omg.CORBA.Object objRef = 
                orb.resolve_initial_references("NameService");
            NamingContext ncRef = NamingContextHelper.narrow(objRef);
 
            NameComponent nc = new NameComponent("FragmentTester", "");
            NameComponent path[] = {nc};

            org.omg.CORBA.Object obj = ncRef.resolve(path);
            */

            org.omg.CORBA.Object obj = readObjref("IOR", orb);

	    FragmentTester tester = 
                (FragmentTester) PortableRemoteObject.narrow(obj, 
                                                            FragmentTester.class);

            // Do the crazy work here

            int arrayLen = Integer.parseInt(System.getProperty("array.length"));

            testByteArray(tester, arrayLen);

        } catch (Exception e) {
            System.out.println("ERROR : " + e) ;
            e.printStackTrace(System.out);
            System.exit (1);
        }
    }
}
