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


import org.omg.CORBA_2_3.portable.OutputStream ;

import com.sun.corba.ee.spi.protocol.ServerRequestDispatcher ;

import com.sun.corba.ee.spi.ior.ObjectKeyTemplate ;
import com.sun.corba.ee.spi.ior.ObjectId ;
import com.sun.corba.ee.spi.ior.ObjectAdapterId ;

import com.sun.corba.ee.spi.orb.ORB ;
import com.sun.corba.ee.spi.orb.ORBVersion ;
import com.sun.corba.ee.spi.orb.ORBVersionFactory ;

import com.sun.corba.ee.spi.misc.ORBConstants ;

import com.sun.corba.ee.spi.logging.IORSystemException ;

/**
 * @author Ken Cavanaugh
 */
public class WireObjectKeyTemplate implements ObjectKeyTemplate 
{
    private ORB orb ;
    private static final IORSystemException wrapper =
        IORSystemException.self ;
    private static ObjectAdapterId NULL_OBJECT_ADAPTER_ID = 
        new ObjectAdapterIdArray( new String[0] ) ;

    @Override
    public boolean equals( Object obj )
    {
        if (obj == null) {
            return false ;
        }

        return obj instanceof WireObjectKeyTemplate ;
    }

    @Override
    public int hashCode()
    {
        return 53 ; // All WireObjectKeyTemplates are the same, so they should 
                    // have the same hashCode.
    }

    public WireObjectKeyTemplate( ORB orb )
    {
        initORB( orb ) ;
    }

    private void initORB( ORB orb ) 
    {
        this.orb = orb ;
    }

    public void write( ObjectId id, OutputStream os ) 
    {
        byte[] key = id.getId() ;
        os.write_octet_array( key, 0, key.length ) ;
    }

    public void write( OutputStream os ) 
    {
        // Does nothing
    }

    public int getSubcontractId()
    {
        return ORBConstants.DEFAULT_SCID ;
    }

    // While it might make sense to throw an exception here, this causes
    // problems since we need to check whether unusual object references
    // are local or not.  It seems that the easiest way to handle this is
    // to return an invalid server id.
    public int getServerId() 
    {
        return -1 ;
    }

    public String getORBId()
    {
        throw wrapper.orbIdNotAvailable() ;
    }

    public ObjectAdapterId getObjectAdapterId() 
    {
        return NULL_OBJECT_ADAPTER_ID ;

        // throw wrapper.objectAdapterIdNotAvailable() ;
    }

    // Adapter ID is not available, since our
    // ORB did not implement the object carrying this key.
    public byte[] getAdapterId()
    {
        throw wrapper.adapterIdNotAvailable() ;
    }

    public ORBVersion getORBVersion() 
    {
        return ORBVersionFactory.getFOREIGN() ;
    }

    public ServerRequestDispatcher getServerRequestDispatcher( ObjectId id )
    {
        byte[] bid = id.getId() ;
        String str = new String( bid ) ;
        return orb.getRequestDispatcherRegistry().getServerRequestDispatcher(
            str ) ;
    }
}
