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

package com.sun.corba.se.spi.orbutil.generic ;

import java.util.concurrent.locks.ReentrantReadWriteLock ;

/** This is a thread safe wrapper for data that allows many threads
 * to read the data, but only a single thread to write it.
 * The idea is that the first access to the data causes it to
 * be initialized by the compute() method.  This requires a
 * writeLock.  Normal access to the data only requires a
 * read lock, so normally any number of threads can access
 * the data without contention.  It is also possible to
 * flush that cached data.
 * <p>
 * Typical use of this class looks something like:
 * <pre>
 * private CachedData<Foo> myfoo = new CachedData() {
 *     protected Foo compute() {
 *         ...
 *         return new Foo( ... ) ;
 *     }
 * } ;
 * ...
 * Foo foo = myfoo.get() ;
 * try {
 *     .... // do something with foo
 * } finally {
 *     myfoo.release() ;
 * }
 * </pre>
 * Note in particular that every get call must be paired
 * with a release call, otherwise any attempt to call
 * flush() will block indefinitely.
 */
public abstract class CachedData<T> {
    private T data = null ;
    private volatile boolean cacheValid = false ;
    private ReentrantReadWriteLock rwl = new ReentrantReadWriteLock() ;

    /** Must be overridden in a subclass to supply the
     * necessary computation of the cached data.
     */
    protected abstract T compute() ;

    /** Discard the cached data.  Acquires a write lock.
     */
    public void flush() {
	rwl.writeLock().lock() ;
	data = null ;
	cacheValid = false ;
	rwl.writeLock().unlock() ;
    }

    /** Get a copy of the data.  If the data is cached,
     * this only needs a read lock, otherwise it needs
     * a write lock only while calling compute().  The 
     * write lock is downgraded to a read lock after
     * the compute() call.  The read lock is always
     * held after this call completes.  Any exceptions
     * thrown by compute are ignored, but will cause
     * the result to be null.
     */
    public T get() {
	rwl.readLock().lock() ;
	if (!cacheValid) {
	    // Drop read lock, acquire write lock
	    rwl.readLock().unlock() ;
	    rwl.writeLock().lock() ;

	    try {
		// Make sure that cacheValid is set before
		// compute is called in case compute throws
		// an exception (which is ignored here).
		if (!cacheValid) {
		    cacheValid = true ;
		    data = compute() ;
		}
	    } catch (Exception exc) {
		// XXX may want to log
	    } finally {
		// downgrade write lock to read lock
		rwl.readLock().lock() ;
		rwl.writeLock().unlock() ;
	    }
	}

	return data ;
    }

    /** Release the read lock that is held by the call to get().
     */
    public void release() {
	rwl.readLock().unlock() ;
    }
}
