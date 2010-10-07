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

package com.sun.corba.se.impl.interceptors;

import org.omg.IOP.Codec;
import org.omg.IOP.CodecFactory;
import org.omg.IOP.CodecFactoryPackage.UnknownEncoding;
import org.omg.IOP.Encoding;
import org.omg.IOP.ENCODING_CDR_ENCAPS;

import com.sun.corba.se.impl.logging.ORBUtilSystemException;

import org.omg.CORBA.ORB;
import org.omg.CORBA.LocalObject;

/**
 * CodecFactoryImpl is the implementation of the Codec Factory, as described
 * in orbos/99-12-02.  
 */
public final class CodecFactoryImpl 
    extends org.omg.CORBA.LocalObject
    implements CodecFactory 
{
    // The ORB that created this Codec Factory
    private transient ORB orb;
    private transient ORBUtilSystemException wrapper ;

    // The maximum minor version of GIOP supported by this codec factory.
    // Currently, this is 1.2.
    private static final int MAX_MINOR_VERSION_SUPPORTED = 2;

    // The pre-created minor versions of Codec version 1.0, 1.1, ...,
    // 1.(MAX_MINOR_VERSION_SUPPORTED)
    private Codec codecs[] = new Codec[MAX_MINOR_VERSION_SUPPORTED + 1];

    /**
     * Creates a new CodecFactory implementation.  Stores the ORB that
     * created this factory, for later use by the Codec.
     */
    public CodecFactoryImpl( ORB orb ) {
        this.orb = orb;
	wrapper = ((com.sun.corba.se.spi.orb.ORB)orb)
	    .getLogWrapperTable().get_RPC_PROTOCOL_ORBUtil() ;

	// Precreate a codec for version 1.0 through 
	// 1.(MAX_MINOR_VERSION_SUPPORTED).  This can be
	// done since Codecs are immutable in their current implementation.
	// This is an optimization that eliminates the overhead of creating
	// a new Codec each time create_codec is called.
	for( int minor = 0; minor <= MAX_MINOR_VERSION_SUPPORTED; minor++ ) {
	    codecs[minor] = new CDREncapsCodec( orb, 1, minor );
        }
    }

    /**
     * Creates a codec of the given encoding.  The only format recognized
     * by this factory is ENCODING_CDR_ENCAPS, versions 1.0 through 
     * 1.(MAX_MINOR_VERSION_SUPPORTED).
     *
     * @exception UnknownEncoding Thrown if this factory cannot create a 
     *   Codec of the given encoding.
     */
    public Codec create_codec ( Encoding enc ) 
        throws UnknownEncoding 
    {
        if( enc == null ) nullParam();

	Codec result = null;

	// This is the only format we can currently create codecs for:
	if( (enc.format == ENCODING_CDR_ENCAPS.value) &&
            (enc.major_version == 1) ) 
        {
	    if( (enc.minor_version >= 0) && 
		(enc.minor_version <= MAX_MINOR_VERSION_SUPPORTED) ) 
            {
		result = codecs[enc.minor_version];
	    }
	}

	if( result == null ) {
	    throw new UnknownEncoding();
	}

	return result;
    }

    /**
     * Called when an invalid null parameter was passed.  Throws a
     * BAD_PARAM with a minor code of 1
     */
    private void nullParam() 
    {
	throw wrapper.nullParam() ;
    }
}
