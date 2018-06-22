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

// Like PlanetStatsView, this view displays text and scales
// it to fit the given view size.


package ibmspace.client;

import java.awt.*;
import javax.swing.*;

public class TechLevelsPanel extends JComponent
{
    private String fRange = "Range = UNKNOWN";
    private String fSpeed = "Speed = UNKNOWN";
    private String fWeapons = "Weapons = UNKNOWN";
    private String fShields = "Shields = UNKNOWN";
    private String fMini = "Mini = UNKNOWN";

    public TechLevelsPanel ()
    {
    }

    public void setRange (int range)
    {
        fRange = "Range = " + String.valueOf(range);
    }

    public void setSpeed (int speed)
    {
        fSpeed = "Speed = " + String.valueOf(speed);
    }

    public void setWeapons (int weapons)
    {
        fWeapons = "Weapons = " + String.valueOf(weapons);
    }

    public void setShields (int shields)
    {
        fShields = "Shields = " + String.valueOf(shields);
    }

    public void setMini (int mini)
    {
        fMini = "Mini = " + String.valueOf(mini);
    }


    public void paint (Graphics g)
    {
        update (g);
    }

    public void update (Graphics g)
    {
        Rectangle bounds = getBounds ();

        int bx = bounds.x;
        int by = bounds.y;
        int bw = bounds.width;
        int bh = bounds.height;

        //
        // Determine and Set Optimal Font Point Size
        //

        int maxHeight = bh / 7;
        int maxWidth  = bw - 15;
        int pointSize = 1;
        int padding = 0;

        for ( int pt = 1; pt < 72; pt ++ ) {
            Font f = new Font ("SansSerif",Font.PLAIN,pt);
            g.setFont (f);
            FontMetrics fm = g.getFontMetrics ();
            int height = fm.getHeight () + fm.getLeading ();
            int width = fm.stringWidth (fRange);
            width = Math.max(width,fm.stringWidth (fSpeed));
            width = Math.max(width,fm.stringWidth (fWeapons));
            width = Math.max(width,fm.stringWidth (fShields));
            width = Math.max(width,fm.stringWidth (fMini));

            if ( height > maxHeight || width > maxWidth )
                break;

            padding = (maxWidth - width) / 2;
            pointSize = pt;
        }



        //
        // Align Text Fields
        //

        int x, y;
        FontMetrics fm = g.getFontMetrics ();
        g.setColor (Color.black);

        x = padding;
        y = 2 * maxHeight;
        g.drawString (fRange, x, y);
        y += maxHeight;
        g.drawString (fSpeed, x, y);
        y += maxHeight;
        g.drawString (fWeapons, x, y);
        y += maxHeight;
        g.drawString (fShields, x, y);
        y += maxHeight;
        g.drawString (fMini, x, y);
    }

}
