/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2002-2007 Sun Microsystems, Inc. All rights reserved.
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

package com.sun.tools.corba.se.idl.toJavaPortable;

// NOTES:

import java.util.Enumeration;
import java.util.Hashtable;

import com.sun.tools.corba.se.idl.*;

// This class is passed through the JavaGenerator.HelperType methods.
// It is ONLY used when a recursive sequence is detected. ie.
//
//   struct S1
//   {
//     sequence <S1> others;
//   };

/**
 *
 **/
public class TCOffsets
{
  /**
   * Return -1 if the given name is not in the list of types.
   **/
  public int offset (String name)
  {
    Integer value = (Integer)tcs.get (name);
    return value == null ? -1 : value.intValue ();
  } // offset

  /**
   *
   **/
  public void set (SymtabEntry entry)
  {
    if (entry == null)
      offset += 8;
    else
    {
      tcs.put (entry.fullName (), Integer.valueOf (offset));
      offset += 4;
      String repID = Util.stripLeadingUnderscoresFromID (entry.repositoryID ().ID ());
      if (entry instanceof InterfaceEntry)
        offset += alignStrLen (repID) + alignStrLen (entry.name ());
      else if (entry instanceof StructEntry)
        offset += alignStrLen (repID) + alignStrLen (entry.name ()) + 4;
      else if (entry instanceof UnionEntry)
        offset += alignStrLen (repID) + alignStrLen (entry.name ()) + 12;
      else if (entry instanceof EnumEntry)
      {
        offset += alignStrLen (repID) + alignStrLen (entry.name ()) + 4;
        Enumeration e = ((EnumEntry)entry).elements ().elements ();
        while (e.hasMoreElements ())
          offset += alignStrLen ((String)e.nextElement ());
      }
      else if (entry instanceof StringEntry)
        offset += 4;
      else if (entry instanceof TypedefEntry)
      {
        offset += alignStrLen (repID) + alignStrLen (entry.name ());
        if (((TypedefEntry)entry).arrayInfo ().size () != 0)
          offset += 8;
      }
    }
  } // set

  /**
   * Return the full length of the string type:  4 byte length, x bytes for
   * string + 1 for the null terminator, align it so it ends on a 4-byte
   * boundary.  This method assumes the string starts at a 4-byte boundary
   * since it doesn't do any leading alignment.
   **/
  public int alignStrLen (String string)
  {
    int len = string.length () + 1;
    int align = 4 - (len % 4);
    if (align == 4) align = 0;
    return len + align + 4;
  } // alignStrLen

  /**
   *
   **/
  public void setMember (SymtabEntry entry)
  {
    offset += alignStrLen (entry.name ());
    if (((TypedefEntry)entry).arrayInfo ().size () != 0)
      offset += 4;
  } // setMember

  /**
   *
   **/
  public int currentOffset ()
  {
    return offset;
  } // currentOffset

  /**
   *
   **/
  public void bumpCurrentOffset (int value)
  {
    offset += value;
  } // bumpCurrentOffset

  private Hashtable tcs    = new Hashtable ();
  private int       offset = 0;
} // class TCOffsets
