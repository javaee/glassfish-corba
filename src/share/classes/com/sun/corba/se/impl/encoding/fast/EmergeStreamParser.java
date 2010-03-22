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

import com.sun.corba.se.impl.encoding.fast.bytebuffer.Reader ; 

public class EmergeStreamParser {
    private EmergeLLP fsm ;
    private Reader reader ;

    public EmergeStreamParser( Reader reader ) {
	this.reader = reader ;
	fsm = new EmergeLLP() ;
    }

    public void parse( EmergeStreamEventHandler handler ) {
        LabelManager.Label selfLabel ;
        LabelManager.Label typeLabel ;
        long sessionId ;
        long offset = 0 ;
        long length = 0 ;
	byte bytecode = reader.getByte() ;
	EmergeCode ec = EmergeCodeFactory.getByteCode( bytecode ) ;

	// XXX Is the fsm a performance issue?  Speeding it up is tricky,
	// because there is no way to optimize Guard evaluation.  Best bet
	// may be to allow for custom-coded transition functions on States.
	// This could be a custom-coded Transition class that combines evaluation
	// of Guards with selection of nextState.
	// May also need to support better maps than a simple HashMap for
	// states and inputs: assigning indices may be needed (which may require
	// further assignment of state engine IDs to prevent stupid errors).
	fsm.checkState( ec ) ;

	// Have a valid bytecode, so now let's see what it is
	// Unused codes should have been rejected by fsm
	switch (ec.getKind()) {
	    case BYTE :		    
		byte byteResult = 0 ;
		if (ec.hasEncodedValue()) {
		    byteResult = ec.getValue( Byte.class ) ;
		} else {
		    byteResult = reader.getByte() ;
		}
		handler.byteEvent( byteResult ) ;
		break ;

	    case CHAR :		    
		char charResult = 0 ;
		if (ec.hasEncodedValue()) {
		    charResult = ec.getValue( Character.class ) ;
		} else {
		    charResult = reader.getChar() ;
		}
		handler.charEvent( charResult ) ;
		break ;

	    case SHORT :		    
		short shortResult = 0 ;
		if (ec.hasEncodedValue()) {
		    shortResult = ec.getValue( Short.class ) ;
		} else {
		    shortResult = reader.getShort() ;
		}
		handler.shortEvent( shortResult ) ;
		break ;

	    case INT :		    
		int intResult = 0 ;
		if (ec.hasEncodedValue()) {
		    intResult = ec.getValue( Integer.class ) ;
		} else {
		    intResult = reader.getInt() ;
		}
		handler.intEvent( intResult ) ;
		break ;

	    case LONG :		    
		long longResult = 0 ;
		if (ec.hasEncodedValue()) {
		    longResult = ec.getValue( Long.class ) ;
		} else {
		    longResult = reader.getLong() ;
		}
		handler.longEvent( longResult ) ;
		break ;

	    case FLOAT :		    
		float floatResult = 0 ;
		if (ec.hasEncodedValue()) {
		    floatResult = ec.getValue( Float.class ) ;
		} else {
		    floatResult = reader.getFloat() ;
		}
		handler.floatEvent( floatResult ) ;
		break ;

	    case DOUBLE :		    
		double doubleResult = 0 ;
		if (ec.hasEncodedValue()) {
		    doubleResult = ec.getValue( Double.class ) ;
		} else {
		    doubleResult = reader.getDouble() ;
		}
		handler.doubleEvent( doubleResult ) ;
		break ;

	    case BOOL :		    
		boolean boolResult = false ;
		if (ec.hasEncodedValue()) {
		    boolResult = ec.getValue( Boolean.class ) ;
		} else {
		    boolResult = reader.getBoolean() ;
		}
		handler.boolEvent( boolResult ) ;
		break ;

	    case TUPLE :		    
		EmergeCode.TupleCode tc = ec.getValue( EmergeCode.TupleCode.class ) ;
		if (tc == EmergeCode.TupleCode.TUPLE_START) {
		    handler.tupleStartEvent() ;
		} else if (tc == EmergeCode.TupleCode.TUPLE_START) {
		    handler.tupleEndEvent() ;
		} else {
		    // ERROR
		}
		break ;

	    case PART :		    
		typeLabel = new LabelManager.Label( reader ) ;
		// offset length, length needs to set EmergeLLP dataCtr
		offset = VarOctetUtility.get( reader ) ;
		length = VarOctetUtility.get( reader ) ;
		fsm.setDataCtr( length ) ;

		EmergeCode.PartCode pc = ec.getValue( EmergeCode.PartCode.class ) ;
		if (pc == EmergeCode.PartCode.NO_CUSTOM) {
		    handler.simplePartEvent( typeLabel, offset, length ) ;
		} else if (pc == EmergeCode.PartCode.HAS_CUSTOM) {
		    handler.customPartEvent( typeLabel, offset, length ) ;
		} else {
		    // ERROR
		}
		break ;

	    case MSG :		    
		// header body trailer
		// header : MSG-start request-id session-id fiber-id num-args
		// body : tuple-seq ref-seq
		// trailer : MSG-end request-id session-id fiber-id num-args
		long requestId = VarOctetUtility.get( reader ) ;
		sessionId = VarOctetUtility.get( reader ) ;
		long fiberId = VarOctetUtility.get( reader ) ;
		long numArgs = VarOctetUtility.get( reader ) ;

		EmergeCode.MsgCode mc = ec.getValue( EmergeCode.MsgCode.class ) ;
		if (mc == EmergeCode.MsgCode.MSG_START) {
		    handler.messageStartEvent( requestId, sessionId, fiberId, numArgs ) ;
		} else if (mc == EmergeCode.MsgCode.MSG_END) {
		    handler.messageEndEvent( requestId, sessionId, fiberId, numArgs ) ;
		} else {
		    // ERROR
		}
		break ;

	    case LABEL_MSG :	    
		LabelManager.Label label = new LabelManager.Label( reader ) ;
		switch (ec.getValue( EmergeCode.LabelMsg.class )) {
		    case REQUEST :
			handler.labelMessageRequestEvent( label ) ;
			break ;

		    case REPLY_GOOD :
			handler.labelMessageReplyGoodEvent( label ) ;
			break ;
			
		    case REPLY_ERROR :
			long category = VarOctetUtility.get( reader ) ;
			long minorCode = VarOctetUtility.get( reader ) ;
			handler.labelMessageReplyBadEvent( label, category, minorCode ) ;
			break ;
			
		    default:
			// ERROR
		}
		break ;

	    case NULL :		    
		handler.nullEvent() ;
		break ;

	    case INDIR :		    
		LabelManager.Label label2 = new LabelManager.Label( reader ) ;
		handler.indirEvent( label2 ) ;
		break ;

	    case BOOL_ARR :	    
		selfLabel = new LabelManager.Label( reader ) ;
		offset = VarOctetUtility.get( reader ) ;
		length = VarOctetUtility.get( reader ) ;
                boolean[] booleanValue = new boolean[ (int)length ] ;
		reader.getBooleanArray( booleanValue ) ;
		handler.boolArrEvent( selfLabel, offset, length, booleanValue ) ;
		break ;

	    case BYTE_ARR :	    
		selfLabel = new LabelManager.Label( reader ) ;
		offset = VarOctetUtility.get( reader ) ;
		length = VarOctetUtility.get( reader ) ;
		byte[] byteVale = new byte[(int)length] ;
                reader.getByteArray( byteVale ) ;
		handler.byteArrEvent( selfLabel, offset, length, byteVale ) ;
		break ;

	    case CHAR_ARR :	    
		selfLabel = new LabelManager.Label( reader ) ;
		offset = VarOctetUtility.get( reader ) ;
		length = VarOctetUtility.get( reader ) ;
		char[] charValue = new char[(int)length] ;
                reader.getCharArray( charValue ) ;
		handler.charArrEvent( selfLabel, offset, length, charValue ) ;
		break ;

	    case SHORT_ARR :	    
		selfLabel = new LabelManager.Label( reader ) ;
		offset = VarOctetUtility.get( reader ) ;
		length = VarOctetUtility.get( reader ) ;
		short[] shortValue = new short[(int)length] ;
                reader.getShortArray( shortValue ) ;
		handler.shortArrEvent( selfLabel, offset, length, shortValue ) ;
		break ;

	    case INT_ARR :	    
		selfLabel = new LabelManager.Label( reader ) ;
		offset = VarOctetUtility.get( reader ) ;
		length = VarOctetUtility.get( reader ) ;
		int[] intValue = new int[(int)length] ;
                reader.getIntArray( intValue ) ;
		handler.intArrEvent( selfLabel, offset, length, intValue ) ;
		break ;

	    case LONG_ARR :	    
		selfLabel = new LabelManager.Label( reader ) ;
		offset = VarOctetUtility.get( reader ) ;
		length = VarOctetUtility.get( reader ) ;
		long[] longValue = new long[(int)length] ;
                reader.getLongArray( longValue ) ;
		handler.longArrEvent( selfLabel, offset, length, longValue ) ;
		break ;

	    case FLOAT_ARR :	    
		selfLabel = new LabelManager.Label( reader ) ;
		offset = VarOctetUtility.get( reader ) ;
		length = VarOctetUtility.get( reader ) ;
		float[] floatValue = new float[(int)length] ;
                reader.getFloatArray( floatValue ) ;
		handler.floatArrEvent( selfLabel, offset, length, floatValue ) ;
		break ;

	    case DOUBLE_ARR :	    
		selfLabel = new LabelManager.Label( reader ) ;
		offset = VarOctetUtility.get( reader ) ;
		length = VarOctetUtility.get( reader ) ;
		double[] doubleValue = new double[(int)length] ;
                reader.getDoubleArray( doubleValue ) ;
		handler.doubleArrEvent( selfLabel, offset, length, doubleValue ) ;
		break ;

	    case REF_ARR :	    
		selfLabel = new LabelManager.Label( reader ) ;
		typeLabel = new LabelManager.Label( reader ) ;
		offset = VarOctetUtility.get( reader ) ;
		length = VarOctetUtility.get( reader ) ;
		LabelManager.Label[] refValue = new LabelManager.Label[ (int)length ] ;
		for (int ctr=0; ctr<length; ctr++) {
		    refValue[ctr] = new LabelManager.Label( reader ) ;
		}
		handler.refArrEvent( selfLabel, typeLabel, offset, length, refValue ) ;
		break ;

	    case FLIST :		    
		length = VarOctetUtility.get( reader ) ;
		long[] flistValue = new long[(int)length] ;
		for (int ctr=0; ctr<length; ctr++) {
		    flistValue[ctr] = VarOctetUtility.get( reader ) ;
		}
		handler.fiberListMessageEvent( flistValue ) ;
		break ;

	    case CLOSE_SESSION :	    
		sessionId = VarOctetUtility.get( reader ) ;
		handler.closeSessionMessageEvent( sessionId ) ;
		break ;

	    case REJECT_REQUEST :	    
		long category = VarOctetUtility.get( reader ) ;
		long minorCode = VarOctetUtility.get( reader ) ;
		handler.rejectRequestMessageEvent( category, minorCode ) ;
		break ;

	    case REF :		    
		selfLabel = new LabelManager.Label( reader ) ;
		length = VarOctetUtility.get( reader ) ;
		fsm.setPartCtr( length ) ;
		handler.refEvent( selfLabel, length ) ;
		break ; 

	    default :
		// ERROR
		break ;
	}
    }
}
