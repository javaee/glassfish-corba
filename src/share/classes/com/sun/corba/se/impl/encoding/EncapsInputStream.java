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

package com.sun.corba.se.impl.encoding;

import java.nio.ByteBuffer;
import org.omg.CORBA.CompletionStatus;
import com.sun.org.omg.SendingContext.CodeBase;
import com.sun.corba.se.spi.ior.iiop.GIOPVersion;
import com.sun.corba.se.impl.orbutil.ORBUtility;

import com.sun.corba.se.spi.orb.ORB;

import com.sun.corba.se.impl.logging.ORBUtilSystemException;

/**
 * Encapsulations are supposed to explicitly define their
 * code sets and GIOP version.  The original resolution to issue 2784 
 * said that the defaults were UTF-8 and UTF-16, but that was not
 * agreed upon.
 *
 * These streams currently use CDR 1.2 with ISO8859-1 for char/string and
 * UTF16 for wchar/wstring.  If no byte order marker is available,
 * the endianness of the encapsulation is used.
 *
 * When more encapsulations arise that have their own special code
 * sets defined, we can make all constructors take such parameters.
 */
public class EncapsInputStream extends CDRInputObject
{
    private ORBUtilSystemException wrapper ;

    // corba/EncapsOutputStream
    // corba/ORBSingleton
    // iiop/ORB
    public EncapsInputStream(org.omg.CORBA.ORB orb, byte[] buf, 
			     int size, boolean littleEndian,
			     GIOPVersion version) {
        super(orb, ByteBuffer.wrap(buf), size, littleEndian,
	      version, ORBUtility.getEncodingVersion(),
	      BufferManagerFactory.newBufferManagerRead(
				      BufferManagerFactory.GROW,
				      ORBUtility.getEncodingVersion(),
				      (ORB)orb),
	      false); // IDLJavaSerializationInputStream::directRead == false

	wrapper = ((ORB)orb).getLogWrapperTable().get_RPC_ENCODING_ORBUtil() ;

        performORBVersionSpecificInit();
    }

    public EncapsInputStream(org.omg.CORBA.ORB orb, ByteBuffer byteBuffer, 
                             int size, boolean littleEndian,
                             GIOPVersion version) {
        super(orb, byteBuffer, size, littleEndian, 
              version, ORBUtility.getEncodingVersion(),
              BufferManagerFactory.newBufferManagerRead(
				      BufferManagerFactory.GROW,
				      ORBUtility.getEncodingVersion(),
				      (com.sun.corba.se.spi.orb.ORB)orb),
	      false); // IDLJavaSerializationInputStream::directRead == false

        performORBVersionSpecificInit();
    }

    // ior/IdentifiableBase
    // ior/IIOPProfile
    // corba/ORBSingleton
    // iiop/ORB
    public EncapsInputStream(org.omg.CORBA.ORB orb, byte[] data, int size) 
    {
        this(orb, data, size, GIOPVersion.V1_2);
    }
    
    // corba/AnyImpl
    public EncapsInputStream(EncapsInputStream eis) 
    {
        super(eis);

	wrapper = 
	    ((ORB)(eis.orb())).getLogWrapperTable().get_RPC_ENCODING_ORBUtil() ;

        performORBVersionSpecificInit();
    }

    // CDREncapsCodec
    // ServiceContext
    //
    // Assumes big endian (can use consumeEndian to read and set
    // the endianness if it is an encapsulation with a byte order
    // mark at the beginning)
    public EncapsInputStream(org.omg.CORBA.ORB orb, byte[] data, int size, GIOPVersion version) 
    {
        this(orb, data, size, false, version);
    }

    /**
     * Full constructor with a CodeBase parameter useful for
     * unmarshaling RMI-IIOP valuetypes (technically against the
     * intention of an encapsulation, but necessary due to OMG
     * issue 4795.  Used by ServiceContexts.
     */
    public EncapsInputStream(org.omg.CORBA.ORB orb, 
                             byte[] data, 
                             int size, 
                             GIOPVersion version, 
                             CodeBase codeBase) {
        super(orb, 
              ByteBuffer.wrap(data), 
              size, 
              false, 
              version, ORBUtility.getEncodingVersion(),
              BufferManagerFactory.newBufferManagerRead(
				      BufferManagerFactory.GROW,
				      ORBUtility.getEncodingVersion(),
				      (ORB)orb),
	      false); // IDLJavaSerializationInputStream::directRead == false

        this.codeBase = codeBase;

        performORBVersionSpecificInit();
    }

    public CDRInputObject dup() {
        return new EncapsInputStream(this);
    }

    protected CodeSetConversion.BTCConverter createCharBTCConverter() {
        return CodeSetConversion.impl().getBTCConverter(OSFCodeSetRegistry.ISO_8859_1);
    }

    protected CodeSetConversion.BTCConverter createWCharBTCConverter() {
        // Wide characters don't exist in GIOP 1.0
        if (getGIOPVersion().equals(GIOPVersion.V1_0))
	    throw wrapper.wcharDataInGiop10( CompletionStatus.COMPLETED_MAYBE);

        // In GIOP 1.1, we shouldn't have byte order markers.  Take the order
        // of the stream if we don't see them.
        if (getGIOPVersion().equals(GIOPVersion.V1_1))
            return CodeSetConversion.impl().getBTCConverter(OSFCodeSetRegistry.UTF_16,
                                                            isLittleEndian());

        // Assume anything else adheres to GIOP 1.2 requirements.
        //
        // Our UTF_16 converter will work with byte order markers, and if
        // they aren't present, it will use the provided endianness.
        //
        // With no byte order marker, it's big endian in GIOP 1.2.  
        // formal 00-11-03 15.3.16.
        return CodeSetConversion.impl().getBTCConverter(OSFCodeSetRegistry.UTF_16,
                                                        false);
    }

    @Override
    public CodeBase getCodeBase() {
        return codeBase;
    }

    private CodeBase codeBase;
}
