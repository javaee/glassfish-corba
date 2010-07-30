/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
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

    public void write( OutputStream os ) {
        os.write_string( name ) ;
        os.write_long( weight );
        os.write_long( endpoints.size() ) ;
        for (SocketInfo si : endpoints) {
            si.write( os ) ;
        }
    }

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

    public List<SocketInfo> endpoints() {
        return endpoints ;
    }

    public String name() {
        return name;
    }

    public int weight() {
        return weight;
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
