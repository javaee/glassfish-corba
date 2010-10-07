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

package corba.simpledynamic;

import org.omg.PortableInterceptor.ORBInitializer ;
import org.omg.PortableInterceptor.ClientRequestInterceptor ;
import org.omg.PortableInterceptor.ClientRequestInfo ;
import org.omg.PortableInterceptor.ORBInitInfo ;
import org.omg.PortableInterceptor.ForwardRequest ;

import org.omg.PortableInterceptor.ORBInitInfoPackage.DuplicateName ;

import org.omg.CORBA.LocalObject ;
import org.omg.CORBA.SystemException ;
import org.omg.CORBA.INTERNAL ;
import org.omg.CORBA.COMM_FAILURE ;
import org.omg.CORBA.ORB ;
import org.omg.CORBA.Any ;

import com.sun.corba.se.impl.orbutil.ORBUtility ;

public class InterceptorTester extends LocalObject implements
    ORBInitializer, ClientRequestInterceptor {

    public static InterceptorTester theTester = null ;
    public static boolean verbose = false ;

    private int errors = 0 ;
    private boolean exceptionExpected = false ;

    public InterceptorTester() {
	theTester = this ;
    }

    public void clear() {
	errors = 0 ;
	exceptionExpected = false ;
    }
    
    public int getErrors() {
	return errors ;
    }

    public void setExceptionExpected() {
	exceptionExpected = true ;
    }
    
    private void msg( String msg ) {
	if (verbose) {
	    System.out.println( "+++InterceptorTester: " + msg ) ;
	}
    }

    private void error( String msg ) {
	msg( "ERROR: " + msg ) ;
	errors++ ;
    }

    public void pre_init( ORBInitInfo info ) {
    }

    public void post_init( ORBInitInfo info ) {
	try {
	    info.add_client_request_interceptor( this ) ;
	} catch (DuplicateName exc) {
	    INTERNAL internal = new INTERNAL() ;
	    internal.initCause( exc ) ;
	    throw internal ;
	}
    }

    public String name() {
	return "ClientInterceptor" ;
    }

    public void destroy() {
    }

    public void send_request( ClientRequestInfo ri ) throws ForwardRequest {
	msg( "send_request called" ) ;
    }

    public void send_poll( ClientRequestInfo ri ) {
	error( "send_poll should not be called" ) ;
    }

    public void receive_reply( ClientRequestInfo ri ) {
	if (exceptionExpected) {
	    error( "normal completion when exception expected!" ) ;
	} else {
	    msg( "normal completion" ) ;
	}
    }

    public void receive_exception( ClientRequestInfo ri ) throws ForwardRequest {
	if (!exceptionExpected) {
	    error( "exception when normal completion expected!" ) ;
	} else {
	    msg( "expected exception" ) ;
	}

	Any exception = ri.received_exception() ;
	SystemException sysex = ORBUtility.extractSystemException( exception ) ;

	if (!(sysex instanceof COMM_FAILURE)) {
	    error( "Expected COMM_FAILURE, got " + sysex ) ;
	} else {
	    msg( "expected COMM_FAILURE" ) ;
	}

	sysex.printStackTrace() ;
    }

    public void receive_other( ClientRequestInfo ri ) throws ForwardRequest {
	error( "receive_other should not be called" ) ;
    }
}
