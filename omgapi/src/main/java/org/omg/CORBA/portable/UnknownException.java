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
 * Licensed Materials - Property of IBM
 * RMI-IIOP v1.0
 * Copyright IBM Corp. 1998 1999  All Rights Reserved
 *
 * US Government Users Restricted Rights - Use, duplication or
 * disclosure restricted by GSA ADP Schedule Contract with IBM Corp.
 */

package org.omg.CORBA.portable;
/**
 * The org.omg.CORBA.portable.UnknownException is used for reporting 
 * unknown exceptions between ties and ORBs and between ORBs and stubs. 
 * It provides a Java representation of an UNKNOWN system exception 
 * that has an UnknownExceptionInfo service context.
 * If the CORBA system exception org.omg.CORBA.portable.UnknownException
 * is thrown, then the stub does one of the following:
 * (1) Translates it to org.omg.CORBA.UNKNOWN.
 * (2) Translates it to the nested exception that the UnknownException contains.
 * (3) Passes it on directly to the user.
 */
public class UnknownException extends org.omg.CORBA.SystemException {
    /**
     * A throwable--the original exception that was wrapped in a CORBA
     * UnknownException.
     */
    public Throwable originalEx;
    /**
     * Constructs an UnknownException object.
     * @param ex a Throwable object--to be wrapped in this exception.
     */
    public UnknownException(Throwable ex) {
        super("", 0, org.omg.CORBA.CompletionStatus.COMPLETED_MAYBE);
        originalEx = ex;
    }
}
