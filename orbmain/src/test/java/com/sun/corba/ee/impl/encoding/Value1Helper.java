package com.sun.corba.ee.impl.encoding;

import com.sun.org.omg.CORBA.portable.ValueHelper;
import org.glassfish.simplestub.SimpleStub;
import org.glassfish.simplestub.Stub;
import org.omg.CORBA.TypeCode;
import org.omg.CORBA.TypeCodePackage.BadKind;
import org.omg.CORBA.portable.OutputStream;

import java.io.Serializable;

@SimpleStub(strict = true)
abstract class Value1Helper implements ValueHelper {
    Value1Type typeCode = Stub.create(Value1Type.class);

    void setModifier(short modifier) {
        typeCode.modifier = modifier;
    }

    @Override
    public TypeCode get_type() {
        return typeCode;
    }

    @Override
    public void write_value(OutputStream os, Serializable value) {
        Value1 value1 = (Value1) value;
        os.write_wchar(value1.aChar);
        os.write_long(value1.anInt);
    }

    @Override
    public String get_id() {
        return Value1.REPID;
    }
}


@SimpleStub(strict = true)
abstract class Value1Type extends TypeCode {
    short modifier;

    @Override
    public short type_modifier() throws BadKind {
        return modifier;
    }
}
