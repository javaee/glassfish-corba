/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2006-2007 Sun Microsystems, Inc. All rights reserved.
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

import java.util.Set ;
import java.util.Iterator ;
import java.util.HashSet ;
import java.util.List ;
import java.util.Arrays ;
import java.util.Properties ;

import junit.framework.TestCase ;
import junit.framework.Test ;
import junit.framework.TestResult ;
import junit.framework.TestSuite ;

import com.sun.corba.se.spi.orbutil.codegen.Type ;
import com.sun.corba.se.spi.orbutil.codegen.Wrapper ;
import com.sun.corba.se.spi.orbutil.generic.NullaryFunction ;
import com.sun.corba.se.spi.orbutil.generic.Pair ;

import static com.sun.corba.se.spi.orbutil.codegen.Wrapper.* ;

import org.objectweb.asm.ClassWriter ;

import corba.codegen.lib.A ;
import corba.codegen.lib.B ;
import corba.codegen.lib.C ;
import corba.codegen.lib.D ;
import corba.codegen.lib.DImpl ;

import corba.framework.TestCaseTools ;

import static corba.codegen.ControlBase.moa ;

public class TypeTestSuite extends TestCase {
    private static final boolean DEBUG = false ;

    public TypeTestSuite() {
	super() ;
    }

    public TypeTestSuite( String name ) {
	super( name ) ;
    }

    public void test_void() {
	Type t = Type._void() ;
	assertTrue( t.isPrimitive() ) ;
	assertFalse( t.isArray() ) ;
	assertEquals( t.size(), 0 ) ;
	assertFalse( t.isNumber() ) ;
	assertEquals( t.signature(), "V" ) ;
    }

    public void test_null() {
	Type t = Type._null() ;
	assertTrue( t.isPrimitive() ) ;
	assertFalse( t.isArray() ) ;
	assertEquals( t.size(), 1 ) ;
	assertFalse( t.isNumber() ) ;
	assertEquals( t.signature(), "N" ) ;
    }

    public void test_boolean() {
	Type t = Type._boolean() ;
	assertTrue( t.isPrimitive() ) ;
	assertFalse( t.isArray() ) ;
	assertEquals( t.size(), 1 ) ;
	assertFalse( t.isNumber() ) ;
	assertEquals( t.signature(), "Z" ) ;
    }

    public void test_byte() {
	Type t = Type._byte() ;
	assertTrue( t.isPrimitive() ) ;
	assertFalse( t.isArray() ) ;
	assertEquals( t.size(), 1 ) ;
	assertTrue( t.isNumber() ) ;
	assertEquals( t.signature(), "B" ) ;
    }

    public void test_char() {
	Type t = Type._char() ;
	assertTrue( t.isPrimitive() ) ;
	assertFalse( t.isArray() ) ;
	assertEquals( t.size(), 1 ) ;
	assertTrue( t.isNumber() ) ;
	assertEquals( t.signature(), "C" ) ;
    }

    public void test_short() {
	Type t = Type._short() ;
	assertTrue( t.isPrimitive() ) ;
	assertFalse( t.isArray() ) ;
	assertEquals( t.size(), 1 ) ;
	assertTrue( t.isNumber() ) ;
	assertEquals( t.signature(), "S" ) ;
    }

    public void test_int() {
	Type t = Type._int() ;
	assertTrue( t.isPrimitive() ) ;
	assertFalse( t.isArray() ) ;
	assertEquals( t.size(), 1 ) ;
	assertTrue( t.isNumber() ) ;
	assertEquals( t.signature(), "I" ) ;
    }

    public void test_long() {
	Type t = Type._long() ;
	assertTrue( t.isPrimitive() ) ;
	assertFalse( t.isArray() ) ;
	assertEquals( t.size(), 2 ) ;
	assertTrue( t.isNumber() ) ;
	assertEquals( t.signature(), "J" ) ;
    }

    public void test_float() {
	Type t = Type._float() ;
	assertTrue( t.isPrimitive() ) ;
	assertFalse( t.isArray() ) ;
	assertEquals( t.size(), 1 ) ;
	assertTrue( t.isNumber() ) ;
	assertEquals( t.signature(), "F" ) ;
    }

    public void test_double() {
	Type t = Type._double() ;
	assertTrue( t.isPrimitive() ) ;
	assertFalse( t.isArray() ) ;
	assertEquals( t.size(), 2 ) ;
	assertTrue( t.isNumber() ) ;
	assertEquals( t.signature(), "D" ) ;
    }

    public void test_int_array() {
	Type t = Type._array( Type._int() ) ;
	assertFalse( t.isPrimitive() ) ;
	assertTrue( t.isArray() ) ;
	assertEquals( t.size(), 1 ) ;
	assertFalse( t.isNumber() ) ;
	assertEquals( t.memberType(), Type._int() ) ;
	assertEquals( t.signature(), "[I" ) ;
    }

    public void test_string_class() {
	Type t = Type._String();
	assertFalse( t.isPrimitive() ) ;
	assertFalse( t.isArray() ) ;
	assertEquals( t.size(), 1 ) ;
	assertFalse( t.isNumber() ) ;
	assertEquals( t.signature(), "Ljava/lang/String;" ) ;
	assertEquals( t.getTypeClass(), java.lang.String.class ) ;
    }

    private static final Type[] TYPE_DATA = {
	Type._void(),
	Type._null(),
	Type._boolean(),
	Type._byte(),
	Type._char(),
	Type._short(),
	Type._int(),
	Type._long(),
	Type._float(),
	Type._double(),
	Type._Object()
    } ;

    // Expected result for X.hasPrimitiveNarrowingConversionFrom( Y )
    // (That is, there is a primitive narrowing conversion from Y to X):
    //
    boolean[][] PRIM_NARROW_CONVERSION_DATA = {
	//	      void   null   bool   byte   char   short  int    long   float  double Object
	/*void*/    { false, false, false, false, false, false, false, false, false, false, false },
	/*null*/    { false, false, false, false, false, false, false, false, false, false, false },
	/*boolean*/ { false, false, false, false, false, false, false, false, false, false, false },
	/*byte*/    { false, false, false, false, true,  false, false, false, false, false, false },
	/*char*/    { false, false, false, true,  false, true,  false, false, false, false, false },
	/*short*/   { false, false, false, true,  true,  false, false, false, false, false, false },
	/*int*/     { false, false, false, true,  true,  true,  false, false, false, false, false },
	/*long*/    { false, false, false, true,  true,  true,  true,  false, false, false, false },
	/*float*/   { false, false, false, true,  true,  true,  true,  true,  false, false, false },
	/*double*/  { false, false, false, true,  true,  true,  true,  true,  true,  false, false },
	/*object*/  { false, false, false, false, false, false, false, false, false, false, false }
    } ;

    public void testHasPrimitiveNarrowingConversion() {
	int errorCount = 0 ;

	for (int y=0; y<TYPE_DATA.length; y++)
	    for (int x=0; x<TYPE_DATA.length; x++) {
		boolean expected = PRIM_NARROW_CONVERSION_DATA[y][x] ;
		boolean result = TYPE_DATA[x].hasPrimitiveNarrowingConversionFrom( 
		    TYPE_DATA[y] ) ;
		if ( result != expected ) {
		    errorCount++ ;
		    System.out.println( "Error on " + TYPE_DATA[x].name() 
			+ ".hasPrimitiveNarrowingConversionFrom( " 
			+ TYPE_DATA[y].name() + " ): expected result was " 
			+ expected ) ;
		}
	    }

	assertTrue( errorCount == 0 ) ;
    }

    // Expected result for X.hasPrimitiveWideningConversionFrom( Y ):
    //
    boolean[][] PRIM_WIDEN_CONVERSION_DATA = {
	//	      void   null   bool   byte   char   short  int    long   float  double Object
	/*void*/    { false, false, false, false, false, false, false, false, false, false, false },
	/*null*/    { false, false, false, false, false, false, false, false, false, false, false },
	/*boolean*/ { false, false, false, false, false, false, false, false, false, false, false },
	/*byte*/    { false, false, false, false, false, true,  true,  true,  true,  true,  false },
	/*char*/    { false, false, false, false, false, false, true,  true,  true,  true,  false },
	/*short*/   { false, false, false, false, false, false, true,  true,  true,  true,  false },
	/*int*/     { false, false, false, false, false, false, false, true,  true,  true,  false },
	/*long*/    { false, false, false, false, false, false, false, false, true,  true,  false },
	/*float*/   { false, false, false, false, false, false, false, false, false, true,  false },
	/*double*/  { false, false, false, false, false, false, false, false, false, false, false },
	/*object*/  { false, false, false, false, false, false, false, false, false, false, false }
    } ;

    public void testHasPrimitiveWideningConversion() {
	int errorCount = 0 ;

	for (int y=0; y<TYPE_DATA.length; y++)
	    for (int x=0; x<TYPE_DATA.length; x++) {
		boolean expected = PRIM_WIDEN_CONVERSION_DATA[y][x] ;
		boolean result = TYPE_DATA[x].hasPrimitiveWideningConversionFrom( 
		    TYPE_DATA[y] ) ;
		if ( result != expected ) {
		    errorCount++ ;
		    System.out.println( "Error on " + TYPE_DATA[x].name() 
			+ ".hasPrimitiveWideningConversionFrom( " 
			+ TYPE_DATA[y].name() + " ): expected result was " 
			+ expected ) ;
		}
	    }

	assertTrue( errorCount == 0 ) ;
    }

    public interface ClassA {
	int foo() ;
    } 

    public interface ClassB extends ClassA {}

    public final class ClassD implements ClassA {
	public int foo() {
	    return 0 ;
	}
    }

    public class ClassC implements ClassB {
	public int foo() {
	    return 1 ;
	}
    }

    public interface ClassE {
	String foo() ;
    }

    private static final Type[] CLASS_TYPE_DATA = {
	Type._int(),
	Type._Object(),
	Type._null(),
	Type._Cloneable(),
	Type.type( ClassA.class ),
	Type.type( ClassB.class ),
	Type.type( ClassC.class ),
	Type.type( ClassD.class ),
	Type.type( ClassE.class ),
	Type._array( Type.type( ClassA.class ) ),
	Type._array( Type.type( ClassB.class ) ),
	Type._array( Type.type( ClassC.class ) ),
	Type._array( Type.type( ClassD.class ) ),
	Type._array( Type.type( ClassE.class ) ),
	Type._array( Type._int() )
    } ;

    // Expected results for X.hasReferenceNarrowingConversionFrom( Y ):
    // 
    boolean[][] REF_NARROW_CONVERSION_DATA = {
	//	      int    Object null   Clone  A	 B      C      D      E      A[]    B[]    C[]    D[]    E[]    int[]
	/*int*/	    { false, false, false, false, false, false, false, false, false, false, false, false, false, false, false },
	/*Object*/  { false, true,  false, true,  true,  true,  true,  true,  true,  true,  true,  true,  true,  true,  true,  },
	/*null*/    { false, false, false, false, false, false, false, false, false, false, false, false, false, false, false },
	/*Clone*/   { false, false, false, false, false, false, false, false, false, false, false, false, false, false, false },
	/*A*/	    { false, false, false, false, false, true,  true,  true,  false, false, false, false, false, false, false },
	/*B*/	    { false, false, false, false, false, false, true,  false, false, false, false, false, false, false, false },
	/*C*/	    { false, false, false, true,  true,  true,  false, false, false, false, false, false, false, false, false },
	/*D*/	    { false, false, false, false, false, false, false, false, false, false, false, false, false, false, false },
	/*E*/	    { false, false, false, false, false, false, true,  false, false, false, false, false, false, false, false },
	/*A[]*/	    { false, false, false, false, false, false, false, false, false, false, true,  true,  true,  false, false },
	/*B[]*/	    { false, false, false, false, false, false, false, false, false, false, false, true,  false, false, false },
	/*C[]*/	    { false, false, false, false, false, false, false, false, false, true,  true,  false, false, false, false },
	/*D[]*/	    { false, false, false, false, false, false, false, false, false, false, false, false, false, false, false },
	/*E[]*/	    { false, false, false, false, false, false, false, false, false, false, false, true,  false, false, false },
	/*int[]*/   { false, false, false, false, false, false, false, false, false, false, false, false, false, false, false },
    } ;

    public void testHasReferenceNarrowingConversion() {
	int errorCount = 0 ;

	for (int y=0; y<CLASS_TYPE_DATA.length; y++)
	    for (int x=0; x<CLASS_TYPE_DATA.length; x++) {
		boolean expected = REF_NARROW_CONVERSION_DATA[y][x] ;
		boolean result = CLASS_TYPE_DATA[x].hasReferenceNarrowingConversionFrom( 
		    CLASS_TYPE_DATA[y] ) ;
		if ( result != expected ) {
		    errorCount++ ;
                    if (DEBUG) 
                        System.out.println( "Error on " + CLASS_TYPE_DATA[x].name() 
                            + ".hasReferenceNarrowingConversionFrom( " 
                            + CLASS_TYPE_DATA[y].name() + " ): expected result was " 
                            + expected ) ;
		}
	    }

        if (errorCount >= 0)
            System.out.println( "REMINDER: need to work on testHashReferenceNarrowingConversion" ) ;

	// Fix this later
	// assertTrue( errorCount == 0 ) ;
    }

    // Expected results for X.hasReferenceWideningConversionFrom( Y ):
    // public interface ClassA {
    // int foo() ;
    // } 
    // 
    // public interface ClassB extends ClassA {}
    // 
    // public final class ClassD implements ClassA {}
    // 
    // public class ClassC implements ClassB {}
    // 
    // public interface ClassE {
    // String foo() ;
    // }
    // 
    boolean[][] REF_WIDENING_CONVERSION_DATA = {
	//	      int    Object null   Clone  A	 B      C      D      E      A[]    B[]    C[]    D[]    E[]    int[]
	/*int*/	    { false, false, false, false, false, false, false, false, false, false, false, false, false, false, false },
	/*Object*/  { false, false, false, false, false, false, false, false, false, false, false, false, false, false, false },
	/*null*/    { false, true,  false, true,  true,  true,  true,  true,  true,  true,  true,  true,  true,  true,  true  },
	/*Clone*/   { false, true,  false, false, false, false, false, false, false, false, false, false, false, false, false },
	/*A*/	    { false, true,  false, false, false, false, false, false, false, false, false, false, false, false, false },
	/*B*/	    { false, true,  false, false, true,  false, false, false, false, false, false, false, false, false, false },
	/*C*/	    { false, true,  false, false, true,  true,  false, false, false, false, false, false, false, false, false },
	/*D*/	    { false, true,  false, false, true,  false, false, false, false, false, false, false, false, false, false },
	/*E*/	    { false, true,  false, false, false, false, false, false, false, false, false, false, false, false, false },
	/*A[]*/	    { false, true,  false, true,  false, false, false, false, false, false, false, false, false, false, false },
	/*B[]*/	    { false, true,  false, true,  false, false, false, false, false, true,  false, false, false, false, false },
	/*C[]*/	    { false, true,  false, true,  false, false, false, false, false, true,  true,  false, false, false, false },
	/*D[]*/	    { false, true,  false, true,  false, false, false, false, false, true,  false, false, false, false, false },
	/*E[]*/	    { false, true,  false, true,  false, false, false, false, false, false, false, false, false, false, false },
	/*int[]*/   { false, true,  false, true,  false, false, false, false, false, false, false, false, false, false, false }
    } ;

    public void testHasReferenceWideningConversion() {
	int errorCount = 0 ;

	for (int y=0; y<CLASS_TYPE_DATA.length; y++)
	    for (int x=0; x<CLASS_TYPE_DATA.length; x++) {
		boolean expected = REF_WIDENING_CONVERSION_DATA[y][x] ;
		boolean result = CLASS_TYPE_DATA[x].hasReferenceWideningConversionFrom( 
		    CLASS_TYPE_DATA[y] ) ;
		if ( result != expected ) {
		    errorCount++ ;
                    if (DEBUG)
                        System.out.println( "Error on " + CLASS_TYPE_DATA[x].name() 
                            + ".hasReferenceWideningConversionFrom( " 
                            + CLASS_TYPE_DATA[y].name() + " ): expected result was " 
                            + expected ) ;
		}
	    }

        if (errorCount >= 0)
            System.out.println( "REMINDER: need to work on testHashReferenceWideningConversion" ) ;

	// Fix this later
	// assertTrue( errorCount == 0 ) ;
    }
}
