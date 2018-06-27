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

package com.sun.corba.ee.spi.threadpool;

import java.io.Closeable ;

public interface ThreadPoolManager extends Closeable 
{ 
    /** 
    * This method will return an instance of the threadpool given a threadpoolId, 
    * that can be used by any component in the app. server. 
    *
    * @throws NoSuchThreadPoolException thrown when invalid threadpoolId is passed
    * as a parameter
    */ 
    public ThreadPool getThreadPool(String threadpoolId) throws NoSuchThreadPoolException; 

    /** 
    * This method will return an instance of the threadpool given a numeric threadpoolId. 
    * This method will be used by the ORB to support the functionality of 
    * dedicated threadpool for EJB beans 
    *
    * @throws NoSuchThreadPoolException thrown when invalidnumericIdForThreadpool is passed
    * as a parameter
    */ 
    public ThreadPool getThreadPool(int numericIdForThreadpool) throws NoSuchThreadPoolException; 

    /** 
    * This method is used to return the numeric id of the threadpool, given a String 
    * threadpoolId. This is used by the POA interceptors to add the numeric threadpool 
    * Id, as a tagged component in the IOR. This is used to provide the functionality of 
    * dedicated threadpool for EJB beans 
    */ 
    public int  getThreadPoolNumericId(String threadpoolId); 

    /** 
    * Return a String Id for a numericId of a threadpool managed by the threadpool 
    * manager 
    */ 
    public String getThreadPoolStringId(int numericIdForThreadpool); 

    /** 
    * Returns the first instance of ThreadPool in the ThreadPoolManager 
    */ 
    public ThreadPool getDefaultThreadPool(); 

    /**
     * Return an instance of ThreadPoolChooser based on the componentId that was
     * passed as argument
     */
    public ThreadPoolChooser getThreadPoolChooser(String componentId);

    /**
     * Return an instance of ThreadPoolChooser based on the componentIndex that was
     * passed as argument. This is added for improved performance so that the caller
     * does not have to pay the cost of computing hashcode for the componentId
     */
    public ThreadPoolChooser getThreadPoolChooser(int componentIndex);

    /**
     * Sets a ThreadPoolChooser for a particular componentId in the ThreadPoolManager. This 
     * would enable any component to add a ThreadPoolChooser for their specific use
     */
    public void setThreadPoolChooser(String componentId, ThreadPoolChooser aThreadPoolChooser);

    /**
     * Gets the numeric index associated with the componentId specified for a 
     * ThreadPoolChooser. This method would help the component call the more
     * efficient implementation i.e. getThreadPoolChooser(int componentIndex)
     */
    public int getThreadPoolChooserNumericId(String componentId);
} 

// End of file.
