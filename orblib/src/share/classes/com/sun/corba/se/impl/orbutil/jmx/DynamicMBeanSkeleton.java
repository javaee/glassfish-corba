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
import com.sun.corba.se.spi.orbutil.generic.BinaryFunction ;

import com.sun.corba.se.spi.orbutil.jmx.ManagedObject ;
import com.sun.corba.se.spi.orbutil.jmx.ManagedData ;
import com.sun.corba.se.spi.orbutil.jmx.ManagedAttribute ;
import com.sun.corba.se.spi.orbutil.jmx.ManagedOperation ;
import com.sun.corba.se.spi.orbutil.jmx.InheritedAttribute ;
import com.sun.corba.se.spi.orbutil.jmx.InheritedAttributes ;
import com.sun.corba.se.spi.orbutil.jmx.IncludeSubclass ;

// XXX What about open constructors and notifications?  Do we need them?
class DynamicMBeanSkeleton {
    // Object evaluate( Object, List<Object> ) (or Result evaluate( Target, ArgList ))
    public interface Operation extends BinaryFunction<Object,List<Object>,Object> {} ;

    private String type ;
    private final MBeanInfo mbInfo ;
    private final ManagedObjectManagerInternal mom ;
    private final Map<String,AttributeDescriptor> setters ;
    private final Map<String,AttributeDescriptor> getters ;
    private final Map<String,Map<List<String>,Operation>> operations ;
    private final List<OpenMBeanAttributeInfo> mbeanAttributeInfoList ; 
    private final List<OpenMBeanOperationInfo> mbeanOperationInfoList ; 
 
    // This method should only be called when getter.id.equals( setter.id ) 
    private void processAttribute( AttributeDescriptor getter, 
        AttributeDescriptor setter ) {

        if ((setter == null) && (getter == null))
            throw new IllegalArgumentException(
                "At least one of getter and setter must not be null" ) ;

	if ((setter != null) && (getter != null))
	    if (!setter.type().equals( getter.type() ))
		throw new IllegalArgumentException( 
		    "Getter and setter types do not match" ) ;

        AttributeDescriptor nonNullDescriptor = (getter != null) ? getter : setter ;

        String name = nonNullDescriptor.id() ;
        String description = nonNullDescriptor.description() ;
        TypeConverter tc = mom.getTypeConverter( nonNullDescriptor.type() ) ;

	OpenMBeanAttributeInfo ainfo = new OpenMBeanAttributeInfoSupport( name, 
	    description, tc.getManagedType(), 
	    getter != null, setter != null, false ) ;

	mbeanAttributeInfoList.add( ainfo ) ;
    }

    private void analyzeInheritedAttributes( Class<?> annotatedClass, ClassAnalyzer ca ) {
	// Check for @InheritedAttribute(s) annotation.  
        // Find methods for these attributes in superclasses. 
	final InheritedAttribute[] iaa = AnnotationUtil.getInheritedAttributes( annotatedClass ) ;
	if (iaa != null) {
	    for (InheritedAttribute attr : iaa) {
		AttributeDescriptor setterInfo = AttributeDescriptor.findAttribute( 
                    mom, ca, attr.id(), attr.description(), 
                    AttributeDescriptor.AttributeType.SETTER ) ; 

		AttributeDescriptor getterInfo = AttributeDescriptor.findAttribute( 
                    mom, ca, attr.id(), attr.description(), 
                    AttributeDescriptor.AttributeType.GETTER ) ; 

		processAttribute( getterInfo, setterInfo ) ;
	    }
	}
    }

    private void analyzeAnnotatedAttributes( ClassAnalyzer ca ) {
	final List<Method> attributes = ca.findMethods( ca.forAnnotation( ManagedAttribute.class ) ) ;

	for (Method m : attributes) {
	    AttributeDescriptor minfo = new AttributeDescriptor( mom, m ) ;

	    if (minfo.atype() == AttributeDescriptor.AttributeType.GETTER) {
		getters.put( minfo.id(), minfo ) ;
	    } else {
		setters.put( minfo.id(), minfo ) ;
	    }
	}

	final Set<String> setterNames = new HashSet<String>( setters.keySet() ) ;
	for (String str : getters.keySet()) {
	    processAttribute( getters.get( str ), setters.get( str ) ) ;
	    setterNames.remove( str ) ;
	}

	// Handle setters without getters
	for (String str : setterNames) {
	    processAttribute( null, setters.get( str ) ) ;
	}
    }

    private Pair<Operation,OpenMBeanOperationInfo> makeOperation( final Method m ) {
	ManagedOperation mo = m.getAnnotation( ManagedOperation.class ) ;
	final String desc = mo.description() ;
	final Type rtype = m.getGenericReturnType() ;
	final TypeConverter rtc = rtype == null ? null : mom.getTypeConverter( rtype ) ;
	final Type[] atypes = m.getGenericParameterTypes() ;
	final List<TypeConverter> atcs = new ArrayList<TypeConverter>() ;
	for (Type type : atypes) {
	    atcs.add( mom.getTypeConverter( type ) ) ;
	}

	final Operation oper = new Operation() {
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
		    throw new RuntimeException( exc ) ;
		}
	    }
	} ;

	final OpenMBeanParameterInfo[] paramInfo = new OpenMBeanParameterInfo[ atcs.size() ] ;
	int ctr = 0 ;
	for (TypeConverter tc : atcs) {
	    paramInfo[ctr++] = new OpenMBeanParameterInfoSupport( 
		"arg" + ctr, desc, tc.getManagedType() ) ;
	}

	// XXX Note that impact is always set to ACTION_INFO here.  If this is useful to set
	// in general, we need to add impact to the ManagedOperation annotation.
        // This is basically what JSR 255 does.
	final OpenMBeanOperationInfo operInfo = new OpenMBeanOperationInfoSupport( m.getName(),
	    desc, paramInfo, rtc.getManagedType(), MBeanOperationInfo.ACTION_INFO ) ;

	return new Pair<Operation,OpenMBeanOperationInfo>( oper, operInfo ) ;
    }

    private void analyzeOperations( ClassAnalyzer ca ) {
	// Scan for all methods annotation with @ManagedOperation, including inherited methods.
	final List<Method> ops = ca.findMethods( ca.forAnnotation( ManagedOperation.class ) ) ;
	for (Method m : ops) {
            final Pair<Operation,OpenMBeanOperationInfo> data = makeOperation( m ) ;
            final OpenMBeanOperationInfo info = data.second() ;
            
            final List<String> dataTypes = new ArrayList<String>() ;
            for (MBeanParameterInfo pi : info.getSignature()) {

                // Replace recursion marker with the constructed implementation
                dataTypes.add( pi.getType() ) ;
            }
            
            Map<List<String>,Operation> map = operations.get( m.getName() ) ;
            if (map == null) {
                map = new HashMap<List<String>,Operation>() ;
                operations.put( m.getName(), map ) ;
            }

            // XXX we might want to check and see if this was previously defined
            map.put( dataTypes, data.first() ) ;

            mbeanOperationInfoList.add( info ) ;
	}
    }

    public DynamicMBeanSkeleton( final Class<?> annotatedClass, final ClassAnalyzer ca,
        final ManagedObjectManagerInternal mom ) {

	this.mom = mom ;

        final ManagedObject mo = annotatedClass.getAnnotation( ManagedObject.class ) ;
        
	type = mo.type() ;
	if (type.equals( "" ))
	    type = annotatedClass.getName() ;

	setters = new HashMap<String,AttributeDescriptor>() ;
	getters = new HashMap<String,AttributeDescriptor>() ; 
	operations = new HashMap<String,Map<List<String>,Operation>>() ;
	mbeanAttributeInfoList = new ArrayList<OpenMBeanAttributeInfo>() ;
	mbeanOperationInfoList = new ArrayList<OpenMBeanOperationInfo>() ;

        analyzeInheritedAttributes( annotatedClass, ca ) ;
        analyzeAnnotatedAttributes( ca ) ;
        analyzeOperations( ca ) ;
        
	OpenMBeanAttributeInfo[] attrInfos = mbeanAttributeInfoList.toArray( 
	    new OpenMBeanAttributeInfo[mbeanAttributeInfoList.size()] ) ;
	OpenMBeanOperationInfo[] operInfos = mbeanOperationInfoList.toArray(
	    new OpenMBeanOperationInfo[mbeanOperationInfoList.size() ] ) ;
	mbInfo = new OpenMBeanInfoSupport( 
	    type, mo.description(), attrInfos, null, operInfos, null ) ;
    }

    // The rest of the methods are used in the DynamicMBeanImpl code.
    
    public String getType() {
	return type ;
    }

    public Object getAttribute( Object obj, String name) throws AttributeNotFoundException,
	MBeanException, ReflectionException {

	AttributeDescriptor getter = getters.get( name ) ;
	if (getter == null)
	    throw new AttributeNotFoundException( "Could not find attribute " + name ) ;

        return getter.get( obj ) ;
    }
    
    public void setAttribute(Object obj, Attribute attribute) throws AttributeNotFoundException,
	InvalidAttributeValueException, MBeanException, ReflectionException  {

	String name = attribute.getName() ;
	Object value = attribute.getValue() ;
	AttributeDescriptor setter = setters.get( name ) ;
	if (setter == null)
	    throw new AttributeNotFoundException( "Could not find writable attribute " + name ) ;

        setter.set( obj, value ) ;
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

	Map<List<String>,Operation> opMap = operations.get( actionName ) ;
	if (opMap == null)
	    throw new IllegalArgumentException( "Could not find operation named " + actionName ) ;

	List<String> sig = Arrays.asList( signature ) ;
	Operation op = opMap.get( sig ) ;
	if (op == null)
	    throw new IllegalArgumentException( "Could not find operation named " + actionName 
		+ " with signature " + sig ) ;

	try {
	    return op.evaluate( obj, Arrays.asList( params ) ) ;
	} catch (Exception exc) {
	    throw new ReflectionException( exc ) ;
	}
    }
    
    public MBeanInfo getMBeanInfo() {
	return mbInfo ;
    }
}

