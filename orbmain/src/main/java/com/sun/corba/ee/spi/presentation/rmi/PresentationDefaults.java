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

package com.sun.corba.ee.spi.presentation.rmi;

import java.security.AccessController ;
import java.security.PrivilegedAction ;

import com.sun.corba.ee.spi.misc.ORBConstants ;

import com.sun.corba.ee.impl.presentation.rmi.StubFactoryFactoryStaticImpl;
import com.sun.corba.ee.impl.presentation.rmi.StubFactoryStaticImpl;
import com.sun.corba.ee.impl.presentation.rmi.PresentationManagerImpl ;

import com.sun.corba.ee.impl.presentation.rmi.codegen.StubFactoryFactoryCodegenImpl ;

public abstract class PresentationDefaults
{
    private static PresentationManager.StubFactoryFactory staticImpl = null ;
    private static PresentationManager.StubFactoryFactory dynamicImpl = null ;

    private PresentationDefaults() {}

    public synchronized static PresentationManager.StubFactoryFactory 
        getDynamicStubFactoryFactory()
    {
        if (dynamicImpl == null) {
            dynamicImpl =
                new StubFactoryFactoryCodegenImpl();
        }

        return dynamicImpl ;
    }

    public synchronized static PresentationManager.StubFactoryFactory 
        getStaticStubFactoryFactory()
    {
        if (staticImpl == null) {
            staticImpl =
                new StubFactoryFactoryStaticImpl();
        }

        return staticImpl ;
    }

    public static PresentationManager.StubFactory makeStaticStubFactory( 
        final Class stubClass )
    {
        return new StubFactoryStaticImpl( stubClass ) ;
    }

    private static InvocationInterceptor nullInvocationInterceptor = 
        new InvocationInterceptor() {
            public void preInvoke() {}
            public void postInvoke() {}
        } ;

    public static InvocationInterceptor getNullInvocationInterceptor() 
    {
        return nullInvocationInterceptor ;
    }
    
    public static boolean inAppServer() {
        final String thisClassRenamed = 
            "com.sun.corba.ee.spi.presentation.rmi.PresentationDefaults" ;
        final boolean inAppServer = 
            PresentationDefaults.class.getName().equals( thisClassRenamed ) ;
        return inAppServer ;
    }

    private static boolean getBooleanPropertyValue( final String propName, 
        final boolean def ) {

        final String defs = Boolean.toString( def ) ;
        final String value = AccessController.doPrivileged(
            new PrivilegedAction<String>() {
                public String run() {
                    return System.getProperty( propName, defs ) ;
                }
            }
        ) ;

        return Boolean.valueOf( value ) ;
    }

    public static PresentationManagerImpl makeOrbPresentationManager() {
        final boolean useDynamicStub = getBooleanPropertyValue( 
            ORBConstants.USE_DYNAMIC_STUB_PROPERTY, inAppServer() ) ;

        final boolean debug = getBooleanPropertyValue( 
            ORBConstants.DEBUG_DYNAMIC_STUB, false ) ;

        final PresentationManagerImpl result = new PresentationManagerImpl( useDynamicStub ) ;
        result.setStaticStubFactoryFactory(PresentationDefaults.getStaticStubFactoryFactory());
        result.setDynamicStubFactoryFactory(PresentationDefaults.getDynamicStubFactoryFactory());
        if (debug) {
            result.enableDebug( System.out ) ;
        }

        return result ;
    }
}
