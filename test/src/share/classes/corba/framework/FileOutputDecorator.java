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
package corba.framework;

import java.io.*;
import java.util.*;

/**
 * Decorator around a Controller, allowing the user to simply specify
 * file names to initialize rather than creating the streams.
 * Delegates everything else.
 */
public class FileOutputDecorator implements Controller
{
    private Controller delegate;
    private boolean closed = false;
    private int emmaPort ;

    public FileOutputDecorator(Controller delegate)
    {
        this.delegate = delegate;
    }

    public long duration() {
        return delegate.duration() ;
    }

    /**
     * Setup everything necessary to execute the given class.
     *
     *@param className    Full class name to execute
     *@param processName  Name identifying this process for
     *                    output file name purposes
     *@param environment  Environment variables to provide
     *@param VMArgs       Arguments to the VM(can be ignored)
     *@param programArgs  Arguments to the class when run
     *@param outFileName  Name of file to pipe stdout to
     *@param errFileName  Name of file to pipe stderr to
     *@param extra        Strategy specific initialization extras
     *
     *@exception   Exception  Any fatal error that occured
     */
    public void initialize(String className,
                           String processName,
                           Properties environment,
                           String VMArgs[],
                           String programArgs[],
                           String outFileName,
                           String errFileName,
                           Hashtable extra,
			   int emmaPort ) throws Exception
    {
        OutputStream outstr = CORBAUtil.openFile(outFileName);
        OutputStream errstr = CORBAUtil.openFile(errFileName);
	this.emmaPort = emmaPort ;

        delegate.initialize(className,
                            processName,
                            environment,
                            VMArgs,
                            programArgs,
                            outstr,
                            errstr,
                            extra);
    }

    public void initialize(String className,
                           String processName,
                           Properties environment,
                           String VMArgs[],
                           String programArgs[],
                           OutputStream out,
                           OutputStream err,
                           Hashtable extra) throws Exception
    {
	// There is no reason to call this (it defeats the
	// purpose of this class), but must be present.
        delegate.initialize(className,
                            processName,
                            environment,
                            VMArgs,
                            programArgs,
                            out,
                            err,
                            extra);
    }

    public void start() throws Exception
    {
        delegate.start();
    }
    
    public void stop()
    {
        try {
	    EmmaControl.writeCoverageData( emmaPort, Options.getEmmaFile() ) ;

	    try {
		Thread.sleep( 500 ) ; // give emma time to write out the file
				      // (This may not be required)
	    } catch (InterruptedException exc) {
		// ignore this
	    }

            delegate.stop();
        } finally {
            try {
                closeStreams();
            } catch (IOException ex) {
                // Ignore
            }
        }
    }
    
    public void kill()
    {
        try {

            delegate.kill();

        } finally {
            try {
                closeStreams();
            } catch (IOException ex) {
                // Ignore
            }
        }
    }
    
    public int waitFor() throws Exception
    {
        try {

            return delegate.waitFor();

        } finally {
            closeStreams();
        }
    }

    public int waitFor(long timeout) throws Exception
    {
        try {
            
            return delegate.waitFor(timeout);

        } finally {
            closeStreams();
        }
    }

    public int exitValue() throws IllegalThreadStateException
    {
        return delegate.exitValue();
    }
    
    public boolean finished() throws IllegalThreadStateException
    {
        return delegate.finished();
    }

    public OutputStream getOutputStream()
    {
        return delegate.getOutputStream();
    }

    public OutputStream getErrorStream()
    {
        return delegate.getErrorStream();
    }

    public Controller getDelegate()
    {
        return delegate;
    }

    /**
     * Flushes and closes the streams.
     */
    public void closeStreams() throws IOException
    {
        if (!closed) {

            closed = true;

            // In a recent change, the ProcessMonitor that handles
            // copying of output from a java.lang.Process now
            // closes the streams on its on when the process ends.
            // Closing them here could lead to problems.
            if (delegate instanceof corba.framework.ExternalExec)
                return;

            OutputStream out = delegate.getOutputStream();
            OutputStream err = delegate.getErrorStream();

            try {
                out.flush();
                err.flush();
            } finally {
                if (out != System.out)
                    out.close();
                if (err != System.err)
                    err.close();
            }
        }
    }

    public String getProcessName()
    {
        return delegate.getProcessName();
    }

    public String getClassName()
    {
        return delegate.getClassName();
    }
}
