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
package com.sun.corba.se.spi.protocol ;


import com.sun.corba.se.spi.protocol.LocalClientRequestDispatcherFactory ;
import com.sun.corba.se.spi.protocol.CorbaServerRequestDispatcher ;

import com.sun.corba.se.spi.orb.ORB ;

// Used only in the implementation: no client of this class ever needs these
import com.sun.corba.se.spi.ior.IOR ;

import com.sun.corba.se.impl.protocol.CorbaClientRequestDispatcherImpl ;
import com.sun.corba.se.impl.protocol.CorbaServerRequestDispatcherImpl ;
import com.sun.corba.se.impl.protocol.MinimalServantCacheLocalCRDImpl ;
import com.sun.corba.se.impl.protocol.InfoOnlyServantCacheLocalCRDImpl ;
import com.sun.corba.se.impl.protocol.FullServantCacheLocalCRDImpl ;
import com.sun.corba.se.impl.protocol.JIDLLocalCRDImpl ;
import com.sun.corba.se.impl.protocol.POALocalCRDImpl ;
import com.sun.corba.se.impl.protocol.INSServerRequestDispatcher ;
import com.sun.corba.se.impl.protocol.BootstrapServerRequestDispatcher ;

public final class RequestDispatcherDefault {
    private RequestDispatcherDefault() {}

    public static CorbaClientRequestDispatcher makeClientRequestDispatcher()
    {
	return new CorbaClientRequestDispatcherImpl() ;
    }

    public static CorbaServerRequestDispatcher makeServerRequestDispatcher( ORB orb ) 
    {
	return new CorbaServerRequestDispatcherImpl( (com.sun.corba.se.spi.orb.ORB)orb ) ;
    }

    public static CorbaServerRequestDispatcher makeBootstrapServerRequestDispatcher( ORB orb ) 
    {
	return new BootstrapServerRequestDispatcher( orb ) ;
    }

    public static CorbaServerRequestDispatcher makeINSServerRequestDispatcher( ORB orb ) 
    {
	return new INSServerRequestDispatcher( orb ) ;
    }

    public static LocalClientRequestDispatcherFactory makeMinimalServantCacheLocalClientRequestDispatcherFactory( final ORB orb ) 
    {
	return new LocalClientRequestDispatcherFactory() {
	    public LocalClientRequestDispatcher create( int id, IOR ior ) {
		return new MinimalServantCacheLocalCRDImpl( orb, id, ior ) ;
	    }
	} ;
    }

    public static LocalClientRequestDispatcherFactory makeInfoOnlyServantCacheLocalClientRequestDispatcherFactory( final ORB orb ) 
    {
	return new LocalClientRequestDispatcherFactory() {
	    public LocalClientRequestDispatcher create( int id, IOR ior ) {
		return new InfoOnlyServantCacheLocalCRDImpl( orb, id, ior ) ;
	    }
	} ;
    }

    public static LocalClientRequestDispatcherFactory makeFullServantCacheLocalClientRequestDispatcherFactory( final ORB orb ) 
    {
	return new LocalClientRequestDispatcherFactory() {
	    public LocalClientRequestDispatcher create( int id, IOR ior ) {
		return new FullServantCacheLocalCRDImpl( orb, id, ior ) ;
	    }
	} ;
    }

    public static LocalClientRequestDispatcherFactory makeJIDLLocalClientRequestDispatcherFactory( final ORB orb ) 
    {
	return new LocalClientRequestDispatcherFactory() {
	    public LocalClientRequestDispatcher create( int id, IOR ior ) {
		return new JIDLLocalCRDImpl( orb, id, ior ) ;
	    }
	} ;
    }

    public static LocalClientRequestDispatcherFactory makePOALocalClientRequestDispatcherFactory( final ORB orb ) 
    {
	return new LocalClientRequestDispatcherFactory() {
	    public LocalClientRequestDispatcher create( int id, IOR ior ) {
		return new POALocalCRDImpl( orb, id, ior ) ;
	    }
	} ;
    }
}
