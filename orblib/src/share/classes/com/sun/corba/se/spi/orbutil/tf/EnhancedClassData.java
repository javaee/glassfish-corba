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

package com.sun.corba.se.spi.orbutil.tf ;

import com.sun.corba.se.spi.orbutil.generic.SynchronizedHolder;
import com.sun.corba.se.spi.orbutil.tf.annotation.InfoMethod;
import com.sun.corba.se.spi.orbutil.newtimer.TimingPointType;
import java.util.List;
import java.util.Map;
import org.glassfish.gmbal.Description;
import org.objectweb.asm.Type;

/**
 *
 * @author ken
 */
public interface EnhancedClassData {
    Type OBJECT_TYPE = Type.getType( Object.class ) ;
    String OBJECT_NAME = OBJECT_TYPE.getInternalName() ;

    Type SH_TYPE = Type.getType( SynchronizedHolder.class ) ;
    String SH_NAME = SH_TYPE.getInternalName() ;

    Type MM_TYPE = Type.getType( MethodMonitor.class ) ;
    String MM_NAME = MM_TYPE.getInternalName() ;

    String INFO_METHOD_NAME = Type.getInternalName( InfoMethod.class ) ;

    String DESCRIPTION_NAME = Type.getInternalName( Description.class ) ;

    /** Return the internal name of the class.
     * @return The class name.
     */
    String getClassName() ;

    /** Returns true iff this class is monitored.
     *
     * @return true iff this class has one or more MM annotations.
     */
    boolean isTracedClass() ;

    /** Map from MM annotation name to the name of the holder 
     * field that contains the SynchronizedHolder for the
     * corresponding MethodMonitor.  The domain of this map is the set of
     * MM annotations on this class.
     *
     * @return Map from MM annotations defined on this class to the names of
     * the holder fields.
     */
    Map<String,String> getAnnotationToHolderName() ;

    public enum MethodType {
        STATIC_INITIALIZER,
        INFO_METHOD,
        NORMAL_METHOD,
        MONITORED_METHOD
    }

    /** Classify the method.
     * @param fullMethodDescriptor The full method descriptor of the method.
     * @return The kind of the corresponding method.
     */
    MethodType classifyMethod( String fullMethodDescriptor ) ;

    /** Name of the holder fields corresponding to a particular
     * method.  Note that the full descriptor (name + arg/return
     * descriptor) is used to unambiguously identify the method in the class.
     *
     * @param fullMethodDescriptor The full method descriptor of the method.
     * @return The name of the holder field used for this method.
     */
    String getHolderName( String fullMethodDescriptor ) ;

    /** List of method names for all MM methods and info methods 
     * in the class.  Order is significant, as the index of the
     * method in the list is the ordinal used to represent it.
     * This list is in sorted order.
     *
     * @return List of all method tracing names in sorted order.
     */
    List<String> getMethodNames() ;

    /** List of timing point names corresponding to method names.
     * For monitored methods, this is just the method name.
     * For info methods whose tpType is not NONE, this is specified
     * in tpName.
     * @return List of timing point names, in the same order as in 
     * getMethodTracingNames.
     */
    List<String> getTimingPointNames() ;

    /** List of descriptions of monitored methods and info methods.
     * If no description was given in the annotations, the value is "".
     * 
     * @return List of descriptions in the same order as in 
     * getMethodTracingNames.
     */
    List<String> getDescriptions() ;

    /** List of timing point types of monitored methods and info methods.
     * The list contains BOTH for a monitored method.  An info method that
     * does not represent a timing point is represented by NONE.
     * 
     * @return List of TimingPointTypes in the same order as in
     * getMethodTracingNames.
     */
    List<TimingPointType> getTimingPointTypes() ;

    /** List of annotation names for each info method and monitored method.
     * It is interpreted as follows:
     * <ul>
     * <li>If the entry in the list is not null, it is the only annotation
     * applicable to this method.  This is the case for monitored methods.
     * <li>If the entry in the list is null, all annotations on the
     * enclosing class apply to this method.  This is the case for an
     * InfoMethod, which can be called from any monitored method regardless of
     * the annotation on the monitored method.
     * </ul>
     * @return List of annotation names for methods.
     */
    List<String> getMethodMMAnnotationName() ;

    /** Index of method name in the list of method names.
     *
     * @param methodName The method name as defined for tracing.
     * @return the method index
     */
    int getMethodIndex( String methodName ) ;

    /** Enhance all of the descriptors for infoMethods.
     */
    void updateInfoDesc() ;
}
