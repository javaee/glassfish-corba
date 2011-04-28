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

package com.sun.corba.se.impl.ior;

import java.util.Iterator ;
import java.util.Map ;
import java.util.HashMap ;

import java.io.StringWriter;
import java.io.IOException;

import org.omg.CORBA_2_3.portable.InputStream ;
import org.omg.CORBA_2_3.portable.OutputStream ;

import org.omg.IOP.TAG_INTERNET_IOP ;

import com.sun.corba.se.spi.ior.ObjectId ;
import com.sun.corba.se.spi.ior.TaggedProfileTemplate ;
import com.sun.corba.se.spi.ior.TaggedProfile ;
import com.sun.corba.se.spi.ior.IOR ;
import com.sun.corba.se.spi.ior.IORTemplate ;
import com.sun.corba.se.spi.ior.IORTemplateList ;
import com.sun.corba.se.spi.ior.IdentifiableFactoryFinder ;
import com.sun.corba.se.spi.ior.IdentifiableContainerBase ;
import com.sun.corba.se.spi.ior.ObjectKeyTemplate ;
import com.sun.corba.se.spi.ior.IORFactories ;

import com.sun.corba.se.spi.orb.ORB;

import com.sun.corba.se.impl.encoding.MarshalOutputStream;

import com.sun.corba.se.impl.encoding.EncapsOutputStream;

import com.sun.corba.se.impl.misc.HexOutputStream;
import com.sun.corba.se.spi.misc.ORBConstants;

import com.sun.corba.se.spi.logging.IORSystemException ;

import com.sun.corba.se.spi.ior.iiop.IIOPProfile ;

/** An IOR is represented as a list of profiles.
* Only objects that extend TaggedProfile should be added to an IOR.
* However, enforcing this restriction requires overriding all
* of the addYYY methods inherited from List, so no check
* is included here.
* @author Ken Cavanaugh
*/
public class IORImpl extends IdentifiableContainerBase<TaggedProfile> 
    implements IOR
{
    private String typeId;
    private ORB factory = null ;
    static final IORSystemException wrapper =
        IORSystemException.self ;
    private boolean isCachedHashValue = false;
    private int cachedHashValue;

    public Iterator<TaggedProfile> getTaggedProfiles() {
        return iterator() ;
    }

    public ORB getORB()
    {
	return factory ;
    }

    /* This variable is set directly from the constructors that take
     * an IORTemplate or IORTemplateList as arguments; otherwise it
     * is derived from the list of TaggedProfile instances on the first
     * call to getIORTemplates.  Note that we assume that an IOR with
     * mutiple TaggedProfile instances has the same ObjectId in each
     * TaggedProfile, as otherwise the IOR could never be created through
     * an ObjectReferenceFactory.
     */
    private IORTemplateList iortemps = null ;

    @Override
    public boolean equals( Object obj )
    {
	if (obj == null) {
            return false;
        }

	if (!(obj instanceof IOR)) {
            return false;
        }

	IOR other = (IOR)obj ;

	return super.equals( obj ) && typeId.equals( other.getTypeId() ) ;
    }

    @Override
    public int hashCode() 
    {
	if (!isCachedHashValue) { 
	    cachedHashValue = (super.hashCode() ^ typeId.hashCode()); 
	    isCachedHashValue = true; 
	}
	return cachedHashValue;
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder() ;
	sb.append( "IORImpl[type=") ;
	sb.append( typeId ) ;
	sb.append( " iorTemplates=" ) ;

	IORTemplateList list = getIORTemplates() ;
	sb.append( list.toString() ) ;
	return sb.toString() ;
    }

    /** Construct an empty IOR.  This is needed for null object references.
    */
    public IORImpl( ORB orb )
    {
	this( orb, "" ) ;
    }

    public IORImpl( ORB orb, String typeid )
    {
	factory = orb ;
	this.typeId = typeid ;
    }

    /** Construct an IOR from an IORTemplate by applying the same
    * object id to each TaggedProfileTemplate in the IORTemplate.
    */
    public IORImpl( ORB orb, String typeId, IORTemplate iortemp, ObjectId id) 
    {
	this( orb, typeId ) ;

	this.iortemps = IORFactories.makeIORTemplateList() ;
	this.iortemps.add( iortemp ) ;
    
	addTaggedProfiles( iortemp, id ) ;
	
	makeImmutable() ;
    }
    
    private void addTaggedProfiles( IORTemplate iortemp, ObjectId id ) 
    {
	ObjectKeyTemplate oktemp = iortemp.getObjectKeyTemplate() ;
	for( TaggedProfileTemplate temp : iortemp) {
	    TaggedProfile profile = temp.create( oktemp, id ) ;
	    add( profile ) ;
	}
    }

    /** Construct an IOR from an IORTemplate by applying the same
    * object id to each TaggedProfileTemplate in the IORTemplate.
    */
    public IORImpl( ORB orb, String typeId, IORTemplateList iortemps, ObjectId id) 
    {
	this( orb, typeId ) ;

	this.iortemps = iortemps ;

	Iterator<IORTemplate> iter = iortemps.iterator() ;
	while (iter.hasNext()) {
	    IORTemplate iortemp = iter.next() ;
	    addTaggedProfiles( iortemp, id ) ;
	}
	
	makeImmutable() ;
    }
    
    // Note that orb is not always the ORB of is!
    public IORImpl(ORB orb, InputStream is) 
    {
	this( orb, is.read_string() ) ;

    	IdentifiableFactoryFinder<TaggedProfile> finder = 
	    factory.getTaggedProfileFactoryFinder() ;

	EncapsulationUtility.readIdentifiableSequence( this, finder, is ) ;

	makeImmutable() ;
    }
    
    public String getTypeId() 
    {
	return typeId ;
    }
    
    public void write(OutputStream os) 
    {
	os.write_string( typeId ) ;
	EncapsulationUtility.writeIdentifiableSequence( this, os ) ;
    }

    public String stringify()
    {
        StringWriter bs;

        MarshalOutputStream s = new EncapsOutputStream(factory);
        s.putEndian();
        write( (OutputStream)s );
        bs = new StringWriter();
        try {
            s.writeTo(new HexOutputStream(bs));
        } catch (IOException ex) {
	    throw wrapper.stringifyWriteError( ex ) ;
        }

        return ORBConstants.STRINGIFY_PREFIX + bs;
    }

    @Override
    public synchronized void makeImmutable()
    {
	makeElementsImmutable() ;

	if (iortemps != null) {
            iortemps.makeImmutable();
        }

	super.makeImmutable() ;
    }
    
    public org.omg.IOP.IOR getIOPIOR() {    
	EncapsOutputStream os = new EncapsOutputStream(factory);
	write(os);
	InputStream is = (InputStream) (os.create_input_stream());
	return org.omg.IOP.IORHelper.read(is);
    }

    public boolean isNil()
    {
        //
        // The check for typeId length of 0 below is commented out
        // as a workaround for a bug in ORBs which send a
        // null objref with a non-empty typeId string.
        //
	return ((size() == 0) /* && (typeId.length() == 0) */);
    }

    public boolean isEquivalent(IOR ior)
    {
	Iterator<TaggedProfile> myIterator = iterator() ;
	Iterator<TaggedProfile> otherIterator = ior.iterator() ;
	while (myIterator.hasNext() && otherIterator.hasNext()) {
	    TaggedProfile myProfile = myIterator.next() ;
	    TaggedProfile otherProfile = otherIterator.next() ;
	    if (!myProfile.isEquivalent( otherProfile )) {
                return false;
            }
	}

	return myIterator.hasNext() == otherIterator.hasNext() ; 
    }

    private void initializeIORTemplateList() 
    {
	// Maps ObjectKeyTemplate to IORTemplate
	Map<ObjectKeyTemplate,IORTemplate> oktempToIORTemplate = 
	    new HashMap<ObjectKeyTemplate,IORTemplate>() ;

	iortemps = IORFactories.makeIORTemplateList() ;
	Iterator<TaggedProfile> iter = iterator() ;
	ObjectId oid = null ; // used to check that all profiles have the same oid.
	while (iter.hasNext()) {
	    TaggedProfile prof = iter.next() ;
	    TaggedProfileTemplate ptemp = prof.getTaggedProfileTemplate() ;
	    ObjectKeyTemplate oktemp = prof.getObjectKeyTemplate() ;

	    // Check that all oids for all profiles are the same: if they are not,
	    // throw exception.
	    if (oid == null) {
                oid = prof.getObjectId();
            } else if (!oid.equals( prof.getObjectId() )) {
                throw wrapper.badOidInIorTemplateList();
            }

	    // Find or create the IORTemplate for oktemp.
	    IORTemplate iortemp = oktempToIORTemplate.get( oktemp ) ;
	    if (iortemp == null) {
		iortemp = IORFactories.makeIORTemplate( oktemp ) ;
		oktempToIORTemplate.put( oktemp, iortemp ) ;
		iortemps.add( iortemp ) ;
	    }

	    iortemp.add( ptemp ) ;
	}

	iortemps.makeImmutable() ;
    }

    /** Return the IORTemplateList for this IOR.  Will throw
     * exception if it is not possible to generate an IOR
     * from the IORTemplateList that is equal to this IOR, 
     * which can only happen if not every TaggedProfile in the
     * IOR has the same ObjectId.
     */
    public synchronized IORTemplateList getIORTemplates() 
    {
	if (iortemps == null) {
            initializeIORTemplateList();
        }

	return iortemps ;
    }

    /** Return the first IIOPProfile in this IOR.
     * Originally we planned to remove this, because we planned to use
     * multiple IIOP profiles.  However, we really have no need for 
     * multiple profiles in the ORB, so we will probably never remove
     * this API.
     */
    public IIOPProfile getProfile() 
    {
	IIOPProfile iop = null ;
	Iterator<TaggedProfile> iter = 
	    iteratorById( TAG_INTERNET_IOP.value ) ;
	if (iter.hasNext()) {
            iop =
                IIOPProfile.class.cast(iter.next());
        }
 
        if (iop != null) {
            return iop;
        }
 
        // if we come to this point then no IIOP Profile
        // is present.  Therefore, throw an exception.
	throw wrapper.iorMustHaveIiopProfile() ;
    }
}
