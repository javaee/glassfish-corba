/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2007-2007 Sun Microsystems, Inc. All rights reserved.
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

package com.sun.corba.se.impl.orbutil.jmx ;

import java.util.Collections ;
import java.util.List ;
import java.util.Set ;
import java.util.HashSet ;
import java.util.Queue ;
import java.util.LinkedList ;
import java.util.ArrayList ;
import java.util.Map ;
import java.util.WeakHashMap ;

import java.lang.reflect.AnnotatedElement ;
import java.lang.reflect.Method ;
import java.lang.reflect.Type ;

import java.lang.annotation.Annotation ;

import com.sun.corba.se.spi.orbutil.generic.UnaryBooleanFunction ;
import com.sun.corba.se.spi.orbutil.generic.UnaryFunction ;
import com.sun.corba.se.spi.orbutil.generic.BinaryVoidFunction ;
import com.sun.corba.se.spi.orbutil.generic.BinaryFunction ;
import com.sun.corba.se.spi.orbutil.generic.Algorithms ;
import com.sun.corba.se.spi.orbutil.generic.Pair ;
import com.sun.corba.se.spi.orbutil.generic.Graph ;

import com.sun.corba.se.spi.orbutil.jmx.ManagedObjectManager ;
import com.sun.corba.se.spi.orbutil.jmx.ManagedObject ;
import com.sun.corba.se.spi.orbutil.jmx.ManagedData ;
import com.sun.corba.se.spi.orbutil.jmx.ManagedAttribute ;
import com.sun.corba.se.spi.orbutil.jmx.ManagedOperation ;
import com.sun.corba.se.spi.orbutil.jmx.InheritedAttribute ;
import com.sun.corba.se.spi.orbutil.jmx.InheritedAttributes ;
import com.sun.corba.se.spi.orbutil.jmx.IncludeSubclass ;
import com.sun.corba.se.spi.orbutil.jmx.TypeConverter ;
    
/** Analyzes class inheritance hiearchy and provides methods for searching for
 * classes and methods.
 */
public class ClassAnalyzer {
    // General purpose class analyzer
    //
    // The basic problem is to determine for any class its linearized inheritance
    // sequence.  This is an old problem in OOP.  For my purpose, I want the following
    // to be true:
    //
    // Let C be a class, let C.super be C's superclass, and let C.inter be the list of
    // C's implemented interfaces (C may be an interface, abstract, or concrete class).
    // Define ILIST(C) to be a sequence that satisfies the following properties:
    //
    // 1. ILIST(C) starts with C.
    // 2. If X is in ILIST(C), then so is X.super and each element of X.inter.
    // 3. For any class X in ILIST(C):
    //    2a. X appears before X.super in ILIST(C)
    //    2b. X appears before any X.inter in ILIST(C)
    // 4. No class appears more than once in ILIST(C)
    //
    // Note that the order can change when new classes are analyzed, so each class must be 
    // analyzed independently
    //
    // We need to elaborate on this idea to handle several issues:
    //
    // 1. We start with needing to determine whether a particular class C is ManagedData (mapped
    //    to composite data, and used for attribute and operation values in an Open MBean) or
    //    ManagedObject (mapped to an MBean with an ObjectName).  We will require that the super
    //    class graph of any object contain at most one class annotated with @ManagedObject or
    //    @ManagedData (and not both).  This means that for any class C, there is a class MC
    //    (which may be C) which is the unique class that is a superclass of C and is annotated
    //    with either @ManagedData or @ManagedObject.
    // 2. The MC class may also contain InheritedAttribute and IncludeSubclass annotations.
    //    InheritedAttribute is handled by searching in the superclasses for getter and setters
    //    conforming to the InheritedAttribute id.  IncludeSubclass extends the set of classes
    //    to scan for @ManagedAttribute and @ManagedOperation by the union of MC's superclasses,
    //    and the superclasses of all classes specified by IncludeSubclass.
    // 3. What we require here is that ALL classes that share the same MC class translate to the
    //    SAME kind of MBean or CompositeData.
    private static final Graph.Finder<Class<?>> finder = new Graph.Finder<Class<?>>() {
	public List<Class<?>> evaluate( Class<?> arg ) {
	    List<Class<?>> result = new ArrayList<Class<?>>() ;
	    Class<?> sclass = arg.getSuperclass() ;
	    if (sclass != null) {
		result.add( sclass ) ;
	    }
	    for (Class<?> cls : arg.getInterfaces() ) {
		result.add( cls ) ;
	    }
	    return result ;
	}
    } ;

    private List<Class<?>> classInheritance ;

    private ClassAnalyzer( Graph<Class<?>> gr ) {
	List<Class<?>> result = new ArrayList<Class<?>>( gr.getPostorderList() ) ;
	Collections.reverse( result ) ;
        classInheritance = result ;
    }

    public ClassAnalyzer( Class<?> cls ) {
	this( new Graph( cls, finder ) ) ;
    }

    public ClassAnalyzer( List<Class<?>> classes ) {
	this( new Graph( classes, finder ) ) ;
    }

    public interface Predicate extends UnaryBooleanFunction<Object> {} ;

    public Predicate forAnnotation( final Class<? extends Annotation> annotation ) {
        return new Predicate() {
            public boolean evaluate( Object elem ) {
                return ((AnnotatedElement)elem).getAnnotation( annotation ) != null ;
            }
        } ;
    }

    public List<Class<?>> findClasses( Predicate pred ) {
	final List<Class<?>> result = new ArrayList<Class<?>>() ;
	for (Class<?> c : classInheritance) {
            if (pred.evaluate( c )) {
                result.add( c ) ;
            }
        }

        return result ;
    }

    // Tested by testGetAnnotatedMethods XXX rename the test
    public List<Method> findMethods( Predicate pred ) {
	final List<Method> result = new ArrayList<Method>() ;
	for (Class<?> c : classInheritance) {
	    for (Method m : c.getDeclaredMethods()) {
                if (pred.evaluate( m )) {
                    result.add( m ) ;
                }
	    }
	}

	return result ;
    }

    // Tested by testFindMethod
    // XXX Do we really need this, or just use findMethods, and look at 
    // length of result?
    public Method findMethod( Predicate pred ) {
	for (Class<?> c : classInheritance) {
	    for (Method m : c.getDeclaredMethods()) {
                if (pred.evaluate( m )) {
                    return m ;
                }
	    }
	}

	return null ;
    }
}
