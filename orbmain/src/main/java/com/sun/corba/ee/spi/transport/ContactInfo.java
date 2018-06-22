/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright (c) 1997-2018 Oracle and/or its affiliates. All rights reserved.
 *
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common Development
 * and Distribution License("CDDL") (collectively, the "License").  You
 * may not use this file except in compliance with the License.  You can
 * obtain a copy of the License at
 * https://oss.oracle.com/licenses/CDDL+GPL-1.1
 * or LICENSE.txt.  See the License for the specific
 * language governing permissions and limitations under the License.
 *
 * When distributing the software, include this License Header Notice in each
 * file and include the License file at LICENSE.txt.
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

package com.sun.corba.ee.spi.transport;

import com.sun.corba.ee.spi.protocol.ClientRequestDispatcher;
import com.sun.corba.ee.impl.encoding.CDRInputObject;
import com.sun.corba.ee.impl.encoding.CDROutputObject;
import com.sun.corba.ee.spi.ior.IOR ;
import com.sun.corba.ee.spi.ior.iiop.IIOPProfile;

import com.sun.corba.ee.spi.orb.ORB;
import com.sun.corba.ee.spi.protocol.MessageMediator;

/**
 * @author Harold Carr
 */
public abstract interface ContactInfo extends SocketInfo
{
    public ContactInfoList getContactInfoList() ;
    public IOR getTargetIOR();
    public IOR getEffectiveTargetIOR();
    public IIOPProfile getEffectiveProfile(); // REVISIT - type
    public void setAddressingDisposition(short addressingDisposition);
    public short getAddressingDisposition();
    public String getMonitoringName();

    public ORB getBroker();

    public ClientRequestDispatcher getClientRequestDispatcher();

    /**
     * Used to determine if a CorbaConnection
     * will be present in an invocation.
     *
     * For example, it may be
     * <code>false</code> in the case of shared-memory
     * <code>Input/OutputObjects</code>.
     *
     * @return <code>true</code> if a CorbaConnection
     * will be used for an invocation.
     */
    public boolean isConnectionBased();

    /**
     * Used to determine if the CorbaConnection
     * used for a request should be cached.
     *
     * If <code>true</code> then the ORB will attempt to reuse an existing
     * CorbaConnection. If
     * one is not found it will create a new one and cache it for future use.
     *
     *
     * @return <code>true</code> if a CorbaConnection
     * created by this <code>ContactInfo</code> should be cached.
     */
    public boolean shouldCacheConnection();

    public String getConnectionCacheType();

    public void setConnectionCache(OutboundConnectionCache connectionCache);

    public OutboundConnectionCache getConnectionCache();

    public Connection createConnection();

    public MessageMediator createMessageMediator(ORB broker,
        ContactInfo contactInfo, Connection connection,
        String methodName, boolean isOneWay);

    public CDROutputObject createOutputObject(MessageMediator messageMediator);

    /**
     * Used to lookup artifacts associated with this <code>ContactInfo</code>.
     *
     * @return the hash value.
     */
    public int hashCode();
}

// End of file.
