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

import java.util.ArrayList ;
import java.util.Iterator ;

import org.omg.CORBA_2_3.portable.InputStream ;
import org.omg.CORBA_2_3.portable.OutputStream ;

import com.sun.corba.se.spi.ior.IORTemplateList ;
import com.sun.corba.se.spi.ior.ObjectId ;
import com.sun.corba.se.spi.ior.IORTemplate ;
import com.sun.corba.se.spi.ior.IOR ;
import com.sun.corba.se.spi.ior.IORFactory ;
import com.sun.corba.se.spi.ior.IORFactories ;

import com.sun.corba.se.spi.orb.ORB ;

public class IORTemplateListImpl extends FreezableList<IORTemplate> 
    implements IORTemplateList
{
    public IORTemplateListImpl()
    {
	super( new ArrayList<IORTemplate>() ) ;
    }

    public IORTemplateListImpl( InputStream is ) 
    {
	this() ;
	int size = is.read_long() ;
	for (int ctr=0; ctr<size; ctr++) {
	    IORTemplate iortemp = IORFactories.makeIORTemplate( is ) ;
	    add( iortemp ) ;
	}

	makeImmutable() ;
    }

    @Override
    public void makeImmutable()
    {
	makeElementsImmutable() ;
	super.makeImmutable() ;
    }

    public void write( OutputStream os ) 
    {
	os.write_long( size() ) ;
	for (IORTemplate iortemp : this) {
	    iortemp.write( os ) ;
	}
    }

    public IOR makeIOR( ORB orb, String typeid, ObjectId oid ) 
    {
	return new IORImpl( orb, typeid, this, oid ) ;
    }

    public boolean isEquivalent( IORFactory other ) 
    {
	if (!(other instanceof IORTemplateList))
	    return false ;

	IORTemplateList list = (IORTemplateList)other ;

	Iterator<IORTemplate> thisIterator = iterator() ;
	Iterator<IORTemplate> listIterator = list.iterator() ;
	while (thisIterator.hasNext() && listIterator.hasNext()) {
	    IORTemplate thisTemplate = thisIterator.next() ;
	    IORTemplate listTemplate = listIterator.next() ;
	    if (!thisTemplate.isEquivalent( listTemplate ))
		return false ;
	}

	return thisIterator.hasNext() == listIterator.hasNext() ;
    }
}
