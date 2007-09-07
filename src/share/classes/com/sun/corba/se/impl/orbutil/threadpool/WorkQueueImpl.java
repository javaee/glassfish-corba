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

package com.sun.corba.se.impl.orbutil.threadpool;

import java.util.LinkedList;
import java.util.Queue;

import com.sun.corba.se.spi.monitoring.LongMonitoredAttributeBase;
import com.sun.corba.se.spi.monitoring.MonitoringConstants;
import com.sun.corba.se.spi.monitoring.MonitoringFactories;
import com.sun.corba.se.spi.monitoring.MonitoredObject;
import com.sun.corba.se.spi.orb.ORB;
import com.sun.corba.se.spi.orbutil.threadpool.ThreadPool;
import com.sun.corba.se.spi.orbutil.threadpool.Work;
import com.sun.corba.se.spi.orbutil.threadpool.WorkQueue;

import com.sun.corba.se.impl.logging.ORBUtilSystemException;
import com.sun.corba.se.impl.orbutil.ORBConstants;


public class WorkQueueImpl implements WorkQueue
{
    final private static ORBUtilSystemException wrapper = 
	       ORB.getStaticLogWrapperTable().get_RPC_TRANSPORT_ORBUtil();
    final private Queue<Work> queue;
    private ThreadPool workerThreadPool;

    private long workItemsAdded = 0;

    // Initialized to 1 to avoid divide by zero in averageTimeInQueue()
    private long workItemsDequeued = 1;

    private long totalTimeInQueue = 0;

    // Name of the work queue
    final private String name;

    // MonitoredObject for work queue
    final private MonitoredObject workqueueMonitoredObject;

    public WorkQueueImpl() {
	this.name = ORBConstants.WORKQUEUE_DEFAULT_NAME;
        this.queue = new LinkedList<Work>();
        this.workqueueMonitoredObject = MonitoringFactories.
			    getMonitoredObjectFactory().
			    createMonitoredObject(name,
			    MonitoringConstants.WORKQUEUE_MONITORING_DESCRIPTION);
	initializeMonitoring();
    }

    public WorkQueueImpl(ThreadPool workerThreadPool) {
        this(workerThreadPool, ORBConstants.WORKQUEUE_DEFAULT_NAME);
    }

    public WorkQueueImpl(ThreadPool workerThreadPool, String name) {
        this.workerThreadPool = workerThreadPool;
	this.name = name;
        this.queue = new LinkedList<Work>();
        this.workqueueMonitoredObject = MonitoringFactories.
			    getMonitoredObjectFactory().
			    createMonitoredObject(name,
			    MonitoringConstants.WORKQUEUE_MONITORING_DESCRIPTION);
	initializeMonitoring();
    }

    // Setup monitoring for this workqueue
    private void initializeMonitoring() {
	LongMonitoredAttributeBase b1 = new 
	    LongMonitoredAttributeBase(MonitoringConstants.WORKQUEUE_TOTAL_WORK_ITEMS_ADDED, 
		    MonitoringConstants.WORKQUEUE_TOTAL_WORK_ITEMS_ADDED_DESCRIPTION) {
		public Object getValue() {
		    return Long.valueOf(WorkQueueImpl.this.totalWorkItemsAdded());
		}
	    };
	workqueueMonitoredObject.addAttribute(b1);
	LongMonitoredAttributeBase b2 = new 
	    LongMonitoredAttributeBase(MonitoringConstants.WORKQUEUE_WORK_ITEMS_IN_QUEUE, 
		    MonitoringConstants.WORKQUEUE_WORK_ITEMS_IN_QUEUE_DESCRIPTION) {
		public Object getValue() {
		    return Long.valueOf(WorkQueueImpl.this.workItemsInQueue());
		}
	    };
	workqueueMonitoredObject.addAttribute(b2);
	LongMonitoredAttributeBase b3 = new 
	    LongMonitoredAttributeBase(MonitoringConstants.WORKQUEUE_AVERAGE_TIME_IN_QUEUE, 
		    MonitoringConstants.WORKQUEUE_AVERAGE_TIME_IN_QUEUE_DESCRIPTION) {
		public Object getValue() {
		    return Long.valueOf(WorkQueueImpl.this.averageTimeInQueue());
		}
	    };
	workqueueMonitoredObject.addAttribute(b3);
    }


    // Package private method to get the monitored object for this
    // class
    MonitoredObject getMonitoredObject() {
	return workqueueMonitoredObject;
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

    synchronized Work requestWork(long waitTime) throws WorkerThreadNotNeededException {
        if (this.isQueueEmpty()) {
            try {
                ((ThreadPoolImpl)workerThreadPool).incrementNumberOfAvailableThreads();
                long timeOutTime = System.currentTimeMillis() + waitTime;

                wait(waitTime);
                
                if (System.currentTimeMillis() >= timeOutTime) {
                    int availableThreads = 
                            workerThreadPool.numberOfAvailableThreads();
                    int minThreads = workerThreadPool.minimumNumberOfThreads();
                    if (availableThreads > minThreads) {
                        // This thread has timed out and can die cause 
                        // we have enough available idle threads.
                        // NOTE: It is expected the WorkerThread calling this
                        //       method will gracefully exit as a result of
                        //       catching the WorkerThreadNotNeededException.
                        ((ThreadPoolImpl)workerThreadPool).
                                              decrementCurrentNumberOfThreads();
                        throw new WorkerThreadNotNeededException();
                    } else {
                        // Keep this thread available.
                        return null;
                    }
                } // else
                  // If wait(waitTime) returns before waitTime expired,
                  // then there "should" be Work on the WorkQueue ready to be
                  // returned because some other WorkerThread has notified
                  // the WorkerThread executing this method. However, there is
                  // the possibility of a "spurious wakeup", (see JavaDoc
                  // Object.wait(long timeout) for a description of "spurious
                  // wakeup".
                  // If a "spurious wakeup" there are two possible outcomes
                  // of proceeding. One is that there is at least one Work
                  // item on this WorkQueue or there is no Work on this
                  // WorkQueue. If there is Work on this WorkQueue, then
                  // the WorkerThread that has called this method will
                  // execute the Work item returned.  If this WorkQueue is
                  // empty, a null Work item is returned.  When a null Work
                  // item is returned, the WorkerThread simply returns back
                  // to this method and waits for additional Work. Hence,
                  // a "spurious wakeup" is handled appropriately.
            } catch (InterruptedException e) {
                // clear Thread's interrupted status
                Thread.interrupted();
                wrapper.workQueueThreadInterrupted(getThreadPool().getName(),
                                                   getName(), e.toString());
            } finally {
                ((ThreadPoolImpl)workerThreadPool).decrementNumberOfAvailableThreads();
            }
        }
        
        return queue.poll();
    }

    private synchronized boolean isQueueEmpty() {
        int waitingThreads = workerThreadPool.numberOfAvailableThreads();
        return (queue.size() - waitingThreads <= 0);
    }

    public void setThreadPool(ThreadPool workerThreadPool) {
	this.workerThreadPool = workerThreadPool;
    }

    public ThreadPool getThreadPool() {
	return workerThreadPool;
    }

    /**
     * Returns the total number of Work items added to the Queue.
     */
    public synchronized long totalWorkItemsAdded() {
        return workItemsAdded;
    }

    /**
     * Returns the total number of Work items in the Queue to be processed.
     */
    public synchronized int workItemsInQueue() {
        return queue.size();
    }

    /**
     * Returns the average amount Work items have spent in the Queue waiting
     * to be processed.
     */
    public synchronized long averageTimeInQueue() {
        return (totalTimeInQueue/workItemsDequeued);
    }

    public String getName() {
        return name;
    }
}

// End of file.
