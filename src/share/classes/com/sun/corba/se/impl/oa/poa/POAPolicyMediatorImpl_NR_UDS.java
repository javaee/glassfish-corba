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

package com.sun.corba.se.impl.oa.poa ;

import org.omg.PortableServer.Servant ;
import org.omg.PortableServer.ServantManager ;
import org.omg.PortableServer.ForwardRequest ;
import org.omg.PortableServer.POAPackage.WrongPolicy ;
import org.omg.PortableServer.POAPackage.ObjectNotActive ;
import org.omg.PortableServer.POAPackage.ServantNotActive ;
import org.omg.PortableServer.POAPackage.ObjectAlreadyActive ;
import org.omg.PortableServer.POAPackage.ServantAlreadyActive ;
import org.omg.PortableServer.POAPackage.NoServant ;

/** Implementation of POAPolicyMediator that provides policy specific
 * operations on the POA.
 */
public class POAPolicyMediatorImpl_NR_UDS extends POAPolicyMediatorBase {
    private Servant defaultServant ;

    POAPolicyMediatorImpl_NR_UDS( Policies policies, POAImpl poa ) 
    {
	super( policies, poa ) ;

	// assert !policies.retainServants() && policies.useDefaultServant()
	if (policies.retainServants()) {
            throw wrapper.policyMediatorBadPolicyInFactory();
        }

	if (!policies.useDefaultServant()) {
            throw wrapper.policyMediatorBadPolicyInFactory();
        }

	defaultServant = null ;
    }
    
    protected java.lang.Object internalGetServant( byte[] id, 
	String operation ) throws ForwardRequest {

        poa.readLock() ;
        try {
            if (defaultServant == null) {
                throw wrapper.poaNoDefaultServant();
            }

            return defaultServant;
        } finally {
            poa.readUnlock() ;
        }
    }

    public void returnServant() 
    {
	// NO-OP
    }

    public void etherealizeAll() 
    {	
	// NO-OP
    }

    public void clearAOM() 
    {
	// NO-OP
    }

    public ServantManager getServantManager() throws WrongPolicy
    {
	throw new WrongPolicy();
    }

    public void setServantManager( ServantManager servantManager ) throws WrongPolicy
    {
	throw new WrongPolicy();
    }

    public Servant getDefaultServant() throws NoServant, WrongPolicy 
    {
	if (defaultServant == null) {
            throw new NoServant();
        }
	return defaultServant;
    }

    public void setDefaultServant( Servant servant ) throws WrongPolicy
    {
	this.defaultServant = servant;
	setDelegate(defaultServant, "DefaultServant".getBytes());
    }

    public final void activateObject(byte[] id, Servant servant) 
	throws WrongPolicy, ServantAlreadyActive, ObjectAlreadyActive
    {
	throw new WrongPolicy();
    }

    public Servant deactivateObject( byte[] id ) throws ObjectNotActive, WrongPolicy 
    {
	throw new WrongPolicy();
    }

    public byte[] servantToId( Servant servant ) throws ServantNotActive, WrongPolicy
    {	
	throw new WrongPolicy();
    }

    public Servant idToServant( byte[] id ) 
	throws WrongPolicy, ObjectNotActive
    {
	if (defaultServant != null) {
            return defaultServant;
        }

	throw new ObjectNotActive() ;
    }
}
