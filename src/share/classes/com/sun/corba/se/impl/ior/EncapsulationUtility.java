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

package com.sun.corba.ee.impl.ior;

import java.util.List;

import org.omg.CORBA_2_3.portable.OutputStream ;
import org.omg.CORBA_2_3.portable.InputStream ;

import com.sun.corba.ee.spi.ior.Identifiable ;
import com.sun.corba.ee.spi.ior.IdentifiableFactoryFinder ;
import com.sun.corba.ee.spi.ior.WriteContents ;

import com.sun.corba.ee.spi.orb.ORB ;

import com.sun.corba.ee.impl.encoding.CDROutputObject ;
import com.sun.corba.ee.impl.encoding.EncapsOutputStream ;
import com.sun.corba.ee.impl.encoding.EncapsInputStream ;

/**
 * This static utility class contains various utility methods for reading and
 * writing CDR encapsulations.
 *
 * @author Ken Cavanaugh
 */
public final class EncapsulationUtility 
{
    private EncapsulationUtility()
    {
    }

    /** Read the count from is, then read count Identifiables from
     * is using the factory.  Add each constructed Identifiable to container.
     */
    public static <E extends Identifiable> void readIdentifiableSequence( 
        List<E> container,
        IdentifiableFactoryFinder<E> finder, InputStream istr) 
    {
        int count = istr.read_long() ;
        for (int ctr = 0; ctr<count; ctr++) {
            int id = istr.read_long() ;
            E obj = finder.create( id, istr ) ;
            container.add( obj ) ;
        }
    }

    /** Write all Identifiables that we contain to os.  The total
     * length must be written before this method is called.
     */
    public static <E extends Identifiable> void writeIdentifiableSequence( 
        List<E> container, OutputStream os) 
    {
        os.write_long( container.size() ) ;
        for (Identifiable obj : container) {
            os.write_long( obj.getId() ) ;
            obj.write( os ) ;
        }
    }

    /** Helper method that is used to extract data from an output
    * stream and write the data to another output stream.  Defined
    * as static so that it can be used in another class.
    */
    public static void writeOutputStream( OutputStream dataStream,
        OutputStream os ) 
    {
        byte[] data = ((CDROutputObject)dataStream).toByteArray() ;
        os.write_long( data.length ) ;
        os.write_octet_array( data, 0, data.length ) ;
    }

    /** Helper method to read the octet array from is, deencapsulate it, 
    * and return
    * as another InputStream.  This must be called inside the
    * constructor of a derived class to obtain the correct stream
    * for unmarshalling data.
    */
    public static InputStream getEncapsulationStream( ORB orb, InputStream is )
    {
        byte[] data = readOctets( is ) ;
        EncapsInputStream result = new EncapsInputStream( orb, data, 
            data.length ) ;
        result.consumeEndian() ;
        return result ;
    } 

    /** Helper method that reads an octet array from an input stream.
    * Defined as static here so that it can be used in another class.
    */
    public static byte[] readOctets( InputStream is ) 
    {
        int len = is.read_ulong() ;
        byte[] data = new byte[len] ;
        is.read_octet_array( data, 0, len ) ;
        return data ;
    }

    public static void writeEncapsulation( WriteContents obj,
        OutputStream os )
    {
        EncapsOutputStream out = new EncapsOutputStream( (ORB)os.orb() ) ;

        out.putEndian() ;

        obj.writeContents( out ) ;

        writeOutputStream( out, os ) ;
    }
}
