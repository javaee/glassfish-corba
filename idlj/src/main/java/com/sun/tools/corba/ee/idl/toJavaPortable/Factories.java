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

package com.sun.tools.corba.ee.idl.toJavaPortable;

// NOTES:
// -D62023<klr> Add corbaLevel=2.4

/**
 *
 **/
public class Factories extends com.sun.tools.corba.ee.idl.Factories
{
  public com.sun.tools.corba.ee.idl.GenFactory genFactory ()
  {
    return new GenFactory();
  } // genFactory

  public com.sun.tools.corba.ee.idl.Arguments arguments ()
  {
    return new com.sun.tools.corba.ee.idl.toJavaPortable.Arguments();
  } // arguments

  public String[] languageKeywords ()
  {
  // These are Java keywords that are not also IDL keywords.
    return keywords;
  } // languageKeywords

  static String[] keywords =
    {"abstract",   "break",     "byte",
     "catch",      "class",     "continue",
     "do",         "else",      "extends",
     "false",      "final",     "finally",
     "for",        "goto",      "if",
     "implements", "import",    "instanceof",
     "int",        "interface", "native",
     "new",        "null",      "operator",
     "outer",      "package",   "private",
     "protected",  "public",    "return",
     "static",     "super",     "synchronized",
     "this",       "throw",     "throws",
     "transient",  "true",      "try",
     "volatile",   "while",
// Special reserved suffixes:
     "+Helper",    "+Holder",   "+Package",
// These following are not strictly keywords.  They
// are methods on java.lang.Object and, as such, must
// not have conflicts with methods defined on IDL
// interfaces.  Treat them the same as keywords.
     "clone",      "equals",       "finalize",
     "getClass",   "hashCode",     "notify",
     "notifyAll",  "toString",     "wait"};

  ///////////////
  // toJava-specific factory methods

  private com.sun.tools.corba.ee.idl.toJavaPortable.Helper _helper = null;        // <62023>
  public com.sun.tools.corba.ee.idl.toJavaPortable.Helper helper ()
  {
    if (_helper == null)
      if (com.sun.tools.corba.ee.idl.toJavaPortable.Util.corbaLevel(2.4f, 99.0f)) // <d60023>
         _helper = new com.sun.tools.corba.ee.idl.toJavaPortable.Helper24();     // <d60023>
      else
         _helper = new com.sun.tools.corba.ee.idl.toJavaPortable.Helper();
    return _helper;
  } // helper

  private com.sun.tools.corba.ee.idl.toJavaPortable.ValueFactory _valueFactory = null;        // <62023>
  public com.sun.tools.corba.ee.idl.toJavaPortable.ValueFactory valueFactory ()
  {
    if (_valueFactory == null)
      if (com.sun.tools.corba.ee.idl.toJavaPortable.Util.corbaLevel(2.4f, 99.0f)) // <d60023>
         _valueFactory = new com.sun.tools.corba.ee.idl.toJavaPortable.ValueFactory();     // <d60023>
      // else return null since shouldn't be used
    return _valueFactory;
  } // valueFactory

  private com.sun.tools.corba.ee.idl.toJavaPortable.DefaultFactory _defaultFactory = null;        // <62023>
  public com.sun.tools.corba.ee.idl.toJavaPortable.DefaultFactory defaultFactory ()
  {
    if (_defaultFactory == null)
      if (com.sun.tools.corba.ee.idl.toJavaPortable.Util.corbaLevel(2.4f, 99.0f)) // <d60023>
         _defaultFactory = new com.sun.tools.corba.ee.idl.toJavaPortable.DefaultFactory();     // <d60023>
      // else return null since shouldn't be used
    return _defaultFactory;
  } // defaultFactory

  private com.sun.tools.corba.ee.idl.toJavaPortable.Holder _holder = new com.sun.tools.corba.ee.idl.toJavaPortable.Holder();
  public com.sun.tools.corba.ee.idl.toJavaPortable.Holder holder ()
  {
    return _holder;
  } // holder

  private com.sun.tools.corba.ee.idl.toJavaPortable.Skeleton _skeleton = new com.sun.tools.corba.ee.idl.toJavaPortable.Skeleton();
  public com.sun.tools.corba.ee.idl.toJavaPortable.Skeleton skeleton ()
  {
    return _skeleton;
  } // skeleton

  private com.sun.tools.corba.ee.idl.toJavaPortable.Stub _stub = new com.sun.tools.corba.ee.idl.toJavaPortable.Stub();
  public com.sun.tools.corba.ee.idl.toJavaPortable.Stub stub ()
  {
    return _stub;
  } // stub

  // toJava-specific factory methods
  ///////////////
} // class Factories
