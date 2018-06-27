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

import com.sun.tools.corba.ee.idl.*;

/**
 *
 **/
public class GenFactory implements com.sun.tools.corba.ee.idl.GenFactory
{

  public com.sun.tools.corba.ee.idl.AttributeGen createAttributeGen ()
  {
    if (com.sun.tools.corba.ee.idl.toJavaPortable.Util.corbaLevel(2.4f, 99.0f)) // <d60023>
      return new AttributeGen24();
    else
      return new com.sun.tools.corba.ee.idl.toJavaPortable.AttributeGen();
  } // createAttributeGen

  public com.sun.tools.corba.ee.idl.ConstGen createConstGen ()
  {
    return new com.sun.tools.corba.ee.idl.toJavaPortable.ConstGen();
  } // createConstGen

  public com.sun.tools.corba.ee.idl.NativeGen createNativeGen ()
  {
    return new com.sun.tools.corba.ee.idl.toJavaPortable.NativeGen();
  } // createNativeGen

  public com.sun.tools.corba.ee.idl.EnumGen createEnumGen ()
  {
    return new com.sun.tools.corba.ee.idl.toJavaPortable.EnumGen();
  } // createEnumGen

  public com.sun.tools.corba.ee.idl.ExceptionGen createExceptionGen ()
  {
    return new com.sun.tools.corba.ee.idl.toJavaPortable.ExceptionGen();
  } // createExceptionGen

  public ForwardGen createForwardGen ()
  {
    return null;
  } // createForwardGen

  public com.sun.tools.corba.ee.idl.ForwardValueGen createForwardValueGen ()
  {
    return null;
  } // createForwardValueGen

  public IncludeGen createIncludeGen ()
  {
    return null;
  } // createIncludeGen

  public com.sun.tools.corba.ee.idl.InterfaceGen createInterfaceGen ()
  {
    return new com.sun.tools.corba.ee.idl.toJavaPortable.InterfaceGen();
  } // createInterfaceGen

  public com.sun.tools.corba.ee.idl.ValueGen createValueGen ()
  {
    if (com.sun.tools.corba.ee.idl.toJavaPortable.Util.corbaLevel(2.4f, 99.0f)) // <d60023>
      return new com.sun.tools.corba.ee.idl.toJavaPortable.ValueGen24();
    else
      return new com.sun.tools.corba.ee.idl.toJavaPortable.ValueGen();
  } // createValueGen

  public com.sun.tools.corba.ee.idl.ValueBoxGen createValueBoxGen ()
  {
    if (com.sun.tools.corba.ee.idl.toJavaPortable.Util.corbaLevel(2.4f, 99.0f)) // <d60023>
      return new com.sun.tools.corba.ee.idl.toJavaPortable.ValueBoxGen24();
    else
      return new com.sun.tools.corba.ee.idl.toJavaPortable.ValueBoxGen();
  } // createValueBoxGen

  public com.sun.tools.corba.ee.idl.MethodGen createMethodGen ()
  {
    if (com.sun.tools.corba.ee.idl.toJavaPortable.Util.corbaLevel(2.4f, 99.0f)) // <d60023>
      return new com.sun.tools.corba.ee.idl.toJavaPortable.MethodGen24();
    else
      return new com.sun.tools.corba.ee.idl.toJavaPortable.MethodGen();
  } // createMethodGen

  public com.sun.tools.corba.ee.idl.ModuleGen createModuleGen ()
  {
    return new com.sun.tools.corba.ee.idl.toJavaPortable.ModuleGen();
  } // createModuleGen

  public ParameterGen createParameterGen ()
  {
    return null;
  } // createParameterGen

  public PragmaGen createPragmaGen ()
  {
    return null;
  } // createPragmaGen

  public com.sun.tools.corba.ee.idl.PrimitiveGen createPrimitiveGen ()
  {
    return new com.sun.tools.corba.ee.idl.toJavaPortable.PrimitiveGen();
  } // createPrimitiveGen

  public com.sun.tools.corba.ee.idl.SequenceGen createSequenceGen ()
  {
    return new com.sun.tools.corba.ee.idl.toJavaPortable.SequenceGen();
  } // createSequenceGen

  public com.sun.tools.corba.ee.idl.StringGen createStringGen ()
  {
    return new com.sun.tools.corba.ee.idl.toJavaPortable.StringGen();
  } // createSequenceGen

  public com.sun.tools.corba.ee.idl.StructGen createStructGen ()
  {
    return new com.sun.tools.corba.ee.idl.toJavaPortable.StructGen();
  } // createStructGen

  public com.sun.tools.corba.ee.idl.TypedefGen createTypedefGen ()
  {
    return new com.sun.tools.corba.ee.idl.toJavaPortable.TypedefGen();
  } // createTypedefGen

  public com.sun.tools.corba.ee.idl.UnionGen createUnionGen ()
  {
    return new com.sun.tools.corba.ee.idl.toJavaPortable.UnionGen();
  } // createUnionGen
} // class GenFactory
