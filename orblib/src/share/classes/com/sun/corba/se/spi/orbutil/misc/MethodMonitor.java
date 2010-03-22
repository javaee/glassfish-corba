/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2002-2009 Sun Microsystems, Inc. All rights reserved.
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

package com.sun.corba.se.spi.orbutil.misc ; 

/** General interface used to add observability to method execution.
 * This may be used for many purposes, for example:
 * <ul>
 * <li>Logging information about method calls.
 * <li>Collecting timing information.
 * <li>Collecting context information to use in constructing error logs.
 * </ul>
 * Note that tools like DTrace and btrace (and glassfish probes) can
 * provide most of this capability at less cost in programming.  However,
 * there are some advantages to this approach:
 * <ul>
 * <li>This approach provides a very fine grained approach that can easily
 * accommodate very specific semantic knowledge about the code under test.
 * For example, not every argument should be logged by using toString().
 * Sometines another method (e.g. name()) should be used instead to identify
 * a particular argument.
 * <li>Placing info call at intermediate points is sometimes
 * helpful as well, otherwise every significant decision point in the code
 * must be encapsulated in a single method.  While small methods are good,
 * sometimes a slightly larger method is a reasonable choice.
 * <li>This approach can be used to enable a program to reason about its
 * own behavior, unlike an external tool.
 * <li>This approach is very lightweight, unlike typical AOP frameworks.
 * </ul>
 * Obviously there are some disadvantages as well.  This includes some runtime
 * overhead (small if no action is registered), as well as the extra effort
 * in putting such calls in all of the appropriate points.  However, there
 * are too many cases (IIOP marshalling, portable interceptors, the
 * Gmbal typelib) where detailed, carefully-controlled logging is the only
 * effective way to diagnose complex problems.
 *
 * @author Ken Cavanaugh
 */
public interface MethodMonitor {
    /** Make information about the beginning of a method available.
     *
     * @param enabled Indicates whether or not data collection is enabled.
     * Some implementations may choose to ignore this.
     *
     * @param name The name of the method.
     * @param args Some or all of the arguments passed to the method.
     */
    void enter( boolean enabled, String name, Object... args )  ;

    /** Information to accummulate about the current method.
     *
     * @param args The data to accummulate.
     */
    void info( boolean enabled, Object... args ) ;

    /** Indicate that the current method has finished with no result.
     *
     */
    void exit( boolean enabled ) ;

    /** Indicate that the current method has finished with a result.
     *
     * @param result The result of this method call.
     */
    void exit(  boolean enabled, Object result ) ;

    /** Clear any accummulated data.
     * 
     */
    void clear() ;
}
