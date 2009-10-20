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

import org.omg.CORBA.CompletionStatus;

import com.sun.corba.se.spi.orb.ORB;

import com.sun.corba.se.spi.ior.iiop.GIOPVersion;

import com.sun.corba.se.spi.orbutil.ORBConstants;
import com.sun.corba.se.impl.orbutil.ORBUtility;

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
public class EncapsOutputStream extends CDROutputObject
{

    // REVISIT - Right now, EncapsOutputStream's do not use
    // pooled byte buffers. This is controlled by the following
    // static constant. This should be re-factored such that
    // the EncapsOutputStream doesn't know it's using pooled
    // byte buffers.
    final static boolean usePooledByteBuffers = false;

    // REVISIT - Right now, valuetypes in encapsulations will
    // only use stream format version 1, which may create problems
    // for service contexts or codecs (?).

    // corba/ORB
    // corba/ORBSingleton
    // iiop/ORB
    // iiop/GIOPImpl
    // corba/AnyImpl
    public EncapsOutputStream(ORB orb) {
        // GIOP version 1.2 with no fragmentation, big endian,
        // UTF8 for char data and UTF-16 for wide char data;
        this(orb, GIOPVersion.V1_2);
    }

    // CDREncapsCodec
    //
    // REVISIT.  A UTF-16 encoding with GIOP 1.1 will not work
    // with byte order markers.
    public EncapsOutputStream(ORB orb, GIOPVersion version) {
        this(orb, version, false);
    }    

    // Used by IIOPProfileTemplate
    // 
    public EncapsOutputStream(ORB orb, boolean isLittleEndian) {
        this(orb, GIOPVersion.V1_2, isLittleEndian);
    }

    public EncapsOutputStream(ORB orb,
			      GIOPVersion version, 
			      boolean isLittleEndian)
    {
        super(orb, version, ORBUtility.getEncodingVersion(), isLittleEndian,
	      BufferManagerFactory.newBufferManagerWrite(
                                        BufferManagerFactory.GROW,
					ORBUtility.getEncodingVersion(),
					orb),
	      ORBConstants.STREAM_FORMAT_VERSION_1,
              usePooledByteBuffers,
	      false); // IDLJavaSerializationOuputStream::directWrite == false
    }

    public org.omg.CORBA.portable.InputStream create_input_stream() {
        freeInternalCaches();

        return new EncapsInputStream(orb(),
                                     getByteBuffer(),
                                     getSize(),
                                     isLittleEndian(),
                                     getGIOPVersion());
    }
    
    protected CodeSetConversion.CTBConverter createCharCTBConverter() {
        return CodeSetConversion.impl().getCTBConverter(OSFCodeSetRegistry.ISO_8859_1);
    }

    protected CodeSetConversion.CTBConverter createWCharCTBConverter() {
        if (getGIOPVersion().equals(GIOPVersion.V1_0))
	    throw wrapper.wcharDataInGiop10(CompletionStatus.COMPLETED_MAYBE);            

        // In the case of GIOP 1.1, we take the byte order of the stream and don't
        // use byte order markers since we're limited to a 2 byte fixed width encoding.
        if (getGIOPVersion().equals(GIOPVersion.V1_1))
            return CodeSetConversion.impl().getCTBConverter(OSFCodeSetRegistry.UTF_16,
                                                            isLittleEndian(),
                                                            false);

        // Assume anything else meets GIOP 1.2 requirements
        //
        // Use byte order markers?  If not, use big endian in GIOP 1.2.  
        // (formal 00-11-03 15.3.16)

        boolean useBOM = ((ORB)orb()).getORBData().useByteOrderMarkersInEncapsulations();

        return CodeSetConversion.impl().getCTBConverter(OSFCodeSetRegistry.UTF_16, 
                                                        false, 
                                                        useBOM);
    }
}
