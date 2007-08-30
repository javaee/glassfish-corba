/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2002-2007 Sun Microsystems, Inc. All rights reserved.
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

package com.sun.corba.se.impl.protocol ;

import javax.rmi.CORBA.Tie;

import org.omg.CORBA.SystemException ;
import org.omg.CORBA.NO_IMPLEMENT ;
import org.omg.CORBA.OBJECT_NOT_EXIST ;
import org.omg.CORBA.CompletionStatus ;
import org.omg.CORBA.portable.InputStream;
import org.omg.CORBA.portable.OutputStream;

import com.sun.corba.se.spi.oa.ObjectAdapter;
import com.sun.corba.se.spi.orb.ORB;

import com.sun.corba.se.spi.protocol.CorbaMessageMediator;

import com.sun.corba.se.impl.logging.ORBUtilSystemException;

import com.sun.corba.se.spi.oa.NullServant ;

public abstract class SpecialMethod {
    public abstract boolean isNonExistentMethod() ;
    public abstract String getName();
    public abstract CorbaMessageMediator invoke(java.lang.Object servant,
						CorbaMessageMediator request,
						byte[] objectId,
						ObjectAdapter objectAdapter);

    public static final SpecialMethod getSpecialMethod(String operation) {
	for(int i = 0; i < methods.length; i++)
	    if (methods[i].getName().equals(operation))
		return methods[i];
	return null;
    }

    static SpecialMethod[] methods = {
	new IsA(),
	new GetInterface(),
	new NonExistent(),
	new NotExistent()
    };
}

class NonExistent extends SpecialMethod {
    public boolean isNonExistentMethod() 
    {
	return true ;
    }

    public String getName() {		// _non_existent
	return "_non_existent";
    }

    public CorbaMessageMediator invoke(java.lang.Object servant,
				       CorbaMessageMediator request,
				       byte[] objectId,
				       ObjectAdapter objectAdapter)
    {
	boolean result = (servant == null) || (servant instanceof NullServant) ;
	CorbaMessageMediator response =
	    request.getProtocolHandler().createResponse(request, null);
	((OutputStream)response.getOutputObject()).write_boolean(result);
	return response;
    }
}

class NotExistent extends NonExistent {
    public String getName() {		// _not_existent
	return "_not_existent";
    }
}

class IsA extends SpecialMethod  {	// _is_a
    public boolean isNonExistentMethod() 
    {
	return false ;
    }

    public String getName() {
	return "_is_a";
    }
    public CorbaMessageMediator invoke(java.lang.Object servant,
				       CorbaMessageMediator request,
				       byte[] objectId,
				       ObjectAdapter objectAdapter)
    {
	if ((servant == null) || (servant instanceof NullServant)) {
	    ORB orb = (ORB)request.getBroker() ;
	    ORBUtilSystemException wrapper = 
		orb.getLogWrapperTable().get_OA_INVOCATION_ORBUtil() ;

	    return request.getProtocolHandler().createSystemExceptionResponse(
		request, wrapper.badSkeleton(), null);
	}
	
	String[] ids = objectAdapter.getInterfaces( servant, objectId );
	String clientId = 
	    ((InputStream)request.getInputObject()).read_string();
	boolean answer = false;
	for(int i = 0; i < ids.length; i++)
	    if (ids[i].equals(clientId)) {
    		answer = true;
    		break;
	    }
	    
	CorbaMessageMediator response =
	    request.getProtocolHandler().createResponse(request, null);
	((OutputStream)response.getOutputObject()).write_boolean(answer);
	return response;
    }
}

class GetInterface extends SpecialMethod  {	// _get_interface
    public boolean isNonExistentMethod() 
    {
	return false ;
    }

    public String getName() {
	return "_interface";
    }
    public CorbaMessageMediator invoke(java.lang.Object servant,
				       CorbaMessageMediator request,
				       byte[] objectId,
				       ObjectAdapter objectAdapter)
    {
	ORB orb = (ORB)request.getBroker() ;
	ORBUtilSystemException wrapper = 
	    orb.getLogWrapperTable().get_OA_INVOCATION_ORBUtil() ;

	if ((servant == null) || (servant instanceof NullServant)) {
	    return request.getProtocolHandler().createSystemExceptionResponse(
		request, wrapper.badSkeleton(), null);
	} else {
	    return request.getProtocolHandler().createSystemExceptionResponse(
		request, wrapper.getinterfaceNotImplemented(), null);
	}
    }
}

// End of file.

