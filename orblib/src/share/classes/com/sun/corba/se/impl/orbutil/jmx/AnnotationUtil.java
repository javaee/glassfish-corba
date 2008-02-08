/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2007-2007 Sun Microsystems, Inc. All rights reserved.
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

package com.sun.corba.se.impl.orbutil.jmx ;

import java.util.Collections ;
import java.util.List ;
import java.util.Set ;
import java.util.HashSet ;
import java.util.Queue ;
import java.util.LinkedList ;
import java.util.ArrayList ;
import java.util.Map ;
import java.util.WeakHashMap ;

import java.lang.reflect.Method ;
import java.lang.reflect.Type ;

import java.lang.annotation.Annotation ;

import com.sun.corba.se.spi.orbutil.generic.UnaryBooleanFunction ;
import com.sun.corba.se.spi.orbutil.generic.UnaryFunction ;
import com.sun.corba.se.spi.orbutil.generic.BinaryVoidFunction ;
import com.sun.corba.se.spi.orbutil.generic.BinaryFunction ;
import com.sun.corba.se.spi.orbutil.generic.Algorithms ;
import com.sun.corba.se.spi.orbutil.generic.Pair ;
import com.sun.corba.se.spi.orbutil.generic.Graph ;

import com.sun.corba.se.spi.orbutil.jmx.ManagedObjectManager ;
import com.sun.corba.se.spi.orbutil.jmx.ManagedObject ;
import com.sun.corba.se.spi.orbutil.jmx.ManagedData ;
import com.sun.corba.se.spi.orbutil.jmx.ManagedAttribute ;
import com.sun.corba.se.spi.orbutil.jmx.ManagedOperation ;
import com.sun.corba.se.spi.orbutil.jmx.InheritedAttribute ;
import com.sun.corba.se.spi.orbutil.jmx.InheritedAttributes ;
import com.sun.corba.se.spi.orbutil.jmx.InheritedTable ;
import com.sun.corba.se.spi.orbutil.jmx.IncludeSubclass ;
    
public abstract class AnnotationUtil {
    // General purpose class analyzer
    //
    // The basic problem is to determine for any class its linerized inheritance
    // sequence.  This is an old problem in OOP.  For my purpose, I want the following
    // to be true:
    //
    // Let C be a class, let C.super be C's superclass, and let C.inter be the list of
    // C's implemented interfaces (C may be an interface, abstract, or concrete class).
    // Define ILIST(C) to be a sequence that satisfies the following properties:
    //
    // 1. ILIST(C) starts with C.
    // 2. If X is in ILIST(C), then so is X.super and each element of X.inter.
    // 3. For any class X in ILIST(C):
    //    2a. X appears before X.super in ILIST(C)
    //    2b. X appears before any X.inter in ILIST(C)
    // 4. No class appears more than once in ILIST(C)
    //
    // Note that the order can change when new classes are analyzed, so each class must be 
    // analyzed independently

    private static Graph.Finder<Class<?>> finder = new Graph.Finder<Class<?>>() {
	public List<Class<?>> evaluate( Class<?> arg ) {
	    List<Class<?>> result = new ArrayList<Class<?>>() ;
	    Class<?> sclass = arg.getSuperclass() ;
	    if (sclass != null) {
		result.add( sclass ) ;
	    }
	    for (Class<?> cls : arg.getInterfaces() ) {
		result.add( cls ) ;
	    }
	    return result ;
	}
    } ;

    private static Map<Class<?>,List<Class<?>>> inheritanceMap =
	new WeakHashMap<Class<?>,List<Class<?>>>() ;

    private static List<Class<?>> makeInheritanceChain( Class<?> cls ) {
	Graph<Class<?>> gr = new Graph( cls, finder ) ;
	List<Class<?>> result = new ArrayList<Class<?>>( gr.getPostorderList() ) ;
	Collections.reverse( result ) ;
	return result ;
    }

    // tested by testGetInheritanceChain
    public static List<Class<?>> getInheritanceChain( Class<?> cls ) {
	List<Class<?>> result = inheritanceMap.get( cls ) ;
	if (result == null) {
	    result = makeInheritanceChain( cls ) ;
	    inheritanceMap.put( cls, result ) ;
	}
	return result ;
    }

    private AnnotationUtil() {}

    // Object evaluate( Object ) 
    public interface Getter extends UnaryFunction<Object,Object> {} ;

    // void evaluate( Object, Object ) 
    public interface Setter extends BinaryVoidFunction<Object,Object> {} ;

    // Object evaluate( Object, List<Object> ) (or Result evaluate( Target, ArgList ))
    public interface Operation extends BinaryFunction<Object,List<Object>,Object> {} ;

    // tested by testFindMethod
    public static Method findMethod( final Class<?> cls, 
	UnaryBooleanFunction<Method> predicate ) {

	List<Class<?>> classes = getInheritanceChain( cls ) ;
	for (Class<?> c : classes) {
	    for (Method m : c.getDeclaredMethods()) {
		if (predicate.evaluate(m)) {
		    return m ;
		}
	    }
	}

	return null ;
    }

    // tested by testGetAnnotatedMethods
    public static List<Method> getAnnotatedMethods( final Class<?> cls, 
	final Class<? extends Annotation> annotation ) {

	final List<Method> result = new ArrayList<Method>() ;

	final List<Class<?>> classes = getInheritanceChain( cls ) ;
	for (Class<?> c : classes) {
	    for (Method m : c.getDeclaredMethods()) {
		Annotation ann = m.getAnnotation( annotation ) ;
		if (ann != null) {
		    result.add( m ) ;
		}
	    }
	}

	return result ;
    }

    // test by testGetClassAnnotations
    public static <T extends Annotation> List<Pair<Class<?>,T>> getClassAnnotations(
	final Class<?> cls, final Class<T> annotation ) {

	final List<Class<?>> classes = getInheritanceChain( cls ) ;

        UnaryFunction<Class<?>,Pair<Class<?>,T>> func = 
	    new UnaryFunction<Class<?>,Pair<Class<?>,T>>() {
		public Pair<Class<?>,T> evaluate( final Class<?> cls ) {
                    return new Pair<Class<?>,T>( cls,
                        cls.getAnnotation(annotation) ) ; 
                }
            } ;
        
        return Algorithms.map( classes, func ) ;
    }

    // Attribute method syntax:
    // id = id as present in annotation, or as derived from method
    // Id = id with initial letter capitalized
    // patterns:
    //	Setter:
    //	    void setId( T arg ) ;
    //
    //
    //	    void id( T arg ) ;
    //	Getter:
    //	    T getId() ;
    //	    T id() ;
    //	    boolean isId() ;
    //	    Boolean isId() ;

    // Tested by testIsSetterIsGetter
    public static boolean isSetter( final Method m, final String id ) {
	Class<?> rt = m.getReturnType() ;
	if (rt != void.class) 
	    return false ;
	if (m.getParameterTypes().length != 1)
	    return false ;

	final String mname = m.getName() ;
	final String initCapId = id.substring(0,1).toUpperCase() + id.substring(1) ;

	if (mname.equals( id ))
	    return true ;

	if (mname.equals( "set" + initCapId ))
	    return true ;

	return false ;
    }

    // Tested by testIsSetterIsGetter
    public static boolean isGetter( final Method m, final String id ) {
	Class<?> rt = m.getReturnType() ;
	if (rt == void.class) 
	    return false ;
	if (m.getParameterTypes().length != 0)
	    return false ;

	final String mname = m.getName() ;
	final String initCapId = id.substring(0,1).toUpperCase() + id.substring(1) ;

	if (mname.equals( id ))
	    return true ;

	if (mname.equals( "get" + initCapId ))
	    return true ;


	if (rt.equals( boolean.class ) || rt.equals( Boolean.class)) {
	    if (mname.equals( "is" + initCapId ))
		return true ;
	}

	return false ;
    }

    public static Method getSetterMethod( final Class<?> cls, final String id ) {
	return findMethod( cls,
	    new UnaryBooleanFunction<Method>() {
		public boolean evaluate( Method m ) {
		    return isSetter( m, id ) ;
		}
	    }
	) ;
    }

    public static Method getGetterMethod( final Class<?> cls, final String id ) {
	return findMethod( cls,
	    new UnaryBooleanFunction<Method>() {
		public boolean evaluate( Method m ) {
		    return isGetter( m, id ) ;
		}
	    }
	) ;
    }

    public enum AttributeType { SETTER, GETTER } ;

    public static final class MethodInfo {
	private Method method ;
	private String id ;
	private String description ;
	private AttributeType atype ;
	private Type type ;
	private TypeConverter tc ;

	public final Method method() { return method ; }
	public final String id() { return id ; }
	public final String description() { return description ; }
	public final AttributeType atype() { return atype ; }
	public final Type type() { return type ; }
	public final TypeConverter tc() { return tc ; }

	// Handle a method that is NOT annotated with @ManagedAttribute
	public MethodInfo( ManagedObjectManagerImpl mom, Method m, String extId, String description ) {
	    this.method = m ;
	    this.id = extId ;
	    this.description = description ;

	    final String name = m.getName() ;
	    if (name.startsWith( "get" )) {
		if (extId.equals( "" )) {
		    id = name.substring( 3,1 ).toLowerCase() + name.substring( 4 ) ;
		} 

		this.atype = AttributeType.GETTER ;

		if (m.getGenericReturnType() == void.class) 
		    throw new IllegalArgumentException( m + " is an illegal setter method" ) ;
		if (m.getGenericParameterTypes().length != 0)
		    throw new IllegalArgumentException( m + " is an illegal getter method" ) ;
		this.type = m.getGenericReturnType() ;
	    } else if (name.startsWith( "set" )) {
		if (extId.equals( "" )) {
		    id = name.substring( 3,1 ).toLowerCase() + name.substring( 4 ) ;
		}

		this.atype = AttributeType.SETTER ;

		if (m.getGenericReturnType() != void.class) 
		    throw new IllegalArgumentException( m + " is an illegal setter method" ) ;
		if (m.getGenericParameterTypes().length != 1 ) 
		    throw new IllegalArgumentException( m + " is an illegal setter method" ) ;
		this.type = m.getGenericParameterTypes()[0] ;
	    } else if (name.startsWith( "is" )) {
		if (extId.equals( "" )) {
		    id = name.substring( 2,1 ).toLowerCase() + name.substring( 3 ) ;
		}

		this.atype = AttributeType.GETTER ;

		if (m.getGenericParameterTypes().length != 0)
		    throw new IllegalArgumentException( m + " is an illegal \"is\" method" ) ;
		this.type = m.getGenericReturnType() ;
		if (!type.equals( boolean.class ) && !type.equals( Boolean.class ))
		    throw new IllegalArgumentException( m + " is an illegal \"is\" method" ) ;
	    } else {
		if (extId.equals( "" )) {
		    id = name ;
		}
		
		Type rtype = m.getGenericReturnType() ;
		Type[] ptypes = m.getGenericParameterTypes() ;
		if (rtype.equals( void.class ) && (ptypes.length == 1)) {
		    this.type = ptypes[0] ;
		    this.atype = AttributeType.SETTER ;
		} else if (!rtype.equals( void.class ) && (ptypes.length == 0)) {
		    this.type = rtype ;
		    this.atype = AttributeType.GETTER ;
		} else {
		    throw new IllegalArgumentException( m + " is not a valid attribute method" ) ;
		}
	    }

	    this.tc = mom.getTypeConverter( this.type ) ;
	}

	// Handle a method with an @ManagedAttribute annotation
	public MethodInfo( ManagedObjectManagerImpl mom, Method m ) {
	    this( mom, m,
		m.getAnnotation( ManagedAttribute.class ).id(),
		m.getAnnotation( ManagedAttribute.class ).description() ) ;
	}
    }

    public static class WrappedException extends RuntimeException {
	public WrappedException( Exception exc ) {
	    super( exc ) ;
	}

	public Exception getCause() {
	    return (Exception)super.getCause() ;
	}
    }

    public static AnnotationUtil.Setter makeSetter( final Method m, final TypeConverter tc ) {
	return new AnnotationUtil.Setter() {
	    public void evaluate( Object target, Object value ) {
		try {
		    m.invoke( target, tc.fromManagedEntity( value ) ) ;
		} catch (Exception exc) {
		    throw new WrappedException( exc ) ;
		}
	    }
	} ;
    }

    public static AnnotationUtil.Getter makeGetter( final Method m, final TypeConverter tc ) {
	return new AnnotationUtil.Getter() {
	    public Object evaluate( Object target ) {
		try { 
		    return tc.toManagedEntity( m.invoke( target ) ) ;
		} catch (Exception exc) {
		    throw new WrappedException( exc ) ;
		}
	    }
	} ;
    }

    public static InheritedAttribute[] getInheritedAttributes( Class<?> cls ) {
	// Check for @InheritedAttribute(s) annotation.  
	// Find methods for these attributes in superclasses. 
	final InheritedAttribute ia = cls.getAnnotation( InheritedAttribute.class ) ;
	final InheritedAttributes ias = cls.getAnnotation( InheritedAttributes.class ) ;
	if ((ia != null) && (ias != null)) 
	    throw new IllegalArgumentException( 
		"Only one of the annotations InheritedAttribute or "
		+ "InheritedAttributes may appear on a class" ) ;

	InheritedAttribute[] iaa = null ;
	if (ia != null)	
	    iaa = new InheritedAttribute[] { ia } ;
	else if (ias != null) 
	    iaa = ias.attributes() ;

	return iaa ;
    }
}
