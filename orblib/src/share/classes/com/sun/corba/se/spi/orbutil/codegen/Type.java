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

package com.sun.corba.se.spi.orbutil.codegen;

import java.util.Collections ;
import java.util.Map ;
import java.util.Set ;
import java.util.HashMap ;
import java.util.WeakHashMap ;

import java.lang.reflect.Modifier ;

import com.sun.corba.se.impl.orbutil.codegen.Identifier ;
import com.sun.corba.se.impl.orbutil.codegen.Node ;
import com.sun.corba.se.impl.orbutil.codegen.NodeBase ;
import com.sun.corba.se.impl.orbutil.codegen.ClassGenerator ;
import com.sun.corba.se.impl.orbutil.codegen.ClassInfoReflectiveImpl ;
import com.sun.corba.se.impl.orbutil.codegen.CurrentClassLoader ;
import com.sun.corba.se.impl.orbutil.codegen.Visitor ;

import com.sun.corba.se.spi.orbutil.copyobject.Immutable ;
import com.sun.corba.se.spi.orbutil.generic.Pair ;

/** Representation of Types (no generic support) used for
 * codegen API.
 *
 * @author Ken Cavanaugh
 */
@Immutable
public class Type {
    enum Sort { PRIMITIVE, ARRAY, CLASS } ;


    private String name ;	    // fully qualified name
    private String packageName ;    // package name: "" if none
    private String className ;	    // simple name of class (no package)

    private String signature ;
    private int size ;
    private Sort sort ;
    private boolean isNumber ;
    private int wideningNumber ;
    private Type memberType ;

    // The ClassInfo for this class, constructed on request
    // from either the generator (if specified) or reflectively
    // (if generator == null).  This must not (indirectly)
    // reference a ClassLoader!
    // ClassInfo may be obtained in one of several ways:
    // 1. This Type was obtained from the Type(ClassGenerator) constructor.
    // 2. This Type was obtained from the type(Class) static method, and
    //    the Class represented a Reference type (not an array or primitive).
    // 3. This Type was obtained from a Class name, and a ClassLoader is
    //    used to load the actual Class object, which represents a Class,
    //    and not an array or primitive.  See the classInfo( ClassLoader ) method.
    private ClassInfo classInfo ;

    // The class for this type, if this Type was constructed
    // from a Class instead of a ClassGenerator.
    private Class<?> typeClass ;
   
    // The constructor is private since all Types are either well-known
    // constants, or created through factory methods.
    private Type( String name, String signature, int size, boolean isNumber, 
	Sort sort, int wideningNumber, Type memberType ) 
    {
	this.name = name ;
	Pair<String,String> parts = Identifier.splitFQN( name ) ;
	this.packageName = parts.first() ;
	this.className = parts.second() ;

        this.signature = signature ;
        this.size = size ;
	this.isNumber = isNumber ;
        this.sort = sort ;
	this.wideningNumber = wideningNumber ;
	this.memberType = memberType ;

	this.classInfo = null ;
    }

    private Type( String name, String signature, int size, boolean isNumber, 
	Sort sort, int wideningNumber ) {

	this( name, signature, size, isNumber, sort, wideningNumber, null ) ;
    }
    
    private static ThreadLocal<Map<Class,Type>> classMap =
	new ThreadLocal<Map<Class,Type>>() {
	    public Map<Class,Type> initialValue() {
		return new WeakHashMap<Class,Type>() ;
	    }
	} ;

    private static ThreadLocal<Map<String,Type>> classNameMap =
	new ThreadLocal<Map<String,Type>>() {
	    public Map<String,Type> initialValue() {
		return new WeakHashMap<String,Type>() ;
	    }
	} ;

    private static Map<Class,Type> ptcToType = 
	new HashMap<Class,Type>() ;

    /** This method is only intended for internal use.  It is public because the
     * implementation that needs this is in a different package.
     */
    public static final void clearCaches() {
	classMap.get().clear() ;
	classNameMap.get().clear() ;
    }
 
// --------------------------------------------
// Constants for commonly used types.
// --------------------------------------------

    /* Void and Null are special types.  void is needed so that all
     * methods have a return type.  null is needed so that the null constant
     * can be correctly typed in expressions.  But NULL may not be used
     * as a type in a declaration.
     */
    private static final Type myVoid =	  new Type( "void",    "V", 0, false, Sort.PRIMITIVE, -1 ) ;
    private static final Type myNull =	  new Type( "NULL",    "N", 1, false, Sort.PRIMITIVE, -1 ) ;

    private static final Type myBoolean = new Type( "boolean", "Z", 1, false, Sort.PRIMITIVE, -1 ) ;

    private static final Type myByte =	  new Type( "byte",    "B", 1, true,  Sort.PRIMITIVE, 1 ) ;
    private static final Type myChar =	  new Type( "char",    "C", 1, true,  Sort.PRIMITIVE, 2 ) ;
    private static final Type myShort =	  new Type( "short",   "S", 1, true,  Sort.PRIMITIVE, 2 ) ;
    private static final Type myInt =	  new Type( "int",     "I", 1, true,  Sort.PRIMITIVE, 3 ) ;
    private static final Type myLong =	  new Type( "long",    "J", 2, true,  Sort.PRIMITIVE, 4 ) ;

    private static final Type myFloat =	  new Type( "float",   "F", 1, true,  Sort.PRIMITIVE, 5 ) ;
    private static final Type myDouble =  new Type( "double",  "D", 2, true,  Sort.PRIMITIVE, 6 ) ;

    static {
	ptcToType.put( boolean.class, myBoolean ) ;
	ptcToType.put( byte.class, myByte ) ;
	ptcToType.put( char.class, myChar ) ;
	ptcToType.put( short.class, myShort ) ;
	ptcToType.put( int.class, myInt ) ;
	ptcToType.put( long.class, myLong ) ;
	ptcToType.put( float.class, myFloat ) ;
	ptcToType.put( double.class, myDouble ) ;
	ptcToType.put( void.class, myVoid ) ;

	// Make this immutable!
	ptcToType = Collections.unmodifiableMap( ptcToType ) ;
    }

    private static final Type myObject = _class( "java.lang.Object" ) ;
    private static final Type myString = _class( "java.lang.String" ) ;
    private static final Type myClass = _class( "java.lang.Class" ) ;
    private static final Type myCloneable = _class( "java.lang.Cloneable" ) ;

// --------------------------------------------
// Various static factories and accessors for obtaining Types
// --------------------------------------------

    public static Type _array( Type memberType ) {
	String name = memberType.name() + "[]" ;
	Type result = classNameMap.get().get( name ) ;
	if (result == null) {
	    result = new Type( name, "[" + memberType.signature, 
		1, false, Sort.ARRAY, -1, memberType ) ;
	    classNameMap.get().put( name, result ) ;
	}

	return result ;
    }

    /** Return a codegen Type representing a class with the given name.
     * This is not bound to a specific Class object until/unless getTypeClass is called.
     */
    public static Type _class( String name ) {
	Type result = classNameMap.get().get( name ) ;
	if (result == null) {
	    // XXX Check for name being valid fully qualified Java identifier
	    result = new Type( name, "L" + name.replace( '.', '/' ) + ";", 
		1, false, Sort.CLASS, -1 ) ;
	    classNameMap.get().put( name, result ) ;
	}

	return result ;
    }

    public static Type _classGenerator( ClassGenerator cg ) {
	Type result = _class( cg.name() ) ;
	result.classInfo = cg ;
	return result ;
    }

    // Return whether a class is a standard part of the JDK, which is
    // always loaded by the bootstrap classloader.
    private static boolean classIsStandard( Class cls ) {
	String name = cls.getName() ;
	return name.startsWith("java.") ||
	    name.startsWith("javax.") ;
    }
    
    /** Return the codegen Type that corresponds to the Java (non-generic) 
     * Type represented by cls.
     */
    public static synchronized Type type( Class cls ) {
	// Handle primitive type first
	if (cls.isPrimitive()) {
	    Type type = ptcToType.get( cls ) ;
	    assert type != null ;
	    return type ;
	}

	// Now use non-primitive class cache
	Type result = classMap.get().get( cls ) ;
	if (result == null) {
	    if (cls.isArray()) {
		result = _array(type(cls.getComponentType())) ;
	    } else {
		result = _class(cls.getName()) ;
	    }

	    result.typeClass = cls ;
	    classMap.get().put( cls, result ) ;

	    if (classIsStandard(cls)) {
		// Standard classes have names that are the same in every
		// ClassLoader (unless someone does something really weird
		// with a ClassLoader).
		classNameMap.get().put( cls.getName(), result ) ;
	    }
	}

	return result ;
    }

    public static Type _void() {
	return myVoid ;
    }

    public static Type _null() {
	return myNull ;
    }

    public static Type _boolean() {
        return myBoolean ;
    }
    
    public static Type _byte() {
        return myByte ;
    }
    
    public static Type _char() {
        return myChar ;
    }
    
    public static Type _short() {
        return myShort ;
    }
    
    public static Type _int() {
        return myInt ;
    }
    
    public static Type _long() {
        return myLong ;
    }
    
    public static Type _float() {
        return myFloat ;
    }
    
    public static Type _double() {
        return myDouble ;
    }
   
    public static Type _Object() {
	return myObject ;
    }

    public static Type _String() {
	return myString ;
    }

    public static Type _Class() {
	return myClass ;
    }

    public static Type _Cloneable() {
	return myCloneable ;
    }

// --------------------------------------------
// Public Type methods
// --------------------------------------------

    public boolean isPrimitive() {
        return sort == Sort.PRIMITIVE ;
    }
    
    public boolean isArray() {
        return sort == Sort.ARRAY ;
    }
   
    public Type memberType() {
	if (isArray())
	    return memberType ;
	else
	    throw new IllegalStateException( "memberType() only valid for Array types" ) ;
    }

    /** Number of 32 bit words occupied by this type
     * if primitive, or 0 if non-primitive.
     */
    public int size() {
        return this.size ;
    }
    
    public String signature() {
        return this.signature ;
    }

    public String name() {
	return this.name ;
    }

    public String packageName() {
	return packageName ;
    }

    public String className() {
	return className ;
    }

    public boolean isNumber() {
	return this.isNumber ;
    }

    public Class<?> getTypeClass() {
	if (typeClass == null) {
	    try {
		typeClass = Class.forName( name, true, CurrentClassLoader.get() ) ;
	    } catch (ClassNotFoundException cnfe) {
		throw new IllegalArgumentException( "Cannot load class for type " +
		    name ) ;
	    }

	    // Now that the name has been resolved to a specific Class, 
	    // make it available from _type( Class ).
	    classMap.get().put( typeClass, this ) ;
	}


	return typeClass ;
    }

    public ClassInfo classInfo() {
	if (classInfo == null) {
	    if (isArray())
		throw new IllegalStateException( "Cannot get ClassInfo for array type " +
		    name ) ;

	    if (isPrimitive())
		throw new IllegalStateException( "Cannot get ClassInfo for primitive type " +
		    name ) ;

	    classInfo = new ClassInfoReflectiveImpl( this ) ;
	}

	return classInfo ;
    }

    public int hashCode() {
	return name.hashCode() ;
    }

    public String toString() {
	return "Type[" + name + "," + signature + "," + size + "," + sort 
	    + "]" ;
    }

    public boolean equals( Object obj ) {
	if (!(obj instanceof Type))
	    return false ;

	if (obj == this)
	    return true ;

	Type other = Type.class.cast( obj ) ;

	return signature.equals( other.signature ) ;
    }

    /** Return true iff there is a primitive narrowing conversion 
     * from Type t to this type.
     */
    public boolean hasPrimitiveNarrowingConversionFrom( Type t ) {
	if (isPrimitive()) {
	    if (!t.isPrimitive())
		return false ;

	    if ((wideningNumber < 0) || (t.wideningNumber < 0))
		return false ;

	    // Handle byte -> char as a special case
	    if (t == myByte)
		return this == myChar ;

	    if (t == this)
		return false ;

	    return t.wideningNumber >= wideningNumber ;
	}

	return false ;
    }

    /** Return true iff there is a primitive widening conversion 
     * from Type t to this type.
     */
    public boolean hasPrimitiveWideningConversionFrom( Type t ) {
	if (isPrimitive()) {
	    if (!t.isPrimitive())
		return false ;

	    if ((wideningNumber < 0) || (t.wideningNumber < 0))
		return false ;

	    // Not permitted, because bytes are signed and chars aren't
	    if ((t == myByte) && (this == myChar))
		return false ;

	    return t.wideningNumber < wideningNumber ;
	}

	return false ;
    }

    private boolean returnTypeCollision( Set<MethodInfo> set1,
	Set<MethodInfo> set2 ) {

	for (MethodInfo mi1 : set1) {
	    for (MethodInfo mi2 : set2) {
		if (mi1.signature().equals( mi2.signature() ))
		    if (!mi1.returnType().equals( mi2.returnType() ))
			return true ; 
	    }
	}

	return false ;
    }

    private boolean noMethodConflicts( Type t1, Type t2 ) {
	ClassInfo c1 = null ;
	ClassInfo c2 = null ;

	c1 = t1.classInfo() ;
	c2 = t2.classInfo() ;

	// Check that there is no method in common between c1 and c2
	// with the same name and signature, but difference return types.
	// Such a method would make it impossible to create an interface
	// that extends both c1 and c2.
	for (String name : c1.methodInfoByName().keySet()) {
	    if (c2.methodInfoByName().containsKey( name )) {
		Set<MethodInfo> set1 = c1.methodInfoByName().get( name ) ;
		Set<MethodInfo> set2 = c2.methodInfoByName().get( name ) ;

		if (returnTypeCollision( set1, set2 ))
		    return false ;
	    }
	}
	
	return true ;
    }

    // Should only be called for reference types
    private boolean isSubclass( Type t ) {
	return classInfo().isSubclass( t.classInfo() ) ;
    }

    private boolean isInterface() {
	if (isArray() || isPrimitive())
	    return false ;

	return classInfo().isInterface() ;
    }

    private int modifiers() {
	return classInfo().modifiers() ;
    }

    /** Return true iff there is a reference narrowing conversion 
     * from Type t to this type.
     */
    public boolean hasReferenceNarrowingConversionFrom( Type t ) {
	// This only applies to reference types.
	if (isPrimitive() || t.isPrimitive())
	    return false ;

	// JLS 5.1.5 bullets 3,4, plus object case of bullet 1.
	// These all imply that there is a narrowing reference
	// conversion from Object to any reference type.
	if (t.equals( _Object())) {
	    return true ;
	}

	// t is either interface, class, or array.
	if (t.isInterface()) {
	    if (isArray()) {
		return false ;
	    } else {
		if (!isInterface()) {
		    if (!Modifier.isFinal( modifiers())) {
			// JLS 5.1.5 bullet 5
			return true ;
		    } else if (isSubclass( t )) {
			// JLS 5.1.5 bullet 6
			return true ; 
		    }
		} else { // from interface to interface 
		    // JLS 5.1.5 bullet 7
		    if (!t.isSubclass( this )
			&& noMethodConflicts( t, this ))
			return true ;
		}
	    }
	} else if (t.isArray()) {
	    if (isArray()) {
		// JLS 5.1.5 bullet 8
		return memberType().hasReferenceNarrowingConversionFrom( 
		    t.memberType ) ;
	    } else {
		return false ;
	    }
	} else { // t is a class
	    if (isArray())
		return false ;

	    // JLS 5.1.5 bullet 1
	    if (isSubclass(t)) {
		return true ;
	    }

	    // JLS 5.1.5 bullet 2
	    if (!isInterface() && 
		!Modifier.isFinal(t.modifiers()) && 
		!t.isSubclass(this)) {
		return true ;
	    }
	}

	return false ;
    }

    /** Return true iff there is a reference widening conversion 
     * from Type t to this type.
     */
    public boolean hasReferenceWideningConversionFrom( Type t ) {
	if (isPrimitive() || t.isPrimitive())
	    return false ;

	if (this.equals( _Object() ))
	    return true ;

	if (t.equals( _null() ))
	    return true ;

	if (t.isArray()) {
	    if (this.equals( myCloneable ))
		return true ;

	    if (isArray() && !memberType().isPrimitive()) 
		return memberType().isMethodInvocationConvertibleFrom( 
		    t.memberType() ) ;
	    else
		return false ;
	} 

	// If t is a class, and this is an array, no conversion exists.
	if (isArray())
	    return false ;

	// handle non-array reference case
	// return true if t is a subclass of this, 
	// or t is a subinterface of this
	return t.isSubclass( this ) ;
    }

    /** Return true iff there is an assignment conversion from 
     * Type t to this type.
     */
    public boolean isAssignmentConvertibleFrom( Type t ) {
	if (equals( t ))
	    return true ;

	if (hasPrimitiveWideningConversionFrom( t ))
	    return true ;

	if (hasReferenceWideningConversionFrom( t ))
	    return true ;

	return false ;
    }

    /** Return true iff there is a casting conversion from 
     * Type t to this type.
     */
    public boolean isCastingConvertibleFrom( Type t ) {
	if (equals( t ))
	    return true ;

	if (hasPrimitiveWideningConversionFrom( t ))
	    return true ;

	if (hasReferenceWideningConversionFrom( t ))
	    return true ;

	if (hasPrimitiveNarrowingConversionFrom( t ))
	    return true ;

	if (hasReferenceNarrowingConversionFrom( t ))
	    return true ;

	return false ;
    }

    /** Return the type that is a unary promotion of this
     * type.
     */
    public Type unaryPromotion() {
	if (!isNumber())
	    throw new IllegalArgumentException(
		"Only number types have unary promotions" ) ;

	// JLS 5.6.1 bullet 1
	if (equals( _byte() ))
	    return _int() ;

	// JLS 5.6.1 bullet 1
	if (equals( _short() ))
	    return _int() ;

	// JLS 5.6.1 bullet 1
	if (equals( _char() ))
	    return _int() ;

	// JLS 5.6.1 bullet 2
	return this ;
    }

    /** Return the type that is the binary promotion of this
     * type and Type t.
     */
    public Type binaryPromotion( Type t ) {
	if (!isNumber() || !t.isNumber())
	    throw new IllegalArgumentException(
		"Only number types have binary promotions" ) ;

	// JLS 5.6.2 bullet 1
	if (equals(_double()) || t.equals(_double()))
	    return _double() ;

	// JLS 5.6.2 bullet 2
	if (equals(_float()) || t.equals(_float()))
	    return _float() ;

	// JLS 5.6.2 bullet 3
	if (equals(_long()) || t.equals(_long()))
	    return _long() ;

	// JLS 5.6.2 bullet 4
	return _int() ;
    }

    /** Return true iff one of the following statements is true:
     * <ol>
     * <li>this.equals( t ) .
     * <li>There is a widening primitive conversion from Type t
     * to this Type (see JLS 5.1.2).
     * <li>There is a widening reference conversion from Type t
     * to this Type (see JLS 5.1.4).
     * </ol>
     * This is similar to Class.isAssignmentCompatibleFrom, but 
     * also handles widening primitive conversions.  Throws
     * NullPointerException if t == null.
     */
    public boolean isMethodInvocationConvertibleFrom( Type t ) {
	if (t == null)
	    throw new NullPointerException() ;

	if (this.equals(t))
	    return true ;

	if (isPrimitive()) {
	    return hasPrimitiveWideningConversionFrom( t ) ;
	}

	return hasReferenceWideningConversionFrom( t ) ;
    }
}
