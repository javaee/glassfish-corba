/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2007 Sun Microsystems, Inc. All rights reserved.
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
package com.sun.corba.se.internal.io;

public class IIOPInputStream {
    private static native Object allocateNewObject(Class aclass,
                                                   Class initclass)
        throws InstantiationException, IllegalAccessException;
    /* Create a pending exception.  This is needed to get around
     * the fact that the *Delegate methods do not explicitly
     * declare that they throw exceptions.
     *
     * This native methods creates an exception of the given type with
     * the given message string and posts it to the pending queue.
     */
    private static native void throwExceptionType(Class c, String message);

    /* The following native methods of the form set*Field are used
     * to set private, protected, and package private fields
     * of an Object.
     */
    private static native void setObjectField(Object o, Class c, String fieldName, String fieldSig, Object v);
    private static native void setBooleanField(Object o, Class c, String fieldName, String fieldSig, boolean v);
    private static native void setByteField(Object o, Class c, String fieldName, String fieldSig, byte v);
    private static native void setCharField(Object o, Class c, String fieldName, String fieldSig, char v);
    private static native void setShortField(Object o, Class c, String fieldName, String fieldSig, short v);
    private static native void setIntField(Object o, Class c, String fieldName, String fieldSig, int v);
    private static native void setLongField(Object o, Class c, String fieldName, String fieldSig, long v);
    private static native void setFloatField(Object o, Class c, String fieldName, String fieldSig, float v);
    private static native void setDoubleField(Object o, Class c, String fieldName, String fieldSig, double v);
    private static native void readObject(Object obj, Class asClass, Object ois);

    private static native void setObjectFieldOpt(Object o, long fieldID, Object v);
    private static native void setBooleanFieldOpt(Object o, long fieldID, boolean v);
    private static native void setByteFieldOpt(Object o, long fieldID, byte v);
    private static native void setCharFieldOpt(Object o, long fieldID, char v);
    private static native void setShortFieldOpt(Object o, long fieldID, short v);
    private static native void setIntFieldOpt(Object o, long fieldID, int v);
    private static native void setLongFieldOpt(Object o, long fieldID, long v);

    private static native void setFloatFieldOpt(Object o, long fieldID, float v);
    private static native void setDoubleFieldOpt(Object o, long fieldID, double v);
}
