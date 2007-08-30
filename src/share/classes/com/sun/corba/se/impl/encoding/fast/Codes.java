/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2007 Sun Microsystems, Inc. All rights reserved.
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
package com.sun.corba.se.impl.encoding.fast ;

/** Codes used for fast marshaling. Code defines (usually) a type,
 * which may be followed by a value.  LABEL denotes a positive int,
 * while LEN&lt;TYPE&gt; denotes a non-negative int followed by that
 * many values of the TYPE.
 * <p>
 * This is a flat marshalling scheme.  By that I mean that each object
 * is fully marshalled without ever starting to marshal nested objects.
 * This is an experiment to see what works more quickly.
 */
public enum Codes {
    // All values are represented by a type followed by the corresponding value
    // LEN<TYPE> means a 4 byte non-negative integer LEN followed by LEN values
    // of TYPE
    // LABEL is a 4 byte positive integer
    // FIELDS is a list of type-value pairs for the fields in an object.  Ordering is
    // up to the code generator, which generates read and write together, but must include
    // the superclass fields (up to the first non-serializable superclass) as well.
    // INDIR can be used anywhere a ARRAY-value, or REF-value can be used.
    // 
    // Assumptions and limitations:
    // - No readResolve, writeReplace, writeObject, readObject, custom serialization, etc.
    // - No class evolution.
    //
    // code        type encoding			    value encoding (preceded by type)
    // --------------------------------------------------------------------------
    NULL,	// NULL					    no value
    BOOL,	// BOOL					    --- 
    BOOL_FALSE, // ---					    false value	
    BOOL_TRUE,	// ---					    true value
    BYTE,	// BYTE					    1 byte signed
    CHAR,	// CHAR					    2 bytes unsigned
    SHORT,	// SHORT				    2 bytes signed
    INT,	// INT					    4 bytes signed
    LONG,	// LONG					    8 bytes signed
    FLOAT,	// FLOAT				    IEEE 32 bit float
    DOUBLE,	// DOUBLE				    IEEE 64 bit double
    BOOL_ARR,	// BOOL_ARR				    LABEL LEN<BOOL-value> 
    BYTE_ARR,	// BYTE_ARR				    LABEL LEN<BYTE-value>
    CHAR_ARR,	// CHAR_ARR				    LABEL LEN<CHAR-value>
    SHORT_ARR,	// SHORT_ARR				    LABEL LEN<SHORT-value>
    INT_ARR,	// INT_ARR				    LABEL LEN<INT-value>
    LONG_ARR,	// LONG_ARR				    LABEL LEN<LONG-value>
    FLOAT_ARR,	// FLOAT_ARR				    LABEL LEN<FLOAT-value>
    DOUBLE_ARR,	// DOUBLE_ARR				    LABEL LEN<DOUBLE-value>

    // The first LABEL must be for a char[] of the className of the reference.
    // The second LABEL is the LABEL of this value instance.
    // First the primitive fields are marshalled, then all of the non-primitives
    // as labels.  Both forward and backward references are allowed, but unmarshalling
    // is more efficient if forward references are minimized.
    REF,	// REF LABEL				    LABEL prim_fields LEN<LABEL> END

    // First LABEL is char[] for the component type of the array
    // Second LABEL is the LABEL of this array instance
    // List of LABELS are the LABELS of each element of the array
    REF_ARR,	// REF_ARR LABEL			    LABEL LEN<LABEL>

    END	// Indicates end of fields in a reference.  ---
}
