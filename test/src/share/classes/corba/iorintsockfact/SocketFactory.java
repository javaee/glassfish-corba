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
// Created       : 2002 Jul 19 (Fri) 14:56:53 by Harold Carr.
// Last Modified : 2004 May 12 (Wed) 11:52:02 by Harold Carr.
//

package corba.iorintsockfact;

import java.util.Iterator;

import org.omg.CORBA.ORB;

import com.sun.corba.se.spi.legacy.connection.ORBSocketFactory;
import com.sun.corba.se.spi.ior.IOR;
import com.sun.corba.se.spi.ior.iiop.IIOPProfileTemplate ;
import com.sun.corba.se.spi.ior.iiop.IIOPAddress ;
import com.sun.corba.se.spi.ior.iiop.AlternateIIOPAddressComponent;
import com.sun.corba.se.spi.transport.SocketInfo;

import com.sun.corba.se.impl.legacy.connection.DefaultSocketFactory;
import com.sun.corba.se.impl.legacy.connection.EndPointInfoImpl;

/**
 * @author Harold Carr
 */
public class SocketFactory
    extends
	DefaultSocketFactory
    implements
	ORBSocketFactory
{
    public SocketFactory()
    {
    }

    //
    // Client side.
    //

    @Override
    public SocketInfo getEndPointInfo(ORB orb, 
					IOR ior,
					SocketInfo socketInfo)
    {
	// NOTE: this only uses the first IIOP profile.
	// If there are multiple profiles a different API would be used
	// inside a loop.
	IIOPProfileTemplate iptemp =
	    (IIOPProfileTemplate)ior.getProfile().getTaggedProfileTemplate() ;

	Iterator iterator =
	    iptemp.iteratorById(org.omg.IOP.TAG_ALTERNATE_IIOP_ADDRESS.value);

	// NOTE: this only uses the first address.
	// If one were to try another address after failure you would
	// need a loop and a hashtable, hashed by IOR to keep a loop pointer.
	// Note: IOR hashing is not particularly efficient.  However, the
	// CorbaContactInfoList version of this solves the problem.
	while (iterator.hasNext()) {
	    Client.foundAlternateIIOPAddressComponent = true; // For test.

	    AlternateIIOPAddressComponent iiopAddressComponent =
		(AlternateIIOPAddressComponent) iterator.next();
	    return new EndPointInfoImpl(
                ORBSocketFactory.IIOP_CLEAR_TEXT,
		iiopAddressComponent.getAddress().getPort(),
		iiopAddressComponent.getAddress().getHost());
	}

	// No alternate addresses.  Just use profile address.
	Client.foundAlternateIIOPAddressComponent = false; // For test.

	IIOPProfileTemplate temp = 
	    (IIOPProfileTemplate)ior.getProfile().getTaggedProfileTemplate() ;
	IIOPAddress primary = temp.getPrimaryAddress() ;
	String host = primary.getHost().toLowerCase();
	int    port = primary.getPort();
	return new EndPointInfoImpl(ORBSocketFactory.IIOP_CLEAR_TEXT,
				    primary.getPort(),
				    primary.getHost().toLowerCase());
    }
}

// End of file.
