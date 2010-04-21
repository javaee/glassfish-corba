/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1996-2010 Sun Microsystems, Inc. All rights reserved.
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

package com.sun.corba.se.impl.transport;

import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.ReentrantLock;

import org.omg.CORBA.CompletionStatus;
import org.omg.CORBA.SystemException;


import com.sun.corba.se.spi.orb.ORB;
import com.sun.corba.se.spi.protocol.CorbaMessageMediator;
import com.sun.corba.se.spi.transport.CorbaConnection;
import com.sun.corba.se.spi.transport.CorbaResponseWaitingRoom;

import com.sun.corba.se.impl.encoding.CDRInputObject;
import com.sun.corba.se.impl.logging.ORBUtilSystemException;
import com.sun.corba.se.spi.orbutil.ORBConstants;
import com.sun.corba.se.impl.orbutil.ORBUtility;
import com.sun.corba.se.impl.orbutil.newtimer.generated.TimingPoints;
import com.sun.corba.se.impl.protocol.giopmsgheaders.LocateReplyOrReplyMessage;
import com.sun.corba.se.spi.orbutil.tf.annotation.InfoMethod;
import com.sun.corba.se.spi.trace.Transport;

/**
 * @author Harold Carr
 */
@Transport
public class CorbaResponseWaitingRoomImpl
    implements
	CorbaResponseWaitingRoom
{
    final static class OutCallDesc
    {
	CorbaMessageMediator messageMediator;
        SystemException exception;
        CDRInputObject inputObject;
        ReentrantLock lock = new ReentrantLock();
        Condition condition = lock.newCondition();
    }

    private TimingPoints tp ;

    // Maps requestId to an OutCallDesc.
    final private Map<Integer, OutCallDesc> out_calls;
    final private ORB orb;
    final private CorbaConnection connection;
    final private ORBUtilSystemException wrapper ;


    public CorbaResponseWaitingRoomImpl(ORB orb, CorbaConnection connection)
    {
	this.orb = orb;
	tp = orb.getTimerManager().points() ;
	this.wrapper = orb.getLogWrapperTable().get_RPC_TRANSPORT_ORBUtil() ;
	this.connection = connection;
        this.out_calls = 
               Collections.synchronizedMap(new HashMap<Integer, OutCallDesc>());
    }

    @Transport
    public void registerWaiter(CorbaMessageMediator messageMediator)
    {
        display( "messageMediator request ID",
            messageMediator.getRequestId() ) ;
        display( "messageMediator operation name",
            messageMediator.getOperationName() ) ;

	Integer requestId = messageMediator.getRequestIdInteger();
        
	OutCallDesc call = new OutCallDesc();
	call.messageMediator = messageMediator;
	OutCallDesc exists = out_calls.put(requestId, call);
        if (exists != null) {
            wrapper.duplicateRequestIdsInResponseWaitingRoom(
                       ORBUtility.operationNameAndRequestId(
                           (CorbaMessageMediator)exists.messageMediator),
                       ORBUtility.operationNameAndRequestId(messageMediator));
        }
    }

    @Transport
    public void unregisterWaiter(CorbaMessageMediator mediator)
    {
	CorbaMessageMediator messageMediator = (CorbaMessageMediator) mediator;
        display( "messageMediator request ID",
            messageMediator.getRequestId() ) ;
        display( "messageMediator operation name",
            messageMediator.getOperationName() ) ;

	Integer requestId = messageMediator.getRequestIdInteger();

        out_calls.remove(requestId);
    }

    @Transport
    public CDRInputObject waitForResponse(CorbaMessageMediator messageMediator) {
        try {
            tp.enter_waitForResponse() ;
            CDRInputObject returnStream = null;
            
            display( "messageMediator request ID",
                messageMediator.getRequestId() ) ;
            display( "messageMediator operation name",
                messageMediator.getOperationName() ) ;
            
            Integer requestId = messageMediator.getRequestIdInteger();
            
            if (messageMediator.isOneWay()) {
                // The waiter is removed in releaseReply in the same
                // way as a normal request.
                display( "Oneway request: not waiting") ;
                return null;
            }
            
            OutCallDesc call = out_calls.get(requestId);
            if (call == null) {
                throw wrapper.nullOutCall(CompletionStatus.COMPLETED_MAYBE);
            }

            // Value from ORBData is in milliseconds, will convert it nanoseconds
            // to use it with Condition.awaitNanos()
            long waitForResponseTimeout =
                    orb.getORBData().getWaitForResponseTimeout() * 1000 * 1000;
            
            try {
                call.lock.lock();
                while (call.inputObject == null && call.exception == null) {
                    // Wait for the reply from the server.
                    // The ReaderThread reads in the reply IIOP message
                    // and signals us.
                    try {
                        display( "Waiting for response..." ) ;
                        
                        waitForResponseTimeout =
                                call.condition.awaitNanos(waitForResponseTimeout);
                        if (call.inputObject == null && call.exception == null) {
                            if (waitForResponseTimeout > 0) {
                                // it's a "spurious wait wakeup", need to
                                // continue to wait for a response
                                display( "Spurious wakeup, continuing to wait for ",
                                    waitForResponseTimeout/1000000 );
                            } else {
                                // timed out waiting for data
                                call.exception =
                                        wrapper.communicationsTimeoutWaitingForResponse(
                                        CompletionStatus.COMPLETED_MAYBE,
                                        orb.getORBData().getWaitForResponseTimeout());
                                // REVISIT:
                                // Normally the inputObject or exception is
                                // created from the response stream.
                                // Need to fake encoding version since
                                // it is expected to be popped in endRequest.
                                ORBUtility.pushEncVersionToThreadLocalState(
                                        ORBConstants.JAVA_ENC_VERSION);
                            }
                        }
                    } catch (InterruptedException ie) {};
                }
                if (call.exception != null) {
                    display( "Exception from call", call.exception ) ;
                    throw call.exception;
                }
                
                returnStream = call.inputObject;
            } finally {
                call.lock.unlock();
            }
            
            // REVISIT -- exceptions from unmarshaling code will
            // go up through this client thread!
            
            if (returnStream != null) {
                // On fragmented streams the header MUST be unmarshaled here
                // (in the client thread) in case it blocks.
                // If the header was already unmarshaled, this won't
                // do anything
                // REVISIT: cast - need interface method.
                ((CDRInputObject)returnStream).unmarshalHeader();
            }
            
            return returnStream;
            
        } finally {
            tp.exit_waitForResponse() ;
        }
    }

    @InfoMethod
    private void display( String msg ) { }

    @InfoMethod
    private void display( String msg, int value ) { }

    @InfoMethod
    private void display( String msg, Object value ) { }

    @Transport
    public void responseReceived(CDRInputObject is)
    {
	CDRInputObject inputObject = (CDRInputObject) is;
	LocateReplyOrReplyMessage header = (LocateReplyOrReplyMessage)
	    inputObject.getMessageHeader();
        display( "requestId", header.getRequestId()) ;
        display( "header", header ) ;

        OutCallDesc call = out_calls.get(header.getRequestId());

        // This is an interesting case.  It could mean that someone sent us a
        // reply message, but we don't know what request it was for.  That
        // would probably call for an error.  However, there's another case
        // that's normal and we should think about --
        //
        // If the unmarshaling thread does all of its work inbetween the time
        // the ReaderThread gives it the last fragment and gets to the
        // out_calls.get line, then it will also be null, so just return;
        if (call == null) {
            display( "No waiter" ) ;
            return;
	}

        // Set the reply InputObject and signal the client thread
        // that the reply has been received.
        // The thread signalled will remove outcall descriptor if appropriate.
        // Otherwise, it'll be removed when last fragment for it has been put on
        // BufferManagerRead's queue.
        
        try {
            call.lock.lock();
            CorbaMessageMediator messageMediator = 
                           (CorbaMessageMediator)call.messageMediator;

            display( "Notifying waiters") ;
            display( "messageMediator request ID",
                messageMediator.getRequestId() ) ;
            display( "messageMediator operation name",
                messageMediator.getOperationName() ) ;

	    messageMediator.setReplyHeader(header);
	    messageMediator.setInputObject(is);
	    inputObject.setMessageMediator(messageMediator);
            call.inputObject = is;
            call.condition.signal();
        } finally {
            call.lock.unlock();
        }
    }

    public int numberRegistered()
    {
	return out_calls.size();
    }

    //////////////////////////////////////////////////
    //
    // CorbaResponseWaitingRoom
    //

    @Transport
    public void signalExceptionToAllWaiters(SystemException systemException) {
        OutCallDesc call;
        synchronized (out_calls) {
            Iterator<OutCallDesc> itr = out_calls.values().iterator();
            while (itr.hasNext()) {
                call = itr.next();
                try {
                    call.lock.lock();
                    ((CorbaMessageMediator)call.messageMediator).cancelRequest();
                    call.inputObject = null;
                    call.exception = systemException;
                    call.condition.signal();
                } finally {
                    call.lock.unlock();
                }
            }
        }
    }

    public CorbaMessageMediator getMessageMediator(int requestId)
    {
        OutCallDesc call = out_calls.get(requestId);
	if (call == null) {
	    // This can happen when getting early reply fragments for a
	    // request which has completed (e.g., client marshaling error).
	    return null;
	}
	return call.messageMediator;
    }
}

// End of file.
