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
// Created       : 2004 Aug 12 (Thu) 14:06:19 by Harold Carr.
// Last Modified : 2005 Jun 01 (Wed) 11:07:40 by Harold Carr.
//

package corba.folb_8_1;


import java.util.Collection;
import java.util.Iterator;
import java.util.Properties ;

import org.omg.CORBA.ORB;
import org.omg.PortableServer.POA;

import com.sun.corba.se.pept.transport.Acceptor;
import com.sun.corba.se.pept.transport.Connection;
import com.sun.corba.se.spi.transport.CorbaTransportManager;
import com.sun.corba.se.spi.transport.SocketInfo;

import com.sun.corba.se.spi.orbutil.ORBConstants;
import com.sun.corba.se.impl.transport.SocketOrChannelAcceptorImpl;

/**
 * @author Harold Carr
 */
public class ZeroPortServer1
{
    public static final String baseMsg = Common.class.getName();

    public static ORB orb;

    public static String serverName = Common.zero1;
    public static int[]  socketPorts = Common.socketPorts;

    public static void main(String av[])
    {
	try {
	    Properties props = System.getProperties();
	    Server.setProperties(props, socketPorts);
	    orb = ORB.init(av, props);

	    POA poa = Common.createPOA("zeroPortPOA", true, orb);
	    Common.createAndBind(serverName, orb, poa);
      
	    System.out.println ("Server is ready.");

	    orb.run();
            
        } catch (Exception e) {
            System.out.println(baseMsg + e);
            e.printStackTrace(System.out);
            System.exit(1);
        }
    }
}

// End of file.
