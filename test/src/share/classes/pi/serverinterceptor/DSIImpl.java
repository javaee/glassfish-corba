/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2000-2007 Sun Microsystems, Inc. All rights reserved.
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

package pi.serverinterceptor;

import org.omg.CORBA.*;
import org.omg.PortableInterceptor.*;
import org.omg.PortableServer.*;

import java.util.*;
import java.io.*;

import ServerRequestInterceptor.*;

/**
 * Servant implementation, shared by ServerRequestDispatcher and POA versions of
 * the servant.  
 */
class DSIImpl {
    // The object to delegate all calls to:
    helloDelegate delegate;

    // The orb to use to make DSI-related calls on
    private ORB orb;

    public DSIImpl( ORB orb, PrintStream out, String symbol ) {
	super();
	this.orb = orb;
	this.delegate = new helloDelegate( out, symbol );
    }

    public void invoke( ServerRequest r ) {
	String opName = r.op_name();
	java.lang.Object result = null;

	if( opName.equals( "sayHello" ) ) {
	    sayHello( r );
	}
	else if( opName.equals( "sayOneway" ) ) {
	    sayOneway( r );
	}
	else if( opName.equals( "saySystemException" ) ) {
	    saySystemException( r );
	}
	else if( opName.equals( "sayUserException" ) ) {
	    sayUserException( r );
	}
	else if( opName.equals( "syncWithServer" ) ) {
	    syncWithServer( r );
	}
    }

    private void sayHello( ServerRequest r ) {
	NVList list = orb.create_list( 0 );
	r.arguments( list );

	String answer = delegate.sayHello();

	// Return result:
	Any result = orb.create_any();
	result.insert_string( answer );
	r.result( result );
    }

    private void sayOneway( ServerRequest r ) {
        NVList list = orb.create_list( 0 );
        r.arguments( list );

        delegate.sayOneway();

        // Return void result:
        Any ret = orb.create_any();
        ret.type( orb.get_primitive_tc( TCKind.tk_void ) );
        r.set_result( ret );
    }
    
    private void saySystemException( ServerRequest r ) {
	// Must call arguments first.  Bug?
	NVList list = orb.create_list( 0 );
	r.arguments( list );

	delegate.saySystemException();
    }

    private void sayUserException( ServerRequest r ) {
	// Must call arguments first.  Bug?
	NVList list = orb.create_list( 0 );
	r.arguments( list );

	try {
	    delegate.sayUserException();
	}
	catch( org.omg.PortableInterceptor.ForwardRequest e ) {
	    Any any = orb.create_any();
	    org.omg.PortableInterceptor.ForwardRequestHelper.insert( any, e );
	    r.except( any );
	}
    }
    
    private void syncWithServer( ServerRequest r ) {
	// Decode exceptionRaised parameter
	NVList nvlist = orb.create_list( 0 );

	Any a1 = orb.create_any();
	a1.type( orb.get_primitive_tc( TCKind.tk_boolean ) );
	nvlist.add_value( "exceptionRaised", a1, ARG_IN.value );
	r.arguments( nvlist );

	boolean exceptionRaised = a1.extract_boolean();

	// Make call to delegate:
	String answer = delegate.syncWithServer( exceptionRaised );

	// Return result:
	Any result = orb.create_any();
	result.insert_string( answer );
	r.result( result );
    }
}

