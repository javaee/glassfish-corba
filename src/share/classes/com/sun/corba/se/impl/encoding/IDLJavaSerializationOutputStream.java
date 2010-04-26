/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2004-2007 Sun Microsystems, Inc. All rights reserved.
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
import java.io.ObjectOutputStream;
import java.io.ByteArrayOutputStream;
import java.nio.ByteBuffer;
import java.util.LinkedList;
import java.util.List;
import java.util.Iterator;

import com.sun.corba.se.spi.orb.ORB;
import com.sun.corba.se.spi.ior.IOR;
import com.sun.corba.se.spi.ior.IORFactories;
import com.sun.corba.se.spi.ior.iiop.GIOPVersion;
import com.sun.corba.se.spi.presentation.rmi.StubAdapter;

import com.sun.corba.se.impl.util.Utility;
import com.sun.corba.se.spi.orbutil.ORBConstants;
import com.sun.corba.se.impl.orbutil.ORBUtility;
import com.sun.corba.se.impl.corba.TypeCodeImpl;
import com.sun.corba.se.impl.logging.ORBUtilSystemException;
import com.sun.corba.se.impl.protocol.giopmsgheaders.Message;

import org.omg.CORBA.Any;
import org.omg.CORBA.TypeCode;
import org.omg.CORBA.CompletionStatus;

/**
 * Implementation class that uses Java serialization for output streams.
 * This assumes a GIOP version 1.2 message format. This internally uses a
 * IDLObjectOutputStream linked to a IDLByteArrayOutputStream, to marshal
 * primitives, arrays and complex objects. The IDLByteArrayOutputStream
 * contains the actual data buffer that holds the marshalled output data.
 * The implementation behavior corresponds to JAVA_ENC_VERSION of 1.
 *
 * Direct buffer write (first 16 bytes) and lazy ObjectOutputStream init:
 * 
 * When the directWrite flag is set, the first 16 bytes are written directly
 * as bytes. This is because the GIOP machinery requires that, on the
 * receiving side, the first 16 bytes be read off the buffer first,
 * before unmarshalling begins.
 *
 * The first 16 bytes [GIOPHeader (12 bytes) + requestID (4 bytes)] are 
 * directly written into the output buffer using the IDLByteBufferOutputStream.
 * Subsequent write operations on this output stream object uses 
 * IDLObjectOutputStream class to write into the buffer. The first 16 bytes
 * are written only using the write_octet, write_long or write_ulong methods.
 * The direct write_long and write_ulong operations assume big-endian.
 *
 * When the directWrite flag is not set, the data is marshalled normally.
 *
 * Implicit servant activation and Stub marshalling:
 *
 * Consider a java.rmi.Remote type contained within a serializable object.
 * During marshalling, the Stub for the remote type needs to be marshalled
 * instead of the object. This is important since the type is remote. Refer
 * to IDLObjectOutputStream object below. This implementation provides the
 * callback method (replaceObject method) implementation, which is called
 * during object serialization, that auto-connects (activates) the contained
 * remote type (servant) to the local ORB and returns a Stub object to be
 * marshalled out.
 *
 * @author Ram Jeyaraman
 */
public class IDLJavaSerializationOutputStream extends CDROutputStreamBase {
    
    private ORB orb;
    private ByteBuffer byteBuffer;
    private IDLObjectOutputStream os;
    private IDLByteArrayOutputStream bos;
    private BufferManagerWrite bufferManager;

    private boolean directWrite;
    private boolean useDirectByteBuffer;
    private ByteBuffer pooledByteBuffer;

    // [GIOPHeader(12) + requestID(4)] bytes
    static final int DIRECT_RW_LENGTH = Message.GIOPMessageHeaderLength + 4;

    protected ORBUtilSystemException wrapper;

    private static final List<IDLByteArrayOutputStream> outputStreamPool = 
	new LinkedList<IDLByteArrayOutputStream>();

    void printBuffer() {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    /**
     * This implementation is an alternative to the ByteArrayOutputStream.
     *
     * The ByteArrayOutputStream doubles the buffer size, each time the
     * buffer capacity is exceeded, during marshalling. This unfortunately
     * introduces unnecessary buffer copy overhead. That is, each time the
     * buffer size is doubled, the old buffer contents are copied over to
     * the newly allocated larger buffer. For example,
     * if the initial size of the buffer is 1024 bytes, and the total data
     * content written out is 5000 bytes, there will be 3 buffer copies.
     * That is, (1): 1024 -> 2048 , (2): 2048 -> 4096, (3): 4096 -> 8192,
     * tatalling (1):1024 + (2):2048 + (3):4096 = 7168 bytes.
     *
     * This implementation avoids the buffer copy overhead by using
     * a list of variable size byte arrays. The number of byte arrays
     * in the list grows over time, starting with one, depending on
     * how much data is written out to the stream. For example, if
     * 5000 bytes are written out, the list will eventually contain
     * 3 byte arrays (1024 + 2048 + 4096). The last byte array will
     * contain 1928 bytes of data, and the rest empty.
     *
     * Note, this implementation is not synchronized.
     */
    static final class IDLByteArrayOutputStream extends java.io.OutputStream {

	private byte[] buf; // current buf
	private LinkedList<byte[]> bufQ;
	private int index; // current buf index
	private int count; // bufQ byte count

	private IDLObjectOutputStream parent;

	/*
	 * @param capacity specifies initial buffer capacity.
	 */
	IDLByteArrayOutputStream(int capacity) {
	    buf = new byte[capacity];
	    bufQ = new LinkedList<byte[]>();
	}

	public void write(byte b[], int off, int len) throws IOException {
	    /*
	    if ((off < 0) || (off > b.length) || (len < 0) ||
		((off + len) > b.length) || ((off + len) < 0)) {
		throw new IndexOutOfBoundsException();
	    } else if (len == 0) {
		return;
	    }
	    */
	    if ((index + len) > buf.length) {
		int available = buf.length - index;
		System.arraycopy(b, off, buf, index, available);
		bufQ.addLast(buf);
		count += buf.length;
		buf = new byte[Math.max(buf.length << 1, len - available)];
		System.arraycopy(b, off + available, buf, 0, len - available);
		index = len - available;
	    } else {
		System.arraycopy(b, off, buf, index, len);
		index += len;
	    }
	}

	public void write(int b) throws IOException {
	    if ((index + 1) > buf.length) { // buf full
		bufQ.addLast(buf);
		count += buf.length;
		buf = new byte[buf.length << 1]; // doubles capacity
	    }
	    buf[index] = (byte) b;
	    index++;
	}

	int size() {
	    return count + index;
	}

	// Note, the byteBuffer limit must be large enough to hold the data.
	void writeTo(java.nio.ByteBuffer bb) { // buffer copy
	    Iterator<byte[]> iter = bufQ.iterator();
	    while (iter.hasNext()) {
		byte[] b = iter.next();
		bb.put(b, 0, b.length);
	    }
	    bb.put(buf, 0, index);
	}

	void writeTo(java.io.OutputStream s) throws IOException {
	    Iterator<byte[]> iter = bufQ.iterator();
	    while (iter.hasNext()) {
		byte[] b = iter.next();
		s.write(b, 0, b.length);
	    }
	    s.write(buf, 0, index);
	}

	void writeTo(org.omg.CORBA.portable.OutputStream s) {
	    s.write_long(size());
	    Iterator<byte[]> iter = bufQ.iterator();
	    while (iter.hasNext()) {
		byte[] b = iter.next();
		s.write_octet_array(b, 0, b.length);
	    }
	    s.write_octet_array(buf, 0, index);
	}

	byte[] toByteArray() { // buffer copy
	    int off = 0;
	    byte[] tmpBuf = new byte[size()];
	    Iterator<byte[]> iter = bufQ.iterator();
	    while (iter.hasNext()) {
		byte[] b = iter.next();
		System.arraycopy(b, 0, tmpBuf, off, b.length);
		off += b.length;
	    }
	    System.arraycopy(buf, 0, tmpBuf, off, index);
	    return tmpBuf;
	}

	void setParent(IDLObjectOutputStream os) {
	    parent = os;
	}

	IDLObjectOutputStream getParent() {
	    return parent;
	}

	void reset(int capacity) {
	    // REVISIT: Note that the variable buf is being reused.
	    // While this is good for the case where the pooled objects are
	    // constantly being reused, but it may potentially cause memory
	    // bloat, if the pooled objects are not used for a long time,
	    // and particularly if the buf capacity is very large. Perhaps, a
	    // pool size limiting strategy might help avoid memory bloat, when
	    // the pool is not being actively used.
	    if (buf.length != capacity) {
		buf = new byte[capacity];
	    }
	    bufQ.clear();
	    index = 0;
	    count = 0;
	}
    }

    /**
     * The ObjectOutputStream has been subclassed in order to replace
     * remote types contained within serializable objects, with a proxy
     * (Stub) object, during marshalling.
     */
    static final class IDLObjectOutputStream extends ObjectOutputStream {

	private ORB orb;
	
	IDLObjectOutputStream(java.io.OutputStream os, ORB orb)
	        throws IOException {
	    super(os);
	    this.orb = orb;
	    java.security.AccessController.doPrivileged(
	        new java.security.PrivilegedAction<Object>() {
		    public Object run() {
			// needs SerializablePermission("enableSubstitution")
			enableReplaceObject(true);
			return null;
		    }
	        }
	    );
	}

	/**
	 * Checks for objects that are instances of java.rmi.Remote
	 * that need to be marshalled as proxy (Stub) objects.
	 */
	protected final Object replaceObject(Object obj) throws IOException {
	    try {
		if ((obj instanceof java.rmi.Remote) &&
		        !(StubAdapter.isStub(obj))) {
		    return Utility.autoConnect(obj, orb, true);
		}
	    } catch (Exception e) {
		IOException ie = new IOException("replaceObject failed");
		ie.initCause(e);
		throw ie;
	    }
	    return obj;
	}

	void setOrb(ORB orb) {
	    this.orb = orb;
	}
    }

    IDLJavaSerializationOutputStream(boolean directWrite) {
	this.directWrite = directWrite;
    }

    public void init(org.omg.CORBA.ORB orb, boolean littleEndian,
		     BufferManagerWrite bufferManager,
		     byte streamFormatVersion, boolean useDirectByteBuffer) {
	this.orb = (ORB) orb;
	this.bufferManager = bufferManager;
	bos = getOrCreateByteArrayOutputStream(this.orb);
	wrapper = ((ORB)orb).getLogWrapperTable().get_RPC_ENCODING_ORBUtil() ;

	if (!directWrite) { // used by encaps streams
	    initObjectOutputStream();
	}
	this.useDirectByteBuffer = useDirectByteBuffer;
    }

    /**
     * @return a stream object from pool, if available, or a newly created one.
     */
    private static IDLByteArrayOutputStream
			 getOrCreateByteArrayOutputStream(ORB orb) {
	IDLByteArrayOutputStream bos = null;
	bos = getOutputStreamObjectFromPool();
	if (bos == null) {
	    return new IDLByteArrayOutputStream(
		           orb.getORBData().getGIOPBufferSize());
	}
	return bos;
    }

    /*
     * Called from write_octet or write_long or write_ulong method.
     * This method is called after the direct writes (first 16 bytes)
     * is over. Thus, this lazily creates the object output stream.
     */
    private void initObjectOutputStream() {
	if (os != null) {
	    throw wrapper.javaStreamInitFailed();
	}
	try {
	    os = bos.getParent();
	    if (os != null) {
		os.reset();
		os.setOrb(orb);
		return;
	    }
	    byte[] tmpArray = null;
	    if (directWrite) {
		tmpArray = bos.toByteArray(); // save
	    }
	    os = new IDLObjectOutputStream(bos, orb);
	    os.flush();
	    bos.reset(orb.getORBData().getGIOPBufferSize());
	    if (directWrite) {
		bos.write(tmpArray, 0, tmpArray.length); // restore
	    }
	    os.reset();
	    bos.setParent(os);
	} catch (Exception e) {
	    throw wrapper.javaStreamInitFailed(e);
	}
    }

    // org.omg.CORBA.portable.OutputStream

    // Primitive types.

    public final void write_boolean(boolean value) {
	try {
	    os.writeBoolean(value);
	} catch (Exception e) {
	    throw wrapper.javaSerializationException(e, "write_boolean");
	}
    }

    public final void write_char(char value) {
	try {
	    os.writeChar(value);
	} catch (Exception e) {
	    throw wrapper.javaSerializationException(e, "write_char");
	}
    }

    public final void write_wchar(char value) {
	write_char(value);
    }

    public final void write_octet(byte value) {

	try {
	    // check if size < [ GIOPHeader(12) + requestID(4)] bytes.
	    if (directWrite && (bos.size() < DIRECT_RW_LENGTH)) {
		bos.write(value); // direct write.
		if (bos.size() == DIRECT_RW_LENGTH) {
		    initObjectOutputStream(); // lazy init.
		}
		return;
	    }
	    os.writeByte(value);
	} catch (Exception e) {
	    throw wrapper.javaSerializationException(e, "write_octet");
	}
    }

    public final void write_short(short value) {
	try {
	    os.writeShort(value);
	} catch (Exception e) {
	    throw wrapper.javaSerializationException(e, "write_short");
	}
    }

    public final void write_ushort(short value) {
	write_short(value);
    }

    public final void write_long(int value) {

	try {

	    // check if size < [ GIOPHeader(12) + requestID(4)] bytes.
	    if (directWrite && (bos.size() < DIRECT_RW_LENGTH)) {

		// Use big endian (network byte order). This is fixed.
		// Both the writer and reader use the same byte order.
		bos.write((byte)((value >>> 24) & 0xFF));
		bos.write((byte)((value >>> 16) & 0xFF));
		bos.write((byte)((value >>> 8) & 0xFF));
		bos.write((byte)((value >>> 0) & 0xFF));

		if (bos.size() == DIRECT_RW_LENGTH) {
		    initObjectOutputStream(); // lazy init.
		} else if (bos.size() > DIRECT_RW_LENGTH) {
		    // Should not happen. All direct writes are contained
		    // within the first 16 bytes.
		    wrapper.javaSerializationException("write_long");
		}
		return;
	    }

	    os.writeInt(value);

	} catch (Exception e) {
	    throw wrapper.javaSerializationException(e, "write_long");
	}
    }

    public final void write_ulong(int value) {
        write_long(value);
    }

    public final void write_longlong(long value) {
	try {
	    os.writeLong(value);
	} catch (Exception e) {
	    throw wrapper.javaSerializationException(e, "write_longlong");
	}
    }

    public final void write_ulonglong(long value) {
        write_longlong(value);
    }

    public final void write_float(float value) {
	try {
	    os.writeFloat(value);
	} catch (Exception e) {
	    throw wrapper.javaSerializationException(e, "write_float");
	}
    }

    public final void write_double(double value) {
	try {
	    os.writeDouble(value);
	} catch (Exception e) {
	    throw wrapper.javaSerializationException(e, "write_double");
	}
    }

    // String types.

    public final void write_string(String value) {
	try {
	    os.writeUTF(value);
	} catch (Exception e) {
	    throw wrapper.javaSerializationException(e, "write_string");
	}
    }

    public final void write_wstring(String value) {
	try {
	    os.writeObject(value);
	} catch (Exception e) {
	    throw wrapper.javaSerializationException(e, "write_wstring");
	}
    }

    // Array types.

    public final void write_boolean_array(boolean[] value,
					  int offset, int length) {
        for (int i = 0; i < length; i++) {
            write_boolean(value[offset + i]);
	}
    }

    public final void write_char_array(char[] value, int offset, int length) {
        for (int i = 0; i < length; i++) {
            write_char(value[offset + i]);
	}
    }

    public final void write_wchar_array(char[] value, int offset, int length) {
	write_char_array(value, offset, length);
    }

    public final void write_octet_array(byte[] value, int offset, int length) {
	try {
	    os.write(value, offset, length);
	} catch (Exception e) {
	    throw wrapper.javaSerializationException(e, "write_octet_array");
	}
    }

    public final void write_short_array(short[] value,
					int offset, int length) {
        for (int i = 0; i < length; i++) {
            write_short(value[offset + i]);
	}
    }

    public final void write_ushort_array(short[] value,
					 int offset, int length){
    	write_short_array(value, offset, length);
    }

    public final void write_long_array(int[] value, int offset, int length) {
        for (int i = 0; i < length; i++) {
            write_long(value[offset + i]);
	}
    }

    public final void write_ulong_array(int[] value, int offset, int length) {
	write_long_array(value, offset, length);
    }

    public final void write_longlong_array(long[] value,
					   int offset, int length) {
        for (int i = 0; i < length; i++) {
            write_longlong(value[offset + i]);
	}
    }

    public final void write_ulonglong_array(long[] value,
					    int offset,int length) {
	write_longlong_array(value, offset, length);
    }

    public final void write_float_array(float[] value,
					int offset, int length) {
        for (int i = 0; i < length; i++) {
            write_float(value[offset + i]);
	}
    }

    public final void write_double_array(double[] value,
					 int offset, int length) {
        for (int i = 0; i < length; i++) {
            write_double(value[offset + i]);
	}
    }

    // Complex types (objects and graphs).

    public final void write_Object(org.omg.CORBA.Object value) {
        if (value == null) {
	    IOR nullIOR = IORFactories.makeIOR(orb);
            nullIOR.write(parent);
            return;
        }
        // IDL to Java formal 01-06-06 1.21.4.2
        if (value instanceof org.omg.CORBA.LocalObject) {
	    throw wrapper.writeLocalObject(CompletionStatus.COMPLETED_MAYBE);
	}
	IOR ior = orb.getIOR(value, true);
	ior.write(parent);
	return;
    }

    public final void write_TypeCode(TypeCode tc) {
        if (tc == null) {
	    throw wrapper.nullParam(CompletionStatus.COMPLETED_MAYBE);
	}
        TypeCodeImpl tci;
        if (tc instanceof TypeCodeImpl) {
	    tci = (TypeCodeImpl) tc;
	} else {
	    tci = new TypeCodeImpl(orb, tc);
	}
        tci.write_value((org.omg.CORBA_2_3.portable.OutputStream) parent);
    }

    public final void write_any(Any any) {
        if (any == null) {
	    throw wrapper.nullParam(CompletionStatus.COMPLETED_MAYBE);
	}
    	write_TypeCode(any.type());
    	any.write_value(parent);
    }

    @SuppressWarnings({"deprecation"})
    public final void write_Principal(org.omg.CORBA.Principal p) {
	// We don't need an implementation for this method,
	// since principal is absent in GIOP version 1.2 or above.
	throw wrapper.giopVersionError();
    }
    
    public final void write_fixed(java.math.BigDecimal bigDecimal) {
	write_value(bigDecimal);
    }

    public final org.omg.CORBA.ORB orb() {
        return orb;
    }

    // org.omg.CORBA_2_3.portable.OutputStream

    public final void write_value(java.io.Serializable value) {
        write_value(value, (String) null);
    }

    public final void write_value(java.io.Serializable value,
				  java.lang.Class clz) {
	write_value(value);
    }

    public final void write_value(java.io.Serializable value,
				  String repository_id) {
	try {
	    os.writeObject(value);
	} catch (Exception e) {
	    throw wrapper.javaSerializationException(e, "write_value");
	}
    }

    public final void write_value(java.io.Serializable value,
			     org.omg.CORBA.portable.BoxedValueHelper factory) {
	write_value(value, (String) null);
    }

    public final void write_abstract_interface(java.lang.Object obj) {

	boolean isCorbaObject = false; // Assume value type.
	org.omg.CORBA.Object theCorbaObject = null;
	    
	// Is it a CORBA.Object?
	if (obj != null && obj instanceof org.omg.CORBA.Object) {
	    theCorbaObject = (org.omg.CORBA.Object)obj;
	    isCorbaObject = true;	        
	}
	    
	// Write the boolean flag.
	write_boolean(isCorbaObject);
	    
	// Now write out the object.
	if (isCorbaObject) {
	    write_Object(theCorbaObject);
	} else {
	    try {
		write_value((java.io.Serializable)obj);
	    } catch(ClassCastException cce) {
		if (obj instanceof java.io.Serializable) {
		    throw cce;
		} else {
                    ORBUtility.throwNotSerializableForCorba(
						    obj.getClass().getName());
		}
	    }
	}
    }

    // com.sun.corba.se.os.encoding.MarshalOutputStream

    public final void start_block() {
	throw wrapper.giopVersionError();
    }

    public final void end_block() {
	throw wrapper.giopVersionError();
    }

    // Typically called, before writing to an encaps stream.
    public final void putEndian() {
	write_boolean(false); // network byte order (big-endian);
    }

    public void writeTo(java.io.OutputStream s)	throws IOException {
	try {
	    os.flush();
	    bos.writeTo(s);
	} catch (Exception e) {
	    throw wrapper.javaSerializationException(e, "writeTo");
	}
    }

    public final byte[] toByteArray() {
	try {
	    os.flush();
	} catch (Exception e) {
	    throw wrapper.javaSerializationException(e, "toByteArray");
	}
	return bos.toByteArray(); // buffer copy.
    }

    // org.omg.CORBA.DataOutputStream

    public final void write_Abstract (java.lang.Object value) {
	write_abstract_interface(value);
    }

    public final void write_Value(java.io.Serializable value) {
        write_value(value);
    }

    public final void write_any_array(org.omg.CORBA.Any[] value,
				      int offset, int length) {
    	for(int i = 0; i < length; i++) {
    	    write_any(value[offset + i]);
	}
    }

    // org.omg.CORBA.portable.ValueBase

    public final String[] _truncatable_ids() {
	throw wrapper.giopVersionError();
    }

    // Other.

    public final int getSize() {
	try {
	    os.flush();
	} catch (Exception e) {
	    throw wrapper.javaSerializationException(e, "write_boolean");
	}
	return bos.size();
    }

    public final int getIndex() {
        return getSize();
    }

    protected int getRealIndex(int index) {
        return getSize();
    }

    public final void setIndex(int value) {
	throw wrapper.giopVersionError();
    }

    public final ByteBuffer getByteBuffer() {
	ByteBufferWithInfo bbwi = getByteBufferWithInfo();
	return bbwi.getByteBuffer();
    }

    public final void setByteBuffer(ByteBuffer byteBuffer) {
	throw wrapper.giopVersionError();
    }

    public final boolean isLittleEndian() {
	// Java serialization uses network byte order, that is, big-endian.
	return false; 
    }

    public ByteBufferWithInfo getByteBufferWithInfo() {
	try {
	    os.flush();
	} catch (Exception e) {
	    throw wrapper.javaSerializationException(
					    e, "getByteBufferWithInfo");
	}
	ByteBuffer byteBuffer;
	if (!directWrite || !useDirectByteBuffer) { 
	    // We use non-direct byte buffers for encaps streams,
	    // since it is difficult to know when to free the byte buffer.
	    // Further, encaps streams are not consumed locally (no network
	    // communication), hence no need for nio buffers.
	    byteBuffer = ByteBuffer.wrap(bos.toByteArray());
	} else {
	    byteBuffer = orb.getByteBufferPool().getByteBuffer(bos.size());
	    pooledByteBuffer = byteBuffer; // see close() method.
	    bos.writeTo(byteBuffer);
	}
	return new ByteBufferWithInfo(orb, byteBuffer, bos.size());
    }

    public void setByteBufferWithInfo(ByteBufferWithInfo bbwi) {
	throw wrapper.giopVersionError();
    }

    public final BufferManagerWrite getBufferManager() {
	return bufferManager;
    }

    // This will stay a custom add-on until the java-rtf issue is resolved.
    // Then it should be declared in org.omg.CORBA.portable.OutputStream.
    //
    // Pads the string representation of bigDecimal with zeros to fit the given
    // digits and scale before it gets written to the stream.
    public final void write_fixed(java.math.BigDecimal bigDecimal,
				  short digits, short scale) {
        String string = bigDecimal.toString();
        String integerPart;
        String fractionPart;
        StringBuffer stringBuffer;

        // Get rid of the sign
        if (string.charAt(0) == '-' || string.charAt(0) == '+') {
            string = string.substring(1);
        }

        // Determine integer and fraction parts
        int dotIndex = string.indexOf('.');
        if (dotIndex == -1) {
            integerPart = string;
            fractionPart = null;
        } else if (dotIndex == 0 ) {
            integerPart = null;
            fractionPart = string;
        } else {
            integerPart = string.substring(0, dotIndex);
            fractionPart = string.substring(dotIndex + 1);
        }

        // Pad both parts with zeros as necessary
        stringBuffer = new StringBuffer(digits);
        if (fractionPart != null) {
            stringBuffer.append(fractionPart);
        }
        while (stringBuffer.length() < scale) {
            stringBuffer.append('0');
        }
        if (integerPart != null) {
            stringBuffer.insert(0, integerPart);
        }
        while (stringBuffer.length() < digits) {
            stringBuffer.insert(0, '0');
        }

        // This string contains no sign or dot
        write_fixed(stringBuffer.toString(), bigDecimal.signum());
    }

    // The string may contain a sign and dot
    private void write_fixed(String string, int signum) {

        int stringLength = string.length();

        // Each octet contains (up to) two decimal digits.
        byte doubleDigit = 0;
        char ch;
        byte digit;

        // First calculate the string length without optional sign and dot.
        int numDigits = 0;
        for (int i=0; i<stringLength; i++) {
            ch = string.charAt(i);
            if (ch == '-' || ch == '+' || ch == '.')
                continue;
            numDigits++;
        }

        for (int i=0; i<stringLength; i++) {
            ch = string.charAt(i);
            if (ch == '-' || ch == '+' || ch == '.')
                continue;
            digit = (byte)Character.digit(ch, 10);
            if (digit == -1) {
		throw wrapper.badDigitInFixed(
					    CompletionStatus.COMPLETED_MAYBE);
            }
            // If the fixed type has an odd number of decimal digits, then the
            // representation begins with the first (most significant) digit.
            // Otherwise, this first half-octet is all zero, and the first
            // digit is in the second half-octet.
            if (numDigits % 2 == 0) {
                doubleDigit |= digit;
                write_octet(doubleDigit);
                doubleDigit = 0;
            } else {
                doubleDigit |= (digit << 4);
            }
            numDigits--;
        }

        // The sign configuration in the last half-octet of the representation,
        // is 0xD for negative numbers and 0xC for positive and zero values.
        if (signum == -1) {
            doubleDigit |= 0xd;
        } else {
            doubleDigit |= 0xc;
        }
        write_octet(doubleDigit);
    }

    public final void writeOctetSequenceTo(
            org.omg.CORBA.portable.OutputStream s) {
	bos.writeTo(s);
    }

    public final GIOPVersion getGIOPVersion() {
	return GIOPVersion.V1_2;
    }

    public final void writeIndirection(int tag, int posIndirectedTo) {
	throw wrapper.giopVersionError();
    }

    void freeInternalCaches() {}

    public void alignOnBoundary(int octetBoundary) {
	throw wrapper.giopVersionError();
    }

    public void setHeaderPadding(boolean headerPadding) {
	// no-op. We don't care about body alignment while using
	// Java serialization. What the GIOP spec states does not apply here.
    }

    // ValueOutputStream -----------------------------

    public void start_value(String rep_id) {
	throw wrapper.giopVersionError();
    }

    public void end_value() {
	throw wrapper.giopVersionError();
    }

    // java.io.OutputStream

    public void write(int b) throws IOException {
	throw new UnsupportedOperationException();
    }

    public void write(byte b[]) throws IOException {
	throw new UnsupportedOperationException();
    }

    public void write(byte b[], int off, int len) throws IOException {
	throw new UnsupportedOperationException();
    }

    public void flush() throws IOException {
	throw new UnsupportedOperationException();
    }

    // caller: CorbaClientRequestDispatcherImpl.endRequest
    // caller: CorbaMessageMediatorImpl.endRequest
    public void close() throws IOException {
	if (bos == null) {
	    return; // duplicate close call.
	}
	if (pooledByteBuffer != null) {
	    orb.getByteBufferPool().releaseByteBuffer(pooledByteBuffer);
	}
	try {
	    if (os != null) { // os may be null, if stream is unused.
		os.flush();
	    }
	    bos.reset(orb.getORBData().getGIOPBufferSize());
	} catch (IOException e) {
	    return;
	}
	putOutputStreamObjectToPool(bos);
	bos = null;
    }

    private static void putOutputStreamObjectToPool(
            IDLByteArrayOutputStream value) {
	synchronized (outputStreamPool) {
	    outputStreamPool.add(value);
	}
    }

    private static IDLByteArrayOutputStream getOutputStreamObjectFromPool() {
	//System.out.println("os size: " + outputStreamPool.size());
	synchronized (outputStreamPool) {
	    if (outputStreamPool.size() != 0) {
		return (IDLByteArrayOutputStream)
		    outputStreamPool.remove(0);
	    }
	}
	return null;
    }
}
