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

import java.util.List ;

import com.sun.corba.se.spi.codegen.Expression ;
import com.sun.corba.se.spi.codegen.Signature ;
import com.sun.corba.se.spi.codegen.Type ;
import com.sun.corba.se.spi.codegen.Variable ;
import com.sun.corba.se.spi.codegen.ImportList ;

import com.sun.corba.se.impl.codegen.AssignmentStatement ;
import com.sun.corba.se.impl.codegen.BlockStatement ;
import com.sun.corba.se.impl.codegen.CaseBranch ;
import com.sun.corba.se.impl.codegen.ClassGenerator ;
import com.sun.corba.se.impl.codegen.CodeGenerator ;
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

/** A Visitor that generates Java source for an expression.
 * All expression are converted into a simple Java String.
 * This visitor operates only on Expression nodes. It ignores all 
 * Statements as well as the top level generators.
 * <P>
 * This visitor compile complex expressions by applying another
 * instance of this visitor to the subexpressions recursively
 * using TreeWalker.  Note that the mark facility of the
 * TreeWalkerContext is needed here to handle arguments lists
 * for various types of calls.
 */
public class SourceExpressionVisitor extends TreeWalker {
    private ImportList imports ;
    private StringBuilder sb ;

    private String typeName( Type type ) {
	if (imports.contains( type ))
	    return type.className() ;
	else
	    return type.name() ;
    }

    public String value() {
	return sb.toString() ;
    }

    public SourceExpressionVisitor( TreeWalkerContext context, ImportList imports ) {
	super( context ) ;
	context.push( this ) ;
	this.imports = imports ;
	this.sb = new StringBuilder() ;
    }

    // Node
    @Override
    public boolean preNode( Node arg ) {
	// Make sure that any nodes not otherwise specified
	// are not traversed.
	return false ;
    }

    @Override
    public boolean preVariable( Variable arg ) {
	sb.append( arg.ident() ) ;
	return false ;
    }

    @Override
    public void postVariable( Variable arg ) {
	// NO-OP
    }

    // ConstantExpression
    @Override
    public boolean preConstantExpression( ExpressionFactory.ConstantExpression arg ) {
	// Need to handle the different cases here.
	String javaRep ;
	Object value = arg.value() ;
	Type type = arg.type() ;
	
	if (type == Type._null())
	    javaRep = "null" ;
	else if (type == Type._Class())
	    javaRep = typeName( Type.class.cast(value) ) + ".class" ;
	else if (type == Type._String())
	    javaRep = "\"" + value + "\"" ;
	else if (type == Type._char())
	    javaRep = "\'" + value + "\'" ;
	else 
	    // XXX we may want to add Java flags for
	    // different sized numbers.
	    javaRep = value.toString() ;
	
	sb.append( javaRep ) ;
	return false ;
    }

    // VoidExpression
    @Override
    public boolean preVoidExpression( ExpressionFactory.VoidExpression arg ) {
	sb.append( "void" ) ;
	return false ;
    }

    // ThisExpression
    @Override
    public boolean preThisExpression( ExpressionFactory.ThisExpression arg ) {
	sb.append( "this" ) ;
	return false ;
    }

    // UnaryOperatorExpression
    @Override
    public boolean preUnaryOperatorExpression( ExpressionFactory.UnaryOperatorExpression arg ) {
	sb.append( arg.operator().javaRepresentation() ) ;
	sb.append( '(' ) ;
	return true ;
    }

    @Override
    public void postUnaryOperatorExpression( ExpressionFactory.UnaryOperatorExpression arg ) {
	sb.append( ')' ) ;
    }

    // BinaryOperatorExpression
    @Override
    public boolean preBinaryOperatorExpression( ExpressionFactory.BinaryOperatorExpression arg ) {
	sb.append( '(' ) ;
	return true ;
    }

    @Override
    public void binaryOperatorExpressionBeforeRight( 
	ExpressionFactory.BinaryOperatorExpression arg ) {
	sb.append( arg.operator().javaRepresentation() ) ;
    }

    @Override
    public void postBinaryOperatorExpression( ExpressionFactory.BinaryOperatorExpression arg ) {
	sb.append( ')' ) ;
    }

    // CastExpression
    @Override
    public boolean preCastExpression( ExpressionFactory.CastExpression arg ) {
	sb.append( "((" ) ;
	sb.append( typeName( arg.type() ) ) ;
	sb.append( ')' ) ;

	return true ;
    }

    @Override
    public void postCastExpression( ExpressionFactory.CastExpression arg ) {
	sb.append( ')' ) ;
    }

    // InstofExpression
    @Override
    public boolean preInstofExpression( ExpressionFactory.InstofExpression arg ) {
	sb.append( '(' ) ;
	return true ;
    }

    @Override
    public void postInstofExpression( ExpressionFactory.InstofExpression arg ) {
	sb.append( " instanceof " ) ;
	sb.append( typeName( arg.type() ) ) ;
	sb.append( ')' ) ;
    }

    // NonStaticCallExpression
    @Override
    public boolean preNonStaticCallExpression( ExpressionFactory.NonStaticCallExpression arg ) {
	// Mark the start of the context stack for this call expression
	context.mark() ;
	// Push an expression visitor to handle the target of this method call
	new SourceExpressionVisitor( context, imports ) ;

	return true ;
    }

    @Override
    public void nonStaticCallExpressionBeforeArg( ExpressionFactory.NonStaticCallExpression arg ) {
	// Push an expression visitor to handle the next argument
	new SourceExpressionVisitor( context, imports ) ;
    }

    @Override
    public void postNonStaticCallExpression( ExpressionFactory.NonStaticCallExpression arg ) {
	List<Visitor> marks = context.popMark() ;

	// The first element of marks contains visitor for the target.
	// Each subsequent element contains a visitor for an argument.   
	// Turn all of this into target.op( arg1, ..., arg n ) and append to sb.
	int ctr = 0 ;
	for (Visitor visitor : marks ) {
	    SourceExpressionVisitor sev = SourceExpressionVisitor.class.cast( visitor ) ;
	    if (ctr==0) {
		sb.append( sev.value() ) ;
		sb.append( '.' ) ;
		sb.append( arg.ident() ) ;
		sb.append( '(' ) ;
	    } else {
		if (ctr>1)
		    sb.append( ", " ) ;
		sb.append( sev.value() ) ;
	    }

	    ctr++ ;
	}

	sb.append( ")" ) ;
    }

    // StaticCallExpression
    @Override
    public boolean preStaticCallExpression( ExpressionFactory.StaticCallExpression arg ) {
	// Mark the start of the context stack for this call expression
	context.mark() ;

	return true ;
    }

    @Override
    public void staticCallExpressionBeforeArg( ExpressionFactory.StaticCallExpression arg ) {
	// Push an expression visitor to handle the next argument
	new SourceExpressionVisitor( context, imports ) ;
    }

    @Override
    public void postStaticCallExpression( ExpressionFactory.StaticCallExpression arg ) {
	List<Visitor> marks = context.popMark() ;

	sb.append( typeName( arg.target() ) ) ;
	sb.append( '.' ) ;
	sb.append( arg.ident() ) ;
	sb.append( '(' ) ;

	// Each element of marks contains a visitor for an argument.   
	// Turn all of this into target.op( arg1, ..., arg n ) and append to sb.
	int ctr = 0 ;
	for (Visitor visitor : marks ) {
	    SourceExpressionVisitor sev = SourceExpressionVisitor.class.cast( visitor ) ;
	    if (ctr>0) {
		sb.append( ", " ) ;
	    }

	    sb.append( sev.value() ) ;

	    ctr++ ;
	}

	sb.append( ")" ) ;
    }

    // NewObjExpression
    @Override
    public boolean preNewObjExpression( ExpressionFactory.NewObjExpression arg ) {
	// Mark the start of the context stack for this call expression
	context.mark() ;

	return true ;
    }

    @Override
    public void newObjExpressionBeforeArg( ExpressionFactory.NewObjExpression arg ) {
	// Push an expression visitor to handle the next argument
	new SourceExpressionVisitor( context, imports ) ;
    }

    @Override
    public void postNewObjExpression( ExpressionFactory.NewObjExpression arg ) {
	List<Visitor> marks = context.popMark() ;

	sb.append( "new " ) ;
	sb.append( typeName( arg.type() ) ) ;
	sb.append( '(' ) ;
	
	// The element of marks contains visitor for the target.
	// Each subsequent element contains a visitor for an argument.   
	// Turn all of this into "new type( arg1, ..., arg n )" and append to sb.
	int ctr = 0 ;
	for (Visitor visitor : marks ) {
	    SourceExpressionVisitor sev = SourceExpressionVisitor.class.cast( visitor ) ;

	    if (ctr>0) {
		sb.append( ", " ) ;
	    }

	    sb.append( sev.value() ) ;

	    ctr++ ;
	}

	sb.append( ")" ) ;
    }

    @Override
    public boolean preNewArrExpression( ExpressionFactory.NewArrExpression arg ) {
	context.mark() ;
	new SourceExpressionVisitor( context, imports ) ;

	return true ;
    }

    @Override
    public void newArrExpressionBeforeExpression( ExpressionFactory.NewArrExpression arg ) {
	new SourceExpressionVisitor( context, imports ) ;
    }

    // NewArrExpression
    // This generates either 
    //     new type[expr] if arg.
    // or
    //     new type[] { expr1, ... , exprn }
    //
    // In the first case, arg.exprs() == null, otherwise we generate the second
    // case.
    @Override
    public void postNewArrExpression( ExpressionFactory.NewArrExpression arg ) {
	List<Visitor> marks = context.popMark() ;

	int ctr = 0 ;
	for (Visitor visitor : marks ) {
	    SourceExpressionVisitor sev = SourceExpressionVisitor.class.cast( visitor ) ;
	    if (ctr==0) {
		sb.append( "new " ) ;
		sb.append( typeName( arg.ctype() ) ) ;
		sb.append( '[' ) ;
		if (arg.exprs().size() == 0) {
		    // marks contains only the array size
		    sb.append( sev.value() ) ;
		    sb.append( ']' ) ;
		} else {
		    // marks contains the size followed by the initial elements of array.
		    // Ignore the size in this case.
		    sb.append( "] {" ) ;
		}
	    } else {
		if (ctr>1)
		    sb.append( ", " ) ;
		sb.append( sev.value() ) ;
	    }

	    ctr++ ;
	}

	sb.append( "}" ) ;
    }

    // SuperCallExpression
    @Override
    public boolean preSuperCallExpression( ExpressionFactory.SuperCallExpression arg ) {
	// Mark the start of the context stack for this call expression
	context.mark() ;

	return true ;
    }

    @Override
    public void superCallExpressionBeforeArg( ExpressionFactory.SuperCallExpression arg ) {
	// Push an expression visitor to handle the next argument
	new SourceExpressionVisitor( context, imports ) ;
    }

    @Override
    public void postSuperCallExpression( ExpressionFactory.SuperCallExpression arg ) {
	List<Visitor> marks = context.popMark() ;

	sb.append( "super." ) ;
	sb.append( arg.ident() ) ;
	sb.append( '(' ) ;
	
	// Each element of marks contains a visitor for an argument.   
	// Turn this into "super.ident( arg1, ..., arg n )" and append to sb.
	int ctr = 0 ;
	for (Visitor visitor : marks ) {
	    SourceExpressionVisitor sev = SourceExpressionVisitor.class.cast( visitor ) ;
	    if (ctr>0) {
		sb.append( ", " ) ;
	    }

	    sb.append( sev.value() ) ;

	    ctr++ ;
	}

	sb.append( ")" ) ;
    }

    // SuperObjExpression
    @Override
    public boolean preSuperObjExpression( ExpressionFactory.SuperObjExpression arg ) {
	// Mark the start of the context stack for this call expression
	context.mark() ;

	return true ;
    }

    @Override
    public void superObjExpressionBeforeArg( ExpressionFactory.SuperObjExpression arg ) {
	// Push an expression visitor to handle the next argument
	new SourceExpressionVisitor( context, imports ) ;
    }

    @Override
    public void postSuperObjExpression( ExpressionFactory.SuperObjExpression arg ) {
	List<Visitor> marks = context.popMark() ;

	sb.append( "super(" ) ;
	
	// Each element of marks contains a visitor for an argument.   
	// Turn this into "super( arg1, ..., arg n )" and append to sb.
	int ctr = 0 ;
	for (Visitor visitor : marks ) {
	    SourceExpressionVisitor sev = SourceExpressionVisitor.class.cast( visitor ) ;
	    if (ctr>0) {
		sb.append( ", " ) ;
	    }

	    sb.append( sev.value() ) ;

	    ctr++ ;
	}

	sb.append( ")" ) ;
    }

    // ThisObjExpression
    @Override
    public boolean preThisObjExpression( ExpressionFactory.ThisObjExpression arg ) {
	// Mark the start of the context stack for this call expression
	context.mark() ;

	return true ;
    }

    @Override
    public void thisObjExpressionBeforeArg( ExpressionFactory.ThisObjExpression arg ) {
	// Push an expression visitor to handle the next argument
	new SourceExpressionVisitor( context, imports ) ;
    }

    @Override
    public void postThisObjExpression( ExpressionFactory.ThisObjExpression arg ) {
	List<Visitor> marks = context.popMark() ;

	sb.append( "this(" ) ;
	
	// Each element of marks contains a visitor for an argument.   
	// Turn this into "super( arg1, ..., arg n )" and append to sb.
	int ctr = 0 ;
	for (Visitor visitor : marks ) {
	    SourceExpressionVisitor sev = SourceExpressionVisitor.class.cast( visitor ) ;
	    if (ctr>0) {
		sb.append( ", " ) ;
	    }

	    sb.append( sev.value() ) ;

	    ctr++ ;
	}

	sb.append( ")" ) ;
    }

    // NonStaticFieldAccessExpression
    @Override
    public boolean preNonStaticFieldAccessExpression( 
	ExpressionFactory.NonStaticFieldAccessExpression arg ) {
	// Just let the visitor write to sb for arg.expr()

	return true ;
    }

    @Override
    public void postNonStaticFieldAccessExpression( 
	ExpressionFactory.NonStaticFieldAccessExpression arg ) {
	sb.append( '.' ) ;
	sb.append( arg.fieldName() ) ;
    }

    // StaticFieldAccessExpression
    @Override
    public boolean preStaticFieldAccessExpression( 
	ExpressionFactory.StaticFieldAccessExpression arg ) {
	// Just let the visitor write to sb for arg.expr()

	return true ;
    }

    @Override
    public void postStaticFieldAccessExpression( 
	ExpressionFactory.StaticFieldAccessExpression arg ) {
	sb.append( typeName( arg.target() ) ) ;
	sb.append( '.' ) ;
	sb.append( arg.fieldName() ) ;
    }

    // ArrayIndexExpression
    @Override
    public boolean preArrayIndexExpression( ExpressionFactory.ArrayIndexExpression arg ) {
	// Push an expression visitor to handle the index 
	new SourceExpressionVisitor( context, imports ) ;

	return true ;
    }

    @Override
    public void arrayIndexExpressionBeforeExpr( ExpressionFactory.ArrayIndexExpression arg ) {
	// Push an expression visitor to handle the array expression 
	new SourceExpressionVisitor( context, imports ) ;
    }

    @Override
    public void postArrayIndexExpression( ExpressionFactory.ArrayIndexExpression arg ) {
	SourceExpressionVisitor expr = 
	    SourceExpressionVisitor.class.cast( context.pop() ) ;
	SourceExpressionVisitor index = 
	    SourceExpressionVisitor.class.cast( context.pop() ) ;
	sb.append( expr.value() ) ;
	sb.append( '[' ) ;
	sb.append( index.value() ) ;
	sb.append( ']' ) ;
    }

    // ArrayLengthExpression
    @Override
    public boolean preArrayLengthExpression( 
	ExpressionFactory.ArrayLengthExpression arg ) {
	new SourceExpressionVisitor( context, imports ) ;
	return true ;
    }

    @Override
    public void postArrayLengthExpression( ExpressionFactory.ArrayLengthExpression arg ) {
	SourceExpressionVisitor expr = 
	    SourceExpressionVisitor.class.cast( context.pop() ) ;
	sb.append( expr.value() ) ;
	sb.append( ".length" ) ;
    }
}
