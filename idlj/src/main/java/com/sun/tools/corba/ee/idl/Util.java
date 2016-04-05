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

package com.sun.tools.corba.ee.idl;

// NOTES:
// -capitalize and parseTypeModifier should probably be in the
//  generators package.
// -D58319<daz> Add version() method.
// -D62023<daz> Add absDelta() method to support float computations.

import java.io.DataInputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;

import java.util.Enumeration;
import java.util.Hashtable;
import java.util.Properties;
import java.util.Vector;

import com.sun.tools.corba.ee.idl.som.cff.FileLocator;

public class Util
{
  // <d58319>
  /**
   * Fetch the version number of this build of the IDL Parser Framework
   * from the appropriate properties file.
   * @return the version number contained within the appropriate properties
   *  file, which indicates the build of this IDL Parser Framework.
   **/
  public static String getVersion ()
  {
    return getVersion ("com/sun/tools/corba/se/idl/idl.prp");
  } // getVersion

  /**
   * Fetch the version number of this build of the IDL Parser Framework.
   * This method may be called before or after the framework has been
   * initialized. If the framework is inititialized, the version information
   * is extracted from the message properties object; otherwise, it is extracted
   * from the indicated messages file.
   * @return the version number.
   **/
  public static String getVersion (String filename)
  {
    String version = "";
    if (messages == null)  // Use supplied file
    {
      Vector oldMsgFiles = msgFiles;
      if (filename == null || filename.equals (""))
        filename = "com/sun/tools/corba/se/idl/idl.prp";
      filename = filename.replace ('/', File.separatorChar);
      registerMessageFile (filename);
      version = getMessage ("Version.product", getMessage ("Version.number"));
      msgFiles = oldMsgFiles;
      messages = null;
    }
    else
    {
      version = getMessage ("Version.product", getMessage ("Version.number"));
    }
    return version;
  } // getVersion

  public static boolean isAttribute (String name, Hashtable symbolTable)
  {
    com.sun.tools.corba.ee.idl.SymtabEntry entry = (com.sun.tools.corba.ee.idl.SymtabEntry)symbolTable.get (name);
    return entry == null ? false : entry instanceof AttributeEntry;
  } // isAttribute

  public static boolean isConst (String name, Hashtable symbolTable)
  {
    com.sun.tools.corba.ee.idl.SymtabEntry entry = (com.sun.tools.corba.ee.idl.SymtabEntry)symbolTable.get (name);
    return entry == null ? false : entry instanceof com.sun.tools.corba.ee.idl.ConstEntry;
  } // isConst

  public static boolean isEnum (String name, Hashtable symbolTable)
  {
    com.sun.tools.corba.ee.idl.SymtabEntry entry = (com.sun.tools.corba.ee.idl.SymtabEntry)symbolTable.get (name);
    return entry == null ? false : entry instanceof com.sun.tools.corba.ee.idl.EnumEntry;
  } // isEnum

  public static boolean isException (String name, Hashtable symbolTable)
  {
    com.sun.tools.corba.ee.idl.SymtabEntry entry = (com.sun.tools.corba.ee.idl.SymtabEntry)symbolTable.get (name);
    return entry == null ? false : entry instanceof com.sun.tools.corba.ee.idl.ExceptionEntry;
  } // isException

  public static boolean isInterface (String name, Hashtable symbolTable)
  {
    com.sun.tools.corba.ee.idl.SymtabEntry entry = (com.sun.tools.corba.ee.idl.SymtabEntry)symbolTable.get (name);
    return entry == null ? false : entry instanceof com.sun.tools.corba.ee.idl.InterfaceEntry;
  } // isInterface

  public static boolean isMethod (String name, Hashtable symbolTable)
  {
    com.sun.tools.corba.ee.idl.SymtabEntry entry = (com.sun.tools.corba.ee.idl.SymtabEntry)symbolTable.get (name);
    return entry == null ? false : entry instanceof com.sun.tools.corba.ee.idl.MethodEntry;
  } // isMethod

  public static boolean isModule (String name, Hashtable symbolTable)
  {
    com.sun.tools.corba.ee.idl.SymtabEntry entry = (com.sun.tools.corba.ee.idl.SymtabEntry)symbolTable.get (name);
    return entry == null ? false : entry instanceof com.sun.tools.corba.ee.idl.ModuleEntry;
  } // isModule

  public static boolean isParameter (String name, Hashtable symbolTable)
  {
    com.sun.tools.corba.ee.idl.SymtabEntry entry = (com.sun.tools.corba.ee.idl.SymtabEntry)symbolTable.get (name);
    return entry == null ? false : entry instanceof com.sun.tools.corba.ee.idl.ParameterEntry;
  } // isParameter

  public static boolean isPrimitive (String name, Hashtable symbolTable)
  {
    // Distinguish "string" because the name could be something like:
    // string(25 + 1)
    com.sun.tools.corba.ee.idl.SymtabEntry entry = (com.sun.tools.corba.ee.idl.SymtabEntry)symbolTable.get (name);
    if (entry == null)
    {
      // If it is null then it may be of the form string(<exp>).
      // Don't just check for string because the name "string" may
      // have been overridden.
      int parenIndex = name.indexOf ('(');
      if (parenIndex >= 0)
        entry = (com.sun.tools.corba.ee.idl.SymtabEntry)symbolTable.get (name.substring (0, parenIndex));
    }
    return entry == null ? false : entry instanceof com.sun.tools.corba.ee.idl.PrimitiveEntry;
  } // isPrimitive

  public static boolean isSequence (String name, Hashtable symbolTable)
  {
    com.sun.tools.corba.ee.idl.SymtabEntry entry = (com.sun.tools.corba.ee.idl.SymtabEntry)symbolTable.get (name);
    return entry == null ? false : entry instanceof com.sun.tools.corba.ee.idl.SequenceEntry;
  } // isSequence

  public static boolean isStruct (String name, Hashtable symbolTable)
  {
    com.sun.tools.corba.ee.idl.SymtabEntry entry = (com.sun.tools.corba.ee.idl.SymtabEntry)symbolTable.get (name);
    return entry == null ? false : entry instanceof com.sun.tools.corba.ee.idl.StructEntry;
  } // isStruct

  public static boolean isString (String name, Hashtable symbolTable)
  {
    com.sun.tools.corba.ee.idl.SymtabEntry entry = (com.sun.tools.corba.ee.idl.SymtabEntry)symbolTable.get (name);
    return entry == null ? false : entry instanceof com.sun.tools.corba.ee.idl.StringEntry;
  } // isString

  public static boolean isTypedef (String name, Hashtable symbolTable)
  {
    com.sun.tools.corba.ee.idl.SymtabEntry entry = (com.sun.tools.corba.ee.idl.SymtabEntry)symbolTable.get (name);
    return entry == null ? false : entry instanceof com.sun.tools.corba.ee.idl.TypedefEntry;
  } // isTypedef

  public static boolean isUnion (String name, Hashtable symbolTable)
  {
    com.sun.tools.corba.ee.idl.SymtabEntry entry = (com.sun.tools.corba.ee.idl.SymtabEntry)symbolTable.get (name);
    return entry == null ? false : entry instanceof com.sun.tools.corba.ee.idl.UnionEntry;
  } // isUnion

  //////////////
  // Message-related methods

  public static String getMessage (String key)
  {
    if (messages == null)
      readMessages ();
    String message = messages.getProperty (key);
    if (message == null)
      message = getDefaultMessage (key);
    return message;
  } // getMessage

  public static String getMessage (String key, String fill)
  {
    if (messages == null)
      readMessages ();
    String message = messages.getProperty (key);
    if (message == null)
      message = getDefaultMessage (key);
    else
    {
      int index = message.indexOf ("%0");
      if (index >= 0)
        message = message.substring (0, index) + fill + message.substring (index + 2);
    }
    return message;
  } // getMessage

  public static String getMessage (String key, String[] fill)
  {
    if (messages == null)
      readMessages ();
    String message = messages.getProperty (key);
    if (message == null)
      message = getDefaultMessage (key);
    else
      for (int i = 0; i < fill.length; ++i)
      {
        int index = message.indexOf ("%" + i);
        if (index >= 0)
          message = message.substring (0, index) + fill[i] + message.substring (index + 2);
      }
    return message;
  } // getMessage

  private static String getDefaultMessage (String keyNotFound)
  {
    String message = messages.getProperty (defaultKey);
    int index = message.indexOf ("%0");
    if (index > 0)
      message = message.substring (0, index) + keyNotFound;
    return message;
  } // getDefaultMessage

  /*
  findFile is no longer used now that FileLocator has been provided
  by Larry Raper of the Shasta team.

  static File findFile (String name) throws FileNotFoundException
  {
    String classpath = System.getProperty ("java.class.path");
    String separator = System.getProperty ("path.separator");
    int end = -separator.length (); // so the first pass classpath == original classpath
    File file;
    do
    {
      classpath = classpath.substring (end + separator.length ());
      end = classpath.indexOf (separator);
      if (end < 0) end = classpath.length ();
      file = new File (classpath.substring (0, end) + File.separator + "com" + File.separator + "ibm" + File.separator + "idl" + File.separator + name);
    } while (!file.exists () && end != classpath.length ());
    if (!file.exists ()) throw new FileNotFoundException ();
    return file;
  } // findFile
  */

  private static void readMessages ()
  {
    messages = new Properties ();
    Enumeration fileList = msgFiles.elements ();
    DataInputStream stream;
    while (fileList.hasMoreElements ())
      try
      {
        stream = FileLocator.locateLocaleSpecificFileInClassPath ((String)fileList.nextElement ());
        messages.load (stream);
      }
      catch (IOException e)
      {
      }
    if (messages.size () == 0)
      messages.put (defaultKey, "Error reading Messages File.");
  } // readMessages

  /** Register a message file.  This file will be searched for
      in the CLASSPATH. */
  public static void registerMessageFile (String filename)
  {
    if (filename != null)
      if (messages == null)
        msgFiles.addElement (filename);
      else
        try
        {
          DataInputStream stream = FileLocator.locateLocaleSpecificFileInClassPath (filename);
          messages.load (stream);
        }
        catch (IOException e)
        {
        }
  } // registerMessageFile

  private static Properties messages   = null;
  private static String     defaultKey = "default";
  private static Vector     msgFiles = new Vector ();
  static
  {
    msgFiles.addElement ("com/sun/tools/corba/se/idl/idl.prp");
  }

  // Message-related methods
  ///////////////

  public static String capitalize (String lc)
  {
    String first = new String (lc.substring (0, 1));
    first = first.toUpperCase ();
    return first + lc.substring (1);
  } // capitalize

  ///////////////
  // General file methods

  /** Searches the current user directory and a list of directories for
      a given short file name and returns its absolute file specification.
      @return Absolute file name of a given short filename
      @throws FileNotFoundException The file does not exist in the
       current user or specified directories.
      @see java.io.File.getAbsolutePath */
  public static String getAbsolutePath (String filename, Vector includePaths) throws FileNotFoundException
  {
    String filepath = null;
    File file = new File (filename);
    if (file.canRead ())
      filepath = file.getAbsolutePath ();
    else
    {
      String fullname = null;
      Enumeration pathList = includePaths.elements ();
      while (!file.canRead () && pathList.hasMoreElements ())
      {
        fullname = (String)pathList.nextElement () + File.separatorChar + filename;
        file = new File (fullname);
      }
      if (file.canRead ())
        filepath = file.getPath ();
      else
        throw new FileNotFoundException (filename);
    }
    return filepath;
  } // getAbsolutePath

  // General file methods
  ///////////////

  ///////////////
  // Numeric computations

  // <d62023>
  /**
   * Compute the absolute value of the difference between two floating-point
   * numbers having single precision.
   * @return the absolute value of the difference between two floats.
   **/
  public static float absDelta (float f1, float f2)
  {
    double delta = f1 - f2;
    return (float)((delta < 0) ? delta * -1.0 : delta);
  } // absDelta

  // Numeric computations
  ///////////////

  static com.sun.tools.corba.ee.idl.RepositoryID emptyID = new com.sun.tools.corba.ee.idl.RepositoryID();
} // class Util
