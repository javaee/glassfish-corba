/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright (c) 1997-2018 Oracle and/or its affiliates. All rights reserved.
 *
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common Development
 * and Distribution License("CDDL") (collectively, the "License").  You
 * may not use this file except in compliance with the License.  You can
 * obtain a copy of the License at
 * https://oss.oracle.com/licenses/CDDL+GPL-1.1
 * or LICENSE.txt.  See the License for the specific
 * language governing permissions and limitations under the License.
 *
 * When distributing the software, include this License Header Notice in each
 * file and include the License file at LICENSE.txt.
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

package com.sun.corba.ee.spi.transport.concurrent ;

import com.sun.corba.ee.impl.transport.concurrent.ConcurrentQueueBlockingImpl ;
import com.sun.corba.ee.impl.transport.concurrent.ConcurrentQueueNonBlockingImpl ;
import com.sun.corba.ee.impl.transport.concurrent.ConcurrentQueueImpl ;

/** A factory class for creating instances of ConcurrentQueue.
 * Note that a rather unusual syntax is needed for calling these methods:
 *
 * ConcurrentQueueFactory.<V>makeXXXConcurrentQueue() 
 *
 * This is required because the type variable V is not used in the
 * parameters of the factory method, so the correct type
 * cannot be inferred by the compiler.
 */
public final class ConcurrentQueueFactory {
    private ConcurrentQueueFactory() {} 

    /** Create a ConcurrentQueue whose implementation never blocks.
     * Currently not fully implemented: the NonBlocking and Blocking
     * impls are basically the same.
     */
    public static <V> ConcurrentQueue makeNonBlockingConcurrentQueue(final long ttl ) {
        return new ConcurrentQueueNonBlockingImpl<V>( ttl ) ;
    }

    /** Create a ConcurrentQueue whose implementation uses conventional
     * locking to protect the data structure.
     */
    public static <V> ConcurrentQueue makeBlockingConcurrentQueue(final long ttl ) {
        return new ConcurrentQueueBlockingImpl<V>( ttl ) ;
    }

    /** Create a ConcurrentQueue that does no locking at all.
     * For use in data structures that manage their own locking.
     */
    public static <V> ConcurrentQueue makeConcurrentQueue(final long ttl ) {
        return new ConcurrentQueueImpl<V>( ttl ) ;
    }
}
