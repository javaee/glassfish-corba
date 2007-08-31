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

import java.util.List ;
import java.util.ArrayList ;
import java.util.Map ;
import java.util.HashMap ;

import com.sun.corba.se.spi.orbutil.codegen.Expression ;
import com.sun.corba.se.spi.orbutil.codegen.Type ;
import com.sun.corba.se.spi.orbutil.codegen.Variable ;

import com.sun.corba.se.impl.orbutil.codegen.StatementBase ;

/** Main factory for creating statements.  Represents a block
 * of statements which also defines a scope for local variable
 * declarations.
 *
 * @author Ken Cavanaugh
 */
public class BlockStatement extends StatementBase {
    private List<Statement> body ;
    private ExpressionFactory efactory ;

    // All definitions are present in body.  This
    // map is used to make sure that an identifier
    // is only defined once within a scope.
    private Map<String,DefinitionStatement> definitions ;

    BlockStatement( Node parent ) {
        super( parent ) ;
        body = new ArrayList<Statement>() ;
	efactory = new ExpressionFactory( this ) ;
	definitions = new HashMap<String,DefinitionStatement>() ;
    }

    /** Look up the ident to see if it has an associated Variable in
     * this block.
     */
    public Variable getVar( String ident ) {
	Variable result = null ;
	DefinitionStatement ds = definitions.get( ident ) ;
	if (ds != null)
	    result = ds.var() ;
	return result ;
    }

    /** Return true iff this BlockStatement contains no local variables
     * or statements.
     */
    public boolean isEmpty() {
	return body.isEmpty() ;
    }

    /** Return the list of Statements in this BlockStatement.
     */
    public List<Statement> body() {
	return body ;
    }

    /** Return the ExpressionFactory that must be used to create
     * any expressions occuring either in expressions added to
     * the body, or in other statements immediately contained 
     * in this BlockStatement.
     */
    public ExpressionFactory exprFactory() {
	return efactory ;
    }
    
    /** Add a break statement to this BlockStatement.
     */
    public void addBreak() {
	body.add( new BreakStatement( this )) ;
    }

    /** Add an empty return to this BlockStatement.  The enclosing 
     * MethodGenerator must have a void return type.
     */
    public void addReturn() {
	// XXX check that the return type of this method is void 
	body.add( new ReturnStatement( this )) ;
    } 

    /** Add a return with an expression to this BlockStatement.
     * The enclosing MethodGenerator must have a return type that
     * is assignment compatible with the type of expr.
     */
    public void addReturn( Expression expr ) {
	// XXX check that expr.type() matches the return type of this method
        body.add( new ReturnStatement( this, expr.copy(this, Expression.class) )) ;
    }
    
    public IfStatement addIf( Expression cond ) {
	IfStatement result = new IfStatement( this, cond.copy(this, Expression.class) ) ;
	body.add( result ) ;
	return result ;
    }
    
    public TryStatement addTry() {
        TryStatement result = new TryStatement( this ) ;
        body.add( result ) ;
        return result ;
    }
    
    public void addThrow( Expression expr ) {
        body.add( new ThrowStatement( this, expr.copy(this, Expression.class) )) ;
    }
   
    private void checkSwitchExpressionType( Type type ) {
	if ((type.size() != 1) || type.equals( Type._boolean()))
	    throw new IllegalArgumentException( 
		"A switch expression must have type char, byte, short, or int" ) ;
    }

    public SwitchStatement addSwitch( Expression value ) {
	checkSwitchExpressionType( value.type() ) ;
        SwitchStatement result = new SwitchStatement( this, value.copy(this, Expression.class) ) ;
        body.add( result ) ;
        return result ;
    }
    
    public WhileStatement addWhile( Expression expr ) {
        WhileStatement result = new WhileStatement( this, expr.copy(this, Expression.class) ) ;
        body.add( result ) ;
        return result ;
    }
    
    public void addExpression( Expression expr ) {
        body.add( expr.copy(this, Expression.class) ) ;
    }
    
    public void addAssign( Expression left, Expression right ) {
        body.add(new AssignmentStatement( this, 
	    left.copy(this, Expression.class), right.copy(this, Expression.class))) ;
    }
    
    public Expression addDefinition( Type type, String ident, Expression value ) {
	if (definitions.containsKey( ident ))
	    throw new IllegalArgumentException( 
		"This scope already contains a variable named " + ident ) ;
	Variable var = efactory.variable( type, ident ) ;
	DefinitionStatement ds = new DefinitionStatement( this, var, 
	    value.copy(this, Expression.class) ) ;
	body.add( ds ) ;
	definitions.put( ident, ds ) ;
	return var ;
    }
    
    public void accept( Visitor visitor ) {
	visitor.visitBlockStatement( this ) ;
    }
}
