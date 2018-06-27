/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright (c) 2018 Oracle and/or its affiliates. All rights reserved.
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

package corba.util;

import com.sun.corba.ee.impl.encoding.CDRInputObject;
import com.sun.corba.ee.impl.protocol.MessageParserImpl;
import com.sun.corba.ee.impl.protocol.giopmsgheaders.FragmentMessage;
import com.sun.corba.ee.impl.protocol.giopmsgheaders.Message;
import com.sun.corba.ee.impl.protocol.giopmsgheaders.Message_1_2;
import com.sun.corba.ee.impl.transport.ConnectionImpl;
import com.sun.corba.ee.spi.ior.iiop.GIOPVersion;
import com.sun.corba.ee.spi.orb.ORB;
import com.sun.corba.ee.spi.transport.MessageData;

import java.nio.ByteBuffer;

public class TransportManagerUtil {

    public static MessageData getMessageData(byte[][] data, ORB orb) {
        ConnectionImpl connection = new ConnectionImpl(orb) ;

        final Message[] messages = new Message[data.length] ;
        Message firstMessage = null ;
        CDRInputObject inobj = null ;

        for (int ctr=0; ctr<data.length; ctr++) {
            MessageParserImpl parser = new MessageParserImpl(orb, null);
            parser.offerBuffer(ByteBuffer.wrap(data[ctr]));
            Message message = parser.getMessageMediator().getDispatchHeader();
            ByteBuffer msgByteBuffer = parser.getMsgByteBuffer();
            if (message.getGIOPVersion().equals( GIOPVersion.V1_2 )) {
                ((Message_1_2) message).unmarshalRequestID(msgByteBuffer) ;
            }

            messages[ctr] = message;

            // Check that moreFragments == (ctr < messages.length)?

            if (inobj == null) {
                firstMessage = message;
                inobj = new CDRInputObject(orb, connection, msgByteBuffer, message) ;
                inobj.performORBVersionSpecificInit() ;
            } else {
                inobj.addFragment( (FragmentMessage) message, msgByteBuffer );
            }
        }

        // Unmarshal all the data in the first message.  This may
        // cause other fragments to be read.
        firstMessage.read( inobj ) ;

        final CDRInputObject resultObj = inobj ;

        return new MessageData() {
           public Message[] getMessages() { return messages ; }
           public CDRInputObject getStream() { return resultObj ; }
        } ;
    }

    /** Analyze the header of a message.  This provides enough information to
     * classify the message and group related messages together for use in
     * the getMessageData method.  Also, if data is a GIOP 1.2 message,
     * the result of this call will contain a valid request ID.
     */
    public static Message getMessage(byte[] data, ORB orb) {
        MessageParserImpl parser = new MessageParserImpl(orb, null);
        parser.offerBuffer(ByteBuffer.wrap(data));
        Message msg = parser.getMessageMediator().getDispatchHeader();
        if (msg.getGIOPVersion().equals( GIOPVersion.V1_2 ))
            ((Message_1_2)msg).unmarshalRequestID( parser.getMsgByteBuffer() ) ;

        return msg ;
    }
}
