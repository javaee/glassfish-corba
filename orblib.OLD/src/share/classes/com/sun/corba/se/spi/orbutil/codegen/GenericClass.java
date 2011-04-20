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

package com.sun.corba.se.spi.orbutil.codegen;

import java.util.List ;
import java.util.ArrayList ;

import java.lang.reflect.Constructor ;

/** Class that allows any class to be instantiated via any accessible constructor.
 * Really a short hand to avoid writing a bunch of reflective code.
 */
public class GenericClass<T> {
    private Type implType ;
    private ClassInfo implClassInfo ;
    private Class<T> typeClass ;
    
    // Use the raw type of the constructor here, because
    // MethodInfo can only return a raw type for a constructor.
    // It is not possible to have MethodInfo return a 
    // Constructor<T> because T may not be known at compile time.
    private Constructor constructor ;
  
    /** Create a GenericClass of the given type by modifying classData
     * with the given interceptors.
     * XXX we may need a constructor that specifies the ClassLoader, etc.
     */
    public GenericClass( Class<T> type, InterceptorContext ic, byte[] classData ) {
	throw new IllegalArgumentException( "Not supported yet" ) ;
    }

    /** Create a generic of type T for the untyped class cls.
     * Generally cls is a class that has been generated and loaded, so
     * no compiled code can depend on the class directly.  However, the
     * generated class probably implements some interface T, represented
     * here by Class<T>.
     * @throws IllegalArgumentException if cls is not a subclass of type. 
     */
    public GenericClass( Class<T> type, Class<?> cls ) {

	if (!type.isAssignableFrom( cls ))
	    throw new IllegalArgumentException( "Class " + cls.getName() +
		" is not a subclass of " + type.getName() ) ;

	implType = Type.type( cls ) ;
	implClassInfo = implType.classInfo() ;
	typeClass = type ;
    }

    private synchronized Constructor getConstructor( Object... args ) {
	if (constructor == null) {
	    List<Type> atypes = new ArrayList<Type>() ;
	    for (Object arg : args) {
		Type type = Type._null() ;
		if (arg != null) {
		    Class<?> cls = arg.getClass() ;
		    type = Type.type( cls ) ;
		}

		atypes.add( type ) ;
	    }

	    Signature sig = Signature.fromConstructorUsingTypes( implType, atypes ) ;
	    MethodInfo minfo = implClassInfo.findConstructorInfo( sig ) ;
	    constructor = minfo.getConstructor() ;
	}
	return constructor ;
    }
    
    private synchronized Constructor clearAndGetConstructor( Object... args ) {
	constructor = null ;
	return getConstructor( args ) ;
    }

    /** Create an instance of type T using the constructor that
     * matches the given arguments if possible.  The constructor
     * is cached, so an instance of GenericClass should always be
     * used for the same types of arguments.  If a call fails,
     * a check is made to see if a different constructor could 
     * be used.
     */
    public T create( Object... args ) {
	try {
	    try {
		return typeClass.cast( getConstructor().newInstance( args ) ) ;	
	    } catch (IllegalArgumentException argexc) {
		return typeClass.cast( clearAndGetConstructor( args ).newInstance( args ) ) ;
	    }
	} catch (Exception exc ) {
	    throw new IllegalArgumentException( 
		"Could not construct instance of class " 
		+ implType.name(), exc ) ;
	}
    }
}
