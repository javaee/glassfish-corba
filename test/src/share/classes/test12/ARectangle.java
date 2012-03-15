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

package test12;

import java.io.*;
import java.awt.*;

/**
 * NOTE : This code was taken from Sun's persistent fields example @
 * http://java.sun.com/products/jdk/1.2/docs/guide/serialization/examples/altimpl/EvolvedClass.java
 */

/**
 * The evolved Rectangle Class. Interally consists of two fields of type
 * Point but externally is still 4 integers (so that it is compatible
 * with the original rectangle class) 
 *
 * In order to make this possible, we need to use the Serializable
 * Field API so that we can define serializable fields that are 
 * not part of the implementation class.
 */
class ARectangle implements java.io.Serializable {

    // new rectangle representation
    
    /**
     * First of two points forming diagonal of rectangle.
     *
     * Note that this field is not a default serializable field
     * due to the use of serialPersistentFields member within this class.
     */
    Point point1;


    /**
     * Second of two points forming diagonal of rectangle.
     *
     * Note that this field is not a default serializable field
     * due to the use of serialPersistentFields member within this class.
     */
    Point point2;

    /* 
     * mandatory SUID field for an evolved Serializable class.
     * serialVersionUID is gotten by doing the serialver command
     * on the original class:
     *                  serialver ARectangle (the original rectangle) 
     */ 
    static final long serialVersionUID = 9030593813711490592L;
 

    /**
     * The special member, serialPeristentFields, explicitly declares 
     * Serializable fields for this class. This allows for fields other
     * than the fields in the class to be persistent. Since we want to 
     * save the state of the two Points point1 and point2, we declare 
     * the 4 ints as the serial persistent fields
     * 
     * @serialField x1  Integer 
     *              X-coordinate of point 1 of diagonal points of rectangle.
     * @serialField y1  Integer 
     *              Y-coordinate of point 1 of diagonal points of rectangle.
     * @serialField x2  Integer 
     *              X-coordinate of point 2 of diagonal points of rectangle.
     * @serialField y2  Integer 
     *              Y-coordinate of point 2 of diagonal points of rectangle.
     */
    private static final ObjectStreamField[] serialPersistentFields = { 
        new ObjectStreamField("x1", Integer.TYPE), 
        new ObjectStreamField("y1", Integer.TYPE), 
        new ObjectStreamField("x2", Integer.TYPE), 
        new ObjectStreamField("y2", Integer.TYPE) 
    }; 
    
    ARectangle(int x1, int y1, int x2, int y2) {
        point1 = new Point(x1, y1);
        point2 = new Point(x2, y2);
    }

    /**
     * writeObject - Writes out the serializable fields 
     * (the 4 integers, x1, y1, x2, y2) using the 
     * Serializable Field API. (the methods putFields and
     * writeFields of the ObjectOutputStream Class and the method put
     * of the ObjectOutputStream.PutField inner class)
     * 
     * @serialData Only the serializable fields of the class are written.
     *             No optional data written. 
     */
    private void writeObject(ObjectOutputStream s)
        throws IOException {

        // set the values of the Serializable fields
        ObjectOutputStream.PutField fields = s.putFields();
        fields.put("x1", point1.x);
        fields.put("y1", point1.y);
        fields.put("x2", point2.x);
        fields.put("y2", point2.y);
                
        // save them
        s.writeFields();        
    }

    /**
     * readsObject - Reads in the serializable fields 
     * (the 4 integers, x1, y1, x2, y2) using the 
     * Serializable Field API. (the methods getFields and
     * readFields of the ObjectInputStream Class and the method get
     * of the ObjectOutputStream.GetField inner class)
     *
     * @serialData No optional data is read.
     */
    private void readObject(ObjectInputStream s)
        throws IOException {
                
        // prepare to read the alternate persistent fields
        ObjectInputStream.GetField fields = null;
        try { 
            fields = s.readFields();

        } catch (Exception ClassNotFoundException) {
            throw new IOException();
        }
                
        // read the alternate persistent fields
        int x1 = (int)fields.get("x1", 0);
        int y1 = (int)fields.get("y1", 0);
        int x2 = (int)fields.get("x2", 0);
        int y2 = (int)fields.get("y2", 0);
                
        // save them back as Points.
        point1 = new Point(x1, y1);
        point2 = new Point(x2, y2);
    }

    public boolean equals(Object o){
        if (!(o instanceof ARectangle))
            return false;

        ARectangle other = (ARectangle)o;

        return ((point1.x == other.point1.x) &&
                (point1.y == other.point1.y) &&
                (point2.x == other.point2.x) &&
                (point2.y == other.point2.y));
    }

    public String toString() {
        return("point1.x: " + point1.x + "\npoint1.y: " + point1.y + "\npoint2.x: " + point2.x + "\npoint2.y: " + point2.y);
    }
}
