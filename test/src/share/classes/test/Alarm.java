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

/*
 * Licensed Materials - Property of IBM
 * RMI-IIOP v1.0
 * Copyright IBM Corp. 1998 1999  All Rights Reserved
 *
 * US Government Users Restricted Rights - Use, duplication or
 * disclosure restricted by GSA ADP Schedule Contract with IBM Corp.
 */

package test;

import java.util.*;

/**
 * Alarm provides a one-shot mechanism to schedule asynchronous calls
 * to an AlarmHandler.
 * Typical usage:
 * <pre>
 *   Alarm.scheduleWakeupFromNow(myAlarmHandler,1000); // Wait 1 second.
 * </pre>
 * @author Bryan Atsatt
 */
public class Alarm implements Runnable
{
    private static Vector   fgAlarms;
    private static Thread   fgThread;
    private static boolean      fgStarted;

    private AlarmHandler        fHandler;
    private long                        fWakeupTime;

    /**
     * Constructor.
     * @param handler the alarm handler to invoke at wake up.
     * @param wakeupTime the time to wake up.
     */
    public Alarm (AlarmHandler handler, long wakeupTime)
    {
        fHandler = handler;
        fWakeupTime = wakeupTime;
    }

    /**
     * Check if this alarm's wakeup time is before a given alarm.
     */
    public boolean isBefore (Alarm alarm)
    {
        return fWakeupTime < alarm.fWakeupTime;
    }

    /**
     * Get handler.
     */
    public AlarmHandler getHandler ()
    {
        return fHandler;
    }

    /**
     * Set handler.
     */
    public void setHandler (AlarmHandler handler)
    {
        fHandler = handler;
    }

    /**
     * Get wakeup time.
     */
    public long getWakeupTime ()
    {
        return fWakeupTime;
    }

    /**
     * Set wakeup time. This does <i>not</i> schedule a wake up call.
     */
    public void setWakeupTime (long wakeupTime)
    {
        fWakeupTime = wakeupTime;
    }

    /**
     * Schedule a wake up call relative to now. Alarms are one-shot and
     * therefore must be rescheduled after wakeup if another wakeup is desired.
     * @param handler the alarm handler to invoke at wake up.
     * @param wakeupDeltaMillis the number of milliseconds from now at which to wake up.
     * @return the scheduled alarm.
     */
    public static Alarm scheduleWakeupFromNow (AlarmHandler handler, long wakeupDeltaMillis)
    {
        return scheduleWakeup(new Alarm(handler,System.currentTimeMillis() + wakeupDeltaMillis));
    }

    /**
     * Schedule a wake up call relative to now. Alarms are one-shot and
     * therefore must be rescheduled after wakeup if another wakeup is desired.
     * @param theAlarm the alarm to schedule.
     * @param wakeupDeltaMillis the number of milliseconds from now at which to wake up.
     * @return the scheduled alarm.
     */
    public static Alarm scheduleWakeupFromNow (Alarm theAlarm, long wakeupDeltaMillis)
    {
        theAlarm.setWakeupTime(System.currentTimeMillis() + wakeupDeltaMillis);
        return scheduleWakeup(theAlarm);
    }

    /**
     * Schedule an alarm. Alarms are one-shot and therefore must be rescheduled after
     * wakeup if another wakeup is desired.
     * @param handler the alarm handler to invoke at wake up.
     * @param wakeupDelta the number of milliseconds from now at which to wake up.
     * @return the scheduled alarm.
     */
    public static Alarm scheduleWakeup (Alarm theAlarm)
    {
        synchronized (fgAlarms)
            {
                // Start our thread if needed...

                if (fgStarted == false)
                    {
                        fgStarted = true;
                        fgThread.start();
                    }

                // Insert alarm such that the next alarm is at the lowest index.
                // Do binary search till gap is 2 or less...

                int low = 0;
                int high = fgAlarms.size();

                while (high - low > 2)
                    {
                        int middle = (low + high) / 2;

                        if (theAlarm.isBefore( (Alarm)fgAlarms.elementAt(middle)) )
                            {
                                // Shift to low half of array...

                                high = middle;
                            }
                        else
                            {
                                // Shift to high half of array...

                                low = middle + 1;
                            }
                    }

                // Do linear search on remaining...

                while (low < high)
                    {
                        if (((Alarm)fgAlarms.elementAt(low)).isBefore(theAlarm))
                            {
                                low++;
                            }
                        else
                            {
                                break;
                            }
                    }

                // Ok, do insert...

                fgAlarms.insertElementAt(theAlarm,low);

                // Notify the alarm thread...

                fgAlarms.notify();
            }

        return theAlarm;
    }

    /**
     * Cancel a scheduled an alarm. Cancellation may fail if
     * the alarm has already been fired.
     * @param theAlarm the alarm to cancel.
     * @return true if canceled, false otherwise.
     */
    public static boolean cancelWakeup (Alarm theAlarm)
    {
        boolean result = false;

        synchronized (fgAlarms)
            {
                // Is the alarm in the queue?

                int count = fgAlarms.size();

                for (int index = 0; index < count; index++)
                    {
                        if (fgAlarms.elementAt(index) == theAlarm)
                            {
                                // Found it. Do remove...

                                fgAlarms.removeElementAt(index);

                                // Set result and notify the alarm thread.

                                result = true;
                                fgAlarms.notify();
                            }
                    }
            }

        return result;
    }

    /**
     * Wakeup and call handler.
     */
    private void wakeup (long nextWakeupTime)
    {
        fHandler.wakeup(this, nextWakeupTime);
    }

    /**
     * The method that is executed when a Runnable object is activated.  The run() method
     * is the "soul" of a Thread.  It is in this method that all of the action of a
     * Thread takes place.
     * @see Thread#run
     */
    public void run()
    {
        while (true)
            {
                synchronized (fgAlarms)
                    {
                                // Wait till we have something to schedule...

                        while (fgAlarms.isEmpty())
                            {
                                try
                                    {
                                        fgAlarms.wait();
                                    }
                                catch (Throwable e){}
                            }
                    }

                // Wait till wakeup time of first element. Note that the lock is deliberately
                // released inside the loop in order to provide access to scheduleWakeup()...

                while (true)
                    {
                        synchronized (fgAlarms)
                            {
                                if (fgAlarms.isEmpty()) break; // Can happen if canceled.

                                Alarm theAlarm = (Alarm) fgAlarms.firstElement();

                                long delta = theAlarm.getWakeupTime() - System.currentTimeMillis();

                                if (delta > 0)
                                    {
                                        try
                                            {
                                                fgAlarms.wait(delta);
                                            }
                                        catch (Throwable e){}
                                    }
                                else
                                    {
                                        // Time to wakeup...

                                        try
                                            {
                                                // Remove the current alarm...

                                                fgAlarms.removeElementAt(0);

                                                // Get the next wakeup time, if any...

                                                long nextWakeup = 0;

                                                if (fgAlarms.isEmpty() == false)
                                                    {
                                                        nextWakeup = ((Alarm) fgAlarms.firstElement()).getWakeupTime();
                                                    }

                                                // Wake 'em up...

                                                theAlarm.wakeup(nextWakeup);
                                            }
                                        catch (Throwable e){}

                                        break;   // Break out of loop.
                                    }
                            }
                    }
            }
    }

    // Only for fgThread...

    private Alarm()
    {
    }

    // Init our static data...

    static
    {
        fgAlarms = new Vector();
        fgThread = new Thread(new Alarm(),"AlarmThread");
        fgThread.setDaemon(true);
        fgStarted = false;
    }
}
