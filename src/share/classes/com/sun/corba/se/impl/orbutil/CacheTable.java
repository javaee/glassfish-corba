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
package com.sun.corba.se.impl.orbutil;
import org.omg.CORBA.INTERNAL;
import org.omg.CORBA.CompletionStatus;

import com.sun.corba.se.spi.orb.ORB;

import com.sun.corba.se.impl.logging.ORBUtilSystemException;

import com.sun.corba.se.impl.orbutil.ORBUtility ;

/** This is a hash table implementation that simultaneously maps key to value
 * and value to key.  It is used for marshalling and unmarshalling value types,
 * where it is necessary to track the correspondence between object instances
 * and their offsets in a stream.  It is also used for tracking indirections for
 * Strings that represent codebases and repositoryids.
 * Since the offset is always non-negative,
 * only non-negative values should be stored here (and storing -1 will cause
 * failures).  Also note that the same key (Object) may be stored with multiple
 * values (int offsets) due to the way readResolve works (see also GlassFish issue 1605).
 * <P>
 * Note that this class cannot be generified, as any arbitrary object can be a key.
 *
 * @since 1.1
 *
 * @author Ken Cavanaugh
 */
public class CacheTable<K> {
    private class Entry<K> {
        K key;
        int val;
        Entry next;  // this chains the collision list of table "map"
        Entry rnext; // this chains the collision list of table "rmap" 
        public Entry(K k, int v) {
            key = k;
            val = v;
            next = null;
	    rnext = null;
        } 
    }

    private boolean noReverseMap;
    private String cacheType ;

    // size must be power of 2
    private static final int INITIAL_SIZE = 64 ;
    private static final int MAX_SIZE = 1 << 30;
    private static final int INITIAL_THRESHHOLD = 48 ; 
    private int size;
    private int threshhold ;
    private int entryCount;
    private Entry<K>[] map;
    private Entry<K>[] rmap;
      
    private ORB orb;
    private ORBUtilSystemException wrapper;

    public  CacheTable(String cacheType, ORB orb, boolean u) {
        this.orb = orb;
	this.cacheType = cacheType ;
	wrapper = orb.getLogWrapperTable().get_RPC_ENCODING_ORBUtil() ;
	noReverseMap = u;
        size = INITIAL_SIZE;
	threshhold = INITIAL_THRESHHOLD ;
	entryCount = 0;
	initTables();
    }

    private void initTables() {
	map = new Entry[size];
	if (noReverseMap) 
	    rmap = null ;
	else
	    rmap = new Entry[size] ;
    }

    private void grow() {
	if (size == MAX_SIZE)
	    return;	

	Entry [] oldMap = map;
        int oldSize = size;
	size <<= 1;
	threshhold <<= 1 ;

	initTables();
	// now rehash the entries into the new table
	for (int i = 0; i < oldSize; i++) {
	    for (Entry<K> e = oldMap[i]; e != null; e = e.next)
	        put_table(e.key, e.val);
	}
    }

    private int hashModTableSize(int h) {
	// This is taken from the hash method in the JDK 6 HashMap.
	// This is used for both the
	// key and the value side of the mapping.  It's not clear
	// how useful this is in this application, as the low-order
	// bits change a lot for both sides.  
	h ^= (h >>> 20) ^ (h >>> 12) ;
	return (h ^ (h >>> 7) ^ (h >>> 4)) & (size - 1) ;
    }

    private int hash(K key) {
	return hashModTableSize(System.identityHashCode(key));
    }

    private int hash(int val) {
	return hashModTableSize(val);
    }

    /** Store the (key,val) pair in the hash table, unless 
     * (key,val) is already present.  Returns true if a new (key,val)
     * pair was added, else false.  val must be non-negative, but
     * this is not checked.
     */
    public final void put(K key, int val) {
	if (put_table(key, val)) {
	    entryCount++;
	    if (entryCount > threshhold)
		grow();
	}
    }

    private boolean put_table(K key, int val) {
	if (orb.cdrCacheDebugFlag)
	    ORBUtility.dprint( this, cacheType + " put_table: " 
		+ "key=" + key + "(" + System.identityHashCode( key ) + ") "
		+ "val=" + val + "(" + val + ")" ) ;

	int index = hash(key);

	for (Entry e = map[index]; e != null; e = e.next) {
	    if (e.key == key) {
	        if (e.val != val) {
		    // duplicateIndirectionOffset error here is not an error:
		    // A serializable/externalizable class that defines 
		    // a readResolve method that creates a canonical representation
		    // of a value can legally have the same key occuring at 
		    // multiple values.  This is GlassFish issue 1605.
		    // Note: we store this anyway, so that getVal can find the key.
                    wrapper.duplicateIndirectionOffset();
		} else {	
		    // if we get here we are trying to put in the same key/val pair
		    // this is a no-op, so we just return
		    return false;
		}
	    }
        }
	
	Entry newEntry = new Entry(key, val);
	newEntry.next = map[index];
	map[index] = newEntry;
	if (!noReverseMap) {
	    int rindex = hash(val);
	    newEntry.rnext = rmap[rindex];
	    rmap[rindex] = newEntry;
        }

	return true;
    }

    public final boolean containsKey(K key) {
	return (getVal(key) != -1);
    }

    /** Returns some int val where (key,val) is in this CacheTable.
     */
    public final int getVal(K key) {
	int index = hash(key);
        for (Entry e = map[index]; e != null; e = e.next) {
            if (e.key == key) 
                return e.val;
        }

        return -1;
    }

    public final boolean containsVal(int val) {
	return (getKey(val) != null); 
    }

    /** Return the key where (key,val) is present in the map.
     */
    public final K getKey(int val) {
	if (noReverseMap)
	    throw wrapper.getKeyInvalidInCacheTable() ;

	int index = hash(val);
        for (Entry<K> e = rmap[index]; e != null; e = e.rnext) {
            if (e.val == val)
                return e.key;
        }

        return null;
    }

    public void done() {
	map = null;
        rmap = null;
    }
}
