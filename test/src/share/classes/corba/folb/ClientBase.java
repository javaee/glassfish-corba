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

import com.sun.corba.se.impl.logging.ORBUtilSystemException ;

import com.sun.corba.se.impl.folb.ClientGroupManager;

import com.sun.corba.se.spi.orbutil.ORBConstants;
import com.sun.corba.se.impl.orbutil.ORBUtility;

import corba.folb_8_1.SocketFactoryImpl;
import corba.framework.Controller;
import corba.framework.TestngRunner;
import corba.hcks.U;

import org.testng.annotations.BeforeSuite ;
import org.testng.annotations.Test ;
import org.testng.Assert ;

import org.omg.CORBA.SystemException ;

/**
 * @author Harold Carr
 */
public abstract class ClientBase {    
    public static final boolean SEND_MEMBERSHIP_LABEL = true;
    public static final boolean NO_MEMBERSHIP_LABEL = ! SEND_MEMBERSHIP_LABEL;
    public static final boolean RECEIVE_IOR_UPDATE = true;
    public static final boolean NO_IOR_UPDATE = ! RECEIVE_IOR_UPDATE;

    protected ORB orb = null ;
    protected GroupInfoService gis = null ;
    protected InitialContext initialContext = null ;
    protected GroupInfoServiceTest gisPoaWithAddressesWithLabels = null ;
    protected GroupInfoServiceTest gisPoaWithoutAddressesWithoutLabel = null ;
    protected EchoTest testRfmWithAddressesWithLabel = null ;
    protected EchoTest testRfmWithAddressesWithoutLabel = null ;
    protected ORBUtilSystemException wrapper ;

    protected Properties getDefaultProperties() {
        Properties props = new Properties();
	props.setProperty(ORBConstants.DEBUG_PROPERTY,
            //"giop,transport,subcontract,poa"
            "transport" );

	props.setProperty(ORBConstants.SOCKET_FACTORY_CLASS_PROPERTY,
            SocketFactoryImpl.class.getName());

	// Register a client interceptor to see what connection
	// is being used for test (using a proprietary extension).
	props.setProperty(ORBConstants.PI_ORB_INITIALIZER_CLASS_PREFIX
			  + corba.folb_8_1.Client.class.getName(),
			  "dummy");

	props.setProperty(ORBConstants.USER_CONFIGURATOR_PREFIX
			  + ClientGroupManager.class.getName(),
			  "dummy");

	props.setProperty(ORBConstants.USER_CONFIGURATOR_PREFIX
			  + CSIv2SSLTaggedComponentHandlerImpl.class.getName(),
			  "dummy");

	props.setProperty( ORBConstants.USE_DYNAMIC_STUB_PROPERTY, "true" ) ;

        return props ;
    }

    protected void setup(Properties props) {
        try {
            dprint("--------------------------------------------------");
            dprint("BEGIN SETUP");
            dprint("--------------------------------------------------");

            dprint("--------------------------------------------------");
            dprint("ORB.init");
            dprint("--------------------------------------------------");
            orb = (ORB) ORB.init((String[])null, props);

            wrapper = orb.getLogWrapperTable().get_RPC_TRANSPORT_ORBUtil() ;

            dprint("--------------------------------------------------");
            dprint("Lookup GIS and addObserver for IORUpdates");
            dprint("--------------------------------------------------");
            gis = (GroupInfoService) orb.resolve_initial_references(
                ORBConstants.FOLB_CLIENT_GROUP_INFO_SERVICE);
            gis.addObserver(new GroupInfoServiceObserver() {
                public void membershipChange() {
                    dprint(".membershipChange->:");
                    dprint(".membershipChange: " 
                           + gis.getClusterInstanceInfo((String[])null));
                    dprint(".membershipChange<-:");
                }
            } );
            
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
            testRfmWithAddressesWithLabel = (EchoTest) 
                U.lookupAndNarrow(Common.TEST_RFM_WITH_ADDRESSES_WITH_LABEL,
                                  EchoTest.class,
                                  initialContext);
            
            
            dprint("--------------------------------------------------");
            dprint("Lookup and narrow: " 
                   + Common.TEST_RFM_WITH_ADDRESSES_WITHOUT_LABEL);
            dprint("--------------------------------------------------");
            testRfmWithAddressesWithoutLabel = (EchoTest) 
                U.lookupAndNarrow(Common.TEST_RFM_WITH_ADDRESSES_WITHOUT_LABEL,
                                  EchoTest.class,
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
        } catch (Exception exc) {
            throw new RuntimeException( exc ) ;
        }
    }

    protected void circularSetup() throws Exception {
	dprint("--------------------------------------------------");
	dprint("BEGIN CIRCULAR SETUP");
	dprint("--------------------------------------------------");

	dprint("--------------------------------------------------");
	dprint("Remove W acceptor/connections");
	dprint("--------------------------------------------------");
	gisPoaWithAddressesWithLabels.removeAcceptorAndConnections(
            corba.folb_8_1.Common.W);

	dprint("--------------------------------------------------");
	dprint("Remove X acceptor/connections");
	dprint("--------------------------------------------------");
	gisPoaWithAddressesWithLabels.removeAcceptorAndConnections(
            corba.folb_8_1.Common.X);

	dprint("--------------------------------------------------");
	dprint("Remove Y acceptor/connections");
	dprint("--------------------------------------------------");
	gisPoaWithAddressesWithLabels.removeAcceptorAndConnections(
            corba.folb_8_1.Common.Y);
	Thread.sleep(2000);

	dprint("--------------------------------------------------");
	dprint("Failover without update (send label, no IORUpdate)");
	dprint("--------------------------------------------------");

	makeCall(testRfmWithAddressesWithLabel,
			Common.TEST_RFM_WITH_ADDRESSES_WITH_LABEL,
			"Failover without update (send label, no IORUpdate)",
			corba.folb_8_1.Common.Z,
			SEND_MEMBERSHIP_LABEL, NO_IOR_UPDATE);	    

	dprint("--------------------------------------------------");
	dprint("Restart X Acceptor");
	dprint("--------------------------------------------------");
	gisPoaWithAddressesWithLabels.addAcceptor(
            corba.folb_8_1.Common.X);
	Thread.sleep(2000);

	dprint("--------------------------------------------------");
	dprint("Remove Z Acceptor");
	dprint("--------------------------------------------------");
	gisPoaWithAddressesWithLabels.removeAcceptorAndConnections(
            corba.folb_8_1.Common.Z);
	Thread.sleep(2000);

	dprint("--------------------------------------------------");
	dprint("END CIRCULAR SETUP");
	dprint("--------------------------------------------------");
    }

    protected void makeCall(EchoTest ref, String refName, String arg, 
        String socketType, boolean sendMembershipLabel, 
        boolean receiveIORUpdate) {
        
        try {
            String msg = null ;

            dprint("--------------------------------------------------");
            dprint(refName + ".echo");
            dprint("--------------------------------------------------");

            String result = ref.echo(arg);

            dprint("--------------------------------------------------");
            dprint(refName + ".echo result: " + result);
            if (socketType.equals(corba.folb_8_1.Client.lastSocketTypeUsed)) {
                dprint("    Correct socket type: " + socketType);
            } else {
                Assert.fail( "incorrect socket type: expected: " + socketType
                    + " got: " + corba.folb_8_1.Client.lastSocketTypeUsed ) ;
            }

            if (sendMembershipLabel == ClientGroupManager.sentMemberShipLabel) {
                dprint("    Correctly handled membership label: " 
                       + sentOrNotSent(sendMembershipLabel));
            } else {
                Assert.fail( "incorrectly handled membership label:"
                       + " expected: " + sentOrNotSent(sendMembershipLabel)
                       + " got: " + sentOrNotSent(ClientGroupManager.sentMemberShipLabel) ) ;
            }

            if (receiveIORUpdate == ClientGroupManager.receivedIORUpdate) {
                dprint("    Correctly handled IOR update: " 
                       + receivedOrNotReceived(receiveIORUpdate));
            } else {
                Assert.fail( "incorrectly handled IOR update:"
                       + " expected: " + receivedOrNotReceived(receiveIORUpdate)
                       + " got: " + receivedOrNotReceived(ClientGroupManager.receivedIORUpdate));
            }
        } catch (Exception exc) {
            throw new RuntimeException( exc ) ;
        }
    }

    private String sentOrNotSent(boolean x) {
	return x ? "sent" : "not sent";
    }

    private String receivedOrNotReceived(boolean x) {
	return x ? "received" : "not received";
    }

    protected void checkMarshalException(String msg, Exception got, 
        SystemException expected) {

	if (got.getCause().getClass().isInstance(expected)
	    && ((SystemException)got.getCause()).minor == expected.minor 
	    && ((SystemException)got.getCause()).completed == expected.completed) {

	    dprint("--------------------------------------------------");
	    dprint(msg + ": SUCCEEDED");
	    dprint("--------------------------------------------------");
	} else {
	    got.printStackTrace(System.out);
	    Assert.fail( msg + " FAILED: Expected MarshalException " + expected
	        + "Got   : " + got
	        + "detail: " + got.getCause() ) ;
	}
    }

    protected void dprint(String msg) {
	ORBUtility.dprint(this, msg);
    }

    protected static void doMain( Class<?> cls ) {
        TestngRunner runner = new TestngRunner() ;
        runner.registerClass( cls ) ;
        runner.run() ;
        runner.systemExit() ;
    }
}
