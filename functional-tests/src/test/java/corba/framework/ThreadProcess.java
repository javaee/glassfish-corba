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

import java.io.PrintStream;
import java.util.Properties;
import java.util.Hashtable;

/**
 * Class representing a process which will run a separate thread but
 * the same process as the test framework.  A subclass can extend this
 * and be used with the ThreadExec strategy.
 * <P>
 * Subclasses should construct their run() method such that they
 * exit gracefully when stopped() returns true.
 * <P>
 * A subclass must call setExitValue and then setFinished at the
 * end of execution.
 * <P>
 * Could probably transfer most of this to ThreadExec.
 */
public abstract class ThreadProcess implements InternalProcess, Runnable
{
    protected Properties environment;
    protected String args[];
    protected PrintStream out;
    protected PrintStream err;
    protected Hashtable extra;

    private boolean finished = false;
    protected int exitValue = ExternalExec.INVALID_STATE;
    private boolean stopped = false;

    // Use a separte lock object in case the subclass wants to
    // use synchronized
    private Object lockObj = new Object();

    /**
     * Saves the parameters, and starts in its own thread
     * (so override the Runnable run() method).
     */
    public void run(Properties environment,
                    String args[],
                    PrintStream out,
                    PrintStream err,
                    Hashtable extra)
    {
        this.environment = environment;
        this.args = args;
        this.out = out;
        this.err = out;
        this.extra = extra;

        (new Thread(this)).start();
    }

    public void stop()
    {
        /*
          If not finished:
          Set the exit value to STOPPED, and set the stopped flag to true
          Wait until the executing thread calls setFinished (it knows to
          do so because now stopped() returns true).
        */

        synchronized(lockObj) {

            if (!finished()) {
                exitValue = Controller.STOPPED;
                
                // The thread should eventually call setFinished and
                // exit which will wake up any waiters.  (It knows
                // it must leave because now stopped() returns true.)
                stopped = true;

                try {
                    lockObj.wait();
                } catch (InterruptedException ex) {
                    // Just return from wait -- this really shouldn't
                    // happen
                }
            }
        }
    }

    /**
     * Used by subclasses to determine if they have been stopped,
     * and should exit run().
     */
    protected boolean stopped()
    {
        synchronized(lockObj) {
            return stopped;
        }
    }

    public boolean finished()
    {
        synchronized(lockObj) {
            return finished;
        }
    }

    /**
     * Used by subclasses to declare that they are done, and wake up
     * any threads that are in waitFor.
     */
    protected void setFinished()
    {
        synchronized(lockObj) {
            finished = true;
            lockObj.notifyAll();
        }
    }

    public int waitFor() throws Exception
    {
        return waitFor(0);
    }

    public int waitFor(long timeout) throws Exception
    {
        synchronized(lockObj) {
            if (!finished())
                lockObj.wait(timeout);
            return exitValue;
        }
    }

    public int exitValue() throws IllegalThreadStateException
    {
        synchronized(lockObj) {
            if (exitValue == ExternalExec.INVALID_STATE)
                throw new IllegalThreadStateException("exit value wasn't set");

            return exitValue;
        }
    }

    /**
     * Used by a subclass to set its exit value.  This should be
     * called before setFinished().  If another thread called
     * stop(),  this won't change the exit value.
     */
    protected void setExitValue(int value)
    {
        synchronized(lockObj) {
            if (!stopped())
                exitValue = value;
        }
    }
}



