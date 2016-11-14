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
 * COMPONENT_NAME: idl.toJava
 *
 * ORIGINS: 27
 *
 * Licensed Materials - Property of IBM
 * 5639-D57 (C) COPYRIGHT International Business Machines Corp. 1997, 1999
 * RMI-IIOP v1.0
 * US Government Users Restricted Rights - Use, duplication or
 * disclosure restricted by GSA ADP Schedule Contract with IBM Corp.
 */

package com.sun.tools.corba.ee.idl.toJavaPortable;

// NOTES:
// -D62023  <klr> Update for Java 2.4 RTF

import com.sun.tools.corba.ee.idl.InterfaceState;
import com.sun.tools.corba.ee.idl.PrimitiveEntry;
import com.sun.tools.corba.ee.idl.SequenceEntry;
import com.sun.tools.corba.ee.idl.StringEntry;
import com.sun.tools.corba.ee.idl.SymtabEntry;
import com.sun.tools.corba.ee.idl.TypedefEntry;
import com.sun.tools.corba.ee.idl.ValueBoxEntry;
import com.sun.tools.corba.ee.idl.ValueEntry;

import java.io.PrintWriter;
import java.util.Vector;

/**
 *
 **/
public class ValueBoxGen24 extends ValueBoxGen
{
  /**
   * Public zero-argument constructor.
   **/
  public ValueBoxGen24 ()
  {
  } // ctor

  protected void writeTruncatable () // <d60929>
  {
      stream.print   ("  private static String[] _truncatable_ids = {");
      stream.println (com.sun.tools.corba.ee.idl.toJavaPortable.Util.helperName(v, true) + ".id ()};");
      stream.println ();
      stream.println ("  public String[] _truncatable_ids() {");
      stream.println ("    return _truncatable_ids;"); 
      stream.println ("  }");
      stream.println ();
  } // writeTruncatable

 
  public void helperRead (String entryName, SymtabEntry entry, PrintWriter stream)
  {
    stream.println ("    if (!(istream instanceof org.omg.CORBA_2_3.portable.InputStream)) {");
    stream.println ("      throw new org.omg.CORBA.BAD_PARAM(); }");
    stream.println ("    return (" + entryName +") ((org.omg.CORBA_2_3.portable.InputStream) istream).read_value (_instance);");
    stream.println ("  }");
    stream.println ();

    // done with "read", now do "read_value with real marshalling code.

    stream.println ("  public java.io.Serializable read_value (org.omg.CORBA.portable.InputStream istream)"); // <d60929>
    stream.println ("  {");

    String indent = "    ";
    Vector vMembers = ((ValueBoxEntry) entry).state ();
    TypedefEntry member = ((InterfaceState) vMembers.elementAt (0)).entry;
    SymtabEntry mType = member.type ();
    if (mType instanceof PrimitiveEntry ||
        mType instanceof SequenceEntry ||
        mType instanceof TypedefEntry ||
        mType instanceof StringEntry ||
        !member.arrayInfo ().isEmpty ()) {
      stream.println (indent + com.sun.tools.corba.ee.idl.toJavaPortable.Util.javaName(mType) + " tmp;");
      ((com.sun.tools.corba.ee.idl.toJavaPortable.JavaGenerator)member.generator ()).read (0, indent, "tmp", member, stream);
    }
    else
      stream.println (indent + com.sun.tools.corba.ee.idl.toJavaPortable.Util.javaName(mType) + " tmp = " +
                      com.sun.tools.corba.ee.idl.toJavaPortable.Util.helperName(mType, true) + ".read (istream);");
    if (mType instanceof PrimitiveEntry)
      stream.println (indent + "return new " + entryName + " (tmp);");
    else
      stream.println (indent + "return (java.io.Serializable) tmp;");
  } // helperRead

  public void helperWrite (SymtabEntry entry, PrintWriter stream)
  {
    stream.println ("    if (!(ostream instanceof org.omg.CORBA_2_3.portable.OutputStream)) {");
    stream.println ("      throw new org.omg.CORBA.BAD_PARAM(); }");
    stream.println ("    ((org.omg.CORBA_2_3.portable.OutputStream) ostream).write_value (value, _instance);");
    stream.println ("  }");
    stream.println ();

    // done with "write", now do "write_value with real marshalling code.

    stream.println ("  public void write_value (org.omg.CORBA.portable.OutputStream ostream, java.io.Serializable value)"); 
    stream.println ("  {");

    String entryName = com.sun.tools.corba.ee.idl.toJavaPortable.Util.javaName(entry);
    stream.println ("    if (!(value instanceof " + entryName + ")) {");
    stream.println ("      throw new org.omg.CORBA.MARSHAL(); }");
    stream.println ("    " + entryName + " valueType = (" + entryName + ") value;");
    write (0, "    ", "valueType", entry, stream);
  } // helperWrite

  public int write (int index, String indent, String name, SymtabEntry entry, PrintWriter stream)
  {
    Vector vMembers = ( (ValueEntry) entry ).state ();
    TypedefEntry member = ((InterfaceState) vMembers.elementAt (0)).entry;
    SymtabEntry mType = member.type ();

    if (mType instanceof PrimitiveEntry || !member.arrayInfo ().isEmpty ())
      index = ((com.sun.tools.corba.ee.idl.toJavaPortable.JavaGenerator)member.generator ()).write (index, indent, name + ".value", member, stream);
    else if (mType instanceof SequenceEntry || mType instanceof StringEntry || mType instanceof TypedefEntry || !member.arrayInfo ().isEmpty ())
      index = ((com.sun.tools.corba.ee.idl.toJavaPortable.JavaGenerator)member.generator ()).write (index, indent, name, member, stream);
    else
      stream.println (indent + com.sun.tools.corba.ee.idl.toJavaPortable.Util.helperName(mType, true) + ".write (ostream, " + name + ");"); // <d61056>
    return index;
  } // write
}
