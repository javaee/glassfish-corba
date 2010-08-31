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

package com.sun.corba.se.spi.extension ;

import org.omg.CORBA.Policy ;
import org.omg.CORBA.LocalObject ;
import com.sun.corba.se.spi.orbutil.ORBConstants ;

/** Policy used to implement servant caching optimization in the POA.
* Creating a POA with an instance pol of this policy where 
* pol.getType() &gt; NO_SERVANT_CACHING will cause the servant to be
* looked up in the POA and cached in the LocalClientRequestDispatcher when
* the ClientRequestDispatcher is colocated with the implementation of the
* objref.  This greatly speeds up invocations at the cost of violating the
* POA semantics.  In particular, every request to a particular objref
* must be handled by the same servant.  Note that this is typically the
* case for EJB implementations.
* <p>
* If servant caching is used, there are two different additional 
* features of the POA that are expensive:
* <ol>
* <li>POA current semantics
* <li>Proper handling of POA destroy.
* <ol>
* POA current semantics requires maintaining a ThreadLocal stack of
* invocation information that is always available for POACurrent operations.
* Maintaining this stack is expensive on the timescale of optimized co-located
* calls, so the option is provided to turn it off.  Similarly, causing 
* POA.destroy() calls to wait for all active calls in the POA to complete
* requires careful tracking of the entry and exit of invocations in the POA.
* Again, tracking this is somewhat expensive.
*/
public class ServantCachingPolicy extends LocalObject implements Policy
{
    /** Do not cache servants in the ClientRequestDispatcher.  This will 
     * always support the full POA semantics, including changing the
     * servant that handles requests on a particular objref.
     */
    public static final int NO_SERVANT_CACHING = 0 ;

    /** Perform servant caching, preserving POA current and POA destroy semantics.
    * We will use this as the new default, as the app server is making heavier use
    * now of POA facilities.
    */
    public static final int FULL_SEMANTICS = 1 ;

    /** Perform servant caching, preservent only POA current semantics.
    * At least this level is required in order to support selection of ObjectCopiers
    * for co-located RMI-IIOP calls, as the current copier is stored in 
    * OAInvocationInfo, which must be present on the stack inside the call.
    */
    public static final int INFO_ONLY_SEMANTICS =  2 ;

    /** Perform servant caching, not preserving POA current or POA destroy semantics.
    */
    public static final int MINIMAL_SEMANTICS = 3 ;

    private static ServantCachingPolicy policy = null ;
    private static ServantCachingPolicy infoOnlyPolicy = null ;
    private static ServantCachingPolicy minimalPolicy = null ;

    private int type ;
    
    public String typeToName()
    {
	switch (type) {
	    case FULL_SEMANTICS: 
		return "FULL" ;
	    case INFO_ONLY_SEMANTICS: 
		return "INFO_ONLY" ;
	    case MINIMAL_SEMANTICS: 
		return "MINIMAL" ;
	    default: 
		return "UNKNOWN(" + type + ")" ;
	}
    }

    public String toString() 
    {
	return "ServantCachingPolicy[" + typeToName() + "]" ;
    }

    private ServantCachingPolicy( int type ) 
    {
	this.type = type ;
    }

    public int getType()
    {
	return type ;
    }

    /** Return the default servant caching policy.
    */
    public synchronized static ServantCachingPolicy getPolicy()
    {
	return getFullPolicy() ;
    }

    public synchronized static ServantCachingPolicy getFullPolicy()
    {
	if (policy == null)
	    policy = new ServantCachingPolicy( FULL_SEMANTICS ) ;

	return policy ;
    }

    public synchronized static ServantCachingPolicy getInfoOnlyPolicy()
    {
	if (infoOnlyPolicy == null)
	    infoOnlyPolicy = new ServantCachingPolicy( INFO_ONLY_SEMANTICS ) ;

	return infoOnlyPolicy ;
    }

    public synchronized static ServantCachingPolicy getMinimalPolicy()
    {
	if (minimalPolicy == null)
	    minimalPolicy = new ServantCachingPolicy( MINIMAL_SEMANTICS ) ;

	return minimalPolicy ;
    }

    public int policy_type ()
    {
	return ORBConstants.SERVANT_CACHING_POLICY ;
    }

    public org.omg.CORBA.Policy copy ()
    {
	return this ;
    }

    public void destroy ()
    {
	// NO-OP
    }
}
