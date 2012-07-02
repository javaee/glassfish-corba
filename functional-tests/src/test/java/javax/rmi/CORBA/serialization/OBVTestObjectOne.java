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
/* @(#)OBVTestObjectOne.java    1.7 99/06/07 */
/*
 * Licensed Materials - Property of IBM
 * RMI-IIOP v1.0
 * Copyright IBM Corp. 1998 1999  All Rights Reserved
 *
 * US Government Users Restricted Rights - Use, duplication or
 * disclosure restricted by GSA ADP Schedule Contract with IBM Corp.
 */

package javax.rmi.CORBA.serialization;

public abstract class OBVTestObjectOne implements org.omg.CORBA.portable.StreamableValue
{
  // We mark these as transients just so we can make sure during the test
  // that only by using the Streamable interface could this class be serialized.
  protected transient int fInt = (int)0;
  protected transient long fLong = (long)0;
  protected transient float fFloat = (float)0;
  protected transient double fDouble = (double)0;
  protected transient String fString = null;
    
  private static String[] _truncatable_ids = {
    OBVTestObjectOneHelper.id ()
  };
    
  public String[] _truncatable_ids() {
    return _truncatable_ids;
    }

  public void _read (org.omg.CORBA.portable.InputStream istream)
    {
    this.fInt = istream.read_long ();
    this.fLong = istream.read_longlong ();
    this.fFloat = istream.read_float ();
    this.fDouble = istream.read_double ();
    this.fString = istream.read_string ();
    }

  public void _write (org.omg.CORBA.portable.OutputStream ostream)
    {
    ostream.write_long (this.fInt);
    ostream.write_longlong (this.fLong);
    ostream.write_float (this.fFloat);
    ostream.write_double (this.fDouble);
    ostream.write_string (this.fString);
            }

  public org.omg.CORBA.TypeCode _type ()
    {
    return OBVTestObjectOneHelper.type ();
    }
}
