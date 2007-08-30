/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2007 Sun Microsystems, Inc. All rights reserved.
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

package performance.eesample;

import java.rmi.RemoteException ;
import java.rmi.Remote ;
import java.io.PrintStream ;

import java.rmi.Remote ;
import java.rmi.RemoteException ;

import java.util.Arrays ;
import java.util.List ;
import java.util.ArrayList ;
import java.util.Map ;
import java.util.HashMap ;
import java.util.Properties ;

import javax.rmi.PortableRemoteObject ;

import javax.rmi.CORBA.Tie ;

import org.omg.CORBA.Policy ;
import org.omg.CORBA.BAD_OPERATION ;
import org.omg.CORBA.LocalObject ;

import org.omg.CosNaming.NamingContext ;
import org.omg.CosNaming.NamingContextHelper ;
import org.omg.CosNaming.NamingContextExt ;
import org.omg.CosNaming.NamingContextExtHelper ;
import org.omg.CosNaming.NameComponent ;
import org.omg.CosNaming.NamingContextPackage.CannotProceed ;
import org.omg.CosNaming.NamingContextPackage.InvalidName ;
import org.omg.CosNaming.NamingContextPackage.AlreadyBound ;
import org.omg.CosNaming.NamingContextPackage.NotFound ;

import org.omg.PortableServer.ForwardRequest ;
import org.omg.PortableServer.POA ;
import org.omg.PortableServer.Servant ;
import org.omg.PortableServer.ServantLocator ;

import org.omg.PortableServer.ServantLocatorPackage.CookieHolder ;

import com.sun.corba.se.spi.orb.ORB ;

import com.sun.corba.se.spi.presentation.rmi.PresentationManager ;

import com.sun.corba.se.spi.oa.rfm.ReferenceFactoryManager ;
import com.sun.corba.se.spi.oa.rfm.ReferenceFactory ;

import com.sun.corba.se.spi.orbutil.generic.Pair ;

import com.sun.corba.se.impl.orbutil.ORBConstants ;

import com.sun.corba.se.impl.naming.cosnaming.TransientNameService ;
import com.sun.corba.se.spi.extension.ServantCachingPolicy ;

import com.sun.corba.se.spi.copyobject.CopyobjectDefaults ;

import com.sun.corba.se.spi.orbutil.copyobject.ReflectiveCopyException ;
import com.sun.corba.se.spi.orbutil.copyobject.ObjectCopier ;
import com.sun.corba.se.spi.orbutil.copyobject.ObjectCopierFactory ;

import com.sun.corba.se.spi.orbutil.argparser.DefaultValue ;
import com.sun.corba.se.spi.orbutil.argparser.ArgParser ;

/** Standard top-level ORB test.  Does the following:
 *<OL>
 *<LI>Tests CDR stream marshalling (for a copy) for a trivial call (long as arg) and
 *for more complex data (ArrayList of simple instances)
 *<LI>Tests inter ORB call with trivial data (long arg and result) and returning
 *complex data (same ArrayList).  The ORB instances are in the same VM, so loopback
 *TCP is used.
 *</OL>
 *These tests are designed to be easily profiled.
 *
 * @author Ken Cavanaugh
 */
public class StandardTest {
    static {
	// The following must be set as system properties 
	System.setProperty( "javax.rmi.CORBA.PortableRemoteObjectClass",
	    "com.sun.corba.se.impl.javax.rmi.PortableRemoteObject" ) ;
	System.setProperty( "javax.rmi.CORBA.StubClass",
	    "com.sun.corba.se.impl.javax.rmi.CORBA.StubDelegateImpl" ) ;
	System.setProperty( "javax.rmi.CORBA.UtilClass",
	    "com.sun.corba.se.impl.javax.rmi.CORBA.Util" ) ;
	System.setProperty( ORBConstants.USE_DYNAMIC_STUB_PROPERTY,
	    "true" ) ;
    }

    public interface Test extends Remote {
	public int echo( int value ) throws RemoteException ;
        
        public List<SampleData.Data> getData() throws RemoteException ;

	public SampleData.Data[] getDataArray() throws RemoteException ;

	public List<Test> getTestRefs() throws RemoteException ;

	public void done() throws RemoteException ;
    }

    public static class TestImpl extends PortableRemoteObject implements Test {
        SampleData sd = new SampleData() ;
        List<SampleData.Data> data = sd.parse( SampleData.bankData ) ;
        SampleData.Data[] dataArray = null ;
	List<Test> testRefList = null ;

	public TestImpl() throws RemoteException {
	    super() ;
	    dataArray = new SampleData.Data[data.size()] ;
	    int ctr=0 ;
	    for (SampleData.Data d : data) 
		dataArray[ctr++] = d ;
	}

	public int echo( int value ) throws RemoteException {
	    return value ;
	}
        
        public List<SampleData.Data> getData() throws RemoteException {
            return data ;
        }

	public SampleData.Data[] getDataArray() throws RemoteException {
	    return dataArray ;
	}

	public List<Test> getTestRefs() throws RemoteException {
	    return testRefList ;
	}

	public void done() throws RemoteException {
	    System.exit(0) ;
	}

	public void setRef( Test test ) {
	    testRefList = new ArrayList<Test>() ;
	    for (int ctr=0; ctr<data.size(); ctr++) 
		testRefList.add( test ) ;
	}
    }

    public class TestServantLocator extends LocalObject
	implements ServantLocator {
	private Servant servant ;
	private TestImpl impl = null; ;

	public TestServantLocator( ORB orb ) {
	    try {
		impl = new TestImpl() ;
	    } catch (Exception exc) {
		fatal( "Exception in creating servant: " + exc, exc ) ;
	    }

	    Tie tie = com.sun.corba.se.spi.orb.ORB.class.cast( orb )
		.getPresentationManager().getTie() ;
	    tie.setTarget( impl ) ;
	    servant = Servant.class.cast( tie ) ;
	}

	public synchronized Servant preinvoke( byte[] oid, POA adapter,
	    String operation, CookieHolder the_cookie 
	) throws ForwardRequest {
	    return servant ;
	}

	public void postinvoke( byte[] oid, POA adapter,
	    String operation, Object the_cookie, Servant the_servant ) {
	}

	public void setRef( Test test ) {
	    impl.setRef( test ) ;
	}
    }

    private ORB clientORB = null ;
    private ORB serverORB = null ;
    private NamingContextExt clientNamingRoot = null ;
    private NamingContextExt serverNamingRoot = null ;
    private ArgumentData data ;

    public StandardTest( ArgumentData data ) {
	this.data = data ;
    }

    public synchronized void log( String msg ) {
	System.out.println( msg ) ;
    }

    public synchronized void fatal( String msg, Throwable thr ) {
	thr.printStackTrace() ;
	log( msg ) ;
	System.exit( 1 ) ;
    }

    private void bindName( NamingContext ctx, String sname,
	org.omg.CORBA.Object objref )
	throws NotFound, CannotProceed, AlreadyBound, InvalidName 
    {
	NameComponent[] name = serverNamingRoot.to_name( sname ) ;
	NamingContext current = ctx ;
	for (int ctr=0; ctr<name.length; ctr++) {
	    NameComponent[] arr = new NameComponent[] { name[ctr] } ;

	    if (ctr < name.length - 1) {
		try {
		    org.omg.CORBA.Object ref = current.resolve( arr ) ;
		    if (ref._is_a(NamingContextHelper.id()))
			current = NamingContextHelper.narrow( ref ) ;
		    else
			throw new BAD_OPERATION( 
			    "Name is bound to a non-context object reference" ) ;
		} catch (NotFound exc) {
		    current = current.bind_new_context( arr ) ;
		}
	    } else {
		current.bind( arr, objref ) ; 
	    }
	}
    }
   
    private Object copy( Object obj ) throws ReflectiveCopyException {
	ObjectCopierFactory ocf = 
	    CopyobjectDefaults.makeORBStreamObjectCopierFactory( clientORB ) ;
	ObjectCopier oc = ocf.make() ;
	return oc.copy( obj ) ;
    }

    private void performIntCopy() {
	try {
	    long value = 0 ;

	    int warmup = data.warmup() ;
	    for (int i = 0; i < warmup; i++) {
		int val = (Integer)copy( i ) ;
		value += val ;
	    }

	    long time = System.nanoTime() ;

	    int count = data.count() ;
	    for (int i = 0; i < count; i++) {
		int val = (Integer)copy( i ) ;
		value += val ;
	    }
	    
	    double elapsed = System.nanoTime() - time ;

	    log( "Int copy elapsed time: " + (elapsed/count)/1000 
		+ " microseconds" ) ;
	} catch (Exception rex) {
	    fatal( "Int copy test: error in test: ", rex ) ;
	    rex.printStackTrace() ;
	}
    }

    private void performDataCopy() {
	try {
	    SampleData sd = new SampleData() ;
	    List<SampleData.Data> value = sd.parse( SampleData.bankData ) ;

	    int warmup = data.warmup() ;
	    for (int i = 0; i < warmup; i++) {
		Object data = copy( value ) ;
	    }

	    long time = System.nanoTime() ;

	    int count = data.count() ;
	    for (int i = 0; i < count; i++) {
		Object data = copy( value ) ;
	    }
	    
	    double elapsed = System.nanoTime() - time ;

	    log( "Data copy elapsed time: " + (elapsed/count)/1000 
		+ " microseconds" ) ;
	} catch (Exception rex) {
	    fatal( "Data copy test: error in test: ", rex ) ;
	    rex.printStackTrace() ;
	}
    }

    private void performEcho( Test testRef ) {
	try {
	    long value = 0 ;

	    int warmup = data.warmup() ;
	    for (int i = 0; i < warmup; i++) {
		value += testRef.echo(i);
	    }

	    long time = System.nanoTime() ;

	    int count = data.count() ;
	    for (int i = 0; i < count; i++) {
		value += testRef.echo(i);
	    }
	    
	    double elapsed = System.nanoTime() - time ;

	    log( "Echo Test " + getTransportDescription() + " : " + (elapsed/count)/1000 
		+ " microseconds" ) ;
	} catch (RemoteException rex) {
	    fatal( "Echo test " + getTransportDescription() + ": error in test: ", rex ) ;
	    rex.printStackTrace() ;
	}
    }
    
    private void performGetData( Test testRef ) {
	try {
	    List<SampleData.Data> value ;

	    int warmup = data.warmup() ;
	    for (int i = 0; i < warmup; i++) {
		value = testRef.getData();
	    }

	    long time = System.nanoTime() ;

	    int count = data.count() ;
	    for (int i = 0; i < count; i++) {
		value = testRef.getData();
	    }
	    
	    double elapsed = System.nanoTime() - time ;

	    log( "GetData Test " + getTransportDescription() + " : " 
		+ (elapsed/count)/1000 + " microseconds" ) ;
	} catch (RemoteException rex) {
	    fatal( "GetData test " + getTransportDescription() + ": error in test: ", 
		rex ) ;
	    rex.printStackTrace() ;
	}
    }

    private void performGetDataArray( Test testRef ) {
	try {
	    SampleData.Data[] value ;

	    int warmup = data.warmup() ;
	    for (int i = 0; i < warmup; i++) {
		value = testRef.getDataArray();
	    }

	    long time = System.nanoTime() ;

	    int count = data.count() ;
	    for (int i = 0; i < count; i++) {
		value = testRef.getDataArray();
	    }
	    
	    double elapsed = System.nanoTime() - time ;

	    log( "GetDataArray Test " + getTransportDescription() + " : " 
		+ (elapsed/count)/1000 + " microseconds" ) ;
	} catch (RemoteException rex) {
	    fatal( "GetDataArray test " + getTransportDescription() + ": error in test: ", 
		rex ) ;
	    rex.printStackTrace() ;
	}
    }

    private void performGetTestRefs( Test testRef ) {
	try {
	    List<Test> value ;

	    int warmup = data.warmup() ;
	    for (int i = 0; i < warmup; i++) {
		value = testRef.getTestRefs();
	    }

	    long time = System.nanoTime() ;

	    int count = data.count() ;
	    for (int i = 0; i < count; i++) {
		value = testRef.getTestRefs();
	    }
	    
	    double elapsed = System.nanoTime() - time ;

	    log( "GetTestRefs Test " + getTransportDescription() + " : " 
		+ (elapsed/count)/1000 + " microseconds" ) ;
	} catch (RemoteException rex) {
	    fatal( "GetTestRefs test " + getTransportDescription() + ": error in test: ", 
		rex ) ;
	    rex.printStackTrace() ;
	}
    }

    private Properties getBaseProps() {
	// initializer client and server ORBs.
	// Initialize server using RFM and register objrefs in naming
	Properties baseProps = new Properties() ;
	baseProps.setProperty( "org.omg.CORBA.ORBSingletonClass",
	    "com.sun.corba.se.impl.orb.ORBSingleton" ) ;
	baseProps.setProperty( "org.omg.CORBA.ORBClass",
	    "com.sun.corba.se.impl.orb.ORBImpl" ) ;
	baseProps.setProperty( ORBConstants.INITIAL_HOST_PROPERTY,
	    data.hostName() ) ;
	baseProps.setProperty( ORBConstants.INITIAL_PORT_PROPERTY,
	    Integer.toString( data.port() ) ) ;
	baseProps.setProperty( ORBConstants.GIOP_FRAGMENT_SIZE,
	    Integer.toString( data.fragmentSize() ) ) ;
	baseProps.setProperty( ORBConstants.USE_NIO_SELECT_TO_WAIT_PROPERTY,
	    Boolean.toString( !data.blocking() ) ) ;
    
	// baseProps.setProperty( ORBConstants.ALLOW_LOCAL_OPTIMIZATION,
	    // "true" ) ;
	// For debugging only
	// baseProps.setProperty( ORBConstants.DEBUG_PROPERTY,
	    // "transport,subcontract" ) ;
	
	return baseProps ;
    }

    // Override this method to create test cases with different ORB properties
    protected Properties getClientProperties() {
	Properties baseProps = getBaseProps() ;
	Properties clientProps = new Properties( baseProps ) ;
	clientProps.setProperty( ORBConstants.ORB_ID_PROPERTY,
	    "clientORB" ) ;
	return clientProps ;
    }

    // Override this method to create test cases with different ORB properties
    protected Properties getServerProperties() {
	Properties baseProps = getBaseProps() ;
	Properties serverProps = new Properties( baseProps ) ;
	serverProps.setProperty( ORBConstants.PERSISTENT_SERVER_PORT_PROPERTY,
	    Integer.toString( data.port() ) ) ;
	serverProps.setProperty( ORBConstants.SERVER_HOST_PROPERTY,
	    data.hostName() ) ;
	serverProps.setProperty( ORBConstants.ORB_ID_PROPERTY,
	    "serverORB" ) ;
	serverProps.setProperty( ORBConstants.ORB_SERVER_ID_PROPERTY,
	    "300" ) ;
	serverProps.setProperty( ORBConstants.RFM_PROPERTY,
	    "1" ) ;
	return serverProps ;
    }

    private void setDebugFlags( ORB orb ) {
	if (data.getSize())
	    orb.giopSizeDebugFlag = true ;
	if (data.getRead())
	    orb.giopReadDebugFlag = true ;
    }

    // Test must call this method for initialization
    protected void initializeClientORB() {
	try {
	    String[] myArgs = {} ;

	    Properties clientProps = getClientProperties() ;
	    clientORB = (ORB)ORB.init( myArgs, clientProps ) ;
        	    setDebugFlags( clientORB ) ;
	    if (data.getSize())
		clientORB.giopSizeDebugFlag = true ;
	    clientNamingRoot = NamingContextExtHelper.narrow(
		clientORB.resolve_initial_references( "NameService" )) ;
	} catch (Exception exc) {
	    fatal( "Exception in client initialization: " + exc, exc ) ;
	}
    }

    // Test must call this method to clean up the ORBs used in the test
    private void cleanUpClient() {
	log( "Shutting down clientORB" ) ;
	clientORB.shutdown( true ) ;
	log( "Destroying clientORB" ) ;
	clientORB.destroy() ;
    }

    private static Object[][] objrefData = {
	{ "testref/cache", true }
	// { "testref/nocache", false }
    } ;
   
    protected void initializeServer() {
	String[] myArgs = {} ;

        Properties serverProps = getServerProperties() ;
	serverORB = (ORB)ORB.init( myArgs, serverProps ) ;
    	setDebugFlags( serverORB ) ;
	if (data.getSize())
	    serverORB.giopSizeDebugFlag = true ;
	new TransientNameService( serverORB ) ;
	
	// Get the RFM and naming service
	ReferenceFactoryManager rfm = null ;

	try {
	    rfm = ReferenceFactoryManager.class.cast( 
		serverORB.resolve_initial_references( "ReferenceFactoryManager" )) ;
	    rfm.activate() ;
	    serverNamingRoot = NamingContextExtHelper.narrow(
		serverORB.resolve_initial_references( "NameService" )) ;
	} catch (Exception exc) {
	    fatal( "Exception in getting initial references: " + exc, exc ) ;
	}

	TestServantLocator locator = new TestServantLocator( serverORB ) ;
	PresentationManager pm = ORB.getPresentationManager() ;

	String repositoryId ;
	try {
	    repositoryId = pm.getRepositoryId( new TestImpl() ) ;
	} catch (Exception exc) {
	    throw new RuntimeException( exc ) ;
	}

	String nocacheFactoryName = "nocache" ;
	String cacheFactoryName = "cache" ;

	List<Policy> nocacheFactoryPolicies = null ;
	ReferenceFactory nocacheFactory = rfm.create( nocacheFactoryName,
	    repositoryId, nocacheFactoryPolicies, locator ) ;

	List<Policy> cacheFactoryPolicies = Arrays.asList( 
	    (Policy)ServantCachingPolicy.getPolicy() ) ;
	ReferenceFactory cacheFactory = rfm.create( cacheFactoryName, 
	    repositoryId, cacheFactoryPolicies, locator ) ;

	// Use a ReferenceFactory to create objref and register it with naming
	for ( Object[] data : objrefData) {
	    String sname = String.class.cast( data[0] ) ;
	    boolean useCaching = Boolean.class.cast( data[1] ) ;
	    ReferenceFactory factory = useCaching ? cacheFactory : nocacheFactory ;
	    byte[] oid = new byte[] { (byte)0, (byte)1, (byte)2 } ;
	    org.omg.CORBA.Object objref = factory.createReference( oid ) ; 
	    Test ref = Test.class.cast( PortableRemoteObject.narrow( objref,
		Test.class )) ;
	    locator.setRef( ref ) ;
	    try {
		bindName( serverNamingRoot, sname, objref ) ;
	    } catch (Exception exc) {
		fatal( "Error in initializeServer: " + exc, exc ) ;
	    }
	}
    }

    public void serverWait() {
	serverORB.run() ;
    }

    private void cleanUpServer() {
	log( "Shutting down serverORB" ) ;
	serverORB.shutdown( true ) ;
	log( "Destroying serverORB" ) ;
	serverORB.destroy() ;
    }

    private Test getTestRef( String name ) {
	try {
	    Test ref = Test.class.cast( PortableRemoteObject.narrow( 
		clientNamingRoot.resolve_str( name ), Test.class )) ;
	    return ref ;
	} catch (Exception exc) {
	    fatal( "Could not get object reference: " + exc, exc ) ;
	}
        return null ; // not reachable
    }

    public void testNullStreamCopy() {
	performIntCopy() ;
    }
    
    public void testDataStreamCopy() {
	performDataCopy() ;
    }
    
    public void testNullCall() {
	for ( Object[] data : objrefData) {
	    String oname = (String)data[0] ;
	    Test test = getTestRef( oname ) ;
	    performEcho( test ) ;
	}
    }
   
    public void testDataCall() {
	for ( Object[] data : objrefData) {
	    String oname = (String)data[0] ;
	    Test test = getTestRef( oname ) ;
	    performGetData( test ) ;
	}
    }
    
    public void testDataArrayCall() {
	for ( Object[] data : objrefData) {
	    String oname = (String)data[0] ;
	    Test test = getTestRef( oname ) ;
	    performGetDataArray( test ) ;
	}
    }
    
    public void testGetTestRefsCall() {
	for ( Object[] data : objrefData) {
	    String oname = (String)data[0] ;
	    Test test = getTestRef( oname ) ;
	    performGetTestRefs( test ) ;
	}
    }
    
    public void run() {
	try {
	    initializeClientORB() ;

	    if (data.doCopyTests()) {
		testNullStreamCopy() ;
		testDataStreamCopy() ;
	    } 

	    if (data.testNullCall())
		testNullCall() ;
	    if (data.testDataCall())
		testDataCall() ;
	    if (data.testDataArrayCall())
		testDataArrayCall() ;
	    if (data.testGetTestRefsCall()) 
		testGetTestRefsCall() ;

	    cleanUpClient() ;
	    log( "Test Complete." ) ;
	} catch (Throwable thr) {
	    fatal( "Test FAILED: Caught throwable " + thr, thr ) ;
	}
    }

    public enum TestMode { LOCAL, CLIENT, SERVER } 

    public interface ArgumentData {
	@DefaultValue( "LOCAL" ) 
	TestMode mode() ;

	@DefaultValue( "3700" ) 
	int port() ;

	@DefaultValue( "localhost" ) 
	String hostName() ;

	@DefaultValue( "1024" ) 
	int fragmentSize() ;

	@DefaultValue( "false" ) 
	boolean blocking() ;

	@DefaultValue( "500" ) 
	int warmup() ;

	@DefaultValue( "500" ) 
	int count() ;

	@DefaultValue( "true" )
	boolean doCopyTests() ;

	@DefaultValue( "false" )
	boolean testNullCall() ;

	@DefaultValue( "false" )
	boolean testDataCall() ;

	@DefaultValue( "true" )
	boolean testDataArrayCall() ;

	@DefaultValue( "false" ) 
	boolean testGetTestRefsCall() ;

	@DefaultValue( "false" ) 
	boolean getSize() ;

	@DefaultValue( "false" ) 
	boolean getRead() ;
    }

    String getTransportDescription() {
	return (data.blocking() ? "Blocking transport(" : "Default transport(")
	    + data.fragmentSize() + ")" ;
    }

    /** By default, run client and server co-located.
     * Optional args:
     * <ul>
     * <li>-mode [local client server]: Run as a client only on the given host name.
     * which must be a name of the host running the program.
     * </ul>
     */
    public static void main( String[] args ) {
	ArgParser<ArgumentData> ap = new ArgParser( ArgumentData.class ) ;
	ArgumentData adata = ap.parse( args ) ;
	System.out.println( "Running StandardTest with arguments" ) ;
	System.out.println( adata ) ;
	System.out.println( "-----------------------------------------------" ) ;

	StandardTest st = new StandardTest( adata )  ;

	switch (adata.mode()) {
	    case CLIENT :
		st.run() ;
		// st.stopServer() ;
		System.exit(0) ;
		break ;
	    case SERVER :
		st.initializeServer() ;
		st.serverWait() ;
		break ;
	    case LOCAL :
		// Colocated test:
		st.initializeServer() ;
		st.run() ;
		st.cleanUpServer() ;
		System.exit(0) ;
		break ;
	}
    }
}
