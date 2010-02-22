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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.sun.corba.se.spi.orbutil.generic.NullaryFunction;
import com.sun.corba.se.spi.orbutil.generic.SynchronizedHolder;
import com.sun.corba.se.spi.orbutil.generic.UnaryFunction;

import com.sun.corba.se.spi.orbutil.tf.MethodMonitor;
import com.sun.corba.se.spi.orbutil.tf.MethodMonitorRegistry;
import com.sun.corba.se.spi.orbutil.tf.annotation.InfoMethod;
import java.io.PrintWriter;
import org.objectweb.asm.ClassAdapter;

import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.Label;
import org.objectweb.asm.MethodAdapter;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.Type;
import org.objectweb.asm.commons.AdviceAdapter;
import org.objectweb.asm.commons.GeneratorAdapter;
import org.objectweb.asm.commons.LocalVariablesSorter;
import org.objectweb.asm.tree.AbstractInsnNode;
import org.objectweb.asm.tree.AnnotationNode;
import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.FieldNode;
import org.objectweb.asm.tree.InsnList;
import org.objectweb.asm.tree.LabelNode;
import org.objectweb.asm.tree.LocalVariableNode;
import org.objectweb.asm.tree.MethodInsnNode;
import org.objectweb.asm.tree.MethodNode;
import org.objectweb.asm.util.CheckClassAdapter;

/** ClassFile enhancer for the tracing facility.  This modifies the bytecode
 * for an applicable class, then returns the updated bytecode.
 * Makes extensive use of the ASM library.
 *
 * This is split into two parts.  The first part modifies the schema
 * of the class as follows:
 * <ul>
 * <li>Adds static fields as required for the SynchronizedHolder<MethodMonitor> 
 * instances.
 * <li>Modifies the static initializer to set up the new fields, and register
 * the class with the MethodMonitorRegistry.  This also constructs the list
 * of method names, which is needed by the second part.
 * <li>Re-writes all @InfoMethod methods to take two extra parameters at the
 * end of their argument lists.
 * <li>Re-writes all calls to @InfoMethod methods to supply the two extra 
 * parameters to all calls.
 * <li>Checks that @InfoMethod methods (which must be private) are only called
 * from MM annotated methods.
 * </ul>
 * <p>
 * The second part modifies the MM annotated methods as follows:
 * <li>Adds a preamble to set up some local variables, and to call 
 * the MethodMonitor.enter method when active.
 * <li>Adds a finally block at the end of the method that handles calling
 * MethodMonitor.exit whenever an exception is thrown or propagated from the
 * body of the method.
 * <li>Modifies all exit point in the method as follows:
 * <ul>
 * <li>If the exit point is a return, call MethodMonitor.exit before the return.
 * <li>If the exit point is a throw, call MethodMonitor.exception before the throw.
 * </ul>
 * </ul>
 * <p>
 * Note that the second part could be run in a ClassFileTransformer or ClassLoader
 * if desired, since this design enhances the class files in place for the first
 * part.
 *
 * @author ken
 */
public class TraceEnhanceFunction implements EnhanceTool.EnhanceFunction {
    private boolean dryrun ;
    private Set<String> annotationNames = null ;

    private Util util = new Util();

    // These fields are initialized in the evaluate method.
    private ClassNode currentClass = null ;
    private EnhancedClassData ecd = null ;

    public TraceEnhanceFunction() {
    }

    public void setMMGAnnotations(Set<String> mmgAnnotations) {
        annotationNames = mmgAnnotations ;
    }

    public void setDebug(boolean flag) {
        util.setDebug( flag ) ;
    }

    public void setVerbose(int level) {
        util.setVerbose(level);
    }

    public void setDryrun(boolean flag) {
        dryrun = flag ;
    }

    private boolean hasAccess( int access, int flag ) {
        return (access & flag) == flag ;
    }

    class ClassEnhancer extends ClassAdapter {
        private EnhancedClassData ecd ;
        private boolean hasStaticInitializer = false ;

        public ClassEnhancer( EnhancedClassData ecd, ClassVisitor cv ) {
            super( cv ) ;
            this.ecd = ecd ;
        }


        @Override
        public void visitEnd() {
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
                util.info( "Adding field " + fname + " of type " + desc ) ;
                cv.visitField( acc, fname, desc, sig, null ) ;
            }

            if (!hasStaticInitializer) {
                int siacc = Opcodes.ACC_STATIC + Opcodes.ACC_PRIVATE ;
                MethodVisitor mv = cv.visitMethod( siacc, "<clinit>", "()V",
                    null, null ) ;
                MethodAdapter ma = new StaticInitVisitor( siacc, "()V", mv,
                    util, ecd ) ;

                ma.visitCode() ;
                ma.visitMaxs( 0, 0 ) ;
                ma.visitEnd() ;
            }

            super.visitEnd() ;
        }

        // add MethodMonitor and Object parameters to end of params
        // generate body
        private void handleInfoMethod( int access, String name, String desc ) {
            util.info( "InfoMethod " + name + desc ) ;

            String newDesc = util.augmentInfoMethodDescriptor( desc ) ;
            MethodVisitor mv = cv.visitMethod( access, name, newDesc,
                null, null ) ;

            mv.visitCode() ;

            Type[] argTypes = Type.getArgumentTypes( desc ) ;
            int argSize = 0 ;
            for (Type type : argTypes) {
                argSize += type.getSize() ;
            }

            // Parameter layout on stack:
            // 0: this
            // 1 to argSize: declared args
            // size: MethodMonitor
            // size+1: caller ident
            int mmIndex = argSize ;
            int cidIndex = argSize + 1 ;

            Label jumpLabel = new Label() ;
            mv.visitVarInsn( Opcodes.ALOAD, mmIndex ) ;
            mv.visitJumpInsn( Opcodes.IFNULL, jumpLabel) ;

            mv.visitVarInsn( Opcodes.ALOAD, mmIndex ) ;
            util.wrapArgs( mv, access, desc ) ;
            mv.visitVarInsn( Opcodes.ALOAD, cidIndex ) ;
            util.emitIntConstant( mv, ecd.getMethodIndex( name )) ;
            mv.visitMethodInsn( Opcodes.INVOKESTATIC,
                "java/lang/Integer", "valueOf",
                "(I)Ljava/lang/Integer;" );

            mv.visitMethodInsn( Opcodes.INVOKEINTERFACE,
                EnhancedClassData.MM_NAME, "info", newDesc ) ;

            mv.visitLabel( jumpLabel ) ;
            mv.visitInsn( Opcodes.RETURN ) ;

            mv.visitMaxs( 0, 0 ) ;
            mv.visitEnd() ;
        }

        public class InfoMethodCallRewriter extends GeneratorAdapter {
            public InfoMethodCallRewriter( MethodVisitor mv,
                int acc, String name, String desc ) {

                super( mv, acc, name, desc ) ;
            }

            @Override
            public void visitMethodInsn( int opcode, String owner,
                String name, String desc ) {
                util.info( "MM method: visitMethodInsn: " + owner
                    + "." + name + desc ) ;

                // If opcode is INVOKESPECIAL, owner is this class, and name/desc
                // are in the infoMethodDescs set, update the desc for the call
                // and add the extra parameters to the end of the call.
                String fullDesc = util.getFullMethodDescriptor( name, desc ) ;
                if ((opcode == Opcodes.INVOKESPECIAL)
                    && (owner.equals( currentClass.name )
                    && (ecd.classifyMethod(fullDesc)
                        == EnhancedClassData.MethodType.INFO_METHOD))) {

                    util.info( "    rewriting method call" ) ;

                    // For the re-write, just pass nulls.  These instructions
                    // will be replaced when tracing is enabled.
                    mv.visitInsn( Opcodes.ACONST_NULL ) ;
                    mv.visitInsn( Opcodes.ACONST_NULL ) ;

                    // For the tracing case
                    // mv.visitVarInsn( Opcodes.ALOAD, __mm.index ) ;
                    // mv.visitVarInsn( Opcodes.ALOAD, __ident.index ) ;

                    String newDesc = util.augmentInfoMethodDescriptor(desc) ;

                    mv.visitMethodInsn(opcode, owner, name, newDesc );
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
                util.info( "MM method: visitMethodInsn: " + owner
                    + "." + name + desc ) ;

                // If opcode is INVOKESPECIAL, owner is this class, and name/desc
                // are in the infoMethodDescs set, update the desc for the call
                // and add the extra parameters to the end of the call.
                String fullDesc = util.getFullMethodDescriptor( name, desc ) ;
                if ((opcode == Opcodes.INVOKESPECIAL)
                    && (owner.equals( currentClass.name )
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
            // Enhance the class first (part 1).
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
                    hasStaticInitializer = true ;
                    return new StaticInitVisitor( access, desc, mv, util,
                        ecd ) ;

                case INFO_METHOD :
                    handleInfoMethod( access, name, desc ) ;
                    return null ;

                case MONITORED_METHOD :
                    mv = super.visitMethod( access, name, desc,
                        sig, exceptions ) ;
                    return new InfoMethodCallRewriter( mv, access, name, desc ) ;

                case NORMAL_METHOD :
                    mv = super.visitMethod( access, name, desc,
                        sig, exceptions ) ;
                    return new NormalMethodChecker( mv, access, name, desc) ;
            }

            return null ;
        }
    }

    public byte[] evaluate( final byte[] arg) {
        final ClassNode cn = new ClassNode() ;
        final ClassReader cr = new ClassReader( arg ) ;
        cr.accept( cn, 0 ) ;

        // Ignore annotations and interfaces.
        if (hasAccess(cn.access, Opcodes.ACC_ANNOTATION) ||
            hasAccess(cn.access, Opcodes.ACC_INTERFACE)) {
            return null ;
        }

        // We need EnhancedClassData to hold the results of scanning the class
        // for various details about annotations.  This makes it easy to write
        // a one-pass visitor in part 2 to actually add the tracing code.
        // Note that the ECD can easily be computed either at build time
        // from the classfile byte[] (using ASM), or at runtime, directly
        // from a Class object using reflection.
        ecd = new EnhancedClassDataASMImpl( util, annotationNames, cn ) ;

        // If this class is not annotated as a traced class, ignore it.
        if (!ecd.isTracedClass()) {
            return null ;
        }

        final byte[] phase1 = util.transform( arg,
            new UnaryFunction<ClassWriter, ClassAdapter>() {
                public ClassAdapter evaluate(ClassWriter arg) {
                    return new ClassEnhancer( ecd, arg ) ;
                }
            }
        ) ;

        // Only communication from part 1 to part2 is a byte[] and
        // the EnhancedClassData.  Since the list of methodNames
        // is registered with the MethodMonitorRegistry, a runtime
        // version of this code could easily be made.
        //     Implementation note: runtime would keep byte[] stored of
        //     original version whenever tracing is enabled, so that
        //     disabling tracing simply means using a ClassFileTransformer
        //     to get back to the original code.
        //
        // Then add tracing code (part 2).
        //     This is a pure visitor using the AdviceAdapter.
        //     It must NOT modify its input visitor (or you get an
        //     infinite series of calls to onMethodExit...)

        final byte[] phase2 = util.transform( phase1, new UnaryFunction<ClassWriter, ClassAdapter>() {
            public ClassAdapter evaluate(ClassWriter arg) {
                return new ClassTracer( ecd, arg ) ;
            }
        }) ;

        return phase2 ;
    }

    private class ClassTracer extends ClassAdapter {
        private final EnhancedClassData ecd ;

        public ClassTracer( final EnhancedClassData ecd, ClassVisitor cv ) {
            super( cv ) ;
            this.ecd = ecd ;
        }

        // - Scan method body:
        //   - for each return, add the finally body
        //   - for each call to an InfoMethod, add extra parameters to the
        //     end of the call (note that it is MUCH easier to recognize the
        //     end than the start of a method call, since nested calls and
        //     complex expressions make recognizing the start quite difficult)
        // - add preamble
        // - add outer exception handler
        private class MonitoredMethodEnhancer extends AdviceAdapter {
            private int access ;
            private String name ;
            private String desc ;

            final Set<Integer> returnOpcodes = new HashSet<Integer>() ;

            private final Label start = new Label() ;
            private final LabelNode startNode = new LabelNode( start ) ;

            private final Label excHandler = new Label() ;
            private final LabelNode excHandlerNode = new LabelNode( excHandler ) ;

            private final Label end = new Label() ;
            private final LabelNode endNode = new LabelNode( end ) ;
           
            private final MethodVisitor lmv ;
            private final LocalVariableNode __result ;
            private final LocalVariableNode __ident ;
            private final LocalVariableNode __mm ;
            private final LocalVariableNode __enabled ;

            public MonitoredMethodEnhancer( int access, String name,
                String desc, MethodVisitor mv ) {
                super( mv, access, name, desc ) ;
                this.access = access ;
                this.name = name ;
                this.desc = desc ;

                returnOpcodes.add( Opcodes.RETURN ) ;
                returnOpcodes.add( Opcodes.IRETURN ) ;
                returnOpcodes.add( Opcodes.ARETURN ) ;
                returnOpcodes.add( Opcodes.LRETURN ) ;
                returnOpcodes.add( Opcodes.FRETURN ) ;
                returnOpcodes.add( Opcodes.DRETURN ) ;

                this.lmv = mv ;
                Type type = Type.getReturnType( desc ) ;

                // XXX probably need to move these inits to onMethodEnter.
                if (!type.equals( Type.VOID_TYPE)) {
                    __result = new LocalVariableNode( "__$result$__", 
                        type.getDescriptor(),
                        null, startNode, endNode, newLocal(type) ) ; 
                } else {
                    __result = null ;
                }

                type = Type.getType(Object.class) ;
                __ident = new LocalVariableNode( "__$ident$__",
                    type.getDescriptor(),
                    null, startNode, endNode, newLocal(type)) ;

                type = Type.getType(MethodMonitor.class );
                __mm = new LocalVariableNode( "__$mm$__",
                    type.getDescriptor(),
                    null, startNode, endNode, newLocal(type) ) ;

                type = Type.BOOLEAN_TYPE ;
                __enabled = new LocalVariableNode( "__$enabled$__", 
                    type.getDescriptor(), 
                    null, startNode, endNode, newLocal(type) ) ;
            }

            @Override
            public void onMethodEnter() {
                util.info( "MM method: onMethodEnter" ) ;
                lmv.visitLabel(start);

                // __result = null or 0 (type specific, omitted if void return)
                if (__result != null) {
                    util.initLocal( lmv, __result ) ;
                }

                // Object __ident = null ;
                util.initLocal( lmv, __ident ) ;

                // final MethodMonitor __mm = __mmXX.content() ;
                // (for the appropriate XX for this method)
                __mm.accept( lmv ) ;
                final String fullDesc = util.getFullMethodDescriptor(name,desc) ;
                util.info( "fullDesc = " + fullDesc ) ;

                final String fname = ecd.getHolderName( fullDesc );

                lmv.visitFieldInsn( Opcodes.GETSTATIC, currentClass.name,
                    fname, EnhancedClassData.SH_NAME );
                lmv.visitMethodInsn( Opcodes.INVOKEVIRTUAL,
                    EnhancedClassData.SH_NAME, "content",
                    "()Ljava/lang/Object;" );
                lmv.visitTypeInsn( Opcodes.CHECKCAST,
                    EnhancedClassData.MM_NAME );
                lmv.visitVarInsn( Opcodes.ISTORE, __mm.index );

                // final boolean enabled = __mm != null ;
                Label lab1 = new Label() ;
                Label lab2 = new Label() ;
                lmv.visitVarInsn( Opcodes.ILOAD, __mm.index ) ;
                lmv.visitJumpInsn( Opcodes.IFNULL, lab1) ;
                lmv.visitInsn( Opcodes.ICONST_1 );
                lmv.visitJumpInsn( Opcodes.GOTO, lab2);
                lmv.visitLabel( lab1 ) ;
                lmv.visitInsn( Opcodes.ICONST_0 );
                lmv.visitVarInsn( Opcodes.ISTORE, __enabled.index );
                lmv.visitLabel( lab2 ) ;

                // if (enabled) {
                Label skipPreamble = new Label() ;
                lmv.visitVarInsn( Opcodes.ILOAD, __enabled.index ) ;
                lmv.visitJumpInsn( Opcodes.IFEQ, skipPreamble );

                // __ident = <method constant>
                util.emitIntConstant( lmv, ecd.getMethodIndex(name) ) ;
                String owner = Type.getInternalName( Integer.class ) ;
                lmv.visitMethodInsn( Opcodes.INVOKESTATIC, owner, "valueOf",
                    "(I)Ljava.lang.Integer;" ) ;
                lmv.visitVarInsn( Opcodes.ASTORE, __mm.index ) ;

                // __mm.enter( __ident, <array of wrapped args> ) ;
                lmv.visitVarInsn( Opcodes.ALOAD, __mm.index )  ;
                lmv.visitVarInsn( Opcodes.ALOAD, __ident.index ) ;

                util.wrapArgs( lmv, access, desc ) ;

                lmv.visitMethodInsn( Opcodes.INVOKEINTERFACE,
                    EnhancedClassData.MM_NAME, "enter",
                    "(Ljava/lang/Object;[Ljava/lang/Object;)" ) ;

                // }
                lmv.visitLabel( skipPreamble ) ;
            }

            private void emitFinally() {
                Label skipLabel = new Label() ;
                lmv.visitVarInsn( Opcodes.ILOAD, __enabled.index ) ;
                lmv.visitJumpInsn( Opcodes.IFEQ, skipLabel ) ;

                lmv.visitVarInsn( Opcodes.ALOAD, __mm.index ) ;
                lmv.visitVarInsn( Opcodes.ALOAD, __ident.index ) ;

                Type rtype = Type.getReturnType( desc ) ;
                if (rtype.equals( Type.VOID_TYPE )) {
                    lmv.visitMethodInsn( Opcodes.INVOKEVIRTUAL,
                        EnhancedClassData.MM_NAME, "exit",
                        "(Ljava/lang/Object;)V" ) ;
                } else {
                    util.wrapArg( lmv, __result.index,
                        Type.getType( __result.desc ) ) ;

                    lmv.visitMethodInsn( Opcodes.INVOKEVIRTUAL,
                        EnhancedClassData.MM_NAME, "exit",
                        "(Ljava/lang/Object;Ljava/lang/Object;)V" ) ;
                }

                lmv.visitLabel( skipLabel ) ;
            }

            @Override
            public void onMethodExit( int opcode ) {
                util.info( "MM method: onMethodExit" ) ;
                if (returnOpcodes.contains(opcode )) {
                    util.info( "    handling return" ) ;
                    util.storeFromXReturn( lmv, opcode, __result ) ;

                    emitFinally() ;

                    util.loadFromXReturn( lmv, opcode, __result ) ;
                } else if (opcode == Opcodes.ATHROW) {
                    util.info( "    handling throw" ) ;
                    int exc = newLocal( Type.getType(Throwable.class)) ;
                    lmv.visitVarInsn( Opcodes.ASTORE, exc) ;

                    Label skipLabel = new Label() ;
                    lmv.visitVarInsn( Opcodes.ILOAD, __enabled.index ) ;
                    lmv.visitJumpInsn( Opcodes.IFEQ, skipLabel ) ;

                    // emit code for reporting exception
                    lmv.visitVarInsn( Opcodes.ALOAD, __mm.index ) ;
                    lmv.visitVarInsn( Opcodes.ALOAD, __ident.index ) ;
                    lmv.visitVarInsn( Opcodes.ALOAD, exc ) ;
                    lmv.visitMethodInsn( Opcodes.INVOKEVIRTUAL,
                         EnhancedClassData.MM_NAME, "exception",
                         "(Ljava/lang/Object;Ljava/lang/Throwable;)V") ;

                    lmv.visitLabel( skipLabel ) ;
                    // restore exception from local for following ATHROW
                    // (this will be caught in the finally exception handler,
                    // which will handle calling MethodMonitor.exit).
                    lmv.visitVarInsn( Opcodes.ALOAD, exc ) ;
                } // all others can be ignored.
            }

            @Override
            public void visitMethodInsn( int opcode, String owner,
                String name, String desc ) {
                util.info( "MM method: visitMethodInsn: " + owner
                    + "." + name + desc ) ;

                // If opcode is INVOKESPECIAL, owner is this class, and name/desc
                // are in the infoMethodDescs set, update the desc for the call
                // and add the extra parameters to the end of the call.
                String fullDesc = util.getFullMethodDescriptor( name, desc ) ;
                if ((opcode == Opcodes.INVOKESPECIAL)
                    && (owner.equals( currentClass.name )
                    && (ecd.classifyMethod(fullDesc)
                        == EnhancedClassData.MethodType.INFO_METHOD))) {

                    util.info( "    rewriting method call" ) ;

                    // For the re-write, just pass nulls.  These instructions
                    // will be replaced when tracing is enabled.
                    mv.visitInsn( Opcodes.ACONST_NULL ) ;
                    mv.visitInsn( Opcodes.ACONST_NULL ) ;

                    // For the tracing case
                    // mv.visitVarInsn( Opcodes.ALOAD, __mm.index ) ;
                    // mv.visitVarInsn( Opcodes.ALOAD, __ident.index ) ;

                    String newDesc = util.augmentInfoMethodDescriptor(desc) ;

                    mv.visitMethodInsn(opcode, owner, name, newDesc );
                }
            }

            @Override
            public void visitEnd() {
                util.info( "MM method: visitEnd" ) ;
                lmv.visitLabel( excHandler  ) ;

                // Store the exception
                int excIndex = newLocal( Type.getType( Throwable.class ) ) ;
                lmv.visitVarInsn( Opcodes.ASTORE, excIndex ) ;

                emitFinally() ;

                // throw the exception
                lmv.visitVarInsn( Opcodes.ALOAD, excIndex ) ;
                lmv.visitInsn( Opcodes.ATHROW ) ;

                lmv.visitLabel( end ) ;

                lmv.visitTryCatchBlock( start, end, excHandler, null );
            }
        }


        @Override
        public MethodVisitor visitMethod( final int access, final String name,
            final String desc, final String sig, final String[] exceptions ) {
            // Enhance the class first (part 1).
            // - Modify all of the @InfoMethod methods with extra arguments
            // - Modify all calls to @InfoMethod methods to add the extra arguments
            //   or to flag an error if NOT called from an MM method.

            String fullDesc = util.getFullMethodDescriptor(name, desc) ;
            EnhancedClassData.MethodType mtype =
                ecd.classifyMethod(fullDesc) ;

            MethodVisitor mv = super.visitMethod( access, name, desc,
                sig, exceptions ) ;

            switch (mtype) {
                case STATIC_INITIALIZER :
                case INFO_METHOD :
                case NORMAL_METHOD :
                    return mv ;

                case MONITORED_METHOD :
                    return new MonitoredMethodEnhancer( access, name, desc, mv ) ;

            }

            return null ;
        }
    }
}
