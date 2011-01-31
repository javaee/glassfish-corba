/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright (c) 1997-2011 Oracle and/or its affiliates. All rights reserved.
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

package com.sun.corba.se.spi.oa ;

import org.omg.CORBA.Policy ;

import org.omg.PortableInterceptor.ObjectReferenceTemplate ;
import org.omg.PortableInterceptor.ObjectReferenceFactory ;

import com.sun.corba.se.spi.orb.ORB ;

import com.sun.corba.se.spi.ior.IORTemplate ;
import org.glassfish.gmbal.Description;
import org.glassfish.gmbal.ManagedAttribute;
import org.glassfish.gmbal.ManagedObject;

// REVISIT: What should the order be?  enter/push...pop/exit?

/** ObjectAdapter represents the abstract model of an object
* adapter that was introduced by ORT.  This means that all
* object adapters must:
* <UL>
* <LI>Have an ORB</LI>
* <LI>Have a name</LI>
* <LI>Have an adapter manager (represented by an ID)</LI>
* <LI>Have an adapter template</LI>
* <LI>Support getting and setting their ObjectReferenceFactory</LI>
* <LI>Provide access to their current state</LI>
* <LI>Support adding components to their profiles expressed in the adapter template</LI>
* </UL>
* Other requirements:
* <UL>
* <LI>All object adapters must invoke ORB.AdapterCreated when they are created.
* </LI>
* <LI>All adapter managers must invoke ORB.AdapterManagerStateChanged when
* their state changes, mapping the internal state to an ORT state.</LI>
* <LI>AdapterStateChanged must be invoked (from somewhere) whenever
* an adapter state changes that is not due to an adapter manager state change.</LI>
* </UL>
* <P>
* Object adapters must also provide mechanisms for:
* <UL>
* <LI>Managing object reference lifecycle</LI>
* <LI>Controlling how servants are associated with object references</LI>
* <LI>Manage the state of the adapter, if the adapter desires to implement such mechanisms</LI>
* </UL>
* Such mechanisms are all object adapter specific, and so we do not attempt to
* create general APIs for these functions here.  The object adapter itself
* must provide these APIs directly to the user, and they do not affect the rest of the
* ORB.  This interface basically makes it possible to plug any object adapter into the
* ORB and have the OA work propertly with portable interceptors, and also have requests
* dispatched properly to the object adapter.
* <P>
* The basic function of an ObjectAdapter is to map object IDs to servants and to support
* the dispatch operation of the subcontract, which dispatches requests to servants.
* This is the purpose of the getInvocationServant method.  In addition, ObjectAdapters must be
* able to change state gracefully in the presence of executing methods.  This
* requires the use of the enter/exit methods.  Finally, ObjectAdapters often
* require access to information about requests.  This is accomodated through the
* OAInvocationInfo class and the thread local stack maintained by push/pop/peekInvocationInfo
* on the ORB.
* <P>
* To be useful, this dispatch cycle must be extremely efficient.  There are several
* scenarios that matter:
* <ol>
* <li>A remote invocation, where the dispatch is handled in the server subcontract.</li>
* <li>A local invocation, where the dispatch is handled in the client subcontract.</li>
* <li>A cached local invocation, where the servant is cached when the IOR is established
* for the client subcontract, and the dispatch is handled in the client subcontract
* to the cached subcontract.<li>
* </ol>
* <p>
* Each of these 3 cases is handled a bit differently.  On each request, assume as known
* ObjectId and ObjectAdapterId, which can be obtained from the object key.  
* The ObjectAdaptorFactory is available in the subcontract registry, where it is
* registered under the subcontract ID.  The Subcontract ID is also available in the
* object key.
* <ol>
* <li>The remote pattern:
*   <ol>
*   <li>oa = oaf.find( oaid )</li>
*   <li>oa.enter()</li>
*   <li>info = oa.makeInvocationInfo( oid )</li> 
*   <li>info.setOperation( operation )</li>
*   <li>push info</li>
*   <li>oa.getInvocationServant( info )</li>
*   <li>sreq.setExecuteReturnServantInResponseConstructor( true )</li>
*   <li>dispatch to servant</li>
*   <li>oa.returnServant()</li>
*   <li>oa.exit()</li>
*   <li>pop info</li>
*   <ol>
* </li>
* REVISIT: Is this the required order for exit/pop?  Can they be nested instead?
* Note that getInvocationServant and returnServant may throw exceptions.  In such cases,
* returnServant, exit, and pop must be called in the correct order.
* <li>The local pattern:  
*   <ol>
*   <li>oa = oaf.find( oaid )</li>
*   <li>oa.enter()</li>
*   <li>info = oa.makeInvocationInfo( oid )</li> 
*   <li>info.setOperation( operation )</li>
*   <li>push info</li>
*   <li>oa.getInvocationServant( info )</li>
*   <li>dispatch to servant</li>
*   <li>oa.returnServant()</li>
*   <li>oa.exit()</li>
*   <li>pop info</li>
*   <ol>
* </li>
* This is the same as the remote case, except that setExecuteReturnServantInResponseConstructor
* is not needed (or possible, since there is no server request).
* <li>The fast local pattern: When delegate is constructed, 
*    first extract ObjectKey from IOR in delegate,
*    then get ObjectId, ObjectAdapterId, and ObjectAdapterFactory (oaf). Then:
*    <ol>
*    <li>oa = oaf.find( oaid )</li>
*    <li>info = oa.makeInvocationInfo( oid ) (note: no operation!)</li> 
*    <li>push info (needed for the correct functioning of getInvocationServant)</li>
*    <li>oa.getInvocationServant( info )</li>
*    <li>pop info
*    </ol>
*    The info instance (which includes the Servant) is cached in the client subcontract.
*    <p>Then, on each invocation:</p>
*    <ol>
*    <li>newinfo = copy of info (clone)</li>
*    <li>info.setOperation( operation )</li>
*    <li>push newinfo</li>
*    <li>oa.enter()</li>
*    <li>dispatch to servant</li>
*    <li>oa.returnServant()</li>  
*    <li>oa.exit()</li>
*    <li>pop info</li>
*    </ol>
* </li>
* </ol>
*/
@ManagedObject
@Description( "ObjectAdapter used to dispatch requests and manage servants")
public interface ObjectAdapter 
{
    ////////////////////////////////////////////////////////////////////////////
    // Basic methods for supporting interceptors
    ////////////////////////////////////////////////////////////////////////////

    /** Returns the ORB associated with this adapter.
    */
    ORB getORB() ;

    Policy getEffectivePolicy( int type ) ;

    /** Returns the IOR template of this adapter.  The profiles
    * in this template may be updated only during the AdapterCreated call.
    * After that call completes, the IOR template must be made immutable.
    * Note that the server ID, ORB ID, and adapter name are all available
    * from the IOR template.
    */
    @ManagedAttribute
    @Description( "The IORTemplate used to create Object References")
    IORTemplate getIORTemplate() ;

    ////////////////////////////////////////////////////////////////////////////
    // Methods needed to support ORT.
    ////////////////////////////////////////////////////////////////////////////

    /** Return the ID of the AdapterManager for this object adapter.
    */
    @ManagedAttribute
    @Description( "The identifier for the AdapterManager that manages this ObjectAdapter")
    int getManagerId() ;

    /** Return the current state of this object adapter (see 
    * org.omg.PortableInterceptors for states.
    */
    short getState() ;

    @ManagedAttribute
    @Description( "The adapter template" )
    ObjectReferenceTemplate getAdapterTemplate() ;

    @ManagedAttribute
    @Description( "The current object reference factory" )
    ObjectReferenceFactory getCurrentFactory() ;

    /** Change the current factory.  This may only be called during the
    * AdapterCreated call.
    */
    void setCurrentFactory( ObjectReferenceFactory factory ) ;

    ////////////////////////////////////////////////////////////////////////////
    // Methods required for dispatching to servants
    ////////////////////////////////////////////////////////////////////////////

    /** Get the servant corresponding to the given objectId, if this is supported.
     * This method is only used for models where the servant is an ObjectImpl,
     * which allows the servant to be used directly as the stub.  This allows an object 
     * reference to be replaced by its servant when it is unmarshalled locally.  
     * Such objects are not ORB mediated.
     */
    org.omg.CORBA.Object getLocalServant( byte[] objectId ) ;

    /** Get the servant for the request given by the parameters.
    * info must contain a valid objectId in this call.
    * The servant is set in the InvocationInfo argument that is passed into 
    * this call.  
    * @param info is the InvocationInfo object for the object reference
    * @exception ForwardException (a runtime exception) is thrown if the request 
    * is to be handled by a different object reference.
    */
    void getInvocationServant( OAInvocationInfo info ) ;

    /** enter must be called before each request is invoked on a servant.
      * @exception OADestroyed is thrown when an OA has been destroyed, which 
      * requires a retry in the case where an AdapterActivator is present.
      */
    void enter( ) throws OADestroyed ;

    /** exit must be called after each request has been completed.  If enter
    * is called and completes normally, there must always be a corresponding exit.
    * If enter throw OADestroyed, exit must NOT be called.
    */
    void exit( ) ;

    /** Must be called every time getInvocationServant is called after
     * the request has completed.
     */
    public void returnServant() ;

    /** Create an instance of InvocationInfo that is appropriate for this 
    * Object adapter.
    */
    OAInvocationInfo makeInvocationInfo( byte[] objectId ) ;

    /** Return the most derived interface for the given servant and objectId.
    */
    String[] getInterfaces( Object servant, byte[] objectId ) ;

    public boolean isNameService();

    public void setNameService( boolean flag ) ;
} 
