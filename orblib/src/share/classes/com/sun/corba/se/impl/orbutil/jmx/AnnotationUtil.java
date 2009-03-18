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
    
public abstract class AnnotationUtil {
    private AnnotationUtil() {}

    /** Find the superclass or superinterface of cls (which may be cls itself) that has the
     * given annotationClass as an annotation.  If the annotated Class has an IncludeSubclass
     * annotation, add those classes into the ClassAnalyzer for the annotated class.
     */
    public static Pair<Class<?>,ClassAnalyzer> getClassAnalyzer( Class<?> cls, 
        Class<? extends Annotation> annotationClass ) {

        ClassAnalyzer ca = new ClassAnalyzer( cls ) ;
        /* This is the versions that expects EXACTLY ONE annotation
        Class<?> annotatedClass = Algorithms.getOne( 
            ca.findClasses( ca.forAnnotation( annotationClass ) ),
            "No " + annotationClass.getName() + " annotation found",
            "More than one " + annotationClass.getName() + " annotation found" ) ;
        */
        
        Class<?> annotatedClass = Algorithms.getFirst( 
            ca.findClasses( ca.forAnnotation( annotationClass ) ),
            "No " + annotationClass.getName() + " annotation found" ) ;
        
        List<Class<?>> classes = new ArrayList<Class<?>>() ;
        classes.add( annotatedClass ) ;
	final IncludeSubclass is = annotatedClass.getAnnotation( IncludeSubclass.class ) ;
	if (is != null) {
            for (Class<?> klass : is.cls()) {
                classes.add( klass ) ;
            }
	}

        if (classes.size() > 1) 
            ca = new ClassAnalyzer( classes ) ;

        return new Pair( annotatedClass, ca ) ;
    }

    public static InheritedAttribute[] getInheritedAttributes( Class<?> cls ) {
	// Check for @InheritedAttribute(s) annotation.  
	// Find methods for these attributes in superclasses. 
	final InheritedAttribute ia = cls.getAnnotation( InheritedAttribute.class ) ;
	final InheritedAttributes ias = cls.getAnnotation( InheritedAttributes.class ) ;
	if ((ia != null) && (ias != null)) 
	    throw new IllegalArgumentException( 
		"Only one of the annotations InheritedAttribute or "
		+ "InheritedAttributes may appear on a class" ) ;

	InheritedAttribute[] iaa = null ;
	if (ia != null)	
	    iaa = new InheritedAttribute[] { ia } ;
	else if (ias != null) 
	    iaa = ias.attributes() ;

	return iaa ;
    }
}
