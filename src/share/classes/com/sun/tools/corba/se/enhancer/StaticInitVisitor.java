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
import com.sun.corba.se.spi.orbutil.tf.MethodMonitorRegistry;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.objectweb.asm.Label;

import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.Type;
import org.objectweb.asm.commons.LocalVariablesSorter;
import org.objectweb.asm.tree.LabelNode;
import org.objectweb.asm.tree.LocalVariableNode;

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

    private LocalVariableNode defineLocal( MethodVisitor mv, String name, Class<?> cls,
        Label start, Label end ) {

        Type type = Type.getType( cls ) ;
        int index = newLocal( type ) ;
        LabelNode snode = new LabelNode( start ) ;
        LabelNode enode = new LabelNode( end ) ;
        return new LocalVariableNode( name, type.getDescriptor(), null,
            snode, enode, index ) ;
    }

    @Override
    public void visitCode() {
        util.info( "StaticInitVisitor.visitCode" ) ;
        super.visitCode() ;

        Label start = new Label() ;
        Label end = new Label() ;

        mv.visitLabel( start ) ;

        LocalVariableNode thisClass = defineLocal( mv, "thisClass",
            Class.class, start, end ) ;
        LocalVariableNode mnameList = defineLocal( mv, "mnameList",
            List.class, start, end ) ;
        LocalVariableNode holderMap = defineLocal( mv, "holderMap",
            Map.class, start, end ) ;

        // initialize the holders
        for (String str : ecd.getAnnotationToHolderName().values()) {
            util.info( "Generating code to initialize holder " + str ) ;
            util.newWithSimpleConstructor( mv, SynchronizedHolder.class );
            mv.visitFieldInsn( Opcodes.PUTSTATIC,
                ecd.getClassName(), str,
                Type.getDescriptor(SynchronizedHolder.class ) ) ;
        }

        mv.visitLdcInsn( Type.getType( "L" + ecd.getClassName() + ";" ));
        mv.visitVarInsn( Opcodes.ASTORE, thisClass.index ) ;

        // create list of method names and init
        util.newWithSimpleConstructor( mv, ArrayList.class ) ;
        mv.visitVarInsn( Opcodes.ASTORE, mnameList.index ) ;

        for (String str : ecd.getMethodNames()) {
            util.info( "Generating code to add " + str + " to methodNames" ) ;
            mv.visitVarInsn( Opcodes.ALOAD, mnameList.index ) ;
            mv.visitLdcInsn( str );
            mv.visitMethodInsn( Opcodes.INVOKEINTERFACE,
                "java/util/List", "add", "(Ljava/lang/Object;)Z" );
            mv.visitInsn( Opcodes.POP ) ;
        }

        // create map from MM annotation class to Holder and init
        util.newWithSimpleConstructor( mv, HashMap.class ) ;
        mv.visitVarInsn( Opcodes.ASTORE, holderMap.index ) ;

        for (Map.Entry<String,String> entry :
            ecd.getAnnotationToHolderName().entrySet()) {

            util.info( "Generating code to put " + entry.getKey() + "=>"
                + entry.getValue() + " into holderMap" ) ;

            mv.visitVarInsn( Opcodes.ALOAD, holderMap.index ) ;

            Type annoType = Type.getType( "L" + entry.getKey() + ";" ) ;
            mv.visitLdcInsn( annoType );

            mv.visitFieldInsn( Opcodes.GETSTATIC, ecd.getClassName(),
                entry.getValue(), 
                Type.getDescriptor(SynchronizedHolder.class ) ) ;

            mv.visitMethodInsn( Opcodes.INVOKEINTERFACE,
                "java/util/HashMap", "put",
                "(Ljava/lang/Object;Ljava/lang/Object;)Ljava/lang/Object;" );

            mv.visitInsn( Opcodes.POP ) ;
        }

        // register with MethodMonitorRegistry
        util.info( "Generating code call MethodMonitorRegistry.registerClass" ) ;
        mv.visitVarInsn( Opcodes.ALOAD, thisClass.index ) ;
        mv.visitVarInsn( Opcodes.ALOAD, mnameList.index ) ;
        mv.visitVarInsn( Opcodes.ALOAD, holderMap.index ) ;

        Type mmrType = Type.getType( MethodMonitorRegistry.class ) ;
        String mdesc = "(Ljava/lang/Class;Ljava/util/List;Ljava/util/Map;)V" ;
        mv.visitMethodInsn( Opcodes.INVOKESTATIC,
            mmrType.getInternalName(), "registerClass", mdesc ) ;

        mv.visitLabel( end ) ;

        thisClass.accept( mv ) ;
        mnameList.accept( mv ) ;
        holderMap.accept( mv ) ;

        mv.visitInsn( Opcodes.RETURN ) ;
    }
}