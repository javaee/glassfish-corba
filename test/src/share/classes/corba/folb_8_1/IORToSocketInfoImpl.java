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
// Created       : 2004 May 11 (Tue) 10:26:27 by Harold Carr.
// Last Modified : 2005 Jun 01 (Wed) 11:07:17 by Harold Carr.
//

package corba.folb_8_1;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.omg.CORBA.Any;

import com.sun.corba.se.spi.ior.IOR;
import com.sun.corba.se.spi.ior.TaggedComponent;
import com.sun.corba.se.spi.ior.iiop.IIOPProfileTemplate ;
import com.sun.corba.se.spi.ior.iiop.IIOPAddress ;
import com.sun.corba.se.spi.transport.IORToSocketInfo;
import com.sun.corba.se.spi.transport.SocketInfo;

import com.sun.corba.se.impl.orbutil.ORBUtility;

public class IORToSocketInfoImpl
    implements IORToSocketInfo
{
    public List getSocketInfo(IOR ior, List previous)
    {
	boolean debug = ior.getORB().transportDebugFlag;

	if (debug) {
	    dprint(".getSocketInfo->: " + previous);
	}

	if (! previous.isEmpty()) {
	    if (debug) {
		dprint(".getSocketInfo<-: returning previous: " + previous);
	    }
	    return previous;
	}

	SocketInfo socketInfo;
	List result = new ArrayList();

	//
	// Find and add address from profile.
	//

	IIOPProfileTemplate iiopProfileTemplate = (IIOPProfileTemplate)
	    ior.getProfile().getTaggedProfileTemplate() ;
	IIOPAddress primary = iiopProfileTemplate.getPrimaryAddress() ;
	String hostname = primary.getHost().toLowerCase();
	int    port     = primary.getPort();
	socketInfo = createSocketInfo("Primary", 
				      SocketInfo.IIOP_CLEAR_TEXT,
				      hostname, port);
	result.add(socketInfo);

	//
	// Find and add alternate iiop addresses.
	//

	Iterator iterator;
	/* DO NOT DO THIS FOR THE TEST
	iterator = iiopProfileTemplate.iteratorById(
            TAG_ALTERNATE_IIOP_ADDRESS.value);

	while (iterator.hasNext()) {
	    AlternateIIOPAddressComponent alternate =
		(AlternateIIOPAddressComponent) iterator.next();
	    hostname   = alternate.getAddress().getHost().toLowerCase();
	    port       = alternate.getAddress().getPort();
	    socketInfo = createSocketInfo("Alternate", 
					  SocketInfo.IIOP_CLEAR_TEXT,
					  hostname, port);
	    result.add(socketInfo);
	}
	*/

	//
	// Find and add custom tagged addresses.
	//

	iterator = iiopProfileTemplate.iteratorById(
            TAG_TAGGED_CUSTOM_SOCKET_INFO.value);

	while (iterator.hasNext()) {
	    Object o = iterator.next();
	    if (! Common.timing) {
		System.out.println(o);
	    }
	    byte[] data = ((TaggedComponent)o).getIOPComponent( ior.getORB() ).
		component_data ;
	    Any any = null;
	    try {
		any = Common.getCodec(ior.getORB()).decode(data);
	    } catch (Exception e) {
		System.out.println("Unexpected: " + e);
		System.exit(1);
	    }
	    TaggedCustomSocketInfo taggedSocketInfo = 
		TaggedCustomSocketInfoHelper.extract(any);
	    socketInfo = createSocketInfo("custom",
					  taggedSocketInfo.type,
					  taggedSocketInfo.host,
					  taggedSocketInfo.port);
	    result.add(socketInfo);
	}

	// This should be sorted in the order you want requests tried
	// if failover occurs.

	if (debug) {
	    dprint(".getSocketInfo<-: returning: " + result);
	}
	return result;
    }

    private SocketInfo createSocketInfo(String testMessage,
					final String type,
					final String hostname, final int port)
    {
	if (! Common.timing) {
	    System.out.println(testMessage + " " + type 
			       + " " + hostname + " " + port);
	}
	return new SocketInfo() {
	    public String getType() { return type; }
	    public String getHost() { return hostname; }
	    public int    getPort() { return port; }
            @Override
	    public String toString()
	    {
		return "SocketInfo[" + type + " " + hostname + " " + port +"]";
	    }
       };
    }

    private void dprint(String msg)
    {
	ORBUtility.dprint("IORToSocketInfoImpl", msg);
    }	
}

// End of file.
