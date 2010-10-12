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
package com.sun.corba.se.spi.orb ;

import java.util.Map ;
import java.util.Set ;
import java.util.Iterator ;
import java.util.Properties ;

import java.security.PrivilegedExceptionAction ;
import java.security.PrivilegedActionException ;
import java.security.AccessController ;

import java.lang.reflect.Field ;

import com.sun.corba.se.spi.logging.ORBUtilSystemException ;

public abstract class ParserImplBase {
    private static final ORBUtilSystemException wrapper =
        ORBUtilSystemException.self ;

    protected abstract PropertyParser makeParser() ;

    /** Override this method if there is some needed initialization
    * that takes place after argument parsing.
    */
    protected void complete() 
    {
    }

    public ParserImplBase()
    {
    }

    public void init( DataCollector coll )
    {
	PropertyParser parser = makeParser() ;
	coll.setParser( parser ) ;
	Properties props = coll.getProperties() ;
	Map map = parser.parse( props ) ;
	setFields( map ) ;

	// Make sure that any extra initialization takes place after all the
	// fields are set from the map.
	complete() ;
    }

    private Field getAnyField( String name )
    {
	Field result = null ;

	try {
	    Class cls = this.getClass() ;
	    result = cls.getDeclaredField( name ) ;
	    while (result == null) {
		cls = cls.getSuperclass() ;
		if (cls == null) {
                    break;
                }

		result = cls.getDeclaredField( name ) ;
	    }
	} catch (Exception exc) {
	    throw wrapper.fieldNotFound( exc, name ) ;
	}

	if (result == null) {
            throw wrapper.fieldNotFound(name);
        }

	return result ;
    }

    protected void setFields( Map map )
    {
	Set entries = map.entrySet() ;
	Iterator iter = entries.iterator() ;
	while (iter.hasNext()) {
	    java.util.Map.Entry entry = (java.util.Map.Entry)(iter.next()) ;
	    final String name = (String)(entry.getKey()) ;
	    final Object value = entry.getValue() ;

	    try {
		AccessController.doPrivileged( 
		    new PrivilegedExceptionAction() {
			public Object run() throws IllegalAccessException, 
			    IllegalArgumentException
			{
			    Field field = getAnyField( name ) ;
			    field.setAccessible( true ) ;
			    field.set( ParserImplBase.this, value ) ;
			    return null ;
			}
		    } 
		) ;
	    } catch (PrivilegedActionException exc) {
		// Since exc wraps the actual exception, use exc.getCause()
		// instead of exc.
		throw wrapper.errorSettingField( exc.getCause(), name, value ) ;
	    }
	}
    }
}
