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

package com.sun.corba.ee.impl.transport;

import java.nio.ByteBuffer ;

import java.util.List ;
import java.util.ArrayList ;

import com.sun.corba.ee.spi.transport.MessageTraceManager ;

public class MessageTraceManagerImpl implements MessageTraceManager
{
    // Note: this implementation does not need to be syncronized
    // because an instance of this class is only called from a single
    // thread.
    private List /* <byte[]> */ dataSent ;
    private List /* <byte[]> */ dataReceived ;
    private boolean enabled ;
    private boolean RHRCalled ; // Set to true whenever recordHeaderReceived is called.
    private byte[] header ;

    public MessageTraceManagerImpl()
    {
        init() ;
        enabled = false ;
    }

    public void clear()
    {
        init() ;
    }

    private void init() 
    {
        dataSent = new ArrayList() ;
        dataReceived = new ArrayList() ;
        initHeaderRecorder() ;
    }

    public boolean isEnabled() 
    {
        return enabled ;
    }

    public void enable( boolean flag ) 
    {
        enabled = flag ;
    }

    public byte[][] getDataSent() 
    {
        return (byte[][])dataSent.toArray(
            new byte[dataSent.size()][] ) ;
    }

    public byte[][] getDataReceived() 
    {
        return (byte[][])dataReceived.toArray(
            new byte[dataReceived.size()][] ) ;
    }

    // Methods that are used internally to record messages
   
    private void initHeaderRecorder()
    {
        RHRCalled = false ;
        header = null ;
    }

    /** Return the contents of the byte buffer.  The ByteBuffer
     * is not modified.  The result is written starting at
     * index offset in the byte[].
     */
    public byte[] getBytes( ByteBuffer bb, int offset ) 
    {
        ByteBuffer view = bb.asReadOnlyBuffer() ;
        view.flip() ;
        int len = view.remaining() ;
        byte[] buffer = new byte[ len + offset ] ;
        view.get( buffer, offset, len ) ;

        return buffer ; 
    }

    @Override
    public void recordDataSent(ByteBuffer message)
    {
        byte[] buffer = getBytes( message, 0 ) ;
        dataSent.add( buffer ) ;
    }
    
    public void recordHeaderReceived( ByteBuffer message ) 
    {
        if (RHRCalled) {
            // Previous call was for header only: no body
            dataReceived.add( header ) ;
            initHeaderRecorder() ;
        }

        RHRCalled = true ;
        header = getBytes( message, 0 ) ;
    }

    public void recordBodyReceived( ByteBuffer message ) 
    {
        if (!RHRCalled)
            // This string is 12 characters long, so the ASCII
            // representation should have the same length as a
            // GIOP header.
            header = "NO HEADER!!!".getBytes() ;

        byte[] buffer = getBytes( message, header.length ) ;
        System.arraycopy( header, 0, buffer, header.length,
            message.remaining() ) ;
        dataReceived.add( buffer ) ;    

        initHeaderRecorder() ;
    }
}
