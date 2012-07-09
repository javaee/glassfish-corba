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

package com.sun.corba.ee.impl.encoding;

import java.nio.ByteBuffer;


import com.sun.corba.ee.impl.encoding.BufferManagerWrite;
import com.sun.corba.ee.impl.misc.ORBUtility;
import com.sun.corba.ee.spi.transport.ByteBufferPool;
import com.sun.corba.ee.spi.orb.ORB;
import com.sun.corba.ee.spi.trace.Transport;
import org.glassfish.pfl.tf.spi.annotation.InfoMethod;


// Notes about the class.
// Assumptions, the ByteBuffer's position is set by the constructor's
// index variable and the ByteBuffer's limit points to the end of the
// data.
// ByteBuffer.position() tracks the current empty position in this
// buffer.
// Although, a ByteBuffer's length is often times considered to be
// it's capacity(), the context in which getLength and setLength is
// used in this object is actually this object's ByteBuffer's limit.
// In other words, getLength and setLength represent the end of the
// data in this object's ByteBuffer.

@Transport
class ByteBufferWithInfo
{
    private ORB orb;
    private ByteBuffer byteBuffer;// Marshal buffer.
    private boolean fragmented; // Did the overflow operation fragment?

    ByteBufferWithInfo(org.omg.CORBA.ORB orb,
                              ByteBuffer byteBuffer,
                              int index)
    {
        this.orb = (com.sun.corba.ee.spi.orb.ORB)orb;
        this.setByteBuffer( byteBuffer );
        position( index );
        this.setFragmented(false);
    }

    ByteBufferWithInfo(org.omg.CORBA.ORB orb, ByteBuffer byteBuffer)
    {
        this(orb, byteBuffer, 0);
    }

    ByteBufferWithInfo(org.omg.CORBA.ORB orb,
                              BufferManagerWrite bufferManager)
    {
        this(orb, bufferManager, true);
    }

    @InfoMethod
    private void bufferMessage( String head, int bbAddr, String tail ) {
    }

    @Transport
    private void bbinfo( ByteBuffer buff ) {
        if (orb.transportDebugFlag) {
            // print address of ByteBuffer gotten from pool
            int bbAddress = System.identityHashCode( buff ) ;
            bufferMessage(
                "constructor (ORB, BufferManagerWrite) - got ByteBuffer id (",
                bbAddress, ") from ByteBufferPool." ) ;
        }
    }

    // Right now, EncapsOutputStream's do not use pooled byte buffers.
    // EncapsOutputStream's is the only one that does not use pooled
    // byte buffers. Hence, the reason for the boolean 'usePooledByteBuffers'.
    // See EncapsOutputStream for additional information.

    ByteBufferWithInfo(org.omg.CORBA.ORB orb,
                              BufferManagerWrite bufferManager,
                              boolean usePooledByteBuffers)
    {
        this.orb = (com.sun.corba.ee.spi.orb.ORB)orb;

        int bufferSize = bufferManager.getBufferSize();

        if (usePooledByteBuffers) {
            ByteBufferPool byteBufferPool = this.orb.getByteBufferPool();
            this.setByteBuffer(byteBufferPool.getByteBuffer(bufferSize));

            bbinfo( getByteBuffer() ) ;
        } else {
             // don't allocate from pool, allocate non-direct ByteBuffer
             this.setByteBuffer(ByteBuffer.allocate(bufferSize));
        }

        this.position( 0 );
        this.setLength( bufferSize );
        this.setFragmented(false);
    }

    // Shallow copy constructor
    ByteBufferWithInfo (ByteBufferWithInfo bbwi)
    {
        this.orb = bbwi.orb;
        // IMPORTANT: Cannot simply assign the reference of
        //            bbwi.byteBuffer to this.byteBuffer since
        //            bbwi's can be restored via restore-able
        //            stream in CDRInputObject_1_0.java. To
        //            restore a bbwi, we must also keep the
        //            bbwi's position and limit. If we use
        //            ByteBuffer.duplicate() we'll get independent
        //            positions and limits, but the same ByteBuffer,
        //            (which is what we want).
        this.setByteBuffer( bbwi.getByteBuffer().duplicate() );
        this.setLength( bbwi.getLength() );
        this.position( bbwi.position() );
        this.setFragmented(bbwi.isFragmented());
    }

    // So IIOPOutputStream seems more intuitive
    public int getSize() 
    {
        return position();
    }

    // accessor to buffer's capacity
    public int getCapacity()
    {
        return getByteBuffer().capacity();
    }

    // accessor to buffer's length
    public int getLength()
    {
         return getByteBuffer().limit();
    }

    // accessor to fragmented
    public boolean isFragmented()
    {
        return fragmented;
    }

    public ByteBuffer getByteBuffer() {
        return byteBuffer;
    }

    // get position in this buffer
    public int position()
    {
        return getByteBuffer().position();
    }

    // set position in this buffer
    public void position(int newPosition)
    {
        getByteBuffer().position(newPosition);
    }

    // flip ByteBuffer (sets limit to position & position to 0)
    public void flip()
    {
        getByteBuffer().flip();
    }

    // mutator to buffer's length
    public void setLength(int theLength)
    {
        getByteBuffer().limit(theLength);
    }

    // mutator to fragmented
    public void setFragmented(boolean fragmented)
    {
        this.fragmented = fragmented;
    }

    // mutator to set byteBuffer
    public void setByteBuffer(ByteBuffer byteBuffer) {
        this.byteBuffer = byteBuffer;
    }
    
    // Grow byteBuffer to a size larger than position() + needed
    @Transport
    public void growBuffer( ORB orb, int numBytesNeeded )
    {
        int newLength = getLength() * 2;

        while (position() + numBytesNeeded >= newLength)
            newLength = newLength * 2;

        ByteBufferPool byteBufferPool = orb.getByteBufferPool();
        ByteBuffer newBB = byteBufferPool.getByteBuffer(newLength);

        if (orb.transportDebugFlag) {
            // print address of ByteBuffer just gotten
            int newbbAddress = System.identityHashCode(newBB);
            bufferMessage( "growBuffer() - got ByteBuffer id (",
                newbbAddress, ") from ByteBufferPool." );
        }

        this.flip();
        newBB.put(getByteBuffer());

        // return 'old' byteBuffer reference to the ByteBuffer pool
        if (orb.transportDebugFlag) {
            // print address of ByteBuffer being released
            int bbAddress = System.identityHashCode(getByteBuffer());
            bufferMessage( "growBuffer() - releasing ByteBuffer id (",
                bbAddress, ") to ByteBufferPool.");
        }
        byteBufferPool.releaseByteBuffer(getByteBuffer());

        // update the byteBuffer with a larger ByteBuffer
        setByteBuffer(newBB);

        // set this buffer's length to newLength.
        setLength(newLength);
    }
   
    public String toString()
    {
        StringBuffer str = new StringBuffer("ByteBufferWithInfo:");

        str.append(" length = " + getLength());
        str.append(" position = " + position());
        str.append(" byteBuffer = " + (getByteBuffer() == null ? "null" : "not null"));
        str.append(" fragmented = " + isFragmented());

        return str.toString();
    }

    protected void dprint(String msg)
    {
        ORBUtility.dprint("ByteBufferWithInfo", msg);
    }
}
