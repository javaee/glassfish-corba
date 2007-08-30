/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2002-2007 Sun Microsystems, Inc. All rights reserved.
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

package org.omg.CORBA;

/** Defines the methods used to read primitive data types from input streams
* for unmarshaling custom value types.  This interface is used by user
* written custom unmarshaling code for custom value types.
* @see org.omg.CORBA.DataOutputStream
* @see org.omg.CORBA.CustomMarshal
* @version 1.13 07/27/07
*/
public interface DataInputStream extends org.omg.CORBA.portable.ValueBase
{
    /** Reads an IDL <code>Any</code> value from the input stream.
    * @return  the <code>Any</code> read.
    * @throws <code>org.omg.CORBA.MARSHAL</code>
    * If an inconsistency is detected, including not having registered 
    * a streaming policy, then the standard system exception MARSHAL is raised.
    */
    org.omg.CORBA.Any read_any ();

    /** Reads an IDL boolean value from the input stream.
    * @return  the boolean read.
    * @throws <code>org.omg.CORBA.MARSHAL</code>
    * If an inconsistency is detected, including not having registered 
    * a streaming policy, then the standard system exception MARSHAL is raised.
    */
    boolean read_boolean ();

    /** Reads an IDL character value from the input stream.
    * @return  the character read.
    * @throws <code>org.omg.CORBA.MARSHAL</code>
    * If an inconsistency is detected, including not having registered 
    * a streaming policy, then the standard system exception MARSHAL is raised.
    */
    char read_char ();

    /** Reads an IDL wide character value from the input stream.
    * @return  the wide character read.
    * @throws <code>org.omg.CORBA.MARSHAL</code>
    * If an inconsistency is detected, including not having registered 
    * a streaming policy, then the standard system exception MARSHAL is raised.
    */
    char read_wchar ();

    /** Reads an IDL octet value from the input stream.
    * @return  the octet value read.
    * @throws <code>org.omg.CORBA.MARSHAL</code>
    * If an inconsistency is detected, including not having registered 
    * a streaming policy, then the standard system exception MARSHAL is raised.
    */
    byte read_octet ();

    /** Reads an IDL short from the input stream.
    * @return  the short read.
    * @throws <code>org.omg.CORBA.MARSHAL</code>
    * If an inconsistency is detected, including not having registered 
    * a streaming policy, then the standard system exception MARSHAL is raised.
    */
    short read_short ();

    /** Reads an IDL unsigned short from the input stream.
    * @return  the unsigned short read.
    * @throws <code>org.omg.CORBA.MARSHAL</code>
    * If an inconsistency is detected, including not having registered 
    * a streaming policy, then the standard system exception MARSHAL is raised.
    */
    short read_ushort ();

    /** Reads an IDL long from the input stream.
    * @return  the long read.
    * @throws <code>org.omg.CORBA.MARSHAL</code>
    * If an inconsistency is detected, including not having registered 
    * a streaming policy, then the standard system exception MARSHAL is raised.
    */
    int read_long ();

    /** Reads an IDL unsigned long from the input stream.
    * @return  the unsigned long read.
    * @throws <code>org.omg.CORBA.MARSHAL</code>
    * If an inconsistency is detected, including not having registered 
    * a streaming policy, then the standard system exception MARSHAL is raised.
    */
    int read_ulong ();

    /** Reads an IDL long long from the input stream.
    * @return  the long long read.
    * @throws <code>org.omg.CORBA.MARSHAL</code>
    * If an inconsistency is detected, including not having registered 
    * a streaming policy, then the standard system exception MARSHAL is raised.
    */
    long read_longlong ();

    /** Reads an unsigned IDL long long from the input stream.
    * @return  the unsigned long long read.
    * @throws <code>org.omg.CORBA.MARSHAL</code>
    * If an inconsistency is detected, including not having registered 
    * a streaming policy, then the standard system exception MARSHAL is raised.
    */
    long read_ulonglong ();

    /** Reads an IDL float from the input stream.
    * @return  the float read.
    * @throws <code>org.omg.CORBA.MARSHAL</code>
    * If an inconsistency is detected, including not having registered 
    * a streaming policy, then the standard system exception MARSHAL is raised.
    */
    float read_float ();

    /** Reads an IDL double from the input stream.
    * @return  the double read.
    * @throws <code>org.omg.CORBA.MARSHAL</code>
    * If an inconsistency is detected, including not having registered 
    * a streaming policy, then the standard system exception MARSHAL is raised.
    */
    double read_double ();
    // read_longdouble not supported by IDL/Java mapping

    /** Reads an IDL string from the input stream.
    * @return  the string read.
    * @throws <code>org.omg.CORBA.MARSHAL</code>
    * If an inconsistency is detected, including not having registered 
    * a streaming policy, then the standard system exception MARSHAL is raised.
    */
    String read_string ();

    /** Reads an IDL wide string from the input stream.
    * @return  the wide string read.
    * @throws <code>org.omg.CORBA.MARSHAL</code>
    * If an inconsistency is detected, including not having registered 
    * a streaming policy, then the standard system exception MARSHAL is raised.
    */
    String read_wstring ();

    /** Reads an IDL CORBA::Object from the input stream.
    * @return  the CORBA::Object read.
    * @throws <code>org.omg.CORBA.MARSHAL</code>
    * If an inconsistency is detected, including not having registered 
    * a streaming policy, then the standard system exception MARSHAL is raised.
    */
    org.omg.CORBA.Object read_Object ();

    /** Reads an IDL Abstract interface from the input stream.
    * @return  the Abstract interface read.
    * @throws <code>org.omg.CORBA.MARSHAL</code>
    * If an inconsistency is detected, including not having registered 
    * a streaming policy, then the standard system exception MARSHAL is raised.
    */
    java.lang.Object read_Abstract ();

    /** Reads an IDL value type from the input stream.
    * @return  the value type read.
    * @throws <code>org.omg.CORBA.MARSHAL</code>
    * If an inconsistency is detected, including not having registered 
    * a streaming policy, then the standard system exception MARSHAL is raised.
    */
    java.io.Serializable read_Value ();

    /** Reads an IDL typecode from the input stream.
    * @return  the typecode read.
    * @throws <code>org.omg.CORBA.MARSHAL</code>
    * If an inconsistency is detected, including not having registered 
    * a streaming policy, then the standard system exception MARSHAL is raised.
    */
    org.omg.CORBA.TypeCode read_TypeCode ();

    /** Reads array of IDL Anys from offset for length elements from the
    * input stream.  
    * @param seq The out parameter holder for the array to be read.
    * @param offset The index into seq of the first element to read from the
    * input stream.
    * @param length The number of elements to read from the input stream.
    * @throws <code>org.omg.CORBA.MARSHAL</code>
    * If an inconsistency is detected, including not having registered 
    * a streaming policy, then the standard system exception MARSHAL is raised.
    */
    void read_any_array (org.omg.CORBA.AnySeqHolder seq, int offset, int length);

    /** Reads array of IDL booleans from offset for length elements from the
    * input stream.  
    * @param seq The out parameter holder for the array to be read.
    * @param offset The index into seq of the first element to read from the
    * input stream.
    * @param length The number of elements to read from the input stream.
    * @throws <code>org.omg.CORBA.MARSHAL</code>
    * If an inconsistency is detected, including not having registered 
    * a streaming policy, then the standard system exception MARSHAL is raised.
    */
    void read_boolean_array (org.omg.CORBA.BooleanSeqHolder seq, int offset, int length);

    /** Reads array of IDL characters from offset for length elements from the
    * input stream.  
    * @param seq The out parameter holder for the array to be read.
    * @param offset The index into seq of the first element to read from the
    * input stream.
    * @param length The number of elements to read from the input stream.
    * @throws <code>org.omg.CORBA.MARSHAL</code>
    * If an inconsistency is detected, including not having registered 
    * a streaming policy, then the standard system exception MARSHAL is raised.
    */
    void read_char_array (org.omg.CORBA.CharSeqHolder seq, int offset, int length);

    /** Reads array of IDL wide characters from offset for length elements from the
    * input stream.  
    * @param seq The out parameter holder for the array to be read.
    * @param offset The index into seq of the first element to read from the
    * input stream.
    * @param length The number of elements to read from the input stream.
    * @throws <code>org.omg.CORBA.MARSHAL</code>
    * If an inconsistency is detected, including not having registered 
    * a streaming policy, then the standard system exception MARSHAL is raised.
    */
    void read_wchar_array (org.omg.CORBA.WCharSeqHolder seq, int offset, int length);

    /** Reads array of IDL octets from offset for length elements from the
    * input stream.  
    * @param seq The out parameter holder for the array to be read.
    * @param offset The index into seq of the first element to read from the
    * input stream.
    * @param length The number of elements to read from the input stream.
    * @throws <code>org.omg.CORBA.MARSHAL</code>
    * If an inconsistency is detected, including not having registered 
    * a streaming policy, then the standard system exception MARSHAL is raised.
    */
    void read_octet_array (org.omg.CORBA.OctetSeqHolder seq, int offset, int length);

    /** Reads array of IDL shorts from offset for length elements from the
    * input stream.  
    * @param seq The out parameter holder for the array to be read.
    * @param offset The index into seq of the first element to read from the
    * input stream.
    * @param length The number of elements to read from the input stream.
    * @throws <code>org.omg.CORBA.MARSHAL</code>
    * If an inconsistency is detected, including not having registered 
    * a streaming policy, then the standard system exception MARSHAL is raised.
    */
    void read_short_array (org.omg.CORBA.ShortSeqHolder seq, int offset, int length);

    /** Reads array of IDL unsigned shorts from offset for length elements from the
    * input stream.  
    * @param seq The out parameter holder for the array to be read.
    * @param offset The index into seq of the first element to read from the
    * input stream.
    * @param length The number of elements to read from the input stream.
    * @throws <code>org.omg.CORBA.MARSHAL</code>
    * If an inconsistency is detected, including not having registered 
    * a streaming policy, then the standard system exception MARSHAL is raised.
    */
    void read_ushort_array (org.omg.CORBA.UShortSeqHolder seq, int offset, int length);

    /** Reads array of IDL longs from offset for length elements from the
    * input stream.  
    * @param seq The out parameter holder for the array to be read.
    * @param offset The index into seq of the first element to read from the
    * input stream.
    * @param length The number of elements to read from the input stream.
    * @throws <code>org.omg.CORBA.MARSHAL</code>
    * If an inconsistency is detected, including not having registered 
    * a streaming policy, then the standard system exception MARSHAL is raised.
    */
    void read_long_array (org.omg.CORBA.LongSeqHolder seq, int offset, int length);

    /** Reads array of IDL unsigned longs from offset for length elements from the
    * input stream.  
    * @param seq The out parameter holder for the array to be read.
    * @param offset The index into seq of the first element to read from the
    * input stream.
    * @param length The number of elements to read from the input stream.
    * @throws <code>org.omg.CORBA.MARSHAL</code>
    * If an inconsistency is detected, including not having registered 
    * a streaming policy, then the standard system exception MARSHAL is raised.
    */
    void read_ulong_array (org.omg.CORBA.ULongSeqHolder seq, int offset, int length);

    /** Reads array of IDL unsigned long longs from offset for length elements from the
    * input stream.  
    * @param seq The out parameter holder for the array to be read.
    * @param offset The index into seq of the first element to read from the
    * input stream.
    * @param length The number of elements to read from the input stream.
    * @throws <code>org.omg.CORBA.MARSHAL</code>
    * If an inconsistency is detected, including not having registered 
    * a streaming policy, then the standard system exception MARSHAL is raised.
    */
    void read_ulonglong_array (org.omg.CORBA.ULongLongSeqHolder seq, int offset, int length);

    /** Reads array of IDL long longs from offset for length elements from the
    * input stream.  
    * @param seq The out parameter holder for the array to be read.
    * @param offset The index into seq of the first element to read from the
    * input stream.
    * @param length The number of elements to read from the input stream.
    * @throws <code>org.omg.CORBA.MARSHAL</code>
    * If an inconsistency is detected, including not having registered 
    * a streaming policy, then the standard system exception MARSHAL is raised.
    */
    void read_longlong_array (org.omg.CORBA.LongLongSeqHolder seq, int offset, int length);

    /** Reads array of IDL floats from offset for length elements from the
    * input stream.  
    * @param seq The out parameter holder for the array to be read.
    * @param offset The index into seq of the first element to read from the
    * input stream.
    * @param length The number of elements to read from the input stream.
    * @throws <code>org.omg.CORBA.MARSHAL</code>
    * If an inconsistency is detected, including not having registered 
    * a streaming policy, then the standard system exception MARSHAL is raised.
    */
    void read_float_array (org.omg.CORBA.FloatSeqHolder seq, int offset, int length);

    /** Reads array of IDL doubles from offset for length elements from the
    * input stream.  
    * @param seq The out parameter holder for the array to be read.
    * @param offset The index into seq of the first element to read from the
    * input stream.
    * @param length The number of elements to read from the input stream.
    * @throws <code>org.omg.CORBA.MARSHAL</code>
    * If an inconsistency is detected, including not having registered 
    * a streaming policy, then the standard system exception MARSHAL is raised.
    */
    void read_double_array (org.omg.CORBA.DoubleSeqHolder seq, int offset, int length);
} // interface DataInputStream

