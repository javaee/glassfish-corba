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

import org.omg.CORBA_2_3.portable.InputStream ;

import com.sun.corba.se.spi.ior.TaggedProfile ;
import com.sun.corba.se.spi.ior.TaggedProfileTemplate ;
import com.sun.corba.se.spi.ior.ObjectId ;
import com.sun.corba.se.spi.ior.ObjectKeyTemplate ;
import com.sun.corba.se.spi.ior.ObjectKey ;

import com.sun.corba.se.spi.orb.ORB ;

import com.sun.corba.se.spi.ior.iiop.GIOPVersion ;

import com.sun.corba.se.impl.encoding.EncapsOutputStream ;

/**
 * @author 
 */
public class GenericTaggedProfile extends GenericIdentifiable implements TaggedProfile 
{
    private ORB orb ;

    public GenericTaggedProfile( int id, InputStream is ) 
    {
	super( id, is ) ;
	this.orb = (ORB)(is.orb()) ;
    }

    public GenericTaggedProfile( ORB orb, int id, byte[] data ) 
    {
	super( id, data ) ;
	this.orb = orb ;
    }
    
    public TaggedProfileTemplate getTaggedProfileTemplate() 
    {
	return null ;
    }

    public ObjectId getObjectId() 
    {
	return null ;
    }

    public ObjectKeyTemplate getObjectKeyTemplate() 
    {
	return null ;
    }

    public ObjectKey getObjectKey() 
    {
	return null ;
    }

    public boolean isEquivalent( TaggedProfile prof ) 
    {
	return equals( prof ) ;
    }

    public void makeImmutable()
    {
	// NO-OP
    }

    public boolean isLocal() 
    {
	return false ;
    }
    
    public org.omg.IOP.TaggedProfile getIOPProfile() 
    {
	EncapsOutputStream os = new EncapsOutputStream( orb ) ;
	write( os ) ;
	InputStream is = (InputStream)(os.create_input_stream()) ;
	return org.omg.IOP.TaggedProfileHelper.read( is ) ;
    }
}
