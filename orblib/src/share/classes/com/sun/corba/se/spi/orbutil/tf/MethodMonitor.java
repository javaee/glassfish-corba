/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2002-2010 Sun Microsystems, Inc. All rights reserved.
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

package com.sun.corba.se.spi.orbutil.tf;

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
    void enter( Object ident, Object... args ) ;

    /** Invoked anywhere in the method after enter and before exit, to indicate
     * some useful tracing information in the method.
     * 
     * @param ident The method identifier.
     * @param args Any information needed in the info call.
     */
    void info( Object[] args, Object callerIdent, Object selfIdent ) ;

    /** An exit from a method that has a void return type.  Called as the last
     * operation in the method.
     *
     * @param ident The method identifier.
     */
    void exit( Object ident ) ;

    /** An exit from a method that has a non-void return type.  Called as the last
     * operation in the method.  result will be null if the method terminates
     * by throwing an exception.
     *
     * @param ident The method identifier.
     * @param result The method result.
     */
    void exit( Object ident, Object result ) ;

    /** Called to report an exception that is thrown in the method.  If the
     * method throws and catches the exception, it will still be reported.
     *
     * @param ident The method identifier.
     * @param thr The exception that terminates the method.
     */
    void exception( Object ident, Throwable thr ) ;

    /** Provided for MethodMonitor instances that maintain state.  Simply removes
     * the state and resets the MethodMonitor to its initial state.
     *
     */
    void clear() ;
}
