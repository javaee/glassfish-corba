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

import java.util.List ;
import java.util.Map ;
import java.util.LinkedHashMap ;
import java.util.Set ;
import java.util.HashSet ;

import java.lang.reflect.Modifier ;

import com.sun.corba.se.spi.orbutil.codegen.Type ;
import com.sun.corba.se.spi.orbutil.codegen.Signature ;
import com.sun.corba.se.spi.orbutil.codegen.ClassInfo ;
import com.sun.corba.se.spi.orbutil.codegen.MethodInfo ;
import com.sun.corba.se.spi.orbutil.codegen.FieldInfo ;

public abstract class ClassInfoBase implements ClassInfo {
    // Initialized in the constructor
    private int modifiers ;
    private Type thisType ;
    private String className ;
    private String pkgName ;

    private boolean initComplete ;

    // Initialized in initializeInterface/initializeClass
    private boolean isInterface ;
    private Type superType ;
    private List<Type> impls ;

    // Updated by add methods: also affect hashCode
    private Map<String,Set<MethodInfo>> methodInfoByName ;
    private Set<MethodInfo> constructors ;
    private Map<String,FieldInfo> fields ;

    // Cached hashCode
    private boolean hashIsCached ;
    private int hashValue ;

    /** Construct a ClassInfoBase representing a class or interface.
     */
    public ClassInfoBase( int modifiers, Type thisType ) {
	this.modifiers = modifiers ;
	this.thisType = thisType ;
	String name = thisType.name() ;
	int index = name.lastIndexOf( '.' ) ;
	if (index == -1) {
	    className = name ;
	    pkgName = "" ;
	} else {
	    className = name.substring( index+1 ) ;
	    pkgName = name.substring( 0, index ) ;
	}

	this.initComplete = false ;

	this.constructors = new HashSet<MethodInfo>() ;
	this.methodInfoByName = new LinkedHashMap<String,Set<MethodInfo>>() ;
	this.fields = new LinkedHashMap<String,FieldInfo>() ;

	this.hashValue = 0 ;
	this.hashIsCached = false ;
    }

    private void checkComplete() {
	if (!initComplete)
	    throw new IllegalStateException( 
		"ClassInfoBase initialization is not complete" ) ;
    }

    private void checkReinitialize() {
	if (initComplete)
	    throw new IllegalStateException( 
		"ClassInfoBase cannot be reinitialized" ) ;
    }

    protected void initializeInterface( List<Type> exts ) {
	checkReinitialize() ;

	this.isInterface = true ;
	this.superType = null ; // This should match java, and Class.getSuperclass is null for
				// an interface.
	this.impls = exts ;

	this.initComplete = true ;
    }

    protected void initializeClass( Type thisType, Type superType, 
	List<Type> impls ) {
	checkReinitialize() ;

	this.isInterface = false ;
	this.thisType = thisType ;
	this.superType = superType ;
	this.impls = impls ;

	this.initComplete = true ;
    }

    protected void addFieldInfo( FieldInfo finfo ) {
	checkComplete() ;
	clearHashCode() ;
	if (isInterface) {
	    int mod = finfo.modifiers() ;
	    if (!(Modifier.isPublic(mod) && Modifier.isFinal(mod) && Modifier.isStatic(mod)))
		throw new IllegalStateException(
		    "Only public static final fields can be added to an interface" ) ;
	}

	fields.put( finfo.name(), finfo ) ;
    }

    protected void addMethodInfo( MethodInfo minfo ) {
	checkComplete() ;
	clearHashCode() ;
	Set<MethodInfo> minfos = methodInfoByName.get( minfo.name() ) ;
	if (minfos == null) {
	    minfos = new HashSet<MethodInfo>() ;
	    methodInfoByName.put( minfo.name(), minfos ) ;
	}

	if (isInterface && !Modifier.isAbstract( minfo.modifiers() ))
	    throw new IllegalStateException( 
		"All methods in an interface must be abstract" ) ;

	minfos.add( minfo ) ;
    }

    protected void addConstructorInfo( MethodInfo cinfo ) {
	checkComplete() ;
	clearHashCode() ;
	if (isInterface)
	    throw new IllegalStateException(
		"Cannot add a constructor to an interface" ) ;
	constructors.add( cinfo ) ;
    }

    public Type thisType() {
	return thisType ;
    }

    public boolean isInterface() {
	checkComplete() ;
	return isInterface ;
    }

    public int modifiers() {
	return modifiers ;
    }

    public String name() {
	return thisType.name() ;
    }

    public String className() {
	return className ;
    }

    public String pkgName() {
	return pkgName ;
    }

    public Type superType() {
	checkComplete() ;
	return superType ;
    }

    public List<Type> impls() {
	checkComplete() ;
	return impls ;
    }

    public Map<String,FieldInfo> fieldInfo() {
	checkComplete() ;
	return fields ;
    }

    public FieldInfo findFieldInfo( String name ) {
	FieldInfo info = fields.get( name ) ;
	if (info == null) {
	    if (superType() == null) 
		return null ;

	    ClassInfo superInfo = superType().classInfo() ;
	    info = superInfo.findFieldInfo( name ) ;
	}

	return info ;
    }

    public Map<String,Set<MethodInfo>> methodInfoByName() {
	checkComplete() ;
	return methodInfoByName ;
    }

    public Set<MethodInfo> constructorInfo() {
	return constructors ;
    }


    private MethodInfo findMethodInfo( Signature sig, Set<MethodInfo> minfos ) {
	if (minfos != null)
	    for (MethodInfo minfo : minfos)
		if (sig.equals( minfo.signature() ))
		    return minfo ;

	return null ;
    }

    public MethodInfo findMethodInfo( String name, Signature sig ) {
	MethodInfo result = null ;
	// First search this class and all its superclasses.
	// If this class is an interface, only the methods in the
	// interface are searched here, since superType() is null for
	// interfaces.
	ClassInfo current = this ;
	while (current != null) {
	    Set<MethodInfo> minfos = current.methodInfoByName().get( name ) ;
	    result = findMethodInfo( sig, minfos ) ;
	    if (result != null)
		return result ;

	    if (current.superType() == null)
		current = null ;
	    else
		current = current.superType().classInfo() ;
	}

	// Then search all implemented interfaces recursively
	for (Type type : impls) {
	    result = type.classInfo().findMethodInfo( name, sig ) ;
	    if (result != null)
		return result ;
	}

	return result ;
    }

    public MethodInfo findConstructorInfo( Signature sig ) {
	return findMethodInfo( sig, constructors ) ;
    }

    public boolean isSubclass( ClassInfo info ) {
	// A class is a subclass of itself
	if (this.equals( info ))
	    return true ;

        // All classes are subclasses of java.lang.Object.
        // Note that superType() == null for interfaces.
        if (info.equals( Type._Object().classInfo() ))
            return true ;

	// A class is a subclass of info if the
	// class's superClass is a subclass of info
	if (superType() != null)
	    if (superType().classInfo().isSubclass( info ))
		return true ;

	// A class is a subclass of info if any of
	// its implemented interfaces are subclasses
	// of info
	for (Type t : impls()) {
	    if (t.classInfo().isSubclass( info ))
		return true ;
	}

	return false ;
    }

    public boolean equals( Object obj ) {
	checkComplete() ;
	if (obj == this)
	    return true ;

	if (!(obj instanceof ClassInfo))
	    return false ;
    
	ClassInfo other = ClassInfo.class.cast( obj ) ;

	if (hashCode() != other.hashCode())
	    return false ;

	if (!thisType().equals( other.thisType() ))
	    return false ;

	if (isInterface() != other.isInterface())
	    return false ;

	if (modifiers() != other.modifiers())
	    return false ;

	if (!name().equals( other.name() ))
	    return false ;

	if (superType() == null) {
	    if (other.superType() != null)
		return false ;
	} else if (!superType().equals( other.superType() ))
	    return false ;

	if (!impls().equals( other.impls()))
	    return false ;

	if (!fieldInfo().entrySet().equals( other.fieldInfo().entrySet()))
	    return false ;

	if (!methodInfoByName().equals(other.methodInfoByName()))
	    return false ;

	if (!constructorInfo().equals(other.constructorInfo()))
	    return false ;

	return true ;
    }

    public String toString() {
	checkComplete() ;
	String className = this.getClass().getName() ;
	int lindex = className.lastIndexOf( '.' ) ;
	if (lindex >= 0)
	    className = className.substring( lindex+1) ;

	return className + "[" + name() + "]" ;
    }

    // Clear the hashCode whenever the methods, constructors, or
    // fields are changed.
    private synchronized void clearHashCode() {
	hashIsCached = false ;
	hashValue = 0 ;
    }

    public synchronized int hashCode() {
	checkComplete() ;
	if (!hashIsCached) {
	    hashValue ^= thisType().hashCode() ;
	    hashValue ^= isInterface() ? 0 : 1 ;
	    hashValue ^= modifiers() ;
	    if (superType() != null)
		hashValue ^= superType().hashCode() ;
	    hashValue ^= impls().hashCode() ;
	    hashValue ^= fieldInfo().hashCode() ;
	    hashValue ^= methodInfoByName().hashCode() ;
	    if (constructorInfo() != null)
		hashValue ^= constructorInfo().hashCode() ;

	    hashIsCached = true ;
	}

	return hashValue ;
    }
}
