/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright (c) 1997-2018 Oracle and/or its affiliates. All rights reserved.
 *
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common Development
 * and Distribution License("CDDL") (collectively, the "License").  You
 * may not use this file except in compliance with the License.  You can
 * obtain a copy of the License at
 * https://oss.oracle.com/licenses/CDDL+GPL-1.1
 * or LICENSE.txt.  See the License for the specific
 * language governing permissions and limitations under the License.
 *
 * When distributing the software, include this License Header Notice in each
 * file and include the License file at LICENSE.txt.
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

package com.sun.corba.ee.impl.naming.pcosnaming;

import java.io.Serializable;
import org.omg.CosNaming.NameComponent;


/**
 * Class InternalBindingKey implements the necessary wrapper code
 * around the org.omg.CosNaming::NameComponent class to implement the proper
 * equals() method and the hashCode() method for use in a hash table.
 * It computes the hashCode once and stores it, and also precomputes
 * the lengths of the id and kind strings for faster comparison.
 */
public class InternalBindingKey
        implements Serializable
{

    // computed by serialver tool
    private static final long serialVersionUID = -5410796631793704055L;

    public String id;
    public String kind;

    // Default Constructor
    public InternalBindingKey() {}

    // Normal constructor
    public InternalBindingKey(NameComponent n)
    {
        setup(n);
    }

    // Setup the object
    protected void setup(NameComponent n) {
        this.id = n.id;
        this.kind = n.kind;
    }

    // Compare the keys by comparing name's id and kind
    public boolean equals(java.lang.Object o) {
        if (o == null)
            return false;
        if (o instanceof InternalBindingKey) {
            InternalBindingKey that = (InternalBindingKey)o;
            if( this.id != null && that.id != null )
            {
                if (this.id.length() != that.id.length() )
                {
                        return false;
                }
                // If id is set is must be equal
                if (this.id.length() > 0 && this.id.equals(that.id) == false) 
                {
                        return false;
                }
            }
            else
            {
                // If One is Null and the other is not then it's a mismatch
                // So, return false
                if( ( this.id == null && that.id != null )
                ||  ( this.id !=null && that.id == null ) )
                {
                        return false;
                }
            }
            if( this.kind != null && that.kind != null )
            {
                if (this.kind.length() != that.kind.length() )
                {
                        return false;
                }
                // If kind is set it must be equal
                if (this.kind.length() > 0 && this.kind.equals(that.kind) == false) 
                {
                        return false;
                }
            }
            else
            {
                // If One is Null and the other is not then it's a mismatch
                // So, return false
                if( ( this.kind == null && that.kind != null )
                ||  ( this.kind !=null && that.kind == null ) )
                {
                        return false;
                }
            }
            // We have checked all the possibilities, so return true
            return true;
        } else {
            return false;
        }
    }


    // Return precomputed value
    public int hashCode() {
        int hashVal = 0;
        if (this.id.length() > 0)
        {
            hashVal += this.id.hashCode();
        }
        if (this.kind.length() > 0)
        {
            hashVal += this.kind.hashCode();
        }
        return hashVal;
    }
}

