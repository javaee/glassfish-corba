package com.sun.corba.ee.impl.ior;

import com.sun.corba.ee.impl.encoding.EncodingTestBase;
import com.sun.corba.ee.spi.ior.IOR;
import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class IORImplTest extends EncodingTestBase {

    @Test
    public void stringifyIncludesTypeId() {
        IOR ior = new IORImpl(getOrb(), "TestType");
        assertEquals("IOR:" + "00000000" + "00000009" + "54657374" + "54797065" +"0000000000000000", ior.stringify());
    }
}
