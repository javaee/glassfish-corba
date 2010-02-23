/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package com.sun.tools.corba.se.enhancer;

import java.util.HashSet;
import java.util.Set;
import org.objectweb.asm.ClassAdapter;
import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.Label;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.Type;
import org.objectweb.asm.commons.AdviceAdapter;
import org.objectweb.asm.tree.LabelNode;
import org.objectweb.asm.tree.LocalVariableNode;

import com.sun.corba.se.spi.orbutil.tf.MethodMonitor ;

public class ClassTracer extends ClassAdapter {

    public enum Input {
        ACONST_NULL_BC,
        INFO_METHOD_CALL,
        OTHER
    }

    public enum Action {
        EMIT_NULL,
        EMIT_2_NULL,
        EMIT_CALL,
        EMIT_NORMAL
    }

    public enum State {

        NULL1() {
            public State transition( MethodVisitor mv, Input input ) {
                switch (input) {
                    case ACONST_NULL_BC :
                        return State.NULL2 ;
                    case INFO_METHOD_CALL :
                        return State.NORMAL ;
                    case OTHER :
                        mv.visitInsn( Opcodes.ACONST_NULL ) ;
                        return State.NORMAL ;
                }
                return null ;
            }
        },

        NULL2() {
            public State transition( MethodVisitor mv, Input input ) {
                switch (input) {
                    case ACONST_NULL_BC :
                    case OTHER :
                        mv.visitInsn( Opcodes.ACONST_NULL ) ;
                        mv.visitInsn( Opcodes.ACONST_NULL ) ;
                    case INFO_METHOD_CALL :
                        return State.NORMAL ;
                }
                return null ;
            }
        },

        NORMAL() {
            public State transition( MethodVisitor mv, Input input ) {
                switch (input) {
                    case ACONST_NULL_BC :
                        return State.NULL1 ;
                    case INFO_METHOD_CALL :
                    case OTHER :
                        return State.NORMAL ;
                }
                return null ;
            }
        } ;


        public abstract State transition( MethodVisitor mv, Input input ) ;
    }

    public enum Input {
        ACONST_NULL_BC,
        INFO_METHOD_CALL,
        OTHER
    }

    public enum Action {
        EMIT_NULL,
        EMIT_2_NULL,
        EMIT_CALL,
        EMIT_NORMAL
    }

    public enum State {

        NULL1() {
            public State transition( MethodVisitor mv, Input input ) {
                switch (input) {
                    case ACONST_NULL_BC :
                        return State.NULL2 ;
                    case INFO_METHOD_CALL :
                        return State.NORMAL ;
                    case OTHER :
                        mv.visitInsn( Opcodes.ACONST_NULL ) ;
                        return State.NORMAL ;
                }
                return null ;
            }
        },

        NULL2() {
            public State transition( MethodVisitor mv, Input input ) {
                switch (input) {
                    case ACONST_NULL_BC :
                    case OTHER :
                        mv.visitInsn( Opcodes.ACONST_NULL ) ;
                        mv.visitInsn( Opcodes.ACONST_NULL ) ;
                    case INFO_METHOD_CALL :
                        return State.NORMAL ;
                }
                return null ;
            }
        },

        NORMAL() {
            public State transition( MethodVisitor mv, Input input ) {
                switch (input) {
                    case ACONST_NULL_BC :
                        return State.NULL1 ;
                    case INFO_METHOD_CALL :
                    case OTHER :
                        return State.NORMAL ;
                }
                return null ;
            }
        } ;


        public abstract State transition( MethodVisitor mv, Input input ) ;
    }

    private final Util util ;
    private final EnhancedClassData ecd ;
    private State current = State.NORMAL ;


    public ClassTracer( final Util util, final EnhancedClassData ecd,
        ClassVisitor cv ) {

        super( cv ) ;
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

            type = Type.getType( MethodMonitor.class );
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

            lmv.visitFieldInsn( Opcodes.GETSTATIC, ecd.getClassName(),
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
        public void visitInsn(final int opcode) {
            if (opcode == Opcodes.ACONST_NULL) {
                current = current.transition( mv, Input.ACONST_NULL_BC ) ;
                if (current == State.NORMAL) {
                    mv.visitInsn(opcode);
                }
            } else {
                mv.visitInsn(opcode);
            }
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
                && (owner.equals( ecd.getClassName() )
                && (ecd.classifyMethod(fullDesc)
                    == EnhancedClassData.MethodType.INFO_METHOD))) {

                util.info( "    rewriting method call" ) ;
                current = current.transition( mv, Input.INFO_METHOD_CALL ) ;

                mv.visitVarInsn( Opcodes.ALOAD, __mm.index ) ;
                mv.visitVarInsn( Opcodes.ALOAD, __ident.index ) ;

                String newDesc = util.augmentInfoMethodDescriptor(desc) ;

                mv.visitMethodInsn(opcode, owner, name, newDesc );
            } else {
                current = current.transition( mv, Input.OTHER ) ;

                mv.visitMethodInsn(opcode, owner, name, desc );
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

        @Override
        public void visitIntInsn(final int opcode, final int operand) {
            current = current.transition( mv, Input.OTHER ) ;
            mv.visitIntInsn(opcode, operand);
        }

        @Override
        public void visitVarInsn(final int opcode, final int var) {
            current = current.transition( mv, Input.OTHER ) ;
            mv.visitVarInsn(opcode, var);
        }

        @Override
        public void visitTypeInsn(final int opcode, final String type) {
            current = current.transition( mv, Input.OTHER ) ;
            mv.visitTypeInsn(opcode, type);
        }

        @Override
        public void visitFieldInsn(
            final int opcode,
            final String owner,
            final String name,
            final String desc)
        {
            current = current.transition( mv, Input.OTHER ) ;
            mv.visitFieldInsn(opcode, owner, name, desc);
        }

        @Override
        public void visitJumpInsn(final int opcode, final Label label) {
            current = current.transition(  mv, Input.OTHER ) ;
            mv.visitJumpInsn(opcode, label);
        }

        @Override
        public void visitLdcInsn(final Object cst) {
            current = current.transition(  mv, Input.OTHER ) ;
            mv.visitLdcInsn(cst);
        }

        @Override
        public void visitIincInsn(final int var, final int increment) {
            current = current.transition( mv, Input.OTHER ) ;
            mv.visitIincInsn(var, increment);
        }

        @Override
        public void visitTableSwitchInsn(
            final int min,
            final int max,
            final Label dflt,
            final Label[] labels)
        {
            current = current.transition( mv, Input.OTHER ) ;
            mv.visitTableSwitchInsn(min, max, dflt, labels);
        }

        @Override
        public void visitLookupSwitchInsn(
            final Label dflt,
            final int[] keys,
            final Label[] labels)
        {
            current = current.transition( mv, Input.OTHER ) ;
            mv.visitLookupSwitchInsn(dflt, keys, labels);
        }

        @Override
        public void visitMultiANewArrayInsn(final String desc, final int dims) {
            current = current.transition( mv, Input.OTHER ) ;
            mv.visitMultiANewArrayInsn(desc, dims);
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
} // end of ClassTracer
