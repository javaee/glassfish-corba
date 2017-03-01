package com.sun.corba.ee.impl.folb;

import com.sun.corba.ee.spi.ior.IOR;
import com.sun.corba.ee.spi.protocol.ClientDelegate;
import com.sun.corba.ee.spi.transport.ContactInfoList;
import org.omg.CORBA.portable.ObjectImpl;

import static com.meterware.simplestub.Stub.createStrictStub;

public class StubObject extends ObjectImpl {
    static org.omg.CORBA.Object createObjectWithIOR(IOR ior) {
        StubObject result = new StubObject();
        result._set_delegate(createDelegateWithIOR(ior));
        return result;
    }

    private static TestClientDelegate createDelegateWithIOR(IOR ior) {
        TestClientDelegate delegate = createStrictStub(TestClientDelegate.class);
        delegate.setContactInfoList(createInfoListWithIOR(ior));
        return delegate;
    }

    private static TestContactInfoList createInfoListWithIOR(IOR ior) {
        TestContactInfoList infoList = createStrictStub(TestContactInfoList.class);
        infoList.setTargetIOR(ior);
        return infoList;
    }

    public String[] _ids() {
        return new String[0];
    }

    abstract static class TestClientDelegate extends ClientDelegate {
        private ContactInfoList contactInfoList;

        public void setContactInfoList(ContactInfoList contactInfoList) {
            this.contactInfoList = contactInfoList;
        }

        public ContactInfoList getContactInfoList() {
            return contactInfoList;
        }
    }

    abstract static class TestContactInfoList implements ContactInfoList {
        private IOR ior;

        public void setTargetIOR(IOR ior) {
            this.ior = ior;
        }

        public IOR getTargetIOR() {
            return ior;
        }
    }
}
