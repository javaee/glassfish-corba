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

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;
import org.objectweb.asm.AnnotationVisitor;
import org.objectweb.asm.Attribute;
import org.objectweb.asm.Label;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;

/**
 *
 * @author ken
 */
public class SimpleMethodTracer implements MethodVisitor {
    final MethodVisitor mv ;
    final Util util ;
    final Set<Label> visitedLables = new HashSet<Label>() ;

    private void msg( String str ) {
        util.info( 2, "---" + str ) ;
    }

    public SimpleMethodTracer( MethodVisitor mv, Util util ) {
        this.mv = mv ;
        this.util = util ;
    }

    public AnnotationVisitor visitAnnotationDefault() {
        msg( "visitAnnotationDefault") ;
        return mv.visitAnnotationDefault() ;
    }

    public AnnotationVisitor visitAnnotation(String desc, boolean visible) {
        msg( "visitAnnotation(desc=" + desc + ",visible=" + visible + ")" ) ;
        return mv.visitAnnotation(desc, visible) ;
    }

    public AnnotationVisitor visitParameterAnnotation(int parameter, String desc, boolean visible) {
        msg( "visitParameterAnnotation(parameter=" + parameter
            + ".desc=" + desc + ",visible=" + visible + ")" ) ;
        return mv.visitParameterAnnotation(parameter, desc, visible) ;
    }

    public void visitAttribute(Attribute attr) {
        msg( "visitAttribute(attr=" + attr + ")" ) ;
        mv.visitAttribute( attr ) ;
    }

    public void visitCode() {
        msg( "visitCode" ) ;
        mv.visitCode() ;
    }

    private String getFrameType( int type ) {
        switch (type) {
            case Opcodes.F_APPEND : return "APPEND" ;
            case Opcodes.F_CHOP : return "CHOP" ;
            case Opcodes.F_FULL : return "FULL" ;
            case Opcodes.F_NEW  : return "NEW" ;
            case Opcodes.F_SAME : return "SAME" ;
            case Opcodes.F_SAME1 : return "SAME1" ;
        }
        return "BAD_FRAME_TYPE" ;
    }


    public void visitFrame(int type, int nLocal, Object[] local, int nStack, Object[] stack) {
        msg( "visitFrame(type=" + getFrameType(type)
            + ",nLocal=" + nLocal + ",local=" + Arrays.asList( local )
            + ",nStack=" + nStack + ",stack=" + Arrays.asList( stack ) + ")" ) ;

        mv.visitFrame( type, nLocal, local, nStack, stack ) ;
    }

    public void visitInsn(int opcode) {
        msg( "visitInsn(opcode=" + Util.opcodeToString(opcode) + ")" ) ;
        mv.visitInsn( opcode ) ;
    }

    public void visitIntInsn(int opcode, int operand) {
        msg( "visitIntInsn(opcode=" + Util.opcodeToString(opcode)
            + ",operand=" + operand + ")" ) ;
        mv.visitIntInsn( opcode, operand ) ;
    }

    public void visitVarInsn(int opcode, int var) {
        msg( "visitVarInsn(opcode=" + Util.opcodeToString(opcode)
            + ",var=" + var + ")" ) ;
        mv.visitVarInsn(opcode, var);
    }

    public void visitTypeInsn(int opcode, String type) {
        msg( "visitTypeInsn(opcode=" + Util.opcodeToString(opcode)
            + ",type=" + type + ")" ) ;
        mv.visitTypeInsn(opcode, type);
    }

    public void visitFieldInsn(int opcode, String owner, String name, String desc) {
        msg( "visitFieldInsn(opcode=" + Util.opcodeToString(opcode) 
            + ",owner=" + owner + ",name=" + name + ",desc=" + desc + ")" ) ;
        mv.visitFieldInsn(opcode, owner, name, desc);
    }

    public void visitMethodInsn(int opcode, String owner, String name, String desc) {
        msg( "visitMethodInsn(opcode=" + Util.opcodeToString(opcode)
            + ",owner=" + owner + ",name=" + name + ",desc=" + desc + ")" ) ;
        mv.visitMethodInsn(opcode, owner, name, desc);
    }

    public void visitJumpInsn(int opcode, Label label) {
        msg( "visitJumpInsn(opcode=" + Util.opcodeToString(opcode)
            + ",label=" + label + ")" ) ;
        mv.visitJumpInsn(opcode, label);
    }

    public void visitLabel(Label label) {
        msg( "visitLabel(label=" + label + ")" ) ;
        visitedLables.add( label ) ;
        mv.visitLabel(label);
    }

    public void visitLdcInsn(Object cst) {
        msg( "visitLdcInsn(cst=" + cst + ")" ) ;
        mv.visitLdcInsn(cst);
    }

    public void visitIincInsn(int var, int increment) {
        msg( "visitIincInsn(var=" + var
            + ",increment=" + increment + ")" ) ;
        mv.visitIincInsn(var, increment);
    }

    public void visitTableSwitchInsn(int min, int max, Label dflt, Label[] labels) {
        msg( "visitTableSwitchInsn" ) ;
        mv.visitTableSwitchInsn(min, max, dflt, labels);
    }

    public void visitLookupSwitchInsn(Label dflt, int[] keys, Label[] labels) {
        msg( "visitLookupSwitchInsn" ) ;
        mv.visitLookupSwitchInsn(dflt, keys, labels);
    }

    public void visitMultiANewArrayInsn(String desc, int dims) {
        msg( "visitMultiANewArrayInsn(desc=" + desc + ",dims=" + dims + ")" ) ;
        mv.visitMultiANewArrayInsn(desc, dims);
    }

    public void visitTryCatchBlock(Label start, Label end, Label handler,
        String type) {

        if (visitedLables.contains(start)) {
            throw new RuntimeException(
                "visiting try-catch block: start label has already been visited" ) ;
        }

        if (visitedLables.contains(end)) {
            throw new RuntimeException(
                "visiting try-catch block: end label has already been visited" ) ;
        }

        if (visitedLables.contains(handler)) {
            throw new RuntimeException(
                "visiting try-catch block: handler label has already been visited" ) ;
        }

        msg( "visitTryCatchBlock(start=" + start + ",end=" + end
            + ",handler=" + handler + ",type=" + type + ")" ) ;
        mv.visitTryCatchBlock(start, end, handler, type);
    }

    public void visitLocalVariable(String name, String desc, String signature,
        Label start, Label end, int index) {

        if (!visitedLables.contains( start )) {
            throw new RuntimeException(
                "visitLocalVariable: start label has not been visited" ) ;
        }

        if (!visitedLables.contains( end )) {
            throw new RuntimeException(
                "visitLocalVariable: end label has not been visited" ) ;
        }

        msg( "visitLocalVariable(name=" + name + ",desc=" + desc
            + ",signature=" + signature + ",start=" + start + ",end=" + end
            + ",index=" + index + ")" ) ;
        mv.visitLocalVariable(name, desc, signature, start, end, index);
    }

    public void visitLineNumber(int line, Label start) {
        msg( "visitLineNumber(line=" + line + ",start=" + start + ")" ) ;
        mv.visitLineNumber(line, start);
    }

    public void visitMaxs(int maxStack, int maxLocals) {
        msg( "visitMaxs(maxStack=" + maxStack 
            + ",maxLocals=" + maxLocals + ")" ) ;
        mv.visitMaxs(maxStack, maxLocals);
    }

    public void visitEnd() {
        msg( "visitEnd" ) ;
        mv.visitEnd();
    }
}
