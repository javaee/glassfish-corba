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
 * An object that indicates whether a method had completed running
 * when a <code>SystemException</code> was thrown.
 * <P>
 * The class <code>CompletionStatus</code>
 * contains three <code>CompletionStatus</code> instances, which are constants
 * representing each
 * possible completion status: <code>COMPLETED_MAYBE</code>,
 * <code>COMPLETED_NO</code>, and <code>COMPLETED_YES</code>.
 * It also contains
 * three <code>int</code> members, each a constant corresponding to one of
 * the <code>CompletionStatus</code> instances.  These <code>int</code>
 * members make it possible to use a <code>switch</code> statement.
 * <P>
 * The class also contains two methods:
 * <UL>
 * <LI><code>public int <bold>value</bold>()</code> -- which accesses the
 * <code>value</code> field of a <code>CompletionStatus</code> object
 * <LI><code>public static CompletionStatus
 * <bold>from_int</bold>(int i)</code> --
 * for creating an instance from one of the <code>int</code> members
 * </UL>
 * @version %I, %G
 * @see     org.omg.CORBA.SystemException
 * @since   JDK1.2
 */

// @SuppressWarnings({"serial"})
public final class CompletionStatus implements org.omg.CORBA.portable.IDLEntity
{
/**
 * The constant indicating that a method completed running
 * before a <code>SystemException</code> was thrown.
 */
    public static final int _COMPLETED_YES = 0,

/**
 * The constant indicating that a method had not completed running
 * when a <code>SystemException</code> was thrown.
 */
	_COMPLETED_NO = 1,

/**
 * The constant indicating that it is unknown whether a method had
 * completed running when a <code>SystemException</code> was thrown.
 */
	_COMPLETED_MAYBE = 2;


/**
 * An instance of <code>CompletionStatus</code> initialized with
 * the constant <code>_COMPLETED_YES</code>.
 */
    public static final CompletionStatus COMPLETED_YES   = new CompletionStatus(_COMPLETED_YES);

/**
 * An instance of <code>CompletionStatus</code> initialized with
 * the constant <code>_COMPLETED_NO</code>.
 */
    public static final CompletionStatus COMPLETED_NO    = new CompletionStatus(_COMPLETED_NO);

    /**
 * An instance of <code>CompletionStatus</code> initialized with
 * the constant <code>_COMPLETED_MAYBE</code>.
 */
    public static final CompletionStatus COMPLETED_MAYBE = new CompletionStatus(_COMPLETED_MAYBE);

    /**
 * Retrieves the value of this <code>CompletionStatus</code> object.
 *
 * @return  one of the possible <code>CompletionStatus</code> values:
 *          <code>_COMPLETED_YES</code>, <code>_COMPLETED_NO</code>, or
 *          <code>_COMPLETED_MAYBE</code>
 *
 */
    public int value() { return _value; }

/**
 * Creates a <code>CompletionStatus</code> object from the given <code>int</code>.
 *
 * @param i  one of <code>_COMPLETED_YES</code>, <code>_COMPLETED_NO</code>, or
 *          <code>_COMPLETED_MAYBE</code>
 *
 * @return  one of the possible <code>CompletionStatus</code> objects 
 *          with values:
 *          <code>_COMPLETED_YES</code>, <code>_COMPLETED_NO</code>, or
 *          <code>_COMPLETED_MAYBE</code>
 *
 * @exception org.omg.CORBA.BAD_PARAM  if the argument given is not one of the
 *            <code>int</code> constants defined in <code>CompletionStatus</code>
 */
    public static CompletionStatus from_int(int i)  {
	switch (i) {
	case _COMPLETED_YES:
	    return COMPLETED_YES;
	case _COMPLETED_NO:
	    return COMPLETED_NO;
	case _COMPLETED_MAYBE:
	    return COMPLETED_MAYBE;
	default:
	    throw new org.omg.CORBA.BAD_PARAM();
	}
    }


/**
 * Creates a <code>CompletionStatus</code> object from the given <code>int</code>.
 *
 * @param _value  one of <code>_COMPLETED_YES</code>, <code>_COMPLETED_NO</code>, or
 *          <code>_COMPLETED_MAYBE</code>
 *
 */
    private CompletionStatus(int _value) {
	this._value = _value;
    }

    private int _value;
}
