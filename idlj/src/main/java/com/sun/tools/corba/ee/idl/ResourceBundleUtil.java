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
// -capitalize and parseTypeModifier should probably be in the
//  generators package.
// -D58319<daz> Add version() method.

import java.text.MessageFormat;
import java.util.ResourceBundle;

public class ResourceBundleUtil
{
  // <d58319>
  /**
   * Fetch the version number of this build of the IDL Parser Framework.
   * This method may be called before or after the framework has been
   * initialized. If the framework is inititialized, the version information
   * is extracted from the message properties object; otherwise, it is extracted
   * from the indicated resouce bundle.
   * @return the version number.
   **/
  public static String getVersion ()
  {
    String version = getMessage ("Version.product", getMessage ("Version.number"));
    return version;
  } // getVersion


  //////////////
  // Message-related methods

  public static String getMessage (String key, String... fill)
  {
    String pattern = getResourceBundle().getString(key) ;
    MessageFormat mf = new MessageFormat( pattern ) ;
    return mf.format( fill, new StringBuffer(), null ).toString() ;
  } // getMessage


  /** Register a ResourceBundle.  This file will be searched for
      in the CLASSPATH. */
  public static void registerResourceBundle (ResourceBundle bundle)
  {
    if (bundle != null)
      fBundle = bundle;
  } // registerResourceBundle


  /** Gets the current ResourceBundle.  */
  public static ResourceBundle getResourceBundle ()
  {
    if (fBundle == null) {
      fBundle = ResourceBundle.getBundle("com.sun.tools.corba.ee.idl.idl");
    }
    return fBundle;
  } // getResourceBundle

  private static ResourceBundle  fBundle = null ;
} // class ResourceBundleUtil
