/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2007 Sun Microsystems, Inc. All rights reserved.
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
package com.sun.corba.se.impl.encoding.fast ;

import java.io.ObjectOutputStream ;
import java.io.ObjectInputStream ;
import java.io.IOException ;
import java.io.InvalidClassException ;
import java.io.Serializable ;
import java.io.Externalizable ;
import java.io.ObjectStreamException ;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Member;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.lang.reflect.Proxy;
import java.security.AccessController;
import java.security.PrivilegedAction;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.Set;

// Needed for (at least) translateFields.
import com.sun.corba.se.impl.io.ObjectStreamClass ;

// This seems to be just what we need here.
import com.sun.corba.se.impl.io.ObjectStreamField ;

import sun.corba.Bridge ;

/**
 * Serialization's descriptor for classes.  It contains the name and
 * serialVersionUID of the class.  The ClassAnalyzer for a specific class
 * loaded in this Java VM can be found/created using the lookup method.
 * 
 * Adapted from the original java.io.ObjectStreamClass. 
 * Here we are strictly interested in class analysis, removing all Java 
 * serialization support from this class.
 *
 * @Author      Ken Cavanaugh
 * @author	Mike Warres
 * @author	Roger Riggs
 * @version 1.98 02/02/00
 */
public class ClassAnalyzerImpl implements ClassAnalyzer {
    private static final Bridge bridge = 
	AccessController.doPrivileged(
	    new PrivilegedAction<Bridge>() {
		public Bridge run() {
		    return Bridge.get() ;
		}
	    } 
	) ;

    /** serialPersistentFields value indicating no serializable fields */
    public static final ObjectStreamField[] NO_FIELDS = 
	new ObjectStreamField[0];
    
    private static final ObjectStreamField[] serialPersistentFields =
	NO_FIELDS;
    
    /** class associated with this descriptor (if any) */
    private Class cl;
    /** name of class represented by this descriptor */
    private String name;
    private char[] nameChar;
    // XXX do we need suid here?
    /** serialVersionUID of represented class (null if not computed yet) */
    private volatile Long suid = (long)0 ;

    // XXX do we need proxy and enum here, or just handle at the CORBA level?
    /** true if represents dynamic proxy class */
    private boolean isProxy;
    /** true if represents enum type */
    private boolean isEnum;
    /** true if represented class implements Serializable */
    private boolean serializable;
    /** true if represented class implements Externalizable */
    private boolean externalizable;

    /** serializable fields */
    private ObjectStreamField[] fields;

    /** serialization-appropriate constructor, or null if none */
    private Constructor cons;
    /** class-defined writeObject method, or null if none */
    private Method writeObjectMethod;
    /** class-defined readObject method, or null if none */
    private Method readObjectMethod;
    /** class-defined readObjectNoData method, or null if none */
    private Method readObjectNoDataMethod;
    /** class-defined writeReplace method, or null if none */
    private Method writeReplaceMethod;
    /** class-defined readResolve method, or null if none */
    private Method readResolveMethod;

    /** superclass descriptor appearing in stream */
    private ClassAnalyzer superDesc;
    
    public String getName() {
	return name;
    }

    public char[] getNameAsCharArray() {
	return nameChar ;
    }

    /**
     * Return the class in the local VM that this version is mapped to.  Null
     * is returned if there is no corresponding local class.
     *
     * @return	the <code>Class</code> instance that this descriptor represents
     */
    public Class<?> forClass() {
	return cl;
    }
    
    /**
     * Return an array of the fields of this serializable class.
     *
     * @return	an array containing an element for each persistent field of
     * 		this class. Returns an array of length zero if there are no
     * 		fields.
     * @since 1.2
     */
    public ObjectStreamField[] getFields() {
	return getFields(true);
    }

    /**
     * Get the field of this class by name.
     *
     * @param	name the name of the data field to look for
     * @return	The ObjectStreamField object of the named field or null if
     * 		there is no such named field.
     */
    public ObjectStreamField getField(String name) {
	return getField(name, null);
    }

    /**
     * Return a string describing this ClassAnalyzer.
     */
    public String toString() {
	return name ;
    }

    public ClassAnalyzer getSuperClassAnalyzer() {
	return superDesc ;
    }

    /**
     * Creates local class descriptor representing given class.
     */
    public ClassAnalyzerImpl(final LookupTable<Class,ClassMarshaler> lt, 
	final Class cl) {

	this.cl = cl;
	name = cl.getName();
	nameChar = name.toCharArray();
	isProxy = Proxy.isProxyClass(cl);
	isEnum = Enum.class.isAssignableFrom(cl);
	serializable = Serializable.class.isAssignableFrom(cl);
	externalizable = Externalizable.class.isAssignableFrom(cl);

	Class superCl = cl.getSuperclass();

	if ((superCl != null) && (superCl.isAssignableFrom( Serializable.class ))) {
	    superDesc = lt.lookup(null, superCl).getClassAnalyzer() ;
	}

	if (serializable) {
	    AccessController.doPrivileged(new PrivilegedAction() {
		public Object run() {
		    if (isEnum) {
			suid = new Long(0);
			fields = NO_FIELDS;
			return null;
		    }

		    // XXX suid = getDeclaredSUID(cl);
		    try {
			fields = getSerialFields(cl);
		    } catch (InvalidClassException e) {
			// XXX log this
		    }
		    
		    if (externalizable) {
			cons = getExternalizableConstructor(cl);
		    } else {
			cons = getSerializableConstructor(cl);
			writeObjectMethod = getPrivateMethod(cl, "writeObject", 
			    new Class[] { ObjectOutputStream.class }, 
			    Void.TYPE);
			readObjectMethod = getPrivateMethod(cl, "readObject", 
			    new Class[] { ObjectInputStream.class }, 
			    Void.TYPE);
			readObjectNoDataMethod = getPrivateMethod(
			    cl, "readObjectNoData", 
			    new Class[0], Void.TYPE);
		    }
		    writeReplaceMethod = getInheritableMethod(
			cl, "writeReplace", new Class[0], Object.class);
		    readResolveMethod = getInheritableMethod(
			cl, "readResolve", new Class[0], Object.class);
		    return null;
		}
	    });
	} else {
	    suid = new Long(0);
	    fields = NO_FIELDS;
	}
    }

    /**
     * Returns arrays of ObjectStreamFields representing the serializable
     * fields of the represented class.  If copy is true, a clone of this class
     * descriptor's field array is returned, otherwise the array itself is
     * returned.
     */
    ObjectStreamField[] getFields(boolean copy) {
	return copy ? (ObjectStreamField[]) fields.clone() : fields;
    }
    
    /**
     * Looks up a serializable field of the represented class by name and type.
     * A specified type of null matches all types, Object.class matches all
     * non-primitive types, and any other non-null type matches assignable
     * types only.  Returns matching field, or null if no match found.
     */
    ObjectStreamField getField(String name, Class type) {
	for (int i = 0; i < fields.length; i++) {
	    ObjectStreamField f = fields[i];
	    if (f.getName().equals(name)) {
		if (type == null || 
		    (type == Object.class && !f.isPrimitive()))
		{
		    return f;
		}
		Class ftype = f.getType();
		if (ftype != null && type.isAssignableFrom(ftype)) {
		    return f;
		}
	    }
	}
	return null;
    }

    public boolean isProxy() {
	return isProxy;
    }
    
    public boolean isEnum() {
	return isEnum;
    }
    
    public boolean isExternalizable() {
	return externalizable;
    }
    
    public boolean isSerializable() {
	return serializable;
    }

    /**
     * Returns true if represented class is serializable/externalizable and can
     * be instantiated by the serialization runtime--i.e., if it is
     * externalizable and defines a public no-arg constructor, or if it is
     * non-externalizable and its first non-serializable superclass defines an
     * accessible no-arg constructor.  Otherwise, returns false.
     */
    public boolean isInstantiable() {
	return (cons != null);
    }
    
    /**
     * Returns true if represented class is serializable (but not
     * externalizable) and defines a conformant writeObject method.  Otherwise,
     * returns false.
     */
    public boolean hasWriteObjectMethod() {
	return (writeObjectMethod != null);
    }
    
    /**
     * Returns true if represented class is serializable (but not
     * externalizable) and defines a conformant readObject method.  Otherwise,
     * returns false.
     */
    public boolean hasReadObjectMethod() {
	return (readObjectMethod != null);
    }
    
    /**
     * Returns true if represented class is serializable (but not
     * externalizable) and defines a conformant readObjectNoData method.
     * Otherwise, returns false.
     */
    public boolean hasReadObjectNoDataMethod() {
	return (readObjectNoDataMethod != null);
    }
    
    /**
     * Returns true if represented class is serializable or externalizable and
     * defines a conformant writeReplace method.  Otherwise, returns false.
     */
    public boolean hasWriteReplaceMethod() {
	return (writeReplaceMethod != null);
    }
    
    /**
     * Returns true if represented class is serializable or externalizable and
     * defines a conformant readResolve method.  Otherwise, returns false.
     */
    public boolean hasReadResolveMethod() {
	return (readResolveMethod != null);
    }

    /**
     * Creates a new instance of the represented class.  If the class is
     * externalizable, invokes its public no-arg constructor; otherwise, if the
     * class is serializable, invokes the no-arg constructor of the first
     * non-serializable superclass.  Throws UnsupportedOperationException if
     * this class descriptor is not associated with a class, if the associated
     * class is non-serializable or if the appropriate no-arg constructor is
     * inaccessible/unavailable.
     */
    public Object newInstance()
	throws InstantiationException, InvocationTargetException,
	       UnsupportedOperationException
    {
	if (cons != null) {
	    try {
		return cons.newInstance();
	    } catch (IllegalAccessException ex) {
		// should not occur, as access checks have been suppressed
		throw new InternalError();
	    }
	} else {
	    throw new UnsupportedOperationException();
	}
    }
	       
    /**
     * Invokes the writeObject method of the represented serializable class.
     * Throws UnsupportedOperationException if this class descriptor is not
     * associated with a class, or if the class is externalizable,
     * non-serializable or does not define writeObject.
     */
    public void invokeWriteObject(Object obj, ObjectOutputStream out)
	throws IOException, UnsupportedOperationException
    {
	if (writeObjectMethod != null) {
	    try {
		writeObjectMethod.invoke(obj, new Object[]{ out });
	    } catch (InvocationTargetException ex) {
		Throwable th = ex.getTargetException();
		if (th instanceof IOException) {
		    throw (IOException) th;
		} else {
		    throwMiscException(th);
		}
	    } catch (IllegalAccessException ex) {
		// should not occur, as access checks have been suppressed
		throw new InternalError();
	    }
	} else {
	    throw new UnsupportedOperationException();
	}
    }
    
    /**
     * Invokes the readObject method of the represented serializable class.
     * Throws UnsupportedOperationException if this class descriptor is not
     * associated with a class, or if the class is externalizable,
     * non-serializable or does not define readObject.
     */
    public void invokeReadObject(Object obj, ObjectInputStream in)
	throws ClassNotFoundException, IOException, 
	       UnsupportedOperationException
    {
	if (readObjectMethod != null) {
	    try {
		readObjectMethod.invoke(obj, new Object[]{ in });
	    } catch (InvocationTargetException ex) {
		Throwable th = ex.getTargetException();
		if (th instanceof ClassNotFoundException) {
		    throw (ClassNotFoundException) th;
		} else if (th instanceof IOException) {
		    throw (IOException) th;
		} else {
		    throwMiscException(th);
		}
	    } catch (IllegalAccessException ex) {
		// should not occur, as access checks have been suppressed
		throw new InternalError();
	    }
	} else {
	    throw new UnsupportedOperationException();
	}
    }

    /**
     * Invokes the readObjectNoData method of the represented serializable
     * class.  Throws UnsupportedOperationException if this class descriptor is
     * not associated with a class, or if the class is externalizable,
     * non-serializable or does not define readObjectNoData.
     */
    public void invokeReadObjectNoData(Object obj)
	throws IOException, UnsupportedOperationException
    {
	if (readObjectNoDataMethod != null) {
	    try {
		readObjectNoDataMethod.invoke(obj);
	    } catch (InvocationTargetException ex) {
		Throwable th = ex.getTargetException();
		if (th instanceof ObjectStreamException) {
		    throw (ObjectStreamException) th;
		} else {
		    throwMiscException(th);
		}
	    } catch (IllegalAccessException ex) {
		// should not occur, as access checks have been suppressed
		throw new InternalError();
	    }
	} else {
	    throw new UnsupportedOperationException();
	}
    }

    /**
     * Invokes the writeReplace method of the represented serializable class and
     * returns the result.  Throws UnsupportedOperationException if this class
     * descriptor is not associated with a class, or if the class is
     * non-serializable or does not define writeReplace.
     */
    public Object invokeWriteReplace(Object obj)
	throws IOException, UnsupportedOperationException
    {
	if (writeReplaceMethod != null) {
	    try {
		return writeReplaceMethod.invoke(obj);
	    } catch (InvocationTargetException ex) {
		Throwable th = ex.getTargetException();
		if (th instanceof ObjectStreamException) {
		    throw (ObjectStreamException) th;
		} else {
		    throwMiscException(th);
		    throw new InternalError();	// never reached
		}
	    } catch (IllegalAccessException ex) {
		// should not occur, as access checks have been suppressed
		throw new InternalError();
	    }
	} else {
	    throw new UnsupportedOperationException();
	}
    }

    /**
     * Invokes the readResolve method of the represented serializable class and
     * returns the result.  Throws UnsupportedOperationException if this class
     * descriptor is not associated with a class, or if the class is
     * non-serializable or does not define readResolve.
     */
    public Object invokeReadResolve(Object obj)
	throws IOException, UnsupportedOperationException
    {
	if (readResolveMethod != null) {
	    try {
		return readResolveMethod.invoke(obj);
	    } catch (InvocationTargetException ex) {
		Throwable th = ex.getTargetException();
		if (th instanceof ObjectStreamException) {
		    throw (ObjectStreamException) th;
		} else {
		    throwMiscException(th);
		    throw new InternalError();	// never reached
		}
	    } catch (IllegalAccessException ex) {
		// should not occur, as access checks have been suppressed
		throw new InternalError();
	    }
	} else {
	    throw new UnsupportedOperationException();
	}
    }

    /**
     * Returns public no-arg constructor of given class, or null if none found.
     * Access checks are disabled on the returned constructor (if any), since
     * the defining class may still be non-public.
     */
    private static Constructor getExternalizableConstructor(Class cl) {
	try {
	    Constructor cons = cl.getDeclaredConstructor(new Class[0]);
	    cons.setAccessible(true);
	    return ((cons.getModifiers() & Modifier.PUBLIC) != 0) ? 
		cons : null;
	} catch (NoSuchMethodException ex) {
	    return null;
	}
    }

    /**
     * Returns subclass-accessible no-arg constructor of first non-serializable
     * superclass, or null if none found.  Access checks are disabled on the
     * returned constructor (if any).
     */
    private static Constructor getSerializableConstructor(Class cl) {
	Class initCl = cl;
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
		return null;
	    }
	    cons = bridge.newConstructorForSerialization(cl, cons) ;
	    cons.setAccessible(true);
	    return cons;
	} catch (NoSuchMethodException ex) {
	    return null;
	}
    }

    /**
     * Returns non-static, non-abstract method with given signature provided it
     * is defined by or accessible (via inheritance) by the given class, or
     * null if no match found.  Access checks are disabled on the returned
     * method (if any).
     */
    private static Method getInheritableMethod(Class cl, String name,
					       Class[] argTypes,
					       Class returnType)
    {
	Method meth = null;
	Class defCl = cl;
	while (defCl != null) {
	    try {
		meth = defCl.getDeclaredMethod(name, argTypes);
		break;
	    } catch (NoSuchMethodException ex) {
		defCl = defCl.getSuperclass();
	    }
	}

	if ((meth == null) || (meth.getReturnType() != returnType)) {
	    return null;
	}
	meth.setAccessible(true);
	int mods = meth.getModifiers();
	if ((mods & (Modifier.STATIC | Modifier.ABSTRACT)) != 0) {
	    return null;
	} else if ((mods & (Modifier.PUBLIC | Modifier.PROTECTED)) != 0) {
	    return meth;
	} else if ((mods & Modifier.PRIVATE) != 0) {
	    return (cl == defCl) ? meth : null;
	} else {
	    return packageEquals(cl, defCl) ? meth : null;
	}
    }

    /**
     * Returns non-static private method with given signature defined by given
     * class, or null if none found.  Access checks are disabled on the
     * returned method (if any).
     */
    private static Method getPrivateMethod(Class cl, String name, 
					   Class[] argTypes,
					   Class returnType)
    {
	try {
	    Method meth = cl.getDeclaredMethod(name, argTypes);
	    meth.setAccessible(true);
	    int mods = meth.getModifiers();
	    return ((meth.getReturnType() == returnType) &&
		    ((mods & Modifier.STATIC) == 0) &&
		    ((mods & Modifier.PRIVATE) != 0)) ? meth : null;
	} catch (NoSuchMethodException ex) {
	    return null;
	}
    }

    /**
     * Returns true if classes are defined in the same runtime package, false
     * otherwise.
     */
    private static boolean packageEquals(Class cl1, Class cl2) {
	return (cl1.getClassLoader() == cl2.getClassLoader() &&
		getPackageName(cl1).equals(getPackageName(cl2)));
    }

    /**
     * Returns package name of given class.
     */
    private static String getPackageName(Class cl) {
	String s = cl.getName();
	int i = s.lastIndexOf('[');
	if (i >= 0) {
	    s = s.substring(i + 2);
	}
	i = s.lastIndexOf('.');
	return (i >= 0) ? s.substring(0, i) : "";
    }

    /**
     * Compares class names for equality, ignoring package names.  Returns true
     * if class names equal, false otherwise.
     */
    private static boolean classNamesEqual(String name1, String name2) {
	name1 = name1.substring(name1.lastIndexOf('.') + 1);
	name2 = name2.substring(name2.lastIndexOf('.') + 1);
	return name1.equals(name2);
    }
    
    /**
     * Convenience method for throwing an exception that is either a
     * RuntimeException, Error, or of some unexpected type (in which case it is
     * wrapped inside an IOException).
     */
    private static void throwMiscException(Throwable th) throws IOException {
	if (th instanceof RuntimeException) {
	    throw (RuntimeException) th;
	} else if (th instanceof Error) {
	    throw (Error) th;
	} else {
	    IOException ex = new IOException("unexpected exception type");
	    ex.initCause(th);
	    throw ex;
	}
    }

    /**
     * Returns ObjectStreamField array describing the serializable fields of
     * the given class.  Serializable fields backed by an actual field of the
     * class are represented by ObjectStreamFields with corresponding non-null
     * Field objects.  Throws InvalidClassException if the (explicitly
     * declared) serializable fields are invalid.
     */
    private static ObjectStreamField[] getSerialFields(Class cl) 
	throws InvalidClassException
    {
	ObjectStreamField[] fields;
	if (Serializable.class.isAssignableFrom(cl) &&
	    !Externalizable.class.isAssignableFrom(cl) &&
	    !Proxy.isProxyClass(cl) &&
	    !cl.isInterface())
	{
	    if ((fields = getDeclaredSerialFields(cl)) == null) {
		fields = getDefaultSerialFields(cl);
	    }
	    Arrays.sort(fields);
	} else {
	    fields = NO_FIELDS;
	}
	return fields;
    }
    
    /**
     * Returns serializable fields of given class as defined explicitly by a
     * "serialPersistentFields" field, or null if no appropriate
     * "serialPersistentFields" field is defined.  Serializable fields backed
     * by an actual field of the class are represented by ObjectStreamFields
     * with corresponding non-null Field objects.  For compatibility with past
     * releases, a "serialPersistentFields" field with a null value is
     * considered equivalent to not declaring "serialPersistentFields".  Throws
     * InvalidClassException if the declared serializable fields are
     * invalid--e.g., if multiple fields share the same name.
     */
    private static ObjectStreamField[] getDeclaredSerialFields(Class cl) 
	throws InvalidClassException
    {
	ObjectStreamField[] serialPersistentFields = null;
	try {
	    Field f = cl.getDeclaredField("serialPersistentFields");
	    int mask = Modifier.PRIVATE | Modifier.STATIC | Modifier.FINAL;
	    if ((f.getModifiers() & mask) == mask) {
		f.setAccessible(true);
		java.io.ObjectStreamField[] javaSPF = 
		    (java.io.ObjectStreamField[])f.get(null) ;
		serialPersistentFields = ObjectStreamClass.translateFields( javaSPF ) ;
	    }
	} catch (Exception ex) {
	    // XXX log exception
	}

	if (serialPersistentFields == null) {
	    return null;
	} else if (serialPersistentFields.length == 0) {
	    return NO_FIELDS;
	}
	
	ObjectStreamField[] boundFields = 
	    new ObjectStreamField[serialPersistentFields.length];
	Set fieldNames = new HashSet(serialPersistentFields.length);

	for (int i = 0; i < serialPersistentFields.length; i++) {
	    ObjectStreamField spf = serialPersistentFields[i];

	    String fname = spf.getName();
	    if (fieldNames.contains(fname)) {
		throw new InvalidClassException(
		    "multiple serializable fields named " + fname);
	    }
	    fieldNames.add(fname);

	    try {
		Field f = cl.getDeclaredField(fname);
		if ((f.getType() == spf.getType()) &&
		    ((f.getModifiers() & Modifier.STATIC) == 0))
		{
		    boundFields[i] = 
			new ObjectStreamField(f) ;
		}
	    } catch (NoSuchFieldException ex) {
		// XXX log this
	    }

	    if (boundFields[i] == null) {
		boundFields[i] = new ObjectStreamField(
		    fname, spf.getType() ) ;
	    }
	}
	return boundFields;
    }

    /**
     * Returns array of ObjectStreamFields corresponding to all non-static
     * non-transient fields declared by given class.  Each ObjectStreamField
     * contains a Field object for the field it represents.  If no default
     * serializable fields exist, NO_FIELDS is returned.
     */
    private static ObjectStreamField[] getDefaultSerialFields(Class cl) {
	Field[] clFields = cl.getDeclaredFields();
	ArrayList list = new ArrayList();
	int mask = Modifier.STATIC | Modifier.TRANSIENT;

	for (int i = 0; i < clFields.length; i++) {
	    if ((clFields[i].getModifiers() & mask) == 0) {
		list.add(new ObjectStreamField(clFields[i]));
	    }
	}
	int size = list.size();
	return (size == 0) ? NO_FIELDS :
	    (ObjectStreamField[]) list.toArray(new ObjectStreamField[size]);
    }
}
