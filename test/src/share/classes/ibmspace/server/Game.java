/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright (c) 1997-2018 Oracle and/or its affiliates. All rights reserved.
 * Copyright (c) 1998 IBM Corp. All rights reserved.
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
 * file and include the License file at packager/legal/LICENSE.txt.
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
//
// The game is the game model, that is the game logic and state.  It is
// exercised by multiple remote clients acting through game view servers.
// So clients never access the game object directly.  The game uses many
// other objects that represents portions of the game, e.g. fleet, battle,
// galaxy, planet and population.  In addition to illustrating use of
// rmi-iiop, I also wrote this sample to model good OO design.  But the
// sample was cranked out in a relatively short time so please excuse any
// obvious short-cuts that were taken.

package ibmspace.server;

import ibmspace.common.GameProfile;
import ibmspace.common.Planet;
import ibmspace.common.Fleet;
import ibmspace.common.Journey;
import java.util.Vector;
import ibmspace.common.ID;

public class Game implements java.io.Serializable
{
    private boolean           fGameStarted;
    private int               fNumberOfPlayers;
    private int               fPlayersAdded;
    private Vector            fPlayers;
    private Galaxy            fGalaxy;
    private Vector            fSettlements;
    private Vector            fFleets;
    private Vector            fJournies;
    private int               fTurnsTaken;
    private int               fCalls;

    public Game (int numPlayers)
    {
        fGameStarted = false;
        fGalaxy = new Galaxy (4,4);
        fPlayers = new Vector ();
        fSettlements = new Vector ();
        fFleets = new Vector ();
        fJournies = new Vector ();
        fNumberOfPlayers = numPlayers;
        fPlayersAdded = 0;
        fTurnsTaken = 0;
        fCalls = 0;
    }

    public synchronized boolean isGameStarted ()
    {
        return fGameStarted;
    }

    public boolean areFleetListsIdentical (PlanetImpl home)
    {
        return ( fFleets == home.getFleetList() );
    }

    public synchronized Planet[] createGalaxyMap ()
    {
        Vector pv = fGalaxy.getPlanets ();
        Planet[] planets = new Planet [pv.size()];

        for ( int i=0; i<pv.size(); i++) {
            planets[i] = (Planet)pv.elementAt (i);
        }

        return planets;
    }

    public synchronized int getNumberOfPlanets ()
    {
        return fGalaxy.getNumberOfPlanets ();
    }

    public synchronized void addPlayer (Player player)
    {
        fPlayersAdded++;

        if ( fPlayersAdded == fNumberOfPlayers ) {
            fGameStarted = true;
        }

        fPlayers.addElement (player);
        PlanetImpl home = fGalaxy.createHomeWorldFor (player);
        player.setHome (home);
        settlePlanet (player, home, 50000);
    }

    private synchronized void doOnceForAllPlayers ()
    {
        growPopulations ();
        moveFleets ();
        fuelFleets ();
        runBattleSimulations ();
        createNewSettlements ();
        eliminateLosers ();
        fTurnsTaken = 0;
    }

    private synchronized void waitForAllPlayers (Player player)
    {
        try
            {
                if ( fTurnsTaken < fNumberOfPlayers ) {
                    wait ();
                } else {
                    doOnceForAllPlayers ();
                    notifyAll ();
                }
            }
        catch (Exception e)
            {
            }
    }

    public synchronized void takeTurn (Player player)
    {
        long income = getIncomeFor (player);
        player.getBudget().invest (income);
        fTurnsTaken++;
        waitForAllPlayers (player);
    }

    private synchronized void growPopulations ()
    {
        for (int i=0; i<fSettlements.size(); i++) {
            Settlement settlement = (Settlement)fSettlements.elementAt (i);
            settlement.growPopulation ();
        }
    }

    private synchronized void moveFleets ()
    {
        for (int i=fJournies.size()-1; i>=0; i-- ) {
            JourneyImpl journey = (JourneyImpl)fJournies.elementAt (i);
            journey.moveFleet ();
            if ( journey.isComplete() ) {
                FleetImpl fleet = journey.getFleet ();
                Planet planet = journey.getDestination ();
                Player player = fleet.getOwner ();
                String m =  "Your fleet of " + fleet.toString() + " arrived at " +
                    planet.getName();
                player.addMessage (m);
                fJournies.removeElement (journey);
            }
        }
    }

    private synchronized void fuelFleets ()
    {
        Vector planets = fGalaxy.getPlanets ();
        for (int i=0; i<planets.size(); i++) {
            PlanetImpl planet = (PlanetImpl)planets.elementAt (i);
            planet.fuelAllFleets ();
        }
    }

    private synchronized void runBattleSimulations ()
    {
        Vector planets = fGalaxy.getPlanets ();
        for (int i=0; i<planets.size(); i++) {
            PlanetImpl planet = (PlanetImpl)planets.elementAt (i);
            Settlement s = planet.getSettlement ();
            planet.runBattleSimulation ();
            if ( s != null && planet.getSettlement() == null ) {
                fSettlements.removeElement (s);
            }
        }
    }

    private synchronized void createNewSettlements ()
    {
        Vector planets = fGalaxy.getPlanets ();
        for (int i=0; i<planets.size(); i++) {
            System.out.print (".");
            PlanetImpl planet = (PlanetImpl)planets.elementAt (i);
            if ( planet.shouldColonize() ) {
                Player player = planet.getOwner ();
                String m = "You have colonized " + planet.getName();
                player.addMessage (m);
                settlePlanet (planet.getOwner(), planet, 10);
            }
        }
    }

    public synchronized void eliminateLosers ()
    {
        // Currently, players must manually quit when they determine
        // that all hope is lost.  In the future, the game can
        // automatically eliminite obvious losers here.

        /*
          for (int i=fPlayers.size()-1; i>=0; i--) {
          Player player = (Player)fPlayers.elementAt (i);
          if ( getIncomeFor (player) == 0 ) {
          System.out.println ("Eliminates player " + player.getName());
          player.addMessage ("You have been eliminated from the game.");
          fPlayers.removeElement (player);
          fNumberOfPlayers--;
          for (int j=0; j<fPlayers.size(); j++) {
          Player p = (Player)fPlayers.elementAt (i);
          System.out.println ("Notifying " + p.getName());
          String m = player.getName() + " has been eliminated from the game.";
          p.addMessage (m);
          }
          System.out.println ("done eliminating");
          }
          }
        */
    }

    public synchronized void eliminatePlayer (Player player)
    {
        Vector planets = fGalaxy.getPlanets ();
        for (int i=0; i<planets.size(); i++) {
            PlanetImpl planet = (PlanetImpl)planets.elementAt (i);
            if ( planet != null && planet.getOwner() == player ) {
                planet.setOwner (null);
                planet.setSettlement (null);
            }
        }

        for (int i=fSettlements.size()-1; i >= 0; i--) {
            Settlement s = (Settlement)fSettlements.elementAt (i);
            if ( s != null & s.getOwner() == player ) {
                fSettlements.removeElement (s);
            }
        }

        for (int i=fFleets.size()-1; i>=0; i--) {
            FleetImpl f = (FleetImpl)fFleets.elementAt (i);
            if ( f != null && f.getOwner() == player ) {
                scrapFleet (f);
                fFleets.removeElement (f);
            }
        }

        String name = player.getName ();
        fPlayers.removeElement (player);
        fNumberOfPlayers--;

        for (int i=0; i<fPlayers.size(); i++) {
            Player p = (Player)fPlayers.elementAt (i);
            if ( p != null ) {
                p.addMessage (name + " has been eliminated from the game.");
                if ( fNumberOfPlayers == 1 ) {
                    p.addMessage ("Congratulations!  You are ruler of the galaxy!");
                }
            }
        }

        if ( fTurnsTaken == fNumberOfPlayers ) {
            doOnceForAllPlayers ();
            notifyAll ();
        }
    }

    public synchronized void addFleet (FleetImpl fleet, PlanetImpl station)
    {
        fFleets.addElement (fleet);
        station.stationFleet (fleet);
        fleet.setStation (station);
        fleet.setJourney (null);
    }

    public synchronized void scrapFleet (FleetImpl fleet)
    {
        PlanetImpl station = fleet.getStation ();
        Player owner = fleet.getOwner ();

        if ( station != null ) {
            if ( station.getOwner() == owner ) {
                owner.addShipMetal (fleet.getScrapMetal());
            } else {
                station.addMetal (fleet.getScrapMetal());
            }
            station.removeFleet (fleet);
            if ( station.getSettlement() == null && station.getNumberOfFleets() == 0 ) {
                station.setOwner (null);
            }
        } else {
            JourneyImpl journey = fleet.getJourney ();
            fJournies.removeElement (fleet);
        }
    }

    public synchronized JourneyImpl sendFleet (FleetImpl fleet, PlanetImpl toPlanet)
    {
        PlanetImpl origin = fleet.getStation ();
        JourneyImpl journey = new JourneyImpl (fleet, origin, toPlanet);
        fleet.setJourney (journey);
        fJournies.addElement (journey);
        return journey;
    }

    public synchronized void cancelJourneyFor (ID fleetID)
    {
        JourneyImpl journey = getJourney (fleetID);
        FleetImpl fleet = getFleet (fleetID);
        if ( journey != null && fleet != null ) {
            System.out.println ("cancelled journey");
            fJournies.removeElement (journey);
            fleet.setJourney (null);
        }
    }

    public synchronized PlanetImpl getPlanet (ID planetID)
    {
        return fGalaxy.getPlanet (planetID);
    }

    public synchronized FleetImpl getFleet (ID fleetID)
    {
        for (int i=0; i<fFleets.size(); i++) {
            FleetImpl fleet = (FleetImpl)fFleets.elementAt (i);
            if ( fleetID.identifies(fleet) ) {
                return fleet;
            }
        }
        return null;
    }

    public synchronized JourneyImpl getJourney (ID journeyOrFleetID)
    {
        // Look for journey ID first

        ID journeyID = journeyOrFleetID;

        for (int i=0; i<fJournies.size(); i++) {
            JourneyImpl journey = (JourneyImpl)fJournies.elementAt (i);
            if ( journeyID.identifies(journey) ) {
                return journey;
            }
        }

        // Look for fleet ID next

        ID fleetID = journeyOrFleetID;

        for (int i=0; i<fJournies.size(); i++) {
            JourneyImpl journey = (JourneyImpl)fJournies.elementAt (i);
            if ( fleetID.identifies(journey.getFleet()) ) {
                return journey;
            }
        }

        return null;
    }

    public synchronized ID[] getJourniesFor (Player player)
    {
        Vector journies = new Vector ();

        for (int i=0; i<fJournies.size(); i++) {
            JourneyImpl journey = (JourneyImpl)fJournies.elementAt (i);
            if ( journey.getFleet().getOwner() == player ) {
                journies.addElement (journey.getID());
            }
        }

        int numJournies = journies.size ();
        ID[] ids = null;

        if ( numJournies > 0 ) {
            ids = new ID [journies.size()];
            for (int i=0; i<journies.size(); i++) {
                ids[i] = (ID)journies.elementAt (i);
            }
        }

        return ids;
    }


    public synchronized void killSettlementAt (PlanetImpl planet)
    {
        Budget budget = planet.getOwner().getBudget ();
        BudgetItem item = budget.findBudgetItem (planet.getName());
        budget.removeBudgetItem (item);
        planet.setSettlement (null);
        if ( planet.getNumberOfFleets() == 0 ) {
            planet.setOwner (null);
        }
    }

    public synchronized long getIncomeFor (Player player)
    {
        long income = 0;

        for (int i=0; i<fSettlements.size(); i++) {
            Settlement settlement = (Settlement)fSettlements.elementAt (i);
            if (settlement.getOwner() == player) {
                income += settlement.getIncome ();
            }
        }
        return income;
    }

    private synchronized void settlePlanet (Player player, PlanetImpl planet, long population)
    {
        Settlement settlement = new Settlement (player, planet, population);

        planet.setOwner (player);
        planet.setSettlement (settlement);
        fSettlements.addElement (settlement);

        Budget budget = player.getBudget ();
        budget.addBudgetItem (new BudgetItem(settlement.getBudget(),10));
        budget.balance ();
    }

    public synchronized void logCall ()
    {
        // Used to keep track of calls through server objects.  Only main
        // client entry points through server objects (SpaceConquestServer
        // and GameViewServer) should call this method.

        fCalls++;
    }

    public synchronized long getCalls ()
    {
        fCalls++;
        return fCalls;
    }


}
