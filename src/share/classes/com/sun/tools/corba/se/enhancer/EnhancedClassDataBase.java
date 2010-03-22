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

package com.sun.tools.corba.se.enhancer;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 *
 * @author ken
 */
public abstract class EnhancedClassDataBase implements EnhancedClassData {
    protected Util util ;
    protected final Set<String> annotationNames ;

    protected EnhancedClassDataBase( Util util, Set<String> annotationNames ) {
        this.util = util ;
        this.annotationNames = annotationNames ;
    }

    protected String className ;

    protected final Set<String> annoNamesForClass =
        new HashSet<String>() ;

    // Map from MM annotation internal name to
    // SynchronizedHolder<MethodMonitor> field
    // name.  Use something like __$mm$__nnn that is unlikely to collide with
    // another field name that is already in use.
    protected final Map<String,String> annoToHolderName =
        new HashMap<String,String>() ;

    // List of simple names of MM methods.  Index of method is identifier in
    // MethodMonitor calls.  Sorted to guarantee consistent order.
    protected final List<String> methodNames =
        new ArrayList<String>() ;

    // List of descriptors of @InfoMethod-annotated methods.
    // Needed for validating and transforming calls to such methods.
    protected final Set<String> infoMethodDescs =
        new HashSet<String>() ;

    protected final Set<String> mmMethodDescs =
        new HashSet<String>() ;

    // Map from method signature to internal name of its MM annotation.
    protected final Map<String,String> methodToAnno =
        new HashMap<String,String>() ;

    public String getClassName() {
        return className ;
    }

    public Map<String,String> getAnnotationToHolderName() {
        return annoToHolderName ;
    }

    public List<String> getMethodNames() {
        return methodNames ;
    }
    
    public int getMethodIndex( String methodName ) {
        if (methodName != null) {
            for (int ctr = 0; ctr < methodNames.size(); ctr++) {
                if (methodName.equals(methodNames.get(ctr))) {
                    return ctr ;
                }
            }
        }

        return -1 ;
    }
    
    public String getHolderName( String fullMethodDescriptor ) {
        String aname = methodToAnno.get( fullMethodDescriptor ) ;
        String result = annoToHolderName.get( aname ) ;
        return result ;
    }
    
    public MethodType classifyMethod( String fullMethodDescriptor ) {
        if (fullMethodDescriptor.equals( "<clinit>()V")) {
            return MethodType.STATIC_INITIALIZER ;
        } else if (infoMethodDescs.contains( fullMethodDescriptor )) {
            return MethodType.INFO_METHOD ;
        } else if (mmMethodDescs.contains( fullMethodDescriptor)) {
            return MethodType.MONITORED_METHOD ;
        } else {
            return MethodType.NORMAL_METHOD ;
        }
    }
    
    public boolean isTracedClass() {
        return !annoNamesForClass.isEmpty() ;
    }

    public void updateInfoDesc() {
        String[] descs = infoMethodDescs.toArray( 
            new String[infoMethodDescs.size() ] ) ;

        infoMethodDescs.clear() ;

        for (String desc : descs) {
            int index = desc.indexOf( '(' ) ;
            String name = desc.substring( 0, index )  ;
            String d = desc.substring( index ) ;
            String fd = util.augmentInfoMethodDescriptor(d) ;
            infoMethodDescs.add( name + fd ) ;
        }
    }
}
