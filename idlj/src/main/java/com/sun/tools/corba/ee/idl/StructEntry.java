/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright (c) 1997-2018 Oracle and/or its affiliates. All rights reserved.
 * Copyright (c) 1997-1999 IBM Corp. All rights reserved.
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

package com.sun.tools.corba.ee.idl;

// NOTES:

import java.io.PrintWriter;
import java.util.Hashtable;
import java.util.Vector;

/**
 * This is the symbol table entry for structs.
 **/
public class StructEntry extends com.sun.tools.corba.ee.idl.SymtabEntry
{
  protected StructEntry ()
  {
    super ();
  } // ctor

  protected StructEntry (StructEntry that)
  {
    super (that);
    if (!name ().equals (""))
    {
      module (module () + name ());
      name ("");
    }
    _members   = (Vector)that._members.clone ();
    _contained = (Vector)that._contained.clone ();
  } // ctor

  protected StructEntry (com.sun.tools.corba.ee.idl.SymtabEntry that, IDLID clone)
  {
    super (that, clone);
    if (module ().equals (""))
      module (name ());
    else if (!name ().equals (""))
      module (module () + "/" + name ());
  } // ctor

  public Object clone ()
  {
    return new StructEntry (this);
  } // clone

  /** Invoke the struct generator.
      @param symbolTable the symbol table is a hash table whose key is
       a fully qualified type name and whose value is a SymtabEntry or
       a subclass of SymtabEntry.
      @param stream the stream to which the generator should sent its output.
      @see com.sun.tools.corba.ee.idl.SymtabEntry */
  public void generate (Hashtable symbolTable, PrintWriter stream)
  {
    structGen.generate (symbolTable, this, stream);
  } // generate

  /** Access the struct generator.
      @returns an object which implements the StructGen interface.
      @see com.sun.tools.corba.ee.idl.StructGen */
  public com.sun.tools.corba.ee.idl.Generator generator ()
  {
    return structGen;
  } // generator

  /** Add a member to the member list. */
  public void addMember (com.sun.tools.corba.ee.idl.TypedefEntry member)
  {
    _members.addElement (member);
  } // addMember

  /** This is a vector of TypedefEntry's.  In this context, only the name,
      type, and arrayInfo fields hold any meaning. */
  public Vector members ()
  {
    return _members;
  } // members

  public void addContained (com.sun.tools.corba.ee.idl.SymtabEntry entry)
  {
    _contained.addElement (entry);
  } // addContained

  /** This is a vector of SymtabEntry's.  It itemizes any types which
      this struct contains.  It is different than the member list.
      For example:
      <pre>
      struct A
      {
        long x;
        Struct B
        {
          long a;
          long b;
        } y;
      }
      </pre>
      Struct B is contained within struct A.
      The members vector will contain entries for x and y. */
  public Vector contained ()
  {
    return _contained;
  } // contained

  private Vector _members   = new Vector ();
  private Vector _contained = new Vector ();

  static com.sun.tools.corba.ee.idl.StructGen structGen;
} // class StructEntry
