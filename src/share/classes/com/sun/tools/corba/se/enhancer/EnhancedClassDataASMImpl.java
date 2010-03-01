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

package com.sun.tools.corba.se.enhancer;

import java.util.List;
import java.util.Set;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.Type;
import org.objectweb.asm.tree.AnnotationNode;
import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.MethodNode;

public class EnhancedClassDataASMImpl extends EnhancedClassDataBase {
    private final ClassNode currentClass ;

    // Get Set<String> for MM annotations present on class
    private void processClassAnnotations() {
        final List<AnnotationNode> classAnnotations =
            currentClass.visibleAnnotations ;
        if (classAnnotations != null) {
            for (AnnotationNode an : classAnnotations) {
                final String aname = Type.getType( an.desc ).getInternalName() ;
                if (annotationNames.contains( aname )) {
                    annoNamesForClass.add( aname ) ;
                    annoToHolderName.put( aname,
                        "__$mm$__" + annoNamesForClass.size() ) ;
                }
            }

            if (util.getDebug()) {
                util.msg( "Enhancing class " + currentClass.name ) ;
                util.msg( "\tannoNamesForClass = " + annoNamesForClass ) ;
                util.msg( "\tannoToHolderName = " + annoToHolderName ) ;
            }
        }
    }

    // Scan methods:
    //    - Build List<String> to map names of MM annotated methods to ints
    //      validate: such methods must have exactly 1 MM annotation that
    //          is in annoNamesForClass.
    //    - Build Set<String> of all MethodInfo annotated methods.
    //      validate: such methods must be private, return void, and have
    //          an empty body.  May NOT have MM annotation.
    private void scanMethods() {
        final List<MethodNode> methods = currentClass.methods ;
        for (MethodNode mn : methods) {
            final String mname = mn.name ;
            final String mdesc = util.getFullMethodDescriptor( mn ) ;

            String annoForMethod = null ;
            boolean hasMethodInfoAnno = false ;

            final List<AnnotationNode> annotations = mn.visibleAnnotations ;
            if (annotations != null) {
                for (AnnotationNode an : annotations) {
                    final String aname =
                        Type.getType( an.desc ).getInternalName() ;

                    if (aname.equals( INFO_METHOD_NAME)) {
                        hasMethodInfoAnno = true ;
                    } else if (annoNamesForClass.contains( aname)) {
                        if (annoForMethod == null) {
                            annoForMethod = aname ;
                        } else {
                            util.error( "Method " + mdesc
                                + " for Class " + currentClass.name
                                + "has multiple MM annotations" ) ;
                        }
                    } else if (annotationNames.contains( aname )) {
                            util.error( "Method " + mdesc
                                + " for Class " + currentClass.name
                                + " has an MM annotation which "
                                + "is not on its class" ) ;
                    }
                }

                if (hasMethodInfoAnno && annoForMethod != null) {
                    util.error( "Method " + mdesc
                        + " for Class " + currentClass.name
                        + " has both @InfoMethod annotation and"
                        + " a MM annotation" ) ;
                }

                // This check is not really essential, but it simplifies
                // passing information to later phases for code generation
                // if we can assume that all @InfoMethod annotated methods
                // are non-static. (Simply because we only need to look for
                // INVOKESPECIAL).
                final boolean isStatic = util.hasAccess( mn.access,
                    Opcodes.ACC_STATIC ) ;
                if (hasMethodInfoAnno && isStatic) {
                    util.error( "Method " + mdesc            
                        + " for Class " + currentClass.name
                        + " is a static method, but must not be" ) ;
                }

                // TF Annotations are not permitted on constructors
                if (mname.equals( "<init>" )) {
                    if (hasMethodInfoAnno) {
                        util.error( "Constructors must not have an "
                            + "@InfoMethod annotations") ;
                    } else if (annoForMethod != null) {
                        util.error( "Constructors must not have an "
                            + "MM annotation") ;
                    }
                }

                // Both infoMethods and MM annotated methods go into methodNames
                methodNames.add( mname ) ;

                // annoForMethod will not be null here
                if (hasMethodInfoAnno) {
                    infoMethodDescs.add( mdesc ) ;
                } else {
                    mmMethodDescs.add( mdesc ) ;
                    methodToAnno.put( mdesc, annoForMethod ) ;
                }
            }
        }

        if (util.getDebug()) {
            util.msg( "\tinfoMethodSignature = " + infoMethodDescs ) ;
            util.msg( "\tmmMethodSignature = " + mmMethodDescs ) ;
            util.msg( "\tmethodNames = " + methodNames ) ;
            util.msg( "\tmethodToAnno = " + methodToAnno ) ;
        }
    }

    public EnhancedClassDataASMImpl( Util util, Set<String> mmAnnotations,
        ClassNode cn ) {

        super( util, mmAnnotations ) ;

        currentClass = cn ;

        // Compute data here: only look at data available to
        // java reflection.
        className = cn.name ;
        processClassAnnotations() ;
        scanMethods();
    }
}
