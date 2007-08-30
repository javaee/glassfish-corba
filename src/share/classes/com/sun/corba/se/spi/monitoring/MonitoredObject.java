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


import com.sun.corba.se.spi.monitoring.MonitoredAttribute;
import java.util.*;
import java.util.Collection;

/**
 * <p>
 *
 * @author Hemanth Puttaswamy
 * </p>
 * <p>
 * Monitored Object provides an Hierarchichal view of the ORB Monitoring
 * System. It can contain multiple children and a single parent. Each
 * Monitored Object may also contain Multiple Monitored Attributes.
 * </p>
 */
public interface MonitoredObject {

  ///////////////////////////////////////
  // operations
/**
 * <p>
 * Gets the name of this MonitoredObject
 * </p><p>
 * 
 * @return a String with name of this Monitored Object 
 * </p>
 */
    public String getName();
/**
 * <p>
 * Gets the description of MonitoredObject
 * </p><p>
 * 
 * @return a String with Monitored Object Description.
 * </p>
 */
    public String getDescription();
/**
 * <p>
 * This method will add a child Monitored Object to this Monitored Object. 
 * </p>
 * <p>
 * </p>
 */
    public void addChild( MonitoredObject m );   
/**
 * <p>
 * This method will remove child Monitored Object identified by the given name 
 * </p>
 * <p>
 * @param name of the ChildMonitored Object
 * </p>
 */
    public void removeChild( String name );   

/**
 * <p>
 * Gets the child MonitoredObject associated with this MonitoredObject
 * instance using name as the key. The name should be fully qualified name
 * like orb.connectionmanager
 * </p>
 * <p>
 * 
 * @return a MonitoredObject identified by the given name
 * </p>
 * <p>
 * @param name of the ChildMonitored Object
 * </p>
 */
    public MonitoredObject getChild(String name);
/**
 * <p>
 * Gets all the Children registered under this instance of Monitored
 * Object. 
 * </p>
 * <p>
 * 
 * @return Collection of immediate Children associated with this MonitoredObject.
 * </p>
 */
    public Collection getChildren();
/**
 * <p>
 * Sets the parent for this Monitored Object.
 * </p>
 * <p>
 * </p>
 */
    public void setParent( MonitoredObject m );
/**
 * <p>
 * There will be only one parent for an instance of MontoredObject, this
 * call gets parent and returns null if the Monitored Object is the root.
 * </p>
 * <p>
 * 
 * @return a MonitoredObject which is a Parent of this Monitored Object instance
 * </p>
 */
    public MonitoredObject getParent();

/**
 * <p>
 * Adds the attribute with the given name.
 * </p>
 * <p>
 * 
 * </p>
 * <p>
 * @param value is the MonitoredAttribute which will be set as one of the
 * attribute of this MonitoredObject.
 * </p>
 */
    public void addAttribute(MonitoredAttribute value);
/**
 * <p>
 * Removes the attribute with the given name.
 * </p>
 * <p>
 * 
 * </p>
 * <p>
 * @param name is the MonitoredAttribute name
 * </p>
 */
    public void removeAttribute(String name);
  
/**
 * <p>
 * Gets the Monitored Object registered by the given name
 * </p>
 * 
 * <p>
 * @return a MonitoredAttribute identified by the given name 
 * </p>
 * <p>
 * @param name of the attribute
 * </p>
 */
    public MonitoredAttribute getAttribute(String name);
/**
 * <p>
 * Gets all the Monitored Attributes for this Monitored Objects. It doesn't
 * include the Child Monitored Object, that needs to be traversed using
 * getChild() or getChildren() call.
 * </p>
 * <p>
 * 
 * @return Collection of all the Attributes for this MonitoredObject
 * </p>
 */
    public Collection getAttributes();
/**
 * <p>
 * Clears the state of all the Monitored Attributes associated with the
 * Monitored Object. It will also clear the state on all it's child
 * Monitored Object. The call to clearState will be initiated from
 * CORBAMBean.startMonitoring() call.
 * </p>
 * 
 */
    public void clearState();

} // end MonitoredObject
