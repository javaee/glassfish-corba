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

package com.sun.corba.se.impl.orbutil.codegen;

import java.util.List ;

/** Represents a node in the AST used to generate code.  All nodes support the
 * dynamic attribute facility.
 *
 * @author Ken Cavanaugh
 */
public interface Node extends AttributedObject {
    /** Return the Node that contains (and created) this Node.
    */
    Node parent() ;

    /** Return the unique ID of this node.  This starts at 1 and is incremented
     * for each new Node that is created.
     */
    int id() ;

    /** Set the parent to a new value.  Should only be called inside NodeBase.
     */
    void parent( Node node ) ;

    /** Return the first ancestor of this node of the given type, if any.
     * Throws IllegalArgumentException if not found.
     */
    <T extends Node> T getAncestor( Class<T> type ) ;

    /** Make a deep copy of this node.  If nn = n.copy(), then 
     * n.parent() == nn.parent(), which also means that the 
     * parent is NOT copied.
     */
    <T extends Node> T copy( Class<T> cls ) ;
    
    /** Copy setting a new parent in the result.
     */
    <T extends Node> T copy( Node newParent, Class<T> cls ) ;

    /** Accept the visitor and allow it to perform actions on this Node.
     */
    void accept( Visitor visitor ) ;
}
