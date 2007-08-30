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

package com.sun.corba.se.spi.codegen;

public interface ModifiableFieldReference extends FieldInfo {
    public enum ReferenceType { GET, SET } ;

    MethodInfo method() ;

    FieldInfo field() ;

    ReferenceType getReferenceType() ;

    /** Return an expression that can be used to access
     * the target object, if !Modifier.isStatic(field().modifiers()).
     * This variable refers to the target object available 
     * before the set or get of the field.  If the field is
     * static, this returns null.
     */
    Variable getTargetObject() ;

    /** Return an expression that can be used to access
     * the field value. 
     * If getReferenceType()==GET, this variable must be set
     * to the value returned by the reference.  
     * If getReferenceType()==SET, this variable contains the
     * value to be stored by the reference.
     */
    Variable getValue() ;

    /** After this call, the field reference will not be emitted.
     * Instead, any sequence of Wrapper calls valid in a method 
     * body may be used to generate replacement code for the
     * field reference.  As an example, the following code
     * would cause equivalent code to the original reference to be
     * emitted, in the case of a non-static field:
     * <pre>
     * Variable target = mf.getTargetObject() ;
     * Variable value = mf.getValue() ;
     * String name = field().name() ;
     *
     * // For getReferenceType() == GET:
     * _assign( value, _field( target, name ) ) ;
     *
     * // For getRerenceType() == SET:
     * _assign( _field( target, name ), value ) ;
     *
     * </pre>
     */
    void replace() ;

    /** Mark the end of the code generation to replace the field
     * reference.
     */
    void complete() ;
}
