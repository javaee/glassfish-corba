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

import java.util.*;

/**
 * <p>
 * 
 * @author Hemanth Puttaswamy
 * </p>
 * <p>
 * Monitored AttributeInfo contains the meta information of the Monitored
 * Attribute.
 * </p>
 */
public interface MonitoredAttributeInfo {

  ///////////////////////////////////////
  // operations

/**
 * <p>
 * If the Attribute is writable from ASAdmin then isWritable() will return
 * true.
 * </p>
 * <p>
 * 
 * @return a boolean with true or false
 * </p>
 */
    public boolean isWritable();
/**
 * <p>
 * isStatistic() is true if the attribute is presented as a Statistic.
 * </p>
 * <p>
 * 
 * @return a boolean with true or false
 * </p>
 */
    public boolean isStatistic();
/**
 * <p>
 * Class Type: We will allow only basic class types: 1)Boolean 2)Integer
 * 3)Byte 4)Long 5)Float 6)Double 7)String 8)Character 
 * </p>
 * <p>
 * 
 * @return a Class Type
 * </p>
 */
    public Class type();
/**
 * <p>
 * Get's the description for the Monitored Attribute.
 * </p>
 * <p>
 * 
 * @return a String with description
 * </p>
 */
    public String getDescription();

} // end MonitoredAttributeInfo
