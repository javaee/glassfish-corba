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

import com.sun.corba.ee.spi.orb.ORB;
import org.junit.Before;
import org.junit.Test;
import org.omg.CORBA.Any;
import org.omg.CORBA.BAD_OPERATION;
import org.omg.CORBA.TCKind;

import java.io.Serializable;

import static com.meterware.simplestub.Stub.createStrictStub;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.*;

public class AnyImplTest {

    private ORBFake orb = createStrictStub(ORBFake.class);
    private Any any;

    @Before
    public void setUp() throws Exception {
        any = new AnyImpl(orb);
    }

    @Test
    public void whenAnyCreated_typeIsNull() {
        assertEquals(TCKind.tk_null, any.type().kind());
    }

    @Test(expected = BAD_OPERATION.class)
    public void whenReadingUninitializedAny_throwException() {
        any.extract_octet();
    }

    @Test(expected = BAD_OPERATION.class)
    public void whenTryingToReadWrongType_throwException() {
        any.insert_octet((byte) 3);
        assertThat(any.extract_double(), equalTo(3.0));
    }

    @Test
    public void whenOctetInserted_canReadBackValue() {
        any.insert_octet((byte) 3);
        assertEquals(TCKind.tk_octet, any.type().kind());
        assertEquals(3, any.extract_octet());
    }

    @Test
    public void whenShortInserted_canReadBackValue() {
        any.insert_short((short) -15);
        assertEquals(TCKind.tk_short, any.type().kind());
        assertEquals(-15, any.extract_short());
    }

    @Test
    public void whenUnsignedShortInserted_canReadBackValue() {
        any.insert_ushort((short) 127);
        assertEquals(TCKind.tk_ushort, any.type().kind());
        assertEquals(127, any.extract_ushort());
    }

    @Test
    public void whenLongInserted_canReadBackValue() {
        any.insert_long(17);
        assertEquals(TCKind.tk_long, any.type().kind());
        assertEquals(17, any.extract_long());
    }

    @Test
    public void whenUnsignedLongInserted_canReadBackValue() {
        any.insert_ulong(170);
        assertEquals(TCKind.tk_ulong, any.type().kind());
        assertEquals(170, any.extract_ulong());
    }

    @Test
    public void whenLongLongInserted_canReadBackValue() {
        any.insert_longlong(Integer.MAX_VALUE);
        assertEquals(TCKind.tk_longlong, any.type().kind());
        assertEquals(Integer.MAX_VALUE, any.extract_longlong());
    }

    @Test
    public void whenUnsignedLongLongInserted_canReadBackValue() {
        any.insert_ulonglong(Integer.MAX_VALUE);
        assertEquals(TCKind.tk_ulonglong, any.type().kind());
        assertEquals(Integer.MAX_VALUE, any.extract_ulonglong());
    }

    @Test
    public void whenBooleanTrueInserted_canReadBackValue() {
        any.insert_boolean(true);
        assertEquals(TCKind.tk_boolean, any.type().kind());
        assertTrue(any.extract_boolean());
    }

    @Test
    public void whenBooleanFalseInserted_canReadBackValue() {
        any.insert_boolean(false);
        assertFalse(any.extract_boolean());
    }

    @Test
    public void whenFloatInserted_canReadBackValue() {
        any.insert_float((float) 21.3);
        assertEquals(TCKind.tk_float, any.type().kind());
        assertEquals(21.3, any.extract_float(), 0.01);
    }

    @Test
    public void whenDoubleInserted_canReadBackValue() {
        any.insert_double(-12.56);
        assertEquals(TCKind.tk_double, any.type().kind());
        assertEquals(-12.56, any.extract_double(), 0.01);
    }

    @Test
    public void whenCharInserted_canReadBackValue() {
        any.insert_char('x');
        assertEquals(TCKind.tk_char, any.type().kind());
        assertEquals('x', any.extract_char());
    }

    @Test
    public void whenWideCharInserted_canReadBackValue() {
        any.insert_wchar('\u0123');
        assertEquals(TCKind.tk_wchar, any.type().kind());
        assertEquals('\u0123', any.extract_wchar());
    }

    @Test
    public void whenStringInserted_canReadBackValue() {
        any.insert_string("This is a test");
        assertEquals(TCKind.tk_string, any.type().kind());
        assertEquals("This is a test", any.extract_string());
    }

    @Test
    public void whenStringInsertedAsValue_canReadBackValue() throws Exception {
        any.insert_Value("This is another test");

        assertThat(any.type().kind(), is(TCKind.tk_value_box));
        assertThat(any.extract_Value(), equalTo((Serializable) "This is another test"));
    }

    abstract static class ORBFake extends ORB {
        protected ORBFake() {
            initializePrimitiveTypeCodeConstants();
        }
    }
}
