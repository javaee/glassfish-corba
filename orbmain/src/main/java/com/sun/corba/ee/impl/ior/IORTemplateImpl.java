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

package com.sun.corba.ee.impl.ior;

import java.util.Iterator ;

import org.omg.CORBA_2_3.portable.OutputStream ;
import org.omg.CORBA_2_3.portable.InputStream ;

import com.sun.corba.ee.spi.ior.IdentifiableContainerBase ;
import com.sun.corba.ee.spi.ior.IdentifiableFactoryFinder ;
import com.sun.corba.ee.spi.ior.IORTemplate ;
import com.sun.corba.ee.spi.ior.ObjectKeyTemplate ;
import com.sun.corba.ee.spi.ior.TaggedProfileTemplate ;
import com.sun.corba.ee.spi.ior.ObjectId ;
import com.sun.corba.ee.spi.ior.IOR ;
import com.sun.corba.ee.spi.ior.IORFactory ;

import com.sun.corba.ee.spi.orb.ORB ;

/**
 * This class is a container of TaggedProfileTemplates.
 * @author 
 */
public class IORTemplateImpl 
    extends IdentifiableContainerBase<TaggedProfileTemplate>
    implements IORTemplate
{
    private ObjectKeyTemplate oktemp ;

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder() ;
        sb.append( "IORTemplate[oktemp=") ;
        sb.append( oktemp.toString() ) ;
        sb.append( " profile templates:") ;
        sb.append( super.toString() ) ;
        sb.append( ']' ) ;
        return sb.toString() ;
    }

    @Override
    public boolean equals( Object obj )
    {
        if (obj == null)
            return false ;

        if (!(obj instanceof IORTemplateImpl))
            return false ;

        IORTemplateImpl other = (IORTemplateImpl)obj ;

        return super.equals( obj ) && oktemp.equals( other.getObjectKeyTemplate() ) ;
    }

    @Override
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
    @Override
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
