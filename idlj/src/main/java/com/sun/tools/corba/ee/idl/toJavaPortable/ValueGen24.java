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
// -D62023 <klr> Update for Java 2.4 RTF
// -D62794.1 <klr> Don't include operations inherited from abstract valuetypes
// -D62794.1 <scn> Don't include operations inherited from supported interfaces

import java.io.PrintWriter;
import java.util.Hashtable;
import java.util.Enumeration;
import java.util.Vector;

import com.sun.tools.corba.ee.idl.GenFileStream;
import com.sun.tools.corba.ee.idl.InterfaceEntry;
import com.sun.tools.corba.ee.idl.SymtabEntry;
import com.sun.tools.corba.ee.idl.TypedefEntry;
import com.sun.tools.corba.ee.idl.ValueEntry;
import com.sun.tools.corba.ee.idl.InterfaceState;
import com.sun.tools.corba.ee.idl.MethodEntry;
import com.sun.tools.corba.ee.idl.AttributeEntry;
import com.sun.tools.corba.ee.idl.PrimitiveEntry;
import com.sun.tools.corba.ee.idl.SequenceEntry;
import com.sun.tools.corba.ee.idl.StringEntry;

/**
 *
 **/
public class ValueGen24 extends ValueGen
{
  /**
   * Public zero-argument constructor.
   **/
  public ValueGen24 ()
  {
  } // ctor

  /**
   * <d62023> - delete constructor; helper is abstract
   **/
  protected void writeConstructor ()
  {
  } // writeConstructor

  /**
   * <d62023> - delete write_value from non-boxed helpers
   *          - delete _write from non-boxed helpers
   **/
  public void helperWrite (SymtabEntry entry, PrintWriter stream)
  {
    // REVISIT: Abstract/Custom??
    // per Simon mail 5/17/99
    stream.println ("    ((org.omg.CORBA_2_3.portable.OutputStream) ostream).write_value (value, id ());");
  } // helperWrite

  /**
   * <d62023> 
   **/
  public void helperRead (String entryName, SymtabEntry entry, PrintWriter stream)
  {
    // REVISIT: Abstract/Custom??
    // per Simon mail 5/17/99
    stream.println ("    return (" + entryName + ")((org.omg.CORBA_2_3.portable.InputStream) istream).read_value (id ());");
  } // helperRead

  /**
   * <d62023> - suppress initializers from mapped value; now generated in
   *    the Helper class and Factory class
   **/
  protected void writeInitializers ()
  {
        // override to do nothing 
  } // writeInitializers

  /**
   * <d62023> - Goes in mapped class, not Helper
   **/
  protected void writeTruncatable () // <d60929>
  {
    if (!v.isAbstract ()) {
       stream.println ("  private static String[] _truncatable_ids = {");
       stream.print   ("    " + com.sun.tools.corba.ee.idl.toJavaPortable.Util.helperName(v, true) + ".id ()");

       // Any safe ValueEntry must have a concete value parent.
       // The topmost parent cannot be safe since it doesn't have
       // a concrete parent.
       ValueEntry child = v;
       while (child.isSafe ())
       {
        stream.println(",");
        ValueEntry parent = (ValueEntry)child.derivedFrom ().elementAt (0);
        stream.print("    \"" + com.sun.tools.corba.ee.idl.toJavaPortable.Util.stripLeadingUnderscoresFromID(parent.repositoryID().ID()) + "\"");
        child = parent;
      }
      stream.println();
      stream.println("  };");
      stream.println();
      stream.println ("  public String[] _truncatable_ids() {");
      stream.println ("    return _truncatable_ids;"); 
      stream.println ("  }");
      stream.println ();
    }
  } // writeTruncatable

  class ImplStreamWriter {
    private boolean isImplementsWritten = false ;

    public void writeClassName( String name ) 
    {
        if (!isImplementsWritten) {
            stream.print( " implements " ) ;
            isImplementsWritten = true ;
        } else
            stream.print( ", " ) ;

        stream.print( name ) ;
    }
  }

  /**
   * <d62023> CustomMarshal -> CustomValue for custom valuetypes
   *          mapped class is abstract
   **/
  protected void writeHeading ()
  {
    ImplStreamWriter isw = new ImplStreamWriter() ;

    com.sun.tools.corba.ee.idl.toJavaPortable.Util.writePackage(stream, v);
    com.sun.tools.corba.ee.idl.toJavaPortable.Util.writeProlog(stream, ((GenFileStream) stream).name());

    if (v.comment () != null)
        v.comment ().generate ("", stream);

    if (v.isAbstract ()) {
        writeAbstract ();
        return;
    } else
        stream.print ("public abstract class " + v.name ());

    // There should always be at least one parent: ValueBase
    SymtabEntry parent = (SymtabEntry) v.derivedFrom ().elementAt (0);

    // If parent is ValueBase, it's mapped to java.io.Serializable
    String parentName = com.sun.tools.corba.ee.idl.toJavaPortable.Util.javaName(parent);
    boolean cv = false; // true if we've already implemented CustomValue

    if (parentName.equals ("java.io.Serializable")) {
        if (((ValueEntry)v).isCustom ()) {
              isw.writeClassName( "org.omg.CORBA.portable.CustomValue" ) ;
              cv = true;
        } else
              isw.writeClassName( "org.omg.CORBA.portable.StreamableValue" ) ;
    } else if ( !((ValueEntry)parent).isAbstract ())
        stream.print (" extends " + parentName);

    // if inheriting from abstract values
    for (int i = 0; i < v.derivedFrom ().size (); i++) {
        parent = (SymtabEntry) v.derivedFrom ().elementAt (i);
        if ( ((ValueEntry)parent).isAbstract ()) {
            isw.writeClassName( com.sun.tools.corba.ee.idl.toJavaPortable.Util.javaName(parent) ) ;
        }
    }

    // Write out the supported interfaces
    Enumeration enumeration = v.supports().elements(); 
    while (enumeration.hasMoreElements())  {
        InterfaceEntry ie = (InterfaceEntry)(enumeration.nextElement()) ;
        String cname = com.sun.tools.corba.ee.idl.toJavaPortable.Util.javaName(ie) ;
        if (!ie.isAbstract())
            cname += "Operations" ;
        isw.writeClassName( cname ) ;
    }
    
    // for when a custom valuetype inherits from a non-custom valuetype
    if ( v.isCustom () && !cv) 
        isw.writeClassName( "org.omg.CORBA.portable.CustomValue" ) ;

    stream.println ();
    stream.println ("{");
  } // writeHeading

  /**
   * <d62023> - private state maps to protected, not default
   **/
  protected void writeMembers ()
  {
    // if the value type contains no data members, a null return is expected
    if (v.state () == null)
      return;

    for (int i = 0; i < v.state ().size (); i ++)
    {
      InterfaceState member = (InterfaceState) v.state ().elementAt (i);
      SymtabEntry entry = (SymtabEntry) member.entry;
      com.sun.tools.corba.ee.idl.toJavaPortable.Util.fillInfo(entry);

      if (entry.comment () != null)
        entry.comment ().generate (" ", stream);

      String modifier = "  ";
      if (member.modifier == InterfaceState.Public)
        modifier = "  public ";
      else
        modifier = "  protected ";
      com.sun.tools.corba.ee.idl.toJavaPortable.Util.writeInitializer(modifier, entry.name(), "", entry, stream);
    }
    stream.println();
  } // writeMembers

  /**
   * <d62023> Methods need to be abstract
   *          writeStreamable
   **/
  protected void writeMethods ()
  {
    // contained vector contains methods, attributes, const, enums, exceptions,
    // structs, unions, or typedefs that are declared inside the value object.
    // State members of the nested types are also included in this vector.
    // Thus, if the declaration of a constructed type is nested in the decl.
    // of a state member, e.g   struct x {boolean b;}  memberx;
    // the generation of the nested type must be handled here.
    Enumeration e = v.contained ().elements ();
    while (e.hasMoreElements ())
    {
      SymtabEntry contained = (SymtabEntry)e.nextElement ();
      if (contained instanceof AttributeEntry)
      {
        AttributeEntry element = (AttributeEntry)contained;
        ((com.sun.tools.corba.ee.idl.toJavaPortable.AttributeGen24)element.generator ()).abstractMethod (symbolTable, element, stream);
      }
      else if (contained instanceof MethodEntry)
      {
        MethodEntry element = (MethodEntry)contained;
        ((com.sun.tools.corba.ee.idl.toJavaPortable.MethodGen24)element.generator ()).abstractMethod (symbolTable, element, stream);
      }
      else
      {
        // Generate the type referenced by the typedef.
        if (contained instanceof TypedefEntry)
          contained.type ().generate (symbolTable, stream);

        // Note that we also need to generate the typedef itself if
        // contained is a typedef.
        contained.generate (symbolTable, stream);
      }
    }

    // Abstract values are mapped to interfaces. There is no need to generate
    // the bindings for inheriting methods in case of inheritance from other
    // abstract values or supporting interface
    if (v.isAbstract ())
        return;

  // Non-abstract, Non-Custom valuetypes support the Streamable interface
  if (!(v.isCustom () || v.isAbstract ()))
      writeStreamableMethods ();
  } // writeMethods

  /**
   * <d62023> Call super._read()
   **/
  public int read (int index, String indent, String name, SymtabEntry entry, PrintWriter stream)
  {
    // First do the state members from concrete parent hierarchy
    Vector vParents = ((ValueEntry) entry).derivedFrom ();
    if (vParents != null && vParents.size() != 0)
    {
      ValueEntry parent = (ValueEntry) vParents.elementAt (0);
      if (parent == null)
        return index;

      // call super._read if non-abstract value parent
      if ((!parent.isAbstract ()) && (! com.sun.tools.corba.ee.idl.toJavaPortable.Util.javaQualifiedName(parent).equals ("java.io.Serializable"))) // <d60929>
          stream.println(indent + "super._read (istream);");
    }

    Vector vMembers = ((ValueEntry) entry).state ();
    int noOfMembers = vMembers == null ? 0 : vMembers.size ();

    for (int k = 0; k < noOfMembers; k++)
    {
      TypedefEntry member = (TypedefEntry)((InterfaceState)vMembers.elementAt (k)).entry;
      String memberName = member.name ();
      SymtabEntry mType = member.type ();

      if (mType instanceof PrimitiveEntry ||
          mType instanceof TypedefEntry   ||
          mType instanceof SequenceEntry  ||
          mType instanceof StringEntry    ||
          !member.arrayInfo ().isEmpty ())
        index = ((com.sun.tools.corba.ee.idl.toJavaPortable.JavaGenerator)member.generator ()).read (index, indent, name + '.' + memberName, member, stream);
      else
        stream.println (indent + name + '.' + memberName + " = " +
                        com.sun.tools.corba.ee.idl.toJavaPortable.Util.helperName(mType, true) + ".read (istream);"); // <d61056>
    }

    return index;
  } // read

  /**
   * <d62023> Call super._write()
   **/
  public int write (int index, String indent, String name, SymtabEntry entry, PrintWriter stream)
  {
    // First do the state members from concrete parent hierarchy
    Vector vParents = ((ValueEntry)entry).derivedFrom ();
    if (vParents != null && vParents.size () != 0)
    {
      ValueEntry parent = (ValueEntry)vParents.elementAt (0);
      if (parent == null)
        return index;
      // call super._read if non-abstract value parent
      if ((!parent.isAbstract ()) && (! com.sun.tools.corba.ee.idl.toJavaPortable.Util.javaQualifiedName(parent).equals ("java.io.Serializable"))) // <d60929>
          stream.println(indent + "super._write (ostream);");
    }

    Vector vMembers = ((ValueEntry) entry ).state ();
    int noOfMembers = vMembers == null ? 0 : vMembers.size ();
    for (int k = 0; k < noOfMembers; k++)
    {
      TypedefEntry member = (TypedefEntry)((InterfaceState)vMembers.elementAt (k)).entry;
      String memberName = member.name ();
      SymtabEntry mType = member.type ();

      if (mType instanceof PrimitiveEntry ||
          mType instanceof TypedefEntry   ||
          mType instanceof SequenceEntry  ||
          mType instanceof StringEntry    ||
          !member.arrayInfo ().isEmpty ())
        index = ((com.sun.tools.corba.ee.idl.toJavaPortable.JavaGenerator)member.generator ()).write (index, indent, name + '.' + memberName, member, stream);
      else
        stream.println (indent + com.sun.tools.corba.ee.idl.toJavaPortable.Util.helperName(mType, true) + // <d61056>
                              ".write (ostream, " + name + '.' + memberName + ");");
    }

    return index;
  } // write

  /**
   * <62023> - generate factory interface and default factory
   **/
  public void generate (Hashtable symbolTable, ValueEntry v, PrintWriter str)
  {
    this.symbolTable = symbolTable;
    this.v = v;
    init ();

    openStream ();
    if (stream == null)
      return;
    generateTie ();
    generateHelper ();
    generateHolder ();
    if (!v.isAbstract ()) {
      generateValueFactory ();
      generateDefaultFactory ();
    }
    writeHeading ();
    writeBody ();
    writeClosing ();
    closeStream ();
  } // generate

  /**
   *
   **/
  protected void generateValueFactory ()
  {
    ((com.sun.tools.corba.ee.idl.toJavaPortable.Factories) com.sun.tools.corba.ee.idl.toJavaPortable.Compile.compiler.factories ()).valueFactory ().generate (symbolTable, v);
  } // generateValueFactory

  /**
   *
   **/
  protected void generateDefaultFactory ()
  {
    ((com.sun.tools.corba.ee.idl.toJavaPortable.Factories) com.sun.tools.corba.ee.idl.toJavaPortable.Compile.compiler.factories ()).defaultFactory ().generate (symbolTable, v);
  } // generateDefaultFactory
}
