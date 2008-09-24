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

import java.lang.reflect.Modifier ;

import java.util.BitSet ;
import java.util.List ;
import java.util.ArrayList ;
import java.util.Iterator ;

import com.sun.corba.se.spi.orbutil.generic.UnaryVoidFunction ;

import com.sun.corba.se.spi.orbutil.codegen.Type ;
import com.sun.corba.se.spi.orbutil.codegen.Signature ;
import com.sun.corba.se.spi.orbutil.codegen.Variable ;
import com.sun.corba.se.spi.orbutil.codegen.ClassInfo ;
import com.sun.corba.se.spi.orbutil.codegen.FieldInfo ;

import com.sun.corba.se.impl.orbutil.codegen.ExpressionFactory ;

import org.objectweb.asm.MethodVisitor ;
import org.objectweb.asm.ClassWriter ;

import static org.objectweb.asm.Opcodes.* ;

/** This class provides methods that allow the construction of an 
 * object that can later be used to emit a bytecode.  This is useful 
 * when multiple visitors are needed for first preparing an AST, 
 * then generating the bytecode.
 */
public final class EmitterFactory {
    private EmitterFactory() {}

    /** Simple wrapper class around a UnaryVoidFunction.  This exists
     * to avoid problems with using nested generics.
     */
    public interface Emitter extends UnaryVoidFunction<MethodVisitor> {
    }

    private static final int MAX_OPCODE = 255 ;

    private static String[] opcodeNames = new String[MAX_OPCODE+1] ;

    static {
	for (int ctr = 0; ctr<=MAX_OPCODE; ctr++) 
	    opcodeNames[ctr] = "ILLEGAL_" + ctr ;

	opcodeNames[0] = "NOP" ; 
	opcodeNames[1] = "ACONST_NULL" ; 
	opcodeNames[2] = "ICONST_M1" ; 
	opcodeNames[3] = "ICONST_0" ; 
	opcodeNames[4] = "ICONST_1" ; 
	opcodeNames[5] = "ICONST_2" ; 
	opcodeNames[6] = "ICONST_3" ; 
	opcodeNames[7] = "ICONST_4" ; 
	opcodeNames[8] = "ICONST_5" ; 
	opcodeNames[9] = "LCONST_0" ; 
	opcodeNames[10] = "LCONST_1" ; 
	opcodeNames[11] = "FCONST_0" ; 
	opcodeNames[12] = "FCONST_1" ; 
	opcodeNames[13] = "FCONST_2" ; 
	opcodeNames[14] = "DCONST_0" ; 
	opcodeNames[15] = "DCONST_1" ; 
	opcodeNames[16] = "BIPUSH" ; 
	opcodeNames[17] = "SIPUSH" ; 
	opcodeNames[18] = "LDC" ; 
	opcodeNames[19] = "LDC_W" ; 
	opcodeNames[20] = "LDC2_W" ; 
	opcodeNames[21] = "ILOAD" ; 
	opcodeNames[22] = "LLOAD" ; 
	opcodeNames[23] = "FLOAD" ; 
	opcodeNames[24] = "DLOAD" ; 
	opcodeNames[25] = "ALOAD" ; 
	opcodeNames[26] = "ILOAD_0" ; 
	opcodeNames[27] = "ILOAD_1" ; 
	opcodeNames[28] = "ILOAD_2" ; 
	opcodeNames[29] = "ILOAD_3" ; 
	opcodeNames[30] = "LLOAD_0" ; 
	opcodeNames[31] = "LLOAD_1" ; 
	opcodeNames[32] = "LLOAD_2" ; 
	opcodeNames[33] = "LLOAD_3" ; 
	opcodeNames[34] = "FLOAD_0" ; 
	opcodeNames[35] = "FLOAD_1" ; 
	opcodeNames[36] = "FLOAD_2" ; 
	opcodeNames[37] = "FLOAD_3" ; 
	opcodeNames[38] = "DLOAD_0" ; 
	opcodeNames[39] = "DLOAD_1" ; 
	opcodeNames[40] = "DLOAD_2" ; 
	opcodeNames[41] = "DLOAD_3" ; 
	opcodeNames[42] = "ALOAD_0" ; 
	opcodeNames[43] = "ALOAD_1" ; 
	opcodeNames[44] = "ALOAD_2" ; 
	opcodeNames[45] = "ALOAD_3" ; 
	opcodeNames[46] = "IALOAD" ; 
	opcodeNames[47] = "LALOAD" ; 
	opcodeNames[48] = "FALOAD" ; 
	opcodeNames[49] = "DALOAD" ; 
	opcodeNames[50] = "AALOAD" ; 
	opcodeNames[51] = "BALOAD" ; 
	opcodeNames[52] = "CALOAD" ; 
	opcodeNames[53] = "SALOAD" ; 
	opcodeNames[54] = "ISTORE" ; 
	opcodeNames[55] = "LSTORE" ; 
	opcodeNames[56] = "FSTORE" ; 
	opcodeNames[57] = "DSTORE" ; 
	opcodeNames[58] = "ASTORE" ; 
	opcodeNames[59] = "ISTORE_0" ; 
	opcodeNames[60] = "ISTORE_1" ; 
	opcodeNames[61] = "ISTORE_2" ; 
	opcodeNames[62] = "ISTORE_3" ; 
	opcodeNames[63] = "LSTORE_0" ; 
	opcodeNames[64] = "LSTORE_1" ; 
	opcodeNames[65] = "LSTORE_2" ; 
	opcodeNames[66] = "LSTORE_3" ; 
	opcodeNames[67] = "FSTORE_0" ; 
	opcodeNames[68] = "FSTORE_1" ; 
	opcodeNames[69] = "FSTORE_2" ; 
	opcodeNames[70] = "FSTORE_3" ; 
	opcodeNames[71] = "DSTORE_0" ; 
	opcodeNames[72] = "DSTORE_1" ; 
	opcodeNames[73] = "DSTORE_2" ; 
	opcodeNames[74] = "DSTORE_3" ; 
	opcodeNames[75] = "ASTORE_0" ; 
	opcodeNames[76] = "ASTORE_1" ; 
	opcodeNames[77] = "ASTORE_2" ; 
	opcodeNames[78] = "ASTORE_3" ; 
	opcodeNames[79] = "IASTORE" ; 
	opcodeNames[80] = "LASTORE" ; 
	opcodeNames[81] = "FASTORE" ; 
	opcodeNames[82] = "DASTORE" ; 
	opcodeNames[83] = "AASTORE" ; 
	opcodeNames[84] = "BASTORE" ; 
	opcodeNames[85] = "CASTORE" ; 
	opcodeNames[86] = "SASTORE" ; 
	opcodeNames[87] = "POP" ; 
	opcodeNames[88] = "POP2" ; 
	opcodeNames[89] = "DUP" ; 
	opcodeNames[90] = "DUP_X1" ; 
	opcodeNames[91] = "DUP_X2" ; 
	opcodeNames[92] = "DUP2" ; 
	opcodeNames[93] = "DUP2_X1" ; 
	opcodeNames[94] = "DUP2_X2" ; 
	opcodeNames[95] = "SWAP" ; 
	opcodeNames[96] = "IADD" ; 
	opcodeNames[97] = "LADD" ; 
	opcodeNames[98] = "FADD" ; 
	opcodeNames[99] = "DADD" ; 
	opcodeNames[100] = "ISUB" ; 
	opcodeNames[101] = "LSUB" ; 
	opcodeNames[102] = "FSUB" ; 
	opcodeNames[103] = "DSUB" ; 
	opcodeNames[104] = "IMUL" ; 
	opcodeNames[105] = "LMUL" ; 
	opcodeNames[106] = "FMUL" ; 
	opcodeNames[107] = "DMUL" ; 
	opcodeNames[108] = "IDIV" ; 
	opcodeNames[109] = "LDIV" ; 
	opcodeNames[110] = "FDIV" ; 
	opcodeNames[111] = "DDIV" ; 
	opcodeNames[112] = "IREM" ; 
	opcodeNames[113] = "LREM" ; 
	opcodeNames[114] = "FREM" ; 
	opcodeNames[115] = "DREM" ; 
	opcodeNames[116] = "INEG" ; 
	opcodeNames[117] = "LNEG" ; 
	opcodeNames[118] = "FNEG" ; 
	opcodeNames[119] = "DNEG" ; 
	opcodeNames[120] = "ISHL" ; 
	opcodeNames[121] = "LSHL" ; 
	opcodeNames[122] = "ISHR" ; 
	opcodeNames[123] = "LSHR" ; 
	opcodeNames[124] = "IUSHR" ; 
	opcodeNames[125] = "LUSHR" ; 
	opcodeNames[126] = "IAND" ; 
	opcodeNames[127] = "LAND" ; 
	opcodeNames[128] = "IOR" ; 
	opcodeNames[129] = "LOR" ; 
	opcodeNames[130] = "IXOR" ; 
	opcodeNames[131] = "LXOR" ; 
	opcodeNames[132] = "IINC" ; 
	opcodeNames[133] = "I2L" ; 
	opcodeNames[134] = "I2F" ; 
	opcodeNames[135] = "I2D" ; 
	opcodeNames[136] = "L2I" ; 
	opcodeNames[137] = "L2F" ; 
	opcodeNames[138] = "L2D" ; 
	opcodeNames[139] = "F2I" ; 
	opcodeNames[140] = "F2L" ; 
	opcodeNames[141] = "F2D" ; 
	opcodeNames[142] = "D2I" ; 
	opcodeNames[143] = "D2L" ; 
	opcodeNames[144] = "D2F" ; 
	opcodeNames[145] = "I2B" ; 
	opcodeNames[146] = "I2C" ; 
	opcodeNames[147] = "I2S" ; 
	opcodeNames[148] = "LCMP" ; 
	opcodeNames[149] = "FCMPL" ; 
	opcodeNames[150] = "FCMPG" ; 
	opcodeNames[151] = "DCMPL" ; 
	opcodeNames[152] = "DCMPG" ; 
	opcodeNames[153] = "IFEQ" ; 
	opcodeNames[154] = "IFNE" ; 
	opcodeNames[155] = "IFLT" ; 
	opcodeNames[156] = "IFGE" ; 
	opcodeNames[157] = "IFGT" ; 
	opcodeNames[158] = "IFLE" ; 
	opcodeNames[159] = "IF_ICMPEQ" ; 
	opcodeNames[160] = "IF_ICMPNE" ; 
	opcodeNames[161] = "IF_ICMPLT" ; 
	opcodeNames[162] = "IF_ICMPGE" ; 
	opcodeNames[163] = "IF_ICMPGT" ; 
	opcodeNames[164] = "IF_ICMPLE" ; 
	opcodeNames[165] = "IF_ACMPEQ" ; 
	opcodeNames[166] = "IF_ACMPNE" ; 
	opcodeNames[167] = "GOTO" ; 
	opcodeNames[168] = "JSR" ; 
	opcodeNames[169] = "RET" ; 
	opcodeNames[170] = "TABLESWITCH" ; 
	opcodeNames[171] = "LOOKUPSWITCH" ; 
	opcodeNames[172] = "IRETURN" ; 
	opcodeNames[173] = "LRETURN" ; 
	opcodeNames[174] = "FRETURN" ; 
	opcodeNames[175] = "DRETURN" ; 
	opcodeNames[176] = "ARETURN" ; 
	opcodeNames[177] = "RETURN" ; 
	opcodeNames[178] = "GETSTATIC" ; 
	opcodeNames[179] = "PUTSTATIC" ; 
	opcodeNames[180] = "GETFIELD" ; 
	opcodeNames[181] = "PUTFIELD" ; 
	opcodeNames[182] = "INVOKEVIRTUAL" ; 
	opcodeNames[183] = "INVOKESPECIAL" ; 
	opcodeNames[184] = "INVOKESTATIC" ; 
	opcodeNames[185] = "INVOKEINTERFACE" ; 
	opcodeNames[186] = "UNUSED" ; 
	opcodeNames[187] = "NEW" ; 
	opcodeNames[188] = "NEWARRAY" ; 
	opcodeNames[189] = "ANEWARRAY" ; 
	opcodeNames[190] = "ARRAYLENGTH" ; 
	opcodeNames[191] = "ATHROW" ; 
	opcodeNames[192] = "CHECKCAST" ; 
	opcodeNames[193] = "INSTANCEOF" ; 
	opcodeNames[194] = "MONITORENTER" ; 
	opcodeNames[195] = "MONITOREXIT" ; 
	opcodeNames[196] = "WIDE" ; 
	opcodeNames[197] = "MULTIANEWARRAY" ; 
	opcodeNames[198] = "IFNULL" ; 
	opcodeNames[199] = "IFNONNULL" ; 
	opcodeNames[200] = "GOTO_W" ; 
	opcodeNames[201] = "JSR_W" ; 
    }

    // Construct a BitSet for the given arguments.
    private static BitSet makeBitSet( int... args ) {
	BitSet result = new BitSet( MAX_OPCODE + 1 ) ;
	for (int value : args )
	    result.set( value ) ;
	return result ;
    }

    // The following BitSets represent the valid opcodes that can
    // be specified on particular MethodVisitor methods.
    private static  BitSet visitInsnSet = makeBitSet( NOP, ACONST_NULL, 
	ICONST_M1, ICONST_0, ICONST_1, ICONST_2, ICONST_3, ICONST_4, ICONST_5, 
	LCONST_0, LCONST_1, FCONST_0, FCONST_1, FCONST_2, DCONST_0, DCONST_1, 
	IALOAD, LALOAD, FALOAD, DALOAD, AALOAD, BALOAD, CALOAD, SALOAD, 
	IASTORE, LASTORE, FASTORE, DASTORE, AASTORE, BASTORE, CASTORE, 
	SASTORE, POP, POP2, DUP, DUP_X1, DUP_X2, DUP2, DUP2_X1, DUP2_X2, SWAP, 
	IADD, LADD, FADD, DADD, ISUB, LSUB, FSUB, DSUB, IMUL, LMUL, FMUL, DMUL,
	IDIV, LDIV, FDIV, DDIV, IREM, LREM, FREM, DREM, INEG, LNEG, FNEG, DNEG,
	ISHL, LSHL, ISHR, LSHR, IUSHR, LUSHR, IAND, LAND, IOR, LOR, IXOR, LXOR,
	I2L, I2F, I2D, L2I, L2F, L2D, F2I, F2L, F2D, D2I, D2L, D2F, 
	I2B, I2C, I2S, 
	LCMP, FCMPL, FCMPG, DCMPL, DCMPG, IRETURN, LRETURN, FRETURN, DRETURN, 
	ARETURN, RETURN, ARRAYLENGTH, ATHROW, MONITORENTER, MONITOREXIT, 
	ARRAYLENGTH ) ;

    private static  BitSet visitIntInsnSet = makeBitSet( BIPUSH, SIPUSH, 
	NEWARRAY ) ;

    private static  BitSet visitVarInsnSet = makeBitSet( 
	ILOAD, LLOAD, FLOAD, DLOAD, ALOAD, RET, 
	ISTORE, LSTORE, FSTORE, DSTORE, ASTORE ) ;

    private static  BitSet visitFieldInsnSet = makeBitSet( 
	GETSTATIC, PUTSTATIC, GETFIELD, PUTFIELD ) ;

    private static  BitSet visitMethodInsnSet = makeBitSet( 
	INVOKEVIRTUAL, INVOKESPECIAL, INVOKESTATIC, INVOKEINTERFACE ) ;

    private static  BitSet visitTypeInsnSet = makeBitSet( 
	NEW, ANEWARRAY, CHECKCAST, INSTANCEOF ) ;

    private static  BitSet visitJumpInsnSet = makeBitSet( 
	IFEQ, IFNE, IFLT, IFGE, IFGT, IFLE, 
	IF_ICMPEQ, IF_ICMPNE, IF_ICMPLT, IF_ICMPGE, IF_ICMPGT, IF_ICMPLE, 
	IF_ACMPEQ, IF_ACMPNE, GOTO, JSR, IFNULL, IFNONNULL ) ;

    // Unique opcodes that require their own visitor methods:
    // 
    private static  BitSet specialOpcodeSet = makeBitSet( IINC, LDC, 
	TABLESWITCH, LOOKUPSWITCH, MULTIANEWARRAY ) ;

    private static  BitSet validOpcodeSet = makeBitSet() ;

    static {
	validOpcodeSet.or( visitInsnSet ) ;
	validOpcodeSet.or( visitIntInsnSet ) ;
	validOpcodeSet.or( visitVarInsnSet ) ;
	validOpcodeSet.or( visitFieldInsnSet ) ;
	validOpcodeSet.or( visitMethodInsnSet ) ;
	validOpcodeSet.or( visitTypeInsnSet ) ;
	validOpcodeSet.or( visitJumpInsnSet ) ;
	validOpcodeSet.or( specialOpcodeSet ) ;
    }

    private static void check( BitSet validOps, int op ) {
	if (!validOpcodeSet.get( op )) 
	    throw new IllegalArgumentException( 
		op + " is not a valid Bytecode" ) ;

	if (!validOps.get( op ))
	    throw new IllegalArgumentException( 
		op + " is not a valid Bytecode for this emitter" ) ;
    }

    public static class NullEmitter implements Emitter {
	public NullEmitter() { }

	public void evaluate( MethodVisitor mv ) {
	    // Does nothing.
	}

	public String toString() {
	    return "NullEmitter[]" ;
	}

	public int hashCode() {
	    return 0 ;
	}

	public boolean equals( Object obj ) {
	    if (obj == this)
		return true ;

	    if (!(obj instanceof NullEmitter))
		return false ;

	    return true ;
	}
    }

    public static class CompoundEmitter implements Emitter {
	private List<Emitter> emitters;

	public CompoundEmitter( Emitter... args ) {
	    emitters = new ArrayList<Emitter>() ;
	    for (Emitter e : args)
		emitters.add( e ) ;
	}

	public void evaluate( MethodVisitor mv ) {
	    for (Emitter e : emitters) 
		e.evaluate( mv ) ;
	}

	public String toString() {
	    StringBuilder sb = new StringBuilder() ;
	    sb.append( "CompoundEmitter[" ) ;
	    boolean first = true ;
	    for (Emitter e : emitters) {
		if (first) 
		    first = false ;
		else
		    sb.append( ", " ) ;
		sb.append( e.toString() ) ;
	    }
	    sb.append( "]" ) ;
	    return sb.toString() ;
	}

	public int hashCode() {
	    int hash = 0 ;
	    for (Emitter e : emitters) 
		hash ^= e.hashCode() ;
	    return hash ;
	}

	public boolean equals( Object obj ) {
	    if (obj == this)
		return true ;

	    if (!(obj instanceof CompoundEmitter))
		return false ;

	    CompoundEmitter other = CompoundEmitter.class.cast( obj ) ;

	    Iterator<Emitter> it1 = emitters.iterator() ;
	    Iterator<Emitter> it2 = other.emitters.iterator() ;
	    while (it1.hasNext() && it2.hasNext()) {
		Emitter e1 = it1.next() ;
		Emitter e2 = it2.next() ;
		if (!e1.equals( e2 ))
		    return false ;
	    }
	    return it1.hasNext() == it2.hasNext() ;
	}
    }

    // Just emit a simple opcode.  Can be used for visitInsnSet.
    public static class SimpleEmitter implements Emitter {
	private int opcode ;

	public SimpleEmitter( int opcode ) {
	    check( visitInsnSet, opcode ) ;
	    this.opcode = opcode ;
	}

	public void evaluate( MethodVisitor mv ) {
	    mv.visitInsn( opcode ) ;
	}

	public String toString() {
	    return "SimpleEmitter[" + opcodeNames[opcode] + "]" ;
	}

	public int hashCode() {
	    return opcode ;
	}

	public boolean equals( Object obj ) {
	    if (obj == this)
		return true ;

	    if (!(obj instanceof SimpleEmitter))
		return false ;

	    SimpleEmitter other = SimpleEmitter.class.cast( obj ) ;

	    return other.opcode == opcode ;
	}
    }

    // Emit an opcode with a single int operand.  Can be used for
    // either visitIntInsn or visitVarInsn.
    private static class IntOperandEmitter implements Emitter {
	private static BitSet validOps = new BitSet() ;
	
	static {
	    validOps.or( visitIntInsnSet ) ;
	    validOps.or( visitVarInsnSet ) ;
	}

	private int opcode ;
	private int arg ;

	public IntOperandEmitter( int opcode, int arg ) {
	    check( validOps, opcode ) ;
	    this.opcode = opcode ;
	    this.arg = arg ;
	}

	public void evaluate( MethodVisitor mv ) {
	    if (visitIntInsnSet.get( opcode )) {
		mv.visitIntInsn( opcode, arg ) ;
	    } else {
		assert visitVarInsnSet.get( opcode ) ;
		mv.visitVarInsn( opcode, arg ) ;
	    } ;
	}

	public String toString() {
	    return "IntOperandEmitter[" + opcodeNames[opcode] + " " 
		+ arg + "]" ;
	}

	public int hashCode() {
	    return opcode * 91 + arg ;
	}

	public boolean equals( Object obj ) {
	    if (obj == this)
		return true ;

	    if (!(obj instanceof IntOperandEmitter))
		return false ;

	    IntOperandEmitter other = 
		IntOperandEmitter.class.cast( obj ) ;

	    return other.opcode == opcode && other.arg == arg ;
	}
    }

    private static class FieldInsnEmitter implements Emitter {
	private int opcode ;
	private String owner ;
	private String name ;
	private String desc ;

	/** The opcode must be one of the PUT/GET FIELD/STATIC 
	 * instructions.
	 * @param Owner is the class name containing the field.
	 * This is constructed from ASMUtil.bcName( cgen.name() ).
	 * @param Name is the name of the field.
	 * @param Desc is the field's descriptor.  This is obtained from the
	 * Type.signature() method.
	 */
	public FieldInsnEmitter( int opcode, String owner, String name, 
	    String desc ) {
	    check( visitFieldInsnSet, opcode ) ;
	    this.opcode = opcode ;
	    this.owner = owner ;
	    this.name = name ;
	    this.desc = desc ;
	}
	
	public void evaluate( MethodVisitor mv ) {
	    mv.visitFieldInsn( opcode, owner, name, desc ) ;
	}

	public String toString() {
	    return "FieldInsnEmitter[" + opcodeNames[opcode] 
		+ " \"" + owner + "\""
		+ " \"" + name + "\""
		+ " \"" + desc + "\""
		+ "]" ;
	}

	public int hashCode() {
	    return opcode * 91 ^ owner.hashCode() 
		^ name.hashCode() ^ desc.hashCode() ;
	}

	public boolean equals( Object obj ) {
	    if (obj == this)
		return true ;

	    if (!(obj instanceof FieldInsnEmitter))
		return false ;

	    FieldInsnEmitter other = FieldInsnEmitter.class.cast( obj ) ;

	    return other.opcode == opcode && 
		other.owner.equals( owner ) &&
		other.name.equals( name ) &&
		other.desc.equals( desc ) ;
	}
    }
    
    // There are several cases here:
    //
    // 1. Local variables.  This is the case for MethodGenerator arguments,
    //    BlockStatement definitions, catch clause exception variables,
    //    non-void return holders, and the uncaught exception holder
    //    for finally clauses.  Here the Variable passed in must already
    //    have an attribute named "stackFrameSlot" that holds an
    //    Integer that gives the start of the local variables.
    // 2. Non-static class data members.  Here the parent of the
    //    Variable must be a ClassGenerator, and the type information
    //    from the ClassGenerator is required in the FieldInsnEmitter.
    //    The emitters here use GETFIELD/PUTFIELD.
    // 3. Static class data members.  This is just like case 2,
    //    but the emitters must use the GETSTATIC/PUTSTATIC opcodes.
    // 4. FieldAccessExpression.
    // 5. ArrayIndexExpression.
    //
    
    private static int getVarInsnOpcode( Type type, boolean isStore ) {
	if (isStore) {
	    if (!type.isPrimitive()) {
		return ASTORE ;
	    } else if (type == Type._float()) {
		return FSTORE ;
	    } else if (type == Type._double()) {
		return DSTORE ;
	    } else if (type == Type._long()) {
		return LSTORE ;
	    } else {
		// must be boolean, byte, char, short, or int.
		// All of these are handled the same way.
		return ISTORE ;
	    }
	} else {
	    if (!type.isPrimitive()) {
		return ALOAD ;
	    } else if (type == Type._float()) {
		return FLOAD ;
	    } else if (type == Type._double()) {
		return DLOAD ;
	    } else if (type == Type._long()) {
		return LLOAD ;
	    } else {
		// must be boolean, byte, char, short, or int.
		// All of these are handled the same way.
		return ILOAD ;
	    }
	}
    }
    
    private static Emitter makeFieldInsnEmitter( boolean isStore, boolean isStatic,
	Type targetType, String name, Type varType ) {

	String owner = ASMUtil.bcName( targetType ) ;
	String descriptor = varType.signature() ;
	int insn = isStore ? 
	    (isStatic ? PUTSTATIC : PUTFIELD) :
	    (isStatic ? GETSTATIC : GETFIELD) ;
	return new FieldInsnEmitter( insn, owner, name, descriptor ) ;
    }

    /** Create an emitter that generates the instruction needed to
     * either store the TOS value into the variable (isStore==true)
     * or push the variable's value onto the stack (isStore==false).
     * The stack index must be set on var in the stackFrameSlot
     * attribute.
     */
    public static Emitter makeEmitter( Variable var, boolean isStore ) {
	Integer slot = ASMUtil.stackFrameSlot.get( var ) ;
	assert slot != null ;
	return new IntOperandEmitter( getVarInsnOpcode( var.type(), isStore ),
	    slot ) ;
    }

    /** Create an emitter that generates the instruction needed to
     * either store the TOS value into the field (isStore==true)
     * or push the fields's value onto the stack (isStore==false).
     */
    public static Emitter makeEmitter( String fieldName, 
	Type targetType, boolean isStore, boolean isStatic ) {

	ClassInfo cinfo = targetType.classInfo() ;
	FieldInfo fld = cinfo.findFieldInfo( fieldName ) ;
	if (fld == null)
	    throw new IllegalArgumentException( "Field " + fieldName + 
		" is not a valid field in class " + targetType.name() ) ;

	ClassInfo definingClass = getAncestor( ClassGenerator.class ) ;
	ClassInfo accessingClass = targetType.classInfo() ;
	if (!fld.isAccessibleInContext( definingClass, accessingClass ) ;
	    throw new IllegalArgumentException( "Field " + fieldName
		+ " cannot be accessed from class " + definingClass.name() 
		+ " in an expression of type " + accessingClass.name() ) ;

	return makeFieldInsnEmitter( isStore, isStatic, targetType, fieldName,
	    fld.type() ) ;
    }

    public static Emitter makeEmitter( 
	ExpressionFactory.NonStaticFieldAccessExpression expr,
	boolean isStore ) {

	Type targetType = expr.target().type() ;	
	makeEmitter( expr.fieldName(), targetType, isStore, false ) ;
    }

    public static Emitter makeEmitter( 
	ExpressionFactory.StaticFieldAccessExpression expr,
	boolean isStore ) {
	Type targetType = expr.target() ;
	makeEmitter( expr.fieldName(), targetType, isStore, true ) ;
    }

    private static final Emitter arrayStore = new SimpleEmitter( AASTORE ) ;
    private static final Emitter arrayLoad = new SimpleEmitter( AALOAD ) ;

    /** Create an emitter that generates the instruction needed to
     * either store the TOS value into an array (aastore) (isStore==true)
     * or push the array element's value onto the stack (aaload) 
     * (isStore==false).
     * This emitter assumes that arrayref and index are already on the stack,
     * and value is on the stack either before (aastore) or after (aaload)
     * the instruction executes.
     */
    public static Emitter makeEmitter( 
	ExpressionFactory.ArrayIndexExpression expr,
	boolean isStore ) {
	if (isStore) {
	    return arrayStore ;
	} else {
	    return arrayLoad ;
	}
    }

    private static final Emitter arrayLength = new SimpleEmitter( ARRAYLENGTH ) ;

    public static Emitter makeEmitter( ExpressionFactory.ArrayLengthExpression expr ) {
	return arrayLength ;
    }
}
