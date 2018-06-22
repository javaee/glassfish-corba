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

import org.omg.CORBA.OctetSeqHolder ;

import org.omg.CORBA_2_3.portable.InputStream ;
import org.omg.CORBA_2_3.portable.OutputStream ;

import com.sun.corba.ee.spi.ior.ObjectId ;

import com.sun.corba.ee.spi.orb.ORB ;
import com.sun.corba.ee.spi.orb.ORBVersionFactory ;


import com.sun.corba.ee.impl.encoding.CDRInputObject ;

/**
 * Handles object keys created by JDK ORBs from before JDK 1.4.0. 
 */
public final class OldJIDLObjectKeyTemplate extends OldObjectKeyTemplateBase
{
    /**
     * JDK 1.3.1 FCS did not include a version byte at the end of
     * its object keys.  JDK 1.3.1_01 included the byte with the
     * value 1.  Anything below 1 is considered an invalid value.
     */
    public static final byte NULL_PATCH_VERSION = 0;

    byte patchVersion = OldJIDLObjectKeyTemplate.NULL_PATCH_VERSION;

    public OldJIDLObjectKeyTemplate( ORB orb, int magic, int scid, 
        InputStream is, OctetSeqHolder osh ) 
    {
        this( orb, magic, scid, is );

        osh.value = readObjectKey( is ) ;
        
        /**
         * Beginning with JDK 1.3.1_01, a byte was placed at the end of
         * the object key with a value indicating the patch version.
         * JDK 1.3.1_01 had the value 1.  If other patches are necessary
         * which involve ORB versioning changes, they should increment
         * the patch version.
         *
         * Note that if we see a value greater than 1 in this code, we
         * will treat it as if we're talking to the most recent ORB version.
         *
         * WARNING: This code is sensitive to changes in CDRInputStream
         * getPosition.  It assumes that the CDRInputStream is an
         * encapsulation whose position can be compared to the object
         * key array length.
         */
        if (magic == ObjectKeyFactoryImpl.JAVAMAGIC_NEW &&
            osh.value.length > ((CDRInputObject)is).getPosition()) {

            patchVersion = is.read_octet();

            if (patchVersion == ObjectKeyFactoryImpl.JDK1_3_1_01_PATCH_LEVEL) {
                setORBVersion(ORBVersionFactory.getJDK1_3_1_01());
            } else if (patchVersion > ObjectKeyFactoryImpl.JDK1_3_1_01_PATCH_LEVEL) {
                setORBVersion(ORBVersionFactory.getORBVersion());
            } else {
                throw wrapper.invalidJdk131PatchLevel(patchVersion);
            }
        }
    }
    
    
    public OldJIDLObjectKeyTemplate( ORB orb, int magic, int scid, int serverid) 
    {
        super( orb, magic, scid, serverid, JIDL_ORB_ID, JIDL_OAID ) ; 
    }
   
    public OldJIDLObjectKeyTemplate(ORB orb, int magic, int scid, InputStream is) 
    {
        this( orb, magic, scid, is.read_long() ) ; 
    }
   
    protected void writeTemplate( OutputStream os )
    {
        os.write_long( getMagic() ) ;
        os.write_long( getSubcontractId() ) ;
        os.write_long( getServerId() ) ;
    }

    @Override
    public void write(ObjectId objectId, OutputStream os) 
    {
        super.write(objectId, os);

        if (patchVersion != OldJIDLObjectKeyTemplate.NULL_PATCH_VERSION) {
            os.write_octet(patchVersion);
        }
    }
}
