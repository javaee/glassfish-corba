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

package corba.rogueclient;

import com.sun.corba.se.spi.misc.ORBConstants;
import com.sun.corba.se.spi.ior.IOR;
import com.sun.corba.se.spi.ior.iiop.GIOPVersion;
import com.sun.corba.se.spi.ior.iiop.IIOPAddress;
import com.sun.corba.se.spi.ior.iiop.IIOPProfile;
import com.sun.corba.se.spi.ior.iiop.IIOPProfileTemplate;
import com.sun.corba.se.spi.presentation.rmi.StubAdapter;
import com.sun.corba.se.spi.protocol.ClientDelegate;
import com.sun.corba.se.spi.transport.ContactInfoList;
import com.sun.corba.se.impl.misc.ORBUtility;
import com.sun.corba.se.impl.protocol.giopmsgheaders.Message;


import corba.hcks.U;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.channels.SocketChannel;
import java.rmi.RemoteException;
import java.util.Properties;
import javax.naming.InitialContext;
import javax.rmi.PortableRemoteObject;

import com.sun.corba.se.spi.orbutil.test.JUnitReportHelper ;

import java.util.concurrent.atomic.AtomicInteger ;

public class RogueClient extends Thread
{
    // shared across all instances of RogueClients
    private static final boolean dprint = false;
    private static final boolean itsBigEndian = (ByteOrder.BIG_ENDIAN == ByteOrder.nativeOrder());
    private static final int NUM_ROGUE_CLIENTS = 10;
    private static final byte HEX_G = (byte)0x47;
    private static final byte HEX_I = (byte)0x49;
    private static final byte HEX_O = (byte)0x4f;
    private static final byte HEX_P = (byte)0x50;
    private static final byte[] BOGUS_BYTES = new byte[] {
	0x00,0x00,0x00,0x06,0x03,0x00,0x00,0x00,0x00,0x00,
	0x00,0x02,0x00,0x00,0x00,0x19,-0x51,-0x55,-0x35,0x00,
        0x00,0x00,0x00,0x02,0x7a,-0x24,0x1d,-0x69,0x00,0x00,
	0x00,0x08,0x00,0x00,0x00,0x01 };

    // unique to each instance of a RogueClient
    private String itsHostname = null;
    private int itsPort = 0;
    private SocketChannel itsSocketChannel = null;
    private Socket itsSocket = null;
    private JUnitReportHelper helper = new JUnitReportHelper( RogueClient.class.getName() ) ;
    private int createConnectionToServerCallCounter = 0 ;
    private static AtomicInteger numFailures = new AtomicInteger() ;

    private static volatile boolean useHelper = true ;

    private void start( String name, int ctr ) {
        if (useHelper)
            helper.start( name + ctr ) ;

        U.sop( "RogueClient." + name + "()" ) ;
    }

    private void start( String name ) {
        if (useHelper)
            helper.start( name ) ;

        U.sop( "RogueClient." + name + "()" ) ;
    }

    private void handlePass() {
        if (useHelper)
            helper.pass() ;

        U.sop( "PASS" ) ;
    }

    private void handleException(Exception ex) throws Exception {
        numFailures.incrementAndGet() ;

	U.sop("Unexpected exception -> " + ex);

        StackTraceElement[] ste = ex.getStackTrace();
        for (int i = 0; i < ste.length; i++) {
            U.sop(ste[i].toString());
        }

	helper.fail( ex ) ;

        throw ex ;
    }

    private void printBuffer(ByteBuffer byteBuffer) {
	U.sop("+++++++ GIOP Buffer ++++++++\n");
        U.sop("Current position: " + byteBuffer.position());
        U.sop("Total length : " + byteBuffer.limit() + "\n");

        char[] charBuf = new char[16];

        try {

            for (int i = 0; i < byteBuffer.position(); i += 16) {
                
                int j = 0;
                
                // For every 16 bytes, there is one line
                // of output.  First, the hex output of
                // the 16 bytes with each byte separated
                // by a space.
                while (j < 16 && j + i < byteBuffer.position()) {
                    int k = byteBuffer.get(i + j);
                    if (k < 0)
                        k = 256 + k;
                    String hex = Integer.toHexString(k);
                    if (hex.length() == 1)
                        hex = "0" + hex;
                    System.out.print(hex + " ");
                    j++;
                }
                
                // Add any extra spaces to align the
                // text column in case we didn't end
                // at 16
                while (j < 16) {
                    System.out.print("   ");
                    j++;
                }
                
                // Now output the ASCII equivalents.  Non-ASCII
                // characters are shown as periods.
                int x = 0;
		while (x < 16 && x + i < byteBuffer.position()) {
                    if (ORBUtility.isPrintable((char)byteBuffer.get(i + x)))
                        charBuf[x] = (char)byteBuffer.get(i + x);
                    else
                        charBuf[x] = '.';
                    x++;
                }
                U.sop(new String(charBuf, 0, x));
            }
        } catch (Throwable t) {
            t.printStackTrace();
        }
        U.sop("++++++++++++++++++++++++++++++");
    }

    private void getHostnameAndPort(Tester tester)
    {
	// Get the host and port number of server
	U.sop("RogueClient.getHostnameAndPort()");
	ClientDelegate delegate =
	    (ClientDelegate)StubAdapter.getDelegate(tester);
	ContactInfoList ccil =
	    (ContactInfoList)delegate.getContactInfoList();
	IOR effectiveTargetIOR = ccil.getEffectiveTargetIOR();
	IIOPProfile iiopProfile = effectiveTargetIOR.getProfile();
        IIOPProfileTemplate iiopProfileTemplate =
	    (IIOPProfileTemplate)iiopProfile.getTaggedProfileTemplate() ;
	IIOPAddress primary = iiopProfileTemplate.getPrimaryAddress() ;

	itsHostname = primary.getHost().toLowerCase();
	itsPort = primary.getPort();
	
	String testerIOR = tester.toString();
	U.sop("\tRemote object, Tester " + testerIOR);
	U.sop("\tCan be found at:");
	U.sop("\tHostname -> " + itsHostname);
	U.sop("\tPort -> " + itsPort);
	U.sop("Successful");
    } 

    private void createConnectionToServer() throws Exception {
        start( "createConnectionToServer",
            createConnectionToServerCallCounter++ ) ;
        
	// create SocketChannel to server
	try {
	    InetSocketAddress isa = new InetSocketAddress(itsHostname, itsPort);
	    itsSocketChannel = ORBUtility.openSocketChannel(isa);
	}
	catch (Exception ex) {
	    handleException(ex);
	}

        handlePass() ;
    }

    private void write_octet(byte[] theBuf, int index, byte theValue) {
	theBuf[index] = theValue;
    }

    private void buildGIOPHeader(byte[] theBuf, int theMessageSize)
    {
	int index = 0;

	// write GIOP string, always written big endian
	write_octet(theBuf, index++, HEX_G);
	write_octet(theBuf, index++, HEX_I);
	write_octet(theBuf, index++, HEX_O);
	write_octet(theBuf, index++, HEX_P);

	// write GIOP version 1.2, bytes 5,6
	write_octet(theBuf, index++, GIOPVersion.DEFAULT_VERSION.getMajor());
	write_octet(theBuf, index++, GIOPVersion.DEFAULT_VERSION.getMinor());

	// write endian-ness and no fragment bit (either 0x00 or 0x01)
	// byte 6, bits 0 & 1
	if (itsBigEndian) {
	    write_octet(theBuf, index++, Message.FLAG_NO_FRAG_BIG_ENDIAN);
	} else {
	    write_octet(theBuf, index++, Message.LITTLE_ENDIAN_BIT);
	}

	// write GIOPRequest type, byte 8
	write_octet(theBuf, index++, Message.GIOPRequest);

	// write message size
	write_message_size(theBuf, index, theMessageSize);
    }

    private void write_message_size(byte[] theBuf, int index, int theMessageSize) {
	// write message size, bytes 9,10,11,12
	if (itsBigEndian) {
	    write_octet(theBuf, index++, (byte)((theMessageSize >>> 24) & 0xFF));
	    write_octet(theBuf, index++, (byte)((theMessageSize >>> 16) & 0xFF));
	    write_octet(theBuf, index++, (byte)((theMessageSize >>> 8) & 0xFF));
	    write_octet(theBuf, index++, (byte)(theMessageSize & 0xFF));
	} else {
	    write_octet(theBuf, index++, (byte)(theMessageSize & 0xFF));
	    write_octet(theBuf, index++, (byte)((theMessageSize >>> 8) & 0xFF));
	    write_octet(theBuf, index++, (byte)((theMessageSize >>> 16) & 0xFF));
	    write_octet(theBuf, index++, (byte)((theMessageSize >>> 24) & 0xFF));
	}
    }

    private void sendData(ByteBuffer byteBuffer, int numBytesToWrite)
	throws Exception { 

	int bytesWrit = 0;
	do {
	    bytesWrit = itsSocketChannel.write(byteBuffer);
	} while (bytesWrit < numBytesToWrite);
    }

    private ByteBuffer createGIOPMessage() {

	// create a GIOP header
	byte[] request = new byte[Message.defaultBufferSize];

	// build GIOP header
	buildGIOPHeader(request, request.length - Message.GIOPMessageHeaderLength);

	// add some bogus junk to a rogue request
	for (int i = 0; i < BOGUS_BYTES.length; i++) {
	    write_octet(request,
		        i+Message.GIOPMessageHeaderLength,
			BOGUS_BYTES[i]);
	}

	ByteBuffer byteBuffer = ByteBuffer.wrap(request);
	byteBuffer.position(0);
	byteBuffer.limit(Message.GIOPMessageHeaderLength+BOGUS_BYTES.length);

	if (dprint) {
	    ByteBuffer viewBuffer = byteBuffer.asReadOnlyBuffer();
	    viewBuffer.position(Message.GIOPMessageHeaderLength+BOGUS_BYTES.length);
	    printBuffer(viewBuffer);
	}

	return byteBuffer;
    }

    private void runValidHeaderSlowBody() throws Exception {
        start( "runValidHeaderSlowBody" ) ;

	ByteBuffer byteBuffer = createGIOPMessage();

	// send full, valid GIOP header
	ByteBuffer b = ByteBuffer.allocateDirect(Message.GIOPMessageHeaderLength);
	for (int i = 0; i < Message.GIOPMessageHeaderLength; i++) {
	    b.put(byteBuffer.get(i));
	}
	b.flip();

	try {
	    sendData(b, Message.GIOPMessageHeaderLength);
	    
	    // send message body 1 byte a time with a delay between them
	    for (int i = Message.GIOPMessageHeaderLength; i < byteBuffer.limit(); i++) {
		b = ByteBuffer.allocateDirect(1);
		b.put(byteBuffer.get(i));
		b.flip();
		sendData(b, 1);
		Thread.sleep(250);
	    }
	    Thread.sleep(5000);
	} catch (IOException ioe) {
	    // We expect Server to complain with an IOException.
	    // So, we must close the connection and re-open it.
	    U.sop("\tReceived expected IOException: " + ioe.toString());
	    U.sop("\tWill attempt to re-establish connection to server..");
	    try {
		itsSocketChannel.close();
	    } catch (IOException ioex) {
		handleException(ioex);
                throw ioex ;
	    }

            handleException( ioe ) ;
	    createConnectionToServer();
            throw ioe ;
	} catch (Exception ex) {
	    handleException(ex);
            throw ex ;
	}

        handlePass() ;
    }

    private void runSlowGIOPHeader() throws Exception {
        start( "runSlowGIOPHeader" ) ;

	ByteBuffer byteBuffer = createGIOPMessage();

	// send GIOP header
	try {
	    // send 1 byte a time with a delay between them
	    for (int i = 0; i < byteBuffer.limit(); i++) {
		ByteBuffer b = ByteBuffer.allocateDirect(1);
		b.put(byteBuffer.get(i));
		b.flip();
		sendData(b, 1);
		Thread.sleep(500);
	    }
	    Thread.sleep(5000);
	} catch (IOException ioe) {
	    // We expect Server to complain with an IOException.
	    // So, we must close the connection and re-open it.
	    U.sop("\tReceived expected IOException: " + ioe.toString());
	    U.sop("\tWill attempt to re-establish connection to server...");
	    try {
		itsSocketChannel.close();
	    } catch (IOException ioex) {
		handleException(ioex);
                throw ioex ;
	    }
	    createConnectionToServer();
	} catch (Exception ex) {
	    handleException(ex);
            throw ex ;
	}

        handlePass() ;
    }

    private void runValidHeaderBogusLength() throws Exception {
        start( "runValidHeaderBogusLength" ) ;

	ByteBuffer byteBuffer = createGIOPMessage();
	write_message_size(byteBuffer.array(),8,byteBuffer.limit() + 50);

	try {
	    // send valid header with bogus message length
	    sendData(byteBuffer, byteBuffer.limit());
	    Thread.sleep(10000);
	} catch (Exception ex) {
	    handleException(ex);
	}

        handlePass() ;
	U.sop("PASSED");
    }


    private void runSendMessageAndCloseConnection() throws Exception {
        start( "runSendMessageAndCloseConnection" ) ;
        
        ByteBuffer byteBuffer = createGIOPMessage();
        byteBuffer.flip();
        try {
            sendData(byteBuffer, byteBuffer.limit());
            // immediately close the channel
            itsSocketChannel.close();
        } catch (Exception ex) {
            handleException(ex);
        }

        handlePass() ;

        createConnectionToServer();
    }

    private void runRogueConnectManyTests() throws Exception {
        helper.start( "runRogueConnectManyTests" ) ;
        try {
            U.sop("RogueClient.runRogueConnectManyTests()");
            // create a bunch of RogueClients and let them bang away
            RogueClient[] rogueClients = new RogueClient[NUM_ROGUE_CLIENTS];

            for (int i = 0; i < NUM_ROGUE_CLIENTS; i++) {
                rogueClients[i] = new RogueClient();
            }

            for (int i = 0; i < rogueClients.length; i++) {
                rogueClients[i].start();
            }
            
            for (int i = 0; i < rogueClients.length; i++) {
                rogueClients[i].join();
            }

            U.sop("PASSED");
        } finally {
            if (numFailures.get() == 0)
                helper.pass() ;
            else
                helper.fail( "Failed with " + numFailures.get() + " errors" ) ;
        }
    }

    private void runSaneTest(Tester tester)
       	throws RemoteException
    {
	// call a method on the Tester object
	U.sop("RogueClient.runSaneTest()");
	String desc = tester.getDescription();
	U.sop("\tGot 'Tester' description: " + desc);
	U.sop("PASSED");
    }

    @Override
    public void run() {
	try {
            U.sop("Finding Tester ...");
            InitialContext rootContext = new InitialContext();
            U.sop("Looking up Tester...");
            java.lang.Object tst = rootContext.lookup("Tester");
            U.sop("Narrowing...");
            Tester tester 
                = (Tester)PortableRemoteObject.narrow(tst,
                                                      Tester.class);
	    getHostnameAndPort(tester);
	    createConnectionToServer();
	    runSaneTest(tester);
	    runValidHeaderBogusLength();
	    runSaneTest(tester);
	    runSlowGIOPHeader();
	    runSaneTest(tester);
	    runValidHeaderSlowBody();
            runSendMessageAndCloseConnection();
        } catch (org.omg.CORBA.COMM_FAILURE c) {
            StackTraceElement[] ste = c.getStackTrace();
            StringBuffer sb = new StringBuffer(256);
            for (int i = 0; i < ste.length; i++) {
                sb.append(ste[i]);
            }
            U.sop("Received an expected org.omg.COMM_FAILURE: " + c.toString()
                    + " stack trace :\n" + sb.toString());
        } catch (Throwable t) {
    	    U.sop("Unexpected throwable!!!");
            t.printStackTrace();
            helper.done() ;
            System.exit(1) ;
        } finally {
            helper.done() ;
        }
    }

    public static void main(String args[]) {
        U.sop("Beginning test...");

	if (dprint) {
	    Properties props = new Properties();
	    props.put(ORBConstants.DEBUG_PROPERTY, "transport,giop");
	}

	// run a single RogueClient
	RogueClient rogueClient = new RogueClient();
	try {
	    rogueClient.start();
	    rogueClient.join();

            useHelper = false ;

	    // run a bunch of RogueClients
	    rogueClient.runRogueConnectManyTests();

	} catch (Exception ex) {
            ex.printStackTrace() ;
	} 

        int failures = numFailures.get() ;
        if (failures == 0) 
            U.sop("Test finished successfully...");

        System.exit( numFailures.get() ) ;
    }
}

