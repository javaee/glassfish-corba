/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright (c) 1997-2018 Oracle and/or its affiliates. All rights reserved.
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

package corba.rogueclient;

import java.rmi.RemoteException;
import java.util.Properties;

import javax.rmi.PortableRemoteObject;
import javax.naming.InitialContext;

import com.sun.corba.ee.spi.misc.ORBConstants;
import corba.hcks.U;

public class Client extends Thread
{
    private final static int NUMBER_OF_CLIENTS = 6;
    private final static boolean dprint = false;
    private final static int stringSize = 131072;
    private final static int TEST_SIZE = 50;
    private final static String stringOf36 = "abcdefghijklmnopqrstuvwxyz0123456789";
    private static String reallyReallyBigString = null;
    private static String tmpString = null;

    private static void initializeReallyBigString() {
        StringBuffer sb = new StringBuffer(stringSize);
        int index = 0;
        final int lengthOfStr = stringOf36.length();
        for (int i = 0; i < stringSize; i++) {
            index = i % lengthOfStr;
            sb.append(stringOf36.charAt(index));
        }
        reallyReallyBigString = sb.toString();
    }

    private void runTest(Tester tester, int iterations)
        throws RemoteException {
        for (int i = 0; i < iterations; i++) {
            tmpString = tester.passString(reallyReallyBigString);
        }
    }

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
            runTest(tester, TEST_SIZE);
        } catch (Throwable t) {
            U.sop("Unexpected throwable...");
            t.printStackTrace();
            System.exit(1);
        }
    }

    public static void main(String args[]) {
        if (dprint) {
            Properties props = new Properties();
            props.put(ORBConstants.DEBUG_PROPERTY, "transport, giop");
        }

        initializeReallyBigString();

        Client[] clients = new Client[NUMBER_OF_CLIENTS];
        for (int i = 0; i < clients.length; i++) {
            clients[i] = new Client();
        }

        for (int i = 0; i < clients.length; i++) {
            U.sop("Beginning client[" + i + "] test...");
            clients[i].start();
        }

        for (int i = 0; i < clients.length; i++) {
            try {
                clients[i].join();
                U.sop("Client[" + i + "] test finished successfully...");
            } catch (InterruptedException ex) {
                ex.printStackTrace();
            }
        }
    }
}

