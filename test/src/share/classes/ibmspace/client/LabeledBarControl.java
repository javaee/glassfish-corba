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

// A container for BarControls that adds labels.

package ibmspace.client;

import java.awt.*;
import java.util.*;
import java.text.*;
import javax.swing.*;
import javax.swing.border.*;
import java.awt.event.*;


public class LabeledBarControl extends JPanel
{
    private JLabel      fLabel;
    private BarControl  fBar;

    public LabeledBarControl(String text)
    {
        BorderLayout layout = new BorderLayout (5,5);

        fLabel = new JLabel (text);
        fLabel.setPreferredSize (new Dimension(80,20));
        fLabel.setMinimumSize (new Dimension(80,20));
        fLabel.setMaximumSize (new Dimension(80,20));

        fBar = new BarControl (Color.blue);
        fBar.setBorder (new EmptyBorder (3,2,3,2));

        setPreferredSize (new Dimension(50,20));
        setMinimumSize (new Dimension(500,20));
        setMaximumSize (new Dimension(1000,20));

        setLayout (layout);
        add (fLabel, "West");
        add (fBar, "Center");
    }

    public LabeledBarControl(String text, byte orientation)
    {
        BorderLayout layout = new BorderLayout (5,5);

        fLabel = new JLabel (text);
        //fLabel.setBorder (new EtchedBorder());

        fBar = new BarControl (Color.blue, orientation);

        setLayout (layout);

        add (fBar, "Center");

        switch ( orientation )
            {
            case BarControl.HORIZONTAL:
                fLabel.setPreferredSize (new Dimension(80,20));
                fLabel.setMinimumSize (new Dimension(80,20));
                fBar.setBorder (new EmptyBorder (3,2,3,2));
                add (fLabel, "West");
                break;
            case BarControl.VERTICAL:
                fLabel.setFont (new Font ("SansSerif", Font.PLAIN, 10));
                fLabel.setPreferredSize (new Dimension(40,20));
                fLabel.setMinimumSize (new Dimension(40,20));
                fLabel.setMaximumSize (new Dimension(40,20));
                fBar.setBorder (new EmptyBorder (2,12,2,12));
                fLabel.setHorizontalAlignment (JLabel.CENTER);
                fBar.setSize (new Dimension(20,80));
                fBar.setMaximumSize (new Dimension(20,100));
                fBar.setMinimumSize (new Dimension(20,50));
                fBar.setPreferredSize (new Dimension(20,80));
                add (fLabel, "South");
                break;
            default:
                break;
            }

    }

    public void addActionListener (ActionListener listener)
    {
        fBar.addActionListener (listener);
    }

    public void removeActionListener (ActionListener listener)
    {
        fBar.removeActionListener (listener);
    }

    public void setLabelText (String labelText)
    {
        fLabel.setText (labelText);
    }

    public String getLabelText ()
    {
        return fLabel.getText ();
    }

    public void setBarColor (Color color)
    {
        fBar.setColor (color);
    }

    public Color getBarColor ()
    {
        return fBar.getColor ();
    }

    public BarControl getBarControl ()
    {
        return fBar;
    }


    public void setPercentage (double percentage)
    {
        fBar.setPercentage (percentage);
    }

    public double getPercentage ()
    {
        return fBar.getPercentage ();
    }

}
