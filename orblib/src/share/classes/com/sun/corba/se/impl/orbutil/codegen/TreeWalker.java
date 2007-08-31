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

import java.util.Stack ;
import java.util.Map ;

import java.lang.reflect.Modifier ;

import com.sun.corba.se.spi.orbutil.generic.Pair ;

import com.sun.corba.se.spi.orbutil.codegen.Type ;
import com.sun.corba.se.spi.orbutil.codegen.Expression ;
import com.sun.corba.se.spi.orbutil.codegen.Variable ;

/** This is a general purpose utility that does a complete traversal
 * of a Node tree.  A stack of Visitors is maintained.  The current
 * Visitor on top of the stack is applied to each node.
 * Pre and post methods are provided for each type.
 * The default implementations of these methods delegate to the pre
 * and post methods of the superclass of the node type, so only the
 * required pre and post methods need be overridden.  
 * <P>
 * This is used as follows:
 * <PRE>
 *     TreeWalkerContext context = new TreeWalkerContext() ;
 *     Visitor visitor = new SubclassOfTreeWalker( context, ... ) ;
 *     context.push( visitor ) ;
 *     node.accept( visitor ) ;
 * </PRE>
 * Note that this allows the temporary changing of the current visitor
 * while traversing the tree.  The fact that the context support mark
 * and popMark operations makes it easy to process all of the children
 * of a node in any order: just call context.mark() in a preXXX method,
 * push appropriate visitors in the intermediate xXXBeforeYYY methods,
 * and then call context.popMark in the postXXX method.
 * <P>
 * Note that all preXXX methods return a boolean which indicates whether
 * or not this node should be traversed.  If true is return, any 
 * child nodes are traversed, and the postXXX method is called.  If false
 * is returned, the visitXXX method completes.
 * <P>
 * If the preXXX method returns true, the postXXX method is always called.
 * If the preXXX method throws an exception or returns false, the
 * postXXX method is not called.
 * <P>
 * The more complex node types also include intermediate control methods
 * that can be overridden to affect the traversal.
 *
 * @author Ken Cavanaugh
 */
public abstract class TreeWalker implements Visitor {
    protected final TreeWalkerContext context ;

    public TreeWalker( TreeWalkerContext context ) {
	this.context = context ;
    }

    // Node
    public boolean preNode( Node arg ) {
	return true ;
    }

    public void postNode( Node arg ) {
    }

    public void visitNode( Node arg ) {
	if (preNode( arg )) {
	    postNode( arg ) ;
	}
    }

    // FieldGenerator
    public boolean preFieldGenerator( FieldGenerator arg ) {
	return preNode( arg ) ;
    }

    public void postFieldGenerator( FieldGenerator arg ) {
    }
    
    public final void visitFieldGenerator( FieldGenerator arg ) {
	if (preFieldGenerator( arg )) {
	    postFieldGenerator( arg )  ;
	}
    }

    // ClassGenerator
    public boolean preClassGenerator( ClassGenerator arg ) {
	return preNode( arg ) ;
    }

    // This allows for simple visitors that always perform the same action 
    // on a variable.  More complex visitors should simply return false
    // and handle class variables in preClassGenerator.
    public boolean classGeneratorBeforeFields( ClassGenerator arg ) {
	return true ;
    }

    public void classGeneratorBeforeInitializer( ClassGenerator arg ) {
    }

    public void classGeneratorBeforeMethod( ClassGenerator arg ) {
    }

    public void classGeneratorBeforeConstructor( ClassGenerator arg ) {
    }

    public void postClassGenerator( ClassGenerator arg ) {
	postNode( arg ) ;
    }
    
    public void visitClassGenerator( ClassGenerator arg ) {
	if (preClassGenerator( arg )) {
	    try {
		if (!arg.isInterface()) {
		    // We currently assume that interface have no data members,
		    // not even static ones.
		    if (classGeneratorBeforeFields(arg)) {
			for (FieldGenerator entry : arg.fields()) {
			    entry.accept( context.current() ) ;
			}
		    }

		    if (!arg.initializer().isEmpty()) {
			classGeneratorBeforeInitializer( arg ) ;
			arg.initializer().accept( context.current() ) ;
		    }
		}

		if (!arg.isInterface()) {
		    for (MethodGenerator mg : arg.constructors() ) {
			classGeneratorBeforeConstructor( arg ) ;
			mg.accept( context.current() ) ;
		    }
		}

		for (MethodGenerator mg : arg.methods() ) {
		    classGeneratorBeforeMethod( arg ) ;
		    mg.accept( context.current() ) ;
		}
	    } finally {
		postClassGenerator( arg ) ;
	    }
	}
    }

    // MethodGenerator
    public boolean preMethodGenerator( MethodGenerator arg ) {
	return preNode( arg ) ;
    }

    // This allows for simple visitors that always do the same thing for
    // variables.  More complex visitors should return false here
    // and handle the argument variables directly in preMethodGenerator.
    public boolean methodGeneratorBeforeArguments( MethodGenerator arg ) {
	return true ;
    }

    // This allows for simple visitors that always do the same thing for
    // types.  More complex visitors should return false here
    // and handle the exception types directly in preMethodGenerator.
    public void methodGeneratorAfterArguments( MethodGenerator arg ) {
    }

    public void postMethodGenerator( MethodGenerator arg ) {
	postNode( arg ) ;
    }

    public void visitMethodGenerator( MethodGenerator arg ) {
	if (preMethodGenerator( arg )) {
	    try {
		if (methodGeneratorBeforeArguments( arg )) {
		    for (Variable var : arg.arguments()) {
			var.accept( context.current() ) ;
		    }
		}

		methodGeneratorAfterArguments( arg ) ;

		if (!Modifier.isAbstract(arg.modifiers()))
		    arg.body().accept( context.current() ) ;
	    } finally {
		postMethodGenerator( arg ) ;
	    }
	}
    }

    // Statement
    public boolean preStatement( Statement arg ) {
	return preNode( arg ) ;
    }

    public void postStatement( Statement arg ) {
	postNode( arg ) ;
    }

    public void visitStatement( Statement arg ) {
	if (preStatement( arg )) {
	    postStatement( arg ) ;
	}
    }

    // ThrowStatement
    public boolean preThrowStatement( ThrowStatement arg ) {
	return preStatement( arg ) ;
    }

    public void postThrowStatement( ThrowStatement arg ) {
	postStatement( arg ) ;
    }

    public void visitThrowStatement( ThrowStatement arg ) {
	if (preThrowStatement( arg )) {
	    try {
		arg.expr().accept( context.current() ) ;
	    } finally {
		postThrowStatement( arg ) ;
	    }
	}
    }
    
    // AssignmentStatement
    public boolean preAssignmentStatement( AssignmentStatement arg ) {
	return preStatement( arg ) ;
    }

    public void assignmentStatementBeforeLeftSide( AssignmentStatement arg ) {
    }

    public void postAssignmentStatement( AssignmentStatement arg ) {
	postStatement( arg ) ;
    }

    public void visitAssignmentStatement( AssignmentStatement arg ) {
	if (preAssignmentStatement( arg )) {
	    try {
		arg.right().accept( context.current() ) ;
		assignmentStatementBeforeLeftSide( arg ) ;
		arg.left().accept( context.current() ) ;
	    } finally {
		postAssignmentStatement( arg ) ;
	    }
	}
    }

    // BlockStatement
    public boolean preBlockStatement( BlockStatement arg ) {
	return preStatement( arg ) ;
    }

    public void blockStatementBeforeBodyStatement( BlockStatement arg, Statement stmt ) {
    }

    public void postBlockStatement( BlockStatement arg ) {
	postStatement( arg ) ;
    }

    public void visitBlockStatement( BlockStatement arg ) {
	if (preBlockStatement( arg )) {
	    try {
		for (Statement stmt : arg.body()) {
		    blockStatementBeforeBodyStatement( arg, stmt ) ;
		    stmt.accept( context.current() ) ;
		}
	    } finally {
		postBlockStatement( arg ) ;
	    }
	}
    }
    
    // CaseBranch
    public boolean preCaseBranch( CaseBranch arg ) {
	return preBlockStatement( arg ) ;
    }

    public void caseBranchBeforeBodyStatement( CaseBranch arg ) {
    }

    public void postCaseBranch( CaseBranch arg ) {
	postBlockStatement( arg ) ;
    }

    public void visitCaseBranch( CaseBranch arg ) {
	if (preCaseBranch( arg )) {
	    try {
		for (Statement stmt : arg.body()) {
		    caseBranchBeforeBodyStatement( arg ) ;
		    stmt.accept( context.current() ) ;
		}
	    } finally {
		postCaseBranch( arg ) ;
	    }
	}
    }
   
    // DefinitionStatement
    public boolean preDefinitionStatement( DefinitionStatement arg ) {
	return preStatement( arg ) ;
    }

    public boolean definitionStatementBeforeExpr( DefinitionStatement arg ) {
	return true ;
    }

    public void postDefinitionStatement( DefinitionStatement arg ) {
	postStatement( arg ) ;
    }

    public void visitDefinitionStatement( DefinitionStatement arg ) {
	if (preDefinitionStatement( arg )) {
	    try {
		arg.var().accept( context.current() ) ;

		if (definitionStatementBeforeExpr( arg )) 
		    arg.expr().accept( context.current() ) ;
	    } finally {
		postDefinitionStatement( arg ) ;
	    }
	}
    }

    // IfStatement
    public boolean preIfStatement( IfStatement arg ) {
	return preStatement( arg ) ;
    }

    public void ifStatementBeforeTruePart( IfStatement arg ) {
    }

    public boolean ifStatementBeforeFalsePart( IfStatement arg ) {
	return true ;
    }

    public void postIfStatement( IfStatement arg ) {
	postStatement( arg ) ;
    }

    public void visitIfStatement( IfStatement arg ) {
	if (preIfStatement( arg )) {
	    try {
		arg.condition().accept( context.current() ) ;
		ifStatementBeforeTruePart( arg ) ;
	        arg.truePart().accept( context.current() ) ;
		if (ifStatementBeforeFalsePart( arg )) 
		    arg.falsePart().accept( context.current() ) ;
	    } finally {
		postIfStatement( arg ) ;
	    }
	}
    }
    
    // BreakStatement
    public boolean preBreakStatement( BreakStatement arg ) {
	return preStatement( arg ) ;
    }

    public void postBreakStatement( BreakStatement arg ) {
	postStatement( arg ) ;
    }

    public void visitBreakStatement( BreakStatement arg ) {
	if (preBreakStatement( arg )) {
	    postBreakStatement( arg ) ;
	}
    }
    
    // ReturnStatement
    public boolean preReturnStatement( ReturnStatement arg ) {
	return preStatement( arg ) ;
    }

    public void postReturnStatement( ReturnStatement arg ) {
	postStatement( arg ) ;
    }

    public void visitReturnStatement( ReturnStatement arg ) {
	if (preReturnStatement( arg )) {
	    try {
		if (arg.expr() != null)
		    arg.expr().accept( context.current() ) ;
	    } finally {
		postReturnStatement( arg ) ;
	    }
	}
    }
    
    // SwitchStatement
    public boolean preSwitchStatement( SwitchStatement arg ) {
	return preStatement( arg ) ;
    }

    public boolean switchStatementBeforeCaseBranches( SwitchStatement arg ) {
	return true ;
    }

    public boolean switchStatementBeforeDefault( SwitchStatement arg ) {
	return true ;
    }

    public void postSwitchStatement( SwitchStatement arg ) {
	postStatement( arg ) ;
    }

    public void visitSwitchStatement( SwitchStatement arg ) {
	if (preSwitchStatement( arg )) {
	    try {
		arg.expr().accept( context.current() ) ;

		if (switchStatementBeforeCaseBranches( arg ))
		    for (Map.Entry<Integer,CaseBranch> entry : arg.cases().entrySet() ) {
			entry.getValue().accept( context.current() ) ;
		    }

		if (switchStatementBeforeDefault( arg )) 
		    arg.defaultCase().accept( context.current() ) ;
	    } finally {
		postSwitchStatement( arg ) ;
	    }
	}
    }
    
    // TryStatement
    public boolean preTryStatement( TryStatement arg ) {
	return preStatement( arg ) ;
    }

    public void tryStatementBeforeBlock( TryStatement arg,
	Type type, Variable var, BlockStatement block ) {
    }

    public boolean tryStatementBeforeFinalPart( TryStatement arg ) {
	return true ;
    }

    public void postTryStatement( TryStatement arg ) {
	postStatement( arg ) ;
    }

    public void visitTryStatement( TryStatement arg ) {
	if (preTryStatement( arg )) {
	    try {
		arg.bodyPart().accept( context.current() ) ;

		for (Map.Entry<Type,Pair<Variable,BlockStatement>> entry : 
		    arg.catches().entrySet()) {
		    tryStatementBeforeBlock( arg, entry.getKey(),
			entry.getValue().first(), entry.getValue().second()) ;
		    entry.getValue().second().accept( context.current() ) ;
		}

		if (tryStatementBeforeFinalPart( arg )) 
		    arg.finalPart().accept( context.current() ) ;
	    } finally {
		postTryStatement( arg ) ;
	    }
	}
    }
    
    // WhileStatement
    public boolean preWhileStatement( WhileStatement arg ) {
	return preStatement( arg ) ;
    }

    public void whileStatementBeforeBody( WhileStatement arg ) {
    }

    public void postWhileStatement( WhileStatement arg ) {
	postStatement( arg ) ;
    }

    public void visitWhileStatement( WhileStatement arg ) {
	if (preWhileStatement( arg )) {
	    try {
		arg.condition().accept( context.current() ) ;
		whileStatementBeforeBody( arg ) ;
		arg.body().accept( context.current() ) ;
	    } finally {
		postWhileStatement( arg ) ;
	    }
	}
    }
    
    // Expression
    public boolean preExpression( Expression arg ) {
	return preStatement( arg ) ;
    }

    public void postExpression( Expression arg ) {
	postStatement( arg ) ;
    }

    public void visitExpression( Expression arg ) {
	if (preExpression( arg )) {
	    postExpression( arg ) ;
	}
    }

    // Variable
    public boolean preVariable( Variable arg ) {
	return preExpression( arg ) ;
    }

    public void postVariable( Variable arg ) {
	postExpression( arg ) ;
    }

    public final void visitVariable( Variable arg ) {
	if (preVariable( arg ))
	    postVariable( arg ) ;
    }

    // ConstantExpression
    public boolean preConstantExpression( ExpressionFactory.ConstantExpression arg ) {
	return preExpression( arg ) ;
    }

    public void postConstantExpression( ExpressionFactory.ConstantExpression arg ) {
	postExpression( arg ) ;
    }

    public void visitConstantExpression( ExpressionFactory.ConstantExpression arg ) {
	if (preConstantExpression( arg )) {
	    postConstantExpression( arg ) ;
	}
    }

    // VoidExpression
    public boolean preVoidExpression( ExpressionFactory.VoidExpression arg ) {
	return preExpression( arg ) ;
    }

    public void postVoidExpression( ExpressionFactory.VoidExpression arg ) {
	postExpression( arg ) ;
    }

    public void visitVoidExpression( ExpressionFactory.VoidExpression arg ) {
	if (preVoidExpression( arg )) {
	    postVoidExpression( arg ) ;
	}
    }

    // ThisExpression
    public boolean preThisExpression( ExpressionFactory.ThisExpression arg ) {
	return preExpression( arg ) ;
    }

    public void postThisExpression( ExpressionFactory.ThisExpression arg ) {
	postExpression( arg ) ;
    }

    public void visitThisExpression( ExpressionFactory.ThisExpression arg ) {
	if (preThisExpression( arg )) {
	    postThisExpression( arg ) ;
	}
    }

    // UnaryOperatorExpression
    public boolean preUnaryOperatorExpression( ExpressionFactory.UnaryOperatorExpression arg ) {
	return preExpression( arg ) ;
    }

    public void postUnaryOperatorExpression( ExpressionFactory.UnaryOperatorExpression arg ) {
	postExpression( arg ) ;
    }

    public void visitUnaryOperatorExpression( ExpressionFactory.UnaryOperatorExpression arg ) {
	if (preUnaryOperatorExpression( arg )) {
	    try {
		arg.expr().accept( context.current() ) ;
	    } finally {
		postUnaryOperatorExpression( arg ) ;
	    }
	}
    }

    // BinaryOperatorExpression
    public boolean preBinaryOperatorExpression( ExpressionFactory.BinaryOperatorExpression arg ) {
	return preExpression( arg ) ;
    }

    public void binaryOperatorExpressionBeforeRight( 
	ExpressionFactory.BinaryOperatorExpression arg ) {
    }

    public void postBinaryOperatorExpression( ExpressionFactory.BinaryOperatorExpression arg ) {
	postExpression( arg ) ;
    }

    public void visitBinaryOperatorExpression( ExpressionFactory.BinaryOperatorExpression arg ) {
	if (preBinaryOperatorExpression( arg )) {
	    try {
		arg.left().accept( context.current() ) ;
		binaryOperatorExpressionBeforeRight( arg ) ;
		arg.right().accept( context.current() ) ;
	    } finally {
		postBinaryOperatorExpression( arg ) ;
	    }
	}
    }

    // CastExpression
    public boolean preCastExpression( ExpressionFactory.CastExpression arg ) {
	return preExpression( arg ) ;
    }

    public void postCastExpression( ExpressionFactory.CastExpression arg ) {
	postExpression( arg ) ;
    }

    public void visitCastExpression( ExpressionFactory.CastExpression arg ) {
	if (preCastExpression( arg )) {
	    try {
		arg.expr().accept( context.current() ) ;
	    } finally {
		postCastExpression( arg ) ;
	    }
	}
    }

    // InstofExpression
    public boolean preInstofExpression( ExpressionFactory.InstofExpression arg ) {
	return preExpression( arg ) ;
    }

    public void postInstofExpression( ExpressionFactory.InstofExpression arg ) {
	postExpression( arg ) ;
    }

    public void visitInstofExpression( ExpressionFactory.InstofExpression arg ) {
	if (preInstofExpression( arg )) {
	    try {
		arg.expr().accept( context.current() ) ;
	    } finally {
		postInstofExpression( arg ) ;
	    }
	}
    }

    // StaticCallExpression
    public boolean preStaticCallExpression( ExpressionFactory.StaticCallExpression arg ) {
	return preExpression( arg ) ;
    }

    public void staticCallExpressionBeforeArg( ExpressionFactory.StaticCallExpression arg ) {
    }

    public void postStaticCallExpression( ExpressionFactory.StaticCallExpression arg ) {
	postExpression( arg ) ;
    }

    public void visitStaticCallExpression( ExpressionFactory.StaticCallExpression arg ) {
	if (preStaticCallExpression( arg )) {
	    try {
		for (Expression expr : arg.args()) {
		    staticCallExpressionBeforeArg( arg ) ;
		    expr.accept( context.current() ) ;
		}
	    } finally {
		postStaticCallExpression( arg ) ;
	    }
	}
    }

    // NonStaticCallExpression
    public boolean preNonStaticCallExpression( ExpressionFactory.NonStaticCallExpression arg ) {
	return preExpression( arg ) ;
    }

    public void nonStaticCallExpressionBeforeArg( ExpressionFactory.NonStaticCallExpression arg ) {
    }

    public void postNonStaticCallExpression( ExpressionFactory.NonStaticCallExpression arg ) {
	postExpression( arg ) ;
    }

    public void visitNonStaticCallExpression( ExpressionFactory.NonStaticCallExpression arg ) {
	if (preNonStaticCallExpression( arg )) {
	    try {
		arg.target().accept( context.current() ) ;
		for (Expression expr : arg.args()) {
		    nonStaticCallExpressionBeforeArg( arg ) ;
		    expr.accept( context.current() ) ;
		}
	    } finally {
		postNonStaticCallExpression( arg ) ;
	    }
	}
    }

    // NewObjExpression
    public boolean preNewObjExpression( ExpressionFactory.NewObjExpression arg ) {
	return preExpression( arg ) ;
    }

    public void newObjExpressionBeforeArg( ExpressionFactory.NewObjExpression arg ) {
    }

    public void postNewObjExpression( ExpressionFactory.NewObjExpression arg ) {
	postExpression( arg ) ;
    }

    public void visitNewObjExpression( ExpressionFactory.NewObjExpression arg ) {
	if (preNewObjExpression( arg )) {
	    try {
		for (Expression expr : arg.args()) {
		    newObjExpressionBeforeArg( arg ) ;
		    expr.accept( context.current() ) ;
		}
	    } finally {
		postNewObjExpression( arg ) ;
	    }
	}
    }

    // NewArrExpression
    public boolean preNewArrExpression( ExpressionFactory.NewArrExpression arg ) {
	return preExpression( arg ) ;
    }

    public void newArrExpressionAfterSize( ExpressionFactory.NewArrExpression arg ) {
    }

    public void newArrExpressionBeforeExpression( ExpressionFactory.NewArrExpression arg ) {
    }

    public void newArrExpressionAfterExpression( ExpressionFactory.NewArrExpression arg ) {
    }

    public void postNewArrExpression( ExpressionFactory.NewArrExpression arg ) {
	postExpression( arg ) ;
    }

    public void visitNewArrExpression( ExpressionFactory.NewArrExpression arg ) {
	if (preNewArrExpression( arg )) {
	    try {
		arg.size().accept( context.current() ) ;
		newArrExpressionAfterSize( arg ) ;

		for (Expression expr : arg.exprs()) {
		    newArrExpressionBeforeExpression( arg ) ;
		    expr.accept( context.current() ) ;
		    newArrExpressionAfterExpression( arg ) ;
		}
	    } finally {
		postNewArrExpression( arg ) ;
	    }
	}
    }

    // SuperCallExpression
    public boolean preSuperCallExpression( ExpressionFactory.SuperCallExpression arg ) {
	return preExpression( arg ) ;
    }

    public void superCallExpressionBeforeArg( ExpressionFactory.SuperCallExpression arg ) {
    }

    public void postSuperCallExpression( ExpressionFactory.SuperCallExpression arg ) {
	postExpression( arg ) ;
    }

    public void visitSuperCallExpression( ExpressionFactory.SuperCallExpression arg ) {
	if (preSuperCallExpression( arg )) {
	    try {
		for (Expression expr : arg.exprs()) {
		    superCallExpressionBeforeArg( arg ) ;
		    expr.accept( context.current() ) ;
		}
	    } finally {
		postSuperCallExpression( arg ) ;
	    }
	}
    }

    // SuperObjExpression
    public boolean preSuperObjExpression( ExpressionFactory.SuperObjExpression arg ) {
	return preExpression( arg ) ;
    }

    public void superObjExpressionBeforeArg( ExpressionFactory.SuperObjExpression arg ) {
    }

    public void postSuperObjExpression( ExpressionFactory.SuperObjExpression arg ) {
	postExpression( arg ) ;
    }

    public void visitSuperObjExpression( ExpressionFactory.SuperObjExpression arg ) {
	if (preSuperObjExpression( arg )) {
	    try {
		for (Expression expr : arg.exprs()) {
		    superObjExpressionBeforeArg( arg ) ;
		    expr.accept( context.current() ) ;
		}
	    } finally {
		postSuperObjExpression( arg ) ;
	    }
	}
    }

    // ThisObjExpression
    public boolean preThisObjExpression( ExpressionFactory.ThisObjExpression arg ) {
	return preExpression( arg ) ;
    }

    public void postThisObjExpression( ExpressionFactory.ThisObjExpression arg ) {
	postExpression( arg ) ;
    }

    public void thisObjExpressionBeforeArg( ExpressionFactory.ThisObjExpression arg ) {
    }

    public void visitThisObjExpression( ExpressionFactory.ThisObjExpression arg ) {
	if (preThisObjExpression( arg )) {
	    try {
		for (Expression expr : arg.exprs()) {
		    thisObjExpressionBeforeArg( arg ) ;
		    expr.accept( context.current() ) ;
		}
	    } finally {
		postThisObjExpression( arg ) ;
	    }
	}
    }

    // NonStaticFieldAccessExpression
    public boolean preNonStaticFieldAccessExpression( 
	ExpressionFactory.NonStaticFieldAccessExpression arg ) {
	return preExpression( arg ) ;
    }

    public void postNonStaticFieldAccessExpression( 
	ExpressionFactory.NonStaticFieldAccessExpression arg ) {
	postExpression( arg ) ;
    }

    public void visitNonStaticFieldAccessExpression( 
	ExpressionFactory.NonStaticFieldAccessExpression arg ) {
	if (preNonStaticFieldAccessExpression( arg )) {
	    try {
		arg.target().accept( context.current() ) ;
	    } finally {
		postNonStaticFieldAccessExpression( arg ) ;
	    }
	}
    }

    // StaticFieldAccessExpression
    public boolean preStaticFieldAccessExpression( 
	ExpressionFactory.StaticFieldAccessExpression arg ) {
	return preExpression( arg ) ;
    }

    public void postStaticFieldAccessExpression( 
	ExpressionFactory.StaticFieldAccessExpression arg ) {
	postExpression( arg ) ;
    }

    public void visitStaticFieldAccessExpression( 
	ExpressionFactory.StaticFieldAccessExpression arg ) {
	if (preStaticFieldAccessExpression( arg )) {
	    postStaticFieldAccessExpression( arg ) ;
	}
    }

    // ArrayIndexExpression
    public boolean preArrayIndexExpression( ExpressionFactory.ArrayIndexExpression arg ) {
	return preExpression( arg ) ;
    }

    public void arrayIndexExpressionBeforeExpr( ExpressionFactory.ArrayIndexExpression arg ) {
    }

    public void postArrayIndexExpression( ExpressionFactory.ArrayIndexExpression arg ) {
	postExpression( arg ) ;
    }

    public void visitArrayIndexExpression( ExpressionFactory.ArrayIndexExpression arg ) {
	if (preArrayIndexExpression( arg )) {
	    try {
		arg.index().accept( context.current() ) ;
		arrayIndexExpressionBeforeExpr( arg ) ;
		arg.expr().accept( context.current() ) ;
	    } finally {
		postArrayIndexExpression( arg ) ;
	    }
	}
    }

    // ArrayLengthExpression
    public boolean preArrayLengthExpression( ExpressionFactory.ArrayLengthExpression arg ) {
	return preExpression( arg ) ;
    }

    public void postArrayLengthExpression( ExpressionFactory.ArrayLengthExpression arg ) {
	postExpression( arg ) ;
    }

    public void visitArrayLengthExpression( ExpressionFactory.ArrayLengthExpression arg ) {
	if (preArrayLengthExpression( arg )) {
	    try {
		arg.expr().accept( context.current() ) ;
	    } finally {
		postArrayLengthExpression( arg ) ;
	    }
	}
    }
    
    // IfExpression
    public boolean preIfExpression( ExpressionFactory.IfExpression arg ) {
	return preExpression( arg ) ;
    }

    public void ifExpressionBeforeTruePart( ExpressionFactory.IfExpression arg ) {
    }

    public boolean ifExpressionBeforeFalsePart( ExpressionFactory.IfExpression arg ) {
	return true ;
    }

    public void postIfExpression( ExpressionFactory.IfExpression arg ) {
	postExpression( arg ) ;
    }

    public void visitIfExpression( ExpressionFactory.IfExpression arg ) {
	if (preIfExpression( arg )) {
	    try {
		arg.condition().accept( context.current() ) ;
		ifExpressionBeforeTruePart( arg ) ;
	        arg.truePart().accept( context.current() ) ;
		if (ifExpressionBeforeFalsePart( arg )) 
		    arg.falsePart().accept( context.current() ) ;
	    } finally {
		postIfExpression( arg ) ;
	    }
	}
    }
    
}
