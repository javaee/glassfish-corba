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

import java.util.concurrent.Callable ;
import java.util.concurrent.Future ;
import java.util.concurrent.FutureTask ;
import java.util.concurrent.ConcurrentMap ;
import java.util.concurrent.ConcurrentHashMap ;
import org.glassfish.pfl.basic.contain.Holder;
import org.glassfish.pfl.basic.func.UnaryFunction;

public class LookupTableSimpleConcurrentImpl<K,V> implements LookupTable<K,V> {
    ConcurrentMap<K,Object> map = new ConcurrentHashMap<K,Object>() ;

    private final UnaryFunction<K,V> factory ;
    private final Class<V> token ;

    /** Create a lookup table.
     * @param factory The factory used to create a value for the key if
     * no value is already present in the table
     * @param token A class type token for the type of value stored in the
     * table.
     */
    public LookupTableSimpleConcurrentImpl( UnaryFunction<K,V> factory, 
	Class<V> token ) {

	this.factory = factory ;
	this.token = token ;
    }

    public V lookup(final Holder<Boolean> firstTime, final K key) {
	// There are several possible results:
	// 1. obj is null
	// 2. obj is an instance of the token.
	// 3. obj is a FutureTask<V>.
	Object obj = map.get( key ) ;
	if (firstTime != null)
	    firstTime.content( true ) ;

	if (obj == null) {
	    Future<V> ft = new FutureTask<V>( 
		new Callable<V>() {
		    public V call() {
			if (firstTime != null) 
			    firstTime.content( true ) ;

			return factory.evaluate( key ) ;
		    }
		} 
	    ) ;

	    obj = map.putIfAbsent( key, ft ) ;

	    // Now obj is either a Future<V>, or a V
	    // if another thread got here first.
	} 
	
	if (token.isInstance( obj )) {
	    return token.cast( obj ) ;
	} else {
	    // assert FutureTask.isInstance( obj )

	    Future<V> ft = (Future<V>)obj ;
	    
	    //
	    // block if necessary until result is ready.
	    V result = null ;
	    try {
		result = ft.get() ;
	    } catch (Exception exc) {
		// XXX what should we do here? InterruptedException, ExecutionException
	    }

	    // make sure that the table contains the result, not the future
	    map.replace( key, ft, result ) ;

	    return result ;
	}
    }
}

