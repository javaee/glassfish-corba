
package com.sun.tools.corba.se.enhancer;

import com.sun.corba.se.spi.orbutil.generic.SynchronizedHolder;
import com.sun.corba.se.spi.orbutil.tf.MethodMonitorRegistry;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.Type;
import org.objectweb.asm.commons.LocalVariablesSorter;
import org.objectweb.asm.tree.LabelNode;

/**
 *
 * @author ken
 */
public class StaticInitVisitor extends LocalVariablesSorter {
    private final Util util ;
    private final EnhancedClassData ecd ;

    public StaticInitVisitor( final int access, final String desc,
        final MethodVisitor mv, Util util, EnhancedClassData ecd ) {

        super( access, desc, mv ) ;
        this.util = util ;
        this.ecd = ecd ;
        util.info( "StaticInitVisitor created" ) ;
    }

    @Override
    public void visitMaxs( int ms, int ml ) {
        util.info( "StaticInitVisitor.visitEnd" ) ;
        LabelNode start = new LabelNode() ;
        LabelNode end = new LabelNode() ;

        int thisClass = newLocal( Type.getType( Class.class )) ;
        int mnameList = newLocal( Type.getType( List.class )) ;
        int holderMap = newLocal( Type.getType( Map.class )) ;

        // initialize the holders
        for (String str : ecd.getAnnotationToHolderName().values()) {
            util.info( "Generating code to initialize holder " + str ) ;
            util.newWithSimpleConstructor( mv, SynchronizedHolder.class );
            mv.visitFieldInsn( Opcodes.PUTSTATIC,
                ecd.getClassName(), str, EnhancedClassData.SH_NAME );
        }

        mv.visitLdcInsn( Type.getType( "L" + ecd.getClassName() + ";" ));
        mv.visitVarInsn( Opcodes.ASTORE, thisClass ) ;

        // create list of method names and init
        util.newWithSimpleConstructor( mv, ArrayList.class ) ;
        mv.visitVarInsn( Opcodes.ASTORE, mnameList ) ;

        for (String str : ecd.getMethodNames()) {
            util.info( "Generating code to add " + str + " to methodNames" ) ;
            mv.visitVarInsn( Opcodes.ALOAD, mnameList ) ;
            mv.visitLdcInsn( str );
            mv.visitMethodInsn( Opcodes.INVOKEINTERFACE,
                "java/util/List", "add", "(Ljava/lang/Object;)Z" );
            mv.visitInsn( Opcodes.POP ) ;
        }

        // create map from MM annotation class to Holder and init
        util.newWithSimpleConstructor( mv, HashMap.class ) ;
        mv.visitVarInsn( Opcodes.ASTORE, holderMap ) ;

        for (Map.Entry<String,String> entry :
            ecd.getAnnotationToHolderName().entrySet()) {

            util.info( "Generating code to put " + entry.getKey() + "=>"
                + entry.getValue() + " into holderMap" ) ;

            mv.visitVarInsn( Opcodes.ALOAD, holderMap ) ;

            Type annoType = Type.getType( "L" + entry.getKey() + ";" ) ;
            mv.visitLdcInsn( annoType );

            mv.visitFieldInsn( Opcodes.GETSTATIC, ecd.getClassName(),
                entry.getValue(), EnhancedClassData.SH_NAME) ;

            mv.visitMethodInsn( Opcodes.INVOKEINTERFACE,
                "/java/util/HashMap", "add",
                "(Ljava/lang/Object;Ljava/lang/Object;)Ljava/lang/Object;" );

            mv.visitInsn( Opcodes.POP ) ;
        }

        // register with MethodMonitorRegistry
        util.info( "Generating code call MethodMonitorRegistry.registerClass" ) ;
        mv.visitVarInsn( Opcodes.ALOAD, thisClass ) ;
        mv.visitVarInsn( Opcodes.ALOAD, mnameList ) ;
        mv.visitVarInsn( Opcodes.ALOAD, holderMap ) ;

        Type mmrType = Type.getType( MethodMonitorRegistry.class ) ;
        String mdesc = "(Ljava/lang/Class;Ljava/util/List;Ljava/util/Map;)V" ;
        mv.visitMethodInsn( Opcodes.INVOKESTATIC,
            mmrType.getInternalName(), "registerClass", mdesc ) ;

        mv.visitInsn( Opcodes.RETURN ) ;

        super.visitMaxs( 0, 0 ) ;
    }
}