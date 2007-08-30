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
import java.util.Set ;
import java.util.HashSet ;
import java.util.IdentityHashMap ;
import java.util.Arrays ;
import java.util.ArrayList ;
import java.util.Map ;
import java.util.HashMap ;

import com.sun.corba.se.spi.codegen.Expression ;
import com.sun.corba.se.spi.codegen.Type ;
import com.sun.corba.se.spi.codegen.Signature ;
import com.sun.corba.se.spi.codegen.Variable ;
import com.sun.corba.se.spi.codegen.ClassInfo ;
import com.sun.corba.se.spi.codegen.FieldInfo ;

import com.sun.corba.se.spi.orbutil.copyobject.Copy ;
import com.sun.corba.se.spi.orbutil.copyobject.CopyType ;

import com.sun.corba.se.impl.codegen.NodeBase ;

/** Used to create all expressions.  BlockStatement is used as the
 * factory for creating instances of ExpressionFactory.  All statements
 * created by an ExpressionFactory have the BlockStatement that
 * created this ExpressionFactory as their parent.  An ExpressionFactory
 * should be used only to create Expression instances in statements
 * inside the creating BlockStatement.  Each subtype of Expression is
 * defined as a static inner class in ExpressionFactory.
 */
public final class ExpressionFactory {
    private final Node efparent ;
    private final IdentityHashMap<Expression,Boolean> unusedExpressions ;

    public final Node efparent() {
	return this.efparent ;
    }

    public final IdentityHashMap<Expression,Boolean> unusedExpressions() {
	return unusedExpressions ;
    }

    public ExpressionFactory( Node parent ) {
	this.efparent = parent ;
	unusedExpressions = new IdentityHashMap<Expression,Boolean>() ;
    }

    /* A type safe method for copying a List<Expression>.
     * This may be the only way this can be done, since there
     * is no way to create a List<Expression>.class constant
     * to pass to a T copy( Class<T> ) call on NodeBase.
     * A cast to List<Expression> is not type safe, so it 
     * would create a compiler warning, although never would 
     * result in a runtime exception.
     */
    static List<Expression> copyExpressionList( Node newParent, List<Expression> exprs ) {
	List<Expression> result = new ArrayList<Expression>() ;
	for (Expression expr : exprs)
	    result.add( expr.copy(newParent, Expression.class) ) ;
	return result ;
    }

    public static abstract class ExpressionBase extends NodeBase implements Expression {
	@Copy( CopyType.IDENTITY ) 
	private ExpressionFactory expressionFactory ;

	public ExpressionBase( ExpressionFactory ef ) {
	    super( ef.efparent() ) ;
	    this.expressionFactory = ef ;
	    ef.unusedExpressions().put( this, true ) ;
	}

	// Override in the subclasses that are assignable.
	public boolean isAssignable() {
	    return false ;
	}

	@Override
	public <T extends Node> T copy( Class<T> cls ) {
	    throw new IllegalArgumentException( 
		"Need to use copy(Node,Class) to copy an expression" ) ;
	}

	@Override
	public <T extends Node> T copy( Node newParent, Class<T> cls ) {
	    if (expressionFactory.unusedExpressions().containsKey( this ))
		expressionFactory.unusedExpressions().remove( this ) ;

	    // Only copy expressions in which all local definitions 
	    // are still in scope.
	    Util.checkScope( this ) ;

	    return super.copy( newParent, cls ) ;
	}

	// Every subclass must define accept( Visitor ) and type().  
	// For debugging purposes, subclasses should also define
	// toString.  Just as in
	// Statement subclasses, we will not define hashCode or equals
	// in subclasses of ExpressionBase.
	@Override
	public abstract void accept( Visitor v ) ;
    }

//--------------- CONSTANT EXPRESSIONS ----------------------------------------
    
    /** Class that represents a constant value of any primitive type,
     * a String, or a Class.
     */
    public static final class ConstantExpression extends ExpressionBase {
	private Type type ;
	private Object value ;

	ConstantExpression( ExpressionFactory ef, Type type, Object value ) {
	    super( ef ) ;
	    this.type = type ;
	    this.value = value ;
	}

	public Object value() {
	    return this.value ;
	}

	@Override
	public String toString() {
	    String valueStr = value == null ? "null" : value.toString() ;
	    return "ConstantExpression" + Util.getNodeIdString(this) 
		+ "[" + type.name() + ":" + valueStr + "]" ;
	}

	public Type type() {
	    return this.type ;
	}

	@Override
	public void accept( Visitor visitor ) {
	    visitor.visitConstantExpression( this ) ;
	}
    }

    public Expression _null() {
	return new ConstantExpression( this, Type._null(), null ) ;
    }

    public Expression _const( boolean c ) {
	return new ConstantExpression( this, Type._boolean(), c ) ;
    }

    public Expression _const( char c ) {
	return new ConstantExpression( this, Type._char(), c ) ;
    }

    public Expression _const( byte c ) {
	return new ConstantExpression( this, Type._byte(), c ) ;
    }

    public Expression _const( short c ) {
	return new ConstantExpression( this, Type._short(), c ) ;
    }

    public Expression _const( int c ) {
	return new ConstantExpression( this, Type._int(), c ) ;
    }

    public Expression _const( long c ) {
	return new ConstantExpression( this, Type._long(), c ) ;
    }

    public Expression _const( float c ) {
	return new ConstantExpression( this, Type._float(), c ) ;
    }

    public Expression _const( double c ) {
	return new ConstantExpression( this, Type._double(), c ) ;
    }

    public Expression _const( String c ) {
	return new ConstantExpression( this, Type._String(), c ) ;
    }

    public Expression _const( Type c ) {
	return new ConstantExpression( this, Type._Class(), c ) ;
    }

//--------------- VOID EXPRESSION ----------------------------------------

    /** Class that represents a void expression. 
     */
    public static final class VoidExpression extends ExpressionBase {
	VoidExpression( ExpressionFactory ef ) {
	    super( ef ) ;
	}

	public Type type() {
	    return Type._void() ;
	}

	@Override
	public void accept( Visitor visitor ) {
	    visitor.visitVoidExpression( this ) ;
	}

	@Override
	public String toString() {
	    return "VoidExpression" + Util.getNodeIdString(this) 
		+ "[]" ;
	}
    }

    public Expression _void() {
	return new VoidExpression( this );
    }

//--------------- THIS EXPRESSION ----------------------------------------

    /** Class used to represent the current object ("this" in java).
     */
    public static final class ThisExpression extends ExpressionBase {
	ThisExpression( ExpressionFactory ef ) {
	    super( ef ) ;
	}

	public Type type() {
	    // The most derived type of "this" is unknown, so
	    // we should type it as the static type of the defining
	    // class.  This can be determined by walking up the
	    // parent chain until we find the class. 
	    ClassGenerator cg = getAncestor( ClassGenerator.class ) ;
	    if (cg == null)
		throw new IllegalStateException(
		    "No ClassGenerator found!" ) ;
	    Type cgType = Type._classGenerator( cg ) ;
	    return cgType ;
	}

	@Override
	public void accept( Visitor visitor ) {
	    visitor.visitThisExpression( this ) ;
	}

	@Override
	public String toString() {
	    return "ThisExpression" + Util.getNodeIdString(this) 
		+ "[]" ;
	}
    }

    public Expression _this() {
	return new ThisExpression( this ) ;
    }

//--------------- CALL EXPRESSION ----------------------------------------

    /** Representation of any sort of method call other than a 
     * constructor invocation.  There are two main cases here:
     * static calls, represented by CallExpression<Type>, and
     * non-static calls, represented by CallExpression<Expression>.
     * This abstract base class has two concrete subclasses, one
     * for static, and one for non-static calls.
     * <P>
     * The call type is determined as follows:
     * <ul>
     * <li>If isStatic is true, the call is static.
     * <li>If isStatic is false, and the target's type is an interface, 
     * the call is an interface call.
     * <li>If isStatic is false, and the target's type is not an interface, 
     * the call is virtual.
     * </ul>
     */
    public static abstract class CallExpression<T> extends ExpressionBase {
	private T target ;
	private String ident ;
	private Signature signature ;
	private List<Expression> args ;

	// Defined in subclass to indicate static or non-static call
	public abstract boolean isStatic() ;

	// Construct a call expression for a call
	CallExpression( ExpressionFactory ef, String ident,
	    Signature signature, List<Expression> args ) {
	    super( ef ) ;
	    this.ident = ident ;
	    this.signature = signature ;
	    this.args = copyExpressionList( this, args ) ;
	}

	public final T target() {
	    return this.target ;
	}

	public final void target( T arg ) {
	    this.target = arg ;
	}

	public final String ident() {
	    return this.ident ;
	}

	public final Signature signature() {
	    return this.signature ;
	}

	public final List<Expression> args() {
	    return this.args ;
	}

	public final Type type() {
	    return signature.returnType() ;
	}
	
	// every subclass must define accept( Visitor ).
    }	
	
    public static final class StaticCallExpression extends CallExpression<Type> {
	StaticCallExpression( ExpressionFactory ef, Type target, String ident,
	    Signature signature, List<Expression> args ) {
	    super( ef, ident, signature, args ) ;
	    target( target ) ;
	}

	@Override
	public boolean isStatic() {
	    return true ;
	}

	@Override
	public void accept( Visitor visitor ) {
	    visitor.visitStaticCallExpression( this ) ;
	}

	@Override
	public String toString() {
	    return "StaticCallExpression" + Util.getNodeIdString(this) 
		+ "[target=" + target() + " " +  
		"ident=" + ident() + " " +
		"signature=" + signature() + "]" ;
	}
    }

    public static final class NonStaticCallExpression extends CallExpression<Expression> {
	NonStaticCallExpression( ExpressionFactory ef, Expression target, String ident,
	    Signature signature, List<Expression> args ) {
	    super( ef, ident, signature, args ) ;
	    target( target.copy( this, Expression.class ) ) ;
	}

	@Override
	public boolean isStatic() {
	    return false ;
	}

	@Override
	public void accept( Visitor visitor ) {
	    visitor.visitNonStaticCallExpression( this ) ;
	}

	@Override
	public String toString() {
	    return "NonStaticCallExpression" + Util.getNodeIdString(this) 
		+ "[ident=" + ident() + " " +
		"signature=" + signature() + "]" ;
	}
    }

    /** Construct a representation of a non-static method invocation.
     */
    public Expression call( Expression target, String ident, Signature signature, 
	List<Expression> exprs ) {
	signature.checkCompatibility( target.type(), ident, exprs ) ;
	return new NonStaticCallExpression( this, target, ident, signature, exprs ) ; 
    }

    /** Shorthand form to construct invocation that looks up Signature based
     * on the types of the expressions in exprs.  Can probably be used in most
     * circumstances.
     */
    public Expression call( Expression target, String ident, 
	List<Expression> exprs ) {
	Signature sig = Signature.fromCall( target.type(), ident, exprs ) ;
	return new NonStaticCallExpression( this, target, ident, sig, exprs ) ; 
    }

    /** Construct a representation of a static method invocation.
     */
    public Expression staticCall( Type target, String ident, Signature signature, 
	List<Expression> exprs ) {

	signature.checkStaticCompatibility( target, ident, exprs ) ;
	if (target.isPrimitive() || target.isArray())
	    throw new IllegalArgumentException(
		"The target for a static call must be a reference type" ) ;

	return new StaticCallExpression( this, target, ident, signature, exprs ) ; 
    }

    /** Shorthand form to construct invocation that looks up Signature based
     * on the types of the expressions in exprs.  Can probably be used in most
     * circumstances.
     */
    public Expression staticCall( Type target, String ident, 
	List<Expression> exprs ) {
	if (target.isPrimitive() || target.isArray())
	    throw new IllegalArgumentException(
		"The target for a static call must be a reference type" ) ;

	Signature sig = Signature.fromStaticCall( target, ident, exprs ) ;
	return new StaticCallExpression( this, target, ident, sig, exprs ) ; 
    }

//--------------- UNARY OPERATOR EXPRESSION ----------------------------------------

    /** Operators that apply to a single expression.
     * The only unary operator that we support at present is ! (NOT)
     * because this is very useful in constructing boolean expressions
     * for while loops and conditionals.
     */
    public enum UnaryOperator{ 
	NOT( "!" ) {
	    public void checkType( Expression arg ) {
		if (arg.type() != Type._boolean())
		    throw new IllegalArgumentException( 
			"! expects a boolean type, found " + arg.type() ) ;
	    }
	} ;

	private final String javaRepresentation ;

	public String javaRepresentation() {
	    return javaRepresentation ;
	}
    
	public abstract void checkType( Expression arg ) ;

	UnaryOperator( String javaRepresentation ) {
	    this.javaRepresentation = javaRepresentation ;
	}
    }

    /** Representation of the application of a UnaryOperator to an
     * Expression.
     */
    public static final class UnaryOperatorExpression extends ExpressionBase {
	private UnaryOperator op ;
	private Expression expr ;

	UnaryOperatorExpression( ExpressionFactory ef, UnaryOperator op, Expression expr ) {
	    super( ef ) ;
	    this.op = op ;
	    this.expr = expr.copy(this, Expression.class);
	}

	public UnaryOperator operator() {
	    return op ;
	}

	public Expression expr() {
	    return expr ;
	}

	public Type type() {
	    return Type._boolean() ;
	}

	@Override
	public void accept( Visitor visitor ) {
	    visitor.visitUnaryOperatorExpression( this ) ;
	}

	@Override
	public String toString() {
	    return "UnaryOperatorExpression" + Util.getNodeIdString(this) 
		+ "[" + op + "]" ;
	}
    }

    public Expression unaryOp( UnaryOperator op, Expression expr ) {
	return new UnaryOperatorExpression( this, op, expr ) ;
    }

//--------------- BINARY OPERATOR EXPRESSION ----------------------------------------

    public enum BinaryOperatorKind { RELATIONAL, NUMERIC, BOOLEAN } ;

    /** Representation of binary operators.
     * We only support a limited set of operators as follows:
     * <ul>
     * <li>Basic arithmetic: PLUS, MINUS, TIMES, DIV, REM
     * <li>Relational operators (LT, GT, LE, FE, EQ, NE)
     * <li>== and != on references (e.g. obj == null) and
     * primitives.
     * <li>AND and OR on booleans
     * </ul>
     */
    public enum BinaryOperator{ 
	PLUS( "+" ) {
	    public Expression create( ExpressionFactory ef, 
		Expression left, Expression right ) {

		return createNumericExpression( this, ef, left, right ) ;
	    }

	    public BinaryOperatorKind kind() {
		return BinaryOperatorKind.NUMERIC ;
	    }
	},

	TIMES( "*" ) {
	    public Expression create( ExpressionFactory ef, 
		Expression left, Expression right ) {

		return createNumericExpression( this, ef, left, right ) ;
	    }

	    public BinaryOperatorKind kind() {
		return BinaryOperatorKind.NUMERIC ;
	    }
	},

	DIV( "/" ) {
	    public Expression create( ExpressionFactory ef, 
		Expression left, Expression right ) {

		return createNumericExpression( this, ef, left, right ) ;
	    }

	    public BinaryOperatorKind kind() {
		return BinaryOperatorKind.NUMERIC ;
	    }
	},

	MINUS( "-" ) {
	    public Expression create( ExpressionFactory ef, 
		Expression left, Expression right ) {

		return createNumericExpression( this, ef, left, right ) ;
	    }

	    public BinaryOperatorKind kind() {
		return BinaryOperatorKind.NUMERIC ;
	    }
	},

	REM( "%" ) {
	    public Expression create( ExpressionFactory ef, 
		Expression left, Expression right ) {

		return createNumericExpression( this, ef, left, right ) ;
	    }

	    public BinaryOperatorKind kind() {
		return BinaryOperatorKind.NUMERIC ;
	    }
	},

	GT( ">" ) {
	    public Expression create( ExpressionFactory ef, 
		Expression left, Expression right ) {

		return createNumericExpression( this, ef, left, right ) ;
	    }
	},

	GE( ">=" ) {
	    public Expression create( ExpressionFactory ef, 
		Expression left, Expression right ) {

		return createNumericExpression( this, ef, left, right ) ;
	    }
	},

	LT( "<" ) {
	    public Expression create( ExpressionFactory ef, 
		Expression left, Expression right ) {

		return createNumericExpression( this, ef, left, right ) ;
	    }
	},

	LE( "<=" ) {
	    public Expression create( ExpressionFactory ef, 
		Expression left, Expression right ) {

		return createNumericExpression( this, ef, left, right ) ;
	    }
	},

	EQ( "==" ) {
	    public Expression create( ExpressionFactory ef, 
		Expression left, Expression right ) {

		return createEqualityExpression( this, ef, left, right ) ;
	    }
	},

	NE( "!=" ) {
	    public Expression create( ExpressionFactory ef, 
		Expression left, Expression right ) {

		return createEqualityExpression( this, ef, left, right ) ;
	    }
	},

	AND( "&&" ) {
	    public Expression create( ExpressionFactory ef, 
		Expression left, Expression right ) {

		if ((left != Type._boolean()) || (right != Type._boolean()))
		    throw new IllegalArgumentException(
			this.javaRepresentation() + " requires boolean expressions" ) ;

		return new IfExpression( ef, left, right, ef._const( false ) ) ;
	    }

	    public BinaryOperatorKind kind() {
		return BinaryOperatorKind.BOOLEAN ;
	    }
	},

	OR( "||" ) {
	    public Expression create( ExpressionFactory ef, 
		Expression left, Expression right ) {

		if ((left != Type._boolean()) || (right != Type._boolean()))
		    throw new IllegalArgumentException(
			this.javaRepresentation() + " requires boolean expressions" ) ;

		return ef.ifExpression( left, ef._const( true ), right ) ;
	    }

	    public BinaryOperatorKind kind() {
		return BinaryOperatorKind.BOOLEAN ;
	    }
	} ;

	private final String javaRepresentation ;

	private static Expression createNumericExpression( 
	    final BinaryOperator op,
	    final ExpressionFactory ef,
	    final Expression left, final Expression right ) {

	    Expression lb = left ;
	    Expression rb = right ;

	    Type ctype = left.type().binaryPromotion( right.type() ) ;
	    if (!ctype.equals( left.type() ))
		lb = ef.cast( ctype, lb ) ;
	    if (!ctype.equals( right.type() ))
		rb = ef.cast( ctype, rb ) ;

	    return new BinaryOperatorExpression( ef, ctype, lb, op, rb ) ;
	}

	// See JLS 15.20
	private static Expression createEqualityExpression( 
	    final BinaryOperator op,
	    final ExpressionFactory ef,
	    final Expression left, final Expression right ) {

	    Type ltype = left.type() ;
	    Type rtype = right.type() ;

	    if (rtype.equals(Type._boolean()) &&
	        ltype.equals(Type._boolean())) {
		return new BinaryOperatorExpression( ef, Type._boolean(), 
		    left, op, right ) ;
	    } else if (ltype.isNumber() && rtype.isNumber()) {
		Expression lb = left ;
		Expression rb = right ;

		Type ctype = left.type().binaryPromotion( right.type() ) ;
		if (!ctype.equals( left.type() ))
		    lb = ef.cast( ctype, lb ) ;
		if (!ctype.equals( right.type() ))
		    rb = ef.cast( ctype, rb ) ;

		return new BinaryOperatorExpression( ef, Type._boolean(), 
		    lb, op, rb ) ;
	    } else {
		boolean lok = !ltype.isPrimitive() || ltype.equals( Type._null() ) ;
		boolean rok = !rtype.isPrimitive() || rtype.equals( Type._null() ) ;

		if (lok && rok)
		    return new BinaryOperatorExpression( ef, Type._boolean(), 
			left, op, right ) ;
		else
		    throw new IllegalArgumentException( "Both arguments to " 
			+ op.javaRepresentation()  
			+ " must be of reference or null type.  left type = " 
			+ ltype.name() + " right type = " + rtype.name() ) ;
	    }
	}

	public String javaRepresentation() {
	    return javaRepresentation ;
	}
    
	public abstract Expression create( ExpressionFactory ef,
	    Expression left, Expression right ) ;

	public BinaryOperatorKind kind() {
	    return BinaryOperatorKind.RELATIONAL ;
	}

	BinaryOperator( String javaRepresentation ) {
	    this.javaRepresentation = javaRepresentation ;
	}
    }

    // Note that left and right can be replaced, in order to re-write the
    // tree for numeric type conversions
    public static final class BinaryOperatorExpression extends ExpressionBase {
	private Expression left ;
	private BinaryOperator op ;
	private Expression right ;
	private Type type ;

	BinaryOperatorExpression( ExpressionFactory ef, Type type, 
	    Expression left, BinaryOperator op, Expression right ) {

	    super( ef ) ;
	    this.type = type ;
	    this.left = left.copy(this, Expression.class);
	    this.op = op ;
	    this.right = right.copy(this, Expression.class);
	}

	public BinaryOperator operator() {
	    return op ;
	}

	public Expression left() {
	    return left ;
	}

	public Expression right() {
	    return right ;
	}

	public Type type() {
	    return type ;
	}

	@Override
	public void accept( Visitor visitor ) {
	    visitor.visitBinaryOperatorExpression( this ) ;
	}

	@Override
	public String toString() {
	    return "BinaryOperatorExpression" + Util.getNodeIdString(this) 
		+ "[" + op + "]" ;
	}
    }

    public Expression binaryOperator( Expression left, BinaryOperator op,
	Expression right ) {
	return op.create( this, left, right ) ;
    }

//--------------- CAST EXPRESSION ----------------------------------------

    public static final class CastExpression extends ExpressionBase {
	private Type type ;
	private Expression expr ;

	CastExpression( ExpressionFactory ef, Type type, Expression expr ) {
	    super( ef ) ;
	    this.type = type ;
	    this.expr = expr.copy(this, Expression.class);
	}

	public Expression expr() {
	    return expr ;
	}

	public Type type() {
	    return type ;
	}

	@Override
	public void accept( Visitor visitor ) {
	    visitor.visitCastExpression( this ) ;
	}

	@Override
	public String toString() {
	    return "CastExpression" + Util.getNodeIdString(this) 
		+ "[" + type.name() + "]" ;
	}
    }

    public Expression cast( Type type, Expression expr ) {
	return new CastExpression( this, type, expr ) ;
    }

//--------------- INSTOF EXPRESSION ----------------------------------------

    public static final class InstofExpression extends ExpressionBase {
	private Expression expr ;
	private Type itype ;

	InstofExpression( ExpressionFactory ef, Expression expr, Type type ) {
	    super( ef ) ;
	    this.expr = expr.copy(this, Expression.class);
	    this.itype = type ;
	}

	public Expression expr() {
	    return expr ;
	}

	public Type itype() {
	    return itype ;
	}

	public Type type() {
	    return Type._boolean() ;
	}

	@Override
	public void accept( Visitor visitor ) {
	    visitor.visitInstofExpression( this ) ;
	}

	@Override
	public String toString() {
	    return "InstofExpression" + Util.getNodeIdString(this) 
		+ "[" + itype.name() + "]" ;
	}
    }

    public Expression instof( Expression expr, Type type ) {
	return new InstofExpression( this, expr, type ) ;
    }

//--------------- NEWOBJ EXPRESSION ----------------------------------------

    public static final class NewObjExpression extends ExpressionBase {
	private Type type ;
	private Signature signature ;
	private List<Expression> args ;

	NewObjExpression( ExpressionFactory ef, Type type, Signature signature,
	    List<Expression> args ) {
	    super( ef ) ;
	    this.type = type ;
	    this.signature = signature ;
	    if (!signature.returnType().equals(Type._void()))
		throw new IllegalArgumentException( 
		    "The signature of a new call to a constructor must have a void return type" ) ;
	    this.args = copyExpressionList(this, args) ;
	}

	public final Signature signature() {
	    return this.signature ;
	}

	public final List<Expression> args() {
	    return this.args ;
	}

	public Type type() {
	    return type ;
	}

	@Override
	public void accept( Visitor visitor ) {
	    visitor.visitNewObjExpression( this ) ;
	}

	@Override
	public String toString() {
	    return "NewObjExpression" + Util.getNodeIdString(this) 
		+ "[" + type.name() + " " +
		signature + "]" ;
	}
    }

    public Expression newObj( Type type, Signature signature, List<Expression> args ) {
	signature.checkConstructorCompatibility( type, args ) ;
	return new NewObjExpression( this, type, signature, args ) ;
    }

    public Expression newObj( Type type, List<Expression> exprs ) {
	Signature signature = Signature.fromConstructor( type, exprs ) ;
	return new NewObjExpression( this, type, signature, exprs ) ;
    }

//--------------- NEWARR EXPRESSION ----------------------------------------

    public static final class NewArrExpression extends ExpressionBase {
	private Type ctype ;
	private Expression size ;
	private List<Expression> exprs ;

	NewArrExpression( ExpressionFactory ef, Type ctype, Expression size, 
	    List<Expression> exprs ) {
	    super( ef ) ;
	    this.ctype = ctype ;
	    this.size = size.copy( this, Expression.class ) ;
	    this.exprs = copyExpressionList( this, exprs ) ;
	}

	public Type ctype() {
	    return ctype ;
	}

	public Expression size() {
	    return size ;
	}

	public List<Expression> exprs() {
	    return exprs ;
	}

	public Type type() {
	    return Type._array( ctype ) ;
	}

	@Override
	public void accept( Visitor visitor ) {
	    visitor.visitNewArrExpression( this ) ;
	}

	@Override
	public String toString() {
	    return "NewArrExpression" + Util.getNodeIdString(this) 
		+ "[" + ctype.name() + "]" ;
	}
    }

    public Expression newArrInit( Type type, List<Expression> exprs ) {
	Expression size = _const( exprs.size() ) ;
	return new NewArrExpression( this, type, size, exprs ) ;
    }

    public Expression newArr( Type type, Expression size ) {
	return new NewArrExpression( this, type, size, null ) ;
    }

//--------------- SUPER CALL EXPRESSION ----------------------------------------

    public static final class SuperCallExpression extends ExpressionBase {
	private String ident ;
	private Signature signature ;
	private List<Expression> exprs ;

	SuperCallExpression( ExpressionFactory ef, String ident, 
	    Signature signature, List<Expression> exprs ) {
	    super( ef ) ;
	    this.ident = ident ;
	    this.signature = signature ;
	    this.exprs = copyExpressionList( this, exprs ) ;
	}
	
	public String ident() {
	    return ident ;
	}

	public Signature signature() {
	    return signature ;
	}

	public List<Expression> exprs() {
	    return exprs ;
	}

	public Type type() {
	    return signature.returnType() ;
	}

	@Override
	public void accept( Visitor visitor ) {
	    visitor.visitSuperCallExpression( this ) ;
	}

	@Override
	public String toString() {
	    return "SuperCallExpression" + Util.getNodeIdString(this) 
		+ "[" + ident + " " + signature + "]" ;
	}
    }

    public Expression superCall( String ident, Signature signature, 
	List<Expression> exprs ) {
	return new SuperCallExpression( this, ident, signature, exprs ) ;
    }

    public Expression superCall( String ident, List<Expression> exprs ) {
	ClassGenerator cg = efparent().getAncestor(ClassGenerator.class) ;
	if (cg == null)
	    throw new IllegalStateException(
		"No ClassGenerator found!" ) ;
	Type type = cg.superType() ;
	Signature signature = Signature.fromCall( type, ident, exprs ) ;
	return new SuperCallExpression( this, ident, signature, exprs ) ;
    }

    //--------------- SUPER OBJ EXPRESSION (super at start of constructor) ----------

    public static final class SuperObjExpression extends ExpressionBase {
	private Signature signature ;
	private List<Expression> exprs ;

	SuperObjExpression( ExpressionFactory ef, Signature signature,
	    List<Expression> exprs ) {
	    super( ef ) ;
	    this.signature = signature ;
	    this.exprs = copyExpressionList( this, exprs ) ;
	}

	public Signature signature() {
	    return signature ;
	}

	public List<Expression> exprs() {
	    return exprs ;
	}

	public Type type() {
	    return Type._void() ;
	}

	@Override
	public void accept( Visitor visitor ) {
	    visitor.visitSuperObjExpression( this ) ;
	}

	@Override
	public String toString() {
	    return "SuperObjExpression" + Util.getNodeIdString(this) 
		+ "[" + signature + "]" ;
	}
    }

    /** Call to superclass constructor.  Must be first in the method.
     */
    public Expression superObj( Signature signature, List<Expression> exprs ) {
	return new SuperObjExpression( this, signature, exprs ) ;
    }

    /** Call to superclass constructor.  Must be first in the method.
     * This is a shorthand form that computes the Signature directly
     * from the Expression list exprs.
     */
    public Expression superObj( List<Expression> exprs ) {
	ClassGenerator cg = efparent().getAncestor(ClassGenerator.class) ;
	if (cg == null)
	    throw new IllegalStateException(
		"No ClassGenerator found!" ) ;
	Type type = cg.superType() ;
	Signature signature = Signature.fromConstructor( type, exprs ) ;
	return new SuperObjExpression( this, signature, exprs ) ;
    }

//--------------- THIS OBJ EXPRESSION (this at start of constructor) ----------

    public static final class ThisObjExpression extends ExpressionBase {
	private Signature signature ;
	private List<Expression> exprs ;

	ThisObjExpression( ExpressionFactory ef, Signature signature,
	    List<Expression> exprs ) {
	    super( ef ) ;
	    this.signature = signature ;
	    this.exprs = copyExpressionList( this, exprs ) ;
	}

	public Signature signature() {
	    return signature ;
	}

	public List<Expression> exprs() {
	    return exprs ;
	}

	public Type type() {
	    return Type._void() ;
	}

	@Override
	public void accept( Visitor visitor ) {
	    visitor.visitThisObjExpression( this ) ;
	}

	@Override
	public String toString() {
	    return "ThisObjExpression" + Util.getNodeIdString(this) 
		+ "[" + signature + "]" ;
	}
    }

    /** Call to another constructor.  Must be first in the method.
     */
    public Expression thisObj( Signature signature, List<Expression> exprs ) {
	return new ThisObjExpression( this, signature, exprs ) ;
    }

    /** Call to another constructor.  Must be first in the method.
     * This is a shorthand form that computes the Signature directly
     * from the Expression list exprs.
     */
    public Expression thisObj( List<Expression> exprs ) {
	ClassGenerator cg = efparent().getAncestor(ClassGenerator.class) ;
	if (cg == null)
	    throw new IllegalStateException(
		"No ClassGenerator found!" ) ;
	Type type = cg.thisType() ;
	Signature signature = Signature.fromConstructor( type, exprs ) ;
	return new ThisObjExpression( this, signature, exprs ) ;
    }

//--------------- Field Access Expression -------------------------------------

    public static abstract class FieldAccessExpressionBase<T> extends ExpressionBase {
	private T target ;
	private String fieldName ;

	FieldAccessExpressionBase( ExpressionFactory ef, String fieldName ) {
	    super( ef ) ;
	    this.fieldName = fieldName ;
	}

	public boolean isAssignable() {
	    // XXX What if this field is final?  
	    return true ;
	}

	abstract boolean isStatic() ;

	abstract Type targetType() ;

	public T target() {
	    return target ;
	}

	public void target( T arg ) {
	    this.target = arg ;
	}

	public String fieldName() {
	    return fieldName ;
	}

	public Type type() {
	    ClassInfo cinfo = targetType().classInfo() ;
	    FieldInfo fld = cinfo.findFieldInfo( fieldName) ;
	    if (fld == null)
		throw new IllegalStateException( 
		    "Type " + targetType().name() + " does not contain field " +
		    fieldName ) ;

	    ClassGenerator defClass = getAncestor( ClassGenerator.class ) ;

	    if (fld.isAccessibleInContext( defClass, cinfo ))
		return fld.type()  ;
	    else 
		throw new IllegalStateException( 
		    "Field " + fieldName + " in type " + targetType().name() 
		    + "is not accessible in context " + defClass.name() ) ;
	}
    }

    public static final class NonStaticFieldAccessExpression extends 
	FieldAccessExpressionBase<Expression> {

	NonStaticFieldAccessExpression( ExpressionFactory ef, Expression target, 
	    String fieldName ) {
	    super( ef, fieldName ) ;
	    target( target.copy(this, Expression.class) ) ;
	}

	@Override
	public boolean isStatic() {
	    return false ;
	}

	@Override
	public Type targetType() {
	    return target().type() ;
	}

	@Override
	public void accept( Visitor visitor ) {
	    visitor.visitNonStaticFieldAccessExpression( this ) ;
	}
	
	@Override
	public String toString() {
	    return "NonStaticFieldAccessExpression" + Util.getNodeIdString(this) 
		+ "[" + fieldName() + "]" ;
	}
    }

    public static final class StaticFieldAccessExpression extends 
	FieldAccessExpressionBase<Type> {

	StaticFieldAccessExpression( ExpressionFactory ef, Type target, 
	    String fieldName ) {
	    super( ef, fieldName ) ;
	    target( target ) ;
	}

	@Override
	public boolean isStatic() {
	    return true ;
	}

	@Override
	public Type targetType() {
	    return target() ;
	}

	@Override
	public void accept( Visitor visitor ) {
	    visitor.visitStaticFieldAccessExpression( this ) ;
	}

	@Override
	public String toString() {
	    return "StaticFieldAccessExpression" + Util.getNodeIdString(this) 
		+ "[" + target().name() + " " + 
		fieldName() + "]" ;
	}
    }

    public Expression fieldAccess( Expression target, String fieldName ) {
	return new NonStaticFieldAccessExpression( this, target, fieldName ) ;
    }

    public Expression fieldAccess( Type target, String fieldName ) {
	return new StaticFieldAccessExpression( this, target, fieldName ) ;
    }

//--------------- Array Index Expression -------------------------------------

    public static final class ArrayIndexExpression extends ExpressionBase {
	private Expression expr ;
	private Expression index ;

	ArrayIndexExpression( ExpressionFactory ef, Expression expr, Expression index ) {
	    super( ef ) ;
	    this.expr = expr.copy(this, Expression.class);
	    this.index = index.copy(this, Expression.class);
	}

	public boolean isAssignable() {
	    return true ;
	}

	public Expression expr() {
	    return expr ;
	}

	public Expression index() {
	    return index ;
	}

	public Type type() {
	    Type atype = expr.type().memberType() ;
	    return atype ;
	}

	@Override
	public void accept( Visitor visitor ) {
	    visitor.visitArrayIndexExpression( this ) ;
	}

	@Override
	public String toString() {
	    return "ArrayIndexExpression" + Util.getNodeIdString(this) 
		+ "[]" ;
	}
    }

    public Expression arrayIndex( Expression expr, Expression index ) {
	return new ArrayIndexExpression( this, expr, index ) ;
    }

//--------------- Array Length Expression -------------------------------------

    public static final class ArrayLengthExpression extends ExpressionBase {
	private Expression expr ;

	ArrayLengthExpression( ExpressionFactory ef, Expression expr ) {
	    super( ef ) ;
	    this.expr = expr.copy(this, Expression.class);
	}

	public Expression expr() {
	    return expr ;
	}

	public Type type() {
	    return Type._int() ;
	}

	@Override
	public void accept( Visitor visitor ) {
	    visitor.visitArrayLengthExpression( this ) ;
	}

	@Override
	public String toString() {
	    return "ArrayLengthExpression" + Util.getNodeIdString(this) 
		+ "[]" ;
	}
    }

    public Expression arrayLength( Expression expr ) {
	return new ArrayLengthExpression( this, expr ) ;
    }
    
//--------------- If expression -------------------------------------

    public static final class IfExpression extends ExpressionBase {
	private Expression condition ;
	private Expression truePart ;
	private Expression falsePart ;
	private Type type ;

	IfExpression( final ExpressionFactory ef, final Expression condition, 
	    final Expression truePart, final Expression falsePart ) {

	    super( ef ) ;
	    this.condition = condition.copy( this, Expression.class ) ;
	    this.truePart = truePart.copy( this, Expression.class ) ;
	    this.falsePart = falsePart.copy( this, Expression.class ) ;

	    // See section 15.24 to compute the type of the conditional expression
	    final Type ttype = truePart.type() ;
	    final Type ftype = falsePart.type() ;
	    if (ttype.equals( ftype )) {
		type = ttype ;
	    } else if (ttype.isNumber() && ftype.isNumber()) {
		Set<Type> special = new HashSet<Type>( 
		    Arrays.asList( Type._byte(), Type._short() ) ) ;
		Set<Type> argTypes = new HashSet<Type>(
		    Arrays.asList( ttype, ftype ) ) ;
		if (special.equals( argTypes )) {
		    type = Type._short() ;
		} else {
		    type = ttype.binaryPromotion( ftype ) ;
		    if (!ttype.equals( type ))
			this.truePart = ef.cast( type, this.truePart ) ;
		    if (!ftype.equals( type ))
			this.falsePart = ef.cast( type, this.falsePart ) ;
		}
	    } else { // handle the reference cases
		if (ttype.equals(Type._null()) && !ftype.isPrimitive()) {
		    type = ftype ;
		} else if (ftype.equals(Type._null()) && !ttype.isPrimitive()) {
		    type = ttype ;
		} else { // both types are reference types
		    if (ttype.isAssignmentConvertibleFrom( ftype ))
			type = ttype ;
		    else if (ftype.isAssignmentConvertibleFrom( ttype ))
			type = ftype ;
		}
	    }
	}

	public Expression condition() {
	    return condition ;
	}
	    
	public Expression truePart() {
	    return this.truePart ;
	}
	
	public Expression falsePart() {
	    return this.falsePart ;
	}

	public Type type() {
	    return type ;
	}

	@Override
	public void accept( Visitor visitor ) {
	    visitor.visitIfExpression( this ) ;
	}

	@Override 
	public String toString() {
	    return "IfExpression" + Util.getNodeIdString(this) 
		+ "[type=" + type.name() + "]" ;
	}
    }

    public Expression ifExpression( Expression condition, Expression truePart,
	Expression falsePart ) {

	return new IfExpression( this, condition, truePart, falsePart ) ;
    }

//----------------Variable---------------------------------------------
//
    public static final class VariableImpl 
	extends ExpressionFactory.ExpressionBase 
	implements Variable {

	private Type type ;
	private String ident ;
	private boolean available = true ;
      
	VariableImpl( ExpressionFactory ef, Type type, String ident ) {
	    super( ef ) ;

	    if (!Identifier.isValidIdentifier( ident ))
		throw new IllegalArgumentException( ident + 
		    " is not a valid Java identifier name" ) ;
	    this.type = type ;
	    this.ident = ident ;
	}

	@Override
	public boolean isAssignable() {
	    return true ;
	}

	public String ident() {
	    return ident ;
	}

	public boolean isAvailable() {
	    return available ;
	}
	
	public void close() {
	    available = false ;
	}

	@Override
	public int hashCode() {
	    return type.hashCode() ^ ident.hashCode() ;
	}

	@Override
	public boolean equals( Object obj ) {
	    if (!(obj instanceof Variable))
		return false ;

	    if (obj == this) 
		return true ;

	    Variable other = Variable.class.cast( obj ) ;

	    return type.equals(other.type()) &&
		ident.equals(other.ident()) ; 
	}

	@Override
	public String toString() {
	    return "VariableImpl" + Util.getNodeIdString(this) 
		+ "[" + type.name() + " " + ident + "]" ;
	}

	public Type type() {
	    return type ;
	}
	

	@Override
	public void accept( Visitor visitor ) {
	    visitor.visitVariable( this ) ;
	}
    }

    public Variable variable( Type type, String ident ) {
	return new VariableImpl( this, type, ident ) ;
    }
}
