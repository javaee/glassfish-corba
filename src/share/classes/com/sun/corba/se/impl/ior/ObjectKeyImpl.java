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

import org.omg.CORBA_2_3.portable.OutputStream ;

import com.sun.corba.se.spi.protocol.CorbaServerRequestDispatcher ;

import com.sun.corba.se.spi.orb.ORB ;

import com.sun.corba.se.spi.ior.ObjectId ;
import com.sun.corba.se.spi.ior.ObjectKey ;
import com.sun.corba.se.spi.ior.ObjectKeyTemplate ;

import com.sun.corba.se.impl.encoding.EncapsOutputStream ;

import com.sun.corba.se.impl.logging.IORSystemException;

/**
 * @author  Ken Cavanaugh
 */
public class ObjectKeyImpl implements ObjectKey 
{
    private ObjectKeyTemplate oktemp;
    private ObjectId id;
    private byte[] array;
    
    public ObjectKeyImpl( ObjectKeyTemplate oktemp, ObjectId id) {
	this.oktemp = oktemp ;
	this.id = id ;
    }

    @Override
    public boolean equals( Object obj )
    {
	if (obj == null)
	    return false ;

	if (!(obj instanceof ObjectKeyImpl))
	    return false ;

	ObjectKeyImpl other = (ObjectKeyImpl)obj ;

	return oktemp.equals( other.oktemp ) &&
	    id.equals( other.id ) ;
    }

    @Override
    public int hashCode()
    {
	return oktemp.hashCode() ^ id.hashCode() ;
    }

    public ObjectKeyTemplate getTemplate() 
    {
	return oktemp ;
    }

    public ObjectId getId()
    {
	return id ;
    }

    public void write( OutputStream os ) 
    {
	oktemp.write( id, os ) ;
    }

    public synchronized byte[] getBytes(org.omg.CORBA.ORB orb) 
    {
        if (array == null) {	    
	    EncapsOutputStream os = new EncapsOutputStream((ORB)orb);
	    try {
	        write(os);
		array = os.toByteArray();
	    } finally {
	        try {
		    os.close();
		} catch (java.io.IOException e) {
		    IORSystemException wrapper;
		    wrapper = ((ORB)orb).getLogWrapperTable().get_OA_IOR_IOR() ;
		    wrapper.ioexceptionDuringStreamClose(e);    
		}
	    }
	} 
	return (byte[])array.clone() ;
    }

    public CorbaServerRequestDispatcher getServerRequestDispatcher() 
    {
	return oktemp.getServerRequestDispatcher( id ) ;
    }
}
