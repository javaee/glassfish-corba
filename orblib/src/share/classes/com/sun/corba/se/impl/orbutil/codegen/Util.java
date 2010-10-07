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

package com.sun.corba.se.impl.orbutil.codegen;

import java.io.PrintStream ;

import java.util.Set ;
import java.util.Map ;
import java.util.IdentityHashMap ;

import java.lang.reflect.Modifier ;

import com.sun.corba.se.spi.orbutil.codegen.Type ;
import com.sun.corba.se.spi.orbutil.codegen.Variable ;

public final class Util {
    private Util() {}

    /** Make sure that all variables reachable from node
     * are marked not available, indicating that they are
     * no longer in scope.  Note that only defining occurrences
     * of variables are to be considered here, as a scope
     * that merely references a variable may be closed while
     * the containing defining scope is still open.
     */
    public static void close( Node node ) {
	TreeWalkerContext context = new TreeWalkerContext() ;
	Visitor visitor = new TreeWalker( context ) {
	    private Variable savedVariable = null ;

	    @Override
	    public void blockStatementBeforeBodyStatement( BlockStatement arg, 
		Statement stmt ) {
		if (stmt instanceof DefinitionStatement) {
		    DefinitionStatement ds = 
			DefinitionStatement.class.cast( stmt ) ;
		    ((VariableInternal)ds.var()).close() ;
		}
	    }
	  
	    @Override
	    public void visitMethodGenerator( MethodGenerator arg ) {
		for (Variable var : arg.arguments()) {
		    ((VariableInternal)var).close() ;
		}
	    }

	    @Override
	    public void tryStatementBeforeBlock( TryStatement arg,
		Type type, Variable var, BlockStatement block ) {

		((VariableInternal)var).close() ;
	    }
	} ;
	context.push( visitor ) ;
	node.accept( visitor ) ;
    }

    /** Throw an exception if any ExpressionInternal reachable
     * from expr contains a Variable that is out of scope.
     * Note that this is only useful for Expressions.
     */
    public static void checkScope( ExpressionInternal expr ) {
	TreeWalkerContext context = new TreeWalkerContext() ;
	Visitor visitor = new TreeWalker( context ) {
	    @Override
	    public boolean preVariable( Variable arg ) {
		if (!((VariableInternal)arg).isAvailable())
		    throw new IllegalStateException( arg + " is no longer in scope" ) ;
		return false ;
	    }
	} ;
	context.push( visitor ) ;
	expr.accept( visitor ) ;
    }

    /** Check that node is really a tree, that is, when we traverse it
     * with the visitor, we never encounter the same node twice.
     */
    public static void checkTree( final Node node, final PrintStream pw ) {
	// Set up a visitor that counts how many times each unique node
	// object is encountered, then visit node.
	TreeWalkerContext context = new TreeWalkerContext() ;
	final Map<Node,Integer> map = new IdentityHashMap<Node,Integer>() ;
	Visitor visitor = new TreeWalker( context ) {
	    @Override
	    public boolean preNode( Node arg ) {
		Integer val = map.get( arg ) ;
		if (val == null) {
		    val = 1 ;
		} else {
		    val++ ;
		}
		map.put( arg, val ) ;
		return false ;
	    }
	} ;
	context.push( visitor ) ;
	node.accept( visitor ) ;

	// Print out any Nodes that appear more than once
	// in the tree rooted at node.
	for (Map.Entry<Node,Integer> entry : map.entrySet()) {
	    if (entry.getValue() > 1) {
		pw.print( "Node " + entry.getKey() 
		    + " appeared " + entry.getValue() + " times in the AST" ) ;
	    }
	}
    }


    // Display all attributes of node.  If the value of an attribute is itself
    // a node, display its attributes as well (recursively).  At the end,
    // leave the Printer indented one level.
    private static void displayAttributes( final Node node, final CodegenPrinter pr ) {
	Set<Attribute<?>> attrs = Attribute.getAttributes( node ) ;
	for (Attribute<?> attr : attrs) {
	    String typeName = attr.type().getName() ;
	    int lastIndex = typeName.lastIndexOf( '.' ) ;
	    if (lastIndex >= 0)
		typeName = typeName.substring( lastIndex + 1 ) ;
	    Object obj = attr.get(node) ;
	    String value = attr.get( node ).toString() ;
	    pr.nl().p("|__:").p(attr.name()).p(":").p(typeName).p("=").p(value) ;

	    if (obj instanceof Node) {
		pr.in() ;
		displayAttributes( Node.class.cast(obj), pr ) ;
		pr.out() ;
	    }
	}
    }

    public static String getNodeIdString( Node obj ) {
	return "@" + obj.id() ;
    }

    /** Display a tree for the node.
     */
    public static void display( final Node node, final PrintStream pw ) {
	TreeWalkerContext context = new TreeWalkerContext() ;
	Visitor visitor = new TreeWalker( context ) {

	    final CodegenPrinter pr = new CodegenPrinter( pw, 2, '.' ) ;

	    private boolean done( Node node ) {
		displayAttributes( node, pr ) ;
		pr.in() ;
		return true ;
	    }

	    // Use this if no more specific type has a defined display
	    // behavior in this visitor.
	    @Override
	    public boolean preNode( Node arg ) {
		pr.nl().p("Node").p(getNodeIdString(arg))
		    .p("[").p(arg.toString()).p("]") ;

		return done( arg ) ;
	    }

	    @Override
	    public void postNode( Node arg ) {
		pr.out() ;
	    }

	    @Override
	    public boolean preClassGenerator( ClassGeneratorImpl arg ) {
		pr.nl().p("ClassGenerator").p(getNodeIdString(arg))
		    .p("[").p(Modifier.toString(arg.modifiers()))
		    .p(" ").p((arg.isInterface() ? "interface" : "class"))
		    .p(" ").p(arg.name()).p("]") ;
		
		return done( arg ) ;
	    }

	    @Override
	    public boolean preMethodGenerator( MethodGenerator arg ) {
		pr.nl().p( "MethodGenerator").p(getNodeIdString(arg))
		    .p("[").p(Modifier.toString(arg.modifiers()))
		    .p(" ").p(arg.returnType().name()).p(" ").p(arg.name()).p("]") ;

		return done( arg ) ;
	    }

	    @Override
	    public boolean preThrowStatement( ThrowStatement arg ) {
		pr.nl().p("ThrowStatement").p(getNodeIdString(arg)) ;

		return done( arg ) ;
	    }

	    @Override
	    public boolean preAssignmentStatement( AssignmentStatement arg ) {
		pr.nl().p("AssignmentStatement").p(getNodeIdString(arg)) ;

		return done( arg ) ;
	    }

	    @Override
	    public boolean preBlockStatement( BlockStatement arg ) {
		pr.nl().p("BlockStatement").p(getNodeIdString(arg)) ;

		return done( arg ) ;
	    }

	    @Override
	    public boolean preCaseBranch( CaseBranch arg ) {
		pr.nl().p("CaseBranch").p(getNodeIdString(arg))
		    .p("[").p(Integer.toString(arg.label())).p("]") ;

		return done( arg ) ;
	    }

	    @Override
	    public boolean preDefinitionStatement( DefinitionStatement arg ) {
		pr.nl().p("DefinitionStatement").p(getNodeIdString(arg)) ;

		return done( arg ) ;
	    }

	    @Override
	    public boolean preIfStatement( IfStatement arg ) {
		pr.nl().p("IfStatement").p(getNodeIdString(arg)) ;

		return done( arg ) ;
	    }

	    @Override
	    public void ifStatementBeforeTruePart( IfStatement arg ) {
		pr.out().nl().p("IfStatement:true").in() ;
	    }

	    @Override
	    public boolean ifStatementBeforeFalsePart( IfStatement arg ) {
		pr.out().nl().p("IfStatement:false").in() ;
		return true ;
	    }

	    @Override
	    public boolean preBreakStatement( BreakStatement arg ) {
		pr.nl().p("BreakStatement").p(getNodeIdString(arg)) ;

		return done( arg ) ;
	    }

	    @Override
	    public boolean preReturnStatement( ReturnStatement arg ) {
		pr.nl().p("ReturnStatement").p(getNodeIdString(arg)) ;

		return done( arg ) ;
	    }

	    @Override
	    public boolean preSwitchStatement( SwitchStatement arg ) {
		pr.nl().p("SwitchStatement").p(getNodeIdString(arg)) ;

		return done( arg ) ;
	    }

	    @Override
	    public boolean preTryStatement( TryStatement arg ) {
		pr.nl().p("TryStatement").p(getNodeIdString(arg)) ;	

		return done( arg ) ;
	    }

	    @Override
	    public void tryStatementBeforeBlock( TryStatement arg,
		Type type, Variable var, BlockStatement block ) {
                VariableInternal ivar = (VariableInternal)var ;
		pr.out().nl().p("TryStatement:catch[").p("type=").p(type.name())
		    .p(",var=").p(ivar.ident()).p("]").in() ;
		ivar.accept( this ) ;
	    }

	    @Override
	    public boolean tryStatementBeforeFinalPart( TryStatement arg ) {
		pr.out().nl().p("TryStatement:finally").in() ;

		return true ;
	    }

	    @Override
	    public boolean preWhileStatement( WhileStatement arg ) {
		pr.nl().p("WhileStatement").p(getNodeIdString(arg)) ;

		return done( arg ) ;
	    }

	    @Override
	    // All Expressions define a usable toString, so they all
	    // can be handled by this method.
	    public boolean preExpression( ExpressionInternal arg ) {
		pr.nl().p(arg.toString()) ;

		return done( arg ) ;
	    }

	    @Override
	    public boolean preFieldGenerator( FieldGenerator arg ) {
		pr.nl().p(arg.toString()) ;

		return done( arg ) ;
	    }

	} ;
	context.push( visitor ) ;
	node.accept( visitor ) ;
    }
}
