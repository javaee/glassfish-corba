/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright (c) 1997-2011 Oracle and/or its affiliates. All rights reserved.
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
package com.sun.corba.ee.impl.encoding.fast ;

import com.sun.corba.ee.impl.encoding.fast.bytebuffer.Reader ;
import com.sun.corba.ee.impl.encoding.fast.bytebuffer.Writer ;

/** Utility methods for reading and writing VarOctets.
 */
public class VarOctetUtility {
    private VarOctetUtility() {}

    public static long get( Reader reader ) {
        long result = 0 ;
        byte next ;
        while ((next = reader.getByte()) >= 0) {
            result = (result << EmergeCodeFactory.NUM_BITS_PER_VAR_OCTET)
                + next ;
        } 

        return (result << EmergeCodeFactory.NUM_BITS_PER_VAR_OCTET)
            + (next + 128) ;
    }

    /* extending this to signed longs:
     * in 7 bits:
     * high bit 0: positive 1-63
     * high bit 1: negative 1-63
     * 0000000: represents 0
     * 1111111: represents Long.MIN_VALUE
     *
     * Not quite right.
     */

    /** Write the long value in var-octet format (7 bits per octet, last octet negated).
     * @throw IllegalArgumentException if data &lt; 0.
     * @throw BufferOverflowException if there is not enough room in the buffer.
     */
    public static void put( final Writer writer, final long data ) {
        // writer.ensure( EmergeCodeFactory.varOctetSize( data ) ) ;
        try {
            // writer.setChecking( false ) ;
            long next = data >> EmergeCodeFactory.NUM_BITS_PER_VAR_OCTET ;
            long current = data & EmergeCodeFactory.VAR_OCTET_MASK ;

            if (next != 0) {
                putPositiveVarOctet(writer, next);
            }

            writer.putByte( (byte)(current - 128) ) ;
        } finally {
            // writer.setChecking( true ) ;
        }
    }

    /* Non-recursive version is about the same speed as the recursive,
     * so we'll use the recursive version to avoid generating short-lived objects.
    private static void putPositiveVarOctetNR( final long data ) {
        final byte[] temp = new byte[ EmergeCodeFactory.MAX_OCTETS_FOR_VAR_OCTET ] ;
        int index = 0 ;
        long current = data ;
        while (current > 0) {
            temp[ index++ ] = (byte)(current & EmergeCodeFactory.VAR_OCTET_MASK) ;
            current >>= EmergeCodeFactory.NUM_BITS_PER_VAR_OCTET ;
        }
        // Index is the number of elements in temp; now write them from index-1 to 0
        for (int ctr=index-1; ctr >= 0; ctr-- )
            buffer.buffer().put( temp[ctr] ) ;
    }
    */

    private static void putPositiveVarOctet( final Writer writer, final long data ) {
        long next = data >> EmergeCodeFactory.NUM_BITS_PER_VAR_OCTET ;
        long current = data & EmergeCodeFactory.VAR_OCTET_MASK ;
        if (next != 0) {
            putPositiveVarOctet( writer, next ) ;
        }

        writer.putByte( (byte)current ) ;
    }
}
