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

package com.sun.corba.se.impl.orbutil.newtimer ;

import java.util.Iterator ;
import java.util.NoSuchElementException ;

import com.sun.corba.se.spi.orbutil.newtimer.Controllable ;
import com.sun.corba.se.spi.orbutil.newtimer.TimerEvent ;
import com.sun.corba.se.spi.orbutil.newtimer.Timer ;
import com.sun.corba.se.spi.orbutil.newtimer.TimerFactory ;
    
public class LogEventHandlerIterator implements Iterator<TimerEvent> {
    private int current = 0 ;
    private TimerEvent entry = null ;

    private final TimerFactory factory ;
    private final long[] data ;
    private final int nextFree ;

    public LogEventHandlerIterator( TimerFactory factory, long[] data, 
	int nextFree ) {

	this.factory = factory ;
	this.data = data ;
	this.nextFree = nextFree ;
    }

    public void remove() {
	throw new UnsupportedOperationException() ;
    }

    public boolean hasNext() {
	return current < nextFree ;
    }

    public TimerEvent next() {
	if (hasNext()) {
	    long elem = data[current] ;

	    TimerEvent.TimerEventType etype = 
		((elem & 1) == 1) ? 
		    TimerEvent.TimerEventType.EXIT :
		    TimerEvent.TimerEventType.ENTER ;

	    int id = (int)(elem >> 1) ;

	    Controllable con = factory.getControllable( id ) ;
	    if (!(con instanceof Timer))
		throw new IllegalStateException( "Controllable id must be Timer" ) ;
	    Timer timer = Timer.class.cast( con ) ; 

	    if (entry == null)
		entry = new TimerEvent( timer, etype, data[current+1] ) ;
	    else 
		entry.update( timer, etype, data[current+1] ) ;

	    current += 2 ;
	    
	    return entry ;
	} else {
	    throw new NoSuchElementException() ;
	}
    }
}
