/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2002-2010 Sun Microsystems, Inc. All rights reserved.
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

package com.sun.corba.se.impl.interceptors;

import java.util.Map ;
import java.util.HashMap ;

import org.omg.CORBA.Any;
import org.omg.CORBA.BAD_INV_ORDER;
import org.omg.CORBA.Context;
import org.omg.CORBA.ContextList;
import org.omg.CORBA.CTX_RESTRICT_SCOPE;
import org.omg.CORBA.ExceptionList;
import org.omg.CORBA.NamedValue;
import org.omg.CORBA.NVList;
import org.omg.CORBA.Object;
import org.omg.CORBA.Policy;
import org.omg.CORBA.SystemException;
import org.omg.CORBA.TypeCode;
import org.omg.CORBA.portable.ApplicationException;
import com.sun.corba.se.spi.servicecontext.ServiceContexts;

import org.omg.IOP.ServiceContext;
import org.omg.IOP.TaggedProfile;
import org.omg.IOP.TaggedComponent;
import org.omg.Dynamic.Parameter;
import org.omg.PortableInterceptor.ClientRequestInfo;
import org.omg.PortableInterceptor.LOCATION_FORWARD;
import org.omg.PortableInterceptor.SUCCESSFUL;
import org.omg.PortableInterceptor.SYSTEM_EXCEPTION;
import org.omg.PortableInterceptor.TRANSPORT_RETRY;
import org.omg.PortableInterceptor.USER_EXCEPTION;


import com.sun.corba.se.spi.ior.IOR;
import com.sun.corba.se.spi.ior.iiop.IIOPProfileTemplate;
import com.sun.corba.se.spi.orb.ORB;
import com.sun.corba.se.spi.protocol.CorbaMessageMediator;
// 6763340
import com.sun.corba.se.spi.protocol.RetryType;
import com.sun.corba.se.spi.transport.CorbaContactInfo;
import com.sun.corba.se.spi.transport.CorbaContactInfoList;
import com.sun.corba.se.spi.transport.CorbaContactInfoListIterator;

import com.sun.corba.se.impl.orbutil.ORBUtility;
import com.sun.corba.se.impl.protocol.CorbaInvocationInfo;

/**
 * Implementation of the ClientRequestInfo interface as specified in
 * orbos/99-12-02 section 5.4.2.
 */
public final class ClientRequestInfoImpl 
    extends RequestInfoImpl 
    implements ClientRequestInfo 
{

    // The available constants for startingPointCall
    static final int CALL_SEND_REQUEST = 0;
    static final int CALL_SEND_POLL = 1;
    
    // The available constants for endingPointCall
    static final int CALL_RECEIVE_REPLY = 0;
    static final int CALL_RECEIVE_EXCEPTION = 1;
    static final int CALL_RECEIVE_OTHER = 2;

    //////////////////////////////////////////////////////////////////////
    //
    // NOTE: IF AN ATTRIBUTE IS ADDED, PLEASE UPDATE RESET();
    //
    //////////////////////////////////////////////////////////////////////
    
    // The current retry request status.  True if this request is being 
    // retried and this info object is to be reused, or false otherwise.
    private RetryType retryRequest;
    
    // The number of times this info object has been (re)used.  This is
    // incremented every time a request is retried, and decremented every
    // time a request is complete.  When this reaches zero, the info object
    // is popped from the ClientRequestInfoImpl ThreadLocal stack in the ORB.
    private int entryCount = 0;

    // The RequestImpl is set when the call is DII based.
    // The DII query calls like ParameterList, ExceptionList,
    // ContextList will be delegated to RequestImpl.
    private org.omg.CORBA.Request request;

    // Sources of client request information
    private boolean diiInitiate;
    private CorbaMessageMediator messageMediator;

    // Cached information:
    private org.omg.CORBA.Object cachedTargetObject;
    private org.omg.CORBA.Object cachedEffectiveTargetObject;
    private Parameter[] cachedArguments;
    private TypeCode[] cachedExceptions;
    private String[] cachedContexts;
    private String[] cachedOperationContext;
    private String cachedReceivedExceptionId;
    private Any cachedResult;
    private Any cachedReceivedException;
    private TaggedProfile cachedEffectiveProfile;
    // key = Integer, value = IOP.ServiceContext.
    private Map<Integer, org.omg.IOP.ServiceContext> cachedRequestServiceContexts;
    private Map<Integer,org.omg.IOP.ServiceContext> cachedReplyServiceContexts;
    private Map<Integer,TaggedComponent[]> cachedEffectiveComponents;


    private boolean piCurrentPushed;
    
    //////////////////////////////////////////////////////////////////////
    //
    // NOTE: IF AN ATTRIBUTE IS ADDED, PLEASE UPDATE RESET();
    //
    //////////////////////////////////////////////////////////////////////
    
    /**
     * Reset the info object so that it can be reused for a retry,
     * for example.
     */
    @Override
    void reset() {
        if (myORB.interceptorDebugFlag) {
            dputil.enter( "reset" ) ;
        }

        try {
            super.reset();

            // Please keep these in the same order that they're declared above.
            
            // 6763340
            retryRequest = RetryType.NONE;

            // Do not reset entryCount because we need to know when to pop this
            // from the stack.

            request = null;
            diiInitiate = false;
            messageMediator = null;

            // Clear cached attributes:
            cachedTargetObject = null;
            cachedEffectiveTargetObject = null;
            cachedArguments = null;
            cachedExceptions = null;
            cachedContexts = null;
            cachedOperationContext = null;
            cachedReceivedExceptionId = null;
            cachedResult = null;
            cachedReceivedException = null;
            cachedEffectiveProfile = null;
            cachedRequestServiceContexts = null;
            cachedReplyServiceContexts = null;
            cachedEffectiveComponents = null;

            piCurrentPushed = false;

            startingPointCall = CALL_SEND_REQUEST;
            endingPointCall = CALL_RECEIVE_REPLY;

        } finally {
            if (myORB.interceptorDebugFlag) {
                dputil.exit() ;
            }
        }
    }
    
    /*
     **********************************************************************
     * Access protection
     **********************************************************************/
    
    // Method IDs for all methods in ClientRequestInfo.  This allows for a 
    // convenient O(1) lookup for checkAccess().
    private static final int MID_TARGET                      = MID_RI_LAST + 1;
    private static final int MID_EFFECTIVE_TARGET            = MID_RI_LAST + 2;
    private static final int MID_EFFECTIVE_PROFILE           = MID_RI_LAST + 3;
    private static final int MID_RECEIVED_EXCEPTION          = MID_RI_LAST + 4;
    private static final int MID_RECEIVED_EXCEPTION_ID       = MID_RI_LAST + 5;
    private static final int MID_GET_EFFECTIVE_COMPONENT     = MID_RI_LAST + 6;
    private static final int MID_GET_EFFECTIVE_COMPONENTS    = MID_RI_LAST + 7;
    private static final int MID_GET_REQUEST_POLICY          = MID_RI_LAST + 8;
    private static final int MID_ADD_REQUEST_SERVICE_CONTEXT = MID_RI_LAST + 9;
    
    // ClientRequestInfo validity table (see ptc/00-08-06 table 21-1).
    // Note: These must be in the same order as specified in contants.
    private static final boolean validCall[][] = {
        // LEGEND:
        // s_req = send_request     r_rep = receive_reply
        // s_pol = send_poll        r_exc = receive_exception
        //                          r_oth = receive_other
        //
        // A true value indicates call is valid at specified point.  
        // A false value indicates the call is invalid.
        //
        //
        // NOTE: If the order or number of columns change, update 
        // checkAccess() accordingly.
        //
        //                              { s_req, s_pol, r_rep, r_exc, r_oth }
        // RequestInfo methods:
        /*request_id*/                  { true , true , true , true , true  },
        /*operation*/                   { true , true , true , true , true  },
        /*arguments*/                   { true , false, true , false, false },
        /*exceptions*/                  { true , false, true , true , true  },
        /*contexts*/                    { true , false, true , true , true  },
        /*operation_context*/           { true , false, true , true , true  },
        /*result*/                      { false, false, true , false, false },
        /*response_expected*/           { true , true , true , true , true  },
        /*sync_scope*/                  { true , false, true , true , true  },
        /*reply_status*/                { false, false, true , true , true  },
        /*forward_reference*/           { false, false, false, false, true  },
        /*get_slot*/                    { true , true , true , true , true  },
        /*get_request_service_context*/ { true , false, true , true , true  },
        /*get_reply_service_context*/   { false, false, true , true , true  },
        //
        // ClientRequestInfo methods::
        /*target*/                      { true , true , true , true , true  },
        /*effective_target*/            { true , true , true , true , true  },
        /*effective_profile*/           { true , true , true , true , true  },
        /*received_exception*/          { false, false, false, true , false },
        /*received_exception_id*/       { false, false, false, true , false },
        /*get_effective_component*/     { true , false, true , true , true  },
        /*get_effective_components*/    { true , false, true , true , true  },
        /*get_request_policy*/          { true , false, true , true , true  },
        /*add_request_service_context*/ { true , false, false, false, false }
    };
    

    /*
     **********************************************************************
     * Public ClientRequestInfo interfaces
     **********************************************************************/
    
    /**
     * Creates a new ClientRequestInfo implementation.
     * The constructor is package scope since no other package need create
     * an instance of this class.
     */
    protected ClientRequestInfoImpl( ORB myORB ) { 
        super( myORB ); 
        startingPointCall = CALL_SEND_REQUEST;
        endingPointCall = CALL_RECEIVE_REPLY;
    }
    
    /**
     * The object which the client called to perform the operation.
     */
    public org.omg.CORBA.Object target (){
        if (myORB.interceptorDebugFlag) {
            dputil.enter( "target" ) ;
        }

        try {
            // access is currently valid for all states:
            //checkAccess( MID_TARGET );
            if (messageMediator != null && cachedTargetObject == null) {
                CorbaContactInfo corbaContactInfo = (CorbaContactInfo)
                    messageMediator.getContactInfo();
                cachedTargetObject =
                    iorToObject(corbaContactInfo.getTargetIOR());
            }
            return cachedTargetObject;
        } finally {
            if (myORB.interceptorDebugFlag) {
                dputil.exit() ;
            }
        }
    }
    
    /**
     * The actual object on which the operation will be invoked.  If the 
     * reply_status is LOCATION_FORWARD, then on subsequent requests, 
     * effective_target will contain the forwarded IOR while target will 
     * remain unchanged.  
     */
    public org.omg.CORBA.Object effective_target() {
        if (myORB.interceptorDebugFlag) {
            dputil.enter( "effective_target" ) ;
        }

        try {
            // access is currently valid for all states:
            //checkAccess( MID_EFFECTIVE_TARGET );

            // Note: This is not necessarily the same as locatedIOR.
            // Reason: See the way we handle COMM_FAILURES in 
            // ClientRequestDispatcher.createRequest, v1.32

            if (messageMediator != null && cachedEffectiveTargetObject == null) {
                CorbaContactInfo corbaContactInfo = (CorbaContactInfo)
                    messageMediator.getContactInfo();
                // REVISIT - get through chain like getLocatedIOR helper below.
                cachedEffectiveTargetObject =
                    iorToObject(corbaContactInfo.getEffectiveTargetIOR());
            }
            return cachedEffectiveTargetObject;
        } finally {
            if (myORB.interceptorDebugFlag) {
                dputil.exit() ;
            }
        }
    }
    
    /**
     * The profile that will be used to send the request.  If a location 
     * forward has occurred for this operation's object and that object's 
     * profile change accordingly, then this profile will be that located 
     * profile.
     */
    public TaggedProfile effective_profile (){
        if (myORB.interceptorDebugFlag) {
            dputil.enter( "effective_profile" ) ;
        }

        try {
            // access is currently valid for all states:
            //checkAccess( MID_EFFECTIVE_PROFILE );

            if(messageMediator != null && cachedEffectiveProfile == null ) {
                CorbaContactInfo corbaContactInfo = (CorbaContactInfo)
                    messageMediator.getContactInfo();
                cachedEffectiveProfile =
                    corbaContactInfo.getEffectiveProfile().getIOPProfile();
            }

            // Good citizen: In the interest of efficiency, we assume interceptors
            // will not modify the returned TaggedProfile in any way so we need
            // not make a deep copy of it.

            return cachedEffectiveProfile;
        } finally {
            if (myORB.interceptorDebugFlag) {
                dputil.exit() ;
            }
        }
    }
    
    /**
     * Contains the exception to be returned to the client.
     */
    public Any received_exception (){
        if (myORB.interceptorDebugFlag) {
            dputil.enter( "received_exception" ) ;
        }

        try {
            checkAccess( MID_RECEIVED_EXCEPTION );

            if( cachedReceivedException == null ) {
                cachedReceivedException = exceptionToAny( exception );
            }

            // Good citizen: In the interest of efficiency, we assume interceptors
            // will not modify the returned Any in any way so we need
            // not make a deep copy of it.

            return cachedReceivedException;
        } finally {
            if (myORB.interceptorDebugFlag) {
                dputil.exit() ;
            }
        }
    }
    
    /**
     * The CORBA::RepositoryId of the exception to be returned to the client.
     */
    public String received_exception_id (){
        if (myORB.interceptorDebugFlag) {
            dputil.enter( "received_exception_id" ) ;
        }

        try {
            checkAccess( MID_RECEIVED_EXCEPTION_ID );

            if( cachedReceivedExceptionId == null ) {
                String result = null;
                
                if( exception == null ) {
                    // Note: exception should never be null here since we will 
                    // throw a BAD_INV_ORDER if this is not called from 
                    // receive_exception.
                    throw wrapper.exceptionWasNull() ;
                } else if( exception instanceof SystemException ) {
                    String name = exception.getClass().getName();
                    result = ORBUtility.repositoryIdOf(name);
                } else if( exception instanceof ApplicationException ) {
                    result = ((ApplicationException)exception).getId();
                }

                // _REVISIT_ We need to be able to handle a UserException in the 
                // DII case.  How do we extract the ID from a UserException?
                
                cachedReceivedExceptionId = result;
            }

            return cachedReceivedExceptionId;
        } finally {
            if (myORB.interceptorDebugFlag) {
                dputil.exit() ;
            }
        }
    }
    
    /**
     * Returns the IOP::TaggedComponent with the given ID from the profile 
     * selected for this request.  IF there is more than one component for a 
     * given component ID, it is undefined which component this operation 
     * returns (get_effective_component should be called instead).
     */
    public TaggedComponent get_effective_component (int id){
        if (myORB.interceptorDebugFlag) {
            dputil.enter( "get_effective_component" ) ;
        }

        try {
            checkAccess( MID_GET_EFFECTIVE_COMPONENT );
            
            return get_effective_components( id )[0];
        } finally {
            if (myORB.interceptorDebugFlag) {
                dputil.exit() ;
            }
        }
    }
    
    /**
     * Returns all the tagged components with the given ID from the profile 
     * selected for this request.
     */
    public TaggedComponent[] get_effective_components (int id){
        if (myORB.interceptorDebugFlag) {
            dputil.enter( "get_effective_components" ) ;
        }

        try {
            checkAccess( MID_GET_EFFECTIVE_COMPONENTS );
            TaggedComponent[] result = null;
            boolean justCreatedCache = false;

            if( cachedEffectiveComponents == null ) {
                cachedEffectiveComponents = new HashMap<Integer,TaggedComponent[]>();
                justCreatedCache = true;
            } else {
                // Look in cache:
                result = cachedEffectiveComponents.get( id );
            }
            
            // null could mean we cached null or not in cache.
            if( (messageMediator != null) && (result == null) &&
                (justCreatedCache ||
                !cachedEffectiveComponents.containsKey( id ) ) )
            {
                // Not in cache.  Get it from the profile:
                CorbaContactInfo corbaContactInfo = (CorbaContactInfo)
                    messageMediator.getContactInfo();
                IIOPProfileTemplate ptemp = 
                    (IIOPProfileTemplate)corbaContactInfo.getEffectiveProfile().
                    getTaggedProfileTemplate();
                result = ptemp.getIOPComponents(myORB, id);
                cachedEffectiveComponents.put( id, result );
            }
            
            // As per ptc/00-08-06, section 21.3.13.6., If not found, raise 
            // BAD_PARAM with minor code INVALID_COMPONENT_ID.
            if( (result == null) || (result.length == 0) ) {
                throw stdWrapper.invalidComponentId( id ) ;
            }

            // Good citizen: In the interest of efficiency, we will assume 
            // interceptors will not modify the returned TaggedCompoent[], or
            // the TaggedComponents inside of it.  Otherwise, we would need to
            // clone the array and make a deep copy of its contents.
            
            return result;
        } finally {
            if (myORB.interceptorDebugFlag) {
                dputil.exit() ;
            }
        }
    }
    
    /**
     * Returns the given policy in effect for this operation.
     */
    public Policy get_request_policy (int type){
        if (myORB.interceptorDebugFlag) {
            dputil.enter( "get_request_policy" ) ;
        }

        try {
            checkAccess( MID_GET_REQUEST_POLICY );
            // _REVISIT_ Our ORB is not policy-based at this time.
            throw wrapper.piOrbNotPolicyBased() ;
        } finally {
            if (myORB.interceptorDebugFlag) {
                dputil.exit() ;
            }
        }
    }
    
    /**
     * Allows interceptors to add service contexts to the request.
     * <p>
     * There is no declaration of the order of the service contexts.  They 
     * may or may not appear in the order they are added.
     */
    public void add_request_service_context (ServiceContext service_context, 
                                             boolean replace)
    {
        if (myORB.interceptorDebugFlag) {
            dputil.enter( "add_request_service_context" ) ;
        }

        try {
            checkAccess( MID_ADD_REQUEST_SERVICE_CONTEXT );

            if( cachedRequestServiceContexts == null ) {
                cachedRequestServiceContexts = 
                    new HashMap<Integer,org.omg.IOP.ServiceContext>();
            }

            addServiceContext( cachedRequestServiceContexts, 
                               messageMediator.getRequestServiceContexts(),
                               service_context, replace );
        } finally {
            if (myORB.interceptorDebugFlag) {
                dputil.exit() ;
            }
        }
    }
    
    // NOTE: When adding a method, be sure to:
    // 1. Add a MID_* constant for that method
    // 2. Call checkAccess at the start of the method
    // 3. Define entries in the validCall[][] table for interception points.

    /*
     **********************************************************************
     * Public RequestInfo interfaces
     *
     * These are implemented here because they have differing 
     * implementations depending on whether this is a client or a server
     * request info object.
     **********************************************************************/
   
    /**
     * See RequestInfoImpl for javadoc.
     */
    public int request_id (){
        // access is currently valid for all states:
        //checkAccess( MID_REQUEST_ID );
	/* 
	 * NOTE: The requestId in client interceptors is the same as the
	 * GIOP request id.  This works because both interceptors and
	 * request ids are scoped by the ORB on the client side.
	 */
	return messageMediator.getRequestId();
    }

    /**
     * See RequestInfoImpl for javadoc.
     */
    public String operation(){
        // access is currently valid for all states:
        //checkAccess( MID_OPERATION );
        if (messageMediator != null)
            return messageMediator.getOperationName();
        else 
            return "<special operation>" ;
    }

    @Override
    public String toString() {
	return "ClientRequestInfoImpl[operation=" 
	    + operation() + "]" ;
    }

    /**
     * See RequestInfoImpl for javadoc.
     */
    public Parameter[] arguments (){
        if (myORB.interceptorDebugFlag) {
            dputil.enter( "arguments" ) ;
        }

        try {
            checkAccess( MID_ARGUMENTS );

            if( cachedArguments == null ) {
                if( request == null ) {
                    throw stdWrapper.piOperationNotSupported1() ;
                }

                // If it is DII request then get the arguments from the DII req
                // and convert that into parameters.
                cachedArguments = nvListToParameterArray( request.arguments() );
            }

            // Good citizen: In the interest of efficiency, we assume 
            // interceptors will be "good citizens" in that they will not 
            // modify the contents of the Parameter[] array.  We also assume 
            // they will not change the values of the containing Anys.

            return cachedArguments;
        } finally {
            if (myORB.interceptorDebugFlag) {
                dputil.exit() ;
            }
        }
    }

    /**
     * See RequestInfoImpl for javadoc.
     */
    public TypeCode[] exceptions (){
        if (myORB.interceptorDebugFlag) {
            dputil.enter( "exceptions" ) ;
        }

        try {
            checkAccess( MID_EXCEPTIONS );

            if( cachedExceptions == null ) {
                if( request == null ) {
                   throw stdWrapper.piOperationNotSupported2() ;
                }

                // Get the list of exceptions from DII request data, If there are
                // no exceptions raised then this method will return null.
                ExceptionList excList = request.exceptions( );
                int count = excList.count();
                TypeCode[] excTCList = new TypeCode[count];
                try {
                    for( int i = 0; i < count; i++ ) {
                        excTCList[i] = excList.item( i );
                    }
                } catch( Exception e ) {
                    throw wrapper.exceptionInExceptions( e ) ;
                }

                cachedExceptions = excTCList;
            }

            // Good citizen: In the interest of efficiency, we assume 
            // interceptors will be "good citizens" in that they will not 
            // modify the contents of the TypeCode[] array.  We also assume 
            // they will not change the values of the containing TypeCodes.

            return cachedExceptions;
        } finally {
            if (myORB.interceptorDebugFlag) {
                dputil.exit() ;
            }
        }
    }

    /**
     * See RequestInfoImpl for javadoc.
     */
    public String[] contexts (){
        if (myORB.interceptorDebugFlag) {
            dputil.enter( "contexts" ) ;
        }

        try {
            checkAccess( MID_CONTEXTS );

            if( cachedContexts == null ) {
                if( request == null ) {
                    throw stdWrapper.piOperationNotSupported3() ;
                }

                // Get the list of contexts from DII request data, If there are
                // no contexts then this method will return null.
                ContextList ctxList = request.contexts( );
                int count = ctxList.count();
                String[] ctxListToReturn = new String[count];
                try {
                    for( int i = 0; i < count; i++ ) {
                        ctxListToReturn[i] = ctxList.item( i );
                    }
                } catch( Exception e ) {
                    throw wrapper.exceptionInContexts( e ) ;
                }

                cachedContexts = ctxListToReturn;
            }

            // Good citizen: In the interest of efficiency, we assume 
            // interceptors will be "good citizens" in that they will not 
            // modify the contents of the String[] array.  

            return cachedContexts;
        } finally {
            if (myORB.interceptorDebugFlag) {
                dputil.exit() ;
            }
        }
    }

    /**
     * See RequestInfoImpl for javadoc.
     */
    public String[] operation_context (){
        if (myORB.interceptorDebugFlag) {
            dputil.enter( "operation_context" ) ;
        }

        try {
            checkAccess( MID_OPERATION_CONTEXT );

            if( cachedOperationContext == null ) {
                if( request == null ) {
                    throw stdWrapper.piOperationNotSupported4() ;
                }

                // Get the list of contexts from DII request data, If there are
                // no contexts then this method will return null.
                Context ctx = request.ctx( );
                // _REVISIT_ The API for get_values is not compliant with the spec,
                // Revisit this code once it's fixed.
                // _REVISIT_ Our ORB doesn't support Operation Context, This code
                // will not be excerscised until it's supported.
                // The first parameter in get_values is the start_scope which 
                // if blank makes it as a global scope.
                // The second parameter is op_flags which is set to RESTRICT_SCOPE
                // As there is only one defined in the spec.
                // The Third param is the pattern which is '*' requiring it to 
                // get all the contexts.
                NVList nvList = ctx.get_values( "", CTX_RESTRICT_SCOPE.value,"*" );
                String[] context = new String[(nvList.count() * 2) ];
                if( nvList.count() != 0 ) {
                    // The String[] array will contain Name and Value for each
                    // context and hence double the size in the array.
                    int index = 0;
                    for( int i = 0; i < nvList.count(); i++ ) {
                        NamedValue nv;
                        try {
                            nv = nvList.item( i );
                        }
                        catch (Exception e ) {
                            return (String[]) null;
                        }
                        context[index] = nv.name();
                        index++;
                        context[index] = nv.value().extract_string();
                        index++;
                    }
                }

                cachedOperationContext = context;
            }

            // Good citizen: In the interest of efficiency, we assume 
            // interceptors will be "good citizens" in that they will not 
            // modify the contents of the String[] array.  

            return cachedOperationContext;
        } finally {
            if (myORB.interceptorDebugFlag) {
                dputil.exit() ;
            }
        }
    }

    /**
     * See RequestInfoImpl for javadoc.
     */
    public Any result (){
        if (myORB.interceptorDebugFlag) {
            dputil.enter( "result" ) ;
        }

        try {
            checkAccess( MID_RESULT );

            if( cachedResult == null ) {
                if( request == null ) {
                    throw stdWrapper.piOperationNotSupported5() ;
                }
                // Get the result from the DII request data.
                NamedValue nvResult = request.result( );

                if( nvResult == null ) {
                    throw wrapper.piDiiResultIsNull() ;
                }

                cachedResult = nvResult.value();
            }

            // Good citizen: In the interest of efficiency, we assume that
            // interceptors will not modify the contents of the result Any.
            // Otherwise, we would need to create a deep copy of the Any.

            return cachedResult;
        } finally {
            if (myORB.interceptorDebugFlag) {
                dputil.exit() ;
            }
        }
    }

    /**
     * See RequestInfoImpl for javadoc.
     */
    public boolean response_expected (){
	// access is currently valid for all states:
	//checkAccess( MID_RESPONSE_EXPECTED );
	return ! messageMediator.isOneWay();
    }

    /**
     * See RequestInfoImpl for javadoc.
     */
    public Object forward_reference (){
        if (myORB.interceptorDebugFlag) {
            dputil.enter( "forward_reference" ) ;
        }

        try {
            checkAccess( MID_FORWARD_REFERENCE );
            // Check to make sure we are in LOCATION_FORWARD
            // state as per ptc/00-08-06, table 21-1
            // footnote 2.
            if( replyStatus != LOCATION_FORWARD.value ) {
                throw stdWrapper.invalidPiCall1() ;
            }

            // Do not cache this value since if an interceptor raises
            // forward request then the next interceptor in the
            // list should see the new value.
            IOR ior = getLocatedIOR();
            return iorToObject(ior);
        } finally {
            if (myORB.interceptorDebugFlag) {
                dputil.exit() ;
            }
        }
    }

    private IOR getLocatedIOR() {
        if (myORB.interceptorDebugFlag) {
            dputil.enter( "getLocatedIOR" ) ;
        }

        try {
            IOR ior;
            CorbaContactInfoList contactInfoList = (CorbaContactInfoList)
                messageMediator.getContactInfo().getContactInfoList();
            ior = contactInfoList.getEffectiveTargetIOR();
            return ior;
        } finally {
            if (myORB.interceptorDebugFlag) {
                dputil.exit() ;
            }
        }
    }

    // Used to be protected. public for IIOPFailoverManagerImpl.
    public void setLocatedIOR(IOR ior) {

        if (myORB.interceptorDebugFlag) {
            dputil.enter( "setLocatedIOR" ) ;
        }

        try {
            ORB orb = (ORB) messageMediator.getBroker();

            CorbaContactInfoListIterator iterator = (CorbaContactInfoListIterator)
                ((CorbaInvocationInfo)orb.getInvocationInfo())
                .getContactInfoListIterator();

            // REVISIT - this most likely causes reportRedirect to happen twice.
            // Once here and once inside the request dispatcher.
            iterator.reportRedirect(
                (CorbaContactInfo)messageMediator.getContactInfo(),
                ior);
        } finally {
            if (myORB.interceptorDebugFlag) {
                dputil.exit() ;
            }
        }
    }

    /**
     * See RequestInfoImpl for javadoc.
     */
    public org.omg.IOP.ServiceContext get_request_service_context( int id ) {
        if (myORB.interceptorDebugFlag) {
            dputil.enter( "get_request_service_context" ) ;
        }

        try {
            checkAccess( MID_GET_REQUEST_SERVICE_CONTEXT );

            if( cachedRequestServiceContexts == null ) {
                cachedRequestServiceContexts = 
                    new HashMap<Integer,org.omg.IOP.ServiceContext>();
            }

            return  getServiceContext(cachedRequestServiceContexts, 
                                      messageMediator.getRequestServiceContexts(),
                                      id);
        } finally {
            if (myORB.interceptorDebugFlag) {
                dputil.exit() ;
            }
        }
    }

    /**
     * does not contain an etry for that ID, BAD_PARAM with a minor code of
     * TBD_BP is raised.
     */
    public org.omg.IOP.ServiceContext get_reply_service_context( int id ) {
        if (myORB.interceptorDebugFlag) {
            dputil.enter( "get_reply_service_context" ) ;
        }

        try {
            checkAccess( MID_GET_REPLY_SERVICE_CONTEXT );       

            if( cachedReplyServiceContexts == null ) {
                cachedReplyServiceContexts = 
                    new HashMap<Integer,org.omg.IOP.ServiceContext>();
            }

            // In the event this is called from a oneway, we will have no
            // response object.
            //
            // In the event this is called after a IIOPConnection.purgeCalls,
            // we will have a response object, but that object will
            // not contain a header (which would hold the service context
            // container).  See bug 4624102.
            //

            // REVISIT: getReplyHeader should not be visible here.
            if (messageMediator.getReplyHeader() != null) {
                ServiceContexts sctxs =
                    messageMediator.getReplyServiceContexts();
                if (sctxs != null) {
                    return getServiceContext(cachedReplyServiceContexts,
                                             sctxs, id);
                }
            }
            // See purge calls test.  The waiter is woken up by the
            // call to purge calls - but there is no reply containing
            // service contexts.
            throw stdWrapper.invalidServiceContextId() ;
        } finally {
            if (myORB.interceptorDebugFlag) {
                dputil.exit() ;
            }
        }
    }

    //
    // REVISIT
    // Override RequestInfoImpl connection to work in framework.
    //

    @Override
    public com.sun.corba.se.spi.legacy.connection.Connection connection()
    {
	return (com.sun.corba.se.spi.legacy.connection.Connection) 
	    messageMediator.getConnection();
    }
    


    /*
     **********************************************************************
     * Package-scope interfaces
     **********************************************************************/

    protected void setInfo(CorbaMessageMediator messageMediator)
    {
	this.messageMediator = (CorbaMessageMediator)messageMediator;
	// REVISIT - so mediator can handle DII in subcontract.
	this.messageMediator.setDIIInfo(request);
    }
    
    /**
     * Set or reset the retry request flag.  
     */
    void setRetryRequest( RetryType retryRequest ) {
        // 6763340
        this.retryRequest = retryRequest;
    }
    
    /**
     * Retrieve the current retry request status.
     */
    RetryType getRetryRequest() {
        // 6763340
        return this.retryRequest;
    }
    
    /**
     * Increases the entry count by 1.
     */
    void incrementEntryCount() {
        if (myORB.interceptorDebugFlag) {
            dputil.enter( "incrementEntryCount" ) ;
        }

        try {
            this.entryCount++;
            if (myORB.interceptorDebugFlag) {
                dputil.info( "entryCount", this.entryCount ) ;
            }
        } finally {
            if (myORB.interceptorDebugFlag) {
                dputil.exit() ;
            }
        }
    }
    
    /**
     * Decreases the entry count by 1.
     */
    void decrementEntryCount() {
        if (myORB.interceptorDebugFlag) {
            dputil.enter( "decrementEntryCount" ) ;
        }

        try {
            this.entryCount--;
            if (myORB.interceptorDebugFlag) {
                dputil.info( "entryCount", this.entryCount ) ;
            }
        } finally {
            if (myORB.interceptorDebugFlag) {
                dputil.exit() ;
            }
        }
    }
    
    /**
     * Retrieve the current entry count
     */
    int getEntryCount() {
        if (myORB.interceptorDebugFlag) {
            dputil.enter( "getEntryCount" ) ;
        }

        int result = 0 ;
        try {
            result = this.entryCount;
        } finally {
            if (myORB.interceptorDebugFlag) {
                dputil.exit( result ) ;
            }
        }
        return result ;
    }
    
    /**
     * Overridden from RequestInfoImpl.  Calls the super class, then
     * sets the ending point call depending on the reply status.
     */
    @Override
    protected void setReplyStatus( short replyStatus ) {
        if (myORB.interceptorDebugFlag) {
            dputil.enter( "setReplyStatus", 
                PIHandlerImpl.getReplyStatus(replyStatus) ) ;
        }
        try {
            super.setReplyStatus( replyStatus );
            switch( replyStatus ) {
            case SUCCESSFUL.value:
                endingPointCall = CALL_RECEIVE_REPLY;
                break;
            case SYSTEM_EXCEPTION.value:
            case USER_EXCEPTION.value:
                endingPointCall = CALL_RECEIVE_EXCEPTION;
                break;
            case LOCATION_FORWARD.value:
            case TRANSPORT_RETRY.value:
                endingPointCall = CALL_RECEIVE_OTHER;
                break;
            }
        } finally {
            if (myORB.interceptorDebugFlag) {
                dputil.exit() ;
            }
        }
    }

    @Override
    protected short getReplyStatus() {
        if (myORB.interceptorDebugFlag) {
            dputil.enter( "getReplyStatus" ) ;
        }

        short result = 0 ;
        try {
            result = super.getReplyStatus() ;
        } finally {
            if (myORB.interceptorDebugFlag) {
                dputil.exit( PIHandlerImpl.getReplyStatus( result ) ) ;
            }
        }
        
        return result ;
    }

    /**
     * Sets DII request object in the RequestInfoObject.
     */
    protected void setDIIRequest(org.omg.CORBA.Request req) {
         request = req;
    }

    /**
     * Keeps track of whether initiate was called for a DII request.  The ORB
     * needs to know this so it knows whether to ignore a second call to
     * initiateClientPIRequest or not.
     */
    protected void setDIIInitiate( boolean diiInitiate ) {
	this.diiInitiate = diiInitiate;
    }

    /**
     * See comment for setDIIInitiate 
     */
    protected boolean isDIIInitiate() {
	return this.diiInitiate;
    }

    /**
     * The PICurrent stack should only be popped if it was pushed.
     * This is generally the case.  But exceptions which occur
     * after the stub's entry to _request but before the push
     * end up in _releaseReply which will try to pop unless told not to.
     */
    protected void setPICurrentPushed( boolean piCurrentPushed ) {
	this.piCurrentPushed = piCurrentPushed;
    }

    protected boolean isPICurrentPushed() {
	return this.piCurrentPushed;
    }

    /**
     * Overridden from RequestInfoImpl.
     */
    @Override
    protected void setException( Exception exception ) {
        super.setException( exception );

	// Clear cached values:
	cachedReceivedException = null;
	cachedReceivedExceptionId = null;
    }

    protected boolean getIsOneWay() {
	return ! response_expected();
    }

    /**
     * See description for RequestInfoImpl.checkAccess
     */
    protected void checkAccess( int methodID ) 
        throws BAD_INV_ORDER 
    {
        // Make sure currentPoint matches the appropriate index in the
        // validCall table:
        int validCallIndex = 0;
        switch( currentExecutionPoint ) {
        case EXECUTION_POINT_STARTING:
            switch( startingPointCall ) {
            case CALL_SEND_REQUEST:
                validCallIndex = 0;
                break;
            case CALL_SEND_POLL:
                validCallIndex = 1;
                break;
            }
            break;
        case EXECUTION_POINT_ENDING:
            switch( endingPointCall ) {
            case CALL_RECEIVE_REPLY:
                validCallIndex = 2;
                break;
            case CALL_RECEIVE_EXCEPTION:
                validCallIndex = 3;
                break;
            case CALL_RECEIVE_OTHER:
                validCallIndex = 4;
                break;
            }
            break;
        }
        
        // Check the validCall table:
        if( !validCall[methodID][validCallIndex] ) {
	    throw stdWrapper.invalidPiCall2() ;
        }
    }
    
}

// End of file.
