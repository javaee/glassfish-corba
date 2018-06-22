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

package com.sun.corba.ee.spi.protocol;

import java.nio.ByteBuffer;

import com.sun.corba.ee.spi.orb.ORB;
import com.sun.corba.ee.spi.trace.Transport;
import com.sun.corba.ee.spi.transport.Connection;

import com.sun.corba.ee.impl.protocol.giopmsgheaders.Message;


/**
 *
 * An interface that knows how to parse bytes into a protocol data unit.
 */
public interface MessageParser {

    @Transport
    ByteBuffer getNewBufferAndCopyOld(ByteBuffer byteBuffer);

    /**
     * Is this MessageParser expecting more data ?
     *
     * This method is typically called after a call to <code>parseBytes()</code>
     * to determine if the <code>ByteBuffer</code> which has been parsed
     * contains a partial <code>Message</code>.
     *
     * @return - <code>true</code> if more bytes are needed to construct a
     *           <code>Message</code>.  <code>false</code>, if no 
     *           additional bytes remain to be parsed into a <code>Message</code>.
     */
    boolean isExpectingMoreData();

    /**
     * If there are sufficient bytes in the <code>ByteBuffer</code> to compose a
     * <code>Message</code>, then return a newly initialized <code>Message</code>.
     * Otherwise, return null.
     *
     * When this method is first called, it is assumed that 
     * <code>ByteBuffer.position()</code> points to the location in the 
     * <code>ByteBuffer</code> where the beginning of the first
     * <code>Message</code> begins.
     * 
     * If there is no partial <code>Message</code> remaining in the 
     * <code>ByteBuffer</code> when this method exits, this method will e
     * <code>this.expectingMoreData</code> to <code>false</code>.
     * Otherwise, it will be set to <code>true</code>.
     * 
     * Callees of this method may check <code>isExpectingMoreData()</code> 
     * subsequently to determine if this <code>MessageParser</code> is expecting 
     * more data to complete a protocol data unit.  Callees may also 
     * subsequently check <code>hasMoreBytesToParse()</code> to determine if this 
     * <code>MessageParser</code> has more data to parse in the given
     * <code>ByteBuffer</code>.
     *
     * @return <code>Message</code> if one is found in the <code>ByteBuffer</code>.
     *         Otherwise, returns null.
     */
    // REVISIT - This interface should be declared without a CorbaConnection.
    //           As a result, this interface will likely be deprecated in a
    //           future release in favor of Message parseBytes(ByteBuffer byteBuffer)
    Message parseBytes(ByteBuffer byteBuffer, Connection connection);

    /**
     * Are there more bytes to be parsed in the <code>ByteBuffer</code> given
     * to this MessageParser's <code>parseBytes</code> ?
     *
     * This method is typically called after a call to <code>parseBytes()</code>
     * to determine if the <code>ByteBuffer</code> has more bytes which need to
     * parsed into a <code>Message</code>.
     *
     * @return <code>true</code> if there are more bytes to be parsed.
     *         Otherwise <code>false</code>.
     */
    boolean hasMoreBytesToParse();

    /**
     * Set the starting position where the next message in the
     * <code>ByteBuffer</code> given to <code>parseBytes()</code> begins.
     */
    void setNextMessageStartPosition(int position);

    /**
     * Get the starting position where the next message in the
     * <code>ByteBuffer</code> given to <code>parseBytes()</code> begins.
     */
    int getNextMessageStartPosition();

    /**
     * Return the suggested number of bytes needed to hold the next message
     * to be parsed.
     */
    int getSizeNeeded();

    /**
     * Returns the byte buffer (if any) associated with the last message returned.
     */
    ByteBuffer getMsgByteBuffer();

    /**
     * Offers an input buffer to the parser. Position must be set to 0, and the buffer must contain at least the start
     * of a GIOP message. The parser will consume what it can and make the remainder available in {@link #getRemainderBuffer}
     * @param buffer a buffer containing at least the start of a GIOP message.
     */
    void offerBuffer(ByteBuffer buffer);

    /**
     * Returns a buffer containing whatever is left after processing the buffer provided in {@link #offerBuffer(ByteBuffer)},
     * which could be the same buffer. The buffer could also be null if all data has been consumed.
     * @return a byte buffer representing data which still needs to be processed.
     */
    ByteBuffer getRemainderBuffer();

    /**
     * Returns the full message constructed by the last call to {@link #offerBuffer(ByteBuffer)}. Will be null if
     * the last such call did not complete a message.
     * @return a complete message, wrapped in a message mediator.
     */
    MessageMediator getMessageMediator();

    /**
     * Checks for a stalled or rogue client. If in the middle of receiving a message and the time exceeds the limit,
     * will throw a communications failure exception.
     * @param timeSinceLastInput the number of milliseconds since the last input was received.
     */
    void checkTimeout(long timeSinceLastInput);

    boolean isExpectingFragments();
}
