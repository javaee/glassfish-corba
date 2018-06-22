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

package com.sun.corba.ee.spi.servicecontext;

import org.omg.CORBA_2_3.portable.InputStream ;

import com.sun.corba.ee.spi.orb.ORB ;
import com.sun.corba.ee.spi.orb.ORBVersion ;

import com.sun.corba.ee.spi.ior.IOR ;

import com.sun.corba.ee.spi.ior.iiop.GIOPVersion ;

import com.sun.corba.ee.spi.servicecontext.ServiceContexts ;
import com.sun.corba.ee.spi.servicecontext.ServiceContext ;
import com.sun.corba.ee.spi.servicecontext.ServiceContextFactoryRegistry ;
import com.sun.corba.ee.spi.servicecontext.CodeSetServiceContext ;
import com.sun.corba.ee.spi.servicecontext.ORBVersionServiceContext ;
import com.sun.corba.ee.spi.servicecontext.MaxStreamFormatVersionServiceContext ;
import com.sun.corba.ee.spi.servicecontext.UEInfoServiceContext ;
import com.sun.corba.ee.spi.servicecontext.UnknownServiceContext ;

import com.sun.corba.ee.impl.servicecontext.ServiceContextsImpl ;
import com.sun.corba.ee.impl.servicecontext.ServiceContextFactoryRegistryImpl ;
import com.sun.corba.ee.impl.servicecontext.CodeSetServiceContextImpl ;
import com.sun.corba.ee.impl.servicecontext.ORBVersionServiceContextImpl ;
import com.sun.corba.ee.impl.servicecontext.MaxStreamFormatVersionServiceContextImpl ;
import com.sun.corba.ee.impl.servicecontext.UEInfoServiceContextImpl ;
import com.sun.corba.ee.impl.servicecontext.UnknownServiceContextImpl ;
import com.sun.corba.ee.impl.servicecontext.SendingContextServiceContextImpl ;

import com.sun.corba.ee.impl.encoding.CodeSetComponentInfo ;

import com.sun.corba.ee.spi.orb.ORBVersionFactory;


public abstract class ServiceContextDefaults {

    private static ORBVersion orbVersion = ORBVersionFactory.getORBVersion();
    private static ORBVersionServiceContext orbVersionContext = 
                        new ORBVersionServiceContextImpl( orbVersion );

    private ServiceContextDefaults() {}

    public static ServiceContexts makeServiceContexts( ORB orb )
    {
        return new ServiceContextsImpl( orb ) ;
    }

    public static ServiceContexts makeServiceContexts( InputStream is ) 
    {
        return new ServiceContextsImpl( is ) ;
    }

    public static ServiceContextFactoryRegistry makeServiceContextFactoryRegistry( 
        ORB orb ) 
    {
        return new ServiceContextFactoryRegistryImpl( orb ) ;
    }

    public static CodeSetServiceContext makeCodeSetServiceContext( 
        CodeSetComponentInfo.CodeSetContext csc ) 
    {
        return new CodeSetServiceContextImpl( csc ) ;
    }

    public static ServiceContext.Factory makeCodeSetServiceContextFactory()
    {
        return new ServiceContext.Factory() {
            public int getId()
            {
                return CodeSetServiceContext.SERVICE_CONTEXT_ID ;
            }

            public ServiceContext create( InputStream s, GIOPVersion gv )
            {
                return new CodeSetServiceContextImpl( s, gv ) ;
            }
        } ;
    }

    public static ServiceContext.Factory 
        makeMaxStreamFormatVersionServiceContextFactory()
    {
        return new ServiceContext.Factory() {
            public int getId()
            {
                return MaxStreamFormatVersionServiceContext.SERVICE_CONTEXT_ID ;
            }

            public ServiceContext create( InputStream s, GIOPVersion gv )
            {
                return new MaxStreamFormatVersionServiceContextImpl( s, gv ) ;
            }
        } ;
    }

    public static MaxStreamFormatVersionServiceContext 
        getMaxStreamFormatVersionServiceContext()
    {
        return MaxStreamFormatVersionServiceContextImpl.singleton ;
    }

    public static MaxStreamFormatVersionServiceContext 
        makeMaxStreamFormatVersionServiceContext( byte version )
    {
        return new MaxStreamFormatVersionServiceContextImpl( version ) ;
    }

    public static ServiceContext.Factory makeORBVersionServiceContextFactory()
    {
        return new ServiceContext.Factory() {
            public int getId()
            {
                return ORBVersionServiceContext.SERVICE_CONTEXT_ID ;
            }

            public ServiceContext create( InputStream s, GIOPVersion gv )
            {
                return new ORBVersionServiceContextImpl( s, gv ) ;
            }
        } ;
    }

    public static ORBVersionServiceContext getORBVersionServiceContext()
    {
        return ORBVersionServiceContextImpl.singleton ;
    }

    public static ORBVersionServiceContext makeORBVersionServiceContext()
    {
        return orbVersionContext ;
    }

    public static ServiceContext.Factory makeSendingContextServiceContextFactory()
    {
        return new ServiceContext.Factory() {
            public int getId()
            {
                return SendingContextServiceContext.SERVICE_CONTEXT_ID ;
            }

            public ServiceContext create( InputStream s, GIOPVersion gv )
            {
                return new SendingContextServiceContextImpl( s, gv ) ;
            }
        } ;
    }

    public static SendingContextServiceContext 
        makeSendingContextServiceContext( IOR ior )
    {
        return new SendingContextServiceContextImpl( ior ) ;
    }

    public static ServiceContext.Factory makeUEInfoServiceContextFactory()
    {
        return new ServiceContext.Factory() {
            public int getId()
            {
                return UEInfoServiceContext.SERVICE_CONTEXT_ID ;
            }

            public ServiceContext create( InputStream s, GIOPVersion gv )
            {
                return new UEInfoServiceContextImpl( s, gv ) ;
            }
        } ;
    }

    public static UEInfoServiceContext 
        makeUEInfoServiceContext( Throwable thr )
    {
        return new UEInfoServiceContextImpl( thr ) ;
    }

    public static UnknownServiceContext 
        makeUnknownServiceContext( int id, byte[] data )
    {
        return new UnknownServiceContextImpl( id, data ) ;
    }

    public static UnknownServiceContext 
        makeUnknownServiceContext( int id, InputStream str )
    {
        return new UnknownServiceContextImpl( id, str ) ;
    }
}

