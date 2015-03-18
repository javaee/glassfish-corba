package com.sun.corba.ee.impl.ior.iiop;

import com.sun.corba.ee.impl.orb.ORBVersionImpl;
import com.sun.corba.ee.spi.ior.ObjectId;
import com.sun.corba.ee.spi.ior.ObjectKeyTemplate;
import com.sun.corba.ee.spi.ior.iiop.IIOPProfileTemplate;
import com.sun.corba.ee.spi.orb.ORB;
import com.sun.corba.ee.spi.orb.ORBVersion;
import org.junit.Test;

import static com.meterware.simplestub.Stub.createStrictStub;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;

public class IIOPProfileImplTest {
    private ORB orb = createStrictStub(ORB.class);
    private ObjectId oid = createStrictStub(ObjectId.class);
    private IIOPProfileTemplate profileTemplate = createStrictStub(IIOPProfileTemplate.class);
    private ObjectKeyTemplateStub objectKeyTemplate = createStrictStub(ObjectKeyTemplateStub.class);

    private IIOPProfileImpl iiopProfile = new IIOPProfileImpl(orb, objectKeyTemplate, oid, profileTemplate);

    @Test
    public void whenForeignProfileOrbVersion_isLocalReturnsFalse() throws Exception {
        setOrbVersion(ORBVersionImpl.FOREIGN);

        assertThat(iiopProfile.isLocal(), is(false));
    }

    private void setOrbVersion(ORBVersion orbVersion) {
        objectKeyTemplate.setOrbVersion(orbVersion);
    }

    abstract static class ObjectKeyTemplateStub implements ObjectKeyTemplate {

        private ORBVersion orbVersion = ORBVersionImpl.NEW;

        public void setOrbVersion(ORBVersion orbVersion) {
            this.orbVersion = orbVersion;
        }

        @Override
        public ORBVersion getORBVersion() {
            return orbVersion;
        }
    }

}