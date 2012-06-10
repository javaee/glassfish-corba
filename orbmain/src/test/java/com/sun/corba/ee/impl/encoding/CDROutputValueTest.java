package com.sun.corba.ee.impl.encoding;
/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright (c) 2012 Oracle and/or its affiliates. All rights reserved.
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

import com.sun.corba.ee.impl.util.RepositoryId;
import com.sun.corba.ee.spi.orb.ORBVersionFactory;
import org.glassfish.simplestub.Stub;
import org.junit.Test;
import org.omg.CORBA.MARSHAL;
import org.omg.CORBA.VM_TRUNCATABLE;
import org.omg.CORBA.portable.IndirectionException;

import java.io.*;

import static org.junit.Assert.*;

public class CDROutputValueTest extends ValueTestBase {

    Value1Helper value1Helper = Stub.create(Value1Helper.class);

    @Test
    public void canWriteStringValue() throws IOException {
        writeValueTag(ONE_REPID_ID);
        writeRepId(RepositoryId.kWStringValueRepID);
        writeStringValue_1_2("This, too!");

        getOutputObject().write_value("This, too!");
        expectByteArray(getGeneratedBody());
    }

    @Test
    public void canWriteSerializedValue() throws IOException {
        writeValueTag(ONE_REPID_ID);
        writeRepId(Value1.REPID);

        writeWchar_1_2('x');
        writeInt(3);

        Value1 value1 = new Value1('x', 3);
        getOutputObject().write_value(value1);

        setMessageBody(getGeneratedBody());
        expectByteArray(getGeneratedBody());
    }

    @Test
    public void canWriteSerializedValueInChunk() throws IOException {
        writeValueTag(ONE_REPID_ID | USE_CHUNKING);
        writeRepId(Value1.REPID);

        startChunk();
        writeWchar_1_2('x');
        writeInt(3);
        endChunk();
        writeEndTag(-1);

        value1Helper.setModifier(VM_TRUNCATABLE.value);
        useRepId();
        getOutputObject().write_value(new Value1('x', 3), value1Helper);
        expectByteArray(getGeneratedBody());
    }

    @Test
    public void canWriteSerializedEnum() throws IOException {
        writeValueTag(ONE_REPID_ID);
        writeRepId(Enum1.REPID);

        writeString(Enum1.strange.toString());

        getOutputObject().write_value(Enum1.strange);
        expectByteArray(getGeneratedBody());
    }

    @Test
    public void canWriteIDLEntity() throws IOException {
        writeValueTag(ONE_REPID_ID | USE_CHUNKING);
        writeRepId(IDLValue.REPID);

        byte aByte = 0x45;

        startChunk();
        writeByte(aByte);
        endChunk();
        writeEndTag(-1);

        IDLValue value = new IDLValue(aByte);
        getOutputObject().write_value(value);
        expectByteArray(getGeneratedBody());
    }

    @Test
    public void canWriteSerializedValueWithIndirection() throws IOException {
        int location = getCurrentLocation();
        writeValueTag(ONE_REPID_ID);
        writeRepId(Value1.REPID);

        writeWchar_1_2('x');
        writeInt(3);
        writeIndirectionTo(location);

        Value1 value = new Value1('x', 3);
        getOutputObject().write_value(value);
        getOutputObject().write_value(value);
        expectByteArray(getGeneratedBody());
    }

    @Test
    public void canWriteSerializedValueWithIndirection_in1_1() throws IOException {
        useV1_1();
        int location = getCurrentLocation();
        writeValueTag(ONE_REPID_ID);
        writeRepId(Value1.REPID);

        writeWchar_1_1('x');
        writeInt(3);
        writeIndirectionTo(location);

        Value1 value = new Value1('x', 3);
        getOutputObject().write_value(value);
        getOutputObject().write_value(value);
        expectByteArray(getGeneratedBody());
    }

    @Test
    public void canWriteSerializedValueWithIndirection_in1_0() throws IOException {
        useV1_0();
        setOrbVersion(ORBVersionFactory.getOLD());
        int location = getCurrentLocation();
        writeValueTag(ONE_REPID_ID);
        writeRepId(Value1.REPID);

        writeWchar_1_1('x');
        writeInt(3);
        writeIndirectionTo(location);

        Value1 value = new Value1('x', 3);
        getOutputObject().write_value(value);
        getOutputObject().write_value(value);
        expectByteArray(getGeneratedBody());
    }

    @Test
    public void whenBufferFull_sendFragment() {
        setFragmentSize(20);
        getOutputObject().write_long(1);
        getOutputObject().write_long(2);
        getOutputObject().write_long(3);
    }

/*

// write codebase

    @Test
    public void canReadSerializedValueWithContinuationChunk() throws IOException {
        writeValueTag(ONE_REPID_ID | USE_CHUNKING);
        writeRepId(Value1.REPID);

        startChunk();
        writeWchar_1_2('x');
        endChunk();

        startChunk();
        writeInt(3);
        endChunk();
        writeEndTag(-1);

        setMessageBody( getGeneratedBody() );

        Object object = getInputObject().read_value();
        assertTrue(object instanceof Value1);
        Value1 value1 = (Value1) object;
        assertEquals('x', value1.aChar);
        assertEquals(3, value1.anInt);
    }

    @Test
    public void canReadSerializedValueWithNestedValue() throws IOException {
        writeValueTag(ONE_REPID_ID | USE_CHUNKING);
        writeRepId(Value2.REPID);

        startChunk();
        writeLong(750);
        endChunk();

        writeValueTag(ONE_REPID_ID | USE_CHUNKING);
        writeRepId(Value1.REPID);
        startChunk();
        writeWchar_1_2('x');
        writeInt(3);
        endChunk();
        writeEndTag(-1);

        setMessageBody( getGeneratedBody() );

        Object object = getInputObject().read_value();
        assertTrue(object instanceof Value2);
        Value2 value2 = (Value2) object;
        assertEquals(750,value2.aLong);
        assertEquals('x', value2.aValue.aChar);
        assertEquals(3, value2.aValue.anInt);
    }

    @Test
    public void canReadSerializedValueUsingDefaultFactory() throws IOException {
        writeValueTag(ONE_REPID_ID | USE_CODEBASE);
        writeCodebase("http://localhost/myClasses");
        writeRepId(Value1.REPID);

        writeWchar_1_2('x');

        setMessageBody( getGeneratedBody() );

        Object object = getInputObject().read_value(Value1.REPID);
        assertTrue(object instanceof Value1);
        Value1 value1 = (Value1) object;
        assertEquals('x', value1.aChar);
        assertEquals('x', value1.anInt);
    }

    @Test
    public void canReadNullValueUsingDefaultFactory() throws IOException {
        writeNull();
        setMessageBody( getGeneratedBody() );

        assertNull(getInputObject().read_value(Value1.REPID));
    }

    @Test(expected = IndirectionException.class)
    public void whenIndirectionHasNoAntecedent_throwExceptionWhenUsingRepId() throws IOException {
        writeIndirectionTo(0);
        setMessageBody( getGeneratedBody() );
        getInputObject().read_value(Value1.REPID);
    }

    @Test
    public void canReadSerializedValueUsingDefaultFactoryAndIndirection() throws IOException {
        int location = getCurrentLocation();

        writeValueTag(ONE_REPID_ID | USE_CHUNKING);
        writeRepId(Value1.REPID);
        startChunk();
        writeWchar_1_2('x');
        endChunk();
        writeEndTag(-1);

        writeIndirectionTo(location);

        setMessageBody( getGeneratedBody() );

        Object object1 = getInputObject().read_value(Value1.REPID);
        Object object2 = getInputObject().read_value(Value1.REPID);
        assertSame(object1, object2);
    }

*/
}
