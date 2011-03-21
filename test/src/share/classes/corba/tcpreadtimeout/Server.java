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
// Created       : 2003 Apr 09 (Wed) 16:28:12 by Harold Carr.
// Last Modified : 2003 Sep 02 (Tue) 10:14:12 by Harold Carr.
//

package corba.tcpreadtimeout;

import java.util.Properties;

import javax.naming.InitialContext;
import org.omg.PortableServer.POA;

import com.sun.corba.se.spi.transport.Acceptor;
import com.sun.corba.se.spi.transport.TransportManager;
import com.sun.corba.se.spi.orb.ORB;
import com.sun.corba.se.impl.transport.AcceptorImpl;


import corba.framework.Controller;
import corba.framework.Options;
import corba.hcks.C;
import corba.hcks.U;

public class Server 
{
    public static final String baseMsg = Server.class.getName();
    public static final String main = baseMsg + ".main";

    public static final String idlIPOA  = "idlIPOA";

    public static ORB orb;
    public static TransportManager transportManager;
    public static Acceptor acceptor;
    public static POA rootPOA;

    public static void main(String[] av)
    {
        try {
	    U.sop(main + " starting");

	    Properties props = System.getProperties();

	    //props.setProperty();
	    orb = (ORB) ORB.init(av, props);

	    transportManager = orb.getCorbaTransportManager();
	    acceptor = new AcceptorImpl(orb, 4444);
	    transportManager.registerAcceptor(acceptor);
	    acceptor = new AcceptorImpl(orb, 5555);
	    transportManager.registerAcceptor(acceptor);

	    rootPOA = U.getRootPOA(orb);
            rootPOA.the_POAManager().activate();

	    U.createWithServantAndBind(idlIPOA,
				       new idlIServantPOA(orb), rootPOA, orb);

	    U.sop(main + " ready");
	    U.sop(Options.defServerHandshake);
	    System.out.flush();

	    orb.run();

        } catch (Exception e) {
	    U.sopUnexpectedException(main, e);
	    System.exit(1);
        }
	U.sop(main + " ending successfully");
	System.exit(Controller.SUCCESS);
    }
}

// End of file.

