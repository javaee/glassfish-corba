package com.sun.corba.ee.impl.folb;

import com.sun.corba.ee.impl.corba.AnyImpl;
import com.sun.corba.ee.impl.interceptors.CodecFactoryImpl;
import com.sun.corba.ee.spi.folb.GroupInfoServiceObserver;
import com.sun.corba.ee.spi.misc.ORBConstants;
import com.sun.corba.ee.spi.orb.ORB;
import com.sun.corba.ee.spi.orb.ORBData;
import com.sun.corba.ee.spi.transport.ContactInfo;
import com.sun.corba.ee.spi.transport.IIOPPrimaryToContactInfo;
import com.sun.corba.ee.spi.transport.IORToSocketInfo;
import org.glassfish.corba.testutils.TestCorbaObject;
import org.junit.Before;
import org.junit.Test;
import org.omg.CORBA.Any;
import org.omg.CORBA.ORBPackage.InvalidName;
import org.omg.CORBA.Object;
import org.omg.IOP.*;
import org.omg.IOP.CodecFactoryPackage.UnknownEncoding;
import org.omg.IOP.CodecPackage.InvalidTypeForEncoding;
import org.omg.PortableInterceptor.*;
import org.omg.PortableInterceptor.ORBInitInfoPackage.DuplicateName;

import java.util.*;

import static org.glassfish.corba.testutils.EasyStub.stub;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.fail;

public class GroupManagerServiceInterceptorsTest implements GroupInfoServiceObserver {

    private final ClientGroupManager clientGroupManager = new ClientGroupManager();
    private final TestORB orb = stub(TestORB.class);
    private final TestORBData orbData = stub(TestORBData.class);
    private final TestORBInitInfo orbInitInfo = stub(TestORBInitInfo.class);
    private final TestClientRequestInfo clientRequestInfo = stub(TestClientRequestInfo.class);
    private final TestContactInfo contactInfo = stub(TestContactInfo.class);

    private static final byte[] COMPONENT_DATA_1 = new byte[]{0, 1, 2, 5, 6};
    private static final byte[] COMPONENT_DATA_2 = new byte[]{9, 3, 3};

    private int numMembershipChanges;
    private Codec codec;

    public void membershipChange() {
        numMembershipChanges++;
    }

    @Before
    public void setUp() throws InvalidName, UnknownEncoding {
        orb.setORBData(orbData);
        clientGroupManager.configure(null, orb);
        clientGroupManager.addObserver(this);
        CodecFactoryImpl codecFactory = new CodecFactoryImpl(orb);
        orb.register_initial_reference(ORBConstants.CODEC_FACTORY_NAME, codecFactory);
        codec = codecFactory.create_codec(new Encoding((short)0, (byte)1, (byte)2));
        preInitInitializers();
        postInitInitializers();
        clientGroupManager.reset(contactInfo);
    }

    private void preInitInitializers() {
        for (ORBInitializer initializer : orbData.getORBInitializers())
            initializer.pre_init(orbInitInfo);
    }

    private void postInitInitializers() {
        for (ORBInitializer initializer : orbData.getORBInitializers())
            initializer.post_init(orbInitInfo);
    }

    @Test
    public void whenClientGroupManagerInitialized_registerForCallbacks() {
        assertEquals(clientGroupManager, orbData.getIIOPPrimaryToContactInfo());
        assertEquals(clientGroupManager, orbData.getIORToSocketInfo());
    }

    @Test
    public void whenRequestIORContainsNoFolbMembershipComponent_doNothing() throws ForwardRequest {
        defineFolbRequestTaggedComponents();  // No components defined

        sendRequest();

        assertNull(getFolbMembershipServiceContext());
    }

    @Test
    public void whenRequestIORContainsOneFolbMembershipComponent_createCorrespondingServiceContext() throws ForwardRequest {
        defineFolbRequestTaggedComponents(COMPONENT_DATA_1);

        sendRequest();

        assertEqualData(COMPONENT_DATA_1, getFolbMembershipServiceContext().context_data);
    }

    @Test
    public void whenRequestIORContainsMultipleFolbMembershipComponents_useFirstForServiceContext() throws ForwardRequest {
        defineFolbRequestTaggedComponents(COMPONENT_DATA_1, COMPONENT_DATA_2);

        sendRequest();

        assertEqualData(COMPONENT_DATA_1, getFolbMembershipServiceContext().context_data);
    }

    @Test
    public void whenReceiveReplyWithNoFolbServiceContext_doNoCallbacks() throws ForwardRequest {
        receiveReply();

        assertEquals(0, numMembershipChanges);
    }

    @Test
    public void whenReceiveExceptionWithNoFolbServiceContext_doNoCallbacks() throws ForwardRequest {
        receiveException();

        assertEquals(0, numMembershipChanges);
    }

    @Test
    public void whenReceiveReplyWithFolbServiceContext_issueCallback() throws ForwardRequest, InvalidTypeForEncoding {
        TestIOR ior = createIORWithFolbTaggedComponents();
        Object forwardTo = TestObject.createObjectWithIOR(ior);
        ForwardRequest forwardRequest = new ForwardRequest(forwardTo);
        Any any = new AnyImpl(orb);
        ForwardRequestHelper.insert(any, forwardRequest);
        setFolbIorUpdateContext(codec.encode(any));

        receiveReply();

        assertEquals(1, numMembershipChanges);
    }

    private void setFolbIorUpdateContext(byte[] data) {
        clientRequestInfo.setReplyServiceContext(new ServiceContext(ORBConstants.FOLB_IOR_UPDATE_SERVICE_CONTEXT_ID, data));
    }


    public void clientInterceptorOnReceive_addsNewIORForListeners() {
        // when we receive a reply, if it has a service context of type ORBConstants.FOLB_IOR_UPDATE_SERVICE_CONTEXT_ID,
        // extract a forwarding IOR and notify any listeners.
        // (need this for receive_request, receive_exception, and receive_other)
    }

    private void assertEqualData( byte[] expected, byte[] actual) {
        if (!Arrays.equals(expected, actual))
            fail( "expected " + Arrays.toString(expected) + " but was " + Arrays.toString(actual));
    }

    private ServiceContext getFolbMembershipServiceContext() {
        return clientRequestInfo.getRequestServiceContext(ORBConstants.FOLB_MEMBERSHIP_LABEL_SERVICE_CONTEXT_ID);
    }

    private void sendRequest() throws ForwardRequest {
        for (ClientRequestInterceptor interceptor : orbInitInfo.clientRequestInterceptors)
            interceptor.send_request(clientRequestInfo);
    }

    private void receiveReply() throws ForwardRequest {
        for (ClientRequestInterceptor interceptor : orbInitInfo.clientRequestInterceptors)
            interceptor.receive_reply(clientRequestInfo);
    }

    private void receiveException() throws ForwardRequest {
        for (ClientRequestInterceptor interceptor : orbInitInfo.clientRequestInterceptors)
            interceptor.receive_exception(clientRequestInfo);
    }

    private void defineOperation(String operationName) {
        clientRequestInfo.setOperation(operationName);
    }

    private void defineFolbRequestTaggedComponents(byte[]... componentData) {
        clientRequestInfo.setEffectiveTarget(createObjectWithFolbTaggedComponents(componentData));
    }

    private Object createObjectWithFolbTaggedComponents(byte[]... componentData) {
        TestIOR ior = createIORWithFolbTaggedComponents(componentData);
        return TestObject.createObjectWithIOR(ior);
    }

    private static TestIOR createIORWithFolbTaggedComponents(byte[]... componentData) {
        TaggedComponent[] taggedComponents = new TaggedComponent[componentData.length];
        for (int i = 0; i < taggedComponents.length; i++)
            taggedComponents[i] = new TaggedComponent(ORBConstants.FOLB_MEMBERSHIP_LABEL_TAGGED_COMPONENT_ID, componentData[i]);

        return TestIOR.createIORWithTaggedComponents(ORBConstants.FOLB_MEMBERSHIP_LABEL_TAGGED_COMPONENT_ID, taggedComponents);
    }


    abstract static class TestORBInitInfo extends TestCorbaObject implements ORBInitInfo {
        List<ClientRequestInterceptor> clientRequestInterceptors = new ArrayList<ClientRequestInterceptor>();

        public void add_client_request_interceptor(ClientRequestInterceptor interceptor) throws DuplicateName {
            clientRequestInterceptors.add(interceptor);
        }

    }

    abstract static class TestORBData implements ORBData {
        private IORToSocketInfo IORToSocketInfo;
        private IIOPPrimaryToContactInfo IIOPPrimaryToContactInfo;
        private List<ORBInitializer> orbInitializers = new ArrayList<ORBInitializer>();

        public IORToSocketInfo getIORToSocketInfo() {
            return IORToSocketInfo;
        }

        public void setIORToSocketInfo(IORToSocketInfo IORToSocketInfo) {
            this.IORToSocketInfo = IORToSocketInfo;
        }

        public ORBInitializer[] getORBInitializers() {
            return orbInitializers.toArray(new ORBInitializer[orbInitializers.size()]);
        }

        public void addORBInitializer(ORBInitializer orbInitializer) {
            orbInitializers.add(orbInitializer);
        }

        public IIOPPrimaryToContactInfo getIIOPPrimaryToContactInfo() {
            return IIOPPrimaryToContactInfo;
        }

        public void setIIOPPrimaryToContactInfo(IIOPPrimaryToContactInfo IIOPPrimaryToContactInfo) {
            this.IIOPPrimaryToContactInfo = IIOPPrimaryToContactInfo;
        }
    }


    abstract static class TestORB extends ORB {
        private ORBData ORBData;
        private Map<String, Object> initialReferences = new HashMap<String, Object>();

        public void setORBData(ORBData orbData) {
            this.ORBData = orbData;
        }

        public ORBData getORBData() {
            return ORBData;
        }

        public void register_initial_reference(String id, Object obj) throws org.omg.CORBA.ORBPackage.InvalidName {
            initialReferences.put(id, obj);
        }

        public Object resolve_initial_references(String id) throws InvalidName {
            return initialReferences.get(id);
        }
    }


    abstract static public class TestClientRequestInfo implements ClientRequestInfo {
        private Object effectiveTarget;
        private String operation = "";
        private Map<Integer,ServiceContext> requestServiceContexts = new HashMap<Integer, ServiceContext>();
        private Map<Integer,ServiceContext> replyServiceContexts = new HashMap<Integer, ServiceContext>();

        public Object effective_target() {
            return effectiveTarget;
        }

        public String operation() {
            return operation;
        }

        public void add_request_service_context(ServiceContext serviceContext, boolean replace) {
            requestServiceContexts.put(serviceContext.context_id, serviceContext);
        }

        public ServiceContext get_reply_service_context(int id) {
            return replyServiceContexts.get(id);
        }

        public void setEffectiveTarget(Object effectiveTarget) {
            this.effectiveTarget = effectiveTarget;
        }

        public void setOperation(String operation) {
            this.operation = operation;
        }

        public ServiceContext getRequestServiceContext(int id) {
            return requestServiceContexts.get(id);
        }

        public void setReplyServiceContext(ServiceContext serviceContext) {
            replyServiceContexts.put(serviceContext.context_id, serviceContext);
        }
    }

    abstract static class TestContactInfo implements ContactInfo {
        public int getPort() {
            return 1000;
        }
    }


}
