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

package com.sun.corba.ee.impl.io;

import java.security.AccessController;
import java.security.PrivilegedExceptionAction;
import java.security.PrivilegedActionException;
import java.security.PrivilegedAction;

import java.lang.reflect.Modifier;
import java.lang.reflect.Array;
import java.lang.reflect.Field;
import java.lang.reflect.Member;
import java.lang.reflect.Method;

import com.sun.corba.ee.impl.misc.ClassInfoCache ;

import com.sun.corba.ee.impl.misc.ORBUtility ;

// This file contains some utility methods that
// originally were in the OSC in the RMI-IIOP
// code delivered by IBM.  They don't make
// sense there, and hence have been put
// here so that they can be factored out in
// an attempt to eliminate redundant code from
// ObjectStreamClass.  Eventually the goal is
// to move to java.io.ObjectStreamClass, and
// java.io.ObjectStreamField.

// class is package private for security reasons

class ObjectStreamClassCorbaExt {

    /**
     * Return true, iff,
     *
     * 1. 'cl' is an interface, and
     * 2. 'cl' and all its ancestors do not implement java.rmi.Remote, and
     * 3. if 'cl' has no methods (including those of its ancestors), or,
     *    if all the methods (including those of its ancestors) throw an
     *    exception that is atleast java.rmi.RemoteException or one of
     *    java.rmi.RemoteException's super classes.
     */
    static final boolean isAbstractInterface(Class cl) {
        ClassInfoCache.ClassInfo cinfo = ClassInfoCache.get( cl ) ;
        if (!cinfo.isInterface() || cinfo.isARemote(cl)) {
            return false;
        }

        Method[] methods = cl.getMethods();
        for (int i = 0; i < methods.length; i++) {
            Class exceptions[] = methods[i].getExceptionTypes();
            boolean exceptionMatch = false;
            for (int j = 0; (j < exceptions.length) && !exceptionMatch; j++) {
                if ((java.rmi.RemoteException.class == exceptions[j]) ||
                    (java.lang.Throwable.class == exceptions[j]) ||
                    (java.lang.Exception.class == exceptions[j]) ||
                    (java.io.IOException.class == exceptions[j])) {
                    exceptionMatch = true;
                }
            }
            if (!exceptionMatch) {
                return false;
            }
        }

        return true;
    }

    // Common collisions (same length):
    // java.lang.String
    // java.math.BigDecimal
    // java.math.BigInteger
    private static final String objectString         = "Ljava/lang/Object;" ;
    private static final String serializableString   = "Ljava/io/Serializable;" ;
    private static final String externalizableString = "Ljava/io/Externalizable;" ;

    // Note that these 3 lengths are different!
    private static final int objectLength = objectString.length() ;
    private static final int serializableLength = serializableString.length() ;
    private static final int externalizableLength = externalizableString.length() ;

    private static final boolean debugIsAny = false ;

    /*
     *  Returns TRUE if type is 'any'.
     *  This is in the marshaling path, so we want it to run as
     *  fast as possible.
     */
    static final boolean isAny(String typeString) {
        if (debugIsAny) {
            ORBUtility.dprint( 
                ObjectStreamClassCorbaExt.class.getName(), 
                "IsAny: typeString = " + typeString ) ;
        }

        int length = typeString.length() ;

        if (length == objectLength) {
            // Note that java.lang.String occurs a lot, and has the
            // same length as java.lang.Object!
            if (typeString.charAt(length-2) == 't')
                return objectString.equals( typeString ) ;
            else
                return false ;
        }

        if (length == serializableLength) {
            // java.math.BigInteger and java.math.BigDecimal have the same
            // length as java.io.Serializable
            if (typeString.charAt(length-2) == 'e')
                return serializableString.equals( typeString ) ;
            else 
                return false ;
        }

        if (length == externalizableLength)
            return externalizableString.equals( typeString ) ;

        return false ;
    }

    private static final Method[] getDeclaredMethods(final Class clz) {
        return AccessController.doPrivileged(
            new PrivilegedAction<Method[]>() {
                public Method[] run() {
                    return clz.getDeclaredMethods();
                }
            }
        );
    }

}
