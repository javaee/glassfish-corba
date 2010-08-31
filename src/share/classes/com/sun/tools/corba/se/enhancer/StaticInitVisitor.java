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

import com.sun.corba.se.spi.orbutil.generic.SynchronizedHolder;
import com.sun.corba.se.spi.orbutil.tf.EnhancedClassData;
import com.sun.corba.se.spi.orbutil.tf.MethodMonitorRegistry;
import com.sun.corba.se.spi.orbutil.tf.Util;

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
        util.info( 2, "StaticInitVisitor created" ) ;
    }

    private LocalVariableNode defineLocal( MethodVisitor mv, String name, 
        Class<?> cls, Label start, Label end ) {

        Type type = Type.getType( cls ) ;
        int index = newLocal( type ) ;
        LabelNode snode = new LabelNode( start ) ;
        LabelNode enode = new LabelNode( end ) ;
        return new LocalVariableNode( name, type.getDescriptor(), null,
            snode, enode, index ) ;
    }

    private static final boolean ENABLED = false ;

    private void generateTraceMsg( MethodVisitor mv, String msg, int num ) {
        if (ENABLED && util.getDebug()) {
            final Label start = new Label() ;
            mv.visitLabel( start ) ;
            mv.visitLineNumber( num, start ) ;
            mv.visitFieldInsn( Opcodes.GETSTATIC, "java/lang/System", "out", 
                "Ljava/io/PrintStream;" ) ;
            mv.visitLdcInsn( msg );
            mv.visitMethodInsn( Opcodes.INVOKEVIRTUAL, "java/io/PrintStream",
                "println", "(Ljava/lang/String;)V");
        }
    }

    private static final boolean SHORT_FORM = true ;

    @Override
    public void visitCode() {
	if (SHORT_FORM) {
	    super.visitCode() ;
	    mv.visitLdcInsn( Type.getType( "L" + ecd.getClassName() + ";" ));
	    Type mmrType = Type.getType( MethodMonitorRegistry.class ) ;
	    String mdesc = "(Ljava/lang/Class;)V" ;
	    mv.visitMethodInsn( Opcodes.INVOKESTATIC,
		mmrType.getInternalName(), "registerClass", mdesc ) ;
	} else {
	    int line = 1 ;
	    util.info( 2, "StaticInitVisitor.visitCode" ) ;
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

	    generateTraceMsg( mv, "initialize the holders", line++ ) ;
	    for (String str : ecd.getAnnotationToHolderName().values()) {
		generateTraceMsg( mv, "Generating to initialize holder " + str,
		    line++ ) ;
		util.info( 2, "Generating code to initialize holder " + str ) ;
		util.newWithSimpleConstructor( mv, SynchronizedHolder.class );
		mv.visitFieldInsn( Opcodes.PUTSTATIC,
		    ecd.getClassName(), str,
		    Type.getDescriptor(SynchronizedHolder.class ) ) ;
	    }

	    generateTraceMsg( mv, "Store the Class of this class", line++ );
	    mv.visitLdcInsn( Type.getType( "L" + ecd.getClassName() + ";" ));
	    mv.visitVarInsn( Opcodes.ASTORE, thisClass.index ) ;

	    generateTraceMsg( mv, "Create list of method names", line++ );
	    util.newWithSimpleConstructor( mv, ArrayList.class ) ;
	    mv.visitVarInsn( Opcodes.ASTORE, mnameList.index ) ;

	    for (String str : ecd.getMethodNames()) {
		util.info( 2, "Generating code to add " + str
                    + " to methodNames" ) ;
		mv.visitVarInsn( Opcodes.ALOAD, mnameList.index ) ;
		mv.visitLdcInsn( str );
		mv.visitMethodInsn( Opcodes.INVOKEINTERFACE,
		    "java/util/List", "add", "(Ljava/lang/Object;)Z" );
		mv.visitInsn( Opcodes.POP ) ;
	    }

	    generateTraceMsg( mv,
		"create map from MM annotation class to Holder and init",
                line++ ) ;
	    util.newWithSimpleConstructor( mv, HashMap.class ) ;
	    mv.visitVarInsn( Opcodes.ASTORE, holderMap.index ) ;

	    for (Map.Entry<String,String> entry :
		ecd.getAnnotationToHolderName().entrySet()) {

		util.info( 2, "Generating code to put " + entry.getKey() + "=>"
		    + entry.getValue() + " into holderMap" ) ;

		mv.visitVarInsn( Opcodes.ALOAD, holderMap.index ) ;

		Type annoType = Type.getType( "L" + entry.getKey() + ";" ) ;
		mv.visitLdcInsn( annoType );

		mv.visitFieldInsn( Opcodes.GETSTATIC, ecd.getClassName(),
		    entry.getValue(),
		    Type.getDescriptor(SynchronizedHolder.class ) ) ;

		mv.visitMethodInsn( Opcodes.INVOKEINTERFACE,
		    "java/util/Map", "put",
		    "(Ljava/lang/Object;Ljava/lang/Object;)Ljava/lang/Object;");

		mv.visitInsn( Opcodes.POP ) ;
	    }

	    generateTraceMsg( mv, "register with MethodMonitorRegistry",
                line++ ) ;
	    util.info( 2,
                "Generating code call MethodMonitorRegistry.registerClass" ) ;
	    mv.visitVarInsn( Opcodes.ALOAD, thisClass.index ) ;
	    mv.visitVarInsn( Opcodes.ALOAD, mnameList.index ) ;
	    mv.visitVarInsn( Opcodes.ALOAD, holderMap.index ) ;

	    Type mmrType = Type.getType( MethodMonitorRegistry.class ) ;
	    String mdesc =
                "(Ljava/lang/Class;Ljava/util/List;Ljava/util/Map;)V" ;
	    mv.visitMethodInsn( Opcodes.INVOKESTATIC,
		mmrType.getInternalName(), "registerClass", mdesc ) ;

	    mv.visitLabel( end ) ;

	    thisClass.accept( mv ) ;
	    mnameList.accept( mv ) ;
	    holderMap.accept( mv ) ;
	}
    }
}
