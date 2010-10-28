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

import java.io.IOException ;
import java.io.Externalizable ;
import java.io.ObjectOutputStream ;

import java.lang.reflect.Field ;

import java.util.LinkedList ;

import java.security.PrivilegedAction ;
import java.security.AccessController ;

import sun.corba.Bridge ;

import com.sun.corba.se.spi.orbutil.generic.Holder ;

import com.sun.corba.se.impl.io.ObjectStreamField ;

// An implementation of this class will probably use the ClassAnalyzer, probably
// with some optimization: using Unsafe for field access, possibly using codegen
// to create an implementation of ClassMarshaler that is customized for a particular
// type.
//

/** Reads and writes an instance of a Class.
 */
public class ClassMarshaler<T> {

    /** Bridge is used to access unsafe methods used to read and write 
     * arbitrary data members in objects.  
     * This is very fast, and completely ignores access
     * protections including final on fields.
     * NOTE WELL: Unsafe is capabile of causing severe damage to the
     * VM, including causing the VM to dump core.  get and put calls
     * must only be made with offsets obtained from objectFieldOffset
     * calls.  Because of the dangerous nature of Unsafe, its use 
     * must be carefully protected.
     */
    private static final Bridge bridge = AccessController.doPrivileged(
        new PrivilegedAction<Bridge>() {
            public Bridge run() {
                return Bridge.get();
            }
        }
    ) ;

    private char[] typeName ;
    // List of ClassMarshalers from first serializable superclass to this class
    private LinkedList<ClassMarshaler<?>> cmChain ;

    private interface ObjectWriter {
	public void write( Object obj, 
	    OutputStream os ) throws IOException ;
    }

    private ObjectWriter writer ;
    private ClassAnalyzer<T> classAnalyzer ;

    public ClassMarshaler( ClassAnalyzer<T> ca ) {
	typeName = ca.getName().toCharArray() ;
    
	cmChain = new LinkedList<ClassMarshaler<?>>() ;
        classAnalyzer = ca ;
	ClassAnalyzer<?> current = ca ;
	do {
	    ClassMarshaler<?> cm = ClassMarshalerFactory.getClassMarshaler(
		current.getClass() ) ;
	    cmChain.addFirst( cm ) ;
	    current = current.getSuperClassAnalyzer() ;
	} while (current != null) ;

	// Make sure an ObjectWriter is created if one is needed for writeObject 
	// and defaultWriteObject.
	if (ca.isSerializable() && !ca.hasWriteObjectMethod()) {
            writer = makeObjectWriter(ca.forClass(), ca.getFields());
        }
    }

    public boolean isImmutable() {
	return false ; // XXX implement me
    }

    /** Get the string that should be used for the type-label for 
     * all instances of this class.  This could be a class name, or a CORBA
     * style repository ID string.
     */
    public char[] getTypeName() {
	// XXX probably need repo ID here
	return cmChain.get(0).getClassAnalyzer().getNameAsCharArray() ;
    }

    public ClassAnalyzer<T> getClassAnalyzer() {
	return classAnalyzer ;
    }

    public ObjectOutputStream.PutField getPutField() {
	throw new UnsupportedOperationException( "PutField not yet supported" ) ;
    }

    /*
	* XXX we need to re-do this to be a bit better.  It must write the
	* fields in the canonical order (as determined by the ClassAnalyzer).
	* In fact, we should just get an instance of PutField from the
	* ClassAnalyzer.  This will allow the ClassAnalyzer to generate a
	* custom implementation of PutFields.
	*
    private class HookPutFields extends ObjectOutputStream.PutField
    {
	private Map<String,Object> fields = new HashMap<String,Object>();

	public void put(String name, boolean value){
	    fields.put(name, Boolean.valueOf(value));
	}
		
	public void put(String name, char value){
	    fields.put(name, Character.valueOf(value));
	}
		
	public void put(String name, byte value){
	    fields.put(name, Byte.valueOf(value));
	}
		
	public void put(String name, short value){
	    fields.put(name, Short.valueOf(value));
	}
		
	public void put(String name, int value){
	    fields.put(name, Integer.valueOf(value));
	}
		
	public void put(String name, long value){
	    fields.put(name, Long.valueOf(value));
	}
		
	public void put(String name, float value){
	    fields.put(name, Float.valueOf(value));
	}
		
	public void put(String name, double value){
	    fields.put(name, Double.valueOf(value));
	}
		
	public void put(String name, Object value){
	    fields.put(name, value);
	}
		
	public void write(ObjectOutput out) throws IOException {
            OutputStreamHook hook = (OutputStreamHook)out;

            ObjectStreamField[] osfields = hook.getFieldsNoCopy();

            // Write the fields to the stream in the order
            // provided by the ObjectStreamClass.  (They should
            // be sorted appropriately already.)
            for (int i = 0; i < osfields.length; i++) {

                Object value = fields.get(osfields[i].getName());

                hook.writeField(osfields[i], value);
            }
	}
    }

    private void writeFields( Object obj, ObjectStreamField[] fields, OutputStream os ) {
	for (ObjectStreamField field : fields) {
	    switch (field.getTypeCode()) {
		case 'B' :
		case 'C' :
		case 'S' :
		case 'I' :
		case 'J' :
		case 'F' :
		case 'D' :
		case 'Z' :
		case '[' :
		    String signature = field.getSignature() ;
		    char compTypeCode = signature.charAt(1) ;
		    switch (compTypeCode) {
			case 'B' :
			case 'C' :
			case 'S' :
			case 'I' :
			case 'J' :
			case 'F' :
			case 'D' :
			case 'Z' :
			case '[' :
			case 'L' :
		    }
		case 'L' :
	    }
	}
    }
    */

    public Object handleReplace( Object obj, 
        Holder<ClassMarshaler<?>> cmHolder ) throws IOException {

	ClassAnalyzer<?> currentCa = cmHolder.content().getClassAnalyzer() ;
	Object replacement = obj ;
	while (true) {
	    // Check for further replacement
	    if (!currentCa.hasWriteReplaceMethod()) {
                break;
            }

	    Object newReplacement = currentCa.invokeWriteReplace( replacement ) ;
	    if (newReplacement == null) {
		cmHolder.content( null ) ;
		replacement = null ;
		break ;
	    }

	    if (newReplacement == replacement) {
                break;
            }

	    ClassMarshaler<?> cm = ClassMarshalerFactory.getClassMarshaler(
		newReplacement.getClass() ) ;
            cmHolder.content( cm ) ;
	    currentCa = cm.getClassAnalyzer() ;
	    replacement = newReplacement ;
	}

	return replacement ;
    }

    public void writeObject( Object obj, OutputStream os ) throws IOException {
	os.startValue( obj, cmChain.size() ) ;
        ClassAnalyzer<?> ca = cmChain.get(0).getClassAnalyzer() ;
	if (ca.isExternalizable()) {
            writeExternalData(obj, os);
        } else if (ca.isSerializable()) {
            writeSerialData(obj, os);
        } // XXX else
	    // ERROR
    }
	   
    // Writing fields.
    // Two cases:
    // 1. From object.
    // Follow copyobject code:
    //	    1. May do codegen later, but not first.
    //	    2. All writers can be stateless (no recursion or type specificity in
    //	       reference case).
    //	    3. Basic idea: have 9 different objects that read data from an offset
    //	       in the object, then write that data to the output stream.
    // 2. From PutFields.
    // (later)

    private interface FieldWriter {
	void write( Object obj, long offset, 
	    OutputStream os ) throws IOException ;
    }

    private static final FieldWriter byteWriter = new FieldWriter() {
	public void write( Object obj, long offset, OutputStream os ) throws IOException {
	    byte val = bridge.getByte( obj, offset ) ;
	    os.writeByte( val ) ;
	}
    } ;

    private static final FieldWriter booleanWriter = new FieldWriter() {
	public void write( Object obj, long offset, OutputStream os ) throws IOException {
	    boolean val = bridge.getBoolean( obj, offset ) ;
	    os.writeBoolean( val ) ;
	}
    } ;

    private static final FieldWriter charWriter = new FieldWriter() {
	public void write( Object obj, long offset, OutputStream os ) throws IOException {
	    char val = bridge.getChar( obj, offset ) ;
	    os.writeChar( val ) ;
	}
    } ;

    private static final FieldWriter shortWriter = new FieldWriter() {
	public void write( Object obj, long offset, OutputStream os ) throws IOException {
	    short val = bridge.getShort( obj, offset ) ;
	    os.writeShort( val ) ;
	}
    } ;

    private static final FieldWriter intWriter = new FieldWriter() {
	public void write( Object obj, long offset, OutputStream os ) throws IOException {
	    int val = bridge.getInt( obj, offset ) ;
	    os.writeInt( val ) ;
	}
    } ;

    private static final FieldWriter longWriter = new FieldWriter() {
	public void write( Object obj, long offset, OutputStream os ) throws IOException {
	    long val = bridge.getLong( obj, offset ) ;
	    os.writeLong( val ) ;
	}
    } ;

    private static final FieldWriter floatWriter = new FieldWriter() {
	public void write( Object obj, long offset, OutputStream os ) throws IOException {
	    float val = bridge.getFloat( obj, offset ) ;
	    os.writeFloat( val ) ;
	}
    } ;

    private static final FieldWriter doubleWriter = new FieldWriter() {
	public void write( Object obj, long offset, OutputStream os ) throws IOException {
	    double val = bridge.getDouble( obj, offset ) ;
	    os.writeDouble( val ) ;
	}
    } ;

    private static final FieldWriter nonPrimitiveWriter = new FieldWriter() {
	public void write( Object obj, long offset, OutputStream os ) throws IOException {
	    Object val = bridge.getObject( obj, offset ) ;
	    os.writeObject( val ) ;
	}
    } ;

    private FieldWriter getFieldWriter( Class<?> fldType ) {
	if (fldType.isPrimitive()) {
	    if (fldType == byte.class) {
		return byteWriter ;
	    } else if (fldType == boolean.class) {
		return booleanWriter ;
	    } else if (fldType == char.class) {
		return charWriter ;
	    } else if (fldType == short.class) {
		return shortWriter ;
	    } else if (fldType == int.class) {
		return intWriter ;
	    } else if (fldType == long.class) {
		return longWriter ;
	    } else if (fldType == float.class) {
		return floatWriter ;
	    } else if (fldType == double.class) {
		return doubleWriter ;
	    }

            throw new IllegalStateException( "This can't happen" ) ;
	} else {
	    return nonPrimitiveWriter ;
	}
    }

    private ObjectWriter makeObjectWriter( final Class<?> cls,
        // was: final List<Pair<String,Class>> info ) {
        final ObjectStreamField[] info ) {

	int size = info.length ;
	final long[] offsets = new long[ size ] ;
	final FieldWriter[] fieldWriters = new FieldWriter[ size ] ;

	try {
	    int ctr = 0 ;
	    for (ObjectStreamField osf : info) {
		String fieldName = osf.getName() ;
		Class<?> fieldType = osf.getClazz() ;
		Field fld = cls.getDeclaredField( fieldName ) ;
		if (fld.getType() != fieldType) {
		    // ERROR
		}

		offsets[ctr] = bridge.objectFieldOffset( fld ) ;
		fieldWriters[ctr] = getFieldWriter( fieldType ) ;
		ctr++ ;
	    }
	} catch (Exception exc) {
	    // ERROR
	}
	
	return new ObjectWriter() {
	    public void write( Object obj, OutputStream os ) throws IOException {
		for (int ctr=0; ctr<offsets.length; ctr++) {
		    fieldWriters[ctr].write( obj, offsets[ctr], os ) ;
		}
	    }
	} ;
    }

    private void writeExternalData( Object obj, OutputStream os ) throws IOException {
	if (!(obj instanceof Externalizable))
	    ; // ERROR

	Externalizable ext = (Externalizable)obj ;
	ext.writeExternal( os ) ;
    }

    // Needed by OutputStream.defaultWriteObject().
    void writeClassFields( Object obj, OutputStream os ) throws IOException {
	writer.write( obj, os ) ;
    }

    private void writeSerialData( Object obj, OutputStream os ) throws IOException {
	for (int ctr = cmChain.size(); ctr >= 0; ctr-- ) {
	    ClassMarshaler<?> cm = cmChain.get(ctr) ;
	    ClassAnalyzer<?> ca = cm.getClassAnalyzer() ;
	    char[] typeName = ca.getNameAsCharArray() ;

	    os.startClass( ca.hasWriteObjectMethod(), typeName, ca.getFields().length ) ;

	    if (ca.hasWriteObjectMethod()) {
		os.startCustomPart( obj, cm ) ;
		ca.invokeWriteObject( obj, os ) ;
		os.endCustomPart() ;
	    } else {
		cm.writeClassFields( obj, os ) ;
	    }
	}
    }

    /** Create an instance of the class supported by this ClassAnalyzer.
     * This is constructed according to the usual rules of Java serialization.
     */
    public Object create() {
        try {
            ClassMarshaler<?> cm = cmChain.get(0) ;
            ClassAnalyzer<?> ca = cm.getClassAnalyzer() ;
            if (!ca.isInstantiable())
                ; // ERROR

            return ca.newInstance() ;
        } catch (Exception exc) {
            throw new RuntimeException( exc ) ;
        }
    }

    /** Read data into obj from is.  
     *
    void readFields( Object obj, InputStream is ) {
    }
    */
}
