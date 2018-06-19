/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright (c) 1997-2018 Oracle and/or its affiliates. All rights reserved.
 * Copyright (c) 1997 IBM Corp. All rights reserved.
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
 * file and include the License file at packager/legal/LICENSE.txt.
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

class TokenBuffer
{
  private final int DEFAULT_SIZE = 10;

  private int   _size      = 0;
  private Token _buffer [] = null;
  private int   _currPos   = -1;

  TokenBuffer ()
  {
    _size    = DEFAULT_SIZE;
    _buffer  = new Token[_size];
    _currPos = -1;
  } // ctor

  TokenBuffer (int size) throws Exception
  {
    _size    = size;   // _size == 0 is legal, but useless and problematic
    _buffer  = new Token[_size];
    _currPos = -1;
  } // ctor

  /** Inserts a token at the head of the buffer. */
  void insert (Token token)
  {
    // _size == 0 ==> ArithmeticException: divide by zero
    _currPos = ++_currPos % _size;
    _buffer [_currPos] = token;
  }

  /** Returns the token residing "i" elements from the head of the buffer. */
  Token lookBack (int i)
  {
    // Beware: i > _size ==> idx < 0 ==> ArrayOutOfBoundsException
    return _buffer [(_currPos - i) >= 0 ? _currPos - i : _currPos - i + _size];
  }

  /** Return the token most recently inserted into the buffer (i.e., the head of the buffer.) */
  Token current ()
  {
    // Beware: _buffer empty || _size == 0 ==> ArrayOutOfBoundsException
    return _buffer [_currPos];
  }
}   // class TokenBuffer


/*==================================================================================
  DATE<AUTHOR>   ACTION
  ----------------------------------------------------------------------------------
  11aug1997<daz> Initial version completed.  Buffer used to maintain history of
                 comments extracted from source file during parse.
  ==================================================================================*/

