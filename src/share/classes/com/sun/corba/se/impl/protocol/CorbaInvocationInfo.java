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

package com.sun.corba.se.impl.protocol;

import java.util.Iterator;

import com.sun.corba.se.spi.orb.ORB;

import com.sun.corba.se.spi.protocol.ClientInvocationInfo;
import com.sun.corba.se.spi.protocol.CorbaMessageMediator;

import com.sun.corba.se.spi.protocol.CorbaClientRequestDispatcher;

/**
 * @author Harold Carr
 */
public class CorbaInvocationInfo implements ClientInvocationInfo 
{
    // REVISIT - these needs to be an interface-based impl.

    private boolean isRetryInvocation;
    private int entryCount;
    private Iterator contactInfoListIterator;
    private CorbaClientRequestDispatcher clientRequestDispatcher;
    private CorbaMessageMediator messageMediator;
    
    public CorbaInvocationInfo() 
    {
	isRetryInvocation = false;
	entryCount = 0;
    }
    
    public Iterator getContactInfoListIterator() 
    {
	return contactInfoListIterator;
    }
    
    public void setContactInfoListIterator(Iterator contactInfoListIterator)
    {
	this.contactInfoListIterator = contactInfoListIterator;
    }
    
    public boolean isRetryInvocation() 
    {
	return isRetryInvocation;
    }
    
    public void setIsRetryInvocation(boolean isRetryInvocation) 
    {
	this.isRetryInvocation = isRetryInvocation;
    }
    
    public int getEntryCount() 
    {
	return entryCount;
    }
    
    public void incrementEntryCount() 
    {
	entryCount++;
    }
    
    public void decrementEntryCount() 
    {
	entryCount--;
    }
    
    public void setClientRequestDispatcher(CorbaClientRequestDispatcher clientRequestDispatcher)
    {
	this.clientRequestDispatcher = clientRequestDispatcher;
    }

    public CorbaClientRequestDispatcher getClientRequestDispatcher()
    {
	return clientRequestDispatcher;
    }

    public void setMessageMediator(CorbaMessageMediator messageMediator)
    {
        this.messageMediator = messageMediator;
    }

    public CorbaMessageMediator getMessageMediator()
    {
        return messageMediator;
    }
}

// End of file.
