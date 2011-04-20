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

package com.sun.corba.se.spi.orbutil.tf;

import java.util.Collection;

/** Interface representing some action that takes place on entry and exit to
 * a method that is being traced.
 *
 * @author ken
 */
public interface MethodMonitor {
    /** The class for which this MethodMonitor is defined.
     * 
     * @return The class of this MethodMonitor.
     */
    Class<?> myClass() ;

    /** Invoked at the start of a method, before any actions in the method
     * take place.
     * 
     * @param ident The method identifier.
     * @param args The arguments passed into the method.
     */
    void enter( int ident, Object... args ) ;

    /** Invoked anywhere in the method after enter and before exit, to indicate
     * some useful tracing information in the method.
     * 
     * @param callerIdent The identifier of the method calling the InfoMethod.
     * @param selfIdent The identifier of the InfoMethod itself.
     * @param args Any information needed in the info call.
     */
    void info( Object[] args, int callerIdent, int selfIdent ) ;

    /** An exit from a method that has a void return type.  Called as the last
     * operation in the method.
     *
     * @param ident The method identifier.
     */
    void exit( int ident ) ;

    /** An exit from a method that has a non-void return type.  Called as the last
     * operation in the method.  result will be null if the method terminates
     * by throwing an exception.
     *
     * @param ident The method identifier.
     * @param result The method result.
     */
    void exit( int ident, Object result ) ;

    /** Called to report an exception that is thrown in the method.  If the
     * method throws and catches the exception, it will still be reported.
     *
     * @param ident The method identifier.
     * @param thr The exception that terminates the method.
     */
    void exception( int ident, Throwable thr ) ;

    /** Provided for MethodMonitor instances that maintain state.  Simply removes
     * the state and resets the MethodMonitor to its initial state.
     *
     */
    void clear() ;

    /** Returns the contents of this method monitor.  If it is a composite
     * method monitor, all the component MethoMonitor instances are 
     * returned.  If it is a single MethodMonitor, it just returns itself.
     * It is required that a composite method monitor only return MethodMonitor
     * instances which are not themselves composite.
     */
    Collection<MethodMonitor> contents() ;

    /** Factory used to create this MethodMonitor
     * Note: is is required that this.factory().create(myClass()).equals( this )
     * for any MethodMonitor.
     */
    MethodMonitorFactory factory() ;

    String name() ;
}
