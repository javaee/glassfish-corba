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

package com.sun.corba.se.impl.orbutil.newtimer ;

import java.util.Iterator ;
import java.util.Stack ;
import java.util.NoSuchElementException ;

import java.io.PrintStream ;

import com.sun.corba.se.spi.orbutil.newtimer.Controllable ;
import com.sun.corba.se.spi.orbutil.newtimer.TimerFactory ;
import com.sun.corba.se.spi.orbutil.newtimer.TimerEvent ;
import com.sun.corba.se.spi.orbutil.newtimer.LogEventHandler ;
import com.sun.corba.se.spi.orbutil.newtimer.Timer ;
import com.sun.corba.se.spi.orbutil.newtimer.NamedBase ;

// XXX This needs to be able to properly handle multiple reporting threads!
public class LogEventHandlerImpl extends NamedBase implements LogEventHandler {
    // Default number of entries in data
    private static final int DEFAULT_SIZE = 1000 ;

    // Default increment to number of entries in data
    private static final int DEFAULT_INCREMENT = 1000 ;

    // This is an array for speed.  All data is interleaved here:
    // data[2n] is the id, data[2n+1] is the timestamp for all n >= 0.
    // The array will be resized as needed.
    // id is actually 2*id for enter, 2*id+1 for exit.
    private long[] data ;

    private int size ;
    private int increment ;
    
    // Index of the next free slot in data 
    private int nextFree ;

    LogEventHandlerImpl( TimerFactory factory, String name ) {
	super( factory, name ) ;
	initData( DEFAULT_SIZE, DEFAULT_INCREMENT ) ;
    }

    public synchronized Iterator<TimerEvent> iterator() {
	return new LogEventHandlerIterator( factory(), data, nextFree ) ;
    }

    private void initData( int size, int increment ) {
        this.size = 2*size ;
        this.increment = 2*increment ;
	data = new long[ this.size ] ;
	nextFree = 0 ;
    }

    public void notify( TimerEvent event ) {
	final int id = 2*event.timer().id() + 
	    ((event.type() == TimerEvent.TimerEventType.ENTER) ? 0 : 1) ;
	log( id, event.time() ) ;
    }

    // XXX ignore old compensation idea; do we need it here?
    private synchronized void log( int id, long time ) {
        if (data.length - nextFree < 2) {
            // grow the array
	    int newSize = data.length + 2*increment ;
	    long[] newData = new long[ newSize ] ;
	    System.arraycopy( data, 0, newData, 0, data.length ) ;
	    data = newData ;
	}

        int index = nextFree ;
        nextFree += 2 ;
        
	data[ index ] = id ;
        data[ index + 1 ] = time ;
    }

    public synchronized void clear() {
	initData( size, increment ) ;
    }

    // Class used to maintain a variable-length indent.
    // Useful for displaying hierarchies.
    private class Indent {
	private final int width ;
	private int level ;
	private String rep ;

	public Indent( final int width ) {
	    this.width = width ;
	    level = 0 ;
	    rep = "" ;
	}

	private void update() {
	    int size = level*width ;
	    char[] content = new char[size] ;
	    for (int ctr=0; ctr<size; ctr++) {
		content[ctr] = ' ' ;
	    }
	    rep = new String( content ) ;
	}

	public void in() {
	    level++ ;
	    update() ;
	}

	public void out() {
	    level-- ;
	    update() ;
	}

	public String toString() {
	    return rep ;
	}
    }

    private static final String ENTER_REP = ">> " ;
    private static final String EXIT_REP = "<< " ;

    public void display( PrintStream arg, String msg ) {
        arg.println( "Displaying contents of " + this + ": " + msg ) ;
	final Stack<TimerEvent> stack = new Stack<TimerEvent>() ;
	long startTime = -1 ;
	Indent indent = new Indent( ENTER_REP.length() ) ;
	for (TimerEvent te : this) {
	    if (startTime == -1) {
		startTime = te.time() ;
	    }

	    long relativeTime = (te.time() - startTime)/1000 ;

	    final boolean isEnter = te.type() == TimerEvent.TimerEventType.ENTER ;

	    if (isEnter) {
		arg.printf( "%8d: %s%s%s\n", relativeTime, indent, 
		    isEnter ? ENTER_REP : EXIT_REP,  te.timer().name() ) ;

		// Copy te, otherwise the iterator will overwrite it!
		stack.push( new TimerEvent(te) ) ;
		indent.in() ;
	    } else {
		TimerEvent enterEvent = stack.pop() ;
		indent.out() ;

		String duration = null ;
		if (enterEvent.timer().equals( te.timer() )) {
		    duration = Long.toString( (te.time()-enterEvent.time())/1000 ) ;
		} else {
		    duration = "BAD NESTED EVENT: ENTER was " + enterEvent.timer().name() ;
		}

		arg.printf( "%8d: %s%s%s[%s]\n", relativeTime, indent, 
		    isEnter ? ENTER_REP : EXIT_REP,  te.timer().name(), duration ) ;
	    }
	}
    }
}
