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
import java.io.Serializable;
import java.io.ObjectInputStream;
import java.io.ByteArrayInputStream;
import java.io.ObjectStreamClass;
import java.io.ObjectStreamConstants;
import java.nio.ByteBuffer;
import java.math.BigDecimal;
import java.util.LinkedList;
import java.util.List;

import com.sun.corba.se.spi.orb.ORB;
import com.sun.corba.se.spi.ior.IOR;
import com.sun.corba.se.spi.ior.IORFactories;
import com.sun.corba.se.spi.ior.iiop.GIOPVersion;
import com.sun.corba.se.spi.presentation.rmi.StubAdapter;
import com.sun.corba.se.spi.presentation.rmi.PresentationManager;
import com.sun.corba.se.spi.presentation.rmi.PresentationDefaults;

import com.sun.corba.se.impl.util.Utility;
import com.sun.corba.se.impl.util.JDKBridge;
import com.sun.corba.se.impl.util.RepositoryId;
import com.sun.corba.se.impl.corba.TypeCodeImpl;
import com.sun.corba.se.impl.orbutil.ORBUtility;
import com.sun.corba.se.impl.orbutil.ORBConstants;
import com.sun.corba.se.impl.logging.ORBUtilSystemException;
import com.sun.corba.se.impl.protocol.giopmsgheaders.Message;

import org.omg.CORBA.Any;
import org.omg.CORBA.TypeCode;
import org.omg.CORBA.portable.IDLEntity;

import org.omg.CORBA_2_3.portable.InputStream;

/**
 * Implementation class that uses Java serialization for input streams.
 * This assumes a GIOP version 1.2 message format. This internally uses a
 * IDLObjectInputStream linked to a IDLByteArrayInputStream, to unmarshal
 * the primitives, arrays and complex objects. The IDLByteArrayInputStream
 * contains the buffer that holds the raw unmarshalled input data.
 * The implementation behavior corresponds to JAVA_ENC_VERSION of 1.
 *
 * Direct buffer read (first 16 bytes) and lazy ObjectInputStream init:
 * 
 * When the directRead flag is set, the first 16 bytes are read directly
 * as bytes. This is because the GIOP machinery requires that the first
 * 16 bytes be read off the buffer first, before unmarshalling begins.
 *
 * The first 16 bytes [GIOPHeader (12 bytes) + requestID (4 bytes)] are 
 * directly read from the input buffer using the IDLByteArrayInputStream.
 * Subsequent read operations on this input stream object uses 
 * IDLObjectInputStream class to read from the buffer. The first 16 bytes
 * are read only using read_octet, read_long or read_ulong methods.
 * The direct read_long and read_ulong operations assume big-endian.
 *
 * The first 12 bytes, that is, the GIOP Header is read directly from the
 * received message, before this stream object is called. So, this class
 * effectively reads only the requestID (4 bytes) directly, and uses the
 * IDLObjectInputStream for further unmarshalling.
 * 
 * When the directRead flag is not set, the input data is unmarshalled
 * from the beginning of the stream. This is true for EncapsInputStreams.
 *
 * Implicit Stub connection:
 *
 * Consider a java.rmi.Remote type contained within a serializable object.
 * When the serializable object is unmarshalled, the Stub for the contained
 * remote type is encountered. The resurrected Stub object needs to be
 * connected to the local ORB, in order to be usable. Refer to the
 * IDLObjectInputStream object below. This implementation provides the
 * callback method (resolveObject method) implementation, which is called
 * during object deserialization, that connects (activates) the resurrected
 * Stub object to the local ORB.
 
 * Mark and reset support:
 *
 * This implementation supports mark and reset operations on the input stream.
 * However, since the ORB implementation uses this stream object only after
 * reading the GIOP header (12 bytes), the mark and reset operations are
 * effectively available only after the first 12 bytes in the input stream.
 * But the mark and reset implementation does not have any restrictions and
 * may be applied to the whole stream.
 *
 * A FIFO Queue data structure is used to support mark and reset semantics.
 *
 *      Mark On   |    Queue empty        |      Action
 *      -----------------------------------------------------------------
 * (1)  No        |    Yes                |      Read from Stream
 *                |                       |
 * (2)	No        |    No                 |      Read from Queue  + dequeue
 *                |                       |
 * (3)	Yes       |    Yes || No          |      Read from Stream + enqueue
 *                |                       |
 * (4)	Yes       |    No &&              |      Read from Queue + peekIndex++
 *                |(peekIndex < peekCount)|
 *
 * Case (4): This is a special case of case (3).
 * This occurs when a mark-reset is followed by another mark-reset,
 * before all the queued data items are dequeued. That is, when the
 * second mark operation begins, the queue is not empty. In this case,
 * read operations within the second mark-reset duration read from the
 * queue linearly, but does not dequeue the read items, until the last
 * element in the queue is read. This is important since, only read
 * operation outside the mark-reset zone can dequeue items in the queue.
 * After the last element is read, further read operations within the
 * second mark will read from the stream (case 3).
 *
 * Case (4) is supported using peekCount and peekIndex variables. These
 * variables are set when mark() is called, and if the queue is not empty.
 * These variables are unset during reset().
 *
 * A regular read operation after a mark and reset duration may get a
 * java.lang.ClassCastException, if the expected operand type does not match
 * the previously read queued operand type.
 *
 * @author Ram Jeyaraman
 */
public class IDLJavaSerializationInputStream extends CDRInputStreamBase {

    private ORB orb;
    private IDLObjectInputStream is;
    private IDLByteArrayInputStream bis;
    private BufferManagerRead bufferManager;
    private ByteBuffer byteBuffer;

    private boolean directRead;

    // Used for mark / reset operations.
    private boolean markOn;
    private int peekIndex, peekCount;
    private LinkedList<Object> markedItemQ = new LinkedList<Object>();

    private boolean debug;
    protected ORBUtilSystemException wrapper;

    private static final List<IDLByteArrayInputStream> inputStreamPool = 
	new LinkedList<IDLByteArrayInputStream>();

    private static byte[] streamHeader = new byte[4];

    static {
	streamHeader[1] = (byte) (ObjectStreamConstants.STREAM_MAGIC >>> 0);
	streamHeader[0] = (byte) (ObjectStreamConstants.STREAM_MAGIC >>> 8);
	streamHeader[3] = (byte) (ObjectStreamConstants.STREAM_VERSION >>> 0);
	streamHeader[2] = (byte) (ObjectStreamConstants.STREAM_VERSION >>> 8);
    }

    static final class IDLByteArrayInputStream extends java.io.InputStream {

	private int pos;
    	private byte[] buffer;
	private IDLObjectInputStream parent;

	private byte[] savedBuffer;
        private int savedPos;

	public IDLByteArrayInputStream(byte[] b) {
	    this.buffer = b;
            this.pos = 0;
	}

	void init(byte[] b) {
	    this.buffer = b;
            this.pos = 0;
	}

	void save() {
	    savedBuffer = buffer;
            savedPos = pos;
	}

	void restore() {
	    buffer = savedBuffer;
            pos = savedPos;
	}

	void clear() {
	    buffer = null; pos = 0;
	    savedBuffer = null; savedPos = 0;
	}

	int position() {
	    return pos;
	}

	void position(int pos) {
	    this.pos = pos;
	}

	public int read(byte b[], int off, int len) throws IOException {
            System.arraycopy(buffer, pos, b, off, len);
            pos += len;
            return len;            
	}

	// The enclosing IDLObjectInputStream does bulk read. That is, the
	// method read(byte[], ...) is used. This method is otherwise used
	// for direct reads from the buffer.
	public int read() throws IOException {
	    return buffer[pos++] & 0xff;
	}

	public long skip(long n) throws IOException {
	    // Intentionally overridden, since super method calls read().
	    return 0;
	}

	void setParent(IDLObjectInputStream parent) {
	    this.parent = parent;
	}

	IDLObjectInputStream getParent() {
	    return parent;
	}
    }

    static final class IDLObjectInputStream extends ObjectInputStream {

	ORB orb;
	
	IDLObjectInputStream(java.io.InputStream out, ORB orb)
	        throws IOException {

	    super(out);
	    this.orb = orb;

	    java.security.AccessController.doPrivileged(
	        new java.security.PrivilegedAction<Object>() {
		    public Object run() {
			// needs SerializablePermission("enableSubstitution")
			enableResolveObject(true);
			return null;
		    }
	        }
	    );
	}

	/**
	 * Connect the Stub to the ORB.
	 */
	protected final Object resolveObject(Object obj) throws IOException {
	    try {
		if (StubAdapter.isStub(obj)) {
		    StubAdapter.connect(obj, orb);
		}
	    } catch (java.rmi.RemoteException re) {
		IOException ie = new IOException("resolveObject failed");
		ie.initCause(re);
		throw ie;
	    }
	    return obj;
	}

	/**
	 * Find, load, and return the class. The search order:
	 * JDK class loader, RMI class loader, thread context class loader.
	 * The RMI class loader is used for remote code downloading. The
	 * RMI class loader relies on the property java.rmi.server.codebase
	 * to get hold of a URL to download classes remotely.
	 */
	protected Class resolveClass(ObjectStreamClass classDesc)
	        throws IOException, ClassNotFoundException {

	    String className = classDesc.getName();

	    ClassLoader cLoader =
		Thread.currentThread().getContextClassLoader();

	    // Attempts loading from JDK Class loader first, failing which,
	    // RMIClassLoader next, and then the thread context class loader.
	    return JDKBridge.loadClass(className, null, cLoader);
	}

	void setOrb(ORB orb) {
	    this.orb = orb;
	}
    }

    IDLJavaSerializationInputStream(boolean directRead) {
	this.directRead = directRead;
    }

    public void init(org.omg.CORBA.ORB orb, ByteBuffer byteBuffer, int bufSize,
		     boolean littleEndian, BufferManagerRead bmgr) {
	this.orb = (ORB) orb;
	bufferManager = bmgr;
	byteBuffer.position(0).limit(bufSize);
	// NOTE: At this point, the buffer position is zero.
	// Use setIndex() method to set a desired read index.
	this.byteBuffer = byteBuffer;
	bis = getOrCreateByteArrayInputStream(
	          ORBUtility.getByteBufferArray(byteBuffer), this.orb);
	wrapper = this.orb.getLogWrapperTable().get_RPC_ENCODING_ORBUtil() ;
	if (!directRead) {
	    initObjectInputStream();
	}
	debug = ((ORB)orb).transportDebugFlag;
	if (debug) {
	    ORBUtility.dprint(this, "IDLJavaSerializationInputStream init");
	}
    }

    /**
     * @return a stream object from pool, if available, or a newly created one.
     */
    private static IDLByteArrayInputStream
            getOrCreateByteArrayInputStream(byte[] b, ORB orb) {
	IDLByteArrayInputStream bis = null;
	bis = getInputStreamObjectFromPool();
	if (bis == null) {
	    return new IDLByteArrayInputStream(b);
	}
	bis.init(b);
	return bis;
    }

    // Called from read_octet or read_long or read_ulong method.
    // This method is called after the direct reads (first 16 bytes)
    // is over. Thus, this lazily creates the object input stream.
    private void initObjectInputStream() {
	if (is != null) {
	    throw wrapper.javaStreamInitFailed();
	}
	try {
	    is = bis.getParent();
	    if (is != null) {
		is.setOrb(orb);
		return;
	    }
	    bis.save();
	    bis.init(streamHeader);
	    is = new IDLObjectInputStream(bis, orb);
	    bis.restore();
	    bis.setParent(is);
	} catch (Exception e) {
	    throw wrapper.javaStreamInitFailed(e);
	}
    }

    // Utility method

    private Object readFromMarkedItemQ() {
	if (!markOn && !(markedItemQ.isEmpty())) { // dequeue
	    return markedItemQ.removeFirst();
	}
	if (markOn && !(markedItemQ.isEmpty()) &&
	        (peekIndex < peekCount)) { // peek
	    return markedItemQ.get(peekIndex++);
	}
	return null;
    }

    // org.omg.CORBA.portable.InputStream

    // Primitive types.

    public boolean read_boolean() {
	Object obj = readFromMarkedItemQ();
	if (obj != null) {
	    return ((Boolean)obj).booleanValue();
	}
	try {
	    boolean value = is.readBoolean();
	    if (markOn) { // enqueue
		markedItemQ.addLast(Boolean.valueOf(value));
	    }
	    return value;
	} catch (Exception e) {
	    throw wrapper.javaSerializationException(e, "read_boolean");
	}
    }

    public char read_char() {
	Object obj = readFromMarkedItemQ();
	if (obj != null) {
	    return ((Character)obj).charValue();
	}
	try {
	    char value = is.readChar();
	    if (markOn) { // enqueue
		markedItemQ.addLast(new Character(value));
	    }
	    return value;
	} catch (Exception e) {
	    throw wrapper.javaSerializationException(e, "read_char");
	}
    }

    public char read_wchar() {
	return read_char();
    }

    public byte read_octet() {

	Object obj = readFromMarkedItemQ();
	if (obj != null) {
	    return ((Byte)obj).byteValue();
	}

	byte value;

	try {

	    // check if size < [ GIOPHeader(12) + requestID(4)] bytes
	    if (directRead && 
		    (bis.position() <
		         IDLJavaSerializationOutputStream.DIRECT_RW_LENGTH)) {
		byte b = (byte) bis.read();
		if (bis.position() ==
		        IDLJavaSerializationOutputStream.DIRECT_RW_LENGTH) {
		    initObjectInputStream();
		}
		value = b;
	    } else {
		value = is.readByte();
	    }

	    if (markOn) { // enqueue
		//markedItemQ.addLast(Byte.valueOf(value)); // only in JDK 1.5
		markedItemQ.addLast(new Byte(value));
	    }

	    return value;

	} catch (Exception e) {
	    throw wrapper.javaSerializationException(e, "read_octet");
	}
    }

    public short read_short() {
	Object obj = readFromMarkedItemQ();
	if (obj != null) {
	    return ((Short)obj).shortValue();
	}
	try {
	    short value = is.readShort();
	    if (markOn) { // enqueue
		markedItemQ.addLast(new Short(value));
	    }
	    return value;
	} catch (Exception e) {
	    throw wrapper.javaSerializationException(e, "read_short");
	}
    }

    public short read_ushort() {
	return read_short();
    }

    public int read_long() {

	Object obj = readFromMarkedItemQ();
	if (obj != null) {
	    return ((Integer)obj).intValue();
	}

	try {

	    int value;

	    // check if size < [ GIOPHeader(12) + requestID(4)] bytes
	    if (directRead &&
		    (bis.position() <
		         IDLJavaSerializationOutputStream.DIRECT_RW_LENGTH)) {

		// Use big endian (network byte order). This is fixed.
		// Both the writer and reader use the same byte order.
		int b1 = (bis.read() << 24) & 0xFF000000;
		int b2 = (bis.read() << 16) & 0x00FF0000;
		int b3 = (bis.read() << 8)  & 0x0000FF00;
		int b4 = (bis.read() << 0)  & 0x000000FF;

		if (bis.position() ==
		        IDLJavaSerializationOutputStream.DIRECT_RW_LENGTH) {
		    initObjectInputStream();
		} else if (bis.position() >
			       IDLJavaSerializationOutputStream.
			           DIRECT_RW_LENGTH) {
		    // Should not happen. All direct reads are contained
		    // within the first 16 bytes.
		    wrapper.javaSerializationException("read_long");
		}

		value = (b1 | b2 | b3 | b4);

	    } else {
		value = is.readInt();
	    }

	    if (markOn) { // enqueue
		markedItemQ.addLast(new Integer(value));
	    }

	    return value;

	} catch (Exception e) {
	    throw wrapper.javaSerializationException(e, "read_long");
	}
    }

    public int read_ulong() {
	return read_long();
    }

    public long read_longlong() {
	Object obj = readFromMarkedItemQ();
	if (obj != null) {
	    return ((Long)obj).longValue();
	}
	try {
	    long value = is.readLong();
	    if (markOn) { // enqueue
		markedItemQ.addLast(new Long(value));
	    }
	    return value;
	} catch (Exception e) {
	    throw wrapper.javaSerializationException(e, "read_longlong");
	}
    }

    public long read_ulonglong() {
	return read_longlong();
    }

    public float read_float() {
	Object obj = readFromMarkedItemQ();
	if (obj != null) {
	    return ((Float)obj).floatValue();
	}
	try {
	    float value = is.readFloat();
	    if (markOn) { // enqueue
		markedItemQ.addLast(new Float(value));
	    }
	    return value;
	} catch (Exception e) {
	    throw wrapper.javaSerializationException(e, "read_float");
	}
    }

    public double read_double() {
	Object obj = readFromMarkedItemQ();
	if (obj != null) {
	    return ((Double)obj).doubleValue();
	}
	try {
	    double value = is.readDouble();
	    if (markOn) { // enqueue
		markedItemQ.addLast(new Double(value));
	    }
	    return value;
	} catch (Exception e) {
	    throw wrapper.javaSerializationException(e, "read_double");
	}
    }

    // String types.

    public String read_string() {
	Object obj = readFromMarkedItemQ();
	if (obj != null) {
	    return (String) obj;
	}
	try {
	    String value = is.readUTF();
	    if (markOn) { // enqueue
		markedItemQ.addLast(value);
	    }
	    return value;
	} catch (Exception e) {
	    throw wrapper.javaSerializationException(e, "read_string");
	}
    }

    public String read_wstring() {
	Object obj = readFromMarkedItemQ();
	if (obj != null) {
	    return (String) obj;
	}
	try {
	    String value = (String) is.readObject();
	    if (markOn) { // enqueue
		markedItemQ.addLast(value);
	    }
	    return value;
	} catch (Exception e) {
	    throw wrapper.javaSerializationException(e, "read_wstring");
	}
    }

    // Array types.

    public void read_boolean_array(boolean[] value, int offset, int length){
	for(int i = 0; i < length; i++) {
    	    value[i+offset] = read_boolean();
    	}
    }

    public void read_char_array(char[] value, int offset, int length) {
    	for(int i=0; i < length; i++) {
    	    value[i+offset] = read_char();
    	}	
    }

    public void read_wchar_array(char[] value, int offset, int length) {
	read_char_array(value, offset, length);
    }

    public void read_octet_array(byte[] value, int offset, int length) {
	Object obj = readFromMarkedItemQ();
	if (obj != null) {
	    byte[] b = (byte[]) obj;
	    if (length != b.length) {
		throw wrapper.javaSerializationException("read_octet_array");
	    }
	    System.arraycopy(b, 0, value, offset, length);
	    return;
	}
	try {
	    int off = offset;
	    int len = length;
	    if (debug) {
		ORBUtility.dprint(this, "off: " + off + ", len: " + len);
	    }
	    while (len > 0) {
		int n = is.read(value, off, len);
		off += n;
		len -= n;
		if (debug) {
		    ORBUtility.dprint(this, "off: " + off + ", len: " + len);
		}
	    }
	    if (markOn) { // enqueue
		byte[] b = new byte[length];
		System.arraycopy(value, offset, b, 0, length);
		markedItemQ.addLast(b);
	    }
	} catch (Exception e) {
	    throw wrapper.javaSerializationException(e, "read_octet_array");
	}
    }

    public void read_short_array(short[] value, int offset, int length) {
    	for(int i=0; i < length; i++) {
    	    value[i+offset] = read_short();
    	}	
    }

    public void read_ushort_array(short[] value, int offset, int length) {
	read_short_array(value, offset, length);
    }

    public void read_long_array(int[] value, int offset, int length) {
    	for(int i=0; i < length; i++) {
    	    value[i+offset] = read_long();
    	}	
    }

    public void read_ulong_array(int[] value, int offset, int length) {
	read_long_array(value, offset, length);
    }

    public void read_longlong_array(long[] value, int offset, int length) {
    	for(int i=0; i < length; i++) {
    	    value[i+offset] = read_longlong();
    	}	
    }

    public void read_ulonglong_array(long[] value, int offset, int length) {
	read_longlong_array(value, offset, length);
    }

    public void read_float_array(float[] value, int offset, int length) {
    	for(int i=0; i < length; i++) {
    	    value[i+offset] = read_float();
    	}
    }

    public void read_double_array(double[] value, int offset, int length) {
    	for(int i=0; i < length; i++) {
    	    value[i+offset] = read_double();
    	}
    }

    // Complex types.

    public org.omg.CORBA.Object read_Object() {
	return read_Object(null);
    }

    public TypeCode read_TypeCode() {
        TypeCodeImpl tc = new TypeCodeImpl(orb);
        tc.read_value(parent);
	return tc;	
    }

    public Any read_any() {

        Any any = orb.create_any();
        TypeCodeImpl tc = new TypeCodeImpl(orb);

        // read off the typecode
	
        // REVISIT We could avoid this try-catch if we could peek the typecode
	// kind off this stream and see if it is a tk_value.
        // Looking at the code we know that for tk_value the Any.read_value()
	// below ignores the tc argument anyway (except for the kind field).
        // But still we would need to make sure that the whole typecode,
	// including encapsulations, is read off.
        try {
            tc.read_value(parent);
        } catch (org.omg.CORBA.MARSHAL ex) {
            if (tc.kind().value() != org.omg.CORBA.TCKind._tk_value) {
                throw ex;
	    }
            // We can be sure that the whole typecode encapsulation has been
	    // read off.
            ex.printStackTrace();
        }

        // read off the value of the any.
        any.read_value(parent, tc);

        return any;	
    }

    @SuppressWarnings({"deprecation"})
    public org.omg.CORBA.Principal read_Principal() {
	// We don't need an implementation for this method,
	// since principal is absent in GIOP version 1.2 or above.
	throw wrapper.giopVersionError();
    }

    public BigDecimal read_fixed() {
	return (BigDecimal) read_value();
    }

    public org.omg.CORBA.Object read_Object(java.lang.Class clz) {

	// In any case, we must first read the IOR.
	IOR ior = IORFactories.makeIOR(orb, (InputStream)parent) ;
	if (ior.isNil()) {
	    return null;
	}

	PresentationManager.StubFactoryFactory sff = 
	    ORB.getStubFactoryFactory();
	String codeBase = ior.getProfile().getCodebase();
	PresentationManager.StubFactory stubFactory = null;

        if (clz == null) {
	    RepositoryId rid = RepositoryId.cache.getId(ior.getTypeId());
	    String className = rid.getClassName();
	    boolean isIDLInterface = rid.isIDLType();

	    if (className == null || className.equals( "" )) {
		stubFactory = null;
	    } else {
		try {
		    stubFactory = sff.createStubFactory(className, 
			isIDLInterface, codeBase, (Class) null, 
			(ClassLoader) null);
		} catch (Exception exc) {
		    // Could not create stubFactory, so use null.
		    // XXX stubFactory handling is still too complex:
		    // Can we resolve the stubFactory question once in 
		    // a single place?
		    stubFactory = null ;
		}
	    }
        } else if (StubAdapter.isStubClass(clz)) {
	    stubFactory = PresentationDefaults.makeStaticStubFactory(clz);
	} else {
	    // clz is an interface class
	    boolean isIDL = IDLEntity.class.isAssignableFrom(clz);

	    stubFactory = sff.createStubFactory(
		 clz.getName(), isIDL, codeBase, clz, clz.getClassLoader());
	}

	return CDRInputStream_1_0.internalIORToObject(ior, stubFactory, orb);
    }

    public org.omg.CORBA.ORB orb() {
	return orb;
    }

    // org.omg.CORBA_2_3.portable.InputStream

    public Serializable read_value() {
	Object obj = readFromMarkedItemQ();
	if (obj != null) {
	    return (Serializable) obj;
	}
	try {
	    Serializable value = (java.io.Serializable) is.readObject();
	    if (markOn) { // enqueue
		markedItemQ.addLast(value);
	    }
	    return value;
	} catch (Exception e) {
	    throw wrapper.javaSerializationException(e, "read_value");
	}
    }

    public java.io.Serializable read_value(java.lang.Class clz) {
	return read_value();
    }

    public java.io.Serializable read_value(
            org.omg.CORBA.portable.BoxedValueHelper factory) {
	return read_value();
    }

    public java.io.Serializable read_value(java.lang.String rep_id) {
	return read_value();
    }

    public java.io.Serializable read_value(java.io.Serializable value) {
	return read_value();
    }

    public java.lang.Object read_abstract_interface() {
	return read_abstract_interface(null);
    }

    public java.lang.Object read_abstract_interface(java.lang.Class clz) {
    	boolean isObject = read_boolean();
        if (isObject) {
            return read_Object(clz);
        } else {
            return read_value();
	}
    }

    // com.sun.corba.se.impl.encoding.MarshalInputStream
    public void consumeEndian() {
	read_boolean(); // ignore. Network byte order (big-endian) assumed.
    }

    public int getPosition() {
	return bis.position();
    }

    // org.omg.CORBA.DataInputStream
    public java.lang.Object read_Abstract() {
        return read_abstract_interface();
    }

    public java.io.Serializable read_Value() {
        return read_value();
    }

    public void read_any_array (org.omg.CORBA.AnySeqHolder seq,
				int offset, int length) {
	read_any_array(seq.value, offset, length);
    }

    private final void read_any_array(org.omg.CORBA.Any[] value,
				     int offset, int length) {
    	for(int i=0; i < length; i++) {
    	    value[i+offset] = read_any();
    	}
    }

    public void read_boolean_array (org.omg.CORBA.BooleanSeqHolder seq,
				    int offset, int length){
	read_boolean_array(seq.value, offset, length);
    }

    public void read_char_array (org.omg.CORBA.CharSeqHolder seq,
				 int offset, int length){
	read_char_array(seq.value, offset, length);
    }

    public void read_wchar_array (org.omg.CORBA.WCharSeqHolder seq,
				  int offset, int length){
	read_wchar_array(seq.value, offset, length);
    }

    public void read_octet_array (org.omg.CORBA.OctetSeqHolder seq,
				  int offset, int length){
	read_octet_array(seq.value, offset, length);
    }

    public void read_short_array (org.omg.CORBA.ShortSeqHolder seq,
				  int offset, int length){
	read_short_array(seq.value, offset, length);
    }

    public void read_ushort_array (org.omg.CORBA.UShortSeqHolder seq,
				   int offset, int length){
	read_ushort_array(seq.value, offset, length);
    }

    public void read_long_array (org.omg.CORBA.LongSeqHolder seq,
				 int offset, int length){
	read_long_array(seq.value, offset, length);
    }

    public void read_ulong_array (org.omg.CORBA.ULongSeqHolder seq,
				  int offset, int length){
	read_ulong_array(seq.value, offset, length);
    }

    public void read_ulonglong_array (org.omg.CORBA.ULongLongSeqHolder seq,
				      int offset, int length){
	read_ulonglong_array(seq.value, offset, length);
    }

    public void read_longlong_array (org.omg.CORBA.LongLongSeqHolder seq,
				     int offset, int length){
	read_longlong_array(seq.value, offset, length);
    }

    public void read_float_array (org.omg.CORBA.FloatSeqHolder seq,
				  int offset, int length){
	read_float_array(seq.value, offset, length);
    }

    public void read_double_array (org.omg.CORBA.DoubleSeqHolder seq,
				   int offset, int length){
	read_double_array(seq.value, offset, length);
    }

    // org.omg.CORBA.portable.ValueBase

    public String[] _truncatable_ids() {
	throw wrapper.giopVersionError();
    }

    public void mark(int readLimit) {
	if (markOn) { // Nested mark disallowed.
	    throw wrapper.javaSerializationException("mark");
	}
	markOn = true;
	if (!(markedItemQ.isEmpty())) {
	    peekIndex = 0;
	    peekCount = markedItemQ.size();
	}
    }

    public void reset() {
	markOn = false;
	peekIndex = 0;
	peekCount = 0;
    }

    public boolean markSupported() { 
	return true;
    }

    // Needed by AnyImpl and ServiceContexts
    public CDRInputStreamBase dup() {

	if (markOn) {
	    throw wrapper.javaSerializationException(
			      "dup() must not called when mark is on");
	}

	IDLJavaSerializationInputStream result =
	    new IDLJavaSerializationInputStream(directRead);
	
        result.init(orb, byteBuffer, byteBuffer.limit(), false, null);

	// Set dup related data.
	result.setDupData(bis.position());

	// NOTE: Mark/reset data is not carried over. That is, we do not
	// expect dup() to be used while within a mark/reset duration.

	return result;
    }

    void setDupData(int pos) {
	bis.position(pos); // set buffer position
    }

    // Needed by TCUtility
    public java.math.BigDecimal read_fixed(short digits, short scale) {
        // digits isn't really needed here
        StringBuffer buffer = read_fixed_buffer();
        if (digits != buffer.length())
	    throw wrapper.badFixed( new Integer(digits),
		new Integer(buffer.length()) ) ;
        buffer.insert(digits - scale, '.');
        return new BigDecimal(buffer.toString());
    }

    // Each octet contains (up to) two decimal digits. If the fixed type has
    // an odd number of decimal digits, then the representation
    // begins with the first (most significant) digit.
    // Otherwise, this first half-octet is all zero, and the first digit
    // is in the second half-octet.
    // The sign configuration, in the last half-octet of the representation,
    // is 0xD for negative numbers and 0xC for positive and zero values.
    private StringBuffer read_fixed_buffer() {
        StringBuffer buffer = new StringBuffer(64);
        byte doubleDigit;
        int firstDigit;
        int secondDigit;
        boolean wroteFirstDigit = false;
        boolean more = true;
        while (more) {
            doubleDigit = read_octet();
            firstDigit = (int)((doubleDigit & 0xf0) >> 4);
            secondDigit = (int)(doubleDigit & 0x0f);
            if (wroteFirstDigit || firstDigit != 0) {
                buffer.append(Character.forDigit(firstDigit, 10));
                wroteFirstDigit = true;
            }
            if (secondDigit == 12) {
                // positive number or zero
                if ( ! wroteFirstDigit) {
                    // zero
                    return new StringBuffer("0.0");
                } else {
                    // positive number
                    // done
                }
                more = false;
            } else if (secondDigit == 13) {
                // negative number
                buffer.insert(0, '-');
                more = false;
            } else {
                buffer.append(Character.forDigit(secondDigit, 10));
                wroteFirstDigit = true;
            }
        }
        return buffer;
    }

    // Needed by TypeCodeImpl
    public boolean isLittleEndian() {
	throw wrapper.giopVersionError();
    }

    void setHeaderPadding(boolean headerPadding) {
	// no-op. We don't care about body alignment while using
	// Java serialization. What the GIOP spec states does not apply here.
    }
    
    // Needed by IIOPInputStream and other subclasses

    public ByteBuffer getByteBuffer() {
	throw wrapper.giopVersionError();
    }

    public void setByteBuffer(ByteBuffer byteBuffer) {
	throw wrapper.giopVersionError();
    }

    public void setByteBufferWithInfo(ByteBufferWithInfo bbwi) {
	throw wrapper.giopVersionError();
    }

    public int getBufferLength() {
	throw wrapper.giopVersionError();
	//return bis.getByteBuffer().limit();
    }

    // Caller: CDRInputObject constructor
    public void setBufferLength(int value) {
	// Ignore. The buffer length is already set via the init() call.
    }

    public int getIndex() {
	return bis.position();
    }

    // Caller: CDRInputObject sets the index to 12 bytes (GIOP header length).
    // This is done immedietely after the init() method is called, and before
    // any data is read from the stream.
    public void setIndex(int value) {
	bis.position(value);
    }

    public void orb(org.omg.CORBA.ORB orb) {
	orb = (ORB) orb;
    }

    public BufferManagerRead getBufferManager() {
	return bufferManager;
    }

    public GIOPVersion getGIOPVersion() {
	return GIOPVersion.V1_2;
    }

    com.sun.org.omg.SendingContext.CodeBase getCodeBase() {
        return parent.getCodeBase();
    }

    void alignOnBoundary(int octetBoundary) {
	throw wrapper.giopVersionError();
    }

    void performORBVersionSpecificInit() {
	// No-op.
    }

    public void resetCodeSetConverters() {
	// No-op.
    }

    // ValueInputStream -------------------------

    public void start_value() {
	throw wrapper.giopVersionError();
    }

    public void end_value() {
	throw wrapper.giopVersionError();
    }

    // java.io.InputStream

    public int read() throws IOException {
	throw new UnsupportedOperationException();
    }

    public int read(byte b[]) throws IOException {
	throw new UnsupportedOperationException();
    }

    public int read(byte b[], int off, int len) throws IOException {
	throw new UnsupportedOperationException();
    }

    public long skip(long n) throws IOException {
	throw new UnsupportedOperationException();
    }

    public int available() throws IOException {
	throw new UnsupportedOperationException();
    }

    // caller: CorbaClientRequestDispatcherImpl.endRequest
    // caller: CorbaMessageMediatorImpl.endRequest
    public void close() throws IOException {
	if (bis == null) {
	    return;
	}
	orb.getByteBufferPool().releaseByteBuffer(byteBuffer);
	is.skip(is.available()); // skip unread data
	bis.clear();
	putInputStreamObjectToPool(bis);
	bis = null;
    }

    private static void putInputStreamObjectToPool(
	    IDLByteArrayInputStream value) {
	synchronized (inputStreamPool) {
	    inputStreamPool.add(value);
	}
    }

    private static IDLByteArrayInputStream getInputStreamObjectFromPool() {
	//System.out.println("\t\t\tis size: " + inputStreamPool.size());
	synchronized (inputStreamPool) {
	    if (inputStreamPool.size() != 0) {
		return (IDLByteArrayInputStream)
		    inputStreamPool.remove(0);
	    }
	}
	return null;
    }
}
