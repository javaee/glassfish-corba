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

package com.sun.corba.se.impl.orb ;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.security.PrivilegedExceptionAction ;
import java.security.AccessController ;

import com.sun.corba.se.spi.protocol.CorbaClientRequestDispatcher ;

import com.sun.corba.se.spi.orbutil.copyobject.ObjectCopierFactory ;
import com.sun.corba.se.spi.copyobject.CopyobjectDefaults ;
import com.sun.corba.se.spi.copyobject.CopierManager ;

import com.sun.corba.se.spi.ior.IdentifiableFactoryFinder ;
import com.sun.corba.se.spi.ior.IOR;
import com.sun.corba.se.spi.ior.IORFactories ;

import com.sun.corba.se.spi.ior.iiop.IIOPFactories ;

import com.sun.corba.se.spi.legacy.connection.ORBSocketFactory;

import com.sun.corba.se.spi.oa.OADefault ;
import com.sun.corba.se.spi.oa.ObjectAdapterFactory ;

import com.sun.corba.se.spi.orb.Operation ;
import com.sun.corba.se.spi.orb.OperationFactory ;
import com.sun.corba.se.spi.orb.ORBData ;
import com.sun.corba.se.spi.orb.DataCollector ;
import com.sun.corba.se.spi.orb.ORBConfigurator ;
import com.sun.corba.se.spi.orb.ParserImplBase ;
import com.sun.corba.se.spi.orb.PropertyParser ;
import com.sun.corba.se.spi.orb.ORB ;

import com.sun.corba.se.spi.orbutil.closure.Closure ;
import com.sun.corba.se.spi.orbutil.closure.ClosureFactory ;

import com.sun.corba.se.spi.protocol.RequestDispatcherRegistry ;
import com.sun.corba.se.spi.protocol.CorbaServerRequestDispatcher ;
import com.sun.corba.se.spi.protocol.RequestDispatcherDefault ;
import com.sun.corba.se.spi.protocol.LocalClientRequestDispatcherFactory ;

import com.sun.corba.se.spi.resolver.LocalResolver ;
import com.sun.corba.se.spi.resolver.Resolver ;
import com.sun.corba.se.spi.resolver.ResolverDefault ;

import com.sun.corba.se.spi.transport.CorbaContactInfoList;
import com.sun.corba.se.spi.transport.CorbaContactInfoListFactory;
import com.sun.corba.se.spi.transport.SocketInfo;
import com.sun.corba.se.spi.transport.TransportDefault ;

import com.sun.corba.se.spi.presentation.rmi.PresentationDefaults ;

import com.sun.corba.se.spi.servicecontext.ServiceContextDefaults ;
import com.sun.corba.se.spi.servicecontext.ServiceContextFactoryRegistry ;

import com.sun.corba.se.impl.logging.ORBUtilSystemException ;
import com.sun.corba.se.impl.transport.SocketOrChannelAcceptorImpl;

// XXX This should go away once we get rid of the port exchange for ORBD
import com.sun.corba.se.spi.legacy.connection.LegacyServerSocketEndPointInfo;
import com.sun.corba.se.impl.legacy.connection.SocketFactoryAcceptorImpl;
import com.sun.corba.se.impl.legacy.connection.SocketFactoryContactInfoListImpl;
import com.sun.corba.se.impl.legacy.connection.USLPort;

// XXX These should move to SPI
import com.sun.corba.se.spi.orbutil.ORBConstants ;

// XXX This needs an SPI
import com.sun.corba.se.impl.dynamicany.DynAnyFactoryImpl ;

import com.sun.corba.se.spi.transport.CorbaAcceptor;

public class ORBConfiguratorImpl implements ORBConfigurator {
    private ORBUtilSystemException wrapper ;

    protected void persistentServerInitialization(ORB theOrb) {
        // Does nothing, but can be overridden in subclass.
    }

    public static class ConfigParser extends ParserImplBase {
        private ORB orb ;

        public ConfigParser( ORB orb ) {
            this.orb = orb ;
        } ;

	public Class<?>[] userConfigurators = null ;

	public PropertyParser makeParser()
	{
	    PropertyParser parser = new PropertyParser() ;
	    Operation action = OperationFactory.compose( 
		OperationFactory.suffixAction(),
		OperationFactory.classAction( orb.classNameResolver() )
	    ) ;
	    parser.addPrefix( ORBConstants.USER_CONFIGURATOR_PREFIX, action, 
		"userConfigurators", Class.class ) ;
	    return parser ;
	}
    }

    public void configure( DataCollector collector, ORB orb ) 
    {
	ORB theOrb = orb ;
	wrapper = orb.getLogWrapperTable().get_ORB_LIFECYCLE_ORBUtil() ;

	initObjectCopiers( theOrb ) ;
	initIORFinders( theOrb ) ;

	theOrb.setClientDelegateFactory( 
            // REVISIT: this should be ProtocolDefault.
	    TransportDefault.makeClientDelegateFactory( theOrb )) ;

	initializeTransport(theOrb) ;

	initializeNaming( theOrb ) ;
	initServiceContextRegistry( theOrb ) ;
	initRequestDispatcherRegistry( theOrb ) ;
	registerInitialReferences( theOrb ) ;
        
        // Set up the PIHandler now.  The user configurator call is the
        // earliest point at which an invocation on this ORB can occur due to
        // external code extending the ORB through a configurator.
        // persistentServerInitialization also needs to make invocations to ORBD.
        // ORB invocations can also occur during the execution of
        // the ORBInitializers.  
        theOrb.createPIHandler() ;

	theOrb.setInvocationInterceptor( 
	    PresentationDefaults.getNullInvocationInterceptor() ) ;

	persistentServerInitialization( theOrb ) ;

	runUserConfigurators( collector, theOrb ) ;
    }

    private void runUserConfigurators( DataCollector collector, ORB orb ) 
    {
	// Run any pluggable configurators.  This is a lot like 
	// ORBInitializers, only it uses the internal ORB and has
	// access to all data for parsing.  
	ConfigParser parser = new ConfigParser( orb )  ;
	parser.init( collector ) ;
	if (parser.userConfigurators != null) {
	    for (int ctr=0; ctr<parser.userConfigurators.length; ctr++) {
		Class cls = parser.userConfigurators[ctr] ;
		try {
		    ORBConfigurator config = (ORBConfigurator)(cls.newInstance()) ;
		    config.configure( collector, orb ) ;
		} catch (Exception exc) {
		    wrapper.userConfiguratorException( exc ) ;
		}
	    }
	}
    }


    /**
     * This is made somewhat complex because we are currently supporting
     * the ContactInfoList/Acceptor *AND* the legacy SocketFactory 
     * transport architecture.
     */
    private void initializeTransport(final ORB orb)
    {
	ORBData od = orb.getORBData();

	CorbaContactInfoListFactory contactInfoListFactory =
	    od.getCorbaContactInfoListFactory();
	CorbaAcceptor[] acceptors = od.getAcceptors();

	// BEGIN Legacy
	ORBSocketFactory legacySocketFactory = od.getLegacySocketFactory();
	USLPort[] uslPorts = od.getUserSpecifiedListenPorts() ;
	setLegacySocketFactoryORB(orb, legacySocketFactory);
	// END Legacy

	// Check for incorrect configuration.
	if (legacySocketFactory != null && contactInfoListFactory != null) {
	    throw wrapper.socketFactoryAndContactInfoListAtSameTime();
	}

	if (acceptors.length != 0 && legacySocketFactory != null) {
	    throw wrapper.acceptorsAndLegacySocketFactoryAtSameTime();
	}

	// Client and Server side setup.
	od.getSocketFactory().setORB(orb);

	// Set up client side.
	if (legacySocketFactory != null) {
	    // BEGIN Legacy
	    // Since the user specified a legacy socket factory we need to
	    // use a ContactInfoList that will use the legacy socket factory.
	    contactInfoListFactory =
		new CorbaContactInfoListFactory() {
			public void setORB(ORB orb) { }
			public CorbaContactInfoList create( IOR ior ) {
			    return new SocketFactoryContactInfoListImpl( 
                                orb, ior);
			}
		    };
	    // END Legacy
	} else if (contactInfoListFactory != null) {
	    // The user specified an explicit ContactInfoListFactory.
	    contactInfoListFactory.setORB(orb);
	} else {
	    // Use the default.
	    contactInfoListFactory =
	        TransportDefault.makeCorbaContactInfoListFactory(orb);
	}
	orb.setCorbaContactInfoListFactory(contactInfoListFactory);

	//
	// Set up server side.
	//

        if (!od.noDefaultAcceptors()) {
            //
            // Maybe allocate the Legacy default listener.
            //
            // If old legacy properties set, or there are no explicit
            // acceptors then register a default listener.  Type of
            // default listener depends on presence of legacy socket factory.
            //
            // Note: this must happen *BEFORE* registering explicit acceptors.
            //

            // BEGIN Legacy
            int port = -1;
            if (od.getORBServerPort() != 0) {
                port = od.getORBServerPort();
            } else if (od.getPersistentPortInitialized()) {
                port = od.getPersistentServerPort();
            } else if ((acceptors.length == 0)) {
                port = 0;
            }
            if (port != -1) {
                createAndRegisterAcceptor(orb, legacySocketFactory, port,
                            LegacyServerSocketEndPointInfo.DEFAULT_ENDPOINT,
                            SocketInfo.IIOP_CLEAR_TEXT);
            }
            // END Legacy

            for (int i = 0; i < acceptors.length; i++) {
                orb.getCorbaTransportManager().registerAcceptor(acceptors[i]);
            }

            // BEGIN Legacy
            // Allocate user listeners.
            USLPort[] ports = od.getUserSpecifiedListenPorts() ;
            if (ports != null) {
                for (int i = 0; i < ports.length; i++) {
                    createAndRegisterAcceptor(
                        orb, legacySocketFactory, ports[i].getPort(),
                        LegacyServerSocketEndPointInfo.NO_NAME,
                        ports[i].getType());
                }
            }
            // END Legacy
        }
    }

    /*
     * Legacy: name.
     */
    // REVISIT: see ORBD. make factory in TransportDefault.
    private void createAndRegisterAcceptor(ORB orb,
					   ORBSocketFactory legacySocketFactory,
					   int port, String name, String type)
    {
	CorbaAcceptor acceptor;
	if (legacySocketFactory == null) {
	    acceptor =
		new SocketOrChannelAcceptorImpl(orb, port, name, type);
	} else {
	    acceptor =
		new SocketFactoryAcceptorImpl(orb, port, name, type);
	}
	orb.getCorbaTransportManager().registerAcceptor(acceptor);
    }

    private void setLegacySocketFactoryORB(
        final ORB orb, final ORBSocketFactory legacySocketFactory)
    {
	if (legacySocketFactory == null) {
	    return;
	}

	// Note: the createServerSocket and createSocket methods on the
	// DefaultSocketFactory need to get data from the ORB but
	// we cannot change the interface.  So set the ORB (if it's ours)
	// by reflection.

	try {
    	    AccessController.doPrivileged(
                new PrivilegedExceptionAction<Object>() {
	            public Object run()
		        throws InstantiationException, IllegalAccessException
		    {
			try {
			    Method method =
				legacySocketFactory.getClass().getMethod(
                                  "setORB", ORB.class );
			    method.invoke(legacySocketFactory, orb);
			} catch (NoSuchMethodException e) {
			    // NOTE: If there is no method then it
			    // is not ours - so ignore it.
			    ;
			} catch (IllegalAccessException e) {
			    throw new RuntimeException(e);
			} catch (InvocationTargetException e) {
			    throw new RuntimeException(e);
			}
			return null;
		    }
		}
	    );
	} catch (Throwable t) {
	    throw wrapper.unableToSetSocketFactoryOrb(t);
	}
    }

    private void initializeNaming( ORB orb )
    { 
	LocalResolver localResolver = ResolverDefault.makeLocalResolver() ;
	orb.setLocalResolver( localResolver ) ;

	Resolver bootResolver = ResolverDefault.makeBootstrapResolver( orb,
	    orb.getORBData().getORBInitialHost(),
	    orb.getORBData().getORBInitialPort() ) ;

	Operation urlOperation = ResolverDefault.makeINSURLOperation( orb,
	    bootResolver ) ;
	orb.setURLOperation( urlOperation ) ;

	Resolver irResolver = ResolverDefault.makeORBInitRefResolver( urlOperation,
	    orb.getORBData().getORBInitialReferences() ) ;

	Resolver dirResolver = ResolverDefault.makeORBDefaultInitRefResolver( 
	    urlOperation, orb.getORBData().getORBDefaultInitialReference() ) ;

	Resolver resolver = 
	    ResolverDefault.makeCompositeResolver( localResolver,
		ResolverDefault.makeCompositeResolver( irResolver,
		    ResolverDefault.makeCompositeResolver( dirResolver, 
			bootResolver ) ) ) ;
	orb.setResolver( resolver ) ;
    }

    private void initServiceContextRegistry( ORB orb ) 
    {
	ServiceContextFactoryRegistry scr = orb.getServiceContextFactoryRegistry() ;

	scr.register( 
	    ServiceContextDefaults.makeUEInfoServiceContextFactory() ) ;
	scr.register( 
	    ServiceContextDefaults.makeCodeSetServiceContextFactory() ) ;
	scr.register( 
	    ServiceContextDefaults.makeSendingContextServiceContextFactory() ) ;
	scr.register( 
	    ServiceContextDefaults.makeORBVersionServiceContextFactory() ) ;
	scr.register( 
	    ServiceContextDefaults.makeMaxStreamFormatVersionServiceContextFactory() ) ;
    }

    private void registerInitialReferences( final ORB orb ) 
    {
	// Register the Dynamic Any factory
        Closure closure = new Closure() {
            public java.lang.Object evaluate() {
                return new DynAnyFactoryImpl( orb ) ;
            }
        } ;

        Closure future = ClosureFactory.makeFuture( closure ) ;
        orb.getLocalResolver().register( ORBConstants.DYN_ANY_FACTORY_NAME, 
	    future ) ;
    }

    private static final int ORB_STREAM = 0 ;

    private void initObjectCopiers( ORB orb )
    {
	// No optimization or policy selection here.
	ObjectCopierFactory orbStream = 
	    CopyobjectDefaults.makeORBStreamObjectCopierFactory( orb ) ;

	CopierManager cm = orb.getCopierManager() ;
	cm.setDefaultId( ORB_STREAM ) ;

	cm.registerObjectCopierFactory( orbStream, ORB_STREAM ) ;
    }

    private void initIORFinders( ORB orb ) 
    {
	IdentifiableFactoryFinder profFinder = 
	    orb.getTaggedProfileFactoryFinder() ;
	profFinder.registerFactory( IIOPFactories.makeIIOPProfileFactory() ) ;

	IdentifiableFactoryFinder profTempFinder = 
	    orb.getTaggedProfileTemplateFactoryFinder() ;
	profTempFinder.registerFactory( 
	    IIOPFactories.makeIIOPProfileTemplateFactory() ) ;

	IdentifiableFactoryFinder compFinder = 
	    orb.getTaggedComponentFactoryFinder() ;
	compFinder.registerFactory( 
	    IIOPFactories.makeCodeSetsComponentFactory() ) ;
	compFinder.registerFactory( 
	    IIOPFactories.makeJavaCodebaseComponentFactory() ) ;
	compFinder.registerFactory( 
	    IIOPFactories.makeORBTypeComponentFactory() ) ;
	compFinder.registerFactory( 
	    IIOPFactories.makeMaxStreamFormatVersionComponentFactory() ) ;
	compFinder.registerFactory( 
	    IIOPFactories.makeAlternateIIOPAddressComponentFactory() ) ;
	compFinder.registerFactory( 
	    IIOPFactories.makeRequestPartitioningComponentFactory() ) ;
	compFinder.registerFactory(
	    IIOPFactories.makeJavaSerializationComponentFactory());
	compFinder.registerFactory(
	    IIOPFactories.makeLoadBalancingComponentFactory());
	compFinder.registerFactory(
	    IIOPFactories.makeClusterInstanceInfoComponentFactory());

	// Register the ValueFactory instances for ORT
	IORFactories.registerValueFactories( orb ) ;

	// Register an ObjectKeyFactory
	orb.setObjectKeyFactory( IORFactories.makeObjectKeyFactory(orb) ) ;
    }

    private void initRequestDispatcherRegistry( ORB orb ) 
    {
	RequestDispatcherRegistry scr = orb.getRequestDispatcherRegistry() ;

	// register client subcontracts
	CorbaClientRequestDispatcher csub =
	    RequestDispatcherDefault.makeClientRequestDispatcher() ;
	scr.registerClientRequestDispatcher( csub, 
	    ORBConstants.TOA_SCID ) ;
	scr.registerClientRequestDispatcher( csub, 
	    ORBConstants.TRANSIENT_SCID ) ;
	scr.registerClientRequestDispatcher( csub, 
	    ORBConstants.PERSISTENT_SCID ) ;
	scr.registerClientRequestDispatcher( csub, 
	    ORBConstants.SC_TRANSIENT_SCID ) ;
	scr.registerClientRequestDispatcher( csub, 
	    ORBConstants.SC_PERSISTENT_SCID ) ;
	scr.registerClientRequestDispatcher( csub,  
	    ORBConstants.IISC_TRANSIENT_SCID ) ;
	scr.registerClientRequestDispatcher( csub, 
	    ORBConstants.IISC_PERSISTENT_SCID ) ;
	scr.registerClientRequestDispatcher( csub, 
	    ORBConstants.MINSC_TRANSIENT_SCID ) ;
	scr.registerClientRequestDispatcher( csub, 
	    ORBConstants.MINSC_PERSISTENT_SCID ) ;
	
	// register server delegates
	CorbaServerRequestDispatcher sd = 
	    RequestDispatcherDefault.makeServerRequestDispatcher( orb );
	scr.registerServerRequestDispatcher( sd, 
	    ORBConstants.TOA_SCID ) ;
	scr.registerServerRequestDispatcher( sd, 
	    ORBConstants.TRANSIENT_SCID ) ;
	scr.registerServerRequestDispatcher( sd, 
	    ORBConstants.PERSISTENT_SCID ) ;
	scr.registerServerRequestDispatcher( sd, 
	    ORBConstants.SC_TRANSIENT_SCID ) ;
	scr.registerServerRequestDispatcher( sd, 
	    ORBConstants.SC_PERSISTENT_SCID ) ;
	scr.registerServerRequestDispatcher( sd, 
	    ORBConstants.IISC_TRANSIENT_SCID ) ;
	scr.registerServerRequestDispatcher( sd, 
	    ORBConstants.IISC_PERSISTENT_SCID ) ;
	scr.registerServerRequestDispatcher( sd, 
	    ORBConstants.MINSC_TRANSIENT_SCID ) ;
	scr.registerServerRequestDispatcher( sd, 
	    ORBConstants.MINSC_PERSISTENT_SCID ) ;
	
	orb.setINSDelegate( 
	    RequestDispatcherDefault.makeINSServerRequestDispatcher( orb ) ) ;
	    
	// register local client subcontracts
	LocalClientRequestDispatcherFactory lcsf = 
	    RequestDispatcherDefault.makeJIDLLocalClientRequestDispatcherFactory( 
		orb ) ;
	scr.registerLocalClientRequestDispatcherFactory( lcsf, 
	    ORBConstants.TOA_SCID ) ;

	lcsf = 
	    RequestDispatcherDefault.makePOALocalClientRequestDispatcherFactory( 
		orb ) ;
	scr.registerLocalClientRequestDispatcherFactory( lcsf, 
	    ORBConstants.TRANSIENT_SCID ) ;
	scr.registerLocalClientRequestDispatcherFactory( lcsf, 
	    ORBConstants.PERSISTENT_SCID ) ;

	lcsf = RequestDispatcherDefault.
	    makeFullServantCacheLocalClientRequestDispatcherFactory( orb ) ;
	scr.registerLocalClientRequestDispatcherFactory( lcsf, 
	    ORBConstants.SC_TRANSIENT_SCID ) ;
	scr.registerLocalClientRequestDispatcherFactory( lcsf, 
	    ORBConstants.SC_PERSISTENT_SCID ) ;

	lcsf = RequestDispatcherDefault.
	    makeInfoOnlyServantCacheLocalClientRequestDispatcherFactory( orb ) ;
	scr.registerLocalClientRequestDispatcherFactory( lcsf, 
	    ORBConstants.IISC_TRANSIENT_SCID ) ;
	scr.registerLocalClientRequestDispatcherFactory( lcsf, 
	    ORBConstants.IISC_PERSISTENT_SCID ) ;

	lcsf = RequestDispatcherDefault.
	    makeMinimalServantCacheLocalClientRequestDispatcherFactory( orb ) ;
	scr.registerLocalClientRequestDispatcherFactory( lcsf, 
	    ORBConstants.MINSC_TRANSIENT_SCID ) ;
	scr.registerLocalClientRequestDispatcherFactory( lcsf, 
	    ORBConstants.MINSC_PERSISTENT_SCID ) ;

	/* Register the server delegate that implements the ancient bootstrap
	 * naming protocol.  This takes an object key of either "INIT" or 
	 * "TINI" to allow for big or little endian implementations.
	 */
	CorbaServerRequestDispatcher bootsd = 
	    RequestDispatcherDefault.makeBootstrapServerRequestDispatcher( 
		orb ) ;
	scr.registerServerRequestDispatcher( bootsd, "INIT" ) ;
	scr.registerServerRequestDispatcher( bootsd, "TINI" ) ;

	// Register object adapter factories
	ObjectAdapterFactory oaf = OADefault.makeTOAFactory( orb ) ;
	scr.registerObjectAdapterFactory( oaf, ORBConstants.TOA_SCID ) ;

	oaf = OADefault.makePOAFactory( orb ) ;
	scr.registerObjectAdapterFactory( oaf, ORBConstants.TRANSIENT_SCID ) ;
	scr.registerObjectAdapterFactory( oaf, ORBConstants.PERSISTENT_SCID ) ;
	scr.registerObjectAdapterFactory( oaf, ORBConstants.SC_TRANSIENT_SCID ) ;
	scr.registerObjectAdapterFactory( oaf, ORBConstants.SC_PERSISTENT_SCID ) ;
	scr.registerObjectAdapterFactory( oaf, ORBConstants.IISC_TRANSIENT_SCID ) ;
	scr.registerObjectAdapterFactory( oaf, ORBConstants.IISC_PERSISTENT_SCID ) ;
	scr.registerObjectAdapterFactory( oaf, ORBConstants.MINSC_TRANSIENT_SCID ) ;
	scr.registerObjectAdapterFactory( oaf, ORBConstants.MINSC_PERSISTENT_SCID ) ;
    } 
}

// End of file.
