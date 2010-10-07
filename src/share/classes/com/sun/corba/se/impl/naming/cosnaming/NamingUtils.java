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

package com.sun.corba.se.impl.naming.cosnaming;

import java.io.*;
import org.omg.CosNaming.NameComponent;


public class NamingUtils {
    // Do not instantiate this class
    private NamingUtils() {};

    /**
     * Debug flag which must be true for debug streams to be created and
     * dprint output to be generated.
     */ 
    public static boolean debug = false;

    /**
     * Prints the message to the debug stream if debugging is enabled.
     * @param msg the debug message to print.
     */ 
    public static void dprint(String msg) {
	if (debug && debugStream != null)
	    debugStream.println(msg);
    }

    /**
     * Prints the message to the error stream (System.err is default).
     * @param msg the error message to print.
     */ 
    public static void errprint(String msg) {
	if (errStream != null)
	    errStream.println(msg);
	else
	    System.err.println(msg);
    }

    /**
     * Prints the stacktrace of the supplied exception to the error stream.
     * @param e any Java exception.
     */
    public static void printException(java.lang.Exception e) {
	if (errStream != null)
	    e.printStackTrace(errStream);
	else
	    e.printStackTrace();
    }

    /**
     * Create a debug print stream to the supplied log file.
     * @param logFile the file to which debug output will go.
     * @exception IOException thrown if the file cannot be opened for output.
     */ 
    public static void makeDebugStream(File logFile)
	throws java.io.IOException {
	// Create an outputstream for debugging
	java.io.OutputStream logOStream =
	    new java.io.FileOutputStream(logFile);
	java.io.DataOutputStream logDStream =
	    new java.io.DataOutputStream(logOStream);
	debugStream = new java.io.PrintStream(logDStream);
      
	// Emit first message
	debugStream.println("Debug Stream Enabled.");
    }
  
    /**
     * Create a error print stream to the supplied file.
     * @param logFile the file to which error messages will go.
     * @exception IOException thrown if the file cannot be opened for output.
     */ 
    public static void makeErrStream(File errFile)
	throws java.io.IOException {
	if (debug) {
	    // Create an outputstream for errors
	    java.io.OutputStream errOStream =
		new java.io.FileOutputStream(errFile);
	    java.io.DataOutputStream errDStream =
		new java.io.DataOutputStream(errOStream);
	    errStream = new java.io.PrintStream(errDStream);
	    dprint("Error stream setup completed.");
	}
    }  


    /**
     * A utility method that takes Array of NameComponent and converts
     * into a directory structured name in the format of /id1.kind1/id2.kind2..
     * This is used mainly for Logging.
     */
    static String getDirectoryStructuredName( NameComponent[] name ) {
        StringBuffer directoryStructuredName = new StringBuffer("/"); 
        for( int i = 0; i < name.length; i++ ) {
            directoryStructuredName.append( name[i].id + "." + name[i].kind );
        }
        return directoryStructuredName.toString( );
    }

    /**
     * The debug printstream.
     */
    public static java.io.PrintStream debugStream;

    /**
     * The error printstream.
     */
    public static java.io.PrintStream errStream;  
}
