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

package com.sun.corba.ee.impl.orb ;

import java.util.Properties ;
import java.util.List ;
import java.util.LinkedList ;
import java.util.Iterator ;

import java.lang.reflect.Array ;

import com.sun.corba.ee.spi.orb.ORB ;

import com.sun.corba.ee.spi.orb.Operation ;

import com.sun.corba.ee.spi.logging.ORBUtilSystemException ;
import org.glassfish.pfl.basic.contain.Pair;

public class PrefixParserAction extends ParserActionBase {
    private static final ORBUtilSystemException wrapper =
        ORBUtilSystemException.self ;

    private Class componentType ;

    public PrefixParserAction( String propertyName, 
        Operation operation, String fieldName, Class componentType )
    {
        super( propertyName, true, operation, fieldName ) ;
        this.componentType = componentType ;
    }

    /** For each String s that matches the prefix given by getPropertyName(),
     * apply getOperation() to { suffix( s ), value }
     * and add the result to an Object[]
     * which forms the result of apply.  Returns null if there are no
     * matches.
     */
    public Object apply( Properties props ) 
    {
        String prefix = getPropertyName() ;
        int prefixLength = prefix.length() ;
        if (prefix.charAt( prefixLength - 1 ) != '.') {
            prefix += '.' ;
            prefixLength++ ;
        }
            
        List matches = new LinkedList() ;

        // Find all keys in props that start with propertyName
        Iterator iter = props.keySet().iterator() ;
        while (iter.hasNext()) {
            String key = (String)(iter.next()) ;
            if (key.startsWith( prefix )) {
                String suffix = key.substring( prefixLength ) ;
                String value = props.getProperty( key ) ;
                Pair<String,String> data = new Pair<String,String>( suffix, value ) ;
                Object result = getOperation().operate( data ) ;
                matches.add( result ) ;
            }
        }

        int size = matches.size() ;
        if (size > 0) {
            // Convert the list into an array of the proper type.
            // An Object[] as a result does NOT work.  Also report
            // any errors carefully, as errors here or in parsers that
            // use this Operation often show up at ORB.init().
            Object result = null ;
            try {
                result = Array.newInstance( componentType, size ) ;
            } catch (Throwable thr) {
                throw wrapper.couldNotCreateArray( thr,
                    getPropertyName(), componentType, size ) ;
            }

            Iterator iter2 = matches.iterator() ;
            int ctr = 0 ;
            while (iter2.hasNext()) {
                Object obj = iter2.next() ;

                try {
                    Array.set( result, ctr, obj ) ;
                } catch (Throwable thr) {
                    throw wrapper.couldNotSetArray( thr,
                        getPropertyName(), ctr, componentType, size,
                        obj ) ;
                }
                ctr++ ;
            }

            return result ;
        } else {
            return null;
        }
    }
}
