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

package argparser ;

import java.util.List ;
import java.util.ArrayList ;

import java.lang.reflect.Array ;
import java.lang.reflect.Method ;
import java.lang.reflect.Constructor ;
import java.lang.reflect.Type ;
import java.lang.reflect.ParameterizedType ;
import java.lang.reflect.InvocationTargetException ;

public class ElementParserImpl implements ElementParser {
    private UnaryFunction<String,Object> func ;
    private String[] description ;

    @Override
    public Object evaluate( String str ) {
	return func.evaluate( str ) ;
    }

    @Override
    public String[] describe() {
	return description ;
    }

    private static class ResultData extends Pair<UnaryFunction<String,Object>,String[]> {
	public ResultData( UnaryFunction<String,Object> func, String[] desc ) {
	    super( func, desc ) ;
	}
    }

    public ElementParserImpl( Method m ) {
	ResultData result = getData( m ) ;

	func = result.first() ;
	description = result.second() ;
    }

    String[] append( String str, String[] strs ) {
	String[] result = new String[ strs.length + 1 ] ;
	int rctr = 0 ;
	result[rctr++] = str ;
	for (String s : strs) {
	    result[rctr++] = s ;
	}
	return result ;
    }

    // Used for complex data types like List and arrays.
    private ResultData getData( Method meth ) {
	UnaryFunction<String,Object> myFunc = null ;
	String[] myDescription = null ;
	Class<?> type = meth.getReturnType() ;

	if (type.isArray()) {
	    final String sep = getSeparator( meth ) ;
	    final Class<?> elementClass = type.getComponentType() ;
	    final ResultData elementResultData = getSimpleData( elementClass ) ;
	    myDescription = append( "A " + sep + "-separated list of ",
		elementResultData.second() ) ;
	    
	    myFunc = new UnaryFunction<String,Object>() {
                @Override
		public Object evaluate( String value ) {
		    String[] elements = value.isEmpty() ?
                        new String[0] : value.split( sep ) ;
		    Object result = Array.newInstance( elementClass, 
			elements.length ) ;
		    int ctr = 0 ;	
		    for (String str : elements) {
			Object val = elementResultData.first().evaluate( str ) ;
			Array.set( result, ctr++, val ) ;
		    }

		    return result ;
		} 
	    } ;
	} else if (type.equals( List.class )) {
	    final String sep = getSeparator( meth ) ;
	    Class<?> elementClass = getListElementClass( meth ) ;
	    final ResultData elementResultData = getSimpleData( elementClass ) ;
	    myDescription = append( "A " + sep + "-separated list of ",
		elementResultData.second() ) ;

	    myFunc = new UnaryFunction<String,Object>() {
                @Override
                @SuppressWarnings("unchecked")
		public Object evaluate( String value ) {
		    String[] elements = value.isEmpty() ?
                        new String[0] : value.split( sep ) ;
		    List result = new ArrayList( elements.length ) ;
		    for (String str : elements) {
			Object val = elementResultData.first().evaluate( str ) ;
			result.add( val ) ;
		    }

		    return result ;
		} 
	    } ;
	} else {
	    return getSimpleData( type ) ;    
	}

	return new ResultData( myFunc,
	    myDescription ) ;
    }

    // Used for all types that take a single element.  This does 
    // not include List<Type> or Type[].  
    private ResultData getSimpleData( final Class type ) {
	UnaryFunction<String,Object> myFunc = null ;
	String[] myDescription = null ;

	if (type.isPrimitive()) {
	    myDescription = new String[] { "A valid " + type.getName() } ;

	    myFunc = getPrimitiveParser( type ) ;
	} else if (type == String.class){
	    myDescription = new String[] { "A String" } ;

	    myFunc = new UnaryFunction<String,Object>() {
                @Override
		public Object evaluate( String str ) {
		    return str ;
		} 
	    } ;
	} else if (type.isEnum()) {
	    myDescription = new String[] {
		"One of: " + getEnumElements( type ) } ;

	    myFunc = new UnaryFunction<String,Object>() {
                @Override
                @SuppressWarnings("unchecked")
		public Object evaluate( String str ) {
		    try {
			return Enum.valueOf( type, str ) ;
		    } catch (IllegalArgumentException exc) {
			throw new RuntimeException( str 
			    + " is not in enum " + type.getName() ) ;
		    }
		} 
	    };
	} else { // Anything else: must be a class that supports <init>(String)
	    myDescription = new String[] {
		"A string that can create a " + type.getName() } ;

	    myFunc = makeClassConverter( type ) ;
	} 
	
	return new ResultData( myFunc,
	    myDescription ) ;
    }

    private String getEnumElements( Class<?> cls ) {
	boolean isFirst = true ;
	StringBuilder sb = new StringBuilder() ;
	for (Object obj : cls.getEnumConstants()) {
	    if (isFirst) {
		isFirst = false ;
	    } else {
		sb.append( ' ' ) ;
	    }
	    sb.append( obj.toString() ) ;
	}
	return sb.toString() ;
    }

    private UnaryFunction<String,Object> getPrimitiveParser( Class type ) {
	UnaryFunction<String,Object> myFunc = null ;

	if (type == boolean.class) {
	    myFunc = new UnaryFunction<String,Object>() {
                @Override
		public Object evaluate( String str ) {
		    return Boolean.valueOf( str ) ;
		}
	    } ;
	} else if (type == byte.class) {
	    myFunc = new UnaryFunction<String,Object>() {
                @Override
		public Object evaluate( String str ) {
		    return Byte.valueOf( str ) ;
		}
	    } ;
	} else if (type == char.class) {
	    myFunc = new UnaryFunction<String,Object>() {
                @Override
		public Object evaluate( String str ) {
		    if (str.length() != 1) {
                        throw new RuntimeException("String \"" + str +
                            "\" cannot be converted to a Character");
                    }
		    return Character.valueOf( str.charAt(0) ) ;
		}
	    } ;
	} else if (type == short.class) {
	    myFunc = new UnaryFunction<String,Object>() {
                @Override
		public Object evaluate( String str ) {
		    return Short.valueOf( str ) ;
		}
	    } ;
	} else if (type == int.class) {
	    myFunc = new UnaryFunction<String,Object>() {
                @Override
		public Object evaluate( String str ) {
		    return Integer.valueOf( str ) ;
		}
	    } ;
	} else if (type == long.class) {
	    myFunc = new UnaryFunction<String,Object>() {
                @Override
		public Object evaluate( String str ) {
		    return Long.valueOf( str ) ;
		}
	    } ;
	} else if (type == float.class) {
	    myFunc = new UnaryFunction<String,Object>() {
                @Override
		public Object evaluate( String str ) {
		    return Float.valueOf( str ) ;
		}
	    } ;
	} else if (type == double.class) {
	    myFunc = new UnaryFunction<String,Object>() {
                @Override
		public Object evaluate( String str ) {
		    return Double.valueOf( str ) ;
		}
	    } ;
	} 

	return myFunc ;
    }

    private UnaryFunction<String,Object> makeClassConverter( 
	final Class<?> type ) {

	Constructor<?> cons = null ;

	try {
	    cons = type.getConstructor( String.class ) ;
	} catch (NoSuchMethodException e1) {
	    throw new RuntimeException( type.getName() 
		+ " does not have a constructor (String)" ) ;
	} catch (SecurityException e2) {
	    throw new RuntimeException( type.getName() 
		+ " constructor (String) is not accessible" ) ;
	}
    
	final Constructor<?> fcons = cons ;

	return new UnaryFunction<String,Object>() {
            @Override
	    public Object evaluate( String str ) {
		try {
		    return fcons.newInstance( str ) ;
		} catch (InvocationTargetException e1) {
		    throw new RuntimeException( type.getName() 
			+ "(String) constructor threw exception: " 
			+ e1.getTargetException() ) ;
		} catch (Exception e2) {
		    throw new RuntimeException( "Exception " + e2 
			+ " occured in calling constructor " 
			+ type.getName() + "(String)" ) ;
		}
	    }
	} ;
    }

    private Class getListElementClass( Method meth ) { 
	Type rtype = meth.getGenericReturnType() ;
	if (rtype instanceof ParameterizedType) {
	    ParameterizedType ptype = (ParameterizedType)rtype ;
	    Type[] typeArgs = ptype.getActualTypeArguments() ;
	    assert typeArgs.length == 1 ;
	    Type etype = typeArgs[0] ;
	    if (etype instanceof Class) {
		return (Class)etype ;
	    } else {
		throw new RuntimeException( "Method " + meth 
		    + " has a List<> return type " 
		    + " that is not parameterized by a class" ) ;
	    }
	} else {
	    throw new RuntimeException( "Method " + meth 
		+ " does not have a parameterized List return type" ) ;
	}
    }

    private String getSeparator( Method meth ) {
	Separator sep = meth.getAnnotation( Separator.class ) ;
	String result = "," ;
	if (sep != null) {
            result = sep.value();
        }

	return result ;
    }
}
