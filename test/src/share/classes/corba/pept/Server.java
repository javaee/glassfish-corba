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
// Created       : 2003 Apr 09 (Wed) 16:28:12 by Harold Carr.
// Last Modified : 2003 Aug 18 (Mon) 10:14:20 by Harold Carr.
//

package corba.pept;

import javax.naming.InitialContext;
import org.omg.PortableServer.POA;

// To Support IORInterceptor to examine IOR contents placed by acceptor.
import java.util.Iterator;
import java.util.Properties;
import java.net.InetAddress;
import org.omg.PortableInterceptor.ORBInitializer;
import org.omg.PortableInterceptor.ORBInitInfo;

import org.omg.IOP.TaggedComponent;
import org.omg.PortableInterceptor.IORInterceptor;
import org.omg.PortableInterceptor.IORInfo;
import org.omg.PortableInterceptor.ObjectReferenceTemplate;
import com.sun.corba.se.spi.legacy.interceptor.IORInfoExt;

import com.sun.corba.se.spi.ior.TaggedProfileTemplate;
import com.sun.corba.se.spi.ior.iiop.IIOPAddress;
import com.sun.corba.se.spi.ior.iiop.IIOPFactories;
import com.sun.corba.se.spi.ior.iiop.IIOPProfileTemplate;
import com.sun.corba.se.spi.ior.iiop.AlternateIIOPAddressComponent;
import com.sun.corba.se.spi.oa.ObjectAdapter;

// END IORInterceptor support.

import corba.framework.Controller;
import corba.framework.Options;
import corba.hcks.C;
import corba.hcks.U;

import com.sun.corba.se.pept.transport.Acceptor;
import com.sun.corba.se.pept.transport.TransportManager;
import com.sun.corba.se.spi.orb.ORB;
import com.sun.corba.se.impl.legacy.connection.LegacyServerSocketManagerImpl;
import com.sun.corba.se.impl.transport.SocketOrChannelAcceptorImpl;

public class Server 
    // REVISIT - IORInterceptor temporary until acceptor work done.
    extends
        org.omg.CORBA.LocalObject
    implements
	ORBInitializer,
	IORInterceptor
{
    public static final String baseMsg = Server.class.getName();
    public static final String main = baseMsg + ".main";

    public static final String rmiiIServantPOA_Tie = 
	Server.class.getPackage().getName() + "._rmiiIServantPOA_Tie";

    public static final String idlIConnect = "idlIConnect";
    public static final String idlIPOA  = "idlIPOA";
    public static final String rmiiIConnect = "rmiiIConnect";
    public static final String rmiiIPOA = "rmiiIPOA";

    public static ORB orb;
    public static InitialContext initialContext;
    public static TransportManager transportManager;
    public static Acceptor acceptor;
    public static POA rootPOA;

    public static void main(String[] av)
    {
        try {
	    U.sop(main + " starting");

	    //LegacyServerSocketManagerImpl.disabled = true;

	    if (! ColocatedClientServer.isColocated) {
		Properties props = System.getProperties();

		props.setProperty("org.omg.PortableInterceptor.ORBInitializerClass." + Server.class.getName(),
				  "dummy");
		orb = (ORB) ORB.init(av, props);
		initialContext = C.createInitialContext(orb);
	    }

	    transportManager = orb.getTransportManager();
	    //acceptor = new XAcceptorImpl(orb, 4444);
	    acceptor = new SocketOrChannelAcceptorImpl(orb, 4444);
	    transportManager.registerAcceptor(acceptor);
	    acceptor = new SocketOrChannelAcceptorImpl(orb, 5555);
	    transportManager.registerAcceptor(acceptor);

	    rootPOA = U.getRootPOA(orb);
            rootPOA.the_POAManager().activate();

	    U.createWithConnectAndBind(idlIConnect, 
				       new idlIServantConnect(), orb);
	    U.createWithServantAndBind(idlIPOA,
				       new idlIServantPOA(), rootPOA, orb);
	    initialContext.rebind(rmiiIConnect, new rmiiIServantConnect());
	    /* REVISIT
	    U.createWithServantAndBind(rmiiIPOA,
				       new rmiiIServantPOA(), rootPOA, 
				       (org.omg.CORBA.ORB) orb);
	    */

	    U.sop(main + " ready");
	    U.sop(Options.defServerHandshake);
	    System.out.flush();

	    synchronized (ColocatedClientServer.signal) {
		ColocatedClientServer.signal.notifyAll();
	    }
	    
	    orb.run();

        } catch (Exception e) {
	    U.sopUnexpectedException(main, e);
	    System.exit(1);
        }
	U.sop(main + " ending successfully");
	System.exit(Controller.SUCCESS);
    }

    public static String filter(String a, String msg)
    {
	if (Client.siemens) {
	    return a;
	}
	return a + "(echo from " + msg + ")";
    }

    ////////////////////////////////////////////////////
    //
    // IORInterceptor support
    //

    public Server()
    {
    }

    public void pre_init(ORBInitInfo orbInitInfo) { }

    public void post_init(ORBInitInfo orbInitInfo)
    {
	try {
	    orbInitInfo.add_ior_interceptor(new Server());
	    System.out.println(baseMsg + ".post_init: add_* completed.");
	} catch (Exception ex) {
	    System.out.println(baseMsg + ".post_init: " + ex);
	    System.exit(-1);
	}
    }


    public String name()    { return baseMsg; }
    public void   destroy() { }
    public void   establish_components(IORInfo iorInfo)
    {
    }

    public void components_established( IORInfo iorInfo )
    {
	try {
	    IORInfoExt iorInfoExt = (IORInfoExt) iorInfo;
	    ObjectAdapter adapter = iorInfoExt.getObjectAdapter();

	    Iterator iterator = adapter.getIORTemplate().iteratorById(
                org.omg.IOP.TAG_INTERNET_IOP.value);
		
	    while (iterator.hasNext()) {
		IIOPProfileTemplate iiopProfileTemplate =
		    (IIOPProfileTemplate) iterator.next();
		IIOPAddress primary = iiopProfileTemplate.getPrimaryAddress();
		String hostname = primary.getHost().toLowerCase();
		int    port     = primary.getPort();
		// REVISIT - test instead of print
		System.out.println("primary: " + hostname + " " + port);
		Iterator tagIterator = iiopProfileTemplate.iteratorById(
		    org.omg.IOP.TAG_ALTERNATE_IIOP_ADDRESS.value);
		while (tagIterator.hasNext()) {
		    AlternateIIOPAddressComponent alternate =
			(AlternateIIOPAddressComponent) tagIterator.next();
		    hostname = alternate.getAddress().getHost();
		    port     = alternate.getAddress().getPort();
		    // REVISIT - test instead of print
		    System.out.println("alternate: " + hostname + " " + port);
		}
	    }
	} catch (Exception e) {
	    System.out.println(baseMsg + e);
	    System.exit(-1);
	}
    }

    public void adapter_manager_state_changed( int managerId, short state )
    {
    }

    public void adapter_state_changed( ObjectReferenceTemplate[] templates,
	short state ) 
    {
    }

}

// End of file.

