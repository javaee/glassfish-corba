/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2002-2007 Sun Microsystems, Inc. All rights reserved.
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

package com.sun.corba.se.spi.orbutil.generic ;

import java.util.Map ;
import java.util.HashMap ;

/** A simple abstraction of a MultiSet, that is, a "set" that can contain
 * more than one copy of the same element.  I am implementing only the
 * bare minimum that is required for now.
 */
public class MultiSet<E> {
     private Map<E,Integer> contents = new HashMap<E,Integer>() ;

     public void add( E element ) {
	 Integer value = contents.get( element ) ;
	 if (value == null) {
	    value = 0 ;
	 }

	 value += 1 ;
	 contents.put( element, value ) ;
     }

     public void remove( E element ) {
	 Integer value = contents.get( element ) ;
	 if (value == null) {
	     return ;
	 }

	 value -= 1 ;

	 if (value == 0) {
	     contents.remove( element ) ;
	 } else {
	     contents.put( element, value ) ;
	 }
     }

     public boolean contains( E element ) {
	 Integer value = contents.get( element ) ;
	 if (value == null) {
	    value = 0 ;
	 }

	return value > 0 ;
     }

     /** Return the number of unique elements in this MultiSet.
      */
     public int size() {
	 return contents.keySet().size() ;
     }
     
     private static void shouldBeTrue( boolean val, String msg ) {
	 if (!val) 
	     System.out.println( msg ) ;
     }

     private static void shouldBeFalse( boolean val, String msg ) {
	 if (val) 
	     System.out.println( msg ) ;
     }

     public static void main( String[] args ) {
	MultiSet<String> mset = new MultiSet<String>() ;
	String s1 = "first" ;
	String s2 = "second" ;
	
	mset.add( s1 ) ;
	shouldBeTrue( mset.contains( s1 ), "mset does not contain s1 (1)" ) ;

	mset.add( s2 ) ;
	mset.add( s1 ) ;
	mset.remove( s1 ) ;
	shouldBeTrue( mset.contains( s1 ), "mset does not contain s1 (2)" ) ;
	mset.remove( s1 ) ;
	shouldBeFalse( mset.contains( s1 ), "mset still contains s1 (3)" ) ;
	shouldBeTrue( mset.contains( s2 ), "mset does not contain s2 (4)" ) ;
     }
}
