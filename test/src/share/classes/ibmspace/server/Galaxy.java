/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright (c) 1997-2010 Oracle and/or its affiliates. All rights reserved.
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

package ibmspace.server;

import java.awt.Dimension;
import java.awt.Point;
import java.util.Vector;
import ibmspace.common.ID;
import java.io.Serializable;

public class Galaxy implements Serializable
{
    private Dimension       fSize;
    private int             fNumberOfPlanets;
    private Vector          fPlanets;
    private int             fNextHomeWorld = 0;
    private PlanetImpl[]    fHomes = null;

    public Galaxy (int rows, int columns)
    {
        PlanetNames names = new PlanetNames ();
        fPlanets = new Vector ();
        int i = 0;

        for (int r=0; r<rows; r++) {
            for (int c=0; c<columns; c++) {
                double temp = PlanetImpl.RandomTemp ();
                double gravity = PlanetImpl.RandomGravity ();
                long metal = PlanetImpl.RandomMetal ();
                PlanetImpl p = new PlanetImpl (names.getName(), temp, gravity, metal);
                p.setCoordinates (new Point((r+1)*150-50,(c+1)*150-50));
                fPlanets.addElement (p);
            }
        }

        fNumberOfPlanets = rows * columns;
        fSize = new Dimension (rows*100, columns*100);

        fHomes = new PlanetImpl [fNumberOfPlanets];
        for (i=0; i<fNumberOfPlanets; i++) {
            fHomes[i] = null;
        }
    
    }

    public PlanetImpl createHomeWorldFor (Player player)
    {
        int homeIndex = 0;
        do {
            homeIndex = (int)(Math.random() * (fNumberOfPlanets-1));
        } while ( fHomes[homeIndex] != null );

        PlanetImpl home = (PlanetImpl)fPlanets.elementAt (homeIndex);
        home.setTemp (player.getIdealTemp());
        home.setGravity (player.getIdealGravity());
        home.setMetal (20000);
        fHomes[homeIndex] = home;
        return home;
    }

    public int getNumberOfPlanets ()
    {
        return fNumberOfPlanets;
    }

    public Dimension getSize ()
    {
        return fSize;
    }

    public PlanetImpl getPlanet (ID planetID)
    {
        if ( planetID != null ) {
            for (int i=0; i<fNumberOfPlanets; i++) {
                PlanetImpl p = (PlanetImpl)fPlanets.elementAt (i);
                if ( planetID.identifies (p) ) {
                    return p;
                }
            }
        }
        return null;
    }

    public Vector getPlanets ()
    {
        return fPlanets;
    }


}
