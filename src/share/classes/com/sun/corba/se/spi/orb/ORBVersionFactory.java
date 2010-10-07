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

package com.sun.corba.se.spi.orb ;

import com.sun.corba.se.spi.orb.ORBVersion ;
import com.sun.corba.se.impl.orb.ORBVersionImpl ;
import org.omg.CORBA.portable.InputStream ;
import org.omg.CORBA.INTERNAL ;

public class ORBVersionFactory {
    private ORBVersionFactory() {} ;

    public static ORBVersion getFOREIGN() 
    {
	return ORBVersionImpl.FOREIGN ;
    }

    public static ORBVersion getOLD() 
    {
	return ORBVersionImpl.OLD ;
    }

    public static ORBVersion getNEW() 
    {
	return ORBVersionImpl.NEW ;
    }

    public static ORBVersion getJDK1_3_1_01() 
    {
	return ORBVersionImpl.JDK1_3_1_01 ;
    }

    public static ORBVersion getNEWER() 
    {
	return ORBVersionImpl.NEWER ;
    }

    public static ORBVersion getPEORB() 
    {
	return ORBVersionImpl.PEORB ;
    }

    /** Return the current version of this ORB
     */
    public static ORBVersion getORBVersion()
    {
	return ORBVersionImpl.PEORB ;
    }

    public static ORBVersion create( InputStream is ) 
    {
	byte value = is.read_octet() ;
	return byteToVersion( value ) ;
    }

    private static ORBVersion byteToVersion( byte value ) 
    {
	/* Throwing an exception here would cause this version to be 
	* incompatible with future versions of the ORB, to the point 
	* that this version could
	* not even unmarshal objrefs from a newer version that uses 
	* extended versioning.  Therefore, we will simply treat all 
	* unknown versions as the latest version.
	if (value < 0)
	    throw new INTERNAL() ;
	*/

	/**
	 * Update: If we treat all unknown versions as the latest version
	 * then when we send an IOR with a PEORB version to an ORB that
	 * doesn't know the PEORB version it will treat it as whatever
	 * its idea of the latest version is.  Then, if that IOR is
	 * sent back to the server and compared with the original
	 * the equality check will fail because the versions will be
	 * different.
	 *
	 * Instead, just capture the version bytes.
	 */

	switch (value) {
	    case ORBVersion.FOREIGN : return ORBVersionImpl.FOREIGN ;
	    case ORBVersion.OLD : return ORBVersionImpl.OLD ;
	    case ORBVersion.NEW : return ORBVersionImpl.NEW ;
            case ORBVersion.JDK1_3_1_01: return ORBVersionImpl.JDK1_3_1_01 ;
	    case ORBVersion.NEWER : return ORBVersionImpl.NEWER ;
	    case ORBVersion.PEORB : return ORBVersionImpl.PEORB ;
	    default : return new ORBVersionImpl(value); 
	}
    }
}
