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
package corba.cdrstreams;

import java.io.Serializable;
import java.util.Vector;

public class Node implements Serializable
{
    public String value;
    public Vector links;

    public Node() 
    {
        value = "";
        links = new Vector();
    }

    public Node(String value, Vector links)
    {
        this.value = value;
        this.links = links;
    }

    private boolean valueCompare(Node node1, Node node2)
    {
        return node1.value.equals(node2.value);
    }

    // Light equals method
    public boolean equals(Object obj)
    {
        if (obj == null)
            return false;

        if (!(obj instanceof Node))
            return false;

        Node rt = (Node)obj;

        if (!valueCompare(this, rt))
            return false;

        if (this.links.size() != rt.links.size())
            return false;

        for (int i = 0; i < links.size(); i++) {
            Node linkl = (Node)this.links.get(i);
            Node linkr = (Node)rt.links.get(i);

            if (!valueCompare(linkl, linkr))
                return false;
            if (linkl.links.size() != linkr.links.size())
                return false;
        }

        return true;
    }
}

