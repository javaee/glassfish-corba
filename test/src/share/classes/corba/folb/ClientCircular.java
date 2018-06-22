/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright (c) 1997-2018 Oracle and/or its affiliates. All rights reserved.
 *
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common Development
 * and Distribution License("CDDL") (collectively, the "License").  You
 * may not use this file except in compliance with the License.  You can
 * obtain a copy of the License at
 * https://oss.oracle.com/licenses/CDDL+GPL-1.1
 * or LICENSE.txt.  See the License for the specific
 * language governing permissions and limitations under the License.
 *
 * When distributing the software, include this License Header Notice in each
 * file and include the License file at LICENSE.txt.
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
// Created       : 2005 Sep 23 (Fri) 15:17:47 by Harold Carr.
// Last Modified : 2005 Oct 03 (Mon) 10:28:47 by Harold Carr.
//

package corba.folb;

import java.util.Properties;

import org.omg.CORBA.SystemException;

import com.sun.corba.ee.spi.misc.ORBConstants;

import org.testng.annotations.BeforeSuite ;
import org.testng.annotations.Test ;

import org.testng.Assert ;

/**
 * @author Harold Carr
 */
public class ClientCircular extends ClientBase {

    @BeforeSuite
    public void clientSetup() throws Exception {
        Properties props = getDefaultProperties() ;
            
        // Set retry timeout to 5 seconds.
        props.setProperty(ORBConstants.TRANSPORT_TCP_CONNECT_TIMEOUTS_PROPERTY, 
            "250:5000:100");
        // props.setProperty(ORBConstants.DEBUG_PROPERTY,
                          // "transport,subcontract");

        setup( props ) ;
        circularSetup() ;
    }

    @Test
    public void test() throws Exception {
        dprint("--------------------------------------------------");
        dprint("Circular failover without update (send label, no IORUpdate)");
        dprint("--------------------------------------------------");

        makeCall(testRfmWithAddressesWithLabel,
                        Common.TEST_RFM_WITH_ADDRESSES_WITH_LABEL,
                        "Circular failover without update (send label, no IORUpdate)",
                        corba.folb_8_1.Common.X,
                        SEND_MEMBERSHIP_LABEL, NO_IOR_UPDATE);

        dprint("--------------------------------------------------");
        dprint("Remove last Acceptor");
        dprint("--------------------------------------------------");
        gisPoaWithAddressesWithLabels.removeAcceptorAndConnections(
            corba.folb_8_1.Common.X);
        Thread.sleep(5000);

        dprint("--------------------------------------------------");
        dprint("Circular timeout reached.");
        dprint("--------------------------------------------------");
        try {
            makeCall(testRfmWithAddressesWithLabel,
                            Common.TEST_RFM_WITH_ADDRESSES_WITH_LABEL,
                            "Circular timeout reached.",
                            "DUMMY",
                            SEND_MEMBERSHIP_LABEL, NO_IOR_UPDATE);

            Assert.fail( "Circular timeout failed: call incorrectly succeeded" ) ;
        } catch (Exception e) {
            SystemException cf = wrapper.connectFailure( new RuntimeException(),
                "dummy", "dummy", "dummy");
            checkMarshalException("Circular timeout", e, cf);
        }
    }

    public static void main(String[] av) {
        doMain( ClientCircular.class ) ;
    }
}

// End of file.
