/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1996-2007 Sun Microsystems, Inc. All rights reserved.
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

import com.sun.org.omg.SendingContext.CodeBase;

import com.sun.corba.se.spi.orb.ORB;

import com.sun.corba.se.spi.transport.CorbaConnection;

import com.sun.corba.se.spi.ior.iiop.GIOPVersion;

import com.sun.corba.se.impl.protocol.giopmsgheaders.Message;

import com.sun.corba.se.impl.logging.ORBUtilSystemException;
import com.sun.corba.se.impl.logging.OMGSystemException;

import com.sun.corba.se.impl.orbutil.ORBUtility;
import com.sun.corba.se.impl.orbutil.newtimer.generated.TimingPoints;
import com.sun.corba.se.spi.orbutil.ORBConstants;
import com.sun.corba.se.spi.protocol.CorbaMessageMediator;
import java.io.IOException;
import org.omg.CORBA.Any;
import org.omg.CORBA.TypeCode;

/**
 * @author Harold Carr
 */
public class CDRInputObject 
    extends org.omg.CORBA_2_3.portable.InputStream
    implements com.sun.corba.se.impl.encoding.MarshalInputStream,
               org.omg.CORBA.DataInputStream, org.omg.CORBA.portable.ValueInputStream
{
    private ORB orb ;
    private TimingPoints tp ;
    private ORBUtilSystemException wrapper ;
    private OMGSystemException omgWrapper ;
    private CDRInputStreamBase impl;

    private CorbaConnection corbaConnection;
    private Message header;
    private boolean unmarshaledHeader;
    protected CorbaMessageMediator messageMediator;

    private static class InputStreamFactory {
        public static CDRInputStreamBase newInputStream(
	        ORB orb, GIOPVersion version, byte encodingVersion,
		boolean directRead) {
            switch(version.intValue()) {
                case GIOPVersion.VERSION_1_0:
                    return new CDRInputStream_1_0();
                case GIOPVersion.VERSION_1_1:
                    return new CDRInputStream_1_1();
                case GIOPVersion.VERSION_1_2:
		    if (encodingVersion != ORBConstants.CDR_ENC_VERSION) {
			// Assumes JAVA_ENC_VERSION == 1
			return new IDLJavaSerializationInputStream(directRead);
		    }
                    return new CDRInputStream_1_2();
                default:
		    ORBUtilSystemException wrapper = 
			orb.getLogWrapperTable().get_RPC_ENCODING_ORBUtil() ;
		    throw wrapper.unsupportedGiopVersion( version ) ;
            }
        }
    }

    // Required for the case when a ClientResponseImpl is
    // created with a SystemException due to a dead server/closed
    // connection with no warning.  Note that the stream will
    // not be initialized in this case.
    // 
    // Probably also required by ServerRequestImpl.
    // 
    // REVISIT.
    public CDRInputObject() {
	((com.sun.corba.se.spi.orb.ORB)org.omg.CORBA.ORB.init())
	    .getTimerManager().points() ;
    }

     public CDRInputObject(CDRInputObject is) {
        impl = is.impl.dup();
        impl.setParent(this);
	tp = is.tp ;
    }

    // Called from EncapsInputStream
    public CDRInputObject(org.omg.CORBA.ORB orb, ByteBuffer byteBuffer, int size, 
        boolean littleEndian, GIOPVersion version, byte encodingVersion, 
        BufferManagerRead bufMgr, boolean directRead)
    {
        this.orb = (ORB)orb ;
	this.tp = this.orb.getTimerManager().points() ;
	this.wrapper = this.orb.getLogWrapperTable().get_RPC_ENCODING_ORBUtil() ;
	this.omgWrapper = this.orb.getLogWrapperTable().get_RPC_ENCODING_OMG() ;

	tp.enter_createCDRInputStream() ;
	try {
	    impl = InputStreamFactory.newInputStream(this.orb, version,
						     encodingVersion, directRead);

	    impl.init(orb, byteBuffer, size, littleEndian, bufMgr);

	    impl.setParent(this);
	} finally {
	    tp.exit_createCDRInputStream() ;
	}

        this.corbaConnection = null ;
        this.header = null ;
        this.unmarshaledHeader = false ;
        this.messageMediator = null ;
    }

    public CDRInputObject(org.omg.CORBA.ORB orb, ByteBuffer byteBuffer, int size,
        boolean littleEndian, GIOPVersion version, byte encodingVersion, BufferManagerRead bufMgr) {

	this(orb, byteBuffer, size, littleEndian, version, encodingVersion,
	     bufMgr, true);
    }

    public CDRInputObject(ORB orb, CorbaConnection corbaConnection, 
        ByteBuffer byteBuffer, Message header)
    {
        this(orb, byteBuffer, header.getSize(), header.isLittleEndian(),
              header.getGIOPVersion(), header.getEncodingVersion(),
              BufferManagerFactory.newBufferManagerRead(
					  header.getGIOPVersion(),
					  header.getEncodingVersion(),
					  orb));

	this.corbaConnection = corbaConnection;
        this.header = header ;

	if (orb.transportDebugFlag) {
	    dprint(".CDRInputObject constructor:");
	}

        getBufferManager().init(header);
        setIndex(Message.GIOPMessageHeaderLength);
        setBufferLength(header.getSize());
    }

    // REVISIT - think about this some more.
    // This connection normally is accessed from the message mediator.
    // However, giop input needs to get code set info from the connetion
    // *before* the message mediator is available.
    public final CorbaConnection getConnection() 
    {
        return corbaConnection;
    }

    // XREVISIT - Should the header be kept in the stream or the
    // message mediator?  Or should we not have a header and
    // have the information stored in the message mediator
    // directly?
    public Message getMessageHeader() 
    {
        return header;
    }

    /**
     * Unmarshal the extended GIOP header
     * NOTE: May be fragmented, so should not be called by the ReaderThread.
     * See CorbaResponseWaitingRoomImpl.waitForResponse.  It is done
     * there in the client thread.
     */
    public void unmarshalHeader()
    {
        // Unmarshal the extended GIOP message from the buffer.

        if (!unmarshaledHeader) {
	    try {
		if (((ORB)orb()).transportDebugFlag) {
		    dprint(".unmarshalHeader->: " + getMessageHeader());
		}
		getMessageHeader().read(this);
		unmarshaledHeader= true;
	    } catch (RuntimeException e) {
		if (((ORB)orb()).transportDebugFlag) {
		    dprint(".unmarshalHeader: !!ERROR!!: " 
			   + getMessageHeader()
			   + ": " + e);
		}
		throw e;
	    } finally {
		if (((ORB)orb()).transportDebugFlag) {
		    dprint(".unmarshalHeader<-: " + getMessageHeader());
		}
	    }
        }
    }

    public final boolean unmarshaledHeader() 
    {
        return unmarshaledHeader;
    }

    /**
     * Override the default CDR factory behavior to get the
     * negotiated code sets from the connection.
     *
     * These are only called once per message, the first time needed.
     *
     * In the local case, there is no Connection, so use the
     * local code sets.
     */
    protected CodeSetConversion.BTCConverter createCharBTCConverter() {
        CodeSetComponentInfo.CodeSetContext codesets = getCodeSets();

        // If the connection doesn't have its negotiated
        // code sets by now, fall back on the defaults defined
        // in CDRInputStream.
        if (codesets == null)
            return CodeSetConversion.impl().getBTCConverter(
                OSFCodeSetRegistry.ISO_8859_1, impl.isLittleEndian());
        
        OSFCodeSetRegistry.Entry charSet
            = OSFCodeSetRegistry.lookupEntry(codesets.getCharCodeSet());

        if (charSet == null)
	    throw wrapper.unknownCodeset( charSet ) ;

        return CodeSetConversion.impl().getBTCConverter(charSet, isLittleEndian());
    }

    protected CodeSetConversion.BTCConverter createWCharBTCConverter() {

        CodeSetComponentInfo.CodeSetContext codesets = getCodeSets();

        // If the connection doesn't have its negotiated
        // code sets by now, we have to throw an exception.
        // See CORBA formal 00-11-03 13.9.2.6.
        if (codesets == null) {
            if (getConnection().isServer())
		throw omgWrapper.noClientWcharCodesetCtx() ;
            else
		throw omgWrapper.noServerWcharCodesetCmp() ;
        }

        OSFCodeSetRegistry.Entry wcharSet
            = OSFCodeSetRegistry.lookupEntry(codesets.getWCharCodeSet());

        if (wcharSet == null)
	    throw wrapper.unknownCodeset( wcharSet ) ;

        // For GIOP 1.2 and UTF-16, use big endian if there is no byte
        // order marker.  (See issue 3405b)
        //
        // For GIOP 1.1 and UTF-16, use the byte order the stream if
        // there isn't (and there shouldn't be) a byte order marker.
        //
        // GIOP 1.0 doesn't have wchars.  If we're talking to a legacy ORB,
        // we do what our old ORBs did.
        if (wcharSet == OSFCodeSetRegistry.UTF_16) {
            if (getGIOPVersion().equals(GIOPVersion.V1_2))
                return CodeSetConversion.impl().getBTCConverter(wcharSet, false);
        }

        return CodeSetConversion.impl().getBTCConverter(wcharSet, isLittleEndian());
    }

    // If we're local and don't have a Connection, use the
    // local code sets, otherwise get them from the connection.
    // If the connection doesn't have negotiated code sets
    // yet, then we use ISO8859-1 for char/string and wchar/wstring
    // are illegal.
    private CodeSetComponentInfo.CodeSetContext getCodeSets() {
        if (getConnection() == null)
            return CodeSetComponentInfo.LOCAL_CODE_SETS;
        else
            return getConnection().getCodeSetContext();
    }    

    public CodeBase getCodeBase() {
        if (getConnection() == null)
            return null;
        else
            return getConnection().getCodeBase();
    }

    public CDRInputObject dup() {
        return null;
    }

    protected void dprint(String msg) 
    {
	ORBUtility.dprint("CDRInputObject", msg);
    }
    
    // org.omg.CORBA.portable.InputStream
    public final boolean read_boolean() {
	tp.enter_readBooleanFromCDRStream() ;
	try {
	    return impl.read_boolean();
	} finally {
	    tp.exit_readBooleanFromCDRStream() ;
	}
    }

    public final char read_char() {
	tp.enter_readCharFromCDRStream() ;
	try {
	    return impl.read_char();
	} finally {
	    tp.exit_readCharFromCDRStream() ;
	}
    }

    public final char read_wchar() {
	tp.enter_readWideCharFromCDRStream() ;
	try {
	    return impl.read_wchar();
	} finally {
	    tp.exit_readWideCharFromCDRStream() ;
	}
    }

    public final byte read_octet() {
	tp.enter_readOctetFromCDRStream() ;
	try {
	    return impl.read_octet();
	} finally {
	    tp.exit_readOctetFromCDRStream() ;
	}
    }

    public final short read_short() {
	tp.enter_readShortFromCDRStream() ;
	try {
	    return impl.read_short();
	} finally {
	    tp.exit_readShortFromCDRStream() ;
	}
    }

    public final short read_ushort() {
	tp.enter_readUnsignedShortFromCDRStream() ;
	try {
	    return impl.read_ushort();
	} finally {
	    tp.exit_readUnsignedShortFromCDRStream() ;
	}
    }

    public final int read_long() {
	tp.enter_readLongFromCDRStream() ;
	try {
	    return impl.read_long();
	} finally {
	    tp.exit_readLongFromCDRStream() ;
	}
    }

    public final int read_ulong() {
	tp.enter_readUnsignedLongFromCDRStream() ;
	try {
	    return impl.read_ulong();
	} finally {
	    tp.exit_readUnsignedLongFromCDRStream() ;
	}
    }

    public final long read_longlong() {
	tp.enter_readLongLongFromCDRStream() ;
	try {
	    return impl.read_longlong();
	} finally {
	    tp.exit_readLongLongFromCDRStream() ;
	}
    }

    public final long read_ulonglong() {
	tp.enter_readUnsignedLongLongFromCDRStream() ;
	try {
	    return impl.read_ulonglong();
	} finally {
	    tp.exit_readUnsignedLongLongFromCDRStream() ;
	}
    }

    public final float read_float() {
	tp.enter_readFloatFromCDRStream() ;
	try {
	    return impl.read_float();
	} finally {
	    tp.exit_readFloatFromCDRStream() ;
	}
    }

    public final double read_double() {
	tp.enter_readDoubleFromCDRStream() ;
	try {
	    return impl.read_double();
	} finally {
	    tp.exit_readDoubleFromCDRStream() ;
	}
    }

    public final String read_string() {
	tp.enter_readStringFromCDRStream() ;
	try {
	    return impl.read_string();
	} finally {
	    tp.exit_readStringFromCDRStream() ;
	}
    }

    public final String read_wstring() {
	tp.enter_readWideStringFromCDRStream() ;
	try {
	    return impl.read_wstring();
	} finally {
	    tp.exit_readWideStringFromCDRStream() ;
	}
    }

    public final void read_boolean_array(boolean[] value, int offset, int length) {
	tp.enter_readBooleanArrayFromCDRStream() ;
	try {
	    impl.read_boolean_array(value, offset, length);
	} finally {
	    tp.exit_readBooleanArrayFromCDRStream() ;
	}
    }

    public final void read_char_array(char[] value, int offset, int length) {
	tp.enter_readCharArrayFromCDRStream() ;
	try {
	    impl.read_char_array(value, offset, length);
	} finally {
	    tp.exit_readCharArrayFromCDRStream() ;
	}
    }

    public final void read_wchar_array(char[] value, int offset, int length) {
	tp.enter_readWideCharArrayFromCDRStream() ;
	try {
	    impl.read_wchar_array(value, offset, length);
	} finally {
	    tp.exit_readWideCharArrayFromCDRStream() ;
	}
    }

    public final void read_octet_array(byte[] value, int offset, int length) {
	tp.enter_readOctetArrayFromCDRStream() ;
	try {
	    impl.read_octet_array(value, offset, length);
	} finally {
	    tp.exit_readOctetArrayFromCDRStream() ;
	}
    }

    public final void read_short_array(short[] value, int offset, int length) {
	tp.enter_readShortArrayFromCDRStream() ;
	try {
	    impl.read_short_array(value, offset, length);
	} finally {
	    tp.exit_readShortArrayFromCDRStream() ;
	}
    }

    public final void read_ushort_array(short[] value, int offset, int length) {
	tp.enter_readUnsignedShortArrayFromCDRStream() ;
	try {
	    impl.read_ushort_array(value, offset, length);
	} finally {
	    tp.exit_readUnsignedShortArrayFromCDRStream() ;
	}
    }

    public final void read_long_array(int[] value, int offset, int length) {
	tp.enter_readLongArrayFromCDRStream() ;
	try {
	    impl.read_long_array(value, offset, length);
	} finally {
	    tp.exit_readLongArrayFromCDRStream() ;
	}
    }

    public final void read_ulong_array(int[] value, int offset, int length) {
	tp.enter_readUnsignedLongArrayFromCDRStream() ;
	try {
	    impl.read_ulong_array(value, offset, length);
	} finally {
	    tp.exit_readUnsignedLongArrayFromCDRStream() ;
	}
    }

    public final void read_longlong_array(long[] value, int offset, int length) {
	tp.enter_readLongLongArrayFromCDRStream() ;
	try {
	    impl.read_longlong_array(value, offset, length);
	} finally {
	    tp.exit_readLongLongArrayFromCDRStream() ;
	}
    }

    public final void read_ulonglong_array(long[] value, int offset, int length) {
	tp.enter_readUnsignedLongLongArrayFromCDRStream() ;
	try {
	    impl.read_ulonglong_array(value, offset, length);
	} finally {
	    tp.exit_readUnsignedLongLongArrayFromCDRStream() ;
	}
    }

    public final void read_float_array(float[] value, int offset, int length) {
	tp.enter_readFloatArrayFromCDRStream() ;
	try {
	    impl.read_float_array(value, offset, length);
	} finally {
	    tp.exit_readFloatArrayFromCDRStream() ;
	}
    }

    public final void read_double_array(double[] value, int offset, int length) {
	tp.enter_readDoubleArrayFromCDRStream() ;
	try {
	    impl.read_double_array(value, offset, length);
	} finally {
	    tp.exit_readDoubleArrayFromCDRStream() ;
	}
    }

    public final org.omg.CORBA.Object read_Object() {
	tp.enter_readObjectFromCDRStream() ;
	try {
	    return impl.read_Object();
	} finally {
	    tp.exit_readObjectFromCDRStream() ;
	}
    }

    public final TypeCode read_TypeCode() {
	tp.enter_readTypeCodeFromCDRStream() ;
	try {
	    return impl.read_TypeCode();
	} finally {
	    tp.exit_readTypeCodeFromCDRStream() ;
	}
    }

    public final Any read_any() {
	tp.enter_readAnyFromCDRStream() ;
	try {
	    return impl.read_any();
	} finally {
	    tp.exit_readAnyFromCDRStream() ;
	}
    }

    @SuppressWarnings({"deprecation"})
    @Override
    public final org.omg.CORBA.Principal read_Principal() {
	tp.enter_readPrincipalFromCDRStream() ;
	try {
	    return impl.read_Principal();
	} finally {
	    tp.exit_readPrincipalFromCDRStream() ;
	}
    }

    @Override
    public final int read() throws java.io.IOException {
	tp.enter_readIntFromCDRStream() ;
	try {
	    return impl.read();
	} finally {
	    tp.exit_readIntFromCDRStream() ;
	}
    }

    @Override
    public final java.math.BigDecimal read_fixed() {
	tp.enter_readFixedFromCDRStream() ;
	try {
	    return impl.read_fixed();
	} finally {
	    tp.exit_readFixedFromCDRStream() ;
	}
    }

    public final org.omg.CORBA.Context read_Context() {
	tp.enter_readContextFromCDRStream() ;
	try {
	    return impl.read_Context();
	} finally {
	    tp.exit_readContextFromCDRStream() ;
	}
    }

    public final org.omg.CORBA.Object read_Object(java.lang.Class clz) {
	tp.enter_readObjectWithClassFromCDRStream() ;
	try {
	    return impl.read_Object(clz);
	} finally {
	    tp.exit_readObjectWithClassFromCDRStream() ;
	}
    }

    public final org.omg.CORBA.ORB orb() {
	return impl.orb();
    }

    // org.omg.CORBA_2_3.portable.InputStream
    public final java.io.Serializable read_value() {
	tp.enter_readValueFromCDRStream() ;
	try {
	    return impl.read_value();
	} finally {
	    tp.exit_readValueFromCDRStream() ;
	}
    }

    public final java.io.Serializable read_value(java.lang.Class clz) {
	tp.enter_readValueWithClassFromCDRStream() ;
	try {
	    return impl.read_value(clz);
	} finally {
	    tp.exit_readValueWithClassFromCDRStream() ;
	}
    }

    public final java.io.Serializable read_value(org.omg.CORBA.portable.BoxedValueHelper factory) {
	tp.enter_readValueWithFactoryFromCDRStream() ;
	try {
	    return impl.read_value(factory);
	} finally {
	    tp.exit_readValueWithFactoryFromCDRStream() ;
	}
    }

    public final java.io.Serializable read_value(java.lang.String rep_id) {
	tp.enter_readValueWithRepidFromCDRStream() ;
	try {
	    return impl.read_value(rep_id);
	} finally {
	    tp.exit_readValueWithRepidFromCDRStream() ;
	}
    }

    public final java.io.Serializable read_value(java.io.Serializable value) {
	tp.enter_readValueWithSerializableFromCDRStream() ;
	try {
	    return impl.read_value(value);
	} finally {
	    tp.exit_readValueWithSerializableFromCDRStream() ;
	}
    }

    public final java.lang.Object read_abstract_interface() {
	tp.enter_readAbstractInterfaceFromCDRStream() ;
	try {
	    return impl.read_abstract_interface();
	} finally {
	    tp.exit_readAbstractInterfaceFromCDRStream() ;
	}
    }

    public final java.lang.Object read_abstract_interface(java.lang.Class clz) {
	tp.enter_readAbstractInterfaceWithClassFromCDRStream() ;
	try {
	    return impl.read_abstract_interface(clz);
	} finally {
	    tp.exit_readAbstractInterfaceWithClassFromCDRStream() ;
	}
    }
    // com.sun.corba.se.impl.encoding.MarshalInputStream

    public final void consumeEndian() {
	tp.enter_consumeEndianCDRStream() ;
	try {
	    impl.consumeEndian();
	} finally {
	    tp.exit_consumeEndianCDRStream() ;
	}
    }

    public final int getPosition() {
	return impl.getPosition();
    }

    // org.omg.CORBA.DataInputStream

    public final java.lang.Object read_Abstract () {
	tp.enter_readAbstractFromCDRStream() ;
	try {
	    return impl.read_Abstract();
	} finally {
	    tp.exit_readAbstractFromCDRStream() ;
	}
    }

    public final java.io.Serializable read_Value () {
	tp.enter_readValue2FromCDRStream() ;
	try {
	    return impl.read_Value();
	} finally {
	    tp.exit_readValue2FromCDRStream() ;
	}
    }

    public final void read_any_array (org.omg.CORBA.AnySeqHolder seq, int offset, int length) {
	tp.enter_readAnyArrayWithHolderFromCDRStream() ;
	try {
	    impl.read_any_array(seq, offset, length);
	} finally {
	    tp.exit_readAnyArrayWithHolderFromCDRStream() ;
	}
    }

    public final void read_boolean_array (org.omg.CORBA.BooleanSeqHolder seq, int offset, int length) {
	tp.enter_readBooleanArrayWithHolderFromCDRStream() ;
	try {
	    impl.read_boolean_array(seq, offset, length);
	} finally {
	    tp.exit_readBooleanArrayWithHolderFromCDRStream() ;
	}
    }

    public final void read_char_array (org.omg.CORBA.CharSeqHolder seq, int offset, int length) {
	tp.enter_readCharArrayWithHolderFromCDRStream() ;
	try {
	    impl.read_char_array(seq, offset, length);
	} finally {
	    tp.exit_readCharArrayWithHolderFromCDRStream() ;
	}
    }

    public final void read_wchar_array (org.omg.CORBA.WCharSeqHolder seq, int offset, int length) {
	tp.enter_readWideCharArrayWithHolderFromCDRStream() ;
	try {
	    impl.read_wchar_array(seq, offset, length);
	} finally {
	    tp.exit_readWideCharArrayWithHolderFromCDRStream() ;
	}
    }

    public final void read_octet_array (org.omg.CORBA.OctetSeqHolder seq, int offset, int length) {
	tp.enter_readOctetArrayWithHolderFromCDRStream() ;
	try {
	    impl.read_octet_array(seq, offset, length);
	} finally {
	    tp.exit_readOctetArrayWithHolderFromCDRStream() ;
	}
    }

    public final void read_short_array (org.omg.CORBA.ShortSeqHolder seq, int offset, int length) {
	tp.enter_readShortArrayWithHolderFromCDRStream() ;
	try {
	    impl.read_short_array(seq, offset, length);
	} finally {
	    tp.exit_readShortArrayWithHolderFromCDRStream() ;
	}
    }

    public final void read_ushort_array (org.omg.CORBA.UShortSeqHolder seq, int offset, int length) {
	tp.enter_readUnsignedShortArrayWithHolderFromCDRStream() ;
	try {
	    impl.read_ushort_array(seq, offset, length);
	} finally {
	    tp.exit_readUnsignedShortArrayWithHolderFromCDRStream() ;
	}
    }

    public final void read_long_array (org.omg.CORBA.LongSeqHolder seq, int offset, int length) {
	tp.enter_readLongArrayWithHolderFromCDRStream() ;
	try {
	    impl.read_long_array(seq, offset, length);
	} finally {
	    tp.exit_readLongArrayWithHolderFromCDRStream() ;
	}
    }

    public final void read_ulong_array (org.omg.CORBA.ULongSeqHolder seq, int offset, int length) {
	tp.enter_readUnsignedLongArrayWithHolderFromCDRStream() ;
	try {
	    impl.read_ulong_array(seq, offset, length);
	} finally {
	    tp.exit_readUnsignedLongArrayWithHolderFromCDRStream() ;
	}
    }

    public final void read_ulonglong_array (org.omg.CORBA.ULongLongSeqHolder seq, int offset, int length) {
	tp.enter_readUnsignedLongLongArrayWithHolderFromCDRStream() ;
	try {
	    impl.read_ulonglong_array(seq, offset, length);
	} finally {
	    tp.exit_readUnsignedLongLongArrayWithHolderFromCDRStream() ;
	}
    }

    public final void read_longlong_array (org.omg.CORBA.LongLongSeqHolder seq, int offset, int length) {
	tp.enter_readLongLongArrayWithHolderFromCDRStream() ;
	try {
	    impl.read_longlong_array(seq, offset, length);
	} finally {
	    tp.exit_readLongLongArrayWithHolderFromCDRStream() ;
	}
    }

    public final void read_float_array (org.omg.CORBA.FloatSeqHolder seq, int offset, int length) {
	tp.enter_readFloatArrayWithHolderFromCDRStream() ;
	try {
	    impl.read_float_array(seq, offset, length);
	} finally {
	    tp.exit_readFloatArrayWithHolderFromCDRStream() ;
	}
    }

    public final void read_double_array (org.omg.CORBA.DoubleSeqHolder seq, int offset, int length) {
	tp.enter_readDoubleArrayWithHolderFromCDRStream() ;
	try {
	    impl.read_double_array(seq, offset, length);
	} finally {
	    tp.exit_readDoubleArrayWithHolderFromCDRStream() ;
	}
    }

    // org.omg.CORBA.portable.ValueBase
    public final String[] _truncatable_ids() {
        return impl._truncatable_ids();
    }

    // java.io.InputStream
    public final int read(byte b[]) throws IOException {
	tp.enter_readByteArrayFromCDRStream() ;
	try {
	    return impl.read(b);
	} finally {
	    tp.exit_readByteArrayFromCDRStream() ;
	}
    }

    public final int read(byte b[], int off, int len) throws IOException {
	tp.enter_readByteArrayWithOffsetFromCDRStream() ;
	try {
	    return impl.read(b, off, len);
	} finally {
	    tp.exit_readByteArrayWithOffsetFromCDRStream() ;
	}
    }

    public final long skip(long n) throws IOException {
	tp.enter_skipCDRStream() ;
	try {
	    return impl.skip(n);
	} finally {
	    tp.exit_skipCDRStream() ;
	}
    }

    public final int available() throws IOException {
	return impl.available();
    }

    public void close() throws IOException {
	tp.enter_closeCDRInputStream() ;
	try {
	    impl.close();
	} finally {
	    tp.exit_closeCDRInputStream() ;
	}
    }

    public final void mark(int readlimit) {
	tp.enter_markCDRStream() ;
	try {
	    impl.mark(readlimit);
	} finally {
	    tp.exit_markCDRStream() ;
	}
    }

    public final void reset() {
	tp.enter_resetCDRStream() ;
	try {
	    impl.reset();
	} finally {
	    tp.exit_resetCDRStream() ;
	}
    }

    public final boolean markSupported() {
        return impl.markSupported();
    }

    // Needed by TCUtility
    public final java.math.BigDecimal read_fixed(short digits, short scale) {
	tp.enter_readFixed2FromCDRStream() ;
	try {
	    return impl.read_fixed(digits, scale);
	} finally {
	    tp.exit_readFixed2FromCDRStream() ;
	}
    }

    public final boolean isLittleEndian() {
	return impl.isLittleEndian();
    }

    protected final ByteBuffer getByteBuffer() {
        return impl.getByteBuffer();
    }

    protected final void setByteBuffer(ByteBuffer byteBuffer) {
        impl.setByteBuffer(byteBuffer);
    }

    protected final void setByteBufferWithInfo(ByteBufferWithInfo bbwi) {
        impl.setByteBufferWithInfo(bbwi);
    }

    public final int getBufferLength() {
        return impl.getBufferLength();
    }

    protected final void setBufferLength(int value) {
        impl.setBufferLength(value);
    }

    protected final int getIndex() {
        return impl.getIndex();
    }

    protected final void setIndex(int value) {
        impl.setIndex(value);
    }

    public final void orb(org.omg.CORBA.ORB orb) {
        impl.orb(orb);
    }

    public final GIOPVersion getGIOPVersion() {
        return impl.getGIOPVersion();
    }

    public final BufferManagerRead getBufferManager() {
        return impl.getBufferManager();
    }

    /**
     * Aligns the current position on the given octet boundary
     * if there are enough bytes available to do so.  Otherwise,
     * it just returns.  This is used for some (but not all)
     * GIOP 1.2 message headers.
     */
    public void alignOnBoundary(int octetBoundary) {
	tp.enter_alignOnBoundaryCDRStream() ;
	try {
	    impl.alignOnBoundary(octetBoundary);
	} finally {
	    tp.exit_alignOnBoundaryCDRStream() ;
	}
    }

    // Needed by request and reply messages for GIOP versions >= 1.2 only.
    public void setHeaderPadding(boolean headerPadding) {
	tp.enter_setHeaderPaddingCDRStream() ;
	try {
	    impl.setHeaderPadding(headerPadding);
	} finally {
	    tp.exit_setHeaderPaddingCDRStream() ;
	}
    }
    
    /**
     * This must be called after determining the proper ORB version,
     * and setting it on the stream's ORB instance.  It can be called
     * after reading the service contexts, since that is the only place
     * we can get the ORB version info.
     *
     * Trying to unmarshal things requiring repository IDs before calling
     * this will result in NullPtrExceptions.
     */
    public void performORBVersionSpecificInit() {
        // In the case of SystemExceptions, a stream is created
        // with its default constructor (and thus no impl is set).
        if (impl != null)
            impl.performORBVersionSpecificInit();
    }

    /**
     * Resets any internal references to code set converters.
     * This is useful for forcing the CDR stream to reacquire
     * converters (probably from its subclasses) when state
     * has changed.
     */
    public void resetCodeSetConverters() {
        impl.resetCodeSetConverters();
    }

    public void setMessageMediator(CorbaMessageMediator messageMediator)
    {
        this.messageMediator = (CorbaMessageMediator) messageMediator;
    }

    public CorbaMessageMediator getMessageMediator()
    {
        return messageMediator;
    }

    // ValueInputStream -----------------------------

    public void start_value() {
	tp.enter_startValueCDRInputStream() ;
	try {
	    impl.start_value();
	} finally {
	    tp.exit_startValueCDRInputStream() ;
	}
    }

    public void end_value() {
	tp.enter_endValueCDRInputStream() ;
	try {
	    impl.end_value();
	} finally {
	    tp.exit_endValueCDRInputStream() ;
	}
    }}

// End of file.
