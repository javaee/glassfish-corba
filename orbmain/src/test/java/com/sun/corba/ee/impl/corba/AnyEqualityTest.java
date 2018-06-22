/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright (c) 2012-2018 Oracle and/or its affiliates. All rights reserved.
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

package com.sun.corba.ee.impl.corba;

import com.meterware.simplestub.Memento;
import com.meterware.simplestub.SystemPropertySupport;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.omg.CORBA.Any;
import org.omg.CORBA.ORB;
import org.omg.CORBA.TypeCode;

import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.assertTrue;

public class AnyEqualityTest {
    List<Memento> mementos = new ArrayList<>();

    private Any any;
    private Any any1;
    private Any any2;

    @Before
    public void setUp() throws Exception {
        mementos.add(SystemPropertySupport.install("org.omg.CORBA.ORBClass", "com.sun.corba.ee.impl.orb.ORBImpl"));
        ORB orb = ORB.init(new String[0], null);
        any = orb.create_any();
        any1 = orb.create_any();
        any2 = orb.create_any();
     }

    @After
    public void tearDown() throws Exception {
        for (Memento memento : mementos) memento.revert();
    }

    @Test
    public void nullAnys_areEqual() throws Exception {
        assertTrue(any1.equal(any2));
    }

    @Test
    public void twoAnysContainingTheSameShort_areEqual() throws Exception {
        short shortData = Short.MAX_VALUE;
        any1.insert_short(shortData);
        any2.insert_short(shortData);

        assertTrue(any1.equal(any2));
    }

    @Test
    public void twoAnysContainingTheSameUnsignedShort_areEqual() throws Exception {
        short uShortData = -1;
        any1.insert_ushort(uShortData);
        any2.insert_ushort(uShortData);

        assertTrue(any1.equal(any2));
    }

    @Test
    public void twoAnysContainingTheSameLong_areEqual() throws Exception {
        int longData = Integer.MAX_VALUE;
        any1.insert_long(longData);
        any2.insert_long(longData);

        assertTrue(any1.equal(any2));
    }

    @Test
    public void twoAnysContainingTheSameUnsignedLong_areEqual() throws Exception {
        int ulongData = -1;
        any1.insert_ulong(ulongData);
        any2.insert_ulong(ulongData);

        assertTrue(any1.equal(any2));
    }

    @Test
    public void twoAnysContainingTheSameLongLong_areEqual() throws Exception {
        long longlongData = Long.MAX_VALUE;
        any1.insert_longlong(longlongData);
        any2.insert_longlong(longlongData);

        assertTrue(any1.equal(any2));
    }

    @Test
    public void twoAnysContainingTheSameUnsignedLongLong_areEqual() throws Exception {
        long ulonglongData = -1L;
        any1.insert_ulonglong(ulonglongData);
        any2.insert_ulonglong(ulonglongData);

        assertTrue(any1.equal(any2));
    }

    @Test
    public void twoAnysContainingTheSameFloat_areEqual() throws Exception {
        float floatData = Float.MAX_VALUE;
        any1.insert_float(floatData);
        any2.insert_float(floatData);

        assertTrue(any1.equal(any2));
    }

    @Test
    public void twoAnysContainingTheSameDouble_areEqual() throws Exception {
        double doubleData = Double.MAX_VALUE;
        any1.insert_double(doubleData);
        any2.insert_double(doubleData);

        assertTrue(any1.equal(any2));
    }

    @Test
    public void twoAnysContainingTheSameChar_areEqual() throws Exception {
        char charData = Character.MAX_VALUE;
        any1.insert_char(charData);
        any2.insert_char(charData);

        assertTrue(any1.equal(any2));
    }

    @Test
    public void twoAnysContainingTheSameOctet_areEqual() throws Exception {
        byte octetData = Byte.MAX_VALUE;
        any1.insert_octet(octetData);
        any2.insert_octet(octetData);

        assertTrue(any1.equal(any2));
    }

    @Test
    public void twoAnysContainingTheSameAny_areEqual() throws Exception {
        byte octetData = Byte.MAX_VALUE;
        any.insert_octet(octetData);

        any1.insert_any(any);
        any2.insert_any(any);

        assertTrue(any1.equal(any2));
    }

    @Test
    public void twoAnysContainingTheSameTypecode_areEqual() throws Exception {
        byte octetData = Byte.MAX_VALUE;
        any.insert_octet(octetData);
        TypeCode typeCodeData = any.type();

        any1.insert_TypeCode(typeCodeData);
        any2.insert_TypeCode(typeCodeData);

        assertTrue(any1.equal(any2));
    }

    @Test
    public void twoAnysContainingTheSameString_areEqual() throws Exception {
        String stringData = "stringData";
        any1.insert_string(stringData);
        any2.insert_string(stringData);

        assertTrue(any1.equal(any2));
    }

    @Test
    public void twoAnysContainingTheSameEnum_areEqual() throws Exception {
        Enum1 enumData = Enum1.zeroth;
        Enum1Helper.insert(any1, enumData);
        Enum1Helper.insert(any2, enumData);

        assertTrue(any1.equal(any2));
    }
}
