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

package pi.clientrequestinfo;

import com.sun.corba.se.impl.interceptors.*;
import org.omg.PortableInterceptor.*;
import org.omg.CORBA.*;
import ClientRequestInfo.*;

/**
 * Invocation strategy in which three calls are made.  
 * 1. No exception raised
 * 2. SystemException raised
 * 3. UserException raised
 * 4. No exception raised, receive_other is called.
 */
public class InvokeExceptions
    extends InvokeStrategy
{
    public void invoke() throws Exception {
	super.invoke();

	// Invoke send_request then receive_reply
	invokeMethod( "sayHello" );

	// Invoke send_request then receive_exception:
	try {
	    invokeMethod( "saySystemException" );
	}
	catch( UNKNOWN e ) {
	    // We expect this, but no other exception.
	}

	// Invoke send_request then receive_exception (user exception):
	try {
	    invokeMethod( "sayUserException" );
	}
	catch( ExampleException e ) {
	    // We expect these, but no other exceptions.
	}
	catch( UnknownUserException e ) {
	    // We expect these, but no other exceptions.
	    // This occurs in the DII case.
	}

        // Invoke send_request then receive_other:
        SampleClientRequestInterceptor.exceptionRedirectToOther = true;
        try {
            invokeMethod( "saySystemException" );
        }
        catch( UNKNOWN e ) {
            // We expect this, but no other exception.
        }
    }
}
