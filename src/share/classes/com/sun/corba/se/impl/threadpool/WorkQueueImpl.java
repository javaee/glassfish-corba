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

package com.sun.corba.ee.impl.threadpool;

import java.util.LinkedList;
import java.util.Queue;

import com.sun.corba.ee.spi.threadpool.ThreadPool;
import com.sun.corba.ee.spi.threadpool.Work;
import com.sun.corba.ee.spi.threadpool.WorkQueue;

import org.glassfish.gmbal.Description ;
import org.glassfish.gmbal.ManagedAttribute ;
import org.glassfish.gmbal.NameValue ;

public class WorkQueueImpl implements WorkQueue
{
    public static final String WORKQUEUE_DEFAULT_NAME = "default-workqueue";

    final private Queue<Work> queue;
    private ThreadPool workerThreadPool;

    private long workItemsAdded = 0;
    private long workItemsDequeued = 0;
    private long totalTimeInQueue = 0;

    // Name of the work queue
    final private String name;

    public WorkQueueImpl() {
        this.name = WORKQUEUE_DEFAULT_NAME;
        this.queue = new LinkedList<Work>();
    }

    public WorkQueueImpl(ThreadPool workerThreadPool) {
        this(workerThreadPool, WORKQUEUE_DEFAULT_NAME);
    }

    public WorkQueueImpl(ThreadPool workerThreadPool, String name) {
        this.workerThreadPool = workerThreadPool;
        this.name = name;
        this.queue = new LinkedList<Work>();
    }

    private synchronized int getWorkQueueSize() {
        return queue.size();
    }

    public synchronized void addWork(Work work) {
        workItemsAdded++;
        work.setEnqueueTime(System.currentTimeMillis());

        queue.offer(work);
        notify();

        int waitingThreads = workerThreadPool.numberOfAvailableThreads();
        int threadCount = workerThreadPool.currentNumberOfThreads();
        int maxThreads = workerThreadPool.maximumNumberOfThreads();
        if (threadCount < maxThreads && waitingThreads < getWorkQueueSize()) {
        // NOTE: It is possible that the Work that was just added may unblock
        //       Worker Threads waiting on the Work just added and all Worker
        //       Threads are busy, (blocked & waiting for a response). This
        //       situation can lead to a deadlock.  The solution to such a
        //       a problem should it occur is to increase the maximum number
        //       of threads.
        // REVISIT - A possible solution to the above issue is check the
        //           enqueued Work timestamp periodically by another thread
        //           and create a Worker Thread if a piece of Work sits on 
        //           Work Queue for longer than some threshold.
            // add a WorkerThread
            ((ThreadPoolImpl)workerThreadPool).createWorkerThread();
        }
    }

    // XXX Re-write this to use a simple poll( waitTime, TimeUnit.MILLISECONDS )
    // and avoid the race conditions.  The change is a little too large to make
    // right now (a few days before GFv3 HCF).  See issue 7722.
    synchronized Work requestWork(long waitTime) throws WorkerThreadNotNeededException, 
        InterruptedException {

        if (this.isQueueEmpty()) {
            try {
                ((ThreadPoolImpl)workerThreadPool).incrementNumberOfAvailableThreads();
                long timeOutTime = System.currentTimeMillis() + waitTime;

                wait(waitTime);
                
                if (this.isQueueEmpty() && System.currentTimeMillis() >= timeOutTime) {
                    int availableThreads = 
                            workerThreadPool.numberOfAvailableThreads();
                    int minThreads = workerThreadPool.minimumNumberOfThreads();
                    if (availableThreads > minThreads) {
                        // This thread has timed out and can die because 
                        // we have enough available idle threads.
                        // NOTE: It is expected the WorkerThread calling this
                        //       method will gracefully exit as a result of
                        //       catching the WorkerThreadNotNeededException.
                        ((ThreadPoolImpl)workerThreadPool).
                                              decrementCurrentNumberOfThreads();
                        throw new WorkerThreadNotNeededException();
                    } 
                }
                
                // We still want to keep this thread, but let it do a
                // poll in case we waited at least as long
                // as timeOutTime, and then we were awakened by addWork.
                // In that case, we still want to do the queue.poll().
            } finally {
                ((ThreadPoolImpl)workerThreadPool).decrementNumberOfAvailableThreads();
            }
        }
        
        Work work = queue.poll();
        if (work != null) {
            workItemsDequeued++ ;
            totalTimeInQueue += System.currentTimeMillis() - work.getEnqueueTime() ;
        }

        return work ;
    }

    private synchronized boolean isQueueEmpty() {
        int waitingThreads = workerThreadPool.numberOfAvailableThreads();
        return (queue.size() - waitingThreads <= 0);
    }

    public synchronized void setThreadPool(ThreadPool workerThreadPool) {
        this.workerThreadPool = workerThreadPool;
    }

    public synchronized ThreadPool getThreadPool() {
        return workerThreadPool;
    }

    /**
     * Returns the total number of Work items added to the Queue.
     */
    @ManagedAttribute
    @Description( "Total number of items added to the queue" )
    public synchronized long totalWorkItemsAdded() {
        return workItemsAdded;
    }

    /**
     * Returns the total number of Work items in the Queue to be processed.
     */
    @ManagedAttribute
    @Description( "Total number of items in the queue to be processed" )
    public synchronized int workItemsInQueue() {
        return queue.size();
    }

    /**
     * Returns the average amount Work items have spent in the Queue waiting
     * to be processed.
     */
    @ManagedAttribute
    @Description( "Average time work items spend waiting in the queue in milliseconds" )
    public synchronized long averageTimeInQueue() {
        if (workItemsDequeued == 0) {
            return 0 ;
        } else { 
            return (totalTimeInQueue/workItemsDequeued);
        }
    }

    @NameValue
    public String getName() {
        return name;
    }
}

// End of file.
