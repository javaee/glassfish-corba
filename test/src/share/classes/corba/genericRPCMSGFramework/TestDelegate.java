/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1996-2007 Sun Microsystems, Inc. All rights reserved.
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

package corba.genericRPCMSGFramework;

import java.util.Iterator;

import org.omg.CORBA.Context;
import org.omg.CORBA.ContextList;
import org.omg.CORBA.ExceptionList;
import org.omg.CORBA.NamedValue;
import org.omg.CORBA.NVList;
import org.omg.CORBA.Request;
import org.omg.CORBA.SystemException;
import org.omg.CORBA.TypeCode;
import org.omg.CORBA.portable.ApplicationException;
import org.omg.CORBA.portable.RemarshalException;
import org.omg.CORBA.portable.InputStream;
import org.omg.CORBA.portable.OutputStream;
import org.omg.CORBA.portable.ServantObject;

import com.sun.corba.se.pept.broker.Broker;
import com.sun.corba.se.pept.protocol.ClientInvocationInfo;
import com.sun.corba.se.pept.protocol.ClientRequestDispatcher;
import com.sun.corba.se.pept.encoding.InputObject;
import com.sun.corba.se.pept.encoding.OutputObject;
import com.sun.corba.se.pept.transport.ContactInfo;
import com.sun.corba.se.pept.transport.ContactInfoList;
import com.sun.corba.se.pept.transport.ContactInfoListIterator;

import com.sun.corba.se.spi.ior.IOR;
import com.sun.corba.se.spi.protocol.CorbaClientDelegate;

import com.sun.corba.se.spi.orb.ORB;

// implements com.sun.corba.se.impl.core.ClientDelegate
// so RMI-IIOP StubAdapter.isLocal can call ClientDelegate.useLocalInvocation.

/**
 * @author Harold Carr
 */
public class TestDelegate extends CorbaClientDelegate
{
    private org.omg.CORBA_2_3.portable.Delegate clientDelegate;
    private ORB corbaBroker;
    private ContactInfoList contactInfoList;

    public TestDelegate(ORB corbaBroker, 
			com.sun.corba.se.spi.protocol.CorbaClientDelegate clientDelegate,
			ContactInfoList contactInfoList) 
    {
	this.corbaBroker = corbaBroker;
	this.clientDelegate = (org.omg.CORBA_2_3.portable.Delegate) 
	    clientDelegate;
	this.contactInfoList = contactInfoList;
    }
    
    //
    // framework.subcontract.Delegate
    //

    public Broker getBroker()
    {
	return corbaBroker;
    }

    public ContactInfoList getContactInfoList()
    {
	return contactInfoList;
    }

    //
    // CORBA_2_3.portable.Delegate
    //
    
    public OutputStream request(org.omg.CORBA.Object self, 
				String operation, 
				boolean responseExpected) 
    {
	ClientInvocationInfo invocationInfo = 
	    corbaBroker.createOrIncrementInvocationInfo();
	Iterator contactInfoListIterator =
	    invocationInfo.getContactInfoListIterator();
	if (contactInfoListIterator == null) {
	    contactInfoListIterator = contactInfoList.iterator();
	    invocationInfo.setContactInfoListIterator(contactInfoListIterator);
	}
	ContactInfo contactInfo = (ContactInfo) contactInfoListIterator.next();
	ClientRequestDispatcher clientRequestDispatcher = null;
	try {
	    Object o = contactInfo.getClientRequestDispatcher();
	    System.out.println(o.getClass().getInterfaces()[0]);
	    clientRequestDispatcher = (ClientRequestDispatcher) o;
	} catch (Throwable t) {
	    t.printStackTrace(System.out);
	}
	invocationInfo.setClientRequestDispatcher(clientRequestDispatcher);
	return (OutputStream)
	    clientRequestDispatcher.beginRequest(self, operation,
					!responseExpected, contactInfo);
    }
    
    public InputStream invoke(org.omg.CORBA.Object self, OutputStream output)
	throws
	    ApplicationException,
	    RemarshalException 
    {
	ClientInvocationInfo invocationInfo = corbaBroker.getInvocationInfo();
	return (InputStream)
	    ((ClientRequestDispatcher)invocationInfo.getClientRequestDispatcher())
	    .marshalingComplete((Object)self, (OutputObject)output);
    }
    
    public void releaseReply(org.omg.CORBA.Object self, InputStream input) 
    {
        // XREVISIT - This was here to do cancel request final fragment
	// and interceptor cleanup.  But these are not exercised in
	// this prototype (and cause interceptor stack underflow if
	// left in.
	//clientDelegate.releaseReply(null, null);

        ClientRequestDispatcher subcontract = (ClientRequestDispatcher)
            corbaBroker.getInvocationInfo().getClientRequestDispatcher();
        subcontract.endRequest(corbaBroker, self, (InputObject) input);

	// REVISIT: probably needs InputObject parameter to deal
	// with fragment problems or early replies.
	corbaBroker.releaseOrDecrementInvocationInfo();
    }

    public org.omg.CORBA.Object get_interface_def(org.omg.CORBA.Object self) 
    {
	return clientDelegate.get_interface_def(self);
    }
    
    public org.omg.CORBA.Object duplicate(org.omg.CORBA.Object obj) 
    {
	return clientDelegate.duplicate(obj);
    }
    
    public void release(org.omg.CORBA.Object obj) 
    {
    }
    
    public boolean is_a(org.omg.CORBA.Object obj, String repository_id) 
    {
	return clientDelegate.is_a(obj, repository_id);
    }
    
    public boolean non_existent(org.omg.CORBA.Object obj) 
    {
	return clientDelegate.non_existent(obj);
    }
    
    public boolean is_equivalent(org.omg.CORBA.Object obj, org.omg.CORBA.Object other) 
    {
	return clientDelegate.is_equivalent(obj, other);
    }
    
    public int hash(org.omg.CORBA.Object obj, int max) 
    {
	return clientDelegate.hash(obj, max);
    }
    
    public Request request(org.omg.CORBA.Object obj, String operation) 
    {
	return clientDelegate.request(obj, operation);
    }
    
    public Request create_request(org.omg.CORBA.Object obj, Context ctx, String operation, NVList arg_list, NamedValue result) 
    {
	return clientDelegate.create_request(obj, ctx, operation, arg_list, result);
    }
    
    public Request create_request(org.omg.CORBA.Object obj, Context ctx, String operation, NVList arg_list, NamedValue result, ExceptionList exclist, ContextList ctxlist) 
    {
	return clientDelegate.create_request(obj, ctx, operation, arg_list, result, exclist, ctxlist);
    }
    
    public org.omg.CORBA.ORB orb(org.omg.CORBA.Object obj) 
    {
	return clientDelegate.orb( obj);
    }
    
    public org.omg.CORBA.Policy get_policy(org.omg.CORBA.Object self, int policy_type) 
    {
	return clientDelegate.get_policy(self, policy_type);
    }
    
    public org.omg.CORBA.DomainManager[] get_domain_managers(org.omg.CORBA.Object self) 
    {
	return clientDelegate.get_domain_managers(self);
    }
    
    public org.omg.CORBA.Object set_policy_override(org.omg.CORBA.Object self, org.omg.CORBA.Policy[] policies, org.omg.CORBA.SetOverrideType set_add) 
    {
	return clientDelegate.set_policy_override(self, policies, set_add);
    }
    
    public boolean is_local(org.omg.CORBA.Object self) 
    {
	return clientDelegate.is_local(self);
    }
    
    public ServantObject servant_preinvoke(org.omg.CORBA.Object self, String operation, Class expectedType) 
    {
	return clientDelegate.servant_preinvoke(self, operation, expectedType);
    }
    
    public void servant_postinvoke(org.omg.CORBA.Object self, ServantObject servant) 
    {
    }
    
    public String toString(org.omg.CORBA.Object self) 
    {
	return clientDelegate.toString(self);
    }
    
    public int hashCode(org.omg.CORBA.Object self) 
    {
	return clientDelegate.hashCode(self);
    }
    
    public boolean equals(org.omg.CORBA.Object self, java.lang.Object obj) 
    {
	return clientDelegate.equals(self, obj);
    }
    
    public String get_codebase(org.omg.CORBA.Object self) 
    {
	return clientDelegate.get_codebase(self);
    }

    ////////////////////////////////////////////////////    
    //
    // com.sun.corba.se.impl.core.ClientDelegate
    //

    public boolean useLocalInvocation( org.omg.CORBA.Object self )
    {
	return false;
    }

    public int getId()
    {
	return -1;
    }

    public IOR getIOR()
    {
	return null;
    }

    public void setIOR(IOR ior)
    {
    }

    public IOR getLocatedIOR()
    {
	return null;
    }

    public void setLocatedIOR(IOR ior)
    {
    }

    public short getAddressingDisposition() 
    {
	return 0 ;
    }

    public void setAddressingDisposition( short ad )
    {
    }

    public void setServant( java.lang.Object servant )
    {
    }

    public void unexport()
    {
    }
}

// End of file.

