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

public class IDLID extends RepositoryID
{
  public IDLID ()
  {
    _prefix  = "";
    _name    = "";
    _version = "1.0";
  } // ctor

  public IDLID (String prefix, String name, String version)
  {
    _prefix  = prefix;
    _name    = name;
    _version = version;
  } // ctor

  public String ID ()
  {
    if (_prefix.equals (""))
      return "IDL:" + _name + ':' + _version;
    else
      return "IDL:" + _prefix + '/' + _name + ':' + _version;
  } // ID

  public String prefix ()
  {
    return _prefix;
  } // prefix

  void prefix (String prefix)
  {
    if (prefix == null)
      _prefix = "";
    else
      _prefix = prefix;
  } // prefix

  public String name ()
  {
    return _name;
  } // name

  void name (String name)
  {
    if (name == null)
      _name = "";
    else
      _name = name;
  } // name

  public String version ()
  {
    return _version;
  } // version

  void version (String version)
  {
    if (version == null)
      _version = "";
    else
      _version = version;
  } // version

  void appendToName (String name)
  {
    if (name != null)
      if (_name.equals (""))
        _name = name;
      else
        _name = _name + '/' + name;
  } // appendToName

  void replaceName (String name)
  {
    if (name == null)
      _name = "";
    else
    {
      int index = _name.lastIndexOf ('/');
      if (index < 0)
        _name = name;
      else
        _name = _name.substring (0, index + 1) + name;
    }
  } // replaceName

  public Object clone ()
  {
    return new IDLID (_prefix, _name, _version);
  } // clone

  private String _prefix;
  private String _name;
  private String _version;
} // class IDLID
