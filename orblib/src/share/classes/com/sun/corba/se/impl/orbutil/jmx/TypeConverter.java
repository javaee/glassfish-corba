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

import java.lang.reflect.Array ;
import java.lang.reflect.Constructor ;
import java.lang.reflect.Method ;
import java.lang.reflect.Type ;
import java.lang.reflect.ParameterizedType ;
import java.lang.reflect.TypeVariable ;
import java.lang.reflect.WildcardType ;
import java.lang.reflect.GenericArrayType ;

import java.util.Date ;
import java.util.List ;
import java.util.ArrayList ;
import java.util.Map ;
import java.util.HashMap ;

import java.math.BigDecimal ;
import java.math.BigInteger ;

import javax.management.ObjectName ;

import javax.management.openmbean.ArrayType ;
import javax.management.openmbean.OpenType ;
import javax.management.openmbean.OpenDataException ;
import javax.management.openmbean.SimpleType ;
import javax.management.openmbean.TabularType ;
import javax.management.openmbean.CompositeType ;
import javax.management.openmbean.CompositeData ;
import javax.management.openmbean.CompositeDataSupport ;
import javax.management.openmbean.TabularData ;
import javax.management.openmbean.TabularDataSupport ;

import com.sun.corba.se.spi.orbutil.generic.Pair ;

import com.sun.corba.se.spi.orbutil.jmx.ManagedObjectManager ;
import com.sun.corba.se.spi.orbutil.jmx.ManagedObject ;
import com.sun.corba.se.spi.orbutil.jmx.ManagedData ;
import com.sun.corba.se.spi.orbutil.jmx.ManagedAttribute ;
import com.sun.corba.se.spi.orbutil.jmx.ManagedOperation ;
import com.sun.corba.se.spi.orbutil.jmx.InheritedAttribute ;
import com.sun.corba.se.spi.orbutil.jmx.InheritedAttributes ;
import com.sun.corba.se.spi.orbutil.jmx.InheritedTable ;
import com.sun.corba.se.spi.orbutil.jmx.IncludeSubclass ;

/** A ManagedEntity is one of the pre-defined Open MBean types: SimpleType, ObjectName, 
 * TabularData, or CompositeData.
 */
abstract class TypeConverter {
    private static Map<Type,OpenType> simpleTypeMap = new HashMap<Type,OpenType>() ;
    private static Map<OpenType,Type> simpleOpenTypeMap = new HashMap<OpenType,Type>() ;

    private static void initMaps( Type type, OpenType otype ) {
	simpleTypeMap.put( type, otype ) ;
	simpleOpenTypeMap.put( otype, type ) ;
    }

    static {
	// XXX maps are not 1-1: is this a problem?
	initMaps( boolean.class, SimpleType.BOOLEAN ) ;
	initMaps( Boolean.class, SimpleType.BOOLEAN ) ;

	initMaps( char.class, SimpleType.CHARACTER ) ;
	initMaps( Character.class, SimpleType.CHARACTER ) ;

	initMaps( byte.class, SimpleType.BYTE ) ;
	initMaps( Byte.class, SimpleType.BYTE ) ;

	initMaps( short.class, SimpleType.SHORT ) ;
	initMaps( Short.class, SimpleType.SHORT ) ;

	initMaps( int.class, SimpleType.INTEGER ) ;
	initMaps( Integer.class, SimpleType.INTEGER ) ;

	initMaps( long.class, SimpleType.LONG ) ;
	initMaps( Long.class, SimpleType.LONG ) ;

	initMaps( float.class, SimpleType.FLOAT ) ;
	initMaps( Float.class, SimpleType.FLOAT ) ;

	initMaps( double.class, SimpleType.DOUBLE ) ;
	initMaps( Double.class, SimpleType.DOUBLE ) ;

	initMaps( String.class, SimpleType.STRING ) ;
	initMaps( void.class, SimpleType.VOID ) ;

	initMaps( Date.class, SimpleType.DATE ) ;
	initMaps( ObjectName.class, SimpleType.OBJECTNAME ) ;

	initMaps( BigDecimal.class, SimpleType.BIGDECIMAL ) ;
	initMaps( BigInteger.class, SimpleType.BIGINTEGER ) ;
    }

    public static Class getJavaClass( OpenType ot ) {
	if (ot instanceof SimpleType) {
	    SimpleType st = (SimpleType)ot ;
	    return (Class)simpleOpenTypeMap.get( st ) ;
	} else if (ot instanceof ArrayType) {
	    // This code is rather odd.  We need to get the opentype of the array components, convert
	    // that to a java type, and then construct a Java type (Class) that has that java type
	    // as its component (java) type.  I think the only way to do this is to call 
	    // Array.newInstance, and then take the class from the resulting array instance.
	    ArrayType at = (ArrayType)ot ;
	    OpenType cot = at.getElementOpenType() ;
	    Class cjt = getJavaClass( cot ) ;
	    Object temp = Array.newInstance( cjt, 0 ) ;
	    return temp.getClass() ;
	} else if (ot instanceof TabularData) {
	    return TabularData.class ;
	} else if (ot instanceof CompositeData) {
	    return CompositeData.class ;
	} else {
	    throw new IllegalArgumentException( "Unsupported OpenType " + ot ) ;
	}
    }

    public static Class getJavaClass( Type type ) {
	if (type instanceof Class) {
	    return (Class)type ;
	} else if (type instanceof GenericArrayType) {
	    // Same trick as above.
	    GenericArrayType gat = (GenericArrayType)type ;
	    Type ctype = gat.getGenericComponentType() ;
	    Class cclass = getJavaClass( ctype ) ;
	    Object temp = Array.newInstance( cclass, 0 ) ;
	    return temp.getClass() ;
	} else if (type instanceof ParameterizedType) {
	    ParameterizedType pt = (ParameterizedType)type ;
	    Type rpt = pt.getRawType() ;
	    return (Class)rpt ;
	} else {
	    throw new IllegalArgumentException( type + " cannot be converted into a Java class" ) ;
	}
    }

    /** Type mapping rules for OT : Type -> OpenType:
     *  <pre>
     *  Java Type			Open Type
     *  -----------------------------------------
     *  boolean, Boolean		BOOLEAN
     *  char, Character			CHARACTER
     *  byte, Byte			BYTE
     *  short, Short			SHORT
     *  int, Integer			INTEGER
     *  long, LONG			LONG
     *  float, Float			FLOAT	
     *  double, Double			DOUBLE
     *  String				STRING
     *  void				VOID
     *  java.util.Date			DATE
     *  javax.management.ObjectName	OBJECTNAME
     *  java.math.BigDecimal		BIGDECIMAL
     *  java.math.BigInteger		BIGINTEGER
     *  (all of the above types have the same JT and OT representation.
     *  
     *  Enumeration type		String (only valid values are keywords)
     *
     *  @ManagedObject			ObjectName
     *
     *  @ManagedData			CompositeType( 
     *					    @ManagedData.name, 
     *					    @ManagedData.description, 
     *					    ALL @ManagedAttribute ID, // extract from the method name
     *					    ALL @ManagedAttribute.description, 
     *					    ALL @ManagedAttribute OT(Type) ) // extracted from the 
     *						method attribute TYPE
     *					We also need to include @IncludeSubclass and 
     *					@InheritedAttribute(s) attributes
     *					Also note that @InheritTable adds an attribute to the class.  
     *					The inherited table is mapped to TabularData.  
     *					The InheritTable annotation specifies a class X, which must
     *					be part of the Type C<X>, where C is a subclass of Collection.  
     *					The TabularData contains elements of type OT(X).
     *					JT -> OT: invoke attribute methods, collect results, 
     *					    construct CompositeDataSupport
     *					OT -> JT: NOT SUPPORTED (for now)
     *
     *	C<X>, C a subtype of
     *	    Collection			Mapped to array data containing OT(X) 
     *					Also need to handle C isA Iterable
     *					JT -> OT: Construct ArrayType of type OT(X), 
     *					    fill it with all JT elements as mapped to their OT type
     *					OT -> JT: NOT SUPPORTED 
     *					    (unless CompositeData -> Java type is supported)
     *
     *	M<K,V>, M a subtype of		Mapped to tabular data containing id="key" OT(K) as the key 
     *	    Map				and id="value" OT(V) (follow the JDK 6 MXBeans rules for 
     *					Map vs. SortedMap here) 
     *	    
     *	X[]				OT(X)[]
     *					    As for CompositeData, we will mostly treat this as readonly.
     *					Mappings same as C<X> case.
     *
     *	TypeVariable			
     *	WildCardType			Both need to be supported.  The basic idea is that the upper bounds
     *					give us information on what interfaces must be implemented by 
     *					by an instance of the type variable.  This in turn tells us what
     *					subclasses identified by @IncludeSubclass should be used when 
     *					mapping the actual data to a CompositeData instance.
     *					Not supported: lower bounds (what would that mean?  Do I need it?)
     *						       multiple upper bounds (I don't think I need this?
     *					Multiple upper bounds PROBABLY means the intersection of the 
     *					included subclasses for all of the bounds
     *
     *	Other				String
     *					    JT -> OT: use toString
     *					    OT -> JT: requires a <init>(String) constructor
     *
     * XXX How much do we support the OpenType -> JavaType mapping?  This is mainly a question for
     * CompositeData.  Note that CompositeData is immutable, so all @ManagedAttributes in 
     * CompositeData must be getters.  I think this would mainly apply to a setter in an MBean whose 
     * type maps to CompositeData.
     * For now, we will ignore this, because I think all CompositeData types in the ORB will be read only.
     * If this is NOT the case, we can adopt a solution similar to the MXBean @ConstructorProperties.
     */
    public static TypeConverter makeTypeConverter( Type type, ManagedObjectManagerImpl mom ) {
	OpenType stype = simpleTypeMap.get( type ) ;
	if (stype != null) {
	    return handleSimpleType( (Class)type, mom, stype ) ;
	}

	if (type instanceof Class) {
	    Class<?> cls = (Class<?>)type ;
	    ManagedObject mo = cls.getAnnotation( ManagedObject.class ) ;
	    ManagedData md = cls.getAnnotation( ManagedData.class ) ;

	    if (mo != null) {
		return handleManagedObject( cls, mom, mo ) ;
	    } else if (md != null) {
		return handleManagedData( cls, mom, md ) ;
	    } else {
		// map to string
		return handleAsString( cls, mom ) ;
	    }

	    // XXX still need to handle enums
	} 
	
	if (type instanceof ParameterizedType) {
	    return handleParameterizedType( (ParameterizedType)type, mom ) ;
	} 
	
	if (type instanceof GenericArrayType) {
	    return handleArrayType( (GenericArrayType)type, mom ) ;
	} 
	
	if (type instanceof TypeVariable) {
	    throw new IllegalArgumentException( "TypeVariable " + type + " not supported" ) ;
	} 
	
	if (type instanceof WildcardType) {
	    throw new IllegalArgumentException( "WildcardType " + type + " not supported" ) ;
	} else {
	    // this should not happen
	    throw new IllegalArgumentException( "Unknown kind of Type " + type ) ;
	}
    }

    private static TypeConverter handleManagedObject( final Class type, 
	final ManagedObjectManagerImpl mom, ManagedObject mo ) {

	return new TypeConverter() {
	    Type getDataType() {
		return type ;
	    }

	    OpenType getManagedType() {
		return SimpleType.OBJECTNAME ;
	    }

	    Object toManagedEntity( Object obj ) {
		return mom.getObjectName( obj ) ;
	    }

	    Object fromManagedEntity( Object entity ) {
		if (!(entity instanceof ObjectName))
		    throw new IllegalArgumentException( 
			"Management entity " + entity + " is not an ObjectName" ) ;

		ObjectName oname = (ObjectName)entity ;
		return mom.getObject( oname ) ;
	    }

	    boolean isIdentity() {
		return false ; 
	    }
	} ;
    }

    private static TypeConverter handleArrayType( final GenericArrayType type, 
	final ManagedObjectManagerImpl mom ) {

	final Type ctype = type.getGenericComponentType() ;
	final TypeConverter ctypeTc = mom.getTypeConverter( ctype ) ;
	final OpenType cotype = ctypeTc.getManagedType() ;
	OpenType ot = null ;
	
	try {
	    ot = new ArrayType( 1, cotype ) ;
	} catch (OpenDataException exc) {
	    throw new IllegalArgumentException( "Arrays of arrays not support: " + cotype, exc ) ;
	}

	final OpenType myManagedType = ot ;

	return new TypeConverter() {
	    Type getDataType() {
		return type ;
	    }

	    OpenType getManagedType() {
		return myManagedType ;
	    }

	    Object toManagedEntity( Object obj ) {
		if (isIdentity()) {
		    return obj ;
		} else {
		    Class cclass = getJavaClass( ctype ) ;
		    int length = Array.getLength( obj ) ;
		    Object result = Array.newInstance( cclass, length ) ;
		    for (int ctr=0; ctr<length; ctr++) {
			Object elem = Array.get( obj, ctr ) ;
			Object relem =  ctypeTc.toManagedEntity( elem ) ;
			Array.set( result, ctr, relem ) ;
		    }

		    return result ;
		}
	    }

	    Object fromManagedEntity( Object entity ) {
		if (isIdentity()) {
		    return entity ;
		} else {
		    Class cclass = getJavaClass( cotype ) ;

		    int length = Array.getLength( entity ) ;
		    Object result = Array.newInstance( cclass, length ) ;
		    for (int ctr=0; ctr<length; ctr++) {
			Object elem = Array.get( entity, ctr ) ;
			Object relem =  ctypeTc.fromManagedEntity( elem ) ;
			Array.set( result, ctr, relem ) ;
		    }

		    return result ;
		}
	    }

	    boolean isIdentity() {
		return ctypeTc.isIdentity() ; 
	    }
	} ;
    }

    private static TypeConverter handleAsString( final Class cls, 
	final ManagedObjectManagerImpl mom ) {

	Constructor cs = null ;
	try {
	    cs = cls.getDeclaredConstructor( String.class ) ;
	} catch (Exception exc) {
	    throw new IllegalArgumentException( 
		"Error in obtaining (String) constructor for " 
		+ cls, exc ) ;
	}
	final Constructor cons = cs ;

	return new TypeConverter() {
	    Type getDataType() {
		return cls ;
	    }

	    OpenType getManagedType() {
		return SimpleType.STRING ;
	    }

	    Object toManagedEntity( Object obj ) {
		return obj.toString() ;
	    }

	    Object fromManagedEntity( Object entity ) {
		if (cons != null) {
		    try {
			String str = (String)entity ;
			return cons.newInstance( str ) ;
		    } catch (Exception exc) {
			throw new IllegalArgumentException( 
			    "Error in converting from String to " 
			    + cls, exc ) ;
		    }
		} else {
		    throw new UnsupportedOperationException( 
			"There is no <init>(String) constructor available to convert a String into a " 
			+ cls ) ;
		}
	    }

	    boolean isIdentity() {
		return false ; 
	    }
	} ;
    }

    private static TypeConverter handleSimpleType( final Class cls, 
	final ManagedObjectManagerImpl mom, final OpenType stype ) {

	return new TypeConverter() {
	    Type getDataType() {
		return cls ;
	    }

	    OpenType getManagedType() {
		return stype ;
	    }

	    Object toManagedEntity( Object obj ) {
		return obj ;
	    }

	    Object fromManagedEntity( Object entity ) {
		return entity ;
	    }

	    boolean isIdentity() {
		return true ; 
	    }
	} ;
    }
    
    public static List<AnnotationUtil.MethodInfo> analyzeManagedData( final Class<?> cls, 
	final ManagedObjectManagerImpl mom ) {
	
	List<AnnotationUtil.MethodInfo> minfos = new ArrayList<AnnotationUtil.MethodInfo>() ;

	InheritedAttribute[] ias = AnnotationUtil.getInheritedAttributes( cls ) ;
	
	if (ias != null) {
	    for (InheritedAttribute attr : ias) {
		String name = attr.id() ;
		String desc = attr.description() ;
		
		// Search for methods implementing this attribute in the superclasses of this class.
		Method getter = AnnotationUtil.getGetterMethod( cls.getSuperclass(), name ) ;

		AnnotationUtil.MethodInfo minfo = 
		    new AnnotationUtil.MethodInfo( mom, getter, name, desc ) ;
		minfos.add( minfo ) ;
	    }
	}
	
	// Check for @InheritedTable annotation.
	final InheritedTable it = cls.getAnnotation( InheritedTable.class ) ;
	if (it != null) {
	    // XXX process it
	}

	// Check for @IncludeSubclass annotation.  Scan subclasses for attributes.
	final IncludeSubclass is = cls.getAnnotation( IncludeSubclass.class ) ;
	if (is != null) {
	    // XXX process is
	}
	
	// Scan for all methods annotated with @ManagedAttribute, including inherited methods.
	// Construct tables Map<String,Method> for getters (no setters in CompositeData, since
	// CompositeData is immutable).
	// Get open types for getter type
	// Construct OpenMBeanAttributeInfos and actual methods, and put into CompositeData
	final List<Method> attributes = AnnotationUtil.getAnnotatedMethods( cls, ManagedAttribute.class ) ;

	for (Method m : attributes) {
	    AnnotationUtil.MethodInfo minfo = new AnnotationUtil.MethodInfo( mom, m ) ;

	    if (minfo.atype() == AnnotationUtil.AttributeType.GETTER) {
		minfos.add( minfo ) ;
	    } else {
		throw new IllegalArgumentException( "Method " + m 
		    + " is an illegal setter in a @ManagedData class" ) ;
	    }
	}

	return minfos ;
    }

    private static TypeConverter handleManagedData( final Class cls, 
	final ManagedObjectManagerImpl mom, final ManagedData md ) {

	final List<AnnotationUtil.MethodInfo> minfos = analyzeManagedData(
	    cls, mom ) ;

	String name = md.name() ;
	if (name.equals( "" ))
	    name = cls.getName() ;

	final String mdDescription = md.description() ;

	final int length = minfos.size() ;
	final String[] attrNames = new String[ length ] ;
	final String[] attrDescriptions = new String[ length ] ;
	final OpenType[] attrOTypes = new OpenType[ length ] ;

	int ctr = 0 ;
	for (AnnotationUtil.MethodInfo minfo : minfos) {
	    attrNames[ctr] = minfo.id() ;
	    attrDescriptions[ctr] = minfo.description() ;
	    attrOTypes[ctr] = mom.getTypeConverter( minfo.type() ).getManagedType() ;
	    ctr++ ;
	}

	CompositeType ot = null ;
	try {
	    ot = new CompositeType( 
		name, mdDescription, attrNames, attrDescriptions, attrOTypes ) ;
	} catch (OpenDataException exc) {
	    throw new IllegalArgumentException( exc ) ;
	}
	final CompositeType myType = ot ;

	return new TypeConverter() {
	    Type getDataType() {
		return cls ;
	    }

	    OpenType getManagedType() {
		return myType ;
	    }

	    Object toManagedEntity( Object obj ) {
		Map<String,Object> data = new HashMap<String,Object>() ;
		for (AnnotationUtil.MethodInfo minfo : minfos ) {
		    Method method = minfo.method() ;
		    TypeConverter tc = minfo.tc() ;
		    Object ores = null ;
		    try {
			ores = method.invoke( obj ) ;
		    } catch (Exception exc) {
			throw new RuntimeException( exc ) ;
		    }

		    Object res = tc.toManagedEntity( ores ) ;
		    data.put( minfo.id(), res ) ;
		}

		try {
		    return new CompositeDataSupport( myType, data ) ;
		} catch (OpenDataException exc) {
		    throw new IllegalArgumentException( exc ) ;
		}
	    }

	    Object fromManagedEntity( Object entity ) {
		throw new UnsupportedOperationException(
		    "We do not support converting CompositeData back into Java objects" ) ;
	    }

	    boolean isIdentity() {
		return false ; 
	    }
	} ;
    }

    private static TypeConverter handleParameterizedType( final ParameterizedType type, 
	final ManagedObjectManagerImpl mom ) {

	return new TypeConverter() {
	    Type getDataType() {
		return type ;
	    }

	    OpenType getManagedType() {
		// XXX implement me
		return null ;
	    }

	    Object toManagedEntity( Object obj ) {
		// XXX implement me
		return null ;
	    }

	    Object fromManagedEntity( Object entity ) {
		throw new UnsupportedOperationException(
		    "We do not support converting TabularData back into Java objects" ) ;
	    }

	    boolean isIdentity() {
		return false ; 
	    }
	} ;
    }

    /** Java generic type of attribute in problem-domain Object.
     */
    abstract Type getDataType() ;

    /** Open MBeans Open Type for management domain object.
     */
    abstract OpenType getManagedType() ;

    /** Convert from a problem-domain Object obj to a ManagedEntity.
     */
    abstract Object toManagedEntity( Object obj ) ;

    /** Convert from a ManagedEntity to a problem-domain Object.
     */
    abstract Object fromManagedEntity( Object entity ) ;

    /** Returns true if this TypeConverter is an identity transformation.
     */
    abstract boolean isIdentity() ;
}

