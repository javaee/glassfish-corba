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

package com.sun.corba.se.impl.ior.iiop;

import java.util.Iterator ;

import org.omg.CORBA.SystemException ;

import org.omg.CORBA_2_3.portable.OutputStream ;
import org.omg.CORBA_2_3.portable.InputStream ;

import org.omg.IOP.TAG_INTERNET_IOP;
import org.omg.IOP.TAG_JAVA_CODEBASE;

import com.sun.corba.se.spi.protocol.RequestDispatcherRegistry ;

import com.sun.corba.se.spi.oa.ObjectAdapter ;
import com.sun.corba.se.spi.oa.ObjectAdapterFactory ;

import com.sun.corba.se.spi.ior.ObjectId ;
import com.sun.corba.se.spi.ior.ObjectAdapterId ;
import com.sun.corba.se.spi.ior.TaggedProfile ;
import com.sun.corba.se.spi.ior.TaggedProfileTemplate ;
import com.sun.corba.se.spi.ior.ObjectKey ;
import com.sun.corba.se.spi.ior.ObjectKeyTemplate ;
import com.sun.corba.se.spi.ior.TaggedComponent ;
import com.sun.corba.se.spi.ior.IdentifiableBase ;
import com.sun.corba.se.spi.ior.IORFactories ;

import com.sun.corba.se.spi.ior.iiop.IIOPAddress ;
import com.sun.corba.se.spi.ior.iiop.IIOPProfile ;
import com.sun.corba.se.spi.ior.iiop.IIOPProfileTemplate ;
import com.sun.corba.se.spi.ior.iiop.IIOPFactories ;
import com.sun.corba.se.spi.ior.iiop.GIOPVersion ;
import com.sun.corba.se.spi.ior.iiop.JavaCodebaseComponent ;

import com.sun.corba.se.spi.orb.ORB ;
import com.sun.corba.se.spi.orb.ORBVersion ;

import com.sun.corba.se.impl.ior.EncapsulationUtility ;

import com.sun.corba.se.impl.encoding.EncapsInputStream ;
import com.sun.corba.se.impl.encoding.EncapsOutputStream ;

import com.sun.corba.se.impl.util.JDKBridge;

import com.sun.corba.se.spi.logging.IORSystemException;

/**
 * @author
 */
public class IIOPProfileImpl extends IdentifiableBase implements IIOPProfile
{
    private static final IORSystemException wrapper =
        IORSystemException.self ;

    private ORB orb ;
    private ObjectId oid;
    private IIOPProfileTemplate proftemp;
    private ObjectKeyTemplate oktemp ;
    private ObjectKey objectKey;

    // Cached lookups
    protected String codebase = null ;
    protected boolean cachedCodebase = false;

    private boolean checkedIsLocal = false ;
    private boolean cachedIsLocal = false ;

    // initialize-on-demand holder
    private static class LocalCodeBaseSingletonHolder {
	public static JavaCodebaseComponent comp ;

	static {
	    String localCodebase = JDKBridge.getLocalCodebase() ;
	    if (localCodebase == null) {
                comp = null;
            } else {
                comp = IIOPFactories.makeJavaCodebaseComponent(localCodebase);
            }
	}
    }

    private GIOPVersion giopVersion = null;

    @Override
    public boolean equals( Object obj )
    {
	if (!(obj instanceof IIOPProfileImpl)) {
            return false;
        }

	IIOPProfileImpl other = (IIOPProfileImpl)obj ;

	return oid.equals( other.oid ) && proftemp.equals( other.proftemp ) &&
	    oktemp.equals( other.oktemp ) ;
    }

    @Override
    public int hashCode()
    {
	return oid.hashCode() ^ proftemp.hashCode() ^ oktemp.hashCode() ;
    }

    public ObjectId getObjectId()
    {
	return oid ;
    }

    public TaggedProfileTemplate getTaggedProfileTemplate()
    {
	return proftemp ;
    }

    public ObjectKeyTemplate getObjectKeyTemplate() 
    {
	return oktemp ;
    }

    private IIOPProfileImpl( ORB orb ) 
    {
	this.orb = orb ;
    }

    public IIOPProfileImpl( ORB orb, ObjectKeyTemplate oktemp, ObjectId oid, 
	IIOPProfileTemplate proftemp )
    {
	this( orb ) ;
	this.oktemp = oktemp ;
	this.oid = oid ;
	this.proftemp = proftemp ;
    }

    public IIOPProfileImpl( InputStream is )
    {
	this( (ORB)(is.orb()) ) ;
	init( is ) ;
    }

    public IIOPProfileImpl( ORB orb, org.omg.IOP.TaggedProfile profile) 
    {
	this( orb ) ;

        if (profile == null || profile.tag != TAG_INTERNET_IOP.value ||
	    profile.profile_data == null) {
	    throw wrapper.invalidTaggedProfile() ;
        }

        EncapsInputStream istr = new EncapsInputStream(orb, profile.profile_data, 
	    profile.profile_data.length);
	istr.consumeEndian();
	init( istr ) ;
    }

    private void init( InputStream istr )
    {
	// First, read all of the IIOP IOR data
	GIOPVersion version = new GIOPVersion() ;
	version.read( istr ) ;
	IIOPAddress primary = new IIOPAddressImpl( istr ) ;
	byte[] key = EncapsulationUtility.readOctets( istr ) ;

	ObjectKey okey = orb.getObjectKeyFactory().create( key ) ;
	oktemp = okey.getTemplate() ;
	oid = okey.getId() ;

	proftemp = IIOPFactories.makeIIOPProfileTemplate( orb, 
	    version, primary ) ;

	// Handle any tagged components (if applicable)
	if (version.getMinor() > 0) {
            EncapsulationUtility.readIdentifiableSequence(proftemp,
                orb.getTaggedComponentFactoryFinder(), istr);
        }

	// If there is no codebase in this IOR and there IS a
	// java.rmi.server.codebase property set, we need to
	// update the IOR with the local codebase.  Note that
	// there is only one instance of the local codebase, but it
	// can be safely shared in multiple IORs since it is immutable.
	if (uncachedGetCodeBase() == null) {
	    JavaCodebaseComponent jcc = LocalCodeBaseSingletonHolder.comp ;

	    if (jcc != null) {
		if (version.getMinor() > 0) {
                    proftemp.add(jcc);
                }

		codebase = jcc.getURLs() ;
	    }

	    // Whether codebase is null or not, we have it,
	    // and so getCodebase ned never call uncachedGetCodebase.
	    cachedCodebase = true;
	}
    }	

    public void writeContents(OutputStream os)
    {
	proftemp.write( oktemp, oid, os ) ;
    }

    public int getId()
    {
	return proftemp.getId() ;
    }

    public boolean isEquivalent( TaggedProfile prof )
    {
	if (!(prof instanceof IIOPProfile)) {
            return false;
        }

	IIOPProfile other = (IIOPProfile)prof ;

	return oid.equals( other.getObjectId() ) && 
	       proftemp.isEquivalent( other.getTaggedProfileTemplate() ) &&
	       oktemp.equals( other.getObjectKeyTemplate() ) ;
    }

    public synchronized ObjectKey getObjectKey()
    {
        if (objectKey == null) {
	    objectKey = IORFactories.makeObjectKey( oktemp, oid ) ;
	}
	return objectKey ;
    }

    public org.omg.IOP.TaggedProfile getIOPProfile()
    {
	EncapsOutputStream os = new EncapsOutputStream( orb ) ;
	os.write_long( getId() ) ;
	write( os ) ;
	InputStream is = (InputStream)(os.create_input_stream()) ;
	return org.omg.IOP.TaggedProfileHelper.read( is ) ;
    }

    private String uncachedGetCodeBase() {
	Iterator<TaggedComponent> iter = 
	    proftemp.iteratorById( TAG_JAVA_CODEBASE.value ) ;

	if (iter.hasNext()) {
	    JavaCodebaseComponent jcbc = 
		JavaCodebaseComponent.class.cast( iter.next() ) ;
	    return jcbc.getURLs() ;
	}

	return null ;
    }

    public synchronized String getCodebase() {
	if (!cachedCodebase) {
	    cachedCodebase = true ;
	    codebase = uncachedGetCodeBase() ;
	}

	return codebase ;
    }

    /**
     * @return the ORBVersion associated with the object key in the IOR.
     */
    public ORBVersion getORBVersion() {
        return oktemp.getORBVersion();
    }

    public synchronized boolean isLocal()
    {
	if (!checkedIsLocal) {
	    checkedIsLocal = true ;
	    final String host = proftemp.getPrimaryAddress().getHost() ;
            final boolean isLocalHost = orb.isLocalHost( host ) ;

            final int scid = oktemp.getSubcontractId() ;
            final int sid = oktemp.getServerId() ;
            final boolean isLocalServerId = (sid == -1) ||
                orb.isLocalServerId( scid, sid ) ;

            final boolean isLocalServerPort = 
                orb.getLegacyServerSocketManager().legacyIsLocalServerPort( 
                    proftemp.getPrimaryAddress().getPort() );

	    cachedIsLocal = isLocalHost && isLocalServerId 
                && isLocalServerPort ;
        }

	return cachedIsLocal ;
    }

    /** Return the servant for this IOR, if it is local AND if the OA that
     * implements this objref supports direct access to servants outside of an
     * invocation.
     */
    public java.lang.Object getServant()
    {
	if (!isLocal()) {
            return null;
        }

	RequestDispatcherRegistry scr = orb.getRequestDispatcherRegistry() ;
	ObjectAdapterFactory oaf = scr.getObjectAdapterFactory( 
	    oktemp.getSubcontractId() ) ;

	ObjectAdapterId oaid = oktemp.getObjectAdapterId() ;
	ObjectAdapter oa = null ;

	try {
	    oa = oaf.find( oaid ) ;
	} catch (SystemException exc) {
	    // Could not find the OA, so just return null.
	    // This usually happens when POAs are being deleted,
	    // and the POA always return null for getLocalServant anyway.
	    wrapper.getLocalServantFailure( exc, oaid ) ;
	    return null ;
	}

	byte[] boid = oid.getId() ;
	java.lang.Object servant = oa.getLocalServant( boid ) ;
	return servant ;
    }

    /**
     * Return GIOPVersion for this IOR.
     * Requests created against this IOR will be of the
     * return Version.
     */
    public synchronized GIOPVersion getGIOPVersion()
    {
	return proftemp.getGIOPVersion() ;
    }

    public void makeImmutable()
    {
	proftemp.makeImmutable() ;
    }
}
