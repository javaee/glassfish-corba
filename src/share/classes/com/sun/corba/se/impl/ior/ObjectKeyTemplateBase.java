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
import org.omg.CORBA_2_3.portable.OutputStream ;

import com.sun.corba.se.spi.protocol.ServerRequestDispatcher ;

import com.sun.corba.se.spi.ior.ObjectId ;
import com.sun.corba.se.spi.ior.ObjectAdapterId ;
import com.sun.corba.se.spi.ior.ObjectKeyTemplate ;

import com.sun.corba.se.spi.orb.ORB ;
import com.sun.corba.se.spi.orb.ORBVersion ;


import com.sun.corba.se.spi.logging.IORSystemException ;

/**
 * @author Ken Cavanaugh
 */
public abstract class ObjectKeyTemplateBase implements ObjectKeyTemplate 
{
    protected static final IORSystemException wrapper =
        IORSystemException.self ;

    // Fixed constants for Java IDL object key template forms
    public static final String JIDL_ORB_ID = "" ;
    private static final String[] JIDL_OAID_STRINGS = { "TransientObjectAdapter" } ;
    public static final ObjectAdapterId JIDL_OAID = new ObjectAdapterIdArray( JIDL_OAID_STRINGS ) ;

    private ORB orb ;
    private ORBVersion version ;
    private int magic ;
    private int scid ;
    private int serverid ;
    private String orbid ;
    private ObjectAdapterId oaid ;

    private byte[] adapterId ;

    public String toString() {
        StringBuilder sb = new StringBuilder() ;
        sb.append( "ObjectKeyTemplate[magic=") ;
        sb.append( Integer.toHexString( magic ) ) ;
        sb.append( " scid=" ) ;
        sb.append( scid ) ;
        sb.append( " serverid=") ;
        sb.append( serverid )  ;
        sb.append( " orbid=") ;
        sb.append( orbid ) ;
        sb.append( " oaid=" ) ;
        sb.append( oaid.toString() ) ;
        return sb.toString() ;
    }

    public synchronized byte[] getAdapterId() {
        if (adapterId == null) {
            adapterId = computeAdapterId();
        }

        return (byte[])(adapterId.clone()) ;
    }

    private byte[] computeAdapterId()
    {
        // write out serverid, orbid, oaid
        ByteBuffer buff = new ByteBuffer() ;

        buff.append( getServerId() ) ;
        buff.append( orbid ) ;

        buff.append( oaid.getNumLevels() ) ;
        for (String comp : oaid) {
            buff.append( comp ) ;
        }

        buff.trimToSize() ;

        return buff.toArray() ;
    }

    public ObjectKeyTemplateBase( ORB orb, int magic, int scid, int serverid, 
	String orbid, ObjectAdapterId oaid ) 
    {
	this.orb = orb ;
	this.magic = magic ;
	this.scid = scid ;
	this.serverid = serverid ;
	this.orbid = orbid ;
	this.oaid = oaid ;

        adapterId = null;
    }

    @Override
    public boolean equals( Object obj ) {
        if (!(obj instanceof ObjectKeyTemplateBase))
            return false ;

        ObjectKeyTemplateBase other = (ObjectKeyTemplateBase)obj ;

        return (magic == other.magic) && (scid == other.scid) &&
            (serverid == other.serverid) && (version.equals( other.version ) &&
            orbid.equals( other.orbid ) && oaid.equals( other.oaid )) ;
    }
   
    public int hashCode() {
        int result = 17 ;
        result = 37*result + magic ;
        result = 37*result + scid ;
        result = 37*result + serverid ;
        result = 37*result + version.hashCode() ;
        result = 37*result + orbid.hashCode() ;
        result = 37*result + oaid.hashCode() ;
        return result ;
    }

    public int getSubcontractId() {
        return scid ;
    }

    public int getServerId() {
        return serverid ;
    }

    public String getORBId() {
        return orbid ;
    }

    public ObjectAdapterId getObjectAdapterId() {
        return oaid ;
    }

    public void write(ObjectId objectId, OutputStream os) {
        writeTemplate( os ) ;
        objectId.write( os ) ;
    }

    public void write( OutputStream os )
    {
        writeTemplate( os ) ;
    }

    abstract protected void writeTemplate( OutputStream os ) ;
   
    protected int getMagic() {
        return magic ;
    }

    // All subclasses should set the version in their constructors.
    // Public so it can be used in a white-box test.
    public void setORBVersion( ORBVersion version ) {
        this.version = version ;
    }

    public ORBVersion getORBVersion() {
        return version ;
    }

    protected byte[] readObjectKey( InputStream is ) {
        int len = is.read_long() ;
        byte[] result = new byte[len] ;
        is.read_octet_array( result, 0, len ) ;
        return result ;
    }

    public ServerRequestDispatcher getServerRequestDispatcher(
        ObjectId id ) {

        return orb.getRequestDispatcherRegistry().getServerRequestDispatcher(
            scid ) ;
    }
}
