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
package com.sun.corba.se.impl.oa.poa ;

import java.util.concurrent.locks.Condition ;

import com.sun.corba.se.spi.orbutil.fsm.Action ;
import com.sun.corba.se.spi.orbutil.fsm.Guard ;
import com.sun.corba.se.spi.orbutil.fsm.State ;
import com.sun.corba.se.spi.orbutil.fsm.Input ;
import com.sun.corba.se.spi.orbutil.fsm.FSM ;
import com.sun.corba.se.spi.orbutil.fsm.FSMImpl ;
import com.sun.corba.se.spi.orbutil.fsm.Runner ;
import com.sun.corba.se.spi.orbutil.fsm.StateEngine ;
import com.sun.corba.se.spi.orbutil.tf.annotation.InfoMethod;
import com.sun.corba.se.spi.trace.PoaFSM;
import com.sun.corba.se.spi.logging.POASystemException ;

import org.omg.PortableServer.POAPackage.ObjectAlreadyActive ;

import static com.sun.corba.se.spi.orbutil.fsm.Guard.Base.* ;

/** AOMEntry represents a Servant or potential Servant in the ActiveObjectMap.
* It may be in several states to allow for long incarnate or etherealize 
* operations.  The methods on this class mostly represent input symbols to
* the state machine that controls the lifecycle of the entry.  A library is
* used to build the state machine rather than the more usual state pattern
* so that the state machine transitions are explicitly visible.
*/
@PoaFSM
public class AOMEntry extends FSMImpl {
    private static final POASystemException wrapper =
        POASystemException.self ;

    private Runner runner ;
    private final Thread[] etherealizer ;   // The actual etherealize operation 
					    // for this entry.  It is 
					    // represented as a Thread because
					    // the POA.deactivate_object never 
					    // waits for the completion.
    private final int[] counter ;	    // single element holder for counter 
					    // accessed in actions
    private final Condition wait ;	    // accessed in actions

    final POAImpl poa ;

    public static final State INVALID = new State( "Invalid", 
        State.Kind.INITIAL ) ;

    public static final State INCARN  = new State( "Incarnating" ) {
	@Override
	public void postAction( FSM fsm ) {
	    AOMEntry entry = (AOMEntry)fsm ;
	    entry.wait.signalAll() ;
	}
    };

    public static final State VALID   = new State( "Valid" ) ;

    public static final State ETHP    = new State( "EtherealizePending" ) ;

    public static final State ETH     = new State( "Etherealizing" ) {
        @Override
	public FSM preAction( FSM fsm ) {
	    AOMEntry entry = (AOMEntry)fsm ;
	    Thread etherealizer = entry.etherealizer[0] ;
	    if (etherealizer != null) {
                etherealizer.start();
            }
	    return null ;
	}

        @Override
	public void postAction( FSM fsm ) {
	    AOMEntry entry = (AOMEntry)fsm ;
	    entry.wait.signalAll() ;
	}
    };

    public static final State DESTROYED = new State( "Destroyed" ) ;

    static final Input START_ETH    = new Input.Base( "startEtherealize" ) ;
    static final Input ETH_DONE	    = new Input.Base( "etherealizeDone" ) ;
    static final Input INC_DONE	    = new Input.Base( "incarnateDone" ) ;
    static final Input INC_FAIL	    = new Input.Base( "incarnateFailure" ) ;
    static final Input ACTIVATE	    = new Input.Base( "activateObject" ) ;
    static final Input ENTER	    = new Input.Base( "enter" ) ;
    static final Input EXIT	    = new Input.Base( "exit" ) ;

    private static final Action incrementAction =
        new Action.Base( "increment" ) {
            public void doIt( FSM fsm, Input in ) {
                AOMEntry entry = (AOMEntry)fsm ;
                entry.counter[0]++ ;
            }
        } ;

    private static final Action decrementAction =
        new Action.Base( "decrement" ) {
            public void doIt( FSM fsm, Input in ) {
                AOMEntry entry = (AOMEntry)fsm ;
                if (entry.counter[0] > 0) {
                    entry.counter[0]--;
                } else {
                    throw wrapper.aomEntryDecZero();
                }
            }
        } ;

    private static final Action throwIllegalStateExceptionAction =
        new Action.Base(
            "throwIllegalStateException" ) {
            public void doIt( FSM fsm, Input in ) {
                throw new IllegalStateException(
                    "No transitions allowed from the DESTROYED state" ) ;
            }
        } ;

    private static final Action oaaAction =
        new Action.Base( "throwObjectAlreadyActive" ) {
            public void doIt( FSM fsm, Input in ) {
                throw new RuntimeException( new ObjectAlreadyActive() ) ;
            }
        } ;

    private static final Guard waitGuard = new Guard.Base( "wait" ) {
	public Guard.Result evaluate( FSM fsm, Input in ) {
	    AOMEntry entry = (AOMEntry)fsm ;
	    try {
		entry.wait.await() ;
	    } catch (InterruptedException exc) {
		// XXX Log this
		// NO-OP
	    }

	    return Guard.Result.DEFERRED ;
	}
    } ;

    private static final IntFunc counterFunc =
	new IntFunc( "counterFunc" ) {
	    public int evaluate( FSM fsm, Input in ) {
		AOMEntry entry = (AOMEntry)fsm ;
		return entry.counter[0] ;
	    }
	} ;

    private static final IntFunc one = constant( 1 ) ;
    private static final IntFunc zero = constant( 0 ) ;

    private static final Guard greaterZeroGuard =
        makeGuard( gt( counterFunc, zero ) ) ;
    private static final Guard zeroGuard =
        makeGuard( eq( counterFunc, zero ) ) ;
    private static final Guard greaterOneGuard =
        makeGuard( gt( counterFunc, one ) ) ;
    private static final Guard oneGuard =
        makeGuard( eq( counterFunc, one ) ) ;

    private static final StateEngine engine = StateEngine.create() ;

    static {
	//	    State,   Input,     Guard,			Action,		    new State

	engine.add( INVALID, ENTER,				incrementAction,    INCARN	) ;
	engine.add( INVALID, ACTIVATE,				null,		    VALID	) ;
	engine.setDefault( INVALID ) ;

	engine.add( INCARN,  ENTER,	waitGuard,		null,		    INCARN	) ;
	engine.add( INCARN,  EXIT,				null,		    INCARN	) ;
	engine.add( INCARN,  START_ETH,	waitGuard,		null,		    INCARN	) ;
	engine.add( INCARN,  INC_DONE,				null,		    VALID	) ;
	engine.add( INCARN,  INC_FAIL,				decrementAction,    INVALID	) ;  
	engine.add( INCARN,  ACTIVATE,				oaaAction,	    INCARN	) ;  

	engine.add( VALID,   ENTER,				incrementAction,    VALID	) ;
	engine.add( VALID,   EXIT,				decrementAction,    VALID	) ;
	engine.add( VALID,   START_ETH, greaterZeroGuard,	null,		    ETHP	) ;
	engine.add( VALID,   START_ETH, zeroGuard,		null,		    ETH		) ;
	engine.add( VALID,   ACTIVATE,				oaaAction,	    VALID	) ;  

	engine.add( ETHP,    ENTER,	waitGuard,		null,		    ETHP	) ;
	engine.add( ETHP,    START_ETH,				null,		    ETHP	) ;
	engine.add( ETHP,    EXIT,	greaterOneGuard,	decrementAction,    ETHP	) ;
	engine.add( ETHP,    EXIT,	oneGuard,		decrementAction,    ETH		) ;
	engine.add( ETHP,    ACTIVATE,				oaaAction,	    ETHP	) ;  

	engine.add( ETH,     START_ETH,				null,		    ETH		) ;
	engine.add( ETH,     ETH_DONE,				null,		    DESTROYED	) ;
	engine.add( ETH,     ENTER,	waitGuard,		null,		    ETH		) ;
	engine.add( ETH,     ACTIVATE,				oaaAction,	    ETH	) ;  
	
	engine.setDefault( DESTROYED, throwIllegalStateExceptionAction, DESTROYED ) ;

	engine.done() ;
    }

    public AOMEntry( POAImpl poa )
    {
	super( engine, INVALID ) ;
	runner = new Runner( this ) ;
	this.poa = poa ;
	etherealizer = new Thread[1] ;
	etherealizer[0] = null ;
	counter = new int[1] ;
	counter[0] = 0 ;
	wait = poa.makeCondition() ;
    }

    @InfoMethod
    private void state( State state ) { }

    @PoaFSM
    @Override
    public void setState( State state ) {
	super.setState( state ) ;
	state( getState() ) ;
    }

    // Methods that drive the FSM: the real interface to this class
    // Most just call the doIt method, but startEtherealize needs
    // the etherealizer.
    public void startEtherealize( Thread etherealizer ) 
    { 
	this.etherealizer[0] = etherealizer ;
	runner.doIt( START_ETH ) ; 
    }

    public void etherealizeComplete() { runner.doIt( ETH_DONE ) ; }
    public void incarnateComplete() { runner.doIt( INC_DONE ) ; }
    public void incarnateFailure() { runner.doIt( INC_FAIL ) ; }
    public void enter() { runner.doIt( ENTER ) ; }
    public void exit() { runner.doIt( EXIT ) ; }

    public void activateObject() throws ObjectAlreadyActive { 
	try {
	    runner.doIt( ACTIVATE ) ; 
	} catch (RuntimeException exc) {
	    Throwable thr = exc.getCause() ;
	    if (thr instanceof ObjectAlreadyActive) {
                throw (ObjectAlreadyActive) thr;
            } else {
                throw exc;
            }
	}
    }
}
