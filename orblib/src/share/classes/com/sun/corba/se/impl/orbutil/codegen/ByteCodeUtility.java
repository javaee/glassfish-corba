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

package com.sun.corba.se.impl.orbutil.codegen ;

import java.io.PrintStream ;

import java.lang.reflect.Modifier ;

import java.util.List ;
import java.util.Map ;
import java.util.HashMap ;
import java.util.Collections ;
import java.util.ArrayList ;
import java.util.Comparator ;

import com.sun.corba.se.spi.orbutil.codegen.Type ;
import com.sun.corba.se.spi.orbutil.codegen.Signature ;
import com.sun.corba.se.spi.orbutil.codegen.Variable ;
import com.sun.corba.se.spi.orbutil.codegen.ClassInfo ;
import com.sun.corba.se.spi.orbutil.codegen.MethodInfo ;

import com.sun.corba.se.impl.orbutil.codegen.ExpressionFactory ;

import org.objectweb.asm.MethodVisitor ;
import org.objectweb.asm.ClassWriter ;
import org.objectweb.asm.Label ;
import org.objectweb.asm.util.TraceMethodVisitor ;

import static java.lang.reflect.Modifier.* ;
import static org.objectweb.asm.Opcodes.* ;

/** Class that is responsible for low-level bytecode generation using ASM.
 * It provides methods that directly generate bytecode in a MethodVisitor.
 * This will often be accompanied by type analysis, such as in determining 
 * the correct sort of return instruction to use, or how to generate code
 * for a constant.
 * </ol>
 */
public final class ByteCodeUtility {
    private ClassWriter cw ;
    private MethodVisitor mv ;
    private boolean debug ;
    private PrintStream ps ;
    private String methodName ;
    private String methodSignature ;

    /** Construct an instance of ByteCodeUtility from an ASM ClassWriter
     * and a codegen ClassGenerator.  If debug is true, debugText() can
     * be called to get a text representation of the generated code for
     * debugging purposes.
     */
    public ByteCodeUtility( ClassWriter cw, ClassGenerator cg, 
	boolean debug, PrintStream ps ) {

	this.cw = cw ;
	this.mv = null ;
	this.debug = debug ;
	this.ps = ps ;

	String[] interfaces = new String[cg.impls().size()] ;
	int ctr=0 ;
	for (Type impl : cg.impls())
	    interfaces[ctr++] = ASMUtil.bcName( impl ) ;

	int modifiers = cg.modifiers() ;
	if (cg.isInterface())
	    modifiers += ACC_INTERFACE ;
	String superType = (cg.superType() == null) ? 
	    ASMUtil.bcName( Type._Object() ) :
	    ASMUtil.bcName( cg.superType() ) ;

	cw.visit( V1_5, cg.modifiers(), ASMUtil.bcName( cg.thisType() ), 
	    null, superType, interfaces ) ;
	cw.visitSource( cg.name().replace( '.', '/' ) + ".java", null ) ;
    }

    public void addField( FieldGenerator arg ) {
	String descriptor = arg.type().signature() ;
	cw.visitField( arg.modifiers(), arg.name(), descriptor, null, null ) ;
    }

    public ByteCodeUtility( ClassWriter cw, ClassGenerator cg ) {
	this( cw, cg, false, System.out ) ;
    }

    private void dump() {
	if (debug) {
	    List<String> data = TraceMethodVisitor.class.cast( mv ).getText() ;
	    ps.printf( "MethodVisitor calls for method %s%s\n", 
		methodName, methodSignature ) ;
	    for (String str : data) {
		ps.print( str ) ;
	    }
	}
    }

    public void emitMethodStart( MethodGenerator mg ) {
	
	// Get a List<Type> for parameter types
	List<Type> types = new ArrayList<Type>() ;
	for (Variable var : mg.arguments()) {
	    types.add( var.type() ) ;
	}

	methodName = mg.name() ;
	methodSignature = 
	    Signature.make( mg.returnType(), types ).signature() ;
	   
	// Get a String[] representation of the exception types
	String[] strs = new String[mg.exceptions().size()] ;
	int ctr = 0 ;
	for (Type exception : mg.exceptions()) {
	    strs[ctr++] = ASMUtil.bcName( exception ) ;
	}
	
	mv = cw.visitMethod( mg.modifiers(), methodName, methodSignature, null,
	    strs ) ;

	if (debug)
	    mv = new TraceMethodVisitor( mv ) ;

	mv.visitCode() ;
    }
   
    private void emitLineNumberTable( MethodGenerator mg ) {
	ASMUtil.LineNumberTable lnt = ASMUtil.lineNumberTable.get( mg ) ;
	if (lnt != null) {
	    List<MyLabel> labels = new ArrayList( lnt.keySet() ) ;
	    Collections.sort( labels, 
		new Comparator<MyLabel>() {
		    public int compare( MyLabel l1, MyLabel l2 ) {
			return l1.getOffset() - l2.getOffset() ;
		    }
		}
	    ) ;

	    int currentLineNum = -1 ;
	    for (Label label : labels) {
		int lineNum = lnt.get( label ) ;
		if (lineNum != currentLineNum) {
		    currentLineNum = lineNum ;
		    mv.visitLineNumber( lineNum, label ) ;
		}
	    }
	}
    }

    private void emitLocalVariableTable( MethodGenerator mg ) {
	ASMUtil.VariablesInMethod vm = ASMUtil.variablesInMethod.get( mg ) ;

	for (Variable var : vm) {
	    Label start ;
	    Label end ;

	    Statement stmt = var.getAncestor( Statement.class ) ;
	    if (stmt == null) { // We must have a method argument
		end = ASMUtil.returnLabel.get( mg ) ;

		BlockStatement bs = mg.body() ;
		if (bs.isEmpty()) {
		    // No statements, so set start=end.
		    start = end ;
		} else {
		    // Start is the start of the first statement 
		    // in the method body.
		    List<Statement> stmts = bs.body() ;
		    Statement first = stmts.iterator().next() ;
		    start = ASMUtil.statementStartLabel.get( first ) ;
		}
	    } else {
		start = ASMUtil.statementStartLabel.get( stmt ) ;
		end = ASMUtil.statementEndLabel.get( stmt ) ;
	    }

	    int index = ASMUtil.stackFrameSlot.get( var ) ;
	    mv.visitLocalVariable( var.ident(), var.type().signature(), null,
		start, end, index ) ;
	}
    }

    public void emitMethodEnd( MethodGenerator mg, Label returnLabel, 
	Variable returnVariable, boolean dump ) {

	// Only generate return for non-abstract methods.
	if ((mg.modifiers() & ABSTRACT) == 0) {
	    // Generate the return at the end of the method.
	    // This is just a label, followed by pushing any return value
	    // onto the stack, and then the appropriately typed xRETURN
	    // instruction.  Return instructions are basically just 
	    // setting the return variable and then branching to this
	    // location, EXCEPT that we also need to handle finally
	    // blocks (the real reason for using a variable here).
	    mv.visitLabel( returnLabel ) ;
	    
	    if (returnVariable != null) {
		// We need the get emitter here.
		EmitterFactory.Emitter emitter =
		    ASMUtil.getEmitter.get( returnVariable ) ;
		assert emitter != null ;
		emitter.evaluate( mv ) ;
	    }

	    emitReturn( 
		returnVariable == null ? Type._void() : returnVariable.type() ) ;

	    // Emit debug information, if present
	    emitLineNumberTable( mg ) ;
	    emitLocalVariableTable( mg ) ;
	}

	mv.visitMaxs(0,0) ;
	mv.visitEnd() ;

	if (dump)
	    dump() ;

	mv = null ;
    }

    // Emit the appropriately typed return bytecode.
    private void emitReturn( Type returnType ) {

	if (returnType.equals(Type._void())) {
	    mv.visitInsn( RETURN ) ;
	} else if (returnType.isPrimitive()) {
	    // ILFD
	    if (returnType.equals(Type._long())) {
		mv.visitInsn( LRETURN ) ;
	    } else if (returnType.equals(Type._float())) {
		mv.visitInsn( FRETURN ) ;
	    } else if (returnType.equals(Type._double())) {
		mv.visitInsn( DRETURN ) ;
	    } else {
		mv.visitInsn( IRETURN ) ;
	    }
	} else { // must be a reference 
	    mv.visitInsn( ARETURN ) ;
	} 
    }

    public void emitRet( Variable var ) {
	// We always use Object here, so check this.
	assert var.type().equals( Type._Object() ) ;

	Integer slot = ASMUtil.stackFrameSlot.get( var ) ;
	assert slot != null ;

	mv.visitVarInsn( RET, slot ) ;
    }

    public void emitConstantExpression( Type type, Object value ) {

	// This nasty little piece of code handles all of the special
	// variations in loading numberic constants, as well as 
	// Class, String, and null values.
	if (type.equals(Type._null())) {
	    mv.visitInsn( ACONST_NULL ) ;
	} else if (type.equals(Type._Class())) {
	    // value is a Type, so get a corresponding ASM type.
	    Type vtype = Type.class.cast( value ) ;
	    org.objectweb.asm.Type atype = 
		org.objectweb.asm.Type.getType( vtype.signature() ) ;
	    mv.visitLdcInsn( atype ) ;
	} else if (type.equals( Type._String())) {
	    mv.visitLdcInsn( value ) ;
	} else if (type.equals( Type._float() )) {
	    float val = Float.class.cast( value ).floatValue() ;
	    if (val == 0.0)
		mv.visitInsn( FCONST_0 ) ;
	    else if (val == 1.0)
		mv.visitInsn( FCONST_1 ) ;
	    else if (val == 2.0)
		mv.visitInsn( FCONST_2 ) ;
	    else 
		mv.visitLdcInsn( value ) ;
	} else if (type.equals( Type._double() )) {
	    double val = Double.class.cast( value ).doubleValue() ;
	    if (val == 0.0)
		mv.visitInsn( DCONST_0 ) ;
	    else if (val == 1.0)
		mv.visitInsn( DCONST_1 ) ;
	    else 
		mv.visitLdcInsn( value ) ;
	} else if (type.equals( Type._long() )) {
	    long val = Long.class.cast( value ).longValue() ;
	    if (val == 0)
		mv.visitInsn( LCONST_0 ) ;
	    else if (val == 1)
		mv.visitInsn( LCONST_1 ) ;
	    else 
		mv.visitLdcInsn( value ) ;
	} else if (type.equals( Type._boolean() )) {
	    mv.visitInsn(  
		Boolean.class.cast( value ).booleanValue() 
		    ? ICONST_1 : ICONST_0 ) ;
	} else { // byte, char, short, or int
	    int val = Integer.class.cast( value ).intValue() ;
	    if (val == -1)
		mv.visitInsn( ICONST_M1 ) ;
	    else if (val == 0)
		mv.visitInsn( ICONST_0);
	    else if (val == 1)
		mv.visitInsn( ICONST_1);
	    else if (val == 2)
		mv.visitInsn( ICONST_2);
	    else if (val == 3)
		mv.visitInsn( ICONST_3);
	    else if (val == 4)
		mv.visitInsn( ICONST_4);
	    else if (val == 5)
		mv.visitInsn( ICONST_5);
	    else if ((val >= Byte.MIN_VALUE) && (val <= Byte.MAX_VALUE))
		mv.visitIntInsn( BIPUSH, val ) ;
	    else if ((val >= Short.MIN_VALUE) && (val <= Short.MAX_VALUE))
		mv.visitIntInsn( SIPUSH, val ) ;
	    else
		mv.visitLdcInsn( value ) ;
	}
    }

    public void emitThisExpression() {
	mv.visitIntInsn( ALOAD, 0 ) ;
    }

    // Used for generating the required branch for booleans.
    // Note that true=1, false=0.
    // Stack Map: (boolean) -> ().
    public void emitConditionalBranch( MyLabel falseBranch ) {

	mv.visitJumpInsn( IFEQ, falseBranch ) ;
    }

    public void emitBranch( MyLabel target ) {
	mv.visitJumpInsn( GOTO, target ) ;
    }

    // Visit the label given by the attribute on node if the
    // attribute is set, and has not already been visited.
    // Unfortunately it is possible to call emitLabel twice
    // in some cases (see try statements and preStatement in 
    // ASMByteCodeVisitor).
    public void emitLabel( Attribute<MyLabel> attr, Node node ) {
	if (attr.isSet( node )) {
	    MyLabel label = attr.get( node ) ;

	    if (label.emitted()) {
		if (debug)
		    TraceMethodVisitor.class.cast( mv ).getText().add( 
			"    Already emitted label " + label ) ;
	    } else {
		int lineNumber = Printer.lineNumberAttribute.get( node ) ;
		if (lineNumber > 0) {
		    MethodGenerator mg = node.getAncestor( MethodGenerator.class ) ;
		    ASMUtil.LineNumberTable lnt = ASMUtil.lineNumberTable.get( mg ) ;
		    lnt.put( label, lineNumber ) ;
		}
		label.emitted( true ) ;
		mv.visitLabel( label ) ;
	    }
	}
    }

    /** Emit the NEW, DUP sequence required at the start of a new 
     * call.
     */
    public void emitNewCall( Type type ) {
	String typeName = ASMUtil.bcName( type ) ;

	mv.visitTypeInsn( NEW, typeName ) ;
	mv.visitInsn( DUP ) ;
    }

    public void emitInstanceof( Type type ) {
	String typeName = ASMUtil.bcName( type ) ;

	mv.visitTypeInsn( INSTANCEOF, typeName ) ;
    }

    public void emitCast( Type from, Type to ) {
	// Assume that the cast conversion check has already occurred, so
	// we are only calling emitCast if it is necessary to emit some
	// instructions.
	// Possibilties:
	// from, to are both numeric types: emit appropriate x2y instruction
	// from, to are both non-array reference types: emit CHECKCAST
	// from, to are both array types: emit CHECKCAST?
	
	if (from.isPrimitive()) {
	    if (to.isPrimitive()) {
		if (from.isNumber() && to.isNumber())
		    emitConversion( from, to ) ;
		else
		    throw new IllegalArgumentException( 
			"No conversion is possible from " + from.name() 
			+ " to " + to.name() ) ;
	    } else {
		throw new IllegalArgumentException( "Type " + from.name() +
		    " is a primitive type, but type " + to.name() +
		    " is a reference type: no conversion is possible" ) ;
	    }
	} else {
	    if (to.isPrimitive()) {
		throw new IllegalArgumentException( "Type " + from.name() +
		    " is a reference type, but type " + to.name() +
		    " is a primitive type: no conversion is possible" ) ;
	    } else {
		String sig = to.isArray() ? 
		    to.signature() :
		    ASMUtil.bcName(to) ;
		
		// mv.visitTypeInsn( CHECKCAST, ASMUtil.bcName(to) ) ;
		mv.visitTypeInsn( CHECKCAST, sig ) ;
	    }
	}
    }
    
    public void emitDup() {
	mv.visitInsn( DUP ) ;
    }

    public void emitArrayStore() {
	mv.visitInsn( AASTORE ) ;
    }

    // return the type code for a primitive type in a
    // NEWARRAY instruction.
    public int typeCode( Type type ) {
	if (type.equals(Type._boolean()))
	    return T_BOOLEAN ;

	if (type.equals(Type._byte()))
	    return T_BYTE ;

	if (type.equals(Type._char()))
	    return T_CHAR ;

	if (type.equals(Type._short()))
	    return T_SHORT ;

	if (type.equals(Type._int()))
	    return T_INT ;

	if (type.equals(Type._long()))
	    return T_LONG ;

	if (type.equals(Type._float()))
	    return T_FLOAT ;

	if (type.equals(Type._double()))
	    return T_DOUBLE ;

	throw new IllegalArgumentException( 
	    "Can only get a NEWARRAY typecode for a primitive type" ) ;
    }

    public void emitNewArrayCall( Type type ) {
	if (type.isPrimitive())
	    mv.visitIntInsn( NEWARRAY, typeCode(type) ) ;
	else
	    mv.visitTypeInsn( ANEWARRAY, ASMUtil.bcName(type) ) ;
    }

    /** Emit a static INVOKE instruction. 
     */
    public void emitStaticInvoke( Type type, 
	String name, Signature sig ) {

	mv.visitMethodInsn( INVOKESTATIC, ASMUtil.bcName(type), name, 
	    sig.signature() ) ;
    }

    /** Emit the appropriate non-static INVOKE instruction as follows:
     * <ol>
     * <li>If type is an interface, emit INVOKEINTERFACE.
     * <li>If name/sig has private access in type, emit INVOKESPECIAL.  Note
     * that the target must be "this" in this case.
     * <li>Otherwise emit INVOKEVIRTUAL.
     * </ol>
     */
    public void emitInvoke( Type type, String name, Signature sig ) {
	String sigString = sig.signature() ;

	ClassInfo targetInfo ;
	targetInfo = type.classInfo() ;

	MethodInfo minfo = targetInfo.findMethodInfo( name, sig ) ;
	if (minfo == null)
	    throw new IllegalArgumentException( 
		"Could not find a method " + name + " with signature " +
		sig + " in class " + targetInfo.name() ) ;

	ClassInfo mcinfo = minfo.myClassInfo() ;
	boolean privateMethod = Modifier.isPrivate( minfo.modifiers() ) ;

	int opcode ;
	if (mcinfo.isInterface())
	    opcode = INVOKEINTERFACE ;
	else if (privateMethod) 
	    opcode = INVOKESPECIAL ;
	else
	    opcode = INVOKEVIRTUAL ;

	String typeName = ASMUtil.bcName(mcinfo.thisType()) ;

	mv.visitMethodInsn( opcode, typeName, name, sigString ) ;
    }

    /** Emit the INVOKESPECIAL instruction for calling a constructor
     * with the given signature.  This is used for new Foo() calls,
     * and for super() and this() calls at the start of a constructor.
     */
    public void emitNewInvoke( Type type, Signature sig ) {

	String typeName = ASMUtil.bcName(type) ;
	mv.visitMethodInsn( INVOKESPECIAL, typeName, 
	    CodeGeneratorUtil.CONSTRUCTOR_METHOD_NAME, sig.signature()) ;
    }

    /** Emit the INVOKESPECIAL instruction for calling a method
     * with the given signature.  This is used for 
     * for super.name() and this.name() method calls.
     */
    public void emitSpecialInvoke( Type type, String name, Signature sig ) {

	String typeName = ASMUtil.bcName(type) ;
	mv.visitMethodInsn( INVOKESPECIAL, typeName, 
	    name, sig.signature() ) ;
    }

    public void emitThrow() {
	mv.visitInsn( ATHROW ) ;
    }

    public void emitExceptionTableEntry( Label start, Label end, Label handler,
	Type exceptionType ) {
	String exceptionTypeName = 
	    exceptionType == null ? null : ASMUtil.bcName(exceptionType) ;
	mv.visitTryCatchBlock( start, end, handler, exceptionTypeName ) ;
    }

    public void emitJsr( Label label ) {
	mv.visitJumpInsn( JSR, label ) ;
    }

    public void callEmitter( EmitterFactory.Emitter emitter ) {
	emitter.evaluate( mv ) ;
    }

    public void emitPop() {
	mv.visitInsn( POP ) ;
    }

    private static Map<Type,Integer> typeIndex = new HashMap<Type,Integer>() ;

    static {
	typeIndex.put( Type._byte(),   0 ) ;
	typeIndex.put( Type._short(),  1 ) ;
	typeIndex.put( Type._char(),   2 ) ;
	typeIndex.put( Type._int(),    3 ) ;
	typeIndex.put( Type._long(),   4 ) ;
	typeIndex.put( Type._float(),  5 ) ;
	typeIndex.put( Type._double(), 6 ) ;
    }

    private static final EmitterFactory.Emitter E_NOP = new EmitterFactory.NullEmitter() ;
    private static final EmitterFactory.Emitter E_I2B = new EmitterFactory.SimpleEmitter( I2B ) ;
    private static final EmitterFactory.Emitter E_I2C = new EmitterFactory.SimpleEmitter( I2C ) ;
    private static final EmitterFactory.Emitter E_I2S = new EmitterFactory.SimpleEmitter( I2S ) ;

    private static final EmitterFactory.Emitter E_I2L = new EmitterFactory.SimpleEmitter( I2L ) ;
    private static final EmitterFactory.Emitter E_I2F = new EmitterFactory.SimpleEmitter( I2F ) ;
    private static final EmitterFactory.Emitter E_I2D = new EmitterFactory.SimpleEmitter( I2D ) ;
    private static final EmitterFactory.Emitter E_L2I = new EmitterFactory.SimpleEmitter( L2I ) ;
    private static final EmitterFactory.Emitter E_F2I = new EmitterFactory.SimpleEmitter( F2I ) ;
    private static final EmitterFactory.Emitter E_D2I = new EmitterFactory.SimpleEmitter( D2I ) ;

    private static final EmitterFactory.Emitter E_L2F = new EmitterFactory.SimpleEmitter( L2F ) ;
    private static final EmitterFactory.Emitter E_L2D = new EmitterFactory.SimpleEmitter( L2D ) ;
    private static final EmitterFactory.Emitter E_F2L = new EmitterFactory.SimpleEmitter( F2L ) ;
    private static final EmitterFactory.Emitter E_D2L = new EmitterFactory.SimpleEmitter( D2L ) ;

    private static final EmitterFactory.Emitter E_F2D = new EmitterFactory.SimpleEmitter( F2D ) ;
    private static final EmitterFactory.Emitter E_D2F = new EmitterFactory.SimpleEmitter( D2F ) ;

    private static final EmitterFactory.Emitter E_L2B = new EmitterFactory.CompoundEmitter(
	E_L2I, E_I2B ) ;
    private static final EmitterFactory.Emitter E_F2B = new EmitterFactory.CompoundEmitter(
	E_F2I, E_I2B ) ;
    private static final EmitterFactory.Emitter E_D2B = new EmitterFactory.CompoundEmitter(
	E_D2I, E_I2B ) ;

    private static final EmitterFactory.Emitter E_L2S = new EmitterFactory.CompoundEmitter(
	E_L2I, E_I2S ) ;
    private static final EmitterFactory.Emitter E_F2S = new EmitterFactory.CompoundEmitter(
	E_F2I, E_I2S ) ;
    private static final EmitterFactory.Emitter E_D2S = new EmitterFactory.CompoundEmitter(
	E_D2I, E_I2S ) ;

    private static final EmitterFactory.Emitter E_L2C = new EmitterFactory.CompoundEmitter(
	E_L2I, E_I2C ) ;
    private static final EmitterFactory.Emitter E_F2C = new EmitterFactory.CompoundEmitter(
	E_F2I, E_I2C ) ;
    private static final EmitterFactory.Emitter E_D2C = new EmitterFactory.CompoundEmitter(
	E_D2I, E_I2C ) ;

    // First index: from type, second index: to type
    EmitterFactory.Emitter[][] numericConversions = new EmitterFactory.Emitter[][] {
	// to:	
	// byte	    short	char	    int		long	    float	double
	{ E_I2B,    E_I2S,	E_I2C,	    E_NOP,	E_I2L,	    E_I2F,	E_I2D },   // from byte
	{ E_I2B,    E_I2S,	E_I2C,	    E_NOP,	E_I2L,	    E_I2F,	E_I2D },   // from short
	{ E_I2B,    E_I2S,	E_I2C,	    E_NOP,	E_I2L,	    E_I2F,	E_I2D },   // from char
	{ E_I2B,    E_I2S,	E_I2C,	    E_NOP,	E_I2L,	    E_I2F,	E_I2D },   // from int
	{ E_L2B,    E_L2S,	E_L2C,	    E_L2I,	E_NOP,	    E_L2F,	E_L2D },   // from long
	{ E_F2B,    E_F2S,	E_F2C,	    E_F2I,	E_F2L,	    E_NOP,	E_F2D },   // from float
	{ E_D2B,    E_D2S,	E_D2C,	    E_D2I,	E_D2L,	    E_D2F,	E_NOP }} ; // from double

    public void emitConversion( Type from, Type to ) {
	if (!from.isNumber())
	    throw new IllegalArgumentException( "From type " + from.name() 
		+ " is not a numeric type" ) ;
	if (!to.isNumber())
	    throw new IllegalArgumentException( "To type " + to.name() 
		+ " is not a numeric type" ) ;

	int fromIndex = typeIndex.get( from ) ; 
	int toIndex = typeIndex.get( to ) ; 

	EmitterFactory.Emitter emitter = numericConversions[fromIndex][toIndex] ;

	emitter.evaluate( mv ) ;
    }

    // Operator and type emitter tables
    private static Map<ExpressionFactory.BinaryOperator,Map<Type,Integer>> opInstructions =
	new HashMap<ExpressionFactory.BinaryOperator,Map<Type,Integer>>() ;
   
    private static Map<ExpressionFactory.BinaryOperator,Integer> ifOpInstructions =
	new HashMap<ExpressionFactory.BinaryOperator,Integer>() ;

    static {
	Map<Type,Integer> map = new HashMap<Type,Integer>() ;
	map.put( Type._boolean(), ISUB ) ;
	map.put( Type._byte(), ISUB ) ;
	map.put( Type._char(), ISUB ) ;
	map.put( Type._short(), ISUB ) ;
	map.put( Type._int(), ISUB ) ;
	map.put( Type._long(), LCMP ) ;
	map.put( Type._float(), FCMPG ) ;
	map.put( Type._double(), DCMPG ) ;
	opInstructions.put( ExpressionFactory.BinaryOperator.EQ, map ) ;
	opInstructions.put( ExpressionFactory.BinaryOperator.NE, map ) ;
	opInstructions.put( ExpressionFactory.BinaryOperator.GT, map ) ;
	opInstructions.put( ExpressionFactory.BinaryOperator.GE, map ) ;
	opInstructions.put( ExpressionFactory.BinaryOperator.LT, map ) ;
	opInstructions.put( ExpressionFactory.BinaryOperator.LE, map ) ;

	map.clear() ;
	map.put( Type._int(), IADD ) ;
	map.put( Type._long(), LADD ) ;
	map.put( Type._float(), FADD ) ;
	map.put( Type._double(), DADD ) ;
	opInstructions.put( ExpressionFactory.BinaryOperator.PLUS, map ) ;

	map.clear() ;
	map.put( Type._int(), ISUB ) ;
	map.put( Type._long(), LSUB ) ;
	map.put( Type._float(), FSUB ) ;
	map.put( Type._double(), DSUB ) ;
	opInstructions.put( ExpressionFactory.BinaryOperator.MINUS, map ) ;

	map.clear() ;
	map.put( Type._int(), IMUL ) ;
	map.put( Type._long(), LMUL ) ;
	map.put( Type._float(), FMUL ) ;
	map.put( Type._double(), DMUL ) ;
	opInstructions.put( ExpressionFactory.BinaryOperator.TIMES, map ) ;

	map.clear() ;
	map.put( Type._int(), IDIV ) ;
	map.put( Type._long(), LDIV ) ;
	map.put( Type._float(), FDIV ) ;
	map.put( Type._double(), DDIV ) ;
	opInstructions.put( ExpressionFactory.BinaryOperator.DIV, map ) ;

	map.clear() ;
	map.put( Type._int(), IREM ) ;
	map.put( Type._long(), LREM ) ;
	map.put( Type._float(), FREM ) ;
	map.put( Type._double(), DREM ) ;
	opInstructions.put( ExpressionFactory.BinaryOperator.REM, map ) ;
	
	ifOpInstructions.put( ExpressionFactory.BinaryOperator.EQ, IFEQ ) ;
	ifOpInstructions.put( ExpressionFactory.BinaryOperator.NE, IFNE ) ;
	ifOpInstructions.put( ExpressionFactory.BinaryOperator.GT, IFGT ) ;
	ifOpInstructions.put( ExpressionFactory.BinaryOperator.GE, IFGE ) ;
	ifOpInstructions.put( ExpressionFactory.BinaryOperator.LT, IFLT ) ;
	ifOpInstructions.put( ExpressionFactory.BinaryOperator.LE, IFLE ) ;
    }

    private void emitBooleanCodeForPrimitive( ExpressionFactory.BinaryOperatorExpression arg ) {
	MyLabel internalLabel = new MyLabel() ;
	MyLabel exitLabel = ASMByteCodeVisitor.nextLabel( arg ) ;
	if (!ifOpInstructions.containsKey( arg.operator() )) 
	    throw new IllegalStateException( 
		"emitBooleanCode called with operator " + arg 
		+ ", which is not a relational operator" ) ;
	mv.visitJumpInsn( ifOpInstructions.get(arg.operator()), internalLabel ) ;
	mv.visitInsn( ICONST_0 ) ;
	mv.visitJumpInsn( GOTO, exitLabel ) ;
	mv.visitLabel( internalLabel ) ;
	mv.visitInsn( ICONST_1 ) ;
    }

    private void emitBooleanCodeForReference( 
	ExpressionFactory.BinaryOperatorExpression arg ) {

	MyLabel internalLabel = new MyLabel() ;
	MyLabel exitLabel = new MyLabel() ;

	if (arg.operator() == ExpressionFactory.BinaryOperator.EQ) {
	    mv.visitJumpInsn( IF_ACMPEQ, internalLabel ) ;
	} else if (arg.operator() == ExpressionFactory.BinaryOperator.NE) {
	    mv.visitJumpInsn( IF_ACMPNE, internalLabel ) ;
	}

	mv.visitInsn( ICONST_0 ) ;
	mv.visitJumpInsn( GOTO, exitLabel ) ;
	mv.visitLabel( internalLabel ) ;
	mv.visitInsn( ICONST_1 ) ;
	mv.visitLabel( exitLabel ) ;
    }

    //
    public void emitBinaryOperator( ExpressionFactory.BinaryOperatorExpression arg ) {
	// This is complicated due to the rather ugly structure of the JVM bytecodes
	// for comparisons.  For now, we will not attempt to generate optimal code.
	// Rather we will try to keep this clean and simple.  Unfortunately all of
	// the relational operators require emitting code that contains branches.
	// Cases:
	// 1. EQ, NE: these are the only operators that apply to reference types.
	//    We need to use if_acmpeq and if_acmpne to compile these.
	//    Each of these is followed by:
	//	    ifOP L1
	//	    iconst_0
	//	    GOTO next label
	//	L1: iconst_1
	// 2. relational operators: these only apply to numeric arguments.  We
	//    can assume that both expressions have the same type here, since that
	//    is guaranteed by binary promotions in the ExpressionFactory code. 
	//    The basic pattern here is to compare, then use the appropriate branch
	//    on an integer value as follows:
	//	int: isub
	//	long: lcmp
	//	float: fcmp
	//	double: dcmp
	//    Each of these is followed by:
	//	    ifOP L1
	//	    iconst_0
	//	    GOTO next label
	//	L1: iconst_1
	// 3. Arithmetic.  These ops simply emit (i,l,f,d)(add,sub,mul,div,rem)
	//    instructions.
	//	    
	Type type = arg.left().type() ;
	ExpressionFactory.BinaryOperator op = arg.operator() ;
	if (type.isPrimitive()) {
	    mv.visitInsn( opInstructions.get(op).get(type) ) ;
	    if (op.kind() == ExpressionFactory.BinaryOperatorKind.RELATIONAL) {
		emitBooleanCodeForPrimitive( arg ) ;
	    }
	} else { // Only ops for reference types are EQ and NE
	    if ((op == ExpressionFactory.BinaryOperator.EQ) || 
	        (op == ExpressionFactory.BinaryOperator.NE)) {
		emitBooleanCodeForReference( arg ) ;
	    } else {
		throw new IllegalStateException( 
		    "Binary operator argument types are " + type.name() 
		    + " but operator is not EQ or NE" ) ;
	    }
	}
    }
}
