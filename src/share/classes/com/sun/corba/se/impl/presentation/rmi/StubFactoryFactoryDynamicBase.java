/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2004-2007 Sun Microsystems, Inc. All rights reserved.
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

package com.sun.corba.se.impl.presentation.rmi;

import java.rmi.Remote ;
import javax.rmi.CORBA.Tie ;

import org.omg.CORBA.CompletionStatus;

import org.omg.CORBA.portable.IDLEntity ;

import com.sun.corba.se.spi.presentation.rmi.PresentationManager;
import com.sun.corba.se.spi.presentation.rmi.PresentationDefaults;

import com.sun.corba.se.spi.orb.ORB;

import com.sun.corba.se.impl.logging.ORBUtilSystemException ;

import com.sun.corba.se.impl.javax.rmi.CORBA.Util;

import com.sun.corba.se.impl.orbutil.ClassInfoCache ;

public abstract class StubFactoryFactoryDynamicBase extends 
    StubFactoryFactoryBase
{
    protected final ORBUtilSystemException wrapper ; 

    public StubFactoryFactoryDynamicBase() 
    {
	wrapper = ORB.getStaticLogWrapperTable().get_RPC_PRESENTATION_ORBUtil() ;
    }

    public PresentationManager.StubFactory createStubFactory(
	String className, boolean isIDLStub, String remoteCodeBase, 
	Class expectedClass, ClassLoader classLoader)
    {
	Class cls = null ;

	try {
	    cls = Util.getInstance().loadClass( className, remoteCodeBase, 
		classLoader ) ;
	} catch (ClassNotFoundException exc) {
	    throw wrapper.classNotFound3( 
		CompletionStatus.COMPLETED_MAYBE, exc, className ) ;
	}

	ClassInfoCache.ClassInfo cinfo = ClassInfoCache.get( cls ) ;
	PresentationManager pm = ORB.getPresentationManager() ;

	if (cinfo.isAIDLEntity(cls) && !cinfo.isARemote(cls)) {
	    // IDL stubs must always use static factories.
	    PresentationManager.StubFactoryFactory sff = 
		pm.getStubFactoryFactory( false ) ; 
	    PresentationManager.StubFactory sf = 
		sff.createStubFactory( className, true, remoteCodeBase, 
		    expectedClass, classLoader ) ;
	    return sf ;
	} else {
	    PresentationManager.ClassData classData = pm.getClassData( cls ) ;
	    return makeDynamicStubFactory( pm, classData, classLoader ) ;
	}
    }

    public abstract PresentationManager.StubFactory makeDynamicStubFactory( 
	PresentationManager pm, PresentationManager.ClassData classData, 
	ClassLoader classLoader ) ;

    public Tie getTie( Class cls )
    {
	PresentationManager pm = ORB.getPresentationManager() ;
	return new ReflectiveTie( pm, wrapper ) ;
    }

    public boolean createsDynamicStubs() 
    {
	return true ;
    }
}
