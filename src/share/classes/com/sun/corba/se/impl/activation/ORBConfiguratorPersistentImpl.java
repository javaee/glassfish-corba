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

package com.sun.corba.se.impl.activation ;

import java.util.Collection ;
import java.util.Iterator ;

import org.omg.CORBA.CompletionStatus ;

import com.sun.corba.se.spi.activation.Locator ;
import com.sun.corba.se.spi.activation.Activator ;
import com.sun.corba.se.spi.activation.LocatorHelper ;
import com.sun.corba.se.spi.activation.ActivatorHelper ;
import com.sun.corba.se.spi.activation.EndPointInfo ;

import com.sun.corba.se.spi.orb.ORBData ;
import com.sun.corba.se.spi.orb.ORB ;

import com.sun.corba.se.spi.transport.SocketInfo;

import com.sun.corba.se.impl.logging.ORBUtilSystemException ;

// XXX This should go away once we get rid of the port exchange for ORBD
import com.sun.corba.se.impl.orb.ORBConfiguratorImpl;
import com.sun.corba.se.spi.legacy.connection.LegacyServerSocketEndPointInfo;

// XXX These should move to SPI
import com.sun.corba.se.spi.orbutil.ORBConstants ;

public class ORBConfiguratorPersistentImpl extends ORBConfiguratorImpl {
    private ORBUtilSystemException wrapper ;

    @Override
    protected void persistentServerInitialization( ORB orb )
    {
	ORBData data = orb.getORBData() ;

	// determine the ORBD port so that persistent objrefs can be
	// created.
	if (data.getServerIsORBActivated()) {
	    try {
                Locator locator = LocatorHelper.narrow(
		    orb.resolve_initial_references(
			ORBConstants.SERVER_LOCATOR_NAME )) ;
                Activator activator = ActivatorHelper.narrow(
		    orb.resolve_initial_references(
			ORBConstants.SERVER_ACTIVATOR_NAME )) ;
		Collection serverEndpoints =
		    orb.getCorbaTransportManager().getAcceptors(null, null);
		EndPointInfo[] endpointList =
		    new EndPointInfo[serverEndpoints.size()];
		Iterator iterator = serverEndpoints.iterator();
		int i = 0 ;
		while (iterator.hasNext()) {
		    Object n = iterator.next();
		    if (! (n instanceof LegacyServerSocketEndPointInfo)) {
			continue;
		    }
		    LegacyServerSocketEndPointInfo ep =
			(LegacyServerSocketEndPointInfo) n;
		    // REVISIT - use exception instead of -1.
		    int port = locator.getEndpoint(ep.getType());
		    if (port == -1) {
			port = locator.getEndpoint(SocketInfo.IIOP_CLEAR_TEXT);
			if (port == -1) {
			    throw new Exception(
				"ORBD must support IIOP_CLEAR_TEXT");
			}
		    }

		    ep.setLocatorPort(port);

		    endpointList[i++] =
			new EndPointInfo(ep.getType(), ep.getPort());
		}

	        activator.registerEndpoints(
		    data.getPersistentServerId(), data.getORBId(),
			endpointList);
	    } catch (Exception ex) {
		throw wrapper.persistentServerInitError(
		    CompletionStatus.COMPLETED_MAYBE, ex ) ;
	    }
	}
    }
}
