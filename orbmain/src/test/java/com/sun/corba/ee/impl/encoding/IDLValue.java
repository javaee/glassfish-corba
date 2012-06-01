package com.sun.corba.ee.impl.encoding;

import org.omg.CORBA.portable.IDLEntity;

public class IDLValue implements IDLEntity {
    static final String REPID = "RMI:com.sun.corba.ee.impl.encoding.IDLValue:3E1F37A79F0D0984:F72C4A0542764A7B";
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
