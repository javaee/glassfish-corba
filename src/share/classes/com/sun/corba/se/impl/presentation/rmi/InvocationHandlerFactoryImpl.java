/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright (c) 1997-2010 Oracle and/or its affiliates. All rights reserved.
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

package com.sun.corba.ee.impl.presentation.rmi ;

import java.lang.reflect.InvocationHandler ;
import java.lang.reflect.Proxy ;


import java.io.ObjectStreamException ;
import java.io.Serializable ;

import com.sun.corba.ee.spi.presentation.rmi.PresentationManager ;
import com.sun.corba.ee.spi.presentation.rmi.DynamicStub ;
import org.glassfish.pfl.basic.proxy.CompositeInvocationHandler;
import org.glassfish.pfl.basic.proxy.CompositeInvocationHandlerImpl;
import org.glassfish.pfl.basic.proxy.DelegateInvocationHandlerImpl;
import org.glassfish.pfl.basic.proxy.InvocationHandlerFactory;
import org.glassfish.pfl.basic.proxy.LinkedInvocationHandler;

public class InvocationHandlerFactoryImpl implements InvocationHandlerFactory 
{
    private final PresentationManager.ClassData classData ;
    private final PresentationManager pm ;
    private Class<?>[] proxyInterfaces ;

    public InvocationHandlerFactoryImpl( PresentationManager pm,
        PresentationManager.ClassData classData ) 
    {
        this.classData = classData ;
        this.pm = pm ;

        Class<?>[] remoteInterfaces =
            classData.getIDLNameTranslator().getInterfaces() ;
        proxyInterfaces = new Class<?>[ remoteInterfaces.length + 1 ] ;
        System.arraycopy(remoteInterfaces, 0, proxyInterfaces, 0,
            remoteInterfaces.length);

        proxyInterfaces[remoteInterfaces.length] = DynamicStub.class ;
    }

    private static class CustomCompositeInvocationHandlerImpl extends
        CompositeInvocationHandlerImpl implements LinkedInvocationHandler, 
        Serializable
    {
        private transient DynamicStub stub ;

        public void setProxy( Proxy proxy ) 
        {
            if (proxy instanceof DynamicStub) {
                ((DynamicStubImpl)stub).setSelf( (DynamicStub)proxy ) ;
            } else {
                throw new RuntimeException(
                    "Proxy not instance of DynamicStub" ) ;
            }
        }

        public Proxy getProxy()
        {
            return (Proxy)((DynamicStubImpl)stub).getSelf() ;
        }

        public CustomCompositeInvocationHandlerImpl( DynamicStub stub )
        {
            this.stub = stub ;
        }

        /** Return the stub, which will actually be written to the stream.
         * It will be custom marshaled, with the actual writing done in
         * StubIORImpl.  There is a corresponding readResolve method on
         * DynamicStubImpl which will re-create the full invocation
         * handler on read, and return the invocation handler on the 
         * readResolve method.
         */
        public Object writeReplace() throws ObjectStreamException
        {
            return stub ;
        }
    }

    public InvocationHandler getInvocationHandler() 
    {
        final DynamicStub stub = new DynamicStubImpl( 
            classData.getTypeIds() ) ; 

        return getInvocationHandler( stub ) ;
    }

    // This is also used in DynamicStubImpl to implement readResolve.
    InvocationHandler getInvocationHandler( DynamicStub stub ) 
    {
        // Create an invocation handler for the methods defined on DynamicStub,
        // which extends org.omg.CORBA.Object.  This handler delegates all
        // calls directly to a DynamicStubImpl, which extends 
        // org.omg.CORBA.portable.ObjectImpl.
        InvocationHandler dynamicStubHandler = 
            DelegateInvocationHandlerImpl.create( stub ) ;

        // Create an invocation handler that handles any remote interface
        // methods.
        InvocationHandler stubMethodHandler = new StubInvocationHandlerImpl( 
            pm, classData, stub ) ;

        // Create a composite handler that handles the DynamicStub interface
        // as well as the remote interfaces.
        final CompositeInvocationHandler handler = 
            new CustomCompositeInvocationHandlerImpl( stub ) ;
        handler.addInvocationHandler( DynamicStub.class,
            dynamicStubHandler ) ;
        handler.addInvocationHandler( org.omg.CORBA.Object.class,
            dynamicStubHandler ) ;
        handler.addInvocationHandler( Object.class,
            dynamicStubHandler ) ;

        // If the method passed to invoke is not from DynamicStub or its superclasses,
        // it must be from an implemented interface, so we just handle
        // all of these with the stubMethodHandler.  This used to be
        // done be adding explicit entries for stubMethodHandler for 
        // each remote interface, but that does not work correctly
        // for abstract interfaces, since the graph analysis ignores
        // abstract interfaces in order to compute the type ids 
        // correctly (see PresentationManagerImpl.NodeImpl.getChildren).
        // Rather than produce more graph traversal code to handle this
        // problem, we simply use a default.
        // This also points to a possible optimization: just use explict
        // checks for the three special classes, rather than a general
        // table lookup that usually fails.
        handler.setDefaultHandler( stubMethodHandler ) ;

        return handler ;
    }

    public Class[] getProxyInterfaces()
    {
        return proxyInterfaces ;
    }
}
