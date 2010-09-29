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

import org.omg.CORBA.Any;
import org.omg.CORBA.ORB;
import org.omg.CORBA.TypeCode;

import com.sun.corba.se.spi.ior.iiop.GIOPVersion;

import com.sun.corba.se.impl.corba.AnyImpl;
import com.sun.corba.se.impl.encoding.EncapsInputStream;
import com.sun.corba.se.impl.encoding.EncapsOutputStream;
import com.sun.corba.se.impl.orbutil.ORBUtility;
import com.sun.corba.se.spi.orbutil.ORBConstants;
import com.sun.corba.se.spi.logging.ORBUtilSystemException;

import org.omg.IOP.Codec;
import org.omg.IOP.CodecPackage.FormatMismatch;
import org.omg.IOP.CodecPackage.InvalidTypeForEncoding;
import org.omg.IOP.CodecPackage.TypeMismatch;

/**
 * CDREncapsCodec is an implementation of Codec, as described
 * in orbos/99-12-02, that supports CDR encapsulation version 1.0, 1.1, and
 * 1.2.  
 */
public final class CDREncapsCodec 
    extends org.omg.CORBA.LocalObject 
    implements Codec 
{
    // The ORB that created the factory this codec was created from
    private transient ORB orb;
    static final ORBUtilSystemException wrapper =
        ORBUtilSystemException.self ;

    // The GIOP version we are encoding for
    private transient GIOPVersion giopVersion;

    /*
     *******************************************************************
     * NOTE: CDREncapsCodec must remain immutable!  This is so that we
     * can pre-create CDREncapsCodecs for each version of GIOP in
     * CodecFactoryImpl.
     *******************************************************************/

    /**
     * Creates a new codec implementation.  Uses the given ORB to create
     * CDRInputStreams when necessary.
     *
     * @param orb The ORB to use to create a CDRInputStream or CDROutputStream
     * @param major The major version of GIOP we are encoding for
     * @param minor The minor version of GIOP we are encoding for
     */
    public CDREncapsCodec( ORB orb, int major, int minor ) {
        this.orb = orb;

        giopVersion = GIOPVersion.getInstance( (byte)major, (byte)minor );
    }

    /**
     * Convert the given any into a CDR encapsulated octet sequence 
     */
    public byte[] encode( Any data ) 
        throws InvalidTypeForEncoding 
    {
	if ( data == null ) 
	    throw wrapper.nullParamNoComplete() ;
        return encodeImpl( data, true );
    }

    /**
     * Decode the given octet sequence into an any based on a CDR 
     * encapsulated octet sequence.
     */
    public Any decode ( byte[] data ) 
        throws FormatMismatch 
    {
	if( data == null ) 
	    throw wrapper.nullParamNoComplete() ;
	return decodeImpl( data, null );
    }

    /**
     * Convert the given any into a CDR encapsulated octet sequence.  Only
     * the data is stored.  The type code is not.
     */
    public byte[] encode_value( Any data ) 
        throws InvalidTypeForEncoding 
    {
	if( data == null ) 
	    throw wrapper.nullParamNoComplete() ;
        return encodeImpl( data, false );
    }

    /**
     * Decode the given octet sequence into an any based on a CDR 
     * encapsulated octet sequence.  The type code is expected not to appear
     * in the octet sequence, and the given type code is used instead.
     */
    public Any decode_value( byte[] data, TypeCode tc ) 
        throws FormatMismatch, TypeMismatch
    {
	if( data == null ) 
	    throw wrapper.nullParamNoComplete() ;
	if( tc == null ) 
	    throw  wrapper.nullParamNoComplete() ;
	return decodeImpl( data, tc );
    }

    /**
     * Convert the given any into a CDR encapsulated octet sequence.  
     * If sendTypeCode is true, the type code is sent with the message, as in
     * a standard encapsulation.  If it is false, only the data is sent.
     * Either way, the endian type is sent as the first part of the message.
     */
    private byte[] encodeImpl( Any data, boolean sendTypeCode ) 
        throws InvalidTypeForEncoding 
    {
	if( data == null ) 
	    throw wrapper.nullParamNoComplete() ;

	// _REVISIT_ Note that InvalidTypeForEncoding is never thrown in
	// the body of this method.  This is due to the fact that CDR*Stream
	// will never throw an exception if the encoding is invalid.  To
	// fix this, the CDROutputStream must know the version of GIOP it
	// is encoding for and it must check to ensure that, for example,
	// wstring cannot be encoded in GIOP 1.0.
	//
	// As part of the GIOP 1.2 work, the CDRInput and OutputStream will
	// be versioned.  This can be handled once this work is complete.

	byte[] retValue;

	// Always use CDR encoding for codec streams. If the thread local
	// encoding version is set to JSG, push CDR encoding to the thread
	// local state, and pop it at the end of this method.

	boolean pop = false;
	if (ORBUtility.getEncodingVersion() !=
	    ORBConstants.CDR_ENC_VERSION) {
	    ORBUtility.pushEncVersionToThreadLocalState(ORBConstants.CDR_ENC_VERSION);
	    pop = true;
	}

	try {

	    // Create output stream with default endianness.
	    EncapsOutputStream cdrOut =
		new EncapsOutputStream((com.sun.corba.se.spi.orb.ORB)orb,
				       giopVersion);

	    // This is an encapsulation, so put out the endian:
	    cdrOut.putEndian();

	    // Sometimes encode type code:
	    if( sendTypeCode ) {
		cdrOut.write_TypeCode( data.type() );
	    }

	    // Encode value and return.
	    data.write_value( cdrOut );

	    retValue = cdrOut.toByteArray();

	} finally {
	    if (pop) {
		ORBUtility.popEncVersionFromThreadLocalState();
	    }
	}
	
	return retValue;
    }

    /**
     * Decode the given octet sequence into an any based on a CDR 
     * encapsulated octet sequence.  If the type code is null, it is
     * expected to appear in the octet sequence.  Otherwise, the given
     * type code is used.
     */
    private Any decodeImpl( byte[] data, TypeCode tc ) 
        throws FormatMismatch 
    {
	if( data == null ) 
	    throw wrapper.nullParamNoComplete() ;

	AnyImpl any = null;  // return value

	// _REVISIT_ Currently there is no way for us to distinguish between
	// a FormatMismatch and a TypeMismatch because we cannot get this
	// information from the CDRInputStream.  If a RuntimeException occurs,
	// it is turned into a FormatMismatch exception.

	// Always use CDR encoding for codec streams. If the thread local
	// encoding version is set to JSG, push CDR encoding to the thread
	// local state, and pop it at the end of this method.

	boolean pop = false;
	if (ORBUtility.getEncodingVersion() !=
	    ORBConstants.CDR_ENC_VERSION) {
	    ORBUtility.pushEncVersionToThreadLocalState(ORBConstants.CDR_ENC_VERSION);
	    pop = true;
	}

	try {

	    EncapsInputStream cdrIn = new EncapsInputStream( orb, data, 
                data.length, giopVersion );

	    cdrIn.consumeEndian();

	    // If type code not specified, read it from octet stream:
	    if( tc == null ) {
		tc = cdrIn.read_TypeCode();
	    }

	    // Create a new Any object:
	    any = new AnyImpl( (com.sun.corba.se.spi.orb.ORB)orb );
	    any.read_value( cdrIn, tc );

	} catch( RuntimeException e ) {
	    // See above note.  
	    throw new FormatMismatch();
	} finally {
	    if (pop) {
		ORBUtility.popEncVersionFromThreadLocalState();
	    }
	}

	return any;
    }
}
