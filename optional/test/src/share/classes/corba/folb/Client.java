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
// Created       : 2005 Jun 08 (Wed) 19:24:39 by Harold Carr.
// Last Modified : 2005 Sep 30 (Fri) 15:35:57 by Harold Carr.
//

package corba.folb;

import java.util.Hashtable;
import java.util.Properties;
import javax.naming.InitialContext;
import javax.rmi.PortableRemoteObject;

import com.sun.corba.se.spi.folb.CSIv2SSLTaggedComponentHandler;
import com.sun.corba.se.spi.folb.GroupInfoService;
import com.sun.corba.se.spi.folb.GroupInfoServiceObserver;
import com.sun.corba.se.spi.orb.ORB;

import com.sun.corba.se.impl.folb.ClientGroupManager;
import com.sun.corba.se.impl.orbutil.ORBConstants;
import com.sun.corba.se.impl.orbutil.ORBUtility;

import corba.folb_8_1.SocketFactoryImpl;
import corba.framework.Controller;
import corba.hcks.U;

/**
 * @author Harold Carr
 */
public class Client
    implements GroupInfoServiceObserver
{
    static {
	// This is needed to guarantee that this test will ALWAYS use dynamic
	// RMI-IIOP.  Currently the default is dynamic when renamed to "ee",
	// but static in the default "se" packaging, and this test will
	// fail without dynamic RMI-IIOP.
	System.setProperty( ORBConstants.USE_DYNAMIC_STUB_PROPERTY, "true" ) ;
    }

    public static final boolean SEND_MEMBERSHIP_LABEL = true;
    public static final boolean NO_MEMBERSHIP_LABEL = ! SEND_MEMBERSHIP_LABEL;
    public static final boolean RECEIVE_IOR_UPDATE = true;
    public static final boolean NO_IOR_UPDATE = ! RECEIVE_IOR_UPDATE;
    public static CSIv2SSLTaggedComponentHandler csiv2SSLTaggedComponentHandler;
    public static int numberOfFailures = 0;

    public static ORB orb;
    public static GroupInfoService gis;
    public static InitialContext initialContext;
    public static GroupInfoServiceTest gisPoaWithAddressesWithLabels;
    public static GroupInfoServiceTest gisPoaWithoutAddressesWithoutLabel;
    public static Test testRfmWithAddressesWithLabel;
    public static Test testRfmWithAddressesWithoutLabel;


    public static void setProperties(Properties props)
    {
	//
	// Debugging flags.
	//

	props.setProperty(ORBConstants.DEBUG_PROPERTY,
			  //"giop,transport,subcontract,poa"
			  "transport"
			  );


	//
	// Register the socket factory that knows how to create
	// Sockets of type used by the server.
	//

	props.setProperty(ORBConstants.SOCKET_FACTORY_CLASS_PROPERTY,
			  SocketFactoryImpl.class.getName());

	//
	// Register a client interceptor to see what connection
	// is being used for test (using a proprietary extension).
	//

	props.setProperty(ORBConstants.PI_ORB_INITIALIZER_CLASS_PREFIX
			  + corba.folb_8_1.Client.class.getName(),
			  "dummy");

	//
	// Register a FailoverManager
	//

	props.setProperty(ORBConstants.USER_CONFIGURATOR_PREFIX
			  + ClientGroupManager.class.getName(),
			  "dummy");

	//
	// This configurator registers the CSIv2SSLTaggedComponentHandler
	//

	props.setProperty(ORBConstants.USER_CONFIGURATOR_PREFIX
			  + CSIv2SSLTaggedComponentHandlerImpl.class.getName(),
			  "dummy");
    }

    public static void setup(Properties props)
	throws Exception
    {
	dprint("--------------------------------------------------");
	dprint("BEGIN SETUP");
	dprint("--------------------------------------------------");
	
	dprint("--------------------------------------------------");
	dprint("ORB.init");
	dprint("--------------------------------------------------");
	orb = (ORB) ORB.init((String[])null, props);
	
	dprint("--------------------------------------------------");
	dprint("Lookup GIS and addObserver for IORUpdates");
	dprint("--------------------------------------------------");
	gis = (GroupInfoService) orb.resolve_initial_references(
	    ORBConstants.FOLB_CLIENT_GROUP_INFO_SERVICE);
	gis.addObserver(new Client());
	
	dprint("--------------------------------------------------");
	dprint("new InitialContext");
	dprint("--------------------------------------------------");
	Hashtable env = new Hashtable();
	env.put("java.naming.corba.orb", orb);
	initialContext = new InitialContext(env);

	dprint("--------------------------------------------------");
	dprint("lookup and narrow: " 
	       + Common.GIS_POA_WITH_ADDRESSES_WITH_LABEL);
	dprint("--------------------------------------------------");
	gisPoaWithAddressesWithLabels = (GroupInfoServiceTest)
	    U.lookupAndNarrow(Common.GIS_POA_WITH_ADDRESSES_WITH_LABEL,
			      GroupInfoServiceTest.class, 
			      initialContext);

	dprint("--------------------------------------------------");
	dprint("lookup and narrow: " 
	       + Common.GIS_POA_WITHOUT_ADDRESSES_WITHOUT_LABEL);
	dprint("--------------------------------------------------");
	gisPoaWithoutAddressesWithoutLabel = (GroupInfoServiceTest)
	    U.lookupAndNarrow(Common.GIS_POA_WITHOUT_ADDRESSES_WITHOUT_LABEL, 
			      GroupInfoServiceTest.class, 
			      initialContext);

	dprint("--------------------------------------------------");
	dprint("Lookup and narrow: " 
	       + Common.TEST_RFM_WITH_ADDRESSES_WITH_LABEL);
	dprint("--------------------------------------------------");
	testRfmWithAddressesWithLabel = (Test) 
	    U.lookupAndNarrow(Common.TEST_RFM_WITH_ADDRESSES_WITH_LABEL,
			      Test.class,
			      initialContext);
	
	
	dprint("--------------------------------------------------");
	dprint("Lookup and narrow: " 
	       + Common.TEST_RFM_WITH_ADDRESSES_WITHOUT_LABEL);
	dprint("--------------------------------------------------");
	testRfmWithAddressesWithoutLabel = (Test) 
	    U.lookupAndNarrow(Common.TEST_RFM_WITH_ADDRESSES_WITHOUT_LABEL,
			      Test.class,
			      initialContext);


	dprint("--------------------------------------------------");
	dprint("remove IIOP_CLEAR_TEXT listener");
	dprint("--------------------------------------------------");
	gisPoaWithAddressesWithLabels.removeAcceptorAndConnections(
            com.sun.corba.se.spi.transport.SocketInfo.IIOP_CLEAR_TEXT);
	Thread.sleep(2000);

	dprint("--------------------------------------------------");
	dprint("END SETUP");
	dprint("--------------------------------------------------");
    }

    public static void main(String[] av)
    {
	try {

	    Properties props = new Properties();
	    setProperties(props);

	    //
	    // Setup
	    //

	    setup(props);

	    //
	    // TEST
	    //

	    dprint("--------------------------------------------------");
	    dprint("BOOTSTRAP Test (missing label, therefore IORUpdate)");
	    dprint("--------------------------------------------------");

	    makeCall(testRfmWithAddressesWithoutLabel, 
		     Common.TEST_RFM_WITH_ADDRESSES_WITHOUT_LABEL,
		     "BOOTSTRAP1 Test (missing label, therefore IORUpdate)",
		     corba.folb_8_1.Common.W,
		     NO_MEMBERSHIP_LABEL, RECEIVE_IOR_UPDATE);
	    makeCall(testRfmWithAddressesWithoutLabel,
		     Common.TEST_RFM_WITH_ADDRESSES_WITHOUT_LABEL,
		     "BOOTSTRAP2 Test (missing label, therefore IORUpdate)",
		     corba.folb_8_1.Common.W,
		     NO_MEMBERSHIP_LABEL, RECEIVE_IOR_UPDATE);

	    //
	    // TEST
	    //

	    dprint("--------------------------------------------------");
	    dprint("Normal operation (send label, no IORUpdate)");
	    dprint("--------------------------------------------------");

	    makeCall(testRfmWithAddressesWithLabel,
		     Common.TEST_RFM_WITH_ADDRESSES_WITH_LABEL,
		     "Normal operation1 (send label, no IORUpdate)",
		     corba.folb_8_1.Common.W,
		     SEND_MEMBERSHIP_LABEL, NO_IOR_UPDATE);
	    makeCall(testRfmWithAddressesWithLabel,
		     Common.TEST_RFM_WITH_ADDRESSES_WITH_LABEL,
		     "Normal operation2 (send label, no IORUpdate)",
		     corba.folb_8_1.Common.W,
		     SEND_MEMBERSHIP_LABEL, NO_IOR_UPDATE);

	    //
	    // TEST
	    //

	    dprint("--------------------------------------------------");
	    dprint("IORUpdate only (send label, receive IORUpdate)");
	    dprint("setup: remove instance");
	    dprint("--------------------------------------------------");
	    doRemoveInstance(gisPoaWithAddressesWithLabels,
			     corba.folb_8_1.Common.Z);

	    makeCall(testRfmWithAddressesWithLabel,
		     Common.TEST_RFM_WITH_ADDRESSES_WITH_LABEL,
		     "IORUpdate only1 (send label, receive IORUpdate)",
		     corba.folb_8_1.Common.W,
		     SEND_MEMBERSHIP_LABEL, RECEIVE_IOR_UPDATE);
	    makeCall(testRfmWithAddressesWithLabel,
		     Common.TEST_RFM_WITH_ADDRESSES_WITH_LABEL,
		     "IORUpdate only2 (send label, no IORUpdate)",
		     corba.folb_8_1.Common.W,
		     SEND_MEMBERSHIP_LABEL, NO_IOR_UPDATE);
	    doAddInstance(gisPoaWithAddressesWithLabels,
			  corba.folb_8_1.Common.Z);
	    makeCall(testRfmWithAddressesWithLabel,
		     Common.TEST_RFM_WITH_ADDRESSES_WITH_LABEL,
		     "IORUpdate only3 (send label, receive IORUpdate)",
		     corba.folb_8_1.Common.W,
		     SEND_MEMBERSHIP_LABEL, RECEIVE_IOR_UPDATE);
	    makeCall(testRfmWithAddressesWithLabel,
		     Common.TEST_RFM_WITH_ADDRESSES_WITH_LABEL,
		     "IORUpdate only4 (send label, no IORUpdate)",
		     corba.folb_8_1.Common.W,
		     SEND_MEMBERSHIP_LABEL, NO_IOR_UPDATE);

	    //
	    // TEST
	    //

	    dprint("--------------------------------------------------");
	    dprint("Failover without update (send label, no IORUpdate)");
	    dprint("Setup: remove W listener");
	    dprint("--------------------------------------------------");
	    gisPoaWithAddressesWithLabels.removeAcceptorAndConnections(
                corba.folb_8_1.Common.W);
	    Thread.sleep(2000);

	    makeCall(testRfmWithAddressesWithLabel,
		     Common.TEST_RFM_WITH_ADDRESSES_WITH_LABEL,
		     "Failover without update (send label, no IORUpdate)",
		     corba.folb_8_1.Common.X,
		     SEND_MEMBERSHIP_LABEL, NO_IOR_UPDATE);

	    dprint("--------------------------------------------------");
	    dprint("Check stuck to new instance (send label, no IORUpdate)");
	    dprint("--------------------------------------------------");

	    makeCall(testRfmWithAddressesWithLabel,
		     Common.TEST_RFM_WITH_ADDRESSES_WITH_LABEL,
		     "Check stuck to new instance (send label, no IORUpdate)",
		     corba.folb_8_1.Common.X,
		     SEND_MEMBERSHIP_LABEL, NO_IOR_UPDATE);

	    //
	    // TEST
	    //

	    dprint("--------------------------------------------------");
	    dprint("Failover with update (send label, IORUpdate)");
	    dprint("Setup: remove instances W, X");
	    dprint("Setup: remove X listener");
	    dprint("--------------------------------------------------");
	    doRemoveInstance(gisPoaWithAddressesWithLabels, 
			     corba.folb_8_1.Common.W);
	    doRemoveInstance(gisPoaWithAddressesWithLabels, 
			     corba.folb_8_1.Common.X);
	    gisPoaWithAddressesWithLabels.removeAcceptorAndConnections(
                corba.folb_8_1.Common.X);
	    Thread.sleep(2000);

	    makeCall(testRfmWithAddressesWithLabel,
		     Common.TEST_RFM_WITH_ADDRESSES_WITH_LABEL,
		     "Failover with update (send label, IORUpdate)",
		     corba.folb_8_1.Common.Y,
		     SEND_MEMBERSHIP_LABEL, RECEIVE_IOR_UPDATE);

	    dprint("--------------------------------------------------");
	    dprint("Check stuck to new instance (send label, no IORUpdate)");
	    dprint("--------------------------------------------------");

	    makeCall(testRfmWithAddressesWithLabel,
		     Common.TEST_RFM_WITH_ADDRESSES_WITH_LABEL,
		     "Check stuck to new instance (send label, no IORUpdate)",
		     corba.folb_8_1.Common.Y,
		     SEND_MEMBERSHIP_LABEL, NO_IOR_UPDATE);

	    //
	    // TEST
	    //

	    // TBD: Independent POAs
	    // TBD: Circular failover success
	    // TBD: Circular failover fail

	    if (numberOfFailures > 0) {
		throw new Exception("Failures: " 
				    + new Integer(numberOfFailures).toString());
	    }

	    dprint("--------------------------------------------------");
	    dprint("Client SUCCESS");
	    dprint("--------------------------------------------------");
	    System.exit(Controller.SUCCESS);

	} catch (Exception e) {
	    e.printStackTrace(System.out);
	    dprint("--------------------------------------------------");
	    dprint("Client FAILURE");
	    dprint("--------------------------------------------------");
	    System.exit(1);
	}
    }

    public static void doAddInstance( 
	GroupInfoServiceTest gist,
	String arg ) throws Exception
    {
	gist.addInstance(arg);

	// Add a delay here to avoid race condition in test:
	// We add instance asynchronously, and a call that
	// completes after the start of RFM restartFactories
	// does NOT get updated in order to avoid a serious
	// deadlock problem.

	Thread.sleep( 2*1000 ) ;
    }

    public static void doRemoveInstance( 
	GroupInfoServiceTest gist,
	String arg ) throws Exception
    {
	gist.removeInstance(arg);

	// Add a delay here to avoid race condition in test:
	// We remove instance asynchronously, and a call that
	// completes after the start of RFM restartFactories
	// does NOT get updated in order to avoid a serious
	// deadlock problem.

	Thread.sleep( 2*1000 ) ;
    }

    public static void makeCall(Test ref, 
				String refName, 
				String arg,
				String socketType, 
				boolean sendMembershipLabel,
				boolean receiveIORUpdate)
	throws Exception
    {
	dprint("--------------------------------------------------");
	dprint(refName + ".echo");
	dprint("--------------------------------------------------");

	String result = ref.echo(arg);

	dprint("--------------------------------------------------");
	dprint(refName + ".echo result: " + result);
	if (socketType.equals(corba.folb_8_1.Client.lastSocketTypeUsed)) {
	    dprint("    Correct socket type: " + socketType);
	} else {
	    dprint("!!! INCORRECT socket type:" 
		   + " expected: " + socketType
		   + " got: " + corba.folb_8_1.Client.lastSocketTypeUsed);
	    numberOfFailures++;
	}
	if (sendMembershipLabel == ClientGroupManager.sentMemberShipLabel) {
	    dprint("    Correctly handled membership label: " 
		   + sentOrNotSent(sendMembershipLabel));
	} else {
	    dprint("!!! INCORRECTLY handled membership label:"
		   + " expected: " + sentOrNotSent(sendMembershipLabel)
		   + " got: " + sentOrNotSent(ClientGroupManager.sentMemberShipLabel));

	    numberOfFailures++;
	}
	if (receiveIORUpdate == ClientGroupManager.receivedIORUpdate) {
	    dprint("    Correctly handled IOR update: " 
		   + receivedOrNotReceived(receiveIORUpdate));
	} else {
	    dprint("!!! INCORRECTLY handled IOR update:"
		   + " expected: " + receivedOrNotReceived(receiveIORUpdate)
		   + " got: " + receivedOrNotReceived(ClientGroupManager.receivedIORUpdate));
	    numberOfFailures++;
	}
	dprint("--------------------------------------------------");
    }

    public static String sentOrNotSent(boolean x)
    {
	return x ? "sent" : "not sent";
    }

    public static String receivedOrNotReceived(boolean x)
    {
	return x ? "received" : "not received";
    }

    public static void dprint(String msg)
    {
	ORBUtility.dprint("Client", msg);
    }

    ////////////////////////////////////////////////////
    //
    // GroupInfoServiceObserver
    //

    public void membershipChange() {
	dprint(".membershipChange->:");
	dprint(".membershipChange: " 
	       + gis.getClusterInstanceInfo((String[])null));
	dprint(".membershipChange<-:");
    }
}

// End of file.
