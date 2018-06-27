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

// Arrow represents an arrow to be drawn between two points.  It
// also has some behavior specific to this game.  Namely, (1) you
// can shorten the head or tail so that the the end points can be
// the centers of planet gifs while the arrows are drawn between
// the planet perimeters, and (2) you can also shorten the tail to
// show progress of a journey (the tail shortens until the fleet
// reaches the destination planet.  Even with these, this is probably
// a generally useful class fo UI work.  The arrow head drawing code
// is not but it's fast.

package ibmspace.client;

import java.awt.*;

public class JourneyArrow extends Arrow
{
    private int       fAvailableRange = 0;
    private Color     fAcceptedColor = Color.cyan;
    private Color     fRejectedColor = Color.red;
    private boolean   fShowDistance = false;
    private int       fDistance;
    private String    fDistanceString;

    public JourneyArrow ()
    {
    }

    public void setAvailableRange (int range)
    {
        fAvailableRange = range;
    }

    public void setAcceptedColor (Color color)
    {
        fAcceptedColor = color;
    }

    public void setRejectedColor (Color color)
    {
        fRejectedColor = color;
    }

    public void setShowDistance (boolean show)
    {
        fShowDistance = show;
    }

    public int getDistance ()
    {
        if ( fCacheBlown ) {
            fDistance = (int)(getLength() / 50 + 0.5);
        }

        return fDistance;
    }

    public void draw (Graphics g)
    {
        int distance = getDistance ();

        if ( fShowDistance ) {
            fDistanceString = String.valueOf (distance);
            setLabel (fDistanceString);
        } else {
            setLabel (null);
        }

        if ( distance <= fAvailableRange ) {
            g.setColor (fAcceptedColor);
        } else {
            g.setColor (fRejectedColor);
        }

        super.draw (g);
    }
  



}
