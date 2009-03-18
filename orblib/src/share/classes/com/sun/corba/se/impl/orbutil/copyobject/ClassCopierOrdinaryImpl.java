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

package com.sun.corba.se.impl.orbutil.copyobject;

import java.io.Serializable ;
import java.io.Externalizable ;

import java.util.WeakHashMap ;
import java.util.Map ;
import java.util.StringTokenizer ;

import java.security.ProtectionDomain ;
import java.security.AccessController ;
import java.security.PrivilegedAction ;
import java.security.PrivilegedActionException ;
import java.security.PrivilegedExceptionAction ;

import java.lang.reflect.Field ;
import java.lang.reflect.Method ;
import java.lang.reflect.Modifier ;
import java.lang.reflect.Constructor ;

import sun.corba.Bridge ;

import com.sun.corba.se.spi.orbutil.copyobject.ReflectiveCopyException ;
import com.sun.corba.se.spi.orbutil.copyobject.Copy ;
import com.sun.corba.se.spi.orbutil.copyobject.CopyInterceptor ;

import static com.sun.corba.se.spi.orbutil.copyobject.CopyType.* ;

import com.sun.corba.se.spi.orbutil.copyobject.LibraryClassLoader ;

// General Object: use proper constructor, iterate over fields,
// get/set fields using Unsafe or reflection.  This also handles readResolve,
// but I don't think writeReplace is needed.
// XXX Add proper handling for custom marshalled classes.
public class ClassCopierOrdinaryImpl extends ClassCopierBase {

//******************************************************************************
// Static utilities 
//******************************************************************************

    /** Bridge is used to access unsafe methods used to read and write 
     * arbitrary data members in objects.  
     * This is very fast, and completely ignores access
     * protections including final on fields.
     * NOTE WELL: Unsafe is capabile of causing severe damage to the
     * VM, including causing the VM to dump core.  get and put calls
     * must only be made with offsets obtained from objectFieldOffset
     * calls.  Because of the dangerous nature of Unsafe, its use 
     * must be carefully protected.
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
     * Returns true if classes are defined in the same package, false
     * otherwise.
     *
     * Copied from the Merlin java.io.ObjectStreamClass.
     */
    private static boolean packageEquals(Class<?> cl1, Class<?> cl2) 
    {
	Package pkg1 = cl1.getPackage(), pkg2 = cl2.getPackage();
	return ((pkg1 == pkg2) || ((pkg1 != null) && (pkg1.equals(pkg2))));
    }


//******************************************************************************
// Method access utilities
//******************************************************************************

    /**
     * Returns non-static, non-abstract method with given signature provided it
     * is defined by or accessible (via inheritance) by the given class, or
     * null if no match found.  Access checks are disabled on the returned
     * method (if any).
     * <p>
     * Copied from the Merlin java.io.ObjectStreamClass.
     */
    private static Method getInheritableMethod( final Class<?> cl, final String name, 
	final Class<?> returnType, final Class<?>... argTypes )
    {
	return (Method)AccessController.doPrivileged(
	    new PrivilegedAction() {
		public Object run() {
		    Method meth = null;
		    Class<?> defCl = cl;
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
	    }
	) ; 
    }

    private Object resolve( Object obj ) 
    {
	if (readResolveMethod != null)
	    try {
		return readResolveMethod.invoke( obj ) ;
	    } catch (Throwable t) {
		throw new RuntimeException(t) ;
	    }
	else
	    return obj ;
    }

//******************************************************************************
// Constructor access class
//******************************************************************************

    /** Class used as a factory for the appropriate Serialization constructors.
     * Note that this cannot be exposed in another class (even package private),
     * because this would provide access to a constructor in some cases that
     * can never be used outside of special serialization or copy support.
     */
    private abstract static class ConstructorFactory
    {
	private ConstructorFactory() {} 

	/**
	 * Returns public no-arg constructor of given class, or null if none 
	 * found.  Access checks are disabled on the returned constructor 
	 * (if any), since the defining class may still be non-public.
	 * <p>
	 * Copied from the Merlin java.io.ObjectStreamClass.
	 */
	private static Constructor getExternalizableConstructor(Class<?> cl) 
	{
	    try {
		Constructor cons = cl.getDeclaredConstructor();
		cons.setAccessible(true);
		return ((cons.getModifiers() & Modifier.PUBLIC) != 0) ? 
		    cons : null;
	    } catch (NoSuchMethodException ex) {
		// XXX log this!
		return null;
	    }
	}

	/**
	 * Returns subclass-accessible no-arg constructor of first 
	 * non-serializable superclass, or null if none found.  
	 * Access checks are disabled on the
	 * returned constructor (if any).
	 * <p>
	 * Copied from the Merlin java.io.ObjectStreamClass.
	 */
	private static Constructor getSerializableConstructor(Class<?> cl) 
	{
	    Class<?> initCl = cl;
	    while (Serializable.class.isAssignableFrom(initCl)) {
		if ((initCl = initCl.getSuperclass()) == null) {
		    return null;
		}
	    }

	    try {
		Constructor cons = initCl.getDeclaredConstructor();
		int mods = cons.getModifiers();
		if ((mods & Modifier.PRIVATE) != 0 ||
		    ((mods & (Modifier.PUBLIC | Modifier.PROTECTED)) == 0 &&
		     !packageEquals(cl, initCl)))
		{
		    return null;
		}
		cons = bridge.newConstructorForSerialization(cl, cons);
		cons.setAccessible(true);
		return cons;
	    } catch (NoSuchMethodException ex) {
		// XXX log this!
		return null;
	    }
	}

	/** Returns a constructor based on the first no-args constructor in
	 * the super class chain.  This allows us to construct an instance
	 * of any class at all, serializable or not.
	 */
	private static Constructor getDefaultConstructor(Class<?> cl) 
	{
	    Constructor cons = null;
	    Class<?> defCl = cl;
	    while (defCl != null) {
		try {
		    cons = defCl.getDeclaredConstructor();
		    break;
		} catch (NoSuchMethodException ex) {
		    defCl = defCl.getSuperclass();
		}
	    }

	    if (cons == null) 
		// XXX log error: should always be able to use Object's
		// no-args constructor if nothing else
		return null ;

	    Constructor result ;

	    if (defCl == cl)
		result = cons ;
	    else
		result = bridge.newConstructorForSerialization( cl, cons ) ;

	    result.setAccessible(true) ;
	    return result ;
	}


	/** Analyze the class to determine the correct constructor type.
	 * Returns the appropriate constructor.
	 */
	private static Constructor makeConstructor( final Class<?> cls ) 
	{
	    return (Constructor)AccessController.doPrivileged(
		new PrivilegedAction() {
		    public Object run() {
			Constructor constructor ;

			// We must check for Externalizable first, since Externalizable
			// extends Serializable.  
			if (Externalizable.class.isAssignableFrom( cls ))
			    // Note that the constructor may be null
			    // here, but that's OK, as a class that implements 
			    // Externalizable may not have an appropriate constructor, 
			    // but may be a base class for a class that DOES have the 
			    // appropriate constructor.  This is not an error.
			    constructor = getExternalizableConstructor(cls) ;
			else if (Serializable.class.isAssignableFrom( cls ))
			    constructor = getSerializableConstructor(cls) ;
			else 
			    // For serialization, this case would be an error.
			    // But we are doing a reflective copy, so we need 
			    // this case, in order to copy transient fields.
			    constructor = getDefaultConstructor(cls) ;

			return constructor ;
		    }
		}
	    ) ;
	}
    }

//******************************************************************************
// ClassFieldCopiers and all their associated bits
//******************************************************************************

    // The first implementation of ClassFieldCopiers supported both reflective
    // and unsafe copiers.  However, the ORB now only runs on JDK implementations
    // that fully support the unsafe approach, so the old reflective code has been
    // removed.
    //
    // Unfortunately, we always need 9 field copier 
    // classes: Object plus the 8 primitive type wrappers.  Otherwise every 
    // getObject call would create a new primitive type wrapper, needlessly 
    // creating lots of garbage objects.
    //
    // Note that if a super class is copied in some other way, we must not attempt 
    // to build a ClassFieldCopier for the derived class.  We should just
    // throw a ReflectiveCopyException and fallback in that case.

    public interface ClassFieldCopier {
	/** Copy all fields from src to dest, using
	 * oldToNew as usual to preserve aliasing.  This copies
	 * all fields declared in the class, as well as in the 
	 * super class.  
	 */
	void copy( Map oldToNew, 
	    Object src, Object dest, boolean debug ) throws ReflectiveCopyException ;
    }

    // Utilities for ClassFieldCopier instances.

    // Maps classes to ClassFieldCopier instances.
    private static Map<Class<?>,ClassFieldCopier> classToClassFieldCopier = 
	DefaultClassCopierFactories.USE_FAST_CACHE ? 
	    new FastCache<Class<?>,ClassFieldCopier>(new WeakHashMap<Class<?>,ClassFieldCopier>()) :
	    new WeakHashMap<Class<?>,ClassFieldCopier>() ;

    private static boolean useCodegenCopier()
    {
        // XXX put this in an orblib constants class like ORBConstants
	return Boolean.getBoolean( "com.sun.corba.se.ORBUseCodegenReflectiveCopyobject" ) ;
    }

    private static final Package CODEGEN_SPI = 
	com.sun.corba.se.spi.orbutil.codegen.Type.class.getPackage() ;
    private static final Package CODEGEN_IMPL = 
	com.sun.corba.se.impl.orbutil.codegen.Node.class.getPackage() ;
    
    private static ThreadLocal<Boolean> isCodegenCopierAllowed =
	new ThreadLocal<Boolean>() {
	    public Boolean initialValue() {
		return Boolean.TRUE ;
	    }
	} ;

    // ONLY for use in spi.orbutil.codegen.Wrapper!
    public static void setCodegenCopierAllowed( boolean flag ) {
	isCodegenCopierAllowed.set( flag ) ;
    }

    private static synchronized ClassFieldCopier getClassFieldCopier( 
	final Class<?> cls, 
	final PipelineClassCopierFactory classCopierFactory ) 
	throws ReflectiveCopyException
    {
	ClassFieldCopier copier = classToClassFieldCopier.get( cls ) ;

	if (copier == null) {
	    try {
		copier = (ClassFieldCopier) AccessController.doPrivileged( 
		    new PrivilegedExceptionAction() {
			public Object run() throws ReflectiveCopyException {
			    // Note that we can NOT generate a copier for classes used in codegen!
			    // If we try, generating the copier uses codegen which calls
			    // the copier, which generates a copier using codegen, which...
			    // Just don't do this for anything that is in the codegen packages.
			    if (useCodegenCopier() && isCodegenCopierAllowed.get())
				return makeClassFieldCopierUnsafeCodegenImpl( 
				    cls, classCopierFactory ) ;
			    else
				return new ClassFieldCopierUnsafeImpl(
				    cls, classCopierFactory ) ;
			}
		    }
		) ;
	    } catch (PrivilegedActionException exc) {
		throw (ReflectiveCopyException)exc.getException() ;
	    }

	    classToClassFieldCopier.put( cls, copier ) ;
	}
	    
	return copier ;
    }

    // Get the superclass ClassFieldCopier, or return null if this is the end of the
    // chain.
    private static synchronized ClassFieldCopier getSuperCopier( 
	PipelineClassCopierFactory ccf, Class<?> cls ) throws ReflectiveCopyException
    {
	Class<?> superClass = cls.getSuperclass() ;
	ClassFieldCopier superCopier= null ;
	if ((superClass != java.lang.Object.class) && (superClass != null)) {
	    ClassCopier cachedCopier = ccf.lookupInCache( superClass ) ; 

	    // If there is a cached ClassCopier, and it is not reflective, then cls
	    // should not be copied by reflection.
	    if ((cachedCopier != null) && (!cachedCopier.isReflectiveClassCopier()))
		throw new ReflectiveCopyException( 
		    "Cannot create ClassFieldCopier for superclass " + 
		    superClass.getName() + 
		    ": This class already has a ClassCopier." ) ;

	    // Return an error immediately, rather than waiting until the
	    // superClass copier is invoked.
	    if (!ccf.reflectivelyCopyable( superClass ))
		throw new ReflectiveCopyException(
		    "Cannot create ClassFieldCopier for superclass " + 
		    superClass.getName() + 
		    ": This class cannot be copied." ) ;
		    
	    superCopier = getClassFieldCopier( superClass, ccf ) ;
	}

	return superCopier ;
    }

//******************************************************************************
//******************************************************************************

    /** Use bridge to copy objects.  Now that we are on JDK 5, this has not
     * been known to fail on any VM (unlike on JDK 1.4.1).  Note that the
     * reflective copier should work now on JDK 5, due to the change in
     * suppressAccessChecks that supports writing to final fields.
     * This copier also supports @Copy annotations on fields.
     */
    private static class ClassFieldCopierUnsafeImpl implements ClassFieldCopier
    {
	private Class<?> myClass ;

	// Note that fieldOffsets and fieldCopiers must always be the 
	// same length.
	private long[] fieldOffsets ;		// The field offsets of all 
						// fields in this class, not 
						// including inherited fields.
	private UnsafeFieldCopier[]  fieldCopiers ;   // The FieldCopier instances for
						// this class.

	// The ClassCopierFactory needed inside objectUnsafeFieldCopier.
	private PipelineClassCopierFactory classCopierFactory ;

	// ClassFieldCopier for the immediate super class, if any.
	private ClassFieldCopier superCopier ;

	// Note that most instances of UnsafeFieldCopier are stateless, in that
	// all needed info is passed in the copy call.  This means that
	// we only need instances of UnsafeFieldCopier for the primitive types
	// and Object.  
	// Note: accessing to the static bridge is done through compiler synthesized
	// methods, which is noticeably slow given the number of times bridge is
	// accessed.  I think making bridge public or protected would avoid this,
	// but we can't expose an instance of Bridge, so we'll only pay for 
	// access to bridge while constructing copiers, instead of while running them.
	private abstract static class UnsafeFieldCopier {
	    private Bridge bridge ;

	    public UnsafeFieldCopier( Bridge bridge ) {
		this.bridge = bridge ;
	    }

	    abstract void copy( Map oldToNew, long offset, Object src, 
		Object dest, boolean debug ) throws ReflectiveCopyException ;
	}

	// All of the XXXInitializer instances simply set the field to 0 or null
	private static UnsafeFieldCopier byteUnsafeFieldInitializer = 
	    new UnsafeFieldCopier( bridge ) {

	    public void copy( Map oldToNew, long offset, Object src, 
		Object dest, boolean debug ) {
		bridge.putByte( dest, offset, (byte)0 ) ;
	    }

	    public String toString() {
		return "byteUnsafeFieldInitializer" ;
	    }
	} ;

	private static UnsafeFieldCopier charUnsafeFieldInitializer = 
	    new UnsafeFieldCopier( bridge ) {

	    public void copy( Map oldToNew, long offset, Object src, 
		Object dest, boolean debug ) {
		bridge.putChar( dest, offset, (char)0 ) ;
	    }

	    public String toString() {
		return "charUnsafeFieldInitializer" ;
	    }
	} ;

	private static UnsafeFieldCopier shortUnsafeFieldInitializer = 
	    new UnsafeFieldCopier( bridge ) {

	    public void copy( Map oldToNew, long offset, Object src, 
		Object dest, boolean debug ) {
		bridge.putShort( dest, offset, (short)0 ) ;
	    }

	    public String toString() {
		return "shortUnsafeFieldInitializer" ;
	    }
	} ;

	private static UnsafeFieldCopier intUnsafeFieldInitializer = 
	    new UnsafeFieldCopier( bridge ) {

	    public void copy( Map oldToNew, long offset, Object src, 
		Object dest, boolean debug ) {
		bridge.putInt( dest, offset, 0 ) ;
	    }

	    public String toString() {
		return "intUnsafeFieldInitializer" ;
	    }
	} ;

	private static UnsafeFieldCopier longUnsafeFieldInitializer = 
	    new UnsafeFieldCopier( bridge ) {

	    public void copy( Map oldToNew, long offset, Object src, 
		Object dest, boolean debug ) {
		bridge.putLong( dest, offset, 0 ) ;
	    }

	    public String toString() {
		return "longUnsafeFieldInitializer" ;
	    }
	} ;

	private static UnsafeFieldCopier booleanUnsafeFieldInitializer = 
	    new UnsafeFieldCopier( bridge ) {

	    public void copy( Map oldToNew, long offset, Object src, 
		Object dest, boolean debug ) {
		bridge.putBoolean( dest, offset, false ) ;
	    }

	    public String toString() {
		return "booleanUnsafeFieldInitializer" ;
	    }
	} ;

	private static UnsafeFieldCopier floatUnsafeFieldInitializer = 
	    new UnsafeFieldCopier( bridge ) {

	    public void copy( Map oldToNew, long offset, Object src, 
		Object dest, boolean debug ) {
		bridge.putFloat( dest, offset, 0 ) ;
	    }

	    public String toString() {
		return "floatUnsafeFieldInitializer" ;
	    }
	} ;

	private static UnsafeFieldCopier doubleUnsafeFieldInitializer = 
	    new UnsafeFieldCopier( bridge ) {

	    public void copy( Map oldToNew, long offset, Object src, 
		Object dest, boolean debug ) {
		bridge.putDouble( dest, offset, 0 ) ;
	    }

	    public String toString() {
		return "doubleUnsafeFieldInitializer" ;
	    }
	} ;

	private UnsafeFieldCopier getPrimitiveFieldInitializer( Class<?> cls ) {
	    if (cls.equals( byte.class ))
		return byteUnsafeFieldInitializer ;
	    if (cls.equals( char.class ))
		return charUnsafeFieldInitializer ;
	    if (cls.equals( short.class ))
		return shortUnsafeFieldInitializer ;
	    if (cls.equals( int.class ))
		return intUnsafeFieldInitializer ;
	    if (cls.equals( long.class ))
		return longUnsafeFieldInitializer ;
	    if (cls.equals( boolean.class ))
		return booleanUnsafeFieldInitializer ;
	    if (cls.equals( float.class ))
		return floatUnsafeFieldInitializer ;
	    if (cls.equals( double.class ))
		return doubleUnsafeFieldInitializer ;
	    else
		throw new IllegalArgumentException( "cls must be a primitive type" ) ;
	}

	// The XXXUnsafeFieldCopier instances copy a field from source
	// to destination
	private static UnsafeFieldCopier byteUnsafeFieldCopier = 
	    new UnsafeFieldCopier( bridge ) {

	    public void copy( Map oldToNew, long offset, Object src, 
		Object dest, boolean debug ) {
		byte value = bridge.getByte( src, offset ) ;
		bridge.putByte( dest, offset, value ) ;
	    }

	    public String toString() {
		return "byteUnsafeFieldCopier" ;
	    }
	} ;

	private static UnsafeFieldCopier charUnsafeFieldCopier = 
	    new UnsafeFieldCopier( bridge ) {

	    public void copy( Map oldToNew, long offset, Object src, 
		Object dest, boolean debug ) {
		char value = bridge.getChar( src, offset ) ;
		bridge.putChar( dest, offset, value ) ;
	    }

	    public String toString() {
		return "charUnsafeFieldCopier" ;
	    }
	} ;

	private static UnsafeFieldCopier shortUnsafeFieldCopier = 
	    new UnsafeFieldCopier( bridge ) {

	    public void copy( Map oldToNew, long offset, Object src, 
		Object dest, boolean debug ) {
		short value = bridge.getShort( src, offset ) ;
		bridge.putShort( dest, offset, value ) ;
	    }

	    public String toString() {
		return "shortUnsafeFieldCopier" ;
	    }
	} ;

	private static UnsafeFieldCopier intUnsafeFieldCopier = 
	    new UnsafeFieldCopier( bridge ) {

	    public void copy( Map oldToNew, long offset, Object src, 
		Object dest, boolean debug ) {
		int value = bridge.getInt( src, offset ) ;
		bridge.putInt( dest, offset, value ) ;
	    }

	    public String toString() {
		return "intUnsafeFieldCopier" ;
	    }
	} ;

	private static UnsafeFieldCopier longUnsafeFieldCopier = 
	    new UnsafeFieldCopier( bridge ) {

	    public void copy( Map oldToNew, long offset, Object src, 
		Object dest, boolean debug ) {
		long value = bridge.getLong( src, offset ) ;
		bridge.putLong( dest, offset, value ) ;
	    }

	    public String toString() {
		return "longUnsafeFieldCopier" ;
	    }
	} ;

	private static UnsafeFieldCopier booleanUnsafeFieldCopier = 
	    new UnsafeFieldCopier( bridge ) {

	    public void copy( Map oldToNew, long offset, Object src, 
		Object dest, boolean debug ) {
		boolean value = bridge.getBoolean( src, offset ) ;
		bridge.putBoolean( dest, offset, value ) ;
	    }

	    public String toString() {
		return "booleanUnsafeFieldCopier" ;
	    }
	} ;

	private static UnsafeFieldCopier floatUnsafeFieldCopier = 
	    new UnsafeFieldCopier( bridge ) {

	    public void copy( Map oldToNew, long offset, Object src, 
		Object dest, boolean debug ) {
		float value = bridge.getFloat( src, offset ) ;
		bridge.putFloat( dest, offset, value ) ;
	    }

	    public String toString() {
		return "floatUnsafeFieldCopier" ;
	    }
	} ;

	private static UnsafeFieldCopier doubleUnsafeFieldCopier = 
	    new UnsafeFieldCopier( bridge ) {

	    public void copy( Map oldToNew, long offset, Object src, 
		Object dest, boolean debug ) {
		double value = bridge.getDouble( src, offset ) ;
		bridge.putDouble( dest, offset, value ) ;
	    }

	    public String toString() {
		return "doubleUnsafeFieldCopier" ;
	    }
	} ;

	private UnsafeFieldCopier getPrimitiveFieldCopier( Class<?> cls ) {
	    if (cls.equals( byte.class ))
		return byteUnsafeFieldCopier ;
	    if (cls.equals( char.class ))
		return charUnsafeFieldCopier ;
	    if (cls.equals( short.class ))
		return shortUnsafeFieldCopier ;
	    if (cls.equals( int.class ))
		return intUnsafeFieldCopier ;
	    if (cls.equals( long.class ))
		return longUnsafeFieldCopier ;
	    if (cls.equals( boolean.class ))
		return booleanUnsafeFieldCopier ;
	    if (cls.equals( float.class ))
		return floatUnsafeFieldCopier ;
	    if (cls.equals( double.class ))
		return doubleUnsafeFieldCopier ;
	    else
		throw new IllegalArgumentException( "cls must be a primitive type" ) ;
	}
	
	// Various kinds of Object copiers
	//
	// The objectUnsafeFieldCopier is not stateless, as it requires access to the
	// ClassCopierFactory, so it cannot be static.
	private UnsafeFieldCopier objectUnsafeFieldCopier = new UnsafeFieldCopier( bridge ) {
	    /*
	    private void debugCopy( Map oldToNew, long offset, Object src, 
		Object dest ) throws ReflectiveCopyException
	    {
		System.out.println( "START objectUnsafeFieldCopier: src = " + src ) ;

		try {
		    Object obj = bridge.getObject( src, offset ) ;

		    Object result = null ;

		    if (obj != null) {
			// This lookup must be based on the actual type, not the
			// declared type to allow for polymorphism.
			ClassCopier copier = classCopierFactory.getClassCopier( 
			    obj.getClass()) ;

			result = copier.copy( oldToNew, obj, true ) ;
		    }

		    System.out.println( "IN    objectUnsafeFieldCopier: src = " + src 
			+ " successful copy" ) ;

		    bridge.putObject( dest, offset, result ) ;
		} catch (ReflectiveCopyException exc) {
		    System.out.println( "IN    objectUnsafeFieldCopier: src = " + src 
			+ " Exception " + exc ) ;
		    exc.printStackTrace() ;

		    throw exc ;
		} finally {
		    System.out.println( "END   objectUnsafeFieldCopier: src = " + src ) ;
		}
	    }
	    */

	    private void noDebugCopy( Map oldToNew, long offset, Object src, 
		Object dest ) throws ReflectiveCopyException
	    {
		Object obj = bridge.getObject( src, offset ) ;

		Object result = null ;

		if (obj != null) {
		    // This lookup must be based on the actual type, not the
		    // declared type to allow for polymorphism.
		    ClassCopier copier = classCopierFactory.getClassCopier( 
			obj.getClass()) ;

		    result = copier.copy( oldToNew, obj ) ;
		}

		bridge.putObject( dest, offset, result ) ;
	    }

	    public void copy( Map oldToNew, long offset, Object src, 
		Object dest, boolean debug ) throws ReflectiveCopyException
	    {
		/*
		if (debug)
		    debugCopy( oldToNew, offset, src, dest ) ;
		else
		*/
		    noDebugCopy( oldToNew, offset, src, dest ) ;
	    }

	    public String toString() {
		return "objectUnsafeFieldCopier" ;
	    }
	}  ;

	private static UnsafeFieldCopier objectUnsafeFieldInitializer = 
	    new UnsafeFieldCopier( bridge ) {

	    public void copy( Map oldToNew, long offset, Object src, 
		Object dest, boolean debug ) {
		bridge.putObject( dest, offset, null ) ;
	    }

	    public String toString() {
		return "objectUnsafeFieldInitializer" ;
	    }
	} ;

	private static UnsafeFieldCopier objectUnsafeFieldSourceCopier = 
	    new UnsafeFieldCopier( bridge ) {

	    public void copy( Map oldToNew, long offset, Object src, 
		Object dest, boolean debug ) {
		bridge.putObject( dest, offset, src ) ;
	    }

	    public String toString() {
		return "objectUnsafeFieldSourceCopier" ;
	    }
	} ;

	private static UnsafeFieldCopier objectUnsafeFieldResultCopier = 
	    new UnsafeFieldCopier( bridge ) {

	    public void copy( Map oldToNew, long offset, Object src, 
		Object dest, boolean debug ) {
		bridge.putObject( dest, offset, src ) ;
	    }

	    public String toString() {
		return "objectUnsafeFieldResultCopier" ;
	    }
	} ;

	private static UnsafeFieldCopier objectUnsafeFieldIdentityCopier = 
	    new UnsafeFieldCopier( bridge ) {

	    public void copy( Map oldToNew, long offset, Object src, 
		Object dest, boolean debug ) {
		Object value = bridge.getObject( src, offset ) ;
		bridge.putObject( dest, offset, value ) ;
	    }

	    public String toString() {
		return "objectUnsafeFieldIdentityCopier" ;
	    }
	} ;

	private UnsafeFieldCopier getUnsafeFieldCopier( Field fld )
	{
	    Class<?> defType = fld.getDeclaringClass() ;
	    Class<?> fldType = fld.getType() ;
	    Copy copyAnnotation = fld.getAnnotation( Copy.class ) ;

	    if (copyAnnotation == null) {
		if (fldType.isPrimitive()) 
		    return getPrimitiveFieldCopier( fldType ) ;
		else
		    return objectUnsafeFieldCopier;
	    } else {
		switch (copyAnnotation.value()) {
		    case RECURSE:
			if (fldType.isPrimitive()) 
			    return getPrimitiveFieldCopier( fldType ) ;
			else
			    return objectUnsafeFieldCopier;
		    case IDENTITY:
			if (fldType.isPrimitive()) 
			    return getPrimitiveFieldCopier( fldType ) ;
			else
			    return objectUnsafeFieldIdentityCopier;
		    case NULL:
			if (fldType.isPrimitive()) 
			    return getPrimitiveFieldInitializer( fldType ) ;
			else
			    return objectUnsafeFieldInitializer;
		    case SOURCE:
			if (fldType.isAssignableFrom(defType)) {
			    return objectUnsafeFieldSourceCopier ;
			} else {
			    throw new IllegalArgumentException( 
				"Cannot assign field to source object: "
				+ "incompatible types\n" 
				+ "Field type is " + fldType 
				+ " Class type is " + defType ) ;
			}
		    case RESULT:
			if (fldType.isAssignableFrom(defType)) {
			    return objectUnsafeFieldResultCopier ;
			} else {
			    throw new IllegalArgumentException( 
				"Cannot assign field to result object: "
				+ "incompatible types\n" 
				+ "Field type is " + fldType 
				+ " Class type is " + defType ) ;
			}
		    default:
			throw new IllegalArgumentException(
			    "Unhandled case " + copyAnnotation.value() 
				+ " for field " + fld ) ;
		}
	    }
	}

	private boolean fieldIsCopyable( Field field ) {
	    int modifiers = field.getModifiers() ;
	    boolean result = !Modifier.isStatic( modifiers ) ;
	    return result ;
	}

	public ClassFieldCopierUnsafeImpl( Class<?> cls, 
	    PipelineClassCopierFactory ccf ) throws ReflectiveCopyException
	{
	    myClass = cls ;
	    classCopierFactory = ccf ;
	    superCopier = getSuperCopier( ccf, cls ) ;

	    // Count the number of non-static fields.  These are the
	    // ones we must copy.
	    Field[] fields = cls.getDeclaredFields() ;
	    int numFields = 0 ;
	    for (int ctr=0; ctr<fields.length; ctr++)
		if (fieldIsCopyable( fields[ctr] )) 
		    numFields++ ;

	    fieldOffsets = new long[ numFields ] ;
	    fieldCopiers = new UnsafeFieldCopier[ numFields ] ;

	    // Initialze offsets and field copiers for non-static
	    // fields.
	    int pos = 0 ;
	    for (int ctr=0; ctr<fields.length; ctr++) {
		Field fld = fields[ctr] ;
		if (fieldIsCopyable( fld )) {
		    fieldOffsets[pos] = bridge.objectFieldOffset( fld ) ;
		    fieldCopiers[pos] = getUnsafeFieldCopier( fld ) ;
		    pos++ ;
		}
	    }
	}
	
	public String toString() 
	{
	    StringBuilder sb = new StringBuilder() ;
	    sb.append( "ClassFieldCopierUnsafeImpl[" ) ;
	    sb.append( myClass.getName() ) ;
	    for (int ctr=0; ctr<fieldOffsets.length; ctr++) {
		sb.append( "\n\t" ) ;
		sb.append( fieldOffsets[ctr] ) ;
		sb.append( ':' ) ;
		sb.append( fieldCopiers[ctr].toString() ) ;
	    }
	    sb.append( "\n]" ) ;
	    return sb.toString() ;
	}

	/*
	private void debugCopy( Map oldToNew, Object source,
	    Object result ) throws ReflectiveCopyException
	{
	    System.out.println( "START " + this + " : source = " + source ) ;

	    try {
		if (superCopier != null)
		    ((ClassFieldCopierUnsafeImpl)superCopier).debugCopy( 
			oldToNew, source, result ) ;

		for (int ctr=0; ctr<fieldOffsets.length; ctr++ ) {
		    System.out.println( "IN    " + this + " Field " + ctr 
			+ " to be copied by " + fieldCopiers[ctr] ) ;

		    fieldCopiers[ctr].copy( oldToNew, fieldOffsets[ctr],
			source, result, true ) ;
		}
	    } finally {
		System.out.println( "END   ClassFieldCopierUnsafeImpl(" + myClass.getName() 
		    + "): source = " + source ) ;
	    }
	}
	*/

	public void noDebugCopy( Map oldToNew, Object source,
	    Object result ) throws ReflectiveCopyException
	{
	    if (superCopier != null)
		((ClassFieldCopierUnsafeImpl)superCopier).noDebugCopy( 
		    oldToNew, source, result ) ;

	    for (int ctr=0; ctr<fieldOffsets.length; ctr++ ) {
		fieldCopiers[ctr].copy( oldToNew, fieldOffsets[ctr],
		    source, result, false ) ;
	    }
	}

	public void copy( Map oldToNew, Object source, 
	    Object result, boolean debug ) throws ReflectiveCopyException 
	{
	    /* Commented out to avoid overhead of testing debug
	    if (debug)
		debugCopy( oldToNew, source, result ) ;
	    else
	    */
		noDebugCopy( oldToNew, source, result ) ;
	}
    }

//******************************************************************************
//******************************************************************************

    private static Map classToConstructor = 
	DefaultClassCopierFactories.USE_FAST_CACHE ?
	    new FastCache(new WeakHashMap()) :
	    new WeakHashMap() ;

    /** Use bridge with code generated by codegen to copy objects.  
     * This method must be invoked
     * from inside a doPrivileged call.
     */
    private static synchronized ClassFieldCopier 
	makeClassFieldCopierUnsafeCodegenImpl( 
	final Class<?> cls, final PipelineClassCopierFactory classCopierFactory ) 
	throws ReflectiveCopyException
    {
	Constructor cons = (Constructor)classToConstructor.get( cls ) ;
	if (cons == null) {
	    final String className = "com.sun.corba.se.impl.generated.copiers." +
		cls.getName() + "Copier" ;
	    final CodegenCopierGenerator generator = 
		new CodegenCopierGenerator( className, cls ) ;
	    final ProtectionDomain pd = cls.getProtectionDomain() ;
	    final Class<?> copierClass = generator.create( pd, 
		LibraryClassLoader.getClassLoader() ) ;

	    try {
		cons = copierClass.getDeclaredConstructor( 
		    PipelineClassCopierFactory.class, ClassFieldCopier.class ) ;
	    } catch (Exception exc) {
		ReflectiveCopyException rce = new ReflectiveCopyException( 
		    "Could not access unsafe codegen copier constructor" ) ;
		rce.initCause( exc ) ;
		throw rce ;
	    } ;

	    classToConstructor.put( cls, cons ) ;
	}

	ClassFieldCopier copier = null ;

	try {
	    ClassFieldCopier superCopier = getSuperCopier( classCopierFactory,
		cls ) ; 
	    Object[] args = new Object[] { classCopierFactory, superCopier } ;
	    copier = (ClassFieldCopier)cons.newInstance( args ) ;
	} catch (Exception exc) {
	    ReflectiveCopyException rce = new ReflectiveCopyException( 
		"Could not create unsafe codegen copier" ) ;
	    rce.initCause( exc ) ;
	    throw rce ;
	}
	    
	return copier ;
    }

//******************************************************************************
// private data of ClassCopierOrdinaryImpl
//******************************************************************************

    // The full class copier factory, which is needed inside this class.
    private PipelineClassCopierFactory classCopierFactory ; 

    // Actually copies the declared fields in this class.
    private ClassFieldCopier classFieldCopier ;	    

    // The appropriate constructor for this class, as defined by Java 
    // serialization, or the default constructor if the class is not serializable.
    private Constructor constructor ;		    

    // Null unless the class defines a readResolve() method.
    private Method readResolveMethod ;		    

//******************************************************************************
// Implementation
//******************************************************************************

    public ClassCopierOrdinaryImpl( PipelineClassCopierFactory ccf, 
	Class<?> cls ) throws ReflectiveCopyException
    {
	super( cls.getName(), true ) ;

	classCopierFactory = ccf ;
	classFieldCopier = getClassFieldCopier( cls, ccf ) ;
	constructor = ConstructorFactory.makeConstructor( cls ) ;
	readResolveMethod = getInheritableMethod( cls, 
	    "readResolve", Object.class );
    
	// XXX handle custom marshalled objects.
    }

    public Object createCopy( Object source, boolean debug ) throws ReflectiveCopyException
    {
	try { 
	    return constructor.newInstance() ;
	} catch (Exception exc) {
	    // XXX log this
	    throw new ReflectiveCopyException(
		"Failure in newInstance for constructor " + constructor, exc ) ;
	}
    }

    // Copy all of the non-static fields from source to dest, starting with 
    // java.lang.Object.  
    public Object doCopy( Map oldToNew, Object source, 
	Object result, boolean debug ) throws ReflectiveCopyException
    {
	if (source instanceof CopyInterceptor) {
	    // Note that result will also be an instance of CopyInterceptor in this case.
	    ((CopyInterceptor)source).preCopy() ;
	    classFieldCopier.copy( oldToNew, source, result, debug ) ;
	    ((CopyInterceptor)result).postCopy() ;

	    return resolve( result ) ;
	} else {
	    classFieldCopier.copy( oldToNew, source, result, debug ) ;

	    return resolve( result ) ;
	}
    }
}
