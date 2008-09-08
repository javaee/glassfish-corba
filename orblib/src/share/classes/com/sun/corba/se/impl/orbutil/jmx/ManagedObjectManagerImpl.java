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
import java.util.Map ;
import java.util.HashMap ;
import java.util.WeakHashMap ;
import java.util.Properties ;
import java.util.Hashtable ;
import java.util.Enumeration ;

import java.lang.reflect.Type ;

import java.lang.management.ManagementFactory ;

import javax.management.MBeanServer ;
import javax.management.ObjectName ;
import javax.management.DynamicMBean ;
import javax.management.InstanceNotFoundException ;
import javax.management.InstanceAlreadyExistsException ;
import javax.management.MBeanRegistrationException ;
import javax.management.NotCompliantMBeanException ;

import com.sun.corba.se.spi.orbutil.generic.Pair ;

import com.sun.corba.se.spi.orbutil.jmx.ManagedObjectManager ;
import com.sun.corba.se.spi.orbutil.jmx.ManagedObject ;

// XXX What about cleanup?  Probably want a close() method that unregisters
// all registered objects and flushes caches.
public class ManagedObjectManagerImpl implements ManagedObjectManagerInternal {
    private String domain ;
    private MBeanServer server ; 
    private Map<Object,ObjectName> objectMap ;
    private Map<ObjectName,Object> objectNameMap ;
    private Map<Class<?>,DynamicMBeanSkeleton> skeletonMap ;
    private Map<Type,TypeConverter> typeConverterMap ;

    private static final TypeConverter recursiveTypeMarker = 
        new TypeConverterImpl.TypeConverterPlaceHolderImpl() ;

    public synchronized DynamicMBeanSkeleton getSkeleton( Class<?> cls ) {
	DynamicMBeanSkeleton result = skeletonMap.get( cls ) ;	

	if (result == null) {
            Pair<Class<?>,ClassAnalyzer> pair = AnnotationUtil.getClassAnalyzer( 
                cls, ManagedObject.class ) ;
            Class<?> annotatedClass = pair.first() ;
            ClassAnalyzer ca = pair.second() ;

            result = skeletonMap.get( annotatedClass ) ;
            
            if (result == null) {
                result = new DynamicMBeanSkeleton( annotatedClass, ca, this ) ;
            }

	    skeletonMap.put( cls, result ) ;
	}

	return result ;
    }

    public synchronized TypeConverter getTypeConverter( Type type ) {
	TypeConverter result = typeConverterMap.get( type ) ;	
	if (result == null) {
            // Store a TypeConverter impl that throws an exception when acessed.
            // Used to detect recursive types.
            typeConverterMap.put( type, recursiveTypeMarker ) ;

	    result = TypeConverterImpl.makeTypeConverter( type, this ) ;

            // Replace recursion marker with the constructed implementation
	    typeConverterMap.put( type, result ) ;
	}
	return result ;
    }

    public ManagedObjectManagerImpl( String domain ) {
	this.domain = domain ;
	server = ManagementFactory.getPlatformMBeanServer() ;
	objectMap = new HashMap<Object,ObjectName>() ;
	objectNameMap = new HashMap<ObjectName,Object>() ;
	skeletonMap = new WeakHashMap<Class<?>,DynamicMBeanSkeleton>() ;
	typeConverterMap = new WeakHashMap<Type,TypeConverter>() ;
    }

    /** Create another ManagedObjectManager that delegates to this one, but adds some
     * fixed properties to each ObjectName on the register call.
     * Each element in props must be in the "name=value" form.
     */
    public static ManagedObjectManager makeDelegate( 
        final ManagedObjectManagerInternal mom, 
	final String... props ) {

	return new ManagedObjectManagerInternal() {
	    final Properties savedProps = makeProps( props ) ;

	    public void register( Object obj, String... mprops )  {
		Properties lprops = new Properties( savedProps ) ;
		addToProperties( lprops, mprops ) ;
		mom.register( obj, lprops ) ;
	    }

	    public void register( Object obj, Properties mprops ) {
		Properties lprops = new Properties( savedProps ) ;
		Enumeration<?> names = mprops.propertyNames() ;
		while (names.hasMoreElements()) {
		    String name = (String)names.nextElement() ;
		    String value = mprops.getProperty( name ) ;
		    lprops.setProperty( name, value ) ;
		}

		mom.register( obj, lprops ) ;
	    }

	    public void unregister( Object obj ) {
		try {
		    mom.unregister( obj ) ;
		} catch (Exception exc) {
		    throw new IllegalArgumentException( exc ) ;
		}
	    }

	    public ObjectName getObjectName( Object obj ) {
		return mom.getObjectName( obj ) ;
	    }

	    public String getDomain() {
		return mom.getDomain() ;
	    }

	    public Object getObject( ObjectName oname ) {
		return mom.getObject( oname ) ;
	    }

	    public TypeConverter getTypeConverter( Type type ) {
		return mom.getTypeConverter( type ) ;
	    }

	    public void setMBeanServer( MBeanServer server ) {
		mom.setMBeanServer( server ) ;
	    }

	    public MBeanServer getMBeanServer() {
		return mom.getMBeanServer() ;
	    }
	} ;
    }

    private static Properties makeProps( String... props ) {
	Properties result = new Properties() ;
	addToProperties( result, props ) ;
	return result ;
    }

    private static void addToProperties( Properties base, String... props ) {
	for (String str : props) {
	    int eqIndex = str.indexOf( "=" ) ;
	    if (eqIndex < 1)
		throw new IllegalArgumentException( 
		    "All properties must contain an = after the (non-empty) property name" ) ;
	    String name = str.substring( 0, eqIndex ) ;
	    String value = str.substring( eqIndex+1 ) ;
	    base.setProperty( name, value ) ;
	}
    }

    public void register( Object obj, String... props ) {
	register( obj, makeProps(props) ) ;
    }

    /** The ObjectName constructor expects a Hashtable, not properties,
     * so we need to convert to a flat Hashtable, otherwise any property
     * defaults will be missed in the ObjectName constructor.
     */
    private Hashtable convertToHashtable( Properties props ) {
	Hashtable result = new Hashtable() ;
	Enumeration<?> names = props.propertyNames() ;
	while (names.hasMoreElements()) {
	    String name = (String)names.nextElement() ;
	    String value = props.getProperty( name ) ;
	    result.put( name, value ) ;
	}
	return result ;
    }

    public synchronized void register( final Object obj, 
	final Properties props ) {

	final Class<?> cls = obj.getClass() ;
	final DynamicMBeanSkeleton skel = getSkeleton( cls ) ;
	final DynamicMBean mbean = new DynamicMBeanImpl( skel, obj ) ;
	final Properties myProps = new Properties( props ) ;
	final String type = skel.getType() ;

	myProps.setProperty( "type", type ) ;
	Hashtable onProps = convertToHashtable( myProps ) ;

	ObjectName oname = null ;
	try {
	    oname = new ObjectName( domain, onProps ) ;
	    server.registerMBean( mbean, oname ) ;

	    objectMap.put( obj, oname ) ;
	    objectNameMap.put( oname, obj ) ;
	} catch (Exception exc) {
	    throw new IllegalArgumentException( exc ) ;
	}
    }

    public synchronized void unregister( Object obj ) {
	ObjectName oname = objectMap.get( obj ) ;
	if (oname != null) {
	    try {
		server.unregisterMBean( oname ) ;
	    } catch (Exception exc) {
		throw new IllegalArgumentException( exc ) ;
	    } finally {
		// Make sure obj is removed even if unregisterMBean fails
		objectMap.remove( obj ) ;
		objectNameMap.remove( oname ) ;
	    }
	}
    }

    public ObjectName getObjectName( Object obj ) {
	return objectMap.get( obj ) ;
    }

    public Object getObject( ObjectName oname ) {
	return objectNameMap.get( oname ) ;
    }

    public String getDomain() {
	return domain ;
    }

    public void setMBeanServer( MBeanServer server ) {
	this.server = server ;
    }

    public MBeanServer getMBeanServer() {
	return server ;
    }
}

