/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2007 Sun Microsystems, Inc. All rights reserved.
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
//
// Created       : 2001 Nov 28 (Wed) 23:25:26 by Harold Carr.
// Last Modified : 2003 Sep 29 (Mon) 07:22:55 by Harold Carr.
//

package corba.genericRPCMSGFramework;

import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.nio.channels.SocketChannel;
import java.nio.channels.SelectableChannel;
import java.nio.channels.SelectionKey;

import com.sun.corba.se.pept.broker.Broker;
import com.sun.corba.se.pept.encoding.InputObject;
import com.sun.corba.se.pept.encoding.OutputObject;
import com.sun.corba.se.pept.protocol.MessageMediator;
import com.sun.corba.se.spi.transport.CorbaAcceptor;
import com.sun.corba.se.pept.transport.ContactInfo;
import com.sun.corba.se.pept.transport.Connection;
import com.sun.corba.se.pept.transport.ConnectionCache;
import com.sun.corba.se.pept.transport.EventHandler;

import com.sun.corba.se.spi.orb.ORB;
import com.sun.corba.se.spi.orbutil.threadpool.Work;

import com.sun.corba.se.impl.transport.EventHandlerBase;

import com.sun.corba.se.impl.orbutil.ORBUtility;

/**
 * @author Harold Carr
 */
public class SOAPConnection 
    extends
	EventHandlerBase
    implements
	Connection,
	Work
{
    private ORB broker; // REVISIT: only for debug.
    private SOAPContactInfo soapContactInfo;
    private SocketChannel socketChannel;
    private Socket socket;

    private SOAPConnection(Broker broker)
    {
	this.broker = (ORB) broker;
    }

    public SOAPConnection(Broker broker, SOAPContactInfo soapContactInfo)
    {
	this(broker);
	try {
	    this.soapContactInfo = soapContactInfo;
	    setUseSelectThreadToWait(false);
	    setUseWorkerThreadForEvent(false);
	    setWork(this);

	    InetSocketAddress address =
		new InetSocketAddress(soapContactInfo.getHost(),
				      soapContactInfo.getPort());
	    socketChannel = ORBUtility.openSocketChannel(address);
	    socketChannel.configureBlocking(true);
	    socket = socketChannel.socket();
	} catch (Exception e) {
	    System.out.println("SOAPConnection: " + e);
	    e.printStackTrace(System.out);
	}
    }

    public SOAPConnection(Broker broker, Socket socket)
    {
	this(broker);
	this.socket = socket;
    }

    Socket getSocket()
    {
	return socket;
    }

    public boolean read()
    {
      try {
	BufferedReader bufferedReader =
	    new BufferedReader(new InputStreamReader(socket.getInputStream()));
	String line;
	int len = -1;
	String uri = null;
	while (((line = bufferedReader.readLine()) != null) &&
	       (line.length() != 0)) {
	    if (line.startsWith("Content-Length")) {
		len = Integer.parseInt(line.substring(16, line.length()));
	    } else if (line.startsWith("POST")) {
		uri = line.substring(5, line.length());
	    }
	}
	char[] xml = new char[len];
	for (int i = 0; i < len; i++) {
	    xml[i] = (char) bufferedReader.read();
	}

	// REVISIT: this is not the right place for this.

	if (broker.transportDebugFlag) {
	    System.out.println();
	    System.out.println("----------RECEIVED----------");
	    System.out.println(new String(xml));
	    System.out.println("----------------------------");
	    System.out.println();
	}

	SOAPMessageMediator soapMessageMediator =
	    new SOAPMessageMediator(broker, this, uri);
	SOAPInputObject soapInputObject =
	    new SOAPInputObject(broker, new String(xml));
	soapInputObject.setMessageMediator(soapMessageMediator);
	return soapMessageMediator.handleInput(soapInputObject);
      } catch (IOException e) {
	  RuntimeException rte = new RuntimeException();
	  rte.initCause(e);
	  throw rte;
      }
    }

    public InputObject waitForResponse(MessageMediator messageMediator)
    {
	ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
	PrintWriter printWriter = new PrintWriter(byteArrayOutputStream);
	try {
	BufferedReader bufferedReader = 
	    new BufferedReader(new InputStreamReader(socket.getInputStream()));
	fromToByLine(bufferedReader, printWriter);
	fromToByLine(bufferedReader, printWriter);
	fromToByLine(bufferedReader, printWriter);
	} catch (Exception e) {
	    // REVISIT
	    System.out.println("SOAPConnection.waitForResponse: " + e);
	}
	String message = new String(byteArrayOutputStream.toByteArray());
	int startOfPayload = message.indexOf("<");

	if (broker.transportDebugFlag) {
	    System.out.println();
	    System.out.println("----------RECEIVED----------");
	    System.out.println(message);
	    System.out.println("----------------------------");
	    System.out.println();
	}

	return new SOAPInputObject(broker,
				   message.substring(startOfPayload,
						     message.length() - 1));
    }

    private void fromToByLine(BufferedReader in, PrintWriter out)
	throws IOException
    {
	String line;
	while ((line = in.readLine()) != null) {
	    if (line.length() == 0) break;
	    out.println(line);
	    out.flush();
	}
    }
    
    public OutputObject createOutputObject(MessageMediator messageMediator)
    {
	return null;
    }

    public boolean isBusy()
    {
	throwException("isBusy");
	return false;
    }

    public long getTimeStamp()
    {
	throwException("getTimeStamp");
	return -1;
    }

    public void setTimeStamp(long time)
    {
	throwException("setTimeStamp");
    }

    public void close()
    {
	try {
	    socket.close();
	    socketChannel.close();
	} catch (Throwable t) {
	    ;
	}
    }

    public boolean isClosed()
    {
	boolean result = true;
        if (socketChannel != null)
	{
	    result = ! socketChannel.isOpen();
	}
	else if (socket != null)
	{
	    result = socket.isClosed();
	}
	return result;
    }

    public boolean isServer()
    {
	throwException("isServer");
	return false;
    }

    public boolean shouldRegisterReadEvent()
    {
	return false;
    }

    public boolean shouldRegisterServerReadEvent()
    {
	return true;
    }

    public void setState(String state)
    {
	throwException("setState");
    }

    public void writeLock()
    {
	throwException("writeLock");
    }

    public void writeUnlock()
    {
	throwException("writeUnlock");
    }

    public void sendWithoutLock(OutputObject outputObject)
    {
	try {
	    PrintWriter printWriter = 
		new PrintWriter(new OutputStreamWriter(socket.getOutputStream()));
	    printWriter.print(outputObject.toString());
	    printWriter.flush();
	} catch (Exception e) {
	    // REVISIT
	    System.out.println("SOAPConnection.sendWithoutLock: " + e);
	}

	if (broker.transportDebugFlag) {
	    System.out.println();
	    System.out.println("----------SENDING----------");
	    System.out.println(outputObject.toString());
	    System.out.println("---------------------------");
	    System.out.println();
	}

    }

    public void registerWaiter(MessageMediator messageMediator)
    {
	System.out.println("SOAPConnection.registerWaiter: ignored.");
    }

    public void unregisterWaiter(MessageMediator messageMediator)
    {
	System.out.println("SOAPConnection.unregisterWaiter: ignored.");
    }

    public void setConnectionCache(ConnectionCache connectionCache)
    {
	throwException("setConnectionCache");
    }

    public ConnectionCache getConnectionCache()
    {
	throwException("getConnectionCache");
	return null;
    }

    public CorbaAcceptor getAcceptor()
    {
	throwException("getAcceptor");
	return null;
    }

    public ContactInfo getContactInfo()
    {
	throwException("getContactInfo");
	return null;
    }

    ////////////////////////////////////////////////////
    //
    // EventHandler methods
    //

    public SelectableChannel getChannel()
    {
	return socketChannel;
    }

    public int getInterestOps()
    {
	return SelectionKey.OP_READ;
    }

    //    public Acceptor getAcceptor() - already defined above.

    public Connection getConnection()
    {
	return this;
    }

    ////////////////////////////////////////////////////
    //
    // Work methods.  REVISIT - not necessary for this example
    // by configuration?
    //

    public String getName()
    {
	return this.toString();
    }

    public void doWork()
    {
	System.out.println("----- Connection: BEGIN doEvent -----");

	if (! selectionKey.isReadable()) {
	    throw new RuntimeException("Expecting isReadable.");
	}

	read();

	selectionKey.interestOps(selectionKey.interestOps() |
				 getInterestOps());

	System.out.println("----- Connection: END doWork -----");
    }

    protected long enqueueTime;

    public void setEnqueueTime(long timeInMillis)
    {
	enqueueTime = timeInMillis;
    }

    public long getEnqueueTime()
    {
	return enqueueTime;
    }

    //
    //
    //

    private void throwException(String message)
    {
	throw new Error(
            "SOAPConnection." + message + " should not be called.");
    }

    public String toString()
    {
	return "[SOAPConnection " + socket + "]";
    }

    public EventHandler getEventHandler()
    {
	return this;
    }
}

// End of file.

