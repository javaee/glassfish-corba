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

package com.sun.corba.se.impl.presentation.rmi;

import javax.rmi.CORBA.Tie ;

import org.omg.CORBA.CompletionStatus;

import com.sun.corba.se.spi.presentation.rmi.PresentationManager;

import com.sun.corba.se.impl.util.PackagePrefixChecker;
import com.sun.corba.se.impl.util.Utility;

import com.sun.corba.se.spi.orb.ORB ;

import com.sun.corba.se.spi.orbutil.ORBClassLoader;

import com.sun.corba.se.impl.logging.ORBUtilSystemException ;

import com.sun.corba.se.impl.javax.rmi.CORBA.Util;

public class StubFactoryFactoryStaticImpl extends 
    StubFactoryFactoryBase 
{
    private ORBUtilSystemException wrapper = 
	ORB.getStaticLogWrapperTable().get_RPC_PRESENTATION_ORBUtil() ;

    public PresentationManager.StubFactory createStubFactory(
	String className, boolean isIDLStub, String remoteCodeBase, Class 
	expectedClass, ClassLoader classLoader)
    {
	String stubName = null ;

	if (isIDLStub) 
	    stubName = Utility.idlStubName( className ) ;
	else
	    stubName = Utility.stubNameForCompiler( className ) ;

	ClassLoader expectedTypeClassLoader = 
	    (expectedClass == null ? classLoader : 
	    expectedClass.getClassLoader());

	// The old code was optimized to try to guess which way to load classes
	// first.  The real stub class name could either be className or 
	// "org.omg.stub." + className.  We will compute this as follows:
	// If stubName starts with a "forbidden" package, try the prefixed
	// version first, otherwise try the non-prefixed version first.
	// In any case, try both forms if necessary.

	String firstStubName = stubName ;
	String secondStubName = stubName ;

	if (PackagePrefixChecker.hasOffendingPrefix(stubName))
	    firstStubName = PackagePrefixChecker.packagePrefix() + stubName ;
	else
	    secondStubName = PackagePrefixChecker.packagePrefix() + stubName ;

	Class clz = null;

	try {
	    clz = Util.getInstance().loadClass( firstStubName, remoteCodeBase, 
		expectedTypeClassLoader ) ;
	} catch (ClassNotFoundException e1) {
	    // log only at FINE level
	    wrapper.classNotFound1( CompletionStatus.COMPLETED_MAYBE,
		e1, firstStubName ) ;
	    try {
		clz = Util.getInstance().loadClass( secondStubName, remoteCodeBase, 
		    expectedTypeClassLoader ) ;
	    } catch (ClassNotFoundException e2) {
		throw wrapper.classNotFound2( 
		    CompletionStatus.COMPLETED_MAYBE, e2, secondStubName ) ;
	    }
	}

	// XXX Is this step necessary, or should the Util.loadClass
	// algorithm always produce a valid class if the setup is correct?
	// Does the OMG standard algorithm need to be changed to include
	// this step?
        if ((clz == null) || 
	    ((expectedClass != null) && !expectedClass.isAssignableFrom(clz))) {
	    try {
		clz = ORBClassLoader.loadClass(className);
	    } catch (Exception exc) {
		// XXX make this a system exception
		IllegalStateException ise = new IllegalStateException( 
		    "Could not load class " + stubName ) ;
		ise.initCause( exc ) ;
		throw ise ;
	    }
        }

	return new StubFactoryStaticImpl( clz ) ;
    }

    public Tie getTie( Class cls )
    {
	Class tieClass = null ;
	String className = Utility.tieName(cls.getName());

	// XXX log exceptions at FINE level
	try {
	    try {
		//_REVISIT_ The spec does not specify a loadingContext parameter for
		//the following call.  Would it be useful to pass one?  
		tieClass = Utility.loadClassForClass(className, Util.getInstance().getCodebase(cls), 
		    null, cls, cls.getClassLoader());
		return (Tie) tieClass.newInstance();
	    } catch (Exception err) {
		tieClass = Utility.loadClassForClass(
		    PackagePrefixChecker.packagePrefix() + className, 
		    Util.getInstance().getCodebase(cls), null, cls, cls.getClassLoader());
		return (Tie) tieClass.newInstance();
	    }
        } catch (Exception err) {
            return null;    
        }

    }

    public boolean createsDynamicStubs() 
    {
	return false ;
    }
}
