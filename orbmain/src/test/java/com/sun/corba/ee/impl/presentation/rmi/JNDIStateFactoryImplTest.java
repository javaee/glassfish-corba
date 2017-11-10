/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright (c) 2017 Oracle and/or its affiliates. All rights reserved.
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
package com.sun.corba.ee.impl.presentation.rmi;

import com.meterware.simplestub.Memento;
import com.meterware.simplestub.StaticStubSupport;
import com.sun.corba.ee.spi.orb.ORB;
import com.sun.jndi.cosnaming.CNCtx;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.omg.CosNaming.NamingContext;

import javax.naming.Context;
import javax.naming.NamingException;
import javax.naming.spi.ResolveResult;
import javax.rmi.CORBA.PortableRemoteObjectDelegate;
import javax.rmi.PortableRemoteObject;
import java.rmi.NoSuchObjectException;
import java.rmi.Remote;
import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.Hashtable;
import java.util.List;

import static com.meterware.simplestub.Stub.createStrictStub;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;

public class JNDIStateFactoryImplTest {

    private JNDIStateFactoryImpl impl = new JNDIStateFactoryImpl();
    private final Remote remote = createStrictStub(Remote.class);
    private Hashtable<String,Object> env = new Hashtable<>();
    private ORB orb = createStrictStub(JndiOrb.class);

    private List<Memento> mementos = new ArrayList<>();
    private Context context;
    private CorbaStub corbaStub;

    @After
    public void tearDown() throws Exception {
        for (Memento memento : mementos) memento.revert();
    }

    @Before
    public void setUp() throws Exception {
        context = createContextWithOrb();
        corbaStub = createStrictStub(CorbaStub.class);
    }

    private Context createContextWithOrb() throws NamingException {
        env.put("java.naming.corba.orb", orb);
        ResolveResult result = CNCtx.createUsingURL("iiop://nohost", env);
        return (Context) result.getResolvedObj();
    }

    @Test
    public void whenObjectIsCorbaObject_returnIt() throws Exception {
        Object testObject = createStrictStub(org.omg.CORBA.Object.class);

        assertThat(impl.getStateToBind(testObject, null, null, env), sameInstance(testObject));
    }

    @Test
    public void whenObjectIsNotRemote_returnNull() throws Exception {
        assertThat(impl.getStateToBind(new Object(), null, null, env), nullValue());
    }

    @Test
    public void whenCannotGetOrbFromContext_returnNull() throws Exception {
        assertThat(impl.getStateToBind(remote, null, createStrictStub(Context.class), env), nullValue());
    }

    @Test
    public void whenObjectIsNotAStub_returnNull() throws Exception {
        installDelegate(createStrictStub(NoStubDelegate.class));

        assertThat(impl.getStateToBind(remote, null, context, env), nullValue());
    }

    private void installDelegate(PortableRemoteObjectDelegate delegate) throws NoSuchFieldException {
        mementos.add(StaticStubSupport.install(PortableRemoteObject.class, "proDelegate", delegate));
    }

    abstract static class NoStubDelegate implements PortableRemoteObjectDelegate {
        @Override
        public Remote toStub(Remote obj) throws NoSuchObjectException {
            return null;
        }
    }

    @Test
    public void whenObjectIsAStub_returnIt() throws Exception {
        installDelegateForStub(corbaStub);

        Object state = impl.getStateToBind(remote, null, context, env);
        assertThat(state, sameInstance(corbaStub));
    }

    private void installDelegateForStub(CorbaStub stub) throws NoSuchFieldException {
        installDelegate(createStrictStub(StubPRODelegate.class, stub));
    }

    @Test
    public void whenObjectIsAStub_connectIt() throws Exception {
        installDelegateForStub(corbaStub);

        impl.getStateToBind(remote, null, context, env);
        assertThat(corbaStub.connected, is(true));
    }

    abstract static class StubPRODelegate implements PortableRemoteObjectDelegate {
        Remote stub;

        public StubPRODelegate(Remote stub) {
            this.stub = stub;
        }

        @Override
        public Remote toStub(Remote obj) throws NoSuchObjectException {
            return stub;
        }
    }

    abstract static class CorbaStub extends javax.rmi.CORBA.Stub implements Remote {
        private boolean connected = false;

        @Override
        public void connect(org.omg.CORBA.ORB orb) throws RemoteException {
            connected = true;
        }
    }

    abstract static class JndiOrb extends ORB {
        @Override
        public org.omg.CORBA.Object string_to_object(String str) {
            return createStrictStub(NamingContext.class);
        }
    }
}