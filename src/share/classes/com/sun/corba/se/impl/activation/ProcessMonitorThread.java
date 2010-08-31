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

package com.sun.corba.se.impl.activation;

import java.util.*;
import com.sun.corba.se.spi.orbutil.ORBConstants;

/** ProcessMonitorThread is started when ServerManager is instantiated. The 
  * thread wakes up every minute (This can be changed by setting sleepTime) and
  * makes sure that all the processes (Servers) registered with the ServerTool
  * are healthy. If not the state in ServerTableEntry will be changed to
  * De-Activated.
  * Note: This thread can be killed from the main thread by calling 
  *       interrupThread()
  */
public class ProcessMonitorThread extends java.lang.Thread {
    private Map<Integer,ServerTableEntry> serverTable;
    private int sleepTime; 
    private static ProcessMonitorThread instance = null;

    private ProcessMonitorThread( Map<Integer,ServerTableEntry> ServerTable, int SleepTime ) {
        serverTable = ServerTable;
        sleepTime = SleepTime;
    }

    public void run( ) {
        while( true ) {
            try {
                // Sleep's for a specified time, before checking
                // the Servers health. This will repeat as long as
                // the ServerManager (ORBD) is up and running.
                Thread.sleep( sleepTime );
            } catch( java.lang.InterruptedException e ) {
                break;
            }
            synchronized ( serverTable ) {
                // Check each ServerTableEntry to make sure that they
                // are in the right state.
                Iterator serverList = serverTable.values().iterator();
                checkServerHealth( serverList );
            }
        }
    }

    private void checkServerHealth( Iterator serverList ) {
        while (serverList.hasNext( ) ) {
            ServerTableEntry entry = (ServerTableEntry) serverList.next();
            entry.checkProcessHealth( );
        }
    }

    static void start( Map<Integer,ServerTableEntry> serverTable ) { 
	int sleepTime = ORBConstants.DEFAULT_SERVER_POLLING_TIME;

	String pollingTime = System.getProperties().getProperty( 
	    ORBConstants.SERVER_POLLING_TIME ); 

	if ( pollingTime != null ) {
	    try {
		sleepTime = Integer.parseInt( pollingTime ); 
	    } catch (Exception e ) {
		// Too late to complain, Just use the default 
		// sleepTime
	    }
	}

	instance = new ProcessMonitorThread( serverTable, 
	    sleepTime );
	instance.setDaemon( true );
	instance.start();
    }

    static void interruptThread( ) {
        instance.interrupt();
    }
}
 
