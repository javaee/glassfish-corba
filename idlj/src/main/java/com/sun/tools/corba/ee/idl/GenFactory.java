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

/**
 * To extend this compiler framework to generate something other than
 * the default, this factory interface must be implemented and the name
 * of it must be set in the main method (see idl.Compile).
 * <p>
 * The implementation of each method should be quite simple.  Take
 * createAttributeGen, for instance.  If the interface AttributeGen is
 * implemented by a class called MyAttributeGen, then createAttributeGen
 * will be the following:
 * <pre>
 * public AttributeGen createAttributeGen ()
 * {
 *   return new MyAttributeGen ();
 * }
 * </pre>
 * <p>
 * If it is desired that a generator do nothing, it is not necessary to
 * implement one which does nothing; you may simply write that particular
 * create method so that it returns null.
 * <p>
 * Note that this class MUST have a public default constructor (one which
 * takes no parameters).
 **/
public interface GenFactory
{
  public AttributeGen createAttributeGen ();
  public com.sun.tools.corba.ee.idl.ConstGen createConstGen ();
  public com.sun.tools.corba.ee.idl.EnumGen createEnumGen ();
  public com.sun.tools.corba.ee.idl.ExceptionGen createExceptionGen ();
  public com.sun.tools.corba.ee.idl.ForwardGen createForwardGen ();
  public com.sun.tools.corba.ee.idl.ForwardValueGen createForwardValueGen ();
  public com.sun.tools.corba.ee.idl.IncludeGen createIncludeGen ();
  public com.sun.tools.corba.ee.idl.InterfaceGen createInterfaceGen ();
  public com.sun.tools.corba.ee.idl.ValueGen createValueGen ();
  public com.sun.tools.corba.ee.idl.ValueBoxGen createValueBoxGen ();
  public com.sun.tools.corba.ee.idl.MethodGen createMethodGen ();
  public com.sun.tools.corba.ee.idl.ModuleGen createModuleGen ();
  public com.sun.tools.corba.ee.idl.NativeGen createNativeGen ();
  public com.sun.tools.corba.ee.idl.ParameterGen createParameterGen ();
  public com.sun.tools.corba.ee.idl.PragmaGen createPragmaGen ();
  public com.sun.tools.corba.ee.idl.PrimitiveGen createPrimitiveGen ();
  public com.sun.tools.corba.ee.idl.SequenceGen createSequenceGen ();
  public com.sun.tools.corba.ee.idl.StringGen createStringGen ();
  public com.sun.tools.corba.ee.idl.StructGen createStructGen ();
  public com.sun.tools.corba.ee.idl.TypedefGen createTypedefGen ();
  public com.sun.tools.corba.ee.idl.UnionGen createUnionGen ();
} // interface GenFactory
