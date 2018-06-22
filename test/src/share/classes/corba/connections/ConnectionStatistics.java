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
// Created       : 2003 Sep 28 (Sun) 09:06:43 by Harold Carr.
// Last Modified : 2003 Sep 29 (Mon) 06:53:22 by Harold Carr.
//

package corba.connections;

import java.util.Collection;
import java.util.Iterator;

import com.sun.corba.ee.spi.orb.ORB;
import com.sun.corba.ee.spi.transport.Acceptor;
import com.sun.corba.ee.spi.transport.TransportManager;
import com.sun.corba.ee.spi.transport.ConnectionCache;
import com.sun.corba.ee.spi.presentation.rmi.StubAdapter ;

import corba.hcks.U;

import org.glassfish.gmbal.GmbalException ;
import org.glassfish.gmbal.AMXClient ;
import org.glassfish.gmbal.ManagedObjectManager ;

public class ConnectionStatistics
{
    private final ORB orb ;
    private final TransportManager ctm ;

    public ConnectionStatistics( ORB orb ) {
        this.orb = orb ;
        this.ctm = orb.getCorbaTransportManager() ;
    }

    private void handleAttribute( StringBuffer result, AMXClient amxc,
        String attributeName ) {

        try {
            Object value = amxc.getAttribute( attributeName ) ;

            pac(result, attributeName + " " + value );
        } catch (GmbalException exc) {
            pac(result, "--------------------------------------------------");
            pac(result, "ERROR: Missing: " + attributeName ) ;
            pac(result, "--------------------------------------------------");
            System.exit(1);
        } 
    }

    private void handleConnectionCache( StringBuffer result,
        ConnectionCache connectionCache ) {

        pac(result, connectionCache.getMonitoringName());

        AMXClient amxc = orb.mom().getAMXClient( connectionCache ) ;
        if (amxc == null) {
            pac(result, "--------------------------------------------------");
            pac(result, "ERROR: Missing: " + connectionCache.getMonitoringName());
            pac(result, "--------------------------------------------------");
            System.exit(1);
        }

        handleAttribute( result, amxc, "totalconnections" ) ;
        handleAttribute( result, amxc, "connectionsidle" ) ;
        handleAttribute( result, amxc, "connectionsbusy" ) ;
    }

    public String outbound(String msg, ORB orb) {
        ManagedObjectManager mom = orb.mom() ;

        StringBuffer result = new StringBuffer("");
        pac(result, "==================================================");
        pac(result, msg + " OUTBOUND:");

        for (ConnectionCache cache : ctm.getOutboundConnectionCaches() ) {
            handleConnectionCache( result, cache ) ;
        }

        return result.toString();
    }

    public String inbound(String msg, ORB orb) {
        ManagedObjectManager mom = orb.mom() ;

        StringBuffer result = new StringBuffer("");
        pac(result, "==================================================");
        pac(result, msg + " INBOUND:");

        for (ConnectionCache cache : ctm.getInboundConnectionCaches() ) {
            handleConnectionCache( result, cache ) ;
        }

        return result.toString();
    }

    // Print And Collect
    private void pac(StringBuffer result, String append) {
        U.sop(append);
        result.append(append).append('\n');
    }
}

// End of file.
