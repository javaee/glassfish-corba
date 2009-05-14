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
// Created       : 2001 Sep 18 (Tue) 11:17:55 by Harold Carr.
// Last Modified : 2003 Aug 20 (Wed) 20:54:15 by Harold Carr.
//

package corba.genericRPCMSGFramework;

import java.util.Properties;
import javax.naming.InitialContext;
import javax.rmi.PortableRemoteObject;

import org.omg.CORBA.Policy;
import org.omg.PortableServer.POA;
import org.omg.PortableServer.RequestProcessingPolicyValue;
import org.omg.PortableServer.ServantRetentionPolicyValue;

import corba.framework.Controller;
import corba.hcks.C;
import corba.hcks.U;

import com.sun.corba.se.spi.transport.CorbaAcceptor;

import com.sun.corba.se.spi.legacy.connection.ORBSocketFactory;
import com.sun.corba.se.spi.legacy.connection.LegacyServerSocketEndPointInfo;
import com.sun.corba.se.spi.orb.ORB;

import com.sun.corba.se.spi.orbutil.ORBConstants;
import com.sun.corba.se.impl.transport.SocketOrChannelAcceptorImpl;
import com.sun.corba.se.impl.transport.SocketOrChannelContactInfoImpl;

public class Server
{
    public static final String BasePortServant_Tie = 
	Server.class.getPackage().getName() + "._BasePortServant_Tie";

    public static final String baseMsg = Server.class.getName();
    public static final String main = baseMsg + ".main";

    public static ORB orb;
    public static InitialContext initialContext;
    public static POA rRootPOA;
    public static Header rHeader;
    public static BasePortServant rBasePortServant;

    public static void main(String[] av)
    {
        try {
	    U.sop(main + " starting");

	    Properties props = new Properties();
	    props.put(ORBConstants.GIOP_VERSION, "1.0");
	    props.put(ORBConstants.GIOP_12_BUFFMGR, "0"); // 0 = GROW
	    props.put("org.omg.PortableInterceptor.ORBInitializerClass.corba.genericRPCMSGFramework.InterceptorInitializer", "dummy");
	    orb = (ORB)ORB.init(av, props);
	    initialContext = C.createInitialContext(orb);

            rRootPOA = U.getRootPOA(orb);
            rRootPOA.the_POAManager().activate();

	    CorbaAcceptor acceptor;

	    acceptor = 
	        new SOAPAcceptor(
                    (com.sun.corba.se.spi.orb.ORB)orb,
		    new SOAPContactInfo(orb, "", 4444, "", null));
	    acceptor.initialize();
	    if (acceptor.shouldRegisterAcceptEvent()) {
		orb.getTransportManager().getSelector(0)
		    .registerForEvent(acceptor.getEventHandler());
	    }
	
	    acceptor = new SocketOrChannelAcceptorImpl(
                orb, 5555, LegacyServerSocketEndPointInfo.NO_NAME,
		ORBSocketFactory.IIOP_CLEAR_TEXT);
	    acceptor.initialize();
	    if (acceptor.shouldRegisterAcceptEvent()) {
		orb.getTransportManager().getSelector(0)
		    .registerForEvent(acceptor.getEventHandler());
	    }

	    U.createWithServantAndBind(Constants.Header1,
				       new HeaderServant(orb),
				       rRootPOA, orb);

	    U.createWithServantAndBind(Constants.Header2,
				       new HeaderServant(orb),
				       rRootPOA, orb);

	    U.createWithServantAndBind(Constants.Header3,
				       new HeaderServant(orb),
				       rRootPOA, orb);

	    /*
	    U.createRMIPOABind(Constants.BasePort1,
			       BasePortServant_Tie,
			       rRootPOA, orb, initialContext);
	    U.createRMIPOABind(Constants.BasePort2,
			       BasePortServant_Tie,
			       rRootPOA, orb, initialContext);
	    */

	    initialContext.rebind(Constants.BasePort1, new BasePortServant());
	    initialContext.rebind(Constants.BasePort2, new BasePortServant());
	    initialContext.rebind(Constants.BasePort3, new BasePortServant());


	    U.sop(main + " ready");
	    U.sop("Server is ready."); // CORBATest handshake.
	    System.out.flush();

	    orb.run();

        } catch (Exception e) {
	    U.sopUnexpectedException(main, e);
	    System.exit(1);
        }
	System.exit(Controller.SUCCESS);
    }

}

// End of file.

