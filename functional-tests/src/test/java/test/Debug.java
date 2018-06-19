/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright (c) 1997-2010 Oracle and/or its affiliates. All rights reserved.
 * Copyright (c) 1998 IBM Corp. All rights reserved.
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
 * file and include the License file at packager/legal/LICENSE.txt.
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

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.ByteArrayOutputStream;
import java.io.PrintWriter;
import java.util.Properties;
import java.util.Enumeration;

public class Debug {
    
    private String name = null;
    
    public Debug (String name) {
        if (name == null) {
            name = "";
        } else {
            this.name = name + ": ";
        }
        setTop();
    }
    
    public final void log (String msg) {
        doLog(name,msg);
    }
    
    public final void log (byte[] data) {
        
        StringBuffer buf = new StringBuffer();
        for (int i = 0; i < data.length; i++) {
            if (i > 0) {
                buf.append(' ');
            }
            buf.append((char)ASCII_HEX[(data[i] & 0xF0) >>> 4]);
            buf.append((char)ASCII_HEX[(data[i] & 0x0F)]);
        }
        
        doLog(name,buf.toString());
    }
    
    public final void logStack () {
        doLogStack(name);    
    }
    
    public final void logSystemProperties () {
        doLogSystemProperties(name);    
    }
    
    public final void logException (Throwable e) {
        doLogException(name,e);
    }
    
    
    private static synchronized void setTop() {
        lastName = "atsatt :-)";
    }
    
    private static synchronized void doLog (String name, String msg) {
        if (log == null) {
            initLog(name);
        } else {
            if (name != null && !name.equals(lastName)) {
                log.println("  ---");
            }
        }
        lastName = name;
        log.println(name + msg);    
    }

    private static String getStack(int trimSize) {
        ByteArrayOutputStream os = new ByteArrayOutputStream();
        PrintWriter pw = new PrintWriter(os);
        new Exception().printStackTrace(pw);
        pw.flush();
        String result = os.toString();
        if (trimSize > 0) {
            return result.substring(trimSize);
        }
        return result;
    }

    private static void doLogStack (String name) {
        doLog(name, getStack(0));
    }

    private static void doLogSystemProperties (String name) {
        StringBuffer buf = new StringBuffer();
        Properties props = System.getProperties();
        buf.append("System Properties:");
        buf.append(eol);
        for (Enumeration e = props.propertyNames() ; e.hasMoreElements() ;) {
            String key = (String) e.nextElement();
            String value = (String) props.get(key);
            buf.append("   ");
            buf.append(key);
            buf.append(" = ");
            buf.append(value);
            buf.append(eol);
        }
        doLog(name, buf.toString());
    }
    
    private static void doLogException (String name, Throwable e) {
        doLog(name,"Caught " + e + eol + getStack(0));
    }
    
    public static void main (String[] args) {
        Debug d = new Debug("main");
        d.log("Testing...");
        d.logStack();
        d.logSystemProperties();
    }
    
    private static PrintWriter log = null;
    private static final String LOG_NAME = "DebugLog";
    private static final String LOG_EXT = ".txt";
    private static File rootDir = null;
    private static String lastName = null;
    private static String eol = (String)System.getProperty("line.separator");
    public static final byte ASCII_HEX[] =      {
        (byte)'0',
        (byte)'1',
        (byte)'2',
        (byte)'3',
        (byte)'4',
        (byte)'5',
        (byte)'6',
        (byte)'7',
        (byte)'8',
        (byte)'9',
        (byte)'A',
        (byte)'B',
        (byte)'C',
        (byte)'D',
        (byte)'E',
        (byte)'F',
    };
        
    private static synchronized void initLog(String name) {
        if (log == null) {
            rootDir = new File(System.getProperty("user.dir"));
            log = createLog(0);
            log.println("Log created by " +name+" at " + new java.util.Date().toString());
            log.println();
        }
    }
    
    private static PrintWriter createLog (int number) {
        String fileName = LOG_NAME + Integer.toString(number) + LOG_EXT;
        File file = new File(rootDir,fileName);
        
        // If file exists, assume another vm process owns it, and
        // bump the number...
        
        if (file.exists()) {
            return createLog(++number);
        }
        try {
            FileOutputStream os = new FileOutputStream(file);
            return new PrintWriter(os,true);
        } catch (IOException e) {
            System.err.println("Failed to create vmlog. Caught: "+e);
            return null;
        }
    }
}
