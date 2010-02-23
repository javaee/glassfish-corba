/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
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
                && (owner.equals( ecd.getClassName() )
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
} // end of ClassEnhancer
