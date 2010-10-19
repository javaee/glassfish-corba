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
package com.sun.corba.se.spi.orbutil.generic ;

import java.security.AccessController;
import java.security.PrivilegedAction;
import java.util.List ;
import java.util.Map ;
import java.util.ArrayList ;
import java.util.Arrays;

public abstract class Algorithms {
    /** Convert argument to String, either by toString, ot Arrays.toString.
     *
     * @param arg Object to convert.
     * @return arg converted to string.
     */
    public static String convertToString( Object arg ) {
        if (arg == null) {
            return "<NULL>";
        }

        Class<?> cls = arg.getClass() ;
        if (cls.isArray()) {
            Class<?> cclass = cls.getComponentType() ;
            if (cclass.equals( int.class )) {
                return Arrays.toString((int[]) arg);
            }
            if (cclass.equals( byte.class )) {
                return Arrays.toString((byte[]) arg);
            }
            if (cclass.equals( boolean.class )) {
                return Arrays.toString((boolean[]) arg);
            }
            if (cclass.equals( char.class )) {
                return Arrays.toString((char[]) arg);
            }
            if (cclass.equals( short.class )) {
                return Arrays.toString((short[]) arg);
            }
            if (cclass.equals( long.class )) {
                return Arrays.toString((long[]) arg);
            }
            if (cclass.equals( float.class )) {
                return Arrays.toString((float[]) arg);
            }
            if (cclass.equals( double.class )) {
                return Arrays.toString((double[]) arg);
            }
            return Arrays.toString( (Object[])arg ) ;
        } else {
            return arg.toString() ;
        }
    }

    public static <A,R> UnaryFunction<A,R> mapToFunction( final Map<A,R> map ) {
	return new UnaryFunction<A,R>() {
	    public R evaluate( A arg ) {
		return map.get( arg ) ;
	    }
	} ;
    }

    public static <A,R> void map( final List<A> arg, final List<R> result,
	final UnaryFunction<A,R> func ) {

	for (A a : arg) {
	    R newArg = func.evaluate( a ) ;
	    if (newArg != null) {
                result.add(newArg);
            }
	}
    }

    public static <A,R> List<R> map( final List<A> arg, final UnaryFunction<A,R> func ) {

	final List<R> result = new ArrayList<R>() ;
	map( arg, result, func ) ;
	return result ;
    }

    public static <A> void filter( final List<A> arg, final List<A> result,
	final UnaryBooleanFunction<A> predicate ) {

	final UnaryFunction<A,A> filter = new UnaryFunction<A,A>() {
	    public A evaluate( A arg ) { 
		return predicate.evaluate( arg ) ? arg : null ; } } ;

	map( arg, result, filter ) ;
    }

    public static <A> List<A> filter( List<A> arg, UnaryBooleanFunction<A> predicate ) {
	List<A> result = new ArrayList<A>() ;
	filter( arg, result, predicate ) ;
	return result ;
    }

    public static <A> A find( List<A> arg, UnaryBooleanFunction<A> predicate ) {
	for (A a : arg) {
	    if (predicate.evaluate( a )) {
		return a ;
	    }
	}

	return null ;
    }

    public static <A,R> R fold( List<A> list, R initial, BinaryFunction<R,A,R> func ) {
        R result = initial ;
        for (A elem : list) {
            result = func.evaluate( result, elem ) ;
        }
        return result ;
    }

    public static <T> T getOne( List<T> list, String zeroMsg, String manyMsg ) {
        if (list.isEmpty()) {
            throw new IllegalArgumentException(zeroMsg);
        }
        if (list.size() > 1) {
            throw new IllegalArgumentException(manyMsg);
        }
        return list.get(0) ;
    }

    public static <T> T getFirst( List<T> list, String zeroMsg ) {
        if (list.isEmpty()) {
            throw new IllegalArgumentException(zeroMsg);
        }
        return list.get(0) ;
    }

    public static interface Action<T> {
        T run() throws Exception ;
    }

    private static <T> PrivilegedAction<T> makePrivilegedAction(
        final Action<T> act ) {

        return new PrivilegedAction<T>() {
            public T run() {
                try {
                    return act.run() ;
                } catch (RuntimeException exc) {
                    throw exc ;
                } catch (Exception ex) {
                    throw new RuntimeException( ex ) ;
                }
            }
        } ;
    }

    public static <T> T doPrivileged( Action<T> func ) {
        SecurityManager sman = System.getSecurityManager() ;
        try {
            if (sman == null) {
                return func.run() ;
            } else {
                return AccessController.doPrivileged( makePrivilegedAction(
                    func ) ) ;
            }
        } catch (RuntimeException rex) {
            throw rex ;
        } catch (Exception ex) {
            throw new RuntimeException( ex ) ;
        }
    }
}
