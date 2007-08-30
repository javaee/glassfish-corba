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

package com.sun.corba.se.impl.ior;

import java.util.Iterator ;

import org.omg.CORBA.INTERNAL ;

import org.omg.CORBA_2_3.portable.OutputStream ;
import org.omg.CORBA_2_3.portable.InputStream ;

import org.omg.IOP.TAG_INTERNET_IOP ;

import com.sun.corba.se.spi.ior.IdentifiableContainerBase ;
import com.sun.corba.se.spi.ior.IdentifiableFactoryFinder ;
import com.sun.corba.se.spi.ior.IORTemplate ;
import com.sun.corba.se.spi.ior.ObjectKeyTemplate ;
import com.sun.corba.se.spi.ior.TaggedProfileTemplate ;
import com.sun.corba.se.spi.ior.ObjectId ;
import com.sun.corba.se.spi.ior.IOR ;
import com.sun.corba.se.spi.ior.IORFactory ;

import com.sun.corba.se.spi.orb.ORB ;

/**
 * This class is a container of TaggedProfileTemplates.
 * @author 
 */
public class IORTemplateImpl 
    extends IdentifiableContainerBase<TaggedProfileTemplate>
    implements IORTemplate
{
    private ObjectKeyTemplate oktemp ;

    public boolean equals( Object obj )
    {
	if (obj == null)
	    return false ;

	if (!(obj instanceof IORTemplateImpl))
	    return false ;

	IORTemplateImpl other = (IORTemplateImpl)obj ;

	return super.equals( obj ) && oktemp.equals( other.getObjectKeyTemplate() ) ;
    }

    public int hashCode()
    {
	return super.hashCode() ^ oktemp.hashCode() ;
    }

    public ObjectKeyTemplate getObjectKeyTemplate()
    {
	return oktemp ;
    }

    public IORTemplateImpl( ObjectKeyTemplate oktemp )
    {
	this.oktemp = oktemp ;
    }

    public IOR makeIOR( ORB orb, String typeid, ObjectId oid ) 
    {
	return new IORImpl( orb, typeid, this, oid ) ;
    }

    public boolean isEquivalent( IORFactory other ) 
    {
	if (!(other instanceof IORTemplate))
	    return false ;

	IORTemplate list = (IORTemplate)other ;

	Iterator<TaggedProfileTemplate> thisIterator = iterator() ;
	Iterator<TaggedProfileTemplate> listIterator = list.iterator() ;
	while (thisIterator.hasNext() && listIterator.hasNext()) {
	    TaggedProfileTemplate thisTemplate = thisIterator.next() ;
	    TaggedProfileTemplate listTemplate = listIterator.next() ;
	    if (!thisTemplate.isEquivalent( listTemplate ))
		return false ;
	}

	return (thisIterator.hasNext() == listIterator.hasNext()) &&
	    getObjectKeyTemplate().equals( list.getObjectKeyTemplate() ) ;
    }

    /** Ensure that this IORTemplate and all of its profiles can not be
    * modified.  This overrides the method inherited from 
    * FreezableList through IdentifiableContainerBase.
    */
    public void makeImmutable()
    {
	makeElementsImmutable() ;
	super.makeImmutable() ;
    }

    public void write( OutputStream os ) 
    {
	oktemp.write( os ) ;
	EncapsulationUtility.writeIdentifiableSequence( this, os ) ;
    }

    public IORTemplateImpl( InputStream is ) 
    {
	ORB orb = (ORB)(is.orb()) ;
    	IdentifiableFactoryFinder<TaggedProfileTemplate> finder = 
	    orb.getTaggedProfileTemplateFactoryFinder() ;

	oktemp = orb.getObjectKeyFactory().createTemplate( is ) ;
	EncapsulationUtility.readIdentifiableSequence( this, finder, is ) ;

	makeImmutable() ;
    }
}
