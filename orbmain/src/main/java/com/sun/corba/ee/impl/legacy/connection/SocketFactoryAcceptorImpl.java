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

package com.sun.corba.ee.impl.legacy.connection;

import java.util.Iterator ;

import com.sun.corba.ee.spi.ior.IORTemplate;
import com.sun.corba.ee.spi.ior.iiop.IIOPProfileTemplate;
import com.sun.corba.ee.spi.orb.ORB;

import com.sun.corba.ee.impl.oa.poa.Policies;
import com.sun.corba.ee.impl.transport.AcceptorImpl;
import com.sun.corba.ee.spi.trace.Transport;

/**
 * @author Harold Carr
 */
@Transport
public class SocketFactoryAcceptorImpl
    extends
        AcceptorImpl
{
    public SocketFactoryAcceptorImpl(ORB orb, int port, 
                                     String name, String type)
    {
        super(orb, port, name, type);
    }

    @Transport
    @Override
    public boolean initialize()
    {
        if (initialized) {
            return false;
        }
        try {
            serverSocket = orb.getORBData()
                .getLegacySocketFactory().createServerSocket(type, port);
            internalInitialize();
        } catch (Throwable t) {
            throw wrapper.createListenerFailed( t, "localhost", port ) ;
        }
        initialized = true;
        return true;
    }

    ////////////////////////////////////////////////////
    //
    // Implementation.
    //

    @Override
    protected String toStringName()
    {
        return "SocketFactoryAcceptorImpl";
    }

    // Fix for 6331566.
    // This Acceptor must NOT contribute alternate IIOP address components
    // to the standard IIOPProfileTemplate,
    // because typically this is used for special addresses (such as SSL
    // ports) that must NOT be present in tag alternate components.
    // However, this method MUST add an IIOPProfileTemplate if one is
    // not already present.
    @Override
    public void addToIORTemplate( IORTemplate iorTemplate,
        Policies policies, String codebase ) 
    {
        Iterator iterator = iorTemplate.iteratorById(
            org.omg.IOP.TAG_INTERNET_IOP.value);

        if (!iterator.hasNext()) {
            // If this is the first call, create the IIOP profile template.
            IIOPProfileTemplate iiopProfile = makeIIOPProfileTemplate(
                policies, codebase ) ;
            iorTemplate.add(iiopProfile);
        }
    }
}

// End of file.
