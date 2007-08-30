/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1999-2007 Sun Microsystems, Inc. All rights reserved.
 *
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common Development
 * and Distribution License("CDDL") (collectively, the "License").  You
 * may not use this file except in compliance with the License. You can obtain
 * a copy of the License at https://glassfish.dev.java.net/public/CDDL+GPL.html
 * or glassfish/bootstrap/legal/LICENSE.txt.  See the License for the specific
 * language governing permissions and limitations under the License.
 *
 * When distributing the software, include this License Header Notice in each
 * file and include the License file at glassfish/bootstrap/legal/LICENSE.txt.
 * Sun designates this particular file as subject to the "Classpath" exception
 * as provided by Sun in the GPL Version 2 section of the License file that
 * accompanied this code.  If applicable, add the following below the License
 * Header, with the fields enclosed by brackets [] replaced by your own
 * identifying information: "Portions Copyrighted [year]
 * [name of copyright owner]"
 *
 * Contributor(s):
 *
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
/* @(#)PieControl.java	1.3 99/06/07 */
/*
 * Licensed Materials - Property of IBM
 * RMI-IIOP v1.0
 * Copyright IBM Corp. 1998 1999  All Rights Reserved
 *
 * US Government Users Restricted Rights - Use, duplication or
 * disclosure restricted by GSA ADP Schedule Contract with IBM Corp.
 */

//----------------------------------------------------------------------------
// Change History:
//
// 9/98 created  rtw
//----------------------------------------------------------------------------


// An interactive pie control.


package ibmspace.client;

import java.awt.*;
import java.awt.event.*;
import javax.swing.*;

public  class PieControl extends JComponent implements MouseMotionListener

{
    //
    // Data
    //

    private double  fFirstPercentage = 1.0;        // between 0.0 and 1.0
    private double  fSecondPercentage = 0;         // between 0.0 and 1.0
    private Color   fFirstColor = Color.green;
    private Color   fSecondColor = Color.blue;

    //
    // Constructors
    //

    public PieControl()
    {
	addMouseMotionListener (this);
    }

    //
    // Getters
    //

    public Color getFirstColor ()
    {
	return fFirstColor;
    }

    public Color getSecondColor ()
    {
	return fSecondColor;
    }

    public double getFirstPercentage ()
    {
	return fFirstPercentage;
    }

    public double getSecondPercentage ()
    {
	return fSecondPercentage;
    }

    //
    // Setters
    //

    public void setFirstColor (Color firstColor)
    {
	fFirstColor = firstColor;
	repaint ();
    }

    public void setSecondColor (Color secondColor)
    {
	fSecondColor = secondColor;
	repaint ();
    }

    public void setFirstPercentage (double firstPercentage)
    {
	firstPercentage = Math.max (firstPercentage, 0.0);
	firstPercentage = Math.min (firstPercentage, 1.0);

	fFirstPercentage = firstPercentage;
	fSecondPercentage = 1.0 - fFirstPercentage;
    }

    public void setSecondPercentage (double secondPercentage)
    {
	secondPercentage = Math.max (secondPercentage, 0.0);
	secondPercentage = Math.min (secondPercentage, 1.0);

	fSecondPercentage = secondPercentage;
	fFirstPercentage = 1.0 - fSecondPercentage;
    }

    //
    // Painting
    //

    public void paint (Graphics g)
    {
	update (g);
    }

    public void update (Graphics g)
    {
	Insets insets = getInsets ();
	int x = insets.left; //bounds.x;
	int y = insets.top; //bounds.y;
	int width = getSize().width - insets.left - insets.right;
	int height = getSize().height - insets.top - insets.bottom;

	int tDegrees = (int)(fFirstPercentage * 360);
	int mDegrees = (int)(fSecondPercentage * 360);

	// First Piece of Pie
	g.setColor (fFirstColor);
	g.fillArc (x,y,width,height, 89-mDegrees, -tDegrees );

	// Second Piece of Pie
	g.setColor (fSecondColor);
	g.fillArc (x,y,width,height, 89, -mDegrees);

	// Pie Outline
	g.setColor (Color.black);
	g.drawOval (x,y,width,height);
    }

    //
    // Mouse Input Handling
    //

    public void mouseDragged (MouseEvent e)
    {
	fSecondPercentage = computePercentage (e.getPoint());
	fFirstPercentage = 1 - fSecondPercentage;
	repaint();
    }

    public void mouseMoved (MouseEvent e)
    {
    }


    public double computePercentage (Point p)
    {
	Rectangle bounds = getBounds ();
	int vertInset = bounds.height / 20;
	int horzInset = bounds.width / 20;

	bounds.grow (-horzInset,-vertInset);
	int x = bounds.x;
	int y = bounds.y;
	int width = bounds.width;
	int height = bounds.height;

	Point center = new Point(x+width/2,y+height/2);

	if ( p.x > center.x ) {
	    if ( p.y < center.y ) {
		// first quadrant 0 - 98

		double o = (double)(center.y - p.y);
		double a = (double)(p.x - center.x);
		double h = Math.sqrt(o*o + a*a);
		double sine = o/h;

		return (0.25 - (Math.asin(sine) * 0.5) / Math.PI);
        
	    } else {
		// second quadrant 90 - 179

		double o = (double)(p.y - center.y);
		double a = (double)(p.x - center.x);
		double h = Math.sqrt(o*o + a*a);
		double sine = o/h;

		return ((Math.asin(sine) * 0.5) / Math.PI) + 0.25;
	    }
	} else {
	    if ( p.y > center.y ) {
		// third quadrant 180 - 269

		double o = (double)(p.y - center.y);
		double a = (double)(center.x - p.x);
		double h = Math.sqrt(o*o + a*a);
		double sine = o/h;

		return (0.25 - (Math.asin(sine) * 0.5) / Math.PI) + 0.50;
	    } else {
		// fourth quadrant 270 - 359

		double o = (double)(center.y - p.y);
		double a = (double)(center.x - p.x);
		double h = Math.sqrt(o*o + a*a);
		double sine = o/h;

		return ((Math.asin(sine) * 0.5) / Math.PI) + 0.75;
	    }
	}
    }


}
