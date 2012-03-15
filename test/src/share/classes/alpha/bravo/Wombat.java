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
/* @(#)Wombat.java      1.5 99/06/07 */
/*
 * Licensed Materials - Property of IBM
 * RMI-IIOP v1.0
 * Copyright IBM Corp. 1998 1999  All Rights Reserved
 *
 * US Government Users Restricted Rights - Use, duplication or
 * disclosure restricted by GSA ADP Schedule Contract with IBM Corp.
 */

// 5.4 RemoteInterface unit test
// Steve Newberry, IBM JTC Hursley, UK. Apr98

/*--Notes. See section 5.4-----------------------------------------------------
  5.4.1   passRemote uses java.rmi.Remote as a parameter and return value
  5.4.2   Wombat extends omega.Wallaby which is itself a Remote interface
  5.4.3.1 foo is an int read-write property
  5.4.3.2 URL is a String read-only property
  5.4.3.3 boo is a boolean read-write property
  5.4.3.4 attribute name pre-mangling. See 5.4.3.1-3
          ambiguous attribute name chirp, BLINT_CONSTANT;
  5.4.4   methods chirp and buzz
  5.4.5   constants BLEAT_CONSTANT, BLINT_CONSTANT
 ----------------------------------------------------------------------------*/

package alpha.bravo;

public interface Wombat extends java.rmi.Remote,
                                omega.Wallaby {
    String  BLEAT_CONSTANT = "bleat";
    int     BLINT_CONSTANT = 1;
    void    chirp(int x)       throws java.rmi.RemoteException;
    void    buzz()             throws java.rmi.RemoteException,
    omega.MammalOverload;
    int     getFoo()           throws java.rmi.RemoteException;
    void    setFoo(int x)      throws java.rmi.RemoteException;
    String  getURL()           throws java.rmi.RemoteException;
    boolean isBoo()            throws java.rmi.RemoteException;
    void    setBoo(boolean b)  throws java.rmi.RemoteException;
    void    passRemote()       throws java.rmi.RemoteException;
    java.rmi.Remote
        passRemote( java.rmi.Remote r )
        throws java.rmi.RemoteException;
    int     getChirp()         throws java.rmi.RemoteException;
    boolean isBLINT_CONSTANT() throws java.rmi.RemoteException;
}
