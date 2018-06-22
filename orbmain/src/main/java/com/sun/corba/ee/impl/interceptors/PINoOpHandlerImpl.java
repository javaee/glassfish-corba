/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright (c) 1997-2018 Oracle and/or its affiliates. All rights reserved.
 *
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common Development
 * and Distribution License("CDDL") (collectively, the "License").  You
 * may not use this file except in compliance with the License.  You can
 * obtain a copy of the License at
 * https://oss.oracle.com/licenses/CDDL+GPL-1.1
 * or LICENSE.txt.  See the License for the specific
 * language governing permissions and limitations under the License.
 *
 * When distributing the software, include this License Header Notice in each
 * file and include the License file at LICENSE.txt.
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

package com.sun.corba.ee.impl.interceptors;

             
import org.omg.CORBA.Any;
import org.omg.CORBA.NVList;


import org.omg.CORBA.portable.RemarshalException;

import org.omg.PortableInterceptor.ObjectReferenceTemplate ;
import org.omg.PortableInterceptor.Interceptor;
import org.omg.PortableInterceptor.PolicyFactory;
import org.omg.PortableInterceptor.Current;

import org.omg.PortableInterceptor.ORBInitInfoPackage.DuplicateName ;


import com.sun.corba.ee.spi.ior.ObjectKeyTemplate;

import com.sun.corba.ee.spi.oa.ObjectAdapter;


import com.sun.corba.ee.spi.protocol.PIHandler;
import com.sun.corba.ee.spi.protocol.MessageMediator;

import com.sun.corba.ee.impl.corba.RequestImpl;

import com.sun.corba.ee.impl.protocol.giopmsgheaders.ReplyMessage;

/** 
 * This is No-Op implementation of PIHandler. It is used in ORBConfigurator
 * to initialize a piHandler before the Persistent Server Activation. This 
 * PIHandler implementation will be replaced by the real PIHandler in 
 * ORB.postInit( ) call.
 */
public class PINoOpHandlerImpl implements PIHandler 
{
    public PINoOpHandlerImpl( ) {
    }

    public void close() {
    }

    public void initialize() {
    }

    public void destroyInterceptors() {
    }

    public void objectAdapterCreated( ObjectAdapter oa ) 
    {
    }

    public void adapterManagerStateChanged( int managerId,
        short newState )
    {
    }

    public void adapterStateChanged( ObjectReferenceTemplate[] 
        templates, short newState )
    {
    }


    public void disableInterceptorsThisThread() {
    }
    
    public void enableInterceptorsThisThread() {
    }
    
    public void invokeClientPIStartingPoint() 
        throws RemarshalException
    {
    }
    
    public Exception invokeClientPIEndingPoint(
        int replyStatus, Exception exception )
    {
        return null;
    }

    public Exception makeCompletedClientRequest(
        int replyStatus, Exception exception )
    {
        return null;
    }
    
    public void initiateClientPIRequest( boolean diiRequest ) {
    }
    
    public void cleanupClientPIRequest() {
    }

    public void setClientPIInfo(MessageMediator messageMediator)
    {
    }

    public void setClientPIInfo( RequestImpl requestImpl ) 
    {
    }
    
    final public void sendCancelRequestIfFinalFragmentNotSent()
    {
    }
    
    
    public void invokeServerPIStartingPoint() 
    {
    }

    public void invokeServerPIIntermediatePoint() 
    {
    }
    
    public void invokeServerPIEndingPoint( ReplyMessage replyMessage ) 
    {
    }
    
    public void setServerPIInfo( Exception exception ) {
    }

    public void setServerPIInfo( NVList arguments )
    {
    }

    public void setServerPIExceptionInfo( Any exception )
    {
    }

    public void setServerPIInfo( Any result )
    {
    }

    public void initializeServerPIInfo( MessageMediator request,
        ObjectAdapter oa, byte[] objectId, ObjectKeyTemplate oktemp ) 
    {
    }
    
    public void setServerPIInfo( java.lang.Object servant, 
                                          String targetMostDerivedInterface ) 
    {
    }

    public void cleanupServerPIRequest() {
    }
    
    public void register_interceptor( Interceptor interceptor, int type ) 
        throws DuplicateName
    {
    }

    public Current getPICurrent( ) {
        return null;
    }

    public org.omg.CORBA.Policy create_policy(int type, org.omg.CORBA.Any val)
        throws org.omg.CORBA.PolicyError
    {
        return null;
    }

    public void registerPolicyFactory( int type, PolicyFactory factory ) {
    }
    
    public int allocateServerRequestId ()
    {
        return 0;
    }
}
