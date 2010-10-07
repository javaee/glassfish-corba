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

package com.sun.corba.se.impl.orbutil.copyobject ;

import java.util.Map ;

import com.sun.corba.se.spi.orbutil.copyobject.ReflectiveCopyException ;

/** Provides deep copying of one specific class.
 * An ObjectCopier (what Util.copyObject sees) uses some kind
 * of factory to find the ClassCopier for the Class of the object
 * in order to copy a particular object.
 */
public interface ClassCopier {
    /** Produce a deep copy of source, recursively copying all
     * of its constituents.  Aliasing is preserved through
     * oldToNew, so that no component of source is copied more than
     * once.  Throws ReflectiveCopyException if it cannot copy
     * source.  This may occur in some implementations, depending
     * on the mechanism used to copy the class.
     */
    Object copy( Map oldToNew, 
	Object source ) throws ReflectiveCopyException  ;

    Object copy( Map oldToNew, 
	Object source, boolean debug ) throws ReflectiveCopyException  ;

    /** We need to know whether this class copier operates via reflection
     * or not, as the reflective class copier must be able to tell 
     * when a super class is copied by an incompatible copier.
     */
    boolean isReflectiveClassCopier() ;
}
