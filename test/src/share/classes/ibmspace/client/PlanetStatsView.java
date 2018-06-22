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

// Displays planet statistics.  This is a useful example of text
// scaling, which should be a whole lot easier in 1.2 with
// tranformations, but this is written to work on 1.1.6 or later.

package ibmspace.client;

import java.awt.*;
import javax.swing.*;
import ibmspace.common.Planet;
import ibmspace.common.PlanetView;

public class PlanetStatsView extends JComponent
{
    private String      fName = "Unknown";
    private String      fIncome = "Unknown";
    private String      fPopulation = "Unknown";
    private String      fTemperature = "Unknown";
    private String      fGravity = "Unknown";
    private String      fMetal = "Unknown";

    public PlanetStatsView ()
    {
    }

    public void presentPlanet (PlanetView planet)
    {

        // More info if we have it

        if ( planet != null ) {
            fName = planet.getName ();
            if ( planet.isOwned() ) {
                fIncome = String.valueOf (planet.getIncome());
                fPopulation = String.valueOf (planet.getPopulation());
                fTemperature = String.valueOf (planet.getTemp());
                fGravity = String.valueOf (planet.getGravity());
                fMetal = String.valueOf (planet.getMetal());
            }
        }

        repaint ();
    }

    public void paint (Graphics g)
    {
        update (g);
    }

    public void update (Graphics g)
    {
        Rectangle bounds = getBounds ();

        //bounds.grow (-horzInset,-vertInset);
        int bx = bounds.x;
        int by = bounds.y;
        int bw = bounds.width;
        int bh = bounds.height;

        //
        // Determine and Set Optimal Font Point Size
        //

        int maxHeight = bh / 8;
        int maxWidth  = bw / 2;
        int pointSize = 1;

        for ( int pt = 1; pt < 72; pt ++ ) {
            Font f = new Font ("SansSerif",Font.PLAIN,pt);
            g.setFont (f);
            FontMetrics fm = g.getFontMetrics ();
            int height = fm.getHeight () + fm.getLeading ();
            int width = fm.stringWidth (" Income: ");

            if ( height > maxHeight || width > maxWidth )
                break;

            pointSize = pt;
        }


        //
        // Align Text Fields
        //

        int x, y;
        FontMetrics fm = g.getFontMetrics ();
        g.setColor (Color.black);

        x = maxWidth - fm.stringWidth ("Income:");
        y = 3 * maxHeight;
        g.drawString ("Income:", x, y);
        x = maxWidth + 5;
        g.drawString (fIncome, x, y);

        x = maxWidth - fm.stringWidth ("Pop:");
        y += maxHeight;
        g.drawString ("Pop:", x, y);
        x = maxWidth + 5;
        g.drawString (fPopulation, x, y);

        x = maxWidth - fm.stringWidth ("Temp:");
        y += maxHeight;
        g.drawString ("Temp:", x, y);
        x = maxWidth + 5;
        g.drawString (fTemperature, x, y);

        x = maxWidth - fm.stringWidth ("Gravity:");
        y += maxHeight;
        g.drawString ("Gravity:", x, y);
        x = maxWidth + 5;
        g.drawString (fGravity, x, y);

        x = maxWidth - fm.stringWidth ("Metal:");
        y += maxHeight;
        g.drawString ("Metal:", x, y);
        x = maxWidth + 5;
        g.drawString (fMetal, x, y);

        int planetWidth = fm.stringWidth (fName);
        x = (bw - planetWidth)/2;
        y = (int)(1.5 * maxHeight);
        g.setFont (new Font ("SansSerif",Font.BOLD,pointSize+1));
        g.drawString (fName, x, y);

    }

}
