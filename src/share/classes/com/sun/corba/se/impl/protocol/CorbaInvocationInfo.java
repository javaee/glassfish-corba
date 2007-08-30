/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1996-2007 Sun Microsystems, Inc. All rights reserved.
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

package com.sun.corba.se.impl.protocol;

import java.util.Iterator;

import com.sun.corba.se.spi.orb.ORB;

import com.sun.corba.se.pept.protocol.ClientInvocationInfo;
import com.sun.corba.se.pept.protocol.MessageMediator;

import com.sun.corba.se.pept.protocol.ClientRequestDispatcher;

/**
 * @author Harold Carr
 */
public class CorbaInvocationInfo implements ClientInvocationInfo 
{
    // REVISIT - these needs to be an interface-based impl.

    private boolean isRetryInvocation;
    private int entryCount;
    private ORB orb;
    private Iterator contactInfoListIterator;
    private ClientRequestDispatcher clientRequestDispatcher;
    private MessageMediator messageMediator;
    
    private CorbaInvocationInfo() 
    {
    }

    public CorbaInvocationInfo(ORB orb) 
    {
	this.orb = orb;
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
    
    public void setClientRequestDispatcher(ClientRequestDispatcher clientRequestDispatcher)
    {
	this.clientRequestDispatcher = clientRequestDispatcher;
    }

    public ClientRequestDispatcher getClientRequestDispatcher()
    {
	return clientRequestDispatcher;
    }

    public void setMessageMediator(MessageMediator messageMediator) 
    {
        this.messageMediator = messageMediator;
    }

    public MessageMediator getMessageMediator()
    {
        return messageMediator;
    }
}

// End of file.
