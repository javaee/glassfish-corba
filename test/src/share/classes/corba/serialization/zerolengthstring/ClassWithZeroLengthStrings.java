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

/**
 * ClassWithZeroLengthStrings contains 5 fields of which 4 fields are zero
 * length Strings. field2 and field3 are same instance (so there is an 
 * indirection while reading field4) and field4 and field5 are same instance(
 * so there is an indirection while reading field5). The main idea behind this
 * object is to check whether the aliasing is maintaned when we do a 
 * Util.copyObject( ) by checking for validateObject( ).
 */
package corba.serialization.zerolengthstring;

import java.io.*;

public class ClassWithZeroLengthStrings implements Serializable {
    private int field1;
    private transient String field2;
    private transient String field3;
    private transient String field4;
    private transient String field5;

    public ClassWithZeroLengthStrings( ) {
        field1 = 1;
        field2 = new String("");
        field3 = field2;
        field4 = new String("");
        field5 = field4;
   }

    /**
     * We do write out all the transient String fields using the writeObject.
     */
    private void writeObject(ObjectOutputStream out) throws IOException
    {
        out.defaultWriteObject();
        out.writeObject( field2 );
        out.writeObject( field3 );
        out.writeObject( field4 );
        out.writeObject( field5 );
    }


    /**
     * We do read all the transient String fields using the readObject.
     */
    private void readObject(ObjectInputStream in) 
        throws IOException, ClassNotFoundException
    {
       in.defaultReadObject();
       field2 = (String) in.readObject( );
       field3 = (String) in.readObject( );
       field4 = (String) in.readObject( );
       field5 = (String) in.readObject( );
    }


    /**
     * Important method to check whether the structure of the object is 
     * maintained correctly after Util.copyObject( )
     */
    public boolean validateObject( ) {
       if( field1 != 1 ) {
           System.err.println( "field1 != 1" );
           return false;
       }

       // Structurally fields 2,3,4 and 5 are all Zero Length Strings
       String zeroLengthString = new String("");
       if( !field2.equals( zeroLengthString ) 
        || !field3.equals( zeroLengthString )
        || !field4.equals( zeroLengthString )
        || !field5.equals( zeroLengthString ) )
       {
           System.err.println( "if( !field2.equals( zeroLengthString)" +
               "|| !field3.equals( zeroLengthString )" +
               "|| !field4.equals( zeroLengthString ) "+
               "|| !field5.equals( zeroLengthString ) returned true" );
           return false;
       }

       // We want to make sure field2 and field3 are same instance as well
       // as field4 and field5
       if( field2 != field3 ) {
           System.err.println( "field2 != field3 returned true " );
           return false;
       }
       if( field4 != field5 ) {
           System.err.println( "field4 != field5 returned true " );
           return false;
       }
       if( field3 == field4 ) {
           System.err.println( "field3 == field4 returned true " );
           return false;
       }
       return true;
    }
}


