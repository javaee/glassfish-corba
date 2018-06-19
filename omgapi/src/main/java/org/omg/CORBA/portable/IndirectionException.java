/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright (c) 1997-2018 Oracle and/or its affiliates. All rights reserved.
 * Copyright (c) 1998 IBM Corp. All rights reserved.
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

package org.omg.CORBA.portable;

import org.omg.CORBA.SystemException;
/**
 * The Indirection exception is a Java specific system exception. 
 * It is thrown when the ORB's input stream is called to demarshal 
 * a value that is encoded as an indirection that is in the process 
 * of being demarshaled. This can occur when the ORB input stream 
 * calls the ValueHandler to demarshal an RMI value whose state 
 * contains a recursive reference to itself. Because the top-level 
 * ValueHandler.read_value() call has not yet returned a value, 
 * the ORB input stream's indirection table does not contain an entry 
 * for an object with the stream offset specified by the indirection 
 * tag. The stream offset is returned in the exception's offset field.
 * @see org.omg.CORBA_2_3.portable.InputStream
 * @see org.omg.CORBA_2_3.portable.OuputStream
 */
public class IndirectionException extends SystemException {

    /**
     * Points to the stream's offset.
     */
    public int offset;

    /**
     * Creates an IndirectionException with the right offset value.
     * The stream offset is returned in the exception's offset field.
     * This exception is constructed and thrown during reading 
     * recursively defined values off of a stream.
     *
     * @param offset the stream offset where recursion is detected.
     */
    public IndirectionException(int offset){
        super("", 0, org.omg.CORBA.CompletionStatus.COMPLETED_MAYBE);
        this.offset = offset;
    }
}
