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

package com.sun.corba.se.spi.logging ;

import com.sun.corba.se.spi.oa.ObjectAdapter;
import com.sun.corba.se.spi.orbutil.logex.Chain;
import com.sun.corba.se.spi.orbutil.logex.Log ;
import com.sun.corba.se.spi.orbutil.logex.Message ;
import com.sun.corba.se.spi.orbutil.logex.LogLevel ;
import com.sun.corba.se.spi.orbutil.logex.ExceptionWrapper ;
import com.sun.corba.se.spi.orbutil.logex.WrapperGenerator ;
import com.sun.corba.se.spi.orbutil.logex.corba.CS;
import com.sun.corba.se.spi.orbutil.logex.corba.CSValue;

import com.sun.corba.se.spi.orbutil.logex.corba.ORBException ;
import com.sun.corba.se.spi.orbutil.logex.corba.CorbaExtension ;
import java.util.List;

import org.omg.CORBA.BAD_INV_ORDER;
import org.omg.CORBA.BAD_PARAM;
import org.omg.CORBA.COMM_FAILURE;
import org.omg.CORBA.INTERNAL;
import org.omg.CORBA.NO_IMPLEMENT;
import org.omg.CORBA.OBJECT_NOT_EXIST;
import org.omg.CORBA.UNKNOWN;
import org.omg.PortableInterceptor.ObjectReferenceTemplate;

@ExceptionWrapper( idPrefix="IOP" )
@ORBException( omgException=false, group=CorbaExtension.InterceptorsGroup )
public interface InterceptorsSystemException {
    InterceptorsSystemException self = WrapperGenerator.makeWrapper( 
        InterceptorsSystemException.class, CorbaExtension.self ) ;
    
    @Log( level=LogLevel.WARNING, id=1 )
    @Message( "Interceptor type {0} is out of range" )
    BAD_PARAM typeOutOfRange( int type ) ;
    
    @Log( level=LogLevel.WARNING, id=2 )
    @Message( "Interceptor's name is null: use empty string for "
        + "anonymous interceptors" )
    BAD_PARAM nameNull(  ) ;
    
    @Log( level=LogLevel.WARNING, id=1 )
    @Message( "resolve_initial_reference is invalid during pre_init" )
    BAD_INV_ORDER rirInvalidPreInit(  ) ;
    
    @Log( level=LogLevel.WARNING, id=2 )
    @Message( "Expected state {0}, but current state is {1}" )
    BAD_INV_ORDER badState1( int arg0, int arg1 ) ;
    
    @Log( level=LogLevel.WARNING, id=3 )
    @Message( "Expected state {0} or {1}, but current state is {2}" )
    BAD_INV_ORDER badState2( int arg0, int arg1, int arg2 ) ;
    
    @Log( level=LogLevel.WARNING, id=1 )
    @Message( "IOException during cancel request" )
    @CS( CSValue.MAYBE )
    COMM_FAILURE ioexceptionDuringCancelRequest( @Chain Exception exc ) ;
    
    @Log( level=LogLevel.WARNING, id=1 )
    @Message( "Exception was null" )
    INTERNAL exceptionWasNull(  ) ;
    
    @Log( level=LogLevel.WARNING, id=2 )
    @Message( "Object has no delegate" )
    INTERNAL objectHasNoDelegate(  ) ;
    
    @Log( level=LogLevel.WARNING, id=3 )
    @Message( "Delegate was not a ClientRequestDispatcher" )
    INTERNAL delegateNotClientsub(  ) ;
    
    @Log( level=LogLevel.WARNING, id=4 )
    @Message( "Object is not an ObjectImpl" )
    INTERNAL objectNotObjectimpl(  ) ;
    
    @Log( level=LogLevel.WARNING, id=5 )
    @Message( "Assertion failed: Interceptor set exception to UserException or "
        + "ApplicationException" )
    INTERNAL exceptionInvalid(  ) ;
    
    @Log( level=LogLevel.WARNING, id=6 )
    @Message( "Assertion failed: Reply status is initialized but not "
        + "SYSTEM_EXCEPTION or LOCATION_FORWARD" )
    INTERNAL replyStatusNotInit(  ) ;
    
    @Log( level=LogLevel.WARNING, id=7 )
    @Message( "Exception in arguments" )
    INTERNAL exceptionInArguments( @Chain Exception exc ) ;
    
    @Log( level=LogLevel.WARNING, id=8 )
    @Message( "Exception in exceptions" )
    INTERNAL exceptionInExceptions( @Chain Exception exc ) ;
    
    @Log( level=LogLevel.WARNING, id=9 )
    @Message( "Exception in contexts" )
    INTERNAL exceptionInContexts( @Chain Exception exc ) ;
    
    @Log( level=LogLevel.WARNING, id=10 )
    @Message( "Another exception was null" )
    INTERNAL exceptionWasNull2(  ) ;
    
    @Log( level=LogLevel.WARNING, id=11 )
    @Message( "Servant invalid" )
    INTERNAL servantInvalid(  ) ;
    
    @Log( level=LogLevel.WARNING, id=12 )
    @Message( "Can't pop only PICurrent" )
    INTERNAL cantPopOnlyPicurrent(  ) ;
    
    @Log( level=LogLevel.WARNING, id=13 )
    @Message( "Can't pop another PICurrent" )
    INTERNAL cantPopOnlyCurrent2(  ) ;
    
    @Log( level=LogLevel.WARNING, id=14 )
    @Message( "DSI result is null" )
    INTERNAL piDsiResultIsNull(  ) ;
    
    @Log( level=LogLevel.WARNING, id=15 )
    @Message( "DII result is null" )
    INTERNAL piDiiResultIsNull(  ) ;
    
    @Log( level=LogLevel.WARNING, id=16 )
    @Message( "Exception is unavailable" )
    INTERNAL exceptionUnavailable(  ) ;
    
    @Log( level=LogLevel.WARNING, id=17 )
    @Message( "Assertion failed: client request info stack is null" )
    INTERNAL clientInfoStackNull(  ) ;
    
    @Log( level=LogLevel.WARNING, id=18 )
    @Message( "Assertion failed: Server request info stack is null" )
    INTERNAL serverInfoStackNull(  ) ;
    
    @Log( level=LogLevel.WARNING, id=19 )
    @Message( "Mark and reset failed" )
    INTERNAL markAndResetFailed( @Chain Exception exc ) ;
    
    @Log( level=LogLevel.WARNING, id=20 )
    @Message( "currentIndex > tableContainer.size(): {0} > {1}" )
    INTERNAL slotTableInvariant( int arg0, int arg1 ) ;
    
    @Log( level=LogLevel.WARNING, id=21 )
    @Message( "InterceptorList is locked" )
    INTERNAL interceptorListLocked(  ) ;
    
    @Log( level=LogLevel.WARNING, id=22 )
    @Message( "Invariant: sorted size + unsorted size == total size was violated" )
    INTERNAL sortSizeMismatch(  ) ;
    
    @Log( level=LogLevel.FINE, id=23 )
    @Message( "Ignored exception in establish_components method for "
        + "ObjectAdapter {0} (as per specification)" )
    INTERNAL ignoredExceptionInEstablishComponents( @Chain Exception exc,
        ObjectAdapter oa ) ;
    
    @Log( level=LogLevel.FINE, id=24 )
    @Message( "Exception in components_established method for ObjectAdapter {0}" )
    INTERNAL exceptionInComponentsEstablished( @Chain Exception exc, 
        ObjectAdapter oa ) ;
    
    @Log( level=LogLevel.FINE, id=25 )
    @Message( "Ignored exception in adapter_manager_state_changed method for "
        + "managerId {0} and newState {1} (as per specification)" )
    INTERNAL ignoredExceptionInAdapterManagerStateChanged( @Chain Exception exc,
        int managerId, short newState ) ;
    
    @Log( level=LogLevel.FINE, id=26 )
    @Message( "Ignored exception in adapter_state_changed method for " +
        "templates {0} and newState {1} (as per specification)" )
    INTERNAL ignoredExceptionInAdapterStateChanged( @Chain Exception exc,
        List<ObjectReferenceTemplate> templates, short newState ) ;
    
    @Log( level=LogLevel.WARNING, id=1 )
    @Message( "Policies not implemented" )
    NO_IMPLEMENT piOrbNotPolicyBased(  ) ;
    
    @Log( level=LogLevel.FINE, id=1 )
    @Message( "ORBInitInfo object is only valid during ORB_init" )
    OBJECT_NOT_EXIST orbinitinfoInvalid(  ) ;
    
    @Log( level=LogLevel.FINE, id=1 )
    @Message( "Unknown request invocation error" )
    @CS( CSValue.MAYBE )
    UNKNOWN unknownRequestInvoke(  ) ;
}
