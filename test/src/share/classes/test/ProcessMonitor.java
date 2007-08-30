/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1998-2007 Sun Microsystems, Inc. All rights reserved.
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

/*
 * Licensed Materials - Property of IBM
 * RMI-IIOP v1.0
 * Copyright IBM Corp. 1998 1999  All Rights Reserved
 *
 * US Government Users Restricted Rights - Use, duplication or
 * disclosure restricted by GSA ADP Schedule Contract with IBM Corp.
 */

package test;

import java.io.*;

/**
 * ProcessMonitor provides a thread which will consume output from a
 * java.lang.Process and write it to the specified local streams.
 *
 * @version	1.0, 6/11/98
 * @author	Bryan Atsatt
 *
 * Split into StreamReaders by Everett Anderson 8/1/2000.  Note that
 * the output streams will be closed at the end of the
 * process's inputs.
 */
public class ProcessMonitor {
    Process process;
    boolean run = true;
    StreamReader outReader;
    StreamReader errReader;

    /**
     * Constructor.
     * @param theProcess The process to monitor.
     * @param out The stream to which to copy Process.getInputStream() data.
     * @param err The stream to which to copy Process.getErrorStream() data.
     */
    public ProcessMonitor (Process theProcess,
			   OutputStream out,
			   OutputStream err) {

        process = theProcess;
        
        outReader = new StreamReader(out, theProcess.getInputStream());
        errReader = new StreamReader(err, theProcess.getErrorStream());
    }
    
    /**
     * Constructor.
     * @param theProcess The process to monitor.
     * @param out The stream to which to copy Process.getInputStream() data.
     * @param err The stream to which to copy Process.getErrorStream() data.
     * @param prefix String to prepend to all copied output lines.
     */
    public ProcessMonitor (Process theProcess,
			   OutputStream out,
			   OutputStream err,
			   String handshake) {
        process = theProcess;

        outReader = new StreamReader(out,
                                     process.getInputStream(),
                                     handshake,
                                     null);
        errReader = new StreamReader(err,
                                     process.getErrorStream(),
                                     null,
                                     null);
    }

    public ProcessMonitor (Process theProcess,
                           OutputStream out,
                           OutputStream err,
                           String handshake,
                           String prefix) {

        process = theProcess;

        // Always assume the handshake is on stdout
        outReader = new StreamReader(out,
                                     process.getInputStream(),
                                     handshake,
                                     prefix);
        errReader = new StreamReader(err,
                                     process.getErrorStream(),
                                     null,
                                     prefix);
    }


    public void start() {
        outReader.start();
        errReader.start();
    }

    public void waitForHandshake() throws InterruptedException, Exception {
        outReader.waitForHandshake();
    }

    public void waitForHandshake(long timeout) throws InterruptedException, Exception {
        outReader.waitForHandshake(timeout);
    }

    // The process should have been killed/finished before
    // calling this.
    public void finishWriting() throws InterruptedException
    {
        outReader.join();
        errReader.join();
    }
}
