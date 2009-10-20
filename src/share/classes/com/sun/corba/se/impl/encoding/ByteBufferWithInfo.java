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

package com.sun.corba.se.impl.encoding;

import java.nio.ByteBuffer;


import com.sun.corba.se.impl.encoding.BufferManagerWrite;
import com.sun.corba.se.impl.orbutil.ORBUtility;
import com.sun.corba.se.spi.transport.ByteBufferPool;
import com.sun.corba.se.spi.orb.ORB;


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

public class ByteBufferWithInfo
{
    private ORB orb;
    private boolean debug;
    private ByteBuffer byteBuffer;// Marshal buffer.
    private int     needed;     // How many more bytes are needed on overflow.
    private boolean fragmented; // Did the overflow operation fragment?

    public ByteBufferWithInfo(org.omg.CORBA.ORB orb,
                              ByteBuffer byteBuffer,
                              int index)
    {
        this.orb = (com.sun.corba.se.spi.orb.ORB)orb;
        debug = this.orb.transportDebugFlag;
	this.setByteBuffer(byteBuffer);
        position(index);
	this.setNumberOfBytesNeeded(0);
        this.setFragmented(false);
    }

    public ByteBufferWithInfo(org.omg.CORBA.ORB orb, ByteBuffer byteBuffer)
    {
	this(orb, byteBuffer, 0);
    }

    public ByteBufferWithInfo(org.omg.CORBA.ORB orb,
                              BufferManagerWrite bufferManager)
    {
        this(orb, bufferManager, true);
    }

    // Right now, EncapsOutputStream's do not use pooled byte buffers.
    // EncapsOutputStream's is the only one that does not use pooled
    // byte buffers. Hence, the reason for the boolean 'usePooledByteBuffers'.
    // See EncapsOutputStream for additional information.

    public ByteBufferWithInfo(org.omg.CORBA.ORB orb, 
                              BufferManagerWrite bufferManager,
                              boolean usePooledByteBuffers)
    {
        this.orb = (com.sun.corba.se.spi.orb.ORB)orb;
        debug = this.orb.transportDebugFlag;

        int bufferSize = bufferManager.getBufferSize();

        if (usePooledByteBuffers)
        {
            ByteBufferPool byteBufferPool = this.orb.getByteBufferPool();
            this.setByteBuffer(byteBufferPool.getByteBuffer(bufferSize));

            if (debug)
            {
                // print address of ByteBuffer gotten from pool
                int bbAddress = System.identityHashCode(getByteBuffer());
                StringBuffer sb = new StringBuffer(80);
                sb.append("constructor (ORB, BufferManagerWrite) - got ")
                  .append("ByteBuffer id (").append(bbAddress)
                  .append(") from ByteBufferPool.");
                String msgStr = sb.toString();
                dprint(msgStr);
            }
        }
        else
        {
             // don't allocate from pool, allocate non-direct ByteBuffer
             this.setByteBuffer(ByteBuffer.allocate(bufferSize));
        }

        this.position(0);
        this.setLength(bufferSize);
        this.setNumberOfBytesNeeded(0);
        this.setFragmented(false);
    }

    // Shallow copy constructor
    public ByteBufferWithInfo (ByteBufferWithInfo bbwi)
    {
        this.orb = bbwi.orb;
        this.debug = bbwi.debug;
	// IMPORTANT: Cannot simply assign the reference of
	//            bbwi.byteBuffer to this.byteBuffer since
	//            bbwi's can be restored via restore-able
	//            stream in CDRInputObject_1_0.java. To
	//            restore a bbwi, we must also keep the
	//            bbwi's position and limit. If we use
	//            ByteBuffer.duplicate() we'll get independent
	//            positions and limits, but the same ByteBuffer,
	//            (which is what we want).
        this.setByteBuffer(bbwi.getByteBuffer().duplicate());
        this.setLength(bbwi.getLength());
        this.position(bbwi.position());
        this.setNumberOfBytesNeeded(bbwi.getNumberOfBytesNeeded());
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

    // get number of bytes needed on overflow / underflow
    private int getNumberOfBytesNeeded() {
        return needed;
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

    // mutator to set number of bytes needed on overflow / underflow
    public void setNumberOfBytesNeeded(int needed) {
        this.needed = needed;
    }

    // mutator to set byteBuffer
    public void setByteBuffer(ByteBuffer byteBuffer) {
        this.byteBuffer = byteBuffer;
    }
    
    // Grow byteBuffer to a size larger than position() + needed
    public void growBuffer(com.sun.corba.se.spi.orb.ORB orb)
    {
        int newLength = getLength() * 2;

        while (position() + getNumberOfBytesNeeded() >= newLength)
            newLength = newLength * 2;

        ByteBufferPool byteBufferPool = orb.getByteBufferPool();
        ByteBuffer newBB = byteBufferPool.getByteBuffer(newLength);

        if (debug) {
            // print address of ByteBuffer just gotten
            int newbbAddress = System.identityHashCode(newBB);
            StringBuffer sb = new StringBuffer(80);
            sb.append("growBuffer() - got ByteBuffer id (");
            sb.append(newbbAddress).append(") from ByteBufferPool.");
            String msgStr = sb.toString();
            dprint(msgStr);
        }

	this.flip();
        newBB.put(getByteBuffer());

        // return 'old' byteBuffer reference to the ByteBuffer pool
        if (debug) {
            // print address of ByteBuffer being released
            int bbAddress = System.identityHashCode(getByteBuffer());
            StringBuffer sb = new StringBuffer(80);
            sb.append("growBuffer() - releasing ByteBuffer id (");
            sb.append(bbAddress).append(") to ByteBufferPool.");
            String msgStr2 = sb.toString();
            dprint(msgStr2);
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
        str.append(" needed = " + getNumberOfBytesNeeded());
        str.append(" byteBuffer = " + (getByteBuffer() == null ? "null" : "not null"));
        str.append(" fragmented = " + isFragmented());

        return str.toString();
    }

    protected void dprint(String msg)
    {
        ORBUtility.dprint("ByteBufferWithInfo", msg);
    }
}
