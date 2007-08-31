/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2005-2007 Sun Microsystems, Inc. All rights reserved.
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

package com.sun.corba.se.impl.orbutil.codegen;

import com.sun.corba.se.spi.orbutil.codegen.Type ;
import com.sun.corba.se.spi.orbutil.codegen.Expression ;
import com.sun.corba.se.spi.orbutil.codegen.Signature ;
import com.sun.corba.se.spi.orbutil.codegen.Variable ;

/**
 *
 * @author Ken Cavanaugh
 */
public interface Visitor {
    void visitClassGenerator( ClassGenerator arg ) ;

    void visitMethodGenerator( MethodGenerator arg ) ;

    void visitNode( Node arg ) ;

    void visitFieldGenerator( FieldGenerator arg ) ;
    
    void visitStatement( Statement arg ) ;

    void visitThrowStatement( ThrowStatement arg ) ;
    
    void visitAssignmentStatement( AssignmentStatement arg ) ;

    void visitDefinitionStatement( DefinitionStatement arg ) ;

    void visitBlockStatement( BlockStatement arg ) ;
    
    void visitCaseBranch( CaseBranch arg ) ;

    void visitIfStatement( IfStatement arg ) ;
    
    void visitBreakStatement( BreakStatement arg ) ;

    void visitReturnStatement( ReturnStatement arg ) ;
    
    void visitSwitchStatement( SwitchStatement arg ) ;
    
    void visitTryStatement( TryStatement arg ) ;
    
    void visitWhileStatement( WhileStatement arg ) ;
    
    void visitExpression( Expression arg ) ;

    void visitVariable( Variable arg ) ;

    void visitConstantExpression( ExpressionFactory.ConstantExpression arg ) ;

    void visitVoidExpression( ExpressionFactory.VoidExpression arg ) ;

    void visitThisExpression( ExpressionFactory.ThisExpression arg ) ;

    void visitUnaryOperatorExpression( ExpressionFactory.UnaryOperatorExpression arg ) ;

    void visitBinaryOperatorExpression( ExpressionFactory.BinaryOperatorExpression arg ) ;

    void visitCastExpression( ExpressionFactory.CastExpression arg ) ;

    void visitInstofExpression( ExpressionFactory.InstofExpression arg ) ;

    void visitStaticCallExpression( ExpressionFactory.StaticCallExpression arg ) ;

    void visitNonStaticCallExpression( ExpressionFactory.NonStaticCallExpression arg ) ;

    void visitNewObjExpression( ExpressionFactory.NewObjExpression arg ) ;

    void visitNewArrExpression( ExpressionFactory.NewArrExpression arg ) ;

    void visitSuperCallExpression( ExpressionFactory.SuperCallExpression arg ) ;

    void visitSuperObjExpression( ExpressionFactory.SuperObjExpression arg ) ;

    void visitThisObjExpression( ExpressionFactory.ThisObjExpression arg ) ;

    void visitNonStaticFieldAccessExpression( 
	ExpressionFactory.NonStaticFieldAccessExpression arg ) ;

    void visitStaticFieldAccessExpression( ExpressionFactory.StaticFieldAccessExpression arg ) ;

    void visitArrayIndexExpression( ExpressionFactory.ArrayIndexExpression arg ) ;

    void visitArrayLengthExpression( ExpressionFactory.ArrayLengthExpression arg ) ;

    void visitIfExpression( ExpressionFactory.IfExpression arg ) ;
}
