/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright (c) 1997-2018 Oracle and/or its affiliates. All rights reserved.
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

// Like PlanetStatsView, this view displays text and scales it
// to fit the given space.

package ibmspace.client;

import java.awt.*;
import javax.swing.*;

public class ResourceLevelsPanel extends JComponent
{
    private String      fSavings = "";
    private String      fMetal = "";
    private String      fIncome = "";
    private String      fIIOP = "";

    public ResourceLevelsPanel ()
    {
    }

    public String valueOf (long value)
    {
        String units = "";

        if ( value > 1000000 ) {
            value = value / 1000;
            if ( value > 1000000 ) {
                value = value / 1000;
                units = "M";
            } else {
                units = "K";
            }
        }
        return String.valueOf(value) + units;
    }


    public void setShipSavings (long savings)
    {
        fSavings = valueOf(savings);
    }

    public void setShipMetal (long metal)
    {
        fMetal = valueOf(metal);
    }

    public void setIncome (long income)
    {
        fIncome = valueOf(income);
    }

    public void setIIOPCalls (long calls)
    {
        fIIOP = valueOf(calls);
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

        int maxHeight = bh / 3;
        int maxWidth  = bw / 2;
        int pointSize = 1;
        int padding = 0;

        for ( int pt = 1; pt < 72; pt ++ ) {
            Font f = new Font ("SansSerif",Font.PLAIN,pt);
            g.setFont (f);
            FontMetrics fm = g.getFontMetrics ();
            int height = fm.getHeight () + fm.getLeading ();
            int width = fm.stringWidth ("Savings: " + fSavings);
            width = Math.max(width,fm.stringWidth ("Metal: " + fMetal));
            width = Math.max(width,fm.stringWidth ("Income: " + fIncome));
            width = Math.max(width,fm.stringWidth ("IIOP: " + fIIOP));

            if ( height > maxHeight || width > maxWidth )
                break;

            padding = (maxWidth - width) / 4;
            pointSize = pt;
        }

        //
        // Align Text Fields
        //

        int x, y;
        FontMetrics fm = g.getFontMetrics ();
        g.setColor (Color.black);

        int indent = fm.stringWidth ("Savings: ");

        x = padding;
        y = (int)(1.5 * (double)maxHeight);
        g.drawString ("Savings: ", x, y);
        x += indent;
        g.drawString (fSavings, x, y);

        x = maxWidth + padding;
        g.drawString ("Income: ", x, y);
        x += indent;
        g.drawString (fIncome, x, y);

        x = padding;
        y += maxHeight;
        g.drawString ("Metal: ", x, y);
        x += indent;
        g.drawString (fMetal, x, y);

        x = maxWidth + padding;
        g.setColor (Color.red);
        g.drawString ("IIOP: ", x, y);
        x += indent;
        g.drawString (fIIOP, x, y);

    }



}
