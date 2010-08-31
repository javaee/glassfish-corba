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
package com.sun.corba.se.impl.encoding;

import java.nio.charset.CharsetDecoder;
import java.nio.charset.CharsetEncoder;
import java.util.WeakHashMap;

/**
 * Thread local cache of sun.io code set converters for performance.
 *
 * The thread local class contains a single reference to a Map[]
 * containing two WeakHashMaps.  One for CharsetEncoders and
 * one for CharsetDecoders.  Constants are defined for indexing.
 *
 * This is used internally by CodeSetConversion.
 */
class CodeSetCache
{
    private ThreadLocal<WeakHashMap<String,CharsetEncoder>> ctbMapLocal =
	new ThreadLocal<WeakHashMap<String,CharsetEncoder>>() {
	    protected WeakHashMap<String,CharsetEncoder> initialValue() {
		return new WeakHashMap<String,CharsetEncoder>() ;
	    }
	} ;

    private ThreadLocal<WeakHashMap<String,CharsetDecoder>> btcMapLocal =
	new ThreadLocal<WeakHashMap<String,CharsetDecoder>>() {
	    protected WeakHashMap<String,CharsetDecoder> initialValue() {
		return new WeakHashMap<String,CharsetDecoder>() ;
	    }
	} ;

    /**
     * Retrieve a CharsetDecoder from the Map using the given key.
     */
    CharsetDecoder getByteToCharConverter(String key) {
	return btcMapLocal.get().get( key ) ;
    }

    /**
     * Retrieve a CharsetEncoder from the Map using the given key.
     */
    CharsetEncoder getCharToByteConverter(String key) {
	return ctbMapLocal.get().get( key ) ;
    }

    /**
     * Stores the given CharsetDecoder in the thread local cache,
     * and returns the same converter.
     */
    CharsetDecoder setConverter(String key, CharsetDecoder converter) {
	btcMapLocal.get().put( key, converter ) ;
        return converter;
    }

    /**
     * Stores the given CharsetEncoder in the thread local cache,
     * and returns the same converter.
     */
    CharsetEncoder setConverter(String key, CharsetEncoder converter) {
	ctbMapLocal.get().put( key, converter ) ;
        return converter;
    }
}
