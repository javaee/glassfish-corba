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
// Created       : 2005 Jun 08 (Wed) 19:24:39 by Harold Carr.
// Last Modified : 2005 Sep 30 (Fri) 15:35:57 by Harold Carr.
//

package corba.folb;

import org.testng.Assert ;
import org.testng.annotations.Test ;
import org.testng.annotations.BeforeSuite ;

/**
 * @author Harold Carr
 */
public class Client extends ClientBase {
    
    // public CSIv2SSLTaggedComponentHandler csiv2SSLTaggedComponentHandler;

    public Client() {
    }

    @BeforeSuite 
    public void clientSetup() {
        setup( getDefaultProperties() ) ;
    }

    @Test
    public void testBootstrap() {
        dprint("--------------------------------------------------");
        dprint("testBootstrap (missing label, therefore IORUpdate)");
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
    }

    @Test( dependsOnMethods={ "testBootstrap" } )
    public void testNormalOperation() {
        dprint("--------------------------------------------------");
        dprint("testNormalOperation (send label, no IORUpdate)");
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
    }

    @Test( dependsOnMethods={ "testNormalOperation" } )
    public void testIORUpdate() {
        try {
            dprint("--------------------------------------------------");
            dprint("testIORUpdate only (send label, receive IORUpdate)");
            dprint("setup: remove instance");
            dprint("--------------------------------------------------");
            doRemoveInstance(gisPoaWithAddressesWithLabels, corba.folb_8_1.Common.Z);
            makeCall(testRfmWithAddressesWithLabel,
                Common.TEST_RFM_WITH_ADDRESSES_WITH_LABEL,
                "IORUpdate only1 (send label, receive IORUpdate)",
                corba.folb_8_1.Common.W, SEND_MEMBERSHIP_LABEL,
                RECEIVE_IOR_UPDATE);
            makeCall(testRfmWithAddressesWithLabel,
                Common.TEST_RFM_WITH_ADDRESSES_WITH_LABEL,
                "IORUpdate only2 (send label, no IORUpdate)",
                corba.folb_8_1.Common.W, SEND_MEMBERSHIP_LABEL,
                NO_IOR_UPDATE);

            doAddInstance(gisPoaWithAddressesWithLabels, corba.folb_8_1.Common.Z);
            makeCall(testRfmWithAddressesWithLabel,
                Common.TEST_RFM_WITH_ADDRESSES_WITH_LABEL,
                "IORUpdate only3 (send label, receive IORUpdate)",
                corba.folb_8_1.Common.W, SEND_MEMBERSHIP_LABEL,
                RECEIVE_IOR_UPDATE);
            makeCall(testRfmWithAddressesWithLabel,
                Common.TEST_RFM_WITH_ADDRESSES_WITH_LABEL,
                "IORUpdate only4 (send label, no IORUpdate)",
                corba.folb_8_1.Common.W, SEND_MEMBERSHIP_LABEL,
                NO_IOR_UPDATE);
        } catch (Exception ex) {
            Assert.fail( "Caught exception in testIORUpdate", ex ) ;
        }
    }

    @Test( dependsOnMethods={ "testIORUpdate" } )
    public void testFailoverWithoutUpdate() {
        try {
            dprint("--------------------------------------------------");
            dprint("testFailoverWithoutUpdate (send label, no IORUpdate)");
            dprint("Setup: remove W listener");
            dprint("--------------------------------------------------");
            gisPoaWithAddressesWithLabels.removeAcceptorAndConnections(corba.folb_8_1.Common.W);
            Thread.sleep(2000);
            makeCall(testRfmWithAddressesWithLabel,
                Common.TEST_RFM_WITH_ADDRESSES_WITH_LABEL,
                "Failover without update (send label, no IORUpdate)",
                corba.folb_8_1.Common.X, SEND_MEMBERSHIP_LABEL, NO_IOR_UPDATE);

            dprint("--------------------------------------------------");
            dprint("Check stuck to new instance (send label, no IORUpdate)");
            dprint("--------------------------------------------------");
            makeCall(testRfmWithAddressesWithLabel,
                Common.TEST_RFM_WITH_ADDRESSES_WITH_LABEL,
                "Check stuck to new instance (send label, no IORUpdate)",
                corba.folb_8_1.Common.X, SEND_MEMBERSHIP_LABEL, NO_IOR_UPDATE);
        } catch (Exception ex) {
            Assert.fail( "Caught exception in testFailoverWithoutUpdate", ex ) ;
        }
    }

    @Test( dependsOnMethods={ "testFailoverWithoutUpdate" } )
    public void testFailoverWithUpdate() {
        try {
            dprint("--------------------------------------------------");
            dprint("testFailoverWithUpdate (send label, IORUpdate)");
            dprint("Setup: remove instances W, X");
            dprint("Setup: remove X listener");
            dprint("--------------------------------------------------");
            doRemoveInstance(gisPoaWithAddressesWithLabels, corba.folb_8_1.Common.W);
            doRemoveInstance(gisPoaWithAddressesWithLabels, corba.folb_8_1.Common.X);
            gisPoaWithAddressesWithLabels.removeAcceptorAndConnections(corba.folb_8_1.Common.X);
            Thread.sleep(2000);
            makeCall(testRfmWithAddressesWithLabel,
                Common.TEST_RFM_WITH_ADDRESSES_WITH_LABEL,
                "Failover with update (send label, IORUpdate)",
                corba.folb_8_1.Common.Y, SEND_MEMBERSHIP_LABEL,
                RECEIVE_IOR_UPDATE);

            dprint("--------------------------------------------------");
            dprint("Check stuck to new instance (send label, no IORUpdate)");
            dprint("--------------------------------------------------");
            makeCall(testRfmWithAddressesWithLabel,
                Common.TEST_RFM_WITH_ADDRESSES_WITH_LABEL,
                "Check stuck to new instance (send label, no IORUpdate)",
                corba.folb_8_1.Common.Y, SEND_MEMBERSHIP_LABEL,
                NO_IOR_UPDATE);
        } catch (Exception ex) {
            Assert.fail("Caught exception in testFailoverWithUpdate", ex);
        }
    }

    private void doAddInstance( 
        GroupInfoServiceTest gist,
        String arg ) throws Exception {

        dprint( "Adding instance " + arg ) ;
        gist.addInstance(arg);

        // Add a delay here to avoid race condition in test:
        // We add instance asynchronously, and a call that
        // completes after the start of RFM restartFactories
        // does NOT get updated in order to avoid a serious
        // deadlock problem.

        Thread.sleep( 2*1000 ) ;
    }

    private void doRemoveInstance( 
        GroupInfoServiceTest gist,
        String arg ) throws Exception {

        dprint( "Removing instance " + arg ) ;
        gist.removeInstance(arg);

        // Add a delay here to avoid race condition in test:
        // We remove instance asynchronously, and a call that
        // completes after the start of RFM restartFactories
        // does NOT get updated in order to avoid a serious
        // deadlock problem.

        Thread.sleep( 2*1000 ) ;
    }

    public static void main(String[] av) {
        doMain( Client.class ) ;
    }
}

// End of file.
