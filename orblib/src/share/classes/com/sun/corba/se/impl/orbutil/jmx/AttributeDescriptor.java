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

import javax.management.ReflectionException ;

import java.lang.annotation.Annotation ;

import com.sun.corba.se.spi.orbutil.generic.Algorithms ;

import com.sun.corba.se.spi.orbutil.jmx.ManagedAttribute ;
    
public class AttributeDescriptor {
    public enum AttributeType { SETTER, GETTER } ;

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

    // Attribute method syntax:
    // id = id as present in annotation, or as derived from method
    // Id = id with initial letter capitalized
    // patterns:
    //	Setter:
    //	    void setId( T arg ) ;
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

    // Check whether or not this AttributeDescriptor is applicable to obj.
    public boolean isApplicable( Object obj ) {
        return method.getDeclaringClass().isInstance( obj ) ;
    }

    private void checkType( AttributeType at ) {
        if (at != atype)
            throw new RuntimeException( "Required AttributeType is " + at ) ;
    }

    public Object get( Object obj ) throws ReflectionException {
        checkType( AttributeType.GETTER ) ;

        try {
            return tc.toManagedEntity( method.invoke( obj ) ) ;
        } catch (Exception exc) {
            throw new ReflectionException( exc ) ;
        }
    }

    public void set( Object target, Object value ) throws ReflectionException {
        checkType( AttributeType.SETTER ) ;

        try {
            method.invoke( target, tc.fromManagedEntity( value ) ) ;
        } catch (Exception exc) {
            throw new ReflectionException( exc ) ;
        }
    }

    private static boolean startsWithNotEquals( String str, String prefix ) {
	return str.startsWith( prefix ) && !str.equals( prefix ) ;
    }

    private static String stripPrefix( String str, String prefix ) {
	int prefixLength = prefix.length() ;
	String first = str.substring( prefixLength, prefixLength+1 ).toLowerCase() ;
	if (str.length() == prefixLength + 1) {
	    return first ;
	} else {
	    return first + str.substring( prefixLength + 1 ) ;
	}
    }
    
    /** Find the attribute corresponding to a getter or setter with the given id.
     * Returns null if no such attribute is found.
     */
    public static AttributeDescriptor findAttribute( ManagedObjectManagerInternal mom,
        final ClassAnalyzer ca, final String id, 
        final String description, final AttributeType at ) {

        List<Method> methods = ca.findMethods( 
	    new ClassAnalyzer.Predicate() {
		public boolean evaluate( Object m ) {
                    if (at == AttributeType.GETTER)
                        return isGetter( (Method)m, id ) ;
                    else
                        return isSetter( (Method)m, id ) ;
		}
	    }
	) ;

        if (methods.size() == 0) 
            return null ;

        return new AttributeDescriptor( mom, methods, id, description, at ) ;
    }

    private AttributeDescriptor( ManagedObjectManagerInternal mom, final List<Method> methods, 
        final String id, final String description, final AttributeType at ) {

        this( mom, 
            (at == AttributeType.GETTER) ?
                Algorithms.getFirst( methods, "No getter named " + id + " found" )
            :
                Algorithms.getFirst( methods, "No setter named " + id + " found" ),
            id, description ) ;
    }

    // Handle a method that is NOT annotated with @ManagedAttribute
    public AttributeDescriptor( ManagedObjectManagerInternal mom, Method m, 
        String extId, String description ) {

        this.method = m ;
        this.id = extId ;
        this.description = description ;

        final String name = m.getName() ;
        if (startsWithNotEquals( name, "get" )) {
            if (extId.equals( "" )) {
                id = stripPrefix( name, "get" ) ;
            }

            this.atype = AttributeType.GETTER ;

            if (m.getGenericReturnType() == void.class) 
                throw new IllegalArgumentException( m + " is an illegal getter method" ) ;
            if (m.getGenericParameterTypes().length != 0)
                throw new IllegalArgumentException( m + " is an illegal getter method" ) ;
            this.type = m.getGenericReturnType() ;
        } else if (startsWithNotEquals( name, "set" )) {
            if (extId.equals( "" )) {
                id = stripPrefix( name, "set" ) ;
            }

            this.atype = AttributeType.SETTER ;

            if (m.getGenericReturnType() != void.class) 
                throw new IllegalArgumentException( m + " is an illegal setter method" ) ;
            if (m.getGenericParameterTypes().length != 1 ) 
                throw new IllegalArgumentException( m + " is an illegal setter method" ) ;
            this.type = m.getGenericParameterTypes()[0] ;
        } else if (startsWithNotEquals( name, "is" )) {
            if (extId.equals( "" )) {
                id = stripPrefix( name, "is" ) ;
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
    public AttributeDescriptor( ManagedObjectManagerInternal mom, Method m ) {
        this( mom, m,
            m.getAnnotation( ManagedAttribute.class ).id(),
            m.getAnnotation( ManagedAttribute.class ).description() ) ;
    }

    public static class WrappedException extends RuntimeException {
	public WrappedException( Exception exc ) {
	    super( exc ) ;
	}

	public Exception getCause() {
	    return (Exception)super.getCause() ;
	}
    }
}
