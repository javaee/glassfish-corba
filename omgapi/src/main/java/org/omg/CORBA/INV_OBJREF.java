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

package org.omg.CORBA;

/**
 * This exception indicates that an object reference is internally 
 * malformed. For example, the repository ID may have incorrect 
 * syntax or the addressing information may be invalid. This 
 * exception is raised by ORB::string_to_object if the passed 
 * string does not decode correctly. An ORB may choose to detect 
 * calls via nil references (but is not obliged to do detect them). 
 * <tt>INV_OBJREF</tt> is used to indicate this.<P>
 * It contains a minor code, which gives more detailed information about
 * what caused the exception, and a completion status. It may also contain
 * a string describing the exception.
 * <P>
 * See the section <A href="../../../../guide/idl/jidlExceptions.html#minorcodemeanings">Minor
 * Code Meanings</A> to see the minor codes for this exception.
 *
 * @see <A href="../../../../guide/idl/jidlExceptions.html">documentation on
 * Java&nbsp;IDL exceptions</A>
 * @version     1.17 09/09/97
 * @since       JDK1.2
 */

public final class INV_OBJREF extends SystemException {
    /**
     * Constructs an <code>INV_OBJREF</code> exception with a default
     * minor code of 0 and a completion state of COMPLETED_NO.
     */
    public INV_OBJREF() {
        this("");
    }

    /**
     * Constructs an <code>INV_OBJREF</code> exception with the specified detail
     * message, a minor code of 0, and a completion state of COMPLETED_NO.
     * @param s the String containing a detail message
     */
    public INV_OBJREF(String s) {
        this(s, 0, CompletionStatus.COMPLETED_NO);
    }

    /**
     * Constructs an <code>INV_OBJREF</code> exception with the specified
     * minor code and completion status.
     * @param minor the minor code
     * @param completed a <code>CompletionStatus</code> instance indicating
     *                  the completion status
     */
    public INV_OBJREF(int minor, CompletionStatus completed) {
        this("", minor, completed);
    }

    /**
     * Constructs an <code>INV_OBJREF</code> exception with the specified detail
     * message, minor code, and completion status.
     * A detail message is a String that describes this particular exception.
     * @param s the String containing a detail message
     * @param minor the minor code
     * @param completed a <code>CompletionStatus</code> instance indicating
     *                  the completion status
     */
    public INV_OBJREF(String s, int minor, CompletionStatus completed) {
        super(s, minor, completed);
    }
}
