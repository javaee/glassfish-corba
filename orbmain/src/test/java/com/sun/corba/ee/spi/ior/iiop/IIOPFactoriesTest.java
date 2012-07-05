package com.sun.corba.ee.spi.ior.iiop;

import com.sun.corba.ee.spi.orb.ORBData;
import org.glassfish.simplestub.SimpleStub;
import org.glassfish.simplestub.Stub;
import org.junit.Test;
import com.sun.corba.ee.spi.orb.ORB;
import org.omg.IOP.TAG_ALTERNATE_IIOP_ADDRESS;

import static org.junit.Assert.assertEquals;

public class IIOPFactoriesTest {

    @Test
    public void canCreateAlternateIIOPAddressComponent() {
        IIOPAddress addr = IIOPFactories.makeIIOPAddress( "localhost", 2345 ) ;
        AlternateIIOPAddressComponent comp = IIOPFactories.makeAlternateIIOPAddressComponent( addr ) ;
        org.omg.IOP.TaggedComponent tcomp = comp.getIOPComponent( Stub.create(ORBFake.class) ) ;
        assertEquals( tcomp.tag, TAG_ALTERNATE_IIOP_ADDRESS.value );
    }

    @SimpleStub(strict=true)
    abstract static public class ORBDataFake implements ORBData {
        @Override
        public int getGIOPBufferSize() {
            return 100;
        }
    }

    @SimpleStub(strict=true)
    abstract static public class ORBFake extends ORB {

        private ORBData data = Stub.create(ORBDataFake.class);

        @Override
        public ORBData getORBData() {
            return data;
        }
    }

}
