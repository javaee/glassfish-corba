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

package com.sun.corba.se.impl.legacy.connection;

import org.omg.CORBA.CompletionStatus;
import org.omg.CORBA.SystemException;

import com.sun.corba.se.spi.legacy.connection.GetEndPointInfoAgainException;
import com.sun.corba.se.spi.orb.ORB;
import com.sun.corba.se.spi.transport.CorbaContactInfo;
import com.sun.corba.se.spi.transport.CorbaContactInfoList;
import com.sun.corba.se.spi.transport.SocketInfo;

import com.sun.corba.se.spi.logging.ORBUtilSystemException;
import com.sun.corba.se.impl.transport.CorbaContactInfoListIteratorImpl;
import com.sun.corba.se.impl.transport.SharedCDRContactInfoImpl;
import com.sun.corba.se.spi.trace.IsLocal;

@IsLocal
public class SocketFactoryContactInfoListIteratorImpl
    extends CorbaContactInfoListIteratorImpl
{
    private SocketInfo socketInfoCookie;

    public SocketFactoryContactInfoListIteratorImpl(
        ORB orb,
	CorbaContactInfoList corbaContactInfoList)
    {
	super(orb, corbaContactInfoList, null, null, false);
    }

    ////////////////////////////////////////////////////
    //
    // java.util.Iterator
    //

    @Override
    @IsLocal
    public boolean hasNext()
    {
	return true;
    }

    @Override
    @IsLocal
    public CorbaContactInfo next()
    {
	if (contactInfoList.getEffectiveTargetIOR().getProfile().isLocal()){
	    return new SharedCDRContactInfoImpl(
		orb, contactInfoList,
		contactInfoList.getEffectiveTargetIOR(),
		orb.getORBData().getGIOPAddressDisposition());
	} else {
	    // REVISIT:
	    // on comm_failure maybe need to give IOR instead of located.
	    return new SocketFactoryContactInfoImpl(
	        orb, contactInfoList,
		contactInfoList.getEffectiveTargetIOR(),
		orb.getORBData().getGIOPAddressDisposition(),
		socketInfoCookie);
	}
    }

    @Override
    public boolean reportException(CorbaContactInfo contactInfo,
				   RuntimeException ex)
    {
	this.failureException = ex;
	if (ex instanceof org.omg.CORBA.COMM_FAILURE) {

	    SystemException se = (SystemException) ex;

	    if (se.minor == ORBUtilSystemException.CONNECTION_REBIND)
	    {
		return true;
	    } else {
	        if (ex.getCause() instanceof GetEndPointInfoAgainException) {
		    socketInfoCookie = 
		        ((GetEndPointInfoAgainException) ex.getCause())
		        .getEndPointInfo();
		    return true;
	        }

	        if (se.completed == CompletionStatus.COMPLETED_NO) {
		    if (contactInfoList.getEffectiveTargetIOR() !=
		        contactInfoList.getTargetIOR()) 
                    {
		        // retry from root ior
                        contactInfoList.setEffectiveTargetIOR(
                            contactInfoList.getTargetIOR());
		        return true;
		    }
	        }
	    }
	}
	return false;
    }
}

// End of file.
