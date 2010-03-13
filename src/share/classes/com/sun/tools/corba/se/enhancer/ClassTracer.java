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

import com.sun.corba.se.spi.orbutil.generic.SynchronizedHolder;
import java.util.HashSet;
import java.util.Set;
import org.objectweb.asm.ClassAdapter;
import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.Label;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.Type;
import org.objectweb.asm.tree.LabelNode;
import org.objectweb.asm.tree.LocalVariableNode;

import com.sun.corba.se.spi.orbutil.tf.MethodMonitor ;
import com.sun.corba.se.spi.orbutil.tf.annotation.TraceEnhanceLevel;
import org.objectweb.asm.MethodAdapter;
import org.objectweb.asm.commons.AnalyzerAdapter;
import org.objectweb.asm.commons.LocalVariablesSorter;

public class ClassTracer extends TFEnhanceAdapter {
    private void info( final int level, final String msg ) {
        util.info( level, "ClassTracer: " + msg ) ;
    }

    // Sequence to replace: ACONST_NULL, ICONST_0, INVOKESPECIAL to an
    // InfoMethod.

    public enum Input { ACONST_NULL_BC, ICONST_0_BC, INFO_METHOD_CALL, OTHER }

    public enum State {
        NULL1() {
            public State transition( final Util util, final MethodVisitor mv,
                final Input input ) {

                util.info( 3, "ClassTracer: "
                    + "State transition: NULL1 state, Input " + input ) ;
                switch (input) {
                    case ICONST_0_BC :
                        return State.NULL2 ;

                    case ACONST_NULL_BC :
                    case INFO_METHOD_CALL :
                    case OTHER :
                        util.info( 4, "ClassTracer: Emitting 1 ACONST_NULL" ) ;
                        mv.visitInsn( Opcodes.ACONST_NULL ) ;
                        return State.NORMAL ;
                }
                return null ;
            }
        },

        NULL2() {
            public State transition( final Util util, final MethodVisitor mv,
                final Input input ) {

                util.info( 3, "ClassTracer: "
                    + "State transition: NULL2 state, Input " + input ) ;
                switch (input) {
                    case ICONST_0_BC :
                    case ACONST_NULL_BC :
                    case OTHER :
                        util.info( 4, 
                            "ClassTracer: Emitting ACONST_NULL,ICONST_0" ) ;
                        mv.visitInsn( Opcodes.ACONST_NULL ) ;
                        mv.visitInsn( Opcodes.ICONST_0 ) ;

                    case INFO_METHOD_CALL :
                        return State.NORMAL ;
                }
                return null ;
            }
        },

        NORMAL() {
            public State transition( final Util util, final MethodVisitor mv,
                final Input input ) {

                util.info( 3, "ClassTracer: "
                    + "State transition: NORMAL state, Input " + input ) ;
                switch (input) {
                    case ACONST_NULL_BC :
                        return State.NULL1 ;

                    case INFO_METHOD_CALL :
                    case ICONST_0_BC :
                    case OTHER :
                        return State.NORMAL ;
                }
                return null ;
            }
        } ;

        public abstract State transition( Util util, MethodVisitor mv,
            Input input ) ;
    }

    private final Util util ;
    private final EnhancedClassData ecd ;

    private State current = State.NORMAL ;

    public ClassTracer( final Util util, final EnhancedClassData ecd,
        final ClassVisitor cv ) {

        super( cv, TraceEnhanceLevel.PHASE1, TraceEnhanceLevel.PHASE2, ecd ) ;
        this.util = util ;
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
    private class MonitoredMethodEnhancer extends MethodAdapter {
        private final int access ;
        private final String name ;
        private final String desc ;
        private final MethodVisitor lmv ;
        private final int identVal ;

        private final Set<Integer> returnOpcodes = new HashSet<Integer>() ;

        private final Label start = new Label() ;
        private final LabelNode startNode = new LabelNode( start ) ;

        private final Label excHandler = new Label() ;
        private final LabelNode excHandlerNode = new LabelNode( excHandler ) ;

        private final Label end = new Label() ;
        private final LabelNode endNode = new LabelNode( end ) ;

        private final Label afterExcStore = new Label() ;
        private final LabelNode afterExcStoreNode = new LabelNode( end ) ;

        // Values must be set in setLocalVariablesSorter.
        private LocalVariablesSorter lvs = null ;
        private LocalVariableNode __result = null ;
        private LocalVariableNode __mm = null ;
        private LocalVariableNode __exc = null ;

        public void setLocalVariablesSorter( final LocalVariablesSorter lvs ) {
            this.lvs = lvs ;

            Type type = Type.getReturnType( desc ) ;

            if (!type.equals( Type.VOID_TYPE)) {
                __result = new LocalVariableNode( "__$result$__",
                    type.getDescriptor(),
                    null, startNode, endNode, lvs.newLocal(type) ) ;
            } else {
                __result = null ;
            }

            type = Type.getType( MethodMonitor.class );
            __mm = new LocalVariableNode( "__$mm$__",
                type.getDescriptor(),
                null, startNode, endNode, lvs.newLocal(type) ) ;

            type = Type.getType( Throwable.class ) ;
            __exc = new LocalVariableNode( "__$exc$__",
                type.getDescriptor(), 
                null, excHandlerNode, endNode, lvs.newLocal(type) ) ;
        }

        public MonitoredMethodEnhancer( final int access, final String name,
            final String desc, final MethodVisitor mv ) {
            super( mv ) ;
            this.access = access ;
            this.name = name ;
            this.desc = desc ;
            this.lmv = mv ;
            this.identVal = ecd.getMethodIndex(name) ;

            returnOpcodes.add( Opcodes.RETURN ) ;
            returnOpcodes.add( Opcodes.IRETURN ) ;
            returnOpcodes.add( Opcodes.ARETURN ) ;
            returnOpcodes.add( Opcodes.LRETURN ) ;
            returnOpcodes.add( Opcodes.FRETURN ) ;
            returnOpcodes.add( Opcodes.DRETURN ) ;
        }

        /*
        private Object getTypeForStackMap( Type type ) {
            switch (type.getSort()) {
                case Type.VOID :
                    return null ;
                case Type.BOOLEAN :
                case Type.CHAR :
                case Type.BYTE :
                case Type.SHORT :
                case Type.INT :
                    return Opcodes.INTEGER ;
                case Type.LONG :
                    return Opcodes.LONG ;
                case Type.FLOAT :
                    return Opcodes.FLOAT ;
                case Type.DOUBLE :
                    return Opcodes.DOUBLE ;
                case Type.ARRAY :
                case Type.OBJECT :
                    return type.getInternalName() ;
            }

            return null ;
        }
         */

        @Override
        public void visitCode() {
            info( 1, "visitCode" ) ;

            // visit try-catch block BEFORE visiting start label!
            lmv.visitTryCatchBlock( start, end, excHandler, null );
            lmv.visitTryCatchBlock( excHandler, afterExcStore, excHandler, null );

/*            final Object rt = getTypeForStackMap( Type.getReturnType( desc ) )  ;
            final Object[] locals = (rt == null)
                ? new Object[] { ecd.getClassName(),
                    EnhancedClassData.OBJECT_NAME, EnhancedClassData.MM_NAME }
                : new Object[] { ecd.getClassName(), rt,
                    EnhancedClassData.OBJECT_NAME, EnhancedClassData.MM_NAME } ;

            Object[] stack = new Type[] { } ;
            lmv.visitFrame(Opcodes.F_NEW, locals.length, locals, 
                stack.length, stack) ;
*/
            // __result = null or 0 (type specific, omitted if void return)
            if (__result != null) {
                util.initLocal( lmv, __result ) ;
            }

            // final MethodMonitor __mm = __mmXX.content() ;
            // (for the appropriate XX for this method)
            final String fullDesc = util.getFullMethodDescriptor(name,desc) ;
            info( 2, "fullDesc = " + fullDesc ) ;

            final String fname = ecd.getHolderName( fullDesc );

            lmv.visitFieldInsn( Opcodes.GETSTATIC, ecd.getClassName(),
                fname, Type.getDescriptor( SynchronizedHolder.class ));
            lmv.visitMethodInsn( Opcodes.INVOKEVIRTUAL,
                EnhancedClassData.SH_NAME, "content",
                "()Ljava/lang/Object;" );
            lmv.visitTypeInsn( Opcodes.CHECKCAST,
                EnhancedClassData.MM_NAME );
            lmv.visitVarInsn( Opcodes.ASTORE, __mm.index );

            // if (__mm != null) {
            lmv.visitVarInsn( Opcodes.ALOAD, __mm.index ) ;
            lmv.visitJumpInsn( Opcodes.IFNULL, start );

            // __mm.enter( __ident, <array of wrapped args> ) ;
            lmv.visitVarInsn( Opcodes.ALOAD, __mm.index )  ;
            util.emitIntConstant( lmv, identVal ) ;

            util.wrapArgs( lmv, access, desc ) ;

            lmv.visitMethodInsn( Opcodes.INVOKEINTERFACE,
                EnhancedClassData.MM_NAME, "enter",
                "(I[Ljava/lang/Object;)V" ) ;

            // }
            lmv.visitLabel( start ) ;
        }

        private void emitExceptionReport( final int excIndex ) {
            info( 1, "emitExceptionReport called" ) ;
            final Label skipLabel = new Label() ;
            lmv.visitVarInsn( Opcodes.ALOAD, __mm.index ) ;
            lmv.visitJumpInsn( Opcodes.IFNULL, skipLabel ) ;

            lmv.visitVarInsn( Opcodes.ALOAD, __mm.index ) ;
            util.emitIntConstant( lmv, identVal ) ;
            lmv.visitVarInsn( Opcodes.ALOAD, excIndex ) ;

            lmv.visitMethodInsn( Opcodes.INVOKEINTERFACE,
                EnhancedClassData.MM_NAME, "exception",
                "(ILjava/lang/Throwable;)V" ) ;

            lmv.visitLabel( skipLabel ) ;
        }

        private void emitFinally() {
            info( 1, "emitFinally called" ) ;
            final Label skipLabel = new Label() ;
            lmv.visitVarInsn( Opcodes.ALOAD, __mm.index ) ;
            lmv.visitJumpInsn( Opcodes.IFNULL, skipLabel ) ;

            lmv.visitVarInsn( Opcodes.ALOAD, __mm.index ) ;
            util.emitIntConstant(lmv, identVal ) ;

            final Type rtype = Type.getReturnType( desc ) ;
            if (rtype.equals( Type.VOID_TYPE )) {
                lmv.visitMethodInsn( Opcodes.INVOKEINTERFACE,
                    EnhancedClassData.MM_NAME, "exit",
                    "(I)V" ) ;
            } else {
                util.wrapArg( lmv, __result.index,
                    Type.getType( __result.desc ) ) ;

                lmv.visitMethodInsn( Opcodes.INVOKEINTERFACE,
                    EnhancedClassData.MM_NAME, "exit",
                    "(ILjava/lang/Object;)V" ) ;
            }

            lmv.visitLabel( skipLabel ) ;
        }

        @Override
        public void visitInsn(final int opcode) {
            info( 1, "visitInsn[" + Util.opcodeToString(opcode) + "] called") ;
            if (opcode == Opcodes.ACONST_NULL) {
                current = current.transition( util, mv, Input.ACONST_NULL_BC ) ;

                if (current == State.NORMAL) {
                    lmv.visitInsn(opcode);
                }
            } else if (opcode == Opcodes.ICONST_0) {
                current = current.transition( util, mv, Input.ICONST_0_BC ) ;

                if (current == State.NORMAL) {
                    lmv.visitInsn(opcode);
                }
            } else {
                current = current.transition( util, mv, Input.OTHER ) ;

                if (opcode == Opcodes.ATHROW) {
                    info( 2, "handling throw" ) ;
                    final int exc = lvs.newLocal(
                        Type.getType(Throwable.class)) ;

                    lmv.visitVarInsn( Opcodes.ASTORE, exc) ;

                    emitExceptionReport( exc ) ;

                    // restore exception from local for following ATHROW
                    // (this will be caught in the finally exception handler,
                    // which will handle calling MethodMonitor.exit).
                    lmv.visitVarInsn( Opcodes.ALOAD, exc ) ;
                } else if (returnOpcodes.contains(opcode)) {
                    info( 2, "handling return" ) ;
                    util.storeFromXReturn( lmv, opcode, __result ) ;

                    emitFinally() ;

                    util.loadFromXReturn( lmv, opcode, __result ) ;
                } 

                lmv.visitInsn(opcode);
            }
        }

        @Override
        public void visitMethodInsn( final int opcode, final String owner,
            final String name, final String desc ) {
            info( 1, "MM method: visitMethodInsn[" 
                + Util.opcodeToString(opcode) + "]: " + owner
                + "." + name + desc ) ;

            // If opcode is INVOKESPECIAL, owner is this class, and name/desc
            // are in the infoMethodDescs set, update the desc for the call
            // and add the extra parameters to the end of the call.
            final String fullDesc = util.getFullMethodDescriptor( name, desc ) ;
            if ((opcode == Opcodes.INVOKESPECIAL)
                && (owner.equals( ecd.getClassName() )
                && (ecd.classifyMethod(fullDesc)
                    == EnhancedClassData.MethodType.INFO_METHOD))) {

                info( 2, "rewriting method call" ) ;
                current = current.transition( util, lmv,
                    Input.INFO_METHOD_CALL ) ;

                lmv.visitVarInsn( Opcodes.ALOAD, __mm.index ) ;
                util.emitIntConstant(lmv, identVal );

                lmv.visitMethodInsn(opcode, owner, name, desc );
            } else {
                current = current.transition( util, lmv, Input.OTHER ) ;

                lmv.visitMethodInsn(opcode, owner, name, desc );
            }
        }

        @Override
        public void visitMaxs( final int maxStack, final int maxLocals ) {
            info( 1, "MM method: visitMaxs" ) ;
            lmv.visitLabel( end  ) ;
            lmv.visitLabel( excHandler  ) ;

            // Store the exception
            lmv.visitVarInsn( Opcodes.ASTORE, __exc.index ) ;

            lmv.visitLabel( afterExcStore ) ;

            emitFinally() ;

            // throw the exception
            lmv.visitVarInsn( Opcodes.ALOAD, __exc.index ) ;
            lmv.visitInsn( Opcodes.ATHROW ) ;

            // visit local variables AFTER visiting end!
            __result.accept( lmv ) ;
            __mm.accept( lmv ) ;
            __exc.accept( lmv ) ;

            lmv.visitMaxs( maxStack, maxLocals ) ;
        }

        @Override
        public void visitIntInsn(final int opcode, final int operand) {
            info( 1, "visitIntInsn[" + Util.opcodeToString(opcode)
                + "] operand=" + operand ) ;
            current = current.transition( util, mv, Input.OTHER ) ;
            lmv.visitIntInsn(opcode, operand);
        }

        @Override
        public void visitVarInsn(final int opcode, final int var) {
            info( 1, "visitVarInsn[" + Util.opcodeToString(opcode)
                + "] var=" + var ) ;
            current = current.transition( util, mv, Input.OTHER ) ;
            lmv.visitVarInsn(opcode, var);
        }

        @Override
        public void visitTypeInsn(final int opcode, final String type) {
            info( 1, "visitTypeInsn[" + Util.opcodeToString(opcode)
                + "] type=" + type ) ;
            current = current.transition( util, mv, Input.OTHER ) ;
            lmv.visitTypeInsn(opcode, type);
        }

        @Override
        public void visitFieldInsn(
            final int opcode,
            final String owner,
            final String name,
            final String desc)
        {
            info( 1, "visitFieldInsn[" + Util.opcodeToString(opcode)
                + "] " + owner + "." + name + desc ) ;
            current = current.transition( util, mv, Input.OTHER ) ;
            lmv.visitFieldInsn(opcode, owner, name, desc);
        }

        @Override
        public void visitJumpInsn(final int opcode, final Label label) {
            info( 1, "visitTypeInsn[" + Util.opcodeToString(opcode)
                + "] label=" + label ) ;
            current = current.transition( util,  mv, Input.OTHER ) ;
            lmv.visitJumpInsn(opcode, label);
        }

        @Override
        public void visitLdcInsn(final Object cst) {
            info( 1, "visitLdcInsn " + cst ) ;
            current = current.transition( util,  mv, Input.OTHER ) ;
            lmv.visitLdcInsn(cst);
        }

        @Override
        public void visitIincInsn(final int var, final int increment) {
            info( 1, "visitIincInsn " + " var=" + var
                + " increment=" + increment ) ;
            current = current.transition( util, mv, Input.OTHER ) ;
            lmv.visitIincInsn(var, increment);
        }

        @Override
        public void visitTableSwitchInsn(
            final int min,
            final int max,
            final Label dflt,
            final Label[] labels)
        {
            current = current.transition( util, mv, Input.OTHER ) ;
            lmv.visitTableSwitchInsn(min, max, dflt, labels);
        }

        @Override
        public void visitLookupSwitchInsn(
            final Label dflt,
            final int[] keys,
            final Label[] labels)
        {
            current = current.transition( util, mv, Input.OTHER ) ;
            lmv.visitLookupSwitchInsn(dflt, keys, labels);
        }

        @Override
        public void visitMultiANewArrayInsn(final String desc, final int dims) {
            current = current.transition( util, mv, Input.OTHER ) ;
            lmv.visitMultiANewArrayInsn(desc, dims);
        }
    } // end of MonitoredMethodEnhancer

    @Override
    public MethodVisitor visitMethod( final int access, final String name,
        final String desc, final String sig, final String[] exceptions ) {
        info( 1, "visitMethod: " + name + desc ) ;
        // Enhance the class first (part 1).
        // - Modify all of the @InfoMethod methods with extra arguments
        // - Modify all calls to @InfoMethod methods to add the extra arguments
        //   or to flag an error if NOT called from an MM method.

        final String fullDesc = util.getFullMethodDescriptor(name, desc) ;
        final EnhancedClassData.MethodType mtype =
            ecd.classifyMethod(fullDesc) ;

        MethodVisitor mv = super.visitMethod( access, name, desc,
            sig, exceptions ) ;
        if (util.getDebug()) {
            mv = new SimpleMethodTracer(mv, util) ;
        }

        switch (mtype) {
            case STATIC_INITIALIZER :
            case INFO_METHOD :
            case NORMAL_METHOD :
                return mv ;

            case MONITORED_METHOD :
                final MonitoredMethodEnhancer mme = new MonitoredMethodEnhancer(
                    access, name, desc, mv ) ;
                // AnalyzerAdapter aa = new AnalyzerAdapter( ecd.getClassName(),
                    // access, name, desc, mme ) ;
                final LocalVariablesSorter lvs = new LocalVariablesSorter( access,
                    desc, mme ) ;
                mme.setLocalVariablesSorter(lvs);

                return lvs ;
        }

        return null ;
    }
} // end of ClassTracer
