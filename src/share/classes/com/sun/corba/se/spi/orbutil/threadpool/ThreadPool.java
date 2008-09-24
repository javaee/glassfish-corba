/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2003-2007 Sun Microsystems, Inc. All rights reserved.
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

package com.sun.corba.se.spi.orbutil.threadpool;

import java.io.Closeable ;

/** This interface defines a thread pool execution service.  The ORB uses this
 * interface, which preceeds the JDK 5 ExecutorService.  Note that the close
 * method must be called in order to reclaim thread resources.
 */
public interface ThreadPool extends Closeable
{ 
    /** 
    * This method will return any instance of the WorkQueue. If the ThreadPool 
    * instance only services one WorkQueue then that WorkQueue instance will 
    * be returned. If there are more than one WorkQueues serviced by this 
    * ThreadPool, then this method would return a WorkQueue based on the 
    * implementation of the class that implements this interface. For PE 8.0 we 
    * would return a WorkQueue in a roundrobin fashion everytime this method 
    * is called. In the future we could allow pluggability of  Policy objects for this. 
    */ 
    public WorkQueue getAnyWorkQueue(); 

    /** 
    * This method will return an instance of the of the WorkQueue given a queueId. 
    * This will be useful in situations where there are more than one WorkQueues 
    * managed by the ThreadPool and the user of the ThreadPool wants to always use 
    * the same WorkQueue for doing the Work. 
    * If the number of WorkQueues in the ThreadPool are 10, then queueIds will go 
    * from 0-9
    * 
    * @throws NoSuchWorkQueueException thrown when queueId passed is invalid
    */ 
    public WorkQueue getWorkQueue(int queueId) throws NoSuchWorkQueueException;

    /** 
    * This method will return the number of WorkQueues serviced by the threadpool. 
    */ 
    public int numberOfWorkQueues(); 

    /** 
    * This method will return the minimum number of threads maintained by the threadpool. 
    */ 
    public int minimumNumberOfThreads(); 

    /** 
    * This method will return the maximum number of threads in the threadpool at any 
    * point in time, for the life of the threadpool 
    */ 
    public int maximumNumberOfThreads(); 

    /** 
    * This method will return the time in milliseconds when idle threads in the threadpool are 
    * removed. 
    */ 
    public long idleTimeoutForThreads(); 

    /** 
    * This method will return the current number of threads in the threadpool. This method 
    * returns a value which is not synchronized. 
    */ 
    public int currentNumberOfThreads(); 

    /** 
    * This method will return the number of available threads in the threadpool which are 
     * waiting for work. This method returns a value which is not synchronized. 
    */ 
    public int numberOfAvailableThreads(); 

    /** 
    * This method will return the number of busy threads in the threadpool 
    * This method returns a value which is not synchronized. 
    */ 
    public int numberOfBusyThreads(); 

    /** 
    * This method returns the number of Work items processed by the threadpool 
    */ 
    public long currentProcessedCount(); 

     /**
     * This method returns the average elapsed time taken to complete a Work
     * item.
     */
    public long averageWorkCompletionTime();

    /** 
    * This method will return the name of the threadpool. 
    */ 
    public String getName(); 

}

// End of file.
