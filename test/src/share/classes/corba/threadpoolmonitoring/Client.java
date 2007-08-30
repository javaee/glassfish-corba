/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2000-2007 Sun Microsystems, Inc. All rights reserved.
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
package corba.threadpoolmonitoring;

import java.io.PrintStream ;

import java.util.Collection ;
import java.util.Iterator ;
import java.util.Properties ;

import com.sun.corba.se.spi.monitoring.*;
import com.sun.corba.se.spi.orb.ORB;
import com.sun.corba.se.spi.orbutil.threadpool.Work;

import com.sun.corba.se.impl.orbutil.ORBConstants;
import com.sun.corba.se.impl.orbutil.ORBUtility;
import com.sun.corba.se.impl.orbutil.threadpool.ThreadPoolImpl;

public class Client 
{
    private static final int MIN_THREADS = 0;
    private static final int MAX_THREADS = 3;
    private static final long IDLE_TIMEOUT = 30000;  //In MillisSeconds
    private static final String NAME_OF_THE_THREADPOOL = "MyThreadPool";

    private static final int expectedCurrentNumberOfThreads = 2;
    private static final int expectedBusyThreads = 1;
    private static final int expectedAvailableThreads = 1;
    private static final int expectedWorkItemsAdded = 2;
    private static final int expectedWorkItemsInQueue = 0;

    private Object waitObject1 = new Object();
    private Object waitObject2 = new Object();
    private Object barrier = new Object();

    private PrintStream out ;
    private PrintStream err ;
    private ORB orb ;


    private ThreadPoolImpl threadpool;



    public static void main(String args[])
    {
	System.out.println( "Starting ThreadPool Monitoring Manager test" ) ;
        try{
	    Properties props = new Properties( System.getProperties() ) ;
	    props.put( "org.omg.CORBA.ORBClass", 
		"com.sun.corba.se.impl.orb.ORBImpl" ) ;
	    new Client( props, args, System.out, System.err ) ;
        } catch (Exception e) {
            System.out.println("ERROR : " + e) ;
            e.printStackTrace(System.out);
            System.exit (1);
        }
    }

    public Client( Properties props, String args[], PrintStream out,
	PrintStream err )
    {
	this.orb = (ORB)ORB.init( args, props ) ;
	this.out = System.out ;
	this.err = System.err ;
	threadpool = new ThreadPoolImpl(MIN_THREADS, MAX_THREADS, IDLE_TIMEOUT,
		NAME_OF_THE_THREADPOOL);   

	runTests() ;
    }

// *************************************************
// ***************   Utilities   *******************
// *************************************************

    private void error( String msg )
    {
	RuntimeException exc = new RuntimeException( msg ) ;
	throw exc ;
    }
    
    private void info( String msg )
    {
	out.println( msg ) ;
    }


    private MonitoredObject getThreadPoolInstanceMonitoredObject() {
	MonitoredObject root = orb.getMonitoringManager().
	    getRootMonitoredObject(); 
	MonitoredObject unitTestThreadPoolMonitoredObject =
	    root.getChild(MonitoringConstants.THREADPOOL_MONITORING_ROOT).
	        getChild(NAME_OF_THE_THREADPOOL);
	if( unitTestThreadPoolMonitoredObject == null ) {
	    this.error( "Unsuccessful attempt to get Monitored Object For ThreadPool Instance" );
	}
	return unitTestThreadPoolMonitoredObject;
    }	

    private void createWork() {
	Work w1 = new MyWork(waitObject1, barrier);
	Work w2 = new MyWork(waitObject2, null);
	int count = 0;
	synchronized (barrier) {
	    // Add work to the workqueue
	    threadpool.getAnyWorkQueue().addWork(w1);

	    // Sleep for sometime so that work items added create
	    // exactly 2 threads
	    try {
		Thread.sleep(5000);
	    } catch (InterruptedException ie) {
	    }

	    // Add work to the workqueue
	    threadpool.getAnyWorkQueue().addWork(w2);

	    // Wait till threadpool threads inform that they are ready to 
	    // wait
	    try {
		barrier.wait();
	    } catch (Exception e) {
	    }
	}

    }
    
    private void testThreadPoolMonitoredAttributes(MonitoredObject tpmo) {

	// Notify so that first work item is completed
	synchronized (waitObject1) {
	    try {
		waitObject1.notify();
	    } catch (Exception e) {
	    }
	}

	// Sleep for sometime
	try {
	    Thread.sleep(5000);
	} catch (InterruptedException ie) {
	}

	Long l1 = (Long)tpmo.getAttribute(MonitoringConstants.
		THREADPOOL_CURRENT_NUMBER_OF_THREADS).getValue();
	if (l1.longValue() != expectedCurrentNumberOfThreads) {
	    this.error( "Monitored Attribute CURRENT_NUMBER_OF_THREADS has incorrect value " + l1 );
	}
	l1 = (Long)tpmo.getAttribute(MonitoringConstants.
		THREADPOOL_NUMBER_OF_AVAILABLE_THREADS).getValue();
	if (l1.longValue() != expectedAvailableThreads) {
	    this.error( "Monitored Attribute NUMBER_OF_AVAILABLE_THREADS has incorrect value " + l1);
	}
	l1 = (Long)tpmo.getAttribute(MonitoringConstants.
		THREADPOOL_NUMBER_OF_BUSY_THREADS).getValue();
	if (l1.longValue() != expectedBusyThreads) {
	    this.error( "Monitored Attribute NUMBER_OF_BUSY_THREADS has incorrect value " + l1);
	}

	MonitoredObject wqmo = tpmo.getChild(ORBConstants.WORKQUEUE_DEFAULT_NAME);
	if (wqmo == null) {
	    this.error( "Could not get the work queue monitored object" );
	}

	Long l2 = (Long)wqmo.getAttribute(MonitoringConstants.
		WORKQUEUE_TOTAL_WORK_ITEMS_ADDED).getValue();
	if (l2.longValue() != expectedWorkItemsAdded) {
	    this.error( "Monitored Attribute TOTAL_WORK_ITEMS_ADDED has incorrect value " + l2);
	}

	l2 = (Long)wqmo.getAttribute(MonitoringConstants.
		WORKQUEUE_WORK_ITEMS_IN_QUEUE).getValue();
	if (l2.longValue() != expectedWorkItemsInQueue) {
	    this.error( "Monitored Attribute WORK_ITEMS_IN_QUEUE has incorrect value " + l2 );
	}

	// Notify the other waiter
	synchronized (waitObject2) {
	    try {
		waitObject2.notify();
	    } catch (Exception e) {
	    }
	}

	// Sleep for sometime
	try {
	    Thread.sleep(5000);
	} catch (InterruptedException ie) {
	}

	l1 = (Long)tpmo.getAttribute(MonitoringConstants.
		THREADPOOL_NUMBER_OF_AVAILABLE_THREADS).getValue();
	if (l1.longValue() != expectedAvailableThreads + 1) {
	    this.error( "Monitored Attribute NUMBER_OF_AVAILABLE_THREADS has incorrect value " + l1);
	}
	System.out.println("NUMBER_OF_AVAILABLE_THREADS = " + l1.longValue());
	l1 = (Long)tpmo.getAttribute(MonitoringConstants.
		THREADPOOL_NUMBER_OF_BUSY_THREADS).getValue();
	if (l1.longValue() != 0) {
	    this.error( "Monitored Attribute NUMBER_OF_BUSY_THREADS has incorrect value " + l1);
	}
	System.out.println("NUMBER_OF_BUSY_THREADS = " + l1.longValue());

	l1 = (Long)tpmo.getAttribute(MonitoringConstants.
		THREADPOOL_CURRENT_NUMBER_OF_THREADS).getValue();
	System.out.println("CURRENT_NUMBER_OF_THREADS = " + l1.longValue());
	l1 = (Long)tpmo.getAttribute(MonitoringConstants.
		THREADPOOL_AVERAGE_WORK_COMPLETION_TIME).getValue();
	System.out.println("AVERAGE_WORK_COMPLETION_TIME (in msec) = " + l1.longValue());
	l1 = (Long)tpmo.getAttribute(MonitoringConstants.
		THREADPOOL_CURRENT_PROCESSED_COUNT).getValue();
	System.out.println("THREADPOOL_CURRENT_PROCESSED_COUNT = " + l1.longValue());

	l2 = (Long)wqmo.getAttribute(MonitoringConstants.
		WORKQUEUE_TOTAL_WORK_ITEMS_ADDED).getValue();
	System.out.println("WORKQUEUE_TOTAL_WORK_ITEMS_ADDED = " + l2.longValue());
	l2 = (Long)wqmo.getAttribute(MonitoringConstants.
		WORKQUEUE_WORK_ITEMS_IN_QUEUE).getValue();
	System.out.println("WORKQUEUE_WORK_ITEMS_IN_QUEUE = " + l2.longValue());
	l2 = (Long)wqmo.getAttribute(MonitoringConstants.
		WORKQUEUE_AVERAGE_TIME_IN_QUEUE).getValue();
	System.out.println("WORKQUEUE_AVERAGE_TIME_IN_QUEUE = " + l2.longValue());

    }

// *************************************************
// ***************   TESTS   ***********************
// *************************************************

    private void runTests()
    {
        // NOTE: These tests are order sensitive
	MonitoredObject unitTestThreadPoolMonitoredObject = 
	    getThreadPoolInstanceMonitoredObject();
	createWork();
	testThreadPoolMonitoredAttributes(unitTestThreadPoolMonitoredObject);
    }


    private class MyWork implements Work {

	private long enqueueTime;
	private Object waiter;
	private Object notifier;

	public MyWork(Object waiter, Object notifier) {
	    this.waiter = waiter;
	    this.notifier = notifier;
	}
	
	public void doWork() {

	    if (notifier != null) {
		// Notify the main thread to continue
		synchronized (notifier) {
		    try {
			notifier.notify();
		    } catch (Exception e) {
		    }
		}
	    }

	    // Wait till the main thread notifies
	    synchronized (waiter) {
		try {
		    // Wait until notified
		    waiter.wait();
		} catch (Exception e) {
		}
	    }
	}
	
	public void setEnqueueTime(long timeInMillis) {
	    enqueueTime = timeInMillis;
	}

	public long getEnqueueTime() {
	    return enqueueTime;
	}

    	public String getName() {
	    return "MyWorkItem";
	}
    }
}
