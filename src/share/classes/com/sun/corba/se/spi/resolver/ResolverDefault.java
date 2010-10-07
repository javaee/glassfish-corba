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
package com.sun.corba.se.spi.resolver ;

import java.io.File ;

import com.sun.corba.se.impl.resolver.LocalResolverImpl ;
import com.sun.corba.se.impl.resolver.ORBInitRefResolverImpl ;
import com.sun.corba.se.impl.resolver.ORBDefaultInitRefResolverImpl ;
import com.sun.corba.se.impl.resolver.BootstrapResolverImpl ;
import com.sun.corba.se.impl.resolver.CompositeResolverImpl ;
import com.sun.corba.se.impl.resolver.INSURLOperationImpl ;
import com.sun.corba.se.impl.resolver.SplitLocalResolverImpl ;
import com.sun.corba.se.impl.resolver.FileResolverImpl ;

import com.sun.corba.se.spi.orb.ORB ;
import com.sun.corba.se.spi.orb.Operation ;
import com.sun.corba.se.spi.orbutil.generic.Pair ;

/** Utility class that provides factory methods for all of the 
 * standard resolvers that we provide.
 */
public class ResolverDefault {
    /** Return a local resolver that simply stores bindings in a map.
    */
    public static LocalResolver makeLocalResolver( ) 
    {
	return new LocalResolverImpl() ;
    }

    /** Return a resolver that relies on configured values of ORBInitRef for data.
    */
    public static Resolver makeORBInitRefResolver( Operation urlOperation,
	Pair<String,String>[] initRefs ) 
    {
	return new ORBInitRefResolverImpl( urlOperation, initRefs ) ;
    }

    public static Resolver makeORBDefaultInitRefResolver( Operation urlOperation,
	String defaultInitRef ) 
    {
	return new ORBDefaultInitRefResolverImpl( urlOperation,
	    defaultInitRef ) ;
    }

    /** Return a resolver that uses the proprietary bootstrap protocol 
    * to implement a resolver.  Obtains the necessary host and port 
    * information from the ORB.
    */
    public static Resolver makeBootstrapResolver( ORB orb, String host, int port ) 
    {
	return new BootstrapResolverImpl( orb, host, port ) ;
    }

    /** Return a resolver composed of the two given resolvers.  result.list() is the 
    * union of first.list() and second.list().  result.resolve( name ) returns
    * first.resolve( name ) if that is not null, otherwise returns the result of
    * second.resolve( name ).
    */
    public static Resolver makeCompositeResolver( Resolver first, Resolver second ) 
    {
	return new CompositeResolverImpl( first, second ) ;
    }

    public static Operation makeINSURLOperation( ORB orb, Resolver bootstrapResolver )
    {
	return new INSURLOperationImpl( 
	    (com.sun.corba.se.spi.orb.ORB)orb, bootstrapResolver ) ;
    }

    public static LocalResolver makeSplitLocalResolver( Resolver resolver,
	LocalResolver localResolver ) 
    {
	return new SplitLocalResolverImpl( resolver, localResolver ) ;
    }

    public static Resolver makeFileResolver( ORB orb, File file ) 
    {
	return new FileResolverImpl( orb, file ) ;
    }
}

