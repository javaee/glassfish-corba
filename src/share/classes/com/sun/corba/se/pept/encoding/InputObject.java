/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2001-2007 Sun Microsystems, Inc. All rights reserved.
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

package com.sun.corba.se.pept.encoding;

import java.io.IOException;

import com.sun.corba.se.pept.protocol.MessageMediator;

/**
 * <p> An <code>InputObject</code> is the interface used by the
 * presentation block to get programming language typed data from data
 *  encoded in a message. </p>
 *
 * <p> The implementation of an <code>InputObject</code> contains the
 * encoded data.  When the presentation block asks for data the
 * implementation of <code>InputObject</code> is responsible for converting
 * the encoded representation of the data to the types expected by the
 * programming language.</p>
 *
 * <p>A particular <em>encoding</em> would subclass
 * <code>InputObject</code>.  The subclass would provide methods to get
 * the data types appropriate to the presentation block (e.g., simple
 * types such as int or boolean, all the way to any type derived from
 * <code>java.io.Serializable</code>.).</p>
 *
 * <p>Note: the protocol block may also use the <code>InputObject</code> to
 * obtain header metadata.</p>
 *
 * @author Harold Carr
*/
public interface InputObject 
{
    public void setMessageMediator(MessageMediator messageMediator);

    public MessageMediator getMessageMediator();

    public void close() throws IOException;
}

// End of file.

