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

import org.omg.PortableServer.Servant ;
import org.omg.PortableServer.ServantManager ;
import org.omg.PortableServer.ForwardRequest ;
import org.omg.PortableServer.POAPackage.WrongPolicy ;
import org.omg.PortableServer.POAPackage.ObjectNotActive ;
import org.omg.PortableServer.POAPackage.ServantNotActive ;
import org.omg.PortableServer.POAPackage.ObjectAlreadyActive ;
import org.omg.PortableServer.POAPackage.ServantAlreadyActive ;
import org.omg.PortableServer.POAPackage.NoServant ;

import com.sun.corba.se.impl.orbutil.ORBUtility ;
import com.sun.corba.se.impl.orbutil.ORBConstants ;

import com.sun.corba.se.impl.oa.NullServantImpl ;

/** Implementation of POARequesHandler that provides policy specific
 * operations on the POA in the case:
 * <ul>
 * <li>retain</li>
 * <li>useActiveObjectMapOnly</li>
 * </ul>
 */
public class POAPolicyMediatorImpl_R_AOM extends POAPolicyMediatorBase_R {
    POAPolicyMediatorImpl_R_AOM( Policies policies, POAImpl poa ) 
    {
	// assert policies.retainServants() 
	super( policies, poa ) ;

	// policies.useActiveObjectMapOnly()
	if (!policies.useActiveMapOnly())
	    throw poa.invocationWrapper().policyMediatorBadPolicyInFactory() ;
    }
    
    protected java.lang.Object internalGetServant( byte[] id, 
	String operation ) throws ForwardRequest
    { 
	java.lang.Object servant = internalIdToServant( id ) ;
	if (servant == null)
	    servant = new NullServantImpl( 
		poa.invocationWrapper().nullServant() ) ;
	return servant ;
    }

    public void etherealizeAll() 
    {	
	// NO-OP
    }

    public ServantManager getServantManager() throws WrongPolicy
    {
        throw new WrongPolicy();
    }

    public void setServantManager( ServantManager servantManager ) 
	throws WrongPolicy
    {
        throw new WrongPolicy();
    }

    public Servant getDefaultServant() throws NoServant, WrongPolicy 
    {
	throw new WrongPolicy();
    }

    public void setDefaultServant( Servant servant ) throws WrongPolicy
    {
	throw new WrongPolicy();
    }

    public Servant idToServant( byte[] id ) 
	throws WrongPolicy, ObjectNotActive
    {
	Servant s = internalIdToServant( id ) ; 

	if (s == null)
	    throw new ObjectNotActive() ;
	else
	    return s;                
    }
}
