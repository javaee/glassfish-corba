/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1999-2007 Sun Microsystems, Inc. All rights reserved.
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
/* @(#)Graph.java	1.4 99/06/07 */
/*
 * Licensed Materials - Property of IBM
 * RMI-IIOP v1.0
 * Copyright IBM Corp. 1998 1999  All Rights Reserved
 *
 * US Government Users Restricted Rights - Use, duplication or
 * disclosure restricted by GSA ADP Schedule Contract with IBM Corp.
 */

package javax.rmi.CORBA.serialization;

// Class from Sun


import java.io.*;

public class Graph implements Serializable {

    public final String CONSTANT_STRING = "This is a constant string.";
    public final int CONSTANT_INT = 10;
    public final BitSet CONSTANT_BITSET = new BitSet(32);
    private String _list = null;
    private Graph _next = null;
    private BitSet _bitset = null;
    transient String t_string = "This is a transient string.";
    transient int t_int = 1111;
    transient BitSet t_bitset = new BitSet(32);

    public Graph(String data, Graph next) {
        this._list = data;
	this._bitset = new BitSet(64);
	this._bitset.set(10);this._bitset.set(20);
	this._bitset.set(30);this._bitset.set(40);
	this._bitset.set(50);this._bitset.set(60);
        this._next = next;
    }

    public String data() {
        return this._list;
    }

    public Graph next() {
        return this._next;
    }

    public void next(Graph next) {
        this._next = next;
    }

    public boolean equals(Graph o) {
	try{
	    Graph g = (Graph)o;
	    return ((_list.equals(g._list)) && 
		    (_next.equals(g._next)) &&
		    (_bitset.equals(g._bitset)));
	}
	catch(Throwable t){
	    return false;
	}
    }

    public String toString() {
        StringBuffer result = new StringBuffer("{ ");
        for(Graph list = this; list != null; list = list.next()) {
            result.append(list.data()).append(" ");
        }
        return result.append("}").toString();
    }

	
}
