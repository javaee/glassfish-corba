/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2000-2007 Sun Microsystems, Inc. All rights reserved.
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
package corba.framework;

import java.io.*;
import java.util.*;
import test.*;

/**
 * Runs the class in a separate process using the JPDA options so that it can be
 * remotely debugged.  This of course assumes the class
 * has a static main method, etc.  Output is redirected appropriately by
 * using test.ProcessMonitor.
 */
public class RDebugExec extends ExternalExec
{
    public void initialize(String className,
                           String processName,
                           Properties environment,
                           String VMArgs[],
                           String programArgs[],
                           OutputStream out,
                           OutputStream err,
                           Hashtable extra) throws Exception
    {
        super.initialize(className,
                         processName,
                         environment,
                         VMArgs,
                         programArgs,
                         System.out,
                         err,
                         extra);
    }

    protected String[] getDebugVMArgs()
    {
	String[] result = { "-Xdebug", "-Xnoagent", 
	    "-Xrunjdwp:transport=dt_socket,server=y,suspend=y" } ;

	return result ;
    } ;

    // Don't timeout while debugging
    protected long getMaximumTimeout()
    {
	return 0 ;
    }

    public int waitFor(long timeout) throws Exception
    {
	// We don't want to set a timeout while debugging
	return waitFor() ;
    }

    public void start() throws Exception
    {
	System.out.println( "Starting process " + processName + " in remote debug mode" ) ;
	super.start() ;
	Object waiter = new Object() ;
	synchronized (waiter) {
	    waiter.wait( 5000 ) ;
	}
    }

    public void stop()
    {
	// we don't want to stop; just tell the user and let them
	// tell us when to stop
	
        printDebugBreak();

        System.out.println("The framework wants to stop the "
                           + processName + " process");
        
	waitForEnter("Press enter to terminate the process");

	kill();
    }
}
