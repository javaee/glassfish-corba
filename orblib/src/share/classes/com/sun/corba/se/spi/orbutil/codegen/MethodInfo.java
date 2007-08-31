/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2005-2007 Sun Microsystems, Inc. All rights reserved.
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

package com.sun.corba.se.spi.orbutil.codegen;

import java.util.List ;
import java.util.ArrayList ;

import java.lang.reflect.Method ;
import java.lang.reflect.Constructor ;

import com.sun.corba.se.spi.orbutil.codegen.Signature ;
import com.sun.corba.se.spi.orbutil.codegen.Type ;
import com.sun.corba.se.spi.orbutil.codegen.Variable ;

/** An interface that provides information about methods.  This can be
 * used to describe both MethodGenerators that are used to generate code
 * and pre-existing Java classes.
 */
public interface MethodInfo extends MemberInfo {
    /** Returns true if this is a constructor, false if
     * method.
     */
    boolean isConstructor() ;
    /** Return the Type that is returned by this method.
     */
    Type returnType() ;

    /** Return a list of all Exception types that are declared as being
     * throwable from this method.
     */
    List<Type> exceptions() ;

    /** Return a list of arguments for this method.
     */
    List<Variable> arguments() ;
    
    /** Return the signature of this method.
     */
    Signature signature() ;

    /** Return the Method that is represented by this MethodInfo, or null
     * if no such Method instance exists (because this MethodInfo represents
     * a Method being generated, rather than a Method in a Class that is 
     * loaded into the VM). 
     * @throws IllegalStateException if isConstructor() is true. 
     */
    Method getMethod() ;

    /** Return the Constructor that is represented by this MethodInfo, or null
     * if no such Constructor instance exists (because this MethodInfo represents
     * a Constructor being generated, rather than a Constructor in a Class that is 
     * loaded into the VM). 
     * @throws IllegalStateException if isConstructor() is false. 
     */
    Constructor getConstructor() ;
}
