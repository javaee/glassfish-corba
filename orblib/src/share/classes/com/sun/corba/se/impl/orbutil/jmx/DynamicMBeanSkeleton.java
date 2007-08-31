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

import java.util.List ;
import java.util.Iterator ;
import java.util.ArrayList ;
import java.util.Arrays ;
import java.util.Map ;
import java.util.HashMap ;
import java.util.Set ;
import java.util.HashSet ;

import java.lang.reflect.Method ;
import java.lang.reflect.Type ;

import java.lang.annotation.Annotation ;

import javax.management.Attribute ;
import javax.management.AttributeList ;
import javax.management.MBeanException ;
import javax.management.InvalidAttributeValueException ;
import javax.management.AttributeNotFoundException ;
import javax.management.ReflectionException ;
import javax.management.MBeanInfo ;
import javax.management.MBeanAttributeInfo ;
import javax.management.MBeanConstructorInfo ;
import javax.management.MBeanOperationInfo ;
import javax.management.MBeanNotificationInfo ;
import javax.management.MBeanOperationInfo ;
import javax.management.MBeanParameterInfo ;

import javax.management.openmbean.OpenMBeanInfo ;
import javax.management.openmbean.OpenMBeanInfoSupport ;
import javax.management.openmbean.OpenMBeanAttributeInfo ;
import javax.management.openmbean.OpenMBeanAttributeInfoSupport ;
import javax.management.openmbean.OpenMBeanOperationInfo ;
import javax.management.openmbean.OpenMBeanOperationInfoSupport ;
import javax.management.openmbean.OpenMBeanParameterInfo ;
import javax.management.openmbean.OpenMBeanParameterInfoSupport ;

import com.sun.corba.se.spi.orbutil.generic.Pair ;
import com.sun.corba.se.spi.orbutil.generic.UnaryFunction ;
import com.sun.corba.se.spi.orbutil.generic.UnaryBooleanFunction ;
import com.sun.corba.se.spi.orbutil.generic.BinaryVoidFunction ;
import com.sun.corba.se.spi.orbutil.generic.BinaryFunction ;

import com.sun.corba.se.spi.orbutil.jmx.ManagedObjectManager ;
import com.sun.corba.se.spi.orbutil.jmx.ManagedObject ;
import com.sun.corba.se.spi.orbutil.jmx.ManagedData ;
import com.sun.corba.se.spi.orbutil.jmx.ManagedAttribute ;
import com.sun.corba.se.spi.orbutil.jmx.ManagedOperation ;
import com.sun.corba.se.spi.orbutil.jmx.InheritedAttribute ;
import com.sun.corba.se.spi.orbutil.jmx.InheritedAttributes ;
import com.sun.corba.se.spi.orbutil.jmx.InheritedTable ;
import com.sun.corba.se.spi.orbutil.jmx.IncludeSubclass ;

// XXX What about open constructors and notifications?  Do we need them?
//
// XXX How do we handle @IncludeSubclass?  I think we should just directly add ALL
// of the attributes and operations to the same structures (we'll need to have a
// restricted find/filter for that purpose, and possibly re-factor some code).
// Then we need to check (possibly in the Getter/Setter/Operation itself) whether or
// not that method is applicable to the target object: if not, return null (Getter),
// do nothing (Setter), or throw an exception? (Operation).
class DynamicMBeanSkeleton {
    private String type ;
    private MBeanInfo mbInfo ;
    private ManagedObjectManagerImpl mom ;
    private Map<String,AnnotationUtil.Setter> setters ;
    private Map<String,AnnotationUtil.Getter> getters ;
    private Map<String,Map<List<String>,AnnotationUtil.Operation>> operations ;
 
    // This method should only be called when getter.id.equals( setter.id ) 
    public void processAttribute( List<OpenMBeanAttributeInfo> list, 
	AnnotationUtil.MethodInfo getter, AnnotationUtil.MethodInfo setter ) {

	String name = getter.id() ;

	if ((setter != null) && (getter != null))
	    if (!setter.type().equals( getter.type() ))
		throw new IllegalArgumentException( 
		    "Getter and setter types do not match for inherited attribute " + name ) ;

	TypeConverter tc = mom.getTypeConverter( getter.type() ) ;

	OpenMBeanAttributeInfo ainfo = new OpenMBeanAttributeInfoSupport( name, 
	    getter.description(), tc.getManagedType(), 
	    getter != null, setter != null, false ) ;

	if (setter != null) {
	    AnnotationUtil.Setter setterFunction = AnnotationUtil.makeSetter( setter.method(), tc ) ;
	    setters.put( name, setterFunction ) ;
	}

	if (getter != null) {
	    AnnotationUtil.Getter getterFunction = AnnotationUtil.makeGetter( getter.method(), tc ) ;
	    getters.put( name, getterFunction ) ;
	}

	list.add( ainfo ) ;
    }

    public Pair<AnnotationUtil.Operation,OpenMBeanOperationInfo> makeOperation( final Method m ) {
	ManagedOperation mo = m.getAnnotation( ManagedOperation.class ) ;
	final String desc = mo.description() ;
	final Type rtype = m.getGenericReturnType() ;
	final TypeConverter rtc = rtype == null ? null : mom.getTypeConverter( rtype ) ;
	final Type[] atypes = m.getGenericParameterTypes() ;
	final List<TypeConverter> atcs = new ArrayList<TypeConverter>() ;
	for (Type type : atypes) {
	    atcs.add( mom.getTypeConverter( type ) ) ;
	}

	final AnnotationUtil.Operation oper = new AnnotationUtil.Operation() {
	    public Object evaluate( Object target, List<Object> args ) {
		try {
		    Object[] margs = new Object[args.size()] ;
		    Iterator<Object> argsIterator = args.iterator() ;
		    Iterator<TypeConverter> tcIterator = atcs.iterator() ;
		    int ctr = 0 ;
		    while (argsIterator.hasNext() && tcIterator.hasNext()) {
			final Object arg = argsIterator.next() ;
			final TypeConverter tc = tcIterator.next() ;
			margs[ctr++] = tc.fromManagedEntity( arg ) ;
		    }

		    Object result = m.invoke( target, margs ) ;

		    if (rtc == null)
			return null ;
		    else
			return rtc.toManagedEntity( result ) ;
		} catch (Exception exc) {
		    throw new AnnotationUtil.WrappedException( exc ) ;
		}
	    }
	} ;

	final OpenMBeanParameterInfo[] paramInfo = new OpenMBeanParameterInfo[ atcs.size() ] ;
	int ctr = 0 ;
	for (TypeConverter tc : atcs) {
	    paramInfo[ctr++] = new OpenMBeanParameterInfoSupport( 
		"arg" + ctr, "", tc.getManagedType() ) ;
	}

	// XXX Note that impact is always set to ACTION_INFO here.  If this is useful to set
	// in general, we need to add impact to the ManagedOperation annotation.
	final OpenMBeanOperationInfo operInfo = new OpenMBeanOperationInfoSupport( m.getName(),
	    desc, paramInfo, rtc.getManagedType(), MBeanOperationInfo.ACTION_INFO ) ;

	return new Pair<AnnotationUtil.Operation,OpenMBeanOperationInfo>( oper, operInfo ) ;
    }

    public void processOperation( List<OpenMBeanOperationInfo> list, Method m ) {
	final Pair<AnnotationUtil.Operation,OpenMBeanOperationInfo> data = makeOperation( m ) ;
	final OpenMBeanOperationInfo info = data.second() ;
	
	final List<String> dataTypes = new ArrayList<String>() ;
	for (MBeanParameterInfo pi : info.getSignature()) {
	    dataTypes.add( pi.getType() ) ;
	}
	
	Map<List<String>,AnnotationUtil.Operation> map = operations.get( m.getName() ) ;
	if (map == null) {
	    map = new HashMap<List<String>,AnnotationUtil.Operation>() ;
	    operations.put( m.getName(), map ) ;
	}

	// XXX we might want to check and see if this was previously defined
	map.put( dataTypes, data.first() ) ;

	list.add( info ) ;
    }

    public DynamicMBeanSkeleton( final Class<?> cls, final ManagedObjectManagerImpl mom ) {
	// This constructor analyzes the structure of cls.  It uses TypeConverters as necessary,
	// updating the mapping based on the results of the analysis of cls.

	this.mom = mom ;

	// Get the @ManagedObject annotation.  This gives us the type and the description.
	final ManagedObject mo = cls.getAnnotation( ManagedObject.class ) ;
	if (mo == null)
	    throw new IllegalArgumentException( 
		"Class " + cls 
		+ " does not have an @ManagedObject annotation: cannot construct dynamic MBean" ) ;

	type = mo.type() ;
	final String moDescription = mo.description() ;
	final List<OpenMBeanAttributeInfo> mbeanAttributeInfoList = 
	    new ArrayList<OpenMBeanAttributeInfo>() ;
	final List<OpenMBeanOperationInfo> mbeanOperationInfoList = 
	    new ArrayList<OpenMBeanOperationInfo>() ;

	// Check for @InheritedAttribute(s) annotation.  Find methods for these attributes in superclasses. 
	final InheritedAttribute[] iaa = AnnotationUtil.getInheritedAttributes( cls ) ;
	
	if (iaa != null) {
	    for (InheritedAttribute attr : iaa) {
		String name = attr.id() ;
		String desc = attr.description() ;

		// Search for methods implementing this attribute in the superclasses of this class.
		Method setter = AnnotationUtil.getSetterMethod( cls.getSuperclass(), name ) ;
		AnnotationUtil.MethodInfo setterInfo = new AnnotationUtil.MethodInfo( 
		    mom, setter, name, desc ) ;

		Method getter = AnnotationUtil.getGetterMethod( cls.getSuperclass(), name ) ;
		AnnotationUtil.MethodInfo getterInfo = new AnnotationUtil.MethodInfo( 
		    mom, getter, name, desc ) ;

		processAttribute( mbeanAttributeInfoList, getterInfo, setterInfo ) ;
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
	// Construct tables Map<String,Method> for getters and setters
	// Check types for getters and setters: must be the same
	// Get open types for getter/setter type
	// Construct OpenMBeanAttributeInfos and actual methods, and put into skeleton
	final List<Method> attributes = AnnotationUtil.getAnnotatedMethods( 
	    cls, ManagedAttribute.class ) ;
	final Map<String,AnnotationUtil.MethodInfo> getters = 
	    new HashMap<String,AnnotationUtil.MethodInfo>() ;
	final Map<String,AnnotationUtil.MethodInfo> setters = 
	    new HashMap<String,AnnotationUtil.MethodInfo>() ;

	for (Method m : attributes) {
	    AnnotationUtil.MethodInfo minfo = new AnnotationUtil.MethodInfo( mom, m ) ;

	    if (minfo.atype() == AnnotationUtil.AttributeType.GETTER) {
		getters.put( minfo.id(), minfo ) ;
	    } else {
		setters.put( minfo.id(), minfo ) ;
	    }
	}

	final Set<String> setterNames = new HashSet<String>( setters.keySet() ) ;
	for (String str : getters.keySet()) {
	    processAttribute( mbeanAttributeInfoList, getters.get( str ), setters.get( str ) ) ;
	    setterNames.remove( str ) ;
	}

	// Handle setters without getters
	for (String str : setterNames) {
	    processAttribute( mbeanAttributeInfoList, null, setters.get( str ) ) ;
	}

	// Scan for all methods annotation with @ManagedOperation, including inherited methods.
	final List<Method> operations = AnnotationUtil.getAnnotatedMethods( 
	    cls, ManagedOperation.class ) ;
	for (Method m : operations) {
	    processOperation( mbeanOperationInfoList, m ) ;
	}

	OpenMBeanAttributeInfo[] attrInfos = mbeanAttributeInfoList.toArray( 
	    new OpenMBeanAttributeInfo[mbeanAttributeInfoList.size()] ) ;
	OpenMBeanOperationInfo[] operInfos = mbeanOperationInfoList.toArray(
	    new OpenMBeanOperationInfo[mbeanOperationInfoList.size() ] ) ;
	OpenMBeanInfo mbeanInfo = new OpenMBeanInfoSupport( 
	    cls.getName(), moDescription, attrInfos, null, operInfos, null ) ;
    }

    public String getType() {
	return type ;
    }

    public Object getAttribute( Object obj, String name) throws AttributeNotFoundException,
	MBeanException, ReflectionException {

	AnnotationUtil.Getter getter = getters.get( name ) ;
	if (getter == null)
	    throw new AttributeNotFoundException( "Could not find attribute " + name ) ;

	try {
	    return getter.evaluate( obj ) ;
	} catch (AnnotationUtil.WrappedException exc) {
	    throw new ReflectionException( exc.getCause() ) ;
	}
    }
    
    public void setAttribute(Object obj, Attribute attribute) throws AttributeNotFoundException,
	InvalidAttributeValueException, MBeanException, ReflectionException  {

	String name = attribute.getName() ;
	Object value = attribute.getValue() ;
	AnnotationUtil.Setter setter = setters.get( name ) ;
	if (setter == null)
	    throw new AttributeNotFoundException( "Could not find writable attribute " + name ) ;

	try {
	    setter.evaluate( obj, value ) ;
	} catch (AnnotationUtil.WrappedException exc) {
	    throw new ReflectionException( exc.getCause() ) ;
	} 
    }
        
    public AttributeList getAttributes( Object obj, String[] attributes) {
	AttributeList result = new AttributeList() ;
	for (String str : attributes) {
	    Object value = null ;
	    try {
		value = getAttribute( obj, str ) ;
	    } catch (Exception exc) {
		throw new IllegalArgumentException( exc ) ;
	    }

	    Attribute attr = new Attribute( str, value ) ;
	    result.add( attr ) ;
	}
	return result ;
    }
        
    public AttributeList setAttributes( Object obj, AttributeList attributes) {
	for (Object attr : attributes) {
	    try {
		setAttribute( obj, (Attribute)attr ) ;
	    } catch (Exception exc) {
		throw new IllegalArgumentException( exc ) ;
	    }
	}
	return attributes ;
    }
    
    public Object invoke( Object obj, String actionName, Object params[], String signature[])
	throws MBeanException, ReflectionException  {

	Map<List<String>,AnnotationUtil.Operation> opMap = operations.get( actionName ) ;
	if (opMap == null)
	    throw new IllegalArgumentException( "Could not find operation named " + actionName ) ;

	List<String> sig = Arrays.asList( signature ) ;
	AnnotationUtil.Operation op = opMap.get( sig ) ;
	if (op == null)
	    throw new IllegalArgumentException( "Could not find operation named " + actionName 
		+ " with signature " + sig ) ;

	try {
	    return op.evaluate( obj, Arrays.asList( params ) ) ;
	} catch (AnnotationUtil.WrappedException exc) {
	    throw new ReflectionException( exc.getCause() ) ;
	}
    }
    
    public MBeanInfo getMBeanInfo() {
	return mbInfo ;
    }
}

