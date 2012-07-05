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

package corba.fastencoding  ;

import java.lang.reflect.Method ;
import java.lang.reflect.InvocationTargetException ;

import org.testng.Assert ;
import org.testng.annotations.Test ;

import com.sun.corba.ee.impl.fast.EmergeCode ;
import com.sun.corba.ee.impl.fast.EmergeCodeFactory ;

public class EmergeCodeTest {
    // Short constants that are used a lot
    private static final Boolean TRUE = Boolean.TRUE ;
    private static final Boolean FALSE = Boolean.FALSE ;
    private static final Exception IAE = new IllegalArgumentException() ;
    private static final Object[] ILLEGAL_CODE_DATA = 
        //  getType,    getKind,    Value,  hasEncodedValue,    isValidEmergeCode,  isPrimitive,    isArray,    isSimpleMessage 
        {   IAE,        IAE,        IAE,    IAE,                FALSE,              IAE,            IAE,        IAE } ;

    // Methods corresponding to inner array of values in CODE_INFORMATION
    // EmergeCode also defines code() and byteCode(), but these are checked directly.
    private static Method[] EMERGE_CODE_METHODS = null ;
    private static Method GET_VALUE_METHOD = null ;

    static {
        try {
            GET_VALUE_METHOD =
                EmergeCode.class.getDeclaredMethod( "getValue", Class.class ) ;

            EMERGE_CODE_METHODS = new Method[] {
                EmergeCode.class.getDeclaredMethod( "getType" ),
                EmergeCode.class.getDeclaredMethod( "getKind" ),
                GET_VALUE_METHOD,
                EmergeCode.class.getDeclaredMethod( "hasEncodedValue" ),
                EmergeCode.class.getDeclaredMethod( "isValidEmergeCode" ),
                EmergeCode.class.getDeclaredMethod( "isPrimitive" ),
                EmergeCode.class.getDeclaredMethod( "isArray" ),
                EmergeCode.class.getDeclaredMethod( "isSimpleMessage" )
            } ;
        } catch (Exception exc) {
            exc.printStackTrace() ;
            Assert.fail( "Unexpected exception " + exc + " while initializing EMERGE_CODE_METHODS" ) ;
        }
    }

    private static Object[][] CODE_INFORMATION = new Object[][] {
        // 00-07
        // getType,                                 getKind                         Value                   hasEncodedValue     isValidEmergeCode   isPrimitive     isArray         isSimpleMessage 
        {  EmergeCode.EmergeType.OPTIONAL_VALUE,    EmergeCode.EmergeKind.BYTE,     Byte.valueOf( (byte)-1 ), TRUE,             TRUE,               TRUE,           FALSE,          FALSE },
        {  EmergeCode.EmergeType.OPTIONAL_VALUE,    EmergeCode.EmergeKind.BYTE,     Byte.valueOf( (byte) 0 ), TRUE,             TRUE,               TRUE,           FALSE,          FALSE },
        {  EmergeCode.EmergeType.OPTIONAL_VALUE,    EmergeCode.EmergeKind.BYTE,     Byte.valueOf( (byte) 1 ), TRUE,             TRUE,               TRUE,           FALSE,          FALSE },
        {  EmergeCode.EmergeType.OPTIONAL_VALUE,    EmergeCode.EmergeKind.BYTE,     Byte.valueOf( (byte) 2 ), TRUE,             TRUE,               TRUE,           FALSE,          FALSE },
        {  EmergeCode.EmergeType.OPTIONAL_VALUE,    EmergeCode.EmergeKind.BYTE,     Byte.valueOf( (byte) 3 ), TRUE,             TRUE,               TRUE,           FALSE,          FALSE },
        {  EmergeCode.EmergeType.OPTIONAL_VALUE,    EmergeCode.EmergeKind.BYTE,     Byte.valueOf( (byte) 4 ), TRUE,             TRUE,               TRUE,           FALSE,          FALSE },
        {  EmergeCode.EmergeType.OPTIONAL_VALUE,    EmergeCode.EmergeKind.BYTE,     Byte.valueOf( (byte) 5 ), TRUE,             TRUE,               TRUE,           FALSE,          FALSE },
        {  EmergeCode.EmergeType.OPTIONAL_VALUE,    EmergeCode.EmergeKind.BYTE,     IAE,                      FALSE,            TRUE,               TRUE,           FALSE,          FALSE },

        // 08-0F
        // getType,                                 getKind                         Value                   hasEncodedValue     isValidEmergeCode   isPrimitive     isArray         isSimpleMessage 
        ILLEGAL_CODE_DATA, ILLEGAL_CODE_DATA, ILLEGAL_CODE_DATA, ILLEGAL_CODE_DATA, ILLEGAL_CODE_DATA, ILLEGAL_CODE_DATA, ILLEGAL_CODE_DATA,
        {  EmergeCode.EmergeType.SIMPLE,            EmergeCode.EmergeKind.CHAR,     IAE,                    FALSE,              TRUE,               TRUE,           FALSE,          FALSE },

        // 10-17
        // getType,                                 getKind                         Value                   hasEncodedValue     isValidEmergeCode   isPrimitive     isArray         isSimpleMessage 
        {  EmergeCode.EmergeType.OPTIONAL_VALUE,    EmergeCode.EmergeKind.SHORT,    Short.valueOf( (short)-1 ), TRUE,           TRUE,               TRUE,           FALSE,          FALSE },
        {  EmergeCode.EmergeType.OPTIONAL_VALUE,    EmergeCode.EmergeKind.SHORT,    Short.valueOf( (short) 0 ), TRUE,           TRUE,               TRUE,           FALSE,          FALSE },
        {  EmergeCode.EmergeType.OPTIONAL_VALUE,    EmergeCode.EmergeKind.SHORT,    Short.valueOf( (short) 1 ), TRUE,           TRUE,               TRUE,           FALSE,          FALSE },
        {  EmergeCode.EmergeType.OPTIONAL_VALUE,    EmergeCode.EmergeKind.SHORT,    Short.valueOf( (short) 2 ), TRUE,           TRUE,               TRUE,           FALSE,          FALSE },
        {  EmergeCode.EmergeType.OPTIONAL_VALUE,    EmergeCode.EmergeKind.SHORT,    Short.valueOf( (short) 3 ), TRUE,           TRUE,               TRUE,           FALSE,          FALSE },
        {  EmergeCode.EmergeType.OPTIONAL_VALUE,    EmergeCode.EmergeKind.SHORT,    Short.valueOf( (short) 4 ), TRUE,           TRUE,               TRUE,           FALSE,          FALSE },
        {  EmergeCode.EmergeType.OPTIONAL_VALUE,    EmergeCode.EmergeKind.SHORT,    Short.valueOf( (short) 5 ), TRUE,           TRUE,               TRUE,           FALSE,          FALSE },
        {  EmergeCode.EmergeType.OPTIONAL_VALUE,    EmergeCode.EmergeKind.SHORT,    IAE,                    FALSE,              TRUE,               TRUE,           FALSE,          FALSE },
        
        // 18-1F
        // getType,                                 getKind                         Value                   hasEncodedValue     isValidEmergeCode   isPrimitive     isArray         isSimpleMessage 
        {  EmergeCode.EmergeType.OPTIONAL_VALUE,    EmergeCode.EmergeKind.INT,      Integer.valueOf( -1 ),  TRUE,               TRUE,               TRUE,           FALSE,          FALSE },
        {  EmergeCode.EmergeType.OPTIONAL_VALUE,    EmergeCode.EmergeKind.INT,      Integer.valueOf(  0 ),  TRUE,               TRUE,               TRUE,           FALSE,          FALSE },
        {  EmergeCode.EmergeType.OPTIONAL_VALUE,    EmergeCode.EmergeKind.INT,      Integer.valueOf(  1 ),  TRUE,               TRUE,               TRUE,           FALSE,          FALSE },
        {  EmergeCode.EmergeType.OPTIONAL_VALUE,    EmergeCode.EmergeKind.INT,      Integer.valueOf(  2 ),  TRUE,               TRUE,               TRUE,           FALSE,          FALSE },
        {  EmergeCode.EmergeType.OPTIONAL_VALUE,    EmergeCode.EmergeKind.INT,      Integer.valueOf(  3 ),  TRUE,               TRUE,               TRUE,           FALSE,          FALSE },
        {  EmergeCode.EmergeType.OPTIONAL_VALUE,    EmergeCode.EmergeKind.INT,      Integer.valueOf(  4 ),  TRUE,               TRUE,               TRUE,           FALSE,          FALSE },
        {  EmergeCode.EmergeType.OPTIONAL_VALUE,    EmergeCode.EmergeKind.INT,      Integer.valueOf(  5 ),  TRUE,               TRUE,               TRUE,           FALSE,          FALSE },
        {  EmergeCode.EmergeType.OPTIONAL_VALUE,    EmergeCode.EmergeKind.INT,      IAE,                    FALSE,              TRUE,               TRUE,           FALSE,          FALSE },

        // 20-27
        // getType,                                 getKind                         Value                   hasEncodedValue     isValidEmergeCode   isPrimitive     isArray         isSimpleMessage 
        {  EmergeCode.EmergeType.OPTIONAL_VALUE,    EmergeCode.EmergeKind.LONG,     Long.valueOf( -1 ),     TRUE,               TRUE,               TRUE,           FALSE,          FALSE },
        {  EmergeCode.EmergeType.OPTIONAL_VALUE,    EmergeCode.EmergeKind.LONG,     Long.valueOf(  0 ),     TRUE,               TRUE,               TRUE,           FALSE,          FALSE },
        {  EmergeCode.EmergeType.OPTIONAL_VALUE,    EmergeCode.EmergeKind.LONG,     Long.valueOf(  1 ),     TRUE,               TRUE,               TRUE,           FALSE,          FALSE },
        {  EmergeCode.EmergeType.OPTIONAL_VALUE,    EmergeCode.EmergeKind.LONG,     Long.valueOf(  2 ),     TRUE,               TRUE,               TRUE,           FALSE,          FALSE },
        {  EmergeCode.EmergeType.OPTIONAL_VALUE,    EmergeCode.EmergeKind.LONG,     Long.valueOf(  3 ),     TRUE,               TRUE,               TRUE,           FALSE,          FALSE },
        {  EmergeCode.EmergeType.OPTIONAL_VALUE,    EmergeCode.EmergeKind.LONG,     Long.valueOf(  4 ),     TRUE,               TRUE,               TRUE,           FALSE,          FALSE },
        {  EmergeCode.EmergeType.OPTIONAL_VALUE,    EmergeCode.EmergeKind.LONG,     Long.valueOf(  5 ),     TRUE,               TRUE,               TRUE,           FALSE,          FALSE },
        {  EmergeCode.EmergeType.OPTIONAL_VALUE,    EmergeCode.EmergeKind.LONG,     IAE,                    FALSE,              TRUE,               TRUE,           FALSE,          FALSE },

        // 28-2F 
        // getType,                                 getKind                         Value                   hasEncodedValue     isValidEmergeCode   isPrimitive     isArray         isSimpleMessage 
        {  EmergeCode.EmergeType.OPTIONAL_VALUE,    EmergeCode.EmergeKind.FLOAT,    Float.valueOf( (float)-1.0 ),  TRUE,        TRUE,               TRUE,           FALSE,          FALSE },
        {  EmergeCode.EmergeType.OPTIONAL_VALUE,    EmergeCode.EmergeKind.FLOAT,    Float.valueOf( (float) 0.0 ),  TRUE,        TRUE,               TRUE,           FALSE,          FALSE },
        {  EmergeCode.EmergeType.OPTIONAL_VALUE,    EmergeCode.EmergeKind.FLOAT,    Float.valueOf( (float) 1.0 ),  TRUE,        TRUE,               TRUE,           FALSE,          FALSE },
        ILLEGAL_CODE_DATA, ILLEGAL_CODE_DATA, ILLEGAL_CODE_DATA, ILLEGAL_CODE_DATA,
        {  EmergeCode.EmergeType.OPTIONAL_VALUE,    EmergeCode.EmergeKind.FLOAT,    IAE,                    FALSE,              TRUE,               TRUE,           FALSE,          FALSE },

        // 30-37 
        // getType,                                 getKind                         Value                   hasEncodedValue     isValidEmergeCode   isPrimitive     isArray         isSimpleMessage 
        {  EmergeCode.EmergeType.OPTIONAL_VALUE,    EmergeCode.EmergeKind.DOUBLE,   Double.valueOf( -1.0 ), TRUE,               TRUE,               TRUE,           FALSE,          FALSE },
        {  EmergeCode.EmergeType.OPTIONAL_VALUE,    EmergeCode.EmergeKind.DOUBLE,   Double.valueOf(  0.0 ), TRUE,               TRUE,               TRUE,           FALSE,          FALSE },
        {  EmergeCode.EmergeType.OPTIONAL_VALUE,    EmergeCode.EmergeKind.DOUBLE,   Double.valueOf(  1.0 ), TRUE,               TRUE,               TRUE,           FALSE,          FALSE },
        ILLEGAL_CODE_DATA, ILLEGAL_CODE_DATA, ILLEGAL_CODE_DATA, ILLEGAL_CODE_DATA,
        {  EmergeCode.EmergeType.OPTIONAL_VALUE,    EmergeCode.EmergeKind.DOUBLE,   IAE,                    FALSE,              TRUE,               TRUE,           FALSE,          FALSE },

        // 38-3F
        // getType,                                 getKind                         Value                   hasEncodedValue     isValidEmergeCode   isPrimitive     isArray         isSimpleMessage 
        ILLEGAL_CODE_DATA, ILLEGAL_CODE_DATA, ILLEGAL_CODE_DATA, ILLEGAL_CODE_DATA, ILLEGAL_CODE_DATA, ILLEGAL_CODE_DATA, ILLEGAL_CODE_DATA, ILLEGAL_CODE_DATA,

        // 40-47
        // getType,                                 getKind                         Value                   hasEncodedValue     isValidEmergeCode   isPrimitive     isArray         isSimpleMessage 
        {  EmergeCode.EmergeType.ENCODED,           EmergeCode.EmergeKind.BOOL,     FALSE,                  TRUE,               TRUE,               TRUE,           FALSE,          FALSE },
        {  EmergeCode.EmergeType.ENCODED,           EmergeCode.EmergeKind.BOOL,     TRUE,                   TRUE,               TRUE,               TRUE,           FALSE,          FALSE },
        ILLEGAL_CODE_DATA, ILLEGAL_CODE_DATA, ILLEGAL_CODE_DATA, ILLEGAL_CODE_DATA, ILLEGAL_CODE_DATA, ILLEGAL_CODE_DATA, 
        // 48-4F 
        // getType,                                 getKind                         Value                           hasEncodedValue     isValidEmergeCode   isPrimitive     isArray         isSimpleMessage 
        {  EmergeCode.EmergeType.ENCODED,           EmergeCode.EmergeKind.TUPLE,    EmergeCode.TupleCode.TUPLE_START, TRUE,             TRUE,               FALSE,           FALSE,          FALSE },
        {  EmergeCode.EmergeType.ENCODED,           EmergeCode.EmergeKind.TUPLE,    EmergeCode.TupleCode.TUPLE_END,   TRUE,             TRUE,               FALSE,           FALSE,          FALSE },
        ILLEGAL_CODE_DATA, ILLEGAL_CODE_DATA, ILLEGAL_CODE_DATA, ILLEGAL_CODE_DATA, ILLEGAL_CODE_DATA, ILLEGAL_CODE_DATA,
   
        // 50-57
        // getType,                                 getKind                         Value                           hasEncodedValue     isValidEmergeCode   isPrimitive     isArray         isSimpleMessage 
        {  EmergeCode.EmergeType.ENCODED,           EmergeCode.EmergeKind.PART,     EmergeCode.PartCode.NO_CUSTOM,  TRUE,               TRUE,               FALSE,           FALSE,          FALSE },
        {  EmergeCode.EmergeType.ENCODED,           EmergeCode.EmergeKind.PART,     EmergeCode.PartCode.HAS_CUSTOM, TRUE,               TRUE,               FALSE,           FALSE,          FALSE },
        ILLEGAL_CODE_DATA, ILLEGAL_CODE_DATA, ILLEGAL_CODE_DATA, ILLEGAL_CODE_DATA, ILLEGAL_CODE_DATA, ILLEGAL_CODE_DATA,

        // 58-5F
        // getType,                                 getKind                         Value                           hasEncodedValue     isValidEmergeCode   isPrimitive     isArray         isSimpleMessage 
        {  EmergeCode.EmergeType.ENCODED,           EmergeCode.EmergeKind.MSG,      EmergeCode.MsgCode.MSG_START,   TRUE,               TRUE,               FALSE,          FALSE,          FALSE },
        {  EmergeCode.EmergeType.ENCODED,           EmergeCode.EmergeKind.MSG,      EmergeCode.MsgCode.MSG_END,     TRUE,              TRUE,               FALSE,          FALSE,          FALSE },
        ILLEGAL_CODE_DATA, ILLEGAL_CODE_DATA, ILLEGAL_CODE_DATA, ILLEGAL_CODE_DATA, ILLEGAL_CODE_DATA, ILLEGAL_CODE_DATA,

        // 60-67
        // getType,                                 getKind                         Value                           hasEncodedValue     isValidEmergeCode   isPrimitive     isArray         isSimpleMessage 
        {  EmergeCode.EmergeType.ENCODED,           EmergeCode.EmergeKind.LABEL_MSG, EmergeCode.LabelMsg.REQUEST,     TRUE,             TRUE,               FALSE,          FALSE,          TRUE },
        {  EmergeCode.EmergeType.ENCODED,           EmergeCode.EmergeKind.LABEL_MSG, EmergeCode.LabelMsg.REPLY_GOOD,  TRUE,             TRUE,               FALSE,          FALSE,          FALSE },
        {  EmergeCode.EmergeType.ENCODED,           EmergeCode.EmergeKind.LABEL_MSG, EmergeCode.LabelMsg.REPLY_ERROR, TRUE,             TRUE,               FALSE,          FALSE,          TRUE },
        ILLEGAL_CODE_DATA, ILLEGAL_CODE_DATA, ILLEGAL_CODE_DATA, ILLEGAL_CODE_DATA, ILLEGAL_CODE_DATA,

        // 68-6F
        ILLEGAL_CODE_DATA, ILLEGAL_CODE_DATA, ILLEGAL_CODE_DATA, ILLEGAL_CODE_DATA, ILLEGAL_CODE_DATA, ILLEGAL_CODE_DATA, ILLEGAL_CODE_DATA, ILLEGAL_CODE_DATA,
        
        // 70-77
        ILLEGAL_CODE_DATA, ILLEGAL_CODE_DATA, ILLEGAL_CODE_DATA, ILLEGAL_CODE_DATA, ILLEGAL_CODE_DATA, ILLEGAL_CODE_DATA, ILLEGAL_CODE_DATA, ILLEGAL_CODE_DATA,
        
        // 78-7F
        ILLEGAL_CODE_DATA, ILLEGAL_CODE_DATA, ILLEGAL_CODE_DATA, ILLEGAL_CODE_DATA, ILLEGAL_CODE_DATA, ILLEGAL_CODE_DATA, ILLEGAL_CODE_DATA, ILLEGAL_CODE_DATA,

        // 80-87
        // getType,                                 getKind                         Value     hasEncodedValue   isValidEmergeCode   isPrimitive     isArray         isSimpleMessage 
        {  EmergeCode.EmergeType.SIMPLE,            EmergeCode.EmergeKind.NULL,     IAE,      FALSE,            TRUE,               TRUE,          FALSE,          FALSE },
        ILLEGAL_CODE_DATA, ILLEGAL_CODE_DATA, ILLEGAL_CODE_DATA, ILLEGAL_CODE_DATA, ILLEGAL_CODE_DATA, ILLEGAL_CODE_DATA, ILLEGAL_CODE_DATA,

        // 88-8F
        // getType,                                 getKind                         Value     hasEncodedValue   isValidEmergeCode   isPrimitive     isArray         isSimpleMessage 
        {  EmergeCode.EmergeType.SIMPLE,            EmergeCode.EmergeKind.INDIR,    IAE,      FALSE,            TRUE,               TRUE,          FALSE,          FALSE },
        ILLEGAL_CODE_DATA, ILLEGAL_CODE_DATA, ILLEGAL_CODE_DATA, ILLEGAL_CODE_DATA, ILLEGAL_CODE_DATA, ILLEGAL_CODE_DATA, ILLEGAL_CODE_DATA,

        // 90-97
        // getType,                                 getKind                         Value     hasEncodedValue   isValidEmergeCode   isPrimitive     isArray         isSimpleMessage 
        {  EmergeCode.EmergeType.SIMPLE,            EmergeCode.EmergeKind.BOOL_ARR, IAE,      FALSE,            TRUE,               FALSE,          TRUE,          FALSE },
        ILLEGAL_CODE_DATA, ILLEGAL_CODE_DATA, ILLEGAL_CODE_DATA, ILLEGAL_CODE_DATA, ILLEGAL_CODE_DATA, ILLEGAL_CODE_DATA, ILLEGAL_CODE_DATA,

        // 98-9F
        // getType,                                 getKind                         Value     hasEncodedValue   isValidEmergeCode   isPrimitive     isArray         isSimpleMessage 
        {  EmergeCode.EmergeType.SIMPLE,            EmergeCode.EmergeKind.BYTE_ARR, IAE,      FALSE,            TRUE,               FALSE,          TRUE,          FALSE },
        ILLEGAL_CODE_DATA, ILLEGAL_CODE_DATA, ILLEGAL_CODE_DATA, ILLEGAL_CODE_DATA, ILLEGAL_CODE_DATA, ILLEGAL_CODE_DATA, ILLEGAL_CODE_DATA,

        // A0-A7
        // getType,                                 getKind                         Value     hasEncodedValue   isValidEmergeCode   isPrimitive     isArray         isSimpleMessage 
        {  EmergeCode.EmergeType.SIMPLE,            EmergeCode.EmergeKind.CHAR_ARR, IAE,      FALSE,            TRUE,               FALSE,          TRUE,          FALSE },
        ILLEGAL_CODE_DATA, ILLEGAL_CODE_DATA, ILLEGAL_CODE_DATA, ILLEGAL_CODE_DATA, ILLEGAL_CODE_DATA, ILLEGAL_CODE_DATA, ILLEGAL_CODE_DATA,

        // A8-AF
        // getType,                                 getKind                         Value     hasEncodedValue   isValidEmergeCode   isPrimitive     isArray         isSimpleMessage 
        {  EmergeCode.EmergeType.SIMPLE,            EmergeCode.EmergeKind.SHORT_ARR,IAE,      FALSE,            TRUE,               FALSE,          TRUE,          FALSE },
        ILLEGAL_CODE_DATA, ILLEGAL_CODE_DATA, ILLEGAL_CODE_DATA, ILLEGAL_CODE_DATA, ILLEGAL_CODE_DATA, ILLEGAL_CODE_DATA, ILLEGAL_CODE_DATA,

        // B0-B7
        // getType,                                 getKind                         Value     hasEncodedValue   isValidEmergeCode   isPrimitive     isArray         isSimpleMessage 
        {  EmergeCode.EmergeType.SIMPLE,            EmergeCode.EmergeKind.INT_ARR,  IAE,      FALSE,            TRUE,               FALSE,          TRUE,          FALSE },
        ILLEGAL_CODE_DATA, ILLEGAL_CODE_DATA, ILLEGAL_CODE_DATA, ILLEGAL_CODE_DATA, ILLEGAL_CODE_DATA, ILLEGAL_CODE_DATA, ILLEGAL_CODE_DATA,

        // B8-BF
        // getType,                                 getKind                         Value     hasEncodedValue   isValidEmergeCode   isPrimitive     isArray         isSimpleMessage 
        {  EmergeCode.EmergeType.SIMPLE,            EmergeCode.EmergeKind.LONG_ARR, IAE,      FALSE,            TRUE,               FALSE,          TRUE,          FALSE },
        ILLEGAL_CODE_DATA, ILLEGAL_CODE_DATA, ILLEGAL_CODE_DATA, ILLEGAL_CODE_DATA, ILLEGAL_CODE_DATA, ILLEGAL_CODE_DATA, ILLEGAL_CODE_DATA,

        // C0-C7
        // getType,                                 getKind                         Value     hasEncodedValue   isValidEmergeCode   isPrimitive     isArray         isSimpleMessage 
        {  EmergeCode.EmergeType.SIMPLE,            EmergeCode.EmergeKind.FLOAT_ARR,IAE,      FALSE,            TRUE,               FALSE,          TRUE,          FALSE },
        ILLEGAL_CODE_DATA, ILLEGAL_CODE_DATA, ILLEGAL_CODE_DATA, ILLEGAL_CODE_DATA, ILLEGAL_CODE_DATA, ILLEGAL_CODE_DATA, ILLEGAL_CODE_DATA,

        // C8-CF
        // getType,                                 getKind                         Value     hasEncodedValue   isValidEmergeCode   isPrimitive     isArray         isSimpleMessage 
        {  EmergeCode.EmergeType.SIMPLE,            EmergeCode.EmergeKind.DOUBLE_ARR, IAE,    FALSE,            TRUE,               FALSE,          TRUE,           FALSE },
        ILLEGAL_CODE_DATA, ILLEGAL_CODE_DATA, ILLEGAL_CODE_DATA, ILLEGAL_CODE_DATA, ILLEGAL_CODE_DATA, ILLEGAL_CODE_DATA, ILLEGAL_CODE_DATA,

        // D0-D7
        // getType,                                 getKind                         Value     hasEncodedValue   isValidEmergeCode   isPrimitive     isArray         isSimpleMessage 
        {  EmergeCode.EmergeType.SIMPLE,            EmergeCode.EmergeKind.REF_ARR,  IAE,      FALSE,            TRUE,               FALSE,          TRUE,           FALSE },
        ILLEGAL_CODE_DATA, ILLEGAL_CODE_DATA, ILLEGAL_CODE_DATA, ILLEGAL_CODE_DATA, ILLEGAL_CODE_DATA, ILLEGAL_CODE_DATA, ILLEGAL_CODE_DATA,

        // D8-DF
        // getType,                                 getKind                         Value     hasEncodedValue   isValidEmergeCode   isPrimitive     isArray         isSimpleMessage 
        {  EmergeCode.EmergeType.SIMPLE,            EmergeCode.EmergeKind.FLIST,    IAE,      FALSE,            TRUE,               FALSE,          FALSE,          TRUE },
        ILLEGAL_CODE_DATA, ILLEGAL_CODE_DATA, ILLEGAL_CODE_DATA, ILLEGAL_CODE_DATA, ILLEGAL_CODE_DATA, ILLEGAL_CODE_DATA, ILLEGAL_CODE_DATA,

        // E0-E7
        // getType,                                 getKind                         Value     hasEncodedValue   isValidEmergeCode   isPrimitive     isArray         isSimpleMessage 
        {  EmergeCode.EmergeType.SIMPLE,            EmergeCode.EmergeKind.CLOSE_SESSION,IAE,  FALSE,            TRUE,               FALSE,          FALSE,          TRUE },
        ILLEGAL_CODE_DATA, ILLEGAL_CODE_DATA, ILLEGAL_CODE_DATA, ILLEGAL_CODE_DATA, ILLEGAL_CODE_DATA, ILLEGAL_CODE_DATA, ILLEGAL_CODE_DATA,

        // E8-EF
        // getType,                                 getKind                         Value     hasEncodedValue   isValidEmergeCode   isPrimitive     isArray         isSimpleMessage 
        {  EmergeCode.EmergeType.SIMPLE,            EmergeCode.EmergeKind.REJECT_REQUEST,IAE, FALSE,            TRUE,               FALSE,          FALSE,          TRUE },
        ILLEGAL_CODE_DATA, ILLEGAL_CODE_DATA, ILLEGAL_CODE_DATA, ILLEGAL_CODE_DATA, ILLEGAL_CODE_DATA, ILLEGAL_CODE_DATA, ILLEGAL_CODE_DATA,

        // F0-F7
        // getType,                                 getKind                         Value     hasEncodedValue   isValidEmergeCode   isPrimitive     isArray         isSimpleMessage 
        {  EmergeCode.EmergeType.SIMPLE,            EmergeCode.EmergeKind.REF,      IAE,      FALSE,            TRUE,               FALSE,          FALSE,          FALSE },
        ILLEGAL_CODE_DATA, ILLEGAL_CODE_DATA, ILLEGAL_CODE_DATA, ILLEGAL_CODE_DATA, ILLEGAL_CODE_DATA, ILLEGAL_CODE_DATA, ILLEGAL_CODE_DATA,

        // F8-FF
        ILLEGAL_CODE_DATA, ILLEGAL_CODE_DATA, ILLEGAL_CODE_DATA, ILLEGAL_CODE_DATA, ILLEGAL_CODE_DATA, ILLEGAL_CODE_DATA, ILLEGAL_CODE_DATA, ILLEGAL_CODE_DATA } ;

        private String makeMessage( String msg, Method method, EmergeCode ec ) {
            return msg + " in call to " + method + " for code " + ec ;
        }

        @Test
        public void testGetCodeInt() {
            Assert.assertTrue( CODE_INFORMATION.length == 256 ) ; 
            for (int ctr = 0; ctr < CODE_INFORMATION.length; ctr++ ) {
                EmergeCode ec = EmergeCodeFactory.getCode( ctr ) ;
                Assert.assertEquals( ec.code(), ctr, "Code does not match" ) ;
                if (ctr < 128)
                    Assert.assertEquals( ec.byteCode(), (byte)ctr, "ByteCode does not match" ) ;
                else
                    Assert.assertEquals( ec.byteCode(), (byte)(ctr-256), "ByteCode does not match" ) ;

                Object[] arr = CODE_INFORMATION[ctr] ;
                for (int inner=0; inner<EMERGE_CODE_METHODS.length; inner++) {
                    Object expected = arr[inner] ;
                    Method method = EMERGE_CODE_METHODS[inner] ;

                    try {
                        Object result = null ;
                        if (method == GET_VALUE_METHOD)
                            result = method.invoke( ec, expected.getClass() ) ;
                        else
                            result = method.invoke( ec ) ;

                        if (expected instanceof Exception) {
                            Assert.fail( makeMessage( "Expected exception " + expected 
                                + " but returned " + result, method, ec ) ) ;
                        } else {
                            Assert.assertEquals( result, expected, 
                                makeMessage( "Unexpected result", method, ec ) ) ;
                        }
                    } catch (Exception exc) {
                        Throwable ex = exc ;
                        if (exc instanceof InvocationTargetException)
                            ex = exc.getCause() ;
                            
                        if ((expected == null) || !expected.getClass().isInstance( ex )) {
                            exc.printStackTrace() ;
                            Assert.fail( makeMessage( "Unexpected exception " + exc, method, ec ) ) ;
                        }
                    }
                }
            }
        }


    // getCode( kind, value ) result may be EmergeCode or exception
    //      if result is ec, check:
    //          ec.getKind = kind
    //          ec.getValue( Type.class ) = value and ec.hasEncodedValue
    //          ec.getValue( Type.class ) = null and !ec.hasEncodedValue
    //
    // Test data: triples of
    //      - kind
    //      - value
    //      - true, false, or Exception
    //
    private static Object[][] GET_CODE_DATA = new Object[][] {
        { EmergeCode.EmergeKind.BOOL,           Boolean.valueOf( true ),TRUE },
        { EmergeCode.EmergeKind.BOOL,           Boolean.valueOf( false ),TRUE },

        { EmergeCode.EmergeKind.CHAR,           null,                   FALSE },
        { EmergeCode.EmergeKind.CHAR,           'A',                    IAE },

        { EmergeCode.EmergeKind.BYTE,           Byte.valueOf( (byte)-1 ),     TRUE },
        { EmergeCode.EmergeKind.BYTE,           Byte.valueOf( (byte) 2 ),     TRUE },
        { EmergeCode.EmergeKind.BYTE,           Byte.valueOf( (byte) 5 ),     TRUE },
        { EmergeCode.EmergeKind.BYTE,           Byte.valueOf( (byte) 10 ),    FALSE },
        { EmergeCode.EmergeKind.BYTE,           null,                   FALSE },
        { EmergeCode.EmergeKind.BYTE,           new Object(),           IAE },

        { EmergeCode.EmergeKind.SHORT,          Short.valueOf( (short)-1 ),    TRUE },
        { EmergeCode.EmergeKind.SHORT,          Short.valueOf( (short) 2 ),    TRUE },
        { EmergeCode.EmergeKind.SHORT,          Short.valueOf( (short) 5 ),    TRUE },
        { EmergeCode.EmergeKind.SHORT,          Short.valueOf( (short) 12 ),   FALSE },

        { EmergeCode.EmergeKind.INT,            Integer.valueOf( -1 ),  TRUE },
        { EmergeCode.EmergeKind.INT,            Integer.valueOf(  2 ),  TRUE },
        { EmergeCode.EmergeKind.INT,            Integer.valueOf(  5 ),  TRUE },
        { EmergeCode.EmergeKind.INT,            Integer.valueOf( 24 ),  FALSE },

        { EmergeCode.EmergeKind.LONG,           Long.valueOf( -1 ),     TRUE },
        { EmergeCode.EmergeKind.LONG,           Long.valueOf(  2 ),     TRUE },
        { EmergeCode.EmergeKind.LONG,           Long.valueOf(  5 ),     TRUE },
        { EmergeCode.EmergeKind.LONG,           Long.valueOf(  231 ),   FALSE },

        { EmergeCode.EmergeKind.FLOAT,          Float.valueOf( (float)-1.0 ),  TRUE },
        { EmergeCode.EmergeKind.FLOAT,          Float.valueOf( (float)0.0 ),  TRUE },
        { EmergeCode.EmergeKind.FLOAT,          Float.valueOf( (float) 1.0 ),  TRUE },
        { EmergeCode.EmergeKind.FLOAT,          Float.valueOf( (float) 1.23 ), FALSE },

        { EmergeCode.EmergeKind.DOUBLE,         Double.valueOf( -1.0 ), TRUE },
        { EmergeCode.EmergeKind.DOUBLE,         Double.valueOf(  0.0 ), TRUE },
        { EmergeCode.EmergeKind.DOUBLE,         Double.valueOf(  1.0 ), TRUE },
        { EmergeCode.EmergeKind.DOUBLE,         Double.valueOf(  1.23 ),FALSE },

        { EmergeCode.EmergeKind.TUPLE,          EmergeCode.TupleCode.TUPLE_START,   TRUE },
        { EmergeCode.EmergeKind.TUPLE,          EmergeCode.TupleCode.TUPLE_END,     TRUE },
        { EmergeCode.EmergeKind.TUPLE,          null,                               IAE },
        { EmergeCode.EmergeKind.TUPLE,          new Object(),                       IAE },

        { EmergeCode.EmergeKind.PART,           EmergeCode.PartCode.NO_CUSTOM,      TRUE },
        { EmergeCode.EmergeKind.PART,           EmergeCode.PartCode.HAS_CUSTOM,     TRUE },
        { EmergeCode.EmergeKind.PART,           null,                               IAE },
        { EmergeCode.EmergeKind.PART,           new Object(),                       IAE },

        { EmergeCode.EmergeKind.MSG,            EmergeCode.MsgCode.MSG_START,       TRUE },
        { EmergeCode.EmergeKind.MSG,            EmergeCode.MsgCode.MSG_END,         TRUE },
        { EmergeCode.EmergeKind.MSG,            null,                               IAE },
        { EmergeCode.EmergeKind.MSG,            new Object(),                       IAE },

        { EmergeCode.EmergeKind.LABEL_MSG,      EmergeCode.LabelMsg.REQUEST,        TRUE },
        { EmergeCode.EmergeKind.LABEL_MSG,      EmergeCode.LabelMsg.REPLY_GOOD,     TRUE },
        { EmergeCode.EmergeKind.LABEL_MSG,      EmergeCode.LabelMsg.REPLY_ERROR,    TRUE },
        { EmergeCode.EmergeKind.LABEL_MSG,      null,                               IAE },
        { EmergeCode.EmergeKind.LABEL_MSG,      new Object(),                       IAE },

        { EmergeCode.EmergeKind.NULL,           null,                               FALSE },
        { EmergeCode.EmergeKind.NULL,           new Object(),                       IAE },

        { EmergeCode.EmergeKind.INDIR,          null,                               FALSE },
        { EmergeCode.EmergeKind.INDIR,          new Object(),                       IAE },

        { EmergeCode.EmergeKind.BOOL_ARR,       null,                               FALSE },
        { EmergeCode.EmergeKind.BOOL_ARR,       new Object(),                       IAE },

        { EmergeCode.EmergeKind.BYTE_ARR,       null,                               FALSE },
        { EmergeCode.EmergeKind.BYTE_ARR,       new Object(),                       IAE },

        { EmergeCode.EmergeKind.CHAR_ARR,       null,                               FALSE },
        { EmergeCode.EmergeKind.CHAR_ARR,       new Object(),                       IAE },

        { EmergeCode.EmergeKind.SHORT_ARR,      null,                               FALSE },
        { EmergeCode.EmergeKind.SHORT_ARR,      new Object(),                       IAE },

        { EmergeCode.EmergeKind.INT_ARR,        null,                               FALSE },
        { EmergeCode.EmergeKind.INT_ARR,        new Object(),                       IAE },

        { EmergeCode.EmergeKind.LONG_ARR,       null,                               FALSE },
        { EmergeCode.EmergeKind.LONG_ARR,       new Object(),                       IAE },

        { EmergeCode.EmergeKind.FLOAT_ARR,      null,                               FALSE },
        { EmergeCode.EmergeKind.FLOAT_ARR,      new Object(),                       IAE },

        { EmergeCode.EmergeKind.DOUBLE_ARR,     null,                               FALSE },
        { EmergeCode.EmergeKind.DOUBLE_ARR,     new Object(),                       IAE },

        { EmergeCode.EmergeKind.REF_ARR,        null,                               FALSE },
        { EmergeCode.EmergeKind.REF_ARR,        new Object(),                       IAE },

        { EmergeCode.EmergeKind.CLOSE_SESSION,  null,                               FALSE },
        { EmergeCode.EmergeKind.CLOSE_SESSION,  new Object(),                       IAE },

        { EmergeCode.EmergeKind.REJECT_REQUEST, null,                               FALSE },
        { EmergeCode.EmergeKind.REJECT_REQUEST, new Object(),                       IAE },

        { EmergeCode.EmergeKind.REF,            null,                               FALSE },
        { EmergeCode.EmergeKind.REF,            new Object(),                       IAE },

        { EmergeCode.EmergeKind.KIND_UNUSED_7,  null,                               IAE },
        { EmergeCode.EmergeKind.KIND_UNUSED_13, null,                               IAE },
        { EmergeCode.EmergeKind.KIND_UNUSED_14, null,                               IAE },
        { EmergeCode.EmergeKind.KIND_UNUSED_15, null,                               IAE },
        { EmergeCode.EmergeKind.KIND_UNUSED_31, null,                               IAE },
    } ;

    @Test
    public void testGetCode() {
        for (Object[] data : GET_CODE_DATA) {
            EmergeCode.EmergeKind kind = (EmergeCode.EmergeKind)data[0] ;
            Object value = data[1] ;
            Object expected = data[2] ;

            try {
                EmergeCode ec = EmergeCodeFactory.getCode( kind, value ) ;
                if (expected instanceof Exception) {
                    Assert.fail( "Expected exception " + expected + " but got return of " + ec ) ;
                } else {
                    Assert.assertTrue( expected instanceof Boolean ) ;
                    Assert.assertTrue( ec.hasEncodedValue() == ((Boolean)expected).booleanValue(), 
                        "Error in hasEncodedValue" ) ;

                    if ((value != null) && ec.hasEncodedValue()) {
                        Object actual = ec.getValue( value.getClass() ) ;
                        Assert.assertEquals( actual, value,
                            "Code " + ec + " does not contain the expected value" ) ;
                    }
                }
            } catch (Exception exc) {
                if (!(expected instanceof Exception)) {
                    exc.printStackTrace() ;
                    Assert.fail( "Unexpected exception " + exc + " for kind " + kind + " and value " + value ) ;
                }
            }
        }
    }
}
