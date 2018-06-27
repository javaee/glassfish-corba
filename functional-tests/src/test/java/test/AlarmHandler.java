/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright (c) 1997-2018 Oracle and/or its affiliates. All rights reserved.
 * Copyright (c) 1998-1999 IBM Corp. All rights reserved.
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

package test;

import java.util.*;

/**
 * AlarmHandler provides an interface which is called asynchronously by Alarm
 * at the scheduled wakeup time.
 * @see Alarm
 * @author Bryan Atsatt
 */
public interface AlarmHandler
{
    /**
     * This method is called asynchronously at the scheduled wakeup time. May be later
     * than the current time, depending on system activity and/or on the amount of time
     * used by previous alarm handlers.
     * <p>
     * The calling thread is owned by the Alarm class and is responsible for <i>all</i>
     * alarms;  therefore, it is recommended that implementations perform
     * only small amounts of processing in order to ensure timely delivery of
     * alarms.  If, in a given process, all usage of alarms is known, implementations
     * may safely use more time.  The nextAlarmWakeupTime argument passed to this
     * method can be used to determine how much time can be used;  however, it is
     * expected that few clients can effectively use this information.
     * @param theAlarm The alarm.
     * @param nextAlarmWakeupTime The next (currently) scheduled wakeup time for any
     * alarm.  May be zero, in which case there are no currently scheduled alarms. (This
     * is not the next scheduled wakeup for <i>this</i> alarm -- alarms are one-shot and
     * must be rescheduled once wakeup is called.)
     */
    public abstract void wakeup (Alarm theAlarm, long nextAlarmWakeupTime);
}
