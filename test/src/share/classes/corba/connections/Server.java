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
// Created       : 2003 Sep 26 (Fri) 17:14:01 by Harold Carr.
// Last Modified : 2003 Nov 21 (Fri) 13:37:34 by Harold Carr.
//

package corba.connections;

import java.util.Properties;
import javax.naming.InitialContext;

import com.sun.corba.se.spi.orb.ORB;

import com.sun.corba.se.spi.orbutil.ORBConstants;

import corba.framework.Options;
import corba.hcks.C;
import corba.hcks.U;

public class Server
{
    public static String server1   = "server1";
    public static String server2   = "server2";
    public static String service11 = "service11";
    public static String service12 = "service12";
    public static String service21 = "service21";
    public static String service22 = "service22";

    public static ORB orb;
    public static InitialContext initialContext;
    public static String serverName;
    public static String name1;
    public static String name2;

    public static boolean setWaterMarks = true;
    public static boolean dprint        = false;

    public static void main(String[] av)
    {
	serverName = av[0];
	name1 = av[1];
	name2 = av[2];
	ConnectionStatistics stats = new ConnectionStatistics();

        try {
	    Properties props = new Properties();

	    if (setWaterMarks) {
		props.put(ORBConstants.HIGH_WATER_MARK_PROPERTY, "25");
		props.put(ORBConstants.LOW_WATER_MARK_PROPERTY, "5");
		props.put(ORBConstants.NUMBER_TO_RECLAIM_PROPERTY, "10");
	    }
	    if (dprint) {
		props.put(ORBConstants.DEBUG_PROPERTY, "transport");
	    }
	    orb = (ORB) org.omg.CORBA.ORB.init((String[])null, props);

	    /* Cannot do these here because there is no "Connections" root
	    stats.inbound(serverName + ": after ORB.init", orb);
	    stats.outbound(serverName + ": after ORB.init", orb);
	    */

	    initialContext = C.createInitialContext(orb);
	    stats.outbound(serverName + ": after InitialContext", orb);
	    stats.inbound(serverName + ": after InitialContext", orb);

	    U.sop(serverName + " binding: " + name1 + " " + name2);

	    initialContext.rebind(name1, new RemoteService(orb, serverName));
	    initialContext.rebind(name2, new RemoteService(orb, serverName));

	    stats.outbound(serverName + ": after binding", orb);
	    stats.inbound(serverName + ": after binding", orb);

	    U.sop(Options.defServerHandshake);
	    orb.run();

        } catch (Exception e) {
	    U.sop(serverName + " exception");
	    e.printStackTrace(System.out);
	    System.exit(1);
        }
	U.sop(serverName + " ending successfully");
    }
}

// End of file.

