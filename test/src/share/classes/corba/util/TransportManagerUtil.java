package corba.util;

import com.sun.corba.ee.impl.encoding.BufferManagerRead;
import com.sun.corba.ee.impl.encoding.CDRInputObject;
import com.sun.corba.ee.impl.protocol.MessageParserImpl;
import com.sun.corba.ee.impl.protocol.giopmsgheaders.FragmentMessage;
import com.sun.corba.ee.impl.protocol.giopmsgheaders.Message;
import com.sun.corba.ee.impl.protocol.giopmsgheaders.Message_1_2;
import com.sun.corba.ee.impl.transport.ConnectionImpl;
import com.sun.corba.ee.spi.ior.iiop.GIOPVersion;
import com.sun.corba.ee.spi.orb.ORB;
import com.sun.corba.ee.spi.transport.MessageData;

import java.io.IOException;
import java.nio.ByteBuffer;

public class TransportManagerUtil {

    public static MessageData getMessageData(byte[][] data, ORB orb) {
        ConnectionImpl connection = new ConnectionImpl(orb) ;

        final Message[] messages = new Message[data.length] ;
        Message firstMessage = null ;
        CDRInputObject inobj = null ;

        for (int ctr=0; ctr<data.length; ctr++) {
            Message msg = getMessage(data[ctr], orb);
            messages[ctr] = msg ;
            if (msg.getGIOPVersion().equals( GIOPVersion.V1_2 ))
                ((Message_1_2)msg).unmarshalRequestID( msg.getByteBuffer() ) ;

            // Check that moreFragments == (ctr < messages.length)?

            if (inobj == null) {
                firstMessage = msg;
                inobj = new CDRInputObject(orb, connection, msg.getByteBuffer(), msg ) ;
                inobj.performORBVersionSpecificInit() ;
            } else {
                inobj.addFragment( (FragmentMessage)msg, msg.getByteBuffer() ); ;
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
        MessageParserImpl parser = new MessageParserImpl(orb);
        Message msg = parser.parseBytes(ByteBuffer.wrap(data), null);
        if (msg.getGIOPVersion().equals( GIOPVersion.V1_2 ))
            ((Message_1_2)msg).unmarshalRequestID( msg.getByteBuffer() ) ;

        return msg ;
    }
}
