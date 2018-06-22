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

package corba.requestpartitioning;

import com.sun.corba.ee.spi.threadpool.ThreadPoolChooser;
import com.sun.corba.ee.spi.threadpool.ThreadPoolManager;
import com.sun.corba.ee.spi.threadpool.ThreadPool;
import com.sun.corba.ee.spi.threadpool.NoSuchThreadPoolException;

import com.sun.corba.ee.impl.threadpool.ThreadPoolImpl;
import com.sun.corba.ee.spi.misc.ORBConstants;

import java.util.HashMap;
import java.util.ArrayList;

public class TestThreadPoolManager implements ThreadPoolManager { 

    public static final int NUMBER_OF_THREAD_POOLS_TO_CREATE = 64;

    private static final int DEFAULT_NUMBER_OF_QUEUES = 0;
    private static final int DEFAULT_MIN_THREAD_COUNT = 10;
    private static final int DEFAULT_MAX_THREAD_COUNT = 100;

    private static HashMap idToIndexTable = new HashMap();
    private static HashMap indexToIdTable = new HashMap();
    private static ArrayList threadpoolList = new ArrayList();
    private static String defaultID;

    private static ThreadPoolManager testThreadPoolMgr = new TestThreadPoolManager();

    public static ThreadPoolManager getThreadPoolManager() {
        return testThreadPoolMgr;
    }

    TestThreadPoolManager() {

        for (int i = 0; i < NUMBER_OF_THREAD_POOLS_TO_CREATE; i++) {
            createThreadPools(i);
        }
        defaultID = (String)indexToIdTable.get(new Integer(0));
    }

    private void createThreadPools(int index) {
        String threadpoolId = Integer.toString(index);

        // Mutiply the idleTimeoutInSeconds by 1000 to convert to milliseconds
        com.sun.corba.ee.spi.threadpool.ThreadPool threadpool = 
            new ThreadPoolImpl(DEFAULT_MIN_THREAD_COUNT,
                               DEFAULT_MAX_THREAD_COUNT, 
                               ThreadPoolImpl.DEFAULT_INACTIVITY_TIMEOUT * 1000,
                               threadpoolId);

        // Add the threadpool instance to the threadpoolList
        threadpoolList.add(threadpool);

        // Associate the threadpoolId to the index passed
        idToIndexTable.put(threadpoolId, new Integer(index));

        // Associate the threadpoolId to the index passed
        indexToIdTable.put(new Integer(index), threadpoolId);
        
    }

    /** 
    * This method will return an instance of the threadpool given a threadpoolId, 
    * that can be used by any component in the app. server. 
    *
    * @throws NoSuchThreadPoolException thrown when invalid threadpoolId is passed
    * as a parameter
    */ 
    public com.sun.corba.ee.spi.threadpool.ThreadPool
                                getThreadPool(String id) 
        throws NoSuchThreadPoolException {

        Integer i = (Integer)idToIndexTable.get(id);
        if (i == null) {
            throw new NoSuchThreadPoolException();
        }
        try {
            com.sun.corba.ee.spi.threadpool.ThreadPool threadpool =
                (com.sun.corba.ee.spi.threadpool.ThreadPool)
                threadpoolList.get(i.intValue());
            return threadpool;
        } catch (IndexOutOfBoundsException iobe) {
            throw new NoSuchThreadPoolException();
        }
    }

    /** 
    * This method will return an instance of the threadpool given a numeric threadpoolId. 
    * This method will be used by the ORB to support the functionality of 
    * dedicated threadpool for EJB beans 
    *
    * @throws NoSuchThreadPoolException thrown when invalidnumericIdForThreadpool is passed
    * as a parameter
    */ 
    public com.sun.corba.ee.spi.threadpool.ThreadPool 
                        getThreadPool(int numericIdForThreadpool) 
        throws NoSuchThreadPoolException { 

        try {
            com.sun.corba.ee.spi.threadpool.ThreadPool threadpool =
                (com.sun.corba.ee.spi.threadpool.ThreadPool)
                threadpoolList.get(numericIdForThreadpool);
            return threadpool;
        } catch (IndexOutOfBoundsException iobe) {
            throw new NoSuchThreadPoolException();
        }
    }

    /** 
    * This method is used to return the numeric id of the threadpool, given a String 
    * threadpoolId. This is used by the POA interceptors to add the numeric threadpool 
    * Id, as a tagged component in the IOR. This is used to provide the functionality of 
    * dedicated threadpool. 
    */ 
    public int  getThreadPoolNumericId(String id) { 
        Integer i = (Integer)idToIndexTable.get(id);
        return ((i == null) ? 0 : i.intValue());
    }

    /** 
    * Return a String Id for a numericId of a threadpool managed by the threadpool 
    * manager 
    */ 
    public String getThreadPoolStringId(int numericIdForThreadpool) {
        String id = (String)indexToIdTable.get(new Integer(numericIdForThreadpool));
        return ((id == null) ? defaultID : id);
    } 

    /** 
    * Returns the first instance of ThreadPool in the ThreadPoolManager 
    */ 
    public com.sun.corba.ee.spi.threadpool.ThreadPool 
                                        getDefaultThreadPool() {
        try {
            return getThreadPool(0);
        } catch (NoSuchThreadPoolException nstpe) {
            System.err.println("No default ThreadPool defined " + nstpe);
            System.exit(1);
        }
        return null;
    }

    public ThreadPoolChooser getThreadPoolChooser(String componentId) {
        // not used
        return null;
    }

    public ThreadPoolChooser getThreadPoolChooser(int componentIndex) {
        // not used
        return null;
    }

    public void setThreadPoolChooser(String componentId, ThreadPoolChooser aThreadPoolChooser) {
        // not used
    }

    public int getThreadPoolChooserNumericId(String componentId) {
        // not used
        return 0;
    }

    public void close() throws java.io.IOException {
    }
} 


