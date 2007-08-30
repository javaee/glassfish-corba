/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2007 Sun Microsystems, Inc. All rights reserved.
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
package com.sun.corba.se.impl.monitoring;

import java.util.Map;
import java.util.HashMap;
import java.util.Collection;
import java.util.Iterator;

import com.sun.corba.se.spi.monitoring.MonitoredObject;
import com.sun.corba.se.spi.monitoring.MonitoredAttribute;

public class MonitoredObjectImpl implements MonitoredObject {
    private final String name;
    private final String description;
   
    // List of all child Monitored Objects
    private Map<String,MonitoredObject> children = 
	new HashMap<String,MonitoredObject>();

    // All the Attributes of this Monitored Object instance
    private Map<String,MonitoredAttribute> attributes = 
	new HashMap<String,MonitoredAttribute>();

    private MonitoredObject parent = null;


    // Constructor
    MonitoredObjectImpl( String name, String description ) {
        this.name = name;
        this.description = description;
    }

    public MonitoredObject getChild( String name ) {
        synchronized( this ) {
            return children.get( name );
        }
    } 

    public Collection getChildren( ) { 
        synchronized( this ) {
            return children.values();
        }
    }

    public void addChild( MonitoredObject m ) {
        if (m != null){
            synchronized( this ) {
                children.put( m.getName(), m);
                m.setParent( this );
            }
        }         
    }

    public void removeChild( String name ) {
        if (name != null){
            synchronized( this ) {
                children.remove( name );
            }
        }         
    }

    public synchronized MonitoredObject getParent( ) {
       return parent;
    }

    public synchronized void setParent( MonitoredObject p ) {
        parent = p;
    }

    public MonitoredAttribute getAttribute( String name ) {
        synchronized( this ) {
            return attributes.get( name );
        }
    } 

    public Collection<MonitoredAttribute> getAttributes( ) { 
        synchronized( this ) {
            return attributes.values();
        }
    }

    public void addAttribute( MonitoredAttribute value ) {
        if (value != null) {
            synchronized( this ) {
                attributes.put( value.getName(), value );
            }
        }
    }

    public void removeAttribute( String name ) {
        if (name != null) {
            synchronized( this ) {
                attributes.remove( name );
            }
        }
    }

    /**
     * calls clearState() on all the registered children MonitoredObjects and 
     * MonitoredAttributes. 
     */
    public void clearState( ) {
	synchronized( this ) {
	    for (MonitoredAttribute ma : attributes.values()) {
		ma.clearState() ;
	    }
	    for (MonitoredObject mo : children.values()) {
		mo.clearState() ;
	    }
        }
    }

    public String getName( ) {
        return name;
    }

    public String getDescription( ) {
        return description;
    }
}
        
    

   

