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

package com.sun.corba.ee.impl.protocol;

import java.nio.ByteBuffer;
import java.util.LinkedList;
import java.util.List;

import com.sun.corba.ee.spi.orb.ORB;
import com.sun.corba.ee.spi.protocol.RequestId;
import com.sun.corba.ee.spi.transport.Connection;
import com.sun.corba.ee.spi.protocol.MessageParser;

import com.sun.corba.ee.impl.misc.ORBUtility;
import com.sun.corba.ee.impl.protocol.giopmsgheaders.Message;
import com.sun.corba.ee.impl.protocol.giopmsgheaders.MessageBase;
import com.sun.corba.ee.spi.trace.Giop;
import com.sun.corba.ee.spi.trace.Transport;
import org.glassfish.pfl.tf.spi.annotation.InfoMethod;


/**
 *
 * An implementation of a <code>MessageParser</code> that knows how to parse
 * bytes into a GIOP protocol data unit.
 *
 *
 */
@Transport
@Giop
public class MessageParserImpl implements MessageParser {
    final private ORB orb;
    private boolean expectingMoreData;
    private boolean moreBytesToParse;
    private int nextMsgStartPos;
    private int sizeNeeded;
    /**
     * A list of request ids awaiting final fragments.  When the size of
     * this list is larger than 0, we have received a fragmented message
     * and expecting to recieve more message fragments for that given
     * request id on this list.  Hence, if there are entries in this list
     * we are expecting more data to arrive.
     * We are using a List here rather than a Set since the size of the
     * List is expected to be rather small, (i.e. less than size 10).
     */
    private List<RequestId> fragmentList;
    
    /** Creates a new instance of MessageParserImpl */
    public MessageParserImpl(ORB orb) {
        this.orb = orb;
        this.expectingMoreData = false;
        this.moreBytesToParse = false;
        this.nextMsgStartPos = 0;
        this.fragmentList = new LinkedList<RequestId>();
        this.sizeNeeded = orb.getORBData().getReadByteBufferSize();
    }
    
    /**
     * Is this MessageParser expecting more data ?
     * @return - True if more bytes are needed to construct at least one
     *           GIOP protocol data unit.  False, if no additional bytes are
     *           remain to be parsed into a GIOP protocol data unit.
     */
    public boolean isExpectingMoreData() {
        return expectingMoreData;
    }

    @InfoMethod
    private void display( String msg ) { }

    @InfoMethod
    private void display( String msg, int value ) { }

    @InfoMethod
    private void display( String msg, Object value ) { }

    
    /**
     * If there are sufficient bytes in the <code>ByteBuffer</code> to compose a
     * <code>Message</code>, then return a newly initialized <code>Message</code>.
     * Otherwise, return null.
     *
     * The first time <code>parseBytes</code> is invoked, it is assumed the
     * <code>ByteBuffer.position()</code> is pointing to the beginning of a
     * GIOP message and <code>ByteBuffer.limit()</code> is pointing to the
     * end of the data in <code>ByteBuffer</code>.
     *
     * When this method exits, <code>this.nextMsgStartPos</code> points to the
     * location in the ByteBuffer where the beginning of the next
     * <code>Message</code> begins.  If there is no partial <code>Message</code>
     * remaining in this <code>ByteBuffer</code> and there are message fragment
     * request ids awaiting a final fragment when this method exits, this
     * method will set <code>this.expectingMoreData</code> to <code>false</code>.
     * Otherwise, it will be set to <code>true</code>.
     *
     * Callees of this method may check <code>isExpectingMoreData()</code> to
     * determine if this <code>MessageParser</code> is expecting more data to
     * complete a protocol data unit.  Callees may also check
     * <code>hasMoreBytesToParse()</code> to determine if this
     * <code>MessageParser</code> has more data to parse in the given
     * <code>ByteBuffer</code>.
     *
     * Use cases and resulting states:
     * 1.) Not enough bytes to parse a GIOP header
     *     . moreBytesToParse = false
     *     . expectingMoreData = true
     *     . nextMsgStartPos is unchanged
     *     . byteBuffer.position = byteBuffer.limit
     *     . byteBuffer.limit = byteBuffer.capacity
     *     . sizeNeeded = orb.getORBData().getReadByteBufferSize()
     * 2.) Parsed a GIOP header, but not enough bytes to parse GIOP body
     *     . moreBytesToParse = false
     *     . expectingMoreData = true
     *     . nextMsgStartPos is unchanged
     *     . byteBuffer.position = byteBuffer.limit
     *     . byteBuffer.limit = byteBuffer.capacity
     *     . sizeNeeded = message.getSize()
     * 3.) Parsed a GIOP fragment message
     *     . moreBytesToParse = true
     *     . expectingMoreData = true
     *     . nextMsgStartPos = old nextMsgStartPos + message size
     *     . byteBuffer.position = new nextMsgStartPos
     *     . byteBuffer.limit unchanged
     *     . sizeNeeded = orb.getORBData().getReadByteBufferSize()
     * 4.) Parsed a GIOP final fragment and waiting on other request fragments
     *     . moreBytesToParse = true
     *     . expectingMoreData = true
     *     . nextMsgStartPos = old nextMsgStartPos + message size
     *     . byteBuffer.position = new nextMsgStartPos
     *     . byteBuffer.limit unchanged
     *     . sizeNeeded = orb.getORBData().getReadByteBufferSize()
     * 5.) Parsed a GIOP final fragment, not waiting on other request fragments
     *     and more bytes in byteBuffer to be parsed
     *     . moreBytesToParse = true
     *     . expectingMoreData = false
     *     . nextMsgStartPos = old nextMsgStartPos + message size
     *     . byteBuffer.position = new nextMsgStartPos
     *     . byteBuffer.limit unchanged
     *     . sizeNeeded = orb.getORBData().getReadByteBufferSize()
     * 6.) Parsed a GIOP final fragment, not waiting on other request fragments
     *     and no more bytes in byteBuffer to be parsed
     *     . moreBytesToParse = false
     *     . expectingMoreData = false
     *     . nextMsgStartPos = old nextMsgStartPos + message size
     *     . byteBuffer.position = new nextMsgStartPos
     *     . byteBuffer.limit unchanged
     *     . sizeNeeded = orb.getORBData().getReadByteBufferSize()
     *
     * @return <code>Message</code> if one is found in the <code>ByteBuffer</code>.
     *         Otherwise, returns null.
     */

    @Giop
    private void parseBytesGiopInfo( ByteBuffer msgByteBuffer,
        Message message ) {
        if (orb.giopDebugFlag) {
            // For debugging purposes, create view buffer
            ByteBuffer viewBuf = msgByteBuffer.asReadOnlyBuffer();
            viewBuf.position(viewBuf.limit());
            RequestId requestId =
                      MessageBase.getRequestIdFromMessageBytes(message);
            display( "Message Type", message.getType() ) ;
            display( "Request Id", requestId.toString() ) ;
            display( "Successfully parsed with sliced ByteBuffer", 
                msgByteBuffer.toString() ) ;
            ORBUtility.printBuffer("GIOP Message Body",
                    viewBuf, System.out);
        }
    }

    @Transport
    public Message parseBytes(ByteBuffer byteBuffer, Connection connection) {
        Message message = null;
        int bytesInBuffer = byteBuffer.limit() - nextMsgStartPos;
        // is there enough bytes available for a message header?
        if (bytesInBuffer >= Message.GIOPMessageHeaderLength) {
            // get message header
            message = MessageBase.parseGiopHeader(orb, connection,
                                                  byteBuffer, nextMsgStartPos);
            
            // is there enough bytes for a message body?
            if (bytesInBuffer >= message.getSize()) {
                // slice the ByteBuffer into a GIOP PDU
                int savedLimit = byteBuffer.limit();
                byteBuffer.position(nextMsgStartPos).
                        limit(nextMsgStartPos + message.getSize());
                ByteBuffer msgByteBuffer = byteBuffer.slice();
                // update nextMsgStartPos and byteBuffer state
                nextMsgStartPos = byteBuffer.limit();
                byteBuffer.position(nextMsgStartPos).limit(savedLimit);
                message.setByteBuffer(msgByteBuffer);

                parseBytesGiopInfo( msgByteBuffer, message );

                if (MessageBase.messageSupportsFragments(message)) {
                    // are there more fragments to follow?
                    if (message.moreFragmentsToFollow()) {
                        // Add to fragmentList if not already there
                        RequestId requestId =
                              MessageBase.getRequestIdFromMessageBytes(message);
                        if (!fragmentList.contains(requestId)) {
                            fragmentList.add(requestId);
                            display( "Added to fragmentList", requestId ) ;
                        } else {
                            display( "fragmentList alreadty has an entry for",
                                requestId ) ;
                        }
                    } else {
                        // no fragments to follow
                        if (message.getType() == MessageBase.GIOPFragment ||
                            message.getType() == MessageBase.GIOPCancelRequest) {
                            // remove request id from fragmentList
                            RequestId requestId =
                                MessageBase.getRequestIdFromMessageBytes(message);
                            if (fragmentList.size() > 0 &&
                                fragmentList.remove(requestId)) {
                                display( "Removed from fragmentList", 
                                    requestId ) ;
                            }
                        }
                    }
                    // if request id's are outstanding, we're expect more data
                    if (fragmentList.size() > 0) {
                        expectingMoreData = true;
                    } else {
                        // not waiting for more fragments
                        expectingMoreData = false;
                    }
                }
                // set last remaining states,
                // any bytes remaining to be parsed?
                if (byteBuffer.hasRemaining()) {
                    // more bytes to parse
                    moreBytesToParse = true;
                } else {
                    // no more bytes to parse
                    moreBytesToParse = false;
                    // set byteBuffer limit to end of buffer so next
                    // read will read up to capacity - position bytes
                    byteBuffer.limit(byteBuffer.capacity());
                }
                sizeNeeded = orb.getORBData().getReadByteBufferSize();
            } else {
                if (orb.transportDebugFlag) {
                    // not enough bytes available for message body
                    display( "Not enough bytes available in ByteBuffer for a "
                        + "complete GIOP message: bytes available ", 
                        bytesInBuffer ) ;
                    display( "bytes needed", message.getSize() ) ;
                    display( "ByteBuffer state", byteBuffer.toString() ) ;
                }

                // set state for next parseBytes invocation
                moreBytesToParse = false;
                expectingMoreData = true;
                // nextMsgStartPos unchanged
                byteBuffer.position(byteBuffer.limit()).limit(byteBuffer.capacity());
                sizeNeeded = message.getSize();
                message = null;
            }
        } else {
            // not enough bytes for message header
            if (orb.transportDebugFlag) {
                // not enough bytes available for message body
                display( "Not enough bytes available in ByteBuffer "
                    + "to parse 12-byte GIOP header" ) ;
                display( "bytes available", bytesInBuffer) ;
                display( "ByteBuffer state", byteBuffer.toString() ) ;
            }
            // set state for next parseBytes invocation
            moreBytesToParse = false;
            expectingMoreData = true;
            // nextMsgStartPos unchanged
            byteBuffer.position(byteBuffer.limit()).limit(byteBuffer.capacity());
            sizeNeeded = orb.getORBData().getReadByteBufferSize();
        }
        return message;
    }
    
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
    public boolean hasMoreBytesToParse() {
        return moreBytesToParse;
    }
    
    /**
     * Set the starting position where the next message in the
     * <code>ByteBuffer</code> given to <code>parseBytes()</code> begins.
     */
    public void setNextMessageStartPosition(int position) {
        this.nextMsgStartPos = position;
    }
    
    /**
     * Get the starting position where the next message in the
     * <code>ByteBuffer</code> given to <code>parseBytes()</code> begins.
     */
    public int getNextMessageStartPosition() {
        return this.nextMsgStartPos;
    }
    
    /** Return a string representing this MessageParser's state */
    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append(toStringPrefix()).append("]");
        return sb.toString();
    }
    
    /**
     * Return a string representing this MessageParser's state and
     * a ByteBuffer's state.
     */
    private String stateString(ByteBuffer byteBuffer) {
        StringBuilder sb = new StringBuilder();
        sb.append(toStringPrefix()).append(" ").append(byteBuffer.toString());
        return sb.toString();
    }
    
    /** Return a common String prefix representing this MessageParser's state */
    private String toStringPrefix() {
        StringBuilder sb = new StringBuilder();
        sb.append("MessageParserImpl[nextMsgStartPos=" + nextMsgStartPos +
                ", expectingMoreData=" + expectingMoreData +
                ", moreBytesToParse=" + moreBytesToParse +
                ", fragmentList size=" + fragmentList.size() +
                ", size needed=" + sizeNeeded + "]");
        return sb.toString();
    }

    /**
     * Return the suggested number of bytes needed to hold the next message
     * to be parsed.
     */
    public int getSizeNeeded() {
        return sizeNeeded;
    }
}
