package corba.util;

import com.sun.corba.ee.impl.encoding.BufferManagerRead;
import com.sun.corba.ee.impl.encoding.CDRInputObject;
import com.sun.corba.ee.impl.protocol.giopmsgheaders.FragmentMessage;
import com.sun.corba.ee.impl.protocol.giopmsgheaders.Message;
import com.sun.corba.ee.impl.protocol.giopmsgheaders.MessageBase;
import com.sun.corba.ee.impl.protocol.giopmsgheaders.Message_1_2;
import com.sun.corba.ee.impl.transport.BufferConnectionImpl;
import com.sun.corba.ee.spi.ior.iiop.GIOPVersion;
import com.sun.corba.ee.spi.orb.ORB;
import com.sun.corba.ee.spi.transport.Connection;
import com.sun.corba.ee.spi.transport.MessageData;

import java.io.IOException;
import java.nio.ByteBuffer;

public class TransportManagerUtil {

    public static MessageData getMessageData(byte[][] data, ORB orb) {
        Connection connection = new BufferConnectionImpl(orb) ;
        for (int ctr=0; ctr<data.length; ctr++) {
            byte[] message = data[ctr] ;
            ByteBuffer bb = ByteBuffer.allocate( message.length ) ;
            bb.put( message ) ;
            bb.position( 0 ) ;
            try {
                connection.write( bb ) ;
            } catch (IOException exc) {
                // should never happen in this case
            }
        }

        final Message[] messages = new Message[data.length] ;
        int requestID = 0 ;
        Message firstMessage = null ;
        Message msg = null ;
        CDRInputObject inobj = null ;
        BufferManagerRead buffman = null ;

        for (int ctr=0; ctr<data.length; ctr++) {
            msg = MessageBase.readGIOPMessage(orb, connection) ;
            messages[ctr] = msg ;
            if (msg.getGIOPVersion().equals( GIOPVersion.V1_2 ))
                ((Message_1_2)msg).unmarshalRequestID( msg.getByteBuffer() ) ;

            // Check that moreFragments == (ctr < messages.length)?

            if (ctr==0) {
                firstMessage = msg ;
                inobj = new CDRInputObject(orb, connection,
                    msg.getByteBuffer(), msg ) ;
                buffman = inobj.getBufferManager() ;
                inobj.performORBVersionSpecificInit() ;
            } else {
                buffman.processFragment( msg.getByteBuffer(), (FragmentMessage)msg ) ;
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
        Connection connection = new BufferConnectionImpl(orb) ;
        ByteBuffer bb = ByteBuffer.allocate( data.length ) ;
        bb.put( data ) ;
        bb.position( 0 ) ;
        try {
            connection.write( bb ) ;
        } catch (IOException exc) {
            // should never happen in this case
        }

        Message msg = MessageBase.readGIOPMessage(orb, connection) ;
        if (msg.getGIOPVersion().equals( GIOPVersion.V1_2 ))
            ((Message_1_2)msg).unmarshalRequestID( msg.getByteBuffer() ) ;

        return msg ;
    }
}
