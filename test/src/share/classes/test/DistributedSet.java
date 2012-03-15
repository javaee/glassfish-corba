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

/*
 * Licensed Materials - Property of IBM
 * RMI-IIOP v1.0
 * Copyright IBM Corp. 1998 1999  All Rights Reserved
 *
 * US Government Users Restricted Rights - Use, duplication or
 * disclosure restricted by GSA ADP Schedule Contract with IBM Corp.
 */

package test;

import java.rmi.Remote;
import java.rmi.RemoteException;

/**
 * DistributedSet is a remote interface which supports a distributed
 * collection of sets, all of which coordinate with each other to maintain,
 * in each instance, a list of all the currently active sets.
 *
 * @version     1.0, 5/13/98
 * @author      Bryan Atsatt
 */
public interface DistributedSet extends Remote {

    public static final String PING_RESPONSE = "Pong";

    /*
     * See if this set is still active. Returns PING_RESPONSE.
     */
    public String ping (String fromSetName) throws RemoteException;

    /*
     * Get this set's name.
     */
    public String getName () throws RemoteException;
    
    /*
     * Notify this set that the specified set is joining. If the set
     * already is 'known' by this instance, this call performs no
     * action.
     */
    public void join (String setName, DistributedSet set) throws RemoteException;
    
    /*
     * Notify this set that the specified set is leaving.
     */
    public void leave (String setName) throws RemoteException;
    
    /*
     * Broadcast a message to all sets.
     */
    public void broadcastMessage (String message) throws RemoteException;
    
    /*
     * Send a message to specified set.
     */
    public void sendMessage (DistributedSet toSet, String message) throws RemoteException;
    
    /*
     * Receive a message from another set.
     */
    public void receiveMessage (String message, String fromSetName) throws RemoteException;
    
    /*
     * Return the number of currently active sets, _excluding_ 
     * this instance.
     */
    public int countSets () throws RemoteException;
    
    /*
     * List the names of all the active sets, _excluding_ this
     * instance.
     */
    public String[] listSetNames () throws RemoteException;
    
    /*
     * Get a set instance by name. Returns null if not found.
     */
    public DistributedSet getSet (String setName) throws RemoteException;
}

