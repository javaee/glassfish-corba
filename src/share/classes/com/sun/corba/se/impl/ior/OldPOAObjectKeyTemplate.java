/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2002-2010 Sun Microsystems, Inc. All rights reserved.
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

import org.omg.CORBA.INTERNAL ;
import org.omg.CORBA.OctetSeqHolder ;

import org.omg.CORBA_2_3.portable.InputStream ;
import org.omg.CORBA_2_3.portable.OutputStream ;

import com.sun.corba.se.spi.orb.ORB ;
import com.sun.corba.se.spi.orb.ORBVersion ;
import com.sun.corba.se.spi.orb.ORBVersionFactory ;


/**
 * @author Ken Cavanaugh
 */
public final class OldPOAObjectKeyTemplate extends OldObjectKeyTemplateBase 
{
    /** This constructor reads the template ONLY from the stream
    */
    public OldPOAObjectKeyTemplate( ORB orb, int magic, int scid, InputStream is ) 
    {
	this( orb, magic, scid, is.read_long(), is.read_long(), is.read_long() ) ;
    }
    
    /** This constructor reads a complete ObjectKey (template and Id)
    * from the stream.
    */
    public OldPOAObjectKeyTemplate( ORB orb, int magic, int scid, InputStream is,
	OctetSeqHolder osh ) 
    {
	this( orb, magic, scid, is ) ;
	osh.value = readObjectKey( is ) ;
    }
    
    public OldPOAObjectKeyTemplate( ORB orb, int magic, int scid, int serverid, 
	int orbid, int poaid) 
    {
	super( orb, magic, scid, serverid,
	    Integer.toString( orbid ), 
	    new ObjectAdapterIdNumber( poaid ) ) ;
    }
    
    public void writeTemplate(OutputStream os) 
    {
	os.write_long( getMagic() ) ;
	os.write_long( getSubcontractId() ) ;
	os.write_long( getServerId() ) ;

	int orbid = Integer.parseInt( getORBId() ) ;
	os.write_long( orbid ) ;

	ObjectAdapterIdNumber oaid = (ObjectAdapterIdNumber)(getObjectAdapterId()) ;
	int poaid = oaid.getOldPOAId()  ;
	os.write_long( poaid ) ;
    }
 
    @Override
    public ORBVersion getORBVersion()
    {
	if (getMagic() == ObjectKeyFactoryImpl.JAVAMAGIC_OLD)
	    return ORBVersionFactory.getOLD() ;
	else if (getMagic() == ObjectKeyFactoryImpl.JAVAMAGIC_NEW)
	    return ORBVersionFactory.getNEW() ;
	else
	    throw new INTERNAL() ;
    }
}

