package com.sun.corba.ee.impl.encoding;

import org.omg.CORBA.portable.IDLEntity;

public class IDLValue implements IDLEntity {
    static final String REPID = "RMI:com.sun.corba.ee.impl.encoding.IDLValue:BB212B05444A560F:000000000ABCDEF0";
    static final long serialVersionUID = 0xABCDEF0;

    byte aByte;
    int anInt;

    public IDLValue() {
    }

    public IDLValue(byte aByte) {
        this.aByte = aByte;
        this.anInt = 0x10 * aByte;
    }
}
