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

package com.sun.corba.se.spi.folb;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import org.omg.CORBA_2_3.portable.InputStream;
import org.omg.CORBA_2_3.portable.OutputStream;

/**
 *
 * @author ken
 */
public class ClusterInstanceInfo {
    private final String name ;
    private final int weight ;
    private final List<SocketInfo> endpoints ;

    public ClusterInstanceInfo( InputStream is ) {
        name = is.read_string() ;
        weight = is.read_long() ;
        int size = is.read_long() ;
        List<SocketInfo> elist = new ArrayList<SocketInfo>( size ) ;
        for (int ctr = 0; ctr<size; ctr++) {
            elist.add( new SocketInfo(is)) ;
        }
        endpoints = Collections.unmodifiableList(elist) ;
    }

    public ClusterInstanceInfo(String name, int weight,
        List<SocketInfo> endpoints) {

        this.name = name;
        this.weight = weight;
        this.endpoints = Collections.unmodifiableList( endpoints ) ;
    }

    public List<SocketInfo> endpoints() { return endpoints ; }
    public String name() { return name; }
    public int weight() { return weight; }

    public void write( OutputStream os ) {
        os.write_string( name ) ;
        os.write_long( weight );
        os.write_long( endpoints.size() ) ;
        for (SocketInfo si : endpoints) {
            si.write( os ) ;
        }
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder() ;
        sb.append( "ClusterInstanceInfo[" ) ;
        sb.append( "name=" ) ;
        sb.append( name ) ;
        sb.append( " weight=" ) ;
        sb.append( weight ) ;
        sb.append( " endpoints=" ) ;
        sb.append( endpoints.toString() ) ;
        sb.append( "]" ) ;
        return sb.toString() ;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null) {
            return false;
        }

        if (getClass() != obj.getClass()) {
            return false;
        }

        final ClusterInstanceInfo other = (ClusterInstanceInfo) obj;

        if ((this.name == null) ?
            (other.name() != null) :
            !this.name.equals(other.name())) {

            return false;
        }

        if (this.weight != other.weight()) {
            return false;
        }

        if (this.endpoints != other.endpoints() &&
           (this.endpoints == null ||
            !this.endpoints.equals(other.endpoints()))) {
            return false;
        }

        return true;
    }

    @Override
    public int hashCode() {
        int hash = 3;
        hash = 79 * hash + (this.name != null ? this.name.hashCode() : 0);
        hash = 79 * hash + this.weight;
        hash = 79 * hash + (this.endpoints != null ? this.endpoints.hashCode() : 0);
        return hash;
    }
}
