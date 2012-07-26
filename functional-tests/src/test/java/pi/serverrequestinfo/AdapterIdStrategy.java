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

package pi.serverrequestinfo;

import org.omg.CORBA.*;
import com.sun.corba.ee.impl.interceptors.*;
import org.omg.PortableInterceptor.*;
import org.omg.Messaging.*;
import com.sun.corba.ee.impl.misc.HexOutputStream;

import java.util.*;
import java.io.*;

/**
 * Strategy to further test adapter_id() (only executed for POA)
 */
public class AdapterIdStrategy
    extends InterceptorStrategy
{

    // The request count. We should be calling:
    //   0 - sayHello
    //   1 - saySystemException
    //   2 - saySystemException.
    private int count = 0;

    // Set from POAServer.  This is the adapter ID to test against.
    private byte[] expectedAdapterId;

    public AdapterIdStrategy( byte[] expectedAdapterId ) {
        this.expectedAdapterId = expectedAdapterId;
    }

    public void receive_request_service_contexts (
        SampleServerRequestInterceptor interceptor, ServerRequestInfo ri)
        throws ForwardRequest
    {
        // We already checked that adapter_id is invalid in rrsc.
        try {
            super.receive_request_service_contexts( interceptor, ri );
            count++;
        }
        catch( Exception ex ) {
            failException( "rrsc", ex );
        }
    }

    public void receive_request (
        SampleServerRequestInterceptor interceptor, ServerRequestInfo ri)
    {
        try {
            super.receive_request( interceptor, ri );
            checkAdapterId( "receive_request", ri.adapter_id() );
        }
        catch( Exception ex ) {
            failException( "receive_request", ex );
        }
    }

    public void send_reply (
        SampleServerRequestInterceptor interceptor, ServerRequestInfo ri)
    {
        try {
            super.send_reply( interceptor, ri );
            checkAdapterId( "send_reply", ri.adapter_id() );
        }
        catch( Exception ex ) {
            failException( "send_reply", ex );
        }
    }

    public void send_exception (
        SampleServerRequestInterceptor interceptor, ServerRequestInfo ri)
        throws ForwardRequest
    {
        try {
            super.send_exception( interceptor, ri );
            checkAdapterId( "send_exception", ri.adapter_id() );
        }
        catch( Exception ex ) {
            failException( "send_exception", ex );
        }
    }

    public void send_other (
        SampleServerRequestInterceptor interceptor, ServerRequestInfo ri)
        throws ForwardRequest
    {
        try {
            super.send_other( interceptor, ri );
            checkAdapterId( "send_other", ri.adapter_id() );
        }
        catch( Exception ex ) {
            failException( "send_other", ex );
        }
    }

    private void checkAdapterId( String method, byte[] adapterId ) {
        log( method + "(): Actual adapter id = " + dumpHex( adapterId ) );
        if( Arrays.equals( adapterId, expectedAdapterId ) ) {
            log( method + "(): Adapter id compares." );
        }
        else {
            fail( method + "(): Adapter id does not compare.  " +
                "(expected id: " + dumpHex( expectedAdapterId ) + ")" );
        }
    }

    private String dumpHex( byte[] bytes ) {
        StringWriter sw = new StringWriter();
        HexOutputStream out = new HexOutputStream( sw );
        try {
            out.write( bytes );
        }
        catch( IOException e ) {}
        return sw.toString();
    }

}

