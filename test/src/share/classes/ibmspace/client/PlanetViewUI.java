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

// PlanetView is really a wrapper for planet gifs that is used
// by GalaxyView. 


package ibmspace.client;

import java.awt.*;
import ibmspace.common.Planet;
import ibmspace.common.PlanetView;

public class PlanetViewUI
{
    private static Image    fgUnknownPlanetImage = null;
    private static Image    fgGoodPlanetImage = null;
    private static Image    fgBadPlanetImage = null;
    private static Image    fgSelectionImage = null;

    private PlanetView      fPlanetView;
    private Image           fIcon;
    private Image           fSelection;

    private boolean         fHasSatelites = false;


  
    public static void setUnknownPlanetImage (Image image)
    {
        fgUnknownPlanetImage = image;
    }

    public static void setGoodPlanetImage (Image image)
    {
        fgGoodPlanetImage = image;
    }

    public static void setBadPlanetImage (Image image)
    {
        fgBadPlanetImage = image;
    }

    public static void setSelectionImage (Image image)
    {
        fgSelectionImage = image;
    }

    public PlanetViewUI (PlanetView planet)
    {
        fPlanetView = planet;
        Button c = new Button ();
        fHasSatelites = planet.hasSatelites ();
        double suitability = planet.getSuitability ();

        //System.out.println ("Suitability: " + suitability);

        if ( suitability == -1 ) {
            fIcon = fgUnknownPlanetImage;
        } else if ( suitability > 0.5 ) {
            fIcon = fgGoodPlanetImage;
        } else {
            fIcon = fgBadPlanetImage;
        }
    
        fSelection = fgSelectionImage;
    }

    public String getName ()
    {
        return fPlanetView.getName ();
    }

    public Rectangle getBounds ()
    {
        Point location = fPlanetView.getCoordinates();
        return new Rectangle (location.x-19, location.y-19, 38, 38);
    }

    public void draw (Graphics g, boolean drawPlanet, boolean drawName, boolean drawSelection)
    {
        Point location = fPlanetView.getCoordinates ();

        if ( drawPlanet ) {
            if ( drawSelection ) {
                g.drawImage (fSelection, location.x-30, location.y-30, 60, 60, null);
            }
            g.drawImage (fIcon, location.x-19, location.y-19, 38, 38, null);

            if ( fHasSatelites ) {
                g.setColor (Color.white);
                g.drawOval (location.x-22, location.y-21, 43, 43);
            }
        }

        if ( drawName ) {
            g.setColor (Color.white);
            g.setFont (new Font ("SansSerif",Font.PLAIN,10));
            FontMetrics fm = g.getFontMetrics ();
            int width = fm.stringWidth (getName());
            int height = fm.getHeight () + fm.getLeading ();
            int x = location.x - (width/2);
            int y = location.y + 20 + height;
            g.drawString (getName(), x, y);
        }

    }

}
