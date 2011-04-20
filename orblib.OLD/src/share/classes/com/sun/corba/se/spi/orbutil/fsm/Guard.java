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

package com.sun.corba.se.spi.orbutil.fsm;

import com.sun.corba.se.spi.orbutil.generic.BinaryBooleanFunction ;
import com.sun.corba.se.spi.orbutil.generic.BinaryIntFunction ;

/**
 *
 * @author Ken Cavanaugh
 */
public interface Guard {
    enum Result { ENABLED, DISABLED, DEFERRED } ;

    /** Called by the state engine to determine whether a
    * transition is enabled, defered, or disabled.
    * The result is interpreted as follows:
    * <ul>
    * <li>ENABLED if the transition is ready to proceed
    * <li>DISABLED if the transition is not ready to proceed
    * <li>DEFERED if the action associated with the transition
    * is to be deferred.  This means that the input will not be 
    * acted upon, but rather it will be saved for later execution.
    * Typically this is implemented using a CondVar wait, and the
    * blocked thread represents the defered input.  The defered
    * input is retried when the thread runs again.
    * </ul>
    *
    * @param fsm is the state machine causing this action.
    * @param in is the input that caused the transition.
    */
    public Result evaluate( FSM fsm, Input in ) ;

    public abstract class Base extends NameBase implements Guard {
	public static abstract class SimpleName {
	    private String name ;

	    public SimpleName( String name ) {
		this.name = name ;
	    }

	    public String toString() {
		return name ;
	    }
	}

	public static abstract class Predicate extends SimpleName 
	    implements BinaryBooleanFunction<FSM,Input> {

	    public Predicate( String name ) {
		super( name ) ;
	    }
	}

	public static abstract class IntFunc extends SimpleName 
	    implements BinaryIntFunction<FSM,Input> {

	    public IntFunc( String name ) {
		super( name ) ;
	    }
	}

	public static Guard makeGuard( final Predicate pred ) {
	    return new Guard.Base( pred.toString() ) {
		public Guard.Result evaluate( FSM fsm, Input in ) {
		    return pred.evaluate( fsm, in ) ?
			Result.ENABLED : Result.DISABLED ;
		}
	    } ;
	}

	public static Predicate not( final Predicate pred ) {
	    return new Predicate( "!" + pred.toString() ) {
		public boolean evaluate( final FSM fsm, final Input in ) {
		    return !pred.evaluate( fsm, in ) ;
		}
	    } ;
	}

	public static Predicate and( final Predicate arg1, final Predicate arg2 ) {
	    return new Predicate( "(" + arg1.toString() + "&&" + arg2.toString() + ")" ) {
		public boolean evaluate( final FSM fsm, final Input in ) {
		    if (!arg1.evaluate( fsm, in ))
			return false ;
		    else 
			return arg2.evaluate( fsm, in ) ;
		}
	    } ;
	}

	public static Predicate or( final Predicate arg1, final Predicate arg2 ) {
	    return new Predicate( "(" + arg1.toString() + "||" + arg2.toString() + ")" ) {
		public boolean evaluate( final FSM fsm, final Input in ) {
		    if (arg1.evaluate( fsm, in ))
			return true ;
		    else 
			return arg2.evaluate( fsm, in ) ;
		}
	    } ;
	}

	public static IntFunc constant( final int val ) {
	    return new IntFunc( "constant(" + val + ")" ) {
		public int evaluate( final FSM fsm, final Input input ) {
		    return val ;
		}
	    } ;
	}

	/* This does not seem to be worthwhile
	public static IntFunc field( final Class cls, final String fieldName ) {
	    final Field fld = cls.getField( fieldName ) ;

	    return new IntFunc( cls + "." + fieldName ) {
		public boolean evaluate( final FSM fsm, final Input in ) {
		    // check that fsm is an instance of cls
		    return fld.getInt( fsm ) ;
		}
	    }
	}
	*/

	public static Predicate lt( final IntFunc arg1, final IntFunc arg2 ) {
	    return new Predicate( "(" + arg1.toString()
		+ "<" + arg2.toString() + ")" ) {

		public boolean evaluate( final FSM fsm, final Input in ) {
		    return arg1.evaluate( fsm, in ) < arg2.evaluate( fsm, in ) ;
		}
	    } ;
	}

	public static Predicate le( final IntFunc arg1, final IntFunc arg2 ) {
	    return new Predicate( "(" + arg1.toString()
		+ "<=" + arg2.toString() + ")" ) {
		
		public boolean evaluate( final FSM fsm, final Input in ) {
		    return arg1.evaluate( fsm, in ) <= arg2.evaluate( fsm, in ) ;
		}
	    } ;
	}

	public static Predicate gt( final IntFunc arg1, final IntFunc arg2 ) {
	    return new Predicate( "(" + arg1.toString()
		+ ">" + arg2.toString() + ")" ) {
		
		public boolean evaluate( final FSM fsm, final Input in ) {
		    return arg1.evaluate( fsm, in ) > arg2.evaluate( fsm, in ) ;
		}
	    } ;
	}

	public static Predicate ge( final IntFunc arg1, final IntFunc arg2 ) {
	    return new Predicate( "(" + arg1.toString() 
		+ ">=" + arg2.toString() + ")" ) {

		public boolean evaluate( final FSM fsm, final Input in ) {
		    return arg1.evaluate( fsm, in ) >= arg2.evaluate( fsm, in ) ;
		}
	    } ;
	}

	public static Predicate eq( final IntFunc arg1, final IntFunc arg2 ) {
	    return new Predicate( "(" + arg1.toString() 
		+ "==" + arg2.toString() + ")" ) {

		public boolean evaluate( final FSM fsm, final Input in ) {
		    return arg1.evaluate( fsm, in ) == arg2.evaluate( fsm, in ) ;
		}
	    } ;
	}

	public static Predicate ne( final IntFunc arg1, final IntFunc arg2 ) {
	    return new Predicate( "(" + arg1.toString() 
		+ "!=" + arg2.toString() + ")") {

		public boolean evaluate( final FSM fsm, final Input in ) {
		    return arg1.evaluate( fsm, in ) != arg2.evaluate( fsm, in ) ;
		}
	    } ;
	}

	public Base( String name ) { super( name ) ; } 
    }
}

// end of Action.java


