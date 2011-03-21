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
//
// Created       : 2003 Apr 09 (Wed) 16:54:21 by Harold Carr.
// Last Modified : 2003 Sep 29 (Mon) 16:25:55 by Harold Carr.
//

package corba.connections;

import java.rmi.RemoteException;
import javax.naming.InitialContext;

import java.util.Properties ;

import com.sun.corba.se.spi.orb.ORB ;

import corba.hcks.C;
import corba.hcks.U;

import com.sun.corba.se.spi.presentation.rmi.StubAdapter ;

public class Client 
{
    public static boolean showInbound = true;
    public static int NUM_THREADS = 100;
    public static boolean inParallel = false;

    public static ORB orb;
    public static InitialContext initialContext;
    public static RemoteInterface c1s11;
    public static RemoteInterface c1s12;
    public static RemoteInterface c1s21;
    public static RemoteInterface c1s22;

    public static Struct[] instance;
    public static Struct[] returnInstance;

    public static String name;

    public static ConnectionStatistics stats;

    public static void main(String[] av)
    {
	instance = Struct.getSampleInstance();

        try {
	    name = av[0];

	    U.sop(name + " ORB.init ...");

	    Properties props = new Properties() ;
	    props.setProperty( "com.sun.corba.se.ORBDebug", "subcontract" ) ;
	    orb = (com.sun.corba.se.spi.orb.ORB)ORB.init(av, props);
            stats = new ConnectionStatistics( orb ) ;

	    U.sop(name + " InitialContext ...");

	    initialContext = C.createInitialContext(orb);

	    showInbound = true;

	    pstats(" after InitialContext");

	    c1s11 = lookup(-1, Server.service11, initialContext);
	    c1s12 = lookup(-1, Server.service12, initialContext);

	    pstats(" after lookup s1*");

	    c1s21 = lookup(-1, Server.service21, initialContext);
	    c1s22 = lookup(-1, Server.service22, initialContext);

	    pstats(" after lookup s2*");

	    showInbound = false;

	    U.sop(name + " making call...");

	    call(c1s11, "c1s11");
	    callBlock(c1s11, "c1s11 BLOCK");
	    call(c1s12, "c1s12");
	    callResume(c1s11, "c1s11 RESUME");
	    call(c1s21, "c1s21");
	    callBlock(c1s21, "c1s21 BLOCK");
	    call(c1s22, "c1s22");
	    callResume(c1s21, "c1s21 RESUME");

	    for (int i = 0; i < NUM_THREADS; i++) {
		//boolean exitAndPrintResult = (i % 10 == 0) ? true : false;
		boolean exitAndPrintResult = true;
		CallThread callThread =
		    new CallThread(i, exitAndPrintResult, exitAndPrintResult);
		if (inParallel) {
		    callThread.start();
		} else {
		    callThread.doWork();
		}
	    }

	    U.sop(name + " PASSED");

	} catch (Exception e) {
	    e.printStackTrace(System.out);
	    U.sop(name + " FAILED");
	    System.exit(1);
	}
    }

    public static RemoteInterface lookup(int i, String rn,
					 InitialContext initialContext)
	throws
	    Exception
    {
	RemoteInterface result = (RemoteInterface)
	    U.lookupAndNarrow(rn, RemoteInterface.class, initialContext);

	if (false) {
	    com.sun.corba.se.spi.ior.IOR ior =
		((com.sun.corba.se.spi.transport.ContactInfoList)
		 ((com.sun.corba.se.spi.protocol.ClientDelegate)
		  StubAdapter.getDelegate( result )).
		  getContactInfoList()).getTargetIOR();

	    ORB thisOrb = (ORB)StubAdapter.getORB( result ) ;

	    U.sop(i + ": lookup: " + rn 
		  + " orbIdentity: " + System.identityHashCode(thisOrb)
		  + " stubIdentity: " + System.identityHashCode(result)
		  + " iorIdentity: " + System.identityHashCode(ior)
		  + " iorHash: " + ior.hashCode());
	}
	return result;
    }

    public static void call(RemoteInterface r, String msg)
	throws
	    Exception
    {
	returnInstance = r.method(instance);
	pstats(msg);
	U.sop(r.testMonitoring());
    }

    public static void callBlock(RemoteInterface r, String msg)
	throws
	    Exception
    {
	BlockThread blockThread = new BlockThread(r);
	blockThread.start();
	Thread.sleep(2000);
	pstats(msg);
	U.sop(r.testMonitoring());
    }

    public static void callResume(RemoteInterface r, String msg)
	throws
	    Exception
    {
	r.resume();
	Thread.sleep(2000);
	pstats(msg);
	U.sop(r.testMonitoring());
    }

    public static void pstats(String msg)
    {
	outbound(msg);
	inbound(msg);
    }

    public static void outbound(String msg)
    {
	stats.outbound(name + " " + msg, (com.sun.corba.se.spi.orb.ORB)orb);
    }

    public static void inbound(String msg)
    {
	if (showInbound) {
	    stats.inbound(name + " " + msg, (com.sun.corba.se.spi.orb.ORB)orb);
	}
    }
}

class BlockThread
    extends
	Thread
{
    RemoteInterface r;

    BlockThread(RemoteInterface r)
    {
	this.r = r;
    }

    public void run()
    {
	try {
	    r.block();
	} catch (RemoteException e) {
	    e.printStackTrace(System.out);
	    U.sop("BlockThread FAILED");
	    System.exit(1);
	}
    }
}

class CallThread
    extends
	Thread
{
    int i;
    boolean exit;
    boolean printResult;

    CallThread(int i, boolean exit, boolean printResult)
    {
	this.i = i;
	this.exit = exit;
	this.printResult = printResult;
    }

    public void run()
    {
	doWork();
    }

    public void doWork()
    {
	try {
	    U.sop(i + ": CallThread ORB.init:");
	    ORB orb = (ORB)ORB.init((String[])null, null);
	    U.sop(i + ": CallThread InitialContext:");
	    InitialContext initialContext = C.createInitialContext(orb);
	    U.sop(i + ": CallThread lookup:");
	    RemoteInterface s11 =
		Client.lookup(i, Server.service11, initialContext);
	    RemoteInterface s21 =
		Client.lookup(i, Server.service21, initialContext);
	    U.sop(i + ": CallThread call:");
	    String s11Result = s11.testMonitoring();
	    String s21Result = s21.testMonitoring();
	    U.sop(i + ": CallThread call complete:");
	    if (printResult) {
		U.sop(i + ": CallThread result: ");
		U.sop(s11Result);
		U.sop(s21Result);
	    }
	    if (! exit) {
		orb.run();
	    }
	    U.sop(i + ": exiting");
	} catch (Exception e) {
	    e.printStackTrace(System.out);
	    U.sop("CallThread " + i + " FAILED");
	    System.exit(1);
	}
    }
}

// End of file.
