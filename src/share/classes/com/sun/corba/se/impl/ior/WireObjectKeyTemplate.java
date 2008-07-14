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

import org.omg.CORBA.OctetSeqHolder ;

import org.omg.CORBA_2_3.portable.OutputStream ;
import org.omg.CORBA_2_3.portable.InputStream ;

import com.sun.corba.se.spi.protocol.CorbaServerRequestDispatcher ;

import com.sun.corba.se.spi.ior.ObjectKeyTemplate ;
import com.sun.corba.se.spi.ior.ObjectId ;
import com.sun.corba.se.spi.ior.ObjectAdapterId ;

import com.sun.corba.se.spi.orb.ORB ;
import com.sun.corba.se.spi.orb.ORBVersion ;
import com.sun.corba.se.spi.orb.ORBVersionFactory ;

import com.sun.corba.se.spi.orbutil.ORBConstants ;

import com.sun.corba.se.impl.encoding.CDRInputStream ;

import com.sun.corba.se.impl.logging.IORSystemException ;

/**
 * @author Ken Cavanaugh
 */
public class WireObjectKeyTemplate implements ObjectKeyTemplate 
{
    private ORB orb ;
    private IORSystemException wrapper ;

    public boolean equals( Object obj )
    {
	if (obj == null)
	    return false ;

	return obj instanceof WireObjectKeyTemplate ;
    }

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
	wrapper = orb.getLogWrapperTable().get_OA_IOR_IOR() ;
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

    /** While it might make sense to throw an exception here, this causes
    * problems since we need to check whether unusual object references
    * are local or not.  It seems that the easiest way to handle this is
    * to return an invalid server id.
    */
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
	throw wrapper.objectAdapterIdNotAvailable() ;
    }

    /** Adapter ID is not available, since our
    * ORB did not implement the object carrying this key.
    */
    public byte[] getAdapterId()
    {
	throw wrapper.adapterIdNotAvailable() ;
    }

    public ORBVersion getORBVersion() 
    {
	return ORBVersionFactory.getFOREIGN() ;
    }

    public CorbaServerRequestDispatcher getServerRequestDispatcher( ORB orb, ObjectId id ) 
    {
	byte[] bid = id.getId() ;
	String str = new String( bid ) ;
	return orb.getRequestDispatcherRegistry().getServerRequestDispatcher( str ) ;
    }
}
