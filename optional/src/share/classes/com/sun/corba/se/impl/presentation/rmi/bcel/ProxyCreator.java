/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2003-2007 Sun Microsystems, Inc. All rights reserved.
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

package com.sun.corba.se.impl.presentation.rmi.bcel;

import java.io.PrintStream ;

import java.lang.reflect.Method ;

import java.security.ProtectionDomain ;

import org.apache.bcel.Constants ;
import org.apache.bcel.generic.InstructionFactory ;
import org.apache.bcel.generic.InstructionList ;
import org.apache.bcel.generic.InstructionConstants ;
import org.apache.bcel.generic.ConstantPoolGen ;
import org.apache.bcel.generic.ClassGen ;
import org.apache.bcel.generic.MethodGen ;
import org.apache.bcel.generic.Type ;
import org.apache.bcel.generic.BasicType ;
import org.apache.bcel.generic.ReferenceType ;
import org.apache.bcel.generic.ArrayType ;
import org.apache.bcel.generic.PUSH ;

import java.io.ByteArrayOutputStream ;
import java.io.OutputStream ;
import java.io.IOException ;

import com.sun.corba.se.impl.codegen.CodeGeneratorUtil ;

/** Generate a proxy with a specified base class.  
 */
public class ProxyCreator implements Constants, ProxyClassCreator {
    // Name that generated code uses for inherited invocation method
    private static final String INVOKE_METHOD_NAME = "invoke" ; 
  
    // Constructor args
    private String className ;
    private String superClassName ;
    private Class[] interfaces ;
    private Method[] methods ;

    // BCEL classes
    private InstructionFactory instructionFactory;
    private ConstantPoolGen    constantPoolGen;
    private ClassGen           classGen;

    // Resulting classdata produced by BCEL
    private byte[] classData ;

    protected byte[] getClassData()
    {
	return classData ;
    }

    /** Construct a generator for a proxy class 
     * that implements the given interfaces and extends superClass.
     * superClass must satisfy the following requirements:
     * <ol>
     * <li>It must have an accessible no args constructor</li>
     * <li>It must have a method satisfying the signature
     * <code>    Object invoke( int methodNumber, Object[] args ) throws Throwable
     * </code>
     * </li>
     * <li>The invoke method described above must be accessible
     * to the generated class (generally either public or 
     * protected.</li>
     * </ol>
     * <p>
     * Each method in methods is implemented by a method that:
     * <ol>
     * <li>Creates an array sized to hold the args</li>
     * <li>Wraps args of primitive type in the appropriate wrapper.</li>
     * <li>Copies each arg or wrapper arg into the array.</li>
     * <li>Calls invoke with a method number corresponding to the
     * index of the method in methods.  Note that the invoke implementation
     * must use the same method array to figure out which method has been
     * invoked.</li>
     * <li>Return the result (if any), extracting values from wrappers
     * as needed to handle a return value of a primitive type.</li>
     * </ol>
     * <p>
     * Note that the generated methods ignore exceptions.
     * It is assumed that the invoke method may throw any
     * desired exception.
     * @param className the name of the generated class
     * @param superClassName the name of the class extends by the generated class
     * @param interfaces the interfaces implemented by the generated class
     * @param methods the methods that the generated class implements
     */
    public ProxyCreator( String className, String superClassName,
	Class[] interfaces, Method[] methods  )
    {
	this.className = className ;
	this.superClassName = superClassName ;
	this.interfaces = interfaces ;
	this.methods = methods ;

	// Compute names
	String fileName = getFileName( className ) ;

	String[] interfaceNames = new String[interfaces.length] ;
	for (int ctr=0; ctr<interfaces.length; ctr++ )
	    interfaceNames[ctr] = interfaces[ctr].getName() ;

	// Set up BCEL
	classGen = new ClassGen( className, superClassName, fileName,
	    ACC_PUBLIC | ACC_SUPER, interfaceNames ) ;

	constantPoolGen = classGen.getConstantPool();
	instructionFactory = new InstructionFactory(classGen, constantPoolGen);
	
    	// Generate the class
	createConstructor() ;
	createWriteReplace() ;
	for (int ctr=0; ctr<methods.length; ctr++)
	    createMethod( ctr, methods[ctr] ) ;

	// Dump it to a byte array
	ByteArrayOutputStream out = new ByteArrayOutputStream() ;
	try {
	    classGen.getJavaClass().dump(out);
	} catch (IOException exc) {
	    RuntimeException rexc = new RuntimeException( "Error in dumping class" ) ;
	    rexc.initCause( exc ) ;
	    throw rexc ;
	}

	// Save the byte array for create.
	classData = out.toByteArray() ;
    }

    public Class<?> create( ProtectionDomain pd, ClassLoader cl,
	boolean debug, PrintStream ps ) {

        final ClassLoader cld = cl;
	final ProtectionDomain pdm = pd;
	return java.security.AccessController.doPrivileged(
	       new java.security.PrivilegedAction<java.lang.Class>() {
	           public Class run() {
		       return CodeGeneratorUtil.makeClass( className, classData,
							   pdm, cld ) ;
		   }
	});

    }

    private String getFileName( String className ) 
    {
	int last = className.lastIndexOf( '.' ) ;
	return className.substring( last + 1 ) + ".java" ;
    }

    private void finalizeMethod( ClassGen classGen,
	InstructionList il, MethodGen method ) 
    {
	method.setMaxStack();
	method.setMaxLocals();
	classGen.addMethod(method.getMethod());
	il.dispose();
    }

    private void createConstructor() 
    {
	InstructionList il = new InstructionList();
	MethodGen method = new MethodGen(ACC_PUBLIC, Type.VOID, Type.NO_ARGS, 
	    new String[] {}, CodeGeneratorUtil.CONSTRUCTOR_METHOD_NAME, 
	    className, il, constantPoolGen);

	il.append( instructionFactory.createLoad( Type.OBJECT, 0 ) );
	il.append( 
	    instructionFactory.createInvoke( superClassName, 
		CodeGeneratorUtil.CONSTRUCTOR_METHOD_NAME, 
		Type.VOID, Type.NO_ARGS, Constants.INVOKESPECIAL ) );
	il.append( instructionFactory.createReturn( Type.VOID ) ) ;

	finalizeMethod( classGen, il, method ) ;
    }

    private void createWriteReplace()
    {
	InstructionList il = new InstructionList();
	MethodGen method = new MethodGen(ACC_PRIVATE, Type.OBJECT, Type.NO_ARGS, 
	    new String[] {}, "writeReplace", className, il, constantPoolGen);

	il.append( instructionFactory.createLoad( Type.OBJECT, 0 ) ) ;
	il.append( instructionFactory.createInvoke( className, "selfAsBaseClass",
	    Type.OBJECT, Type.NO_ARGS, Constants.INVOKEVIRTUAL ) ) ;
	il.append( instructionFactory.createReturn( Type.OBJECT ) ) ;

	finalizeMethod( classGen, il, method ) ;
    }


    private int typeLength( Type type )
    {
	if (type.equals( Type.LONG ))
	    return 2 ;

	if (type.equals( Type.DOUBLE ))
	    return 2 ;
	
	return 1 ;
    }

    private ReferenceType typeForWrapper( BasicType type )
    {
	String signature = "L" + wrapperName( type ) + ";" ;
	return (ReferenceType)Type.getType( signature ) ;
    }

    private String wrapperName( BasicType type )
    {
	if (type.equals( Type.BOOLEAN ))
	    return "java.lang.Boolean" ;
	if (type.equals( Type.BYTE ))
	    return "java.lang.Byte" ;
	if (type.equals( Type.CHAR ))
	    return "java.lang.Character" ;
	if (type.equals( Type.SHORT ))
	    return "java.lang.Short" ;
	if (type.equals( Type.INT ))
	    return "java.lang.Integer" ;
	if (type.equals( Type.LONG ))
	    return "java.lang.Long" ;
	if (type.equals( Type.FLOAT ))
	    return "java.lang.Float" ;
	if (type.equals( Type.DOUBLE ))
	    return "java.lang.Double" ;

	// Only other BasicType is VOID, which
	// is illegal here.
	throw new IllegalStateException() ;
    }

    // Return the name of the method used to extract the 
    // wrapped primitive value from the wrapper.  Here 
    // we use only those forms that return the same
    // type as was initially wrapped: that is, we do
    // not call (for example) longValue on an Integer.
    private String wrapperValueMethod( BasicType type )
    {
	if (type.equals( Type.BOOLEAN ))
	    return "booleanValue" ;
	if (type.equals( Type.BYTE ))
	    return "byteValue" ;
	if (type.equals( Type.CHAR ))
	    return "charValue" ;
	if (type.equals( Type.SHORT ))
	    return "shortValue" ;
	if (type.equals( Type.INT ))
	    return "intValue" ;
	if (type.equals( Type.LONG ))
	    return "longValue" ;
	if (type.equals( Type.FLOAT ))
	    return "floatValue" ;
	if (type.equals( Type.DOUBLE ))
	    return "doubleValue" ;

	// Only other BasicType is VOID, which
	// is illegal here.
	throw new IllegalStateException() ;
    }

    String[] makeArgNames( int num )
    {
	String[] result = new String[num] ;
	for (int ctr=0; ctr<num; ctr++ ) {
	    result[ctr] = "arg" + ctr ;
	}
	return result ;
    }

    // Some important notes about the code generation:
    // 1.  Local 0 is "this".
    // 2.  Locals 1-n hold the arguments, where n is the total
    //     size of all arguments.
    // 3.  The size of an argument is 1 word, unless it is double or long,
    //     in which case its size is 2 words.
    // 4.  Local n+1 is the first available slot for local variables.
    // 5.  Do not reuse local slots for different types: the verifier
    //     won't like it!
    // 6.  Stack maps are useful to understand what's going on here
    //     when the instruction is executed:
    //     <old contents> => <new contents>.  I'll use the symbol #
    //     to indicate a part of the stack that stays the same.
    //     empty means that the stack frame no longer exists after the call.
    //	   [ ] indicates an optional item.  Note that this is far
    //     from complete: in particular, I'm ignoring possible exceptions
    //     that an instruction could throw.
    // 7.  PUSH simply pushes a constant onto the stack.
    //     # => # constant
    // 8.  createNewArray does what is says.
    //     # count => # arrayref
    // 9.  createLoad creates a typed load instruction for a local.
    //     # => # value
    // 10. createStore creates a typed store instruction for a local.
    //	   # value => #
    // 11. createInvoke creates the appropriate invocation instruction.
    //     # object argList => # [return] (if the method returns a value)
    // 12. createReturn creates the appropriate return instruction.
    //	   # value => empty
    // 13. DUP duplicates the top of the stack:
    //	   # T => # T T
    // 14. AASTORE stores a value into an array:
    //	   # arrayref index value => #
    // 15. AALOAD fetches a value from an array:
    //     # arrayref index => # value
    // 16. createCheckCast verifies that an object ref is compatible with
    //     a type:
    //     # objref => # objref
    // 17. I'll write ... for the last stack state.
    //
    // Note that index is the index of method in the method array.
    // The same array must be used to generate this proxy
    // and also to dispatch requests in the invoke method.
    private void createMethod( int index, Method method )
    {
	String methodName = method.getName() ;
	String signature = Type.getSignature( method ) ;
	Type[] argTypes = Type.getArgumentTypes( signature ) ;
	int arglen = argTypes.length ;
	Type resultType = Type.getReturnType( signature ) ;

	// Create the instruction list and the new method.
	InstructionList il = new InstructionList();
	MethodGen mgen = new MethodGen( ACC_PUBLIC, resultType, argTypes,
	    makeArgNames(arglen), methodName, className, il, constantPoolGen ) ;

	if (arglen > 0) { // Stack is empty
	    // Allocate array to hold args
	    il.append( new PUSH( constantPoolGen, arglen ) ) ;				// empty => arglen
	    il.append( instructionFactory.createNewArray( Type.OBJECT, (short) 1 ) ) ;	// ... => arr 

	    int argOffset = 1 ;   // current offset for the argument indexed
				  // by ctr in the for loop.  gets incremented
				  // by the size of the argument (1 or 2).
				  // Starts at 1 because this object is in
				  // local 0.
	    for (int ctr=0; ctr<arglen; ctr++) {
		Type argType = argTypes[ctr] ;
		il.append( InstructionConstants.DUP ) ;		// ... => arr arr
		il.append( new PUSH( constantPoolGen, ctr ) ) ;	// ... => arr arr index

		// Emit wrapper if needed
		if (argType instanceof BasicType) {
		    il.append( instructionFactory.createNew( 
		        wrapperName( (BasicType)argType )) ) ;	// ... => arr arr index wrap
		    il.append( InstructionConstants.DUP ) ;	// ... => arr arr index wrap wrap
		    il.append( instructionFactory.createLoad(argType, 
			argOffset ) ) ;			        // ... => arr arr index wrap wrap arg 
		    // Invoke constructor of wrapper object
		    il.append( instructionFactory.createInvoke( 
			wrapperName( (BasicType)argType ), 
			CodeGeneratorUtil.CONSTRUCTOR_METHOD_NAME, 
			Type.VOID, new Type[] { argType }, 
			Constants.INVOKESPECIAL ) ) ;		// ... => arr arr index wrap
		} else {
		    // load ctr'th arg
		    il.append( instructionFactory.createLoad( argType, 
			argOffset ) ) ;				// ... => arr arr index arg
	        } 

		il.append( InstructionConstants.AASTORE ) ;	// ... => arr

		argOffset += typeLength( argType ) ;
	    }

	    // Store the arg array into a local
	    il.append( instructionFactory.createStore( Type.OBJECT, argOffset ) ) ;
	    // ... => empty

	    // Set up the invoke call.  Note that the index is the
	    // method number (the index of the method in the method array).
	    il.append( instructionFactory.createLoad(Type.OBJECT, 0));		    // ... => this
	    il.append( new PUSH( constantPoolGen, index ) ) ;			    // ... => this index
	    il.append( instructionFactory.createLoad( Type.OBJECT, argOffset ) ) ;  // ... => this index arr
	} else {
	    // push a null
	    il.append( instructionFactory.createLoad(Type.OBJECT, 0));		    // empty => this
	    il.append( new PUSH( constantPoolGen, index ) ) ;			    // ... => this index
	    il.append(InstructionConstants.ACONST_NULL);			    // ... => this index null
	}

	// Set up the invocation
	il.append( instructionFactory.createInvoke( className, INVOKE_METHOD_NAME, Type.OBJECT, 
	    new Type[] { Type.INT, new ArrayType(Type.OBJECT, 1) }, Constants.INVOKEVIRTUAL));
	    // ... => [result]
	
	// If non-void return
	if (!resultType.equals( Type.VOID ) ) {
	    // Cast invoke to resultType
	    if (resultType instanceof ReferenceType)
		il.append( instructionFactory.createCheckCast( 
		    (ReferenceType)resultType ) ) ;				    // ... => [result]
	    else if (resultType instanceof BasicType) {
		BasicType basicResultType = (BasicType)resultType ;
		ReferenceType refType = typeForWrapper( basicResultType ) ;
		il.append( instructionFactory.createCheckCast( refType ) ) ;	    // ... => [result]

		// extract value from wrapper 
		il.append( instructionFactory.createInvoke(
		    wrapperName( basicResultType ),  
		    wrapperValueMethod( basicResultType ), 
		    resultType, Type.NO_ARGS, Constants.INVOKEVIRTUAL));
	    } else
		throw new IllegalStateException() ;
	}
	    
	// Return the result type
	il.append( instructionFactory.createReturn( resultType ) ) ;

	finalizeMethod( classGen, il, mgen ) ;
    }
}
