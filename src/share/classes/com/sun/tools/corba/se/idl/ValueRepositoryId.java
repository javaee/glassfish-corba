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
 * COMPONENT_NAME: idl.parser
 *
 * ORIGINS: 27
 *
 * Licensed Materials - Property of IBM
 * 5639-D57 (C) COPYRIGHT International Business Machines Corp. 1997, 1999
 * RMI-IIOP v1.0
 * US Government Users Restricted Rights - Use, duplication or
 * disclosure restricted by GSA ADP Schedule Contract with IBM Corp.
 */

package com.sun.tools.corba.se.idl;

// NOTES:

import java.security.MessageDigest;
import java.util.Hashtable;

/**
 *
 **/
public class ValueRepositoryId
{
  private MessageDigest sha;       // Message digest used to compute SHA-1
  private int           index;     // Current index in the 'logical' sequence
  private Hashtable     types;     // Already processed types
  private String        hashcode;  // The computed hashcode

  public ValueRepositoryId ()
  {
    try
    {
      sha = MessageDigest.getInstance ("SHA-1");
    }
    catch (Exception exception)
    {}
    index    = 0;
    types    = new Hashtable ();
    hashcode = null;
  } // ctor

  /**Add a value to the hashcode being computed.
     @param value the value to be added to the value RepositoryID. */
  public void addValue (int value)
  {
    sha.update ((byte)((value >> 24) & 0x0F));
    sha.update ((byte)((value >> 16) & 0x0F));
    sha.update ((byte)((value >>  8) & 0x0F));
    sha.update ((byte)(value & 0x0F));
    index++;
  } // addValue

  /** Add a type to the list of types which have already been included.
      Note that the type should be added prior to its value.
      @param entry the type to be added to the value RepositoryID. */
  public void addType (SymtabEntry entry)
  {
    types.put (entry, new Integer (index));
  }

  /** Check to see if a specified type has already been processed. If so,
      add the appropriate 'previously processed' code (0xFFFFFFFF) and
      sequence offset, and return false; otherwise add the symbol table entry
      and current offset to the hashtable and return false.
      @param entry the type to be checked
      @return true if the symbol table entry has not been previously added;
       and false otherwise. */
  public boolean isNewType (SymtabEntry entry)
  {
    Object index = types.get (entry);
    if (index == null)
    {
      addType (entry);
      return true;
    }
    addValue (0xFFFFFFFF);
    addValue (((Integer)index).intValue ());
    return false;
  } // isNewType

  /** Get the hashcode computed for the value type. This method MUST not be
      called until all fields have been added, since it computes the hash
      code from the values entered for each field.
      @return the 64 bit hashcode for the value type represented as a
       16 character hexadecimal string. */
  public String getHashcode ()
  {
    if (hashcode == null)
    {
      byte [] digest = sha.digest ();
      hashcode = hexOf (digest[0]) + hexOf (digest[1]) +
                 hexOf (digest[2]) + hexOf (digest[3]) +
                 hexOf (digest[4]) + hexOf (digest[5]) +
                 hexOf (digest[6]) + hexOf (digest[7]);
    }
    return hashcode;
  } // getHashCode

  // Convert a byte to a two character hex string:
  private static String hexOf (byte value)
  {
    int d1 = (value >> 4) & 0x0F;
    int d2 = value & 0x0F;
    return "0123456789ABCDEF".substring (d1, d1 + 1) +
           "0123456789ABCDEF".substring (d2, d2 + 1);
  } // hexOf
} // class ValueRepositoryId
