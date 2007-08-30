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
//
// Created       : 2002 Jul 19 (Fri) 14:47:13 by Harold Carr.
// Last Modified : 2005 Sep 28 (Wed) 14:55:27 by Harold Carr.
//

package corba.folb_8_1;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.omg.CORBA.CompletionStatus;
import org.omg.CORBA.INTERNAL;
import org.omg.CORBA.ORB;
import org.omg.CORBA.Policy;
import org.omg.PortableServer.LifespanPolicyValue;
import org.omg.PortableServer.POA;
import org.omg.PortableServer.Servant;
import org.omg.CosNaming.*;
import org.omg.IOP.Codec;
import org.omg.IOP.CodecFactory;
import org.omg.IOP.CodecFactoryHelper;
import org.omg.IOP.Encoding;
import org.omg.IOP.ENCODING_CDR_ENCAPS;

import com.sun.corba.se.spi.extension.ZeroPortPolicy;

/**
 * @author Harold Carr
 */
public abstract class Common
{
    public static boolean timing = false;
    public static final String FAILOVER_SUPPORT = "FAILOVER_SUPPORT";
    public static final String FAILOVER         = "FAILOVER";
    public static final String CACHE            = "CACHE";

    public static final String W = "W";
    public static final String X = "X";
    public static final String Y = "Y";
    public static final String Z = "Z";

    public static String[] socketTypes = { W,    X,    Y,    Z };
    public static int[]    socketPorts = { 3333, 4444, 5555, 0 };
    public static int[]    zero2Ports  = { 3334, 4445, 5556, 0 };
    public static HashMap socketTypeToPort = new HashMap();
    public static HashMap portToSocketType = new HashMap();
    static {
	for (int i = 0; i < socketTypes.length; i++) {
	    Integer port = new Integer(socketPorts[i]);
	    socketTypeToPort.put(socketTypes[i], port);
	    portToSocketType.put(port, socketTypes[i]);
	}
    }
    public static final String serverName1 = "I1";
    public static final String serverName2 = "I2";
    public static final String zero1 = "zero1";
    public static final String zero2 = "zero2";

    public static POA createPOA(String name, boolean zeroPortP, ORB orb)
	throws Exception
    {
	// Get rootPOA

	POA rootPoa = (POA) orb.resolve_initial_references("RootPOA");
	rootPoa.the_POAManager().activate();

	// Create child

	List policies = new ArrayList();

	// Create child POA
	policies.add(
	    rootPoa.create_lifespan_policy(LifespanPolicyValue.TRANSIENT));
	if (zeroPortP) {
	    policies.add(ZeroPortPolicy.getPolicy());
	}
	Policy[] policy = (Policy[]) policies.toArray(new Policy[0]);
	POA childPoa = rootPoa.create_POA(name, null, policy);
	childPoa.the_POAManager().activate();
	return childPoa;
    }
	
    // create servant and register it with a POA
    public static org.omg.CORBA.Object createAndBind(String name,
						     ORB orb, POA poa)
	throws Exception
    {
	Servant servant;
	if (name.equals(Common.serverName1)) {
	    servant = new IServant(orb);
	} else {
	    servant = new I2Servant(orb);
	}
	byte[] id = poa.activate_object(servant);
	org.omg.CORBA.Object ref = poa.id_to_reference(id);
	Common.getNameService(orb).rebind(Common.makeNameComponent(name), ref);
	return ref;
    }

    public static NamingContext getNameService(ORB orb)
    {
        org.omg.CORBA.Object objRef = null;
	try {
	    objRef = orb.resolve_initial_references("NameService");
	} catch (Exception ex) {
	    System.out.println("Common.getNameService: " + ex);
	    System.exit(1);
	}
        return NamingContextHelper.narrow(objRef);
    }

    public static NameComponent[] makeNameComponent(String name)
    {
        NameComponent nc = new NameComponent(name, "");
        NameComponent path[] = {nc};
	return path;
    }


    public static Codec getCodec(ORB orb)
    {
	try {
	    CodecFactory codecFactory = 
		CodecFactoryHelper.narrow(orb.resolve_initial_references("CodecFactory"));
	    return codecFactory.create_codec(new Encoding((short)ENCODING_CDR_ENCAPS.value, (byte)1, (byte)2));
	} catch (Exception e) {
	    System.out.println("Unexpected: " + e);
	    System.exit(1);
	}
	return null;
    }

    public static String[] concat(String[] a1, String[] a2)
    {
	String[] result = new String[a1.length + a2.length];

	int index = 0;
	
	for (int i = 0; i < a1.length; ++i) {
	    result[index++] = a1[i];
	}

	for (int i = 0; i < a2.length; ++i) {
	    result[index++] = a2[i];
	}

	/*
        System.out.println(formatStringArray(a1));
	System.out.println(formatStringArray(a2));
	System.out.println(formatStringArray(result));
	*/

	return result;
    }

    public static String formatStringArray(String[] a)
    {
        String result = "";
	for (int i = 0; i < a.length; ++i) {
	    result += a[i] + " ";
	}
	return result;
    }
}

// End of file.

