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
package corba.dynamicrmiiiop ; 

import org.omg.CORBA.portable.ApplicationException ;
import org.omg.CORBA_2_3.portable.InputStream ;
import org.omg.CORBA_2_3.portable.OutputStream ;

import com.sun.corba.se.impl.encoding.CDROutputObject ;
import com.sun.corba.se.impl.encoding.EncapsInputStream ;
import com.sun.corba.se.impl.encoding.EncapsOutputStream ;
import com.sun.corba.se.impl.encoding.CDRInputObject ;


import com.sun.corba.se.spi.orb.ORB ;

public class TestTransport {
    private ORB orb ;

    public TestTransport( ORB orb ) 
    {
	this.orb = orb ;
    }

    private static final int REQUEST_HEADER = 24 ;
    private static final int NORMAL_REPLY_HEADER = 30 ;
    private static final int EXCEPTION_REPLY_HEADER = 36 ;

    public InputStream getInputStream( OutputStream os ) 
    {
	CDROutputObject cos = (CDROutputObject)os ;
	byte[] data = cos.toByteArray() ;
	return new EncapsInputStream( orb, data, data.length ) ;
    }

    public OutputStream makeRequest( String mname )
    {
	OutputStream result = new EncapsOutputStream( orb ) ;
	result.write_long( REQUEST_HEADER ) ;
	result.write_string( mname ) ;
	return result ;
    }

    public OutputStream makeNormalReply() 
    {
	OutputStream result = new EncapsOutputStream( orb ) ;
	result.write_long( NORMAL_REPLY_HEADER ) ;
	return result ;
    }

    public OutputStream makeExceptionReply()
    {
	OutputStream result = new EncapsOutputStream( orb ) ;
	result.write_long( EXCEPTION_REPLY_HEADER ) ;
	return result ;
    }
    
    public String readRequestHeader( InputStream is )
    {
	int header = is.read_long() ;
	if (header != REQUEST_HEADER)
	    throw new RuntimeException( 
		"InputStream does not begin with REQUEST_HEADER" ) ;
	return is.read_string() ;
    }

    // Throw ApplicationException.  Note that this
    // must leave the stream ready to read the repo id
    // string, so we need to use mark/reset here.
    // This code is taken from CorbaClientRequestDispatcher.
    private String peekUserExceptionId(CDRInputObject inputObject)
    {
	// REVISIT - need interface for mark/reset
        inputObject.mark(Integer.MAX_VALUE);
        String result = inputObject.read_string();
	inputObject.reset();
        return result;
    }                     

    public void readReplyHeader( InputStream is ) 
	throws ApplicationException
    {
	int header = is.read_long() ;
	if (header == NORMAL_REPLY_HEADER) {
	    // NO-OP
	} else if (header == EXCEPTION_REPLY_HEADER) {
	    String id = peekUserExceptionId( (CDRInputObject)is ) ;
	    throw new ApplicationException( id, is ) ;
	} else {
	    // error
	    throw new RuntimeException( "Bad reply header in test" ) ;
	}
    }
}
