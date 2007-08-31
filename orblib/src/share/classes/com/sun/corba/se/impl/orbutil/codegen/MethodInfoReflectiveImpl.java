/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2005-2007 Sun Microsystems, Inc. All rights reserved.
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

package com.sun.corba.se.impl.orbutil.codegen;

import java.lang.reflect.Method ;
import java.lang.reflect.Constructor ;

import java.util.List ;
import java.util.ArrayList ;

import com.sun.corba.se.spi.orbutil.copyobject.Immutable ;

import com.sun.corba.se.spi.orbutil.codegen.Type ;
import com.sun.corba.se.spi.orbutil.codegen.Variable ;
import com.sun.corba.se.spi.orbutil.codegen.MethodInfo ;
import com.sun.corba.se.spi.orbutil.codegen.ClassInfo ;

/** Implementation of MethodInfo interface for actual Method.
 * Note that this internally caches the Method, and so all the
 * usual precautions for storing instances of this class in 
 * maps apply.
 */

@Immutable
public class MethodInfoReflectiveImpl extends MethodInfoBase {
    private Method method = null ;
    private Constructor constructor = null ;

    public MethodInfoReflectiveImpl( ClassInfo cinfo, Constructor constructor ) {
	super( cinfo, constructor.getModifiers()  ) ;

	this.constructor = constructor ;
	init( constructor.getExceptionTypes(), 
	    constructor.getParameterTypes() ) ;
    }

    public MethodInfoReflectiveImpl( ClassInfo cinfo, Method method ) {
	super( cinfo, method.getModifiers(), 
	    Type.type( method.getReturnType() ), method.getName() ) ;

	this.method = method ;
	init( method.getExceptionTypes(), 
	    method.getParameterTypes() ) ;
    }

    private void init( Class<?>[] exceptions, Class<?>[] arguments ) {
	ExpressionFactory ef = new ExpressionFactory( null ) ;

	for (Class<?> cls : exceptions) 
	    this.exceptions.add( Type.type(cls) ) ;

	// Note that we can't get the real parameter 
	// names through reflection, so we just make
	// up names in this case.  The names must not
	// affect hashCode or equals (see MethodInfoBase).
	int ctr = 0 ;
	for (Class<?> cls : arguments){
	    String name = "arg" + ctr++ ;
	    Variable var = ef.variable( Type.type(cls), name ) ;
	    var.close() ;
	    this.arguments.add( var ) ;
	}
    }

    public Method getMethod() {
	if (isConstructor())
	    throw new IllegalStateException( 
		"Cannot obtain a Method from a MethodInfo that represents a Constructor" ) ;
	return method ;
    }

    public Constructor getConstructor() {
	if (!isConstructor())
	    throw new IllegalStateException( 
		"Cannot obtain a Constructor from a MethodInfo that represents a Method" ) ;
	return constructor ;
    }
}
