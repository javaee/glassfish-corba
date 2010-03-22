/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2007 Sun Microsystems, Inc. All rights reserved.
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
package com.sun.corba.se.impl.encoding.fast ;

import java.io.PrintStream ;

import com.sun.corba.se.spi.orbutil.file.Printer ;

import com.sun.corba.se.impl.encoding.fast.bytebuffer.Reader ;

/** This class provides a means of decoding the data in an emerge stream, 
 * and validating whether it is correctly encoded.  It does NOT deserialize,
 * as that is the job of the InputStream.  This is intended for debugging
 * the contents of an emerge stream.
 */
public class EmergeDecoder {
    private EmergeStreamEventHandler makeHandler( final PrintStream ps ) {
        return new EmergeStreamEventHandler() {
            Printer pr = new Printer( ps ) ;

            public void nullEvent() {
                pr.nl().p( "NULL" ) ;
            }

            public void indirEvent( LabelManager.Label label ) {
                pr.nl().p( "INDIR: " ).p( label ) ;
            }

            public void boolEvent( boolean value ) {
                pr.nl().p( "BOOL: " ).p( value ) ;
            }

            public void byteEvent( byte value ) {
                pr.nl().p( "BYTE: " ).p( value ) ;
            }

            public void charEvent( char value ) {
                pr.nl().p( "CHAR: " ).p( value ) ;
            }

            public void shortEvent( short value ) {
                pr.nl().p( "SHORT: " ).p( value ) ;
            }

            public void intEvent( int value ) {
                pr.nl().p( "INT: " ).p( value ) ;
            }

            public void longEvent( long value ) {
                pr.nl().p( "LONG: " ).p( value ) ;
            }

            public void floatEvent( float value ) {
                pr.nl().p( "FLOAT: " ).p( value ) ;
            }

            public void doubleEvent( double value ) {
                pr.nl().p( "DOUBLE: " ).p( value ) ;
            }

            private static final int INIT_INDEX_WIDTH = 5 ;
            private static final String INIT_SEPARATOR = ": " ;
            private static final int LINE_LENGTH = 90 ;

            private void displayAsStrings( Object[] data ) {
                // Display format:
                // (indent)XXXXXX: ddd ddd ddd ...
                // where XXXXX: allows for length up to 99999 and ddd is the
                // max length of any string in data.
                // Num elements per line is chosen to not exceed LINE_LENGTH.
                int length = data.length ;
                int maxSize = 0 ;
                for (int ctr=0; ctr<length; ctr++) {
                    int strlen = data[ctr].toString().length() ;
                    if (strlen > maxSize)
                        maxSize = strlen ;
                }
                int numPerLine = (LINE_LENGTH - pr.indent() - INIT_INDEX_WIDTH - INIT_SEPARATOR.length()) 
                    / (maxSize + 1 ) ;
                int numFullLines = length/numPerLine ;
                int numInLastLine = length - (numFullLines * numPerLine) ;
                for (int lineCtr=0; lineCtr < numFullLines; lineCtr++ ) {
                    int startIndex = lineCtr*numPerLine ;
                    pr.nl().rj( INIT_INDEX_WIDTH ).p( startIndex ).p( ": " ) ;
                    for (int innerCtr=0; innerCtr<numPerLine; innerCtr++ ) {
                        pr.rj( maxSize ).p( data[startIndex+innerCtr] ).p( ' ' ) ; 
                    }
                }

                if (numInLastLine > 0) {
                    int startIndex = numFullLines*numPerLine ;
                    pr.nl().rj( INIT_INDEX_WIDTH ).p( startIndex ).p( ": " ) ;
                    for( int ctr=0; ctr<numInLastLine; ctr++) {
                        pr.rj( maxSize ).p( data[startIndex+ctr] ).p( ' ' ) ;
                    }
                }
            }

            public void boolArrEvent(
                LabelManager.Label selfLabel,
                long offset,
                long length,
                boolean[] value ) {

                final Object[] objs = new Object[value.length] ;
                for (int ctr=0; ctr<value.length; ctr++ ) {
                    objs[ctr] = value[ctr] ;
                }

                pr.nl().p( "BOOL_ARR: " ).p( "selfLabel=" ).p( selfLabel ).p( " offset=" )
                    .p( offset ).p( " length=" ).p( length ).in() ;

                displayAsStrings( objs ) ;

                pr.out() ;
            }

            public void charArrEvent( 
                LabelManager.Label selfLabel,
                long offset,
                long length,
                char[] value ) {

                final Object[] objs = new Object[value.length] ;
                for (int ctr=0; ctr<value.length; ctr++ ) {
                    objs[ctr] = value[ctr] ;
                }

                pr.nl().p( "CHAR_ARR: " ).p( "selfLabel=" ).p( selfLabel ).p( " offset=" )
                    .p( offset ).p( " length=" ).p( length ).in() ;

                displayAsStrings( objs ) ;

                pr.out() ;
            }

            public void byteArrEvent( 
                LabelManager.Label selfLabel,
                long offset,
                long length,
                byte[] value ) {

                final Object[] objs = new Object[value.length] ;
                for (int ctr=0; ctr<value.length; ctr++ ) {
                    objs[ctr] = value[ctr] ;
                }

                pr.nl().p( "BYTE_ARR: " ).p( "selfLabel=" ).p( selfLabel ).p( " offset=" )
                    .p( offset ).p( " length=" ).p( length ).in() ;

                displayAsStrings( objs ) ;

                pr.out() ;
            }

            public void shortArrEvent( 
                LabelManager.Label selfLabel,
                long offset,
                long length,
                short[] value ) {

                final Object[] objs = new Object[value.length] ;
                for (int ctr=0; ctr<value.length; ctr++ ) {
                    objs[ctr] = value[ctr] ;
                }

                pr.nl().p( "SHORT_ARR: " ).p( "selfLabel=" ).p( selfLabel ).p( " offset=" )
                    .p( offset ).p( " length=" ).p( length ).in() ;

                displayAsStrings( objs ) ;

                pr.out() ;
            }

            public void intArrEvent( 
                LabelManager.Label selfLabel,
                long offset,
                long length,
                int[] value ) {

                final Object[] objs = new Object[value.length] ;
                for (int ctr=0; ctr<value.length; ctr++ ) {
                    objs[ctr] = value[ctr] ;
                }

                pr.nl().p( "INT_ARR: " ).p( "selfLabel=" ).p( selfLabel ).p( " offset=" )
                    .p( offset ).p( " length=" ).p( length ).in() ;

                displayAsStrings( objs ) ;

                pr.out() ;
            }

            public void longArrEvent( 
                LabelManager.Label selfLabel,
                long offset,
                long length,
                long[] value ) {

                final Object[] objs = new Object[value.length] ;
                for (int ctr=0; ctr<value.length; ctr++ ) {
                    objs[ctr] = value[ctr] ;
                }

                pr.nl().p( "LONG_ARR: " ).p( "selfLabel=" ).p( selfLabel ).p( " offset=" )
                    .p( offset ).p( " length=" ).p( length ).in() ;

                displayAsStrings( objs ) ;

                pr.out() ;
            }

            public void floatArrEvent( 
                LabelManager.Label selfLabel,
                long offset,
                long length,
                float[] value ) {

                final Object[] objs = new Object[value.length] ;
                for (int ctr=0; ctr<value.length; ctr++ ) {
                    objs[ctr] = value[ctr] ;
                }

                pr.nl().p( "FLOAT_ARR: " ).p( "selfLabel=" ).p( selfLabel ).p( " offset=" )
                    .p( offset ).p( " length=" ).p( length ).in() ;

                displayAsStrings( objs ) ;

                pr.out() ;
            }

            public void doubleArrEvent( 
                LabelManager.Label selfLabel,
                long offset,
                long length,
                double[] value ) {

                final Object[] objs = new Object[value.length] ;
                for (int ctr=0; ctr<value.length; ctr++ ) {
                    objs[ctr] = value[ctr] ;
                }

                pr.nl().p( "DOUBLE_ARR: " ).p( "selfLabel=" ).p( selfLabel ).p( " offset=" )
                    .p( offset ).p( " length=" ).p( length ).in() ;

                displayAsStrings( objs ) ;

                pr.out() ;
            }

            public void refArrEvent( 
                LabelManager.Label selfLabel,
                LabelManager.Label typeLabel,
                long offset,
                long length,
                LabelManager.Label[] value ) {

                pr.nl().p( "REF_ARR:" )
                    .p( " selfLabel=" ).p( selfLabel )
                    .p( " typeLabel=" ).p( typeLabel )
                    .p( " offset=" ).p( offset )
                    .p( " length=" ).p( length ).in() ;

                displayAsStrings( value ) ;

                pr.out() ;
            }

            public void refEvent(
                LabelManager.Label selfLabel,
                long numParts ) {
            }

            public void simplePartEvent(
                LabelManager.Label typeLabel,
                long offset,
                long length ) {
            }

            public void customPartEvent(
                LabelManager.Label typeLabel,
                long offset,
                long length ) {
            }

            public void tupleStartEvent() {
                pr.nl().p( "TUPLE (START)" ).in() ;
            }

            public void tupleEndEvent() {
                pr.out().nl().p( "TUPLE (END)" ) ;
            }

            public void labelMessageRequestEvent(
                LabelManager.Label label ) {
            }

            public void labelMessageReplyGoodEvent(
                LabelManager.Label label ) {
            }

            public void labelMessageReplyBadEvent(
                LabelManager.Label label,
                long reasonCodeCategory,
                long reasonCodeMinorCode ) {

                pr.nl().p( "LABEL_MSG (REPLY_ERROR)" )
                    .p( " label=" ).p( label ) 
                    .p( " reason code category=" ).p( reasonCodeCategory ) 
                    .p( " reason code minor code=" ).p( reasonCodeMinorCode ) ;
            }

            public void closeSessionMessageEvent(
                long sessionId ) {

                pr.nl().p( "CLOSE_SESSION: " ).p( "sessionId=" ).p( sessionId ) ;
            }

            public void rejectRequestMessageEvent(
                long reasonCodeCategory,
                long reasonCodeMinorCode ) {
            }

            public void fiberListMessageEvent(
                long[] fibers ) {
            }

            public void messageStartEvent(
                long requestId,
                long sessionId,
                long fiberId,
                long numArgs ) {

                pr.nl().p( "MESSAGE (end):" )
                    .p( " requestId=" ).p( requestId )
                    .p( " sessionId=" ).p( sessionId )
                    .p( " fiberId=" ).p( fiberId )
                    .p( " numArgs=" ).p( numArgs ).in() ;
            }

            public void messageEndEvent(
                long requestId,
                long sessionId,
                long fiberId,
                long numArgs ) {

                pr.out().nl().p( "MESSAGE (end):" )
                    .p( " requestId=" ).p( requestId )
                    .p( " sessionId=" ).p( sessionId )
                    .p( " fiberId=" ).p( fiberId )
                    .p( " numArgs=" ).p( numArgs ) ;
            }
        } ;
    }

    public EmergeDecoder( PrintStream ps, Reader reader ) {
	EmergeStreamParser parser = new EmergeStreamParser( reader ) ;
	parser.parse( makeHandler( ps ) ) ;
    }
}
