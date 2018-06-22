/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright (c) 1997-2018 Oracle and/or its affiliates. All rights reserved.
 *
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common Development
 * and Distribution License("CDDL") (collectively, the "License").  You
 * may not use this file except in compliance with the License.  You can
 * obtain a copy of the License at
 * https://oss.oracle.com/licenses/CDDL+GPL-1.1
 * or LICENSE.txt.  See the License for the specific
 * language governing permissions and limitations under the License.
 *
 * When distributing the software, include this License Header Notice in each
 * file and include the License file at LICENSE.txt.
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

package com.sun.corba.ee.impl.misc;

import java.io.*;
import java.util.Hashtable;

class LegacyHookGetFields extends ObjectInputStream.GetField {
    private Hashtable fields = null;

    LegacyHookGetFields(Hashtable fields){
        this.fields = fields;
    }

    /**
     * Get the ObjectStreamClass that describes the fields in the stream.
     */
    public java.io.ObjectStreamClass getObjectStreamClass() {
        return null;
    }
                
    /**
     * Return true if the named field is defaulted and has no value
     * in this stream.
     */
    public boolean defaulted(String name)
        throws IOException, IllegalArgumentException  {
        return (!fields.containsKey(name));
    }
                
    /**
     * Get the value of the named boolean field from the persistent field.
     */
    public boolean get(String name, boolean defvalue) 
        throws IOException, IllegalArgumentException {
        if (defaulted(name))
            return defvalue;
        else return ((Boolean)fields.get(name)).booleanValue();
    }
                
    /**
     * Get the value of the named char field from the persistent fields.
     */
    public char get(String name, char defvalue) 
        throws IOException, IllegalArgumentException {
        if (defaulted(name))
            return defvalue;
        else return ((Character)fields.get(name)).charValue();

    }
                
    /**
     * Get the value of the named byte field from the persistent fields.
     */
    public byte get(String name, byte defvalue) 
        throws IOException, IllegalArgumentException {
        if (defaulted(name))
            return defvalue;
        else return ((Byte)fields.get(name)).byteValue();

    }
                
    /**
     * Get the value of the named short field from the persistent fields.
     */
    public short get(String name, short defvalue) 
        throws IOException, IllegalArgumentException {
        if (defaulted(name))
            return defvalue;
        else return ((Short)fields.get(name)).shortValue();

    }
                
    /**
     * Get the value of the named int field from the persistent fields.
     */
    public int get(String name, int defvalue) 
        throws IOException, IllegalArgumentException {
        if (defaulted(name))
            return defvalue;
        else return ((Integer)fields.get(name)).intValue();

    }
                
    /**
     * Get the value of the named long field from the persistent fields.
     */
    public long get(String name, long defvalue)
        throws IOException, IllegalArgumentException {
        if (defaulted(name))
            return defvalue;
        else return ((Long)fields.get(name)).longValue();

    }
                
    /**
     * Get the value of the named float field from the persistent fields.
     */
    public float get(String name, float defvalue) 
        throws IOException, IllegalArgumentException {
        if (defaulted(name))
            return defvalue;
        else return ((Float)fields.get(name)).floatValue();

    }
                
    /**
     * Get the value of the named double field from the persistent field.
     */
    public double get(String name, double defvalue) 
        throws IOException, IllegalArgumentException  {
        if (defaulted(name))
            return defvalue;
        else return ((Double)fields.get(name)).doubleValue();

    }
                
    /**
     * Get the value of the named Object field from the persistent field.
     */
    public Object get(String name, Object defvalue) 
        throws IOException, IllegalArgumentException {
        if (defaulted(name))
            return defvalue;
        else return fields.get(name);

    }
                
    public String toString(){
        return fields.toString();
    }
}    
