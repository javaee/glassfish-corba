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

package com.sun.corba.se.spi.orb;

import java.util.Map ;
import java.util.HashMap ;
import java.util.Properties ;

import java.util.logging.Logger ;

import java.security.AccessController ;
import java.security.PrivilegedAction ;

import org.omg.CORBA.TCKind ;

import org.omg.CORBA.portable.ObjectImpl;

import com.sun.corba.se.pept.broker.Broker ;
import com.sun.corba.se.pept.transport.ByteBufferPool;
import com.sun.corba.se.pept.transport.ContactInfoList;

import com.sun.corba.se.spi.protocol.RequestDispatcherRegistry ;
import com.sun.corba.se.spi.protocol.ClientDelegateFactory ;
import com.sun.corba.se.spi.protocol.CorbaClientDelegate;
import com.sun.corba.se.spi.protocol.CorbaServerRequestDispatcher ;
import com.sun.corba.se.spi.protocol.PIHandler ;
import com.sun.corba.se.spi.resolver.LocalResolver ;
import com.sun.corba.se.spi.resolver.Resolver ;
import com.sun.corba.se.spi.transport.CorbaContactInfoListFactory ;
import com.sun.corba.se.spi.monitoring.MonitoringManager ;
import com.sun.corba.se.spi.legacy.connection.LegacyServerSocketManager;

import com.sun.corba.se.spi.ior.IdentifiableFactoryFinder ;
import com.sun.corba.se.spi.ior.TaggedComponentFactoryFinder ;
import com.sun.corba.se.spi.ior.ObjectKey ;
import com.sun.corba.se.spi.ior.ObjectKeyFactory ;
import com.sun.corba.se.spi.ior.IOR ;
import com.sun.corba.se.spi.ior.IORFactories ;
import com.sun.corba.se.spi.ior.TaggedProfile ;
import com.sun.corba.se.spi.ior.TaggedProfileTemplate ;

import com.sun.corba.se.spi.orbutil.threadpool.ThreadPoolManager;

import com.sun.corba.se.spi.oa.OAInvocationInfo ;
import com.sun.corba.se.spi.transport.CorbaTransportManager;

import com.sun.corba.se.spi.logging.LogWrapperFactory ;
import com.sun.corba.se.spi.logging.LogWrapperBase ;
import com.sun.corba.se.spi.logging.LogWrapperName ;

import com.sun.corba.se.spi.copyobject.CopierManager ;

import com.sun.corba.se.spi.presentation.rmi.PresentationManager ;
import com.sun.corba.se.spi.presentation.rmi.PresentationDefaults ;
import com.sun.corba.se.spi.presentation.rmi.InvocationInterceptor ;
import com.sun.corba.se.spi.presentation.rmi.StubAdapter ;

import com.sun.corba.se.spi.servicecontext.ServiceContextFactoryRegistry ;
import com.sun.corba.se.spi.servicecontext.ServiceContextsCache;

import com.sun.corba.se.spi.transport.CorbaContactInfoList;

import com.sun.corba.se.spi.orbutil.newtimer.TimerManager ;
import com.sun.corba.se.impl.orbutil.newtimer.generated.TimingPoints ;
import com.sun.corba.se.impl.orbutil.newtimer.generated.TimingPointsDisabledImpl ;
import com.sun.corba.se.impl.orbutil.newtimer.generated.TimingPointsEnabledImpl ;

// XXX needs an SPI or else it does not belong here
import com.sun.corba.se.impl.corba.TypeCodeImpl ;
import com.sun.corba.se.impl.corba.TypeCodeFactory ;

// XXX Should there be a SPI level constants ?
import com.sun.corba.se.spi.orbutil.ORBConstants ;

// XXX This goes away when we convert ORBD to ORT
import com.sun.corba.se.impl.oa.poa.BadServerIdHandler ;

// XXX Should add a factory method for this
import com.sun.corba.se.impl.ior.WireObjectKeyTemplate;

import com.sun.corba.se.impl.transport.ByteBufferPoolImpl;

import com.sun.corba.se.impl.logging.ORBUtilSystemException ;
import com.sun.corba.se.impl.logging.OMGSystemException ;
import com.sun.corba.se.impl.logging.LogWrapperTable ;
import com.sun.corba.se.impl.logging.LogWrapperTableImpl ;
import com.sun.corba.se.impl.logging.LogWrapperTableStaticImpl ;
import com.sun.corba.se.spi.orbutil.ORBClassLoader;
import com.sun.corba.se.spi.orbutil.generic.UnaryFunction;


public abstract class ORB extends com.sun.corba.se.org.omg.CORBA.ORB
    implements Broker, TypeCodeFactory
{   
    // As much as possible, this class should be stateless.  However,
    // there are a few reasons why it is not:
    //
    // 1. The ORB debug flags are defined here because they are accessed
    //    frequently, and we do not want a cast to the impl just for that.
    // 2. typeCodeMap and primitiveTypeCodeConstants are here because they
    //    are needed in both ORBImpl and ORBSingleton.
    // 3. Logging support is here so that we can avoid problems with
    //    incompletely initialized ORBs that need to perform logging.
    
    // Flag set at compile time to debug flag processing: this can't
    // be one of the xxxDebugFlags because it is used to debug the mechanism
    // that sets the xxxDebugFlags!
    public static boolean orbInitDebug = AccessController.doPrivileged( 
	new PrivilegedAction<Boolean>() {
	    public Boolean run() {
		return Boolean.getBoolean( ORBConstants.INIT_DEBUG_PROPERTY );
	    }
	}
    ) ;

    // Currently defined debug flags.  Any additions must be called xxxDebugFlag.
    // All debug flags must be public boolean types.
    // These are set by passing the flag -ORBDebug x,y,z in the ORB init args.
    // Note that x,y,z must not contain spaces.
    public boolean transportDebugFlag = false ;
    public boolean subcontractDebugFlag = false ;
    public boolean poaDebugFlag = false ;
    public boolean poaConcurrencyDebugFlag = false ;
    public boolean poaFSMDebugFlag = false ;
    public boolean orbdDebugFlag = false ;
    public boolean namingDebugFlag = false ;
    public boolean serviceContextDebugFlag = false ;
    public boolean transientObjectManagerDebugFlag = false ;
    public boolean giopVersionDebugFlag = false;
    public boolean shutdownDebugFlag = false;
    public boolean giopDebugFlag = false;
    public boolean giopSizeDebugFlag = false;
    public boolean giopReadDebugFlag = false;
    public boolean invocationTimingDebugFlag = false ;
    public boolean interceptorDebugFlag = false ;
    public boolean cdrCacheDebugFlag = false ;
    public boolean cdrDebugFlag = false ;
    public boolean streamFormatVersionDebugFlag = false ;
    public boolean valueHandlerDebugFlag = false ;
    public boolean orbLifecycleDebugFlag = true ;           // XXX temporary for testing; should normally be false
    
    private LogWrapperTable logWrapperTable ;

    private static LogWrapperTable staticLogWrapperTable = 
	new LogWrapperTableStaticImpl() ;

    // SystemException log wrappers.  Protected so that they can be used in
    // subclasses.
    protected ORBUtilSystemException wrapper ; 
    protected OMGSystemException omgWrapper ;

    // This map is needed for resolving recursive type code placeholders
    // based on the unique repository id.
    // XXX Should this be a WeakHashMap for GC?
    private Map<String,TypeCodeImpl> typeCodeMap ;

    private TypeCodeImpl[] primitiveTypeCodeConstants ;

    // ByteBufferPool - needed by both ORBImpl and ORBSingleton
    ByteBufferPool byteBufferPool;

    // Cached WireObjectKeyTemplate singleton.
    WireObjectKeyTemplate wireObjectKeyTemplate;

    // Local testing
    // XXX clean this up, probably remove these
    public abstract boolean isLocalHost( String hostName ) ;
    public abstract boolean isLocalServerId( int subcontractId, int serverId ) ;

    // Invocation stack manipulation
    public abstract OAInvocationInfo peekInvocationInfo() ;
    public abstract void pushInvocationInfo( OAInvocationInfo info ) ;
    public abstract OAInvocationInfo popInvocationInfo() ;

    public abstract CorbaTransportManager getCorbaTransportManager();
    public abstract LegacyServerSocketManager getLegacyServerSocketManager();

    // There is only one instance of the PresentationManager
    // that is shared between all ORBs.  This is necessary
    // because RMI-IIOP requires the PresentationManager in
    // places where no ORB is available, so the PresentationManager
    // must be global.  It is initialized here as well.
    private static final PresentationManager globalPM = 
	PresentationDefaults.makeOrbPresentationManager() ;

    public void destroy() {
        logWrapperTable = null ;
        wrapper = null ;
        omgWrapper = null ;
        typeCodeMap = null ;
        primitiveTypeCodeConstants = null ;
        byteBufferPool = null ;
        wireObjectKeyTemplate = null ;
    }

    /** Get the single instance of the PresentationManager
     */
    public static PresentationManager getPresentationManager() 
    {
	return globalPM ;
    }

    /** Get the appropriate StubFactoryFactory.  This 
     * will be dynamic or static depending on whether
     * com.sun.corba.se.ORBUseDynamicStub is true or false.
     */
    public static PresentationManager.StubFactoryFactory 
	getStubFactoryFactory()
    {
	boolean useDynamicStubs = globalPM.useDynamicStubs() ;
	return globalPM.getStubFactoryFactory( useDynamicStubs ) ;
    }

    /** Obtain the InvocationInterceptor for this ORB instance.
     * By default this does nothing.  XXX this would be a good 
     * place for the ORB timing system to gather data.
     */
    public abstract InvocationInterceptor getInvocationInterceptor() ;

    /** Set the InvocationInterceptor for this ORB instance.
     * This will be used around all dynamic RMI-IIOP calls that
     * are mediated by this ORB instance.
     */
    public abstract void setInvocationInterceptor( 
	InvocationInterceptor interceptor ) ;
    
    protected ORB()
    {
	logWrapperTable = new LogWrapperTableImpl( this ) ;

	// Initialize logging first, since it is needed everywhere 
	wrapper = logWrapperTable.get_RPC_PRESENTATION_ORBUtil() ;
	omgWrapper = logWrapperTable.get_RPC_PRESENTATION_OMG() ;

	typeCodeMap = new HashMap<String,TypeCodeImpl>();

	wireObjectKeyTemplate = new WireObjectKeyTemplate(this);
    }

    protected TimerManager<TimingPoints> makeTimerManager( String orbId ) {
	TimerManager<TimingPoints> timerManager = 
	    new TimerManager<TimingPoints>( orbId ) ;

	TimingPoints tp = null ;
	if ((getORBData() != null) && getORBData().timingPointsEnabled()) {
	    tp = new TimingPointsEnabledImpl( timerManager.factory(), 
		timerManager.controller() ) ;
	} else {
	    tp = new TimingPointsDisabledImpl( timerManager.factory(), 
		timerManager.controller() ) ;
	}

	timerManager.initialize( tp ) ;
	return timerManager ;
    }

    protected void initializePrimitiveTypeCodeConstants() {
	primitiveTypeCodeConstants = new TypeCodeImpl[] {
	    new TypeCodeImpl(this, TCKind._tk_null),	
	    new TypeCodeImpl(this, TCKind._tk_void),
	    new TypeCodeImpl(this, TCKind._tk_short),		
	    new TypeCodeImpl(this, TCKind._tk_long),	
	    new TypeCodeImpl(this, TCKind._tk_ushort),	
	    new TypeCodeImpl(this, TCKind._tk_ulong),	
	    new TypeCodeImpl(this, TCKind._tk_float),	
	    new TypeCodeImpl(this, TCKind._tk_double),	
	    new TypeCodeImpl(this, TCKind._tk_boolean),	
	    new TypeCodeImpl(this, TCKind._tk_char),	
	    new TypeCodeImpl(this, TCKind._tk_octet),
	    new TypeCodeImpl(this, TCKind._tk_any),	
	    new TypeCodeImpl(this, TCKind._tk_TypeCode),	
	    new TypeCodeImpl(this, TCKind._tk_Principal),
	    new TypeCodeImpl(this, TCKind._tk_objref),	
	    null,	// tk_struct    
	    null,	// tk_union     
	    null,	// tk_enum      
	    new TypeCodeImpl(this, TCKind._tk_string),		
	    null,	// tk_sequence  
	    null,	// tk_array     
	    null,	// tk_alias     
	    null,	// tk_except    
	    new TypeCodeImpl(this, TCKind._tk_longlong),	
	    new TypeCodeImpl(this, TCKind._tk_ulonglong),
	    new TypeCodeImpl(this, TCKind._tk_longdouble),
	    new TypeCodeImpl(this, TCKind._tk_wchar),		
	    new TypeCodeImpl(this, TCKind._tk_wstring),	
	    new TypeCodeImpl(this, TCKind._tk_fixed),	
	    new TypeCodeImpl(this, TCKind._tk_value),	
	    new TypeCodeImpl(this, TCKind._tk_value_box),
	    new TypeCodeImpl(this, TCKind._tk_native),	
	    new TypeCodeImpl(this, TCKind._tk_abstract_interface)
	} ;
    }

    // Typecode support: needed in both ORBImpl and ORBSingleton
    public TypeCodeImpl get_primitive_tc(int kind) 
    {
	try {
	    return primitiveTypeCodeConstants[kind] ;
	} catch (Throwable t) {
	    throw wrapper.invalidTypecodeKind( t, Integer.valueOf(kind) ) ;
	}
    }

    public synchronized void setTypeCode(String id, TypeCodeImpl code) 
    {
        typeCodeMap.put(id, code);
    }

    public synchronized TypeCodeImpl getTypeCode(String id) 
    {
        return typeCodeMap.get(id);
    }

    public abstract MonitoringManager getMonitoringManager( ) ;
    
    // Special non-standard set_parameters method for
    // creating a precisely controlled ORB instance.
    // An ORB created by this call is affected only by
    // those properties passes explicitly in props, not by
    // the system properties and orb.properties files as
    // with the standard ORB.init methods.
    public abstract void set_parameters( Properties props ) ;

    // Added to provide an API for creating an ORB that avoids the org.omg.CORBA.ORB API
    // to get around an OSGi problem.
    public abstract void setParameters( String[] args, Properties props ) ;

    // ORB versioning
    public abstract ORBVersion getORBVersion() ;
    public abstract void setORBVersion( ORBVersion version ) ;

    // XXX This needs a better name
    public abstract IOR getFVDCodeBaseIOR() ;

    /**
     * Handle a bad server id for the given object key.  This should 
     * always through an exception: either a ForwardException to
     * allow another server to handle the request, or else an error
     * indication.  XXX Remove after ORT for ORBD work is integrated.
     */
    public abstract void handleBadServerId( ObjectKey okey ) ;
    public abstract void setBadServerIdHandler( BadServerIdHandler handler ) ;
    public abstract void initBadServerIdHandler() ;
    
    public abstract void notifyORB() ;

    public abstract PIHandler getPIHandler() ;

    public abstract void checkShutdownState();

    // Dispatch support: in the ORB because it is needed for shutdown.
    // This is used by the first level server side subcontract.
    public abstract boolean isDuringDispatch() ;
    public abstract void startingDispatch();
    public abstract void finishedDispatch();

    /** Return this ORB's transient server ID.  This is needed for 
     * initializing object adapters.
     */
    public abstract int getTransientServerId();

    public abstract ServiceContextFactoryRegistry getServiceContextFactoryRegistry() ;

    public abstract ServiceContextsCache getServiceContextsCache();

    public abstract RequestDispatcherRegistry getRequestDispatcherRegistry();

    public abstract ORBData getORBData() ;

    public abstract void setClientDelegateFactory( ClientDelegateFactory factory ) ;

    public abstract ClientDelegateFactory getClientDelegateFactory() ;

    public abstract void setCorbaContactInfoListFactory( CorbaContactInfoListFactory factory ) ;

    public abstract CorbaContactInfoListFactory getCorbaContactInfoListFactory() ;

    // XXX These next 7 methods should be moved to a ResolverManager.

    /** Set the resolver used in this ORB.  This resolver will be used for list_initial_services
     * and resolve_initial_references.
     */
    public abstract void setResolver( Resolver resolver ) ;

    /** Get the resolver used in this ORB.  This resolver will be used for list_initial_services
     * and resolve_initial_references.
     */
    public abstract Resolver getResolver() ;

    /** Set the LocalResolver used in this ORB.  This LocalResolver is used for 
     * register_initial_reference only.
     */
    public abstract void setLocalResolver( LocalResolver resolver ) ;

    /** Get the LocalResolver used in this ORB.  This LocalResolver is used for 
     * register_initial_reference only.
     */
    public abstract LocalResolver getLocalResolver() ;

    /** Set the operation used in string_to_object calls.  The Operation must expect a
     * String and return an org.omg.CORBA.Object.
     */
    public abstract void setURLOperation( Operation stringToObject ) ;

    /** Get the operation used in string_to_object calls.  The Operation must expect a
     * String and return an org.omg.CORBA.Object.
     */
    public abstract Operation getURLOperation() ;

    /** Set the ServerRequestDispatcher that should be used for handling INS requests.
     */
    public abstract void setINSDelegate( CorbaServerRequestDispatcher insDelegate ) ;

    // XXX The next 5 operations should be moved to an IORManager.

    /** Factory finders for the various parts of the IOR: tagged components, tagged
     * profiles, and tagged profile templates.
     */
    public abstract TaggedComponentFactoryFinder getTaggedComponentFactoryFinder() ;
    public abstract IdentifiableFactoryFinder<TaggedProfile> 
	getTaggedProfileFactoryFinder() ;
    public abstract IdentifiableFactoryFinder<TaggedProfileTemplate> 
	getTaggedProfileTemplateFactoryFinder() ;

    public abstract ObjectKeyFactory getObjectKeyFactory() ;
    public abstract void setObjectKeyFactory( ObjectKeyFactory factory ) ;

    // Logging SPI

    public static Logger getLogger( String name ) 
    {
	return Logger.getLogger( name, ORBConstants.LOG_RESOURCE_FILE ) ;
    }

    /** get the log wrapper class (its type is dependent on the exceptionGroup)
     * for the given log domain and exception group in this ORB instance.
     */
    public LogWrapperBase getLogWrapper( String logDomain, 
        String exceptionGroup, LogWrapperFactory factory ) 
    {
	return factory.create( LogWrapperName.getLoggerName( this, logDomain )) ;
    }

    /** get the log wrapper class (its type is dependent on the exceptionGroup)
     * for the given log domain and exception group in this ORB instance.
     */
    public static LogWrapperBase staticGetLogWrapper( String logDomain, 
        String exceptionGroup, LogWrapperFactory factory ) 
    {
	return factory.create( LogWrapperName.getLoggerName( logDomain )) ;
    }

    public LogWrapperTable getLogWrapperTable() {
	return logWrapperTable ;
    }

    public static LogWrapperTable getStaticLogWrapperTable() {
	return staticLogWrapperTable ;
    }

    // get a reference to a ByteBufferPool, a pool of NIO ByteBuffers
    // NOTE: ByteBuffer pool must be unique per ORB, not per process.
    //       There can be more than one ORB per process.
    //       This method must also be inherited by both ORB and ORBSingleton.
    public ByteBufferPool getByteBufferPool()
    {
        if (byteBufferPool == null)
            byteBufferPool = new ByteBufferPoolImpl(this);

        return byteBufferPool;
    }

    public WireObjectKeyTemplate getWireObjectKeyTemplate() {
	return wireObjectKeyTemplate;
    }

    public abstract void setThreadPoolManager(ThreadPoolManager mgr);

    public abstract ThreadPoolManager getThreadPoolManager();

    public abstract CopierManager getCopierManager() ;

    /** Returns a name for this ORB that is based on the ORB id (if any)
     * and guaranteed to be unique within the ClassLoader that loaded the
     * ORB class.  This is the default implementation inherited by the
     * ORBSingleton.
     */
    public String getUniqueOrbId() {
	return "###DEFAULT_UNIQUE_ORB_ID###" ;
    }

    /** Return the ORB's TimerManager.
     */
    public abstract TimerManager<TimingPoints> getTimerManager() ;
    
    // This method obtains an IOR from a CORBA object reference.
    // The result is never null.
    // Throws BAD_OPERATION (from oi._get_delegate) if obj is a
    // normal objref, but does not have a delegate set.
    // Throws BAD_PARAM if obj is a local object
    protected IOR getIOR( org.omg.CORBA.Object obj ) 
    {
	if (obj == null)
	    throw wrapper.nullObjectReference() ;

	IOR ior = null ;
	if (StubAdapter.isStub(obj)) {
	    org.omg.CORBA.portable.Delegate del = StubAdapter.getDelegate( 
		obj ) ;

	    if (del instanceof CorbaClientDelegate) {
		CorbaClientDelegate cdel = (CorbaClientDelegate)del ;
		ContactInfoList cil = cdel.getContactInfoList() ;

		if (cil instanceof CorbaContactInfoList) {
		    CorbaContactInfoList ccil = (CorbaContactInfoList)cil ;
		    ior = ccil.getTargetIOR() ;
		    if (ior == null)
			throw wrapper.nullIor() ;

		    return ior ;
		} else {
		    // This is our code, but the ContactInfoList is not a 
		    // CorbaContactInfoList.  This should not happen, because
		    // we are in the CORBA application of the PEPt framework.
		    // This is a coding error, and thus an INTERNAL exception
		    // should be thrown.
		    throw wrapper.badTypeInDelegate() ;
		}
	    } 

	    if (obj instanceof ObjectImpl) {
		// Get the ORB instance of obj so we can use that ORB
		// to marshal the object.
		ObjectImpl oi = ObjectImpl.class.cast( obj ) ;
		org.omg.CORBA.ORB oiorb = oi._orb() ;

		// obj is implemented by a foreign ORB, because the Delegate is not a
		// CorbaClientDelegate.  Here we need to marshal obj to an output stream,
		// then read the IOR back in.  Note that the output stream MUST be
		// created by the ORB to which obj is attached, otherwise we get an
		// infinite recursion between this code and 
		// CDROutputStream_1_0.write_Object.
		org.omg.CORBA.portable.OutputStream os = oiorb.create_output_stream() ;
		os.write_Object( obj ) ;
		org.omg.CORBA.portable.InputStream is = os.create_input_stream() ;
		ior = IORFactories.makeIOR( this,  
		    org.omg.CORBA_2_3.portable.InputStream.class.cast( is ) ) ; 
		return ior ;
	    } else {
		throw wrapper.notAnObjectImpl() ;
	    }
	} else
	    throw wrapper.localObjectNotAllowed() ;
    }


    /** Get the IOR for the CORBA object.  If the object is an RMI-IIOP object that
     * is not connected, and connectIfNecessary is true, connect to this ORB.
     * This method will obtain an IOR for any non-local CORBA object, regardless of
     * what ORB implementation created it.  It may be more efficient for objrefs
     * that were created by this ORB implementation.
     *
     * @exception SystemException (nullObjectReference) if obj is null
     * @exception SystemException (localObjectNotAllowed) of obj is a local CORBA object.
     */
    public IOR getIOR( org.omg.CORBA.Object obj, boolean connectIfNecessary ) {
	// Note: this version ignores connectIfNecessary, since an objref can only
	// be connected to an ORBImpl, not an ORBSingleton.
	return getIOR( obj ) ;
    }

    /** The singleton ORB does not need the cache, so just return null here.
     */
    public ObjectKeyCacheEntry extractObjectKeyCacheEntry(byte[] objKey) {
	return null ;
    }

    /** Return whether or not the ORB is shutdown.  A shutdown ORB cannot process
     * incoming requests.
     */
    public boolean orbIsShutdown() {
        return true ;
    }

    private static UnaryFunction<String,Class<?>> classNameResolver =
        new UnaryFunction<String,Class<?>>() {
            public Class<?> evaluate( String name ) {
                try {
                    return ORBClassLoader.getClassLoader().loadClass( name ) ;
                } catch (ClassNotFoundException exc) {
                    throw new RuntimeException( exc ) ;
                }
            }

            public String toString() {
                return "ORBClassNameResolver" ;
            }
        } ;

    public static UnaryFunction<String,Class<?>> defaultClassNameResolver() {
        return classNameResolver ;
    }

    public UnaryFunction<String,Class<?>> makeCompositeClassNameResolver(
        final UnaryFunction<String,Class<?>> first,
        final UnaryFunction<String,Class<?>> second ) {

        return new UnaryFunction<String,Class<?>>() {
            public Class<?> evaluate( String className ) {
                Class<?> result = first.evaluate( className ) ;
                if (result == null) {
                    return second.evaluate( className ) ;
                } else {
                    return result ;
                }
            }

            public String toString() {
                return "CompositeClassNameResolver[" + first + "," + second + "]" ;
            }
        } ;
    }

    public UnaryFunction<String,Class<?>> classNameResolver() {
        return classNameResolver ;
    }

    public void classNameResolver( UnaryFunction<String,Class<?>> arg ) {
        classNameResolver = arg ;
    }
}

// End of file.
