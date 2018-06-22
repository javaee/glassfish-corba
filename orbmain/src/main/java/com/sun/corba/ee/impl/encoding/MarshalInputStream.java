/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright (c) 1997-2018 Oracle and/or its affiliates. All rights reserved.
 *
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common Development
 * and Distribution License("CDDL") (collectively, the "License").  You
 * may not use this file except in compliance with the License.  You can
 * obtain a copy of the License at
 * https://oss.oracle.com/licenses/CDDL+GPL-1.1
 * or LICENSE.txt.  See the License for the specific
 * language governing permissions and limitations under the License.
 *
 * When distributing the software, include this License Header Notice in each
 * file and include the License file at LICENSE.txt.
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

package com.sun.corba.ee.impl.encoding;

import org.omg.CORBA.TypeCode;
import org.omg.CORBA.Any;

public interface MarshalInputStream {
    public boolean      read_boolean();
    public char         read_char();
    public char         read_wchar();
    public byte         read_octet();
    public short        read_short();
    public short        read_ushort();
    public int          read_long();
    public int          read_ulong();
    public long         read_longlong();
    public long         read_ulonglong();
    public float        read_float();
    public double       read_double();
    public String       read_string();
    public String       read_wstring();

    public void read_boolean_array(boolean[] value, int offset, int length);
    public void read_char_array(char[] value, int offset, int length);
    public void read_wchar_array(char[] value, int offset, int length);
    public void read_octet_array(byte[] value, int offset, int length);
    public void read_short_array(short[] value, int offset, int length);
    public void read_ushort_array(short[] value, int offset, int length);
    public void read_long_array(int[] value, int offset, int length);
    public void read_ulong_array(int[] value, int offset, int length);
    public void read_longlong_array(long[] value, int offset, int length);
    public void read_ulonglong_array(long[] value, int offset, int length);
    public void read_float_array(float[] value, int offset, int length);
    public void read_double_array(double[] value, int offset, int length);

    public org.omg.CORBA.Object read_Object();
    public TypeCode     read_TypeCode();
    public Any          read_any();
    @SuppressWarnings({"deprecation"})
    public org.omg.CORBA.Principal      read_Principal();

    /*
     * The methods necessary to support RMI
     */
    public org.omg.CORBA.Object read_Object(Class stubClass);
    public java.io.Serializable read_value() throws Exception;
    
    /* 
     * Additional Methods
     */
    public void consumeEndian();

    // Determines the current byte stream position
    // (also handles fragmented streams)
    public int getPosition();

    // mark/reset from java.io.InputStream
    public void mark(int readAheadLimit);
    public void reset();

    /**
     * This must be called once before unmarshaling valuetypes or anything
     * that uses repository IDs.  The ORB's version should be set
     * to the desired value prior to calling.
     */
    public void performORBVersionSpecificInit();

    /**
     * Tells the input stream to null any code set converter
     * references, forcing it to reacquire them if it needs
     * converters again.  This is used when the server
     * input stream needs to switch the connection's char code set
     * converter to something different after reading the
     * code set service context for the first time.  Initially,
     * we use ISO8859-1 to read the operation name (it can't
     * be more than ASCII).  
     */
    public void resetCodeSetConverters();
}
