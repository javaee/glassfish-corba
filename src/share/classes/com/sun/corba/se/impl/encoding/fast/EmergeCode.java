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

/** Emerge is the encoding we are using for the fast parallel encoding experiment.
 */
public interface EmergeCode extends Input {
    /** Return the actual value used in the encoded data stream for this code.
     * This is in the range 0..255.
     */
    int code() ;

    /** This is the actual value stored in a byte[] or similar structure for this
     * code.  Range is -128..127.
     */
    byte byteCode() ;

    /** Gives the sort of encoding used for this Code.
     * <ul>
     * <li>OPTIONAL_VALUE may or may not have a value encoded.
     * <li>ENCODED always has an encoded value.
     * <li>SIMPLE cannot have an encoded value.
     * </ul>
     * If this code has an encoded value, it can be obtained from
     * the getValue method in the appropriate type.
     */
    public enum EmergeType { OPTIONAL_VALUE, ENCODED, SIMPLE } ;

    EmergeType getType() ;
 
    /** Used to represent exactly which kind of Code this is.
     * This is the upper 5 bits of the emerge code.
     */
    public enum EmergeKind {
    // OPTIONAL_VALUE section: must cover 0...7
	BYTE,		    // 0 
	CHAR,		    // 1 
	SHORT,		    // 2
	INT,		    // 3
	LONG,		    // 4
	FLOAT,		    // 5
	DOUBLE,		    // 6
	KIND_UNUSED_7,	    // 7
    
    // ENCODED section: must cover 8...15
	BOOL,		    // 8
	TUPLE,		    // 9
	PART,		    // 10
	MSG,		    // 11
	LABEL_MSG,	    // 12
	KIND_UNUSED_13,	    // 13
	KIND_UNUSED_14,	    // 14
	KIND_UNUSED_15,	    // 15

    // SIMPLE section: must cover 16...31
	NULL,		    // 16 
	INDIR,		    // 17
	BOOL_ARR,	    // 18
	BYTE_ARR,	    // 19
	CHAR_ARR,	    // 20
	SHORT_ARR,	    // 21
	INT_ARR,	    // 22
	LONG_ARR,	    // 23
	FLOAT_ARR,	    // 24
	DOUBLE_ARR,	    // 25
	REF_ARR,	    // 26
	FLIST,		    // 27
	CLOSE_SESSION,	    // 28
	REJECT_REQUEST,	    // 29
	REF,		    // 30
	KIND_UNUSED_31	    // 31
    } ;
    
    // Value for TUPLE. Encoded according to its ordinal.
    public enum TupleCode { TUPLE_START, TUPLE_END } ;

    // Value for PART. Encoded according to its ordinal.
    public enum PartCode { NO_CUSTOM, HAS_CUSTOM } ;

    // Value for MSG. Encoded according to its ordinal.
    public enum MsgCode { MSG_START, MSG_END } ;

    // Value for LABEL_MSG. Encoded according to its ordinal.
    public enum LabelMsg { REQUEST, REPLY_GOOD, REPLY_ERROR } ;

    EmergeKind getKind() ;
   
    /** Return encoded value if any.
     * @throws IllegalStateException if no encoded value.
     */
    <T> T getValue( Class<T> cls ) ;

    /** Return whether this code contains an encoded value.
     */
    boolean hasEncodedValue() ;

    /** Return whether this code is valid or not
     */
    boolean isValidEmergeCode() ;

    /** Return whether or not this code represents a primitive value.
     * Primitives are: BYTE, CHAR, SHORT, INT, LONG, FLOAT, DOUBLE, BOOL, NULL, INDIR.
     */
    boolean isPrimitive() ;

    /** Return whether or not this code represents an array.
     * Arrays are: BOOL_ARR, BYTE_ARR, CHAR_ARR, SHORT_ARR, INT_ARR, LONG_ARR, 
     * FLOAT_ARR, DOUBLE_ARR, REF_ARR.
     */
    boolean isArray() ;

    /** Return whether or not this code represent a simple message (one that does not need
     * other emerge codes).  Simple messages are FLIST, CLOSE_SESSION, REJECT_REQUEST, 
     * LABEL_MSG-request, LABEL_MSG-reply_error.
     */
    boolean isSimpleMessage() ;
}
