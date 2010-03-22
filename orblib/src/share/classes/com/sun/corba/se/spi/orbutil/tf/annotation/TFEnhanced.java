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

package com.sun.corba.se.spi.orbutil.tf.annotation;

import java.lang.annotation.Target ;
import java.lang.annotation.Documented ;
import java.lang.annotation.ElementType ;
import java.lang.annotation.Retention ;
import java.lang.annotation.RetentionPolicy ;

/** Indicates that a class has already been enhanced for tracing.
 * There are currently two stages, and a class may be enhanced to either
 * stage=1 or stage=2.  stage=1 means that all class-schema changes have
 * taken place, and the static initializer has been modified to register
 * with the tracing facility, but none of the traceable methods have been
 * modified.  stage=2 includes all stage 1 changes, plus all tracing code 
 * has been added.
 * <p>
 * The reason for 2 stages is that stage 1 must be done at build time, while
 * stage 2 can be done either at build time, or dynmically, for example in a 
 * ClassFileTransformer.  It is extremely helpful if EnhanceTool knows whether
 * a class has already been enhanced, so it can avoid making a mess by 
 * enhancing a class multiple times.  This is also necessary for incremental
 * enhancement when a project is recompiled: only those classes that have been
 * recompiled will be enhanced again.
 *
 * @author ken
 */
@Documented
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
public @interface TFEnhanced {
    TraceEnhanceLevel stage() ;
}

