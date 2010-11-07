/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright (c) 1997-2010 Oracle and/or its affiliates. All rights reserved.
 * 
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common Development
 * and Distribution License("CDDL") (collectively, the "License").  You
 * may not use this file except in compliance with the License.  You can
 * obtain a copy of the License at
 * https://glassfish.dev.java.net/public/CDDL+GPL_1_1.html
 * or packager/legal/LICENSE.txt.  See the License for the specific
 * language governing permissions and limitations under the License.
 * 
 * When distributing the software, include this License Header Notice in each
 * file and include the License file at glassfish/bootstrap/legal/LICENSE.txt.
 * 
 * GPL Classpath Exception:
 * Oracle designates this particular file as subject to the "Classpath"
 * exception as provided by Oracle in the GPL Version 2 section of the License
 * file that accompanied this code.
 * 
 * Modifications:
 * If applicable, add the following below the License Header, with the fields
 * enclosed by brackets [] replaced by your own identifying information:
 * "Portions Copyright [year] [name of copyright owner]"
 * 
 * Contributor(s):
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

import java.util.LinkedHashMap ;
import java.util.IdentityHashMap ;

import java.lang.reflect.Field ;
import java.lang.reflect.Method ;
import java.lang.reflect.Constructor ;

import java.security.AccessControlContext ;

import com.sun.corba.se.spi.orbutil.copyobject.ReflectiveCopyException ;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.Properties;
import java.util.TreeMap;
import java.util.WeakHashMap;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentSkipListMap;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import java.util.logging.Logger ;
import java.util.logging.LogManager ;

/** A factory used for creating ClassCopier instances.
 * An instance of this factory can be created and customized to
 * handle special copying semantics for certain classes.
 * This maintains a cache of ClassCopiers, so that a ClassCopier is
 * never created more than once for a particular class.
 */
public class ClassCopierFactoryPipelineImpl implements 
    PipelineClassCopierFactory 
{
    // Note that we reflectively copy many transient fields,
    // because otherwise we could not copy quite a few
    // important classes.  For example, we copy Map classes
    // reflectively in the normal case this way.  However,
    // this can be a rather risky procedure: some classes
    // should never be copied this way.
    // 
    // Putting these sorts of fields in a Serializable class
    // should normally be avoided, and so we won't try
    // to optimize these cases, but we do want to avoid
    // problems.  Consequently we will allow the code
    // to fallback to a stream copier, which may
    // take advantage of readObject/writeObject to 
    // deal with some of these cases.

    // notCopyable contains some common classes that should
    // never be copied reflectively.  This is an optimization,
    // as these would also be picked up in DefaultClassCopierFactory
    // in the internal notCopyable method.
    private static final Class<?>[] notCopyable = new Class<?>[] {
	Thread.class, 
	ThreadGroup.class, 
	ProcessBuilder.class,
    } ;

    // List of some immutable classes that are copied simply
    // with the identity copier.
    private static final Class<?>[] immutable = new Class<?>[] {
	Process.class,
	Class.class,
	ClassLoader.class,
	SecurityManager.class,
	Runtime.class,
	System.class,
	Package.class,
	Field.class,
	Method.class,
	Constructor.class,
	AccessControlContext.class,
	Object.class,
	String.class,
	Byte.class,
	Character.class,
	Short.class,
	Integer.class,
	Long.class,
	Double.class,
	Float.class,
	Boolean.class,
        Logger.class,
        LogManager.class
    } ;

    private static final Class<?>[] mapClasses = {
        ConcurrentHashMap.class,
        ConcurrentSkipListMap.class,
        EnumMap.class,
        HashMap.class,
        Hashtable.class,
        IdentityHashMap.class,
        LinkedHashMap.class,
        Properties.class,
        TreeMap.class,
        WeakHashMap.class
    } ;


    private CachingClassCopierFactory cacheFactory ;
    private ClassCopierFactory specialFactory ;
    private ClassCopierFactory arrayFactory ;
    private ClassCopierFactory ordinaryFactory ;
    private ClassCopier errorCopier ;

    public ClassCopierFactoryPipelineImpl() {
	// Set up internal ClassCopierFactory instances
	cacheFactory = 
	    DefaultClassCopierFactories.makeCachingClassCopierFactory() ;
	specialFactory = 
	    DefaultClassCopierFactories.getNullClassCopierFactory() ;
	arrayFactory = 
	    DefaultClassCopierFactories.makeArrayClassCopierFactory( this ) ;
	ordinaryFactory = 
	    DefaultClassCopierFactories.makeOrdinaryClassCopierFactory( this ) ;
	errorCopier = DefaultClassCopiers.getErrorClassCopier() ;

	// Register Immutables
	for (Class<?> cls : immutable) {
            registerImmutable(cls);
        }

	ClassCopier mapCopier = 
	    DefaultClassCopiers.makeMapClassCopier( this ) ;
	
	// Note that the identity hash map can never be copied by reflection, 
        // due to the identity equality semantics required by NULL_KEY.
	// This also means that no subclass can ever be copied by
	// reflection.  
        // 
        // Another problem is that Linked classes (like LinkedHashMap) can
        // cause stack overflow if analyzed reflectively (issue 13996),
        // so make sure that LinkedHashMap is copied this way as well.
        for (Class<?> cls : mapClasses) {
            cacheFactory.put( cls, mapCopier ) ;
        }

	// Make sure that all non-copyable classes have the error
	// copier in the cache.
	for (Class<?> cls : notCopyable) {
            cacheFactory.put(cls, errorCopier);
        }
    }

    public boolean reflectivelyCopyable( Class<?> cls ) 
    {
	for (Class<?> cl : notCopyable) {
            if (cls == cl) {
                return false;
            }
        }

	return true ;
    }

    /** Look for cls only in the cache; do not create a ClassCopier
     * if there isn't one already in the cache.
     */
    public ClassCopier lookupInCache( Class<?> cls ) {
	try {
	    // TIME enter_lookupInCache
	    return cacheFactory.getClassCopier( cls ) ;
	    // TIME exit_lookupInCache
	} catch (ReflectiveCopyException exc) {
	    // ignore this: it cannot occur on the cache class copier get.
	    return null ;
	}
    }

    /** Register an immutable class, so that it will not be copied, but just
     * passed by reference.
     */
    public synchronized final void registerImmutable( Class<?> cls ) {
	cacheFactory.put( cls, DefaultClassCopiers.getIdentityClassCopier() ) ;
    }

    /** Set a special ClassCopierFactory to handle some application specific 
     * needs.
     */
    public void setSpecialClassCopierFactory( ClassCopierFactory ccf ) {
	specialFactory = ccf ;
    }

    public ReentrantReadWriteLock rwlock = new ReentrantReadWriteLock() ;


     /** Analyze cls to determine the appropriate ClassCopier
     * and return the ClassCopier instance.  Will only create
     * a ClassCopier for a given Class once.
     */
    public ClassCopier getClassCopier( 
	// TIME enter_getClassCopier
	Class<?> cls ) throws ReflectiveCopyException {
	if (cls.isInterface()) {
            // XXX use Exceptions class here
            throw new IllegalArgumentException(
                "Cannot create a ClassCopier for an interface.");
        }

        rwlock.readLock().lock() ;
        boolean readLocked = true ;
        try {
            ClassCopier result = cacheFactory.getClassCopier( cls ) ;
            if (result == null) {
                // New for Java SE 5.0: all Enums are immutable.
                // We'll figure that out here and cache the result.
                if (Enum.class.isAssignableFrom(cls)) {
                    result = DefaultClassCopiers.getIdentityClassCopier();
                }
                if (result == null) {
                    result = specialFactory.getClassCopier(cls);
                }
                if (result == null) {
                    result = arrayFactory.getClassCopier(cls);
                }
                if (result == null) {
                    result = ordinaryFactory.getClassCopier(cls);
                }
                if (result == null) {
                    // XXX use Exceptions
                    throw new IllegalStateException(
                        "Could not find ClassCopier for " + cls.getClass());
                }

                // Result was not cached, so update the cache
                rwlock.readLock().unlock() ;
                readLocked = false ;
                rwlock.writeLock().lock() ;
                try {
                    if (cacheFactory.getClassCopier(cls) == null) {
                        cacheFactory.put( cls, result ) ;
                    }
                } finally {
                    rwlock.writeLock().unlock() ;
                }
            }

            if (result == errorCopier) {
                // Throw the exception early, since there is
                throw new ReflectiveCopyException( "Cannot copy class " + cls ) ;
            }

            // TIME exit_getClassCopier
            return result ;
        } finally {
            if (readLocked) {
                rwlock.readLock().unlock() ;
            }
        }
    }
}
