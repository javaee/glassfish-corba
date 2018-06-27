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

package corba.framework;

import java.io.*;

/**
 * Runs the class in the current process.  This assumes the class implements
 * the InternalProcess interface.  Also beware the natural consequences of
 * running something in the current thread and process -- it won't return
 * unless it does so on its own.  This assumes single threaded access.
 * For multi-threaded options, see ThreadExec.
 *
 */
public class InternalExec extends ControllerAdapter
{
    private long startTime ;
    private long duration ;

    public void start( ) throws Exception
    {
        startTime = System.currentTimeMillis() ;

        try {
            Loader loader = new Loader();
            loader.addPath(Options.getOutputDirectory());

            Object obj = (loader.loadClass(className)).newInstance();

            activateObject(obj);
        } finally {
            duration = System.currentTimeMillis() - startTime ;
        }
    }
   
    public long duration() {
        return duration ;
    }

    public void stop()
    {
        // Can't be stopped
    }

    public void kill()
    {
        // Can't be killed
    }

    public int waitFor() throws Exception
    {
        return exitValue;
    }

    public int waitFor(long timeout) throws Exception
    {
        return exitValue;
    }

    public int exitValue() throws IllegalThreadStateException
    {
        // Just in case a subclass wants to change finished
        if (!finished())
            throw new IllegalThreadStateException("not finished");

        return exitValue;
    }

    public boolean finished() throws IllegalThreadStateException
    {
        return true;
    }

    /**
     * Activate the given Object by casting it to the
     * InternalProcess interface, and calling its
     * run method.
     */
    protected void activateObject(Object obj)
    {
        InternalProcess process = (InternalProcess)obj;

        PrintStream output = new PrintStream(out, true);
        PrintStream errors = new PrintStream(err, true);

        try {
            process.run(environment, programArgs, output, errors, extra);
        } catch (Exception ex) {
            ex.printStackTrace(errors);
            exitValue = 1;
        }
    }
                 
    /**
     * Exit value of this process.
     */
    protected int exitValue = Controller.SUCCESS;
}
