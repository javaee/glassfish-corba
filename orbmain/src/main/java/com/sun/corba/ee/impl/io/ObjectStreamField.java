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

/*
 * Licensed Materials - Property of IBM
 * RMI-IIOP v1.0
 * Copyright IBM Corp. 1998 1999  All Rights Reserved
 *
 * US Government Users Restricted Rights - Use, duplication or
 * disclosure restricted by GSA ADP Schedule Contract with IBM Corp.
 */

package com.sun.corba.ee.impl.io;

import java.lang.reflect.Field;
import java.lang.Comparable;

import org.glassfish.corba.Bridge;
import java.security.AccessController ;
import java.security.PrivilegedAction ;

import com.sun.corba.ee.impl.misc.ClassInfoCache ;

/**
 * A description of a field in a serializable class.
 * A array of these is used to declare the persistent fields of
 * a class.
 *
 */
public class ObjectStreamField implements Comparable 
{
    private static final Bridge bridge = 
        AccessController.doPrivileged(
            new PrivilegedAction<Bridge>() {
                public Bridge run() {
                    return Bridge.get() ;
                }
            } 
        ) ;

    // Create a named field with the specified type.
    public ObjectStreamField(String n, Class clazz) {
        name = n;
        this.clazz = clazz;
        cinfo = ClassInfoCache.get( clazz ) ;

        // Compute the typecode for easy switching
        if (clazz.isPrimitive()) {
            if (clazz == Integer.TYPE) {
                type = 'I';
            } else if (clazz == Byte.TYPE) {
                type = 'B';
            } else if (clazz == Long.TYPE) {
                type = 'J';
            } else if (clazz == Float.TYPE) {
                type = 'F';
            } else if (clazz == Double.TYPE) {
                type = 'D';
            } else if (clazz == Short.TYPE) {
                type = 'S';
            } else if (clazz == Character.TYPE) {
                type = 'C';
            } else if (clazz == Boolean.TYPE) {
                type = 'Z';
            }
        } else if (cinfo.isArray()) {
            type = '[';
            typeString = ObjectStreamClass.getSignature(clazz);
        } else {
            type = 'L';
            typeString = ObjectStreamClass.getSignature(clazz);
        }

        if (typeString != null)
            signature = typeString;
        else
            signature = String.valueOf(type);

    }

    public ObjectStreamField(Field field) {
        this(field.getName(), field.getType());
        setField( field ) ;
    }

    /**
     * Get the name of this field.
     */
    public String getName() {
        return name;
    }

    public ClassInfoCache.ClassInfo getClassInfo() {
        return cinfo ;
    }

    /**
     * Get the type of the field.
     */
    public Class getType() {
        if (clazz != null)
            return clazz;
        switch (type) {
        case 'B': clazz = Byte.TYPE;
            break;
        case 'C': clazz = Character.TYPE;
            break;
        case 'S': clazz = Short.TYPE;
            break;
        case 'I': clazz = Integer.TYPE;
            break;
        case 'J': clazz = Long.TYPE;
            break;
        case 'F': clazz = Float.TYPE;
            break;
        case 'D': clazz = Double.TYPE;
            break;
        case 'Z': clazz = Boolean.TYPE;
            break;
        case '[':
        case 'L':
            clazz = Object.class;
            break;
        }

        return clazz;
    }

    public char getTypeCode() {
        return type;
    }

    public String getTypeString() {
        return typeString;
    }

    Field getField() {
        return field;
    }

    void setField(Field field) {
        this.field = field;
        this.fieldID = bridge.objectFieldOffset( field ) ;
    }


    /**
     * test if this field is a primitive or not.
     */
    public boolean isPrimitive() {
        return (type != '[' && type != 'L');
    }

    /**
     * Compare this with another ObjectStreamField.
     * return -1 if this is smaller, 0 if equal, 1 if greater
     * types that are primitives are "smaller" than objects.
     * if equal, the names are compared.
     */
    public int compareTo(Object o) {
        ObjectStreamField f2 = (ObjectStreamField)o;
        boolean thisprim = (this.typeString == null);
        boolean otherprim = (f2.typeString == null);

        if (thisprim != otherprim) {
            return (thisprim ? -1 : 1);
        }
        return this.name.compareTo(f2.name);
    }

    /**
     * Compare the types of two class descriptors.
     * The match if they have the same primitive types.
     * or if they are both objects and the object types match.
     */
    public boolean typeEquals(ObjectStreamField other) {
        if (other == null || type != other.type)
            return false;

        /* Return true if the primitive types matched */
        if (typeString == null && other.typeString == null)
            return true;

        return ObjectStreamClass.compareClassNames(typeString,
                                                   other.typeString,
                                                   '/');
    }

    /* Returns the signature of the Field.
     *
     */
    public String getSignature() {

        return signature;

    }

    /**
     * Return a string describing this field.
     */
    public String toString() {
        if (typeString != null)
            return typeString + " " + name;
        else
            return type + " " + name;
    }

    public Class getClazz() {
        return clazz;
    }

    /* Returns the Field ID
     *
     */
    public long getFieldID() {
        return fieldID ;
    }

    private String name;                // the name of the field
    private char type;                  // type first byte of the type signature
    private Field field;                // Reflected field
    private String typeString;          // iff object, typename
    private Class clazz;                // the type of this field, if has been resolved
    private ClassInfoCache.ClassInfo cinfo ;

    // the next 2 things are RMI-IIOP specific, it can be easily
    // removed, if we can figure out all place where there are dependencies
    // to this.  Signature is esentially equal to typestring. Then
    // essentially we can use the java.io.ObjectStreamField as such.

    private String signature;   // the signature of the field
    private long fieldID = Bridge.INVALID_FIELD_OFFSET ; 
}
