/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2001-2007 Sun Microsystems, Inc. All rights reserved.
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
package com.sun.corba.se.impl.orbutil;

import java.io.*;
import java.util.Hashtable;

/**
 * Since ObjectOutputStream.PutField methods specify no exceptions,
 * we are not checking for null parameters on put methods.
 */
class LegacyHookPutFields extends ObjectOutputStream.PutField
{
    private Hashtable fields = new Hashtable();

    /**
     * Put the value of the named boolean field into the persistent field.
     */
    public void put(String name, boolean value){
        fields.put(name, new Boolean(value));
    }
		
    /**
     * Put the value of the named char field into the persistent fields.
     */
    public void put(String name, char value){
        fields.put(name, new Character(value));
    }
		
    /**
     * Put the value of the named byte field into the persistent fields.
     */
    public void put(String name, byte value){
        fields.put(name, new Byte(value));
    }
		
    /**
     * Put the value of the named short field into the persistent fields.
     */
    public void put(String name, short value){
        fields.put(name, new Short(value));
    }
		
    /**
     * Put the value of the named int field into the persistent fields.
     */
    public void put(String name, int value){
        fields.put(name, new Integer(value));
    }
		
    /**
     * Put the value of the named long field into the persistent fields.
     */
    public void put(String name, long value){
        fields.put(name, new Long(value));
    }
		
    /**
     * Put the value of the named float field into the persistent fields.
     *
     */
    public void put(String name, float value){
        fields.put(name, new Float(value));
    }
		
    /**
     * Put the value of the named double field into the persistent field.
     */
    public void put(String name, double value){
        fields.put(name, new Double(value));
    }
		
    /**
     * Put the value of the named Object field into the persistent field.
     */
    public void put(String name, Object value){
        fields.put(name, value);
    }
		
    /**
     * Write the data and fields to the specified ObjectOutput stream.
     */
    public void write(ObjectOutput out) throws IOException {
        out.writeObject(fields);
    }
}    
