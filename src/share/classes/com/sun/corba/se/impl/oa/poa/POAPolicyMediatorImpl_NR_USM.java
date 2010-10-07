/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2002-2007 Sun Microsystems, Inc. All rights reserved.
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

package com.sun.corba.se.impl.oa.poa ;


import org.omg.PortableServer.POA ;
import org.omg.PortableServer.Servant ;
import org.omg.PortableServer.ServantManager ;
import org.omg.PortableServer.ServantLocator ;
import org.omg.PortableServer.ForwardRequest ;
import org.omg.PortableServer.POAPackage.NoServant ;
import org.omg.PortableServer.POAPackage.WrongPolicy ;
import org.omg.PortableServer.POAPackage.ObjectNotActive ;
import org.omg.PortableServer.POAPackage.ServantNotActive ;
import org.omg.PortableServer.POAPackage.ObjectAlreadyActive ;
import org.omg.PortableServer.POAPackage.ServantAlreadyActive ;
import org.omg.PortableServer.ServantLocatorPackage.CookieHolder ;


import com.sun.corba.se.spi.oa.OAInvocationInfo ;
import com.sun.corba.se.impl.oa.NullServantImpl ;

/** Implementation of POARequesHandler that provides policy specific
 * operations on the POA.
 */
public class POAPolicyMediatorImpl_NR_USM extends POAPolicyMediatorBase {
    // XXX How do we protect locator from multi-threaded access?
    private ServantLocator locator ;

    POAPolicyMediatorImpl_NR_USM( Policies policies, POAImpl poa ) 
    {
	super( policies, poa ) ;

	// assert !policies.retainServants() && policies.useServantManager()
	if (policies.retainServants()) {
            throw poa.invocationWrapper().policyMediatorBadPolicyInFactory();
        }

	if (!policies.useServantManager()) {
            throw poa.invocationWrapper().policyMediatorBadPolicyInFactory();
        }

	locator = null ;
    }
    
    protected java.lang.Object internalGetServant( byte[] id, 
	String operation ) throws ForwardRequest
    { 
	if (locator == null) {
            throw poa.invocationWrapper().poaNoServantManager();
        }
    
	CookieHolder cookieHolder = orb.peekInvocationInfo().getCookieHolder() ;

	java.lang.Object servant = locator.preinvoke(id, poa, operation,
            cookieHolder);

        if (servant == null) {
            servant = new NullServantImpl(poa.omgInvocationWrapper().nullServantReturned());
        } else {
            setDelegate((Servant) servant, id);
        }

	return servant;
    }

    public void returnServant() 
    {
	OAInvocationInfo info = orb.peekInvocationInfo();

        // 6878245: added info == null check.
	if (locator == null || info == null) {
            return;
        }

        locator.postinvoke(info.id(), (POA)(info.oa()),
            info.getOperation(), info.getCookieHolder().value,
            (Servant)(info.getServantContainer()) );
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
	return locator ;
    }

    public void setServantManager( ServantManager servantManager ) throws WrongPolicy
    {
	if (locator != null) {
            throw poa.invocationWrapper().servantManagerAlreadySet();
        }

	if (servantManager instanceof ServantLocator) {
            locator = (ServantLocator) servantManager;
        } else {
            throw poa.invocationWrapper().servantManagerBadType();
        }
    }

    public Servant getDefaultServant() throws NoServant, WrongPolicy 
    {
	throw new WrongPolicy();
    }

    public void setDefaultServant( Servant servant ) throws WrongPolicy
    {
	throw new WrongPolicy();
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
	throw new WrongPolicy();
    }
}
