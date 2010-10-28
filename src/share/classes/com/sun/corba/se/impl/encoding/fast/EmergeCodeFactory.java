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

/** Codes used for fast marshaling. Code defines (usually) a type,
 * which may be followed by a value.  LABEL denotes a positive int,
 * while LEN[TYPE] denotes a non-negative int followed by that
 * many values of the TYPE.
 * <p>
 * This is a flat marshalling scheme.  By that I mean that each object
 * is fully marshalled without ever starting to marshal nested objects.
 * This is an experiment to see what works more quickly.
 * <p>
 * This class provides the definition of the format, some enums used
 * to represent elements of the format, and some basic methods for manipulating
 * these codes and representations.
 * <p>All Codes are represented in 8 bits:
 * <ul>
 * <li>bit 7: 0=primitive 1=non primitive
 * <li>bits 6-3: Primitive or NonPrimitive enum ordinal
 * <li>bits 2-0: encoded value, if any (7 = no encoded value)
 * </ul>
 * 3 bits can represent 0...6, but we subtract 1 to get -1...5.
 * <p>
 * The following grammar describes the encoding:
 * <pre>
 * Grammar:
 *
 * octet is given as simply an 8-bit unsigned number in this grammar
 *
 * Useful definitions:
 *
 * 2-octet	: octet octet ;
 * 4-octet	: 2-octet 2-octet ;
 * 8-octet	: 4-octet 4-octet ;
 * final-octet	: 128...255 ;		    // (octet: encoding 0...127))
 * cont-octet	: 0...127 ;		    // (octet: encoding 0...127)
 * var-octet	: cont-octet* final-octet ; // at most 8 cont-octet
 * 
 * length	: var-octet ;
 * offset	: var-octet ;
 * label	: context-part id-part ;
 * context-part	: var-octet ;
 * id-part	: var-octet
 * self-label	: label ;
 * type-label	: label ;
 *
 * bool-seq	: bool-seq bool-value
 *		| bool-value ;
 *
 * bool-value	: BOOL-true | BOOL-false ;
 *
 * octet-seq	: octet 
 *		| octet-seq ;
 *
 * 2-octet-seq	: 2-octet 
 *		| 2-octet-seq ;
 *
 * 4-octet-seq	: 4-octet 
 *		| 4-octet-seq ;
 *
 * 8-octet-seq	: 8-octet 
 *		| 8-octet-seq ;
 *
 * The different values:
 *
 * primitive	: NULL
 *		| BOOL-true	| BOOL-false
 *		| BYTE-value	| BYTE-novalue octet
 *		| CHAR 2-octet 
 *		| SHORT-value	| SHORT-novalue 2-octet
 *		| INT-value	| INT-novalue 4-octet
 *		| LONG-value	| LONG-novalue 8-octet
 *		| FLOAT-value	| FLOAT-novalue 4-octet
 *		| DOUBLE-value	| DOUBLE-novalue 8-octet
 *		| INDIR label ;
 *
 * Note: we could consider changing the encoding slightly so
 * that SHORT-novalue, INT-novalue, and LONG-novalue encode a sign bit, 
 * and then simple use a var-octet to encode the unsigned value.
 * 
 * primitive-arr	: BOOL_ARR self-label offset length bool-seq
 *			| BYTE_ARR self-label offset length octet-seq
 *			| CHAR_ARR self-label offset length 2-octet-seq
 *			| SHORT_ARR self-label offset length 2-octet-seq
 *			| INT_ARR self-label offset length 4-octet-seq
 *			| LONG_ARR self-label offset length 8-octet-seq
 *			| FLOAT_ARR self-label offset length 4-octet-seq
 *			| DOUBLE_ARR self-label offset length 8-octet-seq ;
 *
 * ref-arr		| REF_ARR self-label type-label offset length label-seq ;
 *
 * label-seq		: label
 *			| label-seq label ;
 *
 * value		: value-header value-body-seq ;
 *
 * value-header		: REF self-label length ;
 *
 * XXX assume we do not support parallel marshaling of a single value-body (as its probably not
 * very useful in general).  Should we change default-part to:
 *
 * length prim-length data-seq label-seq
 *
 * This would avoid marshaling a bunch of INDIR bytecodes by taking advantage of the
 * primitive/non-primitive ordering in the serialization code.
 *
 * value-body		: PART-custom type-label default-part custom-part
 *			| PART-simple type-label default-part ;
 *
 * default-part		: offset length data-seq ;  // Number of data-seq elem is len
 *
 * custom-part		: tuple-seq ;
 *
 * tuple-seq		: TUPLE-start data-seq TUPLE-end ;
 *
 * data-seq		: primitive 
 *			| data-seq primitive ;
 *
 * ref-seq		: ref-data
 *			| ref-data-seq ;
 *
 * ref-data		: primitive-arr
 *			| ref-arr
 *			| value ;
 *
 * ref-data-seq	: ref-data
 *			| ref-data-seq ref-data ;
 *
 * Emerge LLP message encodings:
 *
 * message		: header body trailer ;
 *			| fiber-list
 *			| close-session
 *			| reject-request
 *			| request-label-value
 *			| reply-label-value ;
 *
 * request-label-value  : LABEL_MSG-request label ;
 *
 * reply-label-value    : LABEL_MSG-reply_good label ref-data ;
 *			| LABEL_MSG-reply_error label reason-code ;
 *
 * close-session	: CLOSE_SESSION session_id ;
 *
 * reject-request	: REJECT_REQUEST reason-code ;
 *
 * fiber-list		: FLIST length fiber-id-list ;
 *
 * header		: MSG-start request-id session-id fiber-id num-args ;
 *
 * num-fibers		: var-octet ;
 * reason-code		: category minor-code ;
 * category		: var-octet ;
 * minor-code		: var-octet ;
 * request-id		: var-octet ;
 * session-id		: var-octet ;
 * fiber-id		: var-octet ;
 * num-args		: var-octet ;
 *
 * body			: tuple-seq ref-seq ;
 *
 * trailer		: MSG-end request-id session-id fiber-id num-args ;
 * </pre>
 *
 * Notes:
 * <ul>
 * <li>Everthing is written to and read from a stream.  Writing will never fragment
 * a primitive or var-octet across buffers, but reading can.
 * <li>Most var-octets will be 1 or 2 (and occasionally 3) octets long.  Anything 
 * longer should almost never occur.
 * <li>Multiple threads (referred to here as fibers) may marshal data concurrently.
 * <li>A number of contexts are supported.  The standard ones are given in the 
 * XXX_CONTEXT_ID constants.
 * </ul>
 */
public final class EmergeCodeFactory {    
    private EmergeCodeFactory() {} 

    public static final int MIN_VALID_INT_CODE = 0 ;
    public static final int MAX_VALID_INT_CODE = 255 ;

    // Context IDs used in labels.
    public static final int MESSAGE_CONTEXT_ID		= 0 ;
    public static final int SESSION_CONTEXT_ID		= 1 ;
    public static final int SENDER_IMMUTABLE_CONTEXT_ID = 2 ;
    public static final int FIRST_GLOBAL_CONTEXT_ID	= 3 ;

    private static final int NUM_BITS_PER_BYTE = 8 ;
    private static final int NUM_CODE_BITS = 5 ;
    private static final int NUM_VALUE_BITS = 
	NUM_BITS_PER_BYTE - NUM_CODE_BITS ;

    private static byte NO_ENCODED_VALUE = (byte)7 ;

    private static int getCodeValue( EmergeCode.EmergeKind kind ) {
	return kind.ordinal() << NUM_VALUE_BITS ;
    }

    private enum CodeClass { NONE, PRIMITIVE, ARRAY, SIMPLE_MESSAGE } 

    private static abstract class EmergeCodeBase implements EmergeCode {
	private int code ;
	private EmergeCode.EmergeKind kind ;
	private Object value ;
        private CodeClass cc ;

        @Override
        public String toString() {
            return "EmergeCode[" + code + " (" + kind + ")" 
                + ((value == null) ? 
                    "" :
                    " " + value)
                + "]" ;
        }

	EmergeCodeBase( int code, EmergeCode.EmergeKind kind, Object value ) {
            this( code, kind, value, CodeClass.NONE ) ;
        }

	EmergeCodeBase( int code, EmergeCode.EmergeKind kind, Object value,
            CodeClass cc ) {
	    if ((code<0) || (code >255))
		; // ERROR

	    this.code = code ;
            this.kind = kind ;
            this.value = value ;
            this.cc = cc ;
	}

	EmergeCodeBase() {
	    this( 0, null, null ) ;
	}

	public <T> T getValue( Class<T> cls ) {
	    if (value == null) {
		throw new IllegalArgumentException( this 
		    + " does not contain a value" ) ;
	    }

	    return cls.cast( value ) ;
	}

	public int code() {
	    return code ;
	}

	public byte byteCode() {
	    if (code < 128) {
                return (byte) code;
            } else {
                return (byte) (code-256);
            }
	}

	public EmergeCode.EmergeKind getKind() {
	    return kind ;
	}

	public boolean hasEncodedValue() {
	    return value != null ;
	}

	public boolean isValidEmergeCode() {
	    return true ;
	}

	public boolean isPrimitive() {
	    return cc == CodeClass.PRIMITIVE ;
	}

        public boolean isArray() {
            return cc == CodeClass.ARRAY ;
        }

        public boolean isSimpleMessage() {
            return cc == CodeClass.SIMPLE_MESSAGE ;
        }

        @Override
	public boolean equals( Object obj ) {
	    if (obj == this) {
                return true;
            }

	    if (!(obj instanceof EmergeCodeBase)) {
                return false;
            }

	    EmergeCodeBase other = (EmergeCodeBase)obj ;

	    return other.code == code ;
	}

        @Override
	public int hashCode() {
	    return code ;
	}
    }

    private static final class EmergeCodeInvalidImpl extends EmergeCodeBase {
	EmergeCodeInvalidImpl( int code ) {
	    super( code, null, null ) ;
	}

	public EmergeType getType() {
	    throw new IllegalArgumentException( code() + " is not a valid Emerge code" ) ;
	}

        @Override
	public EmergeCode.EmergeKind getKind() {
	    throw new IllegalArgumentException( code() + " is not a valid Emerge code" ) ;
	}

        @Override
	public <T> T getValue( Class<T> cls ) {
	    throw new IllegalArgumentException( code() + " is not a valid Emerge code" ) ;
	}

        @Override
	public boolean hasEncodedValue() {
	    throw new IllegalArgumentException( code() + " is not a valid Emerge code" ) ;
	}

        @Override
	public boolean isValidEmergeCode() {
	    return false ;
	}

        @Override
        public boolean isPrimitive() {
	    throw new IllegalArgumentException( code() + " is not a valid Emerge code" ) ;
        }

        @Override
        public boolean isArray() {
	    throw new IllegalArgumentException( code() + " is not a valid Emerge code" ) ;
        }

        @Override
        public boolean isSimpleMessage() {
	    throw new IllegalArgumentException( code() + " is not a valid Emerge code" ) ;
        }
    }

    private static final class EmergeCodeOptionalValueImpl extends EmergeCodeBase {
	EmergeCodeOptionalValueImpl( int code, EmergeCode.EmergeKind kind ) {
	    this( code, kind, null ) ;
	}

	EmergeCodeOptionalValueImpl( int code, EmergeCode.EmergeKind kind, Object value ) {
	    super( code, kind, value, CodeClass.PRIMITIVE ) ;
	}

	public EmergeType getType() {
	    return EmergeType.OPTIONAL_VALUE ;
	}
    }

    private static final class EmergeCodeEncodedImpl extends EmergeCodeBase {
	EmergeCodeEncodedImpl( int code, EmergeCode.EmergeKind kind, Object value, 
            CodeClass cc ) {
	    super( code, kind, value, cc ) ;
            
	    if (value == null) {
                throw new IllegalArgumentException("value cannot be null");
            }
        }
            
	EmergeCodeEncodedImpl( int code, EmergeCode.EmergeKind kind, Object value ) {
	    this( code, kind, value, CodeClass.NONE ) ;
	}

	public EmergeType getType() {
	    return EmergeType.ENCODED ;
	}
    }

    private static final class EmergeCodeSimpleImpl extends EmergeCodeBase {
	EmergeCodeSimpleImpl( int code, EmergeCode.EmergeKind kind, CodeClass cc ) {
            super( code, kind, null, cc ) ;
        }

	EmergeCodeSimpleImpl( int code, EmergeCode.EmergeKind kind ) {
            super( code, kind, null, CodeClass.NONE ) ;
        }

	public EmergeType getType() {
	    return EmergeType.SIMPLE ;
	}
    }

    private final static EmergeCode[] emergeCodes = new EmergeCode[256] ;

    // Initialize emergeCodes
    static {
    // OPTIONAL_VALUE section
    // BYTE
	int start = EmergeCode.EmergeKind.BYTE.ordinal() << NUM_VALUE_BITS ;
	for (int ctr = 0; ctr<NO_ENCODED_VALUE; ctr++) {
	    emergeCodes[start+ctr] = new EmergeCodeOptionalValueImpl(
		start+ctr, EmergeCode.EmergeKind.BYTE, 
		Byte.valueOf((byte)(ctr-1)) ) ;
	}
	emergeCodes[start+NO_ENCODED_VALUE] = new EmergeCodeOptionalValueImpl(
		start+NO_ENCODED_VALUE, EmergeCode.EmergeKind.BYTE, null ) ;


    // CHAR
	start = EmergeCode.EmergeKind.CHAR.ordinal() << NUM_VALUE_BITS ;
	for (int ctr = 0; ctr<NO_ENCODED_VALUE; ctr++) {
	    emergeCodes[start+ctr] = new EmergeCodeInvalidImpl( start+ctr ) ;
	}
        emergeCodes[start+NO_ENCODED_VALUE] = new EmergeCodeSimpleImpl(
            start+NO_ENCODED_VALUE, EmergeCode.EmergeKind.CHAR, CodeClass.PRIMITIVE ) ;

    // SHORT
	start = EmergeCode.EmergeKind.SHORT.ordinal() << NUM_VALUE_BITS ;
	for (int ctr = 0; ctr<NO_ENCODED_VALUE; ctr++) {
	    emergeCodes[start+ctr] = new EmergeCodeOptionalValueImpl(
		start+ctr, EmergeCode.EmergeKind.SHORT, 
		Short.valueOf((byte)(ctr-1)) ) ;
	}
	emergeCodes[start+NO_ENCODED_VALUE] = new EmergeCodeOptionalValueImpl(
		start+NO_ENCODED_VALUE, EmergeCode.EmergeKind.SHORT, null ) ;

    // INT
	start = EmergeCode.EmergeKind.INT.ordinal() << NUM_VALUE_BITS ;
	for (int ctr = 0; ctr<NO_ENCODED_VALUE; ctr++) {
	    emergeCodes[start+ctr] = new EmergeCodeOptionalValueImpl(
		start+ctr, EmergeCode.EmergeKind.INT, 
		Integer.valueOf((byte)(ctr-1)) ) ;
	}
	emergeCodes[start+NO_ENCODED_VALUE] = new EmergeCodeOptionalValueImpl(
		start+NO_ENCODED_VALUE, EmergeCode.EmergeKind.INT, null ) ;

    // LONG
	start = EmergeCode.EmergeKind.LONG.ordinal() << NUM_VALUE_BITS ;
	for (int ctr = 0; ctr<NO_ENCODED_VALUE; ctr++) {
	    emergeCodes[start+ctr] = new EmergeCodeOptionalValueImpl(
		start+ctr, EmergeCode.EmergeKind.LONG, 
		Long.valueOf((byte)(ctr-1)) ) ;
	}
	emergeCodes[start+NO_ENCODED_VALUE] = new EmergeCodeOptionalValueImpl(
		start+NO_ENCODED_VALUE, EmergeCode.EmergeKind.LONG, null ) ;
    
    // FLOAT
        start = EmergeCode.EmergeKind.FLOAT.ordinal() << NUM_VALUE_BITS ;
        emergeCodes[start + 0] = new EmergeCodeOptionalValueImpl(
            start + 0, EmergeCode.EmergeKind.FLOAT,
            Float.valueOf( (float)-1.0 ) ) ;
        emergeCodes[start + 1] = new EmergeCodeOptionalValueImpl(
            start + 1, EmergeCode.EmergeKind.FLOAT,
            Float.valueOf( (float)0.0 ) ) ;
        emergeCodes[start + 2] = new EmergeCodeOptionalValueImpl(
            start + 2, EmergeCode.EmergeKind.FLOAT,
            Float.valueOf( (float)1.0 ) ) ;
        for (int ctr=3; ctr<NO_ENCODED_VALUE; ctr++ ) {
            emergeCodes[start + ctr] = new EmergeCodeInvalidImpl(
                start + ctr ) ;
        }
        emergeCodes[start + NO_ENCODED_VALUE ] = new EmergeCodeOptionalValueImpl(
            start + NO_ENCODED_VALUE, EmergeCode.EmergeKind.FLOAT ) ;

    // DOUBLE
        start = EmergeCode.EmergeKind.DOUBLE.ordinal() << NUM_VALUE_BITS ;
        emergeCodes[start + 0] = new EmergeCodeOptionalValueImpl(
            start + 0, EmergeCode.EmergeKind.DOUBLE,
            Double.valueOf( -1.0) ) ;
        emergeCodes[start + 1] = new EmergeCodeOptionalValueImpl(
            start + 1, EmergeCode.EmergeKind.DOUBLE,
            Double.valueOf( 0.0) ) ;
        emergeCodes[start + 2] = new EmergeCodeOptionalValueImpl(
            start + 2, EmergeCode.EmergeKind.DOUBLE,
            Double.valueOf( 1.0) ) ;
        for (int ctr=3; ctr<NO_ENCODED_VALUE; ctr++ ) {
            emergeCodes[start + ctr] = new EmergeCodeInvalidImpl(
                start + ctr ) ;
        }
        emergeCodes[start + NO_ENCODED_VALUE ] = new EmergeCodeOptionalValueImpl(
            start + NO_ENCODED_VALUE, EmergeCode.EmergeKind.DOUBLE ) ;

    // ENCODED_VALUE section
	// BOOL false true
	start = EmergeCode.EmergeKind.BOOL.ordinal() << NUM_VALUE_BITS ;
	emergeCodes[start] = new EmergeCodeEncodedImpl( start, EmergeCode.EmergeKind.BOOL,
	    Boolean.valueOf( false ), CodeClass.PRIMITIVE ) ;
	emergeCodes[start+1] = new EmergeCodeEncodedImpl( start + 1, EmergeCode.EmergeKind.BOOL,
	    Boolean.valueOf( true ), CodeClass.PRIMITIVE ) ;
	for (int ctr=2; ctr<=NO_ENCODED_VALUE; ctr++) {
	    emergeCodes[start+ctr] = new EmergeCodeInvalidImpl( start+ctr ) ;
        }

	// TUPLE EmergeCode.TupleCode TUPLE_START TUPLE_END
	start = EmergeCode.EmergeKind.TUPLE.ordinal() << NUM_VALUE_BITS ;
	EmergeCode.TupleCode[] tupleEnums = EmergeCode.TupleCode.values() ;
	for (int ctr=0; ctr<tupleEnums.length; ctr++) {
	    emergeCodes[start+ctr] = new EmergeCodeEncodedImpl( start+ctr, 
		EmergeCode.EmergeKind.TUPLE, tupleEnums[ctr] ) ;
	}
	for (int ctr=tupleEnums.length; ctr<=NO_ENCODED_VALUE; ctr++) {
	    emergeCodes[start+ctr] = new EmergeCodeInvalidImpl( start+ctr ) ;
	}
	
	// PART EmergeCode.PartCode NO_CUSTOM HAS_CUSTOM
	start = EmergeCode.EmergeKind.PART.ordinal() << NUM_VALUE_BITS ;
	EmergeCode.PartCode[] partEnums = EmergeCode.PartCode.values() ;
	for (int ctr=0; ctr<partEnums.length; ctr++) {
	    emergeCodes[start+ctr] = new EmergeCodeEncodedImpl( start+ctr, 
		EmergeCode.EmergeKind.PART, partEnums[ctr] ) ;
	}
	for (int ctr=partEnums.length; ctr<=NO_ENCODED_VALUE; ctr++) {
	    emergeCodes[start+ctr] = new EmergeCodeInvalidImpl( start+ctr ) ;
	}

	// MSG EmergeCode.MsgCode MSG_START MSG_END
	start = EmergeCode.EmergeKind.MSG.ordinal() << NUM_VALUE_BITS ;
	EmergeCode.MsgCode[] msgCodeEnums = EmergeCode.MsgCode.values() ;
	for (int ctr=0; ctr<msgCodeEnums.length; ctr++) {
	    emergeCodes[start+ctr] = new EmergeCodeEncodedImpl( start+ctr, 
		EmergeCode.EmergeKind.MSG, msgCodeEnums[ctr] ) ;
	}
	for (int ctr=msgCodeEnums.length; ctr<=NO_ENCODED_VALUE; ctr++) {
	    emergeCodes[start+ctr] = new EmergeCodeInvalidImpl( start+ctr ) ;
	}
	
	// LABEL_MSG EmergeCode.LabelMsg REQUEST REPLY_GOOD REPLY_BAD
	start = EmergeCode.EmergeKind.LABEL_MSG.ordinal() << NUM_VALUE_BITS ;
	EmergeCode.LabelMsg[] labelMsgEnums = EmergeCode.LabelMsg.values() ;
	for (int ctr=0; ctr<labelMsgEnums.length; ctr++) {
            EmergeCode.LabelMsg lm = labelMsgEnums[ctr] ;
	    emergeCodes[start+ctr] = new EmergeCodeEncodedImpl( start+ctr, 
		EmergeCode.EmergeKind.LABEL_MSG, lm, 
                lm != EmergeCode.LabelMsg.REPLY_GOOD ? 
                    CodeClass.SIMPLE_MESSAGE :
                    CodeClass.NONE ) ; 
	}
	for (int ctr=labelMsgEnums.length; ctr<=NO_ENCODED_VALUE; ctr++) {
	    emergeCodes[start+ctr] = new EmergeCodeInvalidImpl( start+ctr ) ;
	}
	
    // SIMPLE section
	initializeSimple( EmergeCode.EmergeKind.NULL, CodeClass.PRIMITIVE ) ;
	initializeSimple( EmergeCode.EmergeKind.INDIR, CodeClass.PRIMITIVE ) ;
	initializeSimple( EmergeCode.EmergeKind.BOOL_ARR, CodeClass.ARRAY ) ;
	initializeSimple( EmergeCode.EmergeKind.BYTE_ARR, CodeClass.ARRAY ) ;
	initializeSimple( EmergeCode.EmergeKind.SHORT_ARR, CodeClass.ARRAY ) ;
	initializeSimple( EmergeCode.EmergeKind.CHAR_ARR, CodeClass.ARRAY ) ;
	initializeSimple( EmergeCode.EmergeKind.INT_ARR, CodeClass.ARRAY ) ;
	initializeSimple( EmergeCode.EmergeKind.LONG_ARR, CodeClass.ARRAY ) ;
	initializeSimple( EmergeCode.EmergeKind.FLOAT_ARR, CodeClass.ARRAY ) ;
	initializeSimple( EmergeCode.EmergeKind.DOUBLE_ARR, CodeClass.ARRAY ) ;
	initializeSimple( EmergeCode.EmergeKind.REF_ARR, CodeClass.ARRAY ) ;
	initializeSimple( EmergeCode.EmergeKind.FLIST, CodeClass.SIMPLE_MESSAGE ) ;
	initializeSimple( EmergeCode.EmergeKind.CLOSE_SESSION, CodeClass.SIMPLE_MESSAGE ) ;
	initializeSimple( EmergeCode.EmergeKind.REJECT_REQUEST, CodeClass.SIMPLE_MESSAGE ) ;
	initializeSimple( EmergeCode.EmergeKind.REF, CodeClass.NONE ) ;

    // Invalid values
	initializeInvalid( EmergeCode.EmergeKind.KIND_UNUSED_7 ) ;
	initializeInvalid( EmergeCode.EmergeKind.KIND_UNUSED_13 ) ;
	initializeInvalid( EmergeCode.EmergeKind.KIND_UNUSED_14 ) ;
	initializeInvalid( EmergeCode.EmergeKind.KIND_UNUSED_15 ) ;
	initializeInvalid( EmergeCode.EmergeKind.KIND_UNUSED_31 ) ;
    }

    private static void initializeInvalid( EmergeCode.EmergeKind kind ) {
	int cv = getCodeValue(kind) ;
	for (int ctr=0; ctr<=NO_ENCODED_VALUE; ctr++) {
            emergeCodes[cv + ctr] =
                new EmergeCodeInvalidImpl(cv + ctr);
        }
    }

    private static void initializeSimple( EmergeCode.EmergeKind kind,
        CodeClass cc ) {
	int code = kind.ordinal() << NUM_VALUE_BITS ;
	emergeCodes[ code ] = new EmergeCodeSimpleImpl( code, kind, cc ) ;

	for (int ctr=1; ctr<=NO_ENCODED_VALUE; ctr++) {
            emergeCodes[code + ctr] =
                new EmergeCodeInvalidImpl(code + ctr);
        }
    }

    // The cast is correct here because boolean does not set the upper bit.
    private static final byte BOOL_CODE = (byte)getCodeValue( EmergeCode.EmergeKind.BOOL ) ;
    static final byte BOOL_TRUE = (byte)(BOOL_CODE & 1) ;
    static final byte BOOL_FALSE = BOOL_CODE ;

// Factory methods
    /** Obtain the correct EmergeCode based on the encoded value.
     * XXX Should code be byte or int? Check usage for signs.
     */
    public static EmergeCode getByteCode( byte code ) {
	int index = code ;
	if (code < 0) {
	    code += 256 ;
	}
	return emergeCodes[ index ] ;
    }

    public static EmergeCode getCode( int code ) {
	return emergeCodes[ code ] ;
    }

    /** Obtain the correct EmergeCode for kind and value.
     * @throws IllegalArgumentException if kind is one of the KIND_UNUSED_XXX values
     * @throws IllegalArgumentException if value is not of the appropriate type for kind
     * @throws IllegalArgumentException if value is null when it should not be
     */
    public static EmergeCode getCode( EmergeCode.EmergeKind kind, Object value ) {
	int offset = NO_ENCODED_VALUE ;
        int ordinal = 0 ;

	switch (kind) {
	    // OPTIONAL_VALUE section
	    case CHAR:		    
		if (value != null) {
                    throw new IllegalArgumentException("CHAR cannot encode a value");
                }
		return emergeCodes[(kind.ordinal() << NUM_VALUE_BITS) 
                       + NO_ENCODED_VALUE] ;

	    case BYTE:		    
	    case SHORT:		    
	    case INT:		    
	    case LONG:
                offset = NO_ENCODED_VALUE ;

                if (value != null) {
                    if (!(value instanceof Number)) {
                        throw new IllegalArgumentException(
                            "value must be a number" ) ;
                    }

                    int numValue = ((Number)value).intValue() ;

                    if ((numValue >= -1) && (numValue <= 5)) {
                        offset = numValue + 1 ;
                    } 
                }

		return emergeCodes[(kind.ordinal() << NUM_VALUE_BITS) 
                    + offset ] ;

	    case FLOAT:		    
	    case DOUBLE:		    
                offset = NO_ENCODED_VALUE ;

                if (value != null) {
                    if (!(value instanceof Number)) {
                        throw new IllegalArgumentException(
                            "value must be a number" ) ;
                    }

                    float floatValue = ((Number)value).floatValue() ;
                    if (floatValue == -1.0) {
                        offset = 0;
                    } else if (floatValue == 0.0) {
                        offset = 1;
                    } else if (floatValue == 1.0) {
                        offset = 2;
                    }
                }

		return emergeCodes[(kind.ordinal() << NUM_VALUE_BITS) + offset ] ;
	    
	    // ENCODED section
	    case BOOL:		    
		if (!(value instanceof Boolean))
		    throw new IllegalArgumentException(
			"BOOL requires Boolean value" ) ;
		int index = EmergeCode.EmergeKind.BOOL.ordinal() << NUM_VALUE_BITS ;
		if (((Boolean)value).booleanValue())
		    return emergeCodes[index + 1] ;
		else
		    return emergeCodes[index] ;

	    case TUPLE:		    
		if (!(value instanceof EmergeCode.TupleCode))
		    throw new IllegalArgumentException(
			"TUPLE value must be a TupleCode" ) ;
		ordinal = ((Enum)value).ordinal() ;
		return emergeCodes[(kind.ordinal() << NUM_VALUE_BITS) 
                    + ordinal] ;

	    case PART:		    
		if (!(value instanceof EmergeCode.PartCode))
		    throw new IllegalArgumentException(
			"PART value must be a PartCode" ) ;
		ordinal = ((Enum)value).ordinal() ;
		return emergeCodes[(kind.ordinal() << NUM_VALUE_BITS) 
                    + ordinal] ;

	    case MSG:		    
		if (!(value instanceof EmergeCode.MsgCode))
		    throw new IllegalArgumentException(
			"MSG value must be a MsgCode" ) ;
		ordinal = ((Enum)value).ordinal() ;
		return emergeCodes[(kind.ordinal() << NUM_VALUE_BITS) 
                    + ordinal] ;

	    case LABEL_MSG:	    
		if (!(value instanceof EmergeCode.LabelMsg))
		    throw new IllegalArgumentException(
			"LABEL value must be a LabelMsg" ) ;
		ordinal = ((Enum)value).ordinal() ;
		return emergeCodes[(kind.ordinal() << NUM_VALUE_BITS) 
                    + ordinal] ;

	    // SIMPLE section
	    case NULL:		    
	    case INDIR:		    
	    case BOOL_ARR:	    
	    case BYTE_ARR:	    
	    case CHAR_ARR:	    
	    case SHORT_ARR:	    
	    case INT_ARR:	    
	    case LONG_ARR:	    
	    case FLOAT_ARR:	    
	    case DOUBLE_ARR:	    
	    case REF_ARR:	    
	    case FLIST:		    
	    case CLOSE_SESSION:	    
	    case REJECT_REQUEST:	    
	    case REF:		    
		if (value != null)
		    throw new IllegalArgumentException(
			"EmergeKind cannot have a value" ) ;
		return emergeCodes[kind.ordinal() << NUM_VALUE_BITS] ;

	    case KIND_UNUSED_7:	    
	    case KIND_UNUSED_13:	    
	    case KIND_UNUSED_14:	    
	    case KIND_UNUSED_15:	    
	    case KIND_UNUSED_31:
		throw new IllegalArgumentException(
		    kind + " is an unused EmergeKind" ) ;
	    default:
		return null ;
	}
    }

// var octet methods 
    public static final int NUM_BITS_PER_VAR_OCTET = 7 ;
    public static final int MAX_VALUE_PER_VAR_OCTET = 1 << NUM_BITS_PER_VAR_OCTET ;
    public static final int VAR_OCTET_MASK = MAX_VALUE_PER_VAR_OCTET - 1 ;
    public static final int MAX_OCTETS_FOR_VAR_OCTET = 
        63 / NUM_BITS_PER_VAR_OCTET ;
    
    private static final long MAX_FOR_1_OCTET	= 
	MAX_VALUE_PER_VAR_OCTET ;
    private static final long MAX_FOR_2_OCTET	= 
	MAX_FOR_1_OCTET * MAX_VALUE_PER_VAR_OCTET ;
    private static final long MAX_FOR_3_OCTET	= 
	MAX_FOR_2_OCTET * MAX_VALUE_PER_VAR_OCTET ;
    private static final long MAX_FOR_4_OCTET	= 
	MAX_FOR_3_OCTET * MAX_VALUE_PER_VAR_OCTET ;
    private static final long MAX_FOR_5_OCTET	= 
	MAX_FOR_4_OCTET * MAX_VALUE_PER_VAR_OCTET ;
    private static final long MAX_FOR_6_OCTET	= 
	MAX_FOR_5_OCTET * MAX_VALUE_PER_VAR_OCTET ;
    private static final long MAX_FOR_7_OCTET	= 
	MAX_FOR_6_OCTET * MAX_VALUE_PER_VAR_OCTET ;
    private static final long MAX_FOR_8_OCTET	= 
	MAX_FOR_7_OCTET * MAX_VALUE_PER_VAR_OCTET ;
    private static final long MAX_FOR_9_OCTET	= 
	MAX_FOR_8_OCTET * MAX_VALUE_PER_VAR_OCTET ;

    public static int varOctetSize( long data ) {
	// This is probably the best way to handle this, since we expect that vast 
	// majority of var-octet data to be 1-3 octets.  It is also possible to loop, 
	// or to use Long.numberOfLeadingZeros, but that is likely to be slower.
	
	if (data < 0)
	    throw new IllegalArgumentException( 
		"var-octet cannot encode a negative long" ) ;

	if (data < MAX_FOR_1_OCTET)
	    return 1 ;
	if (data < MAX_FOR_2_OCTET)
	    return 2 ;
	if (data < MAX_FOR_3_OCTET)
	    return 3 ;
	if (data < MAX_FOR_4_OCTET)
	    return 4 ;
	if (data < MAX_FOR_5_OCTET)
	    return 5 ;
	if (data < MAX_FOR_6_OCTET)
	    return 6 ;
	if (data < MAX_FOR_7_OCTET)
	    return 7 ;
	if (data < MAX_FOR_8_OCTET)
	    return 8 ;
	if (data < MAX_FOR_9_OCTET)
	    return 9 ;
	
	throw new IllegalArgumentException(
	    "varOctetSize should not throw an exception!" ) ;
    }
}
