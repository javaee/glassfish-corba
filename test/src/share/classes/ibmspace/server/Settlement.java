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

//
// A settlement represents the settlement of a planet by a player.
// Each player maitains a list of its current settlements and can
// use these to control and access the planet while settled.
//


public class Settlement implements java.io.Serializable
{
    private Player        fPlayer = null;
    private PlanetImpl    fPlanet = null;

    private Budget        fBudget = null;
    private Terraforming  fTerraforming = null;
    private Mining        fMining = null;
    private Population    fPopulation = null;

    public Settlement (Player player, PlanetImpl planet, long initialSize)
    {
        fPlayer = player;
        fPlanet = planet;

        if ( fPlanet == null ) {
            System.out.println ("fPlanet is null");
            throw new Error ("Settlement.Settlement () - fPlanet is null");
        }

        double temp = fPlayer.getIdealTemp ();

        fTerraforming = new Terraforming (fPlanet, temp);
        fMining = new Mining (fPlanet, fPlayer);
        fBudget = new Budget (fPlanet.getName());
        fBudget.addBudgetItem (new BudgetItem (fTerraforming, 50));
        fBudget.addBudgetItem (new BudgetItem (fMining, 50));

        fPopulation = new Population (fPlanet, initialSize);
    }

    public Player getOwner ()
    {
        return fPlayer;
    }

    public Budget getBudget ()
    {
        return fBudget;
    }

    public Population getPopulation ()
    {
        return fPopulation;
    }

    public long getIncome ()
    {
        if ( fPopulation == null ) System.out.println ("fPopulation is null!!");
        if ( fPlanet == null ) System.out.println ("fPlanet is null!!");
        if ( fPlayer == null ) System.out.println ("fPlayer is null!!");

        return (long)(fPopulation.getIdealIncome() * fPlanet.getSuitabilityFor(fPlayer));
    }

    public void growPopulation ()
    {
        fPopulation.grow (fPlanet.getSuitabilityFor(fPlayer));
    }

}
