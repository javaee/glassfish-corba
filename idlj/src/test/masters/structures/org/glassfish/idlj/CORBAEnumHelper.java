/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright (c) 2018 Oracle and/or its affiliates. All rights reserved.
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

package org.glassfish.idlj;

/**
* org/glassfish/idlj/CORBAEnumHelper.java .
* Generated by the IDL-to-Java compiler (portable), version "4.1"
* from /Users/rgold/projects/glassfish/glassfish-corba/idlj/src/main/idl/org/glassfish/idlj/CORBAServerTest.idl
* Monday, January 29, 2018 11:19:41 AM EST
*/

abstract public class CORBAEnumHelper
{
  private static String  _id = "IDL:org/glassfish/idlj/CORBAEnum:1.0";

  public static void insert (org.omg.CORBA.Any a, org.glassfish.idlj.CORBAEnum that)
  {
    org.omg.CORBA.portable.OutputStream out = a.create_output_stream ();
    a.type (type ());
    write (out, that);
    a.read_value (out.create_input_stream (), type ());
  }

  public static org.glassfish.idlj.CORBAEnum extract (org.omg.CORBA.Any a)
  {
    return read (a.create_input_stream ());
  }

  private static org.omg.CORBA.TypeCode __typeCode = null;
  synchronized public static org.omg.CORBA.TypeCode type ()
  {
    if (__typeCode == null)
    {
      __typeCode = org.omg.CORBA.ORB.init ().create_enum_tc (org.glassfish.idlj.CORBAEnumHelper.id (), "CORBAEnum", new String[] { "ONE", "TWO", "THREE"} );
    }
    return __typeCode;
  }

  public static String id ()
  {
    return _id;
  }

  public static org.glassfish.idlj.CORBAEnum read (org.omg.CORBA.portable.InputStream istream)
  {
    return org.glassfish.idlj.CORBAEnum.from_int (istream.read_long ());
  }

  public static void write (org.omg.CORBA.portable.OutputStream ostream, org.glassfish.idlj.CORBAEnum value)
  {
    ostream.write_long (value.value ());
  }

}
