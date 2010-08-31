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

package com.sun.tools.corba.se.enhancer;

import com.sun.corba.se.spi.orbutil.tf.EnhancedClassData;
import com.sun.corba.se.spi.orbutil.tf.TraceEnhancementException;
import com.sun.corba.se.spi.orbutil.tf.annotation.TFEnhanced;
import com.sun.corba.se.spi.orbutil.tf.annotation.TraceEnhanceLevel;
import org.objectweb.asm.AnnotationVisitor;
import org.objectweb.asm.ClassAdapter;
import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.FieldVisitor;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Type;

/**
 *
 * @author ken
 */
public class TFEnhanceAdapter extends ClassAdapter {
    private static final String TFENHANCED_ANNO_DESC =
        Type.getDescriptor( TFEnhanced.class ) ;
    private static final String TRACE_ENHANCE_LEVEL_DESC =
        Type.getDescriptor( TraceEnhanceLevel.class ) ;

    private boolean firstCall = true ;


    // Holder for actual value of TFEnhanced annotation (NONE if no
    // annotation present.
    private final TraceEnhanceLevel[] present = new TraceEnhanceLevel[1] ;

    private final TraceEnhanceLevel required ; // required for next phase to run
    private final TraceEnhanceLevel result ;   // if at required level, resulting level

    private final EnhancedClassData ecd ;

    public TFEnhanceAdapter( ClassVisitor cv, TraceEnhanceLevel required,
        TraceEnhanceLevel result, EnhancedClassData ecd ) {
        super( cv ) ;
        this.required = required ;
        this.result = result ;
        this.ecd = ecd ;
        present[0] = TraceEnhanceLevel.NONE ;
    }

    private void checkForTFEnhanceAnnotation() {
        if (firstCall) {
            firstCall = false ;
            if (present[0] != required) {
                throw new TraceEnhancementException(
                    "Class " + ecd.getClassName()
                    + " has trace enhancement level " + present[0] 
                    + " but " + required + " is required.") ;
            }

            // Write out annotation with result level.
            AnnotationVisitor av = super.visitAnnotation( TFENHANCED_ANNO_DESC,
                true ) ;
            av.visitEnum( "stage", TRACE_ENHANCE_LEVEL_DESC, result.name() ) ;
            av.visitEnd() ;
        }
    }

    @Override
    public void visitInnerClass( String name,
        String outerName, String innerName, int access ) {

        checkForTFEnhanceAnnotation();
        super.visitInnerClass( name, outerName, innerName, access ) ;
    }

    @Override
    public FieldVisitor visitField( int access, String name, String desc,
        String signature, Object value ) {

        checkForTFEnhanceAnnotation();
        return super.visitField(access, name, desc, signature, value) ;
    }

    @Override
    public MethodVisitor visitMethod( int access, String name, String desc,
        String signature, String[] exceptions ) {

        checkForTFEnhanceAnnotation();
        return super.visitMethod(access, name, desc, signature, exceptions) ;
    }

    @Override
    public AnnotationVisitor visitAnnotation( String desc, boolean isVisible ) {
        if (desc.equals( TFENHANCED_ANNO_DESC )) {
            // Consume the TFEnhanced annotation here.  We'll write out a new
            // one above.
            return new AnnotationVisitor() {
                public void visit(String name, Object value) {
                }

                public void visitEnum(String name, String desc, String value) {
                    if (name.equals( "stage")) {
                        present[0] = Enum.valueOf( TraceEnhanceLevel.class, 
                            value ) ;
                    }
                }

                public AnnotationVisitor visitAnnotation(String name, 
                    String desc) {

                    return null ;
                }

                public AnnotationVisitor visitArray(String name) {
                    return null ;
                }

                public void visitEnd() {
                }
            } ;
        } else {
            final AnnotationVisitor av = super.visitAnnotation( desc, isVisible ) ;
            return av ;
        }
    }
}
