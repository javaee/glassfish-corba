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
package com.sun.corba.se.impl.encoding.fast ;

import java.io.ObjectOutputStream ;
import java.io.IOException ;

import java.util.Map ;
import java.util.Queue ;
import java.util.LinkedList ;
import java.util.IdentityHashMap ;

import com.sun.corba.se.spi.orbutil.generic.Pair ;
import com.sun.corba.se.spi.orbutil.generic.Holder ;
import com.sun.corba.se.spi.orbutil.generic.UnaryFunction ;

import com.sun.corba.se.impl.encoding.fast.bytebuffer.Writer ;

/** Encode the data types into the stream.
 * See Codes for the details of the encoding used.
 */
public class OutputStream extends ObjectOutputStream {
    private static final EmergeCode NULL_CODE = 
	EmergeCodeFactory.getCode( EmergeCode.EmergeKind.NULL, null ) ;
    private static final EmergeCode BOOL_ARR_CODE = 
	EmergeCodeFactory.getCode( EmergeCode.EmergeKind.BOOL_ARR, null ) ;
    private static final EmergeCode BYTE_ARR_CODE = 
	EmergeCodeFactory.getCode( EmergeCode.EmergeKind.BYTE_ARR, null ) ;
    private static final EmergeCode CHAR_ARR_CODE = 
	EmergeCodeFactory.getCode( EmergeCode.EmergeKind.CHAR_ARR, null ) ;
    private static final EmergeCode SHORT_ARR_CODE = 
	EmergeCodeFactory.getCode( EmergeCode.EmergeKind.SHORT_ARR, null ) ;
    private static final EmergeCode INT_ARR_CODE = 
	EmergeCodeFactory.getCode( EmergeCode.EmergeKind.INT_ARR, null ) ;
    private static final EmergeCode LONG_ARR_CODE = 
	EmergeCodeFactory.getCode( EmergeCode.EmergeKind.LONG_ARR, null ) ;
    private static final EmergeCode FLOAT_ARR_CODE = 
	EmergeCodeFactory.getCode( EmergeCode.EmergeKind.FLOAT_ARR, null ) ;
    private static final EmergeCode DOUBLE_ARR_CODE = 
	EmergeCodeFactory.getCode( EmergeCode.EmergeKind.DOUBLE_ARR, null ) ;
    private static final EmergeCode REF_ARR_CODE = 
	EmergeCodeFactory.getCode( EmergeCode.EmergeKind.REF_ARR, null ) ;
    private static final EmergeCode REF_CODE = 
	EmergeCodeFactory.getCode( EmergeCode.EmergeKind.REF, null ) ;
    private static final EmergeCode DEREF_CODE = 
	EmergeCodeFactory.getCode( EmergeCode.EmergeKind.REF, null ) ;
    private static final EmergeCode TUPLE_START_CODE = 
	EmergeCodeFactory.getCode( EmergeCode.EmergeKind.TUPLE, 
	    EmergeCode.TupleCode.TUPLE_START ) ;
    private static final EmergeCode TUPLE_END_CODE = 
	EmergeCodeFactory.getCode( EmergeCode.EmergeKind.TUPLE, 
	    EmergeCode.TupleCode.TUPLE_END ) ;
    private static final EmergeCode PART_NO_CUSTOM_CODE = 
	EmergeCodeFactory.getCode( EmergeCode.EmergeKind.PART,	
	    EmergeCode.PartCode.NO_CUSTOM ) ;
    private static final EmergeCode PART_HAS_CUSTOM_CODE = 
	EmergeCodeFactory.getCode( EmergeCode.EmergeKind.PART, 
	    EmergeCode.PartCode.HAS_CUSTOM ) ;
    private static final EmergeCode INDIR_CODE = 
	EmergeCodeFactory.getCode( EmergeCode.EmergeKind.INDIR, 
	    null ) ;

    // Interface used to write out a reference.
    private interface ReferenceWriter {
	void write( Object obj ) throws IOException ;
    }

    // Outline of concurrent marshaling algorithm:
    // 1. Allocate 2 or more marshalers that do the marshaling
    // 2. OutputStream state that is shared is:
    //	    replacements
    //	    workQ
    //    These data structure must allow for highly concurrent access
    // 3. OutputStream state that is not shared is:
    //	    writer
    //	    firstTime
    //	    cmHolder
    //	    insideBody
    //	    currentObject
    //	    currentClassMarshaler
    //
    // Marshaling steps:
    // 1. An OutputStream is created.
    // 2. Workers are assigned to the stream
    // 3. The client writes the top-level data to the stream.
    // 4. Each worker grabs a workQ item and marshals it.
    // 5. One workQ is empty AND all workers are idle, marshaling is done.
    //
    // Note that we want to parallelize computation, not just IO.  But we
    // also need to maintain a reasonable number of threads.  Probably best
    // just to grab threads from a ThreadPool and attach them to the OutputStream
    // when it is constructed.
    
    // Used to records writeReplace results on objects, so that
    // subsequent references to replaced objects do not call writeReplace
    // again.  Must be a concurrent IdentityHashMap.
    //
    // XXX This map needs to have some special characteristics:
    //
    // 1. If key is not a key in the map, the key is returned.
    // 2. null is never a key in the map.
    // 3. null may be a value in the map.
    private final Map<Object,Object> replacements ;

    // XXX Use a ConcurrentLinkedQueue for MT-safe access
    private final Queue<Pair<Object,ReferenceWriter>> workQ = 
	new LinkedList<Pair<Object,ReferenceWriter>>() ;

    private LabelManager labelManager ;

    // The rest of the data members need to be allocated in each thread 

    private final Writer writer ;

    private final Holder<Boolean> firstTime = 
	new Holder<Boolean>() ;

    private final Holder<ClassMarshaler> cmHolder = 
	new Holder<ClassMarshaler>() ;

    // Set to true if we are writing the body of a reference or a tuple.
    private boolean insideBody = true ;

    // the object cnd the ClassMarshaler for the object currently being 
    // custom marshaled.  Note that the CM is necessary because we need
    // the fields for the exact class in the superclass chain that is being
    // marshaled in the call to defaultWriteObject.
    private Object currentObject ; 
    private ClassMarshaler currentClassMarshaler ; 

    // Used in the ClassMarshaler, which must set and unset this
    // around marshaling the fields of an object.
    void setInsideBody( boolean value ) {
	insideBody = value ;
    }

    public void tupleStart() {
	writer.putByte( (byte)TUPLE_START_CODE.code() ) ;
    }

    public void tupleEnd() {
	writer.putByte( (byte)TUPLE_END_CODE.code() ) ;
    }

    /** First call when writing out the contents of a
     * value.  This is called only from ClassMarshaler.
     * @param data the object being written
     * @param numClasses the number of superclasses (number of calls
     * to startClass)
     */
    void startValue( Object data, int numClasses ) {
	writer.putByte( (byte)REF_CODE.code() ) ;
	LabelManager.Label label = getLabel( data, objectWriter ) ;
	label.put( writer ) ;
	VarOctetUtility.put( writer, numClasses) ;
    }

    /** Called at the start of each class in the superclass chain
     * for a value.
     * @param isCustomMarshaled true if the class has a writeObject method.
     * @param typeName the name of this class (or repositoryId)
     * @param numFields the number of fields that need to be marshaled (not
     * counting custom marshaling).
     */
    void startClass( boolean isCustomMarshaled,
	char[] typeName, int numFields ) {

	if (isCustomMarshaled) {
	    writer.putByte( (byte)PART_HAS_CUSTOM_CODE.code() ) ; // PART-has_custom
	} else {
	    writer.putByte( (byte)PART_NO_CUSTOM_CODE.code() ) ; // PART-no_custom
	}

	writer.putCharArray( typeName ) ;   // type-label
	VarOctetUtility.put( writer, 0 ) ;	    // offset
	VarOctetUtility.put( writer, numFields ) ;   // length
    }

    void startCustomPart( Object obj, ClassMarshaler cm ) {
	tupleStart() ;
	currentObject = obj ;
	currentClassMarshaler = cm ;
    }

    void endCustomPart() {
	tupleEnd() ;
	currentObject = null ;
	currentClassMarshaler = null ;
    }

    /** Create a new output stream with a writer
     * and 0 or more contexts.  
     * @param writer The ByteBufferWriter used to write the data.
     * @param context The contexts to use, starting with 
     * EmergeCodeFactory.SESSION_CONTEXT_ID. MESSAGE_CONTEXT_ID
     * is always local to the message and is created by this
     * constructor.
     * XXX When this is multithreaded, it will need to manage its
     * own writer, since we may want multiple writers as well as multiple threads.
     * Or perhaps we configure this externally?
     */
    public OutputStream( final Writer writer,
	LookupTable<Object,LabelManager.Label> extContext ) throws IOException {
	labelManager = new LabelManager( extContext ) ;
	this.writer = writer ;
	this.replacements = new IdentityHashMap<Object,Object>() ;

	writer.putByte( (byte)TUPLE_START_CODE.code() ) ;
    }

    private ReferenceWriter charArrayWriter = new ReferenceWriter() {
	public void write( Object obj ) throws IOException {
	    OutputStream.this.writeCharArray( (char[])obj ) ;
	}
    } ;

    private ReferenceWriter booleanArrayWriter = new ReferenceWriter() {
	public void write( Object obj ) throws IOException {
	    OutputStream.this.writeBooleanArray( (boolean[])obj ) ;
	}
    } ;

    private ReferenceWriter byteArrayWriter = new ReferenceWriter() {
	public void write( Object obj ) throws IOException {
	    OutputStream.this.writeByteArray( (byte[])obj ) ;
	}
    } ;

    private ReferenceWriter shortArrayWriter = new ReferenceWriter() {
	public void write( Object obj ) throws IOException {
	    OutputStream.this.writeShortArray( (short[])obj ) ;
	}
    } ;

    private ReferenceWriter intArrayWriter = new ReferenceWriter() {
	public void write( Object obj ) throws IOException {
	    OutputStream.this.writeIntArray( (int[])obj ) ;
	}
    } ;

    private ReferenceWriter longArrayWriter = new ReferenceWriter() {
	public void write( Object obj ) throws IOException {
	    OutputStream.this.writeLongArray( (long[])obj ) ;
	}
    } ;

    private ReferenceWriter floatArrayWriter = new ReferenceWriter() {
	public void write( Object obj ) throws IOException {
	    OutputStream.this.writeFloatArray( (float[])obj ) ;
	}
    } ;

    private ReferenceWriter doubleArrayWriter = new ReferenceWriter() {
	public void write( Object obj ) throws IOException {
	    OutputStream.this.writeDoubleArray( (double[])obj ) ;
	}
    } ;

    private ReferenceWriter objectArrayWriter = new ReferenceWriter() {
	public void write( Object obj ) throws IOException {
	    OutputStream.this.writeValueArray( (Object[])obj ) ;
	}
    } ;

    private ReferenceWriter objectWriter = new ReferenceWriter() {
	public void write( Object obj ) throws IOException {
	    OutputStream.this.writeObjectOverride( obj ) ;
	}
    } ;

    private ReferenceWriter getReferenceWriter( Class cls ) {
	if (cls.isArray()) {
	    Class compType = cls.getComponentType() ;
	    if (compType.isPrimitive()) {
		if (compType == Character.TYPE) {
		    return charArrayWriter ;
		} else if (compType == Byte.TYPE) {
		    return byteArrayWriter ;
		} else if (compType == Short.TYPE) {
		    return shortArrayWriter ;
		} else if (compType == Integer.TYPE) {
		    return intArrayWriter ;
		} else if (compType == Long.TYPE) {
		    return longArrayWriter ;
		} else if (compType == Float.TYPE) {
		    return floatArrayWriter ;
		} else if (compType == Double.TYPE) {
		    return doubleArrayWriter ;
		} else if (compType == Boolean.TYPE) {
		    return booleanArrayWriter ;
		}

                throw new IllegalStateException( "can't happen!" ) ;
	    } else {
		return objectArrayWriter ;
	    }
	} else {
	    return objectWriter ;
	}
    }
    
    /** As always, flush causes any buffered data to be flushed.
     * The different here is that ALL of the object graph referenceable
     * from objects marshaled before the flush call is marshaled after
     * the flush call.
     * XXX This is not how the MT-hot case works.
     */
    public void flush() throws IOException {
	writer.putByte( (byte)TUPLE_END_CODE.code() ) ;
	setInsideBody( false ) ;

	// Write out all of the data in the workQ
	Pair<Object,ReferenceWriter> data = null ;
	while ((data = workQ.poll()) != null) {
	    data.second().write( data.first() ) ;
	}
	writer.flush() ;
    }

    public void close() {
	// XXX What else is needed here?
        try {
            writer.close() ;
        } catch (Exception exc) {
            // stupid close exception!
        }
    }

    public void writeBoolean( boolean data ) throws IOException {
	if (data) {
	    writer.putByte( EmergeCodeFactory.BOOL_TRUE ) ;
	} else {
	    writer.putByte( EmergeCodeFactory.BOOL_FALSE ) ;
	}
    }

    public void writeByte( byte data ) throws IOException {
	EmergeCode code = EmergeCodeFactory.getCode( 
	    EmergeCode.EmergeKind.BYTE, data ) ;
	writer.putByte( (byte)code.code() ) ;
	if (!code.hasEncodedValue()) {
	    writer.putByte( data ) ;
	}
    }

    public void writeChar( char data ) throws IOException {
	EmergeCode code = EmergeCodeFactory.getCode( 
	    EmergeCode.EmergeKind.CHAR, null ) ;
	writer.putByte( (byte)code.code() ) ;
    }

    public void writeShort( short data ) throws IOException {
	EmergeCode code = EmergeCodeFactory.getCode( 
	    EmergeCode.EmergeKind.SHORT, data ) ;
	writer.putByte( (byte)code.code() ) ;
	if (!code.hasEncodedValue()) {
	    writer.putShort( data ) ;
	}
    }

    public void writeInt( int data ) throws IOException {
	EmergeCode code = EmergeCodeFactory.getCode( 
	    EmergeCode.EmergeKind.INT, data ) ;
	writer.putByte( (byte)code.code() ) ;
	if (!code.hasEncodedValue()) {
	    writer.putInt( data ) ;
	}
    }

    public void writeLong( long data ) throws IOException {
	EmergeCode code = EmergeCodeFactory.getCode( 
	    EmergeCode.EmergeKind.LONG, data ) ;
	writer.putByte( (byte)code.code() ) ;
	if (!code.hasEncodedValue()) {
	    writer.putLong( data ) ;
	}
    }

    public void writeFloat( float data ) throws IOException {
	EmergeCode code = EmergeCodeFactory.getCode( 
	    EmergeCode.EmergeKind.FLOAT, data ) ;
	writer.putByte( (byte)code.code() ) ;
	if (!code.hasEncodedValue()) {
	    writer.putFloat( data ) ;
	}
    }

    public void writeDouble( double data ) throws IOException {
	EmergeCode code = EmergeCodeFactory.getCode( 
	    EmergeCode.EmergeKind.DOUBLE, data ) ;
	writer.putByte( (byte)code.code() ) ;
	if (!code.hasEncodedValue()) {
	    writer.putDouble( data ) ;
	}
    }

    // rw can be null, in which case we compute it here.  This is
    // done to avoid computing rw in cases where it is not needed.
    private LabelManager.Label getLabel( Object data, ReferenceWriter rw ) {
	LabelManager.Label label = labelManager.lookup( firstTime, data ) ;

	// If this is the first time we have assigned a label to
	// this Object, add it to the workQ, because we have not
	// yet marshaled its contents.
	if (firstTime.content()) {
	    if (rw == null)
		rw = getReferenceWriter( data.getClass() ) ;

	    workQ.offer( new Pair<Object,ReferenceWriter>( data, rw ) ) ;
	}

	return label ;
    }

    private LabelManager.Label handleIndir( Object data, ReferenceWriter rw ) {
	LabelManager.Label label = getLabel( data, rw ) ;

	if (insideBody) {
	    writer.putByte( (byte)INDIR_CODE.code() ) ;
	    label.put( writer ) ;
	} 

	return label ;
    }
    
    private void writePrimitiveArrayHeader( EmergeCode arrCode, 
	LabelManager.Label label, int length ) {

	writer.putByte( (byte)arrCode.code() ) ;  // <>_ARR
	label.put( writer ) ;		    // self-label
	VarOctetUtility.put( writer, 0 ) ;	    // offset (not used here)
	VarOctetUtility.put( writer, length ) ;	    // length
    }

    public void writeBooleanArray( boolean[] data ) {
	LabelManager.Label label = handleIndir( data, booleanArrayWriter ) ;

	if (!insideBody) {
	    writePrimitiveArrayHeader( BOOL_ARR_CODE, label, 
		data.length ) ;

	    for (boolean b : data) {
		writer.putBoolean( b ) ;
	    }
	}
    }

    public void writeByteArray( byte[] data ) {
	LabelManager.Label label = handleIndir( data, byteArrayWriter ) ;

	if (!insideBody) {
	    writePrimitiveArrayHeader( BYTE_ARR_CODE, label, 
		data.length ) ;

	    for (byte b : data) {
		writer.putByte( b ) ;
	    }
	}
    }

    public void writeCharArray( char[] data ) {
	LabelManager.Label label = handleIndir( data, charArrayWriter ) ;

	if (!insideBody) {
	    writePrimitiveArrayHeader( CHAR_ARR_CODE, label, 
		data.length ) ;

	    for (char c : data) {
		writer.putChar( c ) ;
	    }
	}
    }

    public void writeShortArray( short[] data ) {
	LabelManager.Label label = handleIndir( data, shortArrayWriter ) ;

	if (!insideBody) {
	    writePrimitiveArrayHeader( SHORT_ARR_CODE, label, 
		data.length ) ;

	    for (short s : data) {
		writer.putShort( s ) ;
	    }
	}
    }

    public void writeIntArray( int[] data ) {
	LabelManager.Label label = handleIndir( data, intArrayWriter ) ;

	if (!insideBody) {
	    writePrimitiveArrayHeader( INT_ARR_CODE, label, 
		data.length ) ;

	    for (int i : data) {
		writer.putInt( i ) ;
	    }
	}
    }

    public void writeLongArray( long[] data ) {
	LabelManager.Label label = handleIndir( data, longArrayWriter ) ;

	if (!insideBody) {
	    writePrimitiveArrayHeader( LONG_ARR_CODE, label, 
		data.length ) ;

	    for (long elem : data) {
		writer.putLong( elem ) ;
	    }
	}
    }

    public void writeFloatArray( float[] data ) {
	LabelManager.Label label = handleIndir( data, floatArrayWriter ) ;

	if (!insideBody) {
	    writePrimitiveArrayHeader( FLOAT_ARR_CODE, label, 
		data.length ) ;

	    for (float fl : data) {
		writer.putFloat( fl ) ;
	    }
	}
    }

    public void writeDoubleArray( double[] data ) {
	LabelManager.Label label = handleIndir( data, doubleArrayWriter ) ;

	if (!insideBody) {
	    writePrimitiveArrayHeader( DOUBLE_ARR_CODE, label, 
		data.length ) ;

	    for (double d : data) {
		writer.putDouble( d ) ;
	    }
	}
    }

    /** Write this value.  This means writing a REF header followed by all 
     * of the fields for this value.  obj must be serializable or 
     * externalizable.
     */

     /* NOTE: like RMI-IIOP, we do not support writeUnshared semantics.
     *
     * NOTE: IIOP and JRMP have slightly different writeReplace semantics.
     * IIOP will call writeReplace multiple times for the same object, while
     * JRMP only calls the method once and records that call in a table.
     * Does this cause errors?  No one to my knowledge has ever noticed this before.
     * Is the JRMP impl even correct (see writeObject0, where it assigns to the parameter!).
     * Also, IIOP writeReplace only gets called once, whereas JRMP allows a chain of
     * writeReplace calls.
     *
     * Note that JRMP allows subclassing of ObjectOutputStream, which is what we are
     * doing here of course, and so also allows a stream-level replaceObject that can be
     * handled in the stream.  However, we do NOT support further subclassing of this
     * OutputStream by the client, so we don't need to deal with replaceObject.
     *
     * Unfortunately, writeReplace adds a complication to the emerge protocol.
     * Support object A contains a field f1 that refers to object B, and B has
     * a writeReplace method that will result in B being replaced with C.
     * When we write A, we do not analyze B, so we cannot know yet that f1
     * should refer to C instead of B.  So we marshal a reference to B for A.f1,
     * assuming that we will later marshal B.  But we never marshal B in this case.
     * What we need to do is add another typecode to emerge:
     *
     * DEREF B C
     *
     * which means that we should replace all occurrences to label B with whatever
     * label C references.  As always, it is entirely possible that we have not
     * seen at least C yet when DEREF B C is encountered.
     *
     * Patterns of calls
     *
     * When an object is written to the stream, or an object is processed from the
     * work list:
     *	    writeObject
     *		writeObjectOverride
     *		    start insideBody
     *		    cm.writeObject
     *		    end insideBody
     *
     * When a custom marshalled object calls its writeObject method:
     *	    Call to writeObject (for a data member):
     *		writeObject
     *		    writeObjectOverride
     *			we are already insideBody, so just write a reference
     *
     *	    Call to defaultWriteObject
     *		(must already be insideBody)
     *		cm.writeObject
     *
     * As usual, if we are already insideBody, we just write a label,
     * otherwise we set insideBody and write the contents of the value.
     *
     * Replacement (writeReplace) complicates this.  If we are insideBody,
     * we just write the reference to the replacement object (we may already
     * have written the replacement, which is fine).  If we are NOT insideBody,
     * we have the situation where some other object references the object
     * that needs to be replaced, and that reference needs to be fixed.
     * This is why we need the DEREF object-label replacement-label code,
     * which indicates to the reader that all occurrences of object-label
     * should be replaced by replacement-label.
     */
    public void writeObjectOverride( Object obj ) throws IOException {
	if (obj == null) {
	    writer.putByte( (byte)NULL_CODE.code() ) ;
	    return ;
	}

	ClassMarshaler cm = ClassMarshalerFactory.getClassMarshaler( 
	    obj.getClass() ) ;

        Holder<ClassMarshaler> cmHolder = new Holder<ClassMarshaler>( cm ) ;

	Object replacement = replacements.get( obj ) ;
	if (replacement == obj) {
            // obj is not currently in the map, so see if it needs to be replaced.
	    replacement = cm.handleReplace( obj, cmHolder ) ;
	
	    if (replacement != obj) {
		replacements.put( obj, replacement ) ;
	    }
	}

	if (replacement == null) {
	    writer.putByte( (byte)NULL_CODE.code() ) ;
	    return ;
	}

	// Handle special cases:
	// enum: make EnumDesc
	// proxy: make ProxyDesc
	// class: some sort of class desc (not the same as JDK; more like CORBA)
	// Do we do anything special for strings?
	// Do arrays come through this path?

	// If we are already insideBody, this writes out the label
	LabelManager.Label label = handleIndir( replacement, objectWriter ) ;

	if (!insideBody) {
	    Class type = obj.getClass() ;

	    setInsideBody( true ) ;
	    try {
		// writeFields handles the class and all serializable
		// super classes of obj, and all the other details of
		// serialization.  writeFields calls methods on OutputStream
		// to write each field according to its type.  If writeObject
		// is called, we end up back here.
		cmHolder.content().writeObject( obj, this ) ;
	    } finally {
		setInsideBody( false ) ;
	    }
	}
    }

    void writeValueArray( Object[] data ) {
	LabelManager.Label label = handleIndir( data, objectArrayWriter ) ;
	Class dataType = data.getClass() ;
	Class compType = data.getClass().getComponentType() ;
	ClassMarshaler cm = ClassMarshalerFactory.getClassMarshaler( compType ) ;
	char[] selfType = cm.getTypeName() ;
	LabelManager.Label typeLabel = getLabel( selfType, objectArrayWriter ) ;

	if (!insideBody) {
	    writer.putByte( (byte)REF_ARR_CODE.code() ) ;	// REF_ARR
	    label.put( writer ) ;			        // self-label
	    typeLabel.put( writer ) ;			        // type-label
	    VarOctetUtility.put( writer, 0 ) ;			// offset (not used here)
	    VarOctetUtility.put( writer, data.length ) ;	// length

	    // Regardless of the compType of the array, each element can be of
	    // a different type, unless the compType is a final class.  This code
	    // does not optimize the final case.
	    for (Object object : data) {
		LabelManager.Label elem = getLabel( object, null ) ;
		elem.put( writer ) ;
	    }
	}
    }

    // ObjectOutputStream methods (other than basic write<> primitives)
    //

    public void defaultWriteObject() throws IOException {
	// this does the work of writing the object if called after
	// writeObjectOverride sets up the current object.
	if ((currentObject == null) || (currentClassMarshaler == null))
	    throw new IOException( 
		"Cannot call defaultWriteObject outside of a writeObject method" ) ;

	currentClassMarshaler.writeClassFields( currentObject, this ) ;
    }

    // only valid inside call to writeObject, which is actually calling
    // writeObjectOverride here, so we need to save the object being written.
    // Because we NEVER recurse from one object to another, there is no need
    // to stack the current object or current PutField.
    public ObjectOutputStream.PutField putFields() throws IOException {
	throw new IOException( "putFields not yet supported" ) ;
    }

    public void reset() throws IOException {
	// XXX CORBA: reset classDescStack?
	// Should discard state (JRMP writes a TC_RESET to the stream and forgets
	// all substitutions and handles in the stream)
	// No idea what this should do, really.
    }

    // CORBA: write( xxx ) methods just write bytes as appropriate
    public void write( int val )  throws IOException {
	// XXX implement me
    }

    public void write( byte[] buf ) throws IOException {
	// XXX implement me
    }

    public void write( byte[] buf, int off, int len )  throws IOException {
	// XXX implement me
    }

    public void writeBytes( String val )  throws IOException {
	// CORBA: convert string to byte[] (code has BUG: should specify char set)
	// and then write_octet_array
	// XXX implement me
    }

    public void writeChars( String val )  throws IOException {
	// CORBA: convert to char[], then write_wchar_array
	// XXX implement me
    }

    public void writeUTF( String val )  throws IOException {
	// CORBA: we just call CDR write_wstring for this
	// XXX implement me
    }

    // XXX need to add PutField implementation: factory out of OutputStreamHook?
    // XXX clean up names
    // XXX Why does the JDK need 3 ways to write the characters in a String?
}
