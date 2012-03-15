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

import com.sun.corba.ee.spi.logex.stdcorba.StandardLogger;
import org.glassfish.pfl.basic.logex.Chain;
import org.glassfish.pfl.basic.logex.ExceptionWrapper;
import org.glassfish.pfl.basic.logex.Log;
import org.glassfish.pfl.basic.logex.LogLevel;
import org.glassfish.pfl.basic.logex.Message;
import org.glassfish.pfl.basic.logex.WrapperGenerator;

/** Exception wrapper class.  The logex WrapperGenerator uses this interface
 * to generate an implementation which returns the appropriate exception, and
 * generates a log report when the method is called.  This is used for all
 * implementation classes in this package.
 *
 * The exception IDs are allocated in blocks of EXCEPTIONS_PER_CLASS, which is
 * a lot more than is needed, but we have 32 bits for IDs, and multiples of
 * a suitably chosen EXCEPTIONS_PER_CLASS (like 100 here) are easy to read in
 * error messages.
 *
 * @author ken
 */
@ExceptionWrapper( idPrefix="ORBTPOOL" )
public interface Exceptions {
    static final Exceptions self = WrapperGenerator.makeWrapper(
        Exceptions.class, StandardLogger.self ) ;

    // Allow 100 exceptions per class
    static final int EXCEPTIONS_PER_CLASS = 100 ;

// ThreadPoolImpl
    static final int TP_START = 1 ;

    @Message( "Join was interrrupted on thread {0} while closing ThreadPool {1}" )
    @Log( id = TP_START + 0 )
    void interruptedJoinCallWhileClosingThreadPool(
        @Chain InterruptedException exc, Thread wt, ThreadPoolImpl aThis);

    @Message( "Worker Thread {0} has been created with ClassLoader {1}" )
    @Log( id = TP_START + 0, level=LogLevel.FINE )
    void workerThreadCreated(Thread thread,
        ClassLoader contextClassLoader);

    @Message( "Worker thread creation failure" )
    @Log( id = TP_START + 1, level=LogLevel.SEVERE )
    void workerThreadCreationFailure( @Chain Throwable t);

    @Message( "Unable to get worker thread {0}; check securiy policy file:"
        + " must grant 'getContextClassLoader' runtime permission")
    @Log( id = TP_START + 2 )
    RuntimeException workerThreadGetContextClassloaderFailed(
        @Chain SecurityException se, Thread aThis);

    @Message( "Worker thread {0} context ClassLoader was changed to {1};"
        + " will attempt a reset to its initial ClassLoader {2} " )
    @Log( id = TP_START + 3, level=LogLevel.FINE )
    void workerThreadForgotClassloaderReset( Thread aThis,
        ClassLoader currentClassLoader, ClassLoader workerThreadClassLoader);

    @Message( "Unable to set worker thread {0}; check securiy policy file:"
        + " must grant 'setContextClassLoader' runtime permission")
    @Log( id = TP_START + 5 )
    void workerThreadResetContextClassloaderFailed(
        @Chain SecurityException se, Thread aThis);

    @Message( "Worker thread {0} caught throwable while executing work." )
    @Log( id = TP_START + 6 )
    void workerThreadDoWorkThrowable( @Chain Throwable t, Thread aThis);

    @Message( "Worker thread {0} will exit; current thread count {1} is"
        + " greater than minimum worker threads needed {2}" )
    @Log( id = TP_START + 7, level=LogLevel.FINE )
    void workerThreadNotNeeded( Thread aThis,
        int currentNumberOfThreads, int minimumNumberOfThreads);

    @Message( "Worker thread from thread pool {0} was interrupted:"
        + " closeCalled is {1}" )
    @Log( id = TP_START + 8, level=LogLevel.FINE )
    void workQueueThreadInterrupted(
        InterruptedException exc, String name, Boolean valueOf);

    @Message( "Worker thread {0} caught throwable when"
        + " requesting work from work queue {1}" )
    @Log( id = TP_START + 9, level=LogLevel.FINE )
    void workerThreadThrowableFromRequestWork(
        @Chain Throwable t, Thread aThis, String name);

    @Message( "Worker thread {0} caught unexpected throwable" )
    @Log( id = TP_START + 10 )
    void workerThreadCaughtUnexpectedThrowable(
        @Chain Throwable e, Thread aThis);

// ThreadPoolManagerImpl
    static final int TPM_START = TP_START + EXCEPTIONS_PER_CLASS ;

    @Message( "Error in closing ThreadPool" )
    @Log( id = TPM_START + 0 )
    void threadPoolCloseError() ;

    @Message( "ThreadGroup {0} is already destroyed; cannot destroy it again" )
    @Log( id = TPM_START + 1 )
    void threadGroupIsDestroyed( ThreadGroup thgrp ) ;

    @Message( "ThreadGroup {0} has {1} active threads: destroy may cause exceptions" )
    @Log( id = TPM_START + 2 )
    void threadGroupHasActiveThreadsInClose( ThreadGroup thgrp,
        int numThreads) ;

    @Message( "ThreadGroup {0} has {1} sub-ThreadGroups: destroy may cause exceptions" )
    @Log( id = TPM_START + 3 )
    void threadGroupHasSubGroupsInClose(ThreadGroup threadGroup,
        int numGroups);

    @Message( "ThreadGroup {0} could not be destroyed" )
    @Log( id = TPM_START + 4 )
    void threadGroupDestroyFailed( @Chain IllegalThreadStateException exc,
        ThreadGroup threadGroup);

// ThreadStateValidator
    static final int TSV_START = TPM_START + EXCEPTIONS_PER_CLASS ;

    @Message( "Thread state validator threw an exception on validator {0}" )
    @Log( id = TSV_START + 0 ) 
    RuntimeException threadStateValidatorException( Runnable run, @Chain Throwable exc ) ;
}
