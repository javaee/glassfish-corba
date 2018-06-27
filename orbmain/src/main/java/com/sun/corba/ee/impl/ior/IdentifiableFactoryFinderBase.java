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

package com.sun.corba.ee.impl.ior ;

import org.omg.CORBA_2_3.portable.InputStream ;

import java.util.Map ;
import java.util.HashMap ;

import com.sun.corba.ee.spi.orb.ORB ;

import com.sun.corba.ee.spi.ior.Identifiable ;
import com.sun.corba.ee.spi.ior.IdentifiableFactory ;
import com.sun.corba.ee.spi.ior.IdentifiableFactoryFinder ;

import com.sun.corba.ee.spi.logging.IORSystemException ;

public abstract class IdentifiableFactoryFinderBase<E extends Identifiable> 
    implements IdentifiableFactoryFinder<E>
{
    protected static final IORSystemException wrapper =
        IORSystemException.self ;

    private ORB orb ;
    private Map<Integer,IdentifiableFactory<E>> map ;

    protected IdentifiableFactoryFinderBase( ORB orb )
    {
        map = new HashMap<Integer,IdentifiableFactory<E>>() ;
        this.orb = orb ;
    }

    protected IdentifiableFactory<E> getFactory(int id) 
    {
        return map.get( id ) ;
    }

    public abstract E handleMissingFactory( int id, 
        InputStream is ) ;
        
    public E create(int id, InputStream is) 
    {
        IdentifiableFactory<E> factory = getFactory( id ) ;

        if (factory != null) {
            return factory.create(orb, is);
        } else {
            return handleMissingFactory(id, is);
        }
    }
    
    public void registerFactory(IdentifiableFactory<E> factory) 
    {
        map.put( factory.getId(), factory ) ;
    }
}
