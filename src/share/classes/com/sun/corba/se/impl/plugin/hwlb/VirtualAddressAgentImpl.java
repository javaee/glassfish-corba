/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2005-2007 Sun Microsystems, Inc. All rights reserved.
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

package com.sun.corba.se.impl.plugin.hwlb ;

import java.lang.reflect.Field ;

import java.security.AccessController ;
import java.security.PrivilegedAction ;

import java.util.Iterator ;
import java.util.Map ;
import java.util.HashMap ;
import java.util.Properties ;

import org.omg.CORBA.LocalObject ;

import org.omg.CORBA_2_3.portable.InputStream ;

import org.omg.IOP.TAG_INTERNET_IOP ;

import org.omg.PortableInterceptor.ORBInitializer ;
import org.omg.PortableInterceptor.IORInterceptor_3_0 ;
import org.omg.PortableInterceptor.IORInfo ;
import org.omg.PortableInterceptor.ObjectReferenceTemplate ;
import org.omg.PortableInterceptor.ORBInitInfo ;
import org.omg.PortableInterceptor.ObjectReferenceTemplate ;

import com.sun.corba.se.spi.orb.ORB ;
import com.sun.corba.se.spi.orb.ORBData ;
import com.sun.corba.se.spi.orb.ORBConfigurator ;
import com.sun.corba.se.spi.orb.DataCollector ;
import com.sun.corba.se.spi.orb.ParserImplBase ;
import com.sun.corba.se.spi.orb.PropertyParser ;
import com.sun.corba.se.spi.orb.OperationFactory ;

import com.sun.corba.se.spi.ior.Identifiable ;
import com.sun.corba.se.spi.ior.IdentifiableFactoryFinder ;
import com.sun.corba.se.spi.ior.EncapsulationFactoryBase ;
import com.sun.corba.se.spi.ior.IOR ;
import com.sun.corba.se.spi.ior.IORTemplate ;
import com.sun.corba.se.spi.ior.IORFactories ;
import com.sun.corba.se.spi.ior.TaggedProfileTemplate ;
import com.sun.corba.se.spi.ior.TaggedComponent ;
import com.sun.corba.se.spi.ior.TaggedProfile ;
import com.sun.corba.se.spi.ior.ObjectKey ;
import com.sun.corba.se.spi.ior.ObjectId ;
import com.sun.corba.se.spi.ior.ObjectKeyTemplate ;

import com.sun.corba.se.spi.ior.iiop.IIOPAddress ;
import com.sun.corba.se.spi.ior.iiop.IIOPProfile ;
import com.sun.corba.se.spi.ior.iiop.IIOPProfileTemplate ;
import com.sun.corba.se.spi.ior.iiop.IIOPFactories ;
import com.sun.corba.se.spi.ior.iiop.AlternateIIOPAddressComponent ;
import com.sun.corba.se.spi.ior.iiop.GIOPVersion ;

import com.sun.corba.se.spi.protocol.LocalClientRequestDispatcher ;
import com.sun.corba.se.spi.protocol.LocalClientRequestDispatcherFactory ;

import com.sun.corba.se.spi.transport.CorbaContactInfoList ;
import com.sun.corba.se.spi.transport.CorbaContactInfoListFactory ;

import com.sun.corba.se.impl.ior.iiop.IIOPProfileImpl ;
import com.sun.corba.se.impl.ior.iiop.IIOPProfileTemplateImpl ;

import com.sun.corba.se.impl.orbutil.ORBConstants ;

import com.sun.corba.se.impl.orb.ORBDataParserImpl ;

import com.sun.corba.se.impl.protocol.NotLocalLocalCRDImpl ;

import com.sun.corba.se.impl.transport.CorbaContactInfoListImpl ;

import com.sun.corba.se.impl.oa.poa.BadServerIdHandler ;

// Import this to allow running on JDK 1.4, which does
// not contain the CORBA 3.0 IORInfo.
import com.sun.corba.se.impl.interceptors.IORInfoImpl ;

public class VirtualAddressAgentImpl 
    extends LocalObject 
    implements ORBConfigurator, ORBInitializer, IORInterceptor_3_0
{
    private boolean debug = false ;

    private static void dprint( String msg ) {
	System.out.println( msg ) ;
    }

    public static final String VAA_HOST_PROPERTY = ORBConstants.SUN_PREFIX + 
	"ORBVAAHost" ;
    public static final String VAA_PORT_PROPERTY = ORBConstants.SUN_PREFIX + 
	"ORBVAAPort" ;

    private String host = null ;
    private int port = 0 ;
    private ORB orb = null ;
    private IIOPAddress addr = null ;
    private ORBInitializer[] newOrbInits = null ;

    private class AddressParser extends ParserImplBase {
	private String _host = null ;
	private int _port = 0 ;

	public PropertyParser makeParser() {
	    PropertyParser parser = new PropertyParser() ;
	    parser.add( VAA_HOST_PROPERTY, OperationFactory.stringAction(),
		"_host" ) ;
	    parser.add( VAA_PORT_PROPERTY, OperationFactory.integerAction(),
		"_port" ) ;
	    return parser ;
	}

	protected void complete() {
	    if (debug) {
		dprint("VirtualAddressAgentImpl : inside complete..." + _host + ":" + _port);
	    }
	    host = _host ;
	    port = _port ;
	}
    }

    public void configure( DataCollector dc, final ORB orb ) {
	debug = orb.subcontractDebugFlag ;

	if (debug) {
	    dprint("VirtualAddressAgentImpl : inside configure...");
	}
	this.orb = orb ;

	orb.setBadServerIdHandler( 
	    new BadServerIdHandler() {
		public void handle( ObjectKey objectkey ) {
		    // NO-OP
		}
	    }
	) ;

	// Create a new parser to extract the virtual address
	// host/port information from the data collector
	final AddressParser parser = new AddressParser() ;
	parser.init( dc ) ;
	addr = IIOPFactories.makeIIOPAddress( orb, host, port ) ;	
	if (debug) {
	    dprint( "Agent address = " + addr ) ;
	}

	// Register the special IIOPProfile in the TaggedProfileFactoryFinder.
	// This means that the isLocal check will be handled properly even
	// when an objref leaves the server that created it and then comes
	// back and gets unmarshalled.
	IdentifiableFactoryFinder finder = 
	    orb.getTaggedProfileFactoryFinder() ;
	finder.registerFactory( 
	    new EncapsulationFactoryBase( TAG_INTERNET_IOP.value ) {
		public Identifiable readContents( InputStream in ) {
		    Identifiable result = new SpecialIIOPProfileImpl( in ) ;
		    return result ;
		}
	    }
	) ;

	// Add this object to the initializer list
	// by using a PropertyParser in a rather unusual fashion

	final ORBData odata = orb.getORBData() ;

	// Add this object to the end of a copy of the ORBInitializers
	// from the ORBData.
	final ORBInitializer[] oldOrbInits = odata.getORBInitializers() ;
	final int newIndex = oldOrbInits.length ;
	newOrbInits = new ORBInitializer[newIndex+1] ;
	for (int ctr=0; ctr<newIndex; ctr++)
	    newOrbInits[ctr] = oldOrbInits[ctr] ;
	newOrbInits[newIndex] = this ;

	// Nasty hack: Use reflection to set the private field!
	// REVISIT: AS 9 has an ORB API for setting ORBInitializers.
	AccessController.doPrivileged(
	    new PrivilegedAction() {
		public Object run() {
		    try {
			final Field fld = 
			    ORBDataParserImpl.class.getDeclaredField( 
				"orbInitializers" ) ;
			fld.setAccessible( true ) ;
			fld.set( odata, newOrbInits ) ;
			return null ;
		    } catch (Exception exc) {
		      exc.printStackTrace();
			// XXX should log something here
			throw new RuntimeException( 
			    "Could not set ORBData.orbInitializers", exc ) ;
		    }
		}
	    }
	)  ;
    }

    public void pre_init( ORBInitInfo info ) {
	if (debug) {
	    dprint("VirtualAddressAgentImpl :inside pre_init...");
	}
	// NO-OP
    }

    public void post_init( ORBInitInfo info ) {
	if (debug) {
	    dprint("VirtualAddressAgentImpl :inside post_init...");
	}

	// register this object as an IORInterceptor.
	try {
	    info.add_ior_interceptor( this ) ;
	} catch (Exception exc) {
	    // XXX Ignore this for now, but probably should log.
	    // Main exception here is duplicate name for the
	    // interceptr.
	}
    }

    public void establish_components( IORInfo info ) {
	if (debug) {
	    dprint("VirtualAddressAgentImpl :inside establish_components..");
	}
	// NO-OP
    }

    // This is exactly like IIOPProfileImpl, except for isLocal.
    // Here isLocal is true iff the profile's primary address
    // is the address of the LB.
    private class SpecialIIOPProfileImpl extends
	IIOPProfileImpl {

	private boolean isLocalChecked = false ;
	private boolean isLocalCachedValue = false ;

	public SpecialIIOPProfileImpl( InputStream in ) {
	    super( in ) ;
	}

	public SpecialIIOPProfileImpl( ORB orb, ObjectKeyTemplate oktemp,
	    ObjectId id, IIOPProfileTemplate ptemp ) {
	    super( orb, oktemp, id, ptemp ) ;
	}

	public boolean isLocal() {
	    if (!isLocalChecked) {
		isLocalChecked = true ;

		IIOPProfileTemplate ptemp = 
		    (IIOPProfileTemplate)getTaggedProfileTemplate() ;

		if (debug) {
		    dprint( "Inside SpecialIIOPProfileImpl.isLocal: ptemp = " 
			+ ptemp ) ;
		    dprint( "Inside SpecialIIOPProfileImpl.isLocal: template addr = " 
			+ ptemp.getPrimaryAddress() ) ;
		}

		isLocalCachedValue = addr.equals( ptemp.getPrimaryAddress() ) ;
	    }

	    return isLocalCachedValue ;
	}
    }

    // This is exactly like IIOPProfileTemplateImpl, except that
    // create creates SpecialIIOPProfileImpl instead of IIOPProfileImpl.
    private class SpecialIIOPProfileTemplateImpl extends
	IIOPProfileTemplateImpl {
	
	private ORB orb ; 

	public SpecialIIOPProfileTemplateImpl( ORB orb, GIOPVersion version,
	    IIOPAddress primary ) {
	    super( orb, version, primary ) ;
	    this.orb = orb ;
	}

	public TaggedProfile create( ObjectKeyTemplate oktemp, ObjectId id ) {
	    return new SpecialIIOPProfileImpl( orb, oktemp, id, this ) ;
	}
    }

    private TaggedProfileTemplate makeCopy( TaggedProfileTemplate temp ) {
	if (debug) {
	    dprint("VirtualAddressAgentImpl :inside makeCopy...");
	}

	if (temp instanceof IIOPProfileTemplate) {
	    final IIOPProfileTemplate oldTemplate = (IIOPProfileTemplate)temp ;

	    // FINALLY, here is where we actualy replace the
	    // default address (from the ORB configuration) that 
	    // is normally used for IOR creation with the 
	    // virtual adress of the external agent.
	    //
	    // However, we also want to change the behavior of 
	    // the TaggedProfile.isLocal method, so that objrefs
	    // created by this template are recognized as being local.
	    // To do this, we need to subclass IIOPProfileImpl, overriding
	    // the definition of isLocal, and then subclass 
	    // IIOPProfileTemplateImpl, overriding the create method
	    // to use the subclass of IIOPProfileImpl.
	    final IIOPProfileTemplate result = 
		new SpecialIIOPProfileTemplateImpl(
		    orb, oldTemplate.getGIOPVersion(), addr ) ;

	    final Iterator iter = oldTemplate.iterator() ;
	    while (iter.hasNext()) {
		TaggedComponent comp = (TaggedComponent)iter.next() ;
		if (!(comp instanceof AlternateIIOPAddressComponent)) 
		    result.add( comp ) ;
	    }
	
	    return result ;
	} else {
	    return temp ;
	}
    }

    public void components_established( IORInfo info ) {
	// Cast this to the implementation class in case we are building
	// this class on JDK 1.4, which has the pre-CORBA 3.0 version of
	// IOFInfo that does not have the adapter_template or current_factory
	// methods.
	IORInfoImpl myInfo = (IORInfoImpl)info ;

	if (debug) {
	    dprint("VirtualAddressAgentImpl :inside components_established...");
	}
	// Get the object adapter's adapter_template as an IORTemplate
	final IORTemplate iort = 
	    (IORTemplate)IORFactories.getIORFactory( 
		myInfo.adapter_template() ) ;

	// Make a copy of the original IORTempalte
	final IORTemplate result = IORFactories.makeIORTemplate( 
	    iort.getObjectKeyTemplate() ) ;

	// Clone iort, but remove all TAG_ALTERNATE_ADDRESS components,
	// and change the primary address/port to be the host/port
	// values in this class.
	final Iterator iter = iort.iterator() ;
	while (iter.hasNext()) {
	    TaggedProfileTemplate tpt = (TaggedProfileTemplate)iter.next() ;
	    result.add( makeCopy( tpt ) ) ;
	}

	final ObjectReferenceTemplate newOrt = 
	    IORFactories.makeObjectReferenceTemplate( orb, result ) ;

	// Install the modified copy as the current_factory (instead of the
	// default behavior, which is simply to use adapter_template)
	myInfo.current_factory( newOrt );
    }

    public void adapter_manager_state_changed( int id,
	short state ) {
	// NO-OP
    }

    public void adapter_state_changed( ObjectReferenceTemplate[] templates,
	short state ) {
	// NO-OP
    }

    public String name() {
	return this.getClass().getName() ;
    }

    public void destroy() {
	// NO-OP
    }
}
