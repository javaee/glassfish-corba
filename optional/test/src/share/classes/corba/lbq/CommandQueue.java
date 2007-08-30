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
package corba.lbq ;

import com.sun.corba.se.spi.orbutil.generic.UnaryFunction ;

public class CommandQueue {
    private Command end ; // always points to sink
    private Command head ; // points to last command before sink
    private Command tail ; // points to first command to evaluate

    public interface Event { } 

    public interface Command extends UnaryFunction<Event, Command> { 
	void setNext( Command next ) ;
    }
  
    public static abstract class CommandBase implements Command {
	protected Command next = null ;

	public void setNext( Command next ) {
	    this.next = next ;
	}

	public Command evaluate( Event ev ) {
	    action( ev ) ;
	    return next ;
	}

	protected void action( Event ev ) {
	}
    }

    private static class Sink extends CommandBase {
	public Sink() {
	    setNext( this ) ;
	}
    }

    public class Delay extends CommandBase {
	private int count ;

	/** Do nothing the first numEvents calls.
	 */
	public Delay( int numEvents ) {
	    this.count = numEvents ;
	}

	public Command evaluate( Event ev ) {
	    count-- ;
	    if (count == 0)
		return next ;
	    return this ;
	}
    }

    public CommandQueue() {
	end = new Sink() ;
	tail = end ;
	head = end ;
    }

    private void doAdd( Command cmd ) {
	if (tail == end) {
	    head = cmd ;
	} else {
	    tail.setNext( cmd ) ;
	}

	tail = cmd ;
	cmd.setNext( end ) ;
    }

    /** Add this command to the queue.  This command must be
     * triggered (by event()) count times before it calls cmd.
     * If count == 0, the first event() will execute cmd.
     */
    public void add( int count, Command cmd ) {
	if (count > 0)
	    doAdd( new Delay( count ) ) ;
	doAdd( cmd ) ;
    }

    public void event( Event ev ) {
	head = head.evaluate( ev ) ;
	if (head == end)
	    tail = end ;
    }

    private static void p( String msg ) {
	System.out.println( msg ) ;
    }

    private static class Display extends CommandBase {
	String msg ;

	public Display( String msg ) {
	    this.msg = msg ;
	}

	public void action( Event ev ) {
	    p( msg ) ;
	}
    }

    public static void main( String[] args ) {
	p( "Testing CommandQueue" ) ;
	CommandQueue cq = new CommandQueue() ;
	Event ev = new Event() {} ;
	cq.event( ev ) ; // should do nothing
	cq.event( ev ) ; // should do nothing

	Command d1 = new Display( "Display 1" ) ;	
	Command d2 = new Display( "Display 2" ) ;	
	Command d3 = new Display( "Display 3" ) ;	
	Command d4 = new Display( "Display 4" ) ;	

	cq.add( 0, d1 ) ;
	cq.add( 5, d2 ) ;
	cq.add( 8, d3 ) ;
	cq.add( 3, d4 ) ;

	for (int ctr=0; ctr<25; ctr++) {
	    p( "Event " + ctr ) ;
	    cq.event( ev ) ;
	}

	p( "Add to queue while running" ) ;

	cq.add( 0, d1 ) ;
	cq.add( 5, d2 ) ;
	cq.add( 8, d3 ) ;

	for (int ctr=0; ctr<11; ctr++) {
	    p( "Event " + ctr ) ;
	    cq.event( ev ) ;
	}

	cq.add( 3, d4 ) ;

	for (int ctr=11; ctr<25; ctr++) {
	    p( "Event " + ctr ) ;
	    cq.event( ev ) ;
	}

    }
}
