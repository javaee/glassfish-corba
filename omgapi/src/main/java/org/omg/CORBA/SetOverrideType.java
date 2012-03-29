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
 * The mapping of a CORBA <code>enum</code> tagging
 * <code>SET_OVERRIDE</code> and <code>ADD_OVERRIDE</code>, which
 * indicate whether policies should replace the 
 * existing policies of an <code>Object</code> or be added to them.
 * <P>
 * The method {@link omg.org.CORBA.Object#_set_policy_override} takes
 * either <code>SetOverrideType.SET_OVERRIDE</code> or 
 * <code>SetOverrideType.ADD_OVERRIDE</code> as its second argument.
 * The method <code>_set_policy_override</code>
 * creates a new <code>Object</code> initialized with the
 * <code>Policy</code> objects supplied as the first argument.  If the
 * second argument is <code>ADD_OVERRIDE</code>, the new policies
 * are added to those of the <code>Object</code> instance that is
 * calling the <code>_set_policy_override</code> method.  If
 * <code>SET_OVERRIDE</code> is given instead, the existing policies
 * are replaced with the given ones.
 *
 * @author OMG
 * @version 1.20 07/27/07
 * @since   JDK1.2
 */

// @SuppressWarnings({"serial"})
public class SetOverrideType implements org.omg.CORBA.portable.IDLEntity {
    
    /**
     * The <code>int</code> constant for the enum value SET_OVERRIDE.
     */
    public static final int _SET_OVERRIDE = 0;

    /**
     * The <code>int</code> constant for the enum value ADD_OVERRIDE.
     */
    public static final int _ADD_OVERRIDE = 1;

    /**
     * The <code>SetOverrideType</code> constant for the enum value SET_OVERRIDE.
     */
    public static final SetOverrideType SET_OVERRIDE = new SetOverrideType(_SET_OVERRIDE);

    /**
     * The <code>SetOverrideType</code> constant for the enum value ADD_OVERRIDE.
     */
    public static final SetOverrideType ADD_OVERRIDE = new SetOverrideType(_ADD_OVERRIDE);

    /**
     * Retrieves the value of this <code>SetOverrideType</code> instance.
     *
     * @return  the <code>int</code> for this <code>SetOverrideType</code> instance.
     */
    public int value() {
        return _value;
    }

    /**
     * Converts the given <code>int</code> to the corresponding
     * <code>SetOverrideType</code> instance.
     *
     * @param  i the <code>int</code> to convert; must be either
     *         <code>SetOverrideType._SET_OVERRIDE</code> or
     *         <code>SetOverrideType._ADD_OVERRIDE</code> 
     * @return  the <code>SetOverrideType</code> instance whose value
     *       matches the given <code>int</code>
     * @exception  BAD_PARAM  if the given <code>int</code> does not
     *       match the value of
     *       any <code>SetOverrideType</code> instance
     */
    public static SetOverrideType from_int(int i)
    {
        switch (i) {
        case _SET_OVERRIDE:
            return SET_OVERRIDE;
        case _ADD_OVERRIDE:
            return ADD_OVERRIDE;
        default:
            throw new org.omg.CORBA.BAD_PARAM();
        }
    }

    /**
     * Constructs a <code>SetOverrideType</code> instance from an
     * <code>int</code>.
     * @param _value must be either <code>SET_OVERRIDE</code> or 
     *        <code>ADD_OVERRIDE</code>
     */
    protected SetOverrideType(int _value){
        this._value = _value;
    }

    /**
     * The field containing the value for this <code>SetOverrideType</code>
     * object.
     *
     */
    private int _value;
}
