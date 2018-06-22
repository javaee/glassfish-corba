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

package ibmspace.server;

import ibmspace.common.ID;
import ibmspace.common.Journey;
import ibmspace.common.Fleet;
import ibmspace.common.Planet;


public class JourneyImpl implements Journey, java.io.Serializable
{
    //
    // Private Data Members
    //

    private ID          fID;
    private PlanetImpl  fOrigin;
    private PlanetImpl  fDestination;
    private FleetImpl   fFleet;
    private int         fTotalDistance;
    private int         fDistanceTraveled;
    private boolean     fIsComplete;

  //
  // Constructor
  //

    public JourneyImpl (FleetImpl fleet, PlanetImpl origin, PlanetImpl destination)
    {
        fID = new ID ();
        fFleet = fleet;
        fOrigin = origin;
        fDestination = destination;
        fTotalDistance = fOrigin.distanceTo (fDestination);
        fDistanceTraveled = 0;
        fIsComplete = false;
    }

    //
    // Journey Interface Methods
    //

    public ID getID ()
    {
        return fID;
    }

    public Planet getOrigin ()
    {
        return fOrigin;
    }

    public Planet getDestination ()
    {
        return fDestination;
    }

    public double getPercentComplete ()
    {
        return (double)fDistanceTraveled/(double)fTotalDistance;
    }

    //
    // JourneyImpl Methods
    //

    public FleetImpl getFleet ()
    {
        return fFleet;
    }

    public int getTotalDistance ()
    {
        return fTotalDistance;
    }

    public int getDistanceTraveled ()
    {
        return fDistanceTraveled;
    }

    public int getRemainingDistance ()
    {
        return fTotalDistance = fDistanceTraveled;
    }

    public boolean isComplete ()
    {
        return fIsComplete;
    }

    //
    // Turn Taking
    //

    public void moveFleet ()
    {
        if ( fDistanceTraveled == 0 ) {
            fOrigin.removeFleet (fFleet);
            fFleet.setStation (null);
            fFleet.setJourney (this);
        }

        fDistanceTraveled += fFleet.getSpeed ();
        fFleet.move (fDistanceTraveled);
    
        if ( fDistanceTraveled >= fTotalDistance ) {
            fIsComplete = true;
            fDestination.acceptOrbit (fFleet);
            fFleet.setJourney (null);
        }
    }

}
