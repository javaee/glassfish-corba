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

package com.sun.corba.se.impl.codegen;

import java.lang.reflect.Modifier ;

import java.util.Map ;
import java.util.Properties ;
import java.util.List ;
import java.util.Stack ;
import java.util.ArrayList ;

import org.objectweb.asm.ClassWriter ;
import org.objectweb.asm.FieldVisitor ;
import org.objectweb.asm.MethodVisitor ;
import org.objectweb.asm.ClassWriter ;

import static org.objectweb.asm.Opcodes.* ;

import com.sun.corba.se.spi.codegen.Signature ;
import com.sun.corba.se.spi.codegen.Expression ;
import com.sun.corba.se.spi.codegen.Type ;
import com.sun.corba.se.spi.codegen.Variable ;

import com.sun.corba.se.impl.codegen.AssignmentStatement ;
import com.sun.corba.se.impl.codegen.BlockStatement ;
import com.sun.corba.se.impl.codegen.CaseBranch ;
import com.sun.corba.se.impl.codegen.ClassGenerator ;
import com.sun.corba.se.impl.codegen.CodeGenerator ;
import com.sun.corba.se.impl.codegen.DefinitionStatement ;
import com.sun.corba.se.impl.codegen.ExpressionFactory ;
import com.sun.corba.se.impl.codegen.IfStatement ;
import com.sun.corba.se.impl.codegen.MethodGenerator ;
import com.sun.corba.se.impl.codegen.Node ;
import com.sun.corba.se.impl.codegen.ReturnStatement ;
import com.sun.corba.se.impl.codegen.BreakStatement ;
import com.sun.corba.se.impl.codegen.Statement ;
import com.sun.corba.se.impl.codegen.SwitchStatement ;
import com.sun.corba.se.impl.codegen.ThrowStatement ;
import com.sun.corba.se.impl.codegen.TreeWalker ;
import com.sun.corba.se.impl.codegen.TreeWalkerContext ;
import com.sun.corba.se.impl.codegen.TryStatement ;
import com.sun.corba.se.impl.codegen.FieldGenerator ;
import com.sun.corba.se.impl.codegen.Visitor ;
import com.sun.corba.se.impl.codegen.WhileStatement ;

// Visitor that creates the labels given by the 
// returnLabel .
// It also prepares Variables for code generation.
// The basic idea is that this visitor should set up (almost?)
// all of the required attributes that are processed in the
// byte code generator visitor.
// Another job here: perform all necessary numeric coercions
// by re-writing the tree.

/** Visitor that can prepare an AST for bytecode generation,
 * or verify that an AST has been correctly prepared.
 */
public class ASMSetupVisitor extends TreeWalker {
    public enum Mode { PREPARE, VERIFY } ;

    /** Defines the context in which a variable occurs.  This information
     * is used to properly annotate the Variable so that the correct code
     * can be generated.
     * <ul>
     * <li>REFERENCE is used whenever an occurrence of a Variable
     * (actually a VariableDelegateImpl) is simply a reference to an 
     * existing variable definition.
     * <li>DEFINE_LOCAL is used when a local variable is introduced in
     * a method.  Space must be allocated for locals, but no initialization
     * is required.
     * <li>DEFINE_LOCAL_DEFINITION is used when a local variable is 
     * introduced in a DefinitionStatement.  Space is allocated, and the 
     * value of the expression is stored in the variable.
     * </ul>
     */
    private enum VariableContext { REFERENCE, DEFINE_LOCAL, DEFINE_LOCAL_DEFINITION } ;

    /** Class used to represnt an AST verification error
     */
    public static class ErrorReport {
	// The node in which the error was detected
	public Node node ;

	// A description of the error
	public String msg ;
    }

    public List<ErrorReport> getVerificationErrors() {
	return errors ;
    }

    private void verificationError( Node node, String msg ) {
	ErrorReport report = new ErrorReport() ;
	report.node = node ;
	report.msg = msg ;
	errors.add( report ) ;
    }
    
    private VariableContext variableDefiningContext ;

    private Mode mode ;

    // SlotAllocator for current MethodGenerator, if any
    private SlotAllocator slotAllocator ; 

    // List of verification errors if running in verification mode.
    // Note that verification here simply means verifying that this
    // visitor has correctly annotated the AST.
    private List<ErrorReport> errors ;

    public ASMSetupVisitor( TreeWalkerContext context ) {
	this( context, Mode.PREPARE ) ;
    }

    public ASMSetupVisitor( TreeWalkerContext context, Mode mode ) {
	super( context ) ;
	context.push( this ) ;

	variableDefiningContext = VariableContext.REFERENCE ;
	this.mode = mode ;
	slotAllocator = null ;
	errors = new ArrayList<ErrorReport>() ;
    }

    private boolean preparing() {
	return mode == Mode.PREPARE ;
    }

    // Class used to allocate slots for local variables
    static class SlotAllocator {
	private static int id = 0 ;

	private int myId = id++ ;

	// Slot 0 always contains the "this" reference.
	private int current = 1 ; // next slot to allocate

	public int getSlot( Type type ) {
	    int result = current ;
	    current += type.size() ;
	    return result ;
	}

	public String toString() {
	    return "SlotAllocator(" + myId + ")[current=" + current + "]" ;
	}
    }

    @Override
    public boolean preClassGenerator( ClassGenerator arg ) {
	return true ;
    }

    // Make sure that all non-abstract methods have a return
    // label.  The returnLabel is also the next node for the last
    // statement in the method.  Also, make sure that all 
    // method arguments are properly allocated.
    @Override
    public boolean preMethodGenerator( MethodGenerator arg ) {
	slotAllocator = new SlotAllocator() ;
	for (Variable var : arg.arguments())
	    ASMUtil.requiredEmitterType.set( var, 
		ASMUtil.RequiredEmitterType.NONE ) ;
	return !Modifier.isAbstract( arg.modifiers() ) ;
    }

    @Override
    public boolean methodGeneratorBeforeArguments( MethodGenerator arg ) {
	variableDefiningContext = VariableContext.DEFINE_LOCAL ;
	return true ;
    }

    @Override
    public void methodGeneratorAfterArguments( MethodGenerator arg ) {
	variableDefiningContext = VariableContext.REFERENCE ;
    }

    @Override
    public void postMethodGenerator( MethodGenerator arg ) {
	// Lastly, allocate a slot to hold the return value, if not void.
	// This happens after all parameters and local variables have been
	// allocated.
	if (!arg.returnType().equals( Type._void() )) {
	    Variable var ;
	    if (preparing()) {
		var = arg.body().exprFactory().variable( 
		    arg.returnType(), "$$_returnVariable_$$" ) ;
		ASMUtil.returnVariable.set( arg, var ) ;
	    } else {
		var = ASMUtil.returnVariable.get( arg ) ;
	    }

	    defineLocalVariable( var ) ;
	}

	slotAllocator = null ;
    }

    // Variables are defined only in a few places:
    // 1. In MethodGenerator.  This includes arguments
    //    and a local Variable to hold the return value
    //    (if the return type is not void).
    // 2. In BlockStatement.
    // 3. In TryStatement (for catch branches).
    //    In addition, each finally clause requires a 
    //    Variable to hold any uncaught exceptions while
    //    processing the finally block.  We also need a
    //    local to hold a return address for the finally
    //    handler.
    //
    // Note that these cases are all handled the same way:
    // we just need to allocate a stack slot to hold
    // the variable.  This is done as follows:
    // 1. slot 0 is always "this"
    // 2. slots 1-n are used to hold parameters 1-n
    //    (but note that long and doubles take 2 slots)
    // 3. All subsequent slots are used to hold any other locals.
    //
    // This visitor needs to allocate all stack slots for 
    // local variables.  It also needs to allocate the
    // emitStore/emitLoad attributes for each Variable 
    // definition.  
    @Override
    public boolean classGeneratorBeforeFields( ClassGenerator arg ) {
	return true ;
    }

    @Override
    public void classGeneratorBeforeInitializer( ClassGenerator arg ) {
	variableDefiningContext = VariableContext.REFERENCE ;
    }

    @Override
    public void classGeneratorBeforeMethod( ClassGenerator arg ) {
	variableDefiningContext = VariableContext.REFERENCE ;
    }

    @Override
    public void classGeneratorBeforeConstructor( ClassGenerator arg ) {
	variableDefiningContext = VariableContext.REFERENCE ;
    }

    @Override
    public void postClassGenerator( ClassGenerator arg ) {
	variableDefiningContext = VariableContext.REFERENCE ;
    }

    // BlockStatement
    @Override
    public boolean preBlockStatement( BlockStatement arg ) {
	ASMUtil.lastStatement.set( arg, null ) ;
	return true ;
    }

    @Override
    public void blockStatementBeforeBodyStatement( BlockStatement arg, Statement stmt ) {
	Statement lastStatement = ASMUtil.lastStatement.get( arg ) ;
	if (lastStatement != null)
	    ASMUtil.next.set( lastStatement, stmt ) ;
	ASMUtil.lastStatement.set( arg, stmt ) ;
    }

    @Override
    public void postBlockStatement( BlockStatement arg ) {
	Statement lastStatement = ASMUtil.lastStatement.get( arg ) ;
    }
    
    // DefinitionStatement
    @Override
    public boolean preDefinitionStatement( DefinitionStatement arg ) {
	variableDefiningContext = VariableContext.DEFINE_LOCAL_DEFINITION ;
	if (preparing()) {
	    ASMUtil.requiredEmitterType.set( arg.var(), 
		ASMUtil.RequiredEmitterType.SETTER ) ; 
	} else {
	    if (ASMUtil.requiredEmitterType.get(arg.var()) != 
		ASMUtil.RequiredEmitterType.SETTER )
		verificationError( arg, 
		    "Variable of definition statement should have requiredEmitterType true" ) ;
	}
	return true ;
    }

    @Override
    public boolean definitionStatementBeforeExpr( DefinitionStatement arg ) {
	variableDefiningContext = VariableContext.REFERENCE ;
	return true ;
    }

    @Override
    public boolean preTryStatement( TryStatement arg ) {
	if (!arg.finalPart().isEmpty()) {
	    // Allocate local variables for the uncaught
	    // exception and the return address.
	    Variable ucVar = arg.bodyPart().exprFactory().variable( 
		Type._class( "java.lang.Throwable" ), 
		"$$_uncaughtException_$$" ) ;
	    defineLocalVariable( ucVar ) ;
	    ASMUtil.uncaughtException.set( arg, ucVar ) ;

	    Variable raVar = arg.bodyPart().exprFactory().variable( Type._Object(),
		"$$_returnAddress_$$" ) ;
	    defineLocalVariable( raVar ) ;
	    ASMUtil.returnAddress.set( arg, raVar ) ;
	}

	return true ;
    }

    // This is a defining context for Variable var, so mark and
    // process it as such.  If this try statement has a finally
    // block, we also need two additional local variables:
    // One that can contains the return address for a JSR to
    // a finally block, and another for the uncaught exception
    // handler (catch uncaught exception, JSR to finally, and
    // re-throw the exception).
    @Override
    public void tryStatementBeforeBlock( TryStatement arg,
	Type type, Variable var, BlockStatement block ) {

	ASMUtil.requiredEmitterType.set( var, 
	    ASMUtil.RequiredEmitterType.NONE ) ;
	defineLocalVariable( var ) ;
    }

    @Override
    public boolean tryStatementBeforeFinalPart( TryStatement arg ) {
	return true ;
    }

    @Override
    public void postTryStatement( TryStatement arg ) {
    }

    @Override
    public boolean preAssignmentStatement( AssignmentStatement arg ) {
	Expression left = arg.left() ;
	assert left.isAssignable() ;
	Expression right = arg.right() ;

	if (preparing()) {
	    ASMUtil.requiredEmitterType.set( left,
		ASMUtil.RequiredEmitterType.SETTER ) ; 
	} else {
	    if (ASMUtil.requiredEmitterType.get(left) != 
		ASMUtil.RequiredEmitterType.SETTER )
		verificationError( arg, 
		    "Left side of assignment statement should have requiredEmitterType SETTER" ) ;
	}

	return true ;
    }

    @Override
    public boolean preNonStaticFieldAccessExpression( 
	ExpressionFactory.NonStaticFieldAccessExpression arg ) {
	initializeEmitter( arg ) ;
	return true ;
    }

    @Override
    public boolean preStaticFieldAccessExpression( 
	ExpressionFactory.StaticFieldAccessExpression arg ) {
	initializeEmitter( arg ) ;
	return true ;
    }

    @Override
    public boolean preArrayIndexExpression( 
	ExpressionFactory.ArrayIndexExpression arg ) {
	initializeEmitter( arg ) ;
	return true ;
    }

    // Note that the only difference between the three versions
    // of the initializeEmitter method is that the makeEmitter
    // call is also overloaded based on the static argument type.

    private void initializeEmitter( 
	ExpressionFactory.NonStaticFieldAccessExpression arg ) {
	ASMUtil.RequiredEmitterType ret = 
	    ASMUtil.requiredEmitterType.get( arg ) ;
	EmitterFactory.Emitter em = null ;
	if (ret != ASMUtil.RequiredEmitterType.NONE)
	    em = EmitterFactory.makeEmitter( arg, 
		ret == ASMUtil.RequiredEmitterType.SETTER ) ;
	handleEmitter( arg, em ) ;
    }

    private void initializeEmitter( 
	ExpressionFactory.StaticFieldAccessExpression arg ) {
	ASMUtil.RequiredEmitterType ret = 
	    ASMUtil.requiredEmitterType.get( arg ) ;
	EmitterFactory.Emitter em = null ;
	if (ret != ASMUtil.RequiredEmitterType.NONE)
	    em = EmitterFactory.makeEmitter( arg, 
		ret == ASMUtil.RequiredEmitterType.SETTER ) ;
	handleEmitter( arg, em ) ;
    }

    private void initializeEmitter(
	ExpressionFactory.ArrayIndexExpression arg ) {
	ASMUtil.RequiredEmitterType ret = 
	    ASMUtil.requiredEmitterType.get( arg ) ;
	EmitterFactory.Emitter em = null ;
	if (ret != ASMUtil.RequiredEmitterType.NONE)
	    em = EmitterFactory.makeEmitter( arg, 
		ret == ASMUtil.RequiredEmitterType.SETTER ) ;
	handleEmitter( arg, em ) ;
    }

    private void compareEmitter( String nodeType, Node arg,
	EmitterFactory.Emitter expected, EmitterFactory.Emitter actual ) {

	boolean error ;
	if (actual == null)
	    error = expected != null ;
	else
	    error = !(actual.equals( expected )) ;

	if (error)
	    verificationError( arg, "Incorrect " + nodeType + ": expected " +
		expected + ", but found " + actual ) ;
    }

    private void handleEmitter( Node arg, EmitterFactory.Emitter em ) {
	if (preparing()) {
	    ASMUtil.emitter.set( arg, em ) ;
	} else {
	    EmitterFactory.Emitter lem = ASMUtil.emitter.get( arg ) ;
	    compareEmitter( "emitter", arg, em, lem ) ;
	}
    }

    private void initializeVariableEmitter( Variable arg ) {
	// Define the emitter attribute for this Variable.
	EmitterFactory.Emitter em = null ;
	ASMUtil.RequiredEmitterType ret = 
	    ASMUtil.requiredEmitterType.get( arg ) ;

	if (ret != ASMUtil.RequiredEmitterType.NONE) {
	    if (ret == ASMUtil.RequiredEmitterType.SETTER) {
		em = ASMUtil.setEmitter.get( arg ) ;
	    } else {
		em = ASMUtil.getEmitter.get( arg ) ;
	    }

	    handleEmitter( arg, em ) ;
	}
    }

    // Use this method to define local variables outside of
    // the Visitor.
    private void defineLocalVariable( Variable arg ) {
	allocateLocalVariable( arg ) ;
	finishVariableDefinition( arg ) ;
    }

    private void allocateLocalVariable( Variable arg ) {
	// Get a slot from the slot allocator and use it to
	// set the getEmitter and setEmitter attributes.
	assert slotAllocator != null ;
	int sfs = slotAllocator.getSlot( arg.type() ) ;

	if (preparing()) {
	    ASMUtil.stackFrameSlot.set( arg, sfs ) ;
	} else {
	    int slot = ASMUtil.stackFrameSlot.get( arg ) ;
	    if (slot != sfs)
		verificationError( arg, "Expected stackFrameSlot to be " +
		    sfs + ", was " + slot ) ;
	}
    }

    private void finishVariableDefinition( Variable arg ) {
	// Either parent is class, and we can use the class info to 
	// set the getEmitter and setEmitter attributes, or not, 
	// in which case the stackFrameSlot attribute is set.  
	// EmitterFactory uses this
	// information to construct the correct emitter.
	EmitterFactory.Emitter getter = EmitterFactory.makeEmitter( arg, false ) ;
	EmitterFactory.Emitter setter = EmitterFactory.makeEmitter( arg, true ) ;

	if (preparing()) {
	    ASMUtil.getEmitter.set( arg, getter ) ;
	    ASMUtil.setEmitter.set( arg, setter ) ;
	} else {
	    EmitterFactory.Emitter lgetter = ASMUtil.getEmitter.get( arg ) ;
	    compareEmitter( "getEmitter", arg, getter, lgetter ) ;
	    EmitterFactory.Emitter lsetter = ASMUtil.setEmitter.get( arg ) ;
	    compareEmitter( "setEmitter", arg, setter, lsetter ) ;
	}
    }

    @Override
    public boolean preVariable( Variable arg ) {
	switch (variableDefiningContext) {
	    case REFERENCE :
		// Note that a reference to a definition is 
		// copied, and the definition has requiredEmitterType
		// set to NONE, so we need to make sure that ALL
		// references are set to GETTER.  The setters are
		// handled in those particular conxtexts where they
		// occur.
		ASMUtil.requiredEmitterType.set( arg,
		    ASMUtil.RequiredEmitterType.GETTER ) ; 
		
		// Only variable references should be labelled
		initializeVariableEmitter( arg ) ;
		break ;
	    case DEFINE_LOCAL :
		defineLocalVariable( arg ) ;
		break ;
	    case DEFINE_LOCAL_DEFINITION:
		defineLocalVariable( arg ) ;
		initializeVariableEmitter( arg ) ;
		break ; // added for fixing Issue 611
	    default :
		// Added only in case VariableContext gets more
		// values in the future
		assert false ; // should never get here!
	}

	return false ; // We don't need postVariable here.
    }

    @Override
    public boolean preBinaryOperatorExpression( ExpressionFactory.BinaryOperatorExpression arg ) {
	// XXX if the type of arg.left and arg.right are both numeric but not the same,
	// label arg with the type required for coercion.
	return true ;
    }

    @Override
    public void binaryOperatorExpressionBeforeRight( ExpressionFactory.BinaryOperatorExpression arg ) {
    }

    @Override
    public void postBinaryOperatorExpression( ExpressionFactory.BinaryOperatorExpression arg ) {
	postExpression( arg ) ;

    }

    /**
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
    */
}
