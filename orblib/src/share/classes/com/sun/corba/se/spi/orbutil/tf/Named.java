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
package com.sun.corba.se.spi.orbutil.tf;

/**
 *
 * @author ken_admin
 */
public abstract class Named {
    private final String name ;
    private final String type ;

    // lazy init on first toString() call
    private boolean init = false ;
    private String toString = null ;
    private int hashCode = 0 ;

    protected Named( String type, String name ) {
        this.type = type ;
        this.name = name ;
    }

    private void init() {
        StringBuilder sb = new StringBuilder();
        sb.append(type);
        sb.append( '[' ) ;
        sb.append(name);
        sb.append(']');
        toString = sb.toString();
        hashCode = toString.hashCode() ;
    }

    public final String name() {
        return name;
    }

    @Override
    public final boolean equals(Object obj) {
        if (obj == this) {
            return true ;
        }

        if (obj == null) {
            return false;
        }

        if (!(obj instanceof Named)) {
            return false;
        }

        Named other = (Named)obj;
        return name().equals(other.name());
    }

    @Override
    public synchronized final int hashCode() {
        if (!init) {
            init() ;
        }

        return hashCode ;
    }

    @Override
    public synchronized final String toString() {
        if (!init) {
            init() ;
        }

        return toString() ;
    }

}
