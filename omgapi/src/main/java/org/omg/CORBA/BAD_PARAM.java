/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright (c) 1997-2018 Oracle and/or its affiliates. All rights reserved.
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

package org.omg.CORBA;

/**
 * Exception  thrown
 * when a parameter passed to a call is out of range or 
 * otherwise considered illegal. An ORB may raise this exception 
 * if null values or null pointers are passed to an operation (for 
 * language mappings where the concept of a null pointers or null 
 * values applies). BAD_PARAM can also be raised as a result of a
 * client generating requests with incorrect parameters using the DII. <P>
 * It contains a minor code, which gives more detailed information about
 * what caused the exception, and a completion status. It may also contain
 * a string describing the exception.
 *
 * @see <A href="../../../../guide/idl/jidlExceptions.html">documentation on
 * Java&nbsp;IDL exceptions</A>
 * @see <A href="../../../../guide/idl/jidlExceptions.html#minorcodemeanings">meaning of
 * minor codes</A>
 * @version     1.18, 09/09/97
 * @since       JDK1.2
 */

// @SuppressWarnings({"serial"})
public final class BAD_PARAM extends SystemException {

    /**
     * Constructs a <code>BAD_PARAM</code> exception with a default
     * minor code of 0 and a completion state of COMPLETED_NO.
     */
    public BAD_PARAM() {
        this("");
    }

    /**
     * Constructs a <code>BAD_PARAM</code> exception with the specified detail
     * message, a minor code of 0, and a completion state of COMPLETED_NO.
     *
     * @param s the String containing a detail message describing this 
     *          exception
     */
    public BAD_PARAM(String s) {
        this(s, 0, CompletionStatus.COMPLETED_NO);
    }

    /**
     * Constructs a <code>BAD_PARAM</code> exception with the specified
     * minor code and completion status.
     * @param minor the minor code
     * @param completed the completion status
     */
    public BAD_PARAM(int minor, CompletionStatus completed) {
        this("", minor, completed);
    }

    /**
     * Constructs a <code>BAD_PARAM</code> exception with the specified detail
     * message, minor code, and completion status.
     * A detail message is a <code>String</code> that describes 
     * this particular exception.
     *
     * @param s the <code>String</code> containing a detail message
     * @param minor the minor code
     * @param completed the completion status
     */
    public BAD_PARAM(String s, int minor, CompletionStatus completed) {
        super(s, minor, completed);
    }
}
