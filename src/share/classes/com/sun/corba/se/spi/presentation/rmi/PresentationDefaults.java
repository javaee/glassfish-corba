/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2003-2007 Sun Microsystems, Inc. All rights reserved.
 *
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common Development
 * and Distribution License("CDDL") (collectively, the "License").  You
 * may not use this file except in compliance with the License. You can obtain
 * a copy of the License at https://glassfish.dev.java.net/public/CDDL+GPL.html
 * or glassfish/bootstrap/legal/LICENSE.txt.  See the License for the specific
 * language governing permissions and limitations under the License.
 *
 * When distributing the software, include this License Header Notice in each
 * file and include the License file at glassfish/bootstrap/legal/LICENSE.txt.
 * Sun designates this particular file as subject to the "Classpath" exception
 * as provided by Sun in the GPL Version 2 section of the License file that
 * accompanied this code.  If applicable, add the following below the License
 * Header, with the fields enclosed by brackets [] replaced by your own
 * identifying information: "Portions Copyrighted [year]
 * [name of copyright owner]"
 *
 * Contributor(s):
 *
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

package com.sun.corba.se.spi.presentation.rmi;

import java.security.AccessController ;
import java.security.PrivilegedAction ;

import com.sun.corba.se.spi.orb.ORB;

import com.sun.corba.se.spi.presentation.rmi.PresentationManager;
import com.sun.corba.se.spi.presentation.rmi.InvocationInterceptor;

import com.sun.corba.se.impl.orbutil.ORBConstants ;

import com.sun.corba.se.impl.presentation.rmi.StubFactoryFactoryProxyImpl;
import com.sun.corba.se.impl.presentation.rmi.StubFactoryFactoryStaticImpl;
import com.sun.corba.se.impl.presentation.rmi.StubFactoryStaticImpl;
import com.sun.corba.se.impl.presentation.rmi.PresentationManagerImpl ;

import com.sun.corba.se.impl.logging.ORBUtilSystemException ;

import com.sun.corba.se.spi.orbutil.misc.ORBClassLoader ;

public abstract class PresentationDefaults
{
    private static StubFactoryFactoryStaticImpl staticImpl = null ;

    private PresentationDefaults() {}

    public synchronized static PresentationManager.StubFactoryFactory 
	getStaticStubFactoryFactory()
    {
	if (staticImpl == null)
	    staticImpl = new StubFactoryFactoryStaticImpl( );

	return staticImpl ;
    }

    public static PresentationManager.StubFactoryFactory 
	getProxyStubFactoryFactory()
    {
	return new StubFactoryFactoryProxyImpl();
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

    public static PresentationManager makeOrbPresentationManager() {

	final ORBUtilSystemException staticWrapper  = 
	    ORB.getStaticLogWrapperTable().get_RPC_PRESENTATION_ORBUtil() ;

	final boolean useDynamicStub = getBooleanPropertyValue( 
	    ORBConstants.USE_DYNAMIC_STUB_PROPERTY, inAppServer() ) ;

	final boolean debug = getBooleanPropertyValue( 
	    ORBConstants.DEBUG_DYNAMIC_STUB, false ) ;

	PresentationManager.StubFactoryFactory dynamicStubFactoryFactory = 
	    AccessController.doPrivileged(
		new PrivilegedAction<PresentationManager.StubFactoryFactory>() {
		    public PresentationManager.StubFactoryFactory run() {
			PresentationManager.StubFactoryFactory sff = 
			    PresentationDefaults.getProxyStubFactoryFactory() ;

			final String className = System.getProperty( 
			    ORBConstants.DYNAMIC_STUB_FACTORY_FACTORY_CLASS,
			    "com.sun.corba.se.impl.presentation.rmi.codegen.StubFactoryFactoryCodegenImpl" ) ;

			try {
			    // First try the configured class name, if any
			    final Class cls = ORBClassLoader.loadClass( className ) ;
			    sff = (PresentationManager.StubFactoryFactory)cls.newInstance() ;
			} catch (Exception exc) {
			    // Use the default. Log the error as a warning. 
			    staticWrapper.errorInSettingDynamicStubFactoryFactory( 
				exc, className ) ;
			}

			return sff ;
		    }
		}
	    ) ;

	final PresentationManager result = new PresentationManagerImpl( useDynamicStub ) ;
	result.setStubFactoryFactory( false, 
	    PresentationDefaults.getStaticStubFactoryFactory() ) ;
	result.setStubFactoryFactory( true, dynamicStubFactoryFactory ) ; 
	if (debug)
	    result.enableDebug( System.out ) ;
	return result ;
    }
}
