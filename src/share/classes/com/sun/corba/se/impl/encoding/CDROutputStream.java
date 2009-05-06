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

import java.io.IOException;
import java.io.Serializable;
import java.math.BigDecimal;
import java.nio.ByteBuffer;

import org.omg.CORBA.TypeCode;
import org.omg.CORBA.Any;

import com.sun.corba.se.pept.protocol.MessageMediator;

import com.sun.corba.se.spi.orb.ORB;
import com.sun.corba.se.spi.ior.iiop.GIOPVersion;
import com.sun.corba.se.spi.protocol.CorbaMessageMediator;

import com.sun.corba.se.impl.encoding.CodeSetConversion;
import com.sun.corba.se.impl.encoding.OSFCodeSetRegistry;
import com.sun.corba.se.spi.orbutil.ORBConstants;
import com.sun.corba.se.impl.orbutil.newtimer.generated.TimingPoints;
import com.sun.corba.se.impl.logging.ORBUtilSystemException;

/**
 * This is delegates to the real implementation.
 */
@SuppressWarnings({"deprecation"})
public abstract class CDROutputStream
    extends org.omg.CORBA_2_3.portable.OutputStream
    implements com.sun.corba.se.impl.encoding.MarshalOutputStream,
               org.omg.CORBA.DataOutputStream, org.omg.CORBA.portable.ValueOutputStream
{
    private CDROutputStreamBase impl;
    protected TimingPoints tp ;
    protected CorbaMessageMediator corbaMessageMediator;
    protected ORBUtilSystemException wrapper ; // needed in subclasses

    // We can move this out somewhere later.  For now, it serves its purpose
    // to create a concrete CDR delegate based on the GIOP version.
    private static class OutputStreamFactory {
        
        public static CDROutputStreamBase newOutputStream(
	        ORB orb, GIOPVersion version, byte encodingVersion,
		boolean directWrite) {
            switch(version.intValue()) {
                case GIOPVersion.VERSION_1_0:
                    return new CDROutputStream_1_0();
                case GIOPVersion.VERSION_1_1:
                    return new CDROutputStream_1_1();
	    case GIOPVersion.VERSION_1_2:
		if (encodingVersion != ORBConstants.CDR_ENC_VERSION) {
		    // Assumes JAVA_ENC_VERSION == 1
		    return new IDLJavaSerializationOutputStream(directWrite);
		}
		return new CDROutputStream_1_2();
	    default:
		    ORBUtilSystemException wrapper = 
			orb.getLogWrapperTable().get_RPC_ENCODING_ORBUtil() ;
                    // REVISIT - what is appropriate?  INTERNAL exceptions
                    // are really hard to track later.
		    throw wrapper.unsupportedGiopVersion( version ) ;
            }
        }
    }

    // REVISIT - These two constructors should be re-factored to better hide
    // the fact that someone extending this class 'can' construct a CDROutputStream
    // that does not use pooled ByteBuffers. Right now, only EncapsOutputStream
    // does _not_ use pooled ByteBuffers, see EncapsOutputStream.

    // NOTE: When a stream is constructed for non-channel-backed sockets
    // it notifies the constructor not to use pooled (i.e, direct)
    // ByteBuffers.

    // Called from EncapsOutputStream.
    public CDROutputStream(ORB orb,
                           GIOPVersion version,
			   byte encodingVersion,
			   boolean littleEndian,
			   BufferManagerWrite bufferManager,
                           byte streamFormatVersion,
                           boolean usePooledByteBuffers,
			   boolean directWrite)
    {
	wrapper = orb.getLogWrapperTable().get_RPC_ENCODING_ORBUtil() ;
	this.tp = orb.getTimerManager().points() ;
	tp.enter_createCDROutputStream() ;
	try {
	    impl = OutputStreamFactory.newOutputStream(orb, version, 
						       encodingVersion,
						       directWrite);
	    impl.init(orb, littleEndian, bufferManager,
		      streamFormatVersion, usePooledByteBuffers);

	    impl.setParent(this);
	} finally {
	    tp.exit_createCDROutputStream() ;
	}
    }

    public CDROutputStream(ORB orb,
                           GIOPVersion version,
			   byte encodingVersion,
			   boolean littleEndian,
			   BufferManagerWrite bufferManager,
                           byte streamFormatVersion,
                           boolean usePooledByteBuffers) {
	this(orb, version, encodingVersion, littleEndian, 
	     bufferManager, streamFormatVersion, usePooledByteBuffers, true);
    }

    public CDROutputStream(ORB orb,
                           GIOPVersion version,
			   byte encodingVersion,
			   boolean littleEndian,
			   BufferManagerWrite bufferManager,
                           byte streamFormatVersion)
    {
        this(orb, version, encodingVersion, littleEndian, 
	     bufferManager, streamFormatVersion, true);
    }

    // org.omg.CORBA.portable.OutputStream

    // Provided by IIOPOutputStream and EncapsOutputStream
    public abstract org.omg.CORBA.portable.InputStream create_input_stream();

    public final void write_boolean(boolean value) {
	tp.enter_writeBooleanToCDRStream() ;
	try {
	    impl.write_boolean(value);
	} finally {
	    tp.exit_writeBooleanToCDRStream() ;
	}
    }
    public final void write_char(char value) {
	tp.enter_writeCharToCDRStream() ;
	try {
	    impl.write_char(value);
	} finally {
	    tp.exit_writeCharToCDRStream() ;
	}
    }
    public final void write_wchar(char value) {
	tp.enter_writeWideCharToCDRStream() ;
	try {
	    impl.write_wchar(value);
	} finally {
	    tp.exit_writeWideCharToCDRStream() ;
	}
    }
    public final void write_octet(byte value) {
	tp.enter_writeOctetToCDRStream() ;
	try {
	    impl.write_octet(value);
	} finally {
	    tp.exit_writeOctetToCDRStream() ;
	}
    }
    public final void write_short(short value) {
	tp.enter_writeShortToCDRStream() ;
	try {
	    impl.write_short(value);
	} finally {
	    tp.exit_writeShortToCDRStream() ;
	}
    }
    public final void write_ushort(short value) {
	tp.enter_writeUnsignedShortToCDRStream() ;
	try {
	    impl.write_ushort(value);
	} finally {
	    tp.exit_writeUnsignedShortToCDRStream() ;
	}
    }
    public final void write_long(int value) {
	tp.enter_writeLongToCDRStream() ;
	try {
	    impl.write_long(value);
	} finally {
	    tp.exit_writeLongToCDRStream() ;
	}
    }
    public final void write_ulong(int value) {
	tp.enter_writeUnsignedLongToCDRStream() ;
	try {
	    impl.write_ulong(value);
	} finally {
	    tp.exit_writeUnsignedLongToCDRStream() ;
	}
    }
    public final void write_longlong(long value) {
	tp.enter_writeLongLongToCDRStream() ;
	try {
	    impl.write_longlong(value);
	} finally {
	    tp.exit_writeLongLongToCDRStream() ;
	}
    }
    public final void write_ulonglong(long value) {
	tp.enter_writeUnsignedLongLongToCDRStream() ;
	try {
	    impl.write_ulonglong(value);
	} finally {
	    tp.exit_writeUnsignedLongLongToCDRStream() ;
	}
    }
    public final void write_float(float value) {
	tp.enter_writeFloatToCDRStream() ;
	try {
	    impl.write_float(value);
	} finally {
	    tp.exit_writeFloatToCDRStream() ;
	}
    }
    public final void write_double(double value) {
	tp.enter_writeDoubleToCDRStream() ;
	try {
	    impl.write_double(value);
	} finally {
	    tp.exit_writeDoubleToCDRStream() ;
	}
    }
    public final void write_string(String value) {
	tp.enter_writeStringToCDRStream() ;
	try {
	    impl.write_string(value);
	} finally {
	    tp.exit_writeStringToCDRStream() ;
	}
    }
    public final void write_wstring(String value) {
	tp.enter_writeWideStringToCDRStream() ;
	try {
	    impl.write_wstring(value);
	} finally {
	    tp.exit_writeWideStringToCDRStream() ;
	}
    }

    public final void write_boolean_array(boolean[] value, int offset, int length) {
	tp.enter_writeBooleanArrayToCDRStream() ;
	try {
	    impl.write_boolean_array(value, offset, length);
	} finally {
	    tp.exit_writeBooleanArrayToCDRStream() ;
	}
    }
    public final void write_char_array(char[] value, int offset, int length) {
	tp.enter_writeCharArrayToCDRStream() ;
	try {
	    impl.write_char_array(value, offset, length);
	} finally {
	    tp.exit_writeCharArrayToCDRStream() ;
	}
    }
    public final void write_wchar_array(char[] value, int offset, int length) {
	tp.enter_writeWideCharArrayToCDRStream() ;
	try {
	    impl.write_wchar_array(value, offset, length);
	} finally {
	    tp.exit_writeWideCharArrayToCDRStream() ;
	}
    }
    public final void write_octet_array(byte[] value, int offset, int length) {
	tp.enter_writeOctetArrayToCDRStream() ;
	try {
	    impl.write_octet_array(value, offset, length);
	} finally {
	    tp.exit_writeOctetArrayToCDRStream() ;
	}
    }
    public final void write_short_array(short[] value, int offset, int length) {
	tp.enter_writeShortArrayToCDRStream() ;
	try {
	    impl.write_short_array(value, offset, length);
	} finally {
	    tp.exit_writeShortArrayToCDRStream() ;
	}
    }
    public final void write_ushort_array(short[] value, int offset, int length){
	tp.enter_writeUnsignedShortArrayToCDRStream() ;
	try {
	    impl.write_ushort_array(value, offset, length);
	} finally {
	    tp.exit_writeUnsignedShortArrayToCDRStream() ;
	}
    }
    public final void write_long_array(int[] value, int offset, int length) {
	tp.enter_writeLongArrayToCDRStream() ;
	try {
	    impl.write_long_array(value, offset, length);
	} finally {
	    tp.exit_writeLongArrayToCDRStream() ;
	}
    }
    public final void write_ulong_array(int[] value, int offset, int length) {
	tp.enter_writeUnsignedLongArrayToCDRStream() ;
	try {
	    impl.write_ulong_array(value, offset, length);
	} finally {
	    tp.exit_writeUnsignedLongArrayToCDRStream() ;
	}
    }
    public final void write_longlong_array(long[] value, int offset, int length) {
	tp.enter_writeLongLongArrayToCDRStream() ;
	try {
	    impl.write_longlong_array(value, offset, length);
	} finally {
	    tp.exit_writeLongLongArrayToCDRStream() ;
	}
    }
    public final void write_ulonglong_array(long[] value, int offset,int length) {
	tp.enter_writeUnsignedLongLongArrayToCDRStream() ;
	try {
	    impl.write_ulonglong_array(value, offset, length);
	} finally {
	    tp.exit_writeUnsignedLongLongArrayToCDRStream() ;
	}
    }
    public final void write_float_array(float[] value, int offset, int length) {
	tp.enter_writeFloatArrayToCDRStream() ;
	try {
	    impl.write_float_array(value, offset, length);
	} finally {
	    tp.exit_writeFloatArrayToCDRStream() ;
	}
    }
    public final void write_double_array(double[] value, int offset, int length) {
	tp.enter_writeDoubleArrayToCDRStream() ;
	try {
	    impl.write_double_array(value, offset, length);
	} finally {
	    tp.exit_writeDoubleArrayToCDRStream() ;
	}
    }
    public final void write_Object(org.omg.CORBA.Object value) {
	tp.enter_writeObjectToCDRStream() ;
	try {
	    impl.write_Object(value);
	} finally {
	    tp.exit_writeObjectToCDRStream() ;
	}
    }
    public final void write_TypeCode(TypeCode value) {
	tp.enter_writeTypeCodeToCDRStream() ;
	try {
	    impl.write_TypeCode(value);
	} finally {
	    tp.exit_writeTypeCodeToCDRStream() ;
	}
    }
    public final void write_any(Any value) {
	tp.enter_writeAnyToCDRStream() ;
	try {
	    impl.write_any(value);
	} finally {
	    tp.exit_writeAnyToCDRStream() ;
	}
    }

    @SuppressWarnings({"deprecation"})
    public final void write_Principal(org.omg.CORBA.Principal value) {
	tp.enter_writePrincipalToCDRStream() ;
	try {
	    impl.write_Principal(value);
	} finally {
	    tp.exit_writePrincipalToCDRStream() ;
	}
    }

    public final void write(int b) throws java.io.IOException {
	tp.enter_writeIntToCDRStream() ;
	try {
	    impl.write(b);
	} finally {
	    tp.exit_writeIntToCDRStream() ;
	}
    }
    
    public final void write_fixed(java.math.BigDecimal value) {
	tp.enter_writeFixedToCDRStream() ;
	try {
	    impl.write_fixed(value);
	} finally {
	    tp.exit_writeFixedToCDRStream() ;
	}
    }

    public final void write_Context(org.omg.CORBA.Context ctx,
			      org.omg.CORBA.ContextList contexts) {
	tp.enter_writeContextToCDRStream() ;
	try {
	    impl.write_Context(ctx, contexts);
	} finally {
	    tp.exit_writeContextToCDRStream() ;
	}
    }

    public final org.omg.CORBA.ORB orb() {
        return impl.orb();
    }

    // org.omg.CORBA_2_3.portable.OutputStream
    public final void write_value(java.io.Serializable value) {
	tp.enter_writeValueToCDRStream() ;
	try {
	    impl.write_value(value);
	} finally {
	    tp.exit_writeValueToCDRStream() ;
	}
    }

    public final void write_value(java.io.Serializable value, java.lang.Class clz) {
	tp.enter_writeValueWithClassToCDRStream() ;
	try {
	    impl.write_value(value, clz);
	} finally {
	    tp.exit_writeValueWithClassToCDRStream() ;
	}
    }

    public final void write_value(java.io.Serializable value, String repository_id) {
	tp.enter_writeValueWithRepidToCDRStream() ;
	try {
	    impl.write_value(value, repository_id);
	} finally {
	    tp.exit_writeValueWithRepidToCDRStream() ;
	}
    }

    public final void write_value(java.io.Serializable value, 
                            org.omg.CORBA.portable.BoxedValueHelper factory) {
	tp.enter_writeValueWithFactoryToCDRStream() ;
	try {
	    impl.write_value(value, factory);
	} finally {
	    tp.exit_writeValueWithFactoryToCDRStream() ;
	}
    }

    public final void write_abstract_interface(java.lang.Object obj) {
	tp.enter_writeAbstractInterfaceToCDRStream() ;
	try {
	    impl.write_abstract_interface(obj);
	} finally {
	    tp.exit_writeAbstractInterfaceToCDRStream() ;
	}
    }

    // java.io.OutputStream
    public final void write(byte b[]) throws IOException {
	tp.enter_writeByteArrayToCDRStream() ;
	try {
	    impl.write(b);
	} finally {
	    tp.exit_writeByteArrayToCDRStream() ;
	}
    }

    public final void write(byte b[], int off, int len) throws IOException {
	tp.enter_writeByteArrayWithOffsetToCDRStream() ;
	try {
	    impl.write(b, off, len);
	} finally {
	    tp.exit_writeByteArrayWithOffsetToCDRStream() ;
	}
    }

    public final void flush() throws IOException {
	tp.enter_flushCDRStream() ;
	try {
	    impl.flush();
	} finally {
	    tp.exit_flushCDRStream() ;
	}
    }

    public final void close() throws IOException {
	tp.enter_closeCDRStream() ;
	try {
	    impl.close();
	} finally {
	    tp.exit_closeCDRStream() ;
	}
    }

    // com.sun.corba.se.impl.encoding.MarshalOutputStream
    public final void start_block() {
	tp.enter_startBlockCDRStream() ;
	try {
	    impl.start_block();
	} finally {
	    tp.exit_startBlockCDRStream() ;
	}
    }

    public final void end_block() {
	tp.enter_endBlockCDRStream() ;
	try {
	    impl.end_block();
	} finally {
	    tp.exit_endBlockCDRStream() ;
	}
    }

    public final void putEndian() {
	tp.enter_putEndianCDRStream() ;
	try {
	    impl.putEndian();
	} finally {
	    tp.exit_putEndianCDRStream() ;
	}
    }

    public void writeTo(java.io.OutputStream s)
	throws IOException 
    {
	tp.enter_writeToCDRStream() ;
	try {
	    impl.writeTo(s);
	} finally {
	    tp.exit_writeToCDRStream() ;
	}
    }

    public final byte[] toByteArray() {
        return impl.toByteArray();
    }

    // org.omg.CORBA.DataOutputStream
    public final void write_Abstract (java.lang.Object value) {
	tp.enter_writeAbstractToCDRStream() ;
	try {
	    impl.write_Abstract(value);
	} finally {
	    tp.exit_writeAbstractToCDRStream() ;
	}
    }

    public final void write_Value (java.io.Serializable value) {
	tp.enter_writeValue2ToCDRStream() ;
	try {
	    impl.write_Value(value);
	} finally {
	    tp.exit_writeValue2ToCDRStream() ;
	}
    }

    public final void write_any_array(org.omg.CORBA.Any[] seq, int offset, int length) {
	tp.enter_writeAnyArrayToCDRStream() ;
	try {
	    impl.write_any_array(seq, offset, length);
	} finally {
	    tp.exit_writeAnyArrayToCDRStream() ;
	}
    }

    public void setMessageMediator(MessageMediator messageMediator)
    {
        this.corbaMessageMediator = (CorbaMessageMediator) messageMediator;
    }

    public MessageMediator getMessageMediator()
    {
        return corbaMessageMediator;
    }

    // org.omg.CORBA.portable.ValueBase
    public final String[] _truncatable_ids() {
        return impl._truncatable_ids();
    }

    // Other
    protected final int getSize() {
        return impl.getSize();
    }

    protected final int getIndex() {
        return impl.getIndex();
    }

    protected int getRealIndex(int index) {
        // Used in indirections. Overridden by TypeCodeOutputStream.
        return index;
    }

    protected final void setIndex(int value) {
        impl.setIndex(value);
    }

    protected final ByteBuffer getByteBuffer() {
        return impl.getByteBuffer();
    }

    protected final void setByteBuffer(ByteBuffer byteBuffer) {
        impl.setByteBuffer(byteBuffer);
    }

    public final boolean isLittleEndian() {
        return impl.isLittleEndian();
    }

    // XREVISIT - return to final if possible
    // REVISIT - was protected - need access from msgtypes test.
    public ByteBufferWithInfo getByteBufferWithInfo() {
        return impl.getByteBufferWithInfo();
    }

    protected void setByteBufferWithInfo(ByteBufferWithInfo bbwi) {
        impl.setByteBufferWithInfo(bbwi);
    }

    // REVISIT: was protected - but need to access from xgiop.
    public final BufferManagerWrite getBufferManager() {
        return impl.getBufferManager();
    }

    public final void write_fixed(java.math.BigDecimal bigDecimal, short digits, short scale) {
	tp.enter_writeFixed2ToCDRStream() ;
	try {
	    impl.write_fixed(bigDecimal, digits, scale);
	} finally {
	    tp.exit_writeFixed2ToCDRStream() ;
	}
    }

    public final void writeOctetSequenceTo(org.omg.CORBA.portable.OutputStream s) {
	tp.enter_writeOctetSequenceToCDRStream() ;
	try {
	    impl.writeOctetSequenceTo(s);
	} finally {
	    tp.exit_writeOctetSequenceToCDRStream() ;
	}
    }

    public final GIOPVersion getGIOPVersion() {
        return impl.getGIOPVersion();
    }

    public final void writeIndirection(int tag, int posIndirectedTo) {
        impl.writeIndirection(tag, posIndirectedTo);
    }

    // Use Latin-1 for GIOP 1.0 or when code set negotiation was not
    // performed.
    protected CodeSetConversion.CTBConverter createCharCTBConverter() {
        return CodeSetConversion.impl().getCTBConverter(OSFCodeSetRegistry.ISO_8859_1);
    }

    // Subclasses must decide what to do here.  It's inconvenient to
    // make the class and this method abstract because of dup().
    protected abstract CodeSetConversion.CTBConverter createWCharCTBConverter();

    protected final void freeInternalCaches() {
        impl.freeInternalCaches();
    }

    public void alignOnBoundary(int octetBoundary) {
        impl.alignOnBoundary(octetBoundary);
    }

    // Needed by request and reply messages for GIOP versions >= 1.2 only.
    public void setHeaderPadding(boolean headerPadding) {
        impl.setHeaderPadding(headerPadding);
    }

    // ValueOutputStream -----------------------------

    public void start_value(String rep_id) {
	tp.enter_startValueCDRStream() ;
	try {
	    impl.start_value(rep_id);
	} finally {
	    tp.exit_startValueCDRStream() ;
	}
    }

    public void end_value() {
	tp.enter_endValueCDRStream() ;
	try {
	    impl.end_value();
	} finally {
	    tp.exit_endValueCDRStream() ;
	}
    }
}
