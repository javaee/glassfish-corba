/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright (c) 1997-2018 Oracle and/or its affiliates. All rights reserved.
 * Copyright (c) 1998-1999 IBM Corp. All rights reserved.
 *
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common Development
 * and Distribution License("CDDL") (collectively, the "License").  You
 * may not use this file except in compliance with the License.  You can
 * obtain a copy of the License at
 * https://oss.oracle.com/licenses/CDDL+GPL-1.1
 * or LICENSE.txt.  See the License for the specific
 * language governing permissions and limitations under the License.
 *
 * When distributing the software, include this License Header Notice in each
 * file and include the License file at LICENSE.txt.
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

// GameSurrogate is a local object that handles all communications
// with the remote server.  I only put this in one place for
// development convenience.  I also got really mimimalistic (i.e. lazy)
// with exception handling.  You'd want to do more here.


package ibmspace.client;

import ibmspace.common.*;

import javax.naming.InitialContext;
import javax.rmi.PortableRemoteObject;
import java.awt.event.ActionListener;
import java.lang.reflect.Array;
import java.rmi.RMISecurityManager;
import java.rmi.RemoteException;
import java.util.Hashtable;
import java.util.Vector;

public class GameSurrogate
{
    private SpaceConquest           fSpaceConquest;
    private GameView                fGameView;
    private Planet[]                fPlanets;
    private PlanetView[]            fPlanetViews;
    private BudgetSummary[]         fPlanetBudgets;
    private int                     fGalaxySize;
    private ActionSource            fActionSource;
    private int                     fNumberOfPlanets = 0;

    private ShipDesign              fLatestDesign = null;
    private String                  fLatestDesignName = null;

    private Vector                  fJournies;
    private Vector                  fMessages = null;

    private String                  fName = "";

    private InitialContext context ;

    public GameSurrogate (String name)
    {
        fName = name;
        fActionSource = new ActionSource ();
        fGameView = null;
        fPlanets = null;
        fPlanetViews = null;
        fPlanetBudgets = null;
        fSpaceConquest = lookupServer ();
        fJournies = new Vector ();
    }

    public SpaceConquest lookupServer ()
    {
        SpaceConquest server = null;

        // Pass system properties with -D option of java command, e.g.
        // -Djava.naming.factory.initial=<name of factory to use>
        // -Djava.naming.provider.url=iiop://<hostname>

        try
            {
                if ( System.getSecurityManager() == null ) {
                    System.setSecurityManager (new RMISecurityManager());
                }

                Hashtable<String,?> env = new Hashtable<String,String> ();

                context = new InitialContext (env);
                Object o = context.lookup ("SpaceConquest");
                server = (SpaceConquest)PortableRemoteObject.narrow (o,SpaceConquest.class);
                System.out.println ("Connected to server.");
            }
        catch (Exception e)
            {
                System.out.println ("Problem looking up server!");
                System.out.println ("exception: " + e.getMessage ());
                e.printStackTrace ();
            }

        return server;
    }

    public void joinGame ()
    {
        System.out.println ("Joining game...");
        fGameView = null;

        try
            {
                fGameView = fSpaceConquest.joinGame (fName);
                if ( fGameView != null ) fGameView.test ();
                updatePlanetMap ();
            }
        catch (RemoteException e)
            {
                System.out.println ("Problem joining game!");
                fGameView = null;
            }

        if ( fGameView == null ) {
            System.out.println ("Problem joining game: null GameView returned!");
        }
    }

    public void updatePlanetMap ()
    {
        if ( fSpaceConquest == null || fGameView == null ) return;

        int i = 0;
        int numPlanets = 0;

        try
            {
                if ( fNumberOfPlanets == 0 ) {
                    System.out.println ("Initializing planet map.");

                    fPlanets = fSpaceConquest.getGalaxyMap ();


                    /*
                      int length = fSpaceConquest.getNumberOfPlanets ();
                      fPlanets = new Planet [length];
                      for ( int p=0; p<length; p++ ) {
                      fPlanets[p] = fSpaceConquest.getPlanet (p);
                      }
                    */
        

                    fNumberOfPlanets = Array.getLength (fPlanets);
                    fPlanetViews = new PlanetView [fNumberOfPlanets];
                    fPlanetBudgets = new BudgetSummary [fNumberOfPlanets];
                    for (i=0; i<fNumberOfPlanets; i++) {
                        fPlanetViews[i] = null;
                        fPlanetBudgets[i] = null;
                    }

                }

                for (i=0; i<fNumberOfPlanets; i++) {
                    PlanetView planet = fGameView.getPlanet (fPlanets[i].getID());
                    String name = planet.getName ();
                    fPlanetBudgets[i] = fGameView.getPlanetBudget (fPlanets[i].getID());
                    if ( planet != null ) {
                        fPlanetViews[i] = planet;
                    }

                }

            }
        catch (RemoteException e)
            {
                System.out.println ("Problem updating planet map!");
                System.out.println ("RemoteException: " + e.getMessage ());
                e.printStackTrace ();
            }
    }

    public int getNumberOfPlanets ()
    {
        if ( fGameView == null ) return 0;
        return Array.getLength (fPlanets);
    }

    public void takeTurn ()
    {
        if ( fGameView == null ) return;

        try
            {
                fMessages = fGameView.takeTurn ();
            }
        catch (RemoteException e)
            {
                System.out.println ("Problem taking turn!");
            }

        updatePlanetMap ();
        fActionSource.notifyListeners (this, "New Turn");
    }

    public Vector getMessages ()
    {
        return fMessages;
    }



    //
    // Planet Management
    //

    public ID getHome ()
    {
        if ( fGameView == null ) return null;

        try
            {
                return fGameView.getHome ();
            }
        catch (RemoteException e)
            {
                System.out.println ("Problem getting main budget summary!");
                return null;
            }
    }


    public PlanetView getPlanet (int i)
    {
        if ( fGameView == null ) return null;
        return fPlanetViews[i];
    }

    public PlanetView getPlanet (ID planetID)
    {
        int index = getPlanetIndex (planetID);
        if ( index != -1 )
            return fPlanetViews[index];
        else
            return null;
    }


    public BudgetSummary getMainBudget ()
    {
        if ( fGameView == null ) return null;

        try
            {
                return fGameView.getMainBudget ();
            }
        catch (RemoteException e)
            {
                System.out.println ("Problem getting main budget summary!");
                return null;
            }
    }


    public BudgetSummary getTechBudget ()
    {
        if ( fGameView == null ) return null;

        try
            {
                return fGameView.getTechBudget ();
            }
        catch (RemoteException e)
            {
                System.out.println ("Problem getting tect budget summary!");
                return null;
            }
    }

    public BudgetSummary getPlanetBudget (ID planetID)
    {
        int index = getPlanetIndex (planetID);
        if ( index != -1 )
            return fPlanetBudgets [index];
        else
            return null;
    }

    public void setPlanetBudget (ID planetID, BudgetSummary budget)
    {
        int index = getPlanetIndex (planetID);
        if ( index != -1 )
            fPlanetBudgets[index] = budget;
    }

    public void pushPlanetBudgetData ()
    {
        try
            {
                for (int i=0; i<fNumberOfPlanets; i++) {
                    PlanetView planet = fPlanetViews[i];
                    BudgetSummary budget = fPlanetBudgets[i];
                    if ( budget != null && planet != null ) {
                        fGameView.setPlanetBudget (planet.getID(), budget);
                    }
                }
            }
        catch (RemoteException e)
            {
                System.out.println ("Problem writing planet budget data!");
            }
    }




    public void setMainBudget (BudgetSummary budget)
    {
        if ( fGameView == null ) return;

        try
            {
                fGameView.setMainBudget (budget);
            }
        catch (RemoteException e)
            {
                System.out.println ("Problem setting main budget summary!");
            }

    }

    public void setTechBudget (BudgetSummary budget)
    {
        if ( fGameView == null ) return;

        try
            {
                fGameView.setTechBudget (budget);
            }
        catch (RemoteException e)
            {
                System.out.println ("Problem setting tech budget summary!");
            }

    }


    public TechProfile getTechProfile ()
    {
        if ( fGameView == null ) return null;

        try
            {
                return fGameView.getTechProfile ();
            }
        catch (RemoteException e)
            {
                System.out.println ("Problem getting tech profile!");
                return null;
            }
    }

    public long getShipSavings ()
    {
        if ( fGameView == null ) return 0;

        try
            {
                return fGameView.getShipSavings ();
            }
        catch (RemoteException e)
            {
                System.out.println ("Problem getting ship savings!");
                return 0;
            }
    }

    public long getShipMetal ()
    {
        if ( fGameView == null ) return 0;

        try
            {
                return fGameView.getShipMetal ();
            }
        catch (RemoteException e)
            {
                System.out.println ("Problem getting ship metal!");
                return 0;
            }
    }

    public long getIncome ()
    {
        if ( fGameView == null ) return 0;

        try
            {
                return fGameView.getIncome ();
            }
        catch (RemoteException e)
            {
                System.out.println ("Problem getting income!");
                return 0;
            }
    }

  
    public long getCalls ()
    {
        if ( fGameView == null ) return 0;

        try
            {
                return fGameView.getCalls ();
            }
        catch (RemoteException e)
            {
                System.out.println ("Problem getting number of IIOP calls!");
                return 0;
            }
    }

    private boolean isNewerTechnology ()
    {
        if ( fLatestDesign == null ) return true;

        TechProfile current = getTechProfile ();
        TechProfile shipTech = fLatestDesign.getTechProfile ();
        if ( current.getRange() > shipTech.getRange() ) return true;
        if ( current.getSpeed() > shipTech.getSpeed() ) return true;
        if ( current.getWeapons() > shipTech.getWeapons() ) return true;
        if ( current.getShields() > shipTech.getShields() ) return true;
        if ( current.getMini() > shipTech.getMini() ) return true;
        return false;
    }


    public void buildFleet (ShipDesign design, int number, ID station)
    {
        try
            {
                fGameView.buildFleet (design, number, station);
            }
        catch (RemoteException e)
            {
                System.out.println ("Problem building ships!");
            }
    }

    public void scrapFleet (ID fleetID)
    {
        try
            {
                fGameView.scrapFleet (fleetID);
            }
        catch (RemoteException e)
            {
                System.out.println ("Problem scrapping fleet!");
            }
    }

    public ID[] getFleetsAt (ID planetID)
    {
        try
            {
                return fGameView.getFleetsAt (planetID);
            }
        catch (RemoteException e)
            {
                System.out.println ("Problem getting list of fleets at planet!");
                return null;
            }
    }

    public void sendFleet (ID fleetID, ID destinationID)
    {
        try
            {
                fGameView.sendFleet (fleetID, destinationID);
            }
        catch (RemoteException e)
            {
                System.out.println ("Problem getting list of fleets at planet!");
            }
    }


    public Fleet getFleet (ID fleetID)
    {
        try
            {
                return fGameView.getFleet (fleetID);
            }
        catch (RemoteException e)
            {
                System.out.println ("Problem getting fleet summary!");
                return null;
            }
    }


    public void updateJourneys ()
    {
        try
            {
                if ( fJournies == null ) System.out.println ("fJournies is null");
                if ( fGameView == null ) System.out.println ("fGameView is null");
                fJournies.removeAllElements ();
                ID[] journies = fGameView.getAllJournies ();
                if ( journies != null ) {
                    for (int i=0; i<Array.getLength(journies); i++) {
                        if ( journies[i] == null ) System.out.println ("journies[i] is null");
                        System.out.println ("getting journey");
                        Journey journey = fGameView.getJourney (journies[i]);
                        fJournies.addElement (journey);
                    }
                }
            }
        catch (RemoteException e)
            {
                System.out.println ("Problem updating journies!");
            }
    }

    public int getNumberOfJournies ()
    {
        return fJournies.size ();
    }

    public Journey getJourney (int i)
    {
        return (Journey)fJournies.elementAt (i);
    }

    public int getPlanetIndex (ID planetID)
    {
        if ( planetID != null ) {
            for (int i=0; i<fNumberOfPlanets; i++) {
                if (fPlanetViews[i].getID().equals(planetID)) {
                    return i;
                }
            }
        }
        return -1;
    }

    public void abandonPlanet (ID planetID)
    {
        try
            {
                fGameView.abandonPlanet (planetID);
            }
        catch (RemoteException e)
            {
            }
    }

    public void surrender ()
    {
        try
            {
                fGameView.quit ();
            }
        catch (RemoteException e)
            {
            }
    }

    public void addActionListener (ActionListener listener)
    {
        fActionSource.addActionListener (listener);
    }

    public void removeActionListener (ActionListener listener)
    {
        fActionSource.removeActionListener (listener);
    }

}
