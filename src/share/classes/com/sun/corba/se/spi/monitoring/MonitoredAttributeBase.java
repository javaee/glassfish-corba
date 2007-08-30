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
 *  A Convenient class provided to help users extend and implement only 
 *  getValue(), if there is no need to clear the state and the attribute is not 
 *  writable.
 * 
 * </p>
 */
public abstract class MonitoredAttributeBase implements MonitoredAttribute {
    String name;
    MonitoredAttributeInfo attributeInfo;
    /**
     * Constructor.
     */
    public MonitoredAttributeBase( String name, MonitoredAttributeInfo info ) {
        this.name = name;
        this.attributeInfo = info;
    }

    
    /**
     * A Package Private Constructor for internal use only.
     */
    MonitoredAttributeBase( String name ) {
        this.name = name;
    }


    /**
     * A Package Private convenience method for setting MonitoredAttributeInfo
     * for this Monitored Attribute.
     */
    void setMonitoredAttributeInfo( MonitoredAttributeInfo info ) {
        this.attributeInfo = info;
    }

    /**
     *  If the concrete class decides not to provide the implementation of this
     *  method, then it's OK. Some of the  examples where we may decide to not 
     *  provide the implementation is the connection state. Irrespective of 
     *  the call to clearState, the connection state will be showing the 
     *  currect state of the connection.
     *  NOTE: This method is only used to clear the Monitored Attribute state, 
     *  not the real state of the system itself. 
     */
    public void clearState( ) {
    }

    /**
     *  This method should be implemented by the concrete class.
     */
    public abstract Object getValue( );

    /**
     *  This method should be implemented by the concrete class only if the 
     *  attribute is writable. If the attribute is not writable and if this 
     *  method called, it will result in an IllegalStateException.
     */ 
    public void setValue( Object value ) {
        if( !attributeInfo.isWritable() ) {
            throw new IllegalStateException( 
                "The Attribute " + name + " is not Writable..." );
        }
        throw new IllegalStateException( 
            "The method implementation is not provided for the attribute " + 
            name );
    }

    
    /**
     *  Gets the MonitoredAttributeInfo for the attribute.
     */
    public MonitoredAttributeInfo getAttributeInfo( ) {
        return attributeInfo;
    }

    /**
     * Gets the name of the attribute.
     */
    public String getName( ) {
        return name;
    } 
} // end MonitoredAttributeBase



