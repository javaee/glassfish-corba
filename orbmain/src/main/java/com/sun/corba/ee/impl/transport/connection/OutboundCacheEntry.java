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

package com.sun.corba.ee.impl.transport.connection;

import java.util.Queue ;
import java.util.Collection ;
import java.util.Collections ;

import java.util.concurrent.LinkedBlockingQueue ;

import java.util.concurrent.locks.ReentrantLock ;
import java.util.concurrent.locks.Condition ;

import org.glassfish.gmbal.ManagedData ;
import org.glassfish.gmbal.ManagedAttribute ;
import org.glassfish.gmbal.Description ;

import com.sun.corba.ee.spi.transport.connection.Connection ;
import java.util.ArrayList;

// Represents an entry in the outbound connection cache.  
// This version handles normal shareable ContactInfo 
// (we also need to handle no share).
@ManagedData
public class OutboundCacheEntry<C extends Connection> {
    private ReentrantLock lock ;
    private final Condition waitForPendingConnections ;

    public OutboundCacheEntry( ReentrantLock lock ) {
        this.lock = lock ;
        waitForPendingConnections = lock.newCondition() ;
    }

    final Queue<C> idleConnections = new LinkedBlockingQueue<C>() ;
    final Collection<C> idleConnectionsView =
        Collections.unmodifiableCollection( idleConnections ) ;

    final Queue<C> busyConnections = new LinkedBlockingQueue<C>() ;
    final Collection<C> busyConnectionsView =
        Collections.unmodifiableCollection( busyConnections ) ;

    private int pendingConnections = 0 ;

    @Override
    public String toString() {
        lock.lock() ;
        try {
            return "OutboundCacheEntry[numIdle=" + idleConnections.size()
                    + ",numBusy=" + busyConnections.size()
                    + ",numPending=" + pendingConnections + "]" ;
        } finally {
            lock.unlock();
        }
    }

    @ManagedAttribute
    @Description( "list of idle connections")
    private Collection<C> idleConnections() {
        lock.lock() ;
        try {
            return new ArrayList<C>( idleConnections ) ;
        } finally {
            lock.unlock() ;
        }
    }

    @ManagedAttribute
    @Description( "list of idle connections")
    private Collection<C> busyConnections() {
        lock.lock() ;
        try {
            return new ArrayList<C>( busyConnections ) ;
        } finally {
            lock.unlock() ;
        }
    }

    @ManagedAttribute( id="numIdleConnections" )
    @Description( "Number of idle connections" ) 
    private int numIdleConnectionsAttribute() {
        lock.lock() ;
        try {
            return idleConnections.size() ;
        } finally {
            lock.unlock() ;
        }
    }

    @ManagedAttribute( id="numPendingConnections" )
    @Description( "Number of pending connections" ) 
    private int numPendingConnectionsAttribute() {
        lock.lock() ;
        try {
            return pendingConnections ;
        } finally {
            lock.unlock() ;
        }
    }

    @ManagedAttribute( id="numBusyConnections" )
    @Description( "Number of busy connections" ) 
    private int numBusyConnectionsAttribute() {
        lock.lock() ;
        try {
            return busyConnections.size() ;
        } finally {
            lock.unlock() ;
        }
    }

    public int totalConnections() {
        return idleConnections.size() + busyConnections.size() 
            + pendingConnections ;
    }

    public void startConnect() {
        pendingConnections++ ;
    }

    public void finishConnect() {
        pendingConnections-- ;
        waitForPendingConnections.signal() ;
    }

    public void waitForConnection() {
        waitForPendingConnections.awaitUninterruptibly() ;
    }
}

