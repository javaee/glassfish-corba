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

import java.util.Set ;
import java.util.Map ;
import java.util.AbstractMap ;

/** A cache intended to help speed up access to a Map.
 * The idea is that some maps have a few values that are retrieved
 * more frequently than others.  So, we create a fixed size array
 * that holds keys and values, and do a very fast hash on the key's
 * identityHashCode.  The cache is backed by a map, which can be
 * an IdentityHashMap, or any other map (such as a WeakHashMap)
 * where the keys satisfy k1.equals(k2) implies k1 == k2.
 * Note that all put operations MUST go through this class, 
 * because calling put on the underlying map can result in
 * the cache returning incorrect results for get.
 */
public class FastCache<K,V> extends AbstractMap<K,V> {
    public static final int TABLE_SIZE = 256 ; // must be a power of 2

    private Map<K,V> map ;
    private Object[] keys = new Object[256] ;
    private Object[] values = new Object[256] ;

    private long cacheCount = 0 ;
    private long totalCount = 0 ;

    public long getCacheCount() {
	return cacheCount ;
    }

    public long getTotalCount() {
	return totalCount ;
    }

    public FastCache( Map<K,V> map ) {
	this.map = map ;
    }

    public Set<Map.Entry<K,V>> entrySet() {
	return map.entrySet() ;
    }

    private int hash( Object key ) {
	// int hv = key.hashCode() ;
	int hv = System.identityHashCode( key ) ;
	return hv & (TABLE_SIZE-1) ;
    }

    public V get( Object key ) {
	totalCount++ ;
	int slot = hash( key ) ;
	K ckey = (K)keys[slot] ;
	if (ckey == key ) {
	    cacheCount++ ;
	    return (V)values[slot] ;
	} else {
	    V result = map.get( key ) ;
	    keys[slot] = key ;
	    values[slot] = result ;
	    return result ;
	}
    }

    /** Put the key and value in the cache and the underlying
     * map.  This writes through to the map, rather than
     * first storing a value in the cache which is only
     * written as required, because that makes it easier
     * to preserve the correct behavior of the map.
     */
    public V put( K key, V value ) {
	int slot = hash( key ) ;
	keys[slot] = key ;
	values[slot] = value ;
	return map.put( key, value ) ;
    }
}
