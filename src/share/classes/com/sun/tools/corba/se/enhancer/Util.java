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

import com.sun.corba.se.spi.orbutil.generic.UnaryFunction;
import com.sun.corba.se.spi.orbutil.tf.MethodMonitor;
import java.io.PrintWriter;
import org.objectweb.asm.ClassAdapter;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.Type;
import org.objectweb.asm.tree.LocalVariableNode;
import org.objectweb.asm.tree.MethodInsnNode;
import org.objectweb.asm.tree.MethodNode;
import org.objectweb.asm.util.AbstractVisitor;
import org.objectweb.asm.util.CheckClassAdapter;
import org.objectweb.asm.util.TraceClassVisitor;

/** Some useful utilities for generating code using ASM.  Nothing in here
 * should be specific to the classfile enhancer for tracing.
 *
 * @author ken
 */
public class Util {
    private boolean debug = false ;
    private int verbose = 0 ;

    public boolean getDebug() {
        return debug ;
    }

    public void setDebug(boolean flag) {
        debug = flag ;
    }

    public void setVerbose(int level) {
        verbose = level ;
    }

    public void info( String str ) {
        info( 1, str ) ; 
    }

    public void info( int level, String str ) {
        if (verbose >= level) {
            final String format = level>1 ? "%" + (4*(level-1) + 1) + "s"
                                          : "%s" ;
            final String pad = String.format( format, ">" ) ;
            msg( pad + str ) ;
        }
    }

    public void msg( String str ) {
        System.out.println( str ) ;
    }

    public void error( String str ) {
        throw new RuntimeException( str ) ;
    }

    public void initLocal( MethodVisitor mv, LocalVariableNode var ) {
        info( 2, "Initializing variable " + var ) ;
        Type type = Type.getType( var.desc ) ;
        switch (type.getSort()) {
            case Type.BOOLEAN :
            case Type.CHAR :
            case Type.SHORT :
            case Type.INT :
                mv.visitInsn( Opcodes.ICONST_0 ) ;
                mv.visitVarInsn( Opcodes.ISTORE, var.index ) ;
                break ;

            case Type.LONG :
                mv.visitInsn( Opcodes.LCONST_0 ) ;
                mv.visitVarInsn( Opcodes.LSTORE, var.index ) ;
                break ;

            case Type.FLOAT :
                mv.visitInsn( Opcodes.FCONST_0 ) ;
                mv.visitVarInsn( Opcodes.FSTORE, var.index ) ;
                break ;

            case Type.DOUBLE :
                mv.visitInsn( Opcodes.DCONST_0 ) ;
                mv.visitVarInsn( Opcodes.DSTORE, var.index ) ;
                break ;

            default :
                mv.visitInsn( Opcodes.ACONST_NULL ) ;
                mv.visitVarInsn( Opcodes.ASTORE, var.index ) ;
        }
    }

    public String getFullMethodDescriptor( String name, String desc ) {
        return name + desc ;
    }

    public String getFullMethodDescriptor( MethodNode mn ) {
        return mn.name + mn.desc ;
    }

    public String getFullMethodDescriptor( MethodInsnNode mn ) {
        return mn.name + mn.desc ;
    }

    public void newWithSimpleConstructor( MethodVisitor mv, Class cls ) {
        info( 2, "generating new for class " + cls ) ;
        Type type = Type.getType( cls ) ;
        mv.visitTypeInsn( Opcodes.NEW, type.getInternalName() );
        mv.visitInsn( Opcodes.DUP ) ;
        mv.visitMethodInsn( Opcodes.INVOKESPECIAL,
            type.getInternalName(), "<init>", "()V" );
    }

    String augmentInfoMethodDescriptor( String desc ) {
        info( 2, "Augmenting infoMethod descriptor " + desc ) ;
        // Compute new descriptor
        Type[] oldArgTypes = Type.getArgumentTypes( desc ) ;
        Type retType = Type.getReturnType( desc ) ;

        int oldlen = oldArgTypes.length ;
        Type[] argTypes = new Type[ oldlen + 2 ] ;
        for (int ctr=0; ctr<oldlen; ctr++) {
            argTypes[ctr] = oldArgTypes[ctr] ;
        }

        argTypes[oldlen] = Type.getType( MethodMonitor.class ) ;
        argTypes[oldlen+1] = Type.getType( Object.class ) ;

        String newDesc = Type.getMethodDescriptor(retType, argTypes) ;
        info( 3, "result is " + newDesc ) ;
        return newDesc ;
    }

    public void emitIntConstant( MethodVisitor mv, int val ) {
        info( 2, "Emitting constant " + val ) ;
        if (val <= 5) {
            switch (val) {
                case 0:
                    mv.visitInsn( Opcodes.ICONST_0 ) ;
                    break ;
                case 1:
                    mv.visitInsn( Opcodes.ICONST_1 ) ;
                    break ;
                case 2:
                    mv.visitInsn( Opcodes.ICONST_2 ) ;
                    break ;
                case 3:
                    mv.visitInsn( Opcodes.ICONST_3 ) ;
                    break ;
                case 4:
                    mv.visitInsn( Opcodes.ICONST_4 ) ;
                    break ;
                case 5:
                    mv.visitInsn( Opcodes.ICONST_5 ) ;
                    break ;
            }
        } else {
            mv.visitLdcInsn( val );
        }
    }

    // Wrap the argument at index argIndex of type atype into
    // an Object as needed.  Returns the index of the next
    // argument.
    public int wrapArg( MethodVisitor mv, int argIndex, Type atype ) {
        info( 2, "Emitting code to wrap argument at " + argIndex
            + " of type " + atype ) ;

        switch (atype.getSort() ) {
            case Type.BOOLEAN :
                mv.visitVarInsn( Opcodes.ILOAD,  argIndex ) ;
                mv.visitMethodInsn( Opcodes.INVOKESTATIC,
                    Type.getInternalName( Boolean.class ), "valueOf",
                    "(Z)Ljava/lang/Boolean;" );
                break ;
            case Type.BYTE :
                mv.visitVarInsn( Opcodes.ILOAD,  argIndex ) ;
                mv.visitMethodInsn( Opcodes.INVOKESTATIC,
                    Type.getInternalName( Byte.class ), "valueOf",
                    "(B)Ljava/lang/Byte;" );
                break ;
            case Type.CHAR :
                mv.visitVarInsn( Opcodes.ILOAD,  argIndex ) ;
                mv.visitMethodInsn( Opcodes.INVOKESTATIC,
                    Type.getInternalName( Character.class ), "valueOf",
                    "(C)Ljava/lang/Character;" );
                break ;
            case Type.SHORT :
                mv.visitVarInsn( Opcodes.ILOAD,  argIndex ) ;
                mv.visitMethodInsn( Opcodes.INVOKESTATIC,
                    Type.getInternalName( Short.class ), "valueOf",
                    "(S)Ljava/lang/Short;" );
                break ;
            case Type.INT :
                mv.visitVarInsn( Opcodes.ILOAD,  argIndex ) ;
                mv.visitMethodInsn( Opcodes.INVOKESTATIC,
                    Type.getInternalName( Integer.class ), "valueOf",
                    "(I)Ljava/lang/Integer;" );
                break ;
            case Type.LONG :
                mv.visitVarInsn( Opcodes.LLOAD,  argIndex ) ;
                mv.visitMethodInsn( Opcodes.INVOKESTATIC,
                    Type.getInternalName( Long.class ), "valueOf",
                    "(J)Ljava/lang/Long;" );
                break ;
            case Type.DOUBLE :
                mv.visitVarInsn( Opcodes.DLOAD,  argIndex ) ;
                mv.visitMethodInsn( Opcodes.INVOKESTATIC,
                    Type.getInternalName( Double.class ), "valueOf",
                    "(D)Ljava/lang/Double;" );
                break ;
            case Type.FLOAT :
                mv.visitVarInsn( Opcodes.FLOAD,  argIndex ) ;
                mv.visitMethodInsn( Opcodes.INVOKESTATIC,
                    Type.getInternalName( Float.class ), "valueOf",
                    "(F)Ljava/lang/Float;" );
                break ;
            default :
                mv.visitVarInsn( Opcodes.ALOAD,  argIndex ) ;
                break ;
        }

        return argIndex += atype.getSize() ;
    }

    // Emit code to wrap all of the argumnts as Object[],
    // which is left on the stack
    void wrapArgs( MethodVisitor mv, int access, String desc ) {
        info( 2, "Wrapping args for descriptor " + desc ) ;

        Type[] atypes = Type.getArgumentTypes( desc ) ;
        emitIntConstant( mv, atypes.length ) ;
        mv.visitTypeInsn( Opcodes.ANEWARRAY, "java/lang/Object" ) ;

        int argIndex ;
        if ((access & Opcodes.ACC_STATIC) == Opcodes.ACC_STATIC) {
            argIndex = 0 ;
        } else {
            argIndex = 1 ;
        }

        for (int ctr=0; ctr<atypes.length; ctr++) {
            mv.visitInsn( Opcodes.DUP ) ;
            emitIntConstant( mv, ctr );
            argIndex = wrapArg( mv, argIndex, atypes[ctr] ) ;
            mv.visitInsn( Opcodes.AASTORE ) ;
        }
    }

    public void storeFromXReturn( MethodVisitor mv, int returnOpcode,
        LocalVariableNode holder ) {

        switch (returnOpcode) {
            case Opcodes.RETURN :
                // NOP
                break ;
            case Opcodes.ARETURN :
                mv.visitVarInsn( Opcodes.ASTORE, holder.index ) ;
                break ;
            case Opcodes.IRETURN :
                mv.visitVarInsn( Opcodes.ISTORE, holder.index ) ;
                break ;
            case Opcodes.LRETURN :
                mv.visitVarInsn( Opcodes.LSTORE, holder.index ) ;
                break ;
            case Opcodes.FRETURN :
                mv.visitVarInsn( Opcodes.FSTORE, holder.index ) ;
                break ;
            case Opcodes.DRETURN :
                mv.visitVarInsn( Opcodes.DSTORE, holder.index ) ;
                break ;
        }
    }

    public void loadFromXReturn( MethodVisitor mv, int returnOpcode,
        LocalVariableNode holder ) {

        switch (returnOpcode) {
            case Opcodes.RETURN :
                // NOP
                break ;
            case Opcodes.ARETURN :
                mv.visitVarInsn( Opcodes.ALOAD, holder.index ) ;
                break ;
            case Opcodes.IRETURN :
                mv.visitVarInsn( Opcodes.ILOAD, holder.index ) ;
                break ;
            case Opcodes.LRETURN :
                mv.visitVarInsn( Opcodes.LLOAD, holder.index ) ;
                break ;
            case Opcodes.FRETURN :
                mv.visitVarInsn( Opcodes.FLOAD, holder.index ) ;
                break ;
            case Opcodes.DRETURN :
                mv.visitVarInsn( Opcodes.DLOAD, holder.index ) ;
                break ;
        }
    }

    private void verify( byte[] cls ) {
        if (getDebug()) {
            info( "Verifying enhanced class") ;
            ClassReader cr = new ClassReader( cls ) ;
            PrintWriter pw = new PrintWriter( System.out ) ;
            CheckClassAdapter.verify( cr, true, pw ) ;
        }
    }

    public boolean hasAccess( int access, int flag ) {
        return (access & flag) == flag ;
    }

    public static String opcodeToString( int opcode ) {
        String[] opcodes = AbstractVisitor.OPCODES ;
        if ((opcode < 0) || (opcode > opcodes.length)) {
            return "ILLEGAL[" + opcode + "]" ;
        } else {
            return opcodes[opcode] ;
        }
    }

    public byte[] transform( final boolean debug, final byte[] cls,
        final UnaryFunction<ClassVisitor,ClassAdapter> factory ) {

        final ClassReader cr = new ClassReader(cls) ;
        final ClassWriter cw = new ClassWriter(
            ClassWriter.COMPUTE_MAXS ) ;
            // ClassWriter.COMPUTE_FRAMES + ClassWriter.COMPUTE_MAXS ) ;

        PrintWriter pw = null ;
        // TraceClassVisitor tcv = null ;
        ClassVisitor cv = cw ;

        if (debug) {
            pw = new PrintWriter( System.out ) ;
            // tcv = new TraceClassVisitor( cw, new PrintWriter( System.out ) ) ;
            // cv = tcv ;
        }

        ClassAdapter xform = factory.evaluate( cv ) ;

        try {
            cr.accept( xform, ClassReader.SKIP_FRAMES ) ;
        } catch (Exception exc) {
            System.out.println( "Exception: " + exc ) ;
            exc.printStackTrace() ;
        } finally {
            if (pw != null) {
                pw.flush() ;
                pw.close() ;
            }
        }

        byte[] enhancedClass = cw.toByteArray() ;

        verify( enhancedClass ) ;

        return enhancedClass ;
    }
}
