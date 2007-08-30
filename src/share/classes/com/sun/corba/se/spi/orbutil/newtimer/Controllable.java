/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2006-2007 Sun Microsystems, Inc. All rights reserved.
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

package com.sun.corba.se.spi.orbutil.newtimer ;

import java.util.Set ;

/** Represents a named object that can be enabled or disabled.
 * It may also contain other Controllable instances.
 */
public interface Controllable extends Named {
    /** A longer description giving some details of the meaning of this
     * Controllable.
     */
    String description() ;

    /** A small id for this controllable.  Each controllable created from
     * the same TimerFactory will have a unique ID.  All ids will be small
     * integers starting at 0 (so indexing tables by timer ID is supported).
     */
    int id() ;

    /** Return an unmodifiable set of the contents of this Controllable.
     * May always be empty for some subclasses of Controllable.
     */
    Set<? extends Controllable> contents() ;

    /** Enable this controllable.  All Timers that are either enabled, or
     * reachable via contents() from an enabled Controllable are activated,
     * and will cause TimerEvents to be generated when passed to the
     * TimerEventController enter and exit methods.
     */ 
    void enable() ;

    /** Disable this controllable.
     */
    void disable() ;

    /** Return true if enable() was called, otherwise false if enable() was never
     * called, or disable() was last called.
     */
    boolean isEnabled() ;
}
