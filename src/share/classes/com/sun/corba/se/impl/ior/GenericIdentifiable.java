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

package com.sun.corba.ee.impl.ior;

import java.util.Arrays ;

import org.omg.CORBA_2_3.portable.InputStream;
import org.omg.CORBA_2_3.portable.OutputStream;

import com.sun.corba.ee.spi.ior.Identifiable ;

import org.glassfish.gmbal.ManagedData ;
import org.glassfish.gmbal.ManagedAttribute ;
import org.glassfish.gmbal.Description ;

/**
 * @author 
 * This is used for unknown components and profiles.  A TAG_MULTICOMPONENT_PROFILE will be represented this way.
 */
@ManagedData
@Description( "A generic implementation of an IIOP encapsulation with an integer id" ) 
public abstract class GenericIdentifiable implements Identifiable 
{
    private int id;
    private byte data[];
    
    public GenericIdentifiable(int id, InputStream is) 
    {
        this.id = id ;
        data = EncapsulationUtility.readOctets( is ) ;
    }
    
    public int getId() 
    {
        return id ;
    }
    
    public void write(OutputStream os) 
    {
        os.write_ulong( data.length ) ;
        os.write_octet_array( data, 0, data.length ) ;
    }
    
    public String toString() 
    {
        return "GenericIdentifiable[id=" + getId() + "]" ;
    }
    
    public boolean equals(Object obj) 
    {
        if (obj == null)
            return false ;

        if (!(obj instanceof GenericIdentifiable))
            return false ;

        GenericIdentifiable encaps = (GenericIdentifiable)obj ;

        return (getId() == encaps.getId()) && 
            Arrays.equals( data, encaps.data ) ;
    }
   
    public int hashCode() 
    {
        int result = 17 ;
        for (int ctr=0; ctr<data.length; ctr++ )
            result = 37*result + data[ctr] ;
        return result ;
    }

    public GenericIdentifiable(int id, byte[] data) 
    {
        this.id = id ;
        this.data = (byte[])(data.clone()) ;
    }
    
    @ManagedAttribute
    @Description( "The tagged component or profile CDR encoded data" )
    public byte[] getData() 
    {
        return (byte[])data.clone() ;
    }
}
