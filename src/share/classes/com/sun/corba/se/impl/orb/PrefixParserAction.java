/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2006-2007 Sun Microsystems, Inc. All rights reserved.
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

package com.sun.corba.se.impl.orb ;

import org.omg.CORBA.INITIALIZE ;

import java.util.Properties ;
import java.util.List ;
import java.util.LinkedList ;
import java.util.Iterator ;

import java.lang.reflect.Array ;

import com.sun.corba.se.spi.orb.ORB ;

import com.sun.corba.se.spi.orb.Operation ;
import com.sun.corba.se.spi.orbutil.generic.Pair ;
import com.sun.corba.se.spi.orbutil.misc.ObjectUtility ;

import com.sun.corba.se.impl.logging.ORBUtilSystemException ;

public class PrefixParserAction extends ParserActionBase {
    private Class componentType ;
    private ORBUtilSystemException wrapper ;

    public PrefixParserAction( String propertyName, 
	Operation operation, String fieldName, Class componentType )
    {
	super( propertyName, true, operation, fieldName ) ;
	this.componentType = componentType ;
	this.wrapper = ORB.getStaticLogWrapperTable().get_ORB_LIFECYCLE_ORBUtil() ;
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
		    getPropertyName(), componentType,
		    Integer.valueOf( size ) ) ;
	    }

	    Iterator iter2 = matches.iterator() ;
	    int ctr = 0 ;
	    while (iter2.hasNext()) {
		Object obj = iter2.next() ;

		try {
		    Array.set( result, ctr, obj ) ;
		} catch (Throwable thr) {
		    throw wrapper.couldNotSetArray( thr,
			getPropertyName(), Integer.valueOf(ctr), 
			componentType, Integer.valueOf(size),
			ObjectUtility.compactObjectToString( obj )) ;
		}
		ctr++ ;
	    }

	    return result ;
	} else 
	    return null ;
    }
}
