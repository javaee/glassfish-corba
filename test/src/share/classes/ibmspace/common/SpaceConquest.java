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
/* @(#)SpaceConquest.java	1.4 99/06/07 */
/*
 * Licensed Materials - Property of IBM
 * RMI-IIOP v1.0
 * Copyright IBM Corp. 1998 1999  All Rights Reserved
 *
 * US Government Users Restricted Rights - Use, duplication or
 * disclosure restricted by GSA ADP Schedule Contract with IBM Corp.
 */

//----------------------------------------------------------------------------
// Change History:
//
// 9/98 rtw created
// 2/99 rtw added quitGame method for purposes of server unexport() call
//----------------------------------------------------------------------------

// SpaceConquest is where you start.  It represents a game.  A subclass
// of this (in this project's case ibmspace.server.SpaceConquestServer)
// must be created to start a game.  Then through this interface a player
// can join or quit the game, and can also get some basic information
// about the game before or after joining.   Note that the server side
// of this game will never eliminate a player.  Therefore, either the
// player must choose to exit through the user interface, or the client
// must implement "decision logic" to force the user to quit when the
// user has no hope left of winning.  In either case, the client calls
// quitGame to quit.

// My idea is that eventually there will be another server for creating
// and enumerating games and that an associated client user interface
// would let you view a list of games and some information about these
// games before joining.  You can even visually see the galaxy before
// joining (and getGalaxyMap allows for this).


package ibmspace.common;

import java.rmi.Remote;
import java.rmi.RemoteException;

public interface SpaceConquest extends Remote
{
    GameView      joinGame (String playerName) throws RemoteException;
    void          quitGame (GameView gameView) throws RemoteException;

    Planet[]      getGalaxyMap () throws RemoteException;
    int           getNumberOfPlanets () throws RemoteException;
    Planet        getPlanet (int index) throws RemoteException;
} 
