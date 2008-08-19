/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2007-2007 Sun Microsystems, Inc. All rights reserved.
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

package com.sun.corba.se.impl.orbutil.jmx ;

import javax.management.Attribute ;
import javax.management.AttributeList ;
import javax.management.MBeanException ;
import javax.management.InvalidAttributeValueException ;
import javax.management.AttributeNotFoundException ;
import javax.management.ReflectionException ;
import javax.management.MBeanInfo ;
import javax.management.DynamicMBean ;
import javax.management.MBeanAttributeInfo ;
import javax.management.MBeanConstructorInfo ;
import javax.management.MBeanOperationInfo ;
import javax.management.MBeanNotificationInfo ;

import com.sun.corba.se.spi.orbutil.jmx.ManagedObjectManager ;
import com.sun.corba.se.spi.orbutil.jmx.ManagedObject ;
import com.sun.corba.se.spi.orbutil.jmx.ManagedData ;
import com.sun.corba.se.spi.orbutil.jmx.ManagedAttribute ;
import com.sun.corba.se.spi.orbutil.jmx.ManagedOperation ;
import com.sun.corba.se.spi.orbutil.jmx.InheritedAttribute ;
import com.sun.corba.se.spi.orbutil.jmx.InheritedAttributes ;
import com.sun.corba.se.spi.orbutil.jmx.IncludeSubclass ;

class DynamicMBeanImpl implements DynamicMBean {
    // Just delegates to the skeleton which does all of the work.
    // This allows a skeleton to be computed once for a particular class and then
    // cached.  Creating an mbean then simply requires finding a skeleton and
    // creating an instance of this class with the appropriate object.
    private DynamicMBeanSkeleton skel ;
    private Object target ;

    public DynamicMBeanImpl( DynamicMBeanSkeleton skel, Object obj ) {
	this.skel = skel ;
	this.target = obj ;
    }

    public Object getAttribute(String attribute) throws AttributeNotFoundException,
	MBeanException, ReflectionException {

	return skel.getAttribute( target, attribute ) ;
    }
    
    public void setAttribute(Attribute attribute) throws AttributeNotFoundException,
	InvalidAttributeValueException, MBeanException, ReflectionException  {

	skel.setAttribute( target, attribute ) ;
    }
        
    public AttributeList getAttributes(String[] attributes) {
	return skel.getAttributes( target, attributes ) ;
    }
        
    public AttributeList setAttributes(AttributeList attributes) {
	return skel.setAttributes( target, attributes ) ;
    }
    
    public Object invoke(String actionName, Object params[], String signature[])
	throws MBeanException, ReflectionException  {

	return skel.invoke( target, actionName, params, signature ) ;
    }
    
    public MBeanInfo getMBeanInfo() {
	return skel.getMBeanInfo() ;
    }
}
