/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2000-2007 Sun Microsystems, Inc. All rights reserved.
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
package com.sun.corba.se.spi.monitoring;

import com.sun.corba.se.spi.monitoring.MonitoredAttributeInfo;
import java.util.*;

/** * @author Hemanth Puttaswamy
 * <p>
 * Monitored Attribute is the interface to represent a Monitorable
 * Attribute. Using this interface, one can get the value of the attribute
 * and set the value if it is a writeable attribute.
 */
public interface MonitoredAttribute {
    ///////////////////////////////////////
    // operations

    /** Gets the Monitored Attribute Info for the attribute.
     * @return The attribute info for this attribute.
     */
    public MonitoredAttributeInfo getAttributeInfo();

    /** Sets the value for the Monitored Attribute if isWritable() is false, the
     * method will throw ILLEGAL Operation exception.
     * <p>
     * Also, the type of 'value' should be same as specified in the 
     * MonitoredAttributeInfo for a particular instance.
     * @param value should be any one of the Basic Java Type Objects which are
     * Long, Double, Float, String, Integer, Short, Character, Byte.
     */
    public void setValue(Object value);

    /** Gets the value of the Monitored Attribute. The value can be obtained
     * from different parts of the module. User may choose to delegate the call
     * to getValue() to other variables.
     * <p>
     * NOTE: It is important to make sure that the type of Object returned in
     * getvalue is same as the one specified in MonitoredAttributeInfo for this
     * attribute.
     * @return The current value for this MonitoredAttribute 
     */
    public Object getValue();

    /** Gets the name of the Monitored Attribute.
     * @return Name of this Attribute 
     */
    public String getName();

    /** If this attribute needs to be cleared, the user needs to implement this
     * method to reset the state to initial state. If the Monitored Attribute
     * doesn't change like for example (ConnectionManager High Water Mark),
     * then clearState() is a No Op.
     */
    public void clearState();
} // end MonitoredAttribute
