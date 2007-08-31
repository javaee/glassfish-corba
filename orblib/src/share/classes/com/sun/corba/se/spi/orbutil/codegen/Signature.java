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

import java.lang.reflect.Modifier ;

import java.io.StringWriter ;
import java.io.PrintWriter ;

import java.util.List ;
import java.util.Set ;
import java.util.HashSet ;
import java.util.ArrayList ;
import java.util.Iterator ;

import com.sun.corba.se.spi.orbutil.codegen.MethodInfo ;
import com.sun.corba.se.spi.orbutil.codegen.ClassInfo ;

import com.sun.corba.se.impl.orbutil.codegen.Visitor ;
import com.sun.corba.se.impl.orbutil.codegen.NodeBase ;
import com.sun.corba.se.impl.orbutil.codegen.Node ;

import com.sun.corba.se.spi.orbutil.copyobject.Immutable ;

/** Represents the signature of a method, which is sometimes needed
 * for selecting the correct method. 
 *
 * @author Ken Cavanaugh
 */
@Immutable
public final class Signature { 
    private Type rtype ;
    private List<Type> types ;
    private String signature ;
       
    private Signature( Type rtype, List<Type> types) {
        this.rtype = rtype ;
	this.types = types ;
        signature = "(" ;
	if (types != null)
	    for (Type t : types ) {
		signature += t.signature() ;
	    }
        signature += ( ")" + rtype.signature() ) ;
    }

    public static Signature make( Type rtype, List<Type> types) {
	// XXX As for Type, should we intern Signature?
	return new Signature( rtype, types ) ;
    }

    public Type returnType() {
        return rtype ;
    }
    
    public List<Type> argTypes() {
        return types ;
    }
    
    public int hashCode() {
	return signature.hashCode() ;
    }

    public String toString() {
	return "Signature[" + signature + "]" ;
    }

    public String signature() {
        return signature ;
    }

    public String displayAsMethod() {
	return displayAsMethod( "" ) ;
    }

    public String displayAsMethod( String methodName ) {
	StringBuilder result = new StringBuilder() ;
	if (methodName.length() > 0) {
	    result.append( rtype.name() ) ;
	    result.append( " " ) ;
	    result.append( methodName ) ;
	}
	result.append( "(" ) ;
	boolean first = true ;
	for (Type t : types) {
	    if (first) 
		first = false ;
	    else
		result.append( ", " ) ;

	    result.append( t.name() ) ;
	}
	result.append( ")" ) ;
	return result.toString() ;
    }

    public boolean equals( Object obj ) {
	if (!(obj instanceof Signature))
	    return false ;

	if (obj == this)
	    return true ;

	Signature other = Signature.class.cast( obj ) ;
	return signature.equals( other.signature ) ;
    }

    private void checkArgTypeCompatibility( List<Type> atypes ) {
	checkArgTypeCompatibility( atypes, true ) ;
    }

    private boolean checkArgTypeCompatibility( List<Type> atypes, boolean throwsException ) {
	// Check that the each type in Types is assignment
	// compatible with each type in this.types
	Iterator<Type> titer = this.types.iterator() ;
	Iterator<Type> aiter = atypes.iterator() ;
	while (titer.hasNext() && aiter.hasNext()) {
	    Type tt = titer.next() ;
	    Type at = aiter.next() ;
	    if (!tt.isMethodInvocationConvertibleFrom( at ))
		if (throwsException) {
		    throw new IllegalArgumentException(
			"Type " + at.name() + " cannot be converted to type "
			+ tt.name() + " by a method invocation conversion" ) ;
		} else {
		    return false ;
		}
	}

	if (titer.hasNext() != aiter.hasNext())
	    if (throwsException) {
		throw new IllegalArgumentException( 
		    "Signature requires " + types.size() + " but " 
		    + atypes.size() + " arguments were passed to call" ) ;
	    } else {
		return false ;
	    }

	return true ;
    }

//----------------------------------------------------------------------
//
// Methods for call argument checking 
//
//----------------------------------------------------------------------

    static List<Type> getExprTypes( List<Expression> exprs ) {
	List<Type> result = new ArrayList<Type>() ;
	for (Expression ex : exprs ) 
	    result.add( ex.type() ) ;
	return result ;
    }

    private static ClassInfo getClassInfo( Type type ) {
	ClassInfo cinfo = null ;
	if (type != null) {
	    cinfo = type.classInfo() ;
	}

	return cinfo ;
    }

    private void checkCompatibility( Type targetType, String ident,
	List<Expression> args, boolean isStaticMethod ) {

	List<Type> atypes = getExprTypes( args ) ;
	checkArgTypeCompatibility( atypes ) ; 

	ClassInfo cinfo = getClassInfo( targetType ) ;
	MethodInfo minfo = cinfo.findMethodInfo( ident, this ) ;
	if (minfo == null)
	    throw new IllegalArgumentException( 
		"Could not find method " + displayAsMethod( ident )
		+ " in class " + cinfo.name() ) ;

	if (isStaticMethod != Modifier.isStatic( minfo.modifiers() ) ) {
	    if (isStaticMethod)
		throw new IllegalArgumentException( 
		    "Method " + displayAsMethod( ident )
		    + " is not static" ) ;
	    else
		throw new IllegalArgumentException( 
		    "Method " + displayAsMethod( ident )
		    + " is static" ) ;
	}
    }

    /** Check whether the list of expression in args is statically 
     * compatible with this Signature.  This means that 
     * args and this.types have the same length, and the type
     * of each expression in args is assignment compatible with
     * the corresponding types in this.types. Also, the targetType
     * must actually contain a non-static method of the appropriate 
     * signature and name.
     * @throws IllegalArgumentException if args is not compatible with 
     * this.types.
     */
    public void checkCompatibility( Type targetType, String ident,
	List<Expression> args ) {

	checkCompatibility( targetType, ident, args, false ) ;
    }

    /** Check whether the list of expression in args is statically 
     * compatible with this Signature.  This means that 
     * args and this.types have the same length, and the type
     * of each expression in args is assignment compatible with
     * the corresponding types in this.types. Also, the targetType
     * must actually contain a static method of the appropriate 
     * signature and name.
     * @throws IllegalArgumentException if args is not compatible with 
     * this.types.
     */
    public void checkStaticCompatibility( Type targetType, String ident,
	List<Expression> args ) {

	checkCompatibility( targetType, ident, args, true ) ;
    }

    /** Check whether the list of expression in args is statically 
     * compatible with this Signature.  This means that 
     * args and this.types have the same length, and the type
     * of each expression in args is assignment compatible with
     * the corresponding types in this.types. Also, the targetType
     * must actually contain a constructor of the appropriate 
     * signature and name.
     * @throws IllegalArgumentException if args is not compatible with 
     * this.types.
     */
    public void checkConstructorCompatibility( Type targetType, 
	List<Expression> args ) {

	List<Type> atypes = getExprTypes( args ) ;
	checkArgTypeCompatibility( atypes ) ; 

	ClassInfo cinfo = getClassInfo( targetType ) ;
	MethodInfo minfo = null ;
	for (MethodInfo info : cinfo.constructorInfo()) {
	    if (this.equals( info.signature())) {
		minfo = info ;
		break ;
	    }
	}

	if (minfo == null)
	    throw new IllegalArgumentException( 
		"Could not find constructor with signature " 
		+ displayAsMethod() + " in class " + cinfo.name() ) ;
    }

//----------------------------------------------------------------------
// 
// Methods for method overload resolution
// 
//----------------------------------------------------------------------

    private static Set<MethodInfo> getMethods( Type type, String ident, 
	boolean staticOnly ) {

	ClassInfo cinfo = getClassInfo( type ) ;
	final Set<MethodInfo> result = new HashSet<MethodInfo>() ;
	while (cinfo != null) {
	    Set<MethodInfo> methods = cinfo.methodInfoByName().get( ident ) ;

	    if (methods != null) {
		for (MethodInfo mi : methods) {
		    // XXX we also need to do accessibility checking here!
		    if (Modifier.isStatic( mi.modifiers() ) == staticOnly) {
			result.add( mi ) ;
		    }
		}
	    }

	    cinfo = getClassInfo( cinfo.superType() ) ;
	}
	    
	if (result.size() == 0)
	    if (staticOnly) {
		throw new IllegalArgumentException(
		    "Type " + type.name() + " does not have any static methods named " 
		    + ident ) ;
	    } else {
		throw new IllegalArgumentException(
		    "Type " + type.name() + " does not have any non-static methods named " 
		    + ident ) ;
	    }

	return result ;
    }

    private static Set<MethodInfo> getCompatibleMethods( 
	Set<MethodInfo> methods, List<Type> argTypes ) {

	Set<MethodInfo> compatibleMethods = new HashSet<MethodInfo>() ;	
	for (MethodInfo minfo : methods) {
	    Signature sig = minfo.signature() ;
	    if (sig.checkArgTypeCompatibility( argTypes, false ))
		compatibleMethods.add( minfo ) ;
	}

	return compatibleMethods ;
    }

    private static String sprintf( String format, Object... args ) {
	StringWriter sw = new StringWriter() ;
	PrintWriter pw = new PrintWriter( sw ) ;
	pw.printf( format, args ) ;
	return sw.toString() ;
    }

    private enum CallType { STATIC, NON_STATIC, CONSTRUCTOR } ;
    
    private static String getCallTypeString( CallType ct, String ident ) {
	switch (ct) {
	    case STATIC : 
		return "static method named " + ident ;
	    case NON_STATIC :
		return "non-static method named " + ident ;
	    case CONSTRUCTOR :
		return "constructor" ; 
	    default :
		throw new IllegalArgumentException( 
		    "getCallTypeString is missing a CallType" ) ;
	}
    }

    private static String getTypeListString( List<Type> types ) {

	final String start = "(" ;
	final StringBuilder asb = new StringBuilder() ;
	asb.append( start ) ;

	for (Type type : types) {
	    if (asb.length() != start.length()) {
		asb.append( ", " ) ;
	    }
	    asb.append( type.name() ) ;
	}

	asb.append( ")" ) ;
	return asb.toString() ;
    }

    private static String getMethodListString( Set<MethodInfo> mlist ) {
	StringBuilder sb = new StringBuilder() ;
	for (MethodInfo m : mlist ) {
	    sb.append( sprintf( "\t%s\n", 
		m.signature().displayAsMethod( m.name() ) ) ) ;
	}
	return sb.toString() ;
    }

    // Decide which method in compatibleMethods is the best match.
    private static MethodInfo returnCompatibleMethod( 
	Type type, String ident, List<Type> argTypes,
	CallType ctype, Set<MethodInfo> compatibleMethods ) {

	if (compatibleMethods.size() == 0) {
	    String cts = getCallTypeString( ctype, ident ) ;
	    String tls = getTypeListString( argTypes ) ;
	    throw new IllegalArgumentException( sprintf( 
		"Could not find %s in class %s "
		+ "compatible with arguments %s", 
		cts, type.name(), tls ) );
	} else if (compatibleMethods.size() == 1) {
	    for (MethodInfo m : compatibleMethods)
		return m ;
	} else {
	    // XXX temporary: declare error if more than 
	    // one compatible method.  Should compute the
	    // greatest lower bound of the compatible methods
	    // and use that, if it exists (it may not).
	    String cts = getCallTypeString( ctype, ident ) ;
	    String tls = getTypeListString( argTypes ) ;
	    String infoList = getMethodListString( compatibleMethods ) ;
	    throw new IllegalArgumentException( sprintf( 
		"Found more than one %s in class %s "
		+ "compatible with arguments %s:\n%s", 
		cts, ident, type.name(), tls, infoList ) ) ;
	}

	return null ;
    }

    private static Signature fromMethodCallUsingTypes( Type type, String ident,
	List<Type> types, boolean isStaticCall ) {

	Set<MethodInfo> methods = getMethods( type, ident, false ) ;
	Set<MethodInfo> compatibleMethods = getCompatibleMethods( methods, 
	    types ) ;

	MethodInfo minfo = returnCompatibleMethod( type, ident, types, 
	    isStaticCall ? CallType.STATIC : CallType.NON_STATIC, 
	    compatibleMethods ) ;
	return minfo.signature() ;
    }

    private static Signature fromMethodCall( Type type, String ident,
	List<Expression> exprs, boolean isStaticCall ) {

	List<Type> types = getExprTypes( exprs ) ;
	return fromMethodCallUsingTypes( type, ident, types, isStaticCall ) ;
    }

// API for method overload resolution ===========================================

    public static Signature fromCall( Type type, String ident, 
	List<Expression> exprs ) {

	return fromMethodCall( type, ident, exprs, false ) ;
    }

    public static Signature fromCallUsingTypes( Type type, String ident, 
	List<Type> types ) {

	return fromMethodCallUsingTypes( type, ident, types, false ) ;
    }

    public static Signature fromStaticCall( Type type, String ident, 
	List<Expression> exprs ) {

	return fromMethodCall( type, ident, exprs, true ) ;
    }

    public static Signature fromStaticCallUsingTypes( Type type, String ident, 
	List<Type> types ) {

	return fromMethodCallUsingTypes( type, ident, types, true ) ;
    }

    public static Signature fromConstructorUsingTypes( Type type,
	List<Type> types ) {

	ClassInfo cinfo = getClassInfo( type ) ;
	Set<MethodInfo> methods = cinfo.constructorInfo() ;
	if (methods == null)
	    throw new IllegalArgumentException(
		"Type " + type.name() + " does not have any constructors!" ) ;

	Set<MethodInfo> compatibleMethods = getCompatibleMethods( methods, 
	    types ) ;
	MethodInfo minfo = returnCompatibleMethod( type, "", types, CallType.CONSTRUCTOR, 
	    compatibleMethods ) ;
	return minfo.signature() ;
    }

    public static Signature fromConstructor( Type type, 
	List<Expression> exprs ) {

	List<Type> types = getExprTypes( exprs ) ;
	return fromConstructorUsingTypes( type, types ) ;
    }
}
