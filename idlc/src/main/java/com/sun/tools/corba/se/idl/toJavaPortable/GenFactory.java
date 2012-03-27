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

package com.sun.tools.corba.se.idl.toJavaPortable;

// NOTES:

/**
 *
 **/
public class GenFactory implements com.sun.tools.corba.se.idl.GenFactory
{

  public com.sun.tools.corba.se.idl.AttributeGen createAttributeGen ()
  {
    if (Util.corbaLevel (2.4f, 99.0f)) // <d60023>
      return new AttributeGen24 ();
    else
      return new AttributeGen ();
  } // createAttributeGen

  public com.sun.tools.corba.se.idl.ConstGen createConstGen ()
  {
    return new ConstGen ();
  } // createConstGen

  public com.sun.tools.corba.se.idl.NativeGen createNativeGen ()
  {
    return new NativeGen ();
  } // createNativeGen

  public com.sun.tools.corba.se.idl.EnumGen createEnumGen ()
  {
    return new EnumGen ();
  } // createEnumGen

  public com.sun.tools.corba.se.idl.ExceptionGen createExceptionGen ()
  {
    return new ExceptionGen ();
  } // createExceptionGen

  public com.sun.tools.corba.se.idl.ForwardGen createForwardGen ()
  {
    return null;
  } // createForwardGen

  public com.sun.tools.corba.se.idl.ForwardValueGen createForwardValueGen ()
  {
    return null;
  } // createForwardValueGen

  public com.sun.tools.corba.se.idl.IncludeGen createIncludeGen ()
  {
    return null;
  } // createIncludeGen

  public com.sun.tools.corba.se.idl.InterfaceGen createInterfaceGen ()
  {
    return new InterfaceGen ();
  } // createInterfaceGen

  public com.sun.tools.corba.se.idl.ValueGen createValueGen ()
  {
    if (Util.corbaLevel (2.4f, 99.0f)) // <d60023>
      return new ValueGen24 ();
    else
      return new ValueGen ();
  } // createValueGen

  public com.sun.tools.corba.se.idl.ValueBoxGen createValueBoxGen ()
  {
    if (Util.corbaLevel (2.4f, 99.0f)) // <d60023>
      return new ValueBoxGen24 ();
    else
      return new ValueBoxGen ();
  } // createValueBoxGen

  public com.sun.tools.corba.se.idl.MethodGen createMethodGen ()
  {
    if (Util.corbaLevel (2.4f, 99.0f)) // <d60023>
      return new MethodGen24 ();
    else
      return new MethodGen ();
  } // createMethodGen

  public com.sun.tools.corba.se.idl.ModuleGen createModuleGen ()
  {
    return new ModuleGen ();
  } // createModuleGen

  public com.sun.tools.corba.se.idl.ParameterGen createParameterGen ()
  {
    return null;
  } // createParameterGen

  public com.sun.tools.corba.se.idl.PragmaGen createPragmaGen ()
  {
    return null;
  } // createPragmaGen

  public com.sun.tools.corba.se.idl.PrimitiveGen createPrimitiveGen ()
  {
    return new PrimitiveGen ();
  } // createPrimitiveGen

  public com.sun.tools.corba.se.idl.SequenceGen createSequenceGen ()
  {
    return new SequenceGen ();
  } // createSequenceGen

  public com.sun.tools.corba.se.idl.StringGen createStringGen ()
  {
    return new StringGen ();
  } // createSequenceGen

  public com.sun.tools.corba.se.idl.StructGen createStructGen ()
  {
    return new StructGen ();
  } // createStructGen

  public com.sun.tools.corba.se.idl.TypedefGen createTypedefGen ()
  {
    return new TypedefGen ();
  } // createTypedefGen

  public com.sun.tools.corba.se.idl.UnionGen createUnionGen ()
  {
    return new UnionGen ();
  } // createUnionGen
} // class GenFactory
