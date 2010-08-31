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

package com.sun.corba.se.impl.protocol.giopmsgheaders;

import java.io.IOException;
import java.lang.reflect.Constructor;
import java.nio.ByteBuffer;
import java.util.Iterator;

import org.omg.CORBA.CompletionStatus;
import org.omg.CORBA.SystemException;
import org.omg.IOP.TaggedProfile;

import com.sun.corba.se.spi.ior.ObjectKey;
import com.sun.corba.se.spi.ior.IOR;
import com.sun.corba.se.spi.ior.iiop.IIOPProfile;
import com.sun.corba.se.spi.ior.iiop.IIOPFactories;
import com.sun.corba.se.spi.ior.iiop.IIOPProfileTemplate ;
import com.sun.corba.se.spi.ior.iiop.GIOPVersion;
import com.sun.corba.se.spi.ior.iiop.RequestPartitioningComponent;
import com.sun.corba.se.spi.orb.ORB;
import com.sun.corba.se.spi.orb.ObjectKeyCacheEntry;
import com.sun.corba.se.spi.orbutil.ORBClassLoader;
import com.sun.corba.se.spi.protocol.CorbaRequestId;
import com.sun.corba.se.spi.servicecontext.ServiceContexts;
import com.sun.corba.se.spi.transport.CorbaConnection;
import com.sun.corba.se.spi.transport.CorbaTransportManager;

import com.sun.corba.se.impl.logging.ORBUtilSystemException ;
import com.sun.corba.se.impl.orb.ObjectKeyCacheEntryNoObjectAdapterImpl;
import com.sun.corba.se.impl.orbutil.ORBUtility;
import com.sun.corba.se.spi.orbutil.ORBConstants;
import com.sun.corba.se.impl.protocol.AddressingDispositionException;
import com.sun.corba.se.impl.protocol.CorbaRequestIdImpl;
import com.sun.corba.se.impl.transport.MessageTraceManagerImpl;
import com.sun.corba.se.spi.trace.Giop;

/**
 * This class acts as the base class for the various GIOP message types. This
 * also serves as a factory to create various message types. We currently
 * support GIOP 1.0, 1.1 and 1.2 message types.
 *
 * @author Ram Jeyaraman 05/14/2000
 * @version 1.0
 */

@Giop
public abstract class MessageBase implements Message{

    // This is only used when the giopDebug flag is
    // turned on.
    public byte[] giopHeader;
    private ByteBuffer byteBuffer;
    private int threadPoolToUse;

    // (encodingVersion == 0x00) implies CDR encoding, 
    // (encodingVersion >  0x00) implies Java serialization encoding version.
    private byte encodingVersion = ORBConstants.CDR_ENC_VERSION;

    private static ORBUtilSystemException wrapper = 
	ORB.getStaticLogWrapperTable().get_RPC_PROTOCOL_ORBUtil() ;

    // Static methods

    public static String typeToString(int type)
    {
	return typeToString((byte)type);
    }

    public static String typeToString(byte type)
    {
	String result = type + "/";
	switch (type) {
	case GIOPRequest         : result += "GIOPRequest";         break;
	case GIOPReply           : result += "GIOPReply";           break;
	case GIOPCancelRequest   : result += "GIOPCancelRequest";   break;
	case GIOPLocateRequest   : result += "GIOPLocateRequest";   break;
	case GIOPLocateReply     : result += "GIOPLocateReply";     break;
	case GIOPCloseConnection : result += "GIOPCloseConnection"; break;
	case GIOPMessageError    : result += "GIOPMessageError";    break;
	case GIOPFragment        : result += "GIOPFragment";        break;
	default                  : result += "Unknown";             break;
	}
	return result;
    }

    public static MessageBase readGIOPMessage(ORB orb, CorbaConnection connection)
    {
	MessageBase msg = readGIOPHeader( orb, connection ) ;
	msg = (MessageBase)readGIOPBody( orb, connection, (Message)msg ) ;
	return msg ;
    }

    // NOTE: This method is used only when the ORB is configured with
    //       "useNIOSelectToWait=false", aka use blocking Sockets/SocketChannels
    public static MessageBase readGIOPHeader( ORB orb, CorbaConnection connection )
    {
	ByteBuffer buf = null;

	try {
	    buf = connection.read(GIOPMessageHeaderLength,
			  0, GIOPMessageHeaderLength );
	} catch (IOException e) {
	    throw wrapper.ioexceptionWhenReadingConnection(e, connection.toString());
	}
        
        MessageBase msg = parseGiopHeader(orb, connection, buf, 0);
        
	return msg;
    }

    public static MessageBase parseGiopHeader(ORB orb, 
                                              CorbaConnection connection,
                                              ByteBuffer buf,
                                              int startPosition) {
        
        CorbaTransportManager ctm = orb.getTransportManager() ;
	MessageTraceManagerImpl mtm = 
	    (MessageTraceManagerImpl)ctm.getMessageTraceManager() ;
	if (mtm.isEnabled()) {
	    mtm.recordHeaderReceived( buf ) ;
	}

        // Sanity checks

        /*
         * check for magic corruption
         * check for version incompatibility
         * check if fragmentation is allowed based on mesg type.
            . 1.0 fragmentation disallowed; FragmentMessage is non-existent.
            . 1.1 only {Request, Reply} msgs maybe fragmented.
            . 1.2 only {Request, Reply, LocateRequest, LocateReply} msgs
              maybe fragmented.
        */

        byte[] it = new byte[12];
        buf.position(startPosition);
        buf.get(it);

        if (orb.giopDebugFlag) {
            // Since this is executed in debug mode only the overhead of
            // using a view buffer is not an issue. We'll also use a
            // read-only view buffer so we don't disturb the state of
            // byteBuffer.
            ByteBuffer viewBuf = ByteBuffer.wrap(it);
            viewBuf.position(viewBuf.limit());
            dprint(".parseGIOPHeader: " + typeToString(it[7]));
	    dprint(".parseGIOPHeader: GIOP header is: ");
	    ORBUtility.printBuffer("GIOP Message Header", viewBuf, System.out);
        }

        int b1, b2, b3, b4;        
        b1 = (it[0] << 24) & 0xFF000000;
        b2 = (it[1] << 16) & 0x00FF0000;
        b3 = (it[2] << 8)  & 0x0000FF00;
        b4 = (it[3] << 0)  & 0x000000FF;

        int magic = (b1 | b2 | b3 | b4);

        if (magic != GIOPBigMagic) {
            // If Magic is incorrect, it is an error.
            // ACTION : send MessageError and close the connection.
	    throw wrapper.giopMagicError( CompletionStatus.COMPLETED_MAYBE);
        }

	// Extract the encoding version from the request GIOP Version,
	// if it contains an encoding, and set GIOP version appropriately.
	// For Java serialization, we use GIOP Version 1.2 message format.
	byte requestEncodingVersion = ORBConstants.CDR_ENC_VERSION;
	if ((it[4] == GIOPVersion.V13_XX.getMajor()) &&
	        (it[5] <= ORBConstants.JAVA_ENC_VERSION) &&
	        (it[5] > ORBConstants.CDR_ENC_VERSION)) {
	    // Entering this block means the request is using Java encoding,
	    // and the encoding version is <= this ORB's Java encoding version.
	    requestEncodingVersion = it[5];
	    buf.put(startPosition + 4, GIOPVersion.V1_2.getMajor());
	    buf.put(startPosition + 5, GIOPVersion.V1_2.getMinor());
            it[4] = GIOPVersion.V1_2.getMajor();//buf.get(4);
            it[5] = GIOPVersion.V1_2.getMinor();//buf.get(5);
	}

        GIOPVersion orbVersion = orb.getORBData().getGIOPVersion();

        if (orb.giopDebugFlag) {
            dprint(".parseGIOPHeader: Message GIOP version: "
                              + it[4] + '.' + it[5]);
            dprint(".parseGIOPHeader: ORB Max GIOP Version: "
                              + orbVersion);
        }

        if ( (it[4] > orbVersion.getMajor()) ||
             ( (it[4] == orbVersion.getMajor()) && (it[5] > orbVersion.getMinor()) )
            ) {
            // For requests, sending ORB should use the version info
            // published in the IOR or may choose to use a <= version
            // for requests. If the version is greater than published version,
            // it is an error.

            // For replies, the ORB should always receive a version it supports
            // or less, but never greater (except for MessageError)

            // ACTION : Send back a MessageError() with the the highest version
            // the server ORB supports, and close the connection.
            if ( it[7] != GIOPMessageError ) {
		throw wrapper.giopVersionError( CompletionStatus.COMPLETED_MAYBE);
            }
        }

        AreFragmentsAllowed(it[4], it[5], it[6], it[7]);

        // create appropriate messages types

        MessageBase msg = null;
                
        switch (it[7]) {

        case GIOPRequest:
            if (orb.giopDebugFlag) {
                dprint(".parseGIOPHeader: creating RequestMessage");
            }
            //msg = new RequestMessage(orb.giopDebugFlag);
            if ( (it[4] == 0x01) && (it[5] == 0x00) ) { // 1.0
                msg = new RequestMessage_1_0(orb);
            } else if ( (it[4] == 0x01) && (it[5] == 0x01) ) { // 1.1
                msg = new RequestMessage_1_1(orb);
            } else if ( (it[4] == 0x01) && (it[5] == 0x02) ) { // 1.2
                msg = new RequestMessage_1_2(orb);
            } else {
		throw wrapper.giopVersionError(
		    CompletionStatus.COMPLETED_MAYBE);
            }
            break;

        case GIOPLocateRequest:
            if (orb.giopDebugFlag) {
                dprint(".parseGIOPHeader: creating LocateRequestMessage");
            }
            //msg = new LocateRequestMessage(orb.giopDebugFlag);
            if ( (it[4] == 0x01) && (it[5] == 0x00) ) { // 1.0
                msg = new LocateRequestMessage_1_0(orb);
            } else if ( (it[4] == 0x01) && (it[5] == 0x01) ) { // 1.1
                msg = new LocateRequestMessage_1_1(orb);
            } else if ( (it[4] == 0x01) && (it[5] == 0x02) ) { // 1.2
                msg = new LocateRequestMessage_1_2(orb);
            } else {
		throw wrapper.giopVersionError(
		    CompletionStatus.COMPLETED_MAYBE);
            }
            break;

        case GIOPCancelRequest:
            if (orb.giopDebugFlag) {
                dprint(".parseGIOPHeader: creating CancelRequestMessage");
            }
            //msg = new CancelRequestMessage(orb.giopDebugFlag);
            if ( (it[4] == 0x01) && (it[5] == 0x00) ) { // 1.0
                msg = new CancelRequestMessage_1_0();
            } else if ( (it[4] == 0x01) && (it[5] == 0x01) ) { // 1.1
                msg = new CancelRequestMessage_1_1();
            } else if ( (it[4] == 0x01) && (it[5] == 0x02) ) { // 1.2
                msg = new CancelRequestMessage_1_2();
            } else {
		throw wrapper.giopVersionError(
		    CompletionStatus.COMPLETED_MAYBE);
            }
            break;

        case GIOPReply:
            if (orb.giopDebugFlag) {
                dprint(".parseGIOPHeader: creating ReplyMessage");
            }
            //msg = new ReplyMessage(orb.giopDebugFlag);
            if ( (it[4] == 0x01) && (it[5] == 0x00) ) { // 1.0
                msg = new ReplyMessage_1_0(orb);
            } else if ( (it[4] == 0x01) && (it[5] == 0x01) ) { // 1.1
                msg = new ReplyMessage_1_1(orb);
            } else if ( (it[4] == 0x01) && (it[5] == 0x02) ) { // 1.2
                msg = new ReplyMessage_1_2(orb);
            } else {
		throw wrapper.giopVersionError(
		    CompletionStatus.COMPLETED_MAYBE);
            }
            break;

        case GIOPLocateReply:
            if (orb.giopDebugFlag) {
                dprint(".parseGIOPHeader: creating LocateReplyMessage");
            }
            //msg = new LocateReplyMessage(orb.giopDebugFlag);
            if ( (it[4] == 0x01) && (it[5] == 0x00) ) { // 1.0
                msg = new LocateReplyMessage_1_0(orb);
            } else if ( (it[4] == 0x01) && (it[5] == 0x01) ) { // 1.1
                msg = new LocateReplyMessage_1_1(orb);
            } else if ( (it[4] == 0x01) && (it[5] == 0x02) ) { // 1.2
                msg = new LocateReplyMessage_1_2(orb);
            } else {
		throw wrapper.giopVersionError(
		    CompletionStatus.COMPLETED_MAYBE);
            }
            break;

        case GIOPCloseConnection:
	    // IMPORTANT: Must process the CloseConnection message as soon as
	    //            its received to avoid a potential race condition on
	    //            the connection, (i.e. another thread could try to send
	    //            a new request on the same connection while this 
	    //            CloseConnection message would be getting dispatched
	    //            if the CloseConnection message were not processed
	    //            here).
            if (orb.giopDebugFlag) {
                dprint(".parseGIOPHeader: received CloseConnection message");
            }
	    connection.purgeCalls(wrapper.connectionRebind(), false, true);
	    throw wrapper.connectionRebind();

        case GIOPMessageError:
            if (orb.giopDebugFlag) {
                dprint(".parseGIOPHeader: creating Message for CloseConnection or MessageError");
            }
            // REVISIT a MessageError  may contain the highest version server
            // can support. In such a case, a new request may be made with the
            // correct version or the connection be simply closed. Note the
            // connection may have been closed by the server.
            //msg = new Message(orb.giopDebugFlag);
            if ( (it[4] == 0x01) && (it[5] == 0x00) ) { // 1.0
                msg = new Message_1_0();
            } else if ( (it[4] == 0x01) && (it[5] == 0x01) ) { // 1.1
                msg = new Message_1_1();
            } else if ( (it[4] == 0x01) && (it[5] == 0x02) ) { // 1.2
                msg = new Message_1_1();
            } else {
		throw wrapper.giopVersionError(
		    CompletionStatus.COMPLETED_MAYBE);
            }
            break;

        case GIOPFragment:
            if (orb.giopDebugFlag) {
                dprint(".parseGIOPHeader: creating FragmentMessage");
            }
            //msg = new FragmentMessage(orb.giopDebugFlag);
            if ( (it[4] == 0x01) && (it[5] == 0x00) ) { // 1.0
                // not possible (error checking done already)
                // Throw exception just for completeness, and
                // for proper dataflow analysis in FindBugs
		throw wrapper.giopVersionError(
		    CompletionStatus.COMPLETED_MAYBE);
            } else if ( (it[4] == 0x01) && (it[5] == 0x01) ) { // 1.1
                msg = new FragmentMessage_1_1();
            } else if ( (it[4] == 0x01) && (it[5] == 0x02) ) { // 1.2
                msg = new FragmentMessage_1_2();
            } else {
		throw wrapper.giopVersionError(
		    CompletionStatus.COMPLETED_MAYBE);
            }
            break;

        default:
            if (orb.giopDebugFlag)
                dprint(".parseGIOPHeader: UNKNOWN MESSAGE TYPE: "
		       + it[7]);
            // unknown message type ?
            // ACTION : send MessageError and close the connection
	    throw wrapper.giopVersionError(
		CompletionStatus.COMPLETED_MAYBE);
        }

        //
        // Initialize the generic GIOP header instance variables.
        //

        if ( (it[4] == 0x01) && (it[5] == 0x00) ) { // 1.0
            Message_1_0 msg10 = (Message_1_0) msg;
            msg10.magic = magic;
            msg10.GIOP_version = new GIOPVersion(it[4], it[5]);
            msg10.byte_order = (it[6] == LITTLE_ENDIAN_BIT);
	    // 'request partitioning' not supported on GIOP version 1.0
	    // so just use the default thread pool, 0.
	    msg.threadPoolToUse = 0;
            msg10.message_type = it[7];
            msg10.message_size = readSize(it[8], it[9], it[10], it[11],
                                          msg10.isLittleEndian()) +
                                 GIOPMessageHeaderLength;
        } else { // 1.1 & 1.2
            Message_1_1 msg11 = (Message_1_1) msg;
            msg11.magic = magic;
            msg11.GIOP_version = new GIOPVersion(it[4], it[5]);
            msg11.flags = (byte)(it[6] & TRAILING_TWO_BIT_BYTE_MASK);
	    // IMPORTANT: For 'request partitioning', the thread pool to use
	    //            information is stored in the leading 6 bits of byte 6.
	    //
	    // IMPORTANT: Request partitioning is a PROPRIETARY EXTENSION !!!
	    //
	    // NOTE: Bitwise operators will promote a byte to an int before 
	    //       performing a bitwise operation and bytes, ints, longs, etc
	    //       are signed types in Java. Thus, the need for the 
	    //       THREAD_POOL_TO_USE_MASK operation.
	    msg.threadPoolToUse = (it[6] >>> 2) & THREAD_POOL_TO_USE_MASK;
            msg11.message_type = it[7];
            msg11.message_size = 
                      readSize(it[8], it[9], it[10], it[11],
                              msg11.isLittleEndian()) + GIOPMessageHeaderLength;

	    if (orb.giopSizeDebugFlag) {
		StringBuilder sb = new StringBuilder() ;
		sb.append( typeToString( msg11.message_type ) ) ;
		sb.append( "(" ) ;
		sb.append( msg11.message_size ) ;
		sb.append( " bytes)" ) ;
		dprint( sb.toString() ) ;
	    }
        }

        if (orb.giopDebugFlag) {
            // Since this is executed in debug mode only the overhead of
            // using a View Buffer is not an issue. We'll also use a
            // read-only View Buffer so we don't disturb the state of
            // byteBuffer. 
            dprint(".parseGIOPHeader: header construction complete.");

            // For debugging purposes, save the 12 bytes of the header
            ByteBuffer viewBuf = buf.asReadOnlyBuffer();
            byte[] msgBuf = new byte[GIOPMessageHeaderLength];
            viewBuf.position(startPosition).limit(startPosition + 
                                                  GIOPMessageHeaderLength);
            viewBuf.get(msgBuf,0,msgBuf.length);
	    // REVISIT: is giopHeader still used?
            ((MessageBase)msg).giopHeader = msgBuf;
        }

	msg.setByteBuffer(buf);
	msg.setEncodingVersion(requestEncodingVersion);

        return msg;
    }

    public static Message readGIOPBody(ORB orb,
			               CorbaConnection connection,
				       Message msg)
    {
	CorbaTransportManager ctm = 
	    (CorbaTransportManager)orb.getTransportManager() ;
	MessageTraceManagerImpl mtm = 
	    (MessageTraceManagerImpl)ctm.getMessageTraceManager() ;

	ByteBuffer buf = msg.getByteBuffer();

	buf.position(MessageBase.GIOPMessageHeaderLength);
	int msgSizeMinusHeader =
	    msg.getSize() - MessageBase.GIOPMessageHeaderLength;
	try {
	    buf = connection.read(buf, 
			  GIOPMessageHeaderLength, msgSizeMinusHeader ) ;
	} catch (IOException e) {
	    throw wrapper.ioexceptionWhenReadingConnection(e, connection.toString());
	}

	msg.setByteBuffer(buf);

	if (mtm.isEnabled()) {
	    mtm.recordBodyReceived( buf ) ;
	}

	if (orb.giopDebugFlag) {
            // For debugging purposes, create view buffer
            ByteBuffer viewBuf = buf.asReadOnlyBuffer();
            viewBuf.limit(buf.capacity()).position(buf.limit());
	    dprint(".readGIOPBody: received message:");
	    ORBUtility.printBuffer( "GIOP Message Body", 
		viewBuf, System.out ) ;
	}

        return msg;
    }

    @SuppressWarnings({"deprecation"})
    private static RequestMessage createRequest(
            ORB orb, GIOPVersion gv, byte encodingVersion, int request_id,
            boolean response_expected, byte[] object_key, String operation,
            ServiceContexts service_contexts, 
	    org.omg.CORBA.Principal requesting_principal) {

        if (gv.equals(GIOPVersion.V1_0)) { // 1.0
            return new RequestMessage_1_0(orb, service_contexts, request_id,
					 response_expected, object_key,
					 operation, requesting_principal);
        } else if (gv.equals(GIOPVersion.V1_1)) { // 1.1
            return new RequestMessage_1_1(orb, service_contexts, request_id,
                response_expected, new byte[] { 0x00, 0x00, 0x00 },
                object_key, operation, requesting_principal);
        } else if (gv.equals(GIOPVersion.V1_2)) { // 1.2
            // Note: Currently we use response_expected flag to decide if the
            // call is oneway or not. Ideally, it is possible to expect a
            // response on a oneway call too, but we do not support it now.
            byte response_flags = 0x03;
            if (response_expected) {
                response_flags = 0x03;
            } else {
                response_flags = 0x00;
            }
            /*
            // REVISIT The following is the correct way to do it. This gives
            // more flexibility.
            if ((DII::INV_NO_RESPONSE == false) && response_expected) {
                response_flags = 0x03; // regular two-way
            } else if ((DII::INV_NO_RESPONSE == false) && !response_expected) {
                // this condition is not possible
            } else if ((DII::INV_NO_RESPONSE == true) && response_expected) {
                // oneway, but we need response for LocationForwards or
                // SystemExceptions.
                response_flags = 0x01;
            } else if ((DII::INV_NO_RESPONSE == true) && !response_expected) {
                // oneway, no response required
                response_flags = 0x00;
            }
            */
            TargetAddress target = new TargetAddress();
            target.object_key(object_key);
            RequestMessage msg = 
		new RequestMessage_1_2(orb, request_id, response_flags,
				       new byte[] { 0x00, 0x00, 0x00 },
				       target, operation, service_contexts);
	    msg.setEncodingVersion(encodingVersion);
	    return msg;
        } else {
	    throw wrapper.giopVersionError(
		CompletionStatus.COMPLETED_MAYBE);
        }
    }

    @SuppressWarnings({"deprecation"})
    public static RequestMessage createRequest(
            ORB orb, GIOPVersion gv, byte encodingVersion, int request_id,
	    boolean response_expected, IOR ior,
	    short addrDisp, String operation,
            ServiceContexts service_contexts, 
	    org.omg.CORBA.Principal requesting_principal) {

	RequestMessage requestMessage = null;
        IIOPProfile profile = ior.getProfile();
            
        if (addrDisp == KeyAddr.value) {  
            // object key will be used for target addressing
            profile = ior.getProfile();
    	    ObjectKey objKey = profile.getObjectKey();
	    byte[] object_key = objKey.getBytes(orb);            
	    requestMessage = 
		   createRequest(orb, gv, encodingVersion, request_id,
				 response_expected, object_key,
				 operation, service_contexts,
                                 requesting_principal);            
        } else {
        
            if (!(gv.equals(GIOPVersion.V1_2))) {        
                // only object_key based target addressing is allowed for 
                // GIOP 1.0 & 1.1
	        throw wrapper.giopVersionError(
		    CompletionStatus.COMPLETED_MAYBE);
            }
    
            // Note: Currently we use response_expected flag to decide if the
            // call is oneway or not. Ideally, it is possible to expect a
            // response on a oneway call too, but we do not support it now.
            byte response_flags = 0x03;
            if (response_expected) {
                response_flags = 0x03;
            } else {
                response_flags = 0x00;
            }
            
            TargetAddress target = new TargetAddress();            
            if (addrDisp == ProfileAddr.value) { // iop profile will be used
                profile = ior.getProfile();
                target.profile(profile.getIOPProfile());
            } else if (addrDisp == ReferenceAddr.value) {  // ior will be used
                IORAddressingInfo iorInfo = 
                    new IORAddressingInfo( 0, // profile index
                        ior.getIOPIOR());
                target.ior(iorInfo);  
            } else { 
                // invalid target addressing disposition value
	        throw wrapper.illegalTargetAddressDisposition(
		    CompletionStatus.COMPLETED_NO);
            }
        
	    requestMessage =
                   new RequestMessage_1_2(orb, request_id, response_flags,
                                  new byte[] { 0x00, 0x00, 0x00 }, target,
                                  operation, service_contexts);
	    requestMessage.setEncodingVersion(encodingVersion);
	}

	if (gv.supportsIORIIOPProfileComponents()) {
	    // add request partitioning thread pool to use info
	    int poolToUse = 0; // default pool
	    IIOPProfileTemplate temp = 
		(IIOPProfileTemplate)profile.getTaggedProfileTemplate();
	    Iterator iter = 
		temp.iteratorById(ORBConstants.TAG_REQUEST_PARTITIONING_ID);
	    if (iter.hasNext()) {
		poolToUse = 
		    ((RequestPartitioningComponent)iter.next()).getRequestPartitioningId();
	    }

	    if (poolToUse < ORBConstants.REQUEST_PARTITIONING_MIN_THREAD_POOL_ID ||
		poolToUse > ORBConstants.REQUEST_PARTITIONING_MAX_THREAD_POOL_ID) {
		throw wrapper.invalidRequestPartitioningId(poolToUse,
		          ORBConstants.REQUEST_PARTITIONING_MIN_THREAD_POOL_ID,
	      	          ORBConstants.REQUEST_PARTITIONING_MAX_THREAD_POOL_ID);
	    }
	    requestMessage.setThreadPoolToUse(poolToUse);
	}

	return requestMessage;
    }
                    
    public static ReplyMessage createReply(
            ORB orb, GIOPVersion gv, byte encodingVersion, int request_id,
            int reply_status, ServiceContexts service_contexts, IOR ior) {

        if (gv.equals(GIOPVersion.V1_0)) { // 1.0
            return new ReplyMessage_1_0(orb, service_contexts, request_id,
                                        reply_status, ior);
        } else if (gv.equals(GIOPVersion.V1_1)) { // 1.1
            return new ReplyMessage_1_1(orb, service_contexts, request_id,
                                        reply_status, ior);
        } else if (gv.equals(GIOPVersion.V1_2)) { // 1.2
            ReplyMessage msg = 
		new ReplyMessage_1_2(orb, request_id, reply_status,
				     service_contexts, ior);
	    msg.setEncodingVersion(encodingVersion);
	    return msg;
        } else {
	    throw wrapper.giopVersionError(
		CompletionStatus.COMPLETED_MAYBE);
        }
    }

    public static LocateRequestMessage createLocateRequest(
            ORB orb, GIOPVersion gv, byte encodingVersion,
            int request_id, byte[] object_key) {

        if (gv.equals(GIOPVersion.V1_0)) { // 1.0
            return new LocateRequestMessage_1_0(orb, request_id, object_key);
        } else if (gv.equals(GIOPVersion.V1_1)) { // 1.1
            return new LocateRequestMessage_1_1(orb, request_id, object_key);
        } else if (gv.equals(GIOPVersion.V1_2)) { // 1.2
            TargetAddress target = new TargetAddress();
            target.object_key(object_key);
            LocateRequestMessage msg =
		new LocateRequestMessage_1_2(orb, request_id, target);
	    msg.setEncodingVersion(encodingVersion);
	    return msg;
        } else {
	    throw wrapper.giopVersionError(
		CompletionStatus.COMPLETED_MAYBE);
        }
    }

    public static LocateReplyMessage createLocateReply(
	    ORB orb, GIOPVersion gv, byte encodingVersion,
            int request_id, int locate_status, IOR ior) {

        if (gv.equals(GIOPVersion.V1_0)) { // 1.0
            return new LocateReplyMessage_1_0(orb, request_id,
                                              locate_status, ior);
        } else if (gv.equals(GIOPVersion.V1_1)) { // 1.1
            return new LocateReplyMessage_1_1(orb, request_id,
                                              locate_status, ior);
        } else if (gv.equals(GIOPVersion.V1_2)) { // 1.2
            LocateReplyMessage msg = 
		new LocateReplyMessage_1_2(orb, request_id, 
					   locate_status, ior);
	    msg.setEncodingVersion(encodingVersion);
	    return msg;
        } else {
	    throw wrapper.giopVersionError(
		CompletionStatus.COMPLETED_MAYBE);
        }
    }

    public static CancelRequestMessage createCancelRequest(
            GIOPVersion gv, int request_id) {

        if (gv.equals(GIOPVersion.V1_0)) { // 1.0
            return new CancelRequestMessage_1_0(request_id);
        } else if (gv.equals(GIOPVersion.V1_1)) { // 1.1
            return new CancelRequestMessage_1_1(request_id);
        } else if (gv.equals(GIOPVersion.V1_2)) { // 1.2
            return new CancelRequestMessage_1_2(request_id);
        } else {
	    throw wrapper.giopVersionError(
		CompletionStatus.COMPLETED_MAYBE);
        }
    }

    public static Message createCloseConnection(GIOPVersion gv) {
        if (gv.equals(GIOPVersion.V1_0)) { // 1.0
            return new Message_1_0(Message.GIOPBigMagic, false,
                                   Message.GIOPCloseConnection, 0);
        } else if (gv.equals(GIOPVersion.V1_1)) { // 1.1
            return new Message_1_1(Message.GIOPBigMagic, GIOPVersion.V1_1,
                                   FLAG_NO_FRAG_BIG_ENDIAN,
                                   Message.GIOPCloseConnection, 0);
        } else if (gv.equals(GIOPVersion.V1_2)) { // 1.2
            return new Message_1_1(Message.GIOPBigMagic, GIOPVersion.V1_2,
                                   FLAG_NO_FRAG_BIG_ENDIAN,
                                   Message.GIOPCloseConnection, 0);
        } else {
	    throw wrapper.giopVersionError(
		CompletionStatus.COMPLETED_MAYBE);
        }
    }

    public static Message createMessageError(GIOPVersion gv) {
        if (gv.equals(GIOPVersion.V1_0)) { // 1.0
            return new Message_1_0(Message.GIOPBigMagic, false,
                                   Message.GIOPMessageError, 0);
        } else if (gv.equals(GIOPVersion.V1_1)) { // 1.1
            return new Message_1_1(Message.GIOPBigMagic, GIOPVersion.V1_1,
                                   FLAG_NO_FRAG_BIG_ENDIAN,
                                   Message.GIOPMessageError, 0);
        } else if (gv.equals(GIOPVersion.V1_2)) { // 1.2
            return new Message_1_1(Message.GIOPBigMagic, GIOPVersion.V1_2,
                                   FLAG_NO_FRAG_BIG_ENDIAN,
                                   Message.GIOPMessageError, 0);
        } else {
	    throw wrapper.giopVersionError(
		CompletionStatus.COMPLETED_MAYBE);
        }
    }

    /**
     * Set a flag in the given buffer (fragment bit, byte order bit, etc)
     */
    public static void setFlag(ByteBuffer byteBuffer, int flag) {
        byte b = byteBuffer.get(6);
        b |= flag;
        byteBuffer.put(6,b);
    }

    private static void AreFragmentsAllowed(byte major, byte minor, byte flag,
            byte msgType) {

        if ( (major == 0x01) && (minor == 0x00) ) { // 1.0
            if (msgType == GIOPFragment) {
		throw wrapper.fragmentationDisallowed( 
		    CompletionStatus.COMPLETED_MAYBE);
            }
        }

        if ( (flag & MORE_FRAGMENTS_BIT) == MORE_FRAGMENTS_BIT ) {
            switch (msgType) {
            case GIOPCancelRequest :
            case GIOPCloseConnection :
            case GIOPMessageError :
		throw wrapper.fragmentationDisallowed( 
		    CompletionStatus.COMPLETED_MAYBE);
            case GIOPLocateRequest :
            case GIOPLocateReply :
                if ( (major == 0x01) && (minor == 0x01) ) { // 1.1
		    throw wrapper.fragmentationDisallowed( 
			CompletionStatus.COMPLETED_MAYBE);
                }
                break;
            }
        }
    }

    /**
     * Extract the object key from TargetAddress.
     *
     * @return ObjectKey the object key.
     */
    static ObjectKeyCacheEntry extractObjectKeyCacheEntry(TargetAddress target, ORB orb) {

	short orbTargetAddrPref = orb.getORBData().getGIOPTargetAddressPreference();
	short reqAddrDisp = target.discriminator();

	switch (orbTargetAddrPref) {
	    case ORBConstants.ADDR_DISP_OBJKEY :
		if (reqAddrDisp != KeyAddr.value) {
		    throw new AddressingDispositionException(KeyAddr.value);
		}
		break;
	    case ORBConstants.ADDR_DISP_PROFILE :
		if (reqAddrDisp != ProfileAddr.value) {
		    throw new AddressingDispositionException(ProfileAddr.value);
		}
		break;
	    case ORBConstants.ADDR_DISP_IOR :
		if (reqAddrDisp != ReferenceAddr.value) {
		    throw new AddressingDispositionException(ReferenceAddr.value);
		}
		break;
	    case ORBConstants.ADDR_DISP_HANDLE_ALL :
		break;
	    default : 
		throw wrapper.orbTargetAddrPreferenceInExtractObjectkeyInvalid() ;
	}    

	try {
	    ObjectKeyCacheEntry result = null ;
	    switch (reqAddrDisp) {
		case KeyAddr.value :
		    byte[] objKey = target.object_key();
		    if (objKey != null) { // AddressingDisposition::KeyAddr
			return orb.extractObjectKeyCacheEntry(objKey); 
		    }
		    break;
		case ProfileAddr.value :
		    IIOPProfile iiopProfile = null;
		    TaggedProfile profile = target.profile();
		    if (profile != null) { // AddressingDisposition::ProfileAddr
			iiopProfile = IIOPFactories.makeIIOPProfile(orb, profile);
			ObjectKey objectKey = iiopProfile.getObjectKey();
			return new ObjectKeyCacheEntryNoObjectAdapterImpl( objectKey ) ;
		    }
		    break;
		case ReferenceAddr.value :
		    IORAddressingInfo iorInfo = target.ior();
		    if (iorInfo != null) { // AddressingDisposition::IORAddr
			profile = iorInfo.ior.profiles[iorInfo.selected_profile_index];
			iiopProfile = IIOPFactories.makeIIOPProfile(orb, profile);
			ObjectKey objectKey = iiopProfile.getObjectKey();
			return new ObjectKeyCacheEntryNoObjectAdapterImpl( objectKey ) ;
		    }
		    break;
		default : 
		    // this cannot happen
		    // There is no need for a explicit exception, since the
		    // TargetAddressHelper.read() would have raised a BAD_OPERATION
		    // exception by now.
		    break;
	    }
	} catch (Exception e) {
	    throw wrapper.invalidObjectKey( e ) ;
	}

	// If we got here, something went wrong: the object key is null.
	throw wrapper.invalidObjectKey() ;
    }

    private static int readSize(byte b1, byte b2, byte b3, byte b4,
            boolean littleEndian) {

        int a1, a2, a3, a4;

        if (!littleEndian) {
            a1 = (b1 << 24) & 0xFF000000;
            a2 = (b2 << 16) & 0x00FF0000;
            a3 = (b3 << 8)  & 0x0000FF00;
            a4 = (b4 << 0)  & 0x000000FF;
        } else {
            a1 = (b4 << 24) & 0xFF000000;
            a2 = (b3 << 16) & 0x00FF0000;
            a3 = (b2 << 8)  & 0x0000FF00;
            a4 = (b1 << 0)  & 0x000000FF;
        }

        return (a1 | a2 | a3 | a4);
    }

    static void nullCheck(Object obj) {
        if (obj == null) {
	    throw wrapper.nullNotAllowed() ;
        }
    }

    static SystemException getSystemException(
        String exClassName, int minorCode, CompletionStatus completionStatus,
	String message, ORBUtilSystemException wrapper)
    {
	SystemException sysEx = null;

        try {
	    Class clazz = ORBClassLoader.loadClass(exClassName);
	    if (message == null) {
		sysEx = (SystemException) clazz.newInstance();
	    } else {
		Class[] types = { String.class };
		Constructor constructor = clazz.getConstructor(types);
		Object[] args = { message };
		sysEx = (SystemException)constructor.newInstance(args);
	    }
        } catch (Exception someEx) {
	    throw wrapper.badSystemExceptionInReply( 
		CompletionStatus.COMPLETED_MAYBE, someEx );
        }

        sysEx.minor = minorCode;
        sysEx.completed = completionStatus;

        return sysEx;
    }

    public void callback(MessageHandler handler)
        throws java.io.IOException
    {
        handler.handleInput(this);
    }

    public ByteBuffer getByteBuffer()
    {
	return byteBuffer;
    }

    public void setByteBuffer(ByteBuffer byteBuffer)
    {
	this.byteBuffer = byteBuffer;
    }

    public int getThreadPoolToUse()
    {
	return threadPoolToUse;
    }

    public byte getEncodingVersion() {
	return this.encodingVersion;
    }

    public void setEncodingVersion(byte version) {
	this.encodingVersion = version;
    }

    /**
     * Return a Message's CorbaRequestId.
     * NOTE: This method should be overridden for messages that support
     *       a 4 byte request id following the 12 byte GIOP message header.
     */
    public CorbaRequestId getCorbaRequestId() {
        return CorbaRequestIdImpl.UNKNOWN_CORBA_REQUEST_ID;
    }

    /**
     * Returns whether the Message supports message fragmenting.
     *
     * @return <code>true</code> if Message supports fragmenting or is
     *         a message fragment. Otherwise <code>false</code> it does
     *         not support message fragments.
     */
    public static boolean messageSupportsFragments(Message message) {
        boolean result;
        ByteBuffer byteBuffer = message.getByteBuffer();
        byte major = byteBuffer.get(4);
        byte minor = byteBuffer.get(5);
        byte msgType = byteBuffer.get(7);
        
        if (major == 0x01 && minor == 0x02) {
            switch (msgType) {
                case Message.GIOPRequest:
                case Message.GIOPReply:
                case Message.GIOPLocateRequest:
                case Message.GIOPLocateReply:
                case Message.GIOPFragment:
                    result = true;
                    break;
                default:
                    result = false;
            }
        } else if (major == 0x01 && minor == 0x01) {
            switch (msgType) {
                case Message.GIOPReply:
                case Message.GIOPRequest:
                case Message.GIOPFragment:
                    result = true;
                    break;
                default:
                    result = false;
            }
        } else {
            // otherwise message types are not of interest
            result = false;
        }
        return result;
    }

    /**
     * Get the request id from the 4 bytes following the 12 byte GIOP
     * request header if the request header exists. Otherwise, return 0.
     * 
     * NOTE: Assumes Message already been filtered by
     *       MessageBase.messageSupportsFragments(Message)
     * 
     * @return <code>CorbaRequestId</code>if <code>Message</code> supports a 12
     *        + 4 byte GIOP header. Otherwise returns a CorbaRequestId with an
     *        undefined request id.
     */
    public static CorbaRequestId getRequestIdFromMessageBytes(Message message) {
        ByteBuffer byteBuffer = message.getByteBuffer();
        byte major = byteBuffer.get(4);
        byte minor = byteBuffer.get(5);
        if (major == 0x01 && minor == 0x02 &&
                message.getSize() >= (Message.GIOPMessageHeaderLength + 4)) {
            CorbaRequestId requestId = new
                    CorbaRequestIdImpl(unmarshalRequestHeaderRequestId(message));
            return requestId;
        } else {
            // Its a Request / Reply 1.1 type of message for which the request id
            // is not found in the 4 bytes following the 12 GIOP message header.
            return CorbaRequestIdImpl.UNKNOWN_CORBA_REQUEST_ID;
        }
    }

    private static int unmarshalRequestHeaderRequestId(Message message) {
        ByteBuffer byteBuffer = message.getByteBuffer();
        int b1, b2, b3, b4;
        final int offset = Message.GIOPMessageHeaderLength;
        
        if (!message.isLittleEndian()) {
            b1 = (byteBuffer.get(offset+0) << 24) & 0xFF000000;
            b2 = (byteBuffer.get(offset+1) << 16) & 0x00FF0000;
            b3 = (byteBuffer.get(offset+2) << 8)  & 0x0000FF00;
            b4 = (byteBuffer.get(offset+3) << 0)  & 0x000000FF;
        } else {
            b1 = (byteBuffer.get(offset+3) << 24) & 0xFF000000;
            b2 = (byteBuffer.get(offset+2) << 16) & 0x00FF0000;
            b3 = (byteBuffer.get(offset+1) << 8)  & 0x0000FF00;
            b4 = (byteBuffer.get(offset+0) << 0)  & 0x000000FF;
        }
        
        return (b1 | b2 | b3 | b4);
    }

    private static void dprint(String msg)
    {
	ORBUtility.dprint("MessageBase", msg);
    }
}
