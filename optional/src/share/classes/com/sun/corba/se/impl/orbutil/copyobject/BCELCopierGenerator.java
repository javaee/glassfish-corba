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

package com.sun.corba.se.impl.orbutil.copyobject ;

import org.apache.bcel.generic.*;
import org.apache.bcel.classfile.*;
import org.apache.bcel.*;

import java.io.IOException ;
import java.io.ByteArrayOutputStream ;

import java.lang.reflect.Modifier ;
import java.lang.reflect.Field ;

import java.security.AccessController ;
import java.security.PrivilegedAction ;
import java.security.ProtectionDomain ;

import java.util.Map ;

import sun.corba.Bridge ;

import com.sun.corba.se.impl.codegen.CodeGeneratorUtil ;

public class BCELCopierGenerator implements Constants {
    private Class	       classToCopy ;
    private String	       className ;
    private InstructionFactory instructionFactory;
    private ConstantPoolGen    constantPoolGen;
    private ClassGen           classGen;

    private static final Bridge bridge = 
	AccessController.doPrivileged(
	    new PrivilegedAction<Bridge>() {
		public Bridge run() {
		    return Bridge.get() ;
		}
	    } 
	) ;

    private static final String BASE_CLASS_NAME = 
	BCELCopierBase.class.getName() ;

    private static final ObjectType FACTORY_CLASS_OBJECT_TYPE =
	new ObjectType( PipelineClassCopierFactory.class.getName() ) ;

    private static final String CLASS_FIELD_COPIER_CLASS_NAME =
	ClassCopierOrdinaryImpl.ClassFieldCopier.class.getName() ;
    private static final ObjectType CLASS_FIELD_COPIER_OBJECT_TYPE = 
	new ObjectType( CLASS_FIELD_COPIER_CLASS_NAME ) ;

    private static final ObjectType MAP_OBJECT_TYPE =
	new ObjectType( Map.class.getName() ) ;

    private static final String SUPERCOPIER_FIELD_NAME = "superCopier" ;

    private static final Type[] COPY_PRIMITIVE_ARG_TYPES =
	new Type[] { Type.LONG, Type.OBJECT, Type.OBJECT } ;
    private static final Type[] COPY_OBJECT_ARG_TYPES =
	new Type[] { MAP_OBJECT_TYPE, Type.LONG, Type.OBJECT, 
		Type.OBJECT } ;

    private String getFileName( String className ) 
    {
	int last = className.lastIndexOf( '.' ) ;
	return className.substring( last + 1 ) + ".java" ;
    }

    public BCELCopierGenerator( String className, Class classToCopy ) 
    {
	this.className = className ;
	this.classToCopy = classToCopy ;

	classGen = new ClassGen( className,
	    BASE_CLASS_NAME,
	    getFileName( className ), 
	    ACC_PUBLIC | ACC_SUPER, 
	    new String[] { CLASS_FIELD_COPIER_CLASS_NAME } ) ;

	constantPoolGen = classGen.getConstantPool();
	instructionFactory = new InstructionFactory(classGen, constantPoolGen);
    }

    public Class create( ProtectionDomain pd, ClassLoader cl ) 
    {
	createFields();
	createConstructor();
	createCopyMethod();

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
	byte[] classData = out.toByteArray() ;
	return CodeGeneratorUtil.makeClass( className, classData, pd, cl ) ;
	
    }

    private void createFields() 
    {
	FieldGen field;

	field = new FieldGen( ACC_PRIVATE, CLASS_FIELD_COPIER_OBJECT_TYPE,
	    SUPERCOPIER_FIELD_NAME, constantPoolGen);
	classGen.addField(field.getField());
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
	MethodGen method = new MethodGen( ACC_PUBLIC, Type.VOID, 
	    new Type[] { FACTORY_CLASS_OBJECT_TYPE,
		CLASS_FIELD_COPIER_OBJECT_TYPE },
	    new String[] { "arg0", "arg1" }, 
	    CodeGeneratorUtil.CONSTRUCTOR_METHOD_NAME, className,
	    il, constantPoolGen);

	il.append( instructionFactory.createLoad(Type.OBJECT, 0));
	il.append(instructionFactory.createLoad(Type.OBJECT, 1));
	il.append(instructionFactory.createInvoke( BASE_CLASS_NAME, 
	    CodeGeneratorUtil.CONSTRUCTOR_METHOD_NAME, Type.VOID, 
	    new Type[] { FACTORY_CLASS_OBJECT_TYPE },
	    Constants.INVOKESPECIAL));
	il.append( instructionFactory.createLoad(Type.OBJECT, 0));
	il.append(instructionFactory.createLoad(Type.OBJECT, 2));
	il.append(instructionFactory.createFieldAccess( className,
	    SUPERCOPIER_FIELD_NAME, CLASS_FIELD_COPIER_OBJECT_TYPE, 
	    Constants.PUTFIELD));
	il.append( instructionFactory.createReturn(Type.VOID));

	finalizeMethod( classGen, il, method ) ;
    }
    
    private String getCopyMethodName( Class fieldType ) 
    { 
	if (fieldType.equals( Boolean.TYPE ))
	    return "copyBoolean" ;
        else if (fieldType.equals( Byte.TYPE ))
	    return "copyByte" ;
        else if (fieldType.equals( Character.TYPE ))
	    return "copyChar" ;
        else if (fieldType.equals( Integer.TYPE ))
	    return "copyInt" ;
        else if (fieldType.equals( Short.TYPE ))
	    return "copyShort" ;
        else if (fieldType.equals( Long.TYPE ))
	    return "copyLong" ;
        else if (fieldType.equals( Float.TYPE ))
	    return "copyFloat" ;
        else if (fieldType.equals( Double.TYPE ))
	    return "copyDouble" ;
	else 
	    return "copyObject" ;
    }
    
    private InstructionHandle makeFieldCopier( InstructionList il, 
	long offset, Class fieldClass )
    {
	boolean isPrim = fieldClass.isPrimitive() ;
	Type[] callTypes = isPrim ? COPY_PRIMITIVE_ARG_TYPES : 
	    COPY_OBJECT_ARG_TYPES ;

	InstructionHandle ih = il.append(
	    instructionFactory.createLoad(Type.OBJECT, 0));

	if (!fieldClass.isPrimitive()) {
	    il.append(instructionFactory.createLoad(Type.OBJECT, 1));
	}

	il.append(new PUSH(constantPoolGen, offset));
	il.append(instructionFactory.createLoad(Type.OBJECT, 2));
	il.append(instructionFactory.createLoad(Type.OBJECT, 3));
	il.append(instructionFactory.createInvoke( className, 
	    getCopyMethodName( fieldClass ), Type.VOID, 
	    callTypes, Constants.INVOKEVIRTUAL));

	return ih ;
    }

    private void createCopyMethod() {
	InstructionList il = new InstructionList();
	MethodGen method = new MethodGen(
	    ACC_PUBLIC, Type.VOID, 
	    new Type[] { MAP_OBJECT_TYPE, Type.OBJECT, 
		Type.OBJECT, Type.BOOLEAN }, 
	    new String[] { "arg0", "arg1", "arg2", "arg3" }, 
	    "copy", className, il, constantPoolGen);

	// First handle the superCopier call
	
	// Skip the call if superCopier==null
	il.append( instructionFactory.createLoad(Type.OBJECT, 0));
	il.append(instructionFactory.createFieldAccess(
	    className, SUPERCOPIER_FIELD_NAME, CLASS_FIELD_COPIER_OBJECT_TYPE,
	    Constants.GETFIELD));
        BranchInstruction ifnull = instructionFactory.createBranchInstruction(
	    Constants.IFNULL, null);
	il.append(ifnull);

	// Call superCopier.copy
	il.append(instructionFactory.createLoad(
	    Type.OBJECT, 0));
	il.append(instructionFactory.createFieldAccess(
	    className, SUPERCOPIER_FIELD_NAME, CLASS_FIELD_COPIER_OBJECT_TYPE,
	    Constants.GETFIELD));
	il.append(instructionFactory.createLoad(Type.OBJECT, 1));
	il.append(instructionFactory.createLoad(Type.OBJECT, 2));
	il.append(instructionFactory.createLoad(Type.OBJECT, 3));
	il.append(instructionFactory.createLoad(Type.BOOLEAN, 4));
	il.append(instructionFactory.createInvoke(
	    CLASS_FIELD_COPIER_CLASS_NAME, "copy", Type.VOID, 
	    new Type[] { MAP_OBJECT_TYPE, Type.OBJECT, 
		Type.OBJECT, Type.BOOLEAN }, 
	    Constants.INVOKEINTERFACE));

	// Generate code to copy fields of this object
	Field[] classFields = classToCopy.getDeclaredFields() ;
	int numFieldsToCopy = 0 ;
	for ( int ctr=0; ctr<classFields.length; ctr++ ) {
	    Field fld = classFields[ctr] ;
	    if (!Modifier.isStatic( fld.getModifiers())) {
		long offset = bridge.objectFieldOffset( fld ) ;
		InstructionHandle ih = makeFieldCopier( il, offset, fld.getType() ) ; 
		if (numFieldsToCopy == 0) {
		    // Make the superCopier null check branch to the first field copier.
		    ifnull.setTarget(ih) ;
		}

		numFieldsToCopy++ ;
	    }
	}

	InstructionHandle ih = il.append(instructionFactory.createReturn(Type.VOID));

	// If there are no fields that need copying, set the branch target
	// to the return statement.
	if (numFieldsToCopy == 0)
	    ifnull.setTarget(ih) ;

	finalizeMethod( classGen, il, method ) ;
    }
}
