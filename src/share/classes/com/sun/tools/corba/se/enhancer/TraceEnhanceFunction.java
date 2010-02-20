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

import com.sun.corba.se.spi.orbutil.tf.MethodMonitor;
import com.sun.corba.se.spi.orbutil.tf.MethodMonitorRegistry;
import com.sun.corba.se.spi.orbutil.tf.annotation.InfoMethod;

import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.Label;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.Type;
import org.objectweb.asm.commons.AdviceAdapter;
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
    private static  final String SH_NAME = Type.getInternalName(
        SynchronizedHolder.class ) ;
    private static  final String MM_NAME = Type.getInternalName(
        MethodMonitor.class ) ;
    private static  final String INFO_METHOD_NAME = Type.getInternalName(
        InfoMethod.class ) ;

    private boolean debug ;
    private int verbose ;
    private boolean dryrun ;
    private Set<String> annotationNames = new HashSet<String>() ;

    public TraceEnhanceFunction() {
    }

    public void setMMGAnnotations(Set<String> mmgAnnotations) {
        annotationNames = mmgAnnotations ;
    }

    public void setDebug(boolean flag) {
        debug = flag ;
    }

    public void setVerbose(int level) {
        verbose = level ;
    }

    public void setDryrun(boolean flag) {
        dryrun = flag ;
    }

    private void info( String str ) {
        if (verbose > 0) {
            msg( str ) ;
        }
    }

    private void msg( String str ) {
        System.out.println( str ) ;
    }

    private void error( String str ) {
        throw new RuntimeException( str ) ;
    }

    private void initLocal( MethodVisitor mv, LocalVariableNode var ) {
        var.accept( mv ) ;
        Type type = Type.getObjectType( var.desc ) ;
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

    private String getFullMethodDescriptor( String name, String desc ) {
        return name + desc ;        
    }

    private String getFullMethodDescriptor( MethodNode mn ) {
        return mn.name + mn.desc ;
    }

    private String getFullMethodDescriptor( MethodInsnNode mn ) {
        return mn.name + mn.desc ;
    }

    private void newWithSimpleConstructor( MethodVisitor mv, Class cls ) {
        info( "generating new for class " + cls ) ;
        Type type = Type.getType( ArrayList.class ) ;
        mv.visitTypeInsn( Opcodes.NEW, type.getInternalName() );
        mv.visitInsn( Opcodes.DUP ) ;
        mv.visitMethodInsn( Opcodes.INVOKESPECIAL,
            type.getInternalName(), "<init>", "()V" );
    }

    String augmentInfoMethodDescriptor( String desc ) {
        info( "Augmenting infoMethod descriptor " + desc ) ;
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
        info( "    result is " + newDesc ) ;
        return newDesc ;
    }

    private void emitIntConstant( MethodVisitor mv, int val ) {
        info( "Emitting constant " + val ) ;
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

    private void emitMethodConstant( List<String> methodNames,
        MethodVisitor mv, String name ) {

        info( "Emitting int constant for method " + name ) ;

        int index = -1 ;
        for (int ctr=0; ctr<=methodNames.size(); ctr++) {
            if (methodNames.get(ctr).equals(name)) {
                index = ctr ;
                break ;
            }
        }

        if (index == -1) {
            error( "Could not find index for method " + name) ;
        }

        emitIntConstant( mv, index ) ;
    }

    // Wrap the argument at index argIndex of type atype into
    // an Object as needed.  Returns the index of the next
    // argument.
    private int wrapArg( MethodVisitor mv, int argIndex, Type atype ) {
        info( "Emitting code to wrap argument at " + argIndex
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
        info( "Wrapping args for descriptor " + desc ) ;

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

    private void storeFromXReturn( MethodVisitor mv, int returnOpcode,
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

    private void loadFromXReturn( MethodVisitor mv, int returnOpcode,
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

    public byte[] evaluate( final byte[] arg) {
        final ClassNode cn = new ClassNode() ;
        final ClassReader cr = new ClassReader( arg ) ;
        cr.accept( cn, 0 ) ;

        EnhancedClassData ecd = new EnhancedClassData( annotationNames, cn ) ;

        // We need EnhancedClassData to hold the results of scanning the class
        // for various details about annotations.  This makes it easy to write
        // a one-pass visitor in part 2 to actually add the tracing code.
        // Note that the ECD can easily be computed either at build time
        // from the classfile byte[] (using ASM), or at runtime, directly
        // from a Class object using reflection.
        //
        // Enhance the class first (part 1).
        //     This is ClassNode based, since multiple scans are
        //     needed, first to find all of the tracing-related methods,
        //     (which builds the method name list among other things),
        //     then to modify the schema (and code) as needed.
        //     This does NOT use the AdviceAdapter.
        // Run through ASM verifier to check code (debug only?)
        // Extract the list of methodNames for part 2.
        //
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
        // Run through ASM verifier to check code again (debug only?)

        // If the class is updated, return the bytes for the new class.

        Enhancer enhancer = new Enhancer( ecd, cn ) ;
        if (enhancer.enhance()) {
            final ClassWriter cw = new ClassWriter( ClassWriter.COMPUTE_MAXS +
                ClassWriter.COMPUTE_FRAMES ) ;
            cn.accept( cw ) ;

            byte[] enhancedClass = cw.toByteArray() ;

            Tracer tracer = new Tracer( ecd, enhancedClass ) ;
            enhancedClass = tracer.enhance() ;
        } else {
            return null ;
        }
    }

    // Class used to collect TF-relevant data on TF annotated classes.
    // It can be implemented either on an ASM ClassNode, or by reflection
    // on a standard Class.
    private class EnhancedClassData {
        private final Set<String> annoNamesForClass ;

        // Map from MM annotation internal name to
        // SynchronizedHolder<MethodMonitor> field
        // name.  Use something like __$mm$__nnn that is unlikely to collide with
        // another field name that is already in use.
        private final Map<String,String> annoToHolderName =
            new HashMap<String,String>() ;

        // List of simple names of MM methods.  Index of method is identifier in
        // MethodMonitor calls.
        private final List<String> methodNames =
            new ArrayList<String>() ;

        // List of descriptors of @InfoMethod-annotated methods.
        // Needed for validating and transforming calls to such methods.
        private final Set<String> infoMethodDescs =
            new HashSet<String>() ;

        private final Set<String> mmMethodDescs =
            new HashSet<String>() ;

        // Map from method signature to internal name of its MM annotation.
        private final Map<String,String> methodToAnno =
            new HashMap<String,String>() ;

        private final ClassNode currentClass ;

        // Get Set<String> for MM annotations present on class
        private void processClassAnnotations() {
            final List<AnnotationNode> classAnnotations = currentClass.visibleAnnotations ;
            if (classAnnotations != null) {
                for (AnnotationNode an : classAnnotations) {
                    final String aname = Type.getType( an.desc ).getInternalName() ;
                    if (annotationNames.contains( aname )) {
                        annoNamesForClass.add( aname ) ;
                        annoToHolderName.put( aname,
                            "__$mm$__" + annoNamesForClass.size() ) ;
                    }
                }

                if (debug) {
                    msg( "Enhancing class " + currentClass.name ) ;
                    msg( "\tannoNamesForClass = " + annoNamesForClass ) ;
                    msg( "\tannoToHolderName = " + annoToHolderName ) ;
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
                final String mdesc = getFullMethodDescriptor( mn ) ;

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
                                error( "Method " + mdesc
                                    + " for Class " + currentClass.name
                                    + "has multiple MM annotations" ) ;
                            }
                        } else if (annotationNames.contains( aname )) {
                                error( "Method " + mdesc
                                    + " for Class " + currentClass.name
                                    + " has an MM annotation which "
                                    + "is not on its class" ) ;
                        }
                    }

                    if (hasMethodInfoAnno && annoForMethod != null) {
                        error( "Method " + mdesc
                            + " for Class " + currentClass.name
                            + " has both @InfoMethod annotation and"
                            + " a MM annotation" ) ;
                    }

                    // TF Annotations are not permitted on constructors
                    if (mname.equals( "<init>" )) {
                        if (hasMethodInfoAnno) {
                            error( "Constructors must not have an @InfoMethod annotations") ;
                        } else if (annoForMethod != null) {
                            error( "Constructors must not have an MM annotation") ;
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

            if (debug) {
                msg( "\tinfoMethodSignature = " + infoMethodDescs ) ;
                msg( "\tmmMethodSignature = " + mmMethodDescs ) ;
                msg( "\tmethodNames = " + methodNames ) ;
                msg( "\tmethodToAnno = " + methodToAnno ) ;
            }
        }

        public EnhancedClassData( Set<String> mmAnnotations, ClassNode cn ) {
            annoNamesForClass = mmAnnotations ;
            currentClass = cn ;

            // Compute data here: only look at data available to
            // java reflection.
            processClassAnnotations() ;
            scanMethods();
        }

        public EnhancedClassData( Set<String> mmAnnotations, Class<?> cls ) {
            annoNamesForClass = mmAnnotations ;
            currentClass = null ;

            // XXX define me (probably refactor into ABC as usual).
        }

        public Map<String,String> getAnnotationToHolderName() {
            return annoToHolderName ;
        }

        public List<String> getMethodNames() {
            return methodNames ;
        }

        public Set<String> getInfoMethodDescriptors() {
            return infoMethodDescs ;
        }

        public Set<String> getMMMethodDescriptors() {
            return mmMethodDescs ;
        }

        public Map<String,String> getMethodToAnnotation() {
            return methodToAnno ;
        }
    }

    private class Enhancer {
        private ClassNode currentClass ;

        // Set of internal names of MM annotations in use on currentClass.
        private final Set<String> annoNamesForClass ;

        // Map from MM annotation internal name to 
        // SynchronizedHolder<MethodMonitor> field
        // name.  Use something like __$mm$__nnn that is unlikely to collide with
        // another field name that is already in use.
        private final Map<String,String> annoToHolderName ;

        // List of simple names of MM methods.  Index of method is identifier in
        // MethodMonitor calls.
        private final List<String> methodNames ;

        // List of descriptors of @InfoMethod-annotated methods.
        // Needed for validating and transforming calls to such methods.
        private final Set<String> infoMethodDescs ;

        private final Set<String> mmMethodDescs ;

        // Map from method signature to internal name of its MM annotation.
        private final Map<String,String> methodToAnno ;

        public List<String> getMethodNames() {
            return methodNames ;
        }

        public Enhancer( final ClassNode cn ) {
            currentClass = cn ;

            annoNamesForClass = new HashSet<String>() ;
            methodNames = new ArrayList<String>() ;
            annoToHolderName = new HashMap<String,String>() ;
            infoMethodDescs = new HashSet<String>() ;
            mmMethodDescs = new HashSet<String>() ;
            methodToAnno = new HashMap<String,String>() ;
        }

        private boolean hasAccess( int access, int flag ) {
            return (access & flag) == flag ;
        }


        // Add SynchronizedHolder<MethodMonitor> fields to class.
        private void addFieldsToClass() {
            final String desc = Type.getDescriptor(
                SynchronizedHolder.class ) ;

            final int acc = Opcodes.ACC_PRIVATE + Opcodes.ACC_STATIC
                + Opcodes.ACC_FINAL ;

            // Signature is actually L../SynchronizedHolder<L.../MethodMonitor;>
            // where the ... are replaced with appropriate packages.  Not
            // that we actually need a signature here.
            final String sig = null ;

            for (String fname : annoToHolderName.values()) {
                info( "Adding field " + fname + " of type " + desc ) ;
                FieldNode fld = new FieldNode( acc, fname, desc, sig, null ) ;
                currentClass.fields.add( fld ) ;
            }
        }

        private class StaticInitVisitor extends LocalVariablesSorter {
            public StaticInitVisitor( final int access, final String desc,
                final MethodVisitor mv ) {
                super( access, desc, mv ) ;
                info( "StaticInitVisitor created" ) ;
            }

            @Override
            public void visitEnd() {
                info( "StaticInitVisitor.visitEnd" ) ;
                LabelNode start = new LabelNode() ;
                LabelNode end = new LabelNode() ;

                int thisClass = newLocal( Type.getType( Class.class )) ;
                int mnameList = newLocal( Type.getType( List.class )) ;
                int holderMap = newLocal( Type.getType( Map.class )) ;

                // initialize the holders
                for (String str : annoToHolderName.values()) {
                    info( "Generating code to initiale holder " + str ) ;
                    newWithSimpleConstructor( mv, SynchronizedHolder.class );
                    mv.visitFieldInsn( Opcodes.PUTSTATIC,
                        currentClass.name, str, SH_NAME );
                }

                mv.visitLdcInsn( Type.getType( "L" + currentClass.name + ";" ));
                mv.visitVarInsn( Opcodes.ASTORE, thisClass ) ;

                // create list of method names and init
                newWithSimpleConstructor( mv, ArrayList.class ) ;
                mv.visitVarInsn( Opcodes.ASTORE, mnameList ) ;

                for (String str : methodNames) {
                    info( "Generating code to add " + str + " to methodNames" ) ;
                    mv.visitVarInsn( Opcodes.ALOAD, mnameList ) ;
                    mv.visitLdcInsn( str );
                    mv.visitMethodInsn( Opcodes.INVOKEINTERFACE,
                        "java/util/List", "add", "(Ljava/lang/Object;)Z" );
                    mv.visitInsn( Opcodes.POP ) ;
                }

                // create map from MM annotation class to Holder and init
                newWithSimpleConstructor( mv, HashMap.class ) ;
                mv.visitVarInsn( Opcodes.ASTORE, holderMap ) ;

                for (Map.Entry<String,String> entry :
                    annoToHolderName.entrySet()) {

                    info( "Generating code to put " + entry.getKey() + "=>" 
                        + entry.getValue() + " into holderMap" ) ;


                    mv.visitVarInsn( Opcodes.ALOAD, holderMap ) ;

                    Type annoType = Type.getType( "L" + entry.getKey() + ";" ) ;
                    mv.visitLdcInsn( annoType );

                    mv.visitFieldInsn( Opcodes.GETSTATIC, currentClass.name,
                        entry.getValue(), SH_NAME) ;

                    mv.visitMethodInsn( Opcodes.INVOKEINTERFACE,
                        "/java/util/HashMap", "add",
                        "(Ljava/lang/Object;Ljava/lang/Object;)Ljava/lang/Object;" );

                    mv.visitInsn( Opcodes.POP ) ;
                }

                // register with MethodMonitorRegistry
                info( "Generating code call MethodMonitorRegistry.registerClass" ) ;
                mv.visitVarInsn( Opcodes.ALOAD, thisClass ) ;
                mv.visitVarInsn( Opcodes.ALOAD, mnameList ) ;
                mv.visitVarInsn( Opcodes.ALOAD, holderMap ) ;

                Type mmrType = Type.getType( MethodMonitorRegistry.class ) ;
                String mdesc = "(Ljava/lang/Class;Ljava/util/List;Ljava/util/Map;)V" ;
                mv.visitMethodInsn( Opcodes.INVOKESTATIC,
                    mmrType.getInternalName(), "registerClass", mdesc ) ;

                mv.visitInsn( Opcodes.RETURN ) ;
            }
        }

        private void handleStaticInitializer( MethodNode mn ) {
            // Create fields for SynchronizedHolders
            String desc = Type.getInternalName( SynchronizedHolder.class ) ;
            for (String str : annoToHolderName.values()) {
                int acc = Opcodes.ACC_STATIC + Opcodes.ACC_PRIVATE ;
                FieldNode fn = new FieldNode( acc, str, SH_NAME, null, null ) ;
                currentClass.fields.add( fn ) ;
            }

            // If mn is null, create a static initializer for currentClass.
            MethodNode lmn = mn ;
            if (lmn == null) {
                final int access = Opcodes.ACC_STATIC + Opcodes.ACC_PRIVATE ;
                lmn = new MethodNode( access, "<clinit>", "()", null, null )  ;
                lmn.instructions = new InsnList() ;
                currentClass.methods.add( lmn ) ;
            }

            int access = Opcodes.ACC_PRIVATE + Opcodes.ACC_STATIC ;
            MethodVisitor v = new StaticInitVisitor( access, "()", lmn ) ;
            lmn.accept( v ) ;
        }

        // add MethodMonitor and Object parameters to end of params
        // generate body
        private void handleInfoMethod( MethodNode mn ) {
            info( "InfoMethod " + mn.name ) ;
           
            // mn.desc is the old unaugmented descriptor (does not have
            // MethodMonitor and Object at the end)
            String desc = mn.desc ;

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
            mn.visitVarInsn( Opcodes.ALOAD, mmIndex ) ;
            mn.visitJumpInsn( Opcodes.IFNULL, jumpLabel) ;

            mn.visitVarInsn( Opcodes.ALOAD, mmIndex ) ;
            wrapArgs( mn, mn.access, mn.desc ) ;
            mn.visitVarInsn( Opcodes.ALOAD, cidIndex ) ;
            emitMethodConstant( methodNames, mn, mn.name ) ;
            mn.visitMethodInsn( Opcodes.INVOKESTATIC,
                "java/lang/Integer", "valueOf",
                "(I)Ljava/lang/Integer;" );
           
            String newDesc = augmentInfoMethodDescriptor( mn.desc ) ;
            mn.visitMethodInsn( Opcodes.INVOKEINTERFACE, MM_NAME, "info",
                newDesc ) ;

            mn.visitLabel( jumpLabel ) ;
            mn.visitInsn( Opcodes.RETURN ) ;
            mn.desc = newDesc ;
        }

        private class MonitoredMethodEnhancer extends AdviceAdapter {

            final Set<Integer> returnOpcodes = new HashSet<Integer>() ;

            private final Label start = new Label() ;
            private final LabelNode startNode = new LabelNode( start ) ;

            private final Label excHandler = new Label() ;
            private final LabelNode excHandlerNode = new LabelNode( excHandler ) ;

            private final Label end = new Label() ;
            private final LabelNode endNode = new LabelNode( end ) ;
           
            private final MethodNode mn ;
            private final LocalVariableNode __result ;
            private final LocalVariableNode __ident ;
            private final LocalVariableNode __mm ;
            private final LocalVariableNode __enabled ;

            public MonitoredMethodEnhancer( int access, String name,
                String desc, MethodNode mn ) {
                super( mn, access, name, desc ) ;

                returnOpcodes.add( Opcodes.RETURN ) ;
                returnOpcodes.add( Opcodes.IRETURN ) ;
                returnOpcodes.add( Opcodes.ARETURN ) ;
                returnOpcodes.add( Opcodes.LRETURN ) ;
                returnOpcodes.add( Opcodes.FRETURN ) ;
                returnOpcodes.add( Opcodes.DRETURN ) ;

                this.mn = mn ;
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
                info( "MM method: onMethodEnter" ) ;
                mv.visitLabel(start); 

                // __result = null or 0 (type specific, omitted if void return)
                if (__result != null) {
                    initLocal( mv, __result ) ;
                }

                // Object __ident = null ;
                initLocal( mv, __ident ) ;

                // final MethodMonitor __mm = __mmXX.content() ;
                // (for the appropriate XX for this method)
                __mm.accept( mv ) ;
                final String fullDesc = getFullMethodDescriptor(mn) ;
                info( "fullDesc = " + fullDesc ) ;
                final String annoName = methodToAnno.get( fullDesc ) ;
                final String fname = annoToHolderName.get( annoName ) ;
                mv.visitFieldInsn( Opcodes.GETSTATIC, currentClass.name,
                    fname, SH_NAME );
                mv.visitMethodInsn( Opcodes.INVOKEVIRTUAL, SH_NAME, "content",
                    "()Ljava/lang/Object;" );
                mv.visitTypeInsn( Opcodes.CHECKCAST, MM_NAME );
                mv.visitVarInsn( Opcodes.ISTORE, __mm.index );

                // final boolean enabled = __mm != null ;
                Label lab1 = new Label() ;
                Label lab2 = new Label() ;
                mv.visitVarInsn( Opcodes.ILOAD, __mm.index ) ;
                mv.visitJumpInsn( Opcodes.IFNULL, lab1) ;
                mv.visitInsn( Opcodes.ICONST_1 );
                mv.visitJumpInsn( Opcodes.GOTO, lab2);
                mv.visitLabel( lab1 ) ;
                mv.visitInsn( Opcodes.ICONST_0 );
                mv.visitVarInsn( Opcodes.ISTORE, __enabled.index );
                mv.visitLabel( lab2 ) ;

                // if (enabled) {
                Label skipPreamble = new Label() ;
                mv.visitVarInsn( Opcodes.ILOAD, __enabled.index ) ;
                mv.visitJumpInsn( Opcodes.IFEQ, skipPreamble );

                // __ident = <method constant>
                emitMethodConstant( methodNames, mv, mn.name ) ;
                String owner = Type.getInternalName( Integer.class ) ;
                mv.visitMethodInsn( Opcodes.INVOKESTATIC, owner, "valueOf",
                    "(I)Ljava.lang.Integer;" ) ;
                mv.visitVarInsn( Opcodes.ASTORE, __mm.index ) ;

                // __mm.enter( __ident, <array of wrapped args> ) ;
                mv.visitVarInsn( Opcodes.ALOAD, __mm.index )  ;
                mv.visitVarInsn( Opcodes.ALOAD, __ident.index ) ;

                wrapArgs( mv, mn.access, mn.desc ) ;

                mv.visitMethodInsn( Opcodes.INVOKEINTERFACE, MM_NAME, "enter",
                    "(Ljava/lang/Object;[Ljava/lang/Object;)" ) ;

                // }
                mv.visitLabel( skipPreamble ) ;
            }

            @Override
            public void visitMethodInsn( int opcode, String owner, 
                String name, String desc ) {
                info( "MM method: visitMethodInsn: " + owner + "." + name + desc ) ;
                
                // If opcode is INVOKESPECIAL, owner is this class, and name/desc
                // are in the infoMethodDescs set, update the desc for the call
                // and add the extra parameters to the end of the call.
                String fullDesc = getFullMethodDescriptor( name, desc ) ;
                if ((opcode == Opcodes.INVOKESPECIAL) 
                    && (owner.equals( currentClass.name )) 
                    && (infoMethodDescs.contains( fullDesc ))) {

                    info( "    rewriting method call" ) ;
                    mv.visitVarInsn( Opcodes.ALOAD, __mm.index ) ;
                    mv.visitVarInsn( Opcodes.ALOAD, __ident.index ) ;
                    String newDesc = augmentInfoMethodDescriptor(desc) ;

                    mv.visitMethodInsn(opcode, owner, name, newDesc );
                }
            }

            private void emitFinally() {
                Label skipLabel = new Label() ;
                mv.visitVarInsn( Opcodes.ILOAD, __enabled.index ) ;
                mv.visitJumpInsn( Opcodes.IFEQ, skipLabel ) ;

                mv.visitVarInsn( Opcodes.ALOAD, __mm.index ) ;
                mv.visitVarInsn( Opcodes.ALOAD, __ident.index ) ;

                Type rtype = Type.getReturnType( mn.desc ) ;
                if (rtype.equals( Type.VOID_TYPE )) {
                    mv.visitMethodInsn( Opcodes.INVOKEVIRTUAL, MM_NAME, "exit",
                        "(Ljava/lang/Object;)V" ) ;
                } else {
                    wrapArg( mv, __result.index, Type.getType( __result.desc ) ) ;

                    mv.visitMethodInsn( Opcodes.INVOKEVIRTUAL, MM_NAME, "exit",
                        "(Ljava/lang/Object;Ljava/lang/Object;)V" ) ;
                }

                mv.visitLabel( skipLabel ) ;
            }

            @Override
            public void onMethodExit( int opcode ) {
                info( "MM method: onMethodExit" ) ;
                if (returnOpcodes.contains(opcode )) {
                    info( "    handling return" ) ;
                    storeFromXReturn( mv, opcode, __result ) ;

                    emitFinally() ;

                    loadFromXReturn( mv, opcode, __result ) ;
                } else if (opcode == Opcodes.ATHROW) {
                    info( "    handling throw" ) ;
                    int exc = newLocal( Type.getType(Throwable.class)) ;
                    mv.visitVarInsn( Opcodes.ASTORE, exc) ;

                    Label skipLabel = new Label() ;
                    mv.visitVarInsn( Opcodes.ILOAD, __enabled.index ) ;
                    mv.visitJumpInsn( Opcodes.IFEQ, skipLabel ) ;

                    // emit code for reporting exception
                    mv.visitVarInsn( Opcodes.ALOAD, __mm.index ) ;
                    mv.visitVarInsn( Opcodes.ALOAD, __ident.index ) ;
                    mv.visitVarInsn( Opcodes.ALOAD, exc ) ;
                    mv.visitMethodInsn( Opcodes.INVOKEVIRTUAL, MM_NAME, "" +
                         "exception",
                         "(Ljava/lang/Object;Ljava/lang/Throwable;)V") ;

                    mv.visitLabel( skipLabel ) ;
                    // restore exception from local for following ATHROW
                    // (this will be caught in the finally exception handler,
                    // which will handle calling MethodMonitor.exit).
                    mv.visitVarInsn( Opcodes.ALOAD, exc ) ;
                } // all others can be ignored.
            }

            @Override
            public void visitEnd() {
                info( "MM method: visitEnd" ) ;
                mv.visitLabel( excHandler  ) ;

                // Store the exception
                int excIndex = newLocal( Type.getType( Throwable.class ) ) ;
                mv.visitVarInsn( Opcodes.ASTORE, excIndex ) ;

                emitFinally() ;

                // throw the exception
                mv.visitVarInsn( Opcodes.ALOAD, excIndex ) ;
                mv.visitInsn( Opcodes.ATHROW ) ;

                mv.visitLabel( end ) ;

                mv.visitTryCatchBlock( start, end, excHandler, null );
            }
        }

        // - Scan method body:
        //   - for each return, add the finally body
        //   - for each call to an InfoMethod, add extra parameters to the
        //     end of the call (note that it is MUCH easier to recognize the
        //     end than the start of a method call, since nested calls and
        //     complex expressions make recognizing the start quite difficult)
        // - add preamble
        // - add outer exception handler
        private void handleMMMethod( MethodNode mn ) {
            AdviceAdapter aa = new MonitoredMethodEnhancer( mn.access, 
                mn.name, mn.desc, mn ) ;
            mn.accept( aa ) ;
        }

        // Scan method body:
        // - ensure that no info methods are called
        //   (that is an invokespecial
        private void handleDefaultMethod( MethodNode mn ) {
            final Iterator<? extends AbstractInsnNode> ilist = 
                mn.instructions.iterator() ;
            while (ilist.hasNext()) {
                AbstractInsnNode anode = ilist.next() ;
                if (anode instanceof MethodInsnNode) {
                    final MethodInsnNode mnode = (MethodInsnNode)anode ;
                    // mnode.opcode should be INVOKESPECIAL, but we really don't
                    // need to check for that.
                    String mdesc = getFullMethodDescriptor( mnode ) ;
                    if (infoMethodDescs.contains(mdesc)) {
                        error( "Method " + getFullMethodDescriptor(mnode)
                            + " in class " + currentClass.name + " makes an "
                            + " illegal call to an @InfoMethod method" ) ;
                    }
                }
            }
        }

        public Boolean enhance() {
            // Ignore annotations and interfaces.
            if (hasAccess(currentClass.access, Opcodes.ACC_ANNOTATION) ||
                hasAccess(currentClass.access, Opcodes.ACC_INTERFACE)) {
                return false ;
            }

            processClassAnnotations() ;
            if (annoNamesForClass.isEmpty()) { 
                return false ;
            }

            scanMethods() ;

            addFieldsToClass() ;

            final List<MethodNode> mnodes = currentClass.methods ;
            boolean hasStaticInit = false ;
            for (final MethodNode mn : mnodes) {
                final String desc = getFullMethodDescriptor( mn ) ;
                info( "Handling method " + desc ) ;

                if (mn.name.equals( "<clinit>")) {
                    handleStaticInitializer( mn ) ;
                    hasStaticInit = true ;
                } else if (infoMethodDescs.contains( desc )) {
                    handleInfoMethod( mn ) ;
                } else if (mmMethodDescs.contains( desc )) {
                    handleMMMethod( mn ) ;
                } else {
                    handleDefaultMethod( mn ) ;
                }
            }

            if (!hasStaticInit) {
                handleStaticInitializer( null ) ;
            }

            return false ; // for initial testing!
        }
    }
}
