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
package com.sun.corba.se.impl.ior.iiop;

import org.omg.CORBA_2_3.portable.OutputStream;

import com.sun.corba.se.spi.ior.TaggedComponentBase;
import com.sun.corba.se.spi.ior.iiop.RequestPartitioningComponent;

import com.sun.corba.se.spi.logging.ORBUtilSystemException ;
import com.sun.corba.se.spi.misc.ORBConstants;

public class RequestPartitioningComponentImpl extends TaggedComponentBase 
    implements RequestPartitioningComponent
{
    private static final ORBUtilSystemException wrapper =
        ORBUtilSystemException.self ;

    private int partitionToUse;

    @Override
    public boolean equals(Object obj)
    {
	if (!(obj instanceof RequestPartitioningComponentImpl)) {
            return false;
        }

	RequestPartitioningComponentImpl other = 
	    (RequestPartitioningComponentImpl)obj ;

	return partitionToUse == other.partitionToUse ;
    }

    @Override
    public int hashCode()
    {
	return partitionToUse;
    }

    @Override
    public String toString()
    {
	return "RequestPartitioningComponentImpl[partitionToUse=" + partitionToUse + "]" ;
    }

    public RequestPartitioningComponentImpl()
    {
        partitionToUse = 0;
    }

    public RequestPartitioningComponentImpl(int thePartitionToUse) {
	if (thePartitionToUse < ORBConstants.REQUEST_PARTITIONING_MIN_THREAD_POOL_ID ||
	    thePartitionToUse > ORBConstants.REQUEST_PARTITIONING_MAX_THREAD_POOL_ID) {
	    throw wrapper.invalidRequestPartitioningComponentValue(
                    thePartitionToUse,
                    ORBConstants.REQUEST_PARTITIONING_MIN_THREAD_POOL_ID,
                    ORBConstants.REQUEST_PARTITIONING_MAX_THREAD_POOL_ID);
	}
        partitionToUse = thePartitionToUse;
    }

    public int getRequestPartitioningId()
    {
	return partitionToUse;
    }

    public void writeContents(OutputStream os) 
    {
        os.write_ulong(partitionToUse);
    }
    
    public int getId() 
    {
	return ORBConstants.TAG_REQUEST_PARTITIONING_ID;
    }
}
