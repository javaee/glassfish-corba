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

package com.sun.corba.se.spi.orb;

public interface ClassCodeBaseHandler {
    /** Returns some sort of codebase for the given class, or null.
     * It is expected that, if str is the result of getCodeBase( cls ), 
     * then loadClass( str, cls.getClassName() ) will return cls.
     * <return>A codebase to use with this handler, or null if this handler
     * does not apply to this class.
     */
    String getCodeBase( Class cls ) ;

    /** load a class given the classname and a codebase.
     * The className will always satisfy cls.getClassName().equals( className ) 
     * if the call succeeds and returns a Class.
     * <param>codebase A string that somehow describes which ClassLoader to use.
     * For example, the string could be an ordinary URL that a URL ClassLoader can use,
     * or something more specialized, such as a description of an OSGi bundles and version.
     * <param>className The name of the class to load.
     * <return>The loaded class, or null if the class could not be loaded.
     */
    Class loadClass( String codebase, String className ) ;
}
