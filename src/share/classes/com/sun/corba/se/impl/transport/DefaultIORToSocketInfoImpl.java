/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2004-2007 Sun Microsystems, Inc. All rights reserved.
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

package com.sun.corba.se.impl.transport;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.omg.IOP.TAG_ALTERNATE_IIOP_ADDRESS ;

import com.sun.corba.se.spi.ior.IOR;
import com.sun.corba.se.spi.ior.ObjectKeyTemplate;
import com.sun.corba.se.spi.ior.iiop.IIOPProfile ;
import com.sun.corba.se.spi.ior.iiop.IIOPProfileTemplate ;
import com.sun.corba.se.spi.ior.iiop.IIOPAddress ;
import com.sun.corba.se.spi.ior.iiop.AlternateIIOPAddressComponent;
import com.sun.corba.se.spi.transport.IORToSocketInfo;
import com.sun.corba.se.spi.transport.SocketInfo;

public class DefaultIORToSocketInfoImpl
    implements IORToSocketInfo
{
    public List<? extends SocketInfo> getSocketInfo(IOR ior, 
	List<? extends SocketInfo> previous) {

	// 6152681
	if (! previous.isEmpty()) {
	    return previous;
	}

	SocketInfo socketInfo;
	List<SocketInfo> result = new ArrayList<SocketInfo>();

	IIOPProfileTemplate iiopProfileTemplate = (IIOPProfileTemplate)
	    ior.getProfile().getTaggedProfileTemplate() ;
	IIOPAddress primary = iiopProfileTemplate.getPrimaryAddress() ;
	String hostname = primary.getHost().toLowerCase();
	int    port     = primary.getPort();
	// NOTE: we could check for 0 (i.e., CSIv2) but, for a 
	// non-CSIv2-configured client ORB talking to a CSIv2 configured
	// server ORB you might end up with an empty contact info list
	// which would then report a failure which would not be as
	// instructive as leaving a ContactInfo with a 0 port in the list.
	socketInfo = createSocketInfo(hostname, port);
	result.add(socketInfo);

	Iterator iterator = iiopProfileTemplate.iteratorById(
            TAG_ALTERNATE_IIOP_ADDRESS.value);

	while (iterator.hasNext()) {
	    AlternateIIOPAddressComponent alternate =
		(AlternateIIOPAddressComponent) iterator.next();
	    hostname = alternate.getAddress().getHost().toLowerCase();
	    port     = alternate.getAddress().getPort();
	    socketInfo= createSocketInfo(hostname, port);
	    result.add(socketInfo);
	}
	return result;
    }

    private SocketInfo createSocketInfo(final String hostname, final int port)
    {
	return new SocketInfo() {
	    public String getType() { return SocketInfo.IIOP_CLEAR_TEXT; }
	    public String getHost() { return hostname; }
	    public int    getPort() { return port; }};
    }
}

// End of file.
