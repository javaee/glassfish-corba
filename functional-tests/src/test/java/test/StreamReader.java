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
package test;

import java.io.*;
import java.util.IdentityHashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

/**
 * A thread which pipes everything from an input stream
 * to an output stream.  Note that this closes the
 * output stream and dies when the reader encounters an end of
 * stream!  Thus just end the stream it's reading from to make
 * it quit.  For instance, this should occur when a process
 * ends.
 */
public class StreamReader extends Thread
{
    /**
     * The NULL_OUTPUT_STREAM can be used to create a StreamReader
     * which reads everything from the input stream and can even
     * wait for a handshake, but doesn't write to anything.
     */
    public static final OutputStream NULL_OUTPUT_STREAM 
        = new NullOutputStream();

    private OutputStream originalStream;
    private PrintWriter out;
    private BufferedReader in;
    private String prefix = null;
    private String handshake = null;
    private int handshakeStatus = WAITING;
    private List inputReceived = new LinkedList();

    // Handshake status
    private static final int WAITING = 0;
    private static final int RECEIVED = 1;
    private static final int ERROR = 2;

    // Prevent garbage collection so someone can run Processes
    // without keeping references to their StreamReaders or
    // ProcessMonitors, and still not have the program hang.
    private static Map selfReferences = new IdentityHashMap(11);

    public StreamReader(OutputStream output,
                        InputStream input) {
        originalStream = output;
        out = new PrintWriter(output, true);
        in = new BufferedReader(new InputStreamReader(input));
        setDaemon(true);
    }


    public StreamReader(OutputStream output,
                        InputStream input,
                        String handshake) {
        this(output, input);
        this.handshake = handshake;
    }

    public StreamReader(OutputStream output,
                        InputStream input,
                        String handshake,
                        String prefix) {
        this(output, input, handshake);
        this.prefix = prefix;
    }

    private final void output(String msg) {
        if (prefix == null)
            out.println(msg);
        else
            out.println(prefix + msg);
    }

    public void run() {

        try {

            // Keep a reference to ourselves around to try to avoid garbage
            // collection or stopping of this thread when it's out of scope.
            // The thread should die either when the VM exits or the end of
            // the stream is reached.
            synchronized(selfReferences) {
                selfReferences.put(this, this);
            }

            setName("StreamReader");

            int inputReceivedThreshold = 0;
            while (true) {
                String input = null ;

                try {
                    input = in.readLine();
                    if (Test.debug) 
                        System.out.println( "Streamreader.read: " + input ) ;

                    // readLine should return null at the end of the
                    // stream
                    if (input == null)
                        break;
                    if (++inputReceivedThreshold > 10000) {
                       inputReceived.clear();
                    }
        
                } catch (java.io.IOException exc) {
                    // We also can get errors due to the InputStream being
                    // closed.  Simply treat these as termination.  This
                    // seems to happen on JDK 1.4.1 only?
                    break ;
                }

                // For seeing what when wrong if no handshake.
                inputReceived.add(input);

                output(input);

                if (handshake != null && 
                    handshakeStatus == WAITING && 
                    handshake.equals(input)) {

                    signalHandshakeReceived();
                }
                    
            }

            // Process/input stream ended before the handshake
            if (handshake != null && handshakeStatus == WAITING)
                signalBadHandshake();

        } finally {
            synchronized(selfReferences) {
                selfReferences.remove(this);
            }
            out.flush();
            if (originalStream != System.out &&
                originalStream != System.err)
                out.close();
        }
    }
    
    private synchronized void signalBadHandshake() {
        if (Test.debug)
            System.out.println( "Streamreader.signalBadHandshake called" ) ;
        handshakeStatus = ERROR;
        this.notifyAll();
    }

    private synchronized void signalHandshakeReceived() {
        if (Test.debug)
            System.out.println( "Streamreader.signalHandshakeReceived called" ) ;
        handshakeStatus = RECEIVED;
        this.notifyAll();
    }

    public synchronized void waitForHandshake() 
        throws InterruptedException, Exception {

        waitForHandshake(0);
    }

    public synchronized void waitForHandshake(long timeout) 
        throws InterruptedException, Exception {

        if (handshake != null) {
            if (handshakeStatus == WAITING)
                this.wait(timeout);

            if (handshakeStatus == ERROR)
                throw new Exception("Terminated before reading handshake ("
                                    + handshake + ')' + '\n'
                                    + formatInputReceived());

            if (handshakeStatus != RECEIVED)
                throw new Exception("Timed out waiting for handshake ("
                                    + handshake + ")" + "\n"
                                    + formatInputReceived());
        }
    }

    private String formatInputReceived()
    {
        boolean headerWritten = false;
        StringBuffer sb = new StringBuffer();
        Iterator i = inputReceived.iterator();
        while (i.hasNext()) {
            if (! headerWritten) {
                sb.append("Tail of input received so far:\n");
                headerWritten = true;
            }
            String line = (String) i.next();
            sb.append(line + '\n');
        }
        return sb.toString();
    }

    private static final class NullOutputStream extends java.io.OutputStream
    {
        public final void close() {}
        public final void flush() {}
        public final void write(byte[] b) {}
        public final void write(byte[] b, int offset, int len) {}
        public final void write(int b) {}
    }
}


