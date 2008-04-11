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

package corba.codegen ;

import java.io.File ;
import java.io.IOException ;
import java.io.PrintStream ;
import java.io.FileInputStream ;

import java.rmi.RemoteException ;

import java.util.Set ;
import java.util.Iterator ;
import java.util.HashSet ;
import java.util.List ;
import java.util.Arrays ;
import java.util.ArrayList ;
import java.util.Properties ;

import java.lang.reflect.Method ;
import java.lang.reflect.Constructor ;
import java.lang.reflect.InvocationTargetException ;
import java.lang.reflect.InvocationHandler ;
import java.lang.reflect.Proxy ;

import java.security.ProtectionDomain ;

import javax.ejb.EJBException ;

import junit.framework.TestCase ;
import junit.framework.Test ;
import junit.framework.TestResult ;
import junit.framework.TestSuite ;

import com.sun.corba.se.spi.orbutil.codegen.Type ;
import com.sun.corba.se.spi.orbutil.codegen.Wrapper ;
import com.sun.corba.se.spi.orbutil.codegen.ClassInfo ;
import com.sun.corba.se.spi.orbutil.codegen.MethodInfo ;
import com.sun.corba.se.spi.orbutil.codegen.Variable ;
import com.sun.corba.se.spi.orbutil.codegen.GenericClass ;

import com.sun.corba.se.impl.orbutil.codegen.CurrentClassLoader ;
import com.sun.corba.se.impl.orbutil.codegen.Node ;
import com.sun.corba.se.impl.orbutil.codegen.Visitor ;
import com.sun.corba.se.impl.orbutil.codegen.NodeBase ;
import com.sun.corba.se.impl.orbutil.codegen.Attribute ;
import com.sun.corba.se.impl.orbutil.codegen.Identifier ;
import com.sun.corba.se.impl.orbutil.codegen.ClassGenerator ;
import com.sun.corba.se.impl.orbutil.codegen.TreeWalkerContext ;
import com.sun.corba.se.impl.orbutil.codegen.CodeGenerator ;
import com.sun.corba.se.impl.orbutil.codegen.ASMSetupVisitor ;
import com.sun.corba.se.impl.orbutil.codegen.ASMByteCodeVisitor ;
import com.sun.corba.se.impl.orbutil.codegen.Util ;

import com.sun.corba.se.spi.orbutil.generic.NullaryFunction ;
import com.sun.corba.se.spi.orbutil.generic.Pair ;

import com.sun.corba.se.spi.orbutil.copyobject.DefaultCopier ;

import static com.sun.corba.se.spi.orbutil.codegen.Wrapper.* ;

import org.objectweb.asm.ClassWriter ;

import corba.codegen.lib.EchoInt ;
import corba.codegen.lib.Constants ;
import corba.codegen.test.Constants_gen ;

import corba.framework.TestCaseTools ;

import static corba.codegen.ControlBase.moa ;

/** Test for the ASM-based codegen library.  
 * Initial steps to test:
 * <OL>
 * <LI>Test the _DImpl_Tie_gen code
 * <LI>Test the EJB3 code
 * </OL>
 * There will be more tests later.  Here we need to test
 * two cases:
 * <OL>
 * <LI>Generate Java source code, compile it, load it.
 * <LI>Generate Bytecode directly, load it.
 * <LI>
 * These will be unified behind a simple CodeGenerator interface.
 * All tests will be written at the Wrapper level.
 * <P>
 * Other required tests:
 * <UL>
 * <LI>ClassInfo/MethodInfo for Classes
 * <LI>Type
 * <LI>Signature
 * <LI>Dynamic attributes
 * <LI>Method overload resolution
 * </UL>
 */
public class Client extends TestCase {
    private static final boolean DEBUG = false ;

    // Make sure that ControlBase is loaded in the ClassLoader
    // that loaded Client, otherwise it could first be
    // loaded in a TestClassLoader (see GenerationTestSuiteBase)
    // rather than in the parent to which TestClassLoader delegates.
    private static final Object cb = new ControlBase() ;

    public Client() {
    }

    public Client( String name ) {
	super( name ) ;
    }

    private ClassLoader makeClassLoader() {
	ClassLoader cl = new GenerationTestSuiteBase.TestClassLoader(
	    this.getClass().getClassLoader() ) ;
	return cl ;
    }

    public void testTieSource() {
	JavaCodeGenerator gen = new JavaCodeGenerator( 
	    ClassGeneratorFactoryRegistry.get( "_DImpl_Tie" )) ;
	Class<?> cls = gen.generate( makeClassLoader() ) ;
        if (DEBUG)
            gen.reportTimes() ;
	assertNotNull( cls ) ;
    }

    private ClassInfo getClassInfo( SimpleCodeGenerator cg ) {
	ClassLoader cl = makeClassLoader() ;
	Class<?> cls = cg.generate( cl ) ;
        if (DEBUG)
            cg.reportTimes() ;
	assertNotNull( cls ) ;
	Type type = Type.type( cls ) ;
	return type.classInfo() ;
    }

    public void testEJBRemoteInterface() {
	ClassGeneratorFactory myRemoteFactory = 
	    ClassGeneratorFactoryRegistry.get( "MyRemote" ) ;

	JavaCodeGenerator jgen = new JavaCodeGenerator( myRemoteFactory ) ;
	ClassInfo jci = getClassInfo( jgen ) ;

	ByteCodeGenerator bgen = new ByteCodeGenerator(myRemoteFactory, 
	    GenerationTestSuiteBase.getByteCodeGenerationProperties( DEBUG )) ;
	ClassInfo bci = getClassInfo( bgen ) ;

	assertEquals( jci, bci ) ;
    }

    public void testEJBAdapterSource() {
	JavaCodeGenerator gen = new JavaCodeGenerator( 
	    ClassGeneratorFactoryRegistry.get( "MyRemote__Adapter" ) ) ;
	Class<?> cls = gen.generate( makeClassLoader() ) ;
        if (DEBUG)
            gen.reportTimes() ;
	assertNotNull( cls ) ;
    }

    public void testConstantGeneration() {
	ClassGeneratorFactory constantsFactory = 
	    ClassGeneratorFactoryRegistry.get( "ConstantsImpl" ) ;

	constantsFactory.evaluate() ;

	GenericClass<Constants> genClass = null ;

	try {
	    genClass = _generate( Constants.class, 
		GenerationTestSuiteBase.getByteCodeGenerationProperties( DEBUG )) ;
	} catch (Exception exc) {
	    fail( "Unexpected exception " + exc + " in _generate for Constants" ) ;
	}

	Constants constants = genClass.create() ;

	for (Method m : constants.getClass().getDeclaredMethods()) {
	    String name = m.getName() ;
	    int expectedValue = Constants_gen.getValue( name ) ;

	    try {
		int actualValue = (Integer)m.invoke( constants ) ;
		assertEquals( expectedValue, actualValue ) ;
	    } catch (Exception exc) {
		fail( "Unexpected exception " + exc + " in call to " + m ) ;
	    }
	}
    }

    public static class DynamicAttributeTestSuite extends TestCase {
	public DynamicAttributeTestSuite() {
	    super() ;
	}

	public DynamicAttributeTestSuite( String name ) {
	    super( name ) ;
	}

	// Declare some attributes of different types
	private static final Attribute<String> foo = 
	    new Attribute<String>( String.class, "foo", "" ) ;

	private static final Attribute<Integer> bar = 
	    new Attribute<Integer>( Integer.class, "bar", 1 ) ;

	private interface StringList extends List<String> { } 

	private static class StringArrayList extends ArrayList<String> 
	    implements StringList {

	    public StringArrayList() {
		super() ;
	    }

	    public StringArrayList( List<String> list ) {
		super( list ) ;
	    }
	}

	private static final NullaryFunction<StringList> rgbl =
	    new NullaryFunction<StringList>() {
		public StringList evaluate() {
		    return new StringArrayList( Arrays.asList( 
			"red", "blue", "green" ) ) ;
		} 
	    };

	private static final Attribute<Integer> notUsed = 
	    new Attribute<Integer>( Integer.class, "notUsed", 0 ) ;

	private static final Attribute<StringList> baz = 
	    new Attribute<StringList>( StringList.class, "baz", rgbl ) ;

	// Create a single node, set/get the attributes
	public void testSimpleNode() {
	    Node node = new NodeBase( null ) ;
	    assertEquals( foo.get(node), "" ) ;
	    assertEquals( bar.get(node), new Integer(1) ) ;
	    assertEquals( baz.get(node), rgbl.evaluate() ) ;

	    foo.set(node, "Raining") ;
	    assertEquals( foo.get(node), "Raining" ) ;

	    bar.set(node, 42) ;
	    assertEquals( bar.get(node), new Integer(42) ) ;

	    StringList tval = new StringArrayList( Arrays.asList( 
		"yellow", "orange" ) ) ;
	    baz.set(node, tval) ;
	    assertEquals( baz.get(node), tval ) ;

	    Set<Attribute<?>> expectedAttrs = 
		new HashSet<Attribute<?>>() ;
	    expectedAttrs.add( foo ) ;
	    expectedAttrs.add( bar ) ;
	    expectedAttrs.add( baz ) ;

	    assertEquals( expectedAttrs, 
		Attribute.getAttributes(node)) ;

	    // GenerationTestSuiteBase.displayNode( "Simple node", node ) ;
	}
	
	// Create a node with a delegate, set/get the attrs
	public void testNodeWithDelegate() {
	    // Create node1, set some attributes
	    NodeBase node1 = new NodeBase( null ) ;
	    foo.set(node1, "Raining") ;
	    bar.set(node1, 42) ;

	    // Copy node1 and then set some more attributes
	    NodeBase node2 = DefaultCopier.copy( node1, NodeBase.class ) ;
	    bar.set(node2, 13) ;
	    StringList tval = new StringArrayList( Arrays.asList( 
		"yellow", "orange" ) ) ;
	    baz.set(node2, tval) ;

	    // make sure that we get the correct value for bar
	    assertEquals( foo.get(node2), "Raining" ) ;
	    assertEquals( bar.get(node2), new Integer(13) ) ;
	    assertEquals( baz.get(node2), tval ) ;
	    
	    // set bar on node1 to a different value
	    bar.set(node1, 52) ;
	    assertEquals( bar.get(node2), new Integer(13) ) ;
	    
	    // set bar on node2 to a different value
	    bar.set(node2, 137) ;
	    assertEquals( bar.get(node2), new Integer(137) ) ;
	    assertEquals( bar.get(node1), new Integer(52) ) ;

	    // GenerationTestSuiteBase.displayNode( "Delegating node", node2 ) ;
	}
    }

    public void testClassInfo() {
	// This works by first creating a simple class using the
	// framework and the java compiler, then getting ClassInfo
	// from the resulting class and comparing the ClassInfo
	// from the ClassGenerator with the ClassInfo from the
	// generated Class.  We'll just reuse the Tie generator
	// here.
	ClassLoader cl = makeClassLoader() ;
	CurrentClassLoader.set( cl ) ;
	JavaCodeGenerator gen = new JavaCodeGenerator( 
	    ClassGeneratorFactoryRegistry.get( "_DImpl_Tie" ) ) ;
	Class<?> cls = gen.generate( cl ) ;
        if (DEBUG)
            gen.reportTimes() ;
	assertNotNull( cls ) ;

	ClassInfo cinfo = _classGenerator() ;
	ClassInfo clinfo = null ;

	try {
	    clinfo = Type.type( cls ).classInfo();
	} catch (Exception exc) {
	    fail( "Caught exception " + exc ) ;
	}

	// While the last assertEquals( cinfo, clinfo ) implies
	// the other statements, leave this as is so that 
	// an equals failure is easier to diagnose.
	assertEquals( cinfo.thisType(), clinfo.thisType() ) ;
	assertEquals( cinfo.isInterface(), clinfo.isInterface() ) ;
	assertEquals( cinfo.modifiers(), clinfo.modifiers() ) ;
	assertEquals( cinfo.name(), clinfo.name() ) ;
	assertEquals( cinfo.superType(), clinfo.superType() ) ;
	assertEquals( cinfo.impls(), clinfo.impls() ) ;
	assertEquals( cinfo.fieldInfo(), clinfo.fieldInfo() ) ;
	assertEquals( cinfo.methodInfoByName(), clinfo.methodInfoByName() ) ;
	assertEquals( cinfo.constructorInfo(), clinfo.constructorInfo() ) ;
	assertEquals( cinfo.thisType(), clinfo.thisType() ) ;
	assertEquals( cinfo, clinfo ) ;	
    }

    // Simple tests for Identifier
    public static class IdentifierTestSuite extends TestCase {
	public IdentifierTestSuite() {
	    super() ;
	}

	public IdentifierTestSuite( String name ) {
	    super( name ) ;
	}

	public void testIdentifier00() {
	    assertTrue( Identifier.isValidIdentifier( "frobenius" ) ) ;
	}

	public void testIdentifier01() {
	    assertTrue( Identifier.isValidIdentifier( "frob_123" ) ) ;
	}

	public void testIdentifier02() {
	    assertTrue( Identifier.isValidIdentifier( "_12_frob_123" ) ) ;
	}

	public void testIdentifier03() {
	    assertFalse( Identifier.isValidIdentifier( "2_frob_123" ) ) ;
	}

	public void testIdentifier04() {
	    assertTrue( Identifier.isValidFullIdentifier( "frobenius" ) ) ;
	}

	public void testIdentifier05() {
	    assertTrue( Identifier.isValidFullIdentifier( "frobenius.ert" ) ) ;
	}

	public void testIdentifier06() {
	    assertTrue( Identifier.isValidFullIdentifier( "a.b.c.d.e.f" ) ) ;
	}

	public void testIdentifier07() {
	    assertFalse( Identifier.isValidFullIdentifier( "2_frob_123" ) ) ;
	}

	public void testIdentifier08() {
	    assertFalse( Identifier.isValidFullIdentifier( "a..b" ) ) ;
	}

	public void testIdentifier09() {
	    assertFalse( Identifier.isValidFullIdentifier( "a.b." ) ) ;
	}

	public void testIdentifier10() {
	    assertFalse( Identifier.isValidFullIdentifier( ".a.b" ) ) ;
	}

	public void testIdentifier11() {
	    assertEquals( Identifier.makeFQN( "a.b", "c" ), "a.b.c" ) ;
	}

	public void testIdentifier12() {
	    assertEquals( Identifier.makeFQN( "", "c" ), "c" ) ;
	}

	public void testIdentifier13() {
	    assertEquals( Identifier.makeFQN( null, "c" ), "c" ) ;
	}

	public void testIdentifier14() {
	    assertEquals( Identifier.splitFQN( "a.b.c" ),
		new Pair( "a.b", "c" ) ) ;
	}

	public void testIdentifier15() {
	    assertEquals( Identifier.splitFQN( "c" ),
		new Pair( "", "c" ) ) ;
	}
    }

    // 4. Validate ASMSetupVisitor before trying it out result
    //    on ASMByteCodeVisitor
    //
    public void testASMSetupVisitor() {
	_clear() ;
	ClassGeneratorFactory generator = 
	    ClassGeneratorFactoryRegistry.get( "_DImpl_Tie" ) ;
	ClassLoader cl = makeClassLoader() ;
	CurrentClassLoader.set( cl ) ;
	ClassGenerator cgen = generator.evaluate() ;

	// GenerationTestSuiteBase.displayNode( 
	//     "Dump of _DImpl_Tie AST:", cgen ) ;

	TreeWalkerContext twc = new TreeWalkerContext() ;
	ASMSetupVisitor visitor = new ASMSetupVisitor( twc ) ;
	cgen.accept( visitor ) ;

	// GenerationTestSuiteBase.displayNode( 
	//     "Dump of _DImpl_Tie AST after ASMSetupVisitor:", cgen ) ;

	twc = new TreeWalkerContext() ;
	visitor = new ASMSetupVisitor( twc, ASMSetupVisitor.Mode.VERIFY ) ;
	cgen.accept( visitor ) ;
	List<ASMSetupVisitor.ErrorReport> errors = visitor.getVerificationErrors() ;
	if (errors.size() > 0) {
	    for (ASMSetupVisitor.ErrorReport report : errors) {
		System.out.println( "Error on " + report.node + ":" + report.msg ) ;
	    }
	    fail() ;
	}
    }

    public static abstract class DefaultPackageTestSuiteBase extends GenerationTestSuiteBase {
	private Class<?> testClass ;
	private EchoInt echo ;

	private void init() {
	    testClass = getClass( "DefaultPackageTest" ) ;
	    try {
		Object obj = testClass.newInstance() ;
		echo = EchoInt.class.cast( obj ) ;
	    } catch (Exception exc)  {
		throw new RuntimeException( exc ) ;
	    }
	}

	public DefaultPackageTestSuiteBase( boolean gbc, boolean debug ) {
	    super( gbc, debug ) ;
	    init() ;
	}

	public DefaultPackageTestSuiteBase( String name, boolean gbc, boolean debug ) {
	    super( name, gbc, debug ) ;
	    init() ;
	}

	public void testInvoke() {
	    assertEquals( echo.echo( 3 ), 3 ) ;
	}
    }

    // Test code generation by generating source code and compiling it.
    // This is mainly to validate the test itself, but also provides additional
    // testing of the source code generation process.
    public static class DefaultPackageTestSuite extends DefaultPackageTestSuiteBase {
	public DefaultPackageTestSuite( String name ) {
	    super( name, false, DEBUG ) ;
	}
	
	public DefaultPackageTestSuite() {
	    super( false, DEBUG ) ;
	}
    }	

    // The main test suite for code generation.  This tests all of the different
    // patterns of code generation.
    public static class BytecodeGenerationDefaultPackageTestSuite extends DefaultPackageTestSuiteBase {
	public BytecodeGenerationDefaultPackageTestSuite( String name ) {
	    super( name, true, DEBUG ) ;
	}
	
	public BytecodeGenerationDefaultPackageTestSuite() {
	    super( true, DEBUG ) ;
	}
    }	
    
    // Bytecode generator testing (and TODO list)
    //
    // There is a lot that needs testing here.  One test is simply
    // to make sure that the main test samples generate valid
    // bytecode.  Other detailed tests are needed as well.
    //
    // 1. Test that MyRemote_gen produces a valid interface.  This 
    //    is quite simple:
    //	  1. generate the AST, producing a ClassGenerator
    //	  2. generate bytecode from the AST
    //	  3. load the bytecode and get its ClassInfo
    //	  4. verify that the ClassGenerator and the ClassInfo are equal
    //	  DONE
    //
    // For specific features, we put one feature per method in a generated class.
    // The general test strategy then is:
    //	  1. generate the AST for the test class, generate bytecode, load it.
    //	  2. Invoke the methods in the generated class reflectively, validating
    //	     their behavior.
    // We need the following test methods:
    // 1. Simple expressions that return a value
    //	  1. Return a constant of each possile type
    //	  2. Return this
    //	  3. Return the result of a static method call
    //	  4. Return the result of a non-static method call (check cases here)
    //	  5. boolean testUnaryNot( boolean arg ) { return !arg ; }
    //	  6. boolean testBinaryXX( int arg1, int arg2 ) { return arg1 OPXX arg2 ; }
    //	     for each relational operator
    //	  7. boolean testEQU( int arg1, int arg2 ) (and similarly for Object)
    //	  8. boolean testNE similar to testEQU
    //	  9. boolean testAND( boolean arg1, boolean arg2 ) 
    //	     { return eval( 1, arg1 ) && eval( 2, arg2 ) ; }
    //	     (This tests that && is properly short circuited, that is,
    //	      that eval( 2, arg2 ) is only called if arg1 is false.
    //	      eval is a method in a base class that records the first arg
    //	      and echoes the second).
    // 2. Test cast expression, both successful and not successful.
    // 3. Test instanceof, both successful and not.
    // 4. Test various flavors of method calls and constructors
    //	  1. new Class( args ) 
    //	  2. new Class[size]
    //	  3. new Class[] { args }
    //	  4. super.method( args ) 
    //	  5. super( args ) in constructor
    //	  6. this( args ) in constructor
    //	  7. Class.method( args ) (static)
    //	  8. obj.method( args ) (virtual)
    //	  9. this.method( args ) (in current class to private method)
    // 5. obj.field in expression
    // 6. obj.field = expr
    // 7. Class.field in expression
    // 8. Class.field = expr
    // 9. arr[index] = expr
    // 10. arr[index] in expression
    // 11. if statement 
    //     DONE
    // 12. if/else statement 
    //	   DONE
    // 13. if {} else if {} else {} statement
    //	   DONE
    // 14. nested if statements
    //	   DONE
    // 15. while ... do {} 
    // 16. switch variants:
    //     1. no default, dense branches
    //     2. no default, sparse branches
    //     3. default, dense branches
    //     4. default, sparse branches
    //     Multiple branches including fall through cases
    // 17. try statements:
    //	   1. simple try {} catch () {}
    //	   DONE
    //	   2. try {} catch () {} catch () {} 
    //	   3. try {} finally {}
    //	   4. try {} catch () {} finally {}
    //	   DONE
    //	   5. try {} catch () {} catch () {} finally {}
    //	   6. nested try/catch/finally
    // 18. non-local control transfers using return and break, particularly with
    //     respect to handling finally blocks, and nested finally blocks
    //     (Need to add break to framework).
    // 19. Get MyRemote__Adapter working as soon as possible, before all of the 
    //     above tests are complete.
    //     - create an implementation of MyRemote using a dynamic proxy to
    //       implement the doSomething, doSomethingElse, and echo methods
    //     - There are several cases: return, return value, throw RemoteException,
    //       throw app exception for each method.
    //     DONE
    // 20. Use _DImpl_Tie test once most of the above is complete.
    // 21. Work on some sort of simple method overload resolution
    //     - Added method(s) on Type to determine if two types are related by
    //       method invocation conversion.
    //     - Implement Signature methods that compute signatures for calls.
    //     - Basic idea: only allow calls if there is exactly one method
    //       that has the same number of args, is accessible, and has all
    //       types related my method invocation conversion.  If multiple,
    //       user must supply signature.
    // 22. test static initializers (note: need to move this to a <clinit> method
    //     in the framework).  Need to support merging of multiple static 
    //     initializers (or maybe not).  Change _initializer to _static
    //     (generally make things look as much as possible like Java).
    // 
    // Packaging questions.
    // 1. Will we use codegen for new rmic?  (yes, I think)
    // 2. Will we use direct bytecode generation in rmic? (no or optional)
    // 3. Package framework except for bytecode visitor classes
    //    (ASM*, ByteCodeUtility, MyLabel, EmitterFactory) in the optional
    //    branch, rest moves into core?
    // 4. Need to refactor a bit for this.

    // The basic code generation test suite, which can be used for either source
    // or direct byte code generation.  Testing source code generation this way
    // is useful both for testing the source code generation (which is fairly simple),
    // and for testing the test cases.  Testing the direct bytecode generation
    // is where we expect to see the most problems, so debugging the tests themselves
    // first is important.
    public static abstract class FlowTestSuiteBase extends GenerationTestSuiteBase {
	private Class<?> flowClass ;
	private ControlBase cb ;

	private void init() {
	    flowClass = getClass( "Flow" ) ;
	    try { 
		Object obj = flowClass.newInstance() ;
		cb = ControlBase.class.cast( obj ) ;
	    } catch (Exception exc) {
		throw new RuntimeException( exc ) ;
	    }
	}

	public FlowTestSuiteBase( boolean generateBytecode, boolean debug ) {
	    super( generateBytecode, debug ) ;
	    init() ;
	}

	public FlowTestSuiteBase( String name, boolean generateBytecode, boolean debug ) {
	    super( name, generateBytecode, debug ) ;
	    init() ;
	}

	private void defineTest( Object... args ) {
	    cb.defineTest( args ) ;
	}

	// All that we know statically about inner.get() is that it is 
	// an instance of ControlBase.  However, the class actually has
	// many void no-args methods that we need to invoke reflectively
	// for the test cases.
	private void invoke( String name ) {
	    super.invoke( cb, name ) ;
	}

	private void expectException( String methodName,
	    Class<? extends RuntimeException> exClass ) {
	
	    super.expectException( exClass, cb, methodName ) ;
	}

	// Each test case takes the same form:
	// 1. Call defineTest with an expected sequence of operations 
	//    (see ControlBase for the encoding details).
	// 2. Invoke a particular test method.  If the test method does
	//    not follow the expected operation sequence, it will throw
	//    an exception, causing the test to fail.
	
	public void testSimpleIf1() {
	    defineTest( 1, 2, 4 ) ;
	    invoke( "simpleIf" ) ;
	}

	public void testSimpleIf2() {
	    defineTest( moa( 1, false ), 3, 4 ) ;
	    invoke( "simpleIf" ) ;
	}

	// The complexIf tests can be described as all possible combinations
	// of T/F on the if conditionals, which correspond to trace calls
	// with the arguments 1 2 6 8 12 and 15.  Of course, if one conditional
	// prevents another conditional from executing, the value of the 
	// conditional is a don't care state.  We can summarize this as follows:
	//
	// Tests: 1  2  6  8 12 15
	//	  T  T  T  T  -  -
	//	  T  T  T  F  -  -
	//	  T  T  F  -  -  -
	//	  T  F  T  T  -  -
	//	  T  F  T  F  -  -
	//	  T  F  F  -  -  -
	//	  F  -  -  -  T  -
	//	  F  -  -  -  F  T
	//	  F  -  -  -  F  F
	//

	public void testComplexIf1() {
	    defineTest( 1, 2, 3, 5, 6, 7, 8, 9, 18 ) ;
	    invoke( "complexIf" ) ;
	}
	
	public void testComplexIf2() {
	    defineTest( 1, 2, 3, 5, 6, 7, moa(8, false), 10, 18 ) ;
	    invoke( "complexIf" ) ;
	}

	public void testComplexIf3() {
	    defineTest( 1, 2, 3, 5, moa(6, false), 11, 18 )  ;
	    invoke( "complexIf" ) ;
	}

	public void testComplexIf4() {
	    defineTest( 1, moa( 2, false ), 4, 5, 6, 7, 8, 9, 18 )  ;
	    invoke( "complexIf" ) ;
	}

	public void testComplexIf5() {
	    defineTest( 1, moa( 2, false ), 4, 5, 6, 7, moa(8, false ), 10, 18 )  ;
	    invoke( "complexIf" ) ;
	}

	public void testComplexIf6() {
	    defineTest( 1, moa( 2, false ), 4, 5, moa(6, false ), 11, 18 )  ;
	    invoke( "complexIf" ) ;
	}

	public void testComplexIf7() {
	    defineTest( moa(1, false), 12, 13, 18 ) ;
	    invoke( "complexIf" ) ;
	}

	public void testComplexIf8() {
	    defineTest( moa(1, false), moa(12, false), 14, 15, 16, 18 ) ;
	    invoke( "complexIf" ) ;
	}

	public void testComplexIf9() {
	    defineTest( moa(1, false), moa(12, false), 14, moa(15, false), 17, 18 ) ;
	    invoke( "complexIf" ) ;
	}
	
	public void testSimpleTryCatch1() {
	    defineTest( 1, 2, 3, 6 ) ;
	    invoke( "simpleTryCatch" ) ;
	}

	public void testSimpleTryCatch2() {
	    defineTest( moa( 1, FirstException.class ), null ) ;
	    expectException( "simpleTryCatch", FirstException.class ) ;
	}
	
	public void testSimpleTryCatch3() {
	    defineTest( 1, moa( 2, FirstException.class ), 4, 5, 6 ) ;
	    invoke( "simpleTryCatch" ) ;
	}
	
	public void testSimpleTryCatch4() {
	    defineTest( 1, 2, moa( 3, FirstException.class ), 4, 5, 6 ) ;
	    invoke( "simpleTryCatch" ) ;
	}
	
	public void testSimpleTryCatch5() {
	    defineTest( 1, 2, moa( 3, FirstException.class ), 4, 
		moa( 5, SecondException.class ) ) ;
	    expectException( "simpleTryCatch", SecondException.class ) ;
	}

	public void testSimpleTryCatch6() {
	    defineTest( 1, moa( 2, SecondException.class ) ) ;
	    expectException( "simpleTryCatch", SecondException.class ) ;
	}
    }

    // Test code generation by generating source code and compiling it.
    // This is mainly to validate the test itself, but also provides additional
    // testing of the source code generation process.
    public static class SourceGenerationFlowTestSuite extends FlowTestSuiteBase {
	public SourceGenerationFlowTestSuite( String name ) {
	    super( name, false, DEBUG ) ;
	}
	
	public SourceGenerationFlowTestSuite() {
	    super( false, DEBUG ) ;
	}
    }	

    // The main test suite for code generation.  This tests all of the different
    // patterns of code generation.
    public static class BytecodeGenerationFlowTestSuite extends FlowTestSuiteBase {
	public BytecodeGenerationFlowTestSuite( String name ) {
	    super( name, true, DEBUG ) ;
	}
	
	public BytecodeGenerationFlowTestSuite() {
	    super( true, DEBUG ) ;
	}
    }	

    // Base class for testing EJBAdapter code generation.
    // Setup: 
    //	    myRemoteClass is the Class representing a remote interface
    //		that corresponds to the business interface MyBusinessIntf
    //	    myRemote__AdapterClass is the Class representing an adapter
    //		that implements MyBusinessIntf and delegates to myRemoteClass
    //
    // The tests:
    //	    Basically, inject test vector, invoke method, and check results.
    //	    However, the test vector is injected into an implementation of
    //	    myRemoteClass, which must be implemented using a Proxy.  
    //	    We create an instance of the adapter which delegates to the Proxy.
    //	    So, the tests are written as follows:
    //	    1. Inject a test vector into the Proxy, which uses a ControlBase for
    //	       that purpose.
    //	    2. Create an instance of the adapter that delegates to the Proxy.
    //	    3. Invoke a method on the adapter, and verify that the correct result
    //	       is observed.
    //
    //	Details:
    //	    void doSomething():
    //		1. test (1): completes normally
    //		2. test (1, RemoteException): throws EJBException with the given exception
    //		   as cause.
    //	        3. test (1, RuntimeException): throws the RuntimeException
    //	    int doSomethingElse():
    //		1. test (1,1): expect result 1.
    //	    int echo( int ):
    //		2. test (1)
    //		   invoke with 1, proxy returns 1, verify result is 1.
    public static class EJBAdapterTestSuiteBase extends GenerationTestSuiteBase {
	private Class<?> myRemoteClass = getClass( "MyRemote" ) ;
	private Constructor<?> adapterConstructor ;
	private Object invokee ; // object of type given by adapter class

	private static class MyRemoteProxyHandler implements InvocationHandler {
	    private ControlBase cb ;

	    public MyRemoteProxyHandler( Object... args ) {
		cb = new ControlBase() ;
		cb.defineTest( args ) ;
	    }

	    public Object invoke( Object proxy, Method method, 
		Object[] args ) throws Throwable {

		// Action depends on method sig:
		// void(): just call traceInt(1)
		// int(): return traceInt(1) 
		// int(int): return traceInt(arg)
		Class<?> returnType = method.getReturnType() ;
		Class<?>[] argTypes = method.getParameterTypes() ;
		if (argTypes.length > 1)
		    throw new IllegalStateException(
			"Only methods with 0 or 1 parameters are supported in this test" ) ;

		int value = 1 ;
		if (argTypes.length == 1) {
		    if (argTypes[0] != int.class)
			throw new IllegalStateException(
			    "Argument type (if any) must be int" ) ;

		    value = Integer.class.cast( args[0] ) ;
		}

		int result = cb.traceInt( value ) ;

		if (returnType == int.class)
		    return result ;
		else 
		    return null ;
	    }
	}

	private void defineTest( Object... args ) {
	    InvocationHandler handler = new MyRemoteProxyHandler( args ) ;
	    Class<?>[] interfaces = { myRemoteClass } ;
	    Object proxy = Proxy.newProxyInstance( myRemoteClass.getClassLoader(),
		interfaces, handler ) ;

	    try {
		invokee = adapterConstructor.newInstance( proxy ) ;  
	    } catch (Exception exc) {
		throw new RuntimeException( exc ) ;
	    }
	}

	private Object invoke( String name, Object... args ) {
	    return super.invoke( invokee, name, args ) ;
	}

	private Throwable expectException( String methodName,
	    Class<? extends Throwable> exClass, Object... args ) {
	    return super.expectException( exClass, invokee, methodName, args ) ;
	}

	public void testDoSomething1() {
	    defineTest( moa( 1, 1 ), null ) ;
	    invoke( "doSomething" ) ;
	}

	public void testDoSomething2() {
	    defineTest( moa( 1, IllegalStateException.class ), null ) ;
	    expectException( "doSomething", RuntimeException.class ) ;
	}

	public void testDoSomething3() {
	    defineTest( moa( 1, RemoteException.class ), null ) ;
	    EJBException ejbex = EJBException.class.cast( 
		expectException( "doSomething", EJBException.class ) ) ;
	    Exception exc = ejbex.getCausedByException() ;
	    assertEquals( RemoteException.class, exc.getClass() ) ;
	}

	public void testDoSomethingElse1() {
	    defineTest( moa( 1, 1 ), null ) ;
	    assertEquals( invoke( "doSomethingElse" ), 1 ) ;
	}

	public void testEcho() {
	    defineTest( moa( 42, 357 ), null ) ;
	    int result = Integer.class.cast( invoke( "echo", 42 ) ) ;
	    assertEquals( result, 357 ) ;
	}

	public EJBAdapterTestSuiteBase( String className, String name, 
	    boolean generateByteCode, boolean debug ) {
	    super( name, generateByteCode, debug ) ;
	    init( className ) ;
	}

	private void init( String className ) {
	    try {
		final Class<?> myRemote__AdapterClass = getClass( className ) ;
		adapterConstructor = myRemote__AdapterClass.getConstructor(
		    myRemoteClass ) ;
	    } catch (Exception exc) {
		throw new RuntimeException( exc ) ;
	    }
	}

	public EJBAdapterTestSuiteBase( String className, 
	    boolean generateByteCode, boolean debug ) {
	    super( generateByteCode, debug ) ;
	    init( className ) ;
	}
    }
    
    // Test code generation by generating source code and compiling it.
    // This is mainly to validate the test itself, but also provides additional
    // testing of the source code generation process.
    public static class EJBAdapterSourceTestSuite extends EJBAdapterTestSuiteBase {
	public EJBAdapterSourceTestSuite( String name ) {
	    super( "MyRemote__Adapter", name, false, DEBUG ) ;
	}

	public EJBAdapterSourceTestSuite( ) {
	    super( "MyRemote__Adapter", false, DEBUG ) ;
	}
    }

    // Test code generation by generating byte code directly.  
    public static class EJBAdapterBytecodeTestSuite extends EJBAdapterTestSuiteBase {
	public EJBAdapterBytecodeTestSuite( String name ) {
	    super( "MyRemote__Adapter", name, true, DEBUG ) ;
	}

	public EJBAdapterBytecodeTestSuite( ) {
	    super( "MyRemote__Adapter", true, DEBUG ) ;
	}
    }

    // Test code generation by generating source code and compiling it.
    // This is mainly to validate the test itself, but also provides additional
    // testing of the source code generation process.
    public static class EJBAdapterSimplifiedSourceTestSuite extends EJBAdapterTestSuiteBase {
	public EJBAdapterSimplifiedSourceTestSuite( String name ) {
	    super( "MyRemote__Adapter_Simplified", name, false, DEBUG ) ;
	}

	public EJBAdapterSimplifiedSourceTestSuite( ) {
	    super( "MyRemote__Adapter_Simplified", false, DEBUG ) ;
	}
    }

    // Test code generation by generating byte code directly.  
    public static class EJBAdapterSimplifiedBytecodeTestSuite extends EJBAdapterTestSuiteBase {
	public EJBAdapterSimplifiedBytecodeTestSuite( String name ) {
	    super( "MyRemote__Adapter_Simplified", name, true, DEBUG ) ;
	}

	public EJBAdapterSimplifiedBytecodeTestSuite( ) {
	    super( "MyRemote__Adapter_Simplified", true, DEBUG ) ;
	}
    }

    public static Test suite() 
    {
	TestSuite main = TestCaseTools.makeTestSuite( Client.class, 
	    TestCaseTools.TestSuiteType.SINGLE ) ;
	TestSuite typesuite = TestCaseTools.makeTestSuite( TypeTestSuite.class ) ;
	main.addTest( typesuite ) ;
	return main ;
    }

    public static void main( String[] args ) {
	Client test = new Client() ;
	TestResult result = junit.textui.TestRunner.run( 
	    test.suite() ) ;

	if (result.errorCount() + result.failureCount() > 0) {
	    System.out.println( 
		"Error: failures or errors in JUnit test" ) ;
	    System.exit( 1 ) ;
	} else {
	    System.exit( 0 ) ;
	}
    }
}
