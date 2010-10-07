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

package com.sun.corba.se.impl.ior ;

import org.omg.CORBA.portable.InputStream ;
import org.omg.CORBA.portable.OutputStream ;
import org.omg.CORBA.portable.StreamableValue ;

import org.omg.CORBA.TypeCode ;

import org.omg.PortableInterceptor.ObjectReferenceFactory ;
import org.omg.PortableInterceptor.ObjectReferenceFactoryHelper ;


import com.sun.corba.se.spi.ior.IORFactory;
import com.sun.corba.se.spi.ior.IORTemplateList;
import com.sun.corba.se.spi.ior.IORFactories;

import com.sun.corba.se.spi.orb.ORB ;

/** This is an implementation of the ObjectReferenceFactory abstract value 
* type defined by the portable interceptors IDL.  
* Note that this is a direct Java implementation
* of the abstract value type: there is no stateful value type defined in IDL,
* since defining the state in IDL is awkward and inefficient.  The best way
* to define the state is to use internal data structures that can be written
* to and read from CORBA streams.
*/
public class ObjectReferenceFactoryImpl extends ObjectReferenceProducerBase
    implements ObjectReferenceFactory, StreamableValue 
{
    // private static long serialVersionUID = 0 ;

    transient private IORTemplateList iorTemplates ;

    public ObjectReferenceFactoryImpl( InputStream is )
    {
	super( (ORB)(is.orb()) ) ;
	_read( is ) ;
    }

    public ObjectReferenceFactoryImpl( ORB orb, IORTemplateList iortemps ) 
    {
	super( orb ) ;
	iorTemplates = iortemps ;
    }

    @Override
    public boolean equals( Object obj )
    {
	if (!(obj instanceof ObjectReferenceFactoryImpl))
	    return false ;

	ObjectReferenceFactoryImpl other = (ObjectReferenceFactoryImpl)obj ;

	return (iorTemplates != null) && 
	    iorTemplates.equals( other.iorTemplates ) ;
    }

    @Override
    public int hashCode()
    {
	return iorTemplates.hashCode() ;
    }

    // Note that this repository ID must reflect the implementation
    // of the abstract valuetype (that is, this class), not the
    // repository ID of the org.omg.PortableInterceptor.ObjectReferenceFactory
    // class.  This allows for multiple independent implementations 
    // of the abstract valuetype, should that become necessary.
    public static final String repositoryId = 
	"IDL:com/sun/corba/se/impl/ior/ObjectReferenceFactoryImpl:1.0" ;

    public String[] _truncatable_ids() 
    {
	return new String[] { repositoryId } ;
    }

    public TypeCode _type() 
    {
	return ObjectReferenceFactoryHelper.type() ;
    }

    /** Read the data into a (presumably) empty ObjectReferenceFactoryImpl.  
    * This sets the orb to the ORB of the InputStream.  
    */
    public void _read( InputStream is ) 
    {
	org.omg.CORBA_2_3.portable.InputStream istr = 
	    (org.omg.CORBA_2_3.portable.InputStream)is ;

	iorTemplates = IORFactories.makeIORTemplateList( istr ) ;
    }

    /** Write the state to the OutputStream.  
     */
    public void _write( OutputStream os ) 
    {
	org.omg.CORBA_2_3.portable.OutputStream ostr = 
	    (org.omg.CORBA_2_3.portable.OutputStream)os ;

	iorTemplates.write( ostr ) ;
    }

    public IORFactory getIORFactory()
    {
	return iorTemplates ;
    }

    public IORTemplateList getIORTemplateList() 
    {
	return iorTemplates ;
    }
}
