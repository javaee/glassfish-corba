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

package com.sun.corba.se.impl.encoding.fast ;

import java.lang.reflect.InvocationTargetException ;
import java.io.ObjectInputStream ;
import java.io.ObjectOutputStream ;
import java.io.IOException ;

import com.sun.corba.se.impl.io.ObjectStreamField ;

/** Analyze a class to determine its structure for serialization.
 * The results of this class are INDEPENDENT of the serialization algorithm.
 * In particular, no assumption is made that we can compute field offsets at this 
 * level, as every serialization scheme has its own constraints on this: 
 * JRMP simply packs
 * everything together, IIOP must obey CDR padding rules, and Emerge has typecodes
 * between values, all packed together on byte boundaries.  However, all protocols
 * will order fields the same way: first all fields of primitive type, then all
 * fields of non-primitive type.  Within each section, fields are ordered alphabetically
 * by field name.  Currently JRMP support unshared fields, but only if using 
 * serialPersistentFields to specify the class serialization.  IIOP and Emerge
 * will not support this (strange) feature.
 *
 * XXX Making a lot of these methods public is a security risk.  We will need
 * to control access to the ClassAnalyzer using a Permission.
 */
public interface ClassAnalyzer<T> {
    // XXX we probably also need/want both the Java serialVersionUID and the RMI-IIOP
    // structuralUID available here.

    /**
     * The name of the class described by this descriptor.
     *
     * @return	a <code>String</code> representing the fully qualified name of
     * 		the class
     */
    public String getName() ;

    /**
     * The name of the class described by this descriptor as a char[] 
     * instead of a String.
     *
     * @return	a <code>String</code> representing the fully qualified name of
     * 		the class
     */
    public char[] getNameAsCharArray() ;

    /**
     * Return the class in the local VM that this version is mapped to.  Null
     * is returned if there is no corresponding local class.
     *
     * @return	the <code>Class</code> instance that this descriptor represents
     */
    public Class<T> forClass() ;
    
    /** Return field information, ordered correctly for serialization.
     * Of course, the caller is free to use whatever order is desired, but here
     * we use the normal serialization order: all fields of primitive type
     * come first, followed by the non-primitives, and within each section,
     * fields are ordered alphabetically.
     * <p>
     * The caller is expected to deal with obtaining java.lang.reflect.Field instances
     * if they are required.  Note that custom marshaled classes that define
     * serialPersistentFields and use putFields/writeFields to write or
     * readFields/getFields to read may NOT have class fields that correspond to the
     * fields returned in this call.
     */
    // was declared: public List<Pair<String,Class>> getFields() ;
    public ObjectStreamField[] getFields() ;

    /**
     * Return a string describing this ClassAnalyzer.
     */
    @Override
    public String toString() ;

    public ClassAnalyzer<?> getSuperClassAnalyzer() ;

    public boolean isProxy() ;
    
    public boolean isEnum() ;
    
    public boolean isExternalizable() ;
    
    public boolean isSerializable() ;

    /**
     * Returns true if represented class is serializable/externalizable and can
     * be instantiated by the serialization runtime--i.e., if it is
     * externalizable and defines a public no-arg constructor, or if it is
     * non-externalizable and its first non-serializable superclass defines an
     * accessible no-arg constructor.  Otherwise, returns false.
     */
    public boolean isInstantiable() ;

    /**
     * Creates a new instance of the represented class.  If the class is
     * externalizable, invokes its public no-arg constructor; otherwise, if the
     * class is serializable, invokes the no-arg constructor of the first
     * non-serializable superclass.  Throws UnsupportedOperationException if
     * this class descriptor is not associated with a class, if the associated
     * class is non-serializable or if the appropriate no-arg constructor is
     * inaccessible/unavailable.
     */
    public Object newInstance() throws InstantiationException, InvocationTargetException,
        UnsupportedOperationException ;

    /**
     * Returns true if represented class is serializable (but not
     * externalizable) and defines a conformant writeObject method.  Otherwise,
     * returns false.
     */
    public boolean hasWriteObjectMethod() ;
    
    /**
     * Invokes the writeObject method of the represented serializable class.
     * Throws UnsupportedOperationException if this class descriptor is not
     * associated with a class, or if the class is externalizable,
     * non-serializable or does not define writeObject.
     */
    public void invokeWriteObject(Object obj, ObjectOutputStream out)
	throws IOException, UnsupportedOperationException ;

    /**
     * Returns true if represented class is serializable (but not
     * externalizable) and defines a conformant readObject method.  Otherwise,
     * returns false.
     */
    public boolean hasReadObjectMethod() ;
    
    /**
     * Invokes the readObject method of the represented serializable class.
     * Throws UnsupportedOperationException if this class descriptor is not
     * associated with a class, or if the class is externalizable,
     * non-serializable or does not define readObject.
     */
    void invokeReadObject(Object obj, ObjectInputStream in)
	throws ClassNotFoundException, IOException, 
	       UnsupportedOperationException ;

    /**
     * Returns true if represented class is serializable (but not
     * externalizable) and defines a conformant readObjectNoData method.
     * Otherwise, returns false.
     */
    public boolean hasReadObjectNoDataMethod() ;

    /**
     * Invokes the readObjectNoData method of the represented serializable
     * class.  Throws UnsupportedOperationException if this class descriptor is
     * not associated with a class, or if the class is externalizable,
     * non-serializable or does not define readObjectNoData.
     */
    public void invokeReadObjectNoData(Object obj)
	throws IOException, UnsupportedOperationException ;

    /**
     * Returns true if represented class is serializable or externalizable and
     * defines a conformant writeReplace method.  Otherwise, returns false.
     */
    public boolean hasWriteReplaceMethod() ;

    /**
     * Invokes the writeReplace method of the represented serializable class and
     * returns the result.  Throws UnsupportedOperationException if this class
     * descriptor is not associated with a class, or if the class is
     * non-serializable or does not define writeReplace.
     */
    public Object invokeWriteReplace(Object obj)
	throws IOException, UnsupportedOperationException ;

    /**
     * Returns true if represented class is serializable or externalizable and
     * defines a conformant readResolve method.  Otherwise, returns false.
     */
    public boolean hasReadResolveMethod() ;

    /**
     * Invokes the readResolve method of the represented serializable class and
     * returns the result.  Throws UnsupportedOperationException if this class
     * descriptor is not associated with a class, or if the class is
     * non-serializable or does not define readResolve.
     */
    Object invokeReadResolve(Object obj)
	throws IOException, UnsupportedOperationException ;
}

