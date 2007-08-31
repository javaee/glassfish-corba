/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2003-2007 Sun Microsystems, Inc. All rights reserved.
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

/*
 * Appserver logging has been added to this class, levels of
 * verbosity are as follows:
 *
 * INFO = no extra output in logs, standard production setting
 * FINE = (IasUtilDelegate) logs message stating if HIGHP or STDP
 *        copyObject code is executed. Can be used to determine
 *        how often the sun.reflect code falls back to std ORB copy
 * FINER = logs tracing info for arrayCopy and copyFields
 * FINEST = logs everything, including exception stack traces
 */

package com.sun.corba.se.impl.copyobject ;

import java.rmi.MarshalException;
import java.rmi.Remote;
import java.rmi.ServerError;
import java.rmi.RemoteException;

import java.util.Collections;
import java.util.ArrayList;
import java.util.Map;
import java.util.HashMap;
import java.util.IdentityHashMap;
import java.util.Hashtable;
import java.util.Enumeration;
import java.util.Iterator;
import java.util.Properties;

import java.io.Serializable;
import java.io.Externalizable;
import java.io.NotSerializableException;

import java.lang.reflect.AccessibleObject;
import java.lang.reflect.Field;
import java.lang.reflect.Array;
import java.lang.reflect.Method;
import java.lang.reflect.Constructor;
import java.lang.reflect.Modifier;
import java.lang.reflect.InvocationTargetException;

import java.security.AccessController;
import java.security.PrivilegedAction;
import java.security.PrivilegedExceptionAction;

import java.util.logging.Level;
import java.util.logging.Logger;

import sun.corba.Bridge; 

import org.omg.CORBA.Any;
import org.omg.CORBA.portable.InputStream;
import org.omg.CORBA.portable.OutputStream;
import org.omg.CORBA.portable.ObjectImpl;
import org.omg.CORBA.COMM_FAILURE;
import org.omg.CORBA.BAD_PARAM;
import org.omg.CORBA.INV_OBJREF;
import org.omg.CORBA.NO_PERMISSION;
import org.omg.CORBA.MARSHAL;
import org.omg.CORBA.OBJECT_NOT_EXIST;
import org.omg.CORBA.TRANSACTION_REQUIRED;
import org.omg.CORBA.TRANSACTION_ROLLEDBACK;
import org.omg.CORBA.INVALID_TRANSACTION;
import org.omg.CORBA.BAD_OPERATION;
import org.omg.CORBA.CompletionStatus;
import org.omg.CORBA.portable.UnknownException;
import org.omg.CORBA.TCKind;

import com.sun.corba.se.impl.util.Utility;

import com.sun.corba.se.spi.orb.ORB ;

import com.sun.corba.se.impl.logging.CORBALogDomains;

import com.sun.corba.se.spi.orbutil.copyobject.ObjectCopier;
import com.sun.corba.se.spi.orbutil.copyobject.ReflectiveCopyException;

/**
 * Provides the functionality of copying objects using reflection.
 * NOTE: Currently the implementation does not implement this copying
 *       functionality for objects which have fields whose types are
 *       based on inner classes.
 * If for any reason copying cannot be done using reflection it uses
 * the original ORB serialization to implement the copying
 */
public class OldReflectObjectCopierImpl implements ObjectCopier 
{
    private IdentityHashMap objRefs;
    private ORB orb ;
    private Logger _logger ;

    public OldReflectObjectCopierImpl( org.omg.CORBA.ORB orb ) 
    {
        objRefs = new IdentityHashMap();
	this.orb = (ORB)orb ;
	_logger = this.orb.getLogger( CORBALogDomains.RMIIIOP_DELEGATE ) ;
    }

    /**
     * reflectCache is used to cache the reflection attributes of
     *              a class
     */
    private static Map reflectCache = new HashMap();

    /**
     * Provides the functionality of a cache for storing the various
     * reflection attributes of a class so that access to these methods
     * is not done repeatedly
     */
    class ReflectAttrs {
        public Field[] fields;
        public Constructor constr;
	public Class thisClass ;
        public Class arrayClass;
        public Class superClass;
        public boolean isImmutable;
        public boolean isDate;
        public boolean isSQLDate;

        public ReflectAttrs(Class cls) {
	    thisClass = cls ;
	    String name = cls.getName();
	    char ch = name.charAt(0);

	    isImmutable = false;
	    isDate = false;
	    isSQLDate = false; 
	    fields = null;
	    constr = null;
	    superClass = null;
	    if (ch == '[') {
		arrayClass = cls.getComponentType();
	    } else if (isImmutable(name)) {
		isImmutable = true;
	    } else if (name.equals("java.util.Date")) {
		isDate = true;
	    } else if (name.equals("java.sql.Date")) {
		isSQLDate = true;
	    } else {
		if (Externalizable.class.isAssignableFrom( cls ))
		    constr = getExternalizableConstructor(cls) ;
		else if (Serializable.class.isAssignableFrom( cls ))
                    constr = getSerializableConstructor(cls) ;
		if (constr != null) { constr.setAccessible(true); }    
		fields = cls.getDeclaredFields();
		AccessibleObject.setAccessible(fields, true);
		superClass = cls.getSuperclass();
	    }
        }
    };

    /** Bridge is used to access the reflection factory for 
     * obtaining serialization constructors.
     * This must be carefully protected!
     */
    private static final Bridge bridge = 
	(Bridge)AccessController.doPrivileged(
	    new PrivilegedAction() {
		public Object run() {
		    return Bridge.get() ;
		}
	    } 
	) ;

    /**
     * Returns public no-arg constructor of given class, or null if none found.
     * Access checks are disabled on the returned constructor (if any), since
     * the defining class may still be non-public.
     */
    private Constructor getExternalizableConstructor(Class cl) {
        try {
            Constructor cons = cl.getDeclaredConstructor(new Class[0]);
            cons.setAccessible(true);
            return ((cons.getModifiers() & Modifier.PUBLIC) != 0) ?  cons : null;
        } catch (NoSuchMethodException ex) {
             if (_logger.isLoggable(Level.FINEST)) {
               _logger.log(Level.FINEST,
                   "com.iplanet.ias.util.orbutil.CopyObjectLocal.getExternalizableConstructor(class)" +
                   " Threw an exception:  Cannot obtain a externalizable constructor.", ex);
             }
            //test for null on calling routine will avoid NPE just to be safe
            return null;
        }
    }

   /**
     * Returns true if classes are defined in the same package, false
     * otherwise.
     *
     * Copied from the Merlin java.io.ObjectStreamClass.
     */
    private boolean packageEquals(Class cl1, Class cl2) {
        Package pkg1 = cl1.getPackage(), pkg2 = cl2.getPackage();
        return ((pkg1 == pkg2) || ((pkg1 != null) && (pkg1.equals(pkg2))));
    }

    /**
     * Returns subclass-accessible no-arg constructor of first non-serializable
     * superclass, or null if none found.  Access checks are disabled on the
     * returned constructor (if any).
     */
    private Constructor getSerializableConstructor(Class cl) {
        Class initCl = cl;
        if (initCl == null) {
            //should not be possible for initCl==null but log and return null
            if (_logger.isLoggable(Level.FINEST)) {
             _logger.log(Level.FINEST,
                 "com.iplanet.ias.util.orbutil.CopyObjectLocal.getSerializableConstructor(class)" +
                 " Class past to method is null. Could not get constructor.");
            }
            //test for null on calling routine will avoid NPE just to be safe
            return null;
        }
        while (Serializable.class.isAssignableFrom(initCl)) {
            if ((initCl = initCl.getSuperclass()) == null) {
                return null;
            }
        }
        try {
            Constructor cons = initCl.getDeclaredConstructor(new Class[0]);
            int mods = cons.getModifiers();
            if ((mods & Modifier.PRIVATE) != 0 ||
                ((mods & (Modifier.PUBLIC | Modifier.PROTECTED)) == 0 &&
                 !packageEquals(cl, initCl)))
            {
              if (_logger.isLoggable(Level.FINEST)) {
               _logger.log(Level.FINEST,
                   "com.iplanet.ias.util.orbutil.CopyObjectLocal.getSerializableConstructor(class)" +
                   " Class " + cl.getName() + "does not define an appropriate constructor.");
              }
               //test for null on calling routine will avoid NPE just to be safe
               return null;
            }
            cons = bridge.newConstructorForSerialization(cl, cons);
            cons.setAccessible(true);
            return cons;
        } catch (NoSuchMethodException ex) {
            if (_logger.isLoggable(Level.FINEST)) {
               _logger.log(Level.FINEST,
                   "com.iplanet.ias.util.orbutil.CopyObjectLocal.getSerializableConstructor(class)" +
                   " Cannot obtain a serializable constructor.", ex);
            }
            //test for null on calling routine will avoid NPE just to be safe
            return null;
        }
    }

    /**
     * Gets the reflection attributes for a class from the cache or
     * if it is not in the cache yet, computes the attributes and
     * populates the cache
     * @param cls the class whose attributes are needed
     * @return the attributes needed for reflection
     * @exception none
     *
     * This method must be synchronized so that reflectCache.put can
     * safely update the reflectCache.
     */
    private final synchronized ReflectAttrs getClassAttrs(Class cls) {
        ReflectAttrs attrs = null;

        attrs = (ReflectAttrs)reflectCache.get(cls);
        if (attrs == null) {
            attrs = new ReflectAttrs(cls);
            reflectCache.put(cls, (Object)attrs);
        }
        return attrs;
    }

    public static boolean isImmutable(String classname) {
        if (classname.startsWith("java.lang.")) {
            String typename = classname.substring(10);
            if (typename.compareTo("String") == 0 ||
                typename.compareTo("Class") == 0 ||
                typename.compareTo("Integer") == 0 ||
                typename.compareTo("Boolean") == 0 ||
                typename.compareTo("Long") == 0 ||
                typename.compareTo("Double") == 0 ||
                typename.compareTo("Byte") == 0 ||
                typename.compareTo("Char") == 0 ||
                typename.compareTo("Short") == 0 ||
                typename.compareTo("Object") == 0 ||
                typename.compareTo("Float") == 0) {
                return true;
            }
        }
        return false;
    }

    /**
     * Utility to copy array of primitive types or objects. Used by local
     * stubs to copy objects
     * @param obj the object to copy or connect.
     * @return the copied object.
     * @exception RemoteException if any object could not be copied.
     */
    private final Object arrayCopy(Object obj, Class aClass) 
        throws RemoteException, InstantiationException, 
	IllegalAccessException, InvocationTargetException
    {
        if (_logger.isLoggable(Level.FINER)) { 
           _logger.log(Level.FINER,
               "ReflectCopyLocal: ReflectAttrs: in arrayCopy");
        }
        Object acopy = null;

	if (aClass.isPrimitive()) {
            if (_logger.isLoggable(Level.FINER)) {
               _logger.log(Level.FINER,
                   "ReflectCopyLocal: arrayCopy: is a primitive array");
            }
	    if (aClass == byte.class) {
		acopy = ((byte[])obj).clone();
	    } else if (aClass == char.class) {
		acopy = ((char[])obj).clone();
	    } else if (aClass == short.class) {
		acopy = ((short[])obj).clone();
	    } else if (aClass == int.class) {
		acopy = ((int[])obj).clone();
	    } else if (aClass == long.class) {
		acopy = ((long[])obj).clone();
	    } else if (aClass == double.class) {
		acopy = ((double[])obj).clone();
	    } else if (aClass == float.class) {
		acopy = ((float[])obj).clone();
	    } else if (aClass == boolean.class) {
		acopy = ((boolean[])obj).clone();
	    }
	    objRefs.put(obj, acopy);
	} else if (aClass == String.class) {
            if (_logger.isLoggable(Level.FINER)) {
               _logger.log(Level.FINER,
                   "ReflectCopyLocal: arrayCopy: is a String array");
            }

	    acopy = ((String [])obj).clone();
	    objRefs.put(obj, acopy);
	} else {
            if (_logger.isLoggable(Level.FINER)) {
               _logger.log(Level.FINER,
                   "ReflectCopyLocal: arrayCopy: is another type of array, (not primitive or String)");
            }

	    int alen = Array.getLength(obj);

            if (_logger.isLoggable(Level.FINER)) {
               _logger.log(Level.FINER,
                   "ReflectCopyLocal: arrayCopy: array length is " + alen);
            }

            aClass = obj.getClass().getComponentType();

            if (_logger.isLoggable(Level.FINER)) {
               _logger.log(Level.FINER,
                   "ReflectCopyLocal: arrayCopy: got class and name is " + aClass.getName());
               _logger.log(Level.FINER,
                   "ReflectCopyLocal: arrayCopy: before Array.newInstance ClassLoader is " +
                    aClass.getClass().getClassLoader());
            }

            acopy = Array.newInstance(aClass, alen);

            if (_logger.isLoggable(Level.FINER)) {
               _logger.log(Level.FINER,
                   "ReflectCopyLocal: arrayCopy: after Array.newInstance ClassLoader is " +
                    acopy.getClass().getClassLoader());
               _logger.log(Level.FINER,
                   "ReflectCopyLocal: arrayCopy: created a new array");
            }

		objRefs.put(obj, acopy);
		for (int idx=0; idx<alen; idx++) {
                    if (_logger.isLoggable(Level.FINER)) {
                       _logger.log(Level.FINER,
                           "ReflectCopyLocal: arrayCopy: copying number " + idx);
                    }

		    Object aobj = Array.get(obj, idx);

                    if (_logger.isLoggable(Level.FINER)) {
                       _logger.log(Level.FINER,
                           "ReflectCopyLocal: arrayCopy: calling reflectCopy");
                    }

		    aobj = reflectCopy(aobj);

                    if (_logger.isLoggable(Level.FINER)) {
                       _logger.log(Level.FINER,
                           "ReflectCopyLocal: arrayCopy: calling Array.set");
                    }

		    Array.set(acopy, idx, aobj);
		}
	}

        return acopy;
    }

    /**
     * Utility to copy fields of an object. Used by local stub to copy
     * objects
     * @param obj the object whose fields need to be copied
     * @exception RemoteException if any object could not be copied.
     */
    private final void copyFields(Class cls, Field[] fields, Object obj, 
	Object copy) throws RemoteException, IllegalAccessException,
	InstantiationException, InvocationTargetException
    {
        if (_logger.isLoggable(Level.FINER)) {
           _logger.log(Level.FINER,
               "ReflectCopyLocal: copyFields: entering copyFields");
           _logger.log(Level.FINER,
               "ReflectCopyLocal: copyFields: object where fields need copy " + obj.getClass().getName());
           _logger.log(Level.FINER,
               "ReflectCopyLocal: copyFields: class " + cls.getName() + " and is " + cls.toString());
           _logger.log(Level.FINER,
               "ReflectCopyLocal: copyFields: num of fields = " + fields.length);
        }

        if (fields == null || fields.length == 0) {
            return;
        }

	// regular object, so copy the fields over
	for (int idx=0; idx<fields.length; idx++) {
	    Field fld = fields[idx];
	    int modifiers = fld.getModifiers() ;
	    Object fobj = null;
	    Class fieldClass = fld.getType();
            if (_logger.isLoggable(Level.FINER)) {
               _logger.log(Level.FINER,
                   "ReflectCopyLocal: copyFields: field number " + idx + " is " +
                    Modifier.toString(fld.getModifiers()) + " " + fieldClass + " " +
                    fld.getName());
            }

	    if (!Modifier.isStatic(modifiers)) {
                if (_logger.isLoggable(Level.FINER)) {
                   _logger.log(Level.FINER,
                       "ReflectCopyLocal: copyFields: field is non-static primitive");
                }

		if (fieldClass == int.class) {
                    if (_logger.isLoggable(Level.FINER)) {
                       _logger.log(Level.FINER,
                           "ReflectCopyLocal: copyFields: field is an integer");
                    }
		    fld.setInt(copy, fld.getInt(obj));
		} else if (fieldClass == long.class) {
                    if (_logger.isLoggable(Level.FINER)) {
                       _logger.log(Level.FINER,
                           "ReflectCopyLocal: copyFields: field is a long");
                    }
		    fld.setLong(copy, fld.getLong(obj));
		} else if (fieldClass == double.class) {
                    if (_logger.isLoggable(Level.FINER)) {
                       _logger.log(Level.FINER,
                           "ReflectCopyLocal: copyFields: field is a double");
                    }
		    fld.setDouble(copy, fld.getDouble(obj));
		} else if (fieldClass == byte.class) {
                    if (_logger.isLoggable(Level.FINER)) {
                       _logger.log(Level.FINER,
                           "ReflectCopyLocal: copyFields: field is a byte");
                    }
		    fld.setByte(copy, fld.getByte(obj));
		} else if (fieldClass == char.class) {
                    if (_logger.isLoggable(Level.FINER)) {
                       _logger.log(Level.FINER,
                           "ReflectCopyLocal: copyFields: field is a char");
                    }
		    fld.setChar(copy, fld.getChar(obj));
		} else if (fieldClass == short.class) {
                    if (_logger.isLoggable(Level.FINER)) {
                       _logger.log(Level.FINER,
                           "ReflectCopyLocal: copyFields: field is a short");
                    }
		    fld.setShort(copy, fld.getShort(obj));
		} else if (fieldClass == float.class) {
                    if (_logger.isLoggable(Level.FINER)) {
                       _logger.log(Level.FINER,
                           "ReflectCopyLocal: copyFields: field is a float");
                    }
		    fld.setFloat(copy, fld.getFloat(obj));
		} else if (fieldClass == boolean.class) {
                    if (_logger.isLoggable(Level.FINER)) {
                       _logger.log(Level.FINER,
                           "ReflectCopyLocal: copyFields: field is an boolean");
                    }
		    fld.setBoolean(copy, fld.getBoolean(obj));
		} else {
                    if (_logger.isLoggable(Level.FINER)) {
                       _logger.log(Level.FINER,
                           "ReflectCopyLocal: copyFields: field is not a non-static primitive");
                    }
		    fobj = fld.get(obj);
                    if (_logger.isLoggable(Level.FINER)) {
                       _logger.log(Level.FINER,
                           "ReflectCopyLocal: copyFields: calling reflectCopy(fobj)");
                    }
		    Object newfobj = reflectCopy(fobj);
                    if (_logger.isLoggable(Level.FINER)) {
                       _logger.log(Level.FINER,
                           "ReflectCopyLocal: copyFields: caching field");
                    }
		    fld.set(copy, newfobj);
		}
	    }
	}
    }


    // Returns an empty instance of Class cls.  Useful for 
    // cloning collection types.  Requires a no args constructor,
    // public for now (but could use non-public)
    private Object makeInstanceOfClass (Class cls) 
	throws IllegalAccessException, InstantiationException
    {
	return cls.newInstance() ;
    }

    // Copy any object that is an instanceof Map.
    private Object copyMap( Object obj ) 
	throws RemoteException, InstantiationException, IllegalAccessException,
	InvocationTargetException
    {
	Map src = (Map)obj ;
	Map result = (Map)makeInstanceOfClass( src.getClass() ) ;
	// Do this early, or self-references cause stack overflow!
	objRefs.put( src, result ) ;  
	Iterator iter = src.entrySet().iterator() ;
	while (iter.hasNext()) {
	    Map.Entry entry = (Map.Entry)(iter.next());
	    Object key = entry.getKey();
	    Object value = entry.getValue() ;
	    // Checks for null are handled in reflectCopy.
	    Object newKey = reflectCopy( key) ;
	    Object newValue = reflectCopy( value) ;
	    result.put( newKey, newValue ) ;
	}

	return result ;
    }

    // Pass in attrs just to avoid looking them up again.
    private Object copyAnyClass( ReflectAttrs attrs, Object obj ) 
	throws RemoteException, InstantiationException, 
	IllegalAccessException, InvocationTargetException
    {
	// regular object, so copy the fields over
	Constructor cons = attrs.constr;
	if (cons == null)
	    throw new IllegalArgumentException( "Class " + attrs.thisClass +
		 " is not Serializable" ) ;

	Object copy = cons.newInstance();

	// Do this before copyFields, or self-references cause stack overflow!
	objRefs.put(obj, copy);
	copyFields(attrs.thisClass, attrs.fields, obj, copy);
	Class cls = attrs.superClass;
	while (cls != null && cls != Object.class) {
	    attrs = getClassAttrs(cls);
	    copyFields(cls, attrs.fields, obj, copy);
	    cls = attrs.superClass;
	} 

	return copy ;
    }

    /**
     * Utility to copy objects using Java reflection. Used by the local stub
     * to copy objects
     * @param obj the object to copy or connect.
     * @return the copied object.
     */
    private final Object reflectCopy(Object obj) 
	throws RemoteException, InstantiationException, 
	IllegalAccessException, InvocationTargetException
    {
	// Always check for nulls here, so we don't need to check in other places.
	if (obj == null)
	    return null ;

	Class cls = obj.getClass() ;
	ReflectAttrs attrs = getClassAttrs( cls ) ;

        Object copy = null;

        if (attrs.isImmutable || (obj instanceof org.omg.CORBA.Object)) {
            return obj;
        }

        if (obj instanceof Remote) {
            return Utility.autoConnect(obj, orb, true);
        }

        copy = objRefs.get(obj);
        if (copy == null) {
	    // Handle instance of HashMap specially because Map.Entry contains 
	    // non-static finals.  HashTable is likewise handled here.
            if ( ( cls.getName().equals("java.util.HashMap") ) ||
                 ( cls.getName().equals("java.util.HashTable") ) ) {
		copy = copyMap( obj ) ;
	    } else {
		Class aClass = attrs.arrayClass;

		if (aClass != null) {
		    // object is an array, so do the array copy
		    copy = arrayCopy(obj, aClass);
		} else {
		    if (attrs.isDate) {
			copy = new java.util.Date(((java.util.Date)obj).getTime());
			objRefs.put(obj, copy);
		    } else if (attrs.isSQLDate) {
			copy = new java.sql.Date(((java.sql.Date)obj).getTime());
			objRefs.put(obj, copy);
		    } else {
			copy = copyAnyClass( attrs, obj ) ;
		    }
		}
	    }
        }

        return copy;
    }

    // This is the public interface.  It must never be called from 
    // inside this class.  It is the single point at which all exceptions
    // are caught, wrapper, and rethrown as ReflectiveCopyExceptions.
    // This can trigger fallback behavior in IasUtilDelegate.
    public Object copy(final Object obj, boolean debug ) throws ReflectiveCopyException
    {
	return copy( obj ) ;
    }

    public Object copy(final Object obj) throws ReflectiveCopyException
    {
        try {
            return AccessController.doPrivileged(
                new PrivilegedExceptionAction() {
                    public Object run() throws RemoteException, InstantiationException, 
                        IllegalAccessException, InvocationTargetException
                    {
                        return reflectCopy(obj);
                    }
                } 
            ) ;
        } catch (ThreadDeath td) {
            throw td ;
        } catch (Throwable thr) {
            if (_logger.isLoggable(Level.FINEST)) {
                _logger.log(Level.FINEST, 
                    "com.iplanet.ias.util.orbutil.CopyObjectLocal.reflectCopy(object) " +
                    "Threw an exception:", thr);
            }
            throw new ReflectiveCopyException( "Could not copy object of class " + 
                obj.getClass().getName(), thr ) ;
        }
    }
}

