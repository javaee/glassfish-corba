/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2010 Sun Microsystems, Inc. All rights reserved.
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
// Created       : 2003 Sep 28 (Sun) 09:06:43 by Harold Carr.
// Last Modified : 2003 Sep 29 (Mon) 06:53:22 by Harold Carr.
//

package corba.connections;

import java.util.Collection;
import java.util.Iterator;

import com.sun.corba.se.spi.orb.ORB;
import com.sun.corba.se.spi.transport.CorbaAcceptor;
import com.sun.corba.se.spi.transport.CorbaTransportManager;
import com.sun.corba.se.spi.transport.CorbaConnectionCache;
import com.sun.corba.se.spi.presentation.rmi.StubAdapter ;

import corba.hcks.U;

import org.glassfish.gmbal.GmbalException ;
import org.glassfish.gmbal.AMXClient ;
import org.glassfish.gmbal.ManagedObjectManager ;

public class ConnectionStatistics
{
    private final ORB orb ;
    private final CorbaTransportManager ctm ;

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
        CorbaConnectionCache connectionCache ) {

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

        for (CorbaConnectionCache cache : ctm.getOutboundConnectionCaches() ) {
            handleConnectionCache( result, cache ) ;
        }

	return result.toString();
    }

    public String inbound(String msg, ORB orb) {
        ManagedObjectManager mom = orb.mom() ;

	StringBuffer result = new StringBuffer("");
	pac(result, "==================================================");
	pac(result, msg + " INBOUND:");

        for (CorbaConnectionCache cache : ctm.getInboundConnectionCaches() ) {
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
