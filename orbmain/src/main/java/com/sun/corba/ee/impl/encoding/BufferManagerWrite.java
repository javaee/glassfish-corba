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

package com.sun.corba.ee.impl.encoding;

import com.sun.corba.ee.spi.orb.ORB;

import com.sun.corba.ee.spi.logging.ORBUtilSystemException;

import java.nio.ByteBuffer;

/**
 * Defines the contract between the BufferManager and
 * CDR stream on the writing side.  The CDR stream
 * calls back to the BufferManagerWrite when it needs
 * more room in the output buffer to continue.  The
 * BufferManager can then grow the output buffer or
 * use some kind of fragmentation technique.
 */
public abstract class BufferManagerWrite
{
    protected ORB orb ;
    protected static final ORBUtilSystemException wrapper =
        ORBUtilSystemException.self ;

    BufferManagerWrite( ORB orb ) 
    {
        this.orb = orb ;
    }

    /**
     * Has the stream sent out any fragments so far?
     */
    public abstract boolean sentFragment();

    /**
     * Has the entire message been sent?  (Has
     * sendMessage been called?)
     */
    public boolean sentFullMessage() {
        return sentFullMessage;
    }

    /**
     * Returns the correct buffer size for this type of
     * buffer manager as set in the ORB.
     */
    public abstract int getBufferSize();

    /*
     * Invoked when we run out of room to write. Must either expand the buffer or send it as a fragment and clear it.
     */
    protected abstract ByteBuffer overflow(ByteBuffer byteBuffer, int numBytesNeeded);

    /**
     * Returns true if this buffer manager fragments when an overflow occurs.
     */
    public abstract boolean isFragmentOnOverflow();

    /**
     * Called after Stub._invoke (i.e., before complete message has been sent).
     *
     * IIOPOutputStream.writeTo called from IIOPOutputStream.invoke
     *
     * Case: overflow was never called (bbwi.buf contains complete message).
     *       Backpatch size field.
     *       If growing or collecting:
     *          this.bufQ.put(bbwi).
     *          this.bufQ.iterate // However, see comment in getBufferQ
     *             this.connection.send(fragment)
     *       If streaming:
     *          this.connection.send(bbwi).
     *
     * Case: overflow was called N times (bbwi.buf contains last buffer).
     *       If growing or collecting:
     *          this.bufQ.put(bbwi).
     *          backpatch size field in first buffer.
     *          this.bufQ.iterate // However, see comment in getBufferQ
     *             this.connection.send(fragment)
     *       If streaming:
     *          backpatch fragment size field in bbwi.buf.
     *          Set no more fragments bit.
     *          this.connection.send(bbwi).
     */

    public abstract void sendMessage ();


    /**
     * A reference to the connection level stream will be required when
     * sending fragments.
     */
    public void setOutputObject(Object outputObject) {
        this.outputObject = outputObject;
    }

    /**
     * Close the BufferManagerWrite and do any outstanding cleanup.
     */
     abstract public void close();

    // XREVISIT - Currently a java.lang.Object during
    // the rip-int-generic transition.  Should eventually
    // become a GIOPOutputObject.
    protected Object outputObject;

    protected boolean sentFullMessage = false;
}

