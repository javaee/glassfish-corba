/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright (c) 2010-2011 Oracle and/or its affiliates. All rights reserved.
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

package com.sun.corba.se.spi.threadpool;

import com.sun.corba.se.impl.threadpool.Exceptions ;
import java.util.ArrayList;
import java.util.List;

/** Interface to support thread state validation.  The basic idea is that
 * one or more validators can be registered with an implementation of the
 * TSV.  The validators are executed whenever a thread is returned to the
 * threadpool,  For example, a validator may check for unreleased locks or uncleared
 * threadlocals.  This is intended as a last-ditch backstop for leaking resource
 * problems.
 *
 * @author ken
 */
public class ThreadStateValidator {
    private static final Exceptions wrapper = Exceptions.self ;

    private static final List<Runnable> validators = new ArrayList<Runnable>() ;

    private ThreadStateValidator() {}

    /** Register a thread validator (represented as a Runnable).
     * A validator may check for locks that should not be held, check
     * for threadlocals that should be cleared, or take any other action
     * to check for resources that should not be held once the thread is no
     * longer needed, as signaled by the thread being returned to the threadpool.
     * <p>
     * A validator typically may take the following actions:
     * <ol>
     * <li>Check whether or not a resource has been released.
     * <li>Log any detected problems.
     * <li>Reclaim the resource.
     * </ol>
     * A validator should NOT throw an exception, as all exceptions thrown
     * from a validator will be ignored.
     *
     * @param validator
     */
    public static void registerValidator( Runnable validator ) {
        validators.add( validator ) ;
    }

    /** Execute all of the validators.  Should only be called from the
     * threadpool implementation.
     */
    public static void checkValidators() {
        for (Runnable run : validators) {
            try {
                run.run() ;
            } catch (Throwable thr) {
                wrapper.threadStateValidatorException( run, thr ) ;
            }
        }
    }
}
