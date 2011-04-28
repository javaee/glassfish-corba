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
package com.sun.corba.se.impl.encoding.fast ;

import java.util.Set ;
import java.util.HashSet ;

import org.glassfish.pfl.basic.fsm.Input ;
import org.glassfish.pfl.basic.fsm.Action ;
import org.glassfish.pfl.basic.fsm.Guard ;
import org.glassfish.pfl.basic.fsm.State ;
import org.glassfish.pfl.basic.fsm.FSM ;
import org.glassfish.pfl.basic.fsm.FSMImpl ;
import org.glassfish.pfl.basic.fsm.Runner;
import org.glassfish.pfl.basic.fsm.StateEngine ;

public class EmergeLLP {
    // Inputs: common to both FSMs
    static final Input PRIMITIVE = new Input.Base( "Primitive" ) ;
    static final Input ARRAY = new Input.Base( "Array" ) ;
    static final Input SIMPLE_MESSAGE = new Input.Base( "SimpleMessage" ) ;
    static final Input REF = new Input.Base( "Ref" ) ;
    static final Input MSG_START = new Input.Base( "MsgStart" ) ;
    static final Input MSG_END = new Input.Base( "MsgEnd" ) ;
    static final Input TUPLE_START = new Input.Base( "TupleStart" ) ;
    static final Input TUPLE_END = new Input.Base( "TupleEnd" ) ;
    static final Input PART_SIMPLE = new Input.Base( "PartSimple" ) ;
    static final Input PART_CUSTOM = new Input.Base( "PartCustom" ) ;
    static final Input END_OF_DATA = new Input.Base( "EndOfData" ) ;
    static final Input LABEL_MSG_REPLY_GOOD = new Input.Base( "LabelMsgReplyGood" ) ;
    static final Input ILLEGAL_CODE = new Input.Base( "IllegalCode" ) ;
    
    // Simple array that maps (by index) the int bytecode (0-255) to the corresponding
    // FSM input symbol.
    private static Input[] inputs = new Input[256] ;

    static {
	// initialize the input mapping
	for (int ctr=EmergeCodeFactory.MIN_VALID_INT_CODE; 
	    ctr<=EmergeCodeFactory.MAX_VALID_INT_CODE; ctr++) {

	    EmergeCode ec = EmergeCodeFactory.getCode( ctr ) ;
	    if (!ec.isValidEmergeCode()) {
		inputs[ctr] = ILLEGAL_CODE ;
	    } else if (ec.isPrimitive()) {
		inputs[ctr] = PRIMITIVE ;
	    } else if (ec.isArray()) {
		inputs[ctr] = ARRAY ;
	    } else {
		switch (ec.getKind()) {
		    case TUPLE :
			EmergeCode.TupleCode tc = ec.getValue( EmergeCode.TupleCode.class ) ;	
			if (tc == EmergeCode.TupleCode.TUPLE_START) {
                            inputs[ctr] = TUPLE_START;
                        } else if (tc == EmergeCode.TupleCode.TUPLE_END) {
                            inputs[ctr] = TUPLE_END;
                        } else
			    ; // ERROR
			break ;

		    case PART :
			EmergeCode.PartCode pc = ec.getValue( EmergeCode.PartCode.class ) ;	
			if (pc == EmergeCode.PartCode.NO_CUSTOM) {
                            inputs[ctr] = PART_SIMPLE;
                        } else if (pc == EmergeCode.PartCode.HAS_CUSTOM) {
                            inputs[ctr] = PART_CUSTOM;
                        } else
			    ; // ERROR
			break ;

		    case MSG :
			EmergeCode.MsgCode mc = ec.getValue( EmergeCode.MsgCode.class ) ;	
			if (mc == EmergeCode.MsgCode.MSG_START) {
                            inputs[ctr] = MSG_START;
                        } else if (mc == EmergeCode.MsgCode.MSG_END) {
                            inputs[ctr] = MSG_END;
                        } else
			    ; // ERROR
			break ;

		    case LABEL_MSG :
			EmergeCode.LabelMsg lmc = ec.getValue( EmergeCode.LabelMsg.class ) ;	
			if (lmc == EmergeCode.LabelMsg.REPLY_GOOD) {
                            inputs[ctr] = LABEL_MSG_REPLY_GOOD;
                        } else {
                            inputs[ctr] = SIMPLE_MESSAGE;
                        }
			break ;

		    case FLIST :
		    case CLOSE_SESSION :
		    case REJECT_REQUEST :
			inputs[ctr] = SIMPLE_MESSAGE ;
			break ;

		    case REF :
			inputs[ctr] = REF ;
			break ;
		}
	    }
	}
    }

    
    // State common to both FSMs
    private static final State ERROR = new State( "Error", State.Kind.FINAL ) {
        @Override
	public FSM preAction( FSM fsm ) {
	    // XXX clean this up: needs better message
	    throw new IllegalStateException( "Illegal state" ) ;
	}
    } ;
    
    // package private to allow inner class access without synthetic method.
    // The setXXXCtr methods are also accessed from other classes in this package.
    long partCtr = 0 ;
    long dataCtr = 0 ;

    // package private to allow access from the stream decoder
    void setDataCtr( long value ) {
	dataCtr = value ;
    }

    // package private to allow access from the stream decoder
    void setPartCtr( long value ) {
	partCtr = value ;
    }
 
    private final class LLPMain extends FSMImpl {
	private final Set<State> states = new HashSet<State>() ;

	private final State START = new State( states, "Start", State.Kind.INITIAL ) ;

	private final State LABEL_MSG = new State( states, "LabelMsg" ) ;
	private final State START_MSG = new State( states, "StartMsg" ) ;
	private final State EXPECT_TUPLE_START_2 = new State( states, "ExpectTupleStart2" ) ;
	private final State HANDLE_REF_SEQ = new State( states, "HandleRefSeq" ) ;
	
	// REFERENCE states transition into a new FSM in state Value
	private final State OTHER_VALUE = new State( "OtherValue", State.Kind.REFERENCE ) {
            @Override
	    public FSM preAction( FSM fsm ) {
		return valueFSM ;
	    }

            @Override
	    public State returnAction( FSM fsm, FSM nestedFSM ) {
		if (fsm.getState() == OTHER_VALUE) {
                    return START;
                } else if (fsm.getState() == MESSAGE_VALUE) {
                    return END;
                } else {
                    throw new IllegalStateException("Bad state");
                }
	    }
	} ;

	private final State MESSAGE_VALUE = new State( "MessageValue", State.Kind.REFERENCE ) {
            @Override
	    public FSM preAction( FSM fsm ) {
		return valueFSM ;
	    }
	} ;

	private final State END = new State( "End", State.Kind.FINAL ) ;

	private final StateEngine engine ;

	LLPMain() {
	    super( StateEngine.create(), null ) ;
            setState( START ) ;
            engine = getStateEngine() ;

	    for (State state : states) {
                engine.setDefault(state, ERROR);
            }

	    //	    State,		Input,		    Guard,		Action,	    new State
	    engine.add( START,		PRIMITIVE,				null,	    START ) ;
	    engine.add( START,		ARRAY,					null,	    START ) ; 
	    engine.add( START,		SIMPLE_MESSAGE,				null,	    START ) ;
	    engine.add( START,		END_OF_DATA,				null,	    END ) ;
	    engine.add( START,		MSG_START,				null,	    START_MSG ) ;
	    engine.add( START,		LABEL_MSG_REPLY_GOOD,			null,	    LABEL_MSG ) ;
	    engine.add( START,		REF,					null,	    OTHER_VALUE ) ;

	    engine.add( START_MSG,		TUPLE_START,			null,	    EXPECT_TUPLE_START_2 ) ;

	    engine.add( EXPECT_TUPLE_START_2,   PRIMITIVE,			null,	    EXPECT_TUPLE_START_2 ) ;
	    engine.add( EXPECT_TUPLE_START_2,   TUPLE_END,			null,	    HANDLE_REF_SEQ ) ;

	    engine.add( HANDLE_REF_SEQ,	ARRAY,					null,	    HANDLE_REF_SEQ ) ;
	    engine.add( HANDLE_REF_SEQ,	MSG_END,				null,	    START ) ;
	    engine.add( HANDLE_REF_SEQ,	REF,					null,	    MESSAGE_VALUE ) ;
	}
    }

    private final class LLPValue extends FSMImpl {
	private final Set<State> states = new HashSet<State>() ;
	
	// Nested FSM states
	private final State VALUE = new State( states, "Value", State.Kind.INITIAL ) ;

	private final State SIMPLE_PART = new State( states, "SimplePart" ) ;
	private final State CUSTOM_PART = new State( states, "CustomPart" ) ;
	private final State EXPECT_TUPLE_START = new State( states, "ExpectTupleStart" ) ;
	private final State CUSTOM_PART_TUPLE = new State( states, "CustomPartTuple" ) ;

	private final State VALUE_END = new State( "ValueEnd", State.Kind.FINAL ) ;
	
	// Actions
	private final Action ddc = new Action.Base( "decrement(dataCtr)" ) {
	    public void doIt( final FSM fsm, final Input in ) {
		EmergeLLP.this.dataCtr-- ;
	    }
	} ;

	private final Action dpc = new Action.Base( "decrement(partCtr)" ) {
	    public void doIt( final FSM fsm, final Input in ) {
		EmergeLLP.this.partCtr-- ;
	    }
	} ;

	private final Action ddpc = Action.Base.compose( ddc, dpc ) ;

	// Building blocks of Guards
	private final Guard.Base.IntFunc df = new Guard.Base.IntFunc( "EmergeLLP.dataCtr" ) {
	    public Integer evaluate( FSM fsm, Input inp ) {
		return (int)EmergeLLP.this.dataCtr ;
	    }
	} ;

	private final Guard.Base.IntFunc pf = new Guard.Base.IntFunc( "EmergeLLP.partCtr" ) {
	    public Integer evaluate( FSM fsm, Input inp ) {
		return (int)EmergeLLP.this.partCtr ;
	    }
	} ;

	private final Guard.Base.IntFunc one = Guard.Base.constant( 1 ) ;

	// The guards for the transitions
	private final Guard dataNotDone		= 
            Guard.Base.makeGuard( Guard.Base.gt( df, one ) ) ;
	private final Guard dataDone		= 
            Guard.Base.makeGuard( Guard.Base.eq( df, one ) ) ;
	private final Guard partNotDone		= 
            Guard.Base.makeGuard( Guard.Base.gt( pf, one ) ) ;
	private final Guard partDone		= 
            Guard.Base.makeGuard( Guard.Base.eq( pf, one ) ) ;
	private final Guard dataDoneMoreParts	= 
            Guard.Base.makeGuard( Guard.Base.and(
                Guard.Base.eq( df, one ), Guard.Base.gt( pf, one ) ) ) ;
	private final Guard dataDonePartsDone	= 
            Guard.Base.makeGuard( Guard.Base.and(
                Guard.Base.eq( df, one ), Guard.Base.eq( pf, one ) ) ) ;
    
	private StateEngine engine ;

	public LLPValue() {
	    super( StateEngine.create(), null ) ;
            setState( VALUE ) ;
            engine = getStateEngine() ;

	    for (State state : states) {
                engine.setDefault(state, ERROR);
            }

	    //	    State,		    Input,	    Guard,		Action,	    new State
	    engine.add( VALUE,		    PART_SIMPLE,			null,	    SIMPLE_PART ) ;
	    engine.add( VALUE,		    PART_CUSTOM,			null,	    CUSTOM_PART ) ;

	    engine.add( SIMPLE_PART,	    PRIMITIVE,	    dataNotDone,	ddc,	    SIMPLE_PART ) ;
	    engine.add( SIMPLE_PART,	    PRIMITIVE,	    dataDoneMoreParts,	ddpc,	    VALUE ) ;
	    engine.add( SIMPLE_PART,	    PRIMITIVE,	    dataDonePartsDone,	ddpc,	    VALUE_END ) ;

	    engine.add( CUSTOM_PART,	    PRIMITIVE,	    dataNotDone,	ddc,	    CUSTOM_PART ) ;
	    engine.add( CUSTOM_PART,	    PRIMITIVE,	    dataDone,		ddc,	    EXPECT_TUPLE_START ) ;

	    engine.add( EXPECT_TUPLE_START, TUPLE_START,			null,	    CUSTOM_PART_TUPLE ) ;

	    engine.add( CUSTOM_PART_TUPLE,  PRIMITIVE,				null,	    CUSTOM_PART_TUPLE ) ;
	    engine.add( CUSTOM_PART_TUPLE,  TUPLE_END,	    partNotDone,	dpc,	    VALUE ) ;	    
	    engine.add( CUSTOM_PART_TUPLE,  TUPLE_END,	    partDone,		dpc,	    VALUE_END ) ;	    
	}
    }

    private Runner runner ;

    // package private for use in LLPMain machine
    FSM valueFSM = new LLPValue() ;

    public EmergeLLP() {
	FSM mainFSM = new LLPMain() ;
	runner = new Runner( mainFSM ) ;
    }

    // Make sure that the correct series on inputs is observed.  If not,
    // throw an exception.
    public void checkState( EmergeCode input ) {
	runner.doIt( inputs[input.code()] ) ;
    }
}
