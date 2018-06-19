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

// A container for LabeledBarControls.  This was done very quick
// and dirty, but would be a useful class if done more generally
// ... maybe later.  Anyway, this class holds a set of bars and
// arranges them vertically or horizontally, depending on the
// orientations of the bars (which need to all be the same, this
// error is not handled so be careful!).  It then also handles
// some group activity semantics kinda' like a radio group would
// have.  This allows me to resize other bars in the group when
// on is resized by the user.


package ibmspace.client;

import java.util.*;
import javax.swing.*;
import java.awt.event.*;
import java.awt.*;

public class BarGroup extends JPanel implements ActionListener
{
    private Vector fBars;
    private Vector fColors;

    public BarGroup()
    {
        fBars = new Vector ();
        fColors = new Vector ();
    }

    public void addBar (LabeledBarControl bar)
    {
        super.add (bar, null);
        fBars.addElement (bar);
        fColors.addElement (bar.getBarColor());
        bar.addActionListener (this);
        adjustBarColor (bar.getBarControl());
    }

    public void removeAll ()
    {
        super.removeAll ();
        fBars.removeAllElements ();
        fColors.removeAllElements ();
    }

    public LabeledBarControl[] getBars ()
    {
        LabeledBarControl[] bars = new LabeledBarControl [fBars.size()];
        for (int i=0; i<fBars.size(); i++) {
            bars[i] = (LabeledBarControl)fBars.elementAt (i);
        }
        return bars;
    }


    public void actionPerformed (ActionEvent event)
    {
        if ( event.getActionCommand () == "User Changed" ) {
            handleUserChanged ((BarControl)event.getSource ());
        }

        if ( event.getActionCommand () == "Percentage Changed" ) {
            handleBarPercentageChanged ((BarControl)event.getSource ());
        }

    }

    protected void handleUserChanged (BarControl bar)
    {
        int numBars = fBars.size();

        // Compute total percentage

        double totalPercentage = 0.0;
        int barsToChange = 0;

        for (int i=0; i<numBars; i++) {
            LabeledBarControl lbc = (LabeledBarControl)fBars.elementAt (i);
            BarControl bc = lbc.getBarControl ();

            double p = bc.getPercentage ();

            if ( bc != bar && p > 0.001 )
                barsToChange++;

            totalPercentage += bc.getPercentage ();
        }

        // Level total percentage to 1.0

        double perBarExcessPercentage = (totalPercentage - 1.0) / barsToChange;

        for (int i=0; i<fBars.size(); i++) {
            LabeledBarControl lbc = (LabeledBarControl)fBars.elementAt (i);
            BarControl bc = lbc.getBarControl ();
            double p = bc.getPercentage ();

            if (bc != bar && p > 0.001) {
                bc.setPercentage (p-perBarExcessPercentage);
            }

        }

    }


    private Color getBarBaseColor (BarControl bar)
    {
        for (int i=0; i<fBars.size(); i++) {
            LabeledBarControl b = (LabeledBarControl)fBars.elementAt (i);
            if ( b.getBarControl() == bar ) {
                return (Color)fColors.elementAt (i);
            }
        }
        return new Color (0,0,0);
    }


    private void adjustBarColor (BarControl bar)
    {
        double p = bar.getPercentage ();
        Color c = getBarBaseColor(bar);
        float[] hsbc = Color.RGBtoHSB(c.getRed(),c.getGreen(),c.getBlue(),null);
        hsbc[2] = (float)Math.sqrt(Math.sqrt(p));
        bar.setColor (Color.getHSBColor(hsbc[0],hsbc[2],hsbc[2]));
    }


    protected void handleBarPercentageChanged (BarControl bar)
    {
        adjustBarColor (bar);
    }



}
