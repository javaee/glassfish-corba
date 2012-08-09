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

package com.sun.corba.ee.impl.presentation.rmi.codegen ;

import java.io.ObjectInputStream ;
import java.io.IOException ;
import java.io.ObjectStreamException ;

import java.lang.reflect.InvocationHandler ;
import java.lang.reflect.Method ;
import java.lang.reflect.Field ;

import java.security.AccessController ;
import java.security.PrivilegedAction ;

import javax.rmi.CORBA.Stub ;


import com.sun.corba.ee.spi.orb.ORB ;
import com.sun.corba.ee.spi.presentation.rmi.PresentationManager ;
import com.sun.corba.ee.spi.presentation.rmi.StubAdapter ;

import com.sun.corba.ee.spi.logging.ORBUtilSystemException ;
import com.sun.corba.ee.impl.util.JDKBridge ;
import com.sun.corba.ee.impl.util.RepositoryId ;
import com.sun.corba.ee.impl.ior.StubIORImpl ;
import com.sun.corba.ee.impl.javax.rmi.CORBA.StubDelegateImpl ;
import com.sun.corba.ee.impl.presentation.rmi.StubInvocationHandlerImpl ;

public class CodegenStubBase extends Stub 
{
    private transient String[] typeIds ;
    private transient Method[] methods ;
    private transient PresentationManager.ClassData classData ;
    private transient InvocationHandler handler ;

    private static final ORBUtilSystemException wrapper =
        ORBUtilSystemException.self ;

    private Object readResolve( ) throws ObjectStreamException
    {
        // Note that we cannot use PRO.narrow here, because the 
        // delegate is not set by deserializing the stub.  Only
        // the IOR is set, because a deserialized stub is not connected
        // to an ORB.
        PresentationManager pm = ORB.getPresentationManager() ;
        PresentationManager.StubFactoryFactory sff = pm.getDynamicStubFactoryFactory();
        PresentationManager.StubFactory sf = sff.createStubFactory(
            classData.getMyClass().getName(), false, null, null, null ) ;
        org.omg.CORBA.Object stub = sf.makeStub() ;
        StubDelegateImpl stubSDI = getStubDelegateImpl( stub ) ;
        StubDelegateImpl mySDI = getStubDelegateImpl( this ) ;
        stubSDI.setIOR( mySDI.getIOR() ) ;
        return stub ;
    }

    // Get the StubDelegateImpl from the superclass.  Unfortunately stubDelegate
    // is private in javax.rmi.CORBA.Stub, and Stub must follow
    // the OMG standard.  So, we use reflection to get stubDelegate.
    // We also need to handle the case where the stubDelegate has not
    // been set yet.  This reqires a call to the private method 
    // setDefaultDelegate.
    //
    private static StubDelegateImpl getStubDelegateImpl( 
        final org.omg.CORBA.Object stub )
    {
        StubDelegateImpl sdi = getStubDelegateImplField( stub ) ;
        if (sdi == null) {
            setDefaultDelegate(stub);
        }
        sdi = getStubDelegateImplField( stub ) ;
        return sdi ;
    }

    private static StubDelegateImpl getStubDelegateImplField( 
        final org.omg.CORBA.Object stub )
    {
        return (StubDelegateImpl)AccessController.doPrivileged( 
            new PrivilegedAction() {
                public Object run() {
                    try {
                        Field fld = Stub.class.getDeclaredField( "stubDelegate" ) ;
                        fld.setAccessible( true ) ;
                        return fld.get( stub ) ;
                    } catch (Exception exc) {
                        throw wrapper.couldNotAccessStubDelegate() ;
                    } 
                }
            }
        ) ;
    }

    private static Method setDefaultDelegateMethod = null ;

    private static void setDefaultDelegate( final org.omg.CORBA.Object stub )
    {
        AccessController.doPrivileged( 
            new PrivilegedAction() {
                public Object run() {
                    try {
                        if (setDefaultDelegateMethod == null) {
                            setDefaultDelegateMethod = 
                                Stub.class.getDeclaredMethod( "setDefaultDelegate") ;
                            setDefaultDelegateMethod.setAccessible( true ) ;
                        }

                        setDefaultDelegateMethod.invoke( stub ) ;
                    } catch (Exception exc) {
                        throw wrapper.couldNotAccessStubDelegate( exc ) ;
                    }
                    return null ;
                }
            }
        ) ;
    }

    private void readObject( ObjectInputStream stream ) throws
        IOException, ClassNotFoundException 
    {
        // Let the superclass do the read of the internal IOR,
        // but we need to restore our transient fields.  But
        // let defaultReadObject do its normal processing.
        stream.defaultReadObject() ;

        StubDelegateImpl sdi = getStubDelegateImpl( this ) ;

        StubIORImpl ior = sdi.getIOR() ;
        String repositoryId = ior.getRepositoryId() ;
        String cname = RepositoryId.cache.getId( repositoryId ).getClassName() ; 

        Class cls = null ;

        try {
            cls = JDKBridge.loadClass( cname, null, null ) ;
        } catch (ClassNotFoundException exc) {
            throw wrapper.couldNotLoadInterface( exc, cname ) ;
        }

        PresentationManager pm = ORB.getPresentationManager() ;
        classData = pm.getClassData( cls ) ;

        InvocationHandler handler = new StubInvocationHandlerImpl( pm, 
            classData, this ) ; 
        initialize( classData, handler ) ;
    }

    public String[] _ids()
    {
        return typeIds.clone() ;
    }

    /** Must be called to complete the initialization of the stub.
     * Note that we have mutual dependence between the InvocationHandler
     * and the Stub: the InvocationHandler needs the stub in order
     * to get the delegate, and the Stub needs the InvocationHandler
     * to perform an invocation.  We resolve this dependency by
     * constructing the Stub first, using the stub to construct the
     * InvocationHandler, and then completing the initialization of
     * the Stub by calling initialize.
     */
    public void initialize( PresentationManager.ClassData classData,
        InvocationHandler handler )
    {
        this.classData = classData ;
        this.handler = handler ;
        typeIds = classData.getTypeIds() ;
        methods = classData.getIDLNameTranslator().getMethods() ;
    }

    // Needed in generated code: clone self (which is the generated class)
    // as the base class.  This is needed for a generated writeReplace method.
    // This allows the readResolve method on this class to construct the
    // appropriate codegen proxy when this class is deserialized.
    protected Object selfAsBaseClass()
    {
        CodegenStubBase result = new CodegenStubBase() ;
        StubAdapter.setDelegate( result,
            StubAdapter.getDelegate( this )) ;
        return result ;
    }
    
    // Needed in generated code
    protected Object invoke( int methodNumber, Object[] args ) throws Throwable 
    {
        Method method = methods[methodNumber] ;
        
        // Pass null for the Proxy since we don't have one.
        return handler.invoke( null, method, args ) ;
    }
}
