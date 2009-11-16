/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2004-2007 Sun Microsystems, Inc. All rights reserved.
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

package com.sun.corba.se.impl.orb ;

import java.applet.Applet;

import java.io.IOException ;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field ;
import java.lang.reflect.Modifier ;

import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantReadWriteLock ;

import java.util.List ;
import java.util.Set ;
import java.util.HashSet ;
import java.util.ArrayList ;
import java.util.Iterator ;
import java.util.Properties ;
import java.util.Map ;
import java.util.HashMap ;
import java.util.WeakHashMap ;

import java.net.InetAddress ;

import javax.rmi.CORBA.ValueHandler;

import org.omg.CORBA.NVList;
import org.omg.CORBA.TCKind;
import org.omg.CORBA.NamedValue;
import org.omg.CORBA.Request;
import org.omg.CORBA.CompletionStatus;
import org.omg.CORBA.TypeCode;
import org.omg.CORBA.Any;
import org.omg.CORBA.StructMember;
import org.omg.CORBA.UnionMember;
import org.omg.CORBA.ValueMember;
import org.omg.CORBA.BAD_PARAM;
import org.omg.CORBA.BAD_OPERATION;

import org.omg.CORBA.portable.ValueFactory;

import org.omg.CORBA.ORBPackage.InvalidName;

import com.sun.org.omg.SendingContext.CodeBase;

import com.sun.corba.se.spi.protocol.ClientInvocationInfo ;

import com.sun.corba.se.spi.ior.IOR;
import com.sun.corba.se.spi.ior.TaggedProfile;
import com.sun.corba.se.spi.ior.TaggedProfileTemplate;
import com.sun.corba.se.spi.ior.IdentifiableFactoryFinder ;
import com.sun.corba.se.spi.ior.TaggedComponentFactoryFinder;
import com.sun.corba.se.spi.ior.IORFactories ;
import com.sun.corba.se.spi.ior.ObjectKey ;
import com.sun.corba.se.spi.ior.ObjectKeyFactory ;
import com.sun.corba.se.spi.oa.OAInvocationInfo;
import com.sun.corba.se.spi.oa.ObjectAdapterFactory;
import com.sun.corba.se.spi.orb.DataCollector;
import com.sun.corba.se.spi.orb.Operation;
import com.sun.corba.se.spi.orb.ORBData;
import com.sun.corba.se.spi.orb.ORBConfigurator;
import com.sun.corba.se.spi.orb.ParserImplBase;
import com.sun.corba.se.spi.orb.PropertyParser;
import com.sun.corba.se.spi.orb.OperationFactory;
import com.sun.corba.se.spi.orb.ORBVersion;
import com.sun.corba.se.spi.orb.ORBVersionFactory;
import com.sun.corba.se.spi.orb.ObjectKeyCacheEntry;
import com.sun.corba.se.spi.protocol.ClientDelegateFactory;
import com.sun.corba.se.spi.protocol.RequestDispatcherRegistry;
import com.sun.corba.se.spi.protocol.CorbaServerRequestDispatcher;
import com.sun.corba.se.spi.protocol.PIHandler;
import com.sun.corba.se.spi.resolver.Resolver;
import com.sun.corba.se.spi.resolver.LocalResolver;
import com.sun.corba.se.spi.transport.CorbaContactInfoListFactory;
import com.sun.corba.se.spi.transport.CorbaTransportManager;
import com.sun.corba.se.spi.legacy.connection.LegacyServerSocketManager;
import com.sun.corba.se.spi.copyobject.CopierManager ;
import com.sun.corba.se.spi.presentation.rmi.InvocationInterceptor ;
import com.sun.corba.se.spi.presentation.rmi.StubAdapter ;
import com.sun.corba.se.spi.servicecontext.ServiceContextFactoryRegistry;
import com.sun.corba.se.spi.servicecontext.ServiceContextDefaults;
import com.sun.corba.se.spi.servicecontext.ServiceContextsCache;
import com.sun.corba.se.spi.orbutil.threadpool.ThreadPoolManager;

import com.sun.corba.se.spi.orbutil.closure.ClosureFactory;

import com.sun.corba.se.spi.orbutil.misc.ObjectUtility;
import com.sun.corba.se.spi.orbutil.misc.StackImpl;
import com.sun.corba.se.spi.orbutil.newtimer.TimerManager ;
import com.sun.corba.se.impl.orbutil.newtimer.generated.TimingPoints ;



import com.sun.corba.se.impl.corba.TypeCodeImpl;
import com.sun.corba.se.impl.corba.NVListImpl;
import com.sun.corba.se.impl.corba.ExceptionListImpl;
import com.sun.corba.se.impl.corba.ContextListImpl;
import com.sun.corba.se.impl.corba.NamedValueImpl;
import com.sun.corba.se.impl.corba.EnvironmentImpl;
import com.sun.corba.se.impl.corba.AsynchInvoke;
import com.sun.corba.se.impl.corba.AnyImpl;
import com.sun.corba.se.impl.encoding.EncapsOutputStream;
import com.sun.corba.se.impl.encoding.CachedCodeBase;
import com.sun.corba.se.impl.interceptors.PIHandlerImpl;
import com.sun.corba.se.impl.interceptors.PINoOpHandlerImpl;
import com.sun.corba.se.impl.ior.TaggedComponentFactoryFinderImpl;
import com.sun.corba.se.impl.ior.TaggedProfileFactoryFinderImpl;
import com.sun.corba.se.impl.ior.TaggedProfileTemplateFactoryFinderImpl;
import com.sun.corba.se.impl.oa.toa.TOAFactory;
import com.sun.corba.se.impl.oa.poa.BadServerIdHandler;
import com.sun.corba.se.impl.oa.poa.POAFactory;
import com.sun.corba.se.spi.orbutil.ORBConstants;
import com.sun.corba.se.impl.orbutil.ORBUtility;
import com.sun.corba.se.impl.orbutil.threadpool.ThreadPoolManagerImpl;
import com.sun.corba.se.impl.protocol.RequestDispatcherRegistryImpl;
import com.sun.corba.se.impl.protocol.CorbaInvocationInfo;
import com.sun.corba.se.impl.transport.CorbaTransportManagerImpl;
import com.sun.corba.se.impl.legacy.connection.LegacyServerSocketManagerImpl;
import com.sun.corba.se.impl.util.Utility;
import com.sun.corba.se.impl.logging.ORBUtilSystemException;
import com.sun.corba.se.impl.copyobject.CopierManagerImpl;
import com.sun.corba.se.impl.javax.rmi.CORBA.Util;
import com.sun.corba.se.impl.orbutil.ByteArrayWrapper;
         

/**
 * The JavaIDL ORB implementation.
 */
public class ORBImpl extends com.sun.corba.se.spi.orb.ORB
{
    private boolean set_parameters_called = false ;

    protected CorbaTransportManager transportManager;
    protected LegacyServerSocketManager legacyServerSocketManager;

    private ThreadLocal OAInvocationInfoStack ; 

    private ThreadLocal clientInvocationInfoStack ; 

    // pure java orb, caching the servant IOR per ORB
    private CodeBase codeBase = null ; 
    private IOR codeBaseIOR = null ;

    // List holding deferred Requests
    private List<Request>     dynamicRequests ; 
    private SynchVariable     svResponseReceived ;

    private java.lang.Object runObj = new java.lang.Object();
    private java.lang.Object shutdownObj = new java.lang.Object();
    private java.lang.Object waitForCompletionObj = new java.lang.Object();
    private static final byte STATUS_OPERATING = 1;
    private static final byte STATUS_SHUTTING_DOWN = 2;
    private static final byte STATUS_SHUTDOWN = 3;
    private static final byte STATUS_DESTROYED = 4;
    private byte status = STATUS_OPERATING;

    // XXX Should we move invocation tracking to the first level server dispatcher?
    private java.lang.Object invocationObj = new java.lang.Object();
    private int numInvocations = 0;

    // thread local variable to store a boolean to detect deadlock in 
    // ORB.shutdown(true).
    private ThreadLocal<Boolean> isProcessingInvocation = 
	new ThreadLocal<Boolean> () {
	    protected Boolean initialValue() {
		return false ;
	    }
	};

    // This map is caching TypeCodes created for a certain class (key)
    // and is used in Util.writeAny()
    private Map<Class<?>,TypeCodeImpl> typeCodeForClassMap ;

    // Cache to hold ValueFactories (Helper classes) keyed on repository ids
    private Map<String,ValueFactory> valueFactoryCache = 
	new HashMap<String,ValueFactory>();

    // thread local variable to store the current ORB version.
    // default ORB version is the version of ORB with correct Rep-id
    // changes
    private ThreadLocal<ORBVersion> orbVersionThreadLocal ; 

    private RequestDispatcherRegistry requestDispatcherRegistry ;

    private CopierManager copierManager ;

    private TimerManager<TimingPoints> timerManager ;

    private int transientServerId ;

    private ServiceContextFactoryRegistry serviceContextFactoryRegistry ;

    private ServiceContextsCache serviceContextsCache;

    // Needed here to implement connect/disconnect
    private TOAFactory toaFactory ;

    // Needed here for set_delegate
    private POAFactory poaFactory ;

    // The interceptor handler, which provides portable interceptor services for
    // subcontracts and object adapters.
    private PIHandler pihandler ;

    private ORBData configData ;

    private BadServerIdHandler badServerIdHandler ;

    private ClientDelegateFactory clientDelegateFactory ;

    private CorbaContactInfoListFactory corbaContactInfoListFactory ;

    // All access to resolver, localResolver, and urlOperation must be protected using
    // the appropriate locks.  Do not hold the ORBImpl lock while accessing
    // resolver, or deadlocks may occur.
    // Note that we now have separate locks for each resolver type.  This is due
    // to bug 6238477, which was caused by a deadlock while resolving a
    // corbaname: URL that contained a reference to the same ORB as the
    // ORB making the call to string_to_object.  This caused a deadlock between the
    // client thread holding the single lock for access to the urlOperation,
    // and the server thread handling the client is_a request waiting on the
    // same lock to access the localResolver.

    // Used for resolver_initial_references and list_initial_services
    private Resolver resolver ;

    // Used for register_initial_references
    private LocalResolver localResolver ;

    // ServerRequestDispatcher used for all INS object references.
    private CorbaServerRequestDispatcher insNamingDelegate ;

    // resolverLock must be used for all access to either resolver or
    // localResolver, since it is possible for the resolver to indirectly
    // refer to the localResolver.  Also used to protect access to
    // insNamingDelegate.
    private Object resolverLock ;

    // Converts strings to object references for resolvers and string_to_object
    private Operation urlOperation ;
    private Object urlOperationLock ;

    private TaggedComponentFactoryFinder taggedComponentFactoryFinder ;

    private IdentifiableFactoryFinder<TaggedProfile> taggedProfileFactoryFinder ;

    private IdentifiableFactoryFinder<TaggedProfileTemplate> taggedProfileTemplateFactoryFinder ;

    private ObjectKeyFactory objectKeyFactory ;

    private boolean orbOwnsThreadPoolManager = false ;

    private ThreadPoolManager threadpoolMgr;

    private InvocationInterceptor invocationInterceptor ;

    private WeakHashMap<ByteArrayWrapper, ObjectKeyCacheEntry> objectKeyCache = 
                   new WeakHashMap<ByteArrayWrapper, ObjectKeyCacheEntry> ();

    private java.lang.Object objectKeyCacheLock = new java.lang.Object();

    private void dprint( String msg )
    {
        ORBUtility.dprint( this, msg ) ;
    }

    public InvocationInterceptor getInvocationInterceptor() 
    {
	return invocationInterceptor ;
    }

    public void setInvocationInterceptor( 
	InvocationInterceptor interceptor ) 
    {
	this.invocationInterceptor = interceptor ;
    }
    
    ////////////////////////////////////////////////////
    //
    // NOTE:
    //
    // Methods that are synchronized MUST stay synchronized.
    //
    // Methods that are NOT synchronized must stay that way to avoid deadlock.
    //
    //
    // REVISIT:
    //
    // checkShutDownState - lock on different object - and normalize usage.
    // starting/FinishDispatch and Shutdown
    // 

    public ORBData getORBData() 
    {
	return configData ;
    }
 
    public PIHandler getPIHandler()
    {
	return pihandler ;
    }

    /**
     * Create a new ORB. Should be followed by the appropriate
     * set_parameters() call.
     */
    public ORBImpl()
    {
	// All initialization is done through set_parameters().
    }

    public ORBVersion getORBVersion()
    {
        return orbVersionThreadLocal.get() ;
    }

    public void setORBVersion(ORBVersion verObj)
    {
        orbVersionThreadLocal.set(verObj);
    }


    private void initManagedObjectManager() {
        if (orbLifecycleDebugFlag) {
            wrapper.orbLifecycleTrace( getORBData().getORBId(), 
                "starting ManagedObjectManager initialization" ) ;
        }

        createORBManagedObjectManager() ;
        mom.registerAtRoot( configData ) ;

        if (orbLifecycleDebugFlag) {
            wrapper.orbLifecycleTrace( getORBData().getORBId(), 
                "ManagedObjectManager initialization complete" ) ;
        }
    }

/****************************************************************************
 * The following methods are ORB initialization
 ****************************************************************************/

    // preInit initializes all non-pluggable ORB data that is independent
    // of the property parsing.
    private void preInit( String[] params, Properties props )
    {
	// This is the unique id of this server (JVM). Multiple incarnations
	// of this server will get different ids.
	// Compute transientServerId = milliseconds since Jan 1, 1970
	// Note: transientServerId will wrap in about 2^32 / 86400000 = 49.7 days.
	// If two ORBS are started at the same time then there is a possibility
	// of having the same transientServerId. This may result in collision 
	// and may be a problem in ior.isLocal() check to see if the object 
	// belongs to the current ORB. This problem is taken care of by checking
	// to see if the IOR port matches ORB server port in legacyIsLocalServerPort()
	// method.
	//
	// XXX need to move server ID to a string for CORBA 3.0.  At that point,
	// make this more unique (possibly use java.rmi.server.UID).
	transientServerId = (int)System.currentTimeMillis();

	orbVersionThreadLocal  = new ThreadLocal<ORBVersion>() {
	    protected ORBVersion initialValue() {
		// set default to version of the ORB with correct Rep-ids
		return ORBVersionFactory.getORBVersion() ;
	    }
	};

	resolverLock = new java.lang.Object() ;
	urlOperationLock = new java.lang.Object() ;

	requestDispatcherRegistry = new RequestDispatcherRegistryImpl( 
	    this, ORBConstants.DEFAULT_SCID);
	copierManager = new CopierManagerImpl( this ) ;

	taggedComponentFactoryFinder = 
	    new TaggedComponentFactoryFinderImpl(this) ;
	taggedProfileFactoryFinder = 
	    new TaggedProfileFactoryFinderImpl(this) ;
	taggedProfileTemplateFactoryFinder = 
	    new TaggedProfileTemplateFactoryFinderImpl(this) ;

	dynamicRequests = new ArrayList<Request>();
	svResponseReceived = new SynchVariable();

	OAInvocationInfoStack = 
	    new ThreadLocal () {
		protected java.lang.Object initialValue() 
		{
		    return new StackImpl();
		} 
	    };

	clientInvocationInfoStack = 
	    new ThreadLocal() {
		protected java.lang.Object initialValue() {
		    return new StackImpl();
		}
	    };

	serviceContextFactoryRegistry = 
	    ServiceContextDefaults.makeServiceContextFactoryRegistry( this ) ;
    }

    protected void setDebugFlags( String[] args )
    {
	for (int ctr=0; ctr<args.length; ctr++ ) {
            String token = args[ctr] ;

            // If there is a public boolean data member in this class
            // named token + "DebugFlag", set it to true.
            try {
                Field fld = this.getClass().getField( token + "DebugFlag" ) ;
                int mod = fld.getModifiers() ;
                if (Modifier.isPublic( mod ) && !Modifier.isStatic( mod ))
                    if (fld.getType() == boolean.class)
                        fld.setBoolean( this, true ) ;
            } catch (Exception exc) {
                // ignore it XXX log this as info
            }
        }
    }

    // Class that defines a parser that gets the name of the
    // ORBConfigurator class.
    private class ConfigParser extends ParserImplBase {
	// The default here is the ORBConfiguratorImpl that we define,
	// but this can be replaced.
	public Class configurator = ORBConfiguratorImpl.class ;

	public PropertyParser makeParser()
	{
	    PropertyParser parser = new PropertyParser() ;
	    parser.add( ORBConstants.SUN_PREFIX + "ORBConfigurator",
		OperationFactory.classAction( classNameResolver() ),
                "configurator" ) ;
	    return parser ;
	}
    }

    // Map String to Integer to count number of ORBs with the 
    // same ORBId.
    private static Map<String,Integer> idcount = 
	new HashMap<String,Integer>() ;
    private String rootName = null ;

    @Override
    public synchronized String getUniqueOrbId() {
	if (rootName == null) {
	    String orbid = getORBData().getORBId() ;
            if (orbid.equals( "" ))
                orbid = "orb" ;

	    int num = 1 ;
	    // Look up the current count of ORB instances with 
	    // the same ORBId.  If this is the first instance,
	    // the count is 1, otherwise increment the count.
	    synchronized (idcount) {
		if (idcount.containsKey( orbid )) {
		    num = idcount.get( orbid ) + 1 ;
		}

		idcount.put( orbid, num ) ;
	    }

	    if (num != 1) {
		rootName = orbid + "_" + num ;
            } else {
                rootName = orbid ;
            }
	}

	return rootName ;
    }

    private void postInit( String[] params, DataCollector dataCollector )
    {
	// First, create the standard ORB config data.
	// This must be initialized before the ORBConfigurator
	// is executed. Note that the orbId is initialized here.
	configData = new ORBDataParserImpl( this, dataCollector) ;
	if (orbInitDebug) {
	    System.out.println( "Contents of ORB configData:" ) ;
	    System.out.println( ObjectUtility.defaultObjectToString( configData ) ) ;
	}

	// Set the debug flags early so they can be used by other
	// parts of the initialization.
	setDebugFlags( configData.getORBDebugFlags() ) ;

        if (orbLifecycleDebugFlag) {
            wrapper.orbLifecycleTrace( getORBData().getORBId(), 
                "Config data parsing complete" ) ;
        }

        initManagedObjectManager() ;

	// The TimerManager must be
	// initialized BEFORE the pihandler.initialize() call, in
	// case we want to time interceptor setup.  Obviously we
	// want to initialize the timerManager as early as possible
	// so we can time parts of initialization if desired.
	timerManager = makeTimerManager( mom ) ;

	// This requires a valid TimerManager.
	initializePrimitiveTypeCodeConstants() ;

	// REVISIT: this should go away after more transport init cleanup
	// and going to ORT based ORBD.  
	transportManager = new CorbaTransportManagerImpl(this);
	getLegacyServerSocketManager();

        if (orbLifecycleDebugFlag) {
            wrapper.orbLifecycleTrace( getORBData().getORBId(), 
                "Transport initialization complete" ) ;
        }

        super.getByteBufferPool();
	serviceContextsCache = new ServiceContextsCache(this);

        // Set up the full PIHandler now.  The ORB configurator call is the
        // earliest point at which an invocation on this ORB can occur.
        // ORB invocations can also occur during the execution of
        // the ORBInitializers.  Interceptors will not be executed until 
        // after pihandler.initialize().  A request that starts before
        // initialize completes and completes after initialize completes does
        // not see any interceptors.
	pihandler = new PIHandlerImpl( this, params) ;

	// Create a parser to get the configured ORBConfigurator.
	ConfigParser parser = new ConfigParser() ;
	parser.init( dataCollector ) ;

	ORBConfigurator configurator =  null ;
	try {
	    configurator = 
		(ORBConfigurator)(parser.configurator.newInstance()) ;
	} catch (Exception iexc) {
	    throw wrapper.badOrbConfigurator( iexc, parser.configurator.getName() ) ;
	}

	// Finally, run the configurator.  Note that the default implementation allows
	// other configurators with their own parsers to run,
	// using the same DataCollector.
	try {
	    configurator.configure( dataCollector, this ) ;
	} catch (Exception exc) {
	    throw wrapper.orbConfiguratorError( exc ) ;
	}

        if (orbLifecycleDebugFlag) {
            wrapper.orbLifecycleTrace( getORBData().getORBId(), 
                "User configurator execution complete" ) ;
        }

        // Initialize the thread manager pool 
        // so it may be initialized & accessed without synchronization.
        // This must take place here so that a user conifigurator can 
        // set the threadpool manager first.
        getThreadPoolManager();

	// Last of all, run the ORB initializers.
	pihandler.initialize() ;

        if (orbLifecycleDebugFlag) {
            wrapper.orbLifecycleTrace( getORBData().getORBId(), 
                "Interceptor initialization complete" ) ;
        }

        // Now the ORB is ready, so finish all of the MBean registration
        if (configData.registerMBeans()) {
            mom.resumeJMXRegistration() ;

            if (orbLifecycleDebugFlag) {
                wrapper.orbLifecycleTrace( getORBData().getORBId(), 
                    "MBeans should be registered" ) ;
            }
        }
    }

    private synchronized POAFactory getPOAFactory() 
    {
	if (poaFactory == null) {
	    poaFactory = (POAFactory)requestDispatcherRegistry.getObjectAdapterFactory( 
		ORBConstants.TRANSIENT_SCID ) ;
	}

	return poaFactory ;
    }

    private synchronized TOAFactory getTOAFactory() 
    {
	if (toaFactory == null) {
	    toaFactory = (TOAFactory)requestDispatcherRegistry.getObjectAdapterFactory( 
		ORBConstants.TOA_SCID ) ;
	}

	return toaFactory ;
    }

    public void check_set_parameters() {
        if (set_parameters_called) {
            throw wrapper.setParameterCalledAgain() ;
        } else {
            set_parameters_called = true ;
        }
    }

    public void set_parameters( Properties props )
    {
	preInit( null, props ) ;
	DataCollector dataCollector = 
	    DataCollectorFactory.create( props, getLocalHostName() ) ;
	postInit( null, dataCollector ) ;
        if (orbLifecycleDebugFlag) {
            wrapper.orbLifecycleTrace( getORBData().getORBId(), "initialization complete" ) ;
        }
    }

    protected void set_parameters(Applet app, Properties props)
    {
	preInit( null, props ) ;
	DataCollector dataCollector = 
	    DataCollectorFactory.create( app, props, getLocalHostName() ) ;
	postInit( null, dataCollector ) ;
    }

    public void setParameters( String[] params, Properties props ) {
        set_parameters( params, props ) ;
    }

  /** 
   * we can't create object adapters inside the ORB init path, or else we'll get this same problem
   * in slightly different ways. (address in use exception)
   * Having an IORInterceptor (TxSecIORInterceptor) get called during ORB init always results in a
   * nested ORB.init call because of the call to getORB in the IORInterceptor.
   */
    protected void set_parameters (String[] params, Properties props)
    {
	preInit( params, props ) ;
	DataCollector dataCollector = 
	    DataCollectorFactory.create( params, props, getLocalHostName() ) ;
	postInit( params, dataCollector ) ;
    }

/****************************************************************************
 * The following methods are standard public CORBA ORB APIs
 ****************************************************************************/

    public synchronized org.omg.CORBA.portable.OutputStream create_output_stream()
    {
        checkShutdownState();
        return new EncapsOutputStream(this);
    }

    /**
     * Get a Current pseudo-object.
     * The Current interface is used to manage thread-specific
     * information for use by the transactions, security and other
     * services. This method is deprecated,
     * and replaced by ORB.resolve_initial_references("NameOfCurrentObject");
     *
     * @return          a Current pseudo-object.
     * @deprecated
     */
    public synchronized org.omg.CORBA.Current get_current()
    {
        checkShutdownState();

        /* _REVISIT_
           The implementation of get_current is not clear. How would
           ORB know whether the caller wants a Current for transactions
           or security ?? Or is it assumed that there is just one
           implementation for both ? If Current is thread-specific,
           then it should not be instantiated; so where does the
           ORB get a Current ? 
	   
	   This should probably be deprecated. */

	throw wrapper.genericNoImpl() ;
    }

    /**
     * Create an NVList
     *
     * @param count	size of list to create
     * @result		NVList created
     *
     * @see NVList
     */
    public synchronized NVList create_list(int count)
    {
        checkShutdownState();
        return new NVListImpl(this, count);
    }

    /**
     * Create an NVList corresponding to an OperationDef
     *
     * @param oper	operation def to use to create list
     * @result		NVList created
     *
     * @see NVList
     */
    public synchronized NVList create_operation_list(org.omg.CORBA.Object oper)
    {
        checkShutdownState();
	throw wrapper.genericNoImpl() ;
    }

    /**
     * Create a NamedValue
     *
     * @result		NamedValue created
     */
    public synchronized NamedValue create_named_value(String s, Any any, int flags)
    {
        checkShutdownState();
        return new NamedValueImpl(this, s, any, flags);
    }

    /**
     * Create an ExceptionList
     *
     * @result		ExceptionList created
     */
    public synchronized org.omg.CORBA.ExceptionList create_exception_list()
    {
        checkShutdownState();
        return new ExceptionListImpl();
    }

    /**
     * Create a ContextList
     *
     * @result		ContextList created
     */
    public synchronized org.omg.CORBA.ContextList create_context_list()
    {
        checkShutdownState();
        return new ContextListImpl(this);
    }

    /**
     * Get the default Context object
     *
     * @result		the default Context object
     */
    public synchronized org.omg.CORBA.Context get_default_context()
    {
        checkShutdownState();
	throw wrapper.genericNoImpl() ;
    }

    /**
     * Create an Environment
     *
     * @result		Environment created
     */
    public synchronized org.omg.CORBA.Environment create_environment()
    {
        checkShutdownState();
        return new EnvironmentImpl();
    }

    public synchronized void send_multiple_requests_oneway(Request[] req)
    {
        checkShutdownState();

        // Invoke the send_oneway on each new Request
        for (int i = 0; i < req.length; i++) {
            req[i].send_oneway();
        }
    }

    /**
     * Send multiple dynamic requests asynchronously.
     *
     * @param req         an array of request objects.
     */
    public synchronized void send_multiple_requests_deferred(Request[] req)
    {
        checkShutdownState();

        // add the new Requests to pending dynamic Requests
	for (Request r : req) 
	    dynamicRequests.add( r ) ;

        // Invoke the send_deferred on each new Request
	for (Request r : req) {
            AsynchInvoke invokeObject = new AsynchInvoke( this, 
		(com.sun.corba.se.impl.corba.RequestImpl)r, true);
            new Thread(invokeObject).start();
        }
    }

    /**
     * Find out if any of the deferred invocations have a response yet.
     */
    public synchronized boolean poll_next_response()
    {
        checkShutdownState();

        // poll on each pending request
	synchronized(dynamicRequests) {
	    for (Request r : dynamicRequests) {
		if (r.poll_response())
		    return true ;
	    }
	}
        return false;
    }

    /**
     * Get the next request that has gotten a response.
     *
     * @result            the next request ready with a response.
     */
    public org.omg.CORBA.Request get_next_response()
        throws org.omg.CORBA.WrongTransaction
    {
	synchronized( this ) {
	    checkShutdownState();
	}

        while (true) {
            // check if there already is a response
            synchronized ( dynamicRequests ) {
		Iterator<Request> iter = dynamicRequests.iterator() ;
		while (iter.hasNext()) {
		    Request curr = iter.next() ;
		    if (curr.poll_response()) {
			curr.get_response() ;
			iter.remove() ;
			return curr ;
		    }
		}
            }

            // wait for a response
            synchronized(this.svResponseReceived) {
                while (!this.svResponseReceived.value()) {
                    try {
                        this.svResponseReceived.wait();
                    } catch(java.lang.InterruptedException ex) {
			// NO-OP
		    }
                }
                // reinitialize the response flag
                this.svResponseReceived.reset();
            }
        }
    }

    /**
     * Notify response to ORB for get_next_response
     */
    public void notifyORB() 
    {
	synchronized (this.svResponseReceived) {
	    this.svResponseReceived.set();
	    this.svResponseReceived.notify();
	}
    }

    /**
     * Convert an object ref to a string.
     * @param obj The object to stringify.
     * @return A stringified object reference.
     */
    public synchronized String object_to_string(org.omg.CORBA.Object obj)
    {
        checkShutdownState();

        // Handle the null objref case
        if (obj == null) {
	    IOR nullIOR = IORFactories.makeIOR( this ) ;
            return nullIOR.stringify();
	}

	IOR ior = null ;

	try {
	    ior = getIOR( obj, true ) ;
	} catch (BAD_PARAM bp) {
	    // Throw MARSHAL instead if this is a LOCAL_OBJECT_NOT_ALLOWED error.
	    if (bp.minor == ORBUtilSystemException.LOCAL_OBJECT_NOT_ALLOWED) {
		throw omgWrapper.notAnObjectImpl( bp ) ;
	    } else
		// Not a local object problem: just rethrow the exception.
		// Do not wrap and log this, since it was already logged at its
		// point of origin.
		throw bp ;
	}

	return ior.stringify() ;
    }

    /**
     * Convert a stringified object reference to the object it represents.
     * @param str The stringified object reference.
     * @return The unstringified object reference.
     */
    public org.omg.CORBA.Object string_to_object(String str)
    {
	Operation op ;

	synchronized (this) {
	    checkShutdownState();
	    op = urlOperation ;
	}

	if (str == null)
	    throw wrapper.nullParam() ;

	synchronized (urlOperationLock) {
	    org.omg.CORBA.Object obj = (org.omg.CORBA.Object)op.operate( str ) ;
	    return obj ;
	}
    }

    // pure java orb support, moved this method from FVDCodeBaseImpl.
    // Note that we connect this if we have not already done so.
    public synchronized IOR getFVDCodeBaseIOR()
    {
        if (codeBaseIOR == null) {
	    ValueHandler vh = ORBUtility.createValueHandler(this);
	    codeBase = (CodeBase)vh.getRunTimeCodeBase();
	    codeBaseIOR = getIOR( codeBase, true ) ;
	}

	return codeBaseIOR;
    }

    /**
     * Get the TypeCode for a primitive type.
     *
     * @param tcKind	the integer kind for the primitive type
     * @return		the requested TypeCode
     */
    public synchronized TypeCode get_primitive_tc(TCKind tcKind)
    {
        checkShutdownState();
	return get_primitive_tc( tcKind.value() ) ; 
    }

    /**
     * Create a TypeCode for a structure.
     *
     * @param id		the logical id for the typecode.
     * @param name	the name for the typecode.
     * @param members	an array describing the members of the TypeCode.
     * @return		the requested TypeCode.
     */
    public synchronized TypeCode create_struct_tc(String id,
                                     String name,
                                     StructMember[] members)
    {
        checkShutdownState();
        return new TypeCodeImpl(this, TCKind._tk_struct, id, name, members);
    }

    /**
     * Create a TypeCode for a union.
     *
     * @param id		the logical id for the typecode.
     * @param name	the name for the typecode.
     * @param discriminator_type
     *			the type of the union discriminator.
     * @param members	an array describing the members of the TypeCode.
     * @return		the requested TypeCode.
     */
    public synchronized TypeCode create_union_tc(String id,
                                    String name,
                                    TypeCode discriminator_type,
                                    UnionMember[] members)
    {
        checkShutdownState();
        return new TypeCodeImpl(this,
                                TCKind._tk_union,
                                id,
                                name,
                                discriminator_type,
                                members);
    }

    /**
     * Create a TypeCode for an enum.
     *
     * @param id		the logical id for the typecode.
     * @param name	the name for the typecode.
     * @param members	an array describing the members of the TypeCode.
     * @return		the requested TypeCode.
     */
    public synchronized TypeCode create_enum_tc(String id,
                                   String name,
                                   String[] members)
    {
        checkShutdownState();
        return new TypeCodeImpl(this, TCKind._tk_enum, id, name, members);
    }

    /**
     * Create a TypeCode for an alias.
     *
     * @param id		the logical id for the typecode.
     * @param name	the name for the typecode.
     * @param original_type
     * 			the type this is an alias for.
     * @return		the requested TypeCode.
     */
    public synchronized TypeCode create_alias_tc(String id,
                                    String name,
                                    TypeCode original_type)
    {
        checkShutdownState();
        return new TypeCodeImpl(this, TCKind._tk_alias, id, name, original_type);
    }

    /**
     * Create a TypeCode for an exception.
     *
     * @param id		the logical id for the typecode.
     * @param name	the name for the typecode.
     * @param members	an array describing the members of the TypeCode.
     * @return		the requested TypeCode.
     */
    public synchronized TypeCode create_exception_tc(String id,
                                        String name,
                                        StructMember[] members)
    {
        checkShutdownState();
        return new TypeCodeImpl(this, TCKind._tk_except, id, name, members);
    }

    /**
     * Create a TypeCode for an interface.
     *
     * @param id		the logical id for the typecode.
     * @param name	the name for the typecode.
     * @return		the requested TypeCode.
     */
    public synchronized TypeCode create_interface_tc(String id,
                                        String name)
    {
        checkShutdownState();
        return new TypeCodeImpl(this, TCKind._tk_objref, id, name);
    }

    /**
     * Create a TypeCode for a string.
     *
     * @param bound	the bound for the string.
     * @return		the requested TypeCode.
     */
    public synchronized TypeCode create_string_tc(int bound)
    {
        checkShutdownState();
        return new TypeCodeImpl(this, TCKind._tk_string, bound);
    }

    /**
     * Create a TypeCode for a wide string.
     *
     * @param bound	the bound for the string.
     * @return		the requested TypeCode.
     */
    public synchronized TypeCode create_wstring_tc(int bound)
    {
        checkShutdownState();
        return new TypeCodeImpl(this, TCKind._tk_wstring, bound);
    }

    /**
     * Create a TypeCode for a sequence.
     *
     * @param bound	the bound for the sequence.
     * @param element_type
     *			the type of elements of the sequence.
     * @return		the requested TypeCode.
     */
    public synchronized TypeCode create_sequence_tc(int bound,
                                       TypeCode element_type)
    {
        checkShutdownState();
        return new TypeCodeImpl(this, TCKind._tk_sequence, bound, element_type);
    }


    /**
     * Create a recursive TypeCode in a sequence.
     *
     * @param bound	the bound for the sequence.
     * @param offset	the index to the enclosing TypeCode that is
     *			being referenced.
     * @return		the requested TypeCode.
     */
    public synchronized TypeCode create_recursive_sequence_tc(int bound,
                                                 int offset)
    {
        checkShutdownState();
        return new TypeCodeImpl(this, TCKind._tk_sequence, bound, offset);
    }


    /**
     * Create a TypeCode for an array.
     *
     * @param length	the length of the array.
     * @param element_type
     *			the type of elements of the array.
     * @return		the requested TypeCode.
     */
    public synchronized TypeCode create_array_tc(int length,
                                    TypeCode element_type)
    {
        checkShutdownState();
        return new TypeCodeImpl(this, TCKind._tk_array, length, element_type);
    }


    public synchronized org.omg.CORBA.TypeCode create_native_tc(String id,
                                                   String name)
    {
        checkShutdownState();
        return new TypeCodeImpl(this, TCKind._tk_native, id, name);
    }

    public synchronized org.omg.CORBA.TypeCode create_abstract_interface_tc(
                                                               String id,
                                                               String name)
    {
        checkShutdownState();
        return new TypeCodeImpl(this, TCKind._tk_abstract_interface, id, name);
    }

    public synchronized org.omg.CORBA.TypeCode create_fixed_tc(short digits, short scale)
    {
        checkShutdownState();
        return new TypeCodeImpl(this, TCKind._tk_fixed, digits, scale);
    }

    public synchronized org.omg.CORBA.TypeCode create_value_tc(String id,
                                                  String name,
                                                  short type_modifier,
                                                  TypeCode concrete_base,
                                                  ValueMember[] members)
    {
        checkShutdownState();
        return new TypeCodeImpl(this, TCKind._tk_value, id, name,
                                type_modifier, concrete_base, members);
    }

    public synchronized org.omg.CORBA.TypeCode create_recursive_tc(String id) {
        checkShutdownState();
        return new TypeCodeImpl(this, id);
    }

    public synchronized org.omg.CORBA.TypeCode create_value_box_tc(String id,
                                                      String name,
                                                      TypeCode boxed_type)
    {
        checkShutdownState();
        return new TypeCodeImpl(this, TCKind._tk_value_box, id, name, 
	    boxed_type);
    }

    /**
     * Create a new Any
     *
     * @return		the new Any created.
     */
    public synchronized Any create_any()
    {
        checkShutdownState();
        return new AnyImpl(this);
    }

    // TypeCodeFactory interface methods.
    // Keeping track of type codes by repository id.

    // Keeping a cache of TypeCodes associated with the class
    // they got created from in Util.writeAny().

    public synchronized void setTypeCodeForClass(Class c, TypeCodeImpl tci) 
    {
        if (typeCodeForClassMap == null)
            typeCodeForClassMap = 
		new WeakHashMap<Class<?>,TypeCodeImpl>(64);

        // Store only one TypeCode per class.
        if ( ! typeCodeForClassMap.containsKey(c))
            typeCodeForClassMap.put(c, tci);
    }

    public synchronized TypeCodeImpl getTypeCodeForClass(Class c) 
    {
        if (typeCodeForClassMap == null)
            return null;
        return (TypeCodeImpl)typeCodeForClassMap.get(c);
    }

/****************************************************************************
 * The following methods deal with listing and resolving the initial
 * (bootstrap) object references such as "NameService".
 ****************************************************************************/

    /**
     * Get a list of the initially available CORBA services.
     * This does not work unless an ORBInitialHost is specified during 
     * initialization (or unless there is an ORB running on the AppletHost) 
     * since the localhostname
     * is inaccessible to applets. If a service properties URL was specified,
     * then it is used, otherwise the bootstrapping protocol is used.
     * @return A list of the initial services available.
     */
    public String[] list_initial_services()
    {
	Resolver res ;

	synchronized( this ) {
	    checkShutdownState();
	    res = resolver ;
	}

	synchronized (resolverLock) {
	    java.util.Set<String> keys = res.list() ;
	    return keys.toArray( new String[keys.size()] ) ;
	}
    }

    /**
     * Resolve the stringified reference of one of the initially
     * available CORBA services.
     * @param identifier The stringified object reference of the
     * desired service.
     * @return An object reference for the desired service.
     * @exception InvalidName The supplied identifier is not associated
     * with a known service.
     * @exception SystemException One of a fixed set of Corba system exceptions.
     */
    public org.omg.CORBA.Object resolve_initial_references(
        String identifier) throws InvalidName
    {
	Resolver res ;

	synchronized( this ) {
	    checkShutdownState();
	    res = resolver ;
	}

	org.omg.CORBA.Object result = res.resolve( identifier ) ;
	
	if (result == null)
	    throw new InvalidName(identifier + " not found") ;
	else
	    return result ;
    }

    /**
     * If this operation is called with an id, <code>"Y"</code>, and an
     * object, <code>YY</code>, then a subsequent call to
     * <code>ORB.resolve_initial_references( "Y" )</code> will
     * return object <code>YY</code>.
     *
     * @param id The ID by which the initial reference will be known.
     * @param obj The initial reference itself.
     * @throws InvalidName if this operation is called with an empty string id
     *     or this operation is called with an id that is already registered,
     *     including the default names defined by OMG.
     * @throws BAD_PARAM if the obj parameter is null.
     */
    public void register_initial_reference(
        String id, org.omg.CORBA.Object obj ) throws InvalidName
    {
	CorbaServerRequestDispatcher insnd ;

        if ((id == null) || (id.length() == 0))
            throw new InvalidName("Null or empty id string") ;

	synchronized (this) {
	    checkShutdownState();
	}

	synchronized (resolverLock) {
	    insnd = insNamingDelegate ;

	    java.lang.Object obj2 = localResolver.resolve( id ) ;
	    if (obj2 != null)
		throw new InvalidName(id + " already registered") ;

	    localResolver.register( id, ClosureFactory.makeConstant( obj )) ;
	}
      
	synchronized (this) {
	    if (StubAdapter.isStub(obj))
		// Make all remote object references available for INS.
		requestDispatcherRegistry.registerServerRequestDispatcher( 
		    insnd, id ) ;
	}
    }

/****************************************************************************
 * The following methods (introduced in POA / CORBA2.1) deal with
 * shutdown / single threading.
 ****************************************************************************/

    public void run() 
    {
	synchronized (this) {
	    checkShutdownState();
	}

        synchronized (runObj) {
            try {
                runObj.wait();
            } catch ( InterruptedException ex ) {}
        }
    }

    public void shutdown(boolean wait_for_completion) {
	boolean wait = false ;

	synchronized (this) {
	    checkShutdownState();
	    
            // This is to avoid deadlock: don't allow a thread that is 
	    // processing a request to call shutdown( true ), because
	    // the shutdown would block waiting for the request to complete,
	    // while the request would block waiting for shutdown to complete.
            if (wait_for_completion &&
		isProcessingInvocation.get() == Boolean.TRUE) {
		throw omgWrapper.shutdownWaitForCompletionDeadlock() ;
	    }

	    if (status == STATUS_SHUTTING_DOWN) {
		if (wait_for_completion) {
		    wait = true ;
		} else {
		    return ;
		}
	    }

	    status = STATUS_SHUTTING_DOWN ;
	} 

        // Avoid more than one thread performing shutdown at a time.
        synchronized (shutdownObj) {
	    // At this point, the ORB status is certainly STATUS_SHUTTING_DOWN.
	    // If wait is true, another thread already called shutdown( true ),
	    // and so we wait for completion
	    if (wait) {
                while (true) {
                    synchronized (this) {
                        if (status == STATUS_SHUTDOWN)
                            break ;
                    }

		    try {
			shutdownObj.wait() ;
		    } catch (InterruptedException exc) {
			// NOP: just loop and wait until state is changed
		    }
                }
	    } else {
                if (orbLifecycleDebugFlag) {
                    wrapper.orbLifecycleTrace( getORBData().getORBId(), "starting shutdown" ) ;
                }
                
                // perform the actual shutdown
		shutdownServants(wait_for_completion);

		if (wait_for_completion) {
		    synchronized ( waitForCompletionObj ) {
			while (numInvocations > 0) {
			    try {
				waitForCompletionObj.wait();
			    } catch (InterruptedException ex) {}
			}
		    }
		}

		synchronized ( runObj ) {
		    runObj.notifyAll();
		}

		status = STATUS_SHUTDOWN;

		shutdownObj.notifyAll() ;
	    }
        }
    }

    // Cause all ObjectAdapaterFactories to clean up all of their internal state, which 
    // may include activated objects that have associated state and callbacks that must
    // complete in order to shutdown.  This will cause new request to be rejected.
    protected void shutdownServants(boolean wait_for_completion) {
        Set<ObjectAdapterFactory> oaset ;
        synchronized(this) {
            oaset = new HashSet<ObjectAdapterFactory>( 
                requestDispatcherRegistry.getObjectAdapterFactories() ) ;
        }

        for (ObjectAdapterFactory oaf : oaset) 
            oaf.shutdown( wait_for_completion ) ;
    }

    // Note that the caller must hold the ORBImpl lock.
    public void checkShutdownState() 
    {
        if (status == STATUS_DESTROYED) {
	    throw wrapper.orbDestroyed() ;
        }

        if (status == STATUS_SHUTDOWN) {
	    throw omgWrapper.badOperationAfterShutdown() ;
        }
    }

    public boolean isDuringDispatch() 
    {
	return isProcessingInvocation.get() ;
    }

    public void startingDispatch() 
    {
        synchronized (invocationObj) {
            isProcessingInvocation.set(true);
            numInvocations++;
        }
    }

    public void finishedDispatch() 
    {
        synchronized (invocationObj) {
            numInvocations--;
            isProcessingInvocation.set(false);
            if (numInvocations == 0) {
                synchronized (waitForCompletionObj) {
                    waitForCompletionObj.notifyAll();
                }
            } else if (numInvocations < 0) {
		throw wrapper.numInvocationsAlreadyZero(
		    CompletionStatus.COMPLETED_YES ) ;
            }
        }
    }

    /**
     *	formal/99-10-07 p 159: "If destroy is called on an ORB that has
     *	not been shut down, it will start the shutdown process and block until
     *	the ORB has shut down before it destroys the ORB."
     */
    public void destroy() 
    {
        boolean shutdownFirst = false ;
        synchronized (this) {
            shutdownFirst = (status == STATUS_OPERATING) ;
        }

        if (shutdownFirst) {
            shutdown(true);
        }

        synchronized (this) {
            if (status < STATUS_DESTROYED) {
                getCorbaTransportManager().close();
                getPIHandler().destroyInterceptors() ;
                timerManager.destroy() ;
                timerManager = null ;
                status = STATUS_DESTROYED;
            } else {
                // Already destroyed: don't want to throw null pointer exceptions.
                return ;
            }
        }

        if (orbLifecycleDebugFlag) {
            wrapper.orbLifecycleTrace( getORBData().getORBId(), "starting destruction" ) ;
        }

        ThreadPoolManager tpToClose = null ;
        synchronized (threadPoolManagerAccessLock) {
            if (orbOwnsThreadPoolManager) {
                tpToClose = threadpoolMgr ;
                threadpoolMgr = null ;
            }
        }

        if (tpToClose != null) {
            try {
                tpToClose.close() ;
            } catch (IOException exc) {
                wrapper.ioExceptionOnClose( exc ) ;
            }
        }

        CachedCodeBase.cleanCache( this ) ;
        try {
            pihandler.close() ;
        } catch (IOException exc) {
            wrapper.ioExceptionOnClose( exc ) ;
        }

        super.destroy() ;

        badServerIdHandlerAccessLock = null ;
        clientDelegateFactoryAccessorLock = null ;
        corbaContactInfoListFactoryAccessLock = null ; 
        corbaContactInfoListFactoryReadLock = null ;
        corbaContactInfoListFactoryWriteLock = null ;

        objectKeyFactoryAccessLock = null ;
        legacyServerSocketManagerAccessLock = null ;
        threadPoolManagerAccessLock = null ;
        transportManager = null ;
        legacyServerSocketManager = null ;
        OAInvocationInfoStack  = null ; 
        clientInvocationInfoStack  = null ; 
        codeBase = null ; 
        codeBaseIOR = null ;
        dynamicRequests  = null ; 
        svResponseReceived  = null ;
        runObj = null ;
        shutdownObj = null ;
        waitForCompletionObj = null ;
        invocationObj = null ;
        isProcessingInvocation = null ;
        typeCodeForClassMap  = null ;
        valueFactoryCache = null ;
        orbVersionThreadLocal = null ; 
        requestDispatcherRegistry = null ;
        copierManager = null ;
        serviceContextFactoryRegistry = null ;
        serviceContextsCache= null ;
        toaFactory = null ;
        poaFactory = null ;
        pihandler = null ;
        configData = null ;
        badServerIdHandler = null ;
        clientDelegateFactory = null ;
        corbaContactInfoListFactory = null ;
        resolver = null ;
        localResolver = null ;
        insNamingDelegate = null ;
        resolverLock = null ;
        urlOperation = null ;
        urlOperationLock = null ;
        taggedComponentFactoryFinder = null ;
        taggedProfileFactoryFinder = null ;
        taggedProfileTemplateFactoryFinder = null ;
        objectKeyFactory = null ;
        invocationInterceptor = null ;
        objectKeyCache = null ; 
        objectKeyCacheLock = null ;

        try {
            mom.close() ;
        } catch (IOException exc) {
            // ignore: stupid close exception
        }
    }

    /**
     * Registers a value factory for a particular repository ID.
     *
     * @param repositoryID the repository ID.
     * @param factory the factory.
     * @return the previously registered factory for the given repository ID, 
     * or null if no such factory was previously registered.
     * @exception org.omg.CORBA.BAD_PARAM if the registration fails.
     **/
    public synchronized ValueFactory register_value_factory(String repositoryID, 
	ValueFactory factory) 
    {
        checkShutdownState();

        if ((repositoryID == null) || (factory == null))
	    throw omgWrapper.unableRegisterValueFactory() ;

        return valueFactoryCache.put(repositoryID, factory);
    }

    /**
     * Unregisters a value factory for a particular repository ID.
     *
     * @param repositoryID the repository ID.
     **/
    public synchronized void unregister_value_factory(String repositoryID) 
    {
        checkShutdownState();

        if (valueFactoryCache.remove(repositoryID) == null)
	    throw wrapper.nullParam() ;
    }

    /**
     * Finds and returns a value factory for the given repository ID.
     * The value factory returned was previously registered by a call to
     * {@link #register_value_factory} or is the default factory.
     *
     * @param repositoryID the repository ID.
     * @return the value factory.
     * @exception org.omg.CORBA.BAD_PARAM if unable to locate a factory.
     **/
    public synchronized ValueFactory lookup_value_factory(String repositoryID) 
    {
        checkShutdownState();

        ValueFactory factory = valueFactoryCache.get(repositoryID);

        if (factory == null) {
            try {
                factory = Utility.getFactory(null, null, null, repositoryID);
            } catch(org.omg.CORBA.MARSHAL ex) {
		throw wrapper.unableFindValueFactory( ex ) ;
            }
        }

	return factory ;
    }

    public OAInvocationInfo peekInvocationInfo() 
    {
	StackImpl stack = (StackImpl)(OAInvocationInfoStack.get()) ;
	return (OAInvocationInfo)(stack.peek()) ;
    }

    public void pushInvocationInfo( OAInvocationInfo info ) 
    {
	StackImpl stack = (StackImpl)(OAInvocationInfoStack.get()) ;
	stack.push( info ) ;
    }

    public OAInvocationInfo popInvocationInfo() 
    {
	StackImpl stack = (StackImpl)(OAInvocationInfoStack.get()) ;
	return (OAInvocationInfo)(stack.pop()) ;
    }

    /**
     * The bad server id handler is used by the Locator to
     * send back the location of a persistant server to the client.
     */

    private Object badServerIdHandlerAccessLock = new Object();

    public void initBadServerIdHandler() 
    {
	synchronized (badServerIdHandlerAccessLock) {
	    Class cls = configData.getBadServerIdHandler() ;
	    if (cls != null) {
		try {
		    Class[] params = new Class[] { org.omg.CORBA.ORB.class };
		    java.lang.Object[] args = new java.lang.Object[]{this};
		    Constructor cons = cls.getConstructor(params);
		    badServerIdHandler = 
			(BadServerIdHandler) cons.newInstance(args);
		} catch (Exception e) {
		    throw wrapper.errorInitBadserveridhandler( e ) ;
		}
	    }
	}
    }

    public void setBadServerIdHandler( BadServerIdHandler handler ) 
    {
	synchronized (badServerIdHandlerAccessLock) {
	    badServerIdHandler = handler;
	}
    }

    public void handleBadServerId( ObjectKey okey ) 
    {
	synchronized (badServerIdHandlerAccessLock) {
	    if (badServerIdHandler == null)
		throw wrapper.badServerId() ;
	    else 
		badServerIdHandler.handle( okey ) ;
	}
    }

    public synchronized org.omg.CORBA.Policy create_policy( int type, 
	org.omg.CORBA.Any val ) throws org.omg.CORBA.PolicyError
    {
	checkShutdownState() ;

	return pihandler.create_policy( type, val ) ;
    }

    /** This is the implementation of the public API used to connect
     *  a servant-skeleton to the ORB.
     */
    @Override
    public synchronized void connect(org.omg.CORBA.Object servant)
    {
        checkShutdownState();
	if (getTOAFactory() == null)
	    throw wrapper.noToa() ;

        try {
	    String codebase = Util.getInstance().getCodebase( servant.getClass() ) ;
	    getTOAFactory().getTOA( codebase ).connect( servant ) ;
        } catch ( Exception ex ) {
	    throw wrapper.orbConnectError( ex ) ;
        }
    }

    @Override
    public synchronized void disconnect(org.omg.CORBA.Object obj)
    {
        checkShutdownState();
	if (getTOAFactory() == null)
	    throw wrapper.noToa() ;

        try {
	    getTOAFactory().getTOA().disconnect( obj ) ;
        } catch ( Exception ex ) {
	    throw wrapper.orbConnectError( ex ) ;
        }
    }

    public int getTransientServerId()
    {
        if( configData.getPersistentServerIdInitialized( ) ) {
            // ORBServerId is specified then use that value
            return configData.getPersistentServerId( );
        }
        return transientServerId;
    }

    public RequestDispatcherRegistry getRequestDispatcherRegistry()
    {
        return requestDispatcherRegistry;
    }

    public ServiceContextFactoryRegistry getServiceContextFactoryRegistry()
    {
	return serviceContextFactoryRegistry ;
    } 

    public ServiceContextsCache getServiceContextsCache() 
    {
        return serviceContextsCache;
    }

    // XXX All of the isLocalXXX checking needs to be revisited.
    // First of all, all three of these methods are called from
    // only one place in impl.ior.IORImpl.  Second, we have problems
    // both with multi-homed hosts and with multi-profile IORs.
    // A possible strategy: like the LocalClientRequestDispatcher, we need
    // to determine this more abstractly at the ContactInfo level.
    // This level should probably just get the CorbaContactInfoList from
    // the IOR, then iterator over ContactInfo.  If any ContactInfo is
    // local, the IOR is local, and we can pick one to create the 
    // LocalClientRequestDispatcher as well.  Bottom line: this code needs to move.

    // XXX What about multi-homed host?
    public boolean isLocalHost( String hostName ) 
    {
	return hostName.equals( configData.getORBServerHost() ) ||
	    hostName.equals( getLocalHostName() ) ;
    }

    public boolean isLocalServerId( int subcontractId, int serverId )
    {
	if (subcontractDebugFlag) {
	    int psid = -1;
	    if (configData.getPersistentServerIdInitialized())
		psid = configData.getPersistentServerId();
	    dprint("isLocalServerId:"
		   + " subcontractId: " + subcontractId
		   + " serverId: " + serverId
		   + " transientServerId: " + getTransientServerId()
		   + " isTransient: " + ORBConstants.isTransient( subcontractId )
		   + " isPersistentServerIdInitialized: " + configData.getPersistentServerIdInitialized()
		   + " persistentServerId: " + psid
		   );
	}

	if ((subcontractId < ORBConstants.FIRST_POA_SCID) || 
	    (subcontractId > ORBConstants.MAX_POA_SCID))
	    return serverId == getTransientServerId( ) ;
		
	// XXX isTransient info should be stored in subcontract registry
	if (ORBConstants.isTransient( subcontractId ))
	    return (serverId == getTransientServerId()) ;
	else if (configData.getPersistentServerIdInitialized())
	    return (serverId == configData.getPersistentServerId()) ;
	else
	    return false ;
    }

    /*************************************************************************
     *  The following public methods are for ORB shutdown.
     *************************************************************************/

    private String getHostName(String host) 
	throws java.net.UnknownHostException 
    {
        return InetAddress.getByName( host ).getHostAddress();
    }

    /* keeping a copy of the getLocalHostName so that it can only be called 
     * internally and the unauthorized clients cannot have access to the
     * localHost information, originally, the above code was calling 
     * getLocalHostName from Connection.java.  If the hostname is cached in 
     * Connection.java, then
     * it is a security hole, since any unauthorized client has access to
     * the host information.  With this change it is used internally so the
     * security problem is resolved.  Also in Connection.java, the 
     * getLocalHost() implementation has changed to always call the 
     * InetAddress.getLocalHost().getHostAddress()
     * The above mentioned method has been removed from the connection class
     */

    private static String localHostString = null;

    private synchronized String getLocalHostName() 
    {
        if (localHostString == null) {
            try {
		localHostString = InetAddress.getLocalHost().getHostAddress();
            } catch (Exception ex) {
		throw wrapper.getLocalHostFailed( ex ) ;
            }
	}
	return localHostString ;
    }

 /******************************************************************************
 *  The following public methods are for ORB shutdown. 
 *
 ******************************************************************************/

    /** This method always returns false because the ORB never needs the
     *  main thread to do work.
     */
    public synchronized boolean work_pending()
    {
        checkShutdownState();
	throw wrapper.genericNoImpl() ;
    }
  
    /** This method does nothing. It is not required by the spec to do anything!
     */
    public synchronized void perform_work()
    {
        checkShutdownState();
	throw wrapper.genericNoImpl() ;
    }

    public synchronized void set_delegate(java.lang.Object servant){
        checkShutdownState();

	POAFactory poaFactory = getPOAFactory() ;
	if (poaFactory != null)
	    ((org.omg.PortableServer.Servant)servant)
		._set_delegate( poaFactory.getDelegateImpl() ) ;
	else
	    throw wrapper.noPoa() ;
    }

    public ClientInvocationInfo createOrIncrementInvocationInfo() 
    {
	ClientInvocationInfo clientInvocationInfo = null;
	try {
	    if (subcontractDebugFlag) {
		dprint(".createOrIncrementInvocationInfo->:");
	    }
	    StackImpl invocationInfoStack =
		(StackImpl) clientInvocationInfoStack.get();
	    if (!invocationInfoStack.empty()) {
		clientInvocationInfo =
		    (ClientInvocationInfo) invocationInfoStack.peek();
	    }
	    if ((clientInvocationInfo == null) || 
		(!clientInvocationInfo.isRetryInvocation()))
	    {
		// This is a new call - not a retry.
		clientInvocationInfo = new CorbaInvocationInfo(this);
		invocationInfoStack.push(clientInvocationInfo);
		if (subcontractDebugFlag) {
		    dprint(".createOrIncrementInvocationInfo: new call");
		}
	    } else {
		if (subcontractDebugFlag) {
		    dprint(".createOrIncrementInvocationInfo: retry");
		}
	    }
	    // Reset retry so recursive calls will get a new info object.
	    clientInvocationInfo.setIsRetryInvocation(false);
	    clientInvocationInfo.incrementEntryCount();
	} finally {
	    if (subcontractDebugFlag) {
		dprint(".createOrIncrementInvocationInfo<-: entryCount: "
		       + clientInvocationInfo.getEntryCount()
		       + " " + clientInvocationInfo);
	    }
	}
	return clientInvocationInfo;
    }
    
    public void releaseOrDecrementInvocationInfo() 
    {
	int entryCount = -1;
	ClientInvocationInfo clientInvocationInfo = null;
	try {
	    if (subcontractDebugFlag) {
		dprint(".releaseOrDecrementInvocationInfo->:");
	    }
	    StackImpl invocationInfoStack =
		(StackImpl)clientInvocationInfoStack.get();
	    if (!invocationInfoStack.empty()) {
		clientInvocationInfo =
		    (ClientInvocationInfo)invocationInfoStack.peek();
	    } else {
		throw wrapper.invocationInfoStackEmpty() ;
	    }
	    clientInvocationInfo.decrementEntryCount();
	    entryCount = clientInvocationInfo.getEntryCount();
	    if (clientInvocationInfo.getEntryCount() == 0 
                // 6763340: don't pop if this is a retry!
                && !clientInvocationInfo.isRetryInvocation()) {

		invocationInfoStack.pop();
		if (subcontractDebugFlag) {
		    dprint(".releaseOrDecrementInvocationInfo: pop");
		}
                // XXX Does this belong here?
                // finishedDispatch() ;
	    }
	} finally {
	    if (subcontractDebugFlag) {
		dprint(".releaseOrDecrementInvocationInfo<-: entry count: "
		       + entryCount
		       + " " + clientInvocationInfo);
	    }
	}
    }
    
    public ClientInvocationInfo getInvocationInfo() 
    {
	StackImpl invocationInfoStack =
	    (StackImpl) clientInvocationInfoStack.get();
	return (ClientInvocationInfo) invocationInfoStack.peek();
    }

    ////////////////////////////////////////////////////
    //
    //
    //

    private Object clientDelegateFactoryAccessorLock = new Object();

    public void setClientDelegateFactory( ClientDelegateFactory factory ) 
    {
	synchronized (clientDelegateFactoryAccessorLock) {
	    clientDelegateFactory = factory ;
	}
    }

    public ClientDelegateFactory getClientDelegateFactory() 
    {
	synchronized (clientDelegateFactoryAccessorLock) {
	    return clientDelegateFactory ;
	}
    }

    private ReentrantReadWriteLock 
          corbaContactInfoListFactoryAccessLock = new ReentrantReadWriteLock();
    private Lock corbaContactInfoListFactoryReadLock =
                               corbaContactInfoListFactoryAccessLock.readLock();
    private Lock corbaContactInfoListFactoryWriteLock = 
                              corbaContactInfoListFactoryAccessLock.writeLock();
    
    public void setCorbaContactInfoListFactory( CorbaContactInfoListFactory factory ) 
    {
	corbaContactInfoListFactoryWriteLock.lock() ;
        try {
	    corbaContactInfoListFactory = factory ;
        } finally {
            corbaContactInfoListFactoryWriteLock.unlock() ;
	}
    }

    public CorbaContactInfoListFactory getCorbaContactInfoListFactory() 
    {
        corbaContactInfoListFactoryReadLock.lock() ;
        try {
	    return corbaContactInfoListFactory ;
        } finally {
            corbaContactInfoListFactoryReadLock.unlock() ;
        }
    }

    /** Set the resolver used in this ORB.  This resolver will be used for list_initial_services
     * and resolve_initial_references.
     */
    public void setResolver( Resolver resolver ) 
    {
	synchronized (resolverLock) {
	    this.resolver = resolver ;
	}
    }

    /** Get the resolver used in this ORB.  This resolver will be used for list_initial_services
     * and resolve_initial_references.
     */
    public Resolver getResolver() 
    {
	synchronized (resolverLock) {
	    return resolver ;
	}
    }

    /** Set the LocalResolver used in this ORB.  This LocalResolver is used for 
     * register_initial_reference only.
     */
    public void setLocalResolver( LocalResolver resolver ) 
    {
	synchronized (resolverLock) {
	    this.localResolver = resolver ;
	}
    }

    /** Get the LocalResolver used in this ORB.  This LocalResolver is used for 
     * register_initial_reference only.
     */
    public LocalResolver getLocalResolver() 
    {
	synchronized (resolverLock) {
	    return localResolver ;
	}
    }

    /** Set the operation used in string_to_object calls.  The Operation must expect a
     * String and return an org.omg.CORBA.Object.
     */
    public void setURLOperation( Operation stringToObject ) 
    {
	synchronized (urlOperationLock) {
	    urlOperation = stringToObject ;
	}
    }

    /** Get the operation used in string_to_object calls.  The Operation must expect a
     * String and return an org.omg.CORBA.Object.
     */
    public Operation getURLOperation() 
    {
	synchronized (urlOperationLock) {
	    return urlOperation ;
	}
    }

    public void setINSDelegate( CorbaServerRequestDispatcher sdel )
    {
	synchronized (resolverLock) {
	    insNamingDelegate = sdel ;
	}
    }

    public TaggedComponentFactoryFinder getTaggedComponentFactoryFinder() 
    {
	return taggedComponentFactoryFinder ;
    }

    public IdentifiableFactoryFinder<TaggedProfile> getTaggedProfileFactoryFinder() 
    {
	return taggedProfileFactoryFinder ;
    }

    public IdentifiableFactoryFinder<TaggedProfileTemplate> getTaggedProfileTemplateFactoryFinder() 
    {
	return taggedProfileTemplateFactoryFinder ;
    }

    private Object objectKeyFactoryAccessLock = new Object();

    public ObjectKeyFactory getObjectKeyFactory() 
    {
	synchronized (objectKeyFactoryAccessLock) {
	    return objectKeyFactory ;
	}
    }

    public void setObjectKeyFactory( ObjectKeyFactory factory ) 
    {
	synchronized (objectKeyFactoryAccessLock) {
	    objectKeyFactory = factory ;
	}
    }

    public CorbaTransportManager getTransportManager()
    {
	return transportManager;
    }

    public CorbaTransportManager getCorbaTransportManager()
    {
	return (CorbaTransportManager) getTransportManager();
    }

    private Object legacyServerSocketManagerAccessLock = new Object();

    public LegacyServerSocketManager getLegacyServerSocketManager()
    {
	synchronized (legacyServerSocketManagerAccessLock) {
	    if (legacyServerSocketManager == null) {
		legacyServerSocketManager = new LegacyServerSocketManagerImpl(this);
	    }
	    return legacyServerSocketManager;
	}
    }

    private Object threadPoolManagerAccessLock = new Object();

    public void setThreadPoolManager(ThreadPoolManager mgr) 
    {
        // XXX Consider making it an error to set the thread pool manager more than once?
	synchronized (threadPoolManagerAccessLock) {
	    threadpoolMgr = mgr;
	}
    }

    public ThreadPoolManager getThreadPoolManager() 
    {
	synchronized (threadPoolManagerAccessLock) {
	    if (threadpoolMgr == null) {
		threadpoolMgr = new ThreadPoolManagerImpl();
                orbOwnsThreadPoolManager = true ;
	    }
	    return threadpoolMgr;
	}
    }

    public CopierManager getCopierManager()
    {
	return copierManager ;
    }

    public TimerManager<TimingPoints> getTimerManager() 
    {
	return timerManager ;
    }

    public IOR getIOR( org.omg.CORBA.Object obj, boolean connectIfNecessary ) {
	IOR result ;

	if (connectIfNecessary) {
	    try {
		result = getIOR( obj ) ;
	    } catch (BAD_OPERATION bop) {
		if (StubAdapter.isStub(obj)) {
		    try {
			StubAdapter.connect( obj, this ) ;
		    } catch (java.rmi.RemoteException exc) {
			throw wrapper.connectingServant( exc ) ;
		    }
		} else {
		    connect( obj ) ;
		}

		result = getIOR( obj ) ;
	    }
	} else {
	    // Let any exceptions propagate out
	    result = getIOR( obj ) ;
	}
    
	return result ;
    }
    
    public ObjectKeyCacheEntry extractObjectKeyCacheEntry(byte[] objKey) {
	if (objKey == null) 
	    throw wrapper.invalidObjectKey();

	ByteArrayWrapper newObjKeyWrapper = new ByteArrayWrapper(objKey);
	ObjectKeyCacheEntry entry = null ;

	synchronized (objectKeyCacheLock) {
	    entry = objectKeyCache.get(newObjKeyWrapper);
	    if (entry == null) {
		ObjectKey okey ;

		try {
		    okey = this.getObjectKeyFactory().create(objKey);
		} catch (Exception exc) {
		    throw wrapper.invalidObjectKey( exc );
		}

		entry = new ObjectKeyCacheEntryImpl( okey ) ;
		objectKeyCache.put(newObjKeyWrapper, entry); 	  
	    }
	}	  

	return entry ;
    }
    @Override
    public synchronized boolean orbIsShutdown() {
        return ((status == STATUS_DESTROYED) || 
            (status == STATUS_SHUTDOWN)) ;
    }
} // Class ORBImpl

////////////////////////////////////////////////////////////////////////
/// Helper class for a Synchronization Variable
////////////////////////////////////////////////////////////////////////

class SynchVariable 
{
    // Synchronization Variable
    public boolean _flag;

    // Constructor
    SynchVariable() 
    {
        _flag = false;
    }

    // set Flag to true
    public void set() 
    {
        _flag = true;
    }

        // get value
    public boolean value() 
    {
        return _flag;
    }

    // reset Flag to true
    public void reset() 
    {
        _flag = false;
    }
}

// End of file.


