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

import org.objectweb.asm.ClassAdapter;
import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.Label;
import org.objectweb.asm.MethodAdapter;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.Type;
import org.objectweb.asm.commons.GeneratorAdapter;

import com.sun.corba.se.spi.orbutil.generic.SynchronizedHolder ;

public class ClassEnhancer extends ClassAdapter {
    private Util util ;
    private EnhancedClassData ecd ;
    private boolean hasStaticInitializer = false ;

    public ClassEnhancer( Util util, EnhancedClassData ecd,
        ClassVisitor cv ) {

        super( cv ) ;
        this.util = util ;
        this.ecd = ecd ;
    }

    private void info( int level, String msg ) {
        util.info( level, "ClassEnhancer: " + msg ) ;
    }

    @Override
    public void visitEnd() {
        info( 1, "visitEnd") ;
        // Add the additional fields
        final String desc = Type.getDescriptor(
            SynchronizedHolder.class ) ;

        final int acc = Opcodes.ACC_PRIVATE + Opcodes.ACC_STATIC
            + Opcodes.ACC_FINAL ;

        // Signature is actually L../SynchronizedHolder<L.../MethodMonitor;>
        // where the ... are replaced with appropriate packages.  Not
        // that we actually need a signature here.
        final String sig = null ;

        for (String fname : ecd.getAnnotationToHolderName().values()) {
            info( 2, "Adding field " + fname + " of type " + desc ) ;
            cv.visitField( acc, fname, desc, sig, null ) ;
        }

        if (!hasStaticInitializer) {
            info( 2, "creating static init") ;
            int siacc = Opcodes.ACC_STATIC + Opcodes.ACC_PRIVATE ;
            MethodVisitor mv = cv.visitMethod( siacc, "<clinit>", "()V",
                null, null ) ;
            if (util.getDebug()) {
                mv = new SimpleMethodTracer(mv) ;
            }
            MethodAdapter ma = new StaticInitVisitor( siacc, "()V", mv,
                util, ecd ) ;

            ma.visitCode() ;
            ma.visitMaxs( 0, 0 ) ;
            ma.visitEnd() ;
        }

        super.visitEnd() ;
    }

    public class InfoMethodRewriter extends GeneratorAdapter {
        private int access ;
        private String name ;
        private String desc ;

        // Note that desc is the descriptor of the unaugmented method:
        // no MethodMonitor or Object at the end of the args.
        public InfoMethodRewriter( MethodVisitor mv,
            int acc, String name, String desc ) {

            super( mv, acc, name, desc ) ;
            this.access = acc ;
            this.name = name ;
            this.desc = desc ;
        }

        // add MethodMonitor and Object parameters to end of params
        // generate body
        @Override
        public void visitCode() {
            info( 1, "InfoMethodRewriter: visitCode " + name + desc ) ;

            final boolean isStatic = util.hasAccess( access, 
                Opcodes.ACC_STATIC ) ;
            final Type[] argTypes = Type.getArgumentTypes( desc ) ;
            int argSize = isStatic ? 0 : 1 ;
            for (Type type : argTypes) {
                argSize += type.getSize() ;
            }

            info( 2, "InfoMethodRewriter: initial arg size " + argSize ) ;

            int mmIndex = argSize ;
            int cidIndex = argSize + 1 ;

            Label jumpLabel = new Label() ;
            mv.visitVarInsn( Opcodes.ALOAD, mmIndex ) ;
            mv.visitJumpInsn( Opcodes.IFNULL, jumpLabel) ;

            // mm.info( <args>, callerId, selfId )
            mv.visitVarInsn( Opcodes.ALOAD, mmIndex ) ;

            util.wrapArgs( mv, access, desc ) ;

            mv.visitVarInsn( Opcodes.ALOAD, cidIndex ) ;

            util.emitIntConstant( mv, ecd.getMethodIndex( name )) ;
            mv.visitMethodInsn( Opcodes.INVOKESTATIC,
                "java/lang/Integer", "valueOf",
                "(I)Ljava/lang/Integer;" );

            mv.visitMethodInsn( Opcodes.INVOKEINTERFACE,
                EnhancedClassData.MM_NAME, "info",
                "([Ljava/lang/Object;Ljava/lang/Object;Ljava/lang/Object;)V") ;

            mv.visitLabel( jumpLabel ) ;

            // Method should already have a RETURN at the end, so just let
            // the default behavior copy it.
            // mv.visitInsn( Opcodes.RETURN ) ;
        }
    }

    public class InfoMethodCallRewriter extends GeneratorAdapter {
        public InfoMethodCallRewriter( MethodVisitor mv,
            int acc, String name, String desc ) {

            super( mv, acc, name, desc ) ;
        }

        @Override
        public void visitMethodInsn( int opcode, String owner,
            String name, String desc ) {
            info( 1, "InfoMethodCallRewriter: visitMethodInsn: " + owner
                + "." + name + desc ) ;

            // If opcode is INVOKESPECIAL, owner is this class, and name/desc
            // are in the infoMethodDescs set, update the desc for the call
            // and add the extra parameters to the end of the call.
            String fullDesc = util.getFullMethodDescriptor( name, desc ) ;
            if ((opcode == Opcodes.INVOKESPECIAL)
                && (owner.equals( ecd.getClassName() )
                && (ecd.classifyMethod(fullDesc)
                    == EnhancedClassData.MethodType.INFO_METHOD))) {

                info( 2, "InfoMethodCallRewriter: visitMethodInsn: "
                    + "rewriting method call" ) ;

                // For the re-write, just pass nulls.  These instructions
                // will be replaced when tracing is enabled.
                mv.visitInsn( Opcodes.ACONST_NULL ) ;
                mv.visitInsn( Opcodes.ACONST_NULL ) ;

                // For the tracing case
                // mv.visitVarInsn( Opcodes.ALOAD, __mm.index ) ;
                // mv.visitVarInsn( Opcodes.ALOAD, __ident.index ) ;

                String newDesc = util.augmentInfoMethodDescriptor(desc) ;

                mv.visitMethodInsn(opcode, owner, name, newDesc );
            } else {
                mv.visitMethodInsn(opcode, owner, name, desc );
            }
        }
    }

    public class NormalMethodChecker extends GeneratorAdapter {
        public NormalMethodChecker( MethodVisitor mv,
            int acc, String name, String desc ) {

            super( mv, acc, name, desc ) ;
        }

        @Override
        public void visitMethodInsn( int opcode, String owner,
            String name, String desc ) {
            info( 1, "NormalMethodChecker: visitMethodInsn: " + owner
                + "." + name + desc ) ;

            // If opcode is INVOKESPECIAL, owner is this class, and name/desc
            // are in the infoMethodDescs set, update the desc for the call
            // and add the extra parameters to the end of the call.
            String fullDesc = util.getFullMethodDescriptor( name, desc ) ;
            if ((opcode == Opcodes.INVOKESPECIAL)
                && (owner.equals( ecd.getClassName() )
                && (ecd.classifyMethod(fullDesc)
                    == EnhancedClassData.MethodType.INFO_METHOD))) {

                util.error( "Method "
                    + util.getFullMethodDescriptor(name,desc)
                    + " in class " + ecd.getClassName() + " makes an "
                    + " illegal call to an @InfoMethod method" ) ;
            }
        }
    }

    @Override
    public MethodVisitor visitMethod( final int access, final String name,
        final String desc, final String sig, final String[] exceptions ) {
        info( 1, "visitMethod " + name + desc ) ;

        // Enhance the class first (this changes the "schema" of the class).
        // - Enhance the static initializer so that the class will be properly
        //   registered with the tracing facility.
        // - Modify all of the @InfoMethod methods with extra arguments
        // - Modify all calls to @InfoMethod methods to add the extra arguments
        //   or to flag an error if NOT called from an MM method.

        String fullDesc = util.getFullMethodDescriptor(name, desc) ;
        EnhancedClassData.MethodType mtype =
            ecd.classifyMethod(fullDesc) ;

        MethodVisitor mv ;

        switch (mtype) {
            case STATIC_INITIALIZER :
                mv = super.visitMethod( access, name, desc,
                    sig, exceptions ) ;
                if (util.getDebug()) {
                    mv = new SimpleMethodTracer(mv) ;
                }
                hasStaticInitializer = true ;
                return new StaticInitVisitor( access, desc, mv, util,
                    ecd ) ;

            case INFO_METHOD :
                String newDesc = util.augmentInfoMethodDescriptor( desc ) ;
                mv = super.visitMethod( access, name, newDesc,
                    sig, exceptions ) ;
                if (util.getDebug()) {
                    mv = new SimpleMethodTracer(mv) ;
                }
                return new InfoMethodRewriter( mv, access, name, desc ) ;

            case MONITORED_METHOD :
                mv = super.visitMethod( access, name, desc,
                    sig, exceptions ) ;
                if (util.getDebug()) {
                    mv = new SimpleMethodTracer(mv) ;
                }
                return new InfoMethodCallRewriter( mv, access, name, desc ) ;

            case NORMAL_METHOD :
                mv = super.visitMethod( access, name, desc,
                    sig, exceptions ) ;
                if (util.getDebug()) {
                    mv = new SimpleMethodTracer(mv) ;
                }
                return new NormalMethodChecker( mv, access, name, desc) ;
        }

        return null ;
    }
} // end of ClassEnhancer
