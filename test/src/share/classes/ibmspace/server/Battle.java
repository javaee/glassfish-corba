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

import java.util.Vector;
import java.io.Serializable;

public class Battle implements Serializable
{
    private Vector          fGroups;
    private Player          fWinner = null;
    private Player          fLoser = null;
    private long            fScrapMetal = 0;

    public Battle ()
    {
        fGroups = new Vector ();
    }

    public Player getWinner ()
    {
        return fWinner;
    }

    public Player getLoser ()
    {
        return fLoser;
    }

    public long getScrapMetal ()
    {
        return fScrapMetal;
    }

    public void addFleet (FleetImpl fleet)
    {
        int i = findGroupFor (fleet);
        Vector group = getGroup (i);
        group.addElement (fleet);
    }

    public void addFleets (Vector fleets)
    {
        for (int i=0; i<fleets.size(); i++) {
            FleetImpl fleet = (FleetImpl)fleets.elementAt (i);
            addFleet (fleet);
        }
    }

    public void runBattleSimulation (int offender, int defender)
    {
        int os = getStrengthOfGroup (offender);
        int or = getResistanceOfGroup (offender);
        int ds = getStrengthOfGroup (defender);
        int dr = getResistanceOfGroup (defender);

        if ( getStrengthOfGroup(offender) > getResistanceOfGroup(defender) ) {
            fWinner = getOwnerOfGroup (offender);
            fLoser = getOwnerOfGroup (defender);
            eliminateGroup (defender);
        } else if ( getStrengthOfGroup (defender) > getResistanceOfGroup (offender) ) {
            fWinner = getOwnerOfGroup (defender);
            fLoser = getOwnerOfGroup (offender);
            eliminateGroup (offender);
        } else {
            System.out.println ("random call");
            int coin = (int)(Math.random() * 1.0);
            if ( coin == 0 ) {
                fWinner = getOwnerOfGroup (defender);
                fLoser = getOwnerOfGroup (offender);
                eliminateGroup (offender);
            } else {
                fWinner = getOwnerOfGroup (offender);
                fLoser = getOwnerOfGroup (defender);
                eliminateGroup (defender);
            }
        }
    }

    public int getNumberOfGroups ()
    {
        return fGroups.size ();
    }

    public int getStrengthOfGroup (int index)
    {
        int strength = 0;
        Vector group = getGroup (index);
        for (int i=0; i<group.size(); i++) {
            FleetImpl fleet = (FleetImpl)group.elementAt (i);
            strength += fleet.getStrenth ();
        }
        return strength;
    }

    public int getResistanceOfGroup (int index)
    {
        int resistance = 0;
        Vector group = getGroup (index);
        for (int i=0; i<group.size(); i++) {
            FleetImpl fleet = (FleetImpl)group.elementAt (i);
            resistance += fleet.getResistance ();
        }
        return resistance;
    }

    public Player getOwnerOfGroup (int i)
    {
        Vector group = (Vector)fGroups.elementAt (i);
        FleetImpl fleet = (FleetImpl)group.elementAt (0);
        return fleet.getOwner ();
    }

    public Vector getGroup (int i)
    {
        Vector group = (Vector)fGroups.elementAt (i);
        return group;
    }


    public int findGroupFor (FleetImpl fleet)
    {
        // Look for existing group

        for (int i=0; i<getNumberOfGroups(); i++) {
            Player owner = getOwnerOfGroup (i);
            if ( owner == fleet.getOwner() ) {
                return i;
            }
        }

        // Not found so create new group

        Vector group = new Vector ();
        fGroups.addElement (group);
        return getNumberOfGroups () - 1;

    }

    public void eliminateGroup (int index)
    {
        Vector group = (Vector)fGroups.elementAt (index);
        for (int i=0; i<group.size(); i++) {
            FleetImpl fleet = (FleetImpl)group.elementAt (i);
            fScrapMetal += fleet.getScrapMetal ();
        }
        fGroups.removeElement (group);
    }

}
