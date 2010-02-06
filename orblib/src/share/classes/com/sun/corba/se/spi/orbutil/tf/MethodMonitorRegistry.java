/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2002-2010 Sun Microsystems, Inc. All rights reserved.
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


package com.sun.corba.se.spi.orbutil.tf;

import com.sun.corba.se.spi.orbutil.generic.SynchronizedHolder;
import com.sun.corba.se.spi.orbutil.tf.annotation.MethodMonitorGroup;
import java.lang.annotation.Annotation;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

/** Main class for registering MethodMonitorFactories against particular
 * annotation classes that represent method monitor groups.  This
 *
 * @author ken
 */
public class MethodMonitorRegistry {
    // Maps traceable classes to the list of method names (which is in the order
    // used in the generated code, so the index of a method name is the number
    // used in the generated code).
    private static final Map<Class<?>,List<String>> classToMNames =
        new HashMap<Class<?>,List<String>>() ;
    
    // Maps traceable classes to a Map from Annotation class to the 
    // MethodMonitor Holder, which allows easy and safe updates to the MethodMonitor.
    private static final Map<Class<?>,Map<Class<? extends Annotation>,SynchronizedHolder<MethodMonitor>>> classToAnnoMM =
        new HashMap<Class<?>,Map<Class<? extends Annotation>,SynchronizedHolder<MethodMonitor>>>() ;

    // For each MM Annotation, lists all of the immediate subgroups.
    private static final Map<Class<? extends Annotation>,List<Class<? extends Annotation>>> subgroups =
        new HashMap<Class<? extends Annotation>,List<Class<? extends Annotation>>>() ;

    // For each MM Annotation, lists all MM annotations reachable via subgroups.
    // This is the reflexive, transitive closure of subgroups.
    private static final Map<Class<? extends Annotation>,List<Class<? extends Annotation>>> subgroupsTC =
        new HashMap<Class<? extends Annotation>,List<Class<? extends Annotation>>>() ;
    
    // For each MM Annotation, lists all traceable Classes that have that annotation.
    private static final Map<Class<? extends Annotation>,List<Class<?>>> annotationToClasses =
        new HashMap<Class<? extends Annotation>,List<Class<?>>>() ;

    // For each MM Annotation, give the registered MethodMonitorFactory (if any)
    private static final Map<Class<? extends Annotation>,MethodMonitorFactory> annotationToMMF =
        new HashMap<Class<? extends Annotation>,MethodMonitorFactory>() ; 

    // For each MM Annotation a, give the set of all MethodMonitorFactory instances
    // that are registered to any element of subgroupsTC(a).
    private static final Map<Class<? extends Annotation>,Set<MethodMonitorFactory>> annotationToMMFsets =
        new HashMap<Class<? extends Annotation>,Set<MethodMonitorFactory>>() ;

    // For each MM Annotation a, give the composition of annotationToMMFSets(a).
    private static final Map<Class<? extends Annotation>,MethodMonitorFactory> annotationToMMFComposition =
        new HashMap<Class<? extends Annotation>,MethodMonitorFactory>() ;

    /** Register a class with the tracing facility.  The class must be an 
     * instrumented class that is annotated with an annotation with a 
     * meta-annotation of @MethodMonitorGroup.  Note that this method should
     * only be called from the enhanced class, not directly by the user.
     * 
     * @param cls  Class to register, which must have 1 or more MM annotations.
     * @param methodNames The list of method names used in the enhanced code.
     * The index of the name is the value used in the method.
     * @param annoMM The MM holders for each MM annotation on the class.
     */
    public static void registerClass( Class<?> cls, List<String> methodNames,
        Map<Class<? extends Annotation>,SynchronizedHolder<MethodMonitor>> annoMM ) {
        // XXX define me
        classToMNames.put( cls, methodNames ) ;
        classToAnnoMM.put( cls, annoMM ) ;
        scanClassAnnotations( cls ) ;
    }

    private static void scanClassAnnotations( Class<?> cls ) {
        for (Annotation anno : cls.getAnnotations()) {
            scanAnnotation( anno ) ;
        }
    }

    // XXX Do we really needs subgroups AND subgroupsTC?  This should
    // probably just directly compute subgroupsTC.
    private static void scanAnnotation( Annotation anno ) {
        final Class<? extends Annotation> annoClass = anno.getClass() ;
        MethodMonitorGroup mmg =
            annoClass.getAnnotation( MethodMonitorGroup.class ) ;
        if (mmg != null) {
            if (!subgroups.containsKey( annoClass )) {
                List<Class<? extends Annotation>> acs = Arrays.asList(
                    mmg.value() ) ;
                subgroups.put( annoClass, acs ) ;
            }
        }
    }

    /** Provided so that implementation of the MethodMonitor interface can
     * obtain the method name for use in log reports or for other purposes.
     * 
     * @param cls The enhanced class
     * @param identifier An Integer representing the method name.
     * @return The name of the method corresponding to the identifier.
     */
    public static String getMethodName( Class<?> cls, Object identifier ) {
        // XXX define me
        if (!(identifier instanceof Integer)) {
            throw new RuntimeException( "identifier is not an Integer" ) ;
        }

        Integer idVal = (Integer)identifier ;

        List<String> names = classToMNames.get( cls ) ;

        if (names == null) {
            throw new RuntimeException( "Class " + cls + " not found in map" ) ;
        }

        if (idVal < 0 || idVal >= names.size()) {
            throw new RuntimeException( "identifier is out of range" ) ;
        }

        return names.get( idVal ) ;
    }

    /** Register a particular MethodMonitorFactory against an annotation.
     * Annot must be annotated with the MethodMonitorGroup meta-annotation.
     * Only a single mmf may be registered against an annotation.  
     * A subsequent register call overwrites the registered annotation.
     * Annot must not be null.
     * 
     * @param annot
     * @param mmf
     */
    public static void register( Class<? extends Annotation> annot,
        MethodMonitorFactory mmf ) {

        // XXX define me
    }

    /** Remove the MethodMonitorFactory (if any) that is associated with annot.
     * 
     * @param annot
     */
    public static void clear( Class<? extends Annotation> annot ) {
        // XXX define me
    }

    /** Return the MethodMonitorFactory registered against the annotation, or
     * null if nothing is registered.
     *
     * @param annot
     * @return
     */
    public static MethodMonitorFactory registeredFactory(
        Class<? extends Annotation> annot ) {

        // XXX define me
        return null ;
    }
}
