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
package com.sun.corba.se.impl.encoding.fast ;

import java.util.concurrent.ConcurrentMap ;
import java.util.concurrent.ConcurrentHashMap ;
import java.lang.ref.ReferenceQueue ;
import java.lang.ref.Reference ;
import java.lang.ref.WeakReference ;
import java.lang.ref.SoftReference ;

import java.security.AccessController ;
import java.security.PrivilegedAction ;

import com.sun.corba.se.spi.orbutil.generic.UnaryFunction ;
import com.sun.corba.se.spi.orbutil.generic.Holder ;

/** A fast, concurrent, lock-free table that associates data with a key.
 * Typically the key is a Class, and the data may also reference the key.
 * This LookupTable will not pin either key or data in memory, and as
 * such can only be used as a cache for information that can be 
 * re-computed as needed.
 * The data itself may reference the class without causing class memory leaks.
 * This is based on the JDK 6 code in ObjectStreamClass, slightly generalized and
 * abstracted here.
 *
 * @author Ken Cavanaugh
 */
public class LookupTableConcurrentImpl<K,V> implements LookupTable<K,V> {
    private final ConcurrentMap<WeakKey<K>,Reference<?>> localDescs =
	new ConcurrentHashMap<WeakKey<K>,Reference<?>>();
    private final ReferenceQueue<K> localDescsQueue = 
	new ReferenceQueue<K>();

    private final UnaryFunction<? super K,? extends V> factory ;
    private final Class<?> token ;

    /** Create a lookup table.
     * @param factory The factory used to create a value for the key if
     * no value is already present in the table
     * @param token A class type token for the type of value stored in the
     * table.
     */
    public LookupTableConcurrentImpl( UnaryFunction<? super K,? extends V> factory,
	Class<?> token ) {

	this.factory = factory ;
	this.token = token ;
    }

    /** (Internal) callers
     * which receive an EntryFuture belonging to another thread as the result
     * of a lookup should call the get() method of the EntryFuture; this will
     * return the actual entry once it is ready for use and has been set().  To
     * conserve objects, EntryFutures synchronize on themselves.
     */
    private static class EntryFuture<K> {
	private static final Object unset = new Object();

	private final Thread owner = Thread.currentThread();
	private Object entry = unset;

	/**
	 * Attempts to set the value contained by this EntryFuture.  If the
	 * EntryFuture's value has not been set already, then the value is
	 * saved, any callers blocked in the get() method are notified, and
	 * true is returned.  If the value has already been set, then no saving
	 * or notification occurs, and false is returned.
	 */
	synchronized boolean set( final Object entry) {
	    if (this.entry != unset) {
		return false;
	    }
	    this.entry = entry;
	    notifyAll();
	    return true;
	}
	
	/**
	 * Returns the value contained by this EntryFuture, blocking if
	 * necessary until a value is set.
	 */
	synchronized Object get() {
	    boolean interrupted = false;
	    while (entry == unset) {
		try { 
		    wait(); 
		} catch (InterruptedException ex) {
		    interrupted = true;
		}
	    }
	    if (interrupted) {
		AccessController.doPrivileged(
		    new PrivilegedAction<Object>() {
			public Object run() {
			    Thread.currentThread().interrupt();
			    return null;
			}
		    }
		);
	    }
	    return entry;
	}

	/**
	 * Returns the thread that created this EntryFuture.
	 */
	Thread getOwner() {
	    return owner;
	}
    }

    /**
     * Removes from the specified map any keys that have been enqueued
     * on the specified reference queue.
     */
    void processQueue( ReferenceQueue<K> queue, 
	ConcurrentMap<? extends WeakReference<K>, ?> map) {

	Reference<? extends K> ref;
	while((ref = queue.poll()) != null) {
	    map.remove(ref);
	}    
    }

    // Weak key for objects of type K.
    static class WeakKey<K> extends WeakReference<K> {
	/**
	 * saved value of the referent's identity hash code, to maintain
	 * a consistent hash code after the referent has been cleared
	 */
	private final int hash;

	/**
	 * Create a new WeakKey<K> to the given object, registered 
	 * with a queue.
	 */
	WeakKey(K cl, ReferenceQueue<K> refQueue) {
	    super(cl, refQueue);
	    hash = System.identityHashCode(cl);
	}

	/**
	 * Returns the identity hash code of the original referent.
	 */
        @Override
	public int hashCode() {
	    return hash;
	}

	/**
	 * Returns true if the given object is this identical 
	 * WeakKey<K> instance, or, if this object's referent has not 
	 * been cleared, if the given object is another WeakKey<K> 
	 * instance with the identical non-null referent as this one.
	 */
        @Override
        @SuppressWarnings("unchecked")
	public boolean equals(Object obj) {
	    if (obj == this) {
		return true;
	    }

	    if (obj instanceof WeakKey) {
		Object referent = get();
		return (referent != null) && 
		       (referent == ((WeakKey<K>) obj).get());
	    } else {
		return false;
	    }
	}
    }

    @SuppressWarnings({"unchecked", "unchecked", "unchecked"})
    public V lookup(Holder<Boolean> firstTime, K k) {
	if (firstTime != null) {
	    firstTime.content( false ) ;
	}

	processQueue(localDescsQueue, localDescs);
	WeakKey<K> key = new WeakKey<K>(k, localDescsQueue);
	Reference<?> ref = localDescs.get(key);
	Object entry = null;

	if (ref != null) {
	    entry = ref.get();
	}

	EntryFuture<?> future = null;
	if (entry == null) {
	    EntryFuture<?> newEntry = new EntryFuture();
	    Reference<?> newRef = new SoftReference<EntryFuture<?>>(newEntry);
	    do {
		if (ref != null) {
		    localDescs.remove(key, ref);
		}
		
		ref = localDescs.putIfAbsent(key, newRef);

		if (ref != null) {
		    entry = ref.get();
		}
	    } while (ref != null && entry == null);
	    if (entry == null) {
		future = newEntry;
	    }
	}
	
	if (token.isInstance( entry ) ) {  // check common case first
	    return (V)entry ;
	}

	if (entry instanceof EntryFuture) {
	    future = (EntryFuture<?>) entry;
	    if (future.getOwner() == Thread.currentThread()) {
		/*
		 * Handle nested call situation described by 4803747: waiting
		 * for future value to be set by a lookup() call further up the
		 * stack will result in deadlock, so calculate and set the
		 * future value here instead.
		 */
		entry = null;
	    } else {
		entry = future.get();
	    }
	}

	if (entry == null) {
	    try {
		entry = factory.evaluate( k ) ;
		if (firstTime != null) {
                    firstTime.content(true);
                }
	    } catch (Throwable th) {
		entry = th;
	    }
	    if (future.set(entry)) {
		localDescs.put(key, new SoftReference<Object>(entry));
	    } else {
		// nested lookup call already set future
		entry = future.get();
	    }
	}
	
	if (token.isInstance( entry )) {
	    return (V)entry ;
	} else if (entry instanceof RuntimeException) {
	    throw (RuntimeException) entry;
	} else if (entry instanceof Error) {
	    throw (Error) entry;
	} else {
	    throw new InternalError("unexpected entry: " + entry);
	}
    }
}
