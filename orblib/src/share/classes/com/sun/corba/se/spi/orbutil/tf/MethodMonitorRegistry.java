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
import java.lang.reflect.Field;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.ResourceBundle;
import java.util.Set;

/** Main class for registering MethodMonitorFactories against particular
 * annotation classes that represent method monitor groups.  This
 *
 * @author ken
 */
public class MethodMonitorRegistry {
    private static final Set<String> mmAnnotations =
	new HashSet<String>() ;

    public static Set<String> getMMAnnotations() {
	return new HashSet<String>( mmAnnotations ) ;
    }

    public static void registerAnnotationFile( final String fname ) {
	try {
	    // Read tracing annotation property file into mmAnnotations.
	    final ResourceBundle rb = ResourceBundle.getBundle( fname ) ;
	    String obj = rb.getString( "com.sun.corba.tf.annotations.size" ) ;
	    int size = 0 ;
	    if (obj != null) {
		size = Integer.valueOf( obj ) ;
	    }

	    for (int ctr=1; ctr<=size; ctr++) {
		obj = rb.getString( "com.sun.corba.tf.annotation." + ctr ) ;
		mmAnnotations.add( obj ) ;
	    }
	} catch (Exception exc) {
	    System.out.println( "Exception: " + exc ) ;
	}
    }

    // Maps traceable classes to the list of method names (which is in the order
    // used in the generated code, so the index of a method name is the number
    // used in the generated code).
    private static final Map<Class<?>,List<String>> classToMNames =
        new HashMap<Class<?>,List<String>>() ;
    
    // Maps traceable classes to a Map from Annotation class to the 
    // MethodMonitor Holder, which allows easy and safe updates to the
    // MethodMonitor.
    private static final Map<Class<?>,
        Map<Class<? extends Annotation>,
            SynchronizedHolder<MethodMonitor>>> classToAnnoMM =

            new HashMap<Class<?>,
                Map<Class<? extends Annotation>,
                    SynchronizedHolder<MethodMonitor>>>() ;

    // For each MM Annotation, lists all of the immediate subgroups.
    private static final Map<Class<? extends Annotation>,
        Set<Class<? extends Annotation>>> subgroups =
        new HashMap<Class<? extends Annotation>,
            Set<Class<? extends Annotation>>>() ;

    // For each MM Annotation, lists all MM annotations reachable via subgroups.
    // This is the reflexive, transitive closure of subgroups.
    private static final Map<Class<? extends Annotation>,
        Set<Class<? extends Annotation>>> subgroupsTC =
        new HashMap<Class<? extends Annotation>,
            Set<Class<? extends Annotation>>>() ;
    
    // For each MM Annotation, lists all traceable Classes that
    // have that annotation.
    private static final Map<Class<? extends Annotation>,
        Set<Class<?>>> annotationToClasses =
        new HashMap<Class<? extends Annotation>,Set<Class<?>>>() ;

    // For each MM Annotation, give the registered MethodMonitorFactory (if any)
    private static final Map<Class<? extends Annotation>,
        MethodMonitorFactory> annotationToMMF =
        new HashMap<Class<? extends Annotation>,MethodMonitorFactory>() ; 

    // For each MM Annotation a, give the set of all MethodMonitorFactory 
    // instances that are registered to any element of subgroupsTC(a).
    private static final Map<Class<? extends Annotation>,
        Set<MethodMonitorFactory>> annotationToMMFSets =
        new HashMap<Class<? extends Annotation>,Set<MethodMonitorFactory>>() ;

    // For each MM Annotation a, give the composition of annotationToMMFSets(a).
    private static final Map<Class<? extends Annotation>,
        MethodMonitorFactory> annotationToMMFComposition =
        new HashMap<Class<? extends Annotation>,MethodMonitorFactory>() ;

    private static void updateTracedClass( Class<?> cls ) {
        Map<Class<? extends Annotation>,SynchronizedHolder<MethodMonitor>> map =
            classToAnnoMM.get( cls ) ; 

        for (Map.Entry<Class<? extends Annotation>, 
            SynchronizedHolder<MethodMonitor>> entry : map.entrySet() ) {

            MethodMonitorFactory mmf =
                annotationToMMFComposition.get( entry.getKey() ) ;

            if (mmf == null) {
                entry.getValue().content( null ) ;
            } else {
                entry.getValue().content( mmf.create( cls )) ;
            }
        }

    }

    private static void updateAnnotation( Class<? extends Annotation> annot ) {
        // update annotationToMMFSets from annotationToMMF and subgroupsTC
        Set<MethodMonitorFactory> mmfs = new HashSet<MethodMonitorFactory>() ;
        annotationToMMFSets.put( annot, mmfs ) ;

        final Set<Class<? extends Annotation>> relatedAnnos =
            subgroupsTC.get( annot ) ;
        for (Class<? extends Annotation> key : relatedAnnos) {
            MethodMonitorFactory mmf = annotationToMMF.get( key ) ;
            if (mmf != null) {
                mmfs.add( mmf ) ;
            }
        }

        // update annotationsToMMFComposition from annotationToMMFSets
        annotationToMMFComposition.put( annot,
            MethodMonitorFactoryDefaults.compose( mmfs ) ) ;

        // update the classes that are annotated by this annotation.
        final Set<Class<?>> classes = annotationToClasses.get(annot) ;
        if (classes != null) {
            for (Class<?> cls : classes) {
                updateTracedClass( cls ) ;
            }
        }
    }

    // Called after the subgroups relation has changed.  This forces 
    // recomputation of annotationToMMFSets and annotationsToMMFComposition,
    // and also updates to all registered classes in the
    // annotationToClasses map.
    private static void doFullUpdate() {
        for (Class<? extends Annotation> annot : annotationToMMF.keySet() ) {
            updateAnnotation( annot ) ;
        }

        for (Class<?> key : classToAnnoMM.keySet()) {
            updateTracedClass( key ) ;
        }
    }

    private static boolean scanClassAnnotations( final Class<?> cls ) {
        boolean updated = false ;
        boolean hasMMAnnotation = false ;
        for (Annotation anno : cls.getAnnotations()) { 
            final Class<? extends Annotation> annoClass =
                anno.annotationType() ;
            final MethodMonitorGroup mmg =
                annoClass.getAnnotation( MethodMonitorGroup.class ) ;

            if (mmg != null) {
                hasMMAnnotation = true ;
                Set<Class<?>> target = annotationToClasses.get( annoClass ) ;
                if (target == null) {
                    target = new HashSet<Class<?>>() ;
                    annotationToClasses.put( annoClass, target ) ;
                }
                target.add( cls ) ;

                if (scanAnnotation( annoClass, mmg ) ) {
                    updated = true ;
                }
            }
        }

        if (!hasMMAnnotation) {
            throw new RuntimeException( "Class " + cls + " is not traceable" ) ;
        }

        return updated ;
    }

    private static boolean scanAnnotation(
        final Class<? extends Annotation> annoClass,
        final MethodMonitorGroup mmg ) {

        boolean updated = false ;

        if (!subgroups.containsKey( annoClass )) {
            updated = true ;
            Set<Class<? extends Annotation>> acs =
                new HashSet<Class<? extends Annotation>>( Arrays.asList(
                mmg.value() ) ) ;
            subgroups.put( annoClass, acs ) ;

            computeTransitiveClosure() ;
        }

        return updated ;
    }

    private static void computeTransitiveClosure() {
        subgroupsTC.clear() ;
        for (Class<? extends Annotation> anno : subgroups.keySet()) {
            Set<Class<? extends Annotation>> memset =
                new HashSet<Class<? extends Annotation>>() ;
            subgroupsTC.put( anno, memset ) ;
        }

        for (Class<? extends Annotation> anno : subgroupsTC.keySet()) {
            dfs( anno, anno ) ;
        }
    }

    private static void dfs( Class<? extends Annotation> src,
        Class<? extends Annotation> dest ) {

        Set<Class<? extends Annotation>> images = subgroupsTC.get( src ) ;
        images.add( dest ) ;

        Set<Class<? extends Annotation>> temp = subgroups.get(dest) ;
        if (temp != null) {
            for (Class<? extends Annotation> anno : temp) {
                if (!images.contains( anno )) {
                    dfs( src, anno ) ;
                }
            }
        }
    }

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
    public static void registerClass( final Class<?> cls,
        final List<String> methodNames,
        final Map<Class<? extends Annotation>,
            SynchronizedHolder<MethodMonitor>> annoMM ) {

        final boolean fullUpdate = scanClassAnnotations( cls ) ;

        classToMNames.put( cls, methodNames ) ;
        classToAnnoMM.put( cls, annoMM ) ;

        if (fullUpdate) {
            doFullUpdate() ;
        } else {
            updateTracedClass( cls ) ;
        }
    }

    private static String getExternalName( String name ) {
	return name.replace( '/', '.' ) ;
    }

    /** Register a class with the tracing facility.  This form assumes that
     * all of the computation for method names and the mapping from annotation
     * name to MM holder is done at registration time, rather than in the 
     * bytecode enhancer.  This shortens the generated bytecode noticeably.
     * @param cls
     */
    public static void registerClass( final Class<?> cls ) {

	Util util = new Util( false, 0 ) ;
	EnhancedClassData ecd = new EnhancedClassDataReflectiveImpl( 
	    util, cls) ;

        final boolean fullUpdate = scanClassAnnotations( cls ) ;

	classToMNames.put( cls, ecd.getMethodNames() ) ;

        final Map<Class<? extends Annotation>,
            SynchronizedHolder<MethodMonitor>> annoMM =
	    new HashMap<Class<? extends Annotation>,
	        SynchronizedHolder<MethodMonitor>>() ;

	for (Map.Entry<String,String> entry :
	    ecd.getAnnotationToHolderName().entrySet() ) {

	    try {
		final String aname = entry.getKey() ;	// annotation name
		final String fname = entry.getValue() ;	// field name

		final Field fld = cls.getDeclaredField( fname ) ;
		fld.setAccessible(true) ;
		final SynchronizedHolder<MethodMonitor> sh =
		    new SynchronizedHolder<MethodMonitor>() ;
	        fld.set( null, sh) ;

		Class<? extends Annotation> aclass =
		    ecd.getAnnoNameMap().get( aname ) ; 

		if (aclass == null) {
		    final String axname = getExternalName( aname ) ;
		    aclass = (Class<? extends Annotation>)Class.forName(
			axname ) ;
		}

		annoMM.put( aclass, sh ) ;
	    } catch (Exception exc) {
		System.out.println( "Exception: " + exc ) ;
	    }
	}

        classToAnnoMM.put( cls, annoMM ) ;

        if (fullUpdate) {
            doFullUpdate() ;
        } else {
            updateTracedClass( cls ) ;
        }
    }

    /** Provided so that implementation of the MethodMonitor interface can
     * obtain the method name for use in log reports or for other purposes.
     * 
     * @param cls The enhanced class
     * @param identifier An Integer representing the method name.
     * @return The name of the method corresponding to the identifier.
     */
    public static String getMethodName( Class<?> cls, int identifier ) {
        List<String> names = classToMNames.get( cls ) ;

        if (names == null) {
            throw new RuntimeException( "Class " + cls + " not found in map" ) ;
        }

        if (identifier < 0 || identifier >= names.size()) {
            throw new RuntimeException( "identifier is out of range" ) ;
        }

        return names.get( identifier ) ;
    }

    public static int getMethodIdentifier( Class<?> cls, String mname ) {
        List<String> names = classToMNames.get( cls ) ;

        if (names == null) {
            throw new RuntimeException( "Class " + cls + " not found in map" ) ;
        }

        for (int ctr=0; ctr<names.size(); ctr++) {
            String str = names.get(ctr) ;
            if (str.equals( mname )) {
                return ctr ;
            }
        }

        return -1 ;
    }

    private static final MethodMonitorGroup checkAnnotation(
        Class<? extends Annotation> annoClass ) {

        final MethodMonitorGroup mmg =
            annoClass.getAnnotation( MethodMonitorGroup.class ) ;

        if (mmg == null) {
            throw new RuntimeException( "Annotation " + annoClass
                + " does not have the MethodMonitorGroup annotation" ) ;
        } else {
            return mmg ;
        }
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

        final boolean fullUpdate = scanAnnotation( annot,
            checkAnnotation( annot ) );

        annotationToMMF.put( annot, mmf ) ;

        if (fullUpdate) {
            doFullUpdate() ;
        } else {
            updateAnnotation( annot ) ;
        }
    }

    /** Remove the MethodMonitorFactory (if any) that is associated with annot.
     * 
     * @param annot
     */
    public static void clear( Class<? extends Annotation> annot ) {

        final boolean fullUpdate = scanAnnotation( annot,
            checkAnnotation( annot ) );

        annotationToMMF.remove( annot ) ;

        if (fullUpdate) {
            doFullUpdate() ;
        } else {
            updateAnnotation( annot ) ;
        }
    }

    /** Return the MethodMonitorFactory registered against the annotation, or
     * null if nothing is registered.
     *
     * @param annot
     * @return
     */
    public static MethodMonitorFactory registeredFactory(
        Class<? extends Annotation> annot ) {

        final boolean fullUpdate = scanAnnotation( annot,
            checkAnnotation( annot ) );

        if (fullUpdate) {
            doFullUpdate() ;
        }

        return annotationToMMF.get( annot ) ;
    }

    /** Return the current MethodMonitor in use for the given cls and annot.
     * Returns null if no MethodMonitor is in use. Throws an exception if
     * either cls is not a traced class, or annot is not a tracing annotation
     * on cls.
     *
     * @param cls The Traced class.
     * @param annot A trace annotation on cls.
     * @return The MethodMonitor, if any.
     */
    public static MethodMonitor getMethodMonitorForClass( final Class<?> cls,
        final Class<? extends Annotation> annot ) {
        Map<Class<? extends Annotation>,SynchronizedHolder<MethodMonitor>> map =
            classToAnnoMM.get( cls ) ;

        if (map == null) {
            throw new RuntimeException( "Class "
                + cls + " is not a traced class.") ;
        }

        SynchronizedHolder<MethodMonitor> holder = map.get( annot ) ;

        if (holder == null) {
            throw new RuntimeException( "Annotation " + annot
                + " is not a tracing annotation defined on class " + cls ) ;
        }

        return holder.content() ;
    }
}
