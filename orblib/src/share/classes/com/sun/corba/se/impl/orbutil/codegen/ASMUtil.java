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

import java.util.Properties ;
import java.util.List ;
import java.util.HashSet ;
import java.util.HashMap ;

import java.io.IOException ;
import java.io.PrintStream ;
import java.io.FileOutputStream ;
import java.io.File ;

import org.objectweb.asm.ClassWriter ;

// Imports for verify method
import org.objectweb.asm.ClassReader ;
import org.objectweb.asm.Opcodes ;

import org.objectweb.asm.tree.ClassNode ;
import org.objectweb.asm.tree.MethodNode ;
import org.objectweb.asm.tree.AbstractInsnNode ;

import org.objectweb.asm.tree.analysis.Analyzer ;
import org.objectweb.asm.tree.analysis.AnalyzerException ;
import org.objectweb.asm.tree.analysis.SimpleVerifier ;
import org.objectweb.asm.tree.analysis.Frame ;

import org.objectweb.asm.util.CheckClassAdapter ;
import org.objectweb.asm.util.TraceMethodVisitor ;
// end of verify method imports

import com.sun.corba.se.spi.orbutil.generic.NullaryFunction ;
import com.sun.corba.se.spi.orbutil.generic.Pair ;

import com.sun.corba.se.spi.orbutil.codegen.ImportList ;
import com.sun.corba.se.spi.orbutil.codegen.Type ;
import com.sun.corba.se.spi.orbutil.codegen.Variable ;
import com.sun.corba.se.spi.orbutil.codegen.Wrapper ;

// import com.sun.corba.se.impl.orbutil.codegen.constantpool.ConstantPool ;

/** Simple class containing a few ASM-related utilities 
 * and dynamic attributes needs for the byte code generator.
 */
public class ASMUtil {
    public enum RequiredEmitterType { GETTER, SETTER, NONE } ;

    public static String bcName( Type type ) {
	return type.name().replace( '.', '/' ) ;
    }

    private static void displayNode( PrintStream ps, String msg, Node node ) {
	ps.println( ) ;
	ps.println( "=======================================================" ) ;
	ps.println( msg ) ;
	Util.display( node, ps ) ;	
	ps.println() ;
	Util.checkTree( node, ps ) ;
	ps.println() ;
	ps.println( "=======================================================" ) ;
    }

    public static void generateSourceCode( PrintStream ps, ClassGeneratorImpl cg,
	ImportList imports, Properties options ) throws IOException {

	TreeWalkerContext context = new TreeWalkerContext() ;
	Visitor visitor = new SourceStatementVisitor( context, 
	    imports, new Printer( ps ) ) ;
	cg.accept( visitor ) ;
    }

    public static File getFile( String genDir, String className, 
	String suffix ) {

	Pair<String,String> names = Wrapper.splitClassName( className ) ;
	String pkgName = names.first().replace( '.', File.separatorChar ) ;
	File sdir = new File( genDir, pkgName ) ;
	sdir.mkdirs() ; // make sure the directory exists; may fail if already exists
	
	String sfname = names.second() + suffix ;

	File sfile = new File( sdir, sfname ) ;
	return sfile ;
    }

    public static void generateSourceCode( String sourceGenDir, ClassGeneratorImpl cg,
	ImportList imports, Properties options ) throws IOException {
	
	PrintStream ps = null ;

	try {
	    // Create a PrintStream for the source file.
	    File sfile = getFile( sourceGenDir, cg.name(), ".java" ) ;
	    ps = new PrintStream( sfile ) ;

	    // Write out the source code to the source file
	    generateSourceCode( ps, cg, imports, options ) ;
	} finally {
	    if (ps != null)
		ps.close() ;
	}
    }

/* Requires Apache constantpool package, which is not included in the ORB.
    private static final int CLASS_MAGIC = 0xCAFEBABE ;

    private static void readConstantPool( PrintStream ps, byte[] cldata ) {
	try {
	    ps.println( "*** Reading constant pool ***" ) ;
	    ConstantPool cp = new ConstantPool() ;
	    ByteArrayInputStream bos = new ByteArrayInputStream( cldata ) ;
	    DataInputStream dis = new DataInputStream( bos ) ;
	    int magic = dis.readInt() ;
	    ps.println( "Class magic = " + magic ) ;
	    if (magic != CLASS_MAGIC) {
		ps.println( "Bad magic" ) ;
		return ;
	    }

	    int minor = dis.readUnsignedShort() ;
	    int major = dis.readUnsignedShort() ;

	    ps.println( "Version: " + major + "." + minor ) ;
	    cp.read( dis, true ) ;
	    cp.resolve() ;
	} catch (Exception exc) {
	    ps.println( "Error in dumping constant pool: " + exc ) ;
	    exc.printStackTrace() ;
	}
    }
*/

    /** Given a completed ClassGeneratorImpl, use ASM to construct
     * the byte array representing the compiled class.
     */
    public static byte[] generate( ClassLoader cl, ClassGeneratorImpl cg,
	ImportList imports, Properties options, PrintStream debugOutput ) {

	// Make sure that ClassLoader cl is used where required (mainly in the
	// implementation of Type.classInfo, which is used in several places).
	CurrentClassLoader.set( cl ) ;
	
	// get options
	boolean dumpConstantPool = false ;
	boolean dumpAfterSetupVisitor = false ;
	boolean traceByteCodeGeneration = false ;
	boolean useAsmVerifier = false ;
	String classGenDir = null ; 
	String sourceGenDir = null ; 

	if (options != null) {
	    dumpConstantPool = Boolean.parseBoolean(
		options.getProperty( Wrapper.DUMP_CONSTANT_POOL )) ;
	    dumpAfterSetupVisitor = Boolean.parseBoolean(
		options.getProperty( Wrapper.DUMP_AFTER_SETUP_VISITOR )) ;
	    traceByteCodeGeneration = Boolean.parseBoolean(
		options.getProperty( Wrapper.TRACE_BYTE_CODE_GENERATION )) ;
	    useAsmVerifier = Boolean.parseBoolean( 
		options.getProperty( Wrapper.USE_ASM_VERIFIER )) ;
	    classGenDir = options.getProperty( 
		Wrapper.CLASS_GENERATION_DIRECTORY )  ;
	    sourceGenDir = options.getProperty( 
		Wrapper.SOURCE_GENERATION_DIRECTORY )  ;
	}

	if (sourceGenDir != null) {
	    try {
		generateSourceCode( sourceGenDir, cg, imports, options ) ;
	    } catch (IOException exc) {
		throw new IllegalArgumentException( 
		    "Could not generate source code for class " 
		    + cg.name(), exc ) ;
	    }
	}

	ClassWriter cw = new ClassWriter( true ) ; // have ASM compute max stack size

	// Prepare the tree for byte code generation.  We use a fresh
	// TreeWalker context for each pass with a visitor.
	TreeWalkerContext twc = new TreeWalkerContext() ;
	Visitor v1 = new ASMSetupVisitor( twc ) ;
	cg.accept( v1 ) ;
	if (dumpAfterSetupVisitor)
	    displayNode( debugOutput, "Contents of AST after SetupVisitor", cg ) ;

	// generate byte code
	twc = new TreeWalkerContext() ;
	Visitor v2 = new ASMByteCodeVisitor( twc, cw, traceByteCodeGeneration, 
	    debugOutput ) ;
	cg.accept( v2 ) ;

	byte[] result = cw.toByteArray() ;

	if (dumpConstantPool) {
	    // readConstantPool( debugOutput, result ) ;
	}

	if (classGenDir != null) {
	    // Dump the generate bytecode to a directory for debugging.
	    File cfile = getFile( classGenDir, cg.name(), ".class" ) ;
	    FileOutputStream fos = null ;
	    try {
		fos = new FileOutputStream( cfile ) ;
		fos.write( result, 0, result.length ) ;
	    } catch (IOException exc) {
		throw new IllegalArgumentException( 
		    "Could not dump generated bytecode to file "
		    + cfile ) ;
	    } finally {
		if (fos != null)
		    try {
			fos.close() ;
		    } catch (IOException exc) {
			// ignore this
		    }
	    }
	}

	if (useAsmVerifier) {
	    debugOutput.println( "*** Using ASM verifier ***" ) ;
	    verify( debugOutput, result ) ;
	}

	return result ;
    }

    // Adapted from org.objectweb.asm.util.CheckClassAdapter.main,
    // which contains the needed code, but for some reason does NOT
    // expose the code, except by reading a file.  We don't want
    // or need file system overhead here.
    private static void verify( final PrintStream ps, byte[] classData ) {
	ClassReader cr = new ClassReader( classData ) ;

	ClassNode cn = new ClassNode();
	cr.accept(new CheckClassAdapter(cn), true);

	List methods = cn.methods;
	for (int i = 0; i < methods.size(); ++i) {
	    MethodNode method = (MethodNode)methods.get(i);
	    if (method.instructions.size() > 0) {
		Analyzer a = new Analyzer(
		    new SimpleVerifier(
			org.objectweb.asm.Type.getType("L" + cn.name + ";"), 
			org.objectweb.asm.Type.getType("L" + cn.superName + ";"), 
			(cn.access & Opcodes.ACC_INTERFACE) != 0));

		try {
		    a.analyze(cn.name, method);
		    continue;
		} catch (AnalyzerException ae) {
		    ps.println( "Possible problem detected while verifying " 
			+ cn.name + "." + method.name + ": " + ae ) ;
		    // Too much information in app server log: e.printStackTrace();
		}

		final Frame[] frames = a.getFrames();

		ps.println(method.name + method.desc);
		TraceMethodVisitor mv = new TraceMethodVisitor() {
                    @Override
		    public void visitMaxs (final int maxStack, final int maxLocals) {
			for (int i = 0; i < text.size(); ++i) {
			    String s = frames[i] == null ? "null" : frames[i].toString();
			    while (s.length() < maxStack+maxLocals+1) {
				s += " ";
			    }

			    ps.print( Integer.toString(i + 100000).substring(1) 
				+ " " + s + " : " + text.get(i));
			}

			ps.println();
		    }
		};

		for (int j = 0; j < method.instructions.size(); ++j) {
		    ((AbstractInsnNode)method.instructions.get(j)).accept(mv);
		}

		mv.visitMaxs(method.maxStack, method.maxLocals);
	    }
	}
    }

    // Function used to initialize Attribute<MyLabel> instances.
    private static NullaryFunction<MyLabel> makeLabel = 
	new NullaryFunction<MyLabel>() {
	    public MyLabel evaluate() {
		return new MyLabel() ;
	    }
	} ;

    // All attributes are package private so that they can be 
    // used in other parts of the codegen implementation.
    
    // Attribute on MethodGenerator that defines the label on the
    // return instruction at the end of the method.
    static Attribute<MyLabel> returnLabel = new Attribute<MyLabel>(
	MyLabel.class, "returnLabel", makeLabel ) ;

    // Attribute on Statement nodes that labels the start of 
    // the statement.
    static Attribute<MyLabel> statementStartLabel = new Attribute<MyLabel>(
	MyLabel.class, "statementStartLabel", makeLabel ) ;

    static Attribute<MyLabel> statementEndLabel = new Attribute<MyLabel>(
	MyLabel.class, "statementEndLabel", makeLabel ) ;

    // Attribute on BlockStatements in TryStatements used to label
    // the end of the Block.  Needed for generating exception table.
    static Attribute<MyLabel> throwEndLabel = new Attribute<MyLabel>(
	MyLabel.class, "throwEndLabel", makeLabel ) ;

    // Attribute on all Statement nodes that gives the start of the
    // sequentially next statement immediately after the current
    // statement if any.  This is only set if the parent node has
    // a local next statement (e.g. BlockStatement).
    static Attribute<Node> next = new Attribute<Node>(
	Node.class, "next", (Node)null ) ;

    static Attribute<Variable> returnVariable = new Attribute<Variable>(
	Variable.class, "returnVariable", (Variable)null ) ;

    // Variable attributes
    
    // All local Variable definitions have this attribute which defines where
    // they are allocated in the stack frame.
    static Attribute<Integer> stackFrameSlot = new Attribute<Integer>(
	Integer.class, "stackFrameSlot", 0 ) ;

    // All Variable definitions have a getEmitter attribute which defines
    // how to get the value of the Variable.
    static Attribute<EmitterFactory.Emitter> getEmitter = new Attribute<EmitterFactory.Emitter>(
	EmitterFactory.Emitter.class, "getEmitter", (EmitterFactory.Emitter)null ) ;

    // All Variable definitions have a getEmitter attribute which defines
    // how to set the value of the Variable.
    static Attribute<EmitterFactory.Emitter> setEmitter = new Attribute<EmitterFactory.Emitter>(
	EmitterFactory.Emitter.class, "setEmitter", (EmitterFactory.Emitter)null ) ;

    // All assignable expression nodes have an emitter attribute which defines
    // what operation (load or store) is needed when that reference is visited.
    static Attribute<EmitterFactory.Emitter> emitter = new Attribute<EmitterFactory.Emitter>(
	EmitterFactory.Emitter.class, "emitter", (EmitterFactory.Emitter)null ) ;

    // Indicates whether a variable needs to emit a setter, a getter, or no
    // code at all when visited for code generation.
    static Attribute<RequiredEmitterType> requiredEmitterType = new Attribute<RequiredEmitterType>(
	RequiredEmitterType.class, "requiredEmitterType", RequiredEmitterType.GETTER ) ;

    // Used in ASMByteCodeVisitor to track the last statement visited in a
    // BlockStatement
    static Attribute<Statement> lastStatement = new Attribute<Statement>(
	Statement.class, "lastStatement", (Statement)null ) ;

    // Used to hold the exception for the uncaught exception handler when
    // generating code for a try statement with a finally block.
    static Attribute<Variable> uncaughtException = new Attribute<Variable>(
	Variable.class, "uncaughtException", (Variable)null ) ;

    // Used to hold the local variable that holds the return address for
    // a finally block.
    static Attribute<Variable> returnAddress = new Attribute<Variable>(
	Variable.class, "returnAddress", (Variable)null ) ;

    // Used to track the last BlockStatement visited while generating
    // bytecode for a TryStatement.
    static Attribute<BlockStatement> lastBlock = new Attribute<BlockStatement>(
	BlockStatement.class, "lastBlock", (BlockStatement)null ) ;

    // Used to label the start of the uncaught exception handler.
    static Attribute<MyLabel> uncaughtExceptionHandler = new Attribute<MyLabel>(
	MyLabel.class, "uncaughtExceptionHandler", makeLabel ) ;

    static Attribute<Integer> ctr = new Attribute<Integer>(
	Integer.class, "ctr", 0 ) ;

    public static class LineNumberTable extends HashMap<MyLabel,Integer> {
	public LineNumberTable() {
	    super() ;
	}
    }

    private static NullaryFunction<LineNumberTable> tableMaker = 
	new NullaryFunction<LineNumberTable>() {
	    public LineNumberTable evaluate() {
		return new LineNumberTable() ;
	    }
	} ;

    // Attribute on MethodGenerator that contains the LineNumberTable.
    static Attribute<LineNumberTable> lineNumberTable = new Attribute<LineNumberTable>(
	LineNumberTable.class, "lineNumberTable", tableMaker ) ;

    public static class VariablesInMethod extends HashSet<Variable> {
	public VariablesInMethod() {
	    super() ;
	}
    }

    private static NullaryFunction<VariablesInMethod> vmMaker = 
	new NullaryFunction<VariablesInMethod>() {
	    public VariablesInMethod evaluate() {
		return new VariablesInMethod() ;
	    }
	} ;

    static Attribute<VariablesInMethod> variablesInMethod = new Attribute<VariablesInMethod>(
	VariablesInMethod.class, "variablesInMethod", vmMaker ) ;
}
