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

import java.io.PrintWriter;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.Vector;

/**
 * This is the symbol table entry for forward declarations of interfaces.
 **/
public class ForwardEntry extends SymtabEntry implements InterfaceType
{
  protected ForwardEntry ()
  {
    super ();
  } // ctor

  protected ForwardEntry (ForwardEntry that)
  {
    super (that);
  } // ctor

  protected ForwardEntry (SymtabEntry that, IDLID clone)
  {
    super (that, clone);
    if (module ().equals (""))
      module (name ());
    else if (!name ().equals (""))
      module (module () + "/" + name ());
  } // ctor

  public Object clone ()
  {
    return new ForwardEntry (this);
  } // clone

  /** Invoke the forward declaration generator.
      @param symbolTable the symbol table is a hash table whose key is
       a fully qualified type name and whose value is a SymtabEntry or
       a subclass of SymtabEntry.
      @param stream the stream to which the generator should sent its output.
      @see SymtabEntry */
  public void generate (Hashtable symbolTable, PrintWriter stream)
  {
    forwardGen.generate (symbolTable, this, stream);
  } // generate

  /** Access the interface generator.
      @returns an object which implements the InterfaceGen interface.
      @see InterfaceGen */
  public Generator generator ()
  {
    return forwardGen;
  } // generator

  static boolean replaceForwardDecl (InterfaceEntry interfaceEntry)
  {
    boolean result = true;
    try
    {
      ForwardEntry forwardEntry =
          (ForwardEntry)Parser.symbolTable.get (interfaceEntry.fullName ());
      if ( forwardEntry != null )
      {
        result = (interfaceEntry.getInterfaceType () == 
	    forwardEntry.getInterfaceType ());
        forwardEntry.type (interfaceEntry);

        // If this interface has been forward declared, there are probably
        // other interfaces which derive from a ForwardEntry.  Replace
        // those ForwardEntry's with this InterfaceEntry:
        interfaceEntry.forwardedDerivers = forwardEntry.derivers;
        for ( Enumeration derivers = forwardEntry.derivers.elements();
              derivers.hasMoreElements(); )
          ((InterfaceEntry)derivers.nextElement ()).replaceForwardDecl (forwardEntry, interfaceEntry);

        // Replace the entry's whose types are forward declarations:
        for ( Enumeration types = forwardEntry.types.elements ();
              types.hasMoreElements (); )
          ((SymtabEntry)types.nextElement ()).type (interfaceEntry);
      }
    }
    catch (Exception exception)
    {}
    return result;
  } // replaceForwardDecl

  ///////////////
  // Implement interface InterfaceType

  public int getInterfaceType ()
  {
    return _type;
  }

  public void setInterfaceType (int type)
  {
    _type = type;
  }

  static ForwardGen forwardGen;
  Vector            derivers   = new Vector (); // Vector of InterfaceEntry's.
  Vector            types      = new Vector (); // Vector of the entry's whose type is a forward declaration.
  private int   _type  = InterfaceType.NORMAL; // interface type
} // class ForwardEntry
