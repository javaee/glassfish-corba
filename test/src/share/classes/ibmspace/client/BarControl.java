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

// Implements an unlabeled bar control that can be positioned
// either vertically or horizontally.  Use LabeledBarControl
// to get this with a label.


package ibmspace.client;

import java.util.*;
import java.awt.*;
import java.awt.event.*;
import javax.swing.*;

public class BarControl extends JComponent implements MouseMotionListener
{
    static final byte VERTICAL = 0;
    static final byte HORIZONTAL = 1;

    //
    // Data
    //

    private double    fPercentage;      // between 0.0 and 1.0
    private Color     fColor, fLightColor, fDarkColor;
    private byte      fOrientation;
    private Dimension fSize = new Dimension ();

    private ActionSource  fActionSource;


    //
    // Constructors
    //

    public BarControl()
    {
        addMouseMotionListener (this);
        fColor = Color.red;
        fPercentage = 0.0;
        fOrientation = HORIZONTAL;
        fSize = super.getSize ();
        fActionSource = new ActionSource ();
    }

    public BarControl (byte orientation)
    {
        addMouseMotionListener (this);
        fColor = Color.red;
        fPercentage = 0.0;
        fOrientation = orientation;
        fSize = super.getSize ();
        fActionSource = new ActionSource ();
    }

    public BarControl(Color c)
    {
        addMouseMotionListener (this);
        fColor = c;
        fPercentage = 0.0;
        fOrientation = HORIZONTAL;
        fSize = super.getSize ();
        fActionSource = new ActionSource ();
    }

    public BarControl(Color c, byte orientation)
    {
        addMouseMotionListener (this);
        fColor = c;
        fPercentage = 0.0;
        fOrientation = orientation;
        fSize = super.getSize ();
        fActionSource = new ActionSource ();
    }

    public void addActionListener (ActionListener listener)
    {
        fActionSource.addActionListener (listener);
    }

    public void removeActionListener (ActionListener listener)
    {
        fActionSource.removeActionListener (listener);
    }

  
    //
    // Setters
    //
  
    public void setColor (Color c)
    {
        fColor = c;

        int r = c.getRed ();
        int g = c.getGreen ();
        int b = c.getBlue ();

        fLightColor = new Color ((r+255)/2,(g+255)/2,(b+255)/2);
        fDarkColor = new Color (r/2,g/2,b/2);
        repaint ();
    }

    public void setPercentage (double p)
    {
        fPercentage = p;
        fActionSource.notifyListeners (this, "Percentage Changed");
        repaint ();
    }

    //
    // Getters
    //

    public Color getColor ()
    {
        return fColor;
    }

    public double getPercentage ()
    {
        return fPercentage;
    }


    //
    // Painting
    //

    public void paint (Graphics g)
    {
        update (g);
    }


    public double f (double value)
    {
        return Math.sqrt(value);
    }

    public void update (Graphics g)
    {
        Insets insets = getInsets ();
        Dimension size = getSize ();
        int x = insets.left;
        int y = insets.top;
        int width = size.width - insets.left - insets.right;
        int height = size.height - insets.top - insets.bottom;

        Rectangle bounds;


        switch ( fOrientation )
            {
            case HORIZONTAL:
                width = (int)(width*f(fPercentage));
                break;
            case VERTICAL:
                int newHeight = (int)(height*f(fPercentage));
                y = height - newHeight;
                height = newHeight;
                break;
            default:
                return;
            }

        int xi = x + 1;
        int yi = y + 1;
        int xe = x + width;
        int ye = y + height;
        int xei = xe - 1;
        int yei = ye - 1;

        // Bar Fill

        g.setColor (fColor);
        g.fillRect (x,y,width,height);

        // Bar Outline

        g.setColor (fLightColor);
        g.drawLine (x,y,xe,y);
        g.drawLine (x,yi,xei,yi);
        g.drawLine (x,y,x,ye);
        g.drawLine (xi,y,xi,yei);

        g.setColor (fDarkColor);
        g.drawLine (xe,ye,x,ye);
        g.drawLine (xe,ye,xe,y);
        g.drawLine (xei,yei,xi,yei);
        g.drawLine (xei,yei,xei,yi);
    
    }

    //
    // Input Handling
    //

    public void mouseDragged (MouseEvent e)
    {
        fPercentage = computePercentage (e.getPoint());
        fActionSource.notifyListeners (this, "Percentage Changed");
        fActionSource.notifyListeners (this, "User Changed");
        repaint();
    }

    public void mouseMoved (MouseEvent e)
    {
    }

    public double computePercentage (Point p)
    {
        Insets insets = getInsets ();
        double percentage = 0.0;

        if ( fOrientation == HORIZONTAL ) {
            int x = insets.left; 
            int width = getSize().width - insets.left - insets.right;
            width = Math.max(width,0);
            if ( p.x > x )
                percentage = Math.min((double)p.x - x, width)/width;
            else
                percentage = 0.0;
        }

        if ( fOrientation == VERTICAL ) {
            int y = insets.top;
            int height = getSize().height - insets.top - insets.bottom;
            height = Math.max(height,0);
            if ( p.y < (y+height) )
                percentage = Math.min((double)y + height - p.y, height)/height;
            else
                percentage = 0.0;
        }

        return Math.pow(percentage,2);
    }



  
}
