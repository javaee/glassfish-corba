/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2007-2007 Sun Microsystems, Inc. All rights reserved.
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
package com.sun.corba.se.spi.orbutil.jmx ;

import java.lang.annotation.Documented ;
import java.lang.annotation.Target ;
import java.lang.annotation.ElementType ;
import java.lang.annotation.Retention ;
import java.lang.annotation.RetentionPolicy ;

/** This annotation defines an attribute in open MBean (ManagedObject) or 
 * CompositeData (ManagedData).  It is useful in cases where the parent class 
 * cannot be annotated (for example, Object.toString(), or a framework class 
 * that must be extended
 * but cannot be modified).  The attribute id is defined in the annotation, and 
 * it is implemented by the methods inherited by the Managed entity. 
 * <p>
 * An example of a use of this is to handle @ManagedData that inherits from
 * Collection<X>, and it is desired to display a read-only attribute containing
 * the elements of the Collection.  Simple add the annotation
 * <p>
 * @InheritedAttribute( methodName="iterator" )
 * <p>
 * to handle this case.  Note that this only supports read-only attributes.
 */
@Documented 
@Target(ElementType.TYPE) 
@Retention(RetentionPolicy.RUNTIME)
public @interface InheritedAttribute {
    /** The description of the attribute.  Should be a key to a resource
     * bundle for I18N support.
     */
    String description() default "" ;

    /** The name of the attribute,  This class must inherit a method whose name
     * corresponds to this id in one of the standard ways.
     */
    String id() default "" ;

    /** The name of the method implementing this attribute.  At least one of
     * id and methodName must not be empty.  If only one is given, the other
     * is derived according to the extended attribute name rules.
     */
    String methodName() default "" ;
}



