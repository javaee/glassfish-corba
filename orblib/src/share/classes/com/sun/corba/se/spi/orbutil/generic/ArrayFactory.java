/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2005-2007 Sun Microsystems, Inc. All rights reserved.
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

package com.sun.corba.se.spi.orbutil.generic;

import java.lang.reflect.Array ;

import java.math.BigInteger ;

import java.util.Collection ;
import java.util.List ;
import java.util.LinkedList ;
import java.util.ArrayList ;

import com.sun.corba.se.spi.orbutil.misc.ObjectUtility ;

import static com.sun.corba.se.spi.orbutil.misc.ObjectUtility.compactObjectToString ;

/** Take advantage of varargs support to more conveniently 
 * construct an array.
 *
 * @author ken
 */
public abstract class ArrayFactory{
    
    private ArrayFactory() {
    }
    
    public static <T> T[] make (T... args) {
        return args ;
    }

    public static <T,S extends T> T[] make( Collection<S> collection, 
	Class<T> cls ) 
    {
	if (collection == null) {
            return null;
        }

	T[] result = (T[])Array.newInstance( cls, collection.size() ) ;
	int index = 0 ;
	for (S elem : collection) {
	    result[index] = elem ;
            index++ ;
	}
	return result ;
    }

    private static boolean error = false ;

    private static void check( Object obj1, Object obj2, String emsg ) {
	if (!ObjectUtility.equals( obj1, obj2 )) {
	    System.out.println( "ArrayFactory: Objects not equal for " + emsg ) ;
	    System.out.println( "ArrayFactory: " + compactObjectToString( obj1 )) ;
	    System.out.println( "ArrayFactory: " + compactObjectToString( obj2 )) ;
	    error = true ;
	}
    }

    public static void main( String[] args ) {
	// Some simple tests for make
	String[] empty = new String[0] ;
	String[] colors = { "red", "blue", "green" } ;
	int[] numbers = { 1, 2, 3, 4 } ;
	BigInteger[] bigNumbers = { new BigInteger( "23857389573" ), 
	    new BigInteger( "93949384939" ) } ;

	System.out.println( "ArrayFactory: Testing ArrayFactory" ) ;

	// Test empty String[]
	List<String> arg1 = new ArrayList<String>() ;
	for (String s : empty) {
            arg1.add(s);
        }

	String[] res1 = make( arg1, String.class ) ;
	check( res1, empty, "empty" ) ;

	// Test non-empty String[]
	List<String> arg2 = new ArrayList<String>() ;
	for (String s : colors) {
            arg2.add(s);
        }

	String[] res2 = make( arg2, String.class ) ;
	check( res2, colors, "colors" ) ;
    
	// Test mixed
	List<Number> arg4 = new LinkedList<Number>() ;
	Number[] nres = new Number[ numbers.length + bigNumbers.length ] ;

	int index = 0 ;
	for (int x : numbers) {
	    arg4.add( x ) ;
	    nres[index] = x ;
            index++ ;
	}

	for (BigInteger bi : bigNumbers) {
	    arg4.add( bi ) ;
	    nres[index] = bi ;
            index++ ;
	}

	Number[] res4 = make( arg4, Number.class ) ;
	check( res4, nres, "int+numbers" ) ;

	if (!error) {
            System.out.println("ArrayFactory: test passed");
        }

	System.exit( error ? 1 : 0 ) ;
    }
}
