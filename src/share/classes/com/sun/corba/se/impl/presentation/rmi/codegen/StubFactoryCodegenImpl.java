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

package com.sun.corba.se.impl.presentation.rmi.codegen ;

import java.util.Map ;

import java.security.ProtectionDomain ;
import java.security.PrivilegedAction;
import java.security.PrivilegedExceptionAction;
import java.security.AccessController;

import java.lang.reflect.InvocationHandler ;
import java.lang.reflect.Method ;

import com.sun.corba.se.spi.orb.ORB ;

import com.sun.corba.se.impl.util.Utility ;

import com.sun.corba.se.spi.presentation.rmi.PresentationManager ;
import com.sun.corba.se.spi.presentation.rmi.IDLNameTranslator ;

import com.sun.corba.se.impl.presentation.rmi.StubFactoryDynamicBase ;
import com.sun.corba.se.impl.presentation.rmi.StubInvocationHandlerImpl ;


import com.sun.corba.se.impl.logging.ORBUtilSystemException ;

public class StubFactoryCodegenImpl extends StubFactoryDynamicBase  
{
    private static final ORBUtilSystemException wrapper =
	ORB.getStaticLogWrapperTable().get_RPC_PRESENTATION_ORBUtil() ;

    private static final String CODEGEN_KEY = "CodegenStubClass" ;
    private final PresentationManager pm ;

    public StubFactoryCodegenImpl( PresentationManager pm,
	PresentationManager.ClassData classData, ClassLoader loader ) 
    {
	super( classData, loader ) ;
	this.pm = pm ;
    }

    private Class<?> getStubClass()
    {
	Class<?> stubClass = null;

	// IMPORTANT: A get & put to classData's dictionary can occur
	//            by two or more threads in this method at the same
	//            time. Therefore, classData must be synchronized here.

	synchronized (classData) {
            final Map<String,Object> dictionary = classData.getDictionary() ;
            stubClass = (Class<?>)dictionary.get( CODEGEN_KEY ) ;
            if (stubClass == null) {
		final IDLNameTranslator nt = classData.getIDLNameTranslator() ;
                final Class<?> theClass = classData.getMyClass() ;
                final String stubClassName = Utility.dynamicStubName(
			theClass.getName() ) ; 
                final Class<?> baseClass = CodegenStubBase.class ;
                final Class<?>[] interfaces = nt.getInterfaces() ;
                final Method[] methods = nt.getMethods() ;

                final ProtectionDomain pd = 
		    AccessController.doPrivileged(
                         new PrivilegedAction<ProtectionDomain>() {
                             public ProtectionDomain run() {
                                 return theClass.getProtectionDomain() ;
                             }
                         }
                    ) ;

                // Create a StubGenerator that generates this stub class
		final CodegenProxyCreator creator = new CodegenProxyCreator( 
		    stubClassName, baseClass, interfaces, methods ) ;

		// Invoke creator in a doPrivileged block if there is a security 
		// manager installed.
		if (System.getSecurityManager() == null) {
		    stubClass = creator.create( pd, loader, pm.getDebug(), 
			pm.getPrintStream() ) ;
		} else {
		    stubClass = AccessController.doPrivileged(
			new PrivilegedAction<Class<?>>() {
			    public Class<?> run() {
				return creator.create( pd, loader, pm.getDebug(),
				    pm.getPrintStream() ) ;
			    }
			}
		    ) ;
		}

                dictionary.put( CODEGEN_KEY, stubClass ) ;
            }
        }

	return stubClass ;
    }

    public org.omg.CORBA.Object makeStub()
    {
	final Class<?> stubClass = getStubClass( ) ;

	CodegenStubBase stub = null ;

	try {
	    // Added doPriv for issue 778
	    stub = AccessController.doPrivileged( 
		new PrivilegedExceptionAction<CodegenStubBase>() {
		    public CodegenStubBase run() throws Exception {
			return CodegenStubBase.class.cast(
			    stubClass.newInstance() ) ;
		    }
		}
	    ) ;
	} catch (Exception exc) {
            // XXX Should this throw an exception?
	    wrapper.couldNotInstantiateStubClass( exc,
		stubClass.getName() ) ;
	}
	
	InvocationHandler handler = new StubInvocationHandlerImpl( pm, 
	    classData, stub ) ;

	stub.initialize( classData, handler ) ;

	return stub ;
    }
}
