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
// Created       : 2003 Apr 19 (Sat) 07:35:32 by Harold Carr.
// Last Modified : 2004 May 12 (Wed) 12:02:09 by Harold Carr.
//

package corba.pept;

import com.sun.corba.se.pept.encoding.OutputObject;
import com.sun.corba.se.pept.protocol.MessageMediator;
import com.sun.corba.se.pept.transport.Connection;

import com.sun.corba.se.spi.orb.ORB;
import com.sun.corba.se.spi.ior.IOR;
import com.sun.corba.se.spi.protocol.CorbaMessageMediator;
import com.sun.corba.se.spi.transport.CorbaContactInfo;
import com.sun.corba.se.spi.transport.CorbaContactInfoList;
import com.sun.corba.se.spi.transport.SocketInfo;

import com.sun.corba.se.impl.encoding.BufferManagerFactory;
import com.sun.corba.se.impl.encoding.CDROutputObject;
import com.sun.corba.se.impl.legacy.connection.SocketFactoryConnectionImpl;
import com.sun.corba.se.impl.transport.SocketOrChannelContactInfoImpl;

/**
 * @author Harold Carr
 */
public class XContactInfoImpl 
    extends
	SocketOrChannelContactInfoImpl
{
    public XContactInfoImpl() {} // REVISIT
    
    public XContactInfoImpl(
        ORB orb,
	CorbaContactInfoList contactInfoList,
	IOR effectiveTargetIOR,
	short addressingDisposition,
	SocketInfo cookie)
    {
	this.orb = orb;
	this.contactInfoList = contactInfoList;
	this.effectiveTargetIOR = effectiveTargetIOR;
        this.addressingDisposition = addressingDisposition;
    }

    ////////////////////////////////////////////////////
    //
    // pept.transport.Connection
    //

    public boolean shouldCacheConnection()
    {
	return false;
    }

    public Connection createConnection() 
    {
	Connection connection;

	if (effectiveTargetIOR.getProfile().isLocal()) {
	    connection = new XConnectionImpl(orb, this);
	} else {
	    connection = 
		new SocketFactoryConnectionImpl(
		    orb, this,
		    orb.getORBData().connectionSocketUseSelectThreadToWait(),
		    orb.getORBData().connectionSocketUseWorkerThreadForEvent());
	}	    
	return connection;
    }

    public OutputObject createOutputObject(MessageMediator messageMediator)
    {
	CorbaMessageMediator corbaMessageMediator = (CorbaMessageMediator)
	    messageMediator;
	OutputObject outputObject;
	if (((CorbaContactInfo)messageMediator.getContactInfo()).getEffectiveTargetIOR().getProfile().isLocal()) {
	    outputObject =
	    new CDROutputObject(orb, messageMediator, 
				corbaMessageMediator.getRequestHeader(),
				corbaMessageMediator.getStreamFormatVersion(),
				BufferManagerFactory.GROW);
	} else {
	    outputObject =
	    new CDROutputObject(orb, messageMediator, 
				corbaMessageMediator.getRequestHeader(),
				corbaMessageMediator.getStreamFormatVersion());
	}
	messageMediator.setOutputObject(outputObject);
	return outputObject;
    }

    ////////////////////////////////////////////////////
    //
    // java.lang.Object
    //

    public int hashCode() 
    {
	if (! isHashCodeCached) {
	    cachedHashCode = effectiveTargetIOR.hashCode();
	    isHashCodeCached = true;
	}
	return cachedHashCode;
    }
    
    public boolean equals(Object obj) 
    {
	//hashCode(); // to init endPointInfo
	if (obj == null) {
	    return false;
	} else if (!(obj instanceof XContactInfoImpl)) {
	    return false;
	} else {
	    return effectiveTargetIOR.equals(((XContactInfoImpl)obj).effectiveTargetIOR);
	}
    }

    public String toString()
    {
	return
	    "XContactInfoImpl[" 
	    + "]";
    }
}

// End of file.
