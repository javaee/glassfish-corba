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

import org.omg.PortableServer.POAPackage.WrongPolicy ;
import org.omg.PortableServer.POAPackage.ServantNotActive ;
import org.omg.PortableServer.POAPackage.ServantAlreadyActive ;
import org.omg.PortableServer.POAPackage.ObjectNotActive ;
import org.omg.PortableServer.POAPackage.ObjectAlreadyActive ;

import com.sun.corba.se.spi.trace.Poa;
import org.glassfish.pfl.tf.spi.annotation.InfoMethod;

@Poa
public abstract class POAPolicyMediatorBase_R extends POAPolicyMediatorBase {
    protected ActiveObjectMap activeObjectMap ;
    
    POAPolicyMediatorBase_R( Policies policies, POAImpl poa ) 
    {
	super( policies, poa ) ;

	// assert policies.retainServants() && policies.useActiveObjectMapOnly()
	if (!policies.retainServants()) {
            throw wrapper.policyMediatorBadPolicyInFactory();
        }

        activeObjectMap = ActiveObjectMap.create(poa, !isUnique);
    }
    
    public void returnServant() 
    {
	// NO-OP
    }

    public void clearAOM() 
    {
	activeObjectMap.clear() ;
	activeObjectMap = null ;
    }

    protected Servant internalKeyToServant( ActiveObjectMap.Key key )
    {
	AOMEntry entry = activeObjectMap.get(key);
	if (entry == null) {
            return null;
        }

	return activeObjectMap.getServant( entry ) ;
    }

    protected Servant internalIdToServant( byte[] id )
    {
	ActiveObjectMap.Key key = new ActiveObjectMap.Key( id ) ;
	return internalKeyToServant( key ) ;
    }

    @Poa
    protected void activateServant( ActiveObjectMap.Key key, AOMEntry entry, Servant servant )
    {
	setDelegate(servant, key.id() );

	activeObjectMap.putServant( servant, entry ) ;

	POAManagerImpl pm = (POAManagerImpl)poa.the_POAManager() ;
	POAFactory factory = pm.getFactory() ;
	factory.registerPOAForServant(poa, servant);
    }

    @Poa
    public final void activateObject(byte[] id, Servant servant) 
	throws WrongPolicy, ServantAlreadyActive, ObjectAlreadyActive
    {
	if (isUnique && activeObjectMap.contains(servant)) {
            throw new ServantAlreadyActive();
        }
	ActiveObjectMap.Key key = new ActiveObjectMap.Key( id ) ;

	AOMEntry entry = activeObjectMap.get( key ) ;

	// Check for an ObjectAlreadyActive error
	entry.activateObject() ;

	activateServant( key, entry, servant ) ;
    }
    
    @Poa
    public Servant deactivateObject( byte[] id ) 
	throws ObjectNotActive, WrongPolicy 
    {
	ActiveObjectMap.Key key = new ActiveObjectMap.Key( id ) ;
	return deactivateObject( key ) ;
    }
    
    @Poa
    protected void deactivateHelper( ActiveObjectMap.Key key, AOMEntry entry, 
	Servant s ) throws ObjectNotActive, WrongPolicy
    {
	// Default does nothing, but the USE_SERVANT_MANAGER case
	// must handle etherealization

	activeObjectMap.remove(key);

	POAManagerImpl pm = (POAManagerImpl)poa.the_POAManager() ;
	POAFactory factory = pm.getFactory() ;
	factory.unregisterPOAForServant(poa, s);
    }

    @InfoMethod
    private void deactivatingObject( Servant s, POAImpl poa ) { }

    @Poa
    public Servant deactivateObject( ActiveObjectMap.Key key ) 
	throws ObjectNotActive, WrongPolicy {

	AOMEntry entry = activeObjectMap.get(key);
	if (entry == null) {
            throw new ObjectNotActive();
        }

	Servant s = activeObjectMap.getServant( entry ) ;
	if (s == null) {
            throw new ObjectNotActive();
        }

	deactivatingObject( s, poa ) ;

	deactivateHelper( key, entry, s ) ;

	return s ;
    }

    @Poa
    public byte[] servantToId( Servant servant ) throws ServantNotActive, WrongPolicy
    {	
	if (!isUnique && !isImplicit) {
            throw new WrongPolicy();
        }

	if (isUnique) {
	    ActiveObjectMap.Key key = activeObjectMap.getKey(servant);
	    if (key != null) {
                return key.id();
            }
	} 

	// assert !isUnique || (servant not in activateObjectMap)
	
	if (isImplicit) {
            try {
                byte[] id = newSystemId();
                activateObject(id, servant);
                return id;
            } catch (ObjectAlreadyActive oaa) {
                throw wrapper.servantToIdOaa(oaa);
            } catch (ServantAlreadyActive s) {
                throw wrapper.servantToIdSaa(s);
            } catch (WrongPolicy w) {
                throw wrapper.servantToIdWp(w);
            }
        }

	throw new ServantNotActive();
    }
}

