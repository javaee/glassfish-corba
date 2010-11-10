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

package com.sun.corba.se.spi.orbutil.misc;

import java.util.Map;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

/** A simple cache with weak keys.  get may be called safely with good
 * concurrency by multiple threads.  In order to use this, some reasonable
 * properties are expected:
 * <ul>
 * <li>The value is a function of only the key, so it may be safely cached.
 * <li>get operations are very common on the same key.
 * <li>Values may occasionally disappear from the cache, in which case
 * they will just be recomputed on the next get() call.
 * </ul>
 *
 * @author ken_admin
 */
public abstract class WeakCache<K,V> {
    private final ReadWriteLock lock ;
    private final Map<K,V> map ;

    public WeakCache() {
        lock = new ReentrantReadWriteLock() ;
        map = new WeakHashMapSafeReadLock<K,V>() ;
    }

    /** Must be implemented in a subclass.  Must compute a
     * value corresponding to a key.  The computation may be fairly
     * expensive.  Note that no lock is held during this computation.
     *
     * @param key Key value for which a value must be computed.
     * @return The resulting value.
     */
    protected abstract V lookup( K key ) ;

    /** Remove any value associated with the key.
     * 
     * @param key Key to value that may be in cache.
     * @return value from the cache, or null if none.
     */
    public V remove( K key ) {
        lock.writeLock().lock();
        try {
            return map.remove( key ) ;
        } finally {
            lock.writeLock().unlock();
        }
    }

    /** Return the value (if any) associated with key.
     * If the value is already in the cache, only a read lock is held,
     * so many threads can concurrently call get.  If no value is in
     * the cache corresponding to key, a new value will be computed and
     * cached, in which case a write lock is held long enough to update
     * the map.  Note that the write lock is NOT held while the value
     * is computed by calling the lookup method.  Because of this, it
     * is possible for redundant computation to occur when two or more
     * thread concurrently call get on the same key which is not (yet) in
     * the cache.
     *
     * @param key
     * @return
     */
    public V get( K key ) {
        lock.readLock().lock() ;
        boolean readLocked = true ;
        try {
            V value = map.get( key ) ;
            if (value == null) {
                readLocked = false ;
                lock.readLock().unlock();

                value = lookup(key) ;

                lock.writeLock().lock();
                try {
                    V current = map.get( key ) ;
                    if (current == null) {
                        // Only put if this is the first time
                        map.put( key, value ) ;
                    } else {
                        value = current ;
                    }
                } finally {
                    lock.writeLock().unlock();
                }
            }

            return value ;
        } finally {
            if (readLocked) {
                lock.readLock().unlock();
            }
        }
    }

    /** Remove all entries from the cache.
     *
     */
    public void clear() {
        map.clear();
    }
}
