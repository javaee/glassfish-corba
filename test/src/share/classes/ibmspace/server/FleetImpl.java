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
// 9/98     rtw   created
// 1/29/99  rtw   changed to inherit from ibmspace.common.Fleet
//----------------------------------------------------------------------------


package ibmspace.server;

import ibmspace.common.ShipDesign;
import ibmspace.common.ID;
import ibmspace.common.Fleet;
import ibmspace.common.Planet;
import ibmspace.common.Journey;


public class FleetImpl implements Fleet, java.io.Serializable
{
    private ID          fID;
    private ShipDesign  fDesign;
    private int         fId;
    private int         fShipsInFleet;
    private Player      fOwner;
    private int         fDamageLevel;
    private int         fFuelLevel;
    private PlanetImpl  fStation;
    private JourneyImpl fJourney;

    public FleetImpl (ShipDesign design, int shipsInFleet, Player owner)
    {
	fID = new ID ();
	fDesign = design;
	fShipsInFleet = shipsInFleet;
	fOwner = owner;
	fStation = null;
	fJourney = null;
	replentishFuel ();
    }

    //
    // Fleet Interface Methods
    //

    public ID getID ()
    {
	return fID;
    }

    public ShipDesign getDesign ()
    {
	return fDesign;
    }

    public int getNumberInFleet ()
    {
	return fShipsInFleet;
    }

    public int getMaximumRange ()
    {
	return fDesign.getTechProfile().getRange ();
    }

    public int getCurrentRange ()
    {
	if ( isSatelite() )
	    return 0;
	else
	    return fFuelLevel;
    }
  
    public boolean isOnJourney ()
    {
	return ( fJourney == null ? false : true );
    }

    public String toString ()
    {
	String s = String.valueOf (fShipsInFleet);
	s += " " + fDesign.getName ();

	switch ( fDesign.getType () ) {
	case ShipDesign.COLONY_SHIP:
	    s += " Colony Ship ";
	    break;
	case ShipDesign.SCOUT:
	    s += " Scout ";
	    break;
	case ShipDesign.FIGHTER:
	    s += " Fighter ";
	    break;
	case ShipDesign.SATELITE:
	    s += " Satelite ";
	    break;
	}

	s += fDesign.getTechProfile().toString();
	s += " " + String.valueOf (getCurrentRange());

	if ( isOnJourney() ) {
	    s += " *";
	}

	return s;
    }


    //
    // FleetImpl Methods
    //

    public Player getOwner ()
    {
	return fOwner;
    }

    public PlanetImpl getStation ()
    {
	return fStation;
    }

    public JourneyImpl getJourney ()
    {
	return fJourney;
    }

    public void setStation (PlanetImpl station)
    {
	fStation = station;
    }

    public void setJourney (JourneyImpl journey)
    {
	fJourney = journey;
    }

    public int getShipsInFleet ()
    {
	return fShipsInFleet;
    }

    public int getSpeed ()
    {
	return fDesign.getTechProfile().getSpeed ();
    }

    public boolean isColonyShip ()
    {
	if ( fDesign.getType() == ShipDesign.COLONY_SHIP )
	    return true;
	else
	    return false;
    }

    public boolean isFighter ()
    {
	if ( fDesign.getType() == ShipDesign.FIGHTER )
	    return true;
	else
	    return false;
    }

    public boolean isSatelite ()
    {
	if ( fDesign.getType() == ShipDesign.SATELITE )
	    return true;
	else
	    return false;
    }

    public void move (int distance)
    {
	fFuelLevel -= distance;
	fFuelLevel = Math.max (fFuelLevel, 0);
    }

    public void replentishFuel ()
    {
	fFuelLevel = getMaximumRange ();
    }

    public long getScrapMetal ()
    {
	return fDesign.getScrapMetalPerShip() * fShipsInFleet;
    }

    public int getStrenth ()
    {
	int s = fDesign.getTechProfile().getWeapons() * fShipsInFleet;
	if ( isFighter() ) s = (int)(s * 1.5);
	return s;
    }

    public int getResistance ()
    {
	int r = fDesign.getTechProfile().getShields() * fShipsInFleet;
	if ( isSatelite() ) r = (int)(r * 1.5);
	return r;
    }

    public int getDamageLevel ()
    {
	return fDamageLevel;
    }

}
