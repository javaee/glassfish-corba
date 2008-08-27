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
// Created       : 2001 Sep 18 (Tue) 11:01:45 by Harold Carr.
// Last Modified : 2003 Sep 24 (Wed) 15:39:11 by Harold Carr.
//

package corba.genericRPCMSGFramework;

import java.util.Properties;
import javax.naming.InitialContext;

import com.sun.corba.se.pept.transport.ContactInfo;

import com.sun.corba.se.spi.ior.IOR;
import com.sun.corba.se.spi.orb.ORB;
import com.sun.corba.se.spi.protocol.CorbaClientDelegate;
import com.sun.corba.se.spi.transport.CorbaContactInfoList;
import com.sun.corba.se.spi.presentation.rmi.StubAdapter ;

import com.sun.corba.se.spi.orbutil.ORBConstants;
import com.sun.corba.se.impl.protocol.CorbaClientDelegateImpl;
import com.sun.corba.se.impl.transport.CorbaContactInfoListImpl;

import corba.framework.Controller;
import corba.hcks.C;
import corba.hcks.U;

public class Client
{
    public static final boolean contactSoapBuilders = false;

    public static final String baseMsg = Client.class.getName();
    public static final String main = baseMsg + ".main";

    public static ORB orb;
    public static InitialContext initialContext;

    public static Header rHeader1;
    public static Header rHeader2;
    public static Header rHeader3; // Not used.

    public static BasePort rBasePort1;
    public static BasePort rBasePort2;
    public static BasePort rBasePort3; // Not used.

    public static int iterations = 1;
    public static final int ONLY_OLD = 0;
    public static final int ONLY_NEW = 1;
    public static final int BOTH     = 2;
    public static int runType = BOTH;

    public static void main(String av[])
    {
        try {

	    Properties props = new Properties();
	    props.put(ORBConstants.GIOP_VERSION, "1.0");
	    props.put(ORBConstants.GIOP_12_BUFFMGR, "0"); // 0 = GROW
	    props.put("org.omg.PortableInterceptor.ORBInitializerClass.corba.genericRPCMSGFramework.InterceptorInitializer", "dummy");
	    orb = (ORB)ORB.init(av, props);
	    initialContext = C.createInitialContext(orb);
	    
	    lookupReferences();

	    prepareTests();

	    doInitialCalls();

	    runTests();

        } catch (Exception e) {
            U.sopUnexpectedException(main + " : ", e);
	    U.sop("Client FAILED");
	    System.exit(1);
        }
	U.sop("Client PASSED");
	System.exit(Controller.SUCCESS);
    }

    public static void prepareTests()
	throws
	    Exception
    {
	org.omg.CORBA.Object objectImpl;
	com.sun.corba.se.spi.protocol.CorbaClientDelegate clientDelegate;

	//
	// RMI-IIOP - This causes calls to go through test subcontract
	//

	objectImpl =	(org.omg.CORBA.Object)rBasePort2;
	IOR ior = orb.getIOR( objectImpl, false ) ;
	clientDelegate = (CorbaClientDelegate) StubAdapter.getDelegate(objectImpl) ;
	ContactInfo[] contactInfoListArray1 = {
	    new SOAPContactInfo(orb, "localhost", 4444, "dontcare",
				new EchoSerializers()),
	    new SOAPContactInfo(orb, "soap.bluestone.com", 80, 
		"/scripts/SaISAPI.dll/SaServletEngine.class/hp-soap/soap/rpc/interop/EchoService",
				new EchoSerializers()),
	    new IIOPContactInfo(orb, "localhost", 5555,
				ior ),
	};
	ContactInfo[] contactInfoListArray2 = {
	    new SOAPContactInfo(orb, "localhost", 4444, "dontcare",
				new EchoSerializers()),
	    new IIOPContactInfo(orb, "localhost", 5555,
				ior ),
	};
	ContactInfo[] contactInfoListArray;
	if (contactSoapBuilders) {
	    contactInfoListArray = contactInfoListArray1;
	} else {
	    contactInfoListArray = contactInfoListArray2;
	}
	TestContactInfoList contactInfoList =
	    new TestContactInfoList(orb, contactInfoListArray);
	TestDelegate testDelegate = 
	    new TestDelegate(orb, clientDelegate, contactInfoList);
	StubAdapter.setDelegate( objectImpl, testDelegate ) ;

	//
	// IDL - this causes calls to go through xgiop subcontract
	// Uses different CorbaBroker than above so they do not share
	// a connection cache.
	//

	objectImpl =	(org.omg.CORBA.Object)rHeader2;
	IOR newior = orb.getIOR( objectImpl, false ) ;

	// OLD code:
	// clientDelegate = (ClientDelegate) objectImpl._get_delegate();
	// CorbaContactInfoList corbaContactInfoList =
	    // new CorbaContactInfoListImpl(orb, clientDelegate.getLocatedIOR());
	CorbaContactInfoList corbaContactInfoList =
	    new CorbaContactInfoListImpl(orb, newior );
	CorbaClientDelegate newClientDelegate =
	    new CorbaClientDelegateImpl(orb, corbaContactInfoList);
	StubAdapter.setDelegate( objectImpl, newClientDelegate ) ;
    }

    public static void doInitialCalls()
	throws
	    Exception
    {
	// So we get everything loaded before timing/stepping.
	U.sop(rHeader1.HEADER("Initial call OLD"));
	U.sop(rHeader2.HEADER("Initial call NEW"));

	U.sop(rHeader1.HEADER("Initial call OLD"));
	U.sop(rHeader2.HEADER("Initial call NEW"));
    }

    public static void runTests()
	throws
	    Exception
    {
	runIDLTests();
	runRMITests();
    }

    public static void runIDLTests()
	throws
	    Exception
    {
	Timer oldTimer = new Timer();
	Timer newTimer = new Timer();

	for (int i = 0; i < iterations; ++i) {
	    if (runType == ONLY_OLD || runType == BOTH) {
		oldTimer.begin();
		U.sop(rHeader1.HEADER("Testing OLD"));
		oldTimer.end();
	    }

	    if (runType == ONLY_NEW || runType == BOTH) {
		newTimer.begin();
		U.sop(rHeader2.HEADER("Testing NEW"));
		newTimer.end();
	    }

	    if (runType == ONLY_OLD || runType == BOTH) {
		oldTimer.begin();
		U.sop(rHeader1.HEADER("1 OLD"));
		oldTimer.end();
	    }

	    if (runType == ONLY_NEW || runType == BOTH) {
		newTimer.begin();
		U.sop(rHeader2.HEADER("1 NEW"));
		newTimer.end();
	    }
	}

	U.sop("OLD: " + oldTimer.average());
	U.sop("NEW: " + newTimer.average());
    }

    public static void runRMITests()
	throws
	    Exception
    {
	doCall(rBasePort1,
	       "Testing IIOP out of framework", true);

	doCall(rBasePort2,
	       "Testing RMI-IIOP stub - SOAP - RMI-IIOP Tie", true);

	if (contactSoapBuilders) {
	    doCall(rBasePort2,
		   "Testing RMI-IIOP stub - SOAP - SOAPBuilders", false);
	}

	doCall(rBasePort2,
	       "Testing RMI-IIOP stub - IIOP - RMI-IIOP Tie", true);
    }

    private static int callNumber = 1;

    public static void doCall(BasePort rBasePort, String message, 
			      boolean throwExceptions)
    {
	message =  callNumber++ + ": " + message;
	try {
	    U.sop("---> Client sending: " + message);
	    String result = rBasePort.echoString(message);
	    U.sop("<--- Client received: " + result);
	} catch (Throwable e) {
	    U.sop("Got Exception: : " + e);
	    e.printStackTrace(System.out);
	    if (throwExceptions) {
		RuntimeException rte = new RuntimeException();
		rte.initCause(e);
		throw rte;
	    }
	}
	U.lf();
	U.lf();
	U.lf();
    }

    public static void lookupReferences()
	throws
	    Exception
    {
	rHeader1 = HeaderHelper.narrow(U.resolve(Constants.Header1,orb));
	rHeader2 = HeaderHelper.narrow(U.resolve(Constants.Header2,orb));
	rHeader3 = HeaderHelper.narrow(U.resolve(Constants.Header3,orb));

	rBasePort1 = (BasePort) U.resolve(Constants.BasePort1, orb);
	rBasePort2 = (BasePort) U.resolve(Constants.BasePort2, orb);
	rBasePort3 = (BasePort) U.resolve(Constants.BasePort3, orb);
	/*
	rBasePort1 = (BasePort)
	    U.lookupAndNarrow(Constants.BasePort1,
			      BasePort.class, initialContext);
	rBasePort2 = (BasePort)
	    U.lookupAndNarrow(Constants.BasePort2,
			      BasePort.class, initialContext);
	*/
    }
}

// End of file.

