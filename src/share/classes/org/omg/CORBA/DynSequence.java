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


package org.omg.CORBA;

/** 
 * The representation of a <code>DynAny</code> object that is associated
 * with an IDL sequence.
 * @deprecated Use the new <a href="../DynamicAny/DynSequence.html">DynSequence</a> instead
 */
// @Deprecated
public interface DynSequence extends org.omg.CORBA.Object, org.omg.CORBA.DynAny
{

    /**
     * Returns the length of the sequence represented by this
     * <code>DynFixed</code> object.
     *
     * @return the length of the sequence
     */
    public int length();

    /**
     * Sets the length of the sequence represented by this
     * <code>DynFixed</code> object to the given argument.
     *
     * @param arg the length of the sequence
     */
    public void length(int arg);

    /**
     * Returns the value of every element in this sequence.
     *
     * @return an array of <code>Any</code> objects containing the values in
	 *         the sequence
	 * @see #set_elements
     */
    public org.omg.CORBA.Any[] get_elements();

    /**
     * Sets the values of all elements in this sequence with the given
	 * array.
     *
     * @param value the array of <code>Any</code> objects to be set
     * @exception InvalidSeq if the array of values is bad
	 * @see #get_elements
     */
    public void set_elements(org.omg.CORBA.Any[] value)
        throws org.omg.CORBA.DynAnyPackage.InvalidSeq;
}
