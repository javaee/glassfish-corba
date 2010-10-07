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

//----------------------------------------------------------------------------
// Change History:
//
// 9/98 created  rtw
//----------------------------------------------------------------------------

// This server is bound with the name server and is the first stop
// for all clients joining the game.  Each client uses this server
// to (1) get the basic map of the galaxy sans specific details they
// learn about planets later during play, and (2) get a player-
// specific game view server to represent a player-specific view of
// the game.  This models the fact that each player only sees the
// parts of the galaxy they experience through play.  So, for example,
// a player only sees the current temperature, etc, of a planer when
// he/she has ships stationed there or has a colony there.

package ibmspace.server;

import ibmspace.common.SpaceConquest;
import ibmspace.common.GameView;
import ibmspace.common.Planet;
import java.rmi.RemoteException;
import java.util.Properties;
import java.util.Vector;
import java.lang.reflect.Array;
import javax.rmi.PortableRemoteObject;
import javax.naming.InitialContext;
import java.rmi.RMISecurityManager;

import ibmspace.common.*;

public class SpaceConquestServer extends PortableRemoteObject implements SpaceConquest
{
    private Game      fGame;
    private int       fRequiredPlayers = 0;
    private int       fNumberOfPlayers = 0;
    private Vector    fViews;

    public SpaceConquestServer (int numPlayers) throws RemoteException
    {
	fGame = new Game (numPlayers);
	fViews = new Vector ();
	fRequiredPlayers = numPlayers;
    }

    public SpaceConquestServer () throws RemoteException
    {
	fGame = new Game (1);
	fViews = new Vector ();
	fRequiredPlayers = 1;
    }

    private synchronized void waitForAllPlayers ()
    {
	try
	    {
		if ( fNumberOfPlayers < fRequiredPlayers ) {
		    wait ();
		} else {
		    notifyAll ();
		}
	    }
	catch (InterruptedException e)
	    {
	    }
    }

    public synchronized GameView  joinGame (String playerName) throws RemoteException
    {
	if ( fGame.isGameStarted() ) {
	    // Probably should throw an exception here
	    // This is to prevent players from joining after the required
	    // number of players have joined and the game has started.
	    System.out.println (playerName + " tried to join started game");
	    return null;
	}

	fGame.logCall ();
	double idealTemp = PlanetImpl.RandomTemp ();
	double idealGravity = PlanetImpl.RandomGravity ();
	Player player = new Player (playerName, idealTemp, idealGravity);
	fGame.addPlayer (player);
	fNumberOfPlayers++;

	GameView view;

	if ( fRequiredPlayers == 1 ) {
	    view = new GameViewImpl (fGame, player);
	} else {
	    view = new GameViewServer (fGame, player);
	}

	fViews.addElement (view);
	waitForAllPlayers ();
	return view;
    }

    public void quitGame (GameView gameView) throws RemoteException
    {
	gameView.quit ();
	fNumberOfPlayers--;
	if ( fNumberOfPlayers == 0 ) PortableRemoteObject.unexportObject (this);
    }

    public synchronized Planet[] getGalaxyMap () throws RemoteException
    {
	Planet[] planets = fGame.createGalaxyMap ();
	return planets;
    }

    public synchronized int getNumberOfPlanets () throws RemoteException
    {
	Planet[] planets = getGalaxyMap ();
	return Array.getLength (planets);
    }

    public synchronized Planet getPlanet (int index) throws RemoteException
    {
	Planet[] planets = getGalaxyMap ();
	return planets[index];
    }

    private static InitialContext context ;

    public static void main (String[] args)
    {
	int numPlayers = 1;

	if ( Array.getLength (args) != 0 ) {
	    numPlayers = (Integer.valueOf (args[0])).intValue();
	}

	try
	    {
		if ( System.getSecurityManager() == null ) {
		    System.setSecurityManager (new RMISecurityManager());
		}

		SpaceConquestServer obj = new SpaceConquestServer (numPlayers);

		// Pass system properties with -D option of java command, e.g.
		// -Djava.naming.factory.initial=com.sun.jndi.cosnaming.CNCtxFactory

		context = new InitialContext ();
		context.rebind ("SpaceConquest", obj);
		System.out.println ("SpaceConquest server bound in registry");
	    }
	catch (Exception e)
	    {
		System.out.println ("SpaceConquest Server Exception: " + e.getMessage());
		e.printStackTrace ();
	    }
    }


}
