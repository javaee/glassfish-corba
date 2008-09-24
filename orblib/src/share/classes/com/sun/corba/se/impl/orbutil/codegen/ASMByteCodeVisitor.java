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


import java.io.PrintStream ;

import java.util.Map ;

import org.objectweb.asm.ClassWriter ;

import com.sun.corba.se.spi.orbutil.generic.Pair ;

import com.sun.corba.se.spi.orbutil.codegen.Expression ;
import com.sun.corba.se.spi.orbutil.codegen.Type ;
import com.sun.corba.se.spi.orbutil.codegen.Variable ;

import com.sun.corba.se.impl.orbutil.codegen.ASMUtil ;
import com.sun.corba.se.impl.orbutil.codegen.ByteCodeUtility ;
import com.sun.corba.se.impl.orbutil.codegen.AssignmentStatement ;
import com.sun.corba.se.impl.orbutil.codegen.BlockStatement ;
import com.sun.corba.se.impl.orbutil.codegen.CaseBranch ;
import com.sun.corba.se.impl.orbutil.codegen.ClassGenerator ;
import com.sun.corba.se.impl.orbutil.codegen.DefinitionStatement ;
import com.sun.corba.se.impl.orbutil.codegen.ExpressionFactory ;
import com.sun.corba.se.impl.orbutil.codegen.IfStatement ;
import com.sun.corba.se.impl.orbutil.codegen.MethodGenerator ;
import com.sun.corba.se.impl.orbutil.codegen.Node ;
import com.sun.corba.se.impl.orbutil.codegen.ReturnStatement ;
import com.sun.corba.se.impl.orbutil.codegen.Statement ;
import com.sun.corba.se.impl.orbutil.codegen.SwitchStatement ;
import com.sun.corba.se.impl.orbutil.codegen.ThrowStatement ;
import com.sun.corba.se.impl.orbutil.codegen.TreeWalker ;
import com.sun.corba.se.impl.orbutil.codegen.TreeWalkerContext ;
import com.sun.corba.se.impl.orbutil.codegen.TryStatement ;
import com.sun.corba.se.impl.orbutil.codegen.FieldGenerator ;
import com.sun.corba.se.impl.orbutil.codegen.WhileStatement ;

/** Visitor that is used to generate byte code for a class.
 *  SetupVisitor must be called first before this 
 *  visitor can be called.
 *  <P>
 *  This visitor is also responsible for setting up the
 *  bytecode versions of Variables, since we always define
 *  variables before we reference them.
 */
public class ASMByteCodeVisitor extends TreeWalker {
   
    private ClassWriter cw ;
    private ByteCodeUtility bcu ;
    private boolean debug ;
    private PrintStream ps ;

    public ASMByteCodeVisitor( TreeWalkerContext context, ClassWriter cw ) {
	this( context, cw, false, System.out ) ;
    }

    public ASMByteCodeVisitor( TreeWalkerContext context, 
	ClassWriter cw, boolean debug, PrintStream ps ) {
	super( context ) ;
	context.push( this ) ;
	this.cw = cw ;
	this.debug = debug ;
	this.ps = ps ;
    }

    private <T> T findNode( Class<T> cls, Node arg ) {
	Node current = arg ;
	while ((current != null) && !(cls.isInstance(current)))
	    current = current.parent() ;
	assert current != null ;
	return cls.cast( current ) ;
    }

    private MethodGenerator findMethodGenerator( Node arg ) {
	MethodGenerator mg = findNode( MethodGenerator.class, arg ) ;
	assert mg != null ;
	return mg ;
    }

    private ClassGenerator findClassGenerator( Node arg ) {
	ClassGenerator cg = findNode( ClassGenerator.class, arg ) ;
	assert cg != null ;
	return cg ;
    }

    // Returns the label of the next node (representing the next
    // executable code) to execute after the code represented by Node.
    static MyLabel nextLabel( Node node ) {
	MyLabel result = null ;
	Node pos = node ;
	do {
	    // This method should only be called with children of
	    // MethodGenerator, and should not ascend further
	    // than the MethodGenerator in the tree.
	    assert pos != null ;

	    // next is non-null for statements in BlockStatement
	    // (except for the last statement in the list).
	    // For most other node types, next is null.
	    Node next = ASMUtil.next.get( pos ) ;
	    if (next == null) {
		pos = pos.parent() ;
	    } else {
		return ASMUtil.statementStartLabel.get( next ) ;
	    }
	} while (!(pos instanceof MethodGenerator)) ;

	// If we are here, pos is an instanceof MethodGenerator,
	// and so it has a returnLabel, which is the appropriate nextLabel.
	return ASMUtil.returnLabel.get( pos ) ;
    }

    // Node
    @Override
    public boolean preNode( Node arg ) {
	return true ;
    }

    @Override
    public void postNode( Node arg ) {
    }

    // ClassGenerator
    @Override
    public boolean preClassGenerator( ClassGenerator arg ) {
	bcu = new ByteCodeUtility( cw, arg, debug, ps ) ;
	return true ;
    }

    @Override
    public boolean classGeneratorBeforeFields( ClassGenerator arg ) {
	// Just make sure the fields get visited.
	return true ;
    }

    @Override
    public void classGeneratorBeforeInitializer( ClassGenerator arg ) {
	// XXX need to setup the <clinit> method here
    }

    @Override
    public void classGeneratorBeforeMethod( ClassGenerator arg ) {
    }

    @Override
    public void classGeneratorBeforeConstructor( ClassGenerator arg ) {
    }

    @Override
    public void postClassGenerator( ClassGenerator arg ) {
	postNode( arg ) ;
    }
   
    // FieldGenerator (in class)
    @Override
    public boolean preFieldGenerator( FieldGenerator arg ) {
	bcu.addField( arg ) ;
	return true ;
    }

    // MethodGenerator
    @Override
    public boolean preMethodGenerator( MethodGenerator arg ) {
	// XXX Is there anything different about handling constructors here?
	// Every constructor must start with a call to super() or this(),
	// but do we do this here?  I think that may be better handled
	// at a higher layer.
	
	bcu.emitMethodStart( arg ) ;
	for (Variable var : arg.arguments())
	    recordVariable( var ) ;
	return true ;
    }

    @Override
    public boolean methodGeneratorBeforeArguments( MethodGenerator arg ) {
	// Don't need to visit arguments; they are handled in preMethodGenerator.
	return false ;
    }

    @Override
    public void postMethodGenerator( MethodGenerator arg ) {
	bcu.emitMethodEnd( arg, ASMUtil.returnLabel.get(arg),
	    ASMUtil.returnVariable.get(arg), debug ) ;
    }

    // Statement
    @Override
    public boolean preStatement( Statement arg ) {
	bcu.emitLabel( ASMUtil.statementStartLabel, arg ) ;
	return true ;
    }

    @Override
    public void postStatement( Statement arg ) {
	bcu.emitLabel( ASMUtil.statementEndLabel, arg ) ;
	postNode( arg ) ;
    }

    // ThrowStatement
    @Override
    public boolean preThrowStatement( ThrowStatement arg ) {
	return preStatement( arg ) ;
    }

    @Override
    public void postThrowStatement( ThrowStatement arg ) {
	bcu.emitThrow() ;
	postStatement( arg ) ;
    }

    // AssignmentStatement
    @Override
    public boolean preAssignmentStatement( AssignmentStatement arg ) {
	// There are several cases here:
	// 1. LHS is a local variable.
	// 2. LHS is a static field reference.
	// 3. LHS is a non-static field reference.
	// 4. LHS is an array reference.
	// It is easy to get confused between variable references and
	// field references, since many fields references are syntactically
	// the same in java as variable references.  Do not make this
	// mistake!  Variables are always accessed with xLOAD/xSTORE
	// instructions with a stack frame offset.  Fields are always
	// accessed with [PUT/GET][FIELD/STATIC] instructions with 
	// symbolic locations.  It is only on the LHS of an assignment that
	// this is really important, because the code generation needs to
	// carefully take this into account.
	Expression lhs = arg.left() ;
	Expression rhs = arg.right() ;

	assert lhs.isAssignable() ;
   
	if (lhs instanceof Variable) {
	    rhs.accept( context.current() ) ;
	} else if (lhs instanceof ExpressionFactory.NonStaticFieldAccessExpression) {
	    ExpressionFactory.NonStaticFieldAccessExpression expr = 
		ExpressionFactory.NonStaticFieldAccessExpression.class.cast( lhs ) ;
	    expr.target().accept( context.current() ) ;
	    rhs.accept( context.current() ) ;
	} else if (lhs instanceof ExpressionFactory.StaticFieldAccessExpression) {
	    rhs.accept( context.current() ) ;
	} else if (lhs instanceof ExpressionFactory.ArrayIndexExpression) {
	    ExpressionFactory.ArrayIndexExpression expr = 
		ExpressionFactory.ArrayIndexExpression.class.cast( lhs ) ;
	    expr.expr().accept( context.current() ) ;
	    expr.index().accept( context.current() ) ;
	    rhs.accept( context.current() ) ;
	} else {
	    throw new IllegalArgumentException( 
		"ASMByteCodeVisitor.preAssignmentStatement called with " +
		"illegal left expression " + lhs ) ;
	}

	EmitterFactory.Emitter emitter = ASMUtil.emitter.get( lhs ) ;
	bcu.callEmitter( emitter ) ;

	preStatement( arg ) ;
	return false ;
    }

    @Override
    public void assignmentStatementBeforeLeftSide( AssignmentStatement arg ) {
	// NOP
    }

    @Override
    public void postAssignmentStatement( AssignmentStatement arg ) {
	// NOP
    }

    // BlockStatement
    @Override
    public boolean preBlockStatement( BlockStatement arg ) {
	// Make sure that this BlockStatement's lastStatement
	// attribute is clear, so there is no confusion in
	// blockStatementBeforeBodyStatement.
	ASMUtil.lastStatement.set( arg, null ) ;
	return preStatement( arg ) ;
    }

    @Override
    public void blockStatementBeforeBodyStatement( BlockStatement arg, Statement stmt ) {
	Statement lastStatement = ASMUtil.lastStatement.get( arg ) ;
	popIfNeeded( lastStatement ) ;
	ASMUtil.lastStatement.set( arg, stmt ) ;
    }

    @Override
    public void postBlockStatement( BlockStatement arg ) {
	Statement lastStatement = ASMUtil.lastStatement.get( arg ) ;
	popIfNeeded( lastStatement ) ;
	postStatement( arg ) ;
    }

    private void popIfNeeded( Statement lastStatement ) {
	if (lastStatement != null) {
	    // If lastStatement is an expression which does not have void type, 
	    // it will generate code that leaves one value on the stack.  
	    // That's fine if arg represents the conditional in an if or while, 
	    // or if the result of arg is an input to another expression.
	    // However, here such a value must be popped, because it is the 
	    // result of an expression that was executed only for its side-effects.
	    if (lastStatement instanceof Expression) {
		Expression expr = Expression.class.cast( lastStatement ) ;
		if (!expr.type().equals( Type._void() )) 
		    bcu.emitPop() ;
	    }
	}
    }

    // CaseBranch
    @Override
    public boolean preCaseBranch( CaseBranch arg ) {
	return preBlockStatement( arg ) ;
    }

    @Override
    public void caseBranchBeforeBodyStatement( CaseBranch arg ) {
	// NOP: just visit the children
    }

    @Override
    public void postCaseBranch( CaseBranch arg ) {
	// NOP: just visit the children
	postBlockStatement( arg ) ;
    }
    
    // DefinitionStatement
    @Override
    public boolean preDefinitionStatement( DefinitionStatement arg ) {
	preStatement( arg ) ;

	// TreeWalker visits arg.var() then arg.expr(), which is wrong here,
	// so we will do the visiting in this method directly.
	arg.expr().accept( context.current() ) ;
	arg.var().accept( context.current() ) ;
	recordVariable( arg.var() ) ;	
	return false ;
    }

    @Override
    public boolean definitionStatementBeforeExpr( DefinitionStatement arg ) {
	// NOP
	return false ;
    }

    @Override
    public void postDefinitionStatement( DefinitionStatement arg ) {
	postStatement( arg ) ;
    }

    // IfStatement.  Note that arg.truePart and arg.falsePart are
    // always BlockStatement instances.  We will supress the
    // falsePart if it is empty.
    @Override
    public boolean preIfStatement( IfStatement arg ) {
	return preStatement( arg ) ;
    }

    @Override
    public void ifStatementBeforeTruePart( IfStatement arg ) {
	// branch if TOS is false to statementStartLabel of false branch
	bcu.emitConditionalBranch( ASMUtil.statementStartLabel.get( arg.falsePart() ) ) ; 
    }

    @Override
    public boolean ifStatementBeforeFalsePart( IfStatement arg ) {
	bcu.emitBranch( nextLabel( arg ) ) ;
	bcu.emitLabel( ASMUtil.statementStartLabel, arg.falsePart() ) ;
	return true ;
    }

    @Override
    public void postIfStatement( IfStatement arg ) {
	postStatement( arg ) ;
    }

    private void emitJsrToFinallyBlock( TryStatement stmt ) {
	BlockStatement fb = stmt.finalPart() ;
	if (!fb.isEmpty()) {
	    bcu.emitJsr( ASMUtil.statementStartLabel.get( fb ) ) ;
	}
    }

    // emit JSR's to enclosing finally blocks, innermost first.
    private void callFinallyBlocks( Node arg ) {
	Node current = arg ;
	while (current != null) {
	    if (current instanceof TryStatement) {
		emitJsrToFinallyBlock( TryStatement.class.cast( current ) ) ;
	    }

	    current = current.parent() ;
	}
    }

    // BreakStatement
    @Override
    public boolean preBreakStatement( BreakStatement arg ) {
	preStatement( arg ) ;

	return true ;
    }

    @Override
    public void postBreakStatement( BreakStatement arg ) {
	Node current = arg.parent() ;
	boolean foundBreak = false ;
	while (current != null) {
	    if (current instanceof TryStatement) {
		// Must call finally blocks on all enclosing try statements
		emitJsrToFinallyBlock( TryStatement.class.cast( current ) ) ;
	    } else if (current instanceof SwitchStatement) {
		// Branch past end of switch statement
		bcu.emitBranch( nextLabel( current ) ) ;
	    } else if (current instanceof WhileStatement) {
		// Branch past end of while statement
		bcu.emitBranch( nextLabel( current ) ) ;
	    }

	    current = current.parent() ;
	}

	postStatement( arg ) ;
    }

    // ReturnStatement
    @Override
    public boolean preReturnStatement( ReturnStatement arg ) {
	preStatement( arg ) ;

	return true ;
    }

    @Override
    public void postReturnStatement( ReturnStatement arg ) {
	MethodGenerator mg = findMethodGenerator( arg ) ;
	Variable var = ASMUtil.returnVariable.get( mg ) ;
	if (var != null)
	    bcu.callEmitter( ASMUtil.setEmitter.get( var ) ) ;
	
	callFinallyBlocks( arg ) ;
	
	bcu.emitBranch( ASMUtil.returnLabel.get( mg ) ) ;

	postStatement( arg ) ;
    }

    // SwitchStatement
    @Override
    public boolean preSwitchStatement( SwitchStatement arg ) {
	preStatement( arg ) ;
	// XXX implement me
	// Here we will avoid using TreeWalker completely.
	// Sketch:
	// 1. Scan branches and add an attribute nextBranch to all
	//    branches that fall through (that is, the last statement
	//    in the branch is not a break, throw, or return).
	// 2. Create a list of Pair<Integer,Label> that maps each
	//    case value to the label of the corresponding branch.
	// 3. Sort the list by first elements of the Pairs.
	// 4. If the density of the tables is at least .5, 
	//    generate a tableswitch, else generate a lookupswitch.
	//    Note that missing values in a tableswitch either branch
	//    to default, or next(switch) if there is no default.
	//    Note that branching to next(switch) may require calling
	//    finally blocks.
	// 5. Visit the branches (and label them) to generate the branch
	//    code.

	postStatement( arg ) ;
	return false ;
    }

    @Override
    public boolean switchStatementBeforeCaseBranches( SwitchStatement arg ) {
	// not called
	return true ;
    }

    @Override
    public boolean switchStatementBeforeDefault( SwitchStatement arg ) {
	// not called 
	return true ;
    }

    @Override
    public void postSwitchStatement( SwitchStatement arg ) {
	// not called
    }

    // TryStatement
    @Override
    public boolean preTryStatement( TryStatement arg ) {
	// Make sure statementStartLabel is emitted by preStatement
	ASMUtil.statementStartLabel.get( arg.bodyPart() ) ;

	ASMUtil.lastBlock.set( arg, arg.bodyPart() ) ;
	return preStatement( arg ) ;
    }

    @Override
    public void tryStatementBeforeBlock( TryStatement arg,
	Type type, Variable var, BlockStatement block ) {
    
	finishLastBlock( arg ) ;
	ASMUtil.lastBlock.set( arg, block ) ;
	
	// Start codegen for new block
	
	// emit label for handler.  Note that this means
	// that preStatement on the block will NOT emit
	// the label when the block is visited.
	ASMUtil.statementStartLabel.get( block ) ;
	bcu.emitLabel( ASMUtil.statementStartLabel, block ) ;
	
	// emit store of exception into var
	bcu.callEmitter( ASMUtil.setEmitter.get( var ) ) ;

	recordVariable( var ) ;
    }

    private void finishLastBlock( TryStatement arg ) {
	// emit label at end of previous block. If
	// there is a final part, JSR to it, then emit 
	// branch to whatever is after the try statement.
	BlockStatement lastBlock = ASMUtil.lastBlock.get( arg ) ;
	ASMUtil.throwEndLabel.get( lastBlock ) ;
	bcu.emitLabel( ASMUtil.throwEndLabel, lastBlock ) ;
	if (!arg.finalPart().isEmpty()) {
	    bcu.emitJsr( ASMUtil.statementStartLabel.get( arg.finalPart() ) ) ;
	}
	bcu.emitBranch( nextLabel( arg ) ) ;
    }

    @Override
    public boolean tryStatementBeforeFinalPart( TryStatement arg ) {
	
	finishLastBlock( arg ) ;

	if (!arg.finalPart().isEmpty()) {
	    // emit handler for uncaught exception:
	    ASMUtil.uncaughtExceptionHandler.get( arg ) ;
	    bcu.emitLabel( ASMUtil.uncaughtExceptionHandler, arg ) ;

	    Variable uncaughtException = ASMUtil.uncaughtException.get( arg ) ;
	    bcu.callEmitter( ASMUtil.setEmitter.get( uncaughtException ) ) ;
	    bcu.emitJsr( ASMUtil.statementStartLabel.get( arg.finalPart() ) ) ;
	    bcu.callEmitter( ASMUtil.getEmitter.get( uncaughtException ) ) ;
	    bcu.emitThrow() ;

	    // Start finally code: store TOS into return address
	    ASMUtil.statementStartLabel.get( arg.finalPart() ) ;
	    bcu.emitLabel( ASMUtil.statementStartLabel, arg.finalPart() ) ;
	    Variable ra = ASMUtil.returnAddress.get( arg ) ;
	    bcu.callEmitter( ASMUtil.setEmitter.get( ra ) ) ;
	}

	return true ;
    }

    @Override
    public void postTryStatement( TryStatement arg ) {
	if (!arg.finalPart().isEmpty()) {
	    // emit return to stored RA
	    Variable ra = ASMUtil.returnAddress.get( arg ) ;
	    bcu.emitRet( ra ) ;
	}

	// Emit handler table entries
	// We need an entry for the try block for each exception handler
	MyLabel start = ASMUtil.statementStartLabel.get( arg.bodyPart() ) ;
	MyLabel end = ASMUtil.throwEndLabel.get( arg.bodyPart() ) ;

	for (Map.Entry<Type,Pair<Variable,BlockStatement>> entry : 
	    arg.catches().entrySet()) {
	    Pair<Variable,BlockStatement> pair = entry.getValue() ;
	    MyLabel handler = ASMUtil.statementStartLabel.get( pair.second() ) ;
	    bcu.emitExceptionTableEntry( start, end, handler,
		pair.first().type() ) ;
	}

	if (!arg.finalPart().isEmpty()) {
	    // The uncaught exception handler applies to the entire
	    // try-catch statement except for the uncaught exception
	    // handler and the finally block itself.
	    end = ASMUtil.uncaughtExceptionHandler.get( arg ) ;
	    bcu.emitExceptionTableEntry( start, end, end, null ) ;
	}

	postStatement( arg ) ;
    }

    // WhileStatement
    @Override
    public boolean preWhileStatement( WhileStatement arg ) {
	// We always need the start label for the branch at the end
	ASMUtil.statementStartLabel.get( arg ) ;
	bcu.emitLabel( ASMUtil.statementStartLabel, arg ) ;
	return preStatement( arg ) ;
    }

    @Override
    public void whileStatementBeforeBody( WhileStatement arg ) {
	// branch if TOS is false to next statement after loop. 
	bcu.emitConditionalBranch( nextLabel( arg ) ) ; 
    }

    @Override
    public void postWhileStatement( WhileStatement arg ) {
	bcu.emitBranch( ASMUtil.statementStartLabel.get( arg ) ) ;
	postStatement( arg ) ;
    }

    // Expression
    @Override
    public boolean preExpression( Expression arg ) {
	return preStatement( arg ) ;
    }

    @Override
    public void postExpression( Expression arg ) {
	postStatement( arg ) ;
    }

    private void recordVariable( Variable var ) {
	MethodGenerator mg = var.getAncestor( MethodGenerator.class ) ;
	if (mg == null)
	    throw new IllegalStateException(
		"No MethodGenerator found!" ) ;
	ASMUtil.VariablesInMethod vm = ASMUtil.variablesInMethod.get( mg ) ;
	vm.add( var ) ;
    }

    // Variable
    @Override
    public boolean preVariable( Variable arg ) {
	if (ASMUtil.emitter.isSet( arg ))
	    bcu.callEmitter( ASMUtil.emitter.get( arg ) ) ;
	return preExpression( arg ) ;
    }

    @Override
    public void postVariable( Variable arg ) {
	postExpression( arg ) ;
    }

    // ConstantExpression
    @Override
    public boolean preConstantExpression( ExpressionFactory.ConstantExpression arg ) {
	bcu.emitConstantExpression( arg.type(), arg.value() ) ;
	return preExpression( arg ) ;
    }

    @Override
    public void postConstantExpression( ExpressionFactory.ConstantExpression arg ) {
	postExpression( arg ) ;
    }

    // VoidExpression
    @Override
    public boolean preVoidExpression( ExpressionFactory.VoidExpression arg ) {
	return preExpression( arg ) ;
    }

    @Override
    public void postVoidExpression( ExpressionFactory.VoidExpression arg ) {
	postExpression( arg ) ;
    }

    // ThisExpression
    @Override
    public boolean preThisExpression( ExpressionFactory.ThisExpression arg ) {
	bcu.emitThisExpression() ;
	return preExpression( arg ) ;
    }

    @Override
    public void postThisExpression( ExpressionFactory.ThisExpression arg ) {
	postExpression( arg ) ;
    }

    // UnaryOperatorExpression
    @Override
    public boolean preUnaryOperatorExpression( ExpressionFactory.UnaryOperatorExpression arg ) {
	// XXX implement me
	return preExpression( arg ) ;
    }

    @Override
    public void postUnaryOperatorExpression( ExpressionFactory.UnaryOperatorExpression arg ) {
	// XXX implement me
	postExpression( arg ) ;
    }

    // BinaryOperatorExpression
    @Override
    public boolean preBinaryOperatorExpression( ExpressionFactory.BinaryOperatorExpression arg ) {
	return preExpression( arg ) ;
    }

    @Override
    public void binaryOperatorExpressionBeforeRight( 
	ExpressionFactory.BinaryOperatorExpression arg ) {
	// XXX if coercion is required AND arg.left type is not the required coercion type, emit coercion
    }

    @Override
    public void postBinaryOperatorExpression( ExpressionFactory.BinaryOperatorExpression arg ) {
	postExpression( arg ) ;
	// XXX if coercion is required AND arg.right type is not the required coercion type, emit coercion
	bcu.emitBinaryOperator( arg ) ;
    }

    // CastExpression
    @Override
    public boolean preCastExpression( ExpressionFactory.CastExpression arg ) {
	return preExpression( arg ) ;
    }

    @Override
    public void postCastExpression( ExpressionFactory.CastExpression arg ) {
	bcu.emitCast( arg.expr().type(), arg.type() ) ;

	postExpression( arg ) ;
    }

    // InstofExpression
    @Override
    public boolean preInstofExpression( ExpressionFactory.InstofExpression arg ) {
	return preExpression( arg ) ;
    }

    @Override
    public void postInstofExpression( ExpressionFactory.InstofExpression arg ) {
	bcu.emitInstanceof( arg.itype() ) ;
	
	postExpression( arg ) ;
    }

    // StaticCallExpression
    @Override
    public boolean preStaticCallExpression( ExpressionFactory.StaticCallExpression arg ) {
	// TreeWalker visists the target then the args, which
	// generates code that sets up the stack for the call.  The target
	// in this case is just a type, which generates no code.
	return preExpression( arg ) ;
    }

    @Override
    public void staticCallExpressionBeforeArg( ExpressionFactory.StaticCallExpression arg ) {
	// NOP
    }

    @Override
    public void postStaticCallExpression( ExpressionFactory.StaticCallExpression arg ) {
	bcu.emitStaticInvoke( arg.target(), arg.ident(), arg.signature() ) ;
	postExpression( arg ) ;
    }

    // NonStaticCallExpression
    // Expression arg.target()
    // String arg.ident()
    // Signature arg.signature()
    // List<Expression> args()
    @Override
    public boolean preNonStaticCallExpression( ExpressionFactory.NonStaticCallExpression arg ) {
	// TreeWalker visists the target then the args, which
	// generates code that sets up the stack for the call.
	return preExpression( arg ) ;
    }

    @Override
    public void nonStaticCallExpressionBeforeArg( ExpressionFactory.NonStaticCallExpression arg ) {
	// NOP
    }

    @Override
    public void postNonStaticCallExpression( ExpressionFactory.NonStaticCallExpression arg ) {
	bcu.emitInvoke( arg.target().type(), arg.ident(), arg.signature() ) ;
	postExpression( arg ) ;
    }

    // NewObjExpression
    @Override
    public boolean preNewObjExpression( ExpressionFactory.NewObjExpression arg ) {
	// Create the new object then duplicate it.
	bcu.emitNewCall( arg.type() ) ;
	
	// The arguments for the call are pushed by
	// code generated by the visitor.
	return preExpression( arg ) ;
    }

    @Override
    public void newObjExpressionBeforeArg( ExpressionFactory.NewObjExpression arg ) {
	// NOP
    }

    @Override
    public void postNewObjExpression( ExpressionFactory.NewObjExpression arg ) {
	// emit the required INVOKESPECIAL
	bcu.emitNewInvoke( arg.type(), arg.signature() ) ;

	postExpression( arg ) ;
    }

    // NewArrExpression
    // Type ctype()
    // Expression size()
    // List<Expression> exprs()
    // visit order: 
    //	preNewArrExpression 
    //	newArrExpressionBeforeSize 
    //	newArrExpressionBeforeExpression* 
    //	postNewArrExpression
    //	General plan:
    //	first, create array
    //	then, for each element expression:
    //	    emit dup
    //	    emit constant for index
    //	    emit code for expression
    //	    aastore
    @Override
    public boolean preNewArrExpression( ExpressionFactory.NewArrExpression arg ) {
	// init counter
	return preExpression( arg ) ;
    }

    @Override
    public void newArrExpressionAfterSize( ExpressionFactory.NewArrExpression arg ) {
	bcu.emitNewArrayCall( arg.ctype() ) ;
    }

    @Override
    public void newArrExpressionBeforeExpression( ExpressionFactory.NewArrExpression arg ) {
	bcu.emitDup() ;
	int ctr = ASMUtil.ctr.get( arg ) ;
	bcu.emitConstantExpression(Type._int(), ctr) ;
	ASMUtil.ctr.set( arg, ++ctr ) ;
    }

    @Override
    public void newArrExpressionAfterExpression( ExpressionFactory.NewArrExpression arg ) {
	bcu.emitArrayStore() ;
    }

    @Override
    public void postNewArrExpression( ExpressionFactory.NewArrExpression arg ) {
	postExpression( arg ) ;
    }

    // SuperCallExpression
    @Override
    public boolean preSuperCallExpression( ExpressionFactory.SuperCallExpression arg ) {
	bcu.emitThisExpression() ;
	return preExpression( arg ) ;
    }

    @Override
    public void superCallExpressionBeforeArg( ExpressionFactory.SuperCallExpression arg ) {
	// NOP
    }

    @Override
    public void postSuperCallExpression( ExpressionFactory.SuperCallExpression arg ) {
	Type superType = findClassGenerator( arg ).superType() ;
	bcu.emitSpecialInvoke( superType, arg.ident(), arg.signature() ) ;
	postExpression( arg ) ;
    }

    // SuperObjExpression (super at start of constructor)
    @Override
    public boolean preSuperObjExpression( ExpressionFactory.SuperObjExpression arg ) {
	bcu.emitThisExpression() ;
	return preExpression( arg ) ;
    }

    @Override
    public void superObjExpressionBeforeArg( ExpressionFactory.SuperObjExpression arg ) {
    }

    @Override
    public void postSuperObjExpression( ExpressionFactory.SuperObjExpression arg ) {
	Type superType = findClassGenerator( arg ).superType() ;
	bcu.emitNewInvoke( superType, arg.signature() ) ;
	postExpression( arg ) ;
    }

    // ThisObjExpression (this at start of constructor)
    @Override
    public boolean preThisObjExpression( ExpressionFactory.ThisObjExpression arg ) {
	bcu.emitThisExpression() ;
	return preExpression( arg ) ;
    }

    @Override
    public void thisObjExpressionBeforeArg( ExpressionFactory.ThisObjExpression arg ) {
	// NOP
    }

    @Override
    public void postThisObjExpression( ExpressionFactory.ThisObjExpression arg ) {
	Type type = findClassGenerator( arg ).thisType() ;
	bcu.emitNewInvoke( type, arg.signature() ) ;
	postExpression( arg ) ;
    }

    // Note that (Non)StaticFieldAccessExpression and ArrayIndexExpression can occur
    // on either the left or right side of an assignment statement (or in other contexts,
    // all of which are read accesses).  The AssignmentStatement code handles all of the 
    // write acess cases, so we only handle read accesses in the following code.

    // NonStaticFieldAccessExpression
    @Override
    public boolean preNonStaticFieldAccessExpression( 
	ExpressionFactory.NonStaticFieldAccessExpression arg ) {
	return preExpression( arg ) ;
    }

    @Override
    public void postNonStaticFieldAccessExpression( 
	ExpressionFactory.NonStaticFieldAccessExpression arg ) {
	EmitterFactory.Emitter emitter = ASMUtil.emitter.get( arg ) ;
	bcu.callEmitter( emitter ) ;
	postExpression( arg ) ;
    }

    // StaticFieldAccessExpression
    @Override
    public boolean preStaticFieldAccessExpression( 
	ExpressionFactory.StaticFieldAccessExpression arg ) {
	return preExpression( arg ) ;
    }

    @Override
    public void postStaticFieldAccessExpression( 
	ExpressionFactory.StaticFieldAccessExpression arg ) {
	EmitterFactory.Emitter emitter = ASMUtil.emitter.get( arg ) ;
	bcu.callEmitter( emitter ) ;
	postExpression( arg ) ;
    }

    // ArrayIndexExpression
    @Override
    public boolean preArrayIndexExpression( ExpressionFactory.ArrayIndexExpression arg ) {
	return preExpression( arg ) ;
    }

    @Override
    public void postArrayIndexExpression( ExpressionFactory.ArrayIndexExpression arg ) {
	EmitterFactory.Emitter emitter = ASMUtil.emitter.get( arg ) ;
	bcu.callEmitter( emitter ) ;
	postExpression( arg ) ;
    }

    // ArrayLengthExpression
    @Override
    public boolean preArrayLengthExpression( ExpressionFactory.ArrayLengthExpression arg ) {
	return preExpression( arg ) ;
    }

    @Override
    public void postArrayLengthExpression( ExpressionFactory.ArrayLengthExpression arg ) {
	postExpression( arg ) ;
	bcu.callEmitter( EmitterFactory.makeEmitter( arg ) ) ; 
    }
    
    // IfExpression.  
    @Override
    public boolean preIfExpression( ExpressionFactory.IfExpression arg ) {
	return preExpression( arg ) ;
    }

    @Override
    public void ifExpressionBeforeTruePart( ExpressionFactory.IfExpression arg ) {
	// branch if TOS is false to statementStartLabel of false branch
	bcu.emitConditionalBranch( ASMUtil.statementStartLabel.get( arg.falsePart() ) ) ; 
    }

    @Override
    public boolean ifExpressionBeforeFalsePart( ExpressionFactory.IfExpression arg ) {
	bcu.emitBranch( nextLabel( arg ) ) ;
	bcu.emitLabel( ASMUtil.statementStartLabel, arg.falsePart() ) ;
	return true ;
    }

    @Override
    public void postIfExpression( ExpressionFactory.IfExpression arg ) {
	postExpression( arg ) ;
    }
}
