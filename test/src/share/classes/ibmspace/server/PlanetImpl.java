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
/* @(#)PlanetImpl.java  1.4 99/06/07 */
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
// 9/98     rtw   created
// 1/29/99  rtw   changed to inherit from ibmspace.common.Fleet
//----------------------------------------------------------------------------


package ibmspace.server;

import java.awt.Point;
import java.util.Vector;
import ibmspace.common.ID;
import ibmspace.common.Planet;
import ibmspace.common.Fleet;
import java.lang.reflect.Array;

public class PlanetImpl implements Planet, java.io.Serializable
{
    //
    // Some static helpers for computing random
    // temperature, gravity and metal...
    //

    private static double     MIN_TEMP = -200;
    private static double     MAX_TEMP = +200;
    private static double     MIN_GRAV = 0.5;
    private static double     MAX_GRAV = 3.0;
    private static long       MAX_METAL = 40000;

    public static double RandomTemp ()
    {
        return Math.random() * (MAX_TEMP-MIN_TEMP) + MIN_TEMP;
    }

    public static double RandomGravity ()
    {
        return Math.random() * (MAX_GRAV-MIN_GRAV) + MIN_GRAV;
    }

    public static long RandomMetal ()
    {
        return (long)(Math.random() * MAX_METAL);
    }


    //
    // Private Data Members
    //

    private ID          fID;
    private String      fName;
    private Point       fCoordinates;
    private double      fGravity;
    private double      fTemp;
    private long        fMetal;
    private Player      fOwner;
    private Settlement  fSettlement;
    private Vector      fFleetsInOrbit;
    private Vector      fFleetsOnSurface;

  //
  // Helper
  //


    public static int pixelsForDistance (int distance)
    {
        return 50 * distance;
    }

    //
    // Constructor
    //

    public PlanetImpl (String name, double temp, double gravity, long metal)
    {
        fID = new ID ();
        fName = name;
        fCoordinates = new Point (0,0);
        fTemp = temp;
        fGravity = gravity;
        fMetal = metal;
        fOwner = null;
        fSettlement = null;
        fFleetsOnSurface = new Vector ();
        fFleetsInOrbit = new Vector ();
    }


    //
    // Planet Interface Methods
    //

    public Vector getFleetList ()
    {
        return fFleetsOnSurface;
    }


    public ID getID ()
    {
        return fID;
    }

    public String getName ()
    {
        return fName;
    }

    public Point getCoordinates ()
    {
        return fCoordinates;
    }

    public boolean hasSatelites ()
    {
        for (int i=0; i<fFleetsOnSurface.size(); i++) {
            FleetImpl fleet = (FleetImpl)fFleetsOnSurface.elementAt (i);
            if ( fleet.isSatelite() )
                return true;
        }
        return false;
    }


    //
    // PlanetImpl Methods
    //

    public double getGravity ()
    {
        return fGravity;
    }

    public double getTemp ()
    {
        return fTemp;
    }

    public long getMetal ()
    {
        return fMetal;
    }

    public Player getOwner ()
    {
        return fOwner;
    }

    public Settlement getSettlement ()
    {
        return fSettlement;
    }

  
    //
    // Setters
    //

    public void setCoordinates (Point coordinates)
    {
        fCoordinates = coordinates;
    }

    public void setTemp (double temp)
    {
        fTemp = temp;
    }

    public void setGravity (double gravity)
    {
        fGravity = gravity;
    }

    public void setMetal (long metal)
    {
        fMetal = metal;
    }

    public void setOwner (Player owner)
    {
        fOwner = owner;
    }

    public void setSettlement (Settlement settlement)
    {
        fSettlement = settlement;
    }

    //
    // Operations
    //

    public void addMetal (long metal)
    {
        fMetal += metal;
    }

    public void removeMetal (long metal)
    {
        fMetal -= metal;
        if ( fMetal < 0 )
            fMetal = 0;
    }

    public int distanceTo (PlanetImpl other)
    {
        double xx = Math.pow( (double)(fCoordinates.x - other.fCoordinates.x), 2);
        double yy = Math.pow( (double)(fCoordinates.y - other.fCoordinates.y), 2);
        double rawDist = Math.sqrt ( xx + yy );
        return (int) (rawDist / pixelsForDistance(1));
    }

    public void stationFleet (Fleet fleet)
    {
        fFleetsOnSurface.addElement (fleet);
    }

    public void acceptOrbit (FleetImpl fleet)
    {
        fFleetsInOrbit.addElement (fleet);
    }

    public FleetImpl[] getFleets ()
    {
        FleetImpl[] fleets = new FleetImpl [fFleetsOnSurface.size()];
        for (int i=0; i<fFleetsOnSurface.size(); i++) {
            fleets[i] = (FleetImpl)fFleetsOnSurface.elementAt (i);
        }
        return fleets;
    }

    public void removeAllFleets ()
    {
        fFleetsInOrbit = new Vector ();
        fFleetsOnSurface = new Vector ();
    }

    public boolean shouldColonize ()
    {
        if ( fSettlement == null ) {
            for (int i=0; i<fFleetsOnSurface.size(); i++) {
                FleetImpl fleet = (FleetImpl)fFleetsOnSurface.elementAt (i);
                if ( fleet.isColonyShip() ) {
                    return true;
                }
            }
        }
        return false;
    }


    public void removeFleet (FleetImpl fleet)
    {
        for (int i=0; i<fFleetsOnSurface.size(); i++) {
            FleetImpl compare = (FleetImpl)fFleetsOnSurface.elementAt (i);
            if ( fleet == compare ) {
                fFleetsOnSurface.removeElementAt (i);
                return;
            }
        }
    }

    public void fuelAllFleets ()
    {
        if ( fSettlement != null ) {
            for (int i=0; i<fFleetsOnSurface.size(); i++) {
                FleetImpl fleet = (FleetImpl)fFleetsOnSurface.elementAt (i);
                if ( fleet.getOwner() == fOwner )
                    fleet.replentishFuel ();
            }
        }
    }

    public double getSuitabilityFor (Player player)
    {
        double temp = player.getRelativeTempFor (fTemp);
        double gravity = player.getRelativeGravityFor (fGravity);

        double gmaxdiff = MAX_GRAV-MIN_GRAV;
        double gdiff = Math.abs (gravity-1.0);
        double gsuit = 1.0 - (gdiff/gmaxdiff);

        double tmaxdiff = MAX_TEMP- MIN_TEMP;
        double tdiff = Math.abs (temp-72.0);
        double tsuit = 1.0 - (tdiff/tmaxdiff);

        return gsuit * tsuit;
    }


    public int getNumberOfFleets ()
    {
        return fFleetsOnSurface.size();
    }

    private void orbitStationedFleets ()
    {
        for (int i=0; i<fFleetsOnSurface.size(); i++) {
            FleetImpl fleet = (FleetImpl)fFleetsOnSurface.elementAt (i);
            fFleetsInOrbit.addElement (fleet);
            fFleetsOnSurface.removeElement (fleet);
        }
    }

    private void acceptFleets ()
    {
        fFleetsOnSurface = new Vector ();

        for (int i=0; i<fFleetsInOrbit.size(); i++) {
            FleetImpl fleet = (FleetImpl)fFleetsInOrbit.elementAt (i);
            fleet.setStation (this);
            fFleetsOnSurface.addElement (fleet);
        }

        fFleetsInOrbit = new Vector ();
    }

    public void runBattleSimulation ()
    {
        if ( fFleetsInOrbit.size() > 0 ) {
            orbitStationedFleets ();
            Battle battle = new Battle ();
            battle.addFleets (fFleetsInOrbit);
            while ( battle.getNumberOfGroups() > 1 ) {
                int offender = (int)(Math.random() * 1.0);
                int defender = (offender == 0)? 1 : 0;
                battle.runBattleSimulation (offender, defender);
                Player winner = battle.getWinner ();
                Player loser = battle.getLoser ();
                winner.addMessage ("You won a battle against " + loser.getName() + " at " + getName());
                loser.addMessage ("You lost a battle against " + winner.getName() + " at " + getName());
            }

            Player newOwner = battle.getOwnerOfGroup (0);

            long scrapMetal = battle.getScrapMetal ();
            if ( scrapMetal > 0 ) {
                addMetal (scrapMetal);
                newOwner.addMessage (scrapMetal + " tons of metal fell on " + getName() + " from the battle.");
            }

            if ( newOwner != fOwner ) {
                if ( fOwner != null && fSettlement != null ) {
                    newOwner.addMessage ("You have destroyed " + fOwner.getName() + "'s colony at " + getName());
                    fOwner.addMessage ("You colony at " + getName() + " has been destroyed by " + newOwner.getName());
                    Budget budget = fOwner.getBudget ();
                    BudgetItem item = budget.findBudgetItem (getName());
                    budget.removeBudgetItem (item);
                }
                fSettlement = null;
                fOwner = newOwner;
            }

            fFleetsInOrbit = battle.getGroup (0);
            acceptFleets ();
        }
    }

}



